package org.fourthline.cling.model.action;

import org.fourthline.cling.model.VariableValue;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.InvalidValueException;

public class ActionArgumentValue<S extends Service>
  extends VariableValue
{
  private final ActionArgument<S> argument;
  
  public ActionArgumentValue(ActionArgument<S> argument, Object value)
    throws InvalidValueException
  {
    super(argument.getDatatype(), (value != null) && (value.getClass().isEnum()) ? value.toString() : value);
    this.argument = argument;
  }
  
  public ActionArgument<S> getArgument()
  {
    return this.argument;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\action\ActionArgumentValue.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */