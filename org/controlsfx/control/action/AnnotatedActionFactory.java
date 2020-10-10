package org.controlsfx.control.action;

import java.lang.reflect.Method;

public abstract interface AnnotatedActionFactory
{
  public abstract AnnotatedAction createAction(ActionProxy paramActionProxy, Method paramMethod, Object paramObject);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\action\AnnotatedActionFactory.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */