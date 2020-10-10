package com.sun.tools.jxc.apt;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Messager;
import com.sun.tools.xjc.ErrorReceiver;
import org.xml.sax.SAXParseException;

final class ErrorReceiverImpl
  extends ErrorReceiver
{
  private final Messager messager;
  private final boolean debug;
  
  public ErrorReceiverImpl(Messager messager, boolean debug)
  {
    this.messager = messager;
    this.debug = debug;
  }
  
  public ErrorReceiverImpl(Messager messager)
  {
    this(messager, false);
  }
  
  public ErrorReceiverImpl(AnnotationProcessorEnvironment env)
  {
    this(env.getMessager());
  }
  
  public void error(SAXParseException exception)
  {
    this.messager.printError(exception.getMessage());
    this.messager.printError(getLocation(exception));
    printDetail(exception);
  }
  
  public void fatalError(SAXParseException exception)
  {
    this.messager.printError(exception.getMessage());
    this.messager.printError(getLocation(exception));
    printDetail(exception);
  }
  
  public void warning(SAXParseException exception)
  {
    this.messager.printWarning(exception.getMessage());
    this.messager.printWarning(getLocation(exception));
    printDetail(exception);
  }
  
  public void info(SAXParseException exception)
  {
    printDetail(exception);
  }
  
  private String getLocation(SAXParseException e)
  {
    return "";
  }
  
  private void printDetail(SAXParseException e)
  {
    if (this.debug) {
      e.printStackTrace(System.out);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\jxc\apt\ErrorReceiverImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */