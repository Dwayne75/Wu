package com.sun.istack.tools;

import java.util.Collection;

public class MaskingClassLoader
  extends ClassLoader
{
  private final String[] masks;
  
  public MaskingClassLoader(String... masks)
  {
    this.masks = masks;
  }
  
  public MaskingClassLoader(Collection<String> masks)
  {
    this((String[])masks.toArray(new String[masks.size()]));
  }
  
  public MaskingClassLoader(ClassLoader parent, String... masks)
  {
    super(parent);
    this.masks = masks;
  }
  
  public MaskingClassLoader(ClassLoader parent, Collection<String> masks)
  {
    this(parent, (String[])masks.toArray(new String[masks.size()]));
  }
  
  protected synchronized Class<?> loadClass(String name, boolean resolve)
    throws ClassNotFoundException
  {
    for (String mask : this.masks) {
      if (name.startsWith(mask)) {
        throw new ClassNotFoundException();
      }
    }
    return super.loadClass(name, resolve);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\istack\tools\MaskingClassLoader.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */