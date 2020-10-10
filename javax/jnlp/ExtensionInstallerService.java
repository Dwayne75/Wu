package javax.jnlp;

import java.net.URL;

public abstract interface ExtensionInstallerService
{
  public abstract String getInstallPath();
  
  public abstract String getExtensionVersion();
  
  public abstract URL getExtensionLocation();
  
  public abstract void hideProgressBar();
  
  public abstract void hideStatusWindow();
  
  public abstract void setHeading(String paramString);
  
  public abstract void setStatus(String paramString);
  
  public abstract void updateProgress(int paramInt);
  
  public abstract void installSucceeded(boolean paramBoolean);
  
  public abstract void installFailed();
  
  public abstract void setJREInfo(String paramString1, String paramString2);
  
  public abstract void setNativeLibraryInfo(String paramString);
  
  public abstract String getInstalledJRE(URL paramURL, String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\jnlp\ExtensionInstallerService.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */