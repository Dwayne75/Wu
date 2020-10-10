package org.flywaydb.core.internal.util;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.api.FlywayException;

public class ClassUtils
{
  public static synchronized <T> T instantiate(String className, ClassLoader classLoader)
    throws Exception
  {
    return (T)Class.forName(className, true, classLoader).newInstance();
  }
  
  public static <T> List<T> instantiateAll(String[] classes, ClassLoader classLoader)
  {
    List<T> clazzes = new ArrayList();
    for (String clazz : classes) {
      if (StringUtils.hasLength(clazz)) {
        try
        {
          clazzes.add(instantiate(clazz, classLoader));
        }
        catch (Exception e)
        {
          throw new FlywayException("Unable to instantiate class: " + clazz, e);
        }
      }
    }
    return clazzes;
  }
  
  public static boolean isPresent(String className, ClassLoader classLoader)
  {
    try
    {
      classLoader.loadClass(className);
      return true;
    }
    catch (Throwable ex) {}
    return false;
  }
  
  public static String getShortName(Class<?> aClass)
  {
    String name = aClass.getName();
    return name.substring(name.lastIndexOf(".") + 1);
  }
  
  public static String getLocationOnDisk(Class<?> aClass)
  {
    try
    {
      ProtectionDomain protectionDomain = aClass.getProtectionDomain();
      if (protectionDomain == null) {
        return null;
      }
      String url = protectionDomain.getCodeSource().getLocation().getPath();
      return URLDecoder.decode(url, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {}
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\ClassUtils.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */