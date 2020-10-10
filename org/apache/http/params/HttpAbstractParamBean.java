package org.apache.http.params;

public abstract class HttpAbstractParamBean
{
  protected final HttpParams params;
  
  public HttpAbstractParamBean(HttpParams params)
  {
    if (params == null) {
      throw new IllegalArgumentException("HTTP parameters may not be null");
    }
    this.params = params;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\params\HttpAbstractParamBean.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */