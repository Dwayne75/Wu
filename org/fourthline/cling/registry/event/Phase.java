package org.fourthline.cling.registry.event;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

public abstract interface Phase
{
  public static final AnnotationLiteral<Alive> ALIVE = new AnnotationLiteral() {};
  public static final AnnotationLiteral<Complete> COMPLETE = new AnnotationLiteral() {};
  public static final AnnotationLiteral<Byebye> BYEBYE = new AnnotationLiteral() {};
  public static final AnnotationLiteral<Updated> UPDATED = new AnnotationLiteral() {};
  
  @Qualifier
  @Target({java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.PARAMETER})
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Updated {}
  
  @Qualifier
  @Target({java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.PARAMETER})
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Byebye {}
  
  @Qualifier
  @Target({java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.PARAMETER})
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Complete {}
  
  @Qualifier
  @Target({java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.PARAMETER})
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Alive {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\registry\event\Phase.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */