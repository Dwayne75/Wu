package org.fourthline.cling.model.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.Validatable;
import org.fourthline.cling.model.ValidationError;

public class Action<S extends Service>
  implements Validatable
{
  private static final Logger log = Logger.getLogger(Action.class.getName());
  private final String name;
  private final ActionArgument[] arguments;
  private final ActionArgument[] inputArguments;
  private final ActionArgument[] outputArguments;
  private S service;
  
  public Action(String name, ActionArgument[] arguments)
  {
    this.name = name;
    if (arguments != null)
    {
      List<ActionArgument> inputList = new ArrayList();
      List<ActionArgument> outputList = new ArrayList();
      for (ActionArgument argument : arguments)
      {
        argument.setAction(this);
        if (argument.getDirection().equals(ActionArgument.Direction.IN)) {
          inputList.add(argument);
        }
        if (argument.getDirection().equals(ActionArgument.Direction.OUT)) {
          outputList.add(argument);
        }
      }
      this.arguments = arguments;
      this.inputArguments = ((ActionArgument[])inputList.toArray(new ActionArgument[inputList.size()]));
      this.outputArguments = ((ActionArgument[])outputList.toArray(new ActionArgument[outputList.size()]));
    }
    else
    {
      this.arguments = new ActionArgument[0];
      this.inputArguments = new ActionArgument[0];
      this.outputArguments = new ActionArgument[0];
    }
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public boolean hasArguments()
  {
    return (getArguments() != null) && (getArguments().length > 0);
  }
  
  public ActionArgument[] getArguments()
  {
    return this.arguments;
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
  
  public ActionArgument<S> getFirstInputArgument()
  {
    if (!hasInputArguments()) {
      throw new IllegalStateException("No input arguments: " + this);
    }
    return getInputArguments()[0];
  }
  
  public ActionArgument<S> getFirstOutputArgument()
  {
    if (!hasOutputArguments()) {
      throw new IllegalStateException("No output arguments: " + this);
    }
    return getOutputArguments()[0];
  }
  
  public ActionArgument<S>[] getInputArguments()
  {
    return this.inputArguments;
  }
  
  public ActionArgument<S> getInputArgument(String name)
  {
    for (ActionArgument<S> arg : getInputArguments()) {
      if (arg.isNameOrAlias(name)) {
        return arg;
      }
    }
    return null;
  }
  
  public ActionArgument<S>[] getOutputArguments()
  {
    return this.outputArguments;
  }
  
  public ActionArgument<S> getOutputArgument(String name)
  {
    for (ActionArgument<S> arg : getOutputArguments()) {
      if (arg.getName().equals(name)) {
        return arg;
      }
    }
    return null;
  }
  
  public boolean hasInputArguments()
  {
    return (getInputArguments() != null) && (getInputArguments().length > 0);
  }
  
  public boolean hasOutputArguments()
  {
    return (getOutputArguments() != null) && (getOutputArguments().length > 0);
  }
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ", Arguments: " + (getArguments() != null ? Integer.valueOf(getArguments().length) : "NO ARGS") + ") " + getName();
  }
  
  public List<ValidationError> validate()
  {
    List<ValidationError> errors = new ArrayList();
    if ((getName() == null) || (getName().length() == 0))
    {
      errors.add(new ValidationError(
        getClass(), "name", "Action without name of: " + 
        
        getService()));
    }
    else if (!ModelUtil.isValidUDAName(getName()))
    {
      log.warning("UPnP specification violation of: " + getService().getDevice());
      log.warning("Invalid action name: " + this);
    }
    ActionArgument actionArgument;
    for (actionArgument : getArguments()) {
      if (getService().getStateVariable(actionArgument.getRelatedStateVariableName()) == null) {
        errors.add(new ValidationError(
          getClass(), "arguments", "Action argument references an unknown state variable: " + actionArgument
          
          .getRelatedStateVariableName()));
      }
    }
    ActionArgument retValueArgument = null;
    int retValueArgumentIndex = 0;
    int i = 0;
    for (ActionArgument actionArgument : getArguments())
    {
      if (actionArgument.isReturnValue()) {
        if (actionArgument.getDirection() == ActionArgument.Direction.IN)
        {
          log.warning("UPnP specification violation of :" + getService().getDevice());
          log.warning("Input argument can not have <retval/>");
        }
        else
        {
          if (retValueArgument != null)
          {
            log.warning("UPnP specification violation of: " + getService().getDevice());
            log.warning("Only one argument of action '" + getName() + "' can be <retval/>");
          }
          retValueArgument = actionArgument;
          retValueArgumentIndex = i;
        }
      }
      i++;
    }
    int j;
    ActionArgument a;
    if (retValueArgument != null) {
      for (j = 0; j < retValueArgumentIndex; j++)
      {
        a = getArguments()[j];
        if (a.getDirection() == ActionArgument.Direction.OUT)
        {
          log.warning("UPnP specification violation of: " + getService().getDevice());
          log.warning("Argument '" + retValueArgument.getName() + "' of action '" + getName() + "' is <retval/> but not the first OUT argument");
        }
      }
    }
    for (ActionArgument argument : this.arguments) {
      errors.addAll(argument.validate());
    }
    return errors;
  }
  
  public Action<S> deepCopy()
  {
    ActionArgument<S>[] actionArgumentsDupe = new ActionArgument[getArguments().length];
    for (int i = 0; i < getArguments().length; i++)
    {
      ActionArgument arg = getArguments()[i];
      actionArgumentsDupe[i] = arg.deepCopy();
    }
    return new Action(getName(), actionArgumentsDupe);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\meta\Action.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */