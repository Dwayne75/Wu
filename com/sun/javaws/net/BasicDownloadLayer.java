package com.sun.javaws.net;

import com.sun.deploy.util.Trace;
import com.sun.deploy.util.TraceLevel;
import com.sun.javaws.Globals;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.jar.Pack200.Unpacker;

public class BasicDownloadLayer
  implements HttpDownload
{
  private static final int BUF_SIZE = 32768;
  private HttpRequest _httpRequest;
  
  public BasicDownloadLayer(HttpRequest paramHttpRequest)
  {
    this._httpRequest = paramHttpRequest;
  }
  
  public void download(HttpResponse paramHttpResponse, File paramFile, HttpDownloadListener paramHttpDownloadListener)
    throws CanceledDownloadException, IOException
  {
    int i = paramHttpResponse.getContentLength();
    if (paramHttpDownloadListener != null) {
      paramHttpDownloadListener.downloadProgress(0, i);
    }
    Trace.println("Doing download", TraceLevel.NETWORK);
    
    BufferedInputStream localBufferedInputStream = paramHttpResponse.getInputStream();
    BufferedOutputStream localBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(paramFile));
    String str = paramHttpResponse.getContentEncoding();
    try
    {
      if ((str != null) && (str.compareTo("pack200-gzip") == 0) && (Globals.havePack200()))
      {
        Trace.println("download:encoding Pack200: = " + str, TraceLevel.NETWORK);
        Pack200.Unpacker localUnpacker = Pack200.newUnpacker();
        
        localUnpacker.addPropertyChangeListener(new PropertyChangeListener()
        {
          private final HttpDownloadListener val$dl;
          private final int val$length;
          
          public void propertyChange(PropertyChangeEvent paramAnonymousPropertyChangeEvent)
          {
            if ((this.val$dl != null) && (paramAnonymousPropertyChangeEvent.getPropertyName().compareTo("unpack.progress") == 0))
            {
              String str = (String)paramAnonymousPropertyChangeEvent.getNewValue();
              int i = str != null ? Integer.parseInt(str) : 0;
              this.val$dl.downloadProgress(i * this.val$length / 100, this.val$length);
            }
          }
        });
        JarOutputStream localJarOutputStream = new JarOutputStream(localBufferedOutputStream);
        localUnpacker.unpack(localBufferedInputStream, localJarOutputStream);
        localJarOutputStream.close();
      }
      else
      {
        Trace.println("download:encoding GZIP/Plain = " + str, TraceLevel.NETWORK);
        int j = 0;
        int k = 0;
        byte[] arrayOfByte = new byte[32768];
        while ((j = localBufferedInputStream.read(arrayOfByte)) != -1)
        {
          localBufferedOutputStream.write(arrayOfByte, 0, j);
          
          k += j;
          if ((k > i) && (i != 0)) {
            k = i;
          }
          if (paramHttpDownloadListener != null) {
            paramHttpDownloadListener.downloadProgress(k, i);
          }
        }
      }
      Trace.println("Wrote URL " + paramHttpResponse.getRequest() + " to file " + paramFile, TraceLevel.NETWORK);
      
      localBufferedInputStream.close();localBufferedInputStream = null;
      localBufferedOutputStream.close();localBufferedOutputStream = null;
    }
    catch (IOException localIOException)
    {
      Trace.println("Got exception while downloading resource: " + localIOException, TraceLevel.NETWORK);
      if (localBufferedInputStream != null)
      {
        localBufferedInputStream.close();localBufferedInputStream = null;
      }
      if (localBufferedOutputStream != null)
      {
        localBufferedOutputStream.close();localBufferedOutputStream = null;
      }
      if (paramFile != null) {
        paramFile.delete();
      }
      throw localIOException;
    }
    if (paramHttpDownloadListener != null) {
      paramHttpDownloadListener.downloadProgress(i, i);
    }
  }
  
  public void download(URL paramURL, File paramFile, HttpDownloadListener paramHttpDownloadListener)
    throws CanceledDownloadException, IOException
  {
    HttpResponse localHttpResponse = this._httpRequest.doGetRequest(paramURL);
    download(localHttpResponse, paramFile, paramHttpDownloadListener);
    localHttpResponse.disconnect();
  }
  
  class PropertyChangeListenerTask
    implements PropertyChangeListener
  {
    HttpDownloadListener _dl = null;
    
    PropertyChangeListenerTask(HttpDownloadListener paramHttpDownloadListener)
    {
      this._dl = paramHttpDownloadListener;
    }
    
    public void propertyChange(PropertyChangeEvent paramPropertyChangeEvent)
    {
      if (paramPropertyChangeEvent.getPropertyName().compareTo("unpack.progress") == 0)
      {
        String str = (String)paramPropertyChangeEvent.getNewValue();
        if ((this._dl != null) && (str != null)) {
          this._dl.downloadProgress(Integer.parseInt(str), 100);
        }
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\net\BasicDownloadLayer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */