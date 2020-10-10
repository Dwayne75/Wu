package org.fourthline.cling.controlpoint;

import java.util.concurrent.Future;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.registry.Registry;

public abstract interface ControlPoint
{
  public abstract UpnpServiceConfiguration getConfiguration();
  
  public abstract ProtocolFactory getProtocolFactory();
  
  public abstract Registry getRegistry();
  
  public abstract void search();
  
  public abstract void search(UpnpHeader paramUpnpHeader);
  
  public abstract void search(int paramInt);
  
  public abstract void search(UpnpHeader paramUpnpHeader, int paramInt);
  
  public abstract Future execute(ActionCallback paramActionCallback);
  
  public abstract void execute(SubscriptionCallback paramSubscriptionCallback);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\controlpoint\ControlPoint.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */