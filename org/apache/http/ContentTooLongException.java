package org.apache.http;

import java.io.IOException;

public class ContentTooLongException
  extends IOException
{
  private static final long serialVersionUID = -924287689552495383L;
  
  public ContentTooLongException(String message)
  {
    super(message);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\ContentTooLongException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */