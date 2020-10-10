package com.sun.tools.xjc.reader.xmlschema.parser;

import java.text.MessageFormat;
import java.util.ResourceBundle;

class Messages
{
  static final String ERR_UNACKNOWLEDGED_CUSTOMIZATION = "CustomizationContextChecker.UnacknolwedgedCustomization";
  static final String WARN_INCORRECT_URI = "IncorrectNamespaceURIChecker.WarnIncorrectURI";
  static final String STRICT_MODE_PREFIX = "ProhibitedFeaturesFilter.StrictModePrefix";
  static final String ERROR_PREFIX = "ProhibitedFeaturesFilter.ErrorPrefix";
  static final String WARNING_PREFIX = "ProhibitedFeaturesFilter.WarningPrefix";
  static final String UNSUPPORTED_PREFIX = "ProhibitedFeaturesFilter.UnsupportedPrefix";
  static final String PROCESSCONTENTS_ATTR_OF_ANY = "ProhibitedFeaturesFilter.ProcessContentsAttrOfAny";
  static final String ANY_ATTR = "ProhibitedFeaturesFilter.AnyAttr";
  static final String ANY_ATTR_WARNING = "ProhibitedFeaturesFilter.AnyAttrWarning";
  static final String BLOCK_ATTR_OF_COMPLEXTYPE = "ProhibitedFeaturesFilter.BlockAttrOfComplexType";
  static final String ABSTRACT_ATTR_OR_ELEMENT = "ProhibitedFeaturesFilter.AbstractAttrOfElement";
  static final String SUBSTITUTIONGROUP_ATTR_OF_ELEMENT = "ProhibitedFeaturesFilter.SubstitutionGroupAttrOfElement";
  static final String BLOCK_ATTR_OF_ELEMENT = "ProhibitedFeaturesFilter.BlockAttrOfElement";
  static final String KEY = "ProhibitedFeaturesFilter.Key";
  static final String KEY_WARNING = "ProhibitedFeaturesFilter.KeyWarning";
  static final String KEYREF = "ProhibitedFeaturesFilter.Keyref";
  static final String KEYREF_WARNING = "ProhibitedFeaturesFilter.KeyrefWarning";
  static final String NOTATION = "ProhibitedFeaturesFilter.Notation";
  static final String NOTATION_WARNING = "ProhibitedFeaturesFilter.NotationWarning";
  static final String UNIQUE = "ProhibitedFeaturesFilter.Unique";
  static final String UNIQUE_WARNING = "ProhibitedFeaturesFilter.UniqueWarning";
  static final String FINAL_ATTR_OF_ELEMENT = "ProhibitedFeaturesFilter.FinalAttrOfElement";
  static final String FINAL_ATTR_OF_COMPLEXTYPE = "ProhibitedFeaturesFilter.FinalAttrOfComplexType";
  static final String BLOCKDEFAULT_ATTR_OF_SCHEMA = "ProhibitedFeaturesFilter.BlockDefaultAttrOfSchema";
  static final String FINALDEFAULT_ATTR_OF_SCHEMA = "ProhibitedFeaturesFilter.FinalDefaultAttrOfSchema";
  static final String EXTENSIONBINDINGPREFIXES_OF_SCHEMA = "ProhibitedFeaturesFilter.ExtensionBindingPrefixesOfSchema";
  static final String REDEFINE = "ProhibitedFeaturesFilter.Redefine";
  static final String ILLEGAL_BOOLEAN_VALUE = "ProhibitedFeaturesFilter.IllegalBooleanValue";
  static final String XERCES_TOO_OLD = "SchemaConstraintChecker.XercesTooOld";
  static final String UNABLE_TO_CHECK_XERCES_VERSION = "SchemaConstraintChecker.UnableToCheckXercesVersion";
  
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\parser\Messages.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */