package org.apache.http.impl.auth;

import org.apache.http.annotation.Immutable;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.params.HttpParams;

@Immutable
public class KerberosSchemeFactory
  implements AuthSchemeFactory
{
  private final boolean stripPort;
  
  public KerberosSchemeFactory(boolean stripPort)
  {
    this.stripPort = stripPort;
  }
  
  public KerberosSchemeFactory()
  {
    this(false);
  }
  
  public AuthScheme newInstance(HttpParams params)
  {
    return new KerberosScheme(this.stripPort);
  }
  
  public boolean isStripPort()
  {
    return this.stripPort;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\auth\KerberosSchemeFactory.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */