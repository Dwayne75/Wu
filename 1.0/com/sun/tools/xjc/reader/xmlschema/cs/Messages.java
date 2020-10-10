package com.sun.tools.xjc.reader.xmlschema.cs;

import java.text.MessageFormat;
import java.util.ResourceBundle;

class Messages
{
  static final String ERR_ABSTRACT_COMPLEX_TYPE = "ClassSelector.AbstractComplexType";
  static final String ERR_ABSTRACT_COMPLEX_TYPE_SOURCE = "ClassSelector.AbstractComplexType.SourceLocation";
  static final String JAVADOC_HEADING = "ClassSelector.JavadocHeading";
  static final String JAVADOC_LINE_UNKNOWN = "ClassSelector.JavadocLineUnknown";
  static final String ERR_RESERVED_CLASS_NAME = "ClassSelector.ReservedClassName";
  static final String ERR_CLASS_NAME_IS_REQUIRED = "ClassSelector.ClassNameIsRequired";
  static final String ERR_INCORRECT_CLASS_NAME = "ClassSelector.IncorrectClassName";
  static final String ERR_INCORRECT_PACKAGE_NAME = "ClassSelector.IncorrectPackageName";
  static final String ERR_UNABLE_TO_GENERATE_NAME_FROM_MODELGROUP = "DefaultParticleBinder.UnableToGenerateNameFromModelGroup";
  
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\cs\Messages.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */