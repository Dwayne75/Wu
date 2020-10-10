package com.sun.javaws.security;

import com.sun.deploy.util.Trace;
import com.sun.deploy.util.TraceLevel;
import com.sun.javaws.Globals;
import com.sun.javaws.cache.DownloadProtocol.DownloadDelegate;
import com.sun.javaws.exceptions.JARSigningException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class SigningInfo
{
  public static Certificate[] checkSigning(URL paramURL, String paramString, JarFile paramJarFile, DownloadProtocol.DownloadDelegate paramDownloadDelegate, File paramFile)
    throws JARSigningException
  {
    Object localObject1 = null;
    int i = 0;
    int j = 0;
    Object localObject2 = null;
    int k = paramJarFile.size();
    int m = 0;
    if (paramDownloadDelegate != null) {
      paramDownloadDelegate.validating(paramURL, 0, k);
    }
    BufferedOutputStream localBufferedOutputStream = null;
    InputStream localInputStream = null;
    Object localObject3;
    Object localObject4;
    Object localObject5;
    try
    {
      byte[] arrayOfByte = new byte[32768];
      localObject3 = paramJarFile.entries();
      while (((Enumeration)localObject3).hasMoreElements())
      {
        localObject4 = (JarEntry)((Enumeration)localObject3).nextElement();
        localObject5 = ((JarEntry)localObject4).getName();
        if ((!((String)localObject5).startsWith("META-INF/")) && (!((String)localObject5).endsWith("/")) && (((JarEntry)localObject4).getSize() != 0L))
        {
          localInputStream = paramJarFile.getInputStream((ZipEntry)localObject4);
          if ((paramFile != null) && (((String)localObject5).indexOf("/") == -1))
          {
            localObject6 = new File(paramFile, ((JarEntry)localObject4).getName());
            localBufferedOutputStream = new BufferedOutputStream(new FileOutputStream((File)localObject6));
          }
          int n;
          while ((n = localInputStream.read(arrayOfByte, 0, arrayOfByte.length)) != -1) {
            if (localBufferedOutputStream != null) {
              localBufferedOutputStream.write(arrayOfByte, 0, n);
            }
          }
          if (localBufferedOutputStream != null)
          {
            localBufferedOutputStream.close();localBufferedOutputStream = null;
          }
          localInputStream.close();localInputStream = null;
          
          Object localObject6 = ((JarEntry)localObject4).getCertificates();
          if ((localObject6 != null) && (localObject6.length == 0)) {
            localObject6 = null;
          }
          int i1 = 0;
          if (localObject6 != null)
          {
            i1 = 1;
            if (localObject1 == null) {
              localObject1 = localObject6;
            } else if (!equalChains((Certificate[])localObject1, (Certificate[])localObject6)) {
              throw new JARSigningException(paramURL, paramString, 1);
            }
          }
          i = (i != 0) || (i1 != 0) ? 1 : 0;
          j = (j != 0) || (i1 == 0) ? 1 : 0;
        }
        if (paramDownloadDelegate != null) {
          paramDownloadDelegate.validating(paramURL, ++m, k);
        }
      }
      try
      {
        if (localBufferedOutputStream != null) {
          localBufferedOutputStream.close();
        }
        if (localInputStream != null) {
          localInputStream.close();
        }
      }
      catch (IOException localIOException1)
      {
        Trace.ignoredException(localIOException1);
      }
      if (i == 0) {
        break label466;
      }
    }
    catch (SecurityException localSecurityException)
    {
      throw new JARSigningException(paramURL, paramString, 2, localSecurityException);
    }
    catch (IOException localIOException2)
    {
      throw new JARSigningException(paramURL, paramString, 2, localIOException2);
    }
    finally
    {
      try
      {
        if (localBufferedOutputStream != null) {
          localBufferedOutputStream.close();
        }
        if (localInputStream != null) {
          localInputStream.close();
        }
      }
      catch (IOException localIOException4)
      {
        Trace.ignoredException(localIOException4);
      }
    }
    if (j != 0) {
      throw new JARSigningException(paramURL, paramString, 3);
    }
    label466:
    if (localObject1 != null) {
      try
      {
        Manifest localManifest = paramJarFile.getManifest();
        localObject3 = localManifest.getEntries().entrySet();
        localObject4 = ((Set)localObject3).iterator();
        while (((Iterator)localObject4).hasNext())
        {
          localObject5 = (Map.Entry)((Iterator)localObject4).next();
          String str = (String)((Map.Entry)localObject5).getKey();
          if ((isSignedManifestEntry(localManifest, str)) && (paramJarFile.getEntry(str) == null)) {
            throw new JARSigningException(paramURL, paramString, 4, str);
          }
        }
      }
      catch (IOException localIOException3)
      {
        throw new JARSigningException(paramURL, paramString, 2, localIOException3);
      }
    }
    return (Certificate[])localObject1;
  }
  
  public static CodeSource getCodeSource(URL paramURL, JarFile paramJarFile)
  {
    Enumeration localEnumeration = paramJarFile.entries();
    byte[] arrayOfByte = new byte[32768];
    while (localEnumeration.hasMoreElements())
    {
      JarEntry localJarEntry = (JarEntry)localEnumeration.nextElement();
      String str = localJarEntry.getName();
      Trace.println(" ... name=" + str, TraceLevel.SECURITY);
      if ((!str.startsWith("META-INF/")) && (!str.endsWith("/")) && (localJarEntry.getSize() != 0L))
      {
        try
        {
          InputStream localInputStream = paramJarFile.getInputStream(localJarEntry);
          int i;
          while ((i = localInputStream.read(arrayOfByte, 0, arrayOfByte.length)) != -1) {}
          localInputStream.close();
        }
        catch (IOException localIOException)
        {
          Trace.ignoredException(localIOException);
        }
        if (Globals.isJavaVersionAtLeast15()) {
          return new CodeSource(paramURL, localJarEntry.getCodeSigners());
        }
        return new CodeSource(paramURL, localJarEntry.getCertificates());
      }
    }
    return null;
  }
  
  public static boolean equalChains(Certificate[] paramArrayOfCertificate1, Certificate[] paramArrayOfCertificate2)
  {
    if (paramArrayOfCertificate1.length != paramArrayOfCertificate2.length) {
      return false;
    }
    for (int i = 0; i < paramArrayOfCertificate1.length; i++) {
      if (!paramArrayOfCertificate1[i].equals(paramArrayOfCertificate2[i])) {
        return false;
      }
    }
    return true;
  }
  
  private static boolean isSignedManifestEntry(Manifest paramManifest, String paramString)
  {
    Attributes localAttributes = paramManifest.getAttributes(paramString);
    if (localAttributes != null)
    {
      Iterator localIterator = localAttributes.keySet().iterator();
      while (localIterator.hasNext())
      {
        String str = localIterator.next().toString();
        str = str.toUpperCase(Locale.ENGLISH);
        if ((str.endsWith("-DIGEST")) || (str.indexOf("-DIGEST-") != -1)) {
          return true;
        }
      }
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\security\SigningInfo.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */