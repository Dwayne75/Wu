package com.sun.tools.jxc;

import java.text.MessageFormat;
import java.util.ResourceBundle;

 enum Messages
{
  UNEXPECTED_NGCC_TOKEN,  BASEDIR_DOESNT_EXIST,  USAGE,  VERSION;
  
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\jxc\Messages.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */