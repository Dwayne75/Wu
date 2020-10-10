package com.wurmonline.shared.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.AccessController;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import sun.security.action.GetPropertyAction;

public class SimpleLogFormatter
  extends Formatter
{
  Date dat;
  private static final String FORMAT = "{0,date} {0,time}";
  private MessageFormat formatter;
  private Object[] args;
  
  public SimpleLogFormatter()
  {
    this.dat = new Date();
    
    this.args = new Object[1];
  }
  
  private final String lineSeparator = (String)AccessController.doPrivileged(new GetPropertyAction("line.separator"));
  
  public synchronized String format(LogRecord record)
  {
    StringBuilder sb = new StringBuilder();
    
    this.dat.setTime(record.getMillis());
    this.args[0] = this.dat;
    StringBuffer text = new StringBuffer();
    if (this.formatter == null) {
      this.formatter = new MessageFormat("{0,date} {0,time}");
    }
    this.formatter.format(this.args, text, null);
    sb.append(text);
    sb.append('.');
    sb.append(record.getMillis() % 1000L);
    sb.append(' ');
    sb.append(record.getThreadID());
    sb.append(' ');
    if (record.getSourceClassName() != null) {
      sb.append(record.getSourceClassName());
    } else {
      sb.append(record.getLoggerName());
    }
    if (record.getSourceMethodName() != null)
    {
      sb.append(' ');
      sb.append(record.getSourceMethodName());
    }
    sb.append(' ');
    
    String message = formatMessage(record);
    sb.append(record.getLevel().getLocalizedName());
    sb.append(": ");
    sb.append(message);
    sb.append(this.lineSeparator);
    if (record.getThrown() != null) {
      try
      {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        record.getThrown().printStackTrace(pw);
        pw.close();
        sb.append(sw.toString());
      }
      catch (Exception localException) {}
    }
    return sb.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\shared\util\SimpleLogFormatter.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */