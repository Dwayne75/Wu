package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;

public class JreExecException
  extends JNLPException
{
  private String _version;
  
  public JreExecException(String paramString, Exception paramException)
  {
    super(ResourceManager.getString("launch.error.category.unexpected"), paramException);
    this._version = paramString;
  }
  
  public String getRealMessage()
  {
    return ResourceManager.getString("launch.error.failedexec", this._version);
  }
  
  public String toString()
  {
    return "JreExecException[ " + getMessage() + "]";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\exceptions\JreExecException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */