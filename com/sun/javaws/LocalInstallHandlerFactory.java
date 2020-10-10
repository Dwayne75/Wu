package com.sun.javaws;

public class LocalInstallHandlerFactory
{
  public static LocalInstallHandler newInstance()
  {
    return new WinInstallHandler();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\LocalInstallHandlerFactory.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */