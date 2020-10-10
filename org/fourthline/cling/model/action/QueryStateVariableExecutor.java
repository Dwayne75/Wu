package org.fourthline.cling.model.action;

import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.QueryStateVariableAction;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.state.StateVariableAccessor;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.model.types.ErrorCode;

public class QueryStateVariableExecutor
  extends AbstractActionExecutor
{
  protected void execute(ActionInvocation<LocalService> actionInvocation, Object serviceImpl)
    throws Exception
  {
    if ((actionInvocation.getAction() instanceof QueryStateVariableAction))
    {
      if (!((LocalService)actionInvocation.getAction().getService()).isSupportsQueryStateVariables()) {
        actionInvocation.setFailure(new ActionException(ErrorCode.INVALID_ACTION, "This service does not support querying state variables"));
      } else {
        executeQueryStateVariable(actionInvocation, serviceImpl);
      }
    }
    else {
      throw new IllegalStateException("This class can only execute QueryStateVariableAction's, not: " + actionInvocation.getAction());
    }
  }
  
  protected void executeQueryStateVariable(ActionInvocation<LocalService> actionInvocation, Object serviceImpl)
    throws Exception
  {
    LocalService service = (LocalService)actionInvocation.getAction().getService();
    
    String stateVariableName = actionInvocation.getInput("varName").toString();
    StateVariable stateVariable = service.getStateVariable(stateVariableName);
    if (stateVariable == null) {
      throw new ActionException(ErrorCode.ARGUMENT_VALUE_INVALID, "No state variable found: " + stateVariableName);
    }
    StateVariableAccessor accessor;
    if ((accessor = service.getAccessor(stateVariable.getName())) == null) {
      throw new ActionException(ErrorCode.ARGUMENT_VALUE_INVALID, "No accessor for state variable, can't read state: " + stateVariableName);
    }
    try
    {
      setOutputArgumentValue(actionInvocation, actionInvocation
      
        .getAction().getOutputArgument("return"), accessor
        .read(stateVariable, serviceImpl).toString());
    }
    catch (Exception ex)
    {
      throw new ActionException(ErrorCode.ACTION_FAILED, ex.getMessage());
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\action\QueryStateVariableExecutor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */