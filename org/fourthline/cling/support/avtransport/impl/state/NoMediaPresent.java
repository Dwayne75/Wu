package org.fourthline.cling.support.avtransport.impl.state;

import java.net.URI;
import java.util.logging.Logger;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.CurrentTransportActions;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.TransportState;
import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportState;

public abstract class NoMediaPresent<T extends AVTransport>
  extends AbstractState<T>
{
  private static final Logger log = Logger.getLogger(Stopped.class.getName());
  
  public NoMediaPresent(T transport)
  {
    super(transport);
  }
  
  public void onEntry()
  {
    log.fine("Setting transport state to NO_MEDIA_PRESENT");
    getTransport().setTransportInfo(new TransportInfo(TransportState.NO_MEDIA_PRESENT, 
    
      getTransport().getTransportInfo().getCurrentTransportStatus(), 
      getTransport().getTransportInfo().getCurrentSpeed()));
    
    getTransport().getLastChange().setEventedValue(
      getTransport().getInstanceId(), new EventedValue[] { new AVTransportVariable.TransportState(TransportState.NO_MEDIA_PRESENT), new AVTransportVariable.CurrentTransportActions(
      
      getCurrentTransportActions()) });
  }
  
  public abstract Class<? extends AbstractState> setTransportURI(URI paramURI, String paramString);
  
  public TransportAction[] getCurrentTransportActions()
  {
    return new TransportAction[] { TransportAction.Stop };
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\avtransport\impl\state\NoMediaPresent.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */