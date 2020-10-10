package org.seamless.statemachine;

public abstract interface StateMachine<S>
{
  public static final String METHOD_CURRENT_STATE = "getCurrentState";
  public static final String METHOD_FORCE_STATE = "forceState";
  
  public abstract S getCurrentState();
  
  public abstract void forceState(Class<? extends S> paramClass);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\statemachine\StateMachine.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */