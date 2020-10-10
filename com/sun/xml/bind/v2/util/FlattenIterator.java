package com.sun.xml.bind.v2.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public final class FlattenIterator<T>
  implements Iterator<T>
{
  private final Iterator<? extends Map<?, ? extends T>> parent;
  private Iterator<? extends T> child = null;
  private T next;
  
  public FlattenIterator(Iterable<? extends Map<?, ? extends T>> core)
  {
    this.parent = core.iterator();
  }
  
  public void remove()
  {
    throw new UnsupportedOperationException();
  }
  
  public boolean hasNext()
  {
    getNext();
    return this.next != null;
  }
  
  public T next()
  {
    T r = this.next;
    this.next = null;
    if (r == null) {
      throw new NoSuchElementException();
    }
    return r;
  }
  
  private void getNext()
  {
    if (this.next != null) {
      return;
    }
    if ((this.child != null) && (this.child.hasNext()))
    {
      this.next = this.child.next();
      return;
    }
    if (this.parent.hasNext())
    {
      this.child = ((Map)this.parent.next()).values().iterator();
      getNext();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\util\FlattenIterator.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */