package org.fourthline.cling.support.shared.log.impl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.fourthline.cling.support.shared.CenterWindow;
import org.fourthline.cling.support.shared.log.LogView;
import org.fourthline.cling.support.shared.log.LogView.LogCategories;
import org.fourthline.cling.support.shared.log.LogView.Presenter;
import org.seamless.swing.Application;
import org.seamless.swing.logging.LogCategorySelector;
import org.seamless.swing.logging.LogController;
import org.seamless.swing.logging.LogController.Expiration;
import org.seamless.swing.logging.LogMessage;
import org.seamless.swing.logging.LogTableCellRenderer;
import org.seamless.swing.logging.LogTableModel;

@Singleton
public class LogViewImpl
  extends JPanel
  implements LogView
{
  @Inject
  protected LogView.LogCategories logCategories;
  @Inject
  protected Event<CenterWindow> centerWindowEvent;
  protected LogCategorySelector logCategorySelector;
  protected JTable logTable;
  protected LogTableModel logTableModel;
  protected final JToolBar toolBar = new JToolBar();
  protected final JButton configureButton = new JButton("Options...", 
    Application.createImageIcon(LogController.class, "img/configure.png"));
  protected final JButton clearButton = new JButton("Clear Log", 
    Application.createImageIcon(LogController.class, "img/removetext.png"));
  protected final JButton copyButton = new JButton("Copy", 
    Application.createImageIcon(LogController.class, "img/copyclipboard.png"));
  protected final JButton expandButton = new JButton("Expand", 
    Application.createImageIcon(LogController.class, "img/viewtext.png"));
  protected final JButton pauseButton = new JButton("Pause/Continue Log", 
    Application.createImageIcon(LogController.class, "img/pause.png"));
  protected final JLabel pauseLabel = new JLabel(" (Active)");
  protected final JComboBox expirationComboBox = new JComboBox(LogController.Expiration.values());
  protected LogView.Presenter presenter;
  
  @PostConstruct
  public void init()
  {
    setLayout(new BorderLayout());
    
    LogController.Expiration defaultExpiration = getDefaultExpiration();
    
    this.logCategorySelector = new LogCategorySelector(this.logCategories);
    
    this.logTableModel = new LogTableModel(defaultExpiration.getSeconds());
    this.logTable = new JTable(this.logTableModel);
    
    this.logTable.setDefaultRenderer(LogMessage.class, new LogTableCellRenderer()
    {
      protected ImageIcon getWarnErrorIcon()
      {
        return LogViewImpl.this.getWarnErrorIcon();
      }
      
      protected ImageIcon getDebugIcon()
      {
        return LogViewImpl.this.getDebugIcon();
      }
      
      protected ImageIcon getTraceIcon()
      {
        return LogViewImpl.this.getTraceIcon();
      }
      
      protected ImageIcon getInfoIcon()
      {
        return LogViewImpl.this.getInfoIcon();
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
        if (e.getSource() == LogViewImpl.this.logTable.getSelectionModel())
        {
          int[] rows = LogViewImpl.this.logTable.getSelectedRows();
          if ((rows == null) || (rows.length == 0))
          {
            LogViewImpl.this.copyButton.setEnabled(false);
            LogViewImpl.this.expandButton.setEnabled(false);
          }
          else if (rows.length == 1)
          {
            LogViewImpl.this.copyButton.setEnabled(true);
            LogMessage msg = (LogMessage)LogViewImpl.this.logTableModel.getValueAt(rows[0], 0);
            if (msg.getMessage().length() > LogViewImpl.this.getExpandMessageCharacterLimit()) {
              LogViewImpl.this.expandButton.setEnabled(true);
            } else {
              LogViewImpl.this.expandButton.setEnabled(false);
            }
          }
          else
          {
            LogViewImpl.this.copyButton.setEnabled(true);
            LogViewImpl.this.expandButton.setEnabled(false);
          }
        }
      }
    });
    adjustTableUI();
    initializeToolBar(defaultExpiration);
    
    setPreferredSize(new Dimension(250, 100));
    setMinimumSize(new Dimension(250, 50));
    add(new JScrollPane(this.logTable), "Center");
    add(this.toolBar, "South");
  }
  
  public Component asUIComponent()
  {
    return this;
  }
  
  public void setPresenter(LogView.Presenter presenter)
  {
    this.presenter = presenter;
  }
  
  public void pushMessage(LogMessage logMessage)
  {
    this.logTableModel.pushMessage(logMessage);
    if (!this.logTableModel.isPaused()) {
      this.logTable.scrollRectToVisible(this.logTable
        .getCellRect(this.logTableModel.getRowCount() - 1, 0, true));
    }
  }
  
  public void dispose()
  {
    this.logCategorySelector.dispose();
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
    
    this.logTable.getColumnModel().getColumn(2).setMinWidth(110);
    this.logTable.getColumnModel().getColumn(2).setMaxWidth(250);
    
    this.logTable.getColumnModel().getColumn(3).setPreferredWidth(150);
    this.logTable.getColumnModel().getColumn(3).setMaxWidth(400);
    
    this.logTable.getColumnModel().getColumn(4).setPreferredWidth(600);
  }
  
  protected void initializeToolBar(LogController.Expiration expiration)
  {
    this.configureButton.setFocusable(false);
    this.configureButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        LogViewImpl.this.centerWindowEvent.fire(new CenterWindow(LogViewImpl.this.logCategorySelector));
        LogViewImpl.this.logCategorySelector.setVisible(!LogViewImpl.this.logCategorySelector.isVisible());
      }
    });
    this.clearButton.setFocusable(false);
    this.clearButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        LogViewImpl.this.logTableModel.clearMessages();
      }
    });
    this.copyButton.setFocusable(false);
    this.copyButton.setEnabled(false);
    this.copyButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        StringBuilder sb = new StringBuilder();
        List<LogMessage> messages = LogViewImpl.this.getSelectedMessages();
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
        List<LogMessage> messages = LogViewImpl.this.getSelectedMessages();
        if (messages.size() != 1) {
          return;
        }
        LogViewImpl.this.presenter.onExpand((LogMessage)messages.get(0));
      }
    });
    this.pauseButton.setFocusable(false);
    this.pauseButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        LogViewImpl.this.logTableModel.setPaused(!LogViewImpl.this.logTableModel.isPaused());
        if (LogViewImpl.this.logTableModel.isPaused()) {
          LogViewImpl.this.pauseLabel.setText(" (Paused)");
        } else {
          LogViewImpl.this.pauseLabel.setText(" (Active)");
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
        LogViewImpl.this.logTableModel.setMaxAgeSeconds(expiration.getSeconds());
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
  
  protected LogController.Expiration getDefaultExpiration()
  {
    return LogController.Expiration.SIXTY_SECONDS;
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
  
  protected int getExpandMessageCharacterLimit()
  {
    return 100;
  }
  
  protected List<LogMessage> getSelectedMessages()
  {
    List<LogMessage> messages = new ArrayList();
    for (int row : this.logTable.getSelectedRows()) {
      messages.add((LogMessage)this.logTableModel.getValueAt(row, 0));
    }
    return messages;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\shared\log\impl\LogViewImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */