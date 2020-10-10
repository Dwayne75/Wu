package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import java.text.MessageFormat;
import java.util.ResourceBundle;

 enum Messages
{
  ERR_CANNOT_BE_BOUND_TO_SIMPLETYPE,  ERR_UNDEFINED_SIMPLE_TYPE,  ERR_ILLEGAL_FIXEDATTR;
  
  private Messages() {}
  
  String format(Object... args)
  {
    String text = ResourceBundle.getBundle(Messages.class.getPackage().getName() + ".MessageBundle").getString(name());
    return MessageFormat.format(text, args);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\bindinfo\Messages.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */