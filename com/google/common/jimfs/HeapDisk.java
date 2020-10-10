package com.google.common.jimfs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.math.LongMath;
import java.io.IOException;
import java.math.RoundingMode;

final class HeapDisk
{
  private final int blockSize;
  private final int maxBlockCount;
  private final int maxCachedBlockCount;
  @VisibleForTesting
  final RegularFile blockCache;
  private int allocatedBlockCount;
  
  public HeapDisk(Configuration config)
  {
    this.blockSize = config.blockSize;
    this.maxBlockCount = toBlockCount(config.maxSize, this.blockSize);
    this.maxCachedBlockCount = (config.maxCacheSize == -1L ? this.maxBlockCount : toBlockCount(config.maxCacheSize, this.blockSize));
    
    this.blockCache = createBlockCache(this.maxCachedBlockCount);
  }
  
  private static int toBlockCount(long size, int blockSize)
  {
    return (int)LongMath.divide(size, blockSize, RoundingMode.FLOOR);
  }
  
  public HeapDisk(int blockSize, int maxBlockCount, int maxCachedBlockCount)
  {
    Preconditions.checkArgument(blockSize > 0, "blockSize (%s) must be positive", new Object[] { Integer.valueOf(blockSize) });
    Preconditions.checkArgument(maxBlockCount > 0, "maxBlockCount (%s) must be positive", new Object[] { Integer.valueOf(maxBlockCount) });
    Preconditions.checkArgument(maxCachedBlockCount >= 0, "maxCachedBlockCount must be non-negative", new Object[] { Integer.valueOf(maxCachedBlockCount) });
    
    this.blockSize = blockSize;
    this.maxBlockCount = maxBlockCount;
    this.maxCachedBlockCount = maxCachedBlockCount;
    this.blockCache = createBlockCache(maxCachedBlockCount);
  }
  
  private RegularFile createBlockCache(int maxCachedBlockCount)
  {
    return new RegularFile(-1, this, new byte[Math.min(maxCachedBlockCount, 8192)][], 0, 0L);
  }
  
  public int blockSize()
  {
    return this.blockSize;
  }
  
  public synchronized long getTotalSpace()
  {
    return this.maxBlockCount * this.blockSize;
  }
  
  public synchronized long getUnallocatedSpace()
  {
    return (this.maxBlockCount - this.allocatedBlockCount) * this.blockSize;
  }
  
  public synchronized void allocate(RegularFile file, int count)
    throws IOException
  {
    int newAllocatedBlockCount = this.allocatedBlockCount + count;
    if (newAllocatedBlockCount > this.maxBlockCount) {
      throw new IOException("out of disk space");
    }
    int newBlocksNeeded = Math.max(count - this.blockCache.blockCount(), 0);
    for (int i = 0; i < newBlocksNeeded; i++) {
      file.addBlock(new byte[this.blockSize]);
    }
    if (newBlocksNeeded != count) {
      this.blockCache.transferBlocksTo(file, count - newBlocksNeeded);
    }
    this.allocatedBlockCount = newAllocatedBlockCount;
  }
  
  public void free(RegularFile file)
  {
    free(file, file.blockCount());
  }
  
  public synchronized void free(RegularFile file, int count)
  {
    int remainingCacheSpace = this.maxCachedBlockCount - this.blockCache.blockCount();
    if (remainingCacheSpace > 0) {
      file.copyBlocksTo(this.blockCache, Math.min(count, remainingCacheSpace));
    }
    file.truncateBlocks(file.blockCount() - count);
    
    this.allocatedBlockCount -= count;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\HeapDisk.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */