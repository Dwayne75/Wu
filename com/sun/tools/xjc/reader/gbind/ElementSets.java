package com.sun.tools.xjc.reader.gbind;

import java.util.LinkedHashSet;

public final class ElementSets
{
  public static ElementSet union(ElementSet lhs, ElementSet rhs)
  {
    if (lhs.contains(rhs)) {
      return lhs;
    }
    if (lhs == ElementSet.EMPTY_SET) {
      return rhs;
    }
    if (rhs == ElementSet.EMPTY_SET) {
      return lhs;
    }
    return new MultiValueSet(lhs, rhs);
  }
  
  private static final class MultiValueSet
    extends LinkedHashSet<Element>
    implements ElementSet
  {
    public MultiValueSet(ElementSet lhs, ElementSet rhs)
    {
      addAll(lhs);
      addAll(rhs);
      
      assert (size() > 1);
    }
    
    private void addAll(ElementSet lhs)
    {
      if ((lhs instanceof MultiValueSet)) {
        super.addAll((MultiValueSet)lhs);
      } else {
        for (Element e : lhs) {
          add(e);
        }
      }
    }
    
    public boolean contains(ElementSet rhs)
    {
      return (super.contains(rhs)) || (rhs == ElementSet.EMPTY_SET);
    }
    
    public void addNext(Element element)
    {
      for (Element e : this) {
        e.addNext(element);
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\gbind\ElementSets.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */