package org.fourthline.cling.transport.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.logging.Logger;
import sun.net.www.protocol.http.Handler;
import sun.net.www.protocol.http.HttpURLConnection;

public class FixedSunURLStreamHandler
  implements URLStreamHandlerFactory
{
  private static final Logger log = Logger.getLogger(FixedSunURLStreamHandler.class.getName());
  
  public URLStreamHandler createURLStreamHandler(String protocol)
  {
    log.fine("Creating new URLStreamHandler for protocol: " + protocol);
    if ("http".equals(protocol)) {
      new Handler()
      {
        protected URLConnection openConnection(URL u)
          throws IOException
        {
          return openConnection(u, null);
        }
        
        protected URLConnection openConnection(URL u, Proxy p)
          throws IOException
        {
          return new FixedSunURLStreamHandler.UpnpURLConnection(u, this);
        }
      };
    }
    return null;
  }
  
  static class UpnpURLConnection
    extends HttpURLConnection
  {
    private static final String[] methods = { "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "SUBSCRIBE", "UNSUBSCRIBE", "NOTIFY" };
    
    protected UpnpURLConnection(URL u, Handler handler)
      throws IOException
    {
      super(handler);
    }
    
    public UpnpURLConnection(URL u, String host, int port)
      throws IOException
    {
      super(host, port);
    }
    
    public synchronized OutputStream getOutputStream()
      throws IOException
    {
      String savedMethod = this.method;
      if ((this.method.equals("PUT")) || (this.method.equals("POST")) || (this.method.equals("NOTIFY"))) {
        this.method = "PUT";
      } else {
        this.method = "GET";
      }
      OutputStream os = super.getOutputStream();
      this.method = savedMethod;
      return os;
    }
    
    public void setRequestMethod(String method)
      throws ProtocolException
    {
      if (this.connected) {
        throw new ProtocolException("Cannot reset method once connected");
      }
      for (String m : methods) {
        if (m.equals(method))
        {
          this.method = method;
          return;
        }
      }
      throw new ProtocolException("Invalid UPnP HTTP method: " + method);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\impl\FixedSunURLStreamHandler.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */