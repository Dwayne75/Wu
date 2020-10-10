package org.apache.commons.codec;

public class DecoderException
  extends Exception
{
  private static final long serialVersionUID = 1L;
  
  public DecoderException() {}
  
  public DecoderException(String message)
  {
    super(message);
  }
  
  public DecoderException(String message, Throwable cause)
  {
    super(message, cause);
  }
  
  public DecoderException(Throwable cause)
  {
    super(cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\commons\codec\DecoderException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */