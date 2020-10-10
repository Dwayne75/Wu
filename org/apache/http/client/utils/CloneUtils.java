package org.apache.http.client.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.http.annotation.Immutable;

@Immutable
public class CloneUtils
{
  public static Object clone(Object obj)
    throws CloneNotSupportedException
  {
    if (obj == null) {
      return null;
    }
    if ((obj instanceof Cloneable))
    {
      Class<?> clazz = obj.getClass();
      Method m;
      try
      {
        m = clazz.getMethod("clone", (Class[])null);
      }
      catch (NoSuchMethodException ex)
      {
        throw new NoSuchMethodError(ex.getMessage());
      }
      try
      {
        return m.invoke(obj, (Object[])null);
      }
      catch (InvocationTargetException ex)
      {
        Throwable cause = ex.getCause();
        if ((cause instanceof CloneNotSupportedException)) {
          throw ((CloneNotSupportedException)cause);
        }
        throw new Error("Unexpected exception", cause);
      }
      catch (IllegalAccessException ex)
      {
        throw new IllegalAccessError(ex.getMessage());
      }
    }
    throw new CloneNotSupportedException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\client\utils\CloneUtils.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */