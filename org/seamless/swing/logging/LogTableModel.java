package org.seamless.swing.logging;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class LogTableModel
  extends AbstractTableModel
{
  protected int maxAgeSeconds;
  protected boolean paused = false;
  
  public LogTableModel(int maxAgeSeconds)
  {
    this.maxAgeSeconds = maxAgeSeconds;
  }
  
  public int getMaxAgeSeconds()
  {
    return this.maxAgeSeconds;
  }
  
  public void setMaxAgeSeconds(int maxAgeSeconds)
  {
    this.maxAgeSeconds = maxAgeSeconds;
  }
  
  public boolean isPaused()
  {
    return this.paused;
  }
  
  public void setPaused(boolean paused)
  {
    this.paused = paused;
  }
  
  protected List<LogMessage> messages = new ArrayList();
  
  public synchronized void pushMessage(LogMessage message)
  {
    if (this.paused) {
      return;
    }
    if (this.maxAgeSeconds != Integer.MAX_VALUE)
    {
      Iterator<LogMessage> it = this.messages.iterator();
      long currentTime = new Date().getTime();
      while (it.hasNext())
      {
        LogMessage logMessage = (LogMessage)it.next();
        long delta = this.maxAgeSeconds * 1000;
        if (logMessage.getCreatedOn().longValue() + delta < currentTime) {
          it.remove();
        }
      }
    }
    this.messages.add(message);
    fireTableDataChanged();
  }
  
  public Object getValueAt(int row, int column)
  {
    return this.messages.get(row);
  }
  
  public void clearMessages()
  {
    this.messages.clear();
    fireTableDataChanged();
  }
  
  public int getRowCount()
  {
    return this.messages.size();
  }
  
  public int getColumnCount()
  {
    return 5;
  }
  
  public Class<?> getColumnClass(int i)
  {
    return LogMessage.class;
  }
  
  public String getColumnName(int column)
  {
    switch (column)
    {
    case 0: 
      return "";
    case 1: 
      return "Time";
    case 2: 
      return "Thread";
    case 3: 
      return "Source";
    }
    return "Message";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\swing\logging\LogTableModel.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */