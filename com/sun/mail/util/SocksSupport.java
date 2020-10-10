package com.sun.mail.util;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.Socket;

class SocksSupport
{
  public static Socket getSocket(String host, int port)
  {
    if ((host == null) || (host.length() == 0)) {
      return new Socket(Proxy.NO_PROXY);
    }
    return new Socket(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port)));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\util\SocksSupport.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */