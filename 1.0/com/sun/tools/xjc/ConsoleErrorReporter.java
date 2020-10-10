package com.sun.tools.xjc;

import java.io.OutputStream;
import java.io.PrintStream;
import org.xml.sax.SAXParseException;

public class ConsoleErrorReporter
  extends ErrorReceiver
{
  private PrintStream output;
  private boolean supressWarnings;
  private boolean supressInfo;
  
  public ConsoleErrorReporter(PrintStream out, boolean supressInfo, boolean supressWarnings)
  {
    this.output = out;
    this.supressInfo = supressInfo;
    this.supressWarnings = supressWarnings;
  }
  
  public ConsoleErrorReporter(OutputStream out, boolean supressInfo, boolean supressWarnings)
  {
    this(new PrintStream(out), supressInfo, supressWarnings);
  }
  
  public ConsoleErrorReporter()
  {
    this(System.out, true, false);
  }
  
  public void supressInfoOutput()
  {
    this.supressInfo = true;
  }
  
  public void supressWarnings()
  {
    this.supressWarnings = true;
  }
  
  public void warning(SAXParseException e)
  {
    if (this.supressWarnings) {
      return;
    }
    print("Driver.WarningMessage", e);
  }
  
  public void error(SAXParseException e)
  {
    print("Driver.ErrorMessage", e);
  }
  
  public void fatalError(SAXParseException e)
  {
    print("Driver.ErrorMessage", e);
  }
  
  public void info(SAXParseException e)
  {
    if (this.supressInfo) {
      return;
    }
    print("Driver.InfoMessage", e);
  }
  
  private void print(String resource, SAXParseException e)
  {
    this.output.println(Messages.format(resource, e.getMessage()));
    this.output.println(getLocationString(e));
    this.output.println();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\ConsoleErrorReporter.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */