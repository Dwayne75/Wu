package org.fourthline.cling.model.gena;

import java.util.LinkedHashMap;
import java.util.Map;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

public abstract class GENASubscription<S extends Service>
{
  protected S service;
  protected String subscriptionId;
  protected int requestedDurationSeconds = 1800;
  protected int actualDurationSeconds;
  protected UnsignedIntegerFourBytes currentSequence;
  protected Map<String, StateVariableValue<S>> currentValues = new LinkedHashMap();
  
  protected GENASubscription(S service)
  {
    this.service = service;
  }
  
  public GENASubscription(S service, int requestedDurationSeconds)
  {
    this(service);
  }
  
  public synchronized S getService()
  {
    return this.service;
  }
  
  public synchronized String getSubscriptionId()
  {
    return this.subscriptionId;
  }
  
  public synchronized void setSubscriptionId(String subscriptionId)
  {
    this.subscriptionId = subscriptionId;
  }
  
  public synchronized int getRequestedDurationSeconds()
  {
    return this.requestedDurationSeconds;
  }
  
  public synchronized int getActualDurationSeconds()
  {
    return this.actualDurationSeconds;
  }
  
  public synchronized void setActualSubscriptionDurationSeconds(int seconds)
  {
    this.actualDurationSeconds = seconds;
  }
  
  public synchronized UnsignedIntegerFourBytes getCurrentSequence()
  {
    return this.currentSequence;
  }
  
  public synchronized Map<String, StateVariableValue<S>> getCurrentValues()
  {
    return this.currentValues;
  }
  
  public abstract void established();
  
  public abstract void eventReceived();
  
  public String toString()
  {
    return "(GENASubscription, SID: " + getSubscriptionId() + ", SEQUENCE: " + getCurrentSequence() + ")";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\gena\GENASubscription.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */