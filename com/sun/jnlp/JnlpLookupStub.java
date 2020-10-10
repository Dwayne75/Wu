package com.sun.jnlp;

import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.jnlp.ServiceManagerStub;
import javax.jnlp.UnavailableServiceException;

public final class JnlpLookupStub
  implements ServiceManagerStub
{
  public Object lookup(String paramString)
    throws UnavailableServiceException
  {
    Object localObject = AccessController.doPrivileged(new PrivilegedAction()
    {
      private final String val$name;
      
      public Object run()
      {
        return JnlpLookupStub.this.findService(this.val$name);
      }
    });
    if (localObject == null) {
      throw new UnavailableServiceException(paramString);
    }
    return localObject;
  }
  
  private Object findService(String paramString)
  {
    if (paramString != null)
    {
      if (paramString.equals("javax.jnlp.BasicService")) {
        return BasicServiceImpl.getInstance();
      }
      if (paramString.equals("javax.jnlp.FileOpenService")) {
        return FileOpenServiceImpl.getInstance();
      }
      if (paramString.equals("javax.jnlp.FileSaveService")) {
        return FileSaveServiceImpl.getInstance();
      }
      if (paramString.equals("javax.jnlp.ExtensionInstallerService")) {
        return ExtensionInstallerServiceImpl.getInstance();
      }
      if (paramString.equals("javax.jnlp.DownloadService")) {
        return DownloadServiceImpl.getInstance();
      }
      if (paramString.equals("javax.jnlp.ClipboardService")) {
        return ClipboardServiceImpl.getInstance();
      }
      if (paramString.equals("javax.jnlp.PrintService")) {
        return PrintServiceImpl.getInstance();
      }
      if (paramString.equals("javax.jnlp.PersistenceService")) {
        return PersistenceServiceImpl.getInstance();
      }
      if (paramString.equals("javax.jnlp.ExtendedService")) {
        return ExtendedServiceImpl.getInstance();
      }
      if (paramString.equals("javax.jnlp.SingleInstanceService")) {
        return SingleInstanceServiceImpl.getInstance();
      }
    }
    return null;
  }
  
  public String[] getServiceNames()
  {
    if (ExtensionInstallerServiceImpl.getInstance() != null) {
      return new String[] { "javax.jnlp.BasicService", "javax.jnlp.FileOpenService", "javax.jnlp.FileSaveService", "javax.jnlp.ExtensionInstallerService", "javax.jnlp.DownloadService", "javax.jnlp.ClipboardService", "javax.jnlp.PersistenceService", "javax.jnlp.PrintService", "javax.jnlp.ExtendedService", "javax.jnlp.SingleInstanceService" };
    }
    return new String[] { "javax.jnlp.BasicService", "javax.jnlp.FileOpenService", "javax.jnlp.FileSaveService", "javax.jnlp.DownloadService", "javax.jnlp.ClipboardService", "javax.jnlp.PersistenceService", "javax.jnlp.PrintService", "javax.jnlp.ExtendedService", "javax.jnlp.SingleInstanceService" };
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\jnlp\JnlpLookupStub.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */