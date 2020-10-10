package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import java.util.concurrent.TimeUnit;

public abstract class WatchServiceConfiguration
{
  static final WatchServiceConfiguration DEFAULT = polling(5L, TimeUnit.SECONDS);
  
  public static WatchServiceConfiguration polling(long interval, TimeUnit timeUnit)
  {
    return new PollingConfig(interval, timeUnit, null);
  }
  
  abstract AbstractWatchService newWatchService(FileSystemView paramFileSystemView, PathService paramPathService);
  
  private static final class PollingConfig
    extends WatchServiceConfiguration
  {
    private final long interval;
    private final TimeUnit timeUnit;
    
    private PollingConfig(long interval, TimeUnit timeUnit)
    {
      Preconditions.checkArgument(interval > 0L, "interval (%s) must be positive", new Object[] { Long.valueOf(interval) });
      this.interval = interval;
      this.timeUnit = ((TimeUnit)Preconditions.checkNotNull(timeUnit));
    }
    
    AbstractWatchService newWatchService(FileSystemView view, PathService pathService)
    {
      return new PollingWatchService(view, pathService, view.state(), this.interval, this.timeUnit);
    }
    
    public String toString()
    {
      return "WatchServiceConfiguration.polling(" + this.interval + ", " + this.timeUnit + ")";
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\WatchServiceConfiguration.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */