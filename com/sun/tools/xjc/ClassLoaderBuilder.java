package com.sun.tools.xjc;

import com.sun.istack.tools.MaskingClassLoader;
import com.sun.istack.tools.ParallelWorldClassLoader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXBContext;

class ClassLoaderBuilder
{
  protected static ClassLoader createProtectiveClassLoader(ClassLoader cl, String v)
    throws ClassNotFoundException, MalformedURLException
  {
    if (noHack) {
      return cl;
    }
    boolean mustang = false;
    if (JAXBContext.class.getClassLoader() == null)
    {
      mustang = true;
      
      List mask = new ArrayList(Arrays.asList(maskedPackages));
      mask.add("javax.xml.bind.");
      
      cl = new MaskingClassLoader(cl, mask);
      
      URL apiUrl = cl.getResource("javax/xml/bind/annotation/XmlSeeAlso.class");
      if (apiUrl == null) {
        throw new ClassNotFoundException("There's no JAXB 2.1 API in the classpath");
      }
      cl = new URLClassLoader(new URL[] { ParallelWorldClassLoader.toJarUrl(apiUrl) }, cl);
    }
    if (v.equals("1.0"))
    {
      if (!mustang) {
        cl = new MaskingClassLoader(cl, toolPackages);
      }
      cl = new ParallelWorldClassLoader(cl, "1.0/");
    }
    else if (mustang)
    {
      cl = new ParallelWorldClassLoader(cl, "");
    }
    return cl;
  }
  
  private static String[] maskedPackages = { "com.sun.tools.", "com.sun.codemodel.", "com.sun.relaxng.", "com.sun.xml.xsom.", "com.sun.xml.bind." };
  private static String[] toolPackages = { "com.sun.tools.", "com.sun.codemodel.", "com.sun.relaxng.", "com.sun.xml.xsom." };
  public static final boolean noHack = Boolean.getBoolean(XJCFacade.class.getName() + ".nohack");
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\ClassLoaderBuilder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */