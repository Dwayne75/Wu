package com.sun.tools.xjc.api;

import org.xml.sax.SAXParseException;

public abstract interface ErrorListener
  extends com.sun.xml.bind.api.ErrorListener
{
  public abstract void error(SAXParseException paramSAXParseException);
  
  public abstract void fatalError(SAXParseException paramSAXParseException);
  
  public abstract void warning(SAXParseException paramSAXParseException);
  
  public abstract void info(SAXParseException paramSAXParseException);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\ErrorListener.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */