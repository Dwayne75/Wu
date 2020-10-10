package org.seamless.statemachine;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({java.lang.annotation.ElementType.TYPE})
public @interface States
{
  Class<?>[] value();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\statemachine\States.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */