package org.apache.http.protocol;

public abstract interface HttpRequestHandlerResolver
{
  public abstract HttpRequestHandler lookup(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\protocol\HttpRequestHandlerResolver.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */