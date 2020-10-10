package org.apache.commons.codec;

public abstract interface StringEncoder
  extends Encoder
{
  public abstract String encode(String paramString)
    throws EncoderException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\commons\codec\StringEncoder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */