package com.sun.istack;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

public class SAXParseException2
  extends SAXParseException
{
  public SAXParseException2(String message, Locator locator)
  {
    super(message, locator);
  }
  
  public SAXParseException2(String message, Locator locator, Exception e)
  {
    super(message, locator, e);
  }
  
  public SAXParseException2(String message, String publicId, String systemId, int lineNumber, int columnNumber)
  {
    super(message, publicId, systemId, lineNumber, columnNumber);
  }
  
  public SAXParseException2(String message, String publicId, String systemId, int lineNumber, int columnNumber, Exception e)
  {
    super(message, publicId, systemId, lineNumber, columnNumber, e);
  }
  
  public Throwable getCause()
  {
    return getException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\istack\SAXParseException2.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */