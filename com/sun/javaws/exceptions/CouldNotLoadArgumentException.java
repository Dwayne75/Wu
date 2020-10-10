package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;

public class CouldNotLoadArgumentException
  extends JNLPException
{
  private String _argument;
  
  public CouldNotLoadArgumentException(String paramString, Exception paramException)
  {
    super(ResourceManager.getString("launch.error.category.arguments"), paramException);
    this._argument = paramString;
  }
  
  public String getRealMessage()
  {
    return ResourceManager.getString("launch.error.couldnotloadarg", this._argument);
  }
  
  public String getField()
  {
    return getMessage();
  }
  
  public String toString()
  {
    return "CouldNotLoadArgumentException[ " + getRealMessage() + "]";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\exceptions\CouldNotLoadArgumentException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */