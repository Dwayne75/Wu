package org.apache.http.impl.io;

import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.io.HttpTransportMetrics;

@NotThreadSafe
public class HttpTransportMetricsImpl
  implements HttpTransportMetrics
{
  private long bytesTransferred = 0L;
  
  public long getBytesTransferred()
  {
    return this.bytesTransferred;
  }
  
  public void setBytesTransferred(long count)
  {
    this.bytesTransferred = count;
  }
  
  public void incrementBytesTransferred(long count)
  {
    this.bytesTransferred += count;
  }
  
  public void reset()
  {
    this.bytesTransferred = 0L;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\io\HttpTransportMetricsImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */