package org.jetbrains.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public abstract interface Async
{
  @Retention(RetentionPolicy.CLASS)
  @Target({java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.CONSTRUCTOR, java.lang.annotation.ElementType.PARAMETER})
  public static @interface Execute {}
  
  @Retention(RetentionPolicy.CLASS)
  @Target({java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.CONSTRUCTOR, java.lang.annotation.ElementType.PARAMETER})
  public static @interface Schedule {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\jetbrains\annotations\Async.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */