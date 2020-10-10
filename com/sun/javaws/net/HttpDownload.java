package com.sun.javaws.net;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public abstract interface HttpDownload
{
  public abstract void download(HttpResponse paramHttpResponse, File paramFile, HttpDownloadListener paramHttpDownloadListener)
    throws IOException, CanceledDownloadException;
  
  public abstract void download(URL paramURL, File paramFile, HttpDownloadListener paramHttpDownloadListener)
    throws IOException, CanceledDownloadException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\net\HttpDownload.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */