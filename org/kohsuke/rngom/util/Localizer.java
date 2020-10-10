package org.kohsuke.rngom.util;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Localizer
{
  private final Class cls;
  private ResourceBundle bundle;
  private final Localizer parent;
  
  public Localizer(Class cls)
  {
    this(null, cls);
  }
  
  public Localizer(Localizer parent, Class cls)
  {
    this.parent = parent;
    this.cls = cls;
  }
  
  private String getString(String key)
  {
    try
    {
      return getBundle().getString(key);
    }
    catch (MissingResourceException e)
    {
      if (this.parent != null) {
        return this.parent.getString(key);
      }
      throw e;
    }
  }
  
  public String message(String key)
  {
    return MessageFormat.format(getString(key), new Object[0]);
  }
  
  public String message(String key, Object arg)
  {
    return MessageFormat.format(getString(key), new Object[] { arg });
  }
  
  public String message(String key, Object arg1, Object arg2)
  {
    return MessageFormat.format(getString(key), new Object[] { arg1, arg2 });
  }
  
  public String message(String key, Object[] args)
  {
    return MessageFormat.format(getString(key), args);
  }
  
  private ResourceBundle getBundle()
  {
    if (this.bundle == null)
    {
      String s = this.cls.getName();
      int i = s.lastIndexOf('.');
      if (i > 0) {
        s = s.substring(0, i + 1);
      } else {
        s = "";
      }
      this.bundle = ResourceBundle.getBundle(s + "Messages");
    }
    return this.bundle;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\util\Localizer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */