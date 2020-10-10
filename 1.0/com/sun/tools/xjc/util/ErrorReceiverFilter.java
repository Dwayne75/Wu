package com.sun.tools.xjc.util;

import com.sun.tools.xjc.ErrorReceiver;
import org.xml.sax.SAXParseException;

public class ErrorReceiverFilter
  extends ErrorReceiver
{
  private ErrorReceiver core;
  
  public ErrorReceiverFilter() {}
  
  public ErrorReceiverFilter(ErrorReceiver h)
  {
    setErrorReceiver(h);
  }
  
  public void setErrorReceiver(ErrorReceiver handler)
  {
    this.core = handler;
  }
  
  public ErrorReceiver getErrorReceiver()
  {
    return this.core;
  }
  
  private boolean hadError = false;
  
  public final boolean hadError()
  {
    return this.hadError;
  }
  
  public void info(SAXParseException exception)
  {
    if (this.core != null) {
      this.core.info(exception);
    }
  }
  
  public void warning(SAXParseException exception)
  {
    if (this.core != null) {
      this.core.warning(exception);
    }
  }
  
  public void error(SAXParseException exception)
  {
    this.hadError = true;
    if (this.core != null) {
      this.core.error(exception);
    }
  }
  
  public void fatalError(SAXParseException exception)
  {
    this.hadError = true;
    if (this.core != null) {
      this.core.fatalError(exception);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\util\ErrorReceiverFilter.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */