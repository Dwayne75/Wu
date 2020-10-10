package org.fourthline.cling.model.gena;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.ServiceManager;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.meta.StateVariableEventDetails;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.seamless.util.Exceptions;

public abstract class LocalGENASubscription
  extends GENASubscription<LocalService>
  implements PropertyChangeListener
{
  private static Logger log = Logger.getLogger(LocalGENASubscription.class.getName());
  final List<URL> callbackURLs;
  final Map<String, Long> lastSentTimestamp = new HashMap();
  final Map<String, Long> lastSentNumericValue = new HashMap();
  
  protected LocalGENASubscription(LocalService service, List<URL> callbackURLs)
    throws Exception
  {
    super(service);
    this.callbackURLs = callbackURLs;
  }
  
  public LocalGENASubscription(LocalService service, Integer requestedDurationSeconds, List<URL> callbackURLs)
    throws Exception
  {
    super(service);
    
    setSubscriptionDuration(requestedDurationSeconds);
    
    log.fine("Reading initial state of local service at subscription time");
    long currentTime = new Date().getTime();
    this.currentValues.clear();
    
    Collection<StateVariableValue> values = ((LocalService)getService()).getManager().getCurrentState();
    
    log.finer("Got evented state variable values: " + values.size());
    for (StateVariableValue value : values)
    {
      this.currentValues.put(value.getStateVariable().getName(), value);
      if (log.isLoggable(Level.FINEST)) {
        log.finer("Read state variable value '" + value.getStateVariable().getName() + "': " + value.toString());
      }
      this.lastSentTimestamp.put(value.getStateVariable().getName(), Long.valueOf(currentTime));
      if (value.getStateVariable().isModeratedNumericType()) {
        this.lastSentNumericValue.put(value.getStateVariable().getName(), Long.valueOf(value.toString()));
      }
    }
    this.subscriptionId = ("uuid:" + UUID.randomUUID());
    this.currentSequence = new UnsignedIntegerFourBytes(0L);
    this.callbackURLs = callbackURLs;
  }
  
  public synchronized List<URL> getCallbackURLs()
  {
    return this.callbackURLs;
  }
  
  public synchronized void registerOnService()
  {
    ((LocalService)getService()).getManager().getPropertyChangeSupport().addPropertyChangeListener(this);
  }
  
  public synchronized void establish()
  {
    established();
  }
  
  public synchronized void end(CancelReason reason)
  {
    try
    {
      ((LocalService)getService()).getManager().getPropertyChangeSupport().removePropertyChangeListener(this);
    }
    catch (Exception ex)
    {
      log.warning("Removal of local service property change listener failed: " + Exceptions.unwrap(ex));
    }
    ended(reason);
  }
  
  public synchronized void propertyChange(PropertyChangeEvent e)
  {
    if (!e.getPropertyName().equals("_EventedStateVariables")) {
      return;
    }
    log.fine("Eventing triggered, getting state for subscription: " + getSubscriptionId());
    
    long currentTime = new Date().getTime();
    
    Collection<StateVariableValue> newValues = (Collection)e.getNewValue();
    Set<String> excludedVariables = moderateStateVariables(currentTime, newValues);
    
    this.currentValues.clear();
    for (StateVariableValue newValue : newValues)
    {
      String name = newValue.getStateVariable().getName();
      if (!excludedVariables.contains(name))
      {
        log.fine("Adding state variable value to current values of event: " + newValue.getStateVariable() + " = " + newValue);
        this.currentValues.put(newValue.getStateVariable().getName(), newValue);
        
        this.lastSentTimestamp.put(name, Long.valueOf(currentTime));
        if (newValue.getStateVariable().isModeratedNumericType()) {
          this.lastSentNumericValue.put(name, Long.valueOf(newValue.toString()));
        }
      }
    }
    if (this.currentValues.size() > 0)
    {
      log.fine("Propagating new state variable values to subscription: " + this);
      
      eventReceived();
    }
    else
    {
      log.fine("No state variable values for event (all moderated out?), not triggering event");
    }
  }
  
  protected synchronized Set<String> moderateStateVariables(long currentTime, Collection<StateVariableValue> values)
  {
    Set<String> excludedVariables = new HashSet();
    for (StateVariableValue stateVariableValue : values)
    {
      StateVariable stateVariable = stateVariableValue.getStateVariable();
      String stateVariableName = stateVariableValue.getStateVariable().getName();
      if ((stateVariable.getEventDetails().getEventMaximumRateMilliseconds() == 0) && 
        (stateVariable.getEventDetails().getEventMinimumDelta() == 0))
      {
        log.finer("Variable is not moderated: " + stateVariable);
      }
      else if (!this.lastSentTimestamp.containsKey(stateVariableName))
      {
        log.finer("Variable is moderated but was never sent before: " + stateVariable);
      }
      else
      {
        if (stateVariable.getEventDetails().getEventMaximumRateMilliseconds() > 0)
        {
          long timestampLastSent = ((Long)this.lastSentTimestamp.get(stateVariableName)).longValue();
          long timestampNextSend = timestampLastSent + stateVariable.getEventDetails().getEventMaximumRateMilliseconds();
          if (currentTime <= timestampNextSend)
          {
            log.finer("Excluding state variable with maximum rate: " + stateVariable);
            excludedVariables.add(stateVariableName);
            continue;
          }
        }
        if ((stateVariable.isModeratedNumericType()) && (this.lastSentNumericValue.get(stateVariableName) != null))
        {
          long oldValue = Long.valueOf(((Long)this.lastSentNumericValue.get(stateVariableName)).longValue()).longValue();
          long newValue = Long.valueOf(stateVariableValue.toString()).longValue();
          long minDelta = stateVariable.getEventDetails().getEventMinimumDelta();
          if ((newValue > oldValue) && (newValue - oldValue < minDelta))
          {
            log.finer("Excluding state variable with minimum delta: " + stateVariable);
            excludedVariables.add(stateVariableName);
          }
          else if ((newValue < oldValue) && (oldValue - newValue < minDelta))
          {
            log.finer("Excluding state variable with minimum delta: " + stateVariable);
            excludedVariables.add(stateVariableName);
          }
        }
      }
    }
    return excludedVariables;
  }
  
  public synchronized void incrementSequence()
  {
    this.currentSequence.increment(true);
  }
  
  public synchronized void setSubscriptionDuration(Integer requestedDurationSeconds)
  {
    this.requestedDurationSeconds = (requestedDurationSeconds == null ? 1800 : requestedDurationSeconds.intValue());
    
    setActualSubscriptionDurationSeconds(this.requestedDurationSeconds);
  }
  
  public abstract void ended(CancelReason paramCancelReason);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\gena\LocalGENASubscription.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */