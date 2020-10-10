package com.sun.xml.xsom.impl.util;

import java.util.Iterator;

public class ConcatIterator
  implements Iterator
{
  private Iterator lhs;
  private Iterator rhs;
  
  public ConcatIterator(Iterator _lhs, Iterator _rhs)
  {
    this.lhs = _lhs;
    this.rhs = _rhs;
  }
  
  public boolean hasNext()
  {
    if (this.lhs != null)
    {
      if (this.lhs.hasNext()) {
        return true;
      }
      this.lhs = null;
    }
    return this.rhs.hasNext();
  }
  
  public Object next()
  {
    if (this.lhs != null) {
      return this.lhs.next();
    }
    return this.rhs.next();
  }
  
  public void remove()
  {
    if (this.lhs != null) {
      this.lhs.remove();
    } else {
      this.rhs.remove();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\util\ConcatIterator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */