package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;

public class OfflineLaunchException
  extends JNLPException
{
  public OfflineLaunchException()
  {
    super(ResourceManager.getString("launch.error.category.download"));
  }
  
  public String getRealMessage()
  {
    return ResourceManager.getString("launch.error.offlinemissingresource");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\exceptions\OfflineLaunchException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */