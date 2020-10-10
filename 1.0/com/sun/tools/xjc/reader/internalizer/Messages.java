package com.sun.tools.xjc.reader.internalizer;

import java.text.MessageFormat;
import java.util.ResourceBundle;

class Messages
{
  static final String ERR_INCORRECT_SCHEMA_REFERENCE = "Internalizer.IncorrectSchemaReference";
  static final String ERR_XPATH_EVAL = "Internalizer.XPathEvaluationError";
  static final String NO_XPATH_EVAL_TO_NO_TARGET = "Internalizer.XPathEvaluatesToNoTarget";
  static final String NO_XPATH_EVAL_TOO_MANY_TARGETS = "Internalizer.XPathEvaulatesToTooManyTargets";
  static final String NO_XPATH_EVAL_TO_NON_ELEMENT = "Internalizer.XPathEvaluatesToNonElement";
  static final String XPATH_EVAL_TO_NON_SCHEMA_ELEMENT = "Internalizer.XPathEvaluatesToNonSchemaElement";
  static final String CONTEXT_NODE_IS_NOT_ELEMENT = "Internalizer.ContextNodeIsNotElement";
  static final String ERR_INCORRECT_VERSION = "Internalizer.IncorrectVersion";
  static final String ERR_VERSION_NOT_FOUND = "Internalizer.VersionNotPresent";
  static final String TWO_VERSION_ATTRIBUTES = "Internalizer.TwoVersionAttributes";
  static final String ORPHANED_CUSTOMIZATION = "Internalizer.OrphanedCustomization";
  static final String ERR_UNABLE_TO_PARSE = "AbstractReferenceFinderImpl.UnableToParse";
  
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\internalizer\Messages.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */