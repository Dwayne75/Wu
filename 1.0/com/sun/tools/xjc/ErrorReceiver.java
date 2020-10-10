package com.sun.tools.xjc;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

public abstract class ErrorReceiver
  implements ErrorHandler
{
  public final void error(Locator loc, String msg)
  {
    error(new SAXParseException(msg, loc));
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
      return Messages.format("ConsoleErrorReporter.LineXOfY", line == -1 ? "?" : Integer.toString(line), getShortName(e.getSystemId()));
    }
    return Messages.format("ConsoleErrorReporter.UnknownLocation");
  }
  
  private String getShortName(String url)
  {
    if (url == null) {
      return Messages.format("ConsoleErrorReporter.UnknownFile");
    }
    int idx = url.lastIndexOf('/');
    if (idx != -1) {
      return url.substring(idx + 1);
    }
    idx = url.lastIndexOf('\\');
    if (idx != -1) {
      return url.substring(idx + 1);
    }
    return url;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\ErrorReceiver.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */