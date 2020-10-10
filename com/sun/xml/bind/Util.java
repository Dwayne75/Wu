package com.sun.xml.bind;

import java.util.logging.Logger;

public abstract class Util
{
  public static Logger getClassLogger()
  {
    try
    {
      StackTraceElement[] trace = new Exception().getStackTrace();
      return Logger.getLogger(trace[1].getClassName());
    }
    catch (SecurityException _) {}
    return Logger.getLogger("com.sun.xml.bind");
  }
  
  public static String getSystemProperty(String name)
  {
    try
    {
      return System.getProperty(name);
    }
    catch (SecurityException e) {}
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\Util.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */