package org.fourthline.cling.model.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.Validatable;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.types.Datatype;

public class ActionArgument<S extends Service>
  implements Validatable
{
  private static final Logger log = Logger.getLogger(ActionArgument.class.getName());
  private final String name;
  private final String[] aliases;
  private final String relatedStateVariableName;
  private final Direction direction;
  private final boolean returnValue;
  private Action<S> action;
  
  public static enum Direction
  {
    IN,  OUT;
    
    private Direction() {}
  }
  
  public ActionArgument(String name, String relatedStateVariableName, Direction direction)
  {
    this(name, new String[0], relatedStateVariableName, direction, false);
  }
  
  public ActionArgument(String name, String[] aliases, String relatedStateVariableName, Direction direction)
  {
    this(name, aliases, relatedStateVariableName, direction, false);
  }
  
  public ActionArgument(String name, String relatedStateVariableName, Direction direction, boolean returnValue)
  {
    this(name, new String[0], relatedStateVariableName, direction, returnValue);
  }
  
  public ActionArgument(String name, String[] aliases, String relatedStateVariableName, Direction direction, boolean returnValue)
  {
    this.name = name;
    this.aliases = aliases;
    this.relatedStateVariableName = relatedStateVariableName;
    this.direction = direction;
    this.returnValue = returnValue;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public String[] getAliases()
  {
    return this.aliases;
  }
  
  public boolean isNameOrAlias(String name)
  {
    if (getName().equalsIgnoreCase(name)) {
      return true;
    }
    for (String alias : this.aliases) {
      if (alias.equalsIgnoreCase(name)) {
        return true;
      }
    }
    return false;
  }
  
  public String getRelatedStateVariableName()
  {
    return this.relatedStateVariableName;
  }
  
  public Direction getDirection()
  {
    return this.direction;
  }
  
  public boolean isReturnValue()
  {
    return this.returnValue;
  }
  
  public Action<S> getAction()
  {
    return this.action;
  }
  
  void setAction(Action<S> action)
  {
    if (this.action != null) {
      throw new IllegalStateException("Final value has been set already, model is immutable");
    }
    this.action = action;
  }
  
  public Datatype getDatatype()
  {
    return getAction().getService().getDatatype(this);
  }
  
  public List<ValidationError> validate()
  {
    List<ValidationError> errors = new ArrayList();
    if ((getName() == null) || (getName().length() == 0))
    {
      errors.add(new ValidationError(
        getClass(), "name", "Argument without name of: " + 
        
        getAction()));
    }
    else if (!ModelUtil.isValidUDAName(getName()))
    {
      log.warning("UPnP specification violation of: " + getAction().getService().getDevice());
      log.warning("Invalid argument name: " + this);
    }
    else if (getName().length() > 32)
    {
      log.warning("UPnP specification violation of: " + getAction().getService().getDevice());
      log.warning("Argument name should be less than 32 characters: " + this);
    }
    if (getDirection() == null) {
      errors.add(new ValidationError(
        getClass(), "direction", "Argument '" + 
        
        getName() + "' requires a direction, either IN or OUT"));
    }
    if ((isReturnValue()) && (getDirection() != Direction.OUT)) {
      errors.add(new ValidationError(
        getClass(), "direction", "Return value argument '" + 
        
        getName() + "' must be direction OUT"));
    }
    return errors;
  }
  
  public ActionArgument<S> deepCopy()
  {
    return new ActionArgument(getName(), getAliases(), getRelatedStateVariableName(), getDirection(), isReturnValue());
  }
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ", " + getDirection() + ") " + getName();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\meta\ActionArgument.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */