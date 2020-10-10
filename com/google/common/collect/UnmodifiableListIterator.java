package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.ListIterator;

@GwtCompatible
public abstract class UnmodifiableListIterator<E>
  extends UnmodifiableIterator<E>
  implements ListIterator<E>
{
  @Deprecated
  public final void add(E e)
  {
    throw new UnsupportedOperationException();
  }
  
  @Deprecated
  public final void set(E e)
  {
    throw new UnsupportedOperationException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\collect\UnmodifiableListIterator.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */