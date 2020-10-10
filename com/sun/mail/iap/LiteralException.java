package com.sun.mail.iap;

public class LiteralException
  extends ProtocolException
{
  private static final long serialVersionUID = -6919179828339609913L;
  
  public LiteralException(Response r)
  {
    super(r.toString());
    this.response = r;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\iap\LiteralException.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */