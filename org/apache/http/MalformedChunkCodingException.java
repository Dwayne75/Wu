package org.apache.http;

import java.io.IOException;

public class MalformedChunkCodingException
  extends IOException
{
  private static final long serialVersionUID = 2158560246948994524L;
  
  public MalformedChunkCodingException() {}
  
  public MalformedChunkCodingException(String message)
  {
    super(message);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\MalformedChunkCodingException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */