package com.sun.javaws.security;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.util.jar.Manifest;

public abstract class Resource
{
  public abstract String getName();
  
  public abstract URL getURL();
  
  public abstract URL getCodeSourceURL();
  
  public abstract InputStream getInputStream()
    throws IOException;
  
  public abstract int getContentLength()
    throws IOException;
  
  public byte[] getBytes()
    throws IOException
  {
    InputStream localInputStream = getInputStream();
    int i = getContentLength();
    Object localObject1;
    try
    {
      if (i != -1)
      {
        localObject1 = new byte[i];
        while (i > 0)
        {
          j = localInputStream.read((byte[])localObject1, localObject1.length - i, i);
          if (j == -1) {
            throw new IOException("unexpected EOF");
          }
          i -= j;
        }
      }
      localObject1 = new byte['Ѐ'];
      int j = 0;
      byte[] arrayOfByte;
      while ((i = localInputStream.read((byte[])localObject1, j, localObject1.length - j)) != -1)
      {
        j += i;
        if (j >= localObject1.length)
        {
          arrayOfByte = new byte[j * 2];
          System.arraycopy(localObject1, 0, arrayOfByte, 0, j);
          localObject1 = arrayOfByte;
        }
      }
      if (j != localObject1.length)
      {
        arrayOfByte = new byte[j];
        System.arraycopy(localObject1, 0, arrayOfByte, 0, j);
        localObject1 = arrayOfByte;
      }
    }
    finally
    {
      localInputStream.close();
    }
    return (byte[])localObject1;
  }
  
  public Manifest getManifest()
    throws IOException
  {
    return null;
  }
  
  public Certificate[] getCertificates()
  {
    return null;
  }
  
  public CodeSigner[] getCodeSigners()
  {
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\security\Resource.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */