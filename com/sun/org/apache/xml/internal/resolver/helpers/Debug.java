package com.sun.org.apache.xml.internal.resolver.helpers;

import java.io.PrintStream;

public class Debug
{
  protected int debug = 0;
  
  public void setDebug(int newDebug)
  {
    this.debug = newDebug;
  }
  
  public int getDebug()
  {
    return this.debug;
  }
  
  public void message(int level, String message)
  {
    if (this.debug >= level) {
      System.out.println(message);
    }
  }
  
  public void message(int level, String message, String spec)
  {
    if (this.debug >= level) {
      System.out.println(message + ": " + spec);
    }
  }
  
  public void message(int level, String message, String spec1, String spec2)
  {
    if (this.debug >= level)
    {
      System.out.println(message + ": " + spec1);
      System.out.println("\t" + spec2);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\org\apache\xml\internal\resolver\helpers\Debug.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */