package org.fourthline.cling.binding.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface UpnpService
{
  UpnpServiceId serviceId();
  
  UpnpServiceType serviceType();
  
  boolean supportsQueryStateVariables() default true;
  
  Class[] stringConvertibleTypes() default {};
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\binding\annotations\UpnpService.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */