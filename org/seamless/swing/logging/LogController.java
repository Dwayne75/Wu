package org.seamless.swing.logging;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.seamless.swing.AbstractController;
import org.seamless.swing.Application;
import org.seamless.swing.Controller;

public abstract class LogController
  extends AbstractController<JPanel>
{
  private final LogCategorySelector logCategorySelector;
  private final JTable logTable;
  private final LogTableModel logTableModel;
  private final JToolBar toolBar = new JToolBar();
  private final JButton configureButton = createConfigureButton();
  private final JButton clearButton = createClearButton();
  private final JButton copyButton = createCopyButton();
  private final JButton expandButton = createExpandButton();
  private final JButton pauseButton = createPauseButton();
  private final JLabel pauseLabel = new JLabel(" (Active)");
  private final JComboBox expirationComboBox = new JComboBox(Expiration.values());
  
  public static enum Expiration
  {
    TEN_SECONDS(10, "10 Seconds"),  SIXTY_SECONDS(60, "60 Seconds"),  FIVE_MINUTES(300, "5 Minutes"),  NEVER(Integer.MAX_VALUE, "Never");
    
    private int seconds;
    private String label;
    
    private Expiration(int seconds, String label)
    {
      this.seconds = seconds;
      this.label = label;
    }
    
    public int getSeconds()
    {
      return this.seconds;
    }
    
    public String getLabel()
    {
      return this.label;
    }
    
    public String toString()
    {
      return getLabel();
    }
  }
  
  public LogController(Controller parentController, List<LogCategory> logCategories)
  {
    this(parentController, Expiration.SIXTY_SECONDS, logCategories);
  }
  
  public LogController(Controller parentController, Expiration expiration, List<LogCategory> logCategories)
  {
    super(new JPanel(new BorderLayout()), parentController);
    
    this.logCategorySelector = new LogCategorySelector(logCategories);
    
    this.logTableModel = new LogTableModel(expiration.getSeconds());
    this.logTable = new JTable(this.logTableModel);
    
    this.logTable.setDefaultRenderer(LogMessage.class, new LogTableCellRenderer()
    {
      protected ImageIcon getWarnErrorIcon()
      {
        return LogController.this.getWarnErrorIcon();
      }
      
      protected ImageIcon getDebugIcon()
      {
        return LogController.this.getDebugIcon();
      }
      
      protected ImageIcon getTraceIcon()
      {
        return LogController.this.getTraceIcon();
      }
      
      protected ImageIcon getInfoIcon()
      {
        return LogController.this.getInfoIcon();
      }
    });
    this.logTable.setCellSelectionEnabled(false);
    this.logTable.setRowSelectionAllowed(true);
    this.logTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent e)
      {
        if (e.getValueIsAdjusting()) {
          return;
        }
        if (e.getSource() == LogController.this.logTable.getSelectionModel())
        {
          int[] rows = LogController.this.logTable.getSelectedRows();
          if ((rows == null) || (rows.length == 0))
          {
            LogController.this.copyButton.setEnabled(false);
            LogController.this.expandButton.setEnabled(false);
          }
          else if (rows.length == 1)
          {
            LogController.this.copyButton.setEnabled(true);
            LogMessage msg = (LogMessage)LogController.this.logTableModel.getValueAt(rows[0], 0);
            if (msg.getMessage().length() > LogController.this.getExpandMessageCharacterLimit()) {
              LogController.this.expandButton.setEnabled(true);
            } else {
              LogController.this.expandButton.setEnabled(false);
            }
          }
          else
          {
            LogController.this.copyButton.setEnabled(true);
            LogController.this.expandButton.setEnabled(false);
          }
        }
      }
    });
    adjustTableUI();
    initializeToolBar(expiration);
    
    ((JPanel)getView()).setPreferredSize(new Dimension(250, 100));
    ((JPanel)getView()).setMinimumSize(new Dimension(250, 50));
    ((JPanel)getView()).add(new JScrollPane(this.logTable), "Center");
    ((JPanel)getView()).add(this.toolBar, "South");
  }
  
  public void pushMessage(final LogMessage message)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        LogController.this.logTableModel.pushMessage(message);
        if (!LogController.this.logTableModel.isPaused()) {
          LogController.this.logTable.scrollRectToVisible(LogController.this.logTable.getCellRect(LogController.this.logTableModel.getRowCount() - 1, 0, true));
        }
      }
    });
  }
  
  protected void adjustTableUI()
  {
    this.logTable.setFocusable(false);
    this.logTable.setRowHeight(18);
    this.logTable.getTableHeader().setReorderingAllowed(false);
    this.logTable.setBorder(BorderFactory.createEmptyBorder());
    
    this.logTable.getColumnModel().getColumn(0).setMinWidth(30);
    this.logTable.getColumnModel().getColumn(0).setMaxWidth(30);
    this.logTable.getColumnModel().getColumn(0).setResizable(false);
    
    this.logTable.getColumnModel().getColumn(1).setMinWidth(90);
    this.logTable.getColumnModel().getColumn(1).setMaxWidth(90);
    this.logTable.getColumnModel().getColumn(1).setResizable(false);
    
    this.logTable.getColumnModel().getColumn(2).setMinWidth(100);
    this.logTable.getColumnModel().getColumn(2).setMaxWidth(250);
    
    this.logTable.getColumnModel().getColumn(3).setPreferredWidth(150);
    this.logTable.getColumnModel().getColumn(3).setMaxWidth(400);
    
    this.logTable.getColumnModel().getColumn(4).setPreferredWidth(600);
  }
  
  protected void initializeToolBar(Expiration expiration)
  {
    this.configureButton.setFocusable(false);
    this.configureButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        Application.center(LogController.this.logCategorySelector, LogController.this.getParentWindow());
        LogController.this.logCategorySelector.setVisible(!LogController.this.logCategorySelector.isVisible());
      }
    });
    this.clearButton.setFocusable(false);
    this.clearButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        LogController.this.logTableModel.clearMessages();
      }
    });
    this.copyButton.setFocusable(false);
    this.copyButton.setEnabled(false);
    this.copyButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        StringBuilder sb = new StringBuilder();
        List<LogMessage> messages = LogController.this.getSelectedMessages();
        for (LogMessage message : messages) {
          sb.append(message.toString()).append("\n");
        }
        Application.copyToClipboard(sb.toString());
      }
    });
    this.expandButton.setFocusable(false);
    this.expandButton.setEnabled(false);
    this.expandButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        List<LogMessage> messages = LogController.this.getSelectedMessages();
        if (messages.size() != 1) {
          return;
        }
        LogController.this.expand((LogMessage)messages.get(0));
      }
    });
    this.pauseButton.setFocusable(false);
    this.pauseButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        LogController.this.logTableModel.setPaused(!LogController.this.logTableModel.isPaused());
        if (LogController.this.logTableModel.isPaused()) {
          LogController.this.pauseLabel.setText(" (Paused)");
        } else {
          LogController.this.pauseLabel.setText(" (Active)");
        }
      }
    });
    this.expirationComboBox.setSelectedItem(expiration);
    this.expirationComboBox.setMaximumSize(new Dimension(100, 32));
    this.expirationComboBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        JComboBox cb = (JComboBox)e.getSource();
        LogController.Expiration expiration = (LogController.Expiration)cb.getSelectedItem();
        LogController.this.logTableModel.setMaxAgeSeconds(expiration.getSeconds());
      }
    });
    this.toolBar.setFloatable(false);
    this.toolBar.add(this.copyButton);
    this.toolBar.add(this.expandButton);
    this.toolBar.add(Box.createHorizontalGlue());
    this.toolBar.add(this.configureButton);
    this.toolBar.add(this.clearButton);
    this.toolBar.add(this.pauseButton);
    this.toolBar.add(this.pauseLabel);
    this.toolBar.add(Box.createHorizontalGlue());
    this.toolBar.add(new JLabel("Clear after:"));
    this.toolBar.add(this.expirationComboBox);
  }
  
  protected List<LogMessage> getSelectedMessages()
  {
    List<LogMessage> messages = new ArrayList();
    for (int row : this.logTable.getSelectedRows()) {
      messages.add((LogMessage)this.logTableModel.getValueAt(row, 0));
    }
    return messages;
  }
  
  protected int getExpandMessageCharacterLimit()
  {
    return 100;
  }
  
  public LogTableModel getLogTableModel()
  {
    return this.logTableModel;
  }
  
  protected JButton createConfigureButton()
  {
    return new JButton("Options...", Application.createImageIcon(LogController.class, "img/configure.png"));
  }
  
  protected JButton createClearButton()
  {
    return new JButton("Clear Log", Application.createImageIcon(LogController.class, "img/removetext.png"));
  }
  
  protected JButton createCopyButton()
  {
    return new JButton("Copy", Application.createImageIcon(LogController.class, "img/copyclipboard.png"));
  }
  
  protected JButton createExpandButton()
  {
    return new JButton("Expand", Application.createImageIcon(LogController.class, "img/viewtext.png"));
  }
  
  protected JButton createPauseButton()
  {
    return new JButton("Pause/Continue Log", Application.createImageIcon(LogController.class, "img/pause.png"));
  }
  
  protected ImageIcon getWarnErrorIcon()
  {
    return Application.createImageIcon(LogController.class, "img/warn.png");
  }
  
  protected ImageIcon getDebugIcon()
  {
    return Application.createImageIcon(LogController.class, "img/debug.png");
  }
  
  protected ImageIcon getTraceIcon()
  {
    return Application.createImageIcon(LogController.class, "img/trace.png");
  }
  
  protected ImageIcon getInfoIcon()
  {
    return Application.createImageIcon(LogController.class, "img/info.png");
  }
  
  protected abstract void expand(LogMessage paramLogMessage);
  
  protected abstract Frame getParentWindow();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\swing\logging\LogController.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */