package org.controlsfx.control.action;

import java.lang.reflect.Method;

@ActionCheck
public class AnnotatedCheckAction
  extends AnnotatedAction
{
  public AnnotatedCheckAction(String text, Method method, Object target)
  {
    super(text, method, target);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\action\AnnotatedCheckAction.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */