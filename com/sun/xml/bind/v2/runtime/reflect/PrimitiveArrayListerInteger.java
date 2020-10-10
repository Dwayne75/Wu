package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import java.util.Map;

final class PrimitiveArrayListerInteger<BeanT>
  extends Lister<BeanT, int[], Integer, IntegerArrayPack>
{
  static void register()
  {
    Lister.primitiveArrayListers.put(Integer.TYPE, new PrimitiveArrayListerInteger());
  }
  
  public ListIterator<Integer> iterator(final int[] objects, XMLSerializer context)
  {
    new ListIterator()
    {
      int idx = 0;
      
      public boolean hasNext()
      {
        return this.idx < objects.length;
      }
      
      public Integer next()
      {
        return Integer.valueOf(objects[(this.idx++)]);
      }
    };
  }
  
  public IntegerArrayPack startPacking(BeanT current, Accessor<BeanT, int[]> acc)
  {
    return new IntegerArrayPack();
  }
  
  public void addToPack(IntegerArrayPack objects, Integer o)
  {
    objects.add(o);
  }
  
  public void endPacking(IntegerArrayPack pack, BeanT bean, Accessor<BeanT, int[]> acc)
    throws AccessorException
  {
    acc.set(bean, pack.build());
  }
  
  public void reset(BeanT o, Accessor<BeanT, int[]> acc)
    throws AccessorException
  {
    acc.set(o, new int[0]);
  }
  
  static final class IntegerArrayPack
  {
    int[] buf = new int[16];
    int size;
    
    void add(Integer b)
    {
      if (this.buf.length == this.size)
      {
        int[] nb = new int[this.buf.length * 2];
        System.arraycopy(this.buf, 0, nb, 0, this.buf.length);
        this.buf = nb;
      }
      if (b != null) {
        this.buf[(this.size++)] = b.intValue();
      }
    }
    
    int[] build()
    {
      if (this.buf.length == this.size) {
        return this.buf;
      }
      int[] r = new int[this.size];
      System.arraycopy(this.buf, 0, r, 0, this.size);
      return r;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\PrimitiveArrayListerInteger.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */