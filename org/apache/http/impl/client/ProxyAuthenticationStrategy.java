package org.apache.http.impl.client;

import org.apache.http.annotation.Immutable;

@Immutable
public class ProxyAuthenticationStrategy
  extends AuthenticationStrategyImpl
{
  public ProxyAuthenticationStrategy()
  {
    super(407, "Proxy-Authenticate", "http.auth.proxy-scheme-pref");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\client\ProxyAuthenticationStrategy.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */