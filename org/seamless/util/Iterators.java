package org.seamless.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;

public class Iterators
{
  public static class Empty<E>
    implements Iterator<E>
  {
    public boolean hasNext()
    {
      return false;
    }
    
    public E next()
    {
      throw new NoSuchElementException();
    }
    
    public void remove()
    {
      throw new UnsupportedOperationException();
    }
  }
  
  public static class Singular<E>
    implements Iterator<E>
  {
    protected final E element;
    protected int current;
    
    public Singular(E element)
    {
      this.element = element;
    }
    
    public boolean hasNext()
    {
      return this.current == 0;
    }
    
    public E next()
    {
      this.current += 1;
      return (E)this.element;
    }
    
    public void remove()
    {
      throw new UnsupportedOperationException();
    }
  }
  
  public static abstract class Synchronized<E>
    implements Iterator<E>
  {
    final Iterator<E> wrapped;
    int nextIndex = 0;
    boolean removedCurrent = false;
    
    public Synchronized(Collection<E> collection)
    {
      this.wrapped = new CopyOnWriteArrayList(collection).iterator();
    }
    
    public boolean hasNext()
    {
      return this.wrapped.hasNext();
    }
    
    public E next()
    {
      this.removedCurrent = false;
      this.nextIndex += 1;
      return (E)this.wrapped.next();
    }
    
    public void remove()
    {
      if (this.nextIndex == 0) {
        throw new IllegalStateException("Call next() first");
      }
      if (this.removedCurrent) {
        throw new IllegalStateException("Already removed current, call next()");
      }
      synchronizedRemove(this.nextIndex - 1);
      this.removedCurrent = true;
    }
    
    protected abstract void synchronizedRemove(int paramInt);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\util\Iterators.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */