package org.controlsfx.control.action;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import javafx.event.ActionEvent;

public class ActionMap
{
  private static AnnotatedActionFactory actionFactory = new DefaultActionFactory();
  private static final Map<String, AnnotatedAction> actions = new HashMap();
  
  public static AnnotatedActionFactory getActionFactory()
  {
    return actionFactory;
  }
  
  public static void setActionFactory(AnnotatedActionFactory factory)
  {
    Objects.requireNonNull(factory);
    actionFactory = factory;
  }
  
  public static void register(Object target)
  {
    for (Method method : target.getClass().getDeclaredMethods())
    {
      Annotation[] annotations = method.getAnnotationsByType(ActionProxy.class);
      if (annotations.length != 0)
      {
        int paramCount = method.getParameterCount();
        Class[] paramTypes = method.getParameterTypes();
        if (paramCount > 2) {
          throw new IllegalArgumentException(String.format("Method %s has too many parameters", new Object[] { method.getName() }));
        }
        if ((paramCount == 1) && (!ActionEvent.class.isAssignableFrom(paramTypes[0]))) {
          throw new IllegalArgumentException(String.format("Method %s -- single parameter must be of type ActionEvent", new Object[] { method.getName() }));
        }
        if ((paramCount == 2) && ((!ActionEvent.class.isAssignableFrom(paramTypes[0])) || 
          (!Action.class.isAssignableFrom(paramTypes[1])))) {
          throw new IllegalArgumentException(String.format("Method %s -- parameters must be of types (ActionEvent, Action)", new Object[] { method.getName() }));
        }
        ActionProxy annotation = (ActionProxy)annotations[0];
        
        AnnotatedActionFactory factory = determineActionFactory(annotation);
        AnnotatedAction action = factory.createAction(annotation, method, target);
        
        String id = annotation.id().isEmpty() ? method.getName() : annotation.id();
        actions.put(id, action);
      }
    }
  }
  
  private static AnnotatedActionFactory determineActionFactory(ActionProxy annotation)
  {
    AnnotatedActionFactory factory = actionFactory;
    
    String factoryClassName = annotation.factory();
    if (!factoryClassName.isEmpty()) {
      try
      {
        Class factoryClass = Class.forName(factoryClassName);
        factory = (AnnotatedActionFactory)factoryClass.newInstance();
      }
      catch (ClassNotFoundException ex)
      {
        throw new IllegalArgumentException(String.format("Action proxy refers to non-existant factory class %s", new Object[] { factoryClassName }), ex);
      }
      catch (InstantiationException|IllegalAccessException ex)
      {
        throw new IllegalStateException(String.format("Unable to instantiate action factory class %s", new Object[] { factoryClassName }), ex);
      }
    }
    return factory;
  }
  
  public static void unregister(Object target)
  {
    if (target != null)
    {
      Iterator<Map.Entry<String, AnnotatedAction>> entryIter = actions.entrySet().iterator();
      while (entryIter.hasNext())
      {
        Map.Entry<String, AnnotatedAction> entry = (Map.Entry)entryIter.next();
        
        Object actionTarget = ((AnnotatedAction)entry.getValue()).getTarget();
        if ((actionTarget == null) || (actionTarget == target)) {
          entryIter.remove();
        }
      }
    }
  }
  
  public static Action action(String id)
  {
    return (Action)actions.get(id);
  }
  
  public static Collection<Action> actions(String... ids)
  {
    List<Action> result = new ArrayList();
    for (String id : ids)
    {
      if (id.startsWith("---")) {
        result.add(ActionUtils.ACTION_SEPARATOR);
      }
      Action action = action(id);
      if (action != null) {
        result.add(action);
      }
    }
    return result;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\action\ActionMap.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */