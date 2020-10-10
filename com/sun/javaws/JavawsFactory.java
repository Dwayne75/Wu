package com.sun.javaws;

import com.sun.javaws.net.BasicDownloadLayer;
import com.sun.javaws.net.BasicNetworkLayer;
import com.sun.javaws.net.HttpDownload;
import com.sun.javaws.net.HttpRequest;

public class JavawsFactory
{
  private static HttpRequest _httpRequestImpl = new BasicNetworkLayer();
  private static HttpDownload _httpDownloadImpl = new BasicDownloadLayer(_httpRequestImpl);
  
  public static HttpRequest getHttpRequestImpl()
  {
    return _httpRequestImpl;
  }
  
  public static HttpDownload getHttpDownloadImpl()
  {
    return _httpDownloadImpl;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\JavawsFactory.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */