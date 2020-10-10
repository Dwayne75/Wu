package org.apache.http;

import java.io.Closeable;
import java.io.IOException;

public abstract interface HttpConnection
  extends Closeable
{
  public abstract void close()
    throws IOException;
  
  public abstract boolean isOpen();
  
  public abstract boolean isStale();
  
  public abstract void setSocketTimeout(int paramInt);
  
  public abstract int getSocketTimeout();
  
  public abstract void shutdown()
    throws IOException;
  
  public abstract HttpConnectionMetrics getMetrics();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\HttpConnection.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */