package com.sun.xml.bind.api;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public abstract interface ErrorListener
  extends ErrorHandler
{
  public abstract void error(SAXParseException paramSAXParseException);
  
  public abstract void fatalError(SAXParseException paramSAXParseException);
  
  public abstract void warning(SAXParseException paramSAXParseException);
  
  public abstract void info(SAXParseException paramSAXParseException);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\api\ErrorListener.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */