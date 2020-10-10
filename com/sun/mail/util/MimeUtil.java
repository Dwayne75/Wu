package com.sun.mail.util;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.mail.internet.MimePart;

public class MimeUtil
{
  private static final Method cleanContentType;
  
  static
  {
    Method meth = null;
    try
    {
      String cth = System.getProperty("mail.mime.contenttypehandler");
      if (cth != null)
      {
        ClassLoader cl = getContextClassLoader();
        Class clsHandler = null;
        if (cl != null) {
          try
          {
            clsHandler = Class.forName(cth, false, cl);
          }
          catch (ClassNotFoundException cex) {}
        }
        if (clsHandler == null) {
          clsHandler = Class.forName(cth);
        }
        meth = clsHandler.getMethod("cleanContentType", new Class[] { MimePart.class, String.class });
      }
    }
    catch (ClassNotFoundException ex) {}catch (NoSuchMethodException ex) {}catch (RuntimeException ex) {}finally
    {
      cleanContentType = meth;
    }
  }
  
  public static String cleanContentType(MimePart mp, String contentType)
  {
    if (cleanContentType != null) {
      try
      {
        return (String)cleanContentType.invoke(null, new Object[] { mp, contentType });
      }
      catch (Exception ex)
      {
        return contentType;
      }
    }
    return contentType;
  }
  
  private static ClassLoader getContextClassLoader()
  {
    (ClassLoader)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        ClassLoader cl = null;
        try
        {
          cl = Thread.currentThread().getContextClassLoader();
        }
        catch (SecurityException ex) {}
        return cl;
      }
    });
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\util\MimeUtil.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */