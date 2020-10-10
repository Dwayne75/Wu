package org.apache.http.auth;

import org.apache.http.ProtocolException;
import org.apache.http.annotation.Immutable;

@Immutable
public class AuthenticationException
  extends ProtocolException
{
  private static final long serialVersionUID = -6794031905674764776L;
  
  public AuthenticationException() {}
  
  public AuthenticationException(String message)
  {
    super(message);
  }
  
  public AuthenticationException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\auth\AuthenticationException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */