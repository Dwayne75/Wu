package org.fourthline.cling.model.message;

import java.net.InetAddress;

public abstract interface Connection
{
  public abstract boolean isOpen();
  
  public abstract InetAddress getRemoteAddress();
  
  public abstract InetAddress getLocalAddress();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\Connection.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */