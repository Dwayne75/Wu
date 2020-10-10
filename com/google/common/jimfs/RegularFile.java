package com.google.common.jimfs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedBytes;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

final class RegularFile
  extends File
{
  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  private final HeapDisk disk;
  private byte[][] blocks;
  private int blockCount;
  private long size;
  
  public static RegularFile create(int id, HeapDisk disk)
  {
    return new RegularFile(id, disk, new byte[32][], 0, 0L);
  }
  
  RegularFile(int id, HeapDisk disk, byte[][] blocks, int blockCount, long size)
  {
    super(id);
    this.disk = ((HeapDisk)Preconditions.checkNotNull(disk));
    this.blocks = ((byte[][])Preconditions.checkNotNull(blocks));
    this.blockCount = blockCount;
    
    Preconditions.checkArgument(size >= 0L);
    this.size = size;
  }
  
  private int openCount = 0;
  private boolean deleted = false;
  
  public Lock readLock()
  {
    return this.lock.readLock();
  }
  
  public Lock writeLock()
  {
    return this.lock.writeLock();
  }
  
  private void expandIfNecessary(int minBlockCount)
  {
    if (minBlockCount > this.blocks.length) {
      this.blocks = ((byte[][])Arrays.copyOf(this.blocks, Util.nextPowerOf2(minBlockCount)));
    }
  }
  
  int blockCount()
  {
    return this.blockCount;
  }
  
  void copyBlocksTo(RegularFile target, int count)
  {
    int start = this.blockCount - count;
    int targetEnd = target.blockCount + count;
    target.expandIfNecessary(targetEnd);
    
    System.arraycopy(this.blocks, start, target.blocks, target.blockCount, count);
    target.blockCount = targetEnd;
  }
  
  void transferBlocksTo(RegularFile target, int count)
  {
    copyBlocksTo(target, count);
    truncateBlocks(this.blockCount - count);
  }
  
  void truncateBlocks(int count)
  {
    Util.clear(this.blocks, count, this.blockCount - count);
    this.blockCount = count;
  }
  
  void addBlock(byte[] block)
  {
    expandIfNecessary(this.blockCount + 1);
    this.blocks[(this.blockCount++)] = block;
  }
  
  @VisibleForTesting
  byte[] getBlock(int index)
  {
    return this.blocks[index];
  }
  
  public long sizeWithoutLocking()
  {
    return this.size;
  }
  
  public long size()
  {
    readLock().lock();
    try
    {
      return this.size;
    }
    finally
    {
      readLock().unlock();
    }
  }
  
  RegularFile copyWithoutContent(int id)
  {
    byte[][] copyBlocks = new byte[Math.max(this.blockCount * 2, 32)][];
    return new RegularFile(id, this.disk, copyBlocks, 0, this.size);
  }
  
  void copyContentTo(File file)
    throws IOException
  {
    RegularFile copy = (RegularFile)file;
    this.disk.allocate(copy, this.blockCount);
    for (int i = 0; i < this.blockCount; i++)
    {
      byte[] block = this.blocks[i];
      byte[] copyBlock = copy.blocks[i];
      System.arraycopy(block, 0, copyBlock, 0, block.length);
    }
  }
  
  ReadWriteLock contentLock()
  {
    return this.lock;
  }
  
  public synchronized void opened()
  {
    this.openCount += 1;
  }
  
  public synchronized void closed()
  {
    if ((--this.openCount == 0) && (this.deleted)) {
      deleteContents();
    }
  }
  
  public synchronized void deleted()
  {
    if (links() == 0)
    {
      this.deleted = true;
      if (this.openCount == 0) {
        deleteContents();
      }
    }
  }
  
  private void deleteContents()
  {
    this.disk.free(this);
    this.size = 0L;
  }
  
  public boolean truncate(long size)
  {
    if (size >= this.size) {
      return false;
    }
    long lastPosition = size - 1L;
    this.size = size;
    
    int newBlockCount = blockIndex(lastPosition) + 1;
    int blocksToRemove = this.blockCount - newBlockCount;
    if (blocksToRemove > 0) {
      this.disk.free(this, blocksToRemove);
    }
    return true;
  }
  
  private void prepareForWrite(long pos, long len)
    throws IOException
  {
    long end = pos + len;
    
    int lastBlockIndex = this.blockCount - 1;
    int endBlockIndex = blockIndex(end - 1L);
    if (endBlockIndex > lastBlockIndex)
    {
      int additionalBlocksNeeded = endBlockIndex - lastBlockIndex;
      this.disk.allocate(this, additionalBlocksNeeded);
    }
    if (pos > this.size)
    {
      long remaining = pos - this.size;
      
      int blockIndex = blockIndex(this.size);
      byte[] block = this.blocks[blockIndex];
      int off = offsetInBlock(this.size);
      
      remaining -= zero(block, off, length(off, remaining));
      while (remaining > 0L)
      {
        block = this.blocks[(++blockIndex)];
        
        remaining -= zero(block, 0, length(remaining));
      }
      this.size = pos;
    }
  }
  
  public int write(long pos, byte b)
    throws IOException
  {
    prepareForWrite(pos, 1L);
    
    byte[] block = this.blocks[blockIndex(pos)];
    int off = offsetInBlock(pos);
    block[off] = b;
    if (pos >= this.size) {
      this.size = (pos + 1L);
    }
    return 1;
  }
  
  public int write(long pos, byte[] b, int off, int len)
    throws IOException
  {
    prepareForWrite(pos, len);
    if (len == 0) {
      return 0;
    }
    int remaining = len;
    
    int blockIndex = blockIndex(pos);
    byte[] block = this.blocks[blockIndex];
    int offInBlock = offsetInBlock(pos);
    
    int written = put(block, offInBlock, b, off, length(offInBlock, remaining));
    remaining -= written;
    off += written;
    while (remaining > 0)
    {
      block = this.blocks[(++blockIndex)];
      
      written = put(block, 0, b, off, length(remaining));
      remaining -= written;
      off += written;
    }
    long endPos = pos + len;
    if (endPos > this.size) {
      this.size = endPos;
    }
    return len;
  }
  
  public int write(long pos, ByteBuffer buf)
    throws IOException
  {
    int len = buf.remaining();
    
    prepareForWrite(pos, len);
    if (len == 0) {
      return 0;
    }
    int blockIndex = blockIndex(pos);
    byte[] block = this.blocks[blockIndex];
    int off = offsetInBlock(pos);
    
    put(block, off, buf);
    while (buf.hasRemaining())
    {
      block = this.blocks[(++blockIndex)];
      
      put(block, 0, buf);
    }
    long endPos = pos + len;
    if (endPos > this.size) {
      this.size = endPos;
    }
    return len;
  }
  
  public long write(long pos, Iterable<ByteBuffer> bufs)
    throws IOException
  {
    long start = pos;
    for (ByteBuffer buf : bufs) {
      pos += write(pos, buf);
    }
    return pos - start;
  }
  
  public long transferFrom(ReadableByteChannel src, long pos, long count)
    throws IOException
  {
    prepareForWrite(pos, 0L);
    if (count == 0L) {
      return 0L;
    }
    long remaining = count;
    
    int blockIndex = blockIndex(pos);
    byte[] block = blockForWrite(blockIndex);
    int off = offsetInBlock(pos);
    
    ByteBuffer buf = ByteBuffer.wrap(block, off, length(off, remaining));
    
    long currentPos = pos;
    int read = 0;
    while (buf.hasRemaining())
    {
      read = src.read(buf);
      if (read == -1) {
        break;
      }
      currentPos += read;
      remaining -= read;
    }
    if (currentPos > this.size) {
      this.size = currentPos;
    }
    if (read != -1) {
      while (remaining > 0L)
      {
        block = blockForWrite(++blockIndex);
        
        buf = ByteBuffer.wrap(block, 0, length(remaining));
        while (buf.hasRemaining())
        {
          read = src.read(buf);
          if (read == -1) {
            break label229;
          }
          currentPos += read;
          remaining -= read;
        }
        if (currentPos > this.size) {
          this.size = currentPos;
        }
      }
    }
    label229:
    if (currentPos > this.size) {
      this.size = currentPos;
    }
    return currentPos - pos;
  }
  
  public int read(long pos)
  {
    if (pos >= this.size) {
      return -1;
    }
    byte[] block = this.blocks[blockIndex(pos)];
    int off = offsetInBlock(pos);
    return UnsignedBytes.toInt(block[off]);
  }
  
  public int read(long pos, byte[] b, int off, int len)
  {
    int bytesToRead = (int)bytesToRead(pos, len);
    if (bytesToRead > 0)
    {
      int remaining = bytesToRead;
      
      int blockIndex = blockIndex(pos);
      byte[] block = this.blocks[blockIndex];
      int offsetInBlock = offsetInBlock(pos);
      
      int read = get(block, offsetInBlock, b, off, length(offsetInBlock, remaining));
      remaining -= read;
      off += read;
      while (remaining > 0)
      {
        blockIndex++;int index = blockIndex;
        block = this.blocks[index];
        
        read = get(block, 0, b, off, length(remaining));
        remaining -= read;
        off += read;
      }
    }
    return bytesToRead;
  }
  
  public int read(long pos, ByteBuffer buf)
  {
    int bytesToRead = (int)bytesToRead(pos, buf.remaining());
    if (bytesToRead > 0)
    {
      int remaining = bytesToRead;
      
      int blockIndex = blockIndex(pos);
      byte[] block = this.blocks[blockIndex];
      int off = offsetInBlock(pos);
      
      remaining -= get(block, off, buf, length(off, remaining));
      while (remaining > 0)
      {
        blockIndex++;int index = blockIndex;
        block = this.blocks[index];
        remaining -= get(block, 0, buf, length(remaining));
      }
    }
    return bytesToRead;
  }
  
  public long read(long pos, Iterable<ByteBuffer> bufs)
  {
    if (pos >= size()) {
      return -1L;
    }
    long start = pos;
    for (ByteBuffer buf : bufs)
    {
      int read = read(pos, buf);
      if (read == -1) {
        break;
      }
      pos += read;
    }
    return pos - start;
  }
  
  public long transferTo(long pos, long count, WritableByteChannel dest)
    throws IOException
  {
    long bytesToRead = bytesToRead(pos, count);
    if (bytesToRead > 0L)
    {
      long remaining = bytesToRead;
      
      int blockIndex = blockIndex(pos);
      byte[] block = this.blocks[blockIndex];
      int off = offsetInBlock(pos);
      
      ByteBuffer buf = ByteBuffer.wrap(block, off, length(off, remaining));
      while (buf.hasRemaining()) {
        remaining -= dest.write(buf);
      }
      buf.clear();
      while (remaining > 0L)
      {
        blockIndex++;int index = blockIndex;
        block = this.blocks[index];
        
        buf = ByteBuffer.wrap(block, 0, length(remaining));
        while (buf.hasRemaining()) {
          remaining -= dest.write(buf);
        }
        buf.clear();
      }
    }
    return Math.max(bytesToRead, 0L);
  }
  
  private byte[] blockForWrite(int index)
    throws IOException
  {
    if (index >= this.blockCount)
    {
      int additionalBlocksNeeded = index - this.blockCount + 1;
      this.disk.allocate(this, additionalBlocksNeeded);
    }
    return this.blocks[index];
  }
  
  private int blockIndex(long position)
  {
    return (int)(position / this.disk.blockSize());
  }
  
  private int offsetInBlock(long position)
  {
    return (int)(position % this.disk.blockSize());
  }
  
  private int length(long max)
  {
    return (int)Math.min(this.disk.blockSize(), max);
  }
  
  private int length(int off, long max)
  {
    return (int)Math.min(this.disk.blockSize() - off, max);
  }
  
  private long bytesToRead(long pos, long max)
  {
    long available = this.size - pos;
    if (available <= 0L) {
      return -1L;
    }
    return Math.min(available, max);
  }
  
  private static int zero(byte[] block, int offset, int len)
  {
    Util.zero(block, offset, len);
    return len;
  }
  
  private static int put(byte[] block, int offset, byte[] b, int off, int len)
  {
    System.arraycopy(b, off, block, offset, len);
    return len;
  }
  
  private static int put(byte[] block, int offset, ByteBuffer buf)
  {
    int len = Math.min(block.length - offset, buf.remaining());
    buf.get(block, offset, len);
    return len;
  }
  
  private static int get(byte[] block, int offset, byte[] b, int off, int len)
  {
    System.arraycopy(block, offset, b, off, len);
    return len;
  }
  
  private static int get(byte[] block, int offset, ByteBuffer buf, int len)
  {
    buf.put(block, offset, len);
    return len;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\RegularFile.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */