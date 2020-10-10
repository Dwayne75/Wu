package javax.xml.bind.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.PACKAGE})
public @interface XmlSchema
{
  public static final String NO_LOCATION = "##generate";
  
  XmlNs[] xmlns() default {};
  
  String namespace() default "";
  
  XmlNsForm elementFormDefault() default XmlNsForm.UNSET;
  
  XmlNsForm attributeFormDefault() default XmlNsForm.UNSET;
  
  String location() default "##generate";
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\bind\annotation\XmlSchema.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */