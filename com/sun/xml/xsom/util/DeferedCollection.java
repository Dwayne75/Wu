package com.sun.xml.xsom.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class DeferedCollection<T>
  implements Collection<T>
{
  private final Iterator<T> result;
  private final List<T> archive = new ArrayList();
  
  public DeferedCollection(Iterator<T> result)
  {
    this.result = result;
  }
  
  public boolean isEmpty()
  {
    if (this.archive.isEmpty()) {
      fetch();
    }
    return this.archive.isEmpty();
  }
  
  public int size()
  {
    fetchAll();
    return this.archive.size();
  }
  
  public boolean contains(Object o)
  {
    if (this.archive.contains(o)) {
      return true;
    }
    while (this.result.hasNext())
    {
      T value = this.result.next();
      this.archive.add(value);
      if (value.equals(o)) {
        return true;
      }
    }
    return false;
  }
  
  public boolean containsAll(Collection<?> c)
  {
    for (Object o : c) {
      if (!contains(o)) {
        return false;
      }
    }
    return true;
  }
  
  public Iterator<T> iterator()
  {
    new Iterator()
    {
      int idx = 0;
      
      public boolean hasNext()
      {
        if (this.idx < DeferedCollection.this.archive.size()) {
          return true;
        }
        return DeferedCollection.this.result.hasNext();
      }
      
      public T next()
      {
        if (this.idx == DeferedCollection.this.archive.size()) {
          DeferedCollection.this.fetch();
        }
        if (this.idx == DeferedCollection.this.archive.size()) {
          throw new NoSuchElementException();
        }
        return (T)DeferedCollection.this.archive.get(this.idx++);
      }
      
      public void remove() {}
    };
  }
  
  public Object[] toArray()
  {
    fetchAll();
    return this.archive.toArray();
  }
  
  public <T> T[] toArray(T[] a)
  {
    fetchAll();
    return this.archive.toArray(a);
  }
  
  private void fetchAll()
  {
    while (this.result.hasNext()) {
      this.archive.add(this.result.next());
    }
  }
  
  private void fetch()
  {
    if (this.result.hasNext()) {
      this.archive.add(this.result.next());
    }
  }
  
  public boolean add(T o)
  {
    throw new UnsupportedOperationException();
  }
  
  public boolean remove(Object o)
  {
    throw new UnsupportedOperationException();
  }
  
  public boolean addAll(Collection<? extends T> c)
  {
    throw new UnsupportedOperationException();
  }
  
  public boolean removeAll(Collection<?> c)
  {
    throw new UnsupportedOperationException();
  }
  
  public boolean retainAll(Collection<?> c)
  {
    throw new UnsupportedOperationException();
  }
  
  public void clear()
  {
    throw new UnsupportedOperationException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\util\DeferedCollection.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */