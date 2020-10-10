package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import java.util.Map;

final class PrimitiveArrayListerByte<BeanT>
  extends Lister<BeanT, byte[], Byte, ByteArrayPack>
{
  static void register()
  {
    Lister.primitiveArrayListers.put(Byte.TYPE, new PrimitiveArrayListerByte());
  }
  
  public ListIterator<Byte> iterator(final byte[] objects, XMLSerializer context)
  {
    new ListIterator()
    {
      int idx = 0;
      
      public boolean hasNext()
      {
        return this.idx < objects.length;
      }
      
      public Byte next()
      {
        return Byte.valueOf(objects[(this.idx++)]);
      }
    };
  }
  
  public ByteArrayPack startPacking(BeanT current, Accessor<BeanT, byte[]> acc)
  {
    return new ByteArrayPack();
  }
  
  public void addToPack(ByteArrayPack objects, Byte o)
  {
    objects.add(o);
  }
  
  public void endPacking(ByteArrayPack pack, BeanT bean, Accessor<BeanT, byte[]> acc)
    throws AccessorException
  {
    acc.set(bean, pack.build());
  }
  
  public void reset(BeanT o, Accessor<BeanT, byte[]> acc)
    throws AccessorException
  {
    acc.set(o, new byte[0]);
  }
  
  static final class ByteArrayPack
  {
    byte[] buf = new byte[16];
    int size;
    
    void add(Byte b)
    {
      if (this.buf.length == this.size)
      {
        byte[] nb = new byte[this.buf.length * 2];
        System.arraycopy(this.buf, 0, nb, 0, this.buf.length);
        this.buf = nb;
      }
      if (b != null) {
        this.buf[(this.size++)] = b.byteValue();
      }
    }
    
    byte[] build()
    {
      if (this.buf.length == this.size) {
        return this.buf;
      }
      byte[] r = new byte[this.size];
      System.arraycopy(this.buf, 0, r, 0, this.size);
      return r;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\PrimitiveArrayListerByte.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */