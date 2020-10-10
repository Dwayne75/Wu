package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;

public class CacheAccessException
  extends JNLPException
{
  private String _message;
  
  public CacheAccessException(boolean paramBoolean)
  {
    super(ResourceManager.getString("launch.error.category.config"));
    if (paramBoolean) {
      this._message = ResourceManager.getString("launch.error.cant.access.system.cache");
    } else {
      this._message = ResourceManager.getString("launch.error.cant.access.user.cache");
    }
  }
  
  public String getRealMessage()
  {
    return this._message;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\exceptions\CacheAccessException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */