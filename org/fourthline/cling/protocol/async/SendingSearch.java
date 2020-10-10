package org.fourthline.cling.protocol.async;

import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.message.discovery.OutgoingSearchRequest;
import org.fourthline.cling.model.message.header.MXHeader;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.fourthline.cling.protocol.SendingAsync;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.RouterException;

public class SendingSearch
  extends SendingAsync
{
  private static final Logger log = Logger.getLogger(SendingSearch.class.getName());
  private final UpnpHeader searchTarget;
  private final int mxSeconds;
  
  public SendingSearch(UpnpService upnpService)
  {
    this(upnpService, new STAllHeader());
  }
  
  public SendingSearch(UpnpService upnpService, UpnpHeader searchTarget)
  {
    this(upnpService, searchTarget, MXHeader.DEFAULT_VALUE.intValue());
  }
  
  public SendingSearch(UpnpService upnpService, UpnpHeader searchTarget, int mxSeconds)
  {
    super(upnpService);
    if (!UpnpHeader.Type.ST.isValidHeaderType(searchTarget.getClass())) {
      throw new IllegalArgumentException("Given search target instance is not a valid header class for type ST: " + searchTarget.getClass());
    }
    this.searchTarget = searchTarget;
    this.mxSeconds = mxSeconds;
  }
  
  public UpnpHeader getSearchTarget()
  {
    return this.searchTarget;
  }
  
  public int getMxSeconds()
  {
    return this.mxSeconds;
  }
  
  protected void execute()
    throws RouterException
  {
    log.fine("Executing search for target: " + this.searchTarget.getString() + " with MX seconds: " + getMxSeconds());
    
    OutgoingSearchRequest msg = new OutgoingSearchRequest(this.searchTarget, getMxSeconds());
    prepareOutgoingSearchRequest(msg);
    for (int i = 0; i < getBulkRepeat(); i++) {
      try
      {
        getUpnpService().getRouter().send(msg);
        
        log.finer("Sleeping " + getBulkIntervalMilliseconds() + " milliseconds");
        Thread.sleep(getBulkIntervalMilliseconds());
      }
      catch (InterruptedException ex)
      {
        break;
      }
    }
  }
  
  public int getBulkRepeat()
  {
    return 5;
  }
  
  public int getBulkIntervalMilliseconds()
  {
    return 500;
  }
  
  protected void prepareOutgoingSearchRequest(OutgoingSearchRequest message) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\protocol\async\SendingSearch.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */