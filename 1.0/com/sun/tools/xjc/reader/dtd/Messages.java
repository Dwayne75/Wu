package com.sun.tools.xjc.reader.dtd;

import java.text.MessageFormat;
import java.util.ResourceBundle;

class Messages
{
  public static final String ERR_NO_ROOT_ELEMENT = "TDTDReader.NoRootElement";
  public static final String ERR_UNDEFINED_ELEMENT_IN_BINDINFO = "TDTDReader.UndefinedElementInBindInfo";
  public static final String ERR_CONVERSION_FOR_NON_VALUE_ELEMENT = "TDTDReader.ConversionForNonValueElement";
  public static final String ERR_CONTENT_PROPERTY_PARTICLE_MISMATCH = "TDTDReader.ContentProperty.ParticleMismatch";
  public static final String ERR_CONTENT_PROPERTY_DECLARATION_TOO_SHORT = "TDTDReader.ContentProperty.DeclarationTooShort";
  public static final String ERR_BINDINFO_NON_EXISTENT_ELEMENT_DECLARATION = "TDTDReader.BindInfo.NonExistentElementDeclaration";
  public static final String ERR_BINDINFO_NON_EXISTENT_INTERFACE_MEMBER = "TDTDReader.BindInfo.NonExistentInterfaceMember";
  
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\dtd\Messages.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */