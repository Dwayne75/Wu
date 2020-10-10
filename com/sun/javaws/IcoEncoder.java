package com.sun.javaws;

import com.sun.deploy.config.Config;
import com.sun.deploy.util.Trace;
import com.sun.deploy.util.TraceLevel;
import com.sun.javaws.cache.Cache;
import com.sun.javaws.cache.CacheUtilities;
import com.sun.javaws.cache.DiskCacheEntry;
import com.sun.javaws.cache.DownloadProtocol;
import com.sun.javaws.exceptions.JNLPException;
import com.sun.javaws.jnl.IconDesc;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

public class IcoEncoder
{
  private static final int IMAGE_TYPE = 1;
  private static final int IMAGE_KIND = 0;
  private OutputStream outputS;
  private static byte ICON_SIZE = 32;
  private static byte NUM_COLORS = 0;
  private static int BYTE_SIZE = 8;
  private Image awtImage;
  
  private IcoEncoder(OutputStream paramOutputStream, Image paramImage)
  {
    this.outputS = paramOutputStream;
    this.awtImage = paramImage;
  }
  
  public static String getIconPath(LaunchDesc paramLaunchDesc)
  {
    IconDesc localIconDesc = paramLaunchDesc.getInformation().getIconLocation(1, 0);
    if (localIconDesc != null) {
      return getIconPath(localIconDesc.getLocation(), localIconDesc.getVersion());
    }
    return null;
  }
  
  public static String getIconPath(URL paramURL, String paramString)
  {
    Trace.println("Getting icon path", TraceLevel.BASIC);
    
    File localFile1 = null;
    try
    {
      DiskCacheEntry localDiskCacheEntry = DownloadProtocol.getResource(paramURL, paramString, 2, true, null);
      
      File localFile2 = localDiskCacheEntry.getMappedBitmap();
      if ((localFile2 == null) || (!localFile2.exists()))
      {
        localFile2 = null;
        
        Image localImage = CacheUtilities.getSharedInstance().loadImage(localDiskCacheEntry.getFile().getPath());
        
        localFile1 = saveICOfile(localImage);
        
        Trace.println("updating ICO: " + localFile1, TraceLevel.BASIC);
        if (localFile1 != null)
        {
          localFile2 = Cache.putMappedImage(paramURL, paramString, localFile1);
          localFile1 = null;
        }
      }
      if (localFile2 != null) {
        return localFile2.getPath();
      }
    }
    catch (IOException localIOException)
    {
      Trace.println("exception creating BMP: " + localIOException, TraceLevel.BASIC);
    }
    catch (JNLPException localJNLPException)
    {
      Trace.println("exception creating BMP: " + localJNLPException, TraceLevel.BASIC);
    }
    if (localFile1 != null) {
      localFile1.delete();
    }
    return null;
  }
  
  private static File saveICOfile(Image paramImage)
  {
    FileOutputStream localFileOutputStream = null;
    File localFile1 = null;
    File localFile2 = new File(Config.getJavawsCacheDir());
    try
    {
      localFile1 = File.createTempFile("javaws", ".ico", localFile2);
      localFileOutputStream = new FileOutputStream(localFile1);
      
      IcoEncoder localIcoEncoder = new IcoEncoder(localFileOutputStream, paramImage);
      
      localIcoEncoder.encode();
      localFileOutputStream.close();
      return localFile1;
    }
    catch (Throwable localThrowable)
    {
      if (localFileOutputStream != null) {
        try
        {
          localFileOutputStream.close();
        }
        catch (IOException localIOException) {}
      }
      if (localFile1 != null) {
        localFile1.delete();
      }
    }
    return null;
  }
  
  private void createBitmap()
    throws IOException
  {
    int i = 32;
    int j = 32;
    int k = 0;
    
    int[] arrayOfInt = new int[i * j];
    
    byte[] arrayOfByte1 = new byte[i * j * 3];
    byte[] arrayOfByte2 = new byte[i * j * 3];
    
    byte[] arrayOfByte3 = new byte[i * 4];
    byte[] arrayOfByte4 = new byte[i * 4];
    
    PixelGrabber localPixelGrabber = new PixelGrabber(this.awtImage.getScaledInstance(32, 32, 1), 0, 0, i, j, arrayOfInt, 0, i);
    try
    {
      if (localPixelGrabber.grabPixels()) {
        Trace.println("pixels grabbed successfully", TraceLevel.BASIC);
      } else {
        Trace.println("cannot grab pixels!", TraceLevel.BASIC);
      }
    }
    catch (InterruptedException localInterruptedException)
    {
      localInterruptedException.printStackTrace();
    }
    int m = 0;
    int n = 0;
    int i1 = 0;
    int i2 = 0;
    int i5;
    int i7;
    for (int i3 = 0; i3 < i * j; i3++)
    {
      i4 = arrayOfInt[i3] >> 24 & 0xFF;
      i5 = arrayOfInt[i3] >> 16 & 0xFF;
      i6 = arrayOfInt[i3] >> 8 & 0xFF;
      i7 = arrayOfInt[i3] & 0xFF;
      if (i4 != 0) {
        Trace.print(" 1", TraceLevel.BASIC);
      } else {
        Trace.print(" " + i4, TraceLevel.BASIC);
      }
      i2++;
      if (i2 == 32)
      {
        Trace.println(" ", TraceLevel.BASIC);
        i2 = 0;
      }
      if (i4 == 0) {
        m = (byte)(m | 128 >> i1);
      }
      i1++;
      if (i1 == 8)
      {
        arrayOfByte3[(n++)] = m;
        m = 0;
        i1 = 0;
      }
      arrayOfByte1[(k++)] = ((byte)i7);
      arrayOfByte1[(k++)] = ((byte)i6);
      arrayOfByte1[(k++)] = ((byte)i5);
    }
    i3 = 0;
    
    Trace.println("andPxiels bitmap", TraceLevel.BASIC);
    for (int i4 = 0; i4 < 128; i4++)
    {
      for (i5 = 0; i5 < 8; i5 = (byte)(i5 + 1)) {
        if ((arrayOfByte3[i4] & 128 >> i5) != 0) {
          Trace.print(" 1", TraceLevel.BASIC);
        } else {
          Trace.print(" 0", TraceLevel.BASIC);
        }
      }
      i3++;
      if (i3 == 4)
      {
        Trace.println(" ", TraceLevel.BASIC);
        i3 = 0;
      }
    }
    for (int i6 = 0; i6 < j; i6++)
    {
      i5 = i6 * i * 3;
      i4 = (j - i6 - 1) * i * 3;
      for (i7 = 0; i7 < i * 3; i7++) {
        arrayOfByte2[(i5 + i7)] = arrayOfByte1[(i4 + i7)];
      }
      i5 = i6 * (i / 8);
      i4 = (j - i6 - 1) * (i / 8);
      for (i7 = 0; i7 < i / 8; i7++) {
        arrayOfByte4[(i5 + i7)] = arrayOfByte3[(i4 + i7)];
      }
    }
    this.outputS.write(arrayOfByte2);
    
    this.outputS.write(arrayOfByte4);
  }
  
  public void encode()
  {
    writeIcoHeader();
    
    writeIconDirEntry();
    try
    {
      writeInfoHeader(40, 24);
      
      createBitmap();
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
  }
  
  private void writeInfoHeader(int paramInt1, int paramInt2)
    throws IOException
  {
    writeDWord(paramInt1);
    
    writeDWord(32);
    
    writeDWord(64);
    
    writeWord(1);
    
    writeWord(paramInt2);
    
    writeDWord(0);
    
    writeDWord(0);
    
    writeDWord(0);
    
    writeDWord(0);
    
    writeDWord(0);
    
    writeDWord(0);
  }
  
  private void writeIconDirEntry()
  {
    try
    {
      int i = ICON_SIZE;
      this.outputS.write(i);
      
      this.outputS.write(i);
      
      i = NUM_COLORS;
      this.outputS.write(i);
      
      i = 0;
      this.outputS.write(i);
      
      i = 1;
      writeWord(i);
      
      i = 24;
      writeWord(i);
      
      int j = 3240;
      writeDWord(j);
      
      int k = 22;
      writeDWord(k);
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
  }
  
  private void writeIcoHeader()
  {
    try
    {
      int i = 0;
      writeWord(i);
      
      i = 1;
      writeWord(i);
      
      writeWord(i);
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
  }
  
  public void writeWord(int paramInt)
    throws IOException
  {
    this.outputS.write(paramInt & 0xFF);
    this.outputS.write((paramInt & 0xFF00) >> 8);
  }
  
  public void writeDWord(int paramInt)
    throws IOException
  {
    this.outputS.write(paramInt & 0xFF);
    this.outputS.write((paramInt & 0xFF00) >> 8);
    this.outputS.write((paramInt & 0xFF0000) >> 16);
    this.outputS.write((paramInt & 0xFF000000) >> 24);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\IcoEncoder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */