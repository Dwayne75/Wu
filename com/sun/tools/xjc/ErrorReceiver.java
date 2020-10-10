package com.sun.tools.xjc;

import com.sun.istack.SAXParseException2;
import com.sun.tools.xjc.api.ErrorListener;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

public abstract class ErrorReceiver
  implements ErrorHandler, ErrorListener
{
  public final void error(Locator loc, String msg)
  {
    error(new SAXParseException2(msg, loc));
  }
  
  public final void error(Locator loc, String msg, Exception e)
  {
    error(new SAXParseException2(msg, loc, e));
  }
  
  public final void error(String msg, Exception e)
  {
    error(new SAXParseException2(msg, null, e));
  }
  
  public void error(Exception e)
  {
    error(e.getMessage(), e);
  }
  
  public final void warning(Locator loc, String msg)
  {
    warning(new SAXParseException(msg, loc));
  }
  
  public abstract void error(SAXParseException paramSAXParseException)
    throws AbortException;
  
  public abstract void fatalError(SAXParseException paramSAXParseException)
    throws AbortException;
  
  public abstract void warning(SAXParseException paramSAXParseException)
    throws AbortException;
  
  public void pollAbort()
    throws AbortException
  {}
  
  public abstract void info(SAXParseException paramSAXParseException);
  
  public final void debug(String msg)
  {
    info(new SAXParseException(msg, null));
  }
  
  protected final String getLocationString(SAXParseException e)
  {
    if ((e.getLineNumber() != -1) || (e.getSystemId() != null))
    {
      int line = e.getLineNumber();
      return Messages.format("ConsoleErrorReporter.LineXOfY", new Object[] { line == -1 ? "?" : Integer.toString(line), getShortName(e.getSystemId()) });
    }
    return Messages.format("ConsoleErrorReporter.UnknownLocation", new Object[0]);
  }
  
  private String getShortName(String url)
  {
    if (url == null) {
      return Messages.format("ConsoleErrorReporter.UnknownFile", new Object[0]);
    }
    return url;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\ErrorReceiver.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */