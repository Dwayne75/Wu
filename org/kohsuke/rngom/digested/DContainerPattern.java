package org.kohsuke.rngom.digested;

import java.util.Iterator;

public abstract class DContainerPattern
  extends DPattern
  implements Iterable<DPattern>
{
  private DPattern head;
  private DPattern tail;
  
  public DPattern firstChild()
  {
    return this.head;
  }
  
  public DPattern lastChild()
  {
    return this.tail;
  }
  
  public int countChildren()
  {
    int i = 0;
    for (DPattern p = firstChild(); p != null; p = p.next) {
      i++;
    }
    return i;
  }
  
  public Iterator<DPattern> iterator()
  {
    new Iterator()
    {
      DPattern next = DContainerPattern.this.head;
      
      public boolean hasNext()
      {
        return this.next != null;
      }
      
      public DPattern next()
      {
        DPattern r = this.next;
        this.next = this.next.next;
        return r;
      }
      
      public void remove()
      {
        throw new UnsupportedOperationException();
      }
    };
  }
  
  void add(DPattern child)
  {
    if (this.tail == null)
    {
      child.prev = (child.next = null);
      this.head = (this.tail = child);
    }
    else
    {
      child.prev = this.tail;
      this.tail.next = child;
      child.next = null;
      this.tail = child;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\DContainerPattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */