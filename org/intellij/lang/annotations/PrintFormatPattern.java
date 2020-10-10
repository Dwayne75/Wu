package org.intellij.lang.annotations;

class PrintFormatPattern
{
  @Language("RegExp")
  private static final String ARG_INDEX = "(?:\\d+\\$)?";
  @Language("RegExp")
  private static final String FLAGS = "(?:[-#+ 0,(<]*)?";
  @Language("RegExp")
  private static final String WIDTH = "(?:\\d+)?";
  @Language("RegExp")
  private static final String PRECISION = "(?:\\.\\d+)?";
  @Language("RegExp")
  private static final String CONVERSION = "(?:[tT])?(?:[a-zA-Z%])";
  @Language("RegExp")
  private static final String TEXT = "[^%]|%%";
  @Language("RegExp")
  static final String PRINT_FORMAT = "(?:[^%]|%%|(?:%(?:\\d+\\$)?(?:[-#+ 0,(<]*)?(?:\\d+)?(?:\\.\\d+)?(?:[tT])?(?:[a-zA-Z%])))*";
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\intellij\lang\annotations\PrintFormatPattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */