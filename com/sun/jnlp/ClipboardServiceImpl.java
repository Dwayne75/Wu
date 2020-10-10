package com.sun.jnlp;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.util.Trace;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.jnlp.ClipboardService;

public final class ClipboardServiceImpl
  implements ClipboardService
{
  private static ClipboardServiceImpl _sharedInstance = null;
  private Clipboard _sysClipboard = null;
  private SmartSecurityDialog _readDialog = null;
  private SmartSecurityDialog _writeDialog = null;
  
  private ClipboardServiceImpl()
  {
    this._readDialog = new SmartSecurityDialog(ResourceManager.getString("APIImpl.clipboard.message.read"));
    this._writeDialog = new SmartSecurityDialog(ResourceManager.getString("APIImpl.clipboard.message.write"));
    
    Toolkit localToolkit = Toolkit.getDefaultToolkit();
    if (localToolkit != null) {
      this._sysClipboard = localToolkit.getSystemClipboard();
    }
  }
  
  public static synchronized ClipboardServiceImpl getInstance()
  {
    if (_sharedInstance == null) {
      _sharedInstance = new ClipboardServiceImpl();
    }
    return _sharedInstance;
  }
  
  public Transferable getContents()
  {
    if (!askUser(false)) {
      return null;
    }
    (Transferable)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        return ClipboardServiceImpl.this._sysClipboard.getContents(null);
      }
    });
  }
  
  public void setContents(Transferable paramTransferable)
  {
    if (!askUser(true)) {
      return;
    }
    AccessController.doPrivileged(new PrivilegedAction()
    {
      private final Transferable val$contents;
      
      public Object run()
      {
        if (this.val$contents != null)
        {
          DataFlavor[] arrayOfDataFlavor = this.val$contents.getTransferDataFlavors();
          if ((arrayOfDataFlavor == null) || (arrayOfDataFlavor[0] == null)) {
            return null;
          }
          try
          {
            if (this.val$contents.getTransferData(arrayOfDataFlavor[0]) == null) {
              return null;
            }
          }
          catch (IOException localIOException)
          {
            Trace.ignoredException(localIOException);
          }
          catch (UnsupportedFlavorException localUnsupportedFlavorException)
          {
            Trace.ignoredException(localUnsupportedFlavorException);
          }
        }
        ClipboardServiceImpl.this._sysClipboard.setContents(this.val$contents, null);
        return null;
      }
    });
  }
  
  private synchronized boolean askUser(boolean paramBoolean)
  {
    if (!hasClipboard()) {
      return false;
    }
    if (CheckServicePermission.hasClipboardPermissions()) {
      return true;
    }
    return paramBoolean ? this._writeDialog.showDialog() : this._readDialog.showDialog();
  }
  
  private boolean hasClipboard()
  {
    return this._sysClipboard != null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\jnlp\ClipboardServiceImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */