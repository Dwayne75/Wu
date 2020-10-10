package com.sun.tools.xjc.reader.decorator;

import java.text.MessageFormat;
import java.util.ResourceBundle;

class Messages
{
  static final String ERR_INVALID_COLLECTION_TYPE = "InvalidCollectionType";
  static final String ERR_INVALID_ACCESS_MODIFIER = "InvalidAccessModifier";
  static final String ERR_INVALID_ACCESSOR = "InvalidAccessor";
  static final String ERR_UNDEFINED_ROLE = "UndefinedRole";
  static final String ERR_CLASS_NOT_FOUND = "ClassNotFound";
  static final String ERR_NAME_NEEDED = "NameNeeded";
  
  static String format(String property)
  {
    return format(property, null);
  }
  
  static String format(String property, Object arg1)
  {
    return format(property, new Object[] { arg1 });
  }
  
  static String format(String property, Object arg1, Object arg2)
  {
    return format(property, new Object[] { arg1, arg2 });
  }
  
  static String format(String property, Object arg1, Object arg2, Object arg3)
  {
    return format(property, new Object[] { arg1, arg2, arg3 });
  }
  
  static String format(String property, Object[] args)
  {
    String text = ResourceBundle.getBundle(Messages.class.getName()).getString(property);
    return MessageFormat.format(text, args);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\decorator\Messages.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */