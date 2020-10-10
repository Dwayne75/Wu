package org.seamless.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.BitSet;

public class URIUtil
{
  public static URI createAbsoluteURI(URI base, String uri)
    throws IllegalArgumentException
  {
    return createAbsoluteURI(base, URI.create(uri));
  }
  
  public static URI createAbsoluteURI(URI base, URI relativeOrNot)
    throws IllegalArgumentException
  {
    if ((base == null) && (!relativeOrNot.isAbsolute())) {
      throw new IllegalArgumentException("Base URI is null and given URI is not absolute");
    }
    if ((base == null) && (relativeOrNot.isAbsolute())) {
      return relativeOrNot;
    }
    assert (base != null);
    if (base.getPath().length() == 0) {
      try
      {
        base = new URI(base.getScheme(), base.getAuthority(), "/", base.getQuery(), base.getFragment());
      }
      catch (Exception ex)
      {
        throw new IllegalArgumentException(ex);
      }
    }
    return base.resolve(relativeOrNot);
  }
  
  public static URL createAbsoluteURL(URL base, String uri)
    throws IllegalArgumentException
  {
    return createAbsoluteURL(base, URI.create(uri));
  }
  
  public static URL createAbsoluteURL(URL base, URI relativeOrNot)
    throws IllegalArgumentException
  {
    if ((base == null) && (!relativeOrNot.isAbsolute())) {
      throw new IllegalArgumentException("Base URL is null and given URI is not absolute");
    }
    if ((base == null) && (relativeOrNot.isAbsolute())) {
      try
      {
        return relativeOrNot.toURL();
      }
      catch (Exception ex)
      {
        throw new IllegalArgumentException("Base URL was null and given URI can't be converted to URL");
      }
    }
    try
    {
      assert (base != null);
      URI baseURI = base.toURI();
      URI absoluteURI = createAbsoluteURI(baseURI, relativeOrNot);
      return absoluteURI.toURL();
    }
    catch (Exception ex)
    {
      throw new IllegalArgumentException("Base URL is not an URI, or can't create absolute URI (null?), or absolute URI can not be converted to URL", ex);
    }
  }
  
  public static URL createAbsoluteURL(URI base, URI relativeOrNot)
    throws IllegalArgumentException
  {
    try
    {
      return createAbsoluteURI(base, relativeOrNot).toURL();
    }
    catch (Exception ex)
    {
      throw new IllegalArgumentException("Absolute URI can not be converted to URL", ex);
    }
  }
  
  public static URL createAbsoluteURL(InetAddress address, int localStreamPort, URI relativeOrNot)
    throws IllegalArgumentException
  {
    try
    {
      if ((address instanceof Inet6Address)) {
        return createAbsoluteURL(new URL("http://[" + address.getHostAddress() + "]:" + localStreamPort), relativeOrNot);
      }
      if ((address instanceof Inet4Address)) {
        return createAbsoluteURL(new URL("http://" + address.getHostAddress() + ":" + localStreamPort), relativeOrNot);
      }
      throw new IllegalArgumentException("InetAddress is neither IPv4 nor IPv6: " + address);
    }
    catch (Exception ex)
    {
      throw new IllegalArgumentException("Address, port, and URI can not be converted to URL", ex);
    }
  }
  
  public static URI createRelativePathURI(URI uri)
  {
    assertRelativeURI("Given", uri);
    
    URI normalizedURI = uri.normalize();
    
    String uriString = normalizedURI.toString();
    int idx;
    while ((idx = uriString.indexOf("../")) != -1) {
      uriString = uriString.substring(0, idx) + uriString.substring(idx + 3);
    }
    while (uriString.startsWith("/")) {
      uriString = uriString.substring(1);
    }
    return URI.create(uriString);
  }
  
  public static URI createRelativeURI(URI base, URI full)
  {
    return base.relativize(full);
  }
  
  public static URI createRelativeURI(URL base, URL full)
    throws IllegalArgumentException
  {
    try
    {
      return createRelativeURI(base.toURI(), full.toURI());
    }
    catch (Exception ex)
    {
      throw new IllegalArgumentException("Can't convert base or full URL to URI", ex);
    }
  }
  
  public static URI createRelativeURI(URI base, URL full)
    throws IllegalArgumentException
  {
    try
    {
      return createRelativeURI(base, full.toURI());
    }
    catch (Exception ex)
    {
      throw new IllegalArgumentException("Can't convert full URL to URI", ex);
    }
  }
  
  public static URI createRelativeURI(URL base, URI full)
    throws IllegalArgumentException
  {
    try
    {
      return createRelativeURI(base.toURI(), full);
    }
    catch (Exception ex)
    {
      throw new IllegalArgumentException("Can't convert base URL to URI", ex);
    }
  }
  
  public static boolean isAbsoluteURI(String s)
  {
    URI uri = URI.create(s);
    return uri.isAbsolute();
  }
  
  public static void assertRelativeURI(String what, URI uri)
  {
    if (uri.isAbsolute()) {
      throw new IllegalArgumentException(what + " URI must be relative, without scheme and authority");
    }
  }
  
  public static URL toURL(URI uri)
  {
    if (uri == null) {
      return null;
    }
    try
    {
      return uri.toURL();
    }
    catch (MalformedURLException ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  public static URI toURI(URL url)
  {
    if (url == null) {
      return null;
    }
    try
    {
      return url.toURI();
    }
    catch (URISyntaxException ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  public static String percentEncode(String s)
  {
    if (s == null) {
      return "";
    }
    try
    {
      return URLEncoder.encode(s, "UTF-8");
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  public static String percentDecode(String s)
  {
    if (s == null) {
      return "";
    }
    try
    {
      return URLDecoder.decode(s, "UTF-8");
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  public static final BitSet ALLOWED = new BitSet() {};
  public static final BitSet PATH_SEGMENT = new BitSet() {};
  public static final BitSet PATH_PARAM_NAME = new BitSet() {};
  public static final BitSet PATH_PARAM_VALUE = new BitSet() {};
  public static final BitSet QUERY = new BitSet() {};
  public static final BitSet FRAGMENT = new BitSet() {};
  
  public static String encodePathSegment(String pathSegment)
  {
    return encode(PATH_SEGMENT, pathSegment, "UTF-8");
  }
  
  public static String encodePathParamName(String pathParamName)
  {
    return encode(PATH_PARAM_NAME, pathParamName, "UTF-8");
  }
  
  public static String encodePathParamValue(String pathParamValue)
  {
    return encode(PATH_PARAM_VALUE, pathParamValue, "UTF-8");
  }
  
  public static String encodeQueryNameOrValue(String queryNameOrValue)
  {
    return encode(QUERY, queryNameOrValue, "UTF-8");
  }
  
  public static String encodeFragment(String fragment)
  {
    return encode(FRAGMENT, fragment, "UTF-8");
  }
  
  public static String encode(BitSet allowedCharacters, String s, String charset)
  {
    if (s == null) {
      return null;
    }
    StringBuilder encoded = new StringBuilder(s.length() * 3);
    char[] characters = s.toCharArray();
    try
    {
      for (char c : characters) {
        if (allowedCharacters.get(c))
        {
          encoded.append(c);
        }
        else
        {
          byte[] bytes = String.valueOf(c).getBytes(charset);
          for (byte b : bytes) {
            encoded.append(String.format("%%%1$02X", new Object[] { Integer.valueOf(b & 0xFF) }));
          }
        }
      }
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
    return encoded.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\util\URIUtil.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */