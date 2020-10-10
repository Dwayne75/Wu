package org.fourthline.cling.support.avtransport.impl.state;

import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.TransportAction;

public abstract class AbstractState<T extends AVTransport>
{
  private T transport;
  
  public AbstractState(T transport)
  {
    this.transport = transport;
  }
  
  public T getTransport()
  {
    return this.transport;
  }
  
  public abstract TransportAction[] getCurrentTransportActions();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\avtransport\impl\state\AbstractState.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */