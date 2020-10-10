package org.apache.http.client.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Stack;
import org.apache.http.HttpHost;
import org.apache.http.annotation.Immutable;

@Immutable
public class URIUtils
{
  @Deprecated
  public static URI createURI(String scheme, String host, int port, String path, String query, String fragment)
    throws URISyntaxException
  {
    StringBuilder buffer = new StringBuilder();
    if (host != null)
    {
      if (scheme != null)
      {
        buffer.append(scheme);
        buffer.append("://");
      }
      buffer.append(host);
      if (port > 0)
      {
        buffer.append(':');
        buffer.append(port);
      }
    }
    if ((path == null) || (!path.startsWith("/"))) {
      buffer.append('/');
    }
    if (path != null) {
      buffer.append(path);
    }
    if (query != null)
    {
      buffer.append('?');
      buffer.append(query);
    }
    if (fragment != null)
    {
      buffer.append('#');
      buffer.append(fragment);
    }
    return new URI(buffer.toString());
  }
  
  public static URI rewriteURI(URI uri, HttpHost target, boolean dropFragment)
    throws URISyntaxException
  {
    if (uri == null) {
      throw new IllegalArgumentException("URI may not be null");
    }
    URIBuilder uribuilder = new URIBuilder(uri);
    if (target != null)
    {
      uribuilder.setScheme(target.getSchemeName());
      uribuilder.setHost(target.getHostName());
      uribuilder.setPort(target.getPort());
    }
    else
    {
      uribuilder.setScheme(null);
      uribuilder.setHost(null);
      uribuilder.setPort(-1);
    }
    if (dropFragment) {
      uribuilder.setFragment(null);
    }
    if ((uribuilder.getPath() == null) || (uribuilder.getPath().length() == 0)) {
      uribuilder.setPath("/");
    }
    return uribuilder.build();
  }
  
  public static URI rewriteURI(URI uri, HttpHost target)
    throws URISyntaxException
  {
    return rewriteURI(uri, target, false);
  }
  
  public static URI rewriteURI(URI uri)
    throws URISyntaxException
  {
    if (uri == null) {
      throw new IllegalArgumentException("URI may not be null");
    }
    if ((uri.getFragment() != null) || (uri.getUserInfo() != null) || (uri.getPath() == null) || (uri.getPath().length() == 0))
    {
      URIBuilder uribuilder = new URIBuilder(uri);
      uribuilder.setFragment(null).setUserInfo(null);
      if ((uribuilder.getPath() == null) || (uribuilder.getPath().length() == 0)) {
        uribuilder.setPath("/");
      }
      return uribuilder.build();
    }
    return uri;
  }
  
  public static URI resolve(URI baseURI, String reference)
  {
    return resolve(baseURI, URI.create(reference));
  }
  
  public static URI resolve(URI baseURI, URI reference)
  {
    if (baseURI == null) {
      throw new IllegalArgumentException("Base URI may nor be null");
    }
    if (reference == null) {
      throw new IllegalArgumentException("Reference URI may nor be null");
    }
    String s = reference.toString();
    if (s.startsWith("?")) {
      return resolveReferenceStartingWithQueryString(baseURI, reference);
    }
    boolean emptyReference = s.length() == 0;
    if (emptyReference) {
      reference = URI.create("#");
    }
    URI resolved = baseURI.resolve(reference);
    if (emptyReference)
    {
      String resolvedString = resolved.toString();
      resolved = URI.create(resolvedString.substring(0, resolvedString.indexOf('#')));
    }
    return normalizeSyntax(resolved);
  }
  
  private static URI resolveReferenceStartingWithQueryString(URI baseURI, URI reference)
  {
    String baseUri = baseURI.toString();
    baseUri = baseUri.indexOf('?') > -1 ? baseUri.substring(0, baseUri.indexOf('?')) : baseUri;
    
    return URI.create(baseUri + reference.toString());
  }
  
  private static URI normalizeSyntax(URI uri)
  {
    if (uri.isOpaque()) {
      return uri;
    }
    String path = uri.getPath() == null ? "" : uri.getPath();
    String[] inputSegments = path.split("/");
    Stack<String> outputSegments = new Stack();
    for (String inputSegment : inputSegments) {
      if ((inputSegment.length() != 0) && (!".".equals(inputSegment))) {
        if ("..".equals(inputSegment))
        {
          if (!outputSegments.isEmpty()) {
            outputSegments.pop();
          }
        }
        else {
          outputSegments.push(inputSegment);
        }
      }
    }
    StringBuilder outputBuffer = new StringBuilder();
    for (String outputSegment : outputSegments) {
      outputBuffer.append('/').append(outputSegment);
    }
    if (path.lastIndexOf('/') == path.length() - 1) {
      outputBuffer.append('/');
    }
    try
    {
      String scheme = uri.getScheme().toLowerCase();
      String auth = uri.getAuthority().toLowerCase();
      URI ref = new URI(scheme, auth, outputBuffer.toString(), null, null);
      if ((uri.getQuery() == null) && (uri.getFragment() == null)) {
        return ref;
      }
      StringBuilder normalized = new StringBuilder(ref.toASCIIString());
      if (uri.getQuery() != null) {
        normalized.append('?').append(uri.getRawQuery());
      }
      if (uri.getFragment() != null) {
        normalized.append('#').append(uri.getRawFragment());
      }
      return URI.create(normalized.toString());
    }
    catch (URISyntaxException e)
    {
      throw new IllegalArgumentException(e);
    }
  }
  
  public static HttpHost extractHost(URI uri)
  {
    if (uri == null) {
      return null;
    }
    HttpHost target = null;
    if (uri.isAbsolute())
    {
      int port = uri.getPort();
      String host = uri.getHost();
      if (host == null)
      {
        host = uri.getAuthority();
        if (host != null)
        {
          int at = host.indexOf('@');
          if (at >= 0) {
            if (host.length() > at + 1) {
              host = host.substring(at + 1);
            } else {
              host = null;
            }
          }
          if (host != null)
          {
            int colon = host.indexOf(':');
            if (colon >= 0)
            {
              int pos = colon + 1;
              int len = 0;
              for (int i = pos; i < host.length(); i++)
              {
                if (!Character.isDigit(host.charAt(i))) {
                  break;
                }
                len++;
              }
              if (len > 0) {
                try
                {
                  port = Integer.parseInt(host.substring(pos, pos + len));
                }
                catch (NumberFormatException ex) {}
              }
              host = host.substring(0, colon);
            }
          }
        }
      }
      String scheme = uri.getScheme();
      if (host != null) {
        target = new HttpHost(host, port, scheme);
      }
    }
    return target;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\client\utils\URIUtils.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */