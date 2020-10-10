package com.sun.javaws;

import com.sun.deploy.config.Config;
import com.sun.deploy.util.Trace;
import com.sun.deploy.util.TraceLevel;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.javaws.cache.CacheImageLoader;
import com.sun.javaws.cache.CacheImageLoaderCallback;
import com.sun.javaws.jnl.IconDesc;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

class SplashGenerator
  extends Thread
  implements CacheImageLoaderCallback
{
  private File _index;
  private File _dir;
  private final String _key;
  private final LaunchDesc _ld;
  private final Frame _owner;
  private Properties _props = new Properties();
  private boolean _useAppSplash = false;
  
  public SplashGenerator(Frame paramFrame, LaunchDesc paramLaunchDesc)
  {
    this._owner = paramFrame;
    this._ld = paramLaunchDesc;
    this._dir = new File(Config.getSplashDir());
    this._key = this._ld.getCanonicalHome().toString();
    
    String str = Config.getSplashIndex();
    this._index = new File(str);
    
    Config.setSplashCache();
    Config.storeIfDirty();
    if (this._index.exists()) {
      try
      {
        FileInputStream localFileInputStream = new FileInputStream(this._index);
        if (localFileInputStream != null)
        {
          this._props.load(localFileInputStream);
          localFileInputStream.close();
        }
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
      }
    }
  }
  
  public boolean needsCustomSplash()
  {
    return !this._props.containsKey(this._key);
  }
  
  public void remove()
  {
    addSplashToCacheIndex(this._key, null);
  }
  
  public void run()
  {
    InformationDesc localInformationDesc = this._ld.getInformation();
    IconDesc[] arrayOfIconDesc = localInformationDesc.getIcons();
    try
    {
      this._dir.mkdirs();
    }
    catch (Throwable localThrowable1)
    {
      splashError(localThrowable1);
    }
    try
    {
      this._index.createNewFile();
    }
    catch (Throwable localThrowable2)
    {
      splashError(localThrowable2);
    }
    IconDesc localIconDesc = localInformationDesc.getIconLocation(2, 4);
    
    this._useAppSplash = (localIconDesc != null);
    if (!this._useAppSplash) {
      localIconDesc = localInformationDesc.getIconLocation(2, 0);
    }
    if (localIconDesc == null) {
      try
      {
        create(null, null);
      }
      catch (Throwable localThrowable3)
      {
        splashError(localThrowable3);
      }
    } else {
      CacheImageLoader.getInstance().loadImage(localIconDesc, this);
    }
  }
  
  public void imageAvailable(IconDesc paramIconDesc, Image paramImage, File paramFile) {}
  
  public void finalImageAvailable(IconDesc paramIconDesc, Image paramImage, File paramFile)
  {
    if (!Globals.isHeadless()) {
      try
      {
        create(paramImage, paramFile);
      }
      catch (Throwable localThrowable)
      {
        splashError(localThrowable);
      }
    }
  }
  
  public void create(Image paramImage, File paramFile)
  {
    InformationDesc localInformationDesc = this._ld.getInformation();
    String str1 = localInformationDesc.getTitle();
    String str2 = localInformationDesc.getVendor();
    
    int n = 5;
    JPanel localJPanel = new JPanel();
    Dimension localDimension = Toolkit.getDefaultToolkit().getScreenSize();
    
    CompoundBorder localCompoundBorder = new CompoundBorder(BorderFactory.createLineBorder(Color.black), BorderFactory.createBevelBorder(0));
    
    Insets localInsets1 = localCompoundBorder.getBorderInsets(localJPanel);
    
    int k = 320;
    int m = 64 + 2 * n + localInsets1.top + localInsets1.bottom;
    int j;
    int i;
    if (paramImage == null)
    {
      i = j = 0;
    }
    else if (this._useAppSplash)
    {
      j = paramImage.getHeight(this._owner);
      i = paramImage.getWidth(this._owner);
      if (paramFile != null) {
        try
        {
          String str3 = paramFile.getCanonicalPath();
          if (str3.endsWith(".jpg"))
          {
            addSplashToCacheIndex(this._key, str3);
            return;
          }
        }
        catch (IOException localIOException1) {}
      }
      n = 0;
      k = Math.min(i, localDimension.width);
      m = Math.min(j, localDimension.height);
    }
    else
    {
      i = j = 64;
    }
    BufferedImage localBufferedImage = new BufferedImage(k, m, 5);
    Graphics2D localGraphics2D = localBufferedImage.createGraphics();
    Rectangle localRectangle1;
    if (this._useAppSplash)
    {
      localRectangle1 = new Rectangle(0, 0, k, m);
    }
    else
    {
      localGraphics2D.setColor(new Color(238, 238, 238));
      localGraphics2D.fillRect(0, 0, k, m);
      localGraphics2D.setColor(Color.black);
      localCompoundBorder.paintBorder(localJPanel, localGraphics2D, 0, 0, k, m);
      
      Rectangle localRectangle2 = new Rectangle(localInsets1.left, localInsets1.top, k - localInsets1.left - localInsets1.right, m - localInsets1.top - localInsets1.bottom);
      
      Border localBorder = BorderFactory.createLineBorder(Color.black);
      Insets localInsets2 = localBorder.getBorderInsets(localJPanel);
      localRectangle1 = new Rectangle(localInsets1.left + n, localInsets1.top + n, i, j);
      if (paramImage != null)
      {
        localBorder.paintBorder(localJPanel, localGraphics2D, localRectangle1.x - localInsets2.left, localRectangle1.y - localInsets2.top, localRectangle1.width + localInsets2.left + localInsets2.right, localRectangle1.height + localInsets2.top + localInsets2.bottom);
        
        localRectangle2.x += i + 2 * n;
        localRectangle2.width -= i + 2 * n;
      }
      Font localFont1 = new Font("SansSerif", 1, 20);
      Font localFont2 = new Font("SansSerif", 1, 16);
      localGraphics2D.setColor(Color.black);
      localGraphics2D.setFont(localFont1);
      Rectangle localRectangle3 = new Rectangle(localRectangle2.x, localRectangle2.y + 6, localRectangle2.width, localRectangle2.height - 12);
      
      localRectangle3.height /= 2;
      drawStringInRect(localGraphics2D, str1, localRectangle3, 1);
      localGraphics2D.setFont(localFont2);
      localRectangle3.y += localRectangle3.height;
      drawStringInRect(localGraphics2D, str2, localRectangle3, 1);
    }
    if (paramImage != null)
    {
      int i1 = 0;
      while (!localGraphics2D.drawImage(paramImage, localRectangle1.x, localRectangle1.y, localRectangle1.width, localRectangle1.height, this._owner))
      {
        try
        {
          Thread.sleep(2000L);
        }
        catch (Exception localException) {}
        i1++;
        if (i1 > 5) {
          Trace.println("couldnt draw splash image : " + paramImage, TraceLevel.BASIC);
        }
      }
    }
    try
    {
      File localFile = File.createTempFile("splash", ".jpg", this._dir);
      writeImage(localFile, localBufferedImage);
      addSplashToCacheIndex(this._key, localFile.getCanonicalPath());
    }
    catch (IOException localIOException2)
    {
      splashError(localIOException2);
    }
  }
  
  private void drawStringInRect(Graphics2D paramGraphics2D, String paramString, Rectangle paramRectangle, int paramInt)
  {
    FontMetrics localFontMetrics = paramGraphics2D.getFontMetrics();
    Rectangle2D localRectangle2D = localFontMetrics.getStringBounds(paramString, paramGraphics2D);
    int i = localFontMetrics.getMaxAscent();
    
    int m = (int)localRectangle2D.getWidth();
    int n = (int)localRectangle2D.getHeight();
    int j;
    if (m > paramRectangle.width)
    {
      j = paramRectangle.x;
      String str = paramString.substring(0, paramString.length() - 3);
      int i1 = str.length();
      while ((i1 > 3) && (localFontMetrics.stringWidth(str + "...") > paramRectangle.width))
      {
        i1--;
        str = str.substring(0, i1);
      }
      paramString = str + "...";
    }
    else
    {
      switch (paramInt)
      {
      case 0: 
      default: 
        j = paramRectangle.x;
        break;
      case 1: 
        j = paramRectangle.x + (paramRectangle.width - m) / 2;
        break;
      case 2: 
        j = paramRectangle.x + (paramRectangle.width - m - 1);
      }
    }
    if (j < paramRectangle.x) {
      j = paramRectangle.x;
    }
    int k = paramRectangle.y + i + (paramRectangle.height - n) / 2;
    paramGraphics2D.drawString(paramString, j, k);
  }
  
  private void addSplashToCacheIndex(String paramString1, String paramString2)
  {
    if (paramString2 != null) {
      this._props.setProperty(paramString1, paramString2);
    } else if (this._props.containsKey(paramString1)) {
      this._props.remove(paramString1);
    }
    File[] arrayOfFile = this._dir.listFiles();
    if (arrayOfFile == null) {
      return;
    }
    for (int i = 0; i < arrayOfFile.length; i++) {
      if (!arrayOfFile[i].equals(this._index)) {
        try
        {
          String str = arrayOfFile[i].getCanonicalPath();
          if (!this._props.containsValue(str)) {
            arrayOfFile[i].delete();
          }
        }
        catch (IOException localIOException2)
        {
          splashError(localIOException2);
        }
      }
    }
    try
    {
      FileOutputStream localFileOutputStream = new FileOutputStream(this._index);
      this._props.store(localFileOutputStream, "");
      localFileOutputStream.flush();
      localFileOutputStream.close();
    }
    catch (IOException localIOException1)
    {
      splashError(localIOException1);
    }
  }
  
  private void writeImage(File paramFile, BufferedImage paramBufferedImage)
  {
    try
    {
      FileOutputStream localFileOutputStream = new FileOutputStream(paramFile);
      JPEGImageEncoder localJPEGImageEncoder = JPEGCodec.createJPEGEncoder(localFileOutputStream);
      localJPEGImageEncoder.encode(paramBufferedImage);
    }
    catch (Throwable localThrowable)
    {
      splashError(localThrowable);
    }
  }
  
  private void splashError(Throwable paramThrowable)
  {
    LaunchErrorDialog.show(this._owner, paramThrowable, false);
    throw new Error(paramThrowable.toString());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\SplashGenerator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */