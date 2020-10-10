package org.apache.http.impl.conn;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.http.conn.DnsResolver;

public class SystemDefaultDnsResolver
  implements DnsResolver
{
  public InetAddress[] resolve(String host)
    throws UnknownHostException
  {
    return InetAddress.getAllByName(host);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\conn\SystemDefaultDnsResolver.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */