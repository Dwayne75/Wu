package com.sun.mail.iap;

public class ConnectionException
  extends ProtocolException
{
  private transient Protocol p;
  private static final long serialVersionUID = 5749739604257464727L;
  
  public ConnectionException() {}
  
  public ConnectionException(String s)
  {
    super(s);
  }
  
  public ConnectionException(Protocol p, Response r)
  {
    super(r);
    this.p = p;
  }
  
  public Protocol getProtocol()
  {
    return this.p;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\iap\ConnectionException.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */