package com.google.common.jimfs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.Lock;
import javax.annotation.concurrent.GuardedBy;

final class JimfsInputStream
  extends InputStream
{
  @GuardedBy("this")
  @VisibleForTesting
  RegularFile file;
  @GuardedBy("this")
  private long pos;
  @GuardedBy("this")
  private boolean finished;
  private final FileSystemState fileSystemState;
  
  public JimfsInputStream(RegularFile file, FileSystemState fileSystemState)
  {
    this.file = ((RegularFile)Preconditions.checkNotNull(file));
    this.fileSystemState = fileSystemState;
    fileSystemState.register(this);
  }
  
  public synchronized int read()
    throws IOException
  {
    checkNotClosed();
    if (this.finished) {
      return -1;
    }
    this.file.readLock().lock();
    try
    {
      int b = this.file.read(this.pos++);
      if (b == -1) {
        this.finished = true;
      } else {
        this.file.updateAccessTime();
      }
      return b;
    }
    finally
    {
      this.file.readLock().unlock();
    }
  }
  
  public int read(byte[] b)
    throws IOException
  {
    return readInternal(b, 0, b.length);
  }
  
  public int read(byte[] b, int off, int len)
    throws IOException
  {
    Preconditions.checkPositionIndexes(off, off + len, b.length);
    return readInternal(b, off, len);
  }
  
  private synchronized int readInternal(byte[] b, int off, int len)
    throws IOException
  {
    checkNotClosed();
    if (this.finished) {
      return -1;
    }
    this.file.readLock().lock();
    try
    {
      int read = this.file.read(this.pos, b, off, len);
      if (read == -1) {
        this.finished = true;
      } else {
        this.pos += read;
      }
      this.file.updateAccessTime();
      return read;
    }
    finally
    {
      this.file.readLock().unlock();
    }
  }
  
  public long skip(long n)
    throws IOException
  {
    if (n <= 0L) {
      return 0L;
    }
    synchronized (this)
    {
      checkNotClosed();
      if (this.finished) {
        return 0L;
      }
      int skip = (int)Math.min(Math.max(this.file.size() - this.pos, 0L), n);
      this.pos += skip;
      return skip;
    }
  }
  
  public synchronized int available()
    throws IOException
  {
    checkNotClosed();
    if (this.finished) {
      return 0;
    }
    long available = Math.max(this.file.size() - this.pos, 0L);
    return Ints.saturatedCast(available);
  }
  
  @GuardedBy("this")
  private void checkNotClosed()
    throws IOException
  {
    if (this.file == null) {
      throw new IOException("stream is closed");
    }
  }
  
  public synchronized void close()
    throws IOException
  {
    if (isOpen())
    {
      this.fileSystemState.unregister(this);
      this.file.closed();
      
      this.file = null;
    }
  }
  
  @GuardedBy("this")
  private boolean isOpen()
  {
    return this.file != null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\JimfsInputStream.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */