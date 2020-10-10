package org.fourthline.cling.model.action;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.profile.RemoteClientInfo;
import org.fourthline.cling.model.state.StateVariableAccessor;
import org.fourthline.cling.model.types.ErrorCode;
import org.seamless.util.Reflections;

public class MethodActionExecutor
  extends AbstractActionExecutor
{
  private static Logger log = Logger.getLogger(MethodActionExecutor.class.getName());
  protected Method method;
  
  public MethodActionExecutor(Method method)
  {
    this.method = method;
  }
  
  public MethodActionExecutor(Map<ActionArgument<LocalService>, StateVariableAccessor> outputArgumentAccessors, Method method)
  {
    super(outputArgumentAccessors);
    this.method = method;
  }
  
  public Method getMethod()
  {
    return this.method;
  }
  
  protected void execute(ActionInvocation<LocalService> actionInvocation, Object serviceImpl)
    throws Exception
  {
    Object[] inputArgumentValues = createInputArgumentValues(actionInvocation, this.method);
    if (!actionInvocation.getAction().hasOutputArguments())
    {
      log.fine("Calling local service method with no output arguments: " + this.method);
      Reflections.invoke(this.method, serviceImpl, inputArgumentValues);
      return;
    }
    boolean isVoid = this.method.getReturnType().equals(Void.TYPE);
    
    log.fine("Calling local service method with output arguments: " + this.method);
    
    boolean isArrayResultProcessed = true;
    Object result;
    Object result;
    if (isVoid)
    {
      log.fine("Action method is void, calling declared accessors(s) on service instance to retrieve ouput argument(s)");
      Reflections.invoke(this.method, serviceImpl, inputArgumentValues);
      result = readOutputArgumentValues(actionInvocation.getAction(), serviceImpl);
    }
    else
    {
      Object result;
      if (isUseOutputArgumentAccessors(actionInvocation))
      {
        log.fine("Action method is not void, calling declared accessor(s) on returned instance to retrieve ouput argument(s)");
        Object returnedInstance = Reflections.invoke(this.method, serviceImpl, inputArgumentValues);
        result = readOutputArgumentValues(actionInvocation.getAction(), returnedInstance);
      }
      else
      {
        log.fine("Action method is not void, using returned value as (single) output argument");
        result = Reflections.invoke(this.method, serviceImpl, inputArgumentValues);
        isArrayResultProcessed = false;
      }
    }
    ActionArgument<LocalService>[] outputArgs = actionInvocation.getAction().getOutputArguments();
    if ((isArrayResultProcessed) && ((result instanceof Object[])))
    {
      Object[] results = (Object[])result;
      log.fine("Accessors returned Object[], setting output argument values: " + results.length);
      for (int i = 0; i < outputArgs.length; i++) {
        setOutputArgumentValue(actionInvocation, outputArgs[i], results[i]);
      }
    }
    else if (outputArgs.length == 1)
    {
      setOutputArgumentValue(actionInvocation, outputArgs[0], result);
    }
    else
    {
      throw new ActionException(ErrorCode.ACTION_FAILED, "Method return does not match required number of output arguments: " + outputArgs.length);
    }
  }
  
  protected boolean isUseOutputArgumentAccessors(ActionInvocation<LocalService> actionInvocation)
  {
    for (ActionArgument argument : actionInvocation.getAction().getOutputArguments()) {
      if (getOutputArgumentAccessors().get(argument) != null) {
        return true;
      }
    }
    return false;
  }
  
  protected Object[] createInputArgumentValues(ActionInvocation<LocalService> actionInvocation, Method method)
    throws ActionException
  {
    LocalService service = (LocalService)actionInvocation.getAction().getService();
    
    List values = new ArrayList();
    int i = 0;
    for (ActionArgument<LocalService> argument : actionInvocation.getAction().getInputArguments())
    {
      Class methodParameterType = method.getParameterTypes()[i];
      
      ActionArgumentValue<LocalService> inputValue = actionInvocation.getInput(argument);
      if ((methodParameterType.isPrimitive()) && ((inputValue == null) || (inputValue.toString().length() == 0))) {
        throw new ActionException(ErrorCode.ARGUMENT_VALUE_INVALID, "Primitive action method argument '" + argument.getName() + "' requires input value, can't be null or empty string");
      }
      if (inputValue == null)
      {
        values.add(i++, null);
      }
      else
      {
        String inputCallValueString = inputValue.toString();
        if ((inputCallValueString.length() > 0) && (service.isStringConvertibleType(methodParameterType)) && (!methodParameterType.isEnum())) {
          try
          {
            Constructor<String> ctor = methodParameterType.getConstructor(new Class[] { String.class });
            log.finer("Creating new input argument value instance with String.class constructor of type: " + methodParameterType);
            Object o = ctor.newInstance(new Object[] { inputCallValueString });
            values.add(i++, o);
          }
          catch (Exception ex)
          {
            log.warning("Error preparing action method call: " + method);
            log.warning("Can't convert input argument string to desired type of '" + argument.getName() + "': " + ex);
            
            throw new ActionException(ErrorCode.ARGUMENT_VALUE_INVALID, "Can't convert input argument string to desired type of '" + argument.getName() + "': " + ex);
          }
        } else {
          values.add(i++, inputValue.getValue());
        }
      }
    }
    if ((method.getParameterTypes().length > 0) && 
      (RemoteClientInfo.class.isAssignableFrom(method.getParameterTypes()[(method.getParameterTypes().length - 1)]))) {
      if (((actionInvocation instanceof RemoteActionInvocation)) && 
        (((RemoteActionInvocation)actionInvocation).getRemoteClientInfo() != null))
      {
        log.finer("Providing remote client info as last action method input argument: " + method);
        values.add(i, ((RemoteActionInvocation)actionInvocation).getRemoteClientInfo());
      }
      else
      {
        values.add(i, null);
      }
    }
    return values.toArray(new Object[values.size()]);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\action\MethodActionExecutor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */