package org.fourthline.cling.protocol;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.transport.RouterException;
import org.seamless.util.Exceptions;

public abstract class SendingAsync
  implements Runnable
{
  private static final Logger log = Logger.getLogger(UpnpService.class.getName());
  private final UpnpService upnpService;
  
  protected SendingAsync(UpnpService upnpService)
  {
    this.upnpService = upnpService;
  }
  
  public UpnpService getUpnpService()
  {
    return this.upnpService;
  }
  
  public void run()
  {
    try
    {
      execute();
    }
    catch (Exception ex)
    {
      Throwable cause = Exceptions.unwrap(ex);
      if ((cause instanceof InterruptedException)) {
        log.log(Level.INFO, "Interrupted protocol '" + getClass().getSimpleName() + "': " + ex, cause);
      } else {
        throw new RuntimeException("Fatal error while executing protocol '" + getClass().getSimpleName() + "': " + ex, ex);
      }
    }
  }
  
  protected abstract void execute()
    throws RouterException;
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ")";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\protocol\SendingAsync.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */