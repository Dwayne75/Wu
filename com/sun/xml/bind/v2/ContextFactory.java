package com.sun.xml.bind.v2;

import com.sun.istack.FinalArrayList;
import com.sun.xml.bind.Util;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.util.TypeCast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class ContextFactory
{
  public static final String USE_JAXB_PROPERTIES = "_useJAXBProperties";
  
  public static JAXBContext createContext(Class[] classes, Map<String, Object> properties)
    throws JAXBException
  {
    if (properties == null) {
      properties = Collections.emptyMap();
    } else {
      properties = new HashMap(properties);
    }
    String defaultNsUri = (String)getPropertyValue(properties, "com.sun.xml.bind.defaultNamespaceRemap", String.class);
    
    Boolean c14nSupport = (Boolean)getPropertyValue(properties, "com.sun.xml.bind.c14n", Boolean.class);
    if (c14nSupport == null) {
      c14nSupport = Boolean.valueOf(false);
    }
    Boolean allNillable = (Boolean)getPropertyValue(properties, "com.sun.xml.bind.treatEverythingNillable", Boolean.class);
    if (allNillable == null) {
      allNillable = Boolean.valueOf(false);
    }
    Boolean xmlAccessorFactorySupport = (Boolean)getPropertyValue(properties, "com.sun.xml.bind.XmlAccessorFactory", Boolean.class);
    if (xmlAccessorFactorySupport == null)
    {
      xmlAccessorFactorySupport = Boolean.valueOf(false);
      Util.getClassLogger().log(Level.FINE, "Property com.sun.xml.bind.XmlAccessorFactoryis not active.  Using JAXB's implementation");
    }
    RuntimeAnnotationReader ar = (RuntimeAnnotationReader)getPropertyValue(properties, JAXBRIContext.ANNOTATION_READER, RuntimeAnnotationReader.class);
    Map<Class, Class> subclassReplacements;
    try
    {
      subclassReplacements = TypeCast.checkedCast((Map)getPropertyValue(properties, "com.sun.xml.bind.subclassReplacements", Map.class), Class.class, Class.class);
    }
    catch (ClassCastException e)
    {
      throw new JAXBException(Messages.INVALID_TYPE_IN_MAP.format(new Object[0]), e);
    }
    if (!properties.isEmpty()) {
      throw new JAXBException(Messages.UNSUPPORTED_PROPERTY.format(new Object[] { properties.keySet().iterator().next() }));
    }
    return createContext(classes, Collections.emptyList(), subclassReplacements, defaultNsUri, c14nSupport.booleanValue(), ar, xmlAccessorFactorySupport.booleanValue(), allNillable.booleanValue());
  }
  
  private static <T> T getPropertyValue(Map<String, Object> properties, String keyName, Class<T> type)
    throws JAXBException
  {
    Object o = properties.get(keyName);
    if (o == null) {
      return null;
    }
    properties.remove(keyName);
    if (!type.isInstance(o)) {
      throw new JAXBException(Messages.INVALID_PROPERTY_VALUE.format(new Object[] { keyName, o }));
    }
    return (T)type.cast(o);
  }
  
  public static JAXBRIContext createContext(Class[] classes, Collection<TypeReference> typeRefs, Map<Class, Class> subclassReplacements, String defaultNsUri, boolean c14nSupport, RuntimeAnnotationReader ar, boolean xmlAccessorFactorySupport, boolean allNillable)
    throws JAXBException
  {
    return new JAXBContextImpl(classes, typeRefs, subclassReplacements, defaultNsUri, c14nSupport, ar, xmlAccessorFactorySupport, allNillable);
  }
  
  public static JAXBContext createContext(String contextPath, ClassLoader classLoader, Map<String, Object> properties)
    throws JAXBException
  {
    FinalArrayList<Class> classes = new FinalArrayList();
    StringTokenizer tokens = new StringTokenizer(contextPath, ":");
    while (tokens.hasMoreTokens())
    {
      boolean foundJaxbIndex;
      boolean foundObjectFactory = foundJaxbIndex = 0;
      String pkg = tokens.nextToken();
      try
      {
        Class<?> o = classLoader.loadClass(pkg + ".ObjectFactory");
        classes.add(o);
        foundObjectFactory = true;
      }
      catch (ClassNotFoundException e) {}
      List<Class> indexedClasses;
      try
      {
        indexedClasses = loadIndexedClasses(pkg, classLoader);
      }
      catch (IOException e)
      {
        throw new JAXBException(e);
      }
      if (indexedClasses != null)
      {
        classes.addAll(indexedClasses);
        foundJaxbIndex = true;
      }
      if ((!foundObjectFactory) && (!foundJaxbIndex)) {
        throw new JAXBException(Messages.BROKEN_CONTEXTPATH.format(new Object[] { pkg }));
      }
    }
    return createContext((Class[])classes.toArray(new Class[classes.size()]), properties);
  }
  
  private static List<Class> loadIndexedClasses(String pkg, ClassLoader classLoader)
    throws IOException, JAXBException
  {
    String resource = pkg.replace('.', '/') + "/jaxb.index";
    InputStream resourceAsStream = classLoader.getResourceAsStream(resource);
    if (resourceAsStream == null) {
      return null;
    }
    BufferedReader in = new BufferedReader(new InputStreamReader(resourceAsStream, "UTF-8"));
    try
    {
      FinalArrayList<Class> classes = new FinalArrayList();
      String className = in.readLine();
      while (className != null)
      {
        className = className.trim();
        if ((className.startsWith("#")) || (className.length() == 0))
        {
          className = in.readLine();
        }
        else
        {
          if (className.endsWith(".class")) {
            throw new JAXBException(Messages.ILLEGAL_ENTRY.format(new Object[] { className }));
          }
          try
          {
            classes.add(classLoader.loadClass(pkg + '.' + className));
          }
          catch (ClassNotFoundException e)
          {
            throw new JAXBException(Messages.ERROR_LOADING_CLASS.format(new Object[] { className, resource }), e);
          }
          className = in.readLine();
        }
      }
      return classes;
    }
    finally
    {
      in.close();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\ContextFactory.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */