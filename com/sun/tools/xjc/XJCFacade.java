package com.sun.tools.xjc;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class XJCFacade
{
  public static void main(String[] args)
    throws Throwable
  {
    String v = "2.0";
    for (int i = 0; i < args.length; i++) {
      if ((args[i].equals("-source")) && 
        (i + 1 < args.length)) {
        v = parseVersion(args[(i + 1)]);
      }
    }
    try
    {
      ClassLoader cl = ClassLoaderBuilder.createProtectiveClassLoader(XJCFacade.class.getClassLoader(), v);
      
      Class driver = cl.loadClass("com.sun.tools.xjc.Driver");
      Method mainMethod = driver.getDeclaredMethod("main", new Class[] { new String[0].getClass() });
      try
      {
        mainMethod.invoke(null, new Object[] { args });
      }
      catch (IllegalAccessException e)
      {
        throw e;
      }
      catch (InvocationTargetException e)
      {
        if (e.getTargetException() != null) {
          throw e.getTargetException();
        }
      }
    }
    catch (UnsupportedClassVersionError e)
    {
      System.err.println("XJC requires JDK 5.0 or later. Please download it from http://java.sun.com/j2se/1.5/");
    }
  }
  
  private static String parseVersion(String version)
  {
    if (version.equals("1.0")) {
      return version;
    }
    return "2.0";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\XJCFacade.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */