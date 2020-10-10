package com.sun.mail.iap;

public class ParsingException
  extends ProtocolException
{
  private static final long serialVersionUID = 7756119840142724839L;
  
  public ParsingException() {}
  
  public ParsingException(String s)
  {
    super(s);
  }
  
  public ParsingException(Response r)
  {
    super(r);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\iap\ParsingException.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */