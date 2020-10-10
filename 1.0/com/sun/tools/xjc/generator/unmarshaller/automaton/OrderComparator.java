package com.sun.tools.xjc.generator.unmarshaller.automaton;

import java.util.Comparator;

public final class OrderComparator
  implements Comparator
{
  public static final Comparator theInstance = new OrderComparator();
  
  public int compare(Object o1, Object o2)
  {
    Alphabet a1 = (Alphabet)o1;
    Alphabet a2 = (Alphabet)o2;
    
    return a2.order - a1.order;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\unmarshaller\automaton\OrderComparator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */