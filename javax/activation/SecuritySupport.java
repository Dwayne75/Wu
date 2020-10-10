package javax.activation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

class SecuritySupport
{
  public static ClassLoader getContextClassLoader()
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
  
  public static InputStream getResourceAsStream(Class c, final String name)
    throws IOException
  {
    try
    {
      (InputStream)AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        private final Class val$c;
        
        public Object run()
          throws IOException
        {
          return this.val$c.getResourceAsStream(name);
        }
      });
    }
    catch (PrivilegedActionException e)
    {
      throw ((IOException)e.getException());
    }
  }
  
  public static URL[] getResources(ClassLoader cl, final String name)
  {
    (URL[])AccessController.doPrivileged(new PrivilegedAction()
    {
      private final ClassLoader val$cl;
      
      public Object run()
      {
        URL[] ret = null;
        try
        {
          List v = new ArrayList();
          Enumeration e = this.val$cl.getResources(name);
          while ((e != null) && (e.hasMoreElements()))
          {
            URL url = (URL)e.nextElement();
            if (url != null) {
              v.add(url);
            }
          }
          if (v.size() > 0)
          {
            ret = new URL[v.size()];
            ret = (URL[])v.toArray(ret);
          }
        }
        catch (IOException ioex) {}catch (SecurityException ex) {}
        return ret;
      }
    });
  }
  
  public static URL[] getSystemResources(String name)
  {
    (URL[])AccessController.doPrivileged(new PrivilegedAction()
    {
      private final String val$name;
      
      public Object run()
      {
        URL[] ret = null;
        try
        {
          List v = new ArrayList();
          Enumeration e = ClassLoader.getSystemResources(this.val$name);
          while ((e != null) && (e.hasMoreElements()))
          {
            URL url = (URL)e.nextElement();
            if (url != null) {
              v.add(url);
            }
          }
          if (v.size() > 0)
          {
            ret = new URL[v.size()];
            ret = (URL[])v.toArray(ret);
          }
        }
        catch (IOException ioex) {}catch (SecurityException ex) {}
        return ret;
      }
    });
  }
  
  public static InputStream openStream(URL url)
    throws IOException
  {
    try
    {
      (InputStream)AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        private final URL val$url;
        
        public Object run()
          throws IOException
        {
          return this.val$url.openStream();
        }
      });
    }
    catch (PrivilegedActionException e)
    {
      throw ((IOException)e.getException());
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\activation\SecuritySupport.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */