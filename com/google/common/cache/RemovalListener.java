package com.google.common.cache;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;

@Beta
@GwtCompatible
public abstract interface RemovalListener<K, V>
{
  public abstract void onRemoval(RemovalNotification<K, V> paramRemovalNotification);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\cache\RemovalListener.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */