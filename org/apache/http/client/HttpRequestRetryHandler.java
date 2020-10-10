package org.apache.http.client;

import java.io.IOException;
import org.apache.http.protocol.HttpContext;

public abstract interface HttpRequestRetryHandler
{
  public abstract boolean retryRequest(IOException paramIOException, int paramInt, HttpContext paramHttpContext);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\client\HttpRequestRetryHandler.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */