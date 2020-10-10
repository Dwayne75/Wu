package org.apache.http.auth;

import org.apache.http.annotation.Immutable;

@Immutable
public class InvalidCredentialsException
  extends AuthenticationException
{
  private static final long serialVersionUID = -4834003835215460648L;
  
  public InvalidCredentialsException() {}
  
  public InvalidCredentialsException(String message)
  {
    super(message);
  }
  
  public InvalidCredentialsException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\auth\InvalidCredentialsException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */