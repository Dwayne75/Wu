package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;

public class MissingFieldException
  extends LaunchDescException
{
  private String _field;
  private String _launchDescSource;
  
  public MissingFieldException(String paramString1, String paramString2)
  {
    this._field = paramString2;
    this._launchDescSource = paramString1;
  }
  
  public String getRealMessage()
  {
    if (!isSignedLaunchDesc()) {
      return ResourceManager.getString("launch.error.missingfield", this._field);
    }
    return ResourceManager.getString("launch.error.missingfield-signedjnlp", this._field);
  }
  
  public String getField()
  {
    return getMessage();
  }
  
  public String getLaunchDescSource()
  {
    return this._launchDescSource;
  }
  
  public String toString()
  {
    return "MissingFieldException[ " + getField() + "]";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\exceptions\MissingFieldException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */