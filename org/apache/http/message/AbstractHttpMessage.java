package org.apache.http.message;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpMessage;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

@NotThreadSafe
public abstract class AbstractHttpMessage
  implements HttpMessage
{
  protected HeaderGroup headergroup;
  protected HttpParams params;
  
  protected AbstractHttpMessage(HttpParams params)
  {
    this.headergroup = new HeaderGroup();
    this.params = params;
  }
  
  protected AbstractHttpMessage()
  {
    this(null);
  }
  
  public boolean containsHeader(String name)
  {
    return this.headergroup.containsHeader(name);
  }
  
  public Header[] getHeaders(String name)
  {
    return this.headergroup.getHeaders(name);
  }
  
  public Header getFirstHeader(String name)
  {
    return this.headergroup.getFirstHeader(name);
  }
  
  public Header getLastHeader(String name)
  {
    return this.headergroup.getLastHeader(name);
  }
  
  public Header[] getAllHeaders()
  {
    return this.headergroup.getAllHeaders();
  }
  
  public void addHeader(Header header)
  {
    this.headergroup.addHeader(header);
  }
  
  public void addHeader(String name, String value)
  {
    if (name == null) {
      throw new IllegalArgumentException("Header name may not be null");
    }
    this.headergroup.addHeader(new BasicHeader(name, value));
  }
  
  public void setHeader(Header header)
  {
    this.headergroup.updateHeader(header);
  }
  
  public void setHeader(String name, String value)
  {
    if (name == null) {
      throw new IllegalArgumentException("Header name may not be null");
    }
    this.headergroup.updateHeader(new BasicHeader(name, value));
  }
  
  public void setHeaders(Header[] headers)
  {
    this.headergroup.setHeaders(headers);
  }
  
  public void removeHeader(Header header)
  {
    this.headergroup.removeHeader(header);
  }
  
  public void removeHeaders(String name)
  {
    if (name == null) {
      return;
    }
    for (HeaderIterator i = this.headergroup.iterator(); i.hasNext();)
    {
      Header header = i.nextHeader();
      if (name.equalsIgnoreCase(header.getName())) {
        i.remove();
      }
    }
  }
  
  public HeaderIterator headerIterator()
  {
    return this.headergroup.iterator();
  }
  
  public HeaderIterator headerIterator(String name)
  {
    return this.headergroup.iterator(name);
  }
  
  public HttpParams getParams()
  {
    if (this.params == null) {
      this.params = new BasicHttpParams();
    }
    return this.params;
  }
  
  public void setParams(HttpParams params)
  {
    if (params == null) {
      throw new IllegalArgumentException("HTTP parameters may not be null");
    }
    this.params = params;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\message\AbstractHttpMessage.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */