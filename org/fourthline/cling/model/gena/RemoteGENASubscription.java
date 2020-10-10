package org.fourthline.cling.model.gena;

import java.beans.PropertyChangeSupport;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.fourthline.cling.model.Location;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.NetworkAddress;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.UnsignedVariableInteger.Bits;

public abstract class RemoteGENASubscription
  extends GENASubscription<RemoteService>
{
  protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
  
  protected RemoteGENASubscription(RemoteService service, int requestedDurationSeconds)
  {
    super(service, requestedDurationSeconds);
  }
  
  public synchronized URL getEventSubscriptionURL()
  {
    return ((RemoteDevice)((RemoteService)getService()).getDevice()).normalizeURI(
      ((RemoteService)getService()).getEventSubscriptionURI());
  }
  
  public synchronized List<URL> getEventCallbackURLs(List<NetworkAddress> activeStreamServers, Namespace namespace)
  {
    List<URL> callbackURLs = new ArrayList();
    for (NetworkAddress activeStreamServer : activeStreamServers) {
      callbackURLs.add(new Location(activeStreamServer, namespace
      
        .getEventCallbackPathString(getService()))
        .getURL());
    }
    return callbackURLs;
  }
  
  public synchronized void establish()
  {
    established();
  }
  
  public synchronized void fail(UpnpResponse responseStatus)
  {
    failed(responseStatus);
  }
  
  public synchronized void end(CancelReason reason, UpnpResponse response)
  {
    ended(reason, response);
  }
  
  public synchronized void receive(UnsignedIntegerFourBytes sequence, Collection<StateVariableValue> newValues)
  {
    int difference;
    if (this.currentSequence != null)
    {
      if ((this.currentSequence.getValue().equals(Long.valueOf(this.currentSequence.getBits().getMaxValue()))) && (sequence.getValue().longValue() == 1L))
      {
        System.err.println("TODO: HANDLE ROLLOVER");
        return;
      }
      if (this.currentSequence.getValue().longValue() >= sequence.getValue().longValue()) {
        return;
      }
      long expectedValue = this.currentSequence.getValue().longValue() + 1L;
      if ((difference = (int)(sequence.getValue().longValue() - expectedValue)) != 0) {
        eventsMissed(difference);
      }
    }
    this.currentSequence = sequence;
    for (StateVariableValue newValue : newValues) {
      this.currentValues.put(newValue.getStateVariable().getName(), newValue);
    }
    eventReceived();
  }
  
  public abstract void invalidMessage(UnsupportedDataException paramUnsupportedDataException);
  
  public abstract void failed(UpnpResponse paramUpnpResponse);
  
  public abstract void ended(CancelReason paramCancelReason, UpnpResponse paramUpnpResponse);
  
  public abstract void eventsMissed(int paramInt);
  
  public String toString()
  {
    return "(SID: " + getSubscriptionId() + ") " + getService();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\gena\RemoteGENASubscription.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */