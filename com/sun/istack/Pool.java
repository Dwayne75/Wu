package com.sun.istack;

import java.util.concurrent.ConcurrentLinkedQueue;

public abstract interface Pool<T>
{
  @NotNull
  public abstract T take();
  
  public abstract void recycle(@NotNull T paramT);
  
  public static abstract class Impl<T>
    extends ConcurrentLinkedQueue<T>
    implements Pool<T>
  {
    @NotNull
    public final T take()
    {
      T t = super.poll();
      if (t == null) {
        return (T)create();
      }
      return t;
    }
    
    public final void recycle(T t)
    {
      super.offer(t);
    }
    
    @NotNull
    protected abstract T create();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\istack\Pool.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */