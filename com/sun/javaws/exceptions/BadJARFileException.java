package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;
import java.net.URL;

public class BadJARFileException
  extends DownloadException
{
  public BadJARFileException(URL paramURL, String paramString, Exception paramException)
  {
    super(null, paramURL, paramString, paramException);
  }
  
  public String getRealMessage()
  {
    return ResourceManager.getString("launch.error.badjarfile", getResourceString());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\exceptions\BadJARFileException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */