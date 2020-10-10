package com.sun.xml.bind.v2.model.annotation;

import java.lang.annotation.Annotation;

public abstract interface AnnotationSource
{
  public abstract <A extends Annotation> A readAnnotation(Class<A> paramClass);
  
  public abstract boolean hasAnnotation(Class<? extends Annotation> paramClass);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\annotation\AnnotationSource.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */