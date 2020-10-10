package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;

public class MultipleHostsException
  extends JNLPException
{
  public MultipleHostsException()
  {
    super(ResourceManager.getString("launch.error.category.security"));
  }
  
  public String getRealMessage()
  {
    return ResourceManager.getString("launch.error.multiplehostsreferences");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\exceptions\MultipleHostsException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */