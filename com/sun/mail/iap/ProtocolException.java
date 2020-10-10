package com.sun.mail.iap;

public class ProtocolException
  extends Exception
{
  protected transient Response response = null;
  private static final long serialVersionUID = -4360500807971797439L;
  
  public ProtocolException() {}
  
  public ProtocolException(String message)
  {
    super(message);
  }
  
  public ProtocolException(String message, Throwable cause)
  {
    super(message, cause);
  }
  
  public ProtocolException(Response r)
  {
    super(r.toString());
    this.response = r;
  }
  
  public Response getResponse()
  {
    return this.response;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\iap\ProtocolException.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */