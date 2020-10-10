package org.fourthline.cling.model.state;

import org.fourthline.cling.model.VariableValue;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.meta.StateVariableTypeDetails;
import org.fourthline.cling.model.types.InvalidValueException;

public class StateVariableValue<S extends Service>
  extends VariableValue
{
  private StateVariable<S> stateVariable;
  
  public StateVariableValue(StateVariable<S> stateVariable, Object value)
    throws InvalidValueException
  {
    super(stateVariable.getTypeDetails().getDatatype(), value);
    this.stateVariable = stateVariable;
  }
  
  public StateVariable<S> getStateVariable()
  {
    return this.stateVariable;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\state\StateVariableValue.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */