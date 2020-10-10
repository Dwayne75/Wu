package com.sun.istack;

import java.util.ArrayList;
import java.util.Collection;

public final class FinalArrayList<T>
  extends ArrayList<T>
{
  public FinalArrayList(int initialCapacity)
  {
    super(initialCapacity);
  }
  
  public FinalArrayList() {}
  
  public FinalArrayList(Collection<? extends T> ts)
  {
    super(ts);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\istack\FinalArrayList.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */