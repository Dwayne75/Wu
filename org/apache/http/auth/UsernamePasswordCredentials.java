package org.apache.http.auth;

import java.io.Serializable;
import java.security.Principal;
import org.apache.http.annotation.Immutable;
import org.apache.http.util.LangUtils;

@Immutable
public class UsernamePasswordCredentials
  implements Credentials, Serializable
{
  private static final long serialVersionUID = 243343858802739403L;
  private final BasicUserPrincipal principal;
  private final String password;
  
  public UsernamePasswordCredentials(String usernamePassword)
  {
    if (usernamePassword == null) {
      throw new IllegalArgumentException("Username:password string may not be null");
    }
    int atColon = usernamePassword.indexOf(':');
    if (atColon >= 0)
    {
      this.principal = new BasicUserPrincipal(usernamePassword.substring(0, atColon));
      this.password = usernamePassword.substring(atColon + 1);
    }
    else
    {
      this.principal = new BasicUserPrincipal(usernamePassword);
      this.password = null;
    }
  }
  
  public UsernamePasswordCredentials(String userName, String password)
  {
    if (userName == null) {
      throw new IllegalArgumentException("Username may not be null");
    }
    this.principal = new BasicUserPrincipal(userName);
    this.password = password;
  }
  
  public Principal getUserPrincipal()
  {
    return this.principal;
  }
  
  public String getUserName()
  {
    return this.principal.getName();
  }
  
  public String getPassword()
  {
    return this.password;
  }
  
  public int hashCode()
  {
    return this.principal.hashCode();
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o instanceof UsernamePasswordCredentials))
    {
      UsernamePasswordCredentials that = (UsernamePasswordCredentials)o;
      if (LangUtils.equals(this.principal, that.principal)) {
        return true;
      }
    }
    return false;
  }
  
  public String toString()
  {
    return this.principal.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\auth\UsernamePasswordCredentials.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */