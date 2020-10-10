package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;
import com.sun.javaws.jnl.LaunchDesc;

public class JNLPSigningException
  extends LaunchDescException
{
  String _signedSource = null;
  
  public JNLPSigningException(LaunchDesc paramLaunchDesc, String paramString)
  {
    super(paramLaunchDesc, null);
    this._signedSource = paramString;
  }
  
  public String getRealMessage()
  {
    return ResourceManager.getString("launch.error.badsignedjnlp");
  }
  
  public String getSignedSource()
  {
    return this._signedSource;
  }
  
  public String toString()
  {
    return "JNLPSigningException[" + getMessage() + "]";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\exceptions\JNLPSigningException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */