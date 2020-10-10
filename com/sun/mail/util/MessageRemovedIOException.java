package com.sun.mail.util;

import java.io.IOException;

public class MessageRemovedIOException
  extends IOException
{
  private static final long serialVersionUID = 4280468026581616424L;
  
  public MessageRemovedIOException() {}
  
  public MessageRemovedIOException(String s)
  {
    super(s);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\util\MessageRemovedIOException.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */