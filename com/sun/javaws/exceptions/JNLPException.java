package com.sun.javaws.exceptions;

import com.sun.javaws.jnl.LaunchDesc;

public abstract class JNLPException
  extends Exception
{
  private static LaunchDesc _defaultLaunchDesc = null;
  private LaunchDesc _exceptionLaunchDesc = null;
  private String _categoryMsg = null;
  private Throwable _wrappedException = null;
  
  public JNLPException(String paramString)
  {
    this(paramString, null, null);
  }
  
  public JNLPException(String paramString, LaunchDesc paramLaunchDesc)
  {
    this(paramString, paramLaunchDesc, null);
  }
  
  public JNLPException(String paramString, Throwable paramThrowable)
  {
    this(paramString, null, paramThrowable);
  }
  
  public JNLPException(String paramString, LaunchDesc paramLaunchDesc, Throwable paramThrowable)
  {
    this._categoryMsg = paramString;
    this._exceptionLaunchDesc = paramLaunchDesc;
    this._wrappedException = paramThrowable;
  }
  
  public static void setDefaultLaunchDesc(LaunchDesc paramLaunchDesc)
  {
    _defaultLaunchDesc = paramLaunchDesc;
  }
  
  public static LaunchDesc getDefaultLaunchDesc()
  {
    return _defaultLaunchDesc;
  }
  
  public String getMessage()
  {
    return getRealMessage();
  }
  
  public String getBriefMessage()
  {
    return null;
  }
  
  protected abstract String getRealMessage();
  
  public LaunchDesc getLaunchDesc()
  {
    return this._exceptionLaunchDesc != null ? this._exceptionLaunchDesc : _defaultLaunchDesc;
  }
  
  public String getLaunchDescSource()
  {
    LaunchDesc localLaunchDesc = getLaunchDesc();
    if (localLaunchDesc == null) {
      return null;
    }
    return localLaunchDesc.getSource();
  }
  
  public String getCategory()
  {
    return this._categoryMsg;
  }
  
  public Throwable getWrappedException()
  {
    return this._wrappedException;
  }
  
  public String toString()
  {
    return "JNLPException[category: " + this._categoryMsg + " : Exception: " + this._wrappedException + " : LaunchDesc: " + this._exceptionLaunchDesc + " ]";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\exceptions\JNLPException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */