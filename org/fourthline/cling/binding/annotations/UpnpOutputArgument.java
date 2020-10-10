package org.fourthline.cling.binding.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({java.lang.annotation.ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface UpnpOutputArgument
{
  String name();
  
  String stateVariable() default "";
  
  String getterName() default "";
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\binding\annotations\UpnpOutputArgument.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */