package com.sun.mail.util;

import java.io.OutputStream;

public class BEncoderStream
  extends BASE64EncoderStream
{
  public BEncoderStream(OutputStream out)
  {
    super(out, Integer.MAX_VALUE);
  }
  
  public static int encodedLength(byte[] b)
  {
    return (b.length + 2) / 3 * 4;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\util\BEncoderStream.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */