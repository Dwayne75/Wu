package org.apache.http;

import java.io.IOException;

public class NoHttpResponseException
  extends IOException
{
  private static final long serialVersionUID = -7658940387386078766L;
  
  public NoHttpResponseException(String message)
  {
    super(message);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\NoHttpResponseException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */