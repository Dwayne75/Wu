package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import java.util.Map;

final class PrimitiveArrayListerLong<BeanT>
  extends Lister<BeanT, long[], Long, LongArrayPack>
{
  static void register()
  {
    Lister.primitiveArrayListers.put(Long.TYPE, new PrimitiveArrayListerLong());
  }
  
  public ListIterator<Long> iterator(final long[] objects, XMLSerializer context)
  {
    new ListIterator()
    {
      int idx = 0;
      
      public boolean hasNext()
      {
        return this.idx < objects.length;
      }
      
      public Long next()
      {
        return Long.valueOf(objects[(this.idx++)]);
      }
    };
  }
  
  public LongArrayPack startPacking(BeanT current, Accessor<BeanT, long[]> acc)
  {
    return new LongArrayPack();
  }
  
  public void addToPack(LongArrayPack objects, Long o)
  {
    objects.add(o);
  }
  
  public void endPacking(LongArrayPack pack, BeanT bean, Accessor<BeanT, long[]> acc)
    throws AccessorException
  {
    acc.set(bean, pack.build());
  }
  
  public void reset(BeanT o, Accessor<BeanT, long[]> acc)
    throws AccessorException
  {
    acc.set(o, new long[0]);
  }
  
  static final class LongArrayPack
  {
    long[] buf = new long[16];
    int size;
    
    void add(Long b)
    {
      if (this.buf.length == this.size)
      {
        long[] nb = new long[this.buf.length * 2];
        System.arraycopy(this.buf, 0, nb, 0, this.buf.length);
        this.buf = nb;
      }
      if (b != null) {
        this.buf[(this.size++)] = b.longValue();
      }
    }
    
    long[] build()
    {
      if (this.buf.length == this.size) {
        return this.buf;
      }
      long[] r = new long[this.size];
      System.arraycopy(this.buf, 0, r, 0, this.size);
      return r;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\PrimitiveArrayListerLong.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */