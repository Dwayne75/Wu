package com.sun.tools.xjc.reader.xmlschema;

import java.text.MessageFormat;
import java.util.ResourceBundle;

class Messages
{
  static final String WARN_NO_GLOBAL_ELEMENT = "BGMBuilder.NoGlobalElement";
  static final String ERR_MULTIPLE_SCHEMA_BINDINGS = "BGMBuilder.MultipleSchemaBindings";
  static final String ERR_MULTIPLE_SCHEMA_BINDINGS_LOCATION = "BGMBuilder.MultipleSchemaBindings.Location";
  static final String JAVADOC_HEADING = "ClassSelector.JavadocHeading";
  static final String ERR_RESERVED_CLASS_NAME = "ClassSelector.ReservedClassName";
  static final String ERR_CLASS_NAME_IS_REQUIRED = "ClassSelector.ClassNameIsRequired";
  static final String ERR_INCORRECT_CLASS_NAME = "ClassSelector.IncorrectClassName";
  static final String ERR_INCORRECT_PACKAGE_NAME = "ClassSelector.IncorrectPackageName";
  static final String ERR_CANNOT_BE_TYPE_SAFE_ENUM = "ConversionFinder.CannotBeTypeSafeEnum";
  static final String ERR_CANNOT_BE_TYPE_SAFE_ENUM_LOCATION = "ConversionFinder.CannotBeTypeSafeEnum.Location";
  static final String ERR_NO_ENUM_NAME_AVAILABLE = "ConversionFinder.NoEnumNameAvailable";
  static final String ERR_DATATYPE_ERROR = "DatatypeBuilder.DatatypeError";
  static final String ERR_UNABLE_TO_GENERATE_NAME_FROM_MODELGROUP = "DefaultParticleBinder.UnableToGenerateNameFromModelGroup";
  static final String ERR_INCORRECT_FIXED_VALUE = "FieldBuilder.IncorrectFixedValue";
  static final String ERR_INCORRECT_DEFAULT_VALUE = "FieldBuilder.IncorrectDefaultValue";
  static final String ERR_CONFLICT_BETWEEN_USERTYPE_AND_ACTUALTYPE_ATTUSE = "FieldBuilder.ConflictBetweenUserTypeAndActualType.AttUse";
  static final String ERR_CONFLICT_BETWEEN_USERTYPE_AND_ACTUALTYPE_ATTUSE_SOURCE = "FieldBuilder.ConflictBetweenUserTypeAndActualType.AttUse.Source";
  static final String ERR_UNNESTED_JAVATYPE_CUSTOMIZATION_ON_SIMPLETYPE = "SimpleTypeBuilder.UnnestedJavaTypeCustomization";
  static final String JAVADOC_NIL_PROPERTY = "FieldBuilder.Javadoc.NilProperty";
  static final String JAVADOC_VALUEOBJECT_PROPERTY = "FieldBuilder.Javadoc.ValueObject";
  static final String MSG_COLLISION_INFO = "CollisionInfo.CollisionInfo";
  static final String MSG_UNKNOWN_FILE = "CollisionInfo.UnknownFile";
  static final String MSG_LINE_X_OF_Y = "CollisionInfo.LineXOfY";
  static final String MSG_FALLBACK_JAVADOC = "DefaultParticleBinder.FallbackJavadoc";
  
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\Messages.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */