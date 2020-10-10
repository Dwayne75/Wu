package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import java.util.Map;

final class PrimitiveArrayListerShort<BeanT>
  extends Lister<BeanT, short[], Short, ShortArrayPack>
{
  static void register()
  {
    Lister.primitiveArrayListers.put(Short.TYPE, new PrimitiveArrayListerShort());
  }
  
  public ListIterator<Short> iterator(final short[] objects, XMLSerializer context)
  {
    new ListIterator()
    {
      int idx = 0;
      
      public boolean hasNext()
      {
        return this.idx < objects.length;
      }
      
      public Short next()
      {
        return Short.valueOf(objects[(this.idx++)]);
      }
    };
  }
  
  public ShortArrayPack startPacking(BeanT current, Accessor<BeanT, short[]> acc)
  {
    return new ShortArrayPack();
  }
  
  public void addToPack(ShortArrayPack objects, Short o)
  {
    objects.add(o);
  }
  
  public void endPacking(ShortArrayPack pack, BeanT bean, Accessor<BeanT, short[]> acc)
    throws AccessorException
  {
    acc.set(bean, pack.build());
  }
  
  public void reset(BeanT o, Accessor<BeanT, short[]> acc)
    throws AccessorException
  {
    acc.set(o, new short[0]);
  }
  
  static final class ShortArrayPack
  {
    short[] buf = new short[16];
    int size;
    
    void add(Short b)
    {
      if (this.buf.length == this.size)
      {
        short[] nb = new short[this.buf.length * 2];
        System.arraycopy(this.buf, 0, nb, 0, this.buf.length);
        this.buf = nb;
      }
      if (b != null) {
        this.buf[(this.size++)] = b.shortValue();
      }
    }
    
    short[] build()
    {
      if (this.buf.length == this.size) {
        return this.buf;
      }
      short[] r = new short[this.size];
      System.arraycopy(this.buf, 0, r, 0, this.size);
      return r;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\PrimitiveArrayListerShort.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */