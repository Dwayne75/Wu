package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;
import java.net.URL;

public class InvalidJarDiffException
  extends DownloadException
{
  public InvalidJarDiffException(URL paramURL, String paramString, Exception paramException)
  {
    super(null, paramURL, paramString, paramException);
  }
  
  public String getRealMessage()
  {
    return ResourceManager.getString("launch.error.invalidjardiff", getResourceString());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\exceptions\InvalidJarDiffException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */