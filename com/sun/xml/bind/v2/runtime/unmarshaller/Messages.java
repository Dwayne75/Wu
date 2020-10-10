package com.sun.xml.bind.v2.runtime.unmarshaller;

import java.text.MessageFormat;
import java.util.ResourceBundle;

 enum Messages
{
  UNRESOLVED_IDREF,  UNEXPECTED_ELEMENT,  UNEXPECTED_TEXT,  NOT_A_QNAME,  UNRECOGNIZED_TYPE_NAME,  UNRECOGNIZED_TYPE_NAME_MAYBE,  UNABLE_TO_CREATE_MAP,  UNINTERNED_STRINGS;
  
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\Messages.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */