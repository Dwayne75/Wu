package org.apache.http.client.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.message.BasicNameValuePair;

@NotThreadSafe
public class URIBuilder
{
  private String scheme;
  private String encodedSchemeSpecificPart;
  private String encodedAuthority;
  private String userInfo;
  private String encodedUserInfo;
  private String host;
  private int port;
  private String path;
  private String encodedPath;
  private String encodedQuery;
  private List<NameValuePair> queryParams;
  private String fragment;
  private String encodedFragment;
  
  public URIBuilder()
  {
    this.port = -1;
  }
  
  public URIBuilder(String string)
    throws URISyntaxException
  {
    digestURI(new URI(string));
  }
  
  public URIBuilder(URI uri)
  {
    digestURI(uri);
  }
  
  private List<NameValuePair> parseQuery(String query, Charset charset)
  {
    if ((query != null) && (query.length() > 0)) {
      return URLEncodedUtils.parse(query, charset);
    }
    return null;
  }
  
  public URI build()
    throws URISyntaxException
  {
    return new URI(buildString());
  }
  
  private String buildString()
  {
    StringBuilder sb = new StringBuilder();
    if (this.scheme != null) {
      sb.append(this.scheme).append(':');
    }
    if (this.encodedSchemeSpecificPart != null)
    {
      sb.append(this.encodedSchemeSpecificPart);
    }
    else
    {
      if (this.encodedAuthority != null)
      {
        sb.append("//").append(this.encodedAuthority);
      }
      else if (this.host != null)
      {
        sb.append("//");
        if (this.encodedUserInfo != null) {
          sb.append(this.encodedUserInfo).append("@");
        } else if (this.userInfo != null) {
          sb.append(encodeUserInfo(this.userInfo)).append("@");
        }
        if (InetAddressUtils.isIPv6Address(this.host)) {
          sb.append("[").append(this.host).append("]");
        } else {
          sb.append(this.host);
        }
        if (this.port >= 0) {
          sb.append(":").append(this.port);
        }
      }
      if (this.encodedPath != null) {
        sb.append(normalizePath(this.encodedPath));
      } else if (this.path != null) {
        sb.append(encodePath(normalizePath(this.path)));
      }
      if (this.encodedQuery != null) {
        sb.append("?").append(this.encodedQuery);
      } else if (this.queryParams != null) {
        sb.append("?").append(encodeQuery(this.queryParams));
      }
    }
    if (this.encodedFragment != null) {
      sb.append("#").append(this.encodedFragment);
    } else if (this.fragment != null) {
      sb.append("#").append(encodeFragment(this.fragment));
    }
    return sb.toString();
  }
  
  private void digestURI(URI uri)
  {
    this.scheme = uri.getScheme();
    this.encodedSchemeSpecificPart = uri.getRawSchemeSpecificPart();
    this.encodedAuthority = uri.getRawAuthority();
    this.host = uri.getHost();
    this.port = uri.getPort();
    this.encodedUserInfo = uri.getRawUserInfo();
    this.userInfo = uri.getUserInfo();
    this.encodedPath = uri.getRawPath();
    this.path = uri.getPath();
    this.encodedQuery = uri.getRawQuery();
    this.queryParams = parseQuery(uri.getRawQuery(), Consts.UTF_8);
    this.encodedFragment = uri.getRawFragment();
    this.fragment = uri.getFragment();
  }
  
  private String encodeUserInfo(String userInfo)
  {
    return URLEncodedUtils.encUserInfo(userInfo, Consts.UTF_8);
  }
  
  private String encodePath(String path)
  {
    return URLEncodedUtils.encPath(path, Consts.UTF_8);
  }
  
  private String encodeQuery(List<NameValuePair> params)
  {
    return URLEncodedUtils.format(params, Consts.UTF_8);
  }
  
  private String encodeFragment(String fragment)
  {
    return URLEncodedUtils.encFragment(fragment, Consts.UTF_8);
  }
  
  public URIBuilder setScheme(String scheme)
  {
    this.scheme = scheme;
    return this;
  }
  
  public URIBuilder setUserInfo(String userInfo)
  {
    this.userInfo = userInfo;
    this.encodedSchemeSpecificPart = null;
    this.encodedAuthority = null;
    this.encodedUserInfo = null;
    return this;
  }
  
  public URIBuilder setUserInfo(String username, String password)
  {
    return setUserInfo(username + ':' + password);
  }
  
  public URIBuilder setHost(String host)
  {
    this.host = host;
    this.encodedSchemeSpecificPart = null;
    this.encodedAuthority = null;
    return this;
  }
  
  public URIBuilder setPort(int port)
  {
    this.port = (port < 0 ? -1 : port);
    this.encodedSchemeSpecificPart = null;
    this.encodedAuthority = null;
    return this;
  }
  
  public URIBuilder setPath(String path)
  {
    this.path = path;
    this.encodedSchemeSpecificPart = null;
    this.encodedPath = null;
    return this;
  }
  
  public URIBuilder removeQuery()
  {
    this.queryParams = null;
    this.encodedQuery = null;
    this.encodedSchemeSpecificPart = null;
    return this;
  }
  
  public URIBuilder setQuery(String query)
  {
    this.queryParams = parseQuery(query, Consts.UTF_8);
    this.encodedQuery = null;
    this.encodedSchemeSpecificPart = null;
    return this;
  }
  
  public URIBuilder addParameter(String param, String value)
  {
    if (this.queryParams == null) {
      this.queryParams = new ArrayList();
    }
    this.queryParams.add(new BasicNameValuePair(param, value));
    this.encodedQuery = null;
    this.encodedSchemeSpecificPart = null;
    return this;
  }
  
  public URIBuilder setParameter(String param, String value)
  {
    if (this.queryParams == null) {
      this.queryParams = new ArrayList();
    }
    Iterator<NameValuePair> it;
    if (!this.queryParams.isEmpty()) {
      for (it = this.queryParams.iterator(); it.hasNext();)
      {
        NameValuePair nvp = (NameValuePair)it.next();
        if (nvp.getName().equals(param)) {
          it.remove();
        }
      }
    }
    this.queryParams.add(new BasicNameValuePair(param, value));
    this.encodedQuery = null;
    this.encodedSchemeSpecificPart = null;
    return this;
  }
  
  public URIBuilder setFragment(String fragment)
  {
    this.fragment = fragment;
    this.encodedFragment = null;
    return this;
  }
  
  public String getScheme()
  {
    return this.scheme;
  }
  
  public String getUserInfo()
  {
    return this.userInfo;
  }
  
  public String getHost()
  {
    return this.host;
  }
  
  public int getPort()
  {
    return this.port;
  }
  
  public String getPath()
  {
    return this.path;
  }
  
  public List<NameValuePair> getQueryParams()
  {
    if (this.queryParams != null) {
      return new ArrayList(this.queryParams);
    }
    return new ArrayList();
  }
  
  public String getFragment()
  {
    return this.fragment;
  }
  
  public String toString()
  {
    return buildString();
  }
  
  private static String normalizePath(String path)
  {
    if (path == null) {
      return null;
    }
    for (int n = 0; n < path.length(); n++) {
      if (path.charAt(n) != '/') {
        break;
      }
    }
    if (n > 1) {
      path = path.substring(n - 1);
    }
    return path;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\client\utils\URIBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */