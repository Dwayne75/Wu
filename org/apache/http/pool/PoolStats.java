package org.apache.http.pool;

import org.apache.http.annotation.Immutable;

@Immutable
public class PoolStats
{
  private final int leased;
  private final int pending;
  private final int available;
  private final int max;
  
  public PoolStats(int leased, int pending, int free, int max)
  {
    this.leased = leased;
    this.pending = pending;
    this.available = free;
    this.max = max;
  }
  
  public int getLeased()
  {
    return this.leased;
  }
  
  public int getPending()
  {
    return this.pending;
  }
  
  public int getAvailable()
  {
    return this.available;
  }
  
  public int getMax()
  {
    return this.max;
  }
  
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append("[leased: ");
    buffer.append(this.leased);
    buffer.append("; pending: ");
    buffer.append(this.pending);
    buffer.append("; available: ");
    buffer.append(this.available);
    buffer.append("; max: ");
    buffer.append(this.max);
    buffer.append("]");
    return buffer.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\pool\PoolStats.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */