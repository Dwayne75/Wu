package org.apache.http.impl.conn.tsccm;

import java.util.concurrent.TimeUnit;
import org.apache.http.conn.ConnectionPoolTimeoutException;

@Deprecated
public abstract interface PoolEntryRequest
{
  public abstract BasicPoolEntry getPoolEntry(long paramLong, TimeUnit paramTimeUnit)
    throws InterruptedException, ConnectionPoolTimeoutException;
  
  public abstract void abortRequest();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\conn\tsccm\PoolEntryRequest.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */