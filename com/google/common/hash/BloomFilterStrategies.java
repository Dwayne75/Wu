package com.google.common.hash;

import com.google.common.base.Preconditions;
import com.google.common.math.LongMath;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import java.math.RoundingMode;
import java.util.Arrays;

 enum BloomFilterStrategies
  implements BloomFilter.Strategy
{
  MURMUR128_MITZ_32,  MURMUR128_MITZ_64;
  
  private BloomFilterStrategies() {}
  
  static final class BitArray
  {
    final long[] data;
    long bitCount;
    
    BitArray(long bits)
    {
      this(new long[Ints.checkedCast(LongMath.divide(bits, 64L, RoundingMode.CEILING))]);
    }
    
    BitArray(long[] data)
    {
      Preconditions.checkArgument(data.length > 0, "data length is zero!");
      this.data = data;
      long bitCount = 0L;
      for (long value : data) {
        bitCount += Long.bitCount(value);
      }
      this.bitCount = bitCount;
    }
    
    boolean set(long index)
    {
      if (!get(index))
      {
        this.data[((int)(index >>> 6))] |= 1L << (int)index;
        this.bitCount += 1L;
        return true;
      }
      return false;
    }
    
    boolean get(long index)
    {
      return (this.data[((int)(index >>> 6))] & 1L << (int)index) != 0L;
    }
    
    long bitSize()
    {
      return this.data.length * 64L;
    }
    
    long bitCount()
    {
      return this.bitCount;
    }
    
    BitArray copy()
    {
      return new BitArray((long[])this.data.clone());
    }
    
    void putAll(BitArray array)
    {
      Preconditions.checkArgument(this.data.length == array.data.length, "BitArrays must be of equal length (%s != %s)", new Object[] { Integer.valueOf(this.data.length), Integer.valueOf(array.data.length) });
      
      this.bitCount = 0L;
      for (int i = 0; i < this.data.length; i++)
      {
        this.data[i] |= array.data[i];
        this.bitCount += Long.bitCount(this.data[i]);
      }
    }
    
    public boolean equals(Object o)
    {
      if ((o instanceof BitArray))
      {
        BitArray bitArray = (BitArray)o;
        return Arrays.equals(this.data, bitArray.data);
      }
      return false;
    }
    
    public int hashCode()
    {
      return Arrays.hashCode(this.data);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\hash\BloomFilterStrategies.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */