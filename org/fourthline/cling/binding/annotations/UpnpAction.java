package org.fourthline.cling.binding.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({java.lang.annotation.ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UpnpAction
{
  String name() default "";
  
  UpnpOutputArgument[] out() default {};
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\binding\annotations\UpnpAction.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */