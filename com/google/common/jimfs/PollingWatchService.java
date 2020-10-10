package com.google.common.jimfs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.Watchable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

final class PollingWatchService
  extends AbstractWatchService
{
  private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setNameFormat("com.google.common.jimfs.PollingWatchService-thread-%d").setDaemon(true).build();
  private final ScheduledExecutorService pollingService = Executors.newSingleThreadScheduledExecutor(THREAD_FACTORY);
  private final ConcurrentMap<AbstractWatchService.Key, Snapshot> snapshots = new ConcurrentHashMap();
  private final FileSystemView view;
  private final PathService pathService;
  private final FileSystemState fileSystemState;
  @VisibleForTesting
  final long interval;
  @VisibleForTesting
  final TimeUnit timeUnit;
  private ScheduledFuture<?> pollingFuture;
  
  PollingWatchService(FileSystemView view, PathService pathService, FileSystemState fileSystemState, long interval, TimeUnit timeUnit)
  {
    this.view = ((FileSystemView)Preconditions.checkNotNull(view));
    this.pathService = ((PathService)Preconditions.checkNotNull(pathService));
    this.fileSystemState = ((FileSystemState)Preconditions.checkNotNull(fileSystemState));
    
    Preconditions.checkArgument(interval >= 0L, "interval (%s) may not be negative", new Object[] { Long.valueOf(interval) });
    this.interval = interval;
    this.timeUnit = ((TimeUnit)Preconditions.checkNotNull(timeUnit));
    
    fileSystemState.register(this);
  }
  
  public AbstractWatchService.Key register(Watchable watchable, Iterable<? extends WatchEvent.Kind<?>> eventTypes)
    throws IOException
  {
    JimfsPath path = checkWatchable(watchable);
    
    AbstractWatchService.Key key = super.register(path, eventTypes);
    
    Snapshot snapshot = takeSnapshot(path);
    synchronized (this)
    {
      this.snapshots.put(key, snapshot);
      if (this.pollingFuture == null) {
        startPolling();
      }
    }
    return key;
  }
  
  private JimfsPath checkWatchable(Watchable watchable)
  {
    if ((!(watchable instanceof JimfsPath)) || (!isSameFileSystem((Path)watchable))) {
      throw new IllegalArgumentException("watchable (" + watchable + ") must be a Path " + "associated with the same file system as this watch service");
    }
    return (JimfsPath)watchable;
  }
  
  private boolean isSameFileSystem(Path path)
  {
    return ((JimfsFileSystem)path.getFileSystem()).getDefaultView() == this.view;
  }
  
  @VisibleForTesting
  synchronized boolean isPolling()
  {
    return this.pollingFuture != null;
  }
  
  public synchronized void cancelled(AbstractWatchService.Key key)
  {
    this.snapshots.remove(key);
    if (this.snapshots.isEmpty()) {
      stopPolling();
    }
  }
  
  public void close()
  {
    super.close();
    synchronized (this)
    {
      for (AbstractWatchService.Key key : this.snapshots.keySet()) {
        key.cancel();
      }
      this.pollingService.shutdown();
      this.fileSystemState.unregister(this);
    }
  }
  
  private void startPolling()
  {
    this.pollingFuture = this.pollingService.scheduleAtFixedRate(this.pollingTask, this.interval, this.interval, this.timeUnit);
  }
  
  private void stopPolling()
  {
    this.pollingFuture.cancel(false);
    this.pollingFuture = null;
  }
  
  private final Runnable pollingTask = new Runnable()
  {
    public void run()
    {
      synchronized (PollingWatchService.this)
      {
        for (Map.Entry<AbstractWatchService.Key, PollingWatchService.Snapshot> entry : PollingWatchService.this.snapshots.entrySet())
        {
          AbstractWatchService.Key key = (AbstractWatchService.Key)entry.getKey();
          PollingWatchService.Snapshot previousSnapshot = (PollingWatchService.Snapshot)entry.getValue();
          
          JimfsPath path = (JimfsPath)key.watchable();
          try
          {
            PollingWatchService.Snapshot newSnapshot = PollingWatchService.this.takeSnapshot(path);
            boolean posted = previousSnapshot.postChanges(newSnapshot, key);
            entry.setValue(newSnapshot);
            if (posted) {
              key.signal();
            }
          }
          catch (IOException e)
          {
            key.cancel();
          }
        }
      }
    }
  };
  
  private Snapshot takeSnapshot(JimfsPath path)
    throws IOException
  {
    return new Snapshot(this.view.snapshotModifiedTimes(path));
  }
  
  private final class Snapshot
  {
    private final ImmutableMap<Name, Long> modifiedTimes;
    
    Snapshot()
    {
      this.modifiedTimes = ImmutableMap.copyOf(modifiedTimes);
    }
    
    boolean postChanges(Snapshot newState, AbstractWatchService.Key key)
    {
      boolean changesPosted = false;
      if (key.subscribesTo(StandardWatchEventKinds.ENTRY_CREATE))
      {
        Set<Name> created = Sets.difference(newState.modifiedTimes.keySet(), this.modifiedTimes.keySet());
        for (Name name : created)
        {
          key.post(new AbstractWatchService.Event(StandardWatchEventKinds.ENTRY_CREATE, 1, PollingWatchService.this.pathService.createFileName(name)));
          changesPosted = true;
        }
      }
      if (key.subscribesTo(StandardWatchEventKinds.ENTRY_DELETE))
      {
        Set<Name> deleted = Sets.difference(this.modifiedTimes.keySet(), newState.modifiedTimes.keySet());
        for (Name name : deleted)
        {
          key.post(new AbstractWatchService.Event(StandardWatchEventKinds.ENTRY_DELETE, 1, PollingWatchService.this.pathService.createFileName(name)));
          changesPosted = true;
        }
      }
      if (key.subscribesTo(StandardWatchEventKinds.ENTRY_MODIFY)) {
        for (Map.Entry<Name, Long> entry : this.modifiedTimes.entrySet())
        {
          Name name = (Name)entry.getKey();
          Long modifiedTime = (Long)entry.getValue();
          
          Long newModifiedTime = (Long)newState.modifiedTimes.get(name);
          if ((newModifiedTime != null) && (!modifiedTime.equals(newModifiedTime)))
          {
            key.post(new AbstractWatchService.Event(StandardWatchEventKinds.ENTRY_MODIFY, 1, PollingWatchService.this.pathService.createFileName(name)));
            changesPosted = true;
          }
        }
      }
      return changesPosted;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\PollingWatchService.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */