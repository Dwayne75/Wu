package org.fourthline.cling.binding.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface UpnpServiceType
{
  String namespace() default "schemas-upnp-org";
  
  String value();
  
  int version() default 1;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\binding\annotations\UpnpServiceType.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */