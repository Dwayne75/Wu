package org.fourthline.cling.support.avtransport.impl.state;

import java.net.URI;
import java.util.logging.Logger;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.CurrentTransportActions;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.TransportState;
import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.SeekMode;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportState;

public abstract class Stopped<T extends AVTransport>
  extends AbstractState<T>
{
  private static final Logger log = Logger.getLogger(Stopped.class.getName());
  
  public Stopped(T transport)
  {
    super(transport);
  }
  
  public void onEntry()
  {
    log.fine("Setting transport state to STOPPED");
    getTransport().setTransportInfo(new TransportInfo(TransportState.STOPPED, 
    
      getTransport().getTransportInfo().getCurrentTransportStatus(), 
      getTransport().getTransportInfo().getCurrentSpeed()));
    
    getTransport().getLastChange().setEventedValue(
      getTransport().getInstanceId(), new EventedValue[] { new AVTransportVariable.TransportState(TransportState.STOPPED), new AVTransportVariable.CurrentTransportActions(
      
      getCurrentTransportActions()) });
  }
  
  public abstract Class<? extends AbstractState<?>> setTransportURI(URI paramURI, String paramString);
  
  public abstract Class<? extends AbstractState<?>> stop();
  
  public abstract Class<? extends AbstractState<?>> play(String paramString);
  
  public abstract Class<? extends AbstractState<?>> next();
  
  public abstract Class<? extends AbstractState<?>> previous();
  
  public abstract Class<? extends AbstractState<?>> seek(SeekMode paramSeekMode, String paramString);
  
  public TransportAction[] getCurrentTransportActions()
  {
    return new TransportAction[] { TransportAction.Stop, TransportAction.Play, TransportAction.Next, TransportAction.Previous, TransportAction.Seek };
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\avtransport\impl\state\Stopped.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */