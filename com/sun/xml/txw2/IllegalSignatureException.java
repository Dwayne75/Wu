package com.sun.xml.txw2;

public class IllegalSignatureException
  extends TxwException
{
  private static final long serialVersionUID = 1L;
  
  public IllegalSignatureException(String message)
  {
    super(message);
  }
  
  public IllegalSignatureException(String message, Throwable cause)
  {
    super(message, cause);
  }
  
  public IllegalSignatureException(Throwable cause)
  {
    super(cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\txw2\IllegalSignatureException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */