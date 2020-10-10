package com.sun.tools.xjc.api.util;

import com.sun.istack.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public final class APTClassLoader
  extends URLClassLoader
{
  private final String[] packagePrefixes;
  
  public APTClassLoader(@Nullable ClassLoader parent, String[] packagePrefixes)
    throws ToolsJarNotFoundException
  {
    super(getToolsJar(parent), parent);
    if (getURLs().length == 0) {
      this.packagePrefixes = new String[0];
    } else {
      this.packagePrefixes = packagePrefixes;
    }
  }
  
  public Class loadClass(String className)
    throws ClassNotFoundException
  {
    for (String prefix : this.packagePrefixes) {
      if (className.startsWith(prefix)) {
        return findClass(className);
      }
    }
    return super.loadClass(className);
  }
  
  protected Class findClass(String name)
    throws ClassNotFoundException
  {
    StringBuilder sb = new StringBuilder(name.length() + 6);
    sb.append(name.replace('.', '/')).append(".class");
    
    InputStream is = getResourceAsStream(sb.toString());
    if (is == null) {
      throw new ClassNotFoundException("Class not found" + sb);
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
      
      int i = name.lastIndexOf('.');
      if (i != -1)
      {
        String pkgname = name.substring(0, i);
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
  
  private static URL[] getToolsJar(@Nullable ClassLoader parent)
    throws ToolsJarNotFoundException
  {
    try
    {
      Class.forName("com.sun.tools.javac.Main", false, parent);
      Class.forName("com.sun.tools.apt.Main", false, parent);
      return new URL[0];
    }
    catch (ClassNotFoundException e)
    {
      File jreHome = new File(System.getProperty("java.home"));
      File toolsJar = new File(jreHome.getParent(), "lib/tools.jar");
      if (!toolsJar.exists()) {
        throw new ToolsJarNotFoundException(toolsJar);
      }
      try
      {
        return new URL[] { toolsJar.toURL() };
      }
      catch (MalformedURLException e)
      {
        throw new AssertionError(e);
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\util\APTClassLoader.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */