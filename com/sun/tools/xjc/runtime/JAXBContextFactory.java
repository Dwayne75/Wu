package com.sun.tools.xjc.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class JAXBContextFactory
{
  private static final String DOT_OBJECT_FACTORY = ".ObjectFactory";
  private static final String IMPL_DOT_OBJECT_FACTORY = ".impl.ObjectFactory";
  
  public static JAXBContext createContext(Class[] classes, Map properties)
    throws JAXBException
  {
    Class[] r = new Class[classes.length];
    boolean modified = false;
    for (int i = 0; i < r.length; i++)
    {
      Class c = classes[i];
      String name = c.getName();
      if ((name.endsWith(".ObjectFactory")) && (!name.endsWith(".impl.ObjectFactory")))
      {
        name = name.substring(0, name.length() - ".ObjectFactory".length()) + ".impl.ObjectFactory";
        try
        {
          c = c.getClassLoader().loadClass(name);
        }
        catch (ClassNotFoundException e)
        {
          throw new JAXBException(e);
        }
        modified = true;
      }
      r[i] = c;
    }
    if (!modified) {
      throw new JAXBException("Unable to find a JAXB implementation to delegate");
    }
    return JAXBContext.newInstance(r, properties);
  }
  
  public static JAXBContext createContext(String contextPath, ClassLoader classLoader, Map properties)
    throws JAXBException
  {
    List<Class> classes = new ArrayList();
    StringTokenizer tokens = new StringTokenizer(contextPath, ":");
    try
    {
      while (tokens.hasMoreTokens())
      {
        String pkg = tokens.nextToken();
        classes.add(classLoader.loadClass(pkg + ".impl.ObjectFactory"));
      }
    }
    catch (ClassNotFoundException e)
    {
      throw new JAXBException(e);
    }
    return JAXBContext.newInstance((Class[])classes.toArray(new Class[classes.size()]), properties);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\runtime\JAXBContextFactory.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */