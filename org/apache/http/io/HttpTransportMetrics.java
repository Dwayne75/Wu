package org.apache.http.io;

public abstract interface HttpTransportMetrics
{
  public abstract long getBytesTransferred();
  
  public abstract void reset();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\io\HttpTransportMetrics.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */