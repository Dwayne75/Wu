package org.fourthline.cling.registry;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RegistryMaintainer
  implements Runnable
{
  private static Logger log = Logger.getLogger(RegistryMaintainer.class.getName());
  private final RegistryImpl registry;
  private final int sleepIntervalMillis;
  private volatile boolean stopped = false;
  
  public RegistryMaintainer(RegistryImpl registry, int sleepIntervalMillis)
  {
    this.registry = registry;
    this.sleepIntervalMillis = sleepIntervalMillis;
  }
  
  public void stop()
  {
    if (log.isLoggable(Level.FINE)) {
      log.fine("Setting stopped status on thread");
    }
    this.stopped = true;
  }
  
  public void run()
  {
    this.stopped = false;
    if (log.isLoggable(Level.FINE)) {
      log.fine("Running registry maintenance loop every milliseconds: " + this.sleepIntervalMillis);
    }
    while (!this.stopped) {
      try
      {
        this.registry.maintain();
        Thread.sleep(this.sleepIntervalMillis);
      }
      catch (InterruptedException ex)
      {
        this.stopped = true;
      }
    }
    log.fine("Stopped status on thread received, ending maintenance loop");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\registry\RegistryMaintainer.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */