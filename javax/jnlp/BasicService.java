package javax.jnlp;

import java.net.URL;

public abstract interface BasicService
{
  public abstract URL getCodeBase();
  
  public abstract boolean isOffline();
  
  public abstract boolean showDocument(URL paramURL);
  
  public abstract boolean isWebBrowserSupported();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\jnlp\BasicService.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */