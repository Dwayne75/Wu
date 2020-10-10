package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;
import com.sun.javaws.jnl.LaunchDesc;
import java.net.URL;

public class FailedDownloadingResourceException
  extends DownloadException
{
  public FailedDownloadingResourceException(LaunchDesc paramLaunchDesc, URL paramURL, String paramString, Exception paramException)
  {
    super(paramLaunchDesc, paramURL, paramString, paramException);
  }
  
  public FailedDownloadingResourceException(URL paramURL, String paramString, Exception paramException)
  {
    this(null, paramURL, paramString, paramException);
  }
  
  public String getRealMessage()
  {
    return ResourceManager.getString("launch.error.failedloadingresource", getResourceString());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\exceptions\FailedDownloadingResourceException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */