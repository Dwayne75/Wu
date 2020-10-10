package org.fourthline.cling.model.message.gena;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.UpnpRequest.Method;
import org.fourthline.cling.model.message.header.ContentTypeHeader;
import org.fourthline.cling.model.message.header.EventSequenceHeader;
import org.fourthline.cling.model.message.header.NTEventHeader;
import org.fourthline.cling.model.message.header.NTSHeader;
import org.fourthline.cling.model.message.header.SubscriptionIdHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.model.types.NotificationSubtype;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

public class OutgoingEventRequestMessage
  extends StreamRequestMessage
{
  private final Collection<StateVariableValue> stateVariableValues;
  
  public OutgoingEventRequestMessage(GENASubscription subscription, URL callbackURL, UnsignedIntegerFourBytes sequence, Collection<StateVariableValue> values)
  {
    super(new UpnpRequest(UpnpRequest.Method.NOTIFY, callbackURL));
    
    getHeaders().add(UpnpHeader.Type.CONTENT_TYPE, new ContentTypeHeader());
    getHeaders().add(UpnpHeader.Type.NT, new NTEventHeader());
    getHeaders().add(UpnpHeader.Type.NTS, new NTSHeader(NotificationSubtype.PROPCHANGE));
    getHeaders().add(UpnpHeader.Type.SID, new SubscriptionIdHeader(subscription.getSubscriptionId()));
    
    getHeaders().add(UpnpHeader.Type.SEQ, new EventSequenceHeader(sequence.getValue().longValue()));
    
    this.stateVariableValues = values;
  }
  
  public OutgoingEventRequestMessage(GENASubscription subscription, URL callbackURL)
  {
    this(subscription, callbackURL, subscription.getCurrentSequence(), subscription.getCurrentValues().values());
  }
  
  public Collection<StateVariableValue> getStateVariableValues()
  {
    return this.stateVariableValues;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\gena\OutgoingEventRequestMessage.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */