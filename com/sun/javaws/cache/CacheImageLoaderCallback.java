package com.sun.javaws.cache;

import com.sun.javaws.jnl.IconDesc;
import java.awt.Image;
import java.io.File;

public abstract interface CacheImageLoaderCallback
{
  public abstract void imageAvailable(IconDesc paramIconDesc, Image paramImage, File paramFile);
  
  public abstract void finalImageAvailable(IconDesc paramIconDesc, Image paramImage, File paramFile);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\cache\CacheImageLoaderCallback.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */