package javax.xml.bind.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.PACKAGE})
public @interface XmlSchemaType
{
  String name();
  
  String namespace() default "http://www.w3.org/2001/XMLSchema";
  
  Class type() default DEFAULT.class;
  
  public static final class DEFAULT {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\bind\annotation\XmlSchemaType.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */