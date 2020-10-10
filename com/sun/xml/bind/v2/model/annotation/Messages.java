package com.sun.xml.bind.v2.model.annotation;

import java.text.MessageFormat;
import java.util.ResourceBundle;

 enum Messages
{
  DUPLICATE_ANNOTATIONS;
  
  private static final ResourceBundle rb = ResourceBundle.getBundle(Messages.class.getName());
  
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\annotation\Messages.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */