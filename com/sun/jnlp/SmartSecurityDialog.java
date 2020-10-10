package com.sun.jnlp;

import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.util.DeployUIManager;
import com.sun.deploy.util.DialogFactory;
import com.sun.javaws.Main;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.ItemSelectable;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;

final class SmartSecurityDialog
  extends Thread
{
  private boolean _remembered = false;
  private int _lastResult = -1;
  private boolean _cbChecked = false;
  private int _answer;
  private Object _signalObject = null;
  private String _message = null;
  private DummyDialog _dummyDialog;
  private EventQueue _sysEventQueue = null;
  private static final ThreadGroup _secureGroup = ;
  private Object[] _objs = null;
  
  SmartSecurityDialog()
  {
    this(null);
  }
  
  SmartSecurityDialog(String paramString)
  {
    this._signalObject = new Object();
    this._message = paramString;
  }
  
  SmartSecurityDialog(String paramString, boolean paramBoolean)
  {
    this(paramString);
    this._cbChecked = paramBoolean;
  }
  
  boolean showDialog(Object[] paramArrayOfObject)
  {
    this._objs = paramArrayOfObject;
    return showDialog();
  }
  
  boolean showDialog(String paramString)
  {
    this._message = paramString;
    this._objs = null;
    return showDialog();
  }
  
  boolean showDialog()
  {
    Integer localInteger = (Integer)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        return new Integer(SmartSecurityDialog.this.getUserDecision(null, SmartSecurityDialog.this._message));
      }
    });
    return localInteger.intValue() == 0;
  }
  
  private int getUserDecision(Frame paramFrame, String paramString)
  {
    if (this._remembered)
    {
      this._answer = this._lastResult;
      return this._answer;
    }
    if (!Config.getBooleanProperty("deployment.security.sandbox.jnlp.enhanced")) {
      return 1;
    }
    synchronized (this._signalObject)
    {
      this._sysEventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
      
      Thread localThread = new Thread(_secureGroup, this, "userDialog");
      
      this._message = paramString;
      
      this._dummyDialog = new DummyDialog((Frame)null, true);
      this._dummyDialog.addWindowListener(new WindowAdapter()
      {
        private final Thread val$handler;
        
        public void windowOpened(WindowEvent paramAnonymousWindowEvent)
        {
          this.val$handler.start();
        }
        
        public void windowClosing(WindowEvent paramAnonymousWindowEvent)
        {
          SmartSecurityDialog.this._dummyDialog.hide();
        }
      });
      Rectangle localRectangle = new Rectangle(new Point(0, 0), Toolkit.getDefaultToolkit().getScreenSize());
      
      this._dummyDialog.setLocation(localRectangle.x + localRectangle.width / 2 - 50, localRectangle.y + localRectangle.height / 2);
      if (Config.getOSName().equals("Windows")) {
        this._dummyDialog.setLocation(65336, 65336);
      }
      this._dummyDialog.setResizable(false);
      this._dummyDialog.toBack();
      this._dummyDialog.show();
      try
      {
        this._signalObject.wait();
      }
      catch (Exception localException)
      {
        localException.printStackTrace();
        this._dummyDialog.hide();
      }
      return this._answer;
    }
  }
  
  public void run()
  {
    LookAndFeel localLookAndFeel = DeployUIManager.setLookAndFeel();
    try
    {
      JPanel localJPanel = new JPanel();
      JCheckBox localJCheckBox = new JCheckBox(ResourceManager.getString("APIImpl.securityDialog.remember"), this._cbChecked);
      Font localFont1 = localJCheckBox.getFont();
      Font localFont2 = null;
      if (localFont1 != null)
      {
        localFont2 = localFont1.deriveFont(0);
        if (localFont2 != null) {
          localJCheckBox.setFont(localFont2);
        }
      }
      localJCheckBox.addItemListener(new ItemListener()
      {
        private final JCheckBox val$cb;
        
        public void itemStateChanged(ItemEvent paramAnonymousItemEvent)
        {
          ItemSelectable localItemSelectable = paramAnonymousItemEvent.getItemSelectable();
          if (localItemSelectable == this.val$cb) {
            if (paramAnonymousItemEvent.getStateChange() == 2) {
              SmartSecurityDialog.this.setCBChecked(false);
            } else if (paramAnonymousItemEvent.getStateChange() == 1) {
              SmartSecurityDialog.this.setCBChecked(true);
            }
          }
        }
      });
      localJPanel.add(localJCheckBox, "Center");
      localJCheckBox.setOpaque(false);
      localJPanel.setOpaque(false);
      Object[] arrayOfObject1;
      if (this._objs == null) {
        arrayOfObject1 = new Object[] { this._message, localJPanel };
      } else {
        arrayOfObject1 = this._objs;
      }
      Object[] arrayOfObject2 = { ResourceManager.getString("APIImpl.securityDialog.yes"), ResourceManager.getString("APIImpl.securityDialog.no") };
      
      int i = 1;
      try
      {
        int j = DialogFactory.showOptionDialog(4, arrayOfObject1, ResourceManager.getString("APIImpl.securityDialog.title"), arrayOfObject2, arrayOfObject2[0]);
        
        i = j == 0 ? 0 : 1;
        if (this._cbChecked)
        {
          this._remembered = true;
          this._lastResult = i;
        }
      }
      finally
      {
        this._dummyDialog.secureHide();
        synchronized (this._signalObject)
        {
          this._answer = i;
          this._signalObject.notify();
        }
      }
    }
    finally
    {
      DeployUIManager.restoreLookAndFeel(localLookAndFeel);
    }
  }
  
  private void setCBChecked(boolean paramBoolean)
  {
    this._cbChecked = paramBoolean;
  }
  
  private class DummyDialog
    extends JDialog
  {
    private ThreadGroup _unsecureGroup;
    
    DummyDialog(Frame paramFrame, boolean paramBoolean)
    {
      super(paramBoolean);
      this._unsecureGroup = Thread.currentThread().getThreadGroup();
    }
    
    public void secureHide()
    {
      new Thread(this._unsecureGroup, new Runnable()
      {
        public void run()
        {
          SmartSecurityDialog.DummyDialog.this.hide();
        }
      }).start();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\jnlp\SmartSecurityDialog.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */