package com.sun.jnlp;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import sun.awt.image.URLImageSource;

class ImageCache
{
  private static Map images = null;
  
  static synchronized Image getImage(URL paramURL)
  {
    Image localImage = (Image)images.get(paramURL);
    if (localImage == null)
    {
      localImage = Toolkit.getDefaultToolkit().createImage(new URLImageSource(paramURL));
      images.put(paramURL, localImage);
    }
    return localImage;
  }
  
  public static void initialize()
  {
    images = new HashMap();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\jnlp\ImageCache.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */