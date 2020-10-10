package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;
import java.net.URL;

public class BadVersionResponseException
  extends DownloadException
{
  private String _responseVersionID;
  
  public BadVersionResponseException(URL paramURL, String paramString1, String paramString2)
  {
    super(paramURL, paramString1);
    this._responseVersionID = paramString2;
  }
  
  public String getRealMessage()
  {
    return ResourceManager.getString("launch.error.badversionresponse", getResourceString(), this._responseVersionID);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\exceptions\BadVersionResponseException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */