package org.apache.xml.resolver.helpers;

import java.io.PrintStream;
import org.apache.xml.resolver.CatalogManager;

public class Debug
{
  protected static int debug = ;
  
  public static void setDebug(int paramInt)
  {
    debug = paramInt;
  }
  
  public static int getDebug()
  {
    return debug;
  }
  
  public static void message(int paramInt, String paramString)
  {
    if (debug >= paramInt) {
      System.out.println(paramString);
    }
  }
  
  public static void message(int paramInt, String paramString1, String paramString2)
  {
    if (debug >= paramInt) {
      System.out.println(paramString1 + ": " + paramString2);
    }
  }
  
  public static void message(int paramInt, String paramString1, String paramString2, String paramString3)
  {
    if (debug >= paramInt)
    {
      System.out.println(paramString1 + ": " + paramString2);
      System.out.println("\t" + paramString3);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\org\apache\xml\resolver\helpers\Debug.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */