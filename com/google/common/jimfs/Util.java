package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;

final class Util
{
  private static final int C1 = -862048943;
  private static final int C2 = 461845907;
  private static final int ARRAY_LEN = 8192;
  
  public static int nextPowerOf2(int n)
  {
    if (n == 0) {
      return 1;
    }
    int b = Integer.highestOneBit(n);
    return b == n ? n : b << 1;
  }
  
  static void checkNotNegative(long n, String description)
  {
    Preconditions.checkArgument(n >= 0L, "%s must not be negative: %s", new Object[] { description, Long.valueOf(n) });
  }
  
  static void checkNoneNull(Iterable<?> objects)
  {
    if (!(objects instanceof ImmutableCollection)) {
      for (Object o : objects) {
        Preconditions.checkNotNull(o);
      }
    }
  }
  
  static int smearHash(int hashCode)
  {
    return 461845907 * Integer.rotateLeft(hashCode * -862048943, 15);
  }
  
  private static final byte[] ZERO_ARRAY = new byte[' '];
  private static final byte[][] NULL_ARRAY = new byte[' '][];
  
  static void zero(byte[] bytes, int off, int len)
  {
    int remaining = len;
    while (remaining > 8192)
    {
      System.arraycopy(ZERO_ARRAY, 0, bytes, off, 8192);
      off += 8192;
      remaining -= 8192;
    }
    System.arraycopy(ZERO_ARRAY, 0, bytes, off, remaining);
  }
  
  static void clear(byte[][] blocks, int off, int len)
  {
    int remaining = len;
    while (remaining > 8192)
    {
      System.arraycopy(NULL_ARRAY, 0, blocks, off, 8192);
      off += 8192;
      remaining -= 8192;
    }
    System.arraycopy(NULL_ARRAY, 0, blocks, off, remaining);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\Util.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */