package com.sun.javaws.exceptions;

public class ExitException
  extends Exception
{
  private int _reason;
  private Exception _exception;
  public static final int OK = 0;
  public static final int REBOOT = 1;
  public static final int LAUNCH_ERROR = 2;
  
  public ExitException(Exception paramException, int paramInt)
  {
    this._exception = paramException;
    this._reason = paramInt;
  }
  
  public Exception getException()
  {
    return this._exception;
  }
  
  public int getReason()
  {
    return this._reason;
  }
  
  public String toString()
  {
    String str = "ExitException[ " + getReason() + "]";
    if (this._exception != null) {
      str = str + this._exception.toString();
    }
    return str;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\exceptions\ExitException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */