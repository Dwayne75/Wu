package org.apache.http.impl.client;

import org.apache.http.HttpResponse;
import org.apache.http.client.ConnectionBackoffStrategy;

public class NullBackoffStrategy
  implements ConnectionBackoffStrategy
{
  public boolean shouldBackoff(Throwable t)
  {
    return false;
  }
  
  public boolean shouldBackoff(HttpResponse resp)
  {
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\client\NullBackoffStrategy.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */