package org.fourthline.cling.model.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.fourthline.cling.model.ServiceReference;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.Datatype.Builtin;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.ServiceType;

public abstract class Service<D extends Device, S extends Service>
{
  private static final Logger log = Logger.getLogger(Service.class.getName());
  private final ServiceType serviceType;
  private final ServiceId serviceId;
  private final Map<String, Action> actions = new HashMap();
  private final Map<String, StateVariable> stateVariables = new HashMap();
  private D device;
  
  public Service(ServiceType serviceType, ServiceId serviceId)
    throws ValidationException
  {
    this(serviceType, serviceId, null, null);
  }
  
  public Service(ServiceType serviceType, ServiceId serviceId, Action<S>[] actions, StateVariable<S>[] stateVariables)
    throws ValidationException
  {
    this.serviceType = serviceType;
    this.serviceId = serviceId;
    if (actions != null) {
      for (Action action : actions)
      {
        this.actions.put(action.getName(), action);
        action.setService(this);
      }
    }
    if (stateVariables != null) {
      for (StateVariable stateVariable : stateVariables)
      {
        this.stateVariables.put(stateVariable.getName(), stateVariable);
        stateVariable.setService(this);
      }
    }
  }
  
  public ServiceType getServiceType()
  {
    return this.serviceType;
  }
  
  public ServiceId getServiceId()
  {
    return this.serviceId;
  }
  
  public boolean hasActions()
  {
    return (getActions() != null) && (getActions().length > 0);
  }
  
  public Action<S>[] getActions()
  {
    return this.actions == null ? null : (Action[])this.actions.values().toArray(new Action[this.actions.values().size()]);
  }
  
  public boolean hasStateVariables()
  {
    return (getStateVariables() != null) && (getStateVariables().length > 0);
  }
  
  public StateVariable<S>[] getStateVariables()
  {
    return this.stateVariables == null ? null : (StateVariable[])this.stateVariables.values().toArray(new StateVariable[this.stateVariables.values().size()]);
  }
  
  public D getDevice()
  {
    return this.device;
  }
  
  void setDevice(D device)
  {
    if (this.device != null) {
      throw new IllegalStateException("Final value has been set already, model is immutable");
    }
    this.device = device;
  }
  
  public Action<S> getAction(String name)
  {
    return this.actions == null ? null : (Action)this.actions.get(name);
  }
  
  public StateVariable<S> getStateVariable(String name)
  {
    if ("VirtualQueryActionInput".equals(name)) {
      return new StateVariable("VirtualQueryActionInput", new StateVariableTypeDetails(Datatype.Builtin.STRING.getDatatype()));
    }
    if ("VirtualQueryActionOutput".equals(name)) {
      return new StateVariable("VirtualQueryActionOutput", new StateVariableTypeDetails(Datatype.Builtin.STRING.getDatatype()));
    }
    return this.stateVariables == null ? null : (StateVariable)this.stateVariables.get(name);
  }
  
  public StateVariable<S> getRelatedStateVariable(ActionArgument argument)
  {
    return getStateVariable(argument.getRelatedStateVariableName());
  }
  
  public Datatype<S> getDatatype(ActionArgument argument)
  {
    return getRelatedStateVariable(argument).getTypeDetails().getDatatype();
  }
  
  public ServiceReference getReference()
  {
    return new ServiceReference(getDevice().getIdentity().getUdn(), getServiceId());
  }
  
  public List<ValidationError> validate()
  {
    List<ValidationError> errors = new ArrayList();
    if (getServiceType() == null) {
      errors.add(new ValidationError(
        getClass(), "serviceType", "Service type/info is required"));
    }
    if (getServiceId() == null) {
      errors.add(new ValidationError(
        getClass(), "serviceId", "Service ID is required"));
    }
    if (hasStateVariables()) {
      for (StateVariable stateVariable : getStateVariables()) {
        errors.addAll(stateVariable.validate());
      }
    }
    if (hasActions())
    {
      Action action;
      for (action : getActions())
      {
        List<ValidationError> actionErrors = action.validate();
        if (actionErrors.size() > 0)
        {
          this.actions.remove(action.getName());
          log.warning("Discarding invalid action of service '" + getServiceId() + "': " + action.getName());
          for (ValidationError actionError : actionErrors) {
            log.warning("Invalid action '" + action.getName() + "': " + actionError);
          }
        }
      }
    }
    return errors;
  }
  
  public abstract Action getQueryStateVariableAction();
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ") ServiceId: " + getServiceId();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\meta\Service.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */