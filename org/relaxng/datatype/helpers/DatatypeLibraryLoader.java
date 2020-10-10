package org.relaxng.datatype.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;
import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;

public class DatatypeLibraryLoader
  implements DatatypeLibraryFactory
{
  private final Service service = new Service(DatatypeLibraryFactory.class);
  
  public DatatypeLibrary createDatatypeLibrary(String paramString)
  {
    Enumeration localEnumeration = this.service.getProviders();
    while (localEnumeration.hasMoreElements())
    {
      DatatypeLibraryFactory localDatatypeLibraryFactory = (DatatypeLibraryFactory)localEnumeration.nextElement();
      DatatypeLibrary localDatatypeLibrary = localDatatypeLibraryFactory.createDatatypeLibrary(paramString);
      if (localDatatypeLibrary != null) {
        return localDatatypeLibrary;
      }
    }
    return null;
  }
  
  private static class Service
  {
    private final Class serviceClass;
    private final Enumeration configFiles;
    private Enumeration classNames = null;
    private final Vector providers = new Vector();
    private Loader loader;
    private static final int START = 0;
    private static final int IN_NAME = 1;
    private static final int IN_COMMENT = 2;
    
    public Service(Class paramClass)
    {
      try
      {
        this.loader = new Loader2();
      }
      catch (NoSuchMethodError localNoSuchMethodError)
      {
        this.loader = new Loader(null);
      }
      this.serviceClass = paramClass;
      String str = "META-INF/services/" + this.serviceClass.getName();
      this.configFiles = this.loader.getResources(str);
    }
    
    public Enumeration getProviders()
    {
      return new ProviderEnumeration(null);
    }
    
    private synchronized boolean moreProviders()
    {
      for (;;)
      {
        if (!this.configFiles.hasMoreElements()) {
          return false;
        }
        for (this.classNames = parseConfigFile((URL)this.configFiles.nextElement()); this.classNames != null; this.classNames = null) {
          while (this.classNames.hasMoreElements())
          {
            String str = (String)this.classNames.nextElement();
            try
            {
              Class localClass = this.loader.loadClass(str);
              Object localObject = localClass.newInstance();
              if (this.serviceClass.isInstance(localObject))
              {
                this.providers.addElement(localObject);
                return true;
              }
            }
            catch (ClassNotFoundException localClassNotFoundException) {}catch (InstantiationException localInstantiationException) {}catch (IllegalAccessException localIllegalAccessException) {}catch (LinkageError localLinkageError) {}
          }
        }
      }
    }
    
    private static Enumeration parseConfigFile(URL paramURL)
    {
      try
      {
        InputStream localInputStream = paramURL.openStream();
        try
        {
          localObject = new InputStreamReader(localInputStream, "UTF-8");
        }
        catch (UnsupportedEncodingException localUnsupportedEncodingException)
        {
          localObject = new InputStreamReader(localInputStream, "UTF8");
        }
        Object localObject = new BufferedReader((Reader)localObject);
        Vector localVector = new Vector();
        StringBuffer localStringBuffer = new StringBuffer();
        int i = 0;
        for (;;)
        {
          int j = ((Reader)localObject).read();
          if (j < 0) {
            break;
          }
          char c = (char)j;
          switch (c)
          {
          case '\n': 
          case '\r': 
            i = 0;
            break;
          case '\t': 
          case ' ': 
            break;
          case '#': 
            i = 2;
            break;
          default: 
            if (i != 2)
            {
              i = 1;
              localStringBuffer.append(c);
            }
            break;
          }
          if ((localStringBuffer.length() != 0) && (i != 1))
          {
            localVector.addElement(localStringBuffer.toString());
            localStringBuffer.setLength(0);
          }
        }
        if (localStringBuffer.length() != 0) {
          localVector.addElement(localStringBuffer.toString());
        }
        return localVector.elements();
      }
      catch (IOException localIOException) {}
      return null;
    }
    
    private static class Loader2
      extends DatatypeLibraryLoader.Service.Loader
    {
      private ClassLoader cl = Loader2.class.getClassLoader();
      
      Loader2()
      {
        super();
        ClassLoader localClassLoader1 = Thread.currentThread().getContextClassLoader();
        for (ClassLoader localClassLoader2 = localClassLoader1; localClassLoader2 != null; localClassLoader2 = localClassLoader2.getParent()) {
          if (localClassLoader2 == this.cl)
          {
            this.cl = localClassLoader1;
            break;
          }
        }
      }
      
      Enumeration getResources(String paramString)
      {
        try
        {
          return this.cl.getResources(paramString);
        }
        catch (IOException localIOException) {}
        return new DatatypeLibraryLoader.Service.Singleton(null, null);
      }
      
      Class loadClass(String paramString)
        throws ClassNotFoundException
      {
        return Class.forName(paramString, true, this.cl);
      }
    }
    
    private static class Loader
    {
      private Loader() {}
      
      Enumeration getResources(String paramString)
      {
        ClassLoader localClassLoader = Loader.class.getClassLoader();
        URL localURL;
        if (localClassLoader == null) {
          localURL = ClassLoader.getSystemResource(paramString);
        } else {
          localURL = localClassLoader.getResource(paramString);
        }
        return new DatatypeLibraryLoader.Service.Singleton(localURL, null);
      }
      
      Class loadClass(String paramString)
        throws ClassNotFoundException
      {
        return Class.forName(paramString);
      }
      
      Loader(DatatypeLibraryLoader.1 param1)
      {
        this();
      }
    }
    
    private static class Singleton
      implements Enumeration
    {
      private Object obj;
      
      private Singleton(Object paramObject)
      {
        this.obj = paramObject;
      }
      
      public boolean hasMoreElements()
      {
        return this.obj != null;
      }
      
      public Object nextElement()
      {
        if (this.obj == null) {
          throw new NoSuchElementException();
        }
        Object localObject = this.obj;
        this.obj = null;
        return localObject;
      }
      
      Singleton(Object paramObject, DatatypeLibraryLoader.1 param1)
      {
        this(paramObject);
      }
    }
    
    private class ProviderEnumeration
      implements Enumeration
    {
      private int nextIndex = 0;
      
      private ProviderEnumeration() {}
      
      public boolean hasMoreElements()
      {
        return (this.nextIndex < DatatypeLibraryLoader.Service.this.providers.size()) || (DatatypeLibraryLoader.Service.this.moreProviders());
      }
      
      public Object nextElement()
      {
        try
        {
          return DatatypeLibraryLoader.Service.this.providers.elementAt(this.nextIndex++);
        }
        catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException)
        {
          throw new NoSuchElementException();
        }
      }
      
      ProviderEnumeration(DatatypeLibraryLoader.1 param1)
      {
        this();
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\relaxng\datatype\helpers\DatatypeLibraryLoader.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */