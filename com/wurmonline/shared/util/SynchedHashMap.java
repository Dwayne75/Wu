package com.wurmonline.shared.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class SynchedHashMap<K, V>
  extends HashMap<K, V>
{
  private static final long serialVersionUID = -7481512012392422544L;
  private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
  private final Lock readLock = this.rwl.readLock();
  private final Lock writeLock = this.rwl.writeLock();
  
  public SynchedHashMap(Map<? extends K, ? extends V> m)
  {
    super(m);
  }
  
  public SynchedHashMap() {}
  
  public SynchedHashMap(int initialCapacity)
  {
    super(initialCapacity);
  }
  
  public SynchedHashMap(int initialCapacity, float loadFactor)
  {
    super(initialCapacity, loadFactor);
  }
  
  /* Error */
  public void clear()
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 8	com/wurmonline/shared/util/SynchedHashMap:writeLock	Ljava/util/concurrent/locks/Lock;
    //   4: invokeinterface 12 1 0
    //   9: aload_0
    //   10: invokespecial 13	java/util/HashMap:clear	()V
    //   13: aload_0
    //   14: getfield 8	com/wurmonline/shared/util/SynchedHashMap:writeLock	Ljava/util/concurrent/locks/Lock;
    //   17: invokeinterface 14 1 0
    //   22: goto +15 -> 37
    //   25: astore_1
    //   26: aload_0
    //   27: getfield 8	com/wurmonline/shared/util/SynchedHashMap:writeLock	Ljava/util/concurrent/locks/Lock;
    //   30: invokeinterface 14 1 0
    //   35: aload_1
    //   36: athrow
    //   37: return
    // Line number table:
    //   Java source line #98	-> byte code offset #0
    //   Java source line #101	-> byte code offset #9
    //   Java source line #105	-> byte code offset #13
    //   Java source line #106	-> byte code offset #22
    //   Java source line #105	-> byte code offset #25
    //   Java source line #106	-> byte code offset #35
    //   Java source line #107	-> byte code offset #37
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	38	0	this	SynchedHashMap<K, V>
    //   25	11	1	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   9	13	25	finally
  }
  
  public boolean containsKey(Object key)
  {
    this.readLock.lock();
    try
    {
      return super.containsKey(key);
    }
    finally
    {
      this.readLock.unlock();
    }
  }
  
  public boolean containsValue(Object value)
  {
    this.readLock.lock();
    try
    {
      return super.containsValue(value);
    }
    finally
    {
      this.readLock.unlock();
    }
  }
  
  public Set<Map.Entry<K, V>> entrySet()
  {
    this.readLock.lock();
    try
    {
      return super.entrySet();
    }
    finally
    {
      this.readLock.unlock();
    }
  }
  
  public V get(Object key)
  {
    this.readLock.lock();
    try
    {
      return (V)super.get(key);
    }
    finally
    {
      this.readLock.unlock();
    }
  }
  
  public Set<K> keySet()
  {
    this.readLock.lock();
    try
    {
      return super.keySet();
    }
    finally
    {
      this.readLock.unlock();
    }
  }
  
  public V put(K key, V value)
  {
    this.writeLock.lock();
    try
    {
      return (V)super.put(key, value);
    }
    finally
    {
      this.writeLock.unlock();
    }
  }
  
  /* Error */
  public void putAll(Map<? extends K, ? extends V> map)
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 8	com/wurmonline/shared/util/SynchedHashMap:writeLock	Ljava/util/concurrent/locks/Lock;
    //   4: invokeinterface 12 1 0
    //   9: aload_0
    //   10: aload_1
    //   11: invokespecial 21	java/util/HashMap:putAll	(Ljava/util/Map;)V
    //   14: aload_0
    //   15: getfield 8	com/wurmonline/shared/util/SynchedHashMap:writeLock	Ljava/util/concurrent/locks/Lock;
    //   18: invokeinterface 14 1 0
    //   23: goto +15 -> 38
    //   26: astore_2
    //   27: aload_0
    //   28: getfield 8	com/wurmonline/shared/util/SynchedHashMap:writeLock	Ljava/util/concurrent/locks/Lock;
    //   31: invokeinterface 14 1 0
    //   36: aload_2
    //   37: athrow
    //   38: return
    // Line number table:
    //   Java source line #231	-> byte code offset #0
    //   Java source line #234	-> byte code offset #9
    //   Java source line #238	-> byte code offset #14
    //   Java source line #239	-> byte code offset #23
    //   Java source line #238	-> byte code offset #26
    //   Java source line #239	-> byte code offset #36
    //   Java source line #240	-> byte code offset #38
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	39	0	this	SynchedHashMap<K, V>
    //   0	39	1	map	Map<? extends K, ? extends V>
    //   26	11	2	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   9	14	26	finally
  }
  
  public V remove(Object key)
  {
    this.writeLock.lock();
    try
    {
      return (V)super.remove(key);
    }
    finally
    {
      this.writeLock.unlock();
    }
  }
  
  public Collection<V> values()
  {
    this.readLock.lock();
    try
    {
      return super.values();
    }
    finally
    {
      this.readLock.unlock();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\shared\util\SynchedHashMap.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */