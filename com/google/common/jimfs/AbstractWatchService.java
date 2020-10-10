package com.google.common.jimfs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.Watchable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;

abstract class AbstractWatchService
  implements WatchService
{
  private final BlockingQueue<WatchKey> queue;
  private final WatchKey poison;
  private final AtomicBoolean open;
  
  AbstractWatchService()
  {
    this.queue = new LinkedBlockingQueue();
    this.poison = new Key(this, null, ImmutableSet.of());
    
    this.open = new AtomicBoolean(true);
  }
  
  public Key register(Watchable watchable, Iterable<? extends WatchEvent.Kind<?>> eventTypes)
    throws IOException
  {
    checkOpen();
    return new Key(this, watchable, eventTypes);
  }
  
  @VisibleForTesting
  public boolean isOpen()
  {
    return this.open.get();
  }
  
  final void enqueue(Key key)
  {
    if (isOpen()) {
      this.queue.add(key);
    }
  }
  
  public void cancelled(Key key) {}
  
  @VisibleForTesting
  ImmutableList<WatchKey> queuedKeys()
  {
    return ImmutableList.copyOf(this.queue);
  }
  
  @Nullable
  public WatchKey poll()
  {
    checkOpen();
    return check((WatchKey)this.queue.poll());
  }
  
  @Nullable
  public WatchKey poll(long timeout, TimeUnit unit)
    throws InterruptedException
  {
    checkOpen();
    return check((WatchKey)this.queue.poll(timeout, unit));
  }
  
  public WatchKey take()
    throws InterruptedException
  {
    checkOpen();
    return check((WatchKey)this.queue.take());
  }
  
  @Nullable
  private WatchKey check(@Nullable WatchKey key)
  {
    if (key == this.poison)
    {
      this.queue.offer(this.poison);
      throw new ClosedWatchServiceException();
    }
    return key;
  }
  
  protected final void checkOpen()
  {
    if (!this.open.get()) {
      throw new ClosedWatchServiceException();
    }
  }
  
  public void close()
  {
    if (this.open.compareAndSet(true, false))
    {
      this.queue.clear();
      this.queue.offer(this.poison);
    }
  }
  
  static final class Event<T>
    implements WatchEvent<T>
  {
    private final WatchEvent.Kind<T> kind;
    private final int count;
    @Nullable
    private final T context;
    
    public Event(WatchEvent.Kind<T> kind, int count, @Nullable T context)
    {
      this.kind = ((WatchEvent.Kind)Preconditions.checkNotNull(kind));
      Preconditions.checkArgument(count >= 0, "count (%s) must be non-negative", new Object[] { Integer.valueOf(count) });
      this.count = count;
      this.context = context;
    }
    
    public WatchEvent.Kind<T> kind()
    {
      return this.kind;
    }
    
    public int count()
    {
      return this.count;
    }
    
    @Nullable
    public T context()
    {
      return (T)this.context;
    }
    
    public boolean equals(Object obj)
    {
      if ((obj instanceof Event))
      {
        Event<?> other = (Event)obj;
        return (kind().equals(other.kind())) && (count() == other.count()) && (Objects.equals(context(), other.context()));
      }
      return false;
    }
    
    public int hashCode()
    {
      return Objects.hash(new Object[] { kind(), Integer.valueOf(count()), context() });
    }
    
    public String toString()
    {
      return MoreObjects.toStringHelper(this).add("kind", kind()).add("count", count()).add("context", context()).toString();
    }
  }
  
  static final class Key
    implements WatchKey
  {
    @VisibleForTesting
    static final int MAX_QUEUE_SIZE = 256;
    private final AbstractWatchService watcher;
    private final Watchable watchable;
    private final ImmutableSet<WatchEvent.Kind<?>> subscribedTypes;
    
    private static WatchEvent<Object> overflowEvent(int count)
    {
      return new AbstractWatchService.Event(StandardWatchEventKinds.OVERFLOW, count, null);
    }
    
    private final AtomicReference<State> state = new AtomicReference(State.READY);
    private final AtomicBoolean valid = new AtomicBoolean(true);
    private final AtomicInteger overflow = new AtomicInteger();
    private final BlockingQueue<WatchEvent<?>> events = new ArrayBlockingQueue(256);
    
    public Key(AbstractWatchService watcher, @Nullable Watchable watchable, Iterable<? extends WatchEvent.Kind<?>> subscribedTypes)
    {
      this.watcher = ((AbstractWatchService)Preconditions.checkNotNull(watcher));
      this.watchable = watchable;
      this.subscribedTypes = ImmutableSet.copyOf(subscribedTypes);
    }
    
    @VisibleForTesting
    State state()
    {
      return (State)this.state.get();
    }
    
    public boolean subscribesTo(WatchEvent.Kind<?> eventType)
    {
      return this.subscribedTypes.contains(eventType);
    }
    
    public void post(WatchEvent<?> event)
    {
      if (!this.events.offer(event)) {
        this.overflow.incrementAndGet();
      }
    }
    
    public void signal()
    {
      if (this.state.getAndSet(State.SIGNALLED) == State.READY) {
        this.watcher.enqueue(this);
      }
    }
    
    public boolean isValid()
    {
      return (this.watcher.isOpen()) && (this.valid.get());
    }
    
    public List<WatchEvent<?>> pollEvents()
    {
      List<WatchEvent<?>> result = new ArrayList(this.events.size());
      this.events.drainTo(result);
      int overflowCount = this.overflow.getAndSet(0);
      if (overflowCount != 0) {
        result.add(overflowEvent(overflowCount));
      }
      return Collections.unmodifiableList(result);
    }
    
    public boolean reset()
    {
      if ((isValid()) && (this.state.compareAndSet(State.SIGNALLED, State.READY))) {
        if (!this.events.isEmpty()) {
          signal();
        }
      }
      return isValid();
    }
    
    public void cancel()
    {
      this.valid.set(false);
      this.watcher.cancelled(this);
    }
    
    public Watchable watchable()
    {
      return this.watchable;
    }
    
    @VisibleForTesting
    static enum State
    {
      READY,  SIGNALLED;
      
      private State() {}
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\AbstractWatchService.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */