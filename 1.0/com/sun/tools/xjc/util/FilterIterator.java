package com.sun.tools.xjc.util;

import java.util.Iterator;

public abstract class FilterIterator
  implements Iterator
{
  private final Iterator core;
  
  protected abstract boolean test(Object paramObject);
  
  public FilterIterator(Iterator _core)
  {
    this.core = _core;
  }
  
  private boolean ready = false;
  private boolean noMore = false;
  private Object obj = null;
  
  public final Object next()
  {
    if (!hasNext()) {
      throw new IllegalStateException("no more object");
    }
    this.ready = false;
    return this.obj;
  }
  
  public final boolean hasNext()
  {
    if (this.noMore) {
      return false;
    }
    if (this.ready) {
      return true;
    }
    while (this.core.hasNext())
    {
      Object o = this.core.next();
      if (test(o))
      {
        this.obj = o;
        this.ready = true;
        return true;
      }
    }
    this.noMore = true;
    return false;
  }
  
  public final void remove()
  {
    throw new UnsupportedOperationException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\util\FilterIterator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */