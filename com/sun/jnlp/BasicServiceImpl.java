package com.sun.jnlp;

import com.sun.javaws.BrowserSupport;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.jnlp.BasicService;

public final class BasicServiceImpl
  implements BasicService
{
  private URL _codebase = null;
  private boolean _isWebBrowserSupported;
  private boolean _isOffline;
  private static BasicServiceImpl _sharedInstance = null;
  
  private BasicServiceImpl(URL paramURL, boolean paramBoolean1, boolean paramBoolean2)
  {
    this._codebase = paramURL;
    this._isWebBrowserSupported = paramBoolean2;
    this._isOffline = paramBoolean1;
  }
  
  public static BasicServiceImpl getInstance()
  {
    return _sharedInstance;
  }
  
  public static void initialize(URL paramURL, boolean paramBoolean1, boolean paramBoolean2)
  {
    if (_sharedInstance == null) {
      _sharedInstance = new BasicServiceImpl(paramURL, paramBoolean1, paramBoolean2);
    }
  }
  
  public URL getCodeBase()
  {
    return this._codebase;
  }
  
  public boolean isOffline()
  {
    return this._isOffline;
  }
  
  public boolean showDocument(URL paramURL)
  {
    if (!isWebBrowserSupported()) {
      return false;
    }
    Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction()
    {
      private final URL val$url;
      
      public Object run()
      {
        URL localURL = this.val$url;
        try
        {
          localURL = new URL(BasicServiceImpl.this._codebase, this.val$url.toString());
        }
        catch (MalformedURLException localMalformedURLException) {}
        return new Boolean(BrowserSupport.showDocument(localURL));
      }
    });
    return localBoolean == null ? false : localBoolean.booleanValue();
  }
  
  public boolean isWebBrowserSupported()
  {
    return this._isWebBrowserSupported;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\jnlp\BasicServiceImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */