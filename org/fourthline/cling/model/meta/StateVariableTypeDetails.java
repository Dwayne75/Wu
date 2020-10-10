package org.fourthline.cling.model.meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.fourthline.cling.model.Validatable;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.Datatype.Builtin;

public class StateVariableTypeDetails
  implements Validatable
{
  private static final Logger log = Logger.getLogger(StateVariableTypeDetails.class.getName());
  private final Datatype datatype;
  private final String defaultValue;
  private final String[] allowedValues;
  private final StateVariableAllowedValueRange allowedValueRange;
  
  public StateVariableTypeDetails(Datatype datatype)
  {
    this(datatype, null, null, null);
  }
  
  public StateVariableTypeDetails(Datatype datatype, String defaultValue)
  {
    this(datatype, defaultValue, null, null);
  }
  
  public StateVariableTypeDetails(Datatype datatype, String defaultValue, String[] allowedValues, StateVariableAllowedValueRange allowedValueRange)
  {
    this.datatype = datatype;
    this.defaultValue = defaultValue;
    this.allowedValues = allowedValues;
    this.allowedValueRange = allowedValueRange;
  }
  
  public Datatype getDatatype()
  {
    return this.datatype;
  }
  
  public String getDefaultValue()
  {
    return this.defaultValue;
  }
  
  public String[] getAllowedValues()
  {
    if (!foundDefaultInAllowedValues(this.defaultValue, this.allowedValues))
    {
      List<String> list = new ArrayList(Arrays.asList(this.allowedValues));
      list.add(getDefaultValue());
      return (String[])list.toArray(new String[list.size()]);
    }
    return this.allowedValues;
  }
  
  public StateVariableAllowedValueRange getAllowedValueRange()
  {
    return this.allowedValueRange;
  }
  
  protected boolean foundDefaultInAllowedValues(String defaultValue, String[] allowedValues)
  {
    if ((defaultValue == null) || (allowedValues == null)) {
      return true;
    }
    for (String s : allowedValues) {
      if (s.equals(defaultValue)) {
        return true;
      }
    }
    return false;
  }
  
  public List<ValidationError> validate()
  {
    List<ValidationError> errors = new ArrayList();
    if (getDatatype() == null) {
      errors.add(new ValidationError(
        getClass(), "datatype", "Service state variable has no datatype"));
    }
    if (getAllowedValues() != null)
    {
      if (getAllowedValueRange() != null) {
        errors.add(new ValidationError(
          getClass(), "allowedValues", "Allowed value list of state variable can not also be restricted with allowed value range"));
      }
      if (!Datatype.Builtin.STRING.equals(getDatatype().getBuiltin())) {
        errors.add(new ValidationError(
          getClass(), "allowedValues", "Allowed value list of state variable only available for string datatype, not: " + 
          
          getDatatype()));
      }
      for (String s : getAllowedValues()) {
        if (s.length() > 31) {
          log.warning("UPnP specification violation, allowed value string must be less than 32 chars: " + s);
        }
      }
      if (!foundDefaultInAllowedValues(this.defaultValue, this.allowedValues)) {
        log.warning("UPnP specification violation, allowed string values don't contain default value: " + this.defaultValue);
      }
    }
    if (getAllowedValueRange() != null) {
      errors.addAll(getAllowedValueRange().validate());
    }
    return errors;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\meta\StateVariableTypeDetails.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */