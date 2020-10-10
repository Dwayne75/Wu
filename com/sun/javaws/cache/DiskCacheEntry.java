package com.sun.javaws.cache;

import java.io.File;
import java.net.URL;

public class DiskCacheEntry
{
  private char _type;
  private URL _location;
  private String _versionId;
  private long _timestamp;
  private File _file;
  private File _directory;
  private File _mappedBitmap;
  private File _muffinTag;
  
  public DiskCacheEntry()
  {
    this('\000', null, null, null, 0L);
  }
  
  public DiskCacheEntry(char paramChar, URL paramURL, String paramString, File paramFile, long paramLong)
  {
    this(paramChar, paramURL, paramString, paramFile, paramLong, null, null, null);
  }
  
  public DiskCacheEntry(char paramChar, URL paramURL, String paramString, File paramFile1, long paramLong, File paramFile2, File paramFile3)
  {
    this(paramChar, paramURL, paramString, paramFile1, paramLong, paramFile2, paramFile3, null);
  }
  
  public DiskCacheEntry(char paramChar, URL paramURL, String paramString, File paramFile1, long paramLong, File paramFile2, File paramFile3, File paramFile4)
  {
    this._type = paramChar;
    this._location = paramURL;
    this._versionId = paramString;
    this._timestamp = paramLong;
    this._file = paramFile1;
    this._directory = paramFile2;
    this._mappedBitmap = paramFile3;
    this._muffinTag = paramFile4;
  }
  
  public char getType()
  {
    return this._type;
  }
  
  public void setType(char paramChar)
  {
    this._type = paramChar;
  }
  
  public URL getLocation()
  {
    return this._location;
  }
  
  public void setLocataion(URL paramURL)
  {
    this._location = paramURL;
  }
  
  public long getTimeStamp()
  {
    return this._timestamp;
  }
  
  public void setTimeStamp(long paramLong)
  {
    this._timestamp = paramLong;
  }
  
  public File getMuffinTagFile()
  {
    return this._muffinTag;
  }
  
  public void setMuffinTagFile(File paramFile)
  {
    this._muffinTag = paramFile;
  }
  
  public String getVersionId()
  {
    return this._versionId;
  }
  
  public void setVersionId(String paramString)
  {
    this._versionId = paramString;
  }
  
  public File getFile()
  {
    return this._file;
  }
  
  public void setFile(File paramFile)
  {
    this._file = paramFile;
  }
  
  public File getDirectory()
  {
    return this._directory;
  }
  
  public void setDirectory(File paramFile)
  {
    this._directory = paramFile;
  }
  
  public File getMappedBitmap()
  {
    return this._mappedBitmap;
  }
  
  public void setMappedBitmap(File paramFile)
  {
    this._mappedBitmap = paramFile;
  }
  
  public long getLastAccess()
  {
    return this._file == null ? 0L : this._file.lastModified();
  }
  
  public void setLastAccess(long paramLong)
  {
    if (this._file != null) {
      this._file.setLastModified(paramLong);
    }
  }
  
  public boolean isEmpty()
  {
    return this._location == null;
  }
  
  public long getSize()
  {
    if ((this._directory != null) && (this._directory.isDirectory()))
    {
      long l = 0L;
      File[] arrayOfFile = this._directory.listFiles();
      for (int i = 0; i < arrayOfFile.length; i++) {
        l += arrayOfFile[i].length();
      }
      return l;
    }
    return this._file.length();
  }
  
  public boolean newerThan(DiskCacheEntry paramDiskCacheEntry)
  {
    if ((paramDiskCacheEntry == null) || (getVersionId() != null)) {
      return true;
    }
    return getTimeStamp() > paramDiskCacheEntry.getTimeStamp();
  }
  
  public String toString()
  {
    if (isEmpty()) {
      return "DisckCacheEntry[<empty>]";
    }
    return "DisckCacheEntry[" + this._type + ";" + this._location + ";" + this._versionId + ";" + this._timestamp + ";" + this._file + ";" + this._directory + "]";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\cache\DiskCacheEntry.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */