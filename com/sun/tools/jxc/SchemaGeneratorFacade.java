package com.sun.tools.jxc;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SchemaGeneratorFacade
{
  public static void main(String[] args)
    throws Throwable
  {
    try
    {
      ClassLoader cl = SchemaGeneratorFacade.class.getClassLoader();
      if (cl == null) {
        cl = ClassLoader.getSystemClassLoader();
      }
      Class driver = cl.loadClass("com.sun.tools.jxc.SchemaGenerator");
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
      System.err.println("schemagen requires JDK 5.0 or later. Please download it from http://java.sun.com/j2se/1.5/");
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\jxc\SchemaGeneratorFacade.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */