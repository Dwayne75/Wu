package com.sun.tools.xjc.reader.gbind;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

abstract interface ElementSet
  extends Iterable<Element>
{
  public static final ElementSet EMPTY_SET = new ElementSet()
  {
    public void addNext(Element element) {}
    
    public boolean contains(ElementSet element)
    {
      return this == element;
    }
    
    public Iterator<Element> iterator()
    {
      return Collections.emptySet().iterator();
    }
  };
  
  public abstract void addNext(Element paramElement);
  
  public abstract boolean contains(ElementSet paramElementSet);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\gbind\ElementSet.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */