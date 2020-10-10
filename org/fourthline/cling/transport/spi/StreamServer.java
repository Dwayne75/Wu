package org.fourthline.cling.transport.spi;

import java.net.InetAddress;
import org.fourthline.cling.transport.Router;

public abstract interface StreamServer<C extends StreamServerConfiguration>
  extends Runnable
{
  public abstract void init(InetAddress paramInetAddress, Router paramRouter)
    throws InitializationException;
  
  public abstract int getPort();
  
  public abstract void stop();
  
  public abstract C getConfiguration();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\spi\StreamServer.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */