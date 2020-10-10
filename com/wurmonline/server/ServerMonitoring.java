package com.wurmonline.server;

public abstract interface ServerMonitoring
{
  public abstract boolean isLagging();
  
  public abstract byte[] getExternalIp();
  
  public abstract byte[] getInternalIp();
  
  public abstract int getIntraServerPort();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\ServerMonitoring.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */