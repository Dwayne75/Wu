package com.sun.javaws.cache;

import com.sun.deploy.util.Trace;
import com.sun.deploy.util.TraceLevel;
import com.sun.javaws.jnl.IconDesc;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.JARDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.LaunchDescFactory;
import com.sun.javaws.jnl.ResourcesDesc;
import com.sun.javaws.util.VersionID;
import com.sun.javaws.util.VersionString;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class DiskCache
{
  private static final int BUF_SIZE = 32768;
  static final char DIRECTORY_TYPE = 'D';
  static final char TEMP_TYPE = 'X';
  static final char VERSION_TYPE = 'V';
  static final char INDIRECT_TYPE = 'I';
  static final char RESOURCE_TYPE = 'R';
  static final char APPLICATION_TYPE = 'A';
  static final char EXTENSION_TYPE = 'E';
  static final char MUFFIN_TYPE = 'P';
  private File _baseDir;
  static final char MAIN_FILE_TAG = 'M';
  static final char NATIVELIB_FILE_TAG = 'N';
  static final char TIMESTAMP_FILE_TAG = 'T';
  static final char CERTIFICATE_FILE_TAG = 'C';
  static final char LAP_FILE_TAG = 'L';
  static final char MAPPED_IMAGE_FILE_TAG = 'B';
  static final char MUFFIN_ATTR_FILE_TAG = 'U';
  static final int MUFFIN_TAG_INDEX = 0;
  static final int MUFFIN_MAXSIZE_INDEX = 1;
  private static final String LAST_ACCESS_FILE = "lastAccessed";
  private static final String ORPHAN_LIST_FILE = "orphans";
  private static final String BEGIN_CERT_MARK = "-----BEGIN CERTIFICATE-----";
  private static final String END_CERT_MARK = "-----END CERTIFICATE-----";
  
  public DiskCache(File paramFile)
  {
    this._baseDir = paramFile;
  }
  
  long getLastUpdate()
  {
    File localFile = new File(this._baseDir, "lastAccessed");
    return localFile.lastModified();
  }
  
  void recordLastUpdate()
  {
    File localFile = new File(this._baseDir, "lastAccessed");
    try
    {
      FileOutputStream localFileOutputStream = new FileOutputStream(localFile);
      localFileOutputStream.write(46);
      localFileOutputStream.close();
    }
    catch (IOException localIOException) {}
  }
  
  boolean canWrite()
  {
    boolean bool = this._baseDir.canWrite();
    if (!bool) {
      Trace.println("Cannot write to cache: " + this._baseDir.getAbsolutePath(), TraceLevel.BASIC);
    }
    return bool;
  }
  
  String getBaseDirForHost(URL paramURL)
  {
    try
    {
      URL localURL = new URL(paramURL.getProtocol(), paramURL.getHost(), paramURL.getPort(), "");
      String str = keyToFileLocation('R', 'M', localURL, null);
      int i = str.lastIndexOf(File.separator);
      return str.substring(0, i);
    }
    catch (MalformedURLException localMalformedURLException)
    {
      Trace.ignoredException(localMalformedURLException);
    }
    return null;
  }
  
  private void removeEmptyDirs(URL paramURL)
  {
    String str = getBaseDirForHost(paramURL);
    if (str != null) {
      removeEmptyDirs(new File(str));
    }
  }
  
  private void removeEmptyDirs(File paramFile)
  {
    if (paramFile.isDirectory())
    {
      File[] arrayOfFile = paramFile.listFiles();
      int i = 0;
      if (arrayOfFile != null) {
        for (int j = 0; j < arrayOfFile.length; j++)
        {
          removeEmptyDirs(arrayOfFile[j]);
          if (arrayOfFile[j].exists()) {
            i = 1;
          }
        }
      }
      if (i == 0) {
        try
        {
          paramFile.delete();
        }
        catch (Exception localException)
        {
          Trace.ignoredException(localException);
        }
      }
    }
  }
  
  private File getOrphanFileForHost(URL paramURL)
  {
    try
    {
      return new File(getBaseDirForHost(paramURL), "orphans");
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
    return null;
  }
  
  private void removeOrphans(URL paramURL)
  {
    File localFile1 = getOrphanFileForHost(paramURL);
    if ((localFile1 != null) && (localFile1.exists()))
    {
      BufferedReader localBufferedReader = null;
      PrintStream localPrintStream = null;
      int i = 0;
      ArrayList localArrayList = new ArrayList();
      try
      {
        FileInputStream localFileInputStream = new FileInputStream(localFile1);
        localBufferedReader = new BufferedReader(new InputStreamReader(localFileInputStream));
        String str;
        while ((str = localBufferedReader.readLine()) != null) {
          localArrayList.add(str);
        }
        for (int k = localArrayList.size() - 1; k >= 0; k--)
        {
          File localFile2 = new File((String)localArrayList.get(k));
          localFile2.delete();
          if (!localFile2.exists())
          {
            i = 1;
            localArrayList.remove(k);
          }
        }
        if (localBufferedReader != null) {
          try
          {
            localBufferedReader.close();
          }
          catch (IOException localIOException1)
          {
            Trace.ignoredException(localIOException1);
          }
        }
        if (i == 0) {
          return;
        }
      }
      catch (IOException localIOException2)
      {
        Trace.ignoredException(localIOException2);
      }
      finally
      {
        if (localBufferedReader != null) {
          try
          {
            localBufferedReader.close();
          }
          catch (IOException localIOException4)
          {
            Trace.ignoredException(localIOException4);
          }
        }
      }
      try
      {
        if (localArrayList.isEmpty())
        {
          Trace.println("emptying orphans file", TraceLevel.CACHE);
          localFile1.delete();
        }
        else
        {
          localPrintStream = new PrintStream(new FileOutputStream(localFile1));
          for (int j = 0; j < localArrayList.size(); j++)
          {
            Trace.println("Remaining orphan: " + localArrayList.get(j), TraceLevel.CACHE);
            localPrintStream.println((String)localArrayList.get(j));
          }
        }
      }
      catch (Exception localException)
      {
        Trace.ignoredException(localException);
      }
      finally
      {
        if (localPrintStream != null) {
          localPrintStream.close();
        }
      }
    }
  }
  
  private void addOrphan(URL paramURL, File paramFile)
  {
    Trace.println("addOrphan: " + paramFile, TraceLevel.CACHE);
    File localFile = getOrphanFileForHost(paramURL);
    PrintStream localPrintStream = null;
    if (localFile != null) {
      try
      {
        localPrintStream = new PrintStream(new FileOutputStream(localFile.getPath(), true));
        localPrintStream.println(paramFile.getCanonicalPath());
      }
      catch (Exception localException)
      {
        Trace.ignoredException(localException);
      }
      finally
      {
        if (localPrintStream != null) {
          localPrintStream.close();
        }
      }
    }
  }
  
  File getTempCacheFile(URL paramURL, String paramString)
    throws IOException
  {
    String str = keyToFileLocation('X', 'M', paramURL, paramString);
    
    File localFile1 = new File(str);
    File localFile2 = localFile1.getParentFile();
    localFile2.mkdirs();
    
    return File.createTempFile("java-" + localFile1.getName(), "tmp", localFile2);
  }
  
  File createNativeLibDir(URL paramURL, String paramString)
    throws IOException
  {
    File localFile = getFileFromCache('R', 'N', paramURL, paramString, false);
    
    localFile.mkdirs();
    return localFile;
  }
  
  File getNativeLibDir(URL paramURL, String paramString)
    throws IOException
  {
    File localFile = getFileFromCache('R', 'N', paramURL, paramString, false);
    
    return localFile;
  }
  
  void insertMuffinEntry(URL paramURL, File paramFile, int paramInt, long paramLong)
    throws IOException
  {
    File localFile1 = getFileFromCache('P', 'M', paramURL, null, false);
    if (localFile1.exists())
    {
      paramFile.delete();
      throw new IOException("insert failed in cache: target already exixts");
    }
    File localFile2 = localFile1.getParentFile();
    if (localFile2 != null) {
      localFile2.mkdirs();
    }
    if (!paramFile.renameTo(localFile1)) {
      throw new IOException("rename failed in cache");
    }
    putMuffinAttributes(paramURL, paramInt, paramLong);
  }
  
  long getMuffinSize(URL paramURL)
    throws IOException
  {
    long l = 0L;
    File localFile = getFileFromCache('P', 'M', paramURL, null, true);
    if ((localFile != null) && (localFile.exists())) {
      l += localFile.length();
    }
    return l;
  }
  
  void insertEntry(char paramChar, URL paramURL, String paramString, File paramFile, long paramLong)
    throws IOException
  {
    putTimeStamp(paramChar, paramURL, paramString, paramLong);
    
    putFileInCache(paramChar, 'M', paramURL, paramString, paramFile);
    
    recordLastUpdate();
  }
  
  File putMappedImage(URL paramURL, String paramString, File paramFile)
    throws IOException
  {
    if (paramFile.getPath().endsWith(".ico"))
    {
      localObject = paramURL.getFile();
      if (!((String)localObject).endsWith(".ico"))
      {
        localObject = (String)localObject + ".ico";
        paramURL = new URL(paramURL.getProtocol(), paramURL.getHost(), paramURL.getPort(), (String)localObject);
      }
    }
    Object localObject = putFileInCache('R', 'B', paramURL, paramString, paramFile);
    
    recordLastUpdate();
    return (File)localObject;
  }
  
  File getMappedImage(char paramChar1, char paramChar2, URL paramURL, String paramString, boolean paramBoolean)
    throws IOException
  {
    File localFile = getFileFromCache(paramChar1, paramChar2, paramURL, paramString, paramBoolean);
    if ((localFile == null) || (!localFile.exists()))
    {
      String str = paramURL.getFile();
      if (!str.endsWith(".ico"))
      {
        str = str + ".ico";
        paramURL = new URL(paramURL.getProtocol(), paramURL.getHost(), paramURL.getPort(), str);
        localFile = getFileFromCache(paramChar1, paramChar2, paramURL, paramString, paramBoolean);
      }
    }
    return localFile;
  }
  
  void putLaunchFile(char paramChar, URL paramURL, String paramString1, String paramString2)
    throws IOException
  {
    byte[] arrayOfByte = paramString2.getBytes("UTF8");
    storeAtomic(paramChar, 'M', paramURL, paramString1, arrayOfByte);
    putTimeStamp(paramChar, paramURL, paramString1, new Date().getTime());
  }
  
  String getLaunchFile(char paramChar, URL paramURL, String paramString1, String paramString2)
    throws IOException
  {
    byte[] arrayOfByte = getEntryContent(paramChar, 'M', paramURL, paramString1);
    if (arrayOfByte == null) {
      return null;
    }
    return new String(arrayOfByte, "UTF8");
  }
  
  void putMuffinAttributes(URL paramURL, int paramInt, long paramLong)
    throws IOException
  {
    PrintStream localPrintStream = new PrintStream(getOutputStream('P', 'U', paramURL, null));
    try
    {
      localPrintStream.println(paramInt);
      localPrintStream.println(paramLong);
    }
    finally
    {
      if (localPrintStream != null) {
        localPrintStream.close();
      }
    }
  }
  
  void putTimeStamp(char paramChar, URL paramURL, String paramString, long paramLong)
    throws IOException
  {
    if (paramLong == 0L) {
      paramLong = new Date().getTime();
    }
    PrintStream localPrintStream = new PrintStream(getOutputStream(paramChar, 'T', paramURL, paramString));
    try
    {
      localPrintStream.println(paramLong);
      localPrintStream.println("# " + new Date(paramLong));
    }
    finally
    {
      localPrintStream.close();
    }
  }
  
  long[] getMuffinAttributes(URL paramURL)
    throws IOException
  {
    BufferedReader localBufferedReader = null;
    long l1 = -1L;
    long l2 = -1L;
    try
    {
      InputStream localInputStream = getInputStream('P', 'U', paramURL, null);
      localBufferedReader = new BufferedReader(new InputStreamReader(localInputStream));
      String str = localBufferedReader.readLine();
      try
      {
        l1 = Integer.parseInt(str);
      }
      catch (NumberFormatException localNumberFormatException1)
      {
        throw new IOException(localNumberFormatException1.getMessage());
      }
      str = localBufferedReader.readLine();
      try
      {
        l2 = Long.parseLong(str);
      }
      catch (NumberFormatException localNumberFormatException2)
      {
        throw new IOException(localNumberFormatException2.getMessage());
      }
    }
    finally
    {
      if (localBufferedReader != null) {
        localBufferedReader.close();
      }
    }
    return new long[] { l1, l2 };
  }
  
  /* Error */
  long getTimeStamp(char paramChar, URL paramURL, String paramString)
  {
    // Byte code:
    //   0: aconst_null
    //   1: astore 4
    //   3: aload_0
    //   4: iload_1
    //   5: bipush 84
    //   7: aload_2
    //   8: aload_3
    //   9: invokespecial 582	com/sun/javaws/cache/DiskCache:getInputStream	(CCLjava/net/URL;Ljava/lang/String;)Ljava/io/InputStream;
    //   12: astore 5
    //   14: new 305	java/io/BufferedReader
    //   17: dup
    //   18: new 313	java/io/InputStreamReader
    //   21: dup
    //   22: aload 5
    //   24: invokespecial 644	java/io/InputStreamReader:<init>	(Ljava/io/InputStream;)V
    //   27: invokespecial 610	java/io/BufferedReader:<init>	(Ljava/io/Reader;)V
    //   30: astore 4
    //   32: aload 4
    //   34: invokevirtual 611	java/io/BufferedReader:readLine	()Ljava/lang/String;
    //   37: astore 6
    //   39: aload 6
    //   41: invokestatic 656	java/lang/Long:parseLong	(Ljava/lang/String;)J
    //   44: lstore 7
    //   46: aload 4
    //   48: ifnull +8 -> 56
    //   51: aload 4
    //   53: invokevirtual 609	java/io/BufferedReader:close	()V
    //   56: goto +10 -> 66
    //   59: astore 9
    //   61: aload 9
    //   63: invokestatic 552	com/sun/deploy/util/Trace:ignoredException	(Ljava/lang/Exception;)V
    //   66: lload 7
    //   68: lreturn
    //   69: astore 7
    //   71: lconst_0
    //   72: lstore 8
    //   74: aload 4
    //   76: ifnull +8 -> 84
    //   79: aload 4
    //   81: invokevirtual 609	java/io/BufferedReader:close	()V
    //   84: goto +10 -> 94
    //   87: astore 10
    //   89: aload 10
    //   91: invokestatic 552	com/sun/deploy/util/Trace:ignoredException	(Ljava/lang/Exception;)V
    //   94: lload 8
    //   96: lreturn
    //   97: astore 5
    //   99: lconst_0
    //   100: lstore 6
    //   102: aload 4
    //   104: ifnull +8 -> 112
    //   107: aload 4
    //   109: invokevirtual 609	java/io/BufferedReader:close	()V
    //   112: goto +10 -> 122
    //   115: astore 8
    //   117: aload 8
    //   119: invokestatic 552	com/sun/deploy/util/Trace:ignoredException	(Ljava/lang/Exception;)V
    //   122: lload 6
    //   124: lreturn
    //   125: astore 11
    //   127: aload 4
    //   129: ifnull +8 -> 137
    //   132: aload 4
    //   134: invokevirtual 609	java/io/BufferedReader:close	()V
    //   137: goto +10 -> 147
    //   140: astore 12
    //   142: aload 12
    //   144: invokestatic 552	com/sun/deploy/util/Trace:ignoredException	(Ljava/lang/Exception;)V
    //   147: aload 11
    //   149: athrow
    // Line number table:
    //   Java source line #436	-> byte code offset #0
    //   Java source line #438	-> byte code offset #3
    //   Java source line #439	-> byte code offset #14
    //   Java source line #440	-> byte code offset #32
    //   Java source line #442	-> byte code offset #39
    //   Java source line #450	-> byte code offset #46
    //   Java source line #451	-> byte code offset #51
    //   Java source line #455	-> byte code offset #56
    //   Java source line #453	-> byte code offset #59
    //   Java source line #454	-> byte code offset #61
    //   Java source line #456	-> byte code offset #66
    //   Java source line #443	-> byte code offset #69
    //   Java source line #444	-> byte code offset #71
    //   Java source line #450	-> byte code offset #74
    //   Java source line #451	-> byte code offset #79
    //   Java source line #455	-> byte code offset #84
    //   Java source line #453	-> byte code offset #87
    //   Java source line #454	-> byte code offset #89
    //   Java source line #456	-> byte code offset #94
    //   Java source line #446	-> byte code offset #97
    //   Java source line #447	-> byte code offset #99
    //   Java source line #450	-> byte code offset #102
    //   Java source line #451	-> byte code offset #107
    //   Java source line #455	-> byte code offset #112
    //   Java source line #453	-> byte code offset #115
    //   Java source line #454	-> byte code offset #117
    //   Java source line #456	-> byte code offset #122
    //   Java source line #449	-> byte code offset #125
    //   Java source line #450	-> byte code offset #127
    //   Java source line #451	-> byte code offset #132
    //   Java source line #455	-> byte code offset #137
    //   Java source line #453	-> byte code offset #140
    //   Java source line #454	-> byte code offset #142
    //   Java source line #456	-> byte code offset #147
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	150	0	this	DiskCache
    //   0	150	1	paramChar	char
    //   0	150	2	paramURL	URL
    //   0	150	3	paramString	String
    //   1	132	4	localBufferedReader	BufferedReader
    //   12	11	5	localInputStream	InputStream
    //   97	1	5	localIOException1	IOException
    //   37	3	6	str	String
    //   100	23	6	l1	long
    //   44	23	7	l2	long
    //   69	1	7	localNumberFormatException	NumberFormatException
    //   72	23	8	l3	long
    //   115	3	8	localIOException2	IOException
    //   59	3	9	localIOException3	IOException
    //   87	3	10	localIOException4	IOException
    //   125	23	11	localObject	Object
    //   140	3	12	localIOException5	IOException
    // Exception table:
    //   from	to	target	type
    //   46	56	59	java/io/IOException
    //   39	46	69	java/lang/NumberFormatException
    //   74	84	87	java/io/IOException
    //   3	46	97	java/io/IOException
    //   69	74	97	java/io/IOException
    //   102	112	115	java/io/IOException
    //   3	46	125	finally
    //   69	74	125	finally
    //   97	102	125	finally
    //   125	127	125	finally
    //   127	137	140	java/io/IOException
  }
  
  DiskCacheEntry getMuffinEntry(char paramChar, URL paramURL)
    throws IOException
  {
    File localFile1 = getFileFromCache(paramChar, 'M', paramURL, null, true);
    if (localFile1 == null) {
      return null;
    }
    File localFile2 = getFileFromCache(paramChar, 'U', paramURL, null, true);
    return new DiskCacheEntry(paramChar, paramURL, null, localFile1, -1L, null, null, localFile2);
  }
  
  DiskCacheEntry getCacheEntry(char paramChar, URL paramURL, String paramString)
    throws IOException
  {
    File localFile1 = getFileFromCache(paramChar, 'M', paramURL, paramString, true);
    if (localFile1 == null) {
      return null;
    }
    File localFile2 = getFileFromCache(paramChar, 'N', paramURL, paramString, true);
    
    File localFile3 = getMappedImage(paramChar, 'B', paramURL, paramString, true);
    
    long l = getTimeStamp(paramChar, paramURL, paramString);
    
    DiskCacheEntry localDiskCacheEntry = new DiskCacheEntry(paramChar, paramURL, paramString, localFile1, l, localFile2, localFile3, null);
    
    return localDiskCacheEntry;
  }
  
  DiskCacheEntry[] getCacheEntries(char paramChar, URL paramURL, String paramString, boolean paramBoolean)
    throws IOException
  {
    if (paramString == null)
    {
      localObject1 = getCacheEntry(paramChar, paramURL, null);
      if (localObject1 == null) {
        return new DiskCacheEntry[0];
      }
      return new DiskCacheEntry[] { localObject1 };
    }
    Object localObject1 = getCacheEntries(paramChar, paramURL);
    
    VersionString localVersionString = new VersionString(paramString);
    
    Object localObject2 = null;
    Iterator localIterator = ((ArrayList)localObject1).iterator();
    while (localIterator.hasNext())
    {
      localObject3 = (DiskCacheEntry)localIterator.next();
      String str = ((DiskCacheEntry)localObject3).getVersionId();
      if (str == null)
      {
        localIterator.remove();
      }
      else if (!localVersionString.contains(str))
      {
        if ((localObject2 == null) && (localVersionString.containsGreaterThan(str))) {
          localObject2 = localObject3;
        }
        localIterator.remove();
      }
    }
    if ((!paramBoolean) && (((ArrayList)localObject1).size() == 0) && (localObject2 != null)) {
      ((ArrayList)localObject1).add(localObject2);
    }
    Object localObject3 = new DiskCacheEntry[((ArrayList)localObject1).size()];
    return (DiskCacheEntry[])((ArrayList)localObject1).toArray((Object[])localObject3);
  }
  
  void removeMuffinEntry(DiskCacheEntry paramDiskCacheEntry)
  {
    char c = paramDiskCacheEntry.getType();
    URL localURL = paramDiskCacheEntry.getLocation();
    String str = paramDiskCacheEntry.getVersionId();
    deleteEntry(c, 'M', localURL, str);
    deleteEntry(c, 'U', localURL, str);
  }
  
  void removeEntry(DiskCacheEntry paramDiskCacheEntry)
  {
    char c = paramDiskCacheEntry.getType();
    URL localURL = paramDiskCacheEntry.getLocation();
    
    removeOrphans(localURL);
    String str = paramDiskCacheEntry.getVersionId();
    deleteEntry(c, 'M', localURL, str);
    deleteEntry(c, 'T', localURL, str);
    deleteEntry(c, 'C', localURL, str);
    deleteEntry(c, 'N', localURL, str);
    deleteEntry(c, 'B', localURL, str);
    deleteEntry(c, 'L', localURL, str);
    if (c == 'R') {
      deleteEntry('I', 'M', localURL, str);
    }
    removeEmptyDirs(localURL);
    recordLastUpdate();
  }
  
  private void deleteEntry(char paramChar1, char paramChar2, URL paramURL, String paramString)
  {
    File localFile = null;
    try
    {
      if (paramChar2 == 'B') {
        localFile = getMappedImage(paramChar1, paramChar2, paramURL, paramString, false);
      } else {
        localFile = getFileFromCache(paramChar1, paramChar2, paramURL, paramString, false);
      }
      deleteFile(localFile);
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
    }
    if ((localFile != null) && (localFile.exists())) {
      if ((paramChar1 == 'R') && (paramChar2 == 'M')) {
        addOrphan(paramURL, localFile);
      }
    }
  }
  
  DiskCacheEntry getCacheEntryFromFile(File paramFile)
  {
    DiskCacheEntry localDiskCacheEntry = fileToEntry(paramFile);
    if (localDiskCacheEntry != null) {
      try
      {
        if (localDiskCacheEntry.getType() == 'P') {
          return getMuffinEntry(localDiskCacheEntry.getType(), localDiskCacheEntry.getLocation());
        }
        return getCacheEntry(localDiskCacheEntry.getType(), localDiskCacheEntry.getLocation(), localDiskCacheEntry.getVersionId());
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
      }
    }
    return localDiskCacheEntry;
  }
  
  boolean isMainMuffinFile(File paramFile)
    throws IOException
  {
    DiskCacheEntry localDiskCacheEntry = fileToEntry(paramFile);
    return paramFile.equals(getFileFromCache('P', 'M', localDiskCacheEntry.getLocation(), null, false));
  }
  
  private ArrayList getCacheEntries(char paramChar, URL paramURL)
    throws IOException
  {
    ArrayList localArrayList = new ArrayList();
    
    String str1 = keyToFileLocation(paramChar, 'M', paramURL, "MATCH");
    
    int i = str1.indexOf(File.separator + 'V' + "MATCH" + File.separator);
    if (i == -1) {
      throw new IllegalStateException("the javaws cache is corrupted");
    }
    String str2 = str1.substring(0, i);
    File localFile1 = new File(str2);
    File[] arrayOfFile = localFile1.listFiles();
    if (arrayOfFile == null) {
      return localArrayList;
    }
    for (int j = 0; j < arrayOfFile.length; j++)
    {
      localObject = arrayOfFile[j].getName();
      if ((arrayOfFile[j].isDirectory()) && (((String)localObject).length() > 1) && (((String)localObject).charAt(0) == 'V'))
      {
        String str3 = ((String)localObject).substring(1);
        File localFile2 = getFileFromCache(paramChar, 'M', paramURL, str3, true);
        if (localFile2 != null)
        {
          DiskCacheEntry localDiskCacheEntry2 = getCacheEntry(paramChar, paramURL, str3);
          localArrayList.add(localDiskCacheEntry2);
        }
      }
    }
    j = localArrayList.size();
    if (j > 1)
    {
      localObject = new DiskCacheEntry[j];
      localObject = (DiskCacheEntry[])localArrayList.toArray((Object[])localObject);
      
      Arrays.sort((Object[])localObject, new Comparator()
      {
        public int compare(Object paramAnonymousObject1, Object paramAnonymousObject2)
        {
          DiskCacheEntry localDiskCacheEntry1 = (DiskCacheEntry)paramAnonymousObject1;
          DiskCacheEntry localDiskCacheEntry2 = (DiskCacheEntry)paramAnonymousObject2;
          VersionID localVersionID1 = new VersionID(localDiskCacheEntry1.getVersionId());
          VersionID localVersionID2 = new VersionID(localDiskCacheEntry2.getVersionId());
          return localVersionID1.isGreaterThan(localVersionID2) ? -1 : 1;
        }
      });
      for (int k = 0; k < j; k++) {
        localArrayList.set(k, localObject[k]);
      }
    }
    Object localObject = getFileFromCache(paramChar, 'M', paramURL, null, true);
    if (localObject != null)
    {
      DiskCacheEntry localDiskCacheEntry1 = getCacheEntry(paramChar, paramURL, null);
      localArrayList.add(localDiskCacheEntry1);
    }
    return localArrayList;
  }
  
  public String[] getCacheVersions(char paramChar, URL paramURL)
    throws IOException
  {
    ArrayList localArrayList = new ArrayList();
    
    String str1 = keyToFileLocation(paramChar, 'M', paramURL, "MATCH");
    
    int i = str1.indexOf(File.separator + 'V' + "MATCH" + File.separator);
    if (i == -1) {
      throw new IllegalStateException("the javaws cache is corrupted");
    }
    String str2 = str1.substring(0, i);
    File localFile1 = new File(str2);
    File[] arrayOfFile = localFile1.listFiles();
    if (arrayOfFile != null) {
      for (int j = 0; j < arrayOfFile.length; j++)
      {
        String str3 = arrayOfFile[j].getName();
        if ((arrayOfFile[j].isDirectory()) && (str3.length() > 1) && (str3.charAt(0) == 'V'))
        {
          String str4 = str3.substring(1);
          File localFile2 = getFileFromCache(paramChar, 'M', paramURL, str4, true);
          if (localFile2 != null) {
            localArrayList.add(str4);
          }
        }
      }
    }
    return (String[])localArrayList.toArray(new String[0]);
  }
  
  void visitDiskCache(char paramChar, DiskCacheVisitor paramDiskCacheVisitor)
    throws IOException
  {
    visitDiskCacheHelper(this._baseDir, 0, paramChar, paramDiskCacheVisitor);
  }
  
  private void visitDiskCacheHelper(File paramFile, int paramInt, char paramChar, DiskCacheVisitor paramDiskCacheVisitor)
    throws IOException
  {
    String str = paramFile.getName();
    int i;
    if ((paramFile.isDirectory()) && ((str.length() <= 2) || (paramFile.getName().charAt(1) != 'N')))
    {
      File[] arrayOfFile = paramFile.listFiles();
      for (i = 0; i < arrayOfFile.length; i++) {
        visitDiskCacheHelper(arrayOfFile[i], paramInt + 1, paramChar, paramDiskCacheVisitor);
      }
    }
    else if ((str.length() > 2) && (paramInt > 3))
    {
      char c = str.charAt(0);
      i = str.charAt(1);
      if ((c == paramChar) && (i == 77))
      {
        DiskCacheEntry localDiskCacheEntry = getCacheEntryFromFile(paramFile);
        if (localDiskCacheEntry == null) {
          throw new IllegalStateException("the javaws cache is corrupted");
        }
        paramDiskCacheVisitor.visitEntry(localDiskCacheEntry);
      }
    }
  }
  
  private static class MuffinAccessVisitor
    implements DiskCache.DiskCacheVisitor
  {
    private DiskCache _diskCache;
    private URL _theURL;
    private URL[] _urls = new URL['ÿ'];
    private int _counter = 0;
    
    MuffinAccessVisitor(DiskCache paramDiskCache, URL paramURL)
    {
      this._diskCache = paramDiskCache;
      this._theURL = paramURL;
    }
    
    public void visitEntry(DiskCacheEntry paramDiskCacheEntry)
    {
      URL localURL = paramDiskCacheEntry.getLocation();
      if (localURL == null) {
        return;
      }
      if (localURL.getHost().equals(this._theURL.getHost())) {
        this._urls[(this._counter++)] = localURL;
      }
    }
    
    public URL[] getAccessibleMuffins()
    {
      return this._urls;
    }
  }
  
  File getMuffinFileForURL(URL paramURL)
  {
    try
    {
      return getFileFromCache('P', 'M', paramURL, null, false);
    }
    catch (IOException localIOException) {}
    return null;
  }
  
  URL[] getAccessibleMuffins(URL paramURL)
    throws IOException
  {
    MuffinAccessVisitor localMuffinAccessVisitor = new MuffinAccessVisitor(this, paramURL);
    visitDiskCache('P', localMuffinAccessVisitor);
    return localMuffinAccessVisitor.getAccessibleMuffins();
  }
  
  private static class DeleteVisitor
    implements DiskCache.DiskCacheVisitor
  {
    private DiskCache _diskCache;
    
    DeleteVisitor(DiskCache paramDiskCache)
    {
      this._diskCache = paramDiskCache;
    }
    
    public void visitEntry(DiskCacheEntry paramDiskCacheEntry)
    {
      this._diskCache.removeEntry(paramDiskCacheEntry);
    }
  }
  
  private static class SizeVisitor
    implements DiskCache.DiskCacheVisitor
  {
    private DiskCache _diskCache;
    long _size = 0L;
    
    SizeVisitor(DiskCache paramDiskCache)
    {
      this._diskCache = paramDiskCache;
      this._size = 0L;
    }
    
    public void visitEntry(DiskCacheEntry paramDiskCacheEntry)
    {
      if ((paramDiskCacheEntry.getDirectory() != null) && (paramDiskCacheEntry.getDirectory().exists()))
      {
        File[] arrayOfFile = paramDiskCacheEntry.getDirectory().listFiles();
        for (int i = 0; i < arrayOfFile.length; i++) {
          this._size += arrayOfFile[i].length();
        }
      }
      else
      {
        this._size += paramDiskCacheEntry.getFile().length();
      }
    }
    
    public long getSize()
    {
      return this._size;
    }
  }
  
  long getCacheSize()
    throws IOException
  {
    Trace.println("Computing diskcache size: " + this._baseDir.getAbsoluteFile(), TraceLevel.CACHE);
    
    SizeVisitor localSizeVisitor = new SizeVisitor(this);
    visitDiskCache('R', localSizeVisitor);
    return localSizeVisitor.getSize();
  }
  
  void uninstallCache()
  {
    deleteFile(this._baseDir);
    if (this._baseDir.exists()) {
      recordLastUpdate();
    }
  }
  
  private void deleteFile(File paramFile)
  {
    if (paramFile.isDirectory())
    {
      File[] arrayOfFile = paramFile.listFiles();
      if (arrayOfFile != null) {
        for (int i = 0; i < arrayOfFile.length; i++) {
          deleteFile(arrayOfFile[i]);
        }
      }
    }
    paramFile.delete();
  }
  
  private OutputStream getOutputStream(char paramChar1, char paramChar2, URL paramURL, String paramString)
    throws IOException
  {
    File localFile = getFileFromCache(paramChar1, paramChar2, paramURL, paramString, false);
    
    localFile.getParentFile().mkdirs();
    localFile.createNewFile();
    
    recordLastUpdate();
    
    return new FileOutputStream(localFile);
  }
  
  private InputStream getInputStream(char paramChar1, char paramChar2, URL paramURL, String paramString)
    throws IOException
  {
    return new FileInputStream(getFileFromCache(paramChar1, paramChar2, paramURL, paramString, false));
  }
  
  byte[] getLapData(char paramChar, URL paramURL, String paramString)
    throws IOException
  {
    return getEntryContent(paramChar, 'L', paramURL, paramString);
  }
  
  void putLapData(char paramChar, URL paramURL, String paramString, byte[] paramArrayOfByte)
    throws IOException
  {
    storeAtomic(paramChar, 'L', paramURL, paramString, paramArrayOfByte);
  }
  
  private byte[] getEntryContent(char paramChar1, char paramChar2, URL paramURL, String paramString)
    throws IOException
  {
    File localFile = getFileFromCache(paramChar1, paramChar2, paramURL, paramString, true);
    if (localFile == null) {
      return null;
    }
    long l = localFile.length();
    if (l > 1073741824L) {
      return null;
    }
    BufferedInputStream localBufferedInputStream = new BufferedInputStream(new FileInputStream(localFile));
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream((int)l);
    
    byte[] arrayOfByte = new byte[32768];
    try
    {
      int i = localBufferedInputStream.read(arrayOfByte);
      while (i >= 0)
      {
        localByteArrayOutputStream.write(arrayOfByte, 0, i);
        i = localBufferedInputStream.read(arrayOfByte);
      }
    }
    finally
    {
      localByteArrayOutputStream.close();
      localBufferedInputStream.close();
    }
    return localByteArrayOutputStream.toByteArray();
  }
  
  private void storeAtomic(char paramChar1, char paramChar2, URL paramURL, String paramString, byte[] paramArrayOfByte)
    throws IOException
  {
    File localFile = getTempCacheFile(paramURL, paramString);
    ByteArrayInputStream localByteArrayInputStream = new ByteArrayInputStream(paramArrayOfByte);
    BufferedOutputStream localBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(localFile));
    byte[] arrayOfByte = new byte[32768];
    try
    {
      int i = localByteArrayInputStream.read(arrayOfByte);
      while (i >= 0)
      {
        localBufferedOutputStream.write(arrayOfByte, 0, i);
        i = localByteArrayInputStream.read(arrayOfByte);
      }
    }
    finally
    {
      localBufferedOutputStream.close();
      localByteArrayInputStream.close();
    }
    putFileInCache(paramChar1, paramChar2, paramURL, paramString, localFile);
  }
  
  private File putFileInCache(char paramChar1, char paramChar2, URL paramURL, String paramString, File paramFile)
    throws IOException
  {
    File localFile = new File(keyToFileLocation(paramChar1, paramChar2, paramURL, paramString));
    
    removeOrphans(paramURL);
    
    localFile.delete();
    Object localObject1;
    if (!paramFile.renameTo(localFile))
    {
      deleteEntry(paramChar1, paramChar2, paramURL, paramString);
      if ((paramChar1 == 'R') && (paramChar2 == 'M'))
      {
        localObject1 = new PrintStream(getOutputStream('I', 'M', paramURL, paramString));
        try
        {
          ((PrintStream)localObject1).println(paramFile.getCanonicalPath());
        }
        finally
        {
          ((PrintStream)localObject1).close();
        }
        return paramFile;
      }
      throw new IOException("rename failed in cache to: " + localFile);
    }
    if ((paramChar1 == 'R') && (paramChar2 == 'M'))
    {
      localObject1 = getFileFromCache('I', paramChar2, paramURL, paramString, false);
      if (((File)localObject1).exists())
      {
        deleteEntry(paramChar1, paramChar2, paramURL, paramString);
        
        deleteEntry('I', paramChar2, paramURL, paramString);
      }
    }
    return localFile;
  }
  
  File getFileFromCache(char paramChar1, char paramChar2, URL paramURL, String paramString, boolean paramBoolean)
    throws IOException
  {
    BufferedReader localBufferedReader = null;
    if ((paramChar1 == 'R') && (paramChar2 == 'M'))
    {
      File localFile2 = getFileFromCache('I', paramChar2, paramURL, paramString, false);
      if (localFile2.exists()) {
        try
        {
          InputStream localInputStream = getInputStream('I', 'M', paramURL, paramString);
          
          localBufferedReader = new BufferedReader(new InputStreamReader(localInputStream));
          str = localBufferedReader.readLine();
          localFile1 = new File(str);
          return localFile1;
        }
        catch (IOException localIOException)
        {
          String str;
          if (paramBoolean) {
            return null;
          }
        }
        finally
        {
          if (localBufferedReader != null) {
            localBufferedReader.close();
          }
        }
      }
    }
    File localFile1 = new File(keyToFileLocation(paramChar1, paramChar2, paramURL, paramString));
    if ((paramBoolean) && 
      (!localFile1.exists())) {
      return null;
    }
    return localFile1;
  }
  
  private DiskCacheEntry fileToEntry(File paramFile)
  {
    char c = '\000';
    URL localURL = null;
    String str1 = null;
    long l = 0L;
    
    String str2 = paramFile.getAbsolutePath();
    
    String str3 = this._baseDir.getAbsolutePath();
    if (!str2.startsWith(str3)) {
      return null;
    }
    str2 = str2.substring(str3.length());
    
    StringTokenizer localStringTokenizer = new StringTokenizer(str2, File.separator, false);
    try
    {
      String str4 = localStringTokenizer.nextToken();
      
      String str5 = localStringTokenizer.nextToken();
      if (str5.length() < 1) {
        return null;
      }
      str5 = str5.substring(1);
      
      String str6 = localStringTokenizer.nextToken();
      if (str6.length() < 1) {
        return null;
      }
      int i = 0;
      try
      {
        i = Integer.parseInt(str6.substring(1));
        if (i == 80) {
          i = -1;
        }
      }
      catch (NumberFormatException localNumberFormatException)
      {
        return null;
      }
      StringBuffer localStringBuffer = new StringBuffer();
      while (localStringTokenizer.hasMoreElements())
      {
        String str7 = localStringTokenizer.nextToken();
        str7 = removeEscapes(str7);
        if (str7.length() < 1) {
          return null;
        }
        c = str7.charAt(0);
        if (c == 'V')
        {
          str1 = str7.substring(1);
        }
        else
        {
          localStringBuffer.append('/');
          localStringBuffer.append(str7.substring(2));
        }
      }
      localURL = new URL(str4, str5, i, localStringBuffer.toString());
    }
    catch (MalformedURLException localMalformedURLException)
    {
      return null;
    }
    catch (NoSuchElementException localNoSuchElementException)
    {
      return null;
    }
    DiskCacheEntry localDiskCacheEntry = new DiskCacheEntry(c, localURL, str1, paramFile, 0L);
    
    return localDiskCacheEntry;
  }
  
  private static String removeEscapes(String paramString)
  {
    if ((paramString == null) || (paramString.indexOf('&') == -1)) {
      return paramString;
    }
    StringBuffer localStringBuffer = new StringBuffer(paramString.length());
    int i = 0;
    while (i < paramString.length() - 1)
    {
      char c = paramString.charAt(i);
      int j = paramString.charAt(i + 1);
      if ((c == '&') && (j == 112))
      {
        i++;
        localStringBuffer.append('%');
      }
      else if ((c == '&') && (j == 99))
      {
        i++;
        localStringBuffer.append(':');
      }
      else if ((c != '&') || (j != 38))
      {
        localStringBuffer.append(c);
      }
      i++;
    }
    if (i < paramString.length()) {
      localStringBuffer.append(paramString.charAt(i));
    }
    return localStringBuffer.toString();
  }
  
  private String keyToFileLocation(char paramChar1, char paramChar2, URL paramURL, String paramString)
  {
    StringBuffer localStringBuffer = new StringBuffer(paramURL.toString().length() + (paramString == null ? 0 : paramString.length()) * 2);
    
    localStringBuffer.append(paramURL.getProtocol());
    localStringBuffer.append(File.separatorChar);
    
    localStringBuffer.append('D');
    localStringBuffer.append(paramURL.getHost());
    localStringBuffer.append(File.separatorChar);
    
    String str = null;
    if ((paramURL.getPort() == -1) && (paramURL.getProtocol().equals("http"))) {
      str = "P80";
    } else {
      str = "P" + new Integer(paramURL.getPort()).toString();
    }
    localStringBuffer.append(str);
    localStringBuffer.append(File.separatorChar);
    if (paramString != null)
    {
      localStringBuffer.append('V');
      localStringBuffer.append(paramString);
      localStringBuffer.append(File.separatorChar);
    }
    localStringBuffer.append(convertURLfile(paramChar1, paramChar2, paramURL.getFile()));
    
    return this._baseDir.getAbsolutePath() + File.separator + localStringBuffer.toString();
  }
  
  private String convertURLfile(char paramChar1, char paramChar2, String paramString)
  {
    String str = null;
    int i;
    if ((i = paramString.indexOf(";")) != -1)
    {
      str = paramString.substring(i);
      paramString = paramString.substring(0, i);
    }
    int j;
    if ((j = paramString.indexOf("?")) != -1)
    {
      str = paramString.substring(j) + str;
      paramString = paramString.substring(0, j);
    }
    if (str != null) {
      Trace.println("     URL: " + paramString + "\n  PARAMS: " + str, TraceLevel.CACHE);
    }
    StringBuffer localStringBuffer = new StringBuffer(paramString.length() * 2);
    
    int k = -1;
    for (int m = 0; m < paramString.length(); m++) {
      if (paramString.charAt(m) == '/')
      {
        localStringBuffer.append(File.separatorChar);
        localStringBuffer.append('D');
        localStringBuffer.append('M');
        k = localStringBuffer.length();
      }
      else if (paramString.charAt(m) == ':')
      {
        localStringBuffer.append("&c");
      }
      else if (paramString.charAt(m) == '&')
      {
        localStringBuffer.append("&&");
      }
      else if (paramString.charAt(m) == '%')
      {
        localStringBuffer.append("&p");
      }
      else
      {
        localStringBuffer.append(paramString.charAt(m));
      }
    }
    if (k == -1)
    {
      localStringBuffer.insert(0, paramChar1);
      localStringBuffer.insert(1, paramChar2);
    }
    else
    {
      localStringBuffer.setCharAt(k - 2, paramChar1);
      localStringBuffer.setCharAt(k - 1, paramChar2);
    }
    return localStringBuffer.toString();
  }
  
  long getOrphanSize()
  {
    long l = 0L;
    try
    {
      Iterator localIterator = getOrphans();
      while (localIterator.hasNext()) {
        l += ((DiskCacheEntry)localIterator.next()).getSize();
      }
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
    return l;
  }
  
  void cleanResources()
  {
    try
    {
      Iterator localIterator = getOrphans();
      while (localIterator.hasNext()) {
        removeEntry((DiskCacheEntry)localIterator.next());
      }
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
  }
  
  Iterator getOrphans()
  {
    ArrayList localArrayList1 = new ArrayList();
    ArrayList localArrayList2 = new ArrayList();
    Object localObject = new DiskCacheVisitor()
    {
      private final ArrayList val$appResources;
      
      public void visitEntry(DiskCacheEntry paramAnonymousDiskCacheEntry)
      {
        LaunchDesc localLaunchDesc = null;
        try
        {
          localLaunchDesc = LaunchDescFactory.buildDescriptor(paramAnonymousDiskCacheEntry.getFile());
        }
        catch (Exception localException)
        {
          Trace.ignoredException(localException);
        }
        if (localLaunchDesc != null)
        {
          ResourcesDesc localResourcesDesc = localLaunchDesc.getResources();
          JARDesc[] arrayOfJARDesc = localResourcesDesc.getEagerOrAllJarDescs(true);
          if (arrayOfJARDesc != null) {
            for (int i = 0; i < arrayOfJARDesc.length; i++) {
              try
              {
                File localFile1 = DiskCache.this.getFileFromCache('R', 'M', arrayOfJARDesc[i].getLocation(), arrayOfJARDesc[i].getVersion(), false);
                if (localFile1 != null) {
                  this.val$appResources.add(localFile1);
                }
              }
              catch (IOException localIOException1) {}
            }
          }
          InformationDesc localInformationDesc = localLaunchDesc.getInformation();
          if (localInformationDesc != null)
          {
            IconDesc[] arrayOfIconDesc = localInformationDesc.getIcons();
            if (arrayOfIconDesc != null) {
              for (int j = 0; j < arrayOfIconDesc.length; j++) {
                try
                {
                  File localFile2 = DiskCache.this.getFileFromCache('R', 'M', arrayOfIconDesc[j].getLocation(), arrayOfIconDesc[j].getVersion(), false);
                  if (localFile2 != null) {
                    this.val$appResources.add(localFile2);
                  }
                }
                catch (IOException localIOException2) {}
              }
            }
          }
        }
      }
    };
    try
    {
      visitDiskCache('A', (DiskCacheVisitor)localObject);
      visitDiskCache('E', (DiskCacheVisitor)localObject);
      
      localObject = new DiskCacheVisitor()
      {
        private final ArrayList val$appResources;
        private final ArrayList val$orphanResources;
        
        public void visitEntry(DiskCacheEntry paramAnonymousDiskCacheEntry)
        {
          if (!this.val$appResources.contains(paramAnonymousDiskCacheEntry.getFile())) {
            this.val$orphanResources.add(paramAnonymousDiskCacheEntry);
          }
        }
      };
      visitDiskCache('R', (DiskCacheVisitor)localObject);
      visitDiskCache('I', (DiskCacheVisitor)localObject);
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
    }
    return localArrayList2.iterator();
  }
  
  Iterator getJnlpCacheEntries()
  {
    ArrayList localArrayList = new ArrayList();
    try
    {
      DiskCacheVisitor local4 = new DiskCacheVisitor()
      {
        private final ArrayList val$al;
        
        public void visitEntry(DiskCacheEntry paramAnonymousDiskCacheEntry)
        {
          this.val$al.add(paramAnonymousDiskCacheEntry);
        }
      };
      visitDiskCache('A', local4);
      visitDiskCache('E', local4);
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
    }
    return localArrayList.iterator();
  }
  
  public static abstract interface DiskCacheVisitor
  {
    public abstract void visitEntry(DiskCacheEntry paramDiskCacheEntry);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\cache\DiskCache.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */