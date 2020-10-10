package org.apache.http.conn;

import java.net.ConnectException;
import org.apache.http.HttpHost;
import org.apache.http.annotation.Immutable;

@Immutable
public class HttpHostConnectException
  extends ConnectException
{
  private static final long serialVersionUID = -3194482710275220224L;
  private final HttpHost host;
  
  public HttpHostConnectException(HttpHost host, ConnectException cause)
  {
    super("Connection to " + host + " refused");
    this.host = host;
    initCause(cause);
  }
  
  public HttpHost getHost()
  {
    return this.host;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\conn\HttpHostConnectException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */