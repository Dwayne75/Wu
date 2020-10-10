package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.FileLock;
import java.nio.channels.FileLockInterruptionException;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import javax.annotation.concurrent.GuardedBy;

final class JimfsFileChannel
  extends FileChannel
{
  @GuardedBy("blockingThreads")
  private final Set<Thread> blockingThreads = new HashSet();
  private final RegularFile file;
  private final FileSystemState fileSystemState;
  private final boolean read;
  private final boolean write;
  private final boolean append;
  @GuardedBy("this")
  private long position;
  
  public JimfsFileChannel(RegularFile file, Set<OpenOption> options, FileSystemState fileSystemState)
  {
    this.file = file;
    this.fileSystemState = fileSystemState;
    this.read = options.contains(StandardOpenOption.READ);
    this.write = options.contains(StandardOpenOption.WRITE);
    this.append = options.contains(StandardOpenOption.APPEND);
    
    fileSystemState.register(this);
  }
  
  public AsynchronousFileChannel asAsynchronousFileChannel(ExecutorService executor)
  {
    return new JimfsAsynchronousFileChannel(this, executor);
  }
  
  void checkReadable()
  {
    if (!this.read) {
      throw new NonReadableChannelException();
    }
  }
  
  void checkWritable()
  {
    if (!this.write) {
      throw new NonWritableChannelException();
    }
  }
  
  void checkOpen()
    throws ClosedChannelException
  {
    if (!isOpen()) {
      throw new ClosedChannelException();
    }
  }
  
  private boolean beginBlocking()
  {
    begin();
    synchronized (this.blockingThreads)
    {
      if (isOpen())
      {
        this.blockingThreads.add(Thread.currentThread());
        return true;
      }
      return false;
    }
  }
  
  private void endBlocking(boolean completed)
    throws AsynchronousCloseException
  {
    synchronized (this.blockingThreads)
    {
      this.blockingThreads.remove(Thread.currentThread());
    }
    end(completed);
  }
  
  public int read(ByteBuffer dst)
    throws IOException
  {
    Preconditions.checkNotNull(dst);
    checkOpen();
    checkReadable();
    
    int read = 0;
    synchronized (this)
    {
      boolean completed = false;
      try
      {
        if (!beginBlocking())
        {
          int i = 0;
          
          endBlocking(completed);return i;
        }
        this.file.readLock().lockInterruptibly();
        try
        {
          read = this.file.read(this.position, dst);
          if (read != -1) {
            this.position += read;
          }
          this.file.updateAccessTime();
          completed = true;
        }
        finally
        {
          this.file.readLock().unlock();
        }
      }
      catch (InterruptedException e)
      {
        Thread.currentThread().interrupt();
      }
      finally
      {
        endBlocking(completed);
      }
    }
    return read;
  }
  
  public long read(ByteBuffer[] dsts, int offset, int length)
    throws IOException
  {
    Preconditions.checkPositionIndexes(offset, offset + length, dsts.length);
    List<ByteBuffer> buffers = Arrays.asList(dsts).subList(offset, offset + length);
    Util.checkNoneNull(buffers);
    checkOpen();
    checkReadable();
    
    long read = 0L;
    synchronized (this)
    {
      boolean completed = false;
      try
      {
        if (!beginBlocking())
        {
          long l1 = 0L;
          
          endBlocking(completed);return l1;
        }
        this.file.readLock().lockInterruptibly();
        try
        {
          read = this.file.read(this.position, buffers);
          if (read != -1L) {
            this.position += read;
          }
          this.file.updateAccessTime();
          completed = true;
        }
        finally
        {
          this.file.readLock().unlock();
        }
      }
      catch (InterruptedException e)
      {
        Thread.currentThread().interrupt();
      }
      finally
      {
        endBlocking(completed);
      }
    }
    return read;
  }
  
  public int write(ByteBuffer src)
    throws IOException
  {
    Preconditions.checkNotNull(src);
    checkOpen();
    checkWritable();
    
    int written = 0;
    synchronized (this)
    {
      boolean completed = false;
      try
      {
        if (!beginBlocking())
        {
          int i = 0;
          
          endBlocking(completed);return i;
        }
        this.file.writeLock().lockInterruptibly();
        try
        {
          if (this.append) {
            this.position = this.file.size();
          }
          written = this.file.write(this.position, src);
          this.position += written;
          this.file.updateModifiedTime();
          completed = true;
        }
        finally
        {
          this.file.writeLock().unlock();
        }
      }
      catch (InterruptedException e)
      {
        Thread.currentThread().interrupt();
      }
      finally
      {
        endBlocking(completed);
      }
    }
    return written;
  }
  
  public long write(ByteBuffer[] srcs, int offset, int length)
    throws IOException
  {
    Preconditions.checkPositionIndexes(offset, offset + length, srcs.length);
    List<ByteBuffer> buffers = Arrays.asList(srcs).subList(offset, offset + length);
    Util.checkNoneNull(buffers);
    checkOpen();
    checkWritable();
    
    long written = 0L;
    synchronized (this)
    {
      boolean completed = false;
      try
      {
        if (!beginBlocking())
        {
          long l1 = 0L;
          
          endBlocking(completed);return l1;
        }
        this.file.writeLock().lockInterruptibly();
        try
        {
          if (this.append) {
            this.position = this.file.size();
          }
          written = this.file.write(this.position, buffers);
          this.position += written;
          this.file.updateModifiedTime();
          completed = true;
        }
        finally
        {
          this.file.writeLock().unlock();
        }
      }
      catch (InterruptedException e)
      {
        Thread.currentThread().interrupt();
      }
      finally
      {
        endBlocking(completed);
      }
    }
    return written;
  }
  
  public long position()
    throws IOException
  {
    checkOpen();
    long pos;
    synchronized (this)
    {
      boolean completed = false;
      try
      {
        begin();
        if (!isOpen())
        {
          long l1 = 0L;
          
          end(completed);return l1;
        }
        pos = this.position;
        completed = true;
      }
      finally
      {
        end(completed);
      }
    }
    return pos;
  }
  
  public FileChannel position(long newPosition)
    throws IOException
  {
    Util.checkNotNegative(newPosition, "newPosition");
    checkOpen();
    synchronized (this)
    {
      boolean completed = false;
      try
      {
        begin();
        if (!isOpen())
        {
          JimfsFileChannel localJimfsFileChannel = this;
          
          end(completed);return localJimfsFileChannel;
        }
        this.position = newPosition;
        completed = true;
      }
      finally
      {
        end(completed);
      }
    }
    return this;
  }
  
  public long size()
    throws IOException
  {
    checkOpen();
    
    long size = 0L;
    
    boolean completed = false;
    try
    {
      if (!beginBlocking()) {
        return 0L;
      }
      this.file.readLock().lockInterruptibly();
      try
      {
        size = this.file.sizeWithoutLocking();
        completed = true;
      }
      finally
      {
        this.file.readLock().unlock();
      }
    }
    catch (InterruptedException e)
    {
      Thread.currentThread().interrupt();
    }
    finally
    {
      endBlocking(completed);
    }
    return size;
  }
  
  public FileChannel truncate(long size)
    throws IOException
  {
    Util.checkNotNegative(size, "size");
    checkOpen();
    checkWritable();
    synchronized (this)
    {
      boolean completed = false;
      try
      {
        if (!beginBlocking())
        {
          JimfsFileChannel localJimfsFileChannel = this;
          
          endBlocking(completed);return localJimfsFileChannel;
        }
        this.file.writeLock().lockInterruptibly();
        try
        {
          this.file.truncate(size);
          if (this.position > size) {
            this.position = size;
          }
          this.file.updateModifiedTime();
          completed = true;
        }
        finally
        {
          this.file.writeLock().unlock();
        }
      }
      catch (InterruptedException e)
      {
        Thread.currentThread().interrupt();
      }
      finally
      {
        endBlocking(completed);
      }
    }
    return this;
  }
  
  public void force(boolean metaData)
    throws IOException
  {
    checkOpen();
    
    boolean completed = false;
    try
    {
      begin();
      completed = true;
    }
    finally
    {
      end(completed);
    }
  }
  
  public long transferTo(long position, long count, WritableByteChannel target)
    throws IOException
  {
    Preconditions.checkNotNull(target);
    Util.checkNotNegative(position, "position");
    Util.checkNotNegative(count, "count");
    checkOpen();
    checkReadable();
    
    long transferred = 0L;
    
    boolean completed = false;
    try
    {
      if (!beginBlocking()) {
        return 0L;
      }
      this.file.readLock().lockInterruptibly();
      try
      {
        transferred = this.file.transferTo(position, count, target);
        this.file.updateAccessTime();
        completed = true;
      }
      finally
      {
        this.file.readLock().unlock();
      }
    }
    catch (InterruptedException e)
    {
      Thread.currentThread().interrupt();
    }
    finally
    {
      endBlocking(completed);
    }
    return transferred;
  }
  
  public long transferFrom(ReadableByteChannel src, long position, long count)
    throws IOException
  {
    Preconditions.checkNotNull(src);
    Util.checkNotNegative(position, "position");
    Util.checkNotNegative(count, "count");
    checkOpen();
    checkWritable();
    
    long transferred = 0L;
    boolean completed;
    if (this.append)
    {
      synchronized (this)
      {
        completed = false;
        try
        {
          if (!beginBlocking())
          {
            long l1 = 0L;
            
            endBlocking(completed);return l1;
          }
          this.file.writeLock().lockInterruptibly();
          try
          {
            position = this.file.sizeWithoutLocking();
            transferred = this.file.transferFrom(src, position, count);
            this.position = (position + transferred);
            this.file.updateModifiedTime();
            completed = true;
          }
          finally
          {
            this.file.writeLock().unlock();
          }
        }
        catch (InterruptedException e)
        {
          Thread.currentThread().interrupt();
        }
        finally
        {
          endBlocking(completed);
        }
      }
    }
    else
    {
      boolean completed = false;
      try
      {
        if (!beginBlocking()) {
          return 0L;
        }
        this.file.writeLock().lockInterruptibly();
        try
        {
          transferred = this.file.transferFrom(src, position, count);
          this.file.updateModifiedTime();
          completed = true;
        }
        finally
        {
          this.file.writeLock().unlock();
        }
      }
      catch (InterruptedException e)
      {
        Thread.currentThread().interrupt();
      }
      finally
      {
        endBlocking(completed);
      }
    }
    return transferred;
  }
  
  public int read(ByteBuffer dst, long position)
    throws IOException
  {
    Preconditions.checkNotNull(dst);
    Util.checkNotNegative(position, "position");
    checkOpen();
    checkReadable();
    
    int read = 0;
    
    boolean completed = false;
    try
    {
      if (!beginBlocking()) {
        return 0;
      }
      this.file.readLock().lockInterruptibly();
      try
      {
        read = this.file.read(position, dst);
        this.file.updateAccessTime();
        completed = true;
      }
      finally
      {
        this.file.readLock().unlock();
      }
    }
    catch (InterruptedException e)
    {
      Thread.currentThread().interrupt();
    }
    finally
    {
      endBlocking(completed);
    }
    return read;
  }
  
  public int write(ByteBuffer src, long position)
    throws IOException
  {
    Preconditions.checkNotNull(src);
    Util.checkNotNegative(position, "position");
    checkOpen();
    checkWritable();
    
    int written = 0;
    boolean completed;
    if (this.append)
    {
      synchronized (this)
      {
        completed = false;
        try
        {
          if (!beginBlocking())
          {
            int i = 0;
            
            endBlocking(completed);return i;
          }
          this.file.writeLock().lockInterruptibly();
          try
          {
            position = this.file.sizeWithoutLocking();
            written = this.file.write(position, src);
            this.position = (position + written);
            this.file.updateModifiedTime();
            completed = true;
          }
          finally
          {
            this.file.writeLock().unlock();
          }
        }
        catch (InterruptedException e)
        {
          Thread.currentThread().interrupt();
        }
        finally
        {
          endBlocking(completed);
        }
      }
    }
    else
    {
      boolean completed = false;
      try
      {
        if (!beginBlocking()) {
          return false;
        }
        this.file.writeLock().lockInterruptibly();
        try
        {
          written = this.file.write(position, src);
          this.file.updateModifiedTime();
          completed = true;
        }
        finally
        {
          this.file.writeLock().unlock();
        }
      }
      catch (InterruptedException e)
      {
        Thread.currentThread().interrupt();
      }
      finally
      {
        endBlocking(completed);
      }
    }
    return written;
  }
  
  public MappedByteBuffer map(FileChannel.MapMode mode, long position, long size)
    throws IOException
  {
    throw new UnsupportedOperationException();
  }
  
  public FileLock lock(long position, long size, boolean shared)
    throws IOException
  {
    checkLockArguments(position, size, shared);
    
    boolean completed = false;
    try
    {
      begin();
      completed = true;
      return new FakeFileLock(this, position, size, shared);
    }
    finally
    {
      try
      {
        end(completed);
      }
      catch (ClosedByInterruptException e)
      {
        throw new FileLockInterruptionException();
      }
    }
  }
  
  public FileLock tryLock(long position, long size, boolean shared)
    throws IOException
  {
    checkLockArguments(position, size, shared);
    
    return new FakeFileLock(this, position, size, shared);
  }
  
  private void checkLockArguments(long position, long size, boolean shared)
    throws IOException
  {
    Util.checkNotNegative(position, "position");
    Util.checkNotNegative(size, "size");
    checkOpen();
    if (shared) {
      checkReadable();
    } else {
      checkWritable();
    }
  }
  
  protected void implCloseChannel()
  {
    try
    {
      synchronized (this.blockingThreads)
      {
        for (Thread thread : this.blockingThreads) {
          thread.interrupt();
        }
      }
    }
    finally
    {
      this.fileSystemState.unregister(this);
      this.file.closed();
    }
  }
  
  static final class FakeFileLock
    extends FileLock
  {
    private final AtomicBoolean valid = new AtomicBoolean(true);
    
    public FakeFileLock(FileChannel channel, long position, long size, boolean shared)
    {
      super(position, size, shared);
    }
    
    public FakeFileLock(AsynchronousFileChannel channel, long position, long size, boolean shared)
    {
      super(position, size, shared);
    }
    
    public boolean isValid()
    {
      return this.valid.get();
    }
    
    public void release()
      throws IOException
    {
      this.valid.set(false);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\JimfsFileChannel.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */