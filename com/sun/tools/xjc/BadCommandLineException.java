package com.sun.tools.xjc;

import com.sun.istack.Nullable;

public class BadCommandLineException
  extends Exception
{
  private Options options;
  
  public BadCommandLineException(String msg)
  {
    super(msg);
  }
  
  public BadCommandLineException(String message, Throwable cause)
  {
    super(message, cause);
  }
  
  public BadCommandLineException()
  {
    this(null);
  }
  
  public void initOptions(Options opt)
  {
    assert (this.options == null);
    this.options = opt;
  }
  
  @Nullable
  public Options getOptions()
  {
    return this.options;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\BadCommandLineException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */