package com.sun.xml.xsom.impl.util;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class DraconianErrorHandler
  implements ErrorHandler
{
  public void error(SAXParseException e)
    throws SAXException
  {
    throw e;
  }
  
  public void fatalError(SAXParseException e)
    throws SAXException
  {
    throw e;
  }
  
  public void warning(SAXParseException e) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\util\DraconianErrorHandler.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */