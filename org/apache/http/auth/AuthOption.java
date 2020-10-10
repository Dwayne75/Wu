package org.apache.http.auth;

import org.apache.http.annotation.Immutable;

@Immutable
public final class AuthOption
{
  private final AuthScheme authScheme;
  private final Credentials creds;
  
  public AuthOption(AuthScheme authScheme, Credentials creds)
  {
    if (authScheme == null) {
      throw new IllegalArgumentException("Auth scheme may not be null");
    }
    if (creds == null) {
      throw new IllegalArgumentException("User credentials may not be null");
    }
    this.authScheme = authScheme;
    this.creds = creds;
  }
  
  public AuthScheme getAuthScheme()
  {
    return this.authScheme;
  }
  
  public Credentials getCredentials()
  {
    return this.creds;
  }
  
  public String toString()
  {
    return this.authScheme.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\auth\AuthOption.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */