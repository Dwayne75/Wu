package com.sun.javaws.cache;

import com.sun.deploy.util.Trace;
import com.sun.javaws.exceptions.JNLPException;
import com.sun.javaws.jnl.IconDesc;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.SwingUtilities;

public class CacheImageLoader
  implements Runnable
{
  private static CacheImageLoader _instance = null;
  private final Object _imageLoadingLock = new Object();
  private boolean _running = false;
  private ArrayList _toLoad = new ArrayList();
  
  private class LoadEntry
  {
    public IconDesc _id;
    public URL _url;
    public CacheImageLoaderCallback _cb;
    
    public LoadEntry(IconDesc paramIconDesc, CacheImageLoaderCallback paramCacheImageLoaderCallback)
    {
      this._id = paramIconDesc;
      this._cb = paramCacheImageLoaderCallback;
      this._url = null;
    }
    
    public LoadEntry(URL paramURL, CacheImageLoaderCallback paramCacheImageLoaderCallback)
    {
      this._url = paramURL;
      this._cb = paramCacheImageLoaderCallback;
      this._id = null;
    }
  }
  
  public static CacheImageLoader getInstance()
  {
    if (_instance == null) {
      _instance = new CacheImageLoader();
    }
    return _instance;
  }
  
  public void loadImage(IconDesc paramIconDesc, CacheImageLoaderCallback paramCacheImageLoaderCallback)
  {
    int i = 0;
    synchronized (this._imageLoadingLock)
    {
      if (!this._running)
      {
        this._running = true;
        i = 1;
      }
      this._toLoad.add(new LoadEntry(paramIconDesc, paramCacheImageLoaderCallback));
    }
    if (i != 0) {
      new Thread(this).start();
    }
  }
  
  public void loadImage(URL paramURL, CacheImageLoaderCallback paramCacheImageLoaderCallback)
  {
    int i = 0;
    synchronized (this._imageLoadingLock)
    {
      if (!this._running)
      {
        this._running = true;
        i = 1;
      }
      this._toLoad.add(new LoadEntry(paramURL, paramCacheImageLoaderCallback));
    }
    if (i != 0) {
      new Thread(this).start();
    }
  }
  
  public void run()
  {
    int i = 0;
    while (i == 0)
    {
      LoadEntry localLoadEntry = null;
      synchronized (this._imageLoadingLock)
      {
        if (this._toLoad.size() > 0)
        {
          localLoadEntry = (LoadEntry)this._toLoad.remove(0);
        }
        else
        {
          i = 1;
          this._running = false;
        }
      }
      if (i == 0) {
        try
        {
          ??? = null;
          Image localImage = null;
          File localFile = null;
          URL localURL = localLoadEntry._url;
          if (localURL == null)
          {
            ??? = DownloadProtocol.getCachedVersion(localLoadEntry._id.getLocation(), localLoadEntry._id.getVersion(), 2);
            if (??? != null) {
              try
              {
                localFile = ((DiskCacheEntry)???).getFile();
                localURL = localFile.toURL();
              }
              catch (Exception localException) {}
            }
          }
          if (localURL != null) {
            localImage = CacheUtilities.getSharedInstance().loadImage(localURL);
          }
          if (localImage != null) {
            publish(localLoadEntry, localImage, localFile, false);
          }
          if (localLoadEntry._id != null) {
            new DelayedImageLoader(localLoadEntry, localImage, (DiskCacheEntry)???).start();
          }
        }
        catch (MalformedURLException localMalformedURLException)
        {
          Trace.ignoredException(localMalformedURLException);
        }
        catch (IOException localIOException)
        {
          Trace.ignoredException(localIOException);
        }
      }
    }
  }
  
  private class DelayedImageLoader
    extends Thread
  {
    private CacheImageLoader.LoadEntry _entry;
    private Image _image;
    private DiskCacheEntry _dce;
    
    public DelayedImageLoader(CacheImageLoader.LoadEntry paramLoadEntry, Image paramImage, DiskCacheEntry paramDiskCacheEntry)
    {
      this._entry = paramLoadEntry;
      this._image = paramImage;
      this._dce = paramDiskCacheEntry;
    }
    
    public void run()
    {
      try
      {
        File localFile = null;
        if (DownloadProtocol.isUpdateAvailable(this._entry._id.getLocation(), this._entry._id.getVersion(), 2))
        {
          this._dce = DownloadProtocol.getResource(this._entry._id.getLocation(), this._entry._id.getVersion(), 2, false, null);
          if (this._dce != null) {
            localFile = this._dce.getFile();
          }
          if (localFile != null) {
            this._image = CacheUtilities.getSharedInstance().loadImage(localFile.getPath());
          }
          CacheImageLoader.publish(this._entry, this._image, localFile, false);
        }
        else if (this._dce != null)
        {
          localFile = this._dce.getFile();
        }
        CacheImageLoader.publish(this._entry, this._image, localFile, true);
      }
      catch (MalformedURLException localMalformedURLException)
      {
        Trace.ignoredException(localMalformedURLException);
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
      }
      catch (JNLPException localJNLPException)
      {
        Trace.ignoredException(localJNLPException);
      }
    }
  }
  
  private static void publish(LoadEntry paramLoadEntry, Image paramImage, File paramFile, boolean paramBoolean)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      private final boolean val$isComplete;
      private final CacheImageLoader.LoadEntry val$entry;
      private final Image val$image;
      private final File val$file;
      
      public void run()
      {
        if (this.val$isComplete) {
          this.val$entry._cb.finalImageAvailable(this.val$entry._id, this.val$image, this.val$file);
        } else {
          this.val$entry._cb.imageAvailable(this.val$entry._id, this.val$image, this.val$file);
        }
      }
    });
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\cache\CacheImageLoader.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */