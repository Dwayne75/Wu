package org.apache.commons.codec;

public class EncoderException
  extends Exception
{
  private static final long serialVersionUID = 1L;
  
  public EncoderException() {}
  
  public EncoderException(String message)
  {
    super(message);
  }
  
  public EncoderException(String message, Throwable cause)
  {
    super(message, cause);
  }
  
  public EncoderException(Throwable cause)
  {
    super(cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\commons\codec\EncoderException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */