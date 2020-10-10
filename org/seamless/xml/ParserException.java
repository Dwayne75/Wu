package org.seamless.xml;

import org.xml.sax.SAXParseException;

public class ParserException
  extends Exception
{
  public ParserException() {}
  
  public ParserException(String s)
  {
    super(s);
  }
  
  public ParserException(String s, Throwable throwable)
  {
    super(s, throwable);
  }
  
  public ParserException(Throwable throwable)
  {
    super(throwable);
  }
  
  public ParserException(SAXParseException ex)
  {
    super("(Line/Column: " + ex.getLineNumber() + ":" + ex.getColumnNumber() + ") " + ex.getMessage());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\xml\ParserException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */