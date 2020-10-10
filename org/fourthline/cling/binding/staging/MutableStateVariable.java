package org.fourthline.cling.binding.staging;

import java.util.List;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.meta.StateVariableAllowedValueRange;
import org.fourthline.cling.model.meta.StateVariableEventDetails;
import org.fourthline.cling.model.meta.StateVariableTypeDetails;
import org.fourthline.cling.model.types.Datatype;

public class MutableStateVariable
{
  public String name;
  public Datatype dataType;
  public String defaultValue;
  public List<String> allowedValues;
  public MutableAllowedValueRange allowedValueRange;
  public StateVariableEventDetails eventDetails;
  
  public StateVariable build()
  {
    return new StateVariable(this.name, new StateVariableTypeDetails(this.dataType, this.defaultValue, (this.allowedValues == null) || (this.allowedValues.size() == 0) ? null : (String[])this.allowedValues.toArray(new String[this.allowedValues.size()]), this.allowedValueRange == null ? null : new StateVariableAllowedValueRange(this.allowedValueRange.minimum.longValue(), this.allowedValueRange.maximum.longValue(), this.allowedValueRange.step.longValue())), this.eventDetails);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\binding\staging\MutableStateVariable.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */