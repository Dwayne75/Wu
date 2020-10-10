package com.sun.tools.xjc;

import java.io.OutputStream;
import java.io.PrintStream;
import org.xml.sax.SAXParseException;

public class ConsoleErrorReporter
  extends ErrorReceiver
{
  private PrintStream output;
  private boolean hadError = false;
  
  public ConsoleErrorReporter(PrintStream out)
  {
    this.output = out;
  }
  
  public ConsoleErrorReporter(OutputStream out)
  {
    this(new PrintStream(out));
  }
  
  public ConsoleErrorReporter()
  {
    this(System.out);
  }
  
  public void warning(SAXParseException e)
  {
    print("Driver.WarningMessage", e);
  }
  
  public void error(SAXParseException e)
  {
    this.hadError = true;
    print("Driver.ErrorMessage", e);
  }
  
  public void fatalError(SAXParseException e)
  {
    this.hadError = true;
    print("Driver.ErrorMessage", e);
  }
  
  public void info(SAXParseException e)
  {
    print("Driver.InfoMessage", e);
  }
  
  public boolean hadError()
  {
    return this.hadError;
  }
  
  private void print(String resource, SAXParseException e)
  {
    this.output.println(Messages.format(resource, new Object[] { e.getMessage() }));
    this.output.println(getLocationString(e));
    this.output.println();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\ConsoleErrorReporter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */