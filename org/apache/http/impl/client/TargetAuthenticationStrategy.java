package org.apache.http.impl.client;

import org.apache.http.annotation.Immutable;

@Immutable
public class TargetAuthenticationStrategy
  extends AuthenticationStrategyImpl
{
  public TargetAuthenticationStrategy()
  {
    super(401, "WWW-Authenticate", "http.auth.target-scheme-pref");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\client\TargetAuthenticationStrategy.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */