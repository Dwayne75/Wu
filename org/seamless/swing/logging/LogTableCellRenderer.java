package org.seamless.swing.logging;

import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public abstract class LogTableCellRenderer
  extends DefaultTableCellRenderer
{
  protected SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SSS");
  
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
  {
    LogMessage message = (LogMessage)value;
    switch (column)
    {
    case 0: 
      if ((message.getLevel().equals(Level.SEVERE)) || (message.getLevel().equals(Level.WARNING))) {
        return new JLabel(getWarnErrorIcon());
      }
      if (message.getLevel().equals(Level.FINE)) {
        return new JLabel(getDebugIcon());
      }
      if ((message.getLevel().equals(Level.FINER)) || (message.getLevel().equals(Level.FINEST))) {
        return new JLabel(getTraceIcon());
      }
      return new JLabel(getInfoIcon());
    case 1: 
      Date date = new Date(message.getCreatedOn().longValue());
      return super.getTableCellRendererComponent(table, this.dateFormat.format(date), isSelected, hasFocus, row, column);
    case 2: 
      return super.getTableCellRendererComponent(table, message.getThread(), isSelected, hasFocus, row, column);
    case 3: 
      return super.getTableCellRendererComponent(table, message.getSource(), isSelected, hasFocus, row, column);
    }
    return super.getTableCellRendererComponent(table, message.getMessage().replaceAll("\n", "<NL>").replaceAll("\r", "<CR>"), isSelected, hasFocus, row, column);
  }
  
  protected abstract ImageIcon getWarnErrorIcon();
  
  protected abstract ImageIcon getInfoIcon();
  
  protected abstract ImageIcon getDebugIcon();
  
  protected abstract ImageIcon getTraceIcon();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\swing\logging\LogTableCellRenderer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */