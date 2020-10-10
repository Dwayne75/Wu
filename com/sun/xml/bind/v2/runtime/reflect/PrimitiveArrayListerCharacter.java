package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import java.util.Map;

final class PrimitiveArrayListerCharacter<BeanT>
  extends Lister<BeanT, char[], Character, CharacterArrayPack>
{
  static void register()
  {
    Lister.primitiveArrayListers.put(Character.TYPE, new PrimitiveArrayListerCharacter());
  }
  
  public ListIterator<Character> iterator(final char[] objects, XMLSerializer context)
  {
    new ListIterator()
    {
      int idx = 0;
      
      public boolean hasNext()
      {
        return this.idx < objects.length;
      }
      
      public Character next()
      {
        return Character.valueOf(objects[(this.idx++)]);
      }
    };
  }
  
  public CharacterArrayPack startPacking(BeanT current, Accessor<BeanT, char[]> acc)
  {
    return new CharacterArrayPack();
  }
  
  public void addToPack(CharacterArrayPack objects, Character o)
  {
    objects.add(o);
  }
  
  public void endPacking(CharacterArrayPack pack, BeanT bean, Accessor<BeanT, char[]> acc)
    throws AccessorException
  {
    acc.set(bean, pack.build());
  }
  
  public void reset(BeanT o, Accessor<BeanT, char[]> acc)
    throws AccessorException
  {
    acc.set(o, new char[0]);
  }
  
  static final class CharacterArrayPack
  {
    char[] buf = new char[16];
    int size;
    
    void add(Character b)
    {
      if (this.buf.length == this.size)
      {
        char[] nb = new char[this.buf.length * 2];
        System.arraycopy(this.buf, 0, nb, 0, this.buf.length);
        this.buf = nb;
      }
      if (b != null) {
        this.buf[(this.size++)] = b.charValue();
      }
    }
    
    char[] build()
    {
      if (this.buf.length == this.size) {
        return this.buf;
      }
      char[] r = new char[this.size];
      System.arraycopy(this.buf, 0, r, 0, this.size);
      return r;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\PrimitiveArrayListerCharacter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */