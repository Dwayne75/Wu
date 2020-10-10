package com.sun.activation.registries;

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogSupport
{
  private static boolean debug = false;
  private static final Level level = Level.FINE;
  
  static
  {
    try
    {
      debug = Boolean.getBoolean("javax.activation.debug");
    }
    catch (Throwable t) {}
  }
  
  private static Logger logger = Logger.getLogger("javax.activation");
  
  public static void log(String msg)
  {
    if (debug) {
      System.out.println(msg);
    }
    logger.log(level, msg);
  }
  
  public static void log(String msg, Throwable t)
  {
    if (debug) {
      System.out.println(msg + "; Exception: " + t);
    }
    logger.log(level, msg, t);
  }
  
  public static boolean isLoggable()
  {
    return (debug) || (logger.isLoggable(level));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\activation\registries\LogSupport.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */