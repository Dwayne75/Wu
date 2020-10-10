package org.apache.commons.codec.binary;

import java.io.InputStream;

public class Base64InputStream
  extends BaseNCodecInputStream
{
  public Base64InputStream(InputStream in)
  {
    this(in, false);
  }
  
  public Base64InputStream(InputStream in, boolean doEncode)
  {
    super(in, new Base64(false), doEncode);
  }
  
  public Base64InputStream(InputStream in, boolean doEncode, int lineLength, byte[] lineSeparator)
  {
    super(in, new Base64(lineLength, lineSeparator), doEncode);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\commons\codec\binary\Base64InputStream.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */