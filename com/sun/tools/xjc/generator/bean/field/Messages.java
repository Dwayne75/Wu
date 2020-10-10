package com.sun.tools.xjc.generator.bean.field;

import java.text.MessageFormat;
import java.util.ResourceBundle;

 enum Messages
{
  DEFAULT_GETTER_JAVADOC,  DEFAULT_SETTER_JAVADOC;
  
  private static final ResourceBundle rb = ResourceBundle.getBundle(Messages.class.getName().substring(0, Messages.class.getName().lastIndexOf('.')) + ".MessageBundle");
  
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\field\Messages.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */