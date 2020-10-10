package com.sun.codemodel.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import sun.nio.cs.Surrogate;
import sun.nio.cs.Surrogate.Parser;

abstract class SingleByteEncoder
  extends CharsetEncoder
{
  private final short[] index1;
  private final String index2;
  private final int mask1;
  private final int mask2;
  private final int shift;
  private final Surrogate.Parser sgp = new Surrogate.Parser();
  
  protected SingleByteEncoder(Charset cs, short[] index1, String index2, int mask1, int mask2, int shift)
  {
    super(cs, 1.0F, 1.0F);
    this.index1 = index1;
    this.index2 = index2;
    this.mask1 = mask1;
    this.mask2 = mask2;
    this.shift = shift;
  }
  
  public boolean canEncode(char c)
  {
    char testEncode = this.index2.charAt(this.index1[((c & this.mask1) >> this.shift)] + (c & this.mask2));
    if (testEncode == 0) {
      return false;
    }
    return true;
  }
  
  private CoderResult encodeArrayLoop(CharBuffer src, ByteBuffer dst)
  {
    char[] sa = src.array();
    int sp = src.arrayOffset() + src.position();
    int sl = src.arrayOffset() + src.limit();
    sp = sp <= sl ? sp : sl;
    byte[] da = dst.array();
    int dp = dst.arrayOffset() + dst.position();
    int dl = dst.arrayOffset() + dst.limit();
    dp = dp <= dl ? dp : dl;
    try
    {
      char c;
      while (sp < sl)
      {
        c = sa[sp];
        CoderResult localCoderResult1;
        if (Surrogate.is(c))
        {
          if (this.sgp.parse(c, sa, sp, sl) < 0) {
            return this.sgp.error();
          }
          return this.sgp.unmappableResult();
        }
        if (c >= 65534) {
          return CoderResult.unmappableForLength(1);
        }
        if (dl - dp < 1) {
          return CoderResult.OVERFLOW;
        }
        char e = this.index2.charAt(this.index1[((c & this.mask1) >> this.shift)] + (c & this.mask2));
        if ((e == 0) && (c != 0)) {
          return CoderResult.unmappableForLength(1);
        }
        sp++;
        da[(dp++)] = ((byte)e);
      }
      return CoderResult.UNDERFLOW;
    }
    finally
    {
      src.position(sp - src.arrayOffset());
      dst.position(dp - dst.arrayOffset());
    }
  }
  
  private CoderResult encodeBufferLoop(CharBuffer src, ByteBuffer dst)
  {
    int mark = src.position();
    try
    {
      char c;
      while (src.hasRemaining())
      {
        c = src.get();
        CoderResult localCoderResult1;
        if (Surrogate.is(c))
        {
          if (this.sgp.parse(c, src) < 0) {
            return this.sgp.error();
          }
          return this.sgp.unmappableResult();
        }
        if (c >= 65534) {
          return CoderResult.unmappableForLength(1);
        }
        if (!dst.hasRemaining()) {
          return CoderResult.OVERFLOW;
        }
        char e = this.index2.charAt(this.index1[((c & this.mask1) >> this.shift)] + (c & this.mask2));
        if ((e == 0) && (c != 0)) {
          return CoderResult.unmappableForLength(1);
        }
        mark++;
        dst.put((byte)e);
      }
      return CoderResult.UNDERFLOW;
    }
    finally
    {
      src.position(mark);
    }
  }
  
  protected CoderResult encodeLoop(CharBuffer src, ByteBuffer dst)
  {
    if ((src.hasArray()) && (dst.hasArray())) {
      return encodeArrayLoop(src, dst);
    }
    return encodeBufferLoop(src, dst);
  }
  
  public byte encode(char inputChar)
  {
    return (byte)this.index2.charAt(this.index1[((inputChar & this.mask1) >> this.shift)] + (inputChar & this.mask2));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\util\SingleByteEncoder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */