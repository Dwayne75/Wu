package org.fourthline.cling.model;

import java.beans.PropertyChangeSupport;
import java.util.Collection;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.state.StateVariableValue;

public abstract interface ServiceManager<T>
{
  public static final String EVENTED_STATE_VARIABLES = "_EventedStateVariables";
  
  public abstract LocalService<T> getService();
  
  public abstract T getImplementation();
  
  public abstract void execute(Command<T> paramCommand)
    throws Exception;
  
  public abstract PropertyChangeSupport getPropertyChangeSupport();
  
  public abstract Collection<StateVariableValue> getCurrentState()
    throws Exception;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\ServiceManager.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */