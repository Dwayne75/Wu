package com.sun.tools.xjc.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;

class Messages
{
  static final String ERR_CLASSNAME_COLLISION = "CodeModelClassFactory.ClassNameCollision";
  static final String ERR_CLASSNAME_COLLISION_SOURCE = "CodeModelClassFactory.ClassNameCollision.Source";
  static final String ERR_INVALID_CLASSNAME = "ERR_INVALID_CLASSNAME";
  static final String ERR_CASE_SENSITIVITY_COLLISION = "CodeModelClassFactory.CaseSensitivityCollision";
  static final String ERR_CHAMELEON_SCHEMA_GONE_WILD = "ERR_CHAMELEON_SCHEMA_GONE_WILD";
  
  static String format(String property, Object... args)
  {
    String text = ResourceBundle.getBundle(Messages.class.getPackage().getName() + ".MessageBundle").getString(property);
    return MessageFormat.format(text, args);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\util\Messages.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */