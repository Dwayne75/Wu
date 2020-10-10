package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import java.util.Map;

final class PrimitiveArrayListerBoolean<BeanT>
  extends Lister<BeanT, boolean[], Boolean, BooleanArrayPack>
{
  static void register()
  {
    Lister.primitiveArrayListers.put(Boolean.TYPE, new PrimitiveArrayListerBoolean());
  }
  
  public ListIterator<Boolean> iterator(final boolean[] objects, XMLSerializer context)
  {
    new ListIterator()
    {
      int idx = 0;
      
      public boolean hasNext()
      {
        return this.idx < objects.length;
      }
      
      public Boolean next()
      {
        return Boolean.valueOf(objects[(this.idx++)]);
      }
    };
  }
  
  public BooleanArrayPack startPacking(BeanT current, Accessor<BeanT, boolean[]> acc)
  {
    return new BooleanArrayPack();
  }
  
  public void addToPack(BooleanArrayPack objects, Boolean o)
  {
    objects.add(o);
  }
  
  public void endPacking(BooleanArrayPack pack, BeanT bean, Accessor<BeanT, boolean[]> acc)
    throws AccessorException
  {
    acc.set(bean, pack.build());
  }
  
  public void reset(BeanT o, Accessor<BeanT, boolean[]> acc)
    throws AccessorException
  {
    acc.set(o, new boolean[0]);
  }
  
  static final class BooleanArrayPack
  {
    boolean[] buf = new boolean[16];
    int size;
    
    void add(Boolean b)
    {
      if (this.buf.length == this.size)
      {
        boolean[] nb = new boolean[this.buf.length * 2];
        System.arraycopy(this.buf, 0, nb, 0, this.buf.length);
        this.buf = nb;
      }
      if (b != null) {
        this.buf[(this.size++)] = b.booleanValue();
      }
    }
    
    boolean[] build()
    {
      if (this.buf.length == this.size) {
        return this.buf;
      }
      boolean[] r = new boolean[this.size];
      System.arraycopy(this.buf, 0, r, 0, this.size);
      return r;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\PrimitiveArrayListerBoolean.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */