package com.sun.tools.xjc.reader.xmlschema;

import com.sun.tools.xjc.ErrorReceiver;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

public final class ErrorReporter
{
  private ErrorReceiver errorReceiver;
  private boolean hadError = false;
  
  ErrorReporter(ErrorReceiver handler)
  {
    setErrorHandler(handler);
  }
  
  boolean hadError()
  {
    return this.hadError;
  }
  
  void setErrorHandler(ErrorReceiver h)
  {
    this.errorReceiver = h;
  }
  
  void error(Locator loc, String prop)
  {
    error(loc, prop, new Object[0]);
  }
  
  void error(Locator loc, String prop, Object arg1)
  {
    error(loc, prop, new Object[] { arg1 });
  }
  
  void error(Locator loc, String prop, Object arg1, Object arg2)
  {
    error(loc, prop, new Object[] { arg1, arg2 });
  }
  
  void error(Locator loc, String prop, Object arg1, Object arg2, Object arg3)
  {
    error(loc, prop, new Object[] { arg1, arg2, arg3 });
  }
  
  void error(Locator loc, String prop, Object[] args)
  {
    this.errorReceiver.error(loc, Messages.format(prop, args));
  }
  
  void warning(Locator loc, String prop, Object[] args)
  {
    this.errorReceiver.warning(new SAXParseException(Messages.format(prop, args), loc));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\ErrorReporter.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */