package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public final class Handler
  extends URLStreamHandler
{
  private static final String JAVA_PROTOCOL_HANDLER_PACKAGES = "java.protocol.handler.pkgs";
  
  static void register()
  {
    register(Handler.class);
  }
  
  static void register(Class<? extends URLStreamHandler> handlerClass)
  {
    Preconditions.checkArgument("Handler".equals(handlerClass.getSimpleName()));
    
    String pkg = handlerClass.getPackage().getName();
    int lastDot = pkg.lastIndexOf('.');
    Preconditions.checkArgument(lastDot > 0, "package for Handler (%s) must have a parent package", new Object[] { pkg });
    
    String parentPackage = pkg.substring(0, lastDot);
    
    String packages = System.getProperty("java.protocol.handler.pkgs");
    if (packages == null) {
      packages = parentPackage;
    } else {
      packages = packages + "|" + parentPackage;
    }
    System.setProperty("java.protocol.handler.pkgs", packages);
  }
  
  protected URLConnection openConnection(URL url)
    throws IOException
  {
    return new PathURLConnection(url);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\Handler.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */