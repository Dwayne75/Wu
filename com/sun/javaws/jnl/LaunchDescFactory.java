package com.sun.javaws.jnl;

import com.sun.deploy.resources.ResourceManager;
import com.sun.javaws.JavawsFactory;
import com.sun.javaws.cache.Cache;
import com.sun.javaws.exceptions.BadFieldException;
import com.sun.javaws.exceptions.JNLParseException;
import com.sun.javaws.exceptions.MissingFieldException;
import com.sun.javaws.net.HttpRequest;
import com.sun.javaws.net.HttpResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class LaunchDescFactory
{
  public static LaunchDesc buildDescriptor(byte[] paramArrayOfByte)
    throws IOException, BadFieldException, MissingFieldException, JNLParseException
  {
    return XMLFormat.parse(paramArrayOfByte);
  }
  
  public static LaunchDesc buildDescriptor(InputStream paramInputStream)
    throws IOException, BadFieldException, MissingFieldException, JNLParseException
  {
    return buildDescriptor(readBytes(paramInputStream, -1L));
  }
  
  public static LaunchDesc buildDescriptor(InputStream paramInputStream, long paramLong)
    throws IOException, BadFieldException, MissingFieldException, JNLParseException
  {
    return buildDescriptor(readBytes(paramInputStream, paramLong));
  }
  
  public static LaunchDesc buildDescriptor(File paramFile)
    throws IOException, BadFieldException, MissingFieldException, JNLParseException
  {
    return buildDescriptor(new FileInputStream(paramFile), paramFile.length());
  }
  
  public static LaunchDesc buildDescriptor(URL paramURL)
    throws IOException, BadFieldException, MissingFieldException, JNLParseException
  {
    File localFile = Cache.getCachedLaunchedFile(paramURL);
    if (localFile != null) {
      return buildDescriptor(localFile);
    }
    HttpRequest localHttpRequest = JavawsFactory.getHttpRequestImpl();
    HttpResponse localHttpResponse = localHttpRequest.doGetRequest(paramURL);
    BufferedInputStream localBufferedInputStream = localHttpResponse.getInputStream();
    int i = localHttpResponse.getContentLength();
    
    LaunchDesc localLaunchDesc = buildDescriptor(localBufferedInputStream, i);
    
    localBufferedInputStream.close();
    
    return localLaunchDesc;
  }
  
  public static LaunchDesc buildDescriptor(String paramString)
    throws IOException, BadFieldException, MissingFieldException, JNLParseException
  {
    FileInputStream localFileInputStream = null;
    int i = -1;
    try
    {
      URL localURL = new URL(paramString);
      
      return buildDescriptor(localURL);
    }
    catch (MalformedURLException localMalformedURLException)
    {
      if (localMalformedURLException.getMessage().indexOf("https") != -1) {
        throw new BadFieldException(ResourceManager.getString("launch.error.badfield.download.https"), "<jnlp>", "https");
      }
      localFileInputStream = new FileInputStream(paramString);
      long l = new File(paramString).length();
      if (l > 1048576L) {
        throw new IOException("File too large");
      }
      i = (int)l;
    }
    return buildDescriptor(localFileInputStream, i);
  }
  
  public static LaunchDesc buildInternalLaunchDesc(String paramString1, String paramString2, String paramString3)
  {
    return new LaunchDesc("0.1", null, null, null, null, 1, null, 5, null, null, null, null, paramString3 == null ? paramString1 : paramString3, paramString2, null);
  }
  
  public static byte[] readBytes(InputStream paramInputStream, long paramLong)
    throws IOException
  {
    if (paramLong > 1048576L) {
      throw new IOException("File too large");
    }
    BufferedInputStream localBufferedInputStream = null;
    if ((paramInputStream instanceof BufferedInputStream)) {
      localBufferedInputStream = (BufferedInputStream)paramInputStream;
    } else {
      localBufferedInputStream = new BufferedInputStream(paramInputStream);
    }
    if (paramLong <= 0L) {
      paramLong = 10240L;
    }
    Object localObject = new byte[(int)paramLong];
    
    int j = 0;
    int i = localBufferedInputStream.read((byte[])localObject, j, localObject.length - j);
    byte[] arrayOfByte;
    while (i != -1)
    {
      j += i;
      if (localObject.length == j)
      {
        arrayOfByte = new byte[localObject.length * 2];
        System.arraycopy(localObject, 0, arrayOfByte, 0, localObject.length);
        localObject = arrayOfByte;
      }
      i = localBufferedInputStream.read((byte[])localObject, j, localObject.length - j);
    }
    localBufferedInputStream.close();
    paramInputStream.close();
    if (j != localObject.length)
    {
      arrayOfByte = new byte[j];
      System.arraycopy(localObject, 0, arrayOfByte, 0, j);
      localObject = arrayOfByte;
    }
    return (byte[])localObject;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\jnl\LaunchDescFactory.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */