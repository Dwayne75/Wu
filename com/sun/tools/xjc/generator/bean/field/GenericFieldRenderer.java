package com.sun.tools.xjc.generator.bean.field;

import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.FieldOutline;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class GenericFieldRenderer
  implements FieldRenderer
{
  private Constructor constructor;
  
  public GenericFieldRenderer(Class fieldClass)
  {
    try
    {
      this.constructor = fieldClass.getDeclaredConstructor(new Class[] { ClassOutlineImpl.class, CPropertyInfo.class });
    }
    catch (NoSuchMethodException e)
    {
      throw new NoSuchMethodError(e.getMessage());
    }
  }
  
  public FieldOutline generate(ClassOutlineImpl context, CPropertyInfo prop)
  {
    try
    {
      return (FieldOutline)this.constructor.newInstance(new Object[] { context, prop });
    }
    catch (InstantiationException e)
    {
      throw new InstantiationError(e.getMessage());
    }
    catch (IllegalAccessException e)
    {
      throw new IllegalAccessError(e.getMessage());
    }
    catch (InvocationTargetException e)
    {
      Throwable t = e.getTargetException();
      if ((t instanceof RuntimeException)) {
        throw ((RuntimeException)t);
      }
      if ((t instanceof Error)) {
        throw ((Error)t);
      }
      throw new AssertionError(t);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\field\GenericFieldRenderer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */