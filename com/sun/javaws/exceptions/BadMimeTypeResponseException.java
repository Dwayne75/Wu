package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;
import java.net.URL;

public class BadMimeTypeResponseException
  extends DownloadException
{
  private String _mimeType;
  
  public BadMimeTypeResponseException(URL paramURL, String paramString1, String paramString2)
  {
    super(paramURL, paramString1);
    this._mimeType = paramString2;
  }
  
  public String getRealMessage()
  {
    return ResourceManager.getString("launch.error.badmimetyperesponse", getResourceString(), this._mimeType);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\exceptions\BadMimeTypeResponseException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */