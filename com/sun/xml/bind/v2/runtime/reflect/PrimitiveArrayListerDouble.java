package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import java.util.Map;

final class PrimitiveArrayListerDouble<BeanT>
  extends Lister<BeanT, double[], Double, DoubleArrayPack>
{
  static void register()
  {
    Lister.primitiveArrayListers.put(Double.TYPE, new PrimitiveArrayListerDouble());
  }
  
  public ListIterator<Double> iterator(final double[] objects, XMLSerializer context)
  {
    new ListIterator()
    {
      int idx = 0;
      
      public boolean hasNext()
      {
        return this.idx < objects.length;
      }
      
      public Double next()
      {
        return Double.valueOf(objects[(this.idx++)]);
      }
    };
  }
  
  public DoubleArrayPack startPacking(BeanT current, Accessor<BeanT, double[]> acc)
  {
    return new DoubleArrayPack();
  }
  
  public void addToPack(DoubleArrayPack objects, Double o)
  {
    objects.add(o);
  }
  
  public void endPacking(DoubleArrayPack pack, BeanT bean, Accessor<BeanT, double[]> acc)
    throws AccessorException
  {
    acc.set(bean, pack.build());
  }
  
  public void reset(BeanT o, Accessor<BeanT, double[]> acc)
    throws AccessorException
  {
    acc.set(o, new double[0]);
  }
  
  static final class DoubleArrayPack
  {
    double[] buf = new double[16];
    int size;
    
    void add(Double b)
    {
      if (this.buf.length == this.size)
      {
        double[] nb = new double[this.buf.length * 2];
        System.arraycopy(this.buf, 0, nb, 0, this.buf.length);
        this.buf = nb;
      }
      if (b != null) {
        this.buf[(this.size++)] = b.doubleValue();
      }
    }
    
    double[] build()
    {
      if (this.buf.length == this.size) {
        return this.buf;
      }
      double[] r = new double[this.size];
      System.arraycopy(this.buf, 0, r, 0, this.size);
      return r;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\PrimitiveArrayListerDouble.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */