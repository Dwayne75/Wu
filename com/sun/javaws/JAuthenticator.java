package com.sun.javaws;

import com.sun.deploy.config.Config;
import com.sun.deploy.security.DeployAuthenticator;
import java.awt.Frame;
import java.net.PasswordAuthentication;

public class JAuthenticator
  extends DeployAuthenticator
{
  private static JAuthenticator _instance;
  private boolean _challanging = false;
  private boolean _cancel = false;
  
  public static synchronized JAuthenticator getInstance(Frame paramFrame)
  {
    if (_instance == null) {
      _instance = new JAuthenticator();
    }
    _instance.setParentFrame(paramFrame);
    return _instance;
  }
  
  protected synchronized PasswordAuthentication getPasswordAuthentication()
  {
    PasswordAuthentication localPasswordAuthentication = null;
    if (Config.getBooleanProperty("deployment.security.authenticator"))
    {
      this._challanging = true;
      
      localPasswordAuthentication = super.getPasswordAuthentication();
      
      this._challanging = false;
    }
    return localPasswordAuthentication;
  }
  
  boolean isChallanging()
  {
    return this._challanging;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\JAuthenticator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */