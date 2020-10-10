package org.apache.http.impl.cookie;

import java.util.Locale;
import org.apache.http.annotation.Immutable;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieAttributeHandler;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieRestrictionViolationException;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;

@Immutable
public class RFC2109DomainHandler
  implements CookieAttributeHandler
{
  public void parse(SetCookie cookie, String value)
    throws MalformedCookieException
  {
    if (cookie == null) {
      throw new IllegalArgumentException("Cookie may not be null");
    }
    if (value == null) {
      throw new MalformedCookieException("Missing value for domain attribute");
    }
    if (value.trim().length() == 0) {
      throw new MalformedCookieException("Blank value for domain attribute");
    }
    cookie.setDomain(value);
  }
  
  public void validate(Cookie cookie, CookieOrigin origin)
    throws MalformedCookieException
  {
    if (cookie == null) {
      throw new IllegalArgumentException("Cookie may not be null");
    }
    if (origin == null) {
      throw new IllegalArgumentException("Cookie origin may not be null");
    }
    String host = origin.getHost();
    String domain = cookie.getDomain();
    if (domain == null) {
      throw new CookieRestrictionViolationException("Cookie domain may not be null");
    }
    if (!domain.equals(host))
    {
      int dotIndex = domain.indexOf('.');
      if (dotIndex == -1) {
        throw new CookieRestrictionViolationException("Domain attribute \"" + domain + "\" does not match the host \"" + host + "\"");
      }
      if (!domain.startsWith(".")) {
        throw new CookieRestrictionViolationException("Domain attribute \"" + domain + "\" violates RFC 2109: domain must start with a dot");
      }
      dotIndex = domain.indexOf('.', 1);
      if ((dotIndex < 0) || (dotIndex == domain.length() - 1)) {
        throw new CookieRestrictionViolationException("Domain attribute \"" + domain + "\" violates RFC 2109: domain must contain an embedded dot");
      }
      host = host.toLowerCase(Locale.ENGLISH);
      if (!host.endsWith(domain)) {
        throw new CookieRestrictionViolationException("Illegal domain attribute \"" + domain + "\". Domain of origin: \"" + host + "\"");
      }
      String hostWithoutDomain = host.substring(0, host.length() - domain.length());
      if (hostWithoutDomain.indexOf('.') != -1) {
        throw new CookieRestrictionViolationException("Domain attribute \"" + domain + "\" violates RFC 2109: host minus domain may not contain any dots");
      }
    }
  }
  
  public boolean match(Cookie cookie, CookieOrigin origin)
  {
    if (cookie == null) {
      throw new IllegalArgumentException("Cookie may not be null");
    }
    if (origin == null) {
      throw new IllegalArgumentException("Cookie origin may not be null");
    }
    String host = origin.getHost();
    String domain = cookie.getDomain();
    if (domain == null) {
      return false;
    }
    return (host.equals(domain)) || ((domain.startsWith(".")) && (host.endsWith(domain)));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\cookie\RFC2109DomainHandler.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */