package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;
import java.net.URL;

public class MissingVersionResponseException
  extends DownloadException
{
  public MissingVersionResponseException(URL paramURL, String paramString)
  {
    super(paramURL, paramString);
  }
  
  public String getRealMessage()
  {
    return ResourceManager.getString("launch.error.missingversionresponse", getResourceString());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\exceptions\MissingVersionResponseException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */