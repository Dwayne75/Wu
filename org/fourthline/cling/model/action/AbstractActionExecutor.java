package org.fourthline.cling.model.action;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.Command;
import org.fourthline.cling.model.ServiceManager;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.state.StateVariableAccessor;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.InvalidValueException;
import org.seamless.util.Exceptions;

public abstract class AbstractActionExecutor
  implements ActionExecutor
{
  private static Logger log = Logger.getLogger(AbstractActionExecutor.class.getName());
  protected Map<ActionArgument<LocalService>, StateVariableAccessor> outputArgumentAccessors = new HashMap();
  
  protected AbstractActionExecutor() {}
  
  protected AbstractActionExecutor(Map<ActionArgument<LocalService>, StateVariableAccessor> outputArgumentAccessors)
  {
    this.outputArgumentAccessors = outputArgumentAccessors;
  }
  
  public Map<ActionArgument<LocalService>, StateVariableAccessor> getOutputArgumentAccessors()
  {
    return this.outputArgumentAccessors;
  }
  
  public void execute(final ActionInvocation<LocalService> actionInvocation)
  {
    log.fine("Invoking on local service: " + actionInvocation);
    
    LocalService service = (LocalService)actionInvocation.getAction().getService();
    try
    {
      if (service.getManager() == null) {
        throw new IllegalStateException("Service has no implementation factory, can't get service instance");
      }
      service.getManager().execute(new Command()
      {
        public void execute(ServiceManager serviceManager)
          throws Exception
        {
          AbstractActionExecutor.this.execute(actionInvocation, serviceManager
          
            .getImplementation());
        }
        
        public String toString()
        {
          return "Action invocation: " + actionInvocation.getAction();
        }
      });
    }
    catch (ActionException ex)
    {
      if (log.isLoggable(Level.FINE))
      {
        log.fine("ActionException thrown by service, wrapping in invocation and returning: " + ex);
        log.log(Level.FINE, "Exception root cause: ", Exceptions.unwrap(ex));
      }
      actionInvocation.setFailure(ex);
    }
    catch (InterruptedException ex)
    {
      if (log.isLoggable(Level.FINE))
      {
        log.fine("InterruptedException thrown by service, wrapping in invocation and returning: " + ex);
        log.log(Level.FINE, "Exception root cause: ", Exceptions.unwrap(ex));
      }
      actionInvocation.setFailure(new ActionCancelledException(ex));
    }
    catch (Throwable t)
    {
      Throwable rootCause = Exceptions.unwrap(t);
      if (log.isLoggable(Level.FINE))
      {
        log.fine("Execution has thrown, wrapping root cause in ActionException and returning: " + t);
        log.log(Level.FINE, "Exception root cause: ", rootCause);
      }
      actionInvocation.setFailure(new ActionException(ErrorCode.ACTION_FAILED, rootCause
      
        .getMessage() != null ? rootCause.getMessage() : rootCause.toString(), rootCause));
    }
  }
  
  protected abstract void execute(ActionInvocation<LocalService> paramActionInvocation, Object paramObject)
    throws Exception;
  
  protected Object readOutputArgumentValues(Action<LocalService> action, Object instance)
    throws Exception
  {
    Object[] results = new Object[action.getOutputArguments().length];
    log.fine("Attempting to retrieve output argument values using accessor: " + results.length);
    
    int i = 0;
    for (ActionArgument outputArgument : action.getOutputArguments())
    {
      log.finer("Calling acccessor method for: " + outputArgument);
      
      StateVariableAccessor accessor = (StateVariableAccessor)getOutputArgumentAccessors().get(outputArgument);
      if (accessor != null)
      {
        log.fine("Calling accessor to read output argument value: " + accessor);
        results[(i++)] = accessor.read(instance);
      }
      else
      {
        throw new IllegalStateException("No accessor bound for: " + outputArgument);
      }
    }
    if (results.length == 1) {
      return results[0];
    }
    return results.length > 0 ? results : null;
  }
  
  protected void setOutputArgumentValue(ActionInvocation<LocalService> actionInvocation, ActionArgument<LocalService> argument, Object result)
    throws ActionException
  {
    LocalService service = (LocalService)actionInvocation.getAction().getService();
    if (result != null) {
      try
      {
        if (service.isStringConvertibleType(result))
        {
          log.fine("Result of invocation matches convertible type, setting toString() single output argument value");
          actionInvocation.setOutput(new ActionArgumentValue(argument, result.toString()));
        }
        else
        {
          log.fine("Result of invocation is Object, setting single output argument value");
          actionInvocation.setOutput(new ActionArgumentValue(argument, result));
        }
      }
      catch (InvalidValueException ex)
      {
        throw new ActionException(ErrorCode.ARGUMENT_VALUE_INVALID, "Wrong type or invalid value for '" + argument.getName() + "': " + ex.getMessage(), ex);
      }
    } else {
      log.fine("Result of invocation is null, not setting any output argument value(s)");
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\action\AbstractActionExecutor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */