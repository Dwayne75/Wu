package org.fourthline.cling.model.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.Validatable;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.Datatype.Builtin;

public class StateVariable<S extends Service>
  implements Validatable
{
  private static final Logger log = Logger.getLogger(StateVariable.class.getName());
  private final String name;
  private final StateVariableTypeDetails type;
  private final StateVariableEventDetails eventDetails;
  private S service;
  
  public StateVariable(String name, StateVariableTypeDetails type)
  {
    this(name, type, new StateVariableEventDetails());
  }
  
  public StateVariable(String name, StateVariableTypeDetails type, StateVariableEventDetails eventDetails)
  {
    this.name = name;
    this.type = type;
    this.eventDetails = eventDetails;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public StateVariableTypeDetails getTypeDetails()
  {
    return this.type;
  }
  
  public StateVariableEventDetails getEventDetails()
  {
    return this.eventDetails;
  }
  
  public S getService()
  {
    return this.service;
  }
  
  void setService(S service)
  {
    if (this.service != null) {
      throw new IllegalStateException("Final value has been set already, model is immutable");
    }
    this.service = service;
  }
  
  public List<ValidationError> validate()
  {
    List<ValidationError> errors = new ArrayList();
    if ((getName() == null) || (getName().length() == 0))
    {
      errors.add(new ValidationError(
        getClass(), "name", "StateVariable without name of: " + 
        
        getService()));
    }
    else if (!ModelUtil.isValidUDAName(getName()))
    {
      log.warning("UPnP specification violation of: " + getService().getDevice());
      log.warning("Invalid state variable name: " + this);
    }
    errors.addAll(getTypeDetails().validate());
    
    return errors;
  }
  
  public boolean isModeratedNumericType()
  {
    if (Datatype.Builtin.isNumeric(
      getTypeDetails().getDatatype().getBuiltin())) {}
    return getEventDetails().getEventMinimumDelta() > 0;
  }
  
  public StateVariable<S> deepCopy()
  {
    return new StateVariable(getName(), getTypeDetails(), getEventDetails());
  }
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("(").append(getClass().getSimpleName());
    sb.append(", Name: ").append(getName());
    sb.append(", Type: ").append(getTypeDetails().getDatatype().getDisplayString()).append(")");
    if (!getEventDetails().isSendEvents()) {
      sb.append(" (No Events)");
    }
    if (getTypeDetails().getDefaultValue() != null) {
      sb.append(" Default Value: ").append("'").append(getTypeDetails().getDefaultValue()).append("'");
    }
    if (getTypeDetails().getAllowedValues() != null)
    {
      sb.append(" Allowed Values: ");
      for (String s : getTypeDetails().getAllowedValues()) {
        sb.append(s).append("|");
      }
    }
    return sb.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\meta\StateVariable.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */