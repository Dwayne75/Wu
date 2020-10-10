package org.fourthline.cling.support.lastchange;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

public class Event
{
  protected List<InstanceID> instanceIDs = new ArrayList();
  
  public Event() {}
  
  public Event(List<InstanceID> instanceIDs)
  {
    this.instanceIDs = instanceIDs;
  }
  
  public List<InstanceID> getInstanceIDs()
  {
    return this.instanceIDs;
  }
  
  public InstanceID getInstanceID(UnsignedIntegerFourBytes id)
  {
    for (InstanceID instanceID : this.instanceIDs) {
      if (instanceID.getId().equals(id)) {
        return instanceID;
      }
    }
    return null;
  }
  
  public void clear()
  {
    this.instanceIDs = new ArrayList();
  }
  
  public void setEventedValue(UnsignedIntegerFourBytes id, EventedValue ev)
  {
    InstanceID instanceID = null;
    for (InstanceID i : getInstanceIDs()) {
      if (i.getId().equals(id)) {
        instanceID = i;
      }
    }
    if (instanceID == null)
    {
      instanceID = new InstanceID(id);
      getInstanceIDs().add(instanceID);
    }
    Object it = instanceID.getValues().iterator();
    while (((Iterator)it).hasNext())
    {
      EventedValue existingEv = (EventedValue)((Iterator)it).next();
      if (existingEv.getClass().equals(ev.getClass())) {
        ((Iterator)it).remove();
      }
    }
    instanceID.getValues().add(ev);
  }
  
  public <EV extends EventedValue> EV getEventedValue(UnsignedIntegerFourBytes id, Class<EV> type)
  {
    for (InstanceID instanceID : getInstanceIDs()) {
      if (instanceID.getId().equals(id)) {
        for (EventedValue eventedValue : instanceID.getValues()) {
          if (eventedValue.getClass().equals(type)) {
            return eventedValue;
          }
        }
      }
    }
    return null;
  }
  
  public boolean hasChanges()
  {
    for (InstanceID instanceID : this.instanceIDs) {
      if (instanceID.getValues().size() > 0) {
        return true;
      }
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\lastchange\Event.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */