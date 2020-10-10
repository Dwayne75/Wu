package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;
import com.sun.javaws.jnl.LaunchDesc;

public class LaunchDescException
  extends JNLPException
{
  private String _message;
  private boolean _isSignedLaunchDesc;
  
  public LaunchDescException()
  {
    this(null);
  }
  
  public LaunchDescException(Exception paramException)
  {
    this(null, paramException);
  }
  
  public void setIsSignedLaunchDesc()
  {
    this._isSignedLaunchDesc = true;
  }
  
  public boolean isSignedLaunchDesc()
  {
    return this._isSignedLaunchDesc;
  }
  
  public LaunchDescException(LaunchDesc paramLaunchDesc, Exception paramException)
  {
    super(ResourceManager.getString("launch.error.category.launchdesc"), paramLaunchDesc, paramException);
  }
  
  public LaunchDescException(LaunchDesc paramLaunchDesc, String paramString, Exception paramException)
  {
    this(paramLaunchDesc, paramException);
    this._message = paramString;
  }
  
  public String getRealMessage()
  {
    return this._message;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\exceptions\LaunchDescException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */