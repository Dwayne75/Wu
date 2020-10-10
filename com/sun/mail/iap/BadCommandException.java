package com.sun.mail.iap;

public class BadCommandException
  extends ProtocolException
{
  private static final long serialVersionUID = 5769722539397237515L;
  
  public BadCommandException() {}
  
  public BadCommandException(String s)
  {
    super(s);
  }
  
  public BadCommandException(Response r)
  {
    super(r);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\iap\BadCommandException.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */