package org.flywaydb.core.internal.util;

import java.util.concurrent.TimeUnit;

public class StopWatch
{
  private long start;
  private long stop;
  
  public void start()
  {
    this.start = System.nanoTime();
  }
  
  public void stop()
  {
    this.stop = System.nanoTime();
  }
  
  public long getTotalTimeMillis()
  {
    return TimeUnit.NANOSECONDS.toMillis(this.stop - this.start);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\StopWatch.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */