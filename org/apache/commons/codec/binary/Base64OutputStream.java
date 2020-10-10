package org.apache.commons.codec.binary;

import java.io.OutputStream;

public class Base64OutputStream
  extends BaseNCodecOutputStream
{
  public Base64OutputStream(OutputStream out)
  {
    this(out, true);
  }
  
  public Base64OutputStream(OutputStream out, boolean doEncode)
  {
    super(out, new Base64(false), doEncode);
  }
  
  public Base64OutputStream(OutputStream out, boolean doEncode, int lineLength, byte[] lineSeparator)
  {
    super(out, new Base64(lineLength, lineSeparator), doEncode);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\commons\codec\binary\Base64OutputStream.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */