package com.sun.codemodel;

import java.lang.annotation.Annotation;

public abstract interface JAnnotatable
{
  public abstract JAnnotationUse annotate(JClass paramJClass);
  
  public abstract JAnnotationUse annotate(Class<? extends Annotation> paramClass);
  
  public abstract <W extends JAnnotationWriter> W annotate2(Class<W> paramClass);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JAnnotatable.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */