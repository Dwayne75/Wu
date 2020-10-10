package com.mysql.jdbc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

class WatchableOutputStream
  extends ByteArrayOutputStream
{
  private OutputStreamWatcher watcher;
  
  public void close()
    throws IOException
  {
    super.close();
    if (this.watcher != null) {
      this.watcher.streamClosed(this);
    }
  }
  
  public void setWatcher(OutputStreamWatcher watcher)
  {
    this.watcher = watcher;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\WatchableOutputStream.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */