package com.sun.xml.bind.util;

import java.net.URL;

public class Which
{
  public static String which(Class clazz)
  {
    return which(clazz.getName(), clazz.getClassLoader());
  }
  
  public static String which(String classname, ClassLoader loader)
  {
    String classnameAsResource = classname.replace('.', '/') + ".class";
    if (loader == null) {
      loader = ClassLoader.getSystemClassLoader();
    }
    URL it = loader.getResource(classnameAsResource);
    if (it != null) {
      return it.toString();
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\util\Which.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */