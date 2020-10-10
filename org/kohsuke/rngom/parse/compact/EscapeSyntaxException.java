package org.kohsuke.rngom.parse.compact;

class EscapeSyntaxException
  extends RuntimeException
{
  private final String key;
  private final int lineNumber;
  private final int columnNumber;
  
  EscapeSyntaxException(String key, int lineNumber, int columnNumber)
  {
    this.key = key;
    this.lineNumber = lineNumber;
    this.columnNumber = columnNumber;
  }
  
  String getKey()
  {
    return this.key;
  }
  
  int getLineNumber()
  {
    return this.lineNumber;
  }
  
  int getColumnNumber()
  {
    return this.columnNumber;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\parse\compact\EscapeSyntaxException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */