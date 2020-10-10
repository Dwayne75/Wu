package org.fourthline.cling.model.message;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UpnpRequest
  extends UpnpOperation
{
  private Method method;
  private URI uri;
  
  public static enum Method
  {
    GET("GET"),  POST("POST"),  NOTIFY("NOTIFY"),  MSEARCH("M-SEARCH"),  SUBSCRIBE("SUBSCRIBE"),  UNSUBSCRIBE("UNSUBSCRIBE"),  UNKNOWN("UNKNOWN");
    
    private static Map<String, Method> byName = new HashMap() {};
    private String httpName;
    
    private Method(String httpName)
    {
      this.httpName = httpName;
    }
    
    public String getHttpName()
    {
      return this.httpName;
    }
    
    public static Method getByHttpName(String httpName)
    {
      if (httpName == null) {
        return UNKNOWN;
      }
      Method m = (Method)byName.get(httpName.toUpperCase(Locale.ROOT));
      return m != null ? m : UNKNOWN;
    }
  }
  
  public UpnpRequest(Method method)
  {
    this.method = method;
  }
  
  public UpnpRequest(Method method, URI uri)
  {
    this.method = method;
    this.uri = uri;
  }
  
  public UpnpRequest(Method method, URL url)
  {
    this.method = method;
    try
    {
      if (url != null) {
        this.uri = url.toURI();
      }
    }
    catch (URISyntaxException e)
    {
      throw new IllegalArgumentException(e);
    }
  }
  
  public Method getMethod()
  {
    return this.method;
  }
  
  public String getHttpMethodName()
  {
    return this.method.getHttpName();
  }
  
  public URI getURI()
  {
    return this.uri;
  }
  
  public void setUri(URI uri)
  {
    this.uri = uri;
  }
  
  public String toString()
  {
    return getHttpMethodName() + (getURI() != null ? " " + getURI() : "");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\UpnpRequest.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */