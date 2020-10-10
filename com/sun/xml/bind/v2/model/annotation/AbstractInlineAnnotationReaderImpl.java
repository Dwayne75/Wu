package com.sun.xml.bind.v2.model.annotation;

import com.sun.xml.bind.v2.model.core.ErrorHandler;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import java.lang.annotation.Annotation;

public abstract class AbstractInlineAnnotationReaderImpl<T, C, F, M>
  implements AnnotationReader<T, C, F, M>
{
  private ErrorHandler errorHandler;
  
  public void setErrorHandler(ErrorHandler errorHandler)
  {
    if (errorHandler == null) {
      throw new IllegalArgumentException();
    }
    this.errorHandler = errorHandler;
  }
  
  public final ErrorHandler getErrorHandler()
  {
    assert (this.errorHandler != null) : "error handler must be set before use";
    return this.errorHandler;
  }
  
  public final <A extends Annotation> A getMethodAnnotation(Class<A> annotation, M getter, M setter, Locatable srcPos)
  {
    A a1 = getter == null ? null : getMethodAnnotation(annotation, getter, srcPos);
    A a2 = setter == null ? null : getMethodAnnotation(annotation, setter, srcPos);
    if (a1 == null)
    {
      if (a2 == null) {
        return null;
      }
      return a2;
    }
    if (a2 == null) {
      return a1;
    }
    getErrorHandler().error(new IllegalAnnotationException(Messages.DUPLICATE_ANNOTATIONS.format(new Object[] { annotation.getName(), fullName(getter), fullName(setter) }), a1, a2));
    
    return a1;
  }
  
  public boolean hasMethodAnnotation(Class<? extends Annotation> annotation, String propertyName, M getter, M setter, Locatable srcPos)
  {
    boolean x = (getter != null) && (hasMethodAnnotation(annotation, getter));
    boolean y = (setter != null) && (hasMethodAnnotation(annotation, setter));
    if ((x) && (y)) {
      getMethodAnnotation(annotation, getter, setter, srcPos);
    }
    return (x) || (y);
  }
  
  protected abstract String fullName(M paramM);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\annotation\AbstractInlineAnnotationReaderImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */