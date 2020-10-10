package com.mysql.jdbc;

import java.io.CharArrayWriter;

class WatchableWriter
  extends CharArrayWriter
{
  private WriterWatcher watcher;
  
  public void close()
  {
    super.close();
    if (this.watcher != null) {
      this.watcher.writerClosed(this);
    }
  }
  
  public void setWatcher(WriterWatcher watcher)
  {
    this.watcher = watcher;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\WatchableWriter.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */