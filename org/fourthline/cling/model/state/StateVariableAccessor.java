package org.fourthline.cling.model.state;

import org.fourthline.cling.model.Command;
import org.fourthline.cling.model.ServiceManager;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.StateVariable;

public abstract class StateVariableAccessor
{
  public StateVariableValue read(final StateVariable<LocalService> stateVariable, final Object serviceImpl)
    throws Exception
  {
    Command cmd = new Command()
    {
      Object result;
      
      public void execute(ServiceManager serviceManager)
        throws Exception
      {
        this.result = this.this$0.read(serviceImpl);
        if (((LocalService)stateVariable.getService()).isStringConvertibleType(this.result)) {
          this.result = this.result.toString();
        }
      }
    };
    ((LocalService)stateVariable.getService()).getManager().execute(cmd);
    return new StateVariableValue(stateVariable, cmd.result);
  }
  
  public abstract Class<?> getReturnType();
  
  public abstract Object read(Object paramObject)
    throws Exception;
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ")";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\state\StateVariableAccessor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */