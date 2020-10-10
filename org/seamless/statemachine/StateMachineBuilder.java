package org.seamless.statemachine;

import java.lang.reflect.Proxy;
import java.util.Arrays;

public class StateMachineBuilder
{
  public static <T extends StateMachine> T build(Class<T> stateMachine, Class initialState)
  {
    return build(stateMachine, initialState, null, null);
  }
  
  public static <T extends StateMachine> T build(Class<T> stateMachine, Class initialState, Class[] constructorArgumentTypes, Object[] constructorArguments)
  {
    return (StateMachine)Proxy.newProxyInstance(stateMachine.getClassLoader(), new Class[] { stateMachine }, new StateMachineInvocationHandler(Arrays.asList(((States)stateMachine.getAnnotation(States.class)).value()), initialState, constructorArgumentTypes, constructorArguments));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\statemachine\StateMachineBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */