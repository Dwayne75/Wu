package com.sun.xml.xsom.impl.util;

import java.util.Iterator;

public abstract class FilterIterator
  implements Iterator
{
  private final Iterator core;
  private Object next;
  
  protected FilterIterator(Iterator core)
  {
    this.core = core;
  }
  
  protected abstract boolean allows(Object paramObject);
  
  public boolean hasNext()
  {
    while ((this.next == null) && (this.core.hasNext()))
    {
      Object o = this.core.next();
      if (allows(o)) {
        this.next = o;
      }
    }
    return this.next != null;
  }
  
  public Object next()
  {
    Object r = this.next;
    this.next = null;
    return r;
  }
  
  public void remove()
  {
    throw new UnsupportedOperationException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\util\FilterIterator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */