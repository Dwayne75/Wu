package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;

@GwtCompatible
public enum BoundType
{
  OPEN,  CLOSED;
  
  private BoundType() {}
  
  static BoundType forBoolean(boolean inclusive)
  {
    return inclusive ? CLOSED : OPEN;
  }
  
  abstract BoundType flip();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\collect\BoundType.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */