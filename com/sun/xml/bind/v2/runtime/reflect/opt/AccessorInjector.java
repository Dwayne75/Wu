package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.Util;
import com.sun.xml.bind.v2.bytecode.ClassTailor;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

class AccessorInjector
{
  private static final Logger logger = ;
  protected static final boolean noOptimize = Util.getSystemProperty(ClassTailor.class.getName() + ".noOptimize") != null;
  
  static
  {
    if (noOptimize) {
      logger.info("The optimized code generation is disabled");
    }
  }
  
  public static Class<?> prepare(Class beanClass, String templateClassName, String newClassName, String... replacements)
  {
    if (noOptimize) {
      return null;
    }
    try
    {
      ClassLoader cl = beanClass.getClassLoader();
      if (cl == null) {
        return null;
      }
      Class c = Injector.find(cl, newClassName);
      byte[] image;
      if (c == null)
      {
        image = tailor(templateClassName, newClassName, replacements);
        if (image == null) {
          return null;
        }
      }
      return Injector.inject(cl, newClassName, image);
    }
    catch (SecurityException e)
    {
      logger.log(Level.INFO, "Unable to create an optimized TransducedAccessor ", e);
    }
    return null;
  }
  
  private static byte[] tailor(String templateClassName, String newClassName, String... replacements)
  {
    InputStream resource;
    InputStream resource;
    if (CLASS_LOADER != null) {
      resource = CLASS_LOADER.getResourceAsStream(templateClassName + ".class");
    } else {
      resource = ClassLoader.getSystemResourceAsStream(templateClassName + ".class");
    }
    if (resource == null) {
      return null;
    }
    return ClassTailor.tailor(resource, templateClassName, newClassName, replacements);
  }
  
  private static final ClassLoader CLASS_LOADER = AccessorInjector.class.getClassLoader();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\opt\AccessorInjector.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */