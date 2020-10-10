package org.fourthline.cling.model;

public abstract interface Command<T>
{
  public abstract void execute(ServiceManager<T> paramServiceManager)
    throws Exception;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\Command.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */