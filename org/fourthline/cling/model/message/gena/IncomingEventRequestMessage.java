package org.fourthline.cling.model.message.gena;

import java.util.ArrayList;
import java.util.List;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.header.EventSequenceHeader;
import org.fourthline.cling.model.message.header.NTEventHeader;
import org.fourthline.cling.model.message.header.NTSHeader;
import org.fourthline.cling.model.message.header.SubscriptionIdHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.model.types.NotificationSubtype;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

public class IncomingEventRequestMessage
  extends StreamRequestMessage
{
  private final List<StateVariableValue> stateVariableValues = new ArrayList();
  private final RemoteService service;
  
  public IncomingEventRequestMessage(StreamRequestMessage source, RemoteService service)
  {
    super(source);
    this.service = service;
  }
  
  public RemoteService getService()
  {
    return this.service;
  }
  
  public List<StateVariableValue> getStateVariableValues()
  {
    return this.stateVariableValues;
  }
  
  public String getSubscrptionId()
  {
    SubscriptionIdHeader header = (SubscriptionIdHeader)getHeaders().getFirstHeader(UpnpHeader.Type.SID, SubscriptionIdHeader.class);
    return header != null ? (String)header.getValue() : null;
  }
  
  public UnsignedIntegerFourBytes getSequence()
  {
    EventSequenceHeader header = (EventSequenceHeader)getHeaders().getFirstHeader(UpnpHeader.Type.SEQ, EventSequenceHeader.class);
    return header != null ? (UnsignedIntegerFourBytes)header.getValue() : null;
  }
  
  public boolean hasNotificationHeaders()
  {
    UpnpHeader ntHeader = getHeaders().getFirstHeader(UpnpHeader.Type.NT);
    UpnpHeader ntsHeader = getHeaders().getFirstHeader(UpnpHeader.Type.NTS);
    
    return (ntHeader != null) && (ntHeader.getValue() != null) && (ntsHeader != null) && (ntsHeader.getValue() != null);
  }
  
  public boolean hasValidNotificationHeaders()
  {
    NTEventHeader ntHeader = (NTEventHeader)getHeaders().getFirstHeader(UpnpHeader.Type.NT, NTEventHeader.class);
    NTSHeader ntsHeader = (NTSHeader)getHeaders().getFirstHeader(UpnpHeader.Type.NTS, NTSHeader.class);
    
    return (ntHeader != null) && (ntHeader.getValue() != null) && (ntsHeader != null) && (((NotificationSubtype)ntsHeader.getValue()).equals(NotificationSubtype.PROPCHANGE));
  }
  
  public String toString()
  {
    return super.toString() + " SEQUENCE: " + getSequence().getValue();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\gena\IncomingEventRequestMessage.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */