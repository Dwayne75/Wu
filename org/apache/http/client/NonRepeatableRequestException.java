package org.apache.http.client;

import org.apache.http.ProtocolException;
import org.apache.http.annotation.Immutable;

@Immutable
public class NonRepeatableRequestException
  extends ProtocolException
{
  private static final long serialVersionUID = 82685265288806048L;
  
  public NonRepeatableRequestException() {}
  
  public NonRepeatableRequestException(String message)
  {
    super(message);
  }
  
  public NonRepeatableRequestException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\client\NonRepeatableRequestException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */