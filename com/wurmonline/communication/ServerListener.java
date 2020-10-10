package com.wurmonline.communication;

public abstract interface ServerListener
{
  public abstract void clientConnected(SocketConnection paramSocketConnection);
  
  public abstract void clientException(SocketConnection paramSocketConnection, Exception paramException);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\communication\ServerListener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */