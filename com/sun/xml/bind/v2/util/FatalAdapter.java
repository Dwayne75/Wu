package com.sun.xml.bind.v2.util;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class FatalAdapter
  implements ErrorHandler
{
  private final ErrorHandler core;
  
  public FatalAdapter(ErrorHandler handler)
  {
    this.core = handler;
  }
  
  public void warning(SAXParseException exception)
    throws SAXException
  {
    this.core.warning(exception);
  }
  
  public void error(SAXParseException exception)
    throws SAXException
  {
    this.core.fatalError(exception);
  }
  
  public void fatalError(SAXParseException exception)
    throws SAXException
  {
    this.core.fatalError(exception);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\util\FatalAdapter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */