package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;

public class BadFieldException
  extends LaunchDescException
{
  private String _field;
  private String _value;
  private String _launchDescSource;
  
  public BadFieldException(String paramString1, String paramString2, String paramString3)
  {
    this._value = paramString3;
    this._field = paramString2;
    this._launchDescSource = paramString1;
  }
  
  public String getField()
  {
    return getMessage();
  }
  
  public String getValue()
  {
    return this._value;
  }
  
  public String getRealMessage()
  {
    if (getValue().equals("https")) {
      return ResourceManager.getString("launch.error.badfield", this._field, this._value) + "\n" + ResourceManager.getString("launch.error.badfield.https");
    }
    if (!isSignedLaunchDesc()) {
      return ResourceManager.getString("launch.error.badfield", this._field, this._value);
    }
    return ResourceManager.getString("launch.error.badfield-signedjnlp", this._field, this._value);
  }
  
  public String getLaunchDescSource()
  {
    return this._launchDescSource;
  }
  
  public String toString()
  {
    if (getValue().equals("https")) {
      return "BadFieldException[ " + getRealMessage() + "]";
    }
    return "BadFieldException[ " + getField() + "," + getValue() + "]";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\exceptions\BadFieldException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */