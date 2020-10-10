package com.sun.javaws.cache;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;

public class CacheUtilities
{
  private static CacheUtilities _instance = null;
  private Component _component;
  
  public static CacheUtilities getSharedInstance()
  {
    if (_instance == null) {
      synchronized (CacheUtilities.class)
      {
        if (_instance == null) {
          _instance = new CacheUtilities();
        }
      }
    }
    return _instance;
  }
  
  public Image loadImage(String paramString)
    throws IOException
  {
    Image localImage = Toolkit.getDefaultToolkit().createImage(paramString);
    if (localImage != null)
    {
      Component localComponent = getComponent();
      MediaTracker localMediaTracker = new MediaTracker(localComponent);
      localMediaTracker.addImage(localImage, 0);
      try
      {
        localMediaTracker.waitForID(0, 5000L);
      }
      catch (InterruptedException localInterruptedException)
      {
        throw new IOException("Failed to load");
      }
      return localImage;
    }
    return null;
  }
  
  public Image loadImage(URL paramURL)
    throws IOException
  {
    Image localImage = Toolkit.getDefaultToolkit().createImage(paramURL);
    if (localImage != null)
    {
      Component localComponent = getComponent();
      MediaTracker localMediaTracker = new MediaTracker(localComponent);
      localMediaTracker.addImage(localImage, 0);
      try
      {
        localMediaTracker.waitForID(0, 5000L);
      }
      catch (InterruptedException localInterruptedException)
      {
        throw new IOException("Failed to load");
      }
      return localImage;
    }
    return null;
  }
  
  private Component getComponent()
  {
    if (this._component == null) {
      synchronized (this)
      {
        if (this._component == null) {
          this._component = new Component() {};
        }
      }
    }
    return this._component;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\cache\CacheUtilities.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */