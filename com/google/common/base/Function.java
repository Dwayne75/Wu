package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import javax.annotation.Nullable;

@GwtCompatible
public abstract interface Function<F, T>
{
  @Nullable
  public abstract T apply(@Nullable F paramF);
  
  public abstract boolean equals(@Nullable Object paramObject);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\base\Function.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */