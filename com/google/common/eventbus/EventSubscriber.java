package com.google.common.eventbus;

import com.google.common.base.Preconditions;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.annotation.Nullable;

class EventSubscriber
{
  private final Object target;
  private final Method method;
  
  EventSubscriber(Object target, Method method)
  {
    Preconditions.checkNotNull(target, "EventSubscriber target cannot be null.");
    
    Preconditions.checkNotNull(method, "EventSubscriber method cannot be null.");
    
    this.target = target;
    this.method = method;
    method.setAccessible(true);
  }
  
  public void handleEvent(Object event)
    throws InvocationTargetException
  {
    Preconditions.checkNotNull(event);
    try
    {
      this.method.invoke(this.target, new Object[] { event });
    }
    catch (IllegalArgumentException e)
    {
      str = String.valueOf(String.valueOf(event));throw new Error(33 + str.length() + "Method rejected target/argument: " + str, e);
    }
    catch (IllegalAccessException e)
    {
      String str = String.valueOf(String.valueOf(event));throw new Error(28 + str.length() + "Method became inaccessible: " + str, e);
    }
    catch (InvocationTargetException e)
    {
      if ((e.getCause() instanceof Error)) {
        throw ((Error)e.getCause());
      }
      throw e;
    }
  }
  
  public String toString()
  {
    String str = String.valueOf(String.valueOf(this.method));return 10 + str.length() + "[wrapper " + str + "]";
  }
  
  public int hashCode()
  {
    int PRIME = 31;
    return (31 + this.method.hashCode()) * 31 + System.identityHashCode(this.target);
  }
  
  public boolean equals(@Nullable Object obj)
  {
    if ((obj instanceof EventSubscriber))
    {
      EventSubscriber that = (EventSubscriber)obj;
      
      return (this.target == that.target) && (this.method.equals(that.method));
    }
    return false;
  }
  
  public Object getSubscriber()
  {
    return this.target;
  }
  
  public Method getMethod()
  {
    return this.method;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\eventbus\EventSubscriber.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */