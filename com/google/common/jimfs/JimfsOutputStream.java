package com.google.common.jimfs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.locks.Lock;
import javax.annotation.concurrent.GuardedBy;

final class JimfsOutputStream
  extends OutputStream
{
  @GuardedBy("this")
  @VisibleForTesting
  RegularFile file;
  @GuardedBy("this")
  private long pos;
  private final boolean append;
  private final FileSystemState fileSystemState;
  
  JimfsOutputStream(RegularFile file, boolean append, FileSystemState fileSystemState)
  {
    this.file = ((RegularFile)Preconditions.checkNotNull(file));
    this.append = append;
    this.fileSystemState = fileSystemState;
    fileSystemState.register(this);
  }
  
  public synchronized void write(int b)
    throws IOException
  {
    checkNotClosed();
    
    this.file.writeLock().lock();
    try
    {
      if (this.append) {
        this.pos = this.file.sizeWithoutLocking();
      }
      this.file.write(this.pos++, (byte)b);
      
      this.file.updateModifiedTime();
    }
    finally
    {
      this.file.writeLock().unlock();
    }
  }
  
  public void write(byte[] b)
    throws IOException
  {
    writeInternal(b, 0, b.length);
  }
  
  public void write(byte[] b, int off, int len)
    throws IOException
  {
    Preconditions.checkPositionIndexes(off, off + len, b.length);
    writeInternal(b, off, len);
  }
  
  private synchronized void writeInternal(byte[] b, int off, int len)
    throws IOException
  {
    checkNotClosed();
    
    this.file.writeLock().lock();
    try
    {
      if (this.append) {
        this.pos = this.file.sizeWithoutLocking();
      }
      this.pos += this.file.write(this.pos, b, off, len);
      
      this.file.updateModifiedTime();
    }
    finally
    {
      this.file.writeLock().unlock();
    }
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\JimfsOutputStream.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */