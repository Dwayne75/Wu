package com.sun.mail.iap;

public class CommandFailedException
  extends ProtocolException
{
  private static final long serialVersionUID = 793932807880443631L;
  
  public CommandFailedException() {}
  
  public CommandFailedException(String s)
  {
    super(s);
  }
  
  public CommandFailedException(Response r)
  {
    super(r);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\iap\CommandFailedException.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */