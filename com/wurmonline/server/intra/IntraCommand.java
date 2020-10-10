package com.wurmonline.server.intra;

import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import java.util.logging.Logger;

public abstract class IntraCommand
  implements IntraServerConnectionListener
{
  long startTime;
  long timeOutAt;
  long timeOutTime = 10000L;
  private static int nums = 0;
  static int num = 0;
  public int pollTimes = 0;
  static final Logger logger2 = Logger.getLogger("IntraServer");
  
  IntraCommand()
  {
    num = nums++;
    
    this.startTime = System.currentTimeMillis();
    this.timeOutAt = (this.startTime + this.timeOutTime);
  }
  
  public abstract boolean poll();
  
  public abstract void commandExecuted(IntraClient paramIntraClient);
  
  public abstract void commandFailed(IntraClient paramIntraClient);
  
  public abstract void dataReceived(IntraClient paramIntraClient);
  
  boolean isThisLoginServer()
  {
    return Servers.isThisLoginServer();
  }
  
  String getLoginServerIntraServerAddress()
  {
    return Servers.loginServer.INTRASERVERADDRESS;
  }
  
  String getLoginServerIntraServerPort()
  {
    return Servers.loginServer.INTRASERVERPORT;
  }
  
  String getLoginServerIntraServerPassword()
  {
    return Servers.loginServer.INTRASERVERPASSWORD;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\intra\IntraCommand.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */