package com.sun.jnlp;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.util.Trace;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.jnlp.PrintService;

public final class PrintServiceImpl
  implements PrintService
{
  private static PrintServiceImpl _sharedInstance = null;
  private static SmartSecurityDialog _securityDialog = null;
  private PageFormat _pageFormat = null;
  
  public static synchronized PrintServiceImpl getInstance()
  {
    if (_sharedInstance == null) {
      _sharedInstance = new PrintServiceImpl();
    }
    return _sharedInstance;
  }
  
  public PageFormat getDefaultPage()
  {
    PrinterJob localPrinterJob = PrinterJob.getPrinterJob();
    if (localPrinterJob != null) {
      (PageFormat)AccessController.doPrivileged(new PrivilegedAction()
      {
        private final PrinterJob val$sysPrinterJob;
        
        public Object run()
        {
          return this.val$sysPrinterJob.defaultPage();
        }
      });
    }
    return null;
  }
  
  public PageFormat showPageFormatDialog(PageFormat paramPageFormat)
  {
    PrinterJob localPrinterJob = PrinterJob.getPrinterJob();
    if (localPrinterJob != null) {
      (PageFormat)AccessController.doPrivileged(new PrivilegedAction()
      {
        private final PrinterJob val$sysPrinterJob;
        private final PageFormat val$page;
        
        public Object run()
        {
          PrintServiceImpl.this._pageFormat = this.val$sysPrinterJob.pageDialog(this.val$page);
          return PrintServiceImpl.this._pageFormat;
        }
      });
    }
    return null;
  }
  
  public synchronized boolean print(Pageable paramPageable)
  {
    return doPrinting(null, paramPageable);
  }
  
  public synchronized boolean print(Printable paramPrintable)
  {
    return doPrinting(paramPrintable, null);
  }
  
  private boolean doPrinting(Printable paramPrintable, Pageable paramPageable)
  {
    if (!askUser()) {
      return false;
    }
    PrinterJob localPrinterJob = PrinterJob.getPrinterJob();
    if (localPrinterJob == null) {
      return false;
    }
    try
    {
      Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction()
      {
        private final Pageable val$document;
        private final PrinterJob val$sysPrinterJob;
        private final Printable val$painter;
        
        public Object run()
        {
          if (this.val$document != null) {
            this.val$sysPrinterJob.setPageable(this.val$document);
          } else if (PrintServiceImpl.this._pageFormat == null) {
            this.val$sysPrinterJob.setPrintable(this.val$painter);
          } else {
            this.val$sysPrinterJob.setPrintable(this.val$painter, PrintServiceImpl.this._pageFormat);
          }
          if (this.val$sysPrinterJob.printDialog())
          {
            Thread localThread = new Thread(new Runnable()
            {
              public void run()
              {
                try
                {
                  PrintServiceImpl.3.this.val$sysPrinterJob.print();
                }
                catch (PrinterException localPrinterException)
                {
                  Trace.ignoredException(localPrinterException);
                }
              }
            });
            localThread.start();
            return Boolean.TRUE;
          }
          return Boolean.FALSE;
        }
      });
      boolean bool = localBoolean.booleanValue();
      
      return bool;
    }
    finally {}
  }
  
  private synchronized boolean askUser()
  {
    if (CheckServicePermission.hasPrintAccessPermissions()) {
      return true;
    }
    return requestPrintPermission();
  }
  
  public static boolean requestPrintPermission()
  {
    if (_securityDialog == null) {
      _securityDialog = new SmartSecurityDialog(ResourceManager.getString("APIImpl.print.message"), true);
    }
    return _securityDialog.showDialog();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\jnlp\PrintServiceImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */