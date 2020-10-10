package com.sun.javaws.net;

import java.io.BufferedInputStream;
import java.net.URL;

public abstract interface HttpResponse
{
  public abstract URL getRequest();
  
  public abstract int getStatusCode();
  
  public abstract int getContentLength();
  
  public abstract long getLastModified();
  
  public abstract String getContentType();
  
  public abstract String getResponseHeader(String paramString);
  
  public abstract BufferedInputStream getInputStream();
  
  public abstract void disconnect();
  
  public abstract String getContentEncoding();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\net\HttpResponse.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */