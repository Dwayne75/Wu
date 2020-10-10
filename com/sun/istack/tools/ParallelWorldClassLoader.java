package com.sun.istack.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

public class ParallelWorldClassLoader
  extends ClassLoader
{
  private final String prefix;
  
  public ParallelWorldClassLoader(ClassLoader parent, String prefix)
  {
    super(parent);
    this.prefix = prefix;
  }
  
  protected Class findClass(String name)
    throws ClassNotFoundException
  {
    StringBuffer sb = new StringBuffer(name.length() + this.prefix.length() + 6);
    sb.append(this.prefix).append(name.replace('.', '/')).append(".class");
    
    InputStream is = getParent().getResourceAsStream(sb.toString());
    if (is == null) {
      throw new ClassNotFoundException(name);
    }
    try
    {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buf = new byte['Ѐ'];
      int len;
      while ((len = is.read(buf)) >= 0) {
        baos.write(buf, 0, len);
      }
      buf = baos.toByteArray();
      int packIndex = name.lastIndexOf('.');
      if (packIndex != -1)
      {
        String pkgname = name.substring(0, packIndex);
        
        Package pkg = getPackage(pkgname);
        if (pkg == null) {
          definePackage(pkgname, null, null, null, null, null, null, null);
        }
      }
      return defineClass(name, buf, 0, buf.length);
    }
    catch (IOException e)
    {
      throw new ClassNotFoundException(name, e);
    }
  }
  
  protected URL findResource(String name)
  {
    return getParent().getResource(this.prefix + name);
  }
  
  protected Enumeration<URL> findResources(String name)
    throws IOException
  {
    return getParent().getResources(this.prefix + name);
  }
  
  public static URL toJarUrl(URL res)
    throws ClassNotFoundException, MalformedURLException
  {
    String url = res.toExternalForm();
    if (!url.startsWith("jar:")) {
      throw new ClassNotFoundException("Loaded outside a jar " + url);
    }
    url = url.substring(4);
    url = url.substring(0, url.lastIndexOf('!'));
    return new URL(url);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\istack\tools\ParallelWorldClassLoader.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */