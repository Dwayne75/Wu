package com.google.common.collect;

import java.util.SortedSet;

abstract interface SortedMultisetBridge<E>
  extends Multiset<E>
{
  public abstract SortedSet<E> elementSet();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\collect\SortedMultisetBridge.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */