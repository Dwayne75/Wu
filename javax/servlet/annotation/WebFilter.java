package javax.servlet.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.servlet.DispatcherType;

@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebFilter
{
  String description() default "";
  
  String displayName() default "";
  
  WebInitParam[] initParams() default {};
  
  String filterName() default "";
  
  String smallIcon() default "";
  
  String largeIcon() default "";
  
  String[] servletNames() default {};
  
  String[] value() default {};
  
  String[] urlPatterns() default {};
  
  DispatcherType[] dispatcherTypes() default {DispatcherType.REQUEST};
  
  boolean asyncSupported() default false;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\annotation\WebFilter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */