package org.fourthline.cling.binding.staging;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.ServiceType;

public class MutableService
{
  public ServiceType serviceType;
  public ServiceId serviceId;
  public URI descriptorURI;
  public URI controlURI;
  public URI eventSubscriptionURI;
  public List<MutableAction> actions = new ArrayList();
  public List<MutableStateVariable> stateVariables = new ArrayList();
  
  public Service build(Device prototype)
    throws ValidationException
  {
    return prototype.newInstance(this.serviceType, this.serviceId, this.descriptorURI, this.controlURI, this.eventSubscriptionURI, 
    
      createActions(), 
      createStateVariables());
  }
  
  public Action[] createActions()
  {
    Action[] array = new Action[this.actions.size()];
    int i = 0;
    for (MutableAction action : this.actions) {
      array[(i++)] = action.build();
    }
    return array;
  }
  
  public StateVariable[] createStateVariables()
  {
    StateVariable[] array = new StateVariable[this.stateVariables.size()];
    int i = 0;
    for (MutableStateVariable stateVariable : this.stateVariables) {
      array[(i++)] = stateVariable.build();
    }
    return array;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\binding\staging\MutableService.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */