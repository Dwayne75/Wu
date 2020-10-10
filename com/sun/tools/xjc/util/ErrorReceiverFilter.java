package com.sun.tools.xjc.util;

import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.api.ErrorListener;
import org.xml.sax.SAXParseException;

public class ErrorReceiverFilter
  extends ErrorReceiver
{
  private ErrorListener core;
  
  public ErrorReceiverFilter() {}
  
  public ErrorReceiverFilter(ErrorListener h)
  {
    setErrorReceiver(h);
  }
  
  public void setErrorReceiver(ErrorListener handler)
  {
    this.core = handler;
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\util\ErrorReceiverFilter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */