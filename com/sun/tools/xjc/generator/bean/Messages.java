package com.sun.tools.xjc.generator.bean;

import java.text.MessageFormat;
import java.util.ResourceBundle;

 enum Messages
{
  METHOD_COLLISION,  ERR_UNUSABLE_NAME,  ERR_NAME_COLLISION,  ILLEGAL_CONSTRUCTOR_PARAM,  OBJECT_FACTORY_CONFLICT,  OBJECT_FACTORY_CONFLICT_RELATED;
  
  private static final ResourceBundle rb = ResourceBundle.getBundle(Messages.class.getPackage().getName() + ".MessageBundle");
  
  private Messages() {}
  
  public String toString()
  {
    return format(new Object[0]);
  }
  
  public String format(Object... args)
  {
    return MessageFormat.format(rb.getString(name()), args);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\Messages.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */