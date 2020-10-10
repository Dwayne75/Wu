package com.sun.xml.bind.v2.util;

import java.util.AbstractList;
import java.util.Arrays;

public final class CollisionCheckStack<E>
  extends AbstractList<E>
{
  private Object[] data;
  private int[] next;
  private int size = 0;
  private boolean useIdentity = true;
  private final int[] initialHash;
  
  public CollisionCheckStack()
  {
    this.initialHash = new int[17];
    this.data = new Object[16];
    this.next = new int[16];
  }
  
  public void setUseIdentity(boolean useIdentity)
  {
    this.useIdentity = useIdentity;
  }
  
  public boolean getUseIdentity()
  {
    return this.useIdentity;
  }
  
  public boolean push(E o)
  {
    if (this.data.length == this.size) {
      expandCapacity();
    }
    this.data[this.size] = o;
    int hash = hash(o);
    boolean r = findDuplicate(o, hash);
    this.next[this.size] = this.initialHash[hash];
    this.initialHash[hash] = (this.size + 1);
    this.size += 1;
    return r;
  }
  
  public void pushNocheck(E o)
  {
    if (this.data.length == this.size) {
      expandCapacity();
    }
    this.data[this.size] = o;
    this.next[this.size] = -1;
    this.size += 1;
  }
  
  public E get(int index)
  {
    return (E)this.data[index];
  }
  
  public int size()
  {
    return this.size;
  }
  
  private int hash(Object o)
  {
    return ((this.useIdentity ? System.identityHashCode(o) : o.hashCode()) & 0x7FFFFFFF) % this.initialHash.length;
  }
  
  public E pop()
  {
    this.size -= 1;
    Object o = this.data[this.size];
    this.data[this.size] = null;
    int n = this.next[this.size];
    if (n >= 0)
    {
      int hash = hash(o);
      assert (this.initialHash[hash] == this.size + 1);
      this.initialHash[hash] = n;
    }
    return (E)o;
  }
  
  public E peek()
  {
    return (E)this.data[(this.size - 1)];
  }
  
  private boolean findDuplicate(E o, int hash)
  {
    int p = this.initialHash[hash];
    while (p != 0)
    {
      p--;
      Object existing = this.data[p];
      if (this.useIdentity)
      {
        if (existing == o) {
          return true;
        }
      }
      else if (o.equals(existing)) {
        return true;
      }
      p = this.next[p];
    }
    return false;
  }
  
  private void expandCapacity()
  {
    int oldSize = this.data.length;
    int newSize = oldSize * 2;
    Object[] d = new Object[newSize];
    int[] n = new int[newSize];
    
    System.arraycopy(this.data, 0, d, 0, oldSize);
    System.arraycopy(this.next, 0, n, 0, oldSize);
    
    this.data = d;
    this.next = n;
  }
  
  public void reset()
  {
    if (this.size > 0)
    {
      this.size = 0;
      Arrays.fill(this.initialHash, 0);
    }
  }
  
  public String getCycleString()
  {
    StringBuilder sb = new StringBuilder();
    int i = size() - 1;
    E obj = get(i);
    sb.append(obj);
    Object x;
    do
    {
      sb.append(" -> ");
      x = get(--i);
      sb.append(x);
    } while (obj != x);
    return sb.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\util\CollisionCheckStack.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */