package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.Iterator;

@GwtCompatible
public abstract class ForwardingIterator<T>
  extends ForwardingObject
  implements Iterator<T>
{
  protected abstract Iterator<T> delegate();
  
  public boolean hasNext()
  {
    return delegate().hasNext();
  }
  
  public T next()
  {
    return (T)delegate().next();
  }
  
  public void remove()
  {
    delegate().remove();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\collect\ForwardingIterator.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */