package org.apache.http.impl.client;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ConnectionBackoffStrategy;

public class DefaultBackoffStrategy
  implements ConnectionBackoffStrategy
{
  public boolean shouldBackoff(Throwable t)
  {
    return ((t instanceof SocketTimeoutException)) || ((t instanceof ConnectException));
  }
  
  public boolean shouldBackoff(HttpResponse resp)
  {
    return resp.getStatusLine().getStatusCode() == 503;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\client\DefaultBackoffStrategy.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */