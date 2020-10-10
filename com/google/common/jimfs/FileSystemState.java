package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.ClosedFileSystemException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

final class FileSystemState
  implements Closeable
{
  private final Set<Closeable> resources = Sets.newConcurrentHashSet();
  private final Runnable onClose;
  private final AtomicBoolean open = new AtomicBoolean(true);
  private final AtomicInteger registering = new AtomicInteger();
  
  FileSystemState(Runnable onClose)
  {
    this.onClose = ((Runnable)Preconditions.checkNotNull(onClose));
  }
  
  public boolean isOpen()
  {
    return this.open.get();
  }
  
  public void checkOpen()
  {
    if (!this.open.get()) {
      throw new ClosedFileSystemException();
    }
  }
  
  public <C extends Closeable> C register(C resource)
  {
    checkOpen();
    
    this.registering.incrementAndGet();
    try
    {
      checkOpen();
      this.resources.add(resource);
      return resource;
    }
    finally
    {
      this.registering.decrementAndGet();
    }
  }
  
  public void unregister(Closeable resource)
  {
    this.resources.remove(resource);
  }
  
  public void close()
    throws IOException
  {
    if (this.open.compareAndSet(true, false))
    {
      this.onClose.run();
      
      Throwable thrown = null;
      do
      {
        for (Closeable resource : this.resources) {
          try
          {
            resource.close();
          }
          catch (Throwable e)
          {
            if (thrown == null) {
              thrown = e;
            } else {
              thrown.addSuppressed(e);
            }
          }
          finally
          {
            this.resources.remove(resource);
          }
        }
      } while ((this.registering.get() > 0) || (!this.resources.isEmpty()));
      Throwables.propagateIfPossible(thrown, IOException.class);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\FileSystemState.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */