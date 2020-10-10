package com.sun.javaws;

import com.sun.deploy.config.Config;
import java.net.URL;

public class WinBrowserSupport
  extends BrowserSupport
{
  public String getNS6MailCapInfo()
  {
    return null;
  }
  
  public OperaSupport getOperaSupport()
  {
    return new WinOperaSupport(Config.getBooleanProperty("deployment.mime.types.use.default"));
  }
  
  public boolean isWebBrowserSupportedImpl()
  {
    return true;
  }
  
  public boolean showDocumentImpl(URL paramURL)
  {
    if (paramURL == null) {
      return false;
    }
    return showDocument(paramURL.toString());
  }
  
  public String getDefaultHandler(URL paramURL)
  {
    return Config.getInstance().getBrowserPath();
  }
  
  public boolean showDocument(String paramString)
  {
    return Config.getInstance().showDocument(paramString);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\WinBrowserSupport.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */