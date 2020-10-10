package com.sun.codemodel;

import java.lang.annotation.Annotation;

public abstract interface JAnnotationWriter<A extends Annotation>
{
  public abstract JAnnotationUse getAnnotationUse();
  
  public abstract Class<A> getAnnotationType();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JAnnotationWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */