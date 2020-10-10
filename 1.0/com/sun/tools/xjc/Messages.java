package com.sun.tools.xjc;

import java.text.MessageFormat;
import java.util.ResourceBundle;

class Messages
{
  static final String UNKNOWN_LOCATION = "ConsoleErrorReporter.UnknownLocation";
  static final String LINE_X_OF_Y = "ConsoleErrorReporter.LineXOfY";
  static final String UNKNOWN_FILE = "ConsoleErrorReporter.UnknownFile";
  static final String DRIVER_PUBLIC_USAGE = "Driver.Public.Usage";
  static final String DRIVER_PRIVATE_USAGE = "Driver.Private.Usage";
  static final String ADDON_USAGE = "Driver.AddonUsage";
  static final String EXPERIMENTAL_LANGUAGE_WARNING = "Driver.ExperimentalLanguageWarning";
  static final String MISSING_CLASSPATH = "Driver.MissingClassPath";
  static final String MISSING_DIR = "Driver.MissingDir";
  static final String NON_EXISTENT_DIR = "Driver.NonExistentDir";
  static final String MISSING_FILENAME = "Driver.MissingFileName";
  static final String MISSING_PACKAGENAME = "Driver.MissingPackageName";
  static final String MISSING_RUNTIME_PACKAGENAME = "Driver.MissingRuntimePackageName";
  static final String MISSING_MODE_OPERAND = "Driver.MissingModeOperand";
  static final String MISSING_CATALOG = "Driver.MissingCatalog";
  static final String MISSING_COMPATIBILITY_OPERAND = "Driver.MissingCompatibilityOperand";
  static final String MISSING_DOM4J = "Driver.MissingDOM4J";
  static final String MISSING_PROXYHOST = "Driver.MissingProxyHost";
  static final String MISSING_PROXYPORT = "Driver.MissingProxyPort";
  static final String STACK_OVERFLOW = "Driver.StackOverflow";
  static final String UNRECOGNIZED_MODE = "Driver.UnrecognizedMode";
  static final String UNRECOGNIZED_PARAMETER = "Driver.UnrecognizedParameter";
  static final String MISSING_GRAMMAR = "Driver.MissingGrammar";
  static final String PARSING_SCHEMA = "Driver.ParsingSchema";
  static final String PARSE_FAILED = "Driver.ParseFailed";
  static final String COMPILING_SCHEMA = "Driver.CompilingSchema";
  static final String FAILED_TO_GENERATE_CODE = "Driver.FailedToGenerateCode";
  static final String FILE_PROLOG_COMMENT = "Driver.FilePrologComment";
  static final String DATE_FORMAT = "Driver.DateFormat";
  static final String TIME_FORMAT = "Driver.TimeFormat";
  static final String AT = "Driver.At";
  static final String VERSION = "Driver.Version";
  static final String BUILD_ID = "Driver.BuildID";
  static final String ERROR_MSG = "Driver.ErrorMessage";
  static final String WARNING_MSG = "Driver.WarningMessage";
  static final String INFO_MSG = "Driver.InfoMessage";
  static final String ERR_NOT_A_BINDING_FILE = "Driver.NotABindingFile";
  static final String ERR_TOO_MANY_SCHEMA = "GrammarLoader.TooManySchema";
  static final String ERR_INCOMPATIBLE_XERCES = "GrammarLoader.IncompatibleXerces";
  
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\Messages.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */