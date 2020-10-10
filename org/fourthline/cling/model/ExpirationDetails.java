package org.fourthline.cling.model;

import java.util.Date;

public class ExpirationDetails
{
  public static final int UNLIMITED_AGE = 0;
  private int maxAgeSeconds = 0;
  private long lastRefreshTimestampSeconds = getCurrentTimestampSeconds();
  
  public ExpirationDetails() {}
  
  public ExpirationDetails(int maxAgeSeconds)
  {
    this.maxAgeSeconds = maxAgeSeconds;
  }
  
  public int getMaxAgeSeconds()
  {
    return this.maxAgeSeconds;
  }
  
  public long getLastRefreshTimestampSeconds()
  {
    return this.lastRefreshTimestampSeconds;
  }
  
  public void setLastRefreshTimestampSeconds(long lastRefreshTimestampSeconds)
  {
    this.lastRefreshTimestampSeconds = lastRefreshTimestampSeconds;
  }
  
  public void stampLastRefresh()
  {
    setLastRefreshTimestampSeconds(getCurrentTimestampSeconds());
  }
  
  public boolean hasExpired()
  {
    return hasExpired(false);
  }
  
  public boolean hasExpired(boolean halfTime)
  {
    if (this.maxAgeSeconds != 0) {}
    return this.lastRefreshTimestampSeconds + this.maxAgeSeconds / (halfTime ? 2 : 1) < getCurrentTimestampSeconds();
  }
  
  public long getSecondsUntilExpiration()
  {
    return this.maxAgeSeconds == 0 ? 2147483647L : this.lastRefreshTimestampSeconds + this.maxAgeSeconds - getCurrentTimestampSeconds();
  }
  
  protected long getCurrentTimestampSeconds()
  {
    return new Date().getTime() / 1000L;
  }
  
  private static String simpleName = ExpirationDetails.class.getSimpleName();
  
  public String toString()
  {
    return "(" + simpleName + ")" + " MAX AGE: " + this.maxAgeSeconds;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\ExpirationDetails.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */