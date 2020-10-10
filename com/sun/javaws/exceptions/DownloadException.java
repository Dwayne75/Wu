package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;
import com.sun.javaws.jnl.LaunchDesc;
import java.net.URL;

public class DownloadException
  extends JNLPException
{
  private URL _location;
  private String _version;
  private String _message;
  
  public DownloadException(URL paramURL, String paramString)
  {
    this(null, paramURL, paramString, null);
  }
  
  protected DownloadException(LaunchDesc paramLaunchDesc, URL paramURL, String paramString, Exception paramException)
  {
    super(ResourceManager.getString("launch.error.category.download"), paramLaunchDesc, paramException);
    this._location = paramURL;
    this._version = paramString;
  }
  
  public URL getLocation()
  {
    return this._location;
  }
  
  public String getVersion()
  {
    return this._version;
  }
  
  public String getResourceString()
  {
    String str = this._location.toString();
    if (this._version == null) {
      return ResourceManager.getString("launch.error.resourceID", str);
    }
    return ResourceManager.getString("launch.error.resourceID-version", str, this._version);
  }
  
  public String getRealMessage()
  {
    return this._message;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\exceptions\DownloadException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */