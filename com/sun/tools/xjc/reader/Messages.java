package com.sun.tools.xjc.reader;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public enum Messages
{
  DUPLICATE_PROPERTY,  DUPLICATE_ELEMENT,  ERR_UNDECLARED_PREFIX,  ERR_UNEXPECTED_EXTENSION_BINDING_PREFIXES,  ERR_UNSUPPORTED_EXTENSION,  ERR_SUPPORTED_EXTENSION_IGNORED,  ERR_RELEVANT_LOCATION,  ERR_CLASS_NOT_FOUND,  PROPERTY_CLASS_IS_RESERVED,  ERR_VENDOR_EXTENSION_DISALLOWED_IN_STRICT_MODE,  ERR_ILLEGAL_CUSTOMIZATION_TAGNAME,  ERR_PLUGIN_NOT_ENABLED;
  
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\Messages.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */