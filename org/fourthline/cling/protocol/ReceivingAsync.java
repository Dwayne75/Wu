package org.fourthline.cling.protocol;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpMessage;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.fourthline.cling.transport.RouterException;
import org.seamless.util.Exceptions;

public abstract class ReceivingAsync<M extends UpnpMessage>
  implements Runnable
{
  private static final Logger log = Logger.getLogger(UpnpService.class.getName());
  private final UpnpService upnpService;
  private M inputMessage;
  
  protected ReceivingAsync(UpnpService upnpService, M inputMessage)
  {
    this.upnpService = upnpService;
    this.inputMessage = inputMessage;
  }
  
  public UpnpService getUpnpService()
  {
    return this.upnpService;
  }
  
  public M getInputMessage()
  {
    return this.inputMessage;
  }
  
  public void run()
  {
    boolean proceed;
    try
    {
      proceed = waitBeforeExecution();
    }
    catch (InterruptedException ex)
    {
      boolean proceed;
      log.info("Protocol wait before execution interrupted (on shutdown?): " + getClass().getSimpleName());
      proceed = false;
    }
    if (proceed) {
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
  }
  
  protected boolean waitBeforeExecution()
    throws InterruptedException
  {
    return true;
  }
  
  protected abstract void execute()
    throws RouterException;
  
  protected <H extends UpnpHeader> H getFirstHeader(UpnpHeader.Type headerType, Class<H> subtype)
  {
    return getInputMessage().getHeaders().getFirstHeader(headerType, subtype);
  }
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ")";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\protocol\ReceivingAsync.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */