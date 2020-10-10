package com.sun.javaws.ui;

import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.si.DeploySIListener;
import com.sun.deploy.si.SingleInstanceImpl;
import com.sun.deploy.si.SingleInstanceManager;
import com.sun.deploy.util.AboutDialog;
import com.sun.deploy.util.DeployUIManager;
import com.sun.deploy.util.DialogFactory;
import com.sun.deploy.util.Trace;
import com.sun.javaws.BrowserSupport;
import com.sun.javaws.Globals;
import com.sun.javaws.LocalApplicationProperties;
import com.sun.javaws.LocalInstallHandler;
import com.sun.javaws.Main;
import com.sun.javaws.SplashScreen;
import com.sun.javaws.cache.Cache;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.util.JavawsConsoleController;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class CacheViewer
  extends JFrame
  implements ActionListener, ChangeListener, ListSelectionListener, DeploySIListener
{
  private final JButton _removeBtn;
  private final JButton _launchOnlineBtn;
  private final JButton _launchOfflineBtn;
  private final JTabbedPane _tabbedPane;
  private final CacheTable _userTable;
  private final CacheTable _sysTable;
  private final JScrollPane _userTab;
  private final JScrollPane _systemTab;
  private static final String BOUNDS_PROPERTY_KEY = "deployment.javaws.viewer.bounds";
  private JMenuItem _launchOnlineMI;
  private JMenuItem _launchOfflineMI;
  private JMenuItem _removeMI;
  private JMenuItem _showMI;
  private JMenuItem _installMI;
  private JMenuItem _browseMI;
  private JMenu _fileMenu;
  private JMenu _editMenu;
  private JMenu _appMenu;
  private JMenu _viewMenu;
  private JMenu _helpMenu;
  private TitledBorder _titledBorder;
  public static final int STATUS_OK = 0;
  public static final int STATUS_REMOVING = 1;
  public static final int STATUS_LAUNCHING = 2;
  public static final int STATUS_BROWSING = 3;
  public static final int STATUS_SORTING = 4;
  public static final int STATUS_SEARCHING = 5;
  public static final int STATUS_INSTALLING = 6;
  private static int _status = 0;
  private static JLabel _statusLabel;
  private static final JLabel _totalSize = new JLabel();
  private static final LocalInstallHandler _lih = LocalInstallHandler.getInstance();
  private static final boolean _isLocalInstallSupported = _lih.isLocalInstallSupported();
  private static long t0;
  private static long t1;
  private static long t2;
  private static long t3;
  private static long t4;
  private SingleInstanceImpl _sil;
  private static final String JAVAWS_CV_ID = "JNLP Cache Viewer" + Config.getInstance().getSessionSpecificString();
  private static final int SLEEP_DELAY = 2000;
  
  public CacheViewer()
  {
    this._sil = new SingleInstanceImpl();
    this._sil.addSingleInstanceListener(this, JAVAWS_CV_ID);
    
    this._removeBtn = makeButton("jnlp.viewer.remove.btn");
    this._launchOnlineBtn = makeButton("jnlp.viewer.launch.online.btn");
    this._launchOfflineBtn = makeButton("jnlp.viewer.launch.offline.btn");
    
    _statusLabel = new JLabel(" ");
    
    this._tabbedPane = new JTabbedPane();
    this._userTable = new CacheTable(this, false);
    this._sysTable = new CacheTable(this, true);
    this._userTab = new JScrollPane(this._userTable);
    this._systemTab = new JScrollPane(this._sysTable);
    
    initComponents();
  }
  
  private void initComponents()
  {
    setTitle(ResourceManager.getMessage("jnlp.viewer.title"));
    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent paramAnonymousWindowEvent)
      {
        CacheViewer.this.exitViewer();
      }
    });
    JPanel localJPanel1 = new JPanel();
    localJPanel1.setLayout(new BorderLayout());
    this._titledBorder = new TitledBorder(ResourceManager.getMessage("jnlp.viewer.all"));
    
    Border localBorder = BorderFactory.createEmptyBorder(4, 4, 4, 4);
    CompoundBorder localCompoundBorder = BorderFactory.createCompoundBorder(localBorder, this._titledBorder);
    
    localJPanel1.setBorder(BorderFactory.createCompoundBorder(localCompoundBorder, localBorder));
    if (Globals.isSystemCache())
    {
      this._tabbedPane.addTab(ResourceManager.getMessage("cert.dialog.system.level"), this._userTab);
    }
    else
    {
      this._tabbedPane.addTab(ResourceManager.getMessage("cert.dialog.user.level"), this._userTab);
      
      this._tabbedPane.addTab(ResourceManager.getMessage("cert.dialog.system.level"), this._systemTab);
    }
    this._tabbedPane.setSelectedIndex(0);
    this._tabbedPane.addChangeListener(this);
    localJPanel1.add(this._tabbedPane, "Center");
    
    Box localBox = Box.createHorizontalBox();
    localBox.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
    
    localBox.add(this._removeBtn);
    localBox.add(Box.createHorizontalGlue());
    localBox.add(this._launchOnlineBtn);
    localBox.add(Box.createHorizontalStrut(5));
    localBox.add(this._launchOfflineBtn);
    
    localJPanel1.add(localBox, "South");
    
    JPanel localJPanel2 = new JPanel(new BorderLayout());
    _totalSize.setText(getAppMessage("jnlp.viewer.totalSize", ""));
    _totalSize.setHorizontalAlignment(0);
    _totalSize.setFont(ResourceManager.getUIFont());
    
    JPanel localJPanel3 = new JPanel(new BorderLayout());
    _statusLabel = new JLabel(" ");
    _statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
    _statusLabel.setFont(ResourceManager.getUIFont());
    
    localJPanel3.add(_statusLabel, "West");
    localJPanel3.add(_totalSize, "Center");
    localJPanel3.setBorder(BorderFactory.createEtchedBorder(1));
    
    localJPanel2.add(localJPanel3, "South");
    localJPanel2.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
    
    getContentPane().add(Box.createVerticalStrut(8), "North");
    getContentPane().add(localJPanel1, "Center");
    getContentPane().add(localJPanel2, "South");
    
    createMenuBar();
    pack();
    
    this._userTable.getSelectionModel().addListSelectionListener(this);
    this._sysTable.getSelectionModel().addListSelectionListener(this);
  }
  
  private void createMenuBar()
  {
    this._fileMenu = new JMenu(ResourceManager.getMessage("jnlp.viewer.file.menu"));
    
    this._fileMenu.setMnemonic(ResourceManager.getVKCode("jnlp.viewer.file.menu.mnemonic"));
    
    JMenuItem localJMenuItem = this._fileMenu.add(ResourceManager.getMessage("jnlp.viewer.exit.mi"));
    
    localJMenuItem.setMnemonic(ResourceManager.getVKCode("jnlp.viewer.exit.mi.mnemonic"));
    
    localJMenuItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramAnonymousActionEvent)
      {
        CacheViewer.this.exitViewer();
      }
    });
    this._editMenu = new JMenu(ResourceManager.getMessage("jnlp.viewer.edit.menu"));
    
    this._editMenu.setMnemonic(ResourceManager.getVKCode("jnlp.viewer.edit.menu.mnemonic"));
    
    localJMenuItem = this._editMenu.add(ResourceManager.getMessage("jnlp.viewer.reinstall.mi"));
    
    localJMenuItem.setMnemonic(ResourceManager.getVKCode("jnlp.viewer.reinstall.mi.mnemonic"));
    
    localJMenuItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramAnonymousActionEvent)
      {
        CacheViewer.this.showReInstallDialog();
      }
    });
    this._editMenu.addSeparator();
    
    localJMenuItem = this._editMenu.add(ResourceManager.getMessage("jnlp.viewer.preferences.mi"));
    
    localJMenuItem.setMnemonic(ResourceManager.getVKCode("jnlp.viewer.preferences.mi.mnemonic"));
    
    localJMenuItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramAnonymousActionEvent)
      {
        Main.launchJavaControlPanel("general");
      }
    });
    this._appMenu = new JMenu(ResourceManager.getMessage("jnlp.viewer.app.menu"));
    
    this._appMenu.setMnemonic(ResourceManager.getVKCode("jnlp.viewer.app.menu.mnemonic"));
    
    this._appMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramAnonymousActionEvent)
      {
        CacheViewer.this.refresh();
      }
    });
    this._launchOfflineMI = this._appMenu.add("");
    this._launchOfflineMI.setMnemonic(ResourceManager.getVKCode("jnlp.viewer.launch.offline.mi.mnemonic"));
    
    this._launchOnlineMI = this._appMenu.add("");
    this._launchOnlineMI.setMnemonic(ResourceManager.getVKCode("jnlp.viewer.launch.online.mi.mnemonic"));
    
    this._appMenu.addSeparator();
    
    LocalInstallHandler localLocalInstallHandler = LocalInstallHandler.getInstance();
    if (_isLocalInstallSupported)
    {
      this._installMI = this._appMenu.add("");
      this._installMI.setMnemonic(ResourceManager.getVKCode("jnlp.viewer.install.mi.mnemonic"));
      
      this._installMI.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent paramAnonymousActionEvent)
        {
          CacheViewer.this.integrateApplication();
        }
      });
    }
    this._showMI = this._appMenu.add("");
    this._showMI.setMnemonic(ResourceManager.getVKCode("jnlp.viewer.show.mi.mnemonic"));
    
    this._browseMI = this._appMenu.add("");
    this._browseMI.setMnemonic(ResourceManager.getVKCode("jnlp.viewer.browse.mi.mnemonic"));
    
    this._appMenu.addSeparator();
    
    this._removeMI = this._appMenu.add("");
    this._removeMI.setMnemonic(ResourceManager.getVKCode("jnlp.viewer.remove.mi.mnemonic"));
    
    this._launchOfflineMI.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramAnonymousActionEvent)
      {
        CacheViewer.this.launchApplication(false);
      }
    });
    this._launchOnlineMI.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramAnonymousActionEvent)
      {
        CacheViewer.this.launchApplication(true);
      }
    });
    this._showMI.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramAnonymousActionEvent)
      {
        CacheViewer.this.showApplication();
      }
    });
    this._browseMI.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramAnonymousActionEvent)
      {
        CacheViewer.this.browseApplication();
      }
    });
    this._removeMI.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramAnonymousActionEvent)
      {
        CacheViewer.this.removeApplications();
      }
    });
    this._viewMenu = new JMenu(ResourceManager.getMessage("jnlp.viewer.view.menu"));
    
    this._viewMenu.setMnemonic(ResourceManager.getVKCode("jnlp.viewer.view.menu.mnemonic"));
    for (int i = 0; i < 5; i++)
    {
      localJMenuItem = this._viewMenu.add(new JCheckBoxMenuItem(ResourceManager.getMessage("jnlp.viewer.view." + i + ".mi"), i == 0));
      
      localJMenuItem.setMnemonic(ResourceManager.getVKCode("jnlp.viewer.view." + i + ".mi.mnemonic"));
      
      localJMenuItem.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent paramAnonymousActionEvent)
        {
          Object localObject = paramAnonymousActionEvent.getSource();
          SwingUtilities.invokeLater(new Runnable()
          {
            private final Object val$source;
            
            public void run()
            {
              for (int i = 0; i < 5; i++)
              {
                JMenuItem localJMenuItem = CacheViewer.this._viewMenu.getItem(i);
                if ((localJMenuItem instanceof JCheckBoxMenuItem))
                {
                  JCheckBoxMenuItem localJCheckBoxMenuItem = (JCheckBoxMenuItem)localJMenuItem;
                  if (this.val$source.equals(localJCheckBoxMenuItem))
                  {
                    localJCheckBoxMenuItem.setState(true);
                    CacheViewer.this.setFilter(i);
                  }
                  else
                  {
                    localJCheckBoxMenuItem.setState(false);
                  }
                }
              }
            }
          });
        }
      });
    }
    this._helpMenu = new JMenu(ResourceManager.getMessage("jnlp.viewer.help.menu"));
    
    this._helpMenu.setMnemonic(ResourceManager.getVKCode("jnlp.viewer.help.menu.mnemonic"));
    
    localJMenuItem = this._helpMenu.add(ResourceManager.getMessage("jnlp.viewer.help.java.mi"));
    
    localJMenuItem.setMnemonic(ResourceManager.getVKCode("jnlp.viewer.help.java.mi.mnemonic"));
    
    localJMenuItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramAnonymousActionEvent)
      {
        String str = Config.getProperty("deployment.home.j2se.url");
        try
        {
          URL localURL = new URL(str);
          CacheViewer.this.showDocument(localURL);
        }
        catch (Exception localException)
        {
          Trace.ignoredException(localException);
        }
      }
    });
    localJMenuItem = this._helpMenu.add(ResourceManager.getMessage("jnlp.viewer.help.jnlp.mi"));
    
    localJMenuItem.setMnemonic(ResourceManager.getVKCode("jnlp.viewer.help.jnlp.mi.mnemonic"));
    
    localJMenuItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramAnonymousActionEvent)
      {
        String str = Config.getProperty("deployment.javaws.home.jnlp.url");
        try
        {
          URL localURL = new URL(str);
          CacheViewer.this.showDocument(localURL);
        }
        catch (Exception localException)
        {
          Trace.ignoredException(localException);
        }
      }
    });
    this._appMenu.addSeparator();
    
    localJMenuItem = this._helpMenu.add(ResourceManager.getMessage("jnlp.viewer.about.mi"));
    
    localJMenuItem.setMnemonic(ResourceManager.getVKCode("jnlp.viewer.about.mi.mnemonic"));
    
    localJMenuItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramAnonymousActionEvent)
      {
        CacheViewer.this.showAbout();
      }
    });
    JMenuBar localJMenuBar = new JMenuBar();
    localJMenuBar.add(this._fileMenu);
    localJMenuBar.add(this._editMenu);
    localJMenuBar.add(this._appMenu);
    localJMenuBar.add(this._viewMenu);
    
    localJMenuBar.add(this._helpMenu);
    setJMenuBar(localJMenuBar);
    resetSizes();
    refresh();
  }
  
  private void setFilter(int paramInt)
  {
    String str;
    if (paramInt == 0)
    {
      str = ResourceManager.getMessage("jnlp.viewer.all");
    }
    else
    {
      MessageFormat localMessageFormat = new MessageFormat(ResourceManager.getMessage("jnlp.viewer.type"));
      
      Object[] arrayOfObject = new Object[1];
      arrayOfObject[0] = ResourceManager.getMessage("jnlp.viewer.view." + paramInt);
      str = localMessageFormat.format(arrayOfObject);
    }
    this._titledBorder.setTitle(str);
    getSelectedTable().setFilter(paramInt);
    getContentPane().repaint();
  }
  
  public JButton makeButton(String paramString)
  {
    JButton localJButton = new JButton(ResourceManager.getMessage(paramString));
    localJButton.setMnemonic(ResourceManager.getVKCode(paramString + ".mnemonic"));
    localJButton.addActionListener(this);
    return localJButton;
  }
  
  public void valueChanged(ListSelectionEvent paramListSelectionEvent)
  {
    refresh();
  }
  
  public void stateChanged(ChangeEvent paramChangeEvent)
  {
    refresh();
    resetSizes();
  }
  
  private void resetSizes()
  {
    Component localComponent = this._tabbedPane.getSelectedComponent();
    boolean bool = !localComponent.equals(this._userTab);
    new Thread(new Runnable()
    {
      private final boolean val$system;
      
      public void run()
      {
        if (CacheViewer.getStatus() == 0) {
          CacheViewer.setStatus(5);
        }
        try
        {
          long l = Cache.getCacheSize(this.val$system);
          if (l > 0L)
          {
            CacheViewer._totalSize.setText(CacheViewer.this.getAppMessage("jnlp.viewer.totalSize", CacheViewer.access$1200(l)));
          }
          else
          {
            String str;
            if (l < 0L)
            {
              str = CacheViewer.this._tabbedPane.getTitleAt(CacheViewer.this._tabbedPane.getSelectedIndex());
              
              CacheViewer._totalSize.setText(CacheViewer.this.getMessage("jnlp.viewer.noCache"));
            }
            else
            {
              str = CacheViewer.this._tabbedPane.getTitleAt(CacheViewer.this._tabbedPane.getSelectedIndex());
              
              CacheViewer._totalSize.setText(CacheViewer.this.getAppMessage("jnlp.viewer.emptyCache", str));
            }
          }
        }
        finally
        {
          if (CacheViewer.getStatus() == 5) {
            CacheViewer.setStatus(0);
          }
        }
      }
    }).start();
  }
  
  private static String tformat(long paramLong)
  {
    if (paramLong > 10240L) {
      return "" + paramLong / 1024L + " KB";
    }
    return "" + paramLong / 1024L + "." + paramLong % 1024L / 102L + " KB";
  }
  
  public void refresh()
  {
    Component localComponent = this._tabbedPane.getSelectedComponent();
    int i = !localComponent.equals(this._userTab) ? 1 : 0;
    CacheTable localCacheTable;
    if (i != 0) {
      localCacheTable = this._sysTable;
    } else {
      localCacheTable = this._userTable;
    }
    int[] arrayOfInt = localCacheTable.getSelectedRows();
    
    boolean bool1 = false;
    boolean bool2 = false;
    boolean bool3 = false;
    boolean bool4 = false;
    boolean bool5 = false;
    boolean bool6 = false;
    boolean bool7 = (i == 0) && (arrayOfInt.length > 0);
    
    String str = "";
    if (arrayOfInt.length == 1)
    {
      bool4 = true;
      CacheObject localCacheObject = localCacheTable.getCacheObject(arrayOfInt[0]);
      if (localCacheObject != null)
      {
        LaunchDesc localLaunchDesc = localCacheObject.getLaunchDesc();
        str = localCacheObject.getTypeString();
        InformationDesc localInformationDesc = localLaunchDesc.getInformation();
        if ((localLaunchDesc.isApplication()) || (localLaunchDesc.isApplet()))
        {
          if (_isLocalInstallSupported)
          {
            LocalApplicationProperties localLocalApplicationProperties = localCacheObject.getLocalApplicationProperties();
            
            bool6 = localLocalApplicationProperties.isLocallyInstalled();
            bool3 = (i == 0) && (!localLocalApplicationProperties.isLocallyInstalledSystem());
          }
          if (localInformationDesc.supportsOfflineOperation()) {
            bool2 = true;
          }
          if (localLaunchDesc.getLocation() != null) {
            bool1 = true;
          }
        }
        if (localInformationDesc.getHome() != null) {
          bool5 = true;
        }
        this._removeBtn.setText(getAppMessage("jnlp.viewer.remove.1.btn", str));
      }
    }
    else if (arrayOfInt.length == 0)
    {
      this._removeBtn.setText(ResourceManager.getMessage("jnlp.viewer.remove.btn"));
    }
    else
    {
      this._removeBtn.setText(ResourceManager.getMessage("jnlp.viewer.remove.2.btn"));
    }
    this._launchOnlineBtn.setEnabled(bool1);
    this._launchOnlineMI.setEnabled(bool1);
    this._launchOnlineMI.setText(getMessage("jnlp.viewer.launch.online.mi"));
    
    this._launchOfflineBtn.setEnabled(bool2);
    this._launchOfflineMI.setEnabled(bool2);
    this._launchOfflineMI.setText(getMessage("jnlp.viewer.launch.offline.mi"));
    if (_isLocalInstallSupported)
    {
      this._installMI.setEnabled(bool3);
      if (bool6)
      {
        this._installMI.setText(getMessage("jnlp.viewer.uninstall.mi"));
        
        this._installMI.setMnemonic(ResourceManager.getVKCode("jnlp.viewer.uninstall.mi.mnemonic"));
      }
      else
      {
        this._installMI.setText(getMessage("jnlp.viewer.install.mi"));
        
        this._installMI.setMnemonic(ResourceManager.getVKCode("jnlp.viewer.install.mi.mnemonic"));
      }
    }
    this._showMI.setEnabled(bool4);
    this._showMI.setText(getMessage("jnlp.viewer.show.mi"));
    
    this._browseMI.setEnabled(bool5);
    this._browseMI.setText(getMessage("jnlp.viewer.browse.mi"));
    
    this._removeBtn.setEnabled(bool7);
    this._removeMI.setEnabled(bool7);
    if (arrayOfInt.length == 1) {
      this._removeMI.setText(getAppMessage("jnlp.viewer.remove.mi", str));
    } else {
      this._removeMI.setText(getMessage("jnlp.viewer.remove.0.mi"));
    }
  }
  
  private String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }
  
  private String getAppMessage(String paramString1, String paramString2)
  {
    MessageFormat localMessageFormat = new MessageFormat(ResourceManager.getMessage(paramString1));
    Object[] arrayOfObject = new Object[1];
    arrayOfObject[0] = paramString2;
    return localMessageFormat.format(arrayOfObject);
  }
  
  private CacheObject getSelectedCacheObject()
  {
    Component localComponent = this._tabbedPane.getSelectedComponent();
    CacheTable localCacheTable;
    if (localComponent.equals(this._userTab)) {
      localCacheTable = this._userTable;
    } else {
      localCacheTable = this._sysTable;
    }
    int[] arrayOfInt = localCacheTable.getSelectedRows();
    if (arrayOfInt.length == 1) {
      return localCacheTable.getCacheObject(arrayOfInt[0]);
    }
    return null;
  }
  
  private void closeDialog(WindowEvent paramWindowEvent)
  {
    exitViewer();
  }
  
  private void exitViewer()
  {
    this._sil.removeSingleInstanceListener(this);
    setVisible(false);
    dispose();
    Rectangle localRectangle = getBounds();
    Config.setProperty("deployment.javaws.viewer.bounds", "" + localRectangle.x + "," + localRectangle.y + "," + localRectangle.width + "," + localRectangle.height);
    
    Config.storeIfDirty();
    Main.systemExit(0);
  }
  
  public void actionPerformed(ActionEvent paramActionEvent)
  {
    JButton localJButton = (JButton)paramActionEvent.getSource();
    if (localJButton == this._removeBtn) {
      removeApplications();
    } else if (localJButton == this._launchOnlineBtn) {
      launchApplication(true);
    } else if (localJButton == this._launchOfflineBtn) {
      launchApplication(false);
    }
  }
  
  private CacheTable getSelectedTable()
  {
    return this._tabbedPane.getSelectedComponent() == this._userTab ? this._userTable : this._sysTable;
  }
  
  private void launchApplication(boolean paramBoolean)
  {
    if (getStatus() != 2)
    {
      if (getStatus() == 0) {
        setStatus(2);
      }
      try
      {
        CacheObject localCacheObject = getSelectedCacheObject();
        if (localCacheObject != null) {
          try
          {
            File localFile = localCacheObject.getJnlpFile();
            String[] arrayOfString = new String[3];
            arrayOfString[0] = Config.getJavawsCommand();
            arrayOfString[1] = (paramBoolean ? "-online" : "-offline");
            arrayOfString[2] = localFile.getPath();
            Runtime.getRuntime().exec(arrayOfString);
          }
          catch (IOException localIOException)
          {
            Trace.ignoredException(localIOException);
          }
        }
        SwingUtilities.invokeLater(new Runnable()
        {
          public void run()
          {
            CacheViewer.this.reset(CacheViewer.this._userTable);
          }
        });
      }
      finally
      {
        if (getStatus() == 2) {
          setStatus(0);
        }
      }
    }
  }
  
  public void launchApplication()
  {
    if (this._launchOnlineBtn.isEnabled()) {
      launchApplication(true);
    } else if (this._launchOfflineBtn.isEnabled()) {
      launchApplication(false);
    }
  }
  
  private void browseApplication()
  {
    CacheObject localCacheObject = getSelectedCacheObject();
    if (localCacheObject != null)
    {
      LaunchDesc localLaunchDesc = localCacheObject.getLaunchDesc();
      if (localLaunchDesc != null)
      {
        URL localURL = localLaunchDesc.getInformation().getHome();
        showDocument(localURL);
      }
    }
  }
  
  private void showDocument(URL paramURL)
  {
    if (getStatus() != 3) {
      new Thread(new Runnable()
      {
        private final URL val$page;
        
        public void run()
        {
          if (CacheViewer.getStatus() == 0) {
            CacheViewer.setStatus(3);
          }
          try
          {
            BrowserSupport.showDocument(this.val$page);
          }
          finally
          {
            if (CacheViewer.getStatus() == 3) {
              CacheViewer.setStatus(0);
            }
          }
        }
      }).start();
    }
  }
  
  private void showApplication()
  {
    CacheObject localCacheObject = getSelectedCacheObject();
    if (localCacheObject != null)
    {
      LaunchDesc localLaunchDesc = localCacheObject.getLaunchDesc();
      String str = localLaunchDesc.toString();
      
      JTextArea localJTextArea = new JTextArea(str, 24, 81);
      localJTextArea.setEditable(false);
      
      JScrollPane localJScrollPane = new JScrollPane(localJTextArea, 20, 30);
      
      DialogFactory.showMessageDialog(this, 2, localJScrollPane, getAppMessage("jnlp.viewer.show.title", localLaunchDesc.getInformation().getTitle()), false);
    }
  }
  
  private void showAbout()
  {
    new AboutDialog(this, true).setVisible(true);
  }
  
  private void cleanCache()
  {
    if (getStatus() == 0) {
      new Thread(new Runnable()
      {
        public void run()
        {
          CacheViewer.setStatus(1);
          SwingUtilities.invokeLater(new Runnable()
          {
            public void run()
            {
              try
              {
                Cache.clean();
                CacheViewer.this.reset(CacheViewer.this._userTable);
              }
              finally
              {
                if (CacheViewer.getStatus() == 1) {
                  CacheViewer.setStatus(0);
                }
              }
            }
          });
        }
      }).start();
    }
  }
  
  private void removeApplications()
  {
    if (getStatus() == 0) {
      new Thread(new Runnable()
      {
        public void run()
        {
          CacheViewer.setStatus(1);
          Component localComponent = CacheViewer.this._tabbedPane.getSelectedComponent();
          int i = !localComponent.equals(CacheViewer.this._userTab) ? 1 : 0;
          CacheTable localCacheTable = i != 0 ? CacheViewer.this._sysTable : CacheViewer.this._userTable;
          
          int[] arrayOfInt = localCacheTable.getSelectedRows();
          for (int j = 0; j < arrayOfInt.length; j++)
          {
            CacheObject localCacheObject = localCacheTable.getCacheObject(arrayOfInt[j]);
            Cache.remove(localCacheObject.getDCE(), localCacheObject.getLocalApplicationProperties(), localCacheObject.getLaunchDesc());
          }
          Cache.clean();
          SwingUtilities.invokeLater(new Runnable()
          {
            public void run()
            {
              try
              {
                CacheViewer.this.reset(CacheViewer.this._userTable);
              }
              finally
              {
                if (CacheViewer.getStatus() == 1) {
                  CacheViewer.setStatus(0);
                }
              }
            }
          });
        }
      }).start();
    }
  }
  
  public void popupApplicationMenu(Component paramComponent, int paramInt1, int paramInt2)
  {
    CacheObject localCacheObject = getSelectedCacheObject();
    if (localCacheObject != null)
    {
      JPopupMenu localJPopupMenu = new JPopupMenu();
      Component[] arrayOfComponent = this._appMenu.getMenuComponents();
      for (int i = 0; i < arrayOfComponent.length; i++) {
        if ((arrayOfComponent[i] instanceof JMenuItem))
        {
          JMenuItem localJMenuItem1 = (JMenuItem)arrayOfComponent[i];
          JMenuItem localJMenuItem2 = localJPopupMenu.add(new JMenuItem(localJMenuItem1.getText(), localJMenuItem1.getMnemonic()));
          
          localJMenuItem2.setEnabled(localJMenuItem1.isEnabled());
          ActionListener[] arrayOfActionListener = localJMenuItem1.getActionListeners();
          for (int j = 0; j < arrayOfActionListener.length; localJMenuItem2.addActionListener(arrayOfActionListener[(j++)])) {}
        }
        else
        {
          localJPopupMenu.addSeparator();
        }
      }
      localJPopupMenu.show(paramComponent, paramInt1, paramInt2);
    }
  }
  
  private void integrateApplication()
  {
    CacheObject localCacheObject = getSelectedCacheObject();
    if ((localCacheObject != null) && (_isLocalInstallSupported))
    {
      LocalApplicationProperties localLocalApplicationProperties = localCacheObject.getLocalApplicationProperties();
      Component localComponent = this._tabbedPane.getSelectedComponent();
      int i = !localComponent.equals(this._userTab) ? 1 : 0;
      CacheTable localCacheTable = i != 0 ? this._sysTable : this._userTable;
      
      new Thread(new Installer(localCacheObject.getLaunchDesc(), localLocalApplicationProperties, localCacheTable)).start();
    }
  }
  
  class Installer
    implements Runnable
  {
    private final LaunchDesc _ld;
    private final LocalApplicationProperties _lap;
    private final CacheTable _table;
    
    public Installer(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, CacheTable paramCacheTable)
    {
      this._ld = paramLaunchDesc;
      this._lap = paramLocalApplicationProperties;
      this._table = paramCacheTable;
    }
    
    public void run()
    {
      this._lap.refreshIfNecessary();
      if (this._lap.isLocallyInstalled()) {
        CacheViewer._lih.uninstall(this._ld, this._lap, true);
      } else {
        CacheViewer._lih.doInstall(this._ld, this._lap);
      }
      this._lap.setAskedForInstall(true);
      try
      {
        this._lap.store();
      }
      catch (Exception localException)
      {
        Trace.ignoredException(localException);
      }
      CacheViewer.this.refresh();
    }
  }
  
  public void reset(CacheTable paramCacheTable)
  {
    resetSizes();
    paramCacheTable.reset();
    refresh();
  }
  
  public static int getStatus()
  {
    return _status;
  }
  
  public static void setStatus(int paramInt)
  {
    _status = paramInt;
    String str;
    switch (paramInt)
    {
    case 1: 
      str = ResourceManager.getMessage("jnlp.viewer.removing");
      break;
    case 2: 
      str = ResourceManager.getMessage("jnlp.viewer.launching");
      break;
    case 3: 
      str = ResourceManager.getMessage("jnlp.viewer.browsing");
      break;
    case 4: 
      str = ResourceManager.getMessage("jnlp.viewer.sorting");
      break;
    case 5: 
      str = ResourceManager.getMessage("jnlp.viewer.searching");
      break;
    case 6: 
      str = ResourceManager.getMessage("jnlp.viewer.installing");
      break;
    case 0: 
    default: 
      str = "";
    }
    if (paramInt == 0)
    {
      _statusLabel.setText(str);
      _totalSize.setVisible(true);
    }
    else
    {
      _totalSize.setVisible(false);
      _statusLabel.setText(str);
    }
  }
  
  private void showReInstallDialog()
  {
    Properties localProperties = Cache.getRemovedApps();
    
    Object localObject = this._userTable.getAllHrefs();
    int i = 0;
    for (int j = 0; j < localObject.length; j++) {
      if (localProperties.getProperty(localObject[j]) != null)
      {
        localProperties.remove(localObject[j]);
        i = 1;
      }
    }
    if (i != 0) {
      Cache.setRemovedApps(localProperties);
    }
    localObject = new ArrayList();
    ArrayList localArrayList = new ArrayList();
    
    Enumeration localEnumeration = localProperties.propertyNames();
    while (localEnumeration.hasMoreElements())
    {
      str1 = (String)localEnumeration.nextElement();
      ((ArrayList)localObject).add(str1);
      localArrayList.add(localProperties.getProperty(str1));
    }
    String str1 = ResourceManager.getMessage("jnlp.viewer.reinstall.column.title");
    
    String str2 = ResourceManager.getMessage("jnlp.viewer.reinstall.column.location");
    
    AbstractTableModel local21 = new AbstractTableModel()
    {
      private final String val$titleName;
      private final String val$hrefName;
      private final ArrayList val$titles;
      private final ArrayList val$hrefs;
      
      public String getColumnName(int paramAnonymousInt)
      {
        return paramAnonymousInt == 0 ? this.val$titleName : this.val$hrefName;
      }
      
      public Object getValueAt(int paramAnonymousInt1, int paramAnonymousInt2)
      {
        return paramAnonymousInt2 == 0 ? this.val$titles.get(paramAnonymousInt1) : this.val$hrefs.get(paramAnonymousInt1);
      }
      
      public int getColumnCount()
      {
        return 2;
      }
      
      public int getRowCount()
      {
        return this.val$hrefs.size();
      }
      
      public Class getColumnClass(int paramAnonymousInt)
      {
        return String.class;
      }
    };
    String str3 = "jnlp.viewer.reinstallBtn";
    JButton localJButton1 = new JButton(ResourceManager.getMessage(str3));
    localJButton1.setMnemonic(ResourceManager.getVKCode(str3 + ".mnemonic"));
    localJButton1.setEnabled(false);
    
    str3 = "jnlp.viewer.closeBtn";
    JButton localJButton2 = new JButton(ResourceManager.getMessage(str3));
    localJButton2.setMnemonic(ResourceManager.getVKCode(str3 + ".mnemonic"));
    
    Object[] arrayOfObject = { localJButton1, localJButton2 };
    
    JTable localJTable = new JTable(local21);
    localJButton1.addActionListener(new ActionListener()
    {
      private final JTable val$table;
      private final ArrayList val$hrefs;
      
      public void actionPerformed(ActionEvent paramAnonymousActionEvent)
      {
        int[] arrayOfInt = this.val$table.getSelectedRows();
        String[] arrayOfString = new String[arrayOfInt.length];
        for (int i = 0; i < arrayOfString.length; i++) {
          arrayOfString[i] = ((String)this.val$hrefs.get(arrayOfInt[i]));
        }
        CacheViewer.this.do_reinstall(arrayOfString);
      }
    });
    localJTable.getColumnModel().getColumn(0).setPreferredWidth(200);
    localJTable.getColumnModel().getColumn(1).setPreferredWidth(440);
    localJTable.setPreferredScrollableViewportSize(new Dimension(640, 180));
    JScrollPane localJScrollPane = new JScrollPane(localJTable);
    localJTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
    {
      private final JButton val$reinstall;
      private final JTable val$table;
      
      public void valueChanged(ListSelectionEvent paramAnonymousListSelectionEvent)
      {
        this.val$reinstall.setEnabled(this.val$table.getSelectedRowCount() > 0);
      }
    });
    int k = DialogFactory.showOptionDialog(this, 5, localJScrollPane, ResourceManager.getMessage("jnlp.viewer.reinstall.title"), arrayOfObject, localJButton2);
  }
  
  public void do_reinstall(String[] paramArrayOfString)
  {
    new Thread(new Runnable()
    {
      private final String[] val$hrefs;
      
      public void run()
      {
        if (CacheViewer.getStatus() == 0) {
          CacheViewer.setStatus(6);
        }
        try
        {
          for (i = 0; i < this.val$hrefs.length; i++)
          {
            Main.importApp(this.val$hrefs[i]);
            
            k = 0;
            while (Main.getLaunchThreadGroup().activeCount() > 8)
            {
              try
              {
                Thread.sleep(2000L);
              }
              catch (Exception localException2) {}
              k++;
              if (k > 5) {
                break;
              }
            }
            if (Main.getLaunchThreadGroup().activeCount() > 8) {
              Trace.println("Warning: after waiting, still " + Main.getLaunchThreadGroup().activeCount() + " launching threads");
            }
          }
        }
        catch (Exception localException1)
        {
          int i;
          Trace.ignoredException(localException1);
        }
        finally
        {
          int k;
          int j;
          for (int m = 10; m > 0; m--)
          {
            int n = Main.getLaunchThreadGroup().activeCount();
            if (n <= 0) {
              break;
            }
            try
            {
              Thread.sleep(2000L);
            }
            catch (Exception localException5) {}
          }
          if (Main.getLaunchThreadGroup().activeCount() > 0) {
            Trace.println("Warning: after waiting 20 sec., still " + Main.getLaunchThreadGroup().activeCount() + " launching threads");
          }
          if (CacheViewer.getStatus() == 6) {
            CacheViewer.setStatus(0);
          }
        }
      }
    }).start();
  }
  
  public void newActivation(String[] paramArrayOfString)
  {
    this._userTable.setFilter(0);
    this._sysTable.setFilter(0);
    setExtendedState(getExtendedState() & 0xFFFFFFFE);
    toFront();
  }
  
  public Object getSingleInstanceListener()
  {
    return this;
  }
  
  public static void main(String[] paramArrayOfString)
  {
    
    if ((SingleInstanceManager.isServerRunning(JAVAWS_CV_ID)) && 
      (SingleInstanceManager.connectToServer("dummy"))) {
      System.exit(0);
    }
    LookAndFeel localLookAndFeel = null;
    try
    {
      localLookAndFeel = DeployUIManager.setLookAndFeel();
      if (Config.getBooleanProperty("deployment.debug.console")) {
        JavawsConsoleController.showConsoleIfEnable();
      }
      CacheViewer localCacheViewer = new CacheViewer();
      
      String str1 = Config.getProperty("deployment.javaws.viewer.bounds");
      if (str1 != null)
      {
        StringTokenizer localStringTokenizer = new StringTokenizer(str1, ",");
        int[] arrayOfInt = new int[4];
        for (int i = 0; i < 4; i++) {
          if (localStringTokenizer.hasMoreTokens())
          {
            String str2 = localStringTokenizer.nextToken();
            try
            {
              arrayOfInt[i] = Integer.parseInt(str2);
            }
            catch (NumberFormatException localNumberFormatException) {}
          }
        }
        if (i == 4) {
          localCacheViewer.setBounds(arrayOfInt[0], arrayOfInt[1], arrayOfInt[2], arrayOfInt[3]);
        }
      }
      localCacheViewer.setVisible(true);
      
      long l1 = Cache.getLastAccessed(false);
      long l2 = Cache.getLastAccessed(true);
      for (;;)
      {
        try
        {
          Thread.sleep(2000L);
        }
        catch (InterruptedException localInterruptedException)
        {
          break;
        }
        long l3 = Cache.getLastAccessed(false);
        long l4 = Cache.getLastAccessed(true);
        if ((l3 != l1) && 
          (getStatus() == 0))
        {
          l1 = l3;
          
          SwingUtilities.invokeLater(new Runnable()
          {
            private final CacheViewer val$cv;
            
            public void run()
            {
              this.val$cv.reset(this.val$cv._userTable);
            }
          });
        }
        if ((l4 != l2) && 
          (getStatus() == 0))
        {
          l2 = l4;
          
          SwingUtilities.invokeLater(new Runnable()
          {
            private final CacheViewer val$cv;
            
            public void run()
            {
              this.val$cv.reset(this.val$cv._sysTable);
            }
          });
        }
      }
    }
    finally
    {
      DeployUIManager.restoreLookAndFeel(localLookAndFeel);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\ui\CacheViewer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */