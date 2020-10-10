package com.sun.javaws.cache;

import java.io.File;
import java.net.URL;

public class DiskEntry
{
  private URL _url;
  private long _timestamp;
  private File _file;
  
  public DiskEntry(URL paramURL, File paramFile, long paramLong)
  {
    this._url = paramURL;
    this._timestamp = paramLong;
    this._file = paramFile;
  }
  
  public URL getURL()
  {
    return this._url;
  }
  
  public long getTimeStamp()
  {
    return this._timestamp;
  }
  
  public File getFile()
  {
    return this._file;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\cache\DiskEntry.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */