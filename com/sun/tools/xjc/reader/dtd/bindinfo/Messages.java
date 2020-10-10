package com.sun.tools.xjc.reader.dtd.bindinfo;

import java.text.MessageFormat;
import java.util.ResourceBundle;

class Messages
{
  static final String ERR_UNDEFINED_FIELD = "BIConstructor.UndefinedField";
  
  static String format(String property, Object... args)
  {
    String text = ResourceBundle.getBundle(Messages.class.getPackage().getName() + ".MessageBundle").getString(property);
    return MessageFormat.format(text, args);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\dtd\bindinfo\Messages.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */