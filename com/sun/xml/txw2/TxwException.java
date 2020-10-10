package com.sun.xml.txw2;

public class TxwException
  extends RuntimeException
{
  private static final long serialVersionUID = 1L;
  
  public TxwException(String message)
  {
    super(message);
  }
  
  public TxwException(Throwable cause)
  {
    super(cause);
  }
  
  public TxwException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\txw2\TxwException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */