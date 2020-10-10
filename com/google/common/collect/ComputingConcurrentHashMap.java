package com.google.common.collect;

import com.google.common.base.Equivalence;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.ReferenceQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReferenceArray;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

class ComputingConcurrentHashMap<K, V>
  extends MapMakerInternalMap<K, V>
{
  final Function<? super K, ? extends V> computingFunction;
  private static final long serialVersionUID = 4L;
  
  ComputingConcurrentHashMap(MapMaker builder, Function<? super K, ? extends V> computingFunction)
  {
    super(builder);
    this.computingFunction = ((Function)Preconditions.checkNotNull(computingFunction));
  }
  
  MapMakerInternalMap.Segment<K, V> createSegment(int initialCapacity, int maxSegmentSize)
  {
    return new ComputingSegment(this, initialCapacity, maxSegmentSize);
  }
  
  ComputingSegment<K, V> segmentFor(int hash)
  {
    return (ComputingSegment)super.segmentFor(hash);
  }
  
  V getOrCompute(K key)
    throws ExecutionException
  {
    int hash = hash(Preconditions.checkNotNull(key));
    return (V)segmentFor(hash).getOrCompute(key, hash, this.computingFunction);
  }
  
  static final class ComputingSegment<K, V>
    extends MapMakerInternalMap.Segment<K, V>
  {
    ComputingSegment(MapMakerInternalMap<K, V> map, int initialCapacity, int maxSegmentSize)
    {
      super(initialCapacity, maxSegmentSize);
    }
    
    V getOrCompute(K key, int hash, Function<? super K, ? extends V> computingFunction)
      throws ExecutionException
    {
      try
      {
        MapMakerInternalMap.ReferenceEntry<K, V> e;
        Object computingValueReference;
        V value;
        do
        {
          e = getEntry(key, hash);
          if (e != null)
          {
            V value = getLiveValue(e);
            if (value != null)
            {
              recordRead(e);
              return value;
            }
          }
          if ((e == null) || (!e.getValueReference().isComputingReference()))
          {
            boolean createNewEntry = true;
            computingValueReference = null;
            lock();
            int newCount;
            try
            {
              preWriteCleanup();
              
              newCount = this.count - 1;
              AtomicReferenceArray<MapMakerInternalMap.ReferenceEntry<K, V>> table = this.table;
              int index = hash & table.length() - 1;
              MapMakerInternalMap.ReferenceEntry<K, V> first = (MapMakerInternalMap.ReferenceEntry)table.get(index);
              for (e = first; e != null; e = e.getNext())
              {
                K entryKey = e.getKey();
                if ((e.getHash() == hash) && (entryKey != null) && (this.map.keyEquivalence.equivalent(key, entryKey)))
                {
                  MapMakerInternalMap.ValueReference<K, V> valueReference = e.getValueReference();
                  if (valueReference.isComputingReference())
                  {
                    createNewEntry = false; break;
                  }
                  V value = e.getValueReference().get();
                  if (value == null)
                  {
                    enqueueNotification(entryKey, hash, value, MapMaker.RemovalCause.COLLECTED);
                  }
                  else if ((this.map.expires()) && (this.map.isExpired(e)))
                  {
                    enqueueNotification(entryKey, hash, value, MapMaker.RemovalCause.EXPIRED);
                  }
                  else
                  {
                    recordLockedRead(e);
                    return value;
                  }
                  this.evictionQueue.remove(e);
                  this.expirationQueue.remove(e);
                  this.count = newCount;
                  
                  break;
                }
              }
              if (createNewEntry)
              {
                computingValueReference = new ComputingConcurrentHashMap.ComputingValueReference(computingFunction);
                if (e == null)
                {
                  e = newEntry(key, hash, first);
                  e.setValueReference((MapMakerInternalMap.ValueReference)computingValueReference);
                  table.set(index, e);
                }
                else
                {
                  e.setValueReference((MapMakerInternalMap.ValueReference)computingValueReference);
                }
              }
            }
            finally
            {
              unlock();
            }
            if (createNewEntry) {
              return (V)compute(key, hash, e, (ComputingConcurrentHashMap.ComputingValueReference)computingValueReference);
            }
          }
          Preconditions.checkState(!Thread.holdsLock(e), "Recursive computation");
          
          value = e.getValueReference().waitForValue();
        } while (value == null);
        recordRead(e);
        return value;
      }
      finally
      {
        postReadCleanup();
      }
    }
    
    V compute(K key, int hash, MapMakerInternalMap.ReferenceEntry<K, V> e, ComputingConcurrentHashMap.ComputingValueReference<K, V> computingValueReference)
      throws ExecutionException
    {
      V value = null;
      long start = System.nanoTime();
      long end = 0L;
      try
      {
        synchronized (e)
        {
          value = computingValueReference.compute(key, hash);
          end = System.nanoTime();
        }
        V oldValue;
        if (value != null)
        {
          oldValue = put(key, hash, value, true);
          if (oldValue != null) {
            enqueueNotification(key, hash, value, MapMaker.RemovalCause.REPLACED);
          }
        }
        return value;
      }
      finally
      {
        if (end == 0L) {
          end = System.nanoTime();
        }
        if (value == null) {
          clearValue(key, hash, computingValueReference);
        }
      }
    }
  }
  
  private static final class ComputationExceptionReference<K, V>
    implements MapMakerInternalMap.ValueReference<K, V>
  {
    final Throwable t;
    
    ComputationExceptionReference(Throwable t)
    {
      this.t = t;
    }
    
    public V get()
    {
      return null;
    }
    
    public MapMakerInternalMap.ReferenceEntry<K, V> getEntry()
    {
      return null;
    }
    
    public MapMakerInternalMap.ValueReference<K, V> copyFor(ReferenceQueue<V> queue, V value, MapMakerInternalMap.ReferenceEntry<K, V> entry)
    {
      return this;
    }
    
    public boolean isComputingReference()
    {
      return false;
    }
    
    public V waitForValue()
      throws ExecutionException
    {
      throw new ExecutionException(this.t);
    }
    
    public void clear(MapMakerInternalMap.ValueReference<K, V> newValue) {}
  }
  
  private static final class ComputedReference<K, V>
    implements MapMakerInternalMap.ValueReference<K, V>
  {
    final V value;
    
    ComputedReference(@Nullable V value)
    {
      this.value = value;
    }
    
    public V get()
    {
      return (V)this.value;
    }
    
    public MapMakerInternalMap.ReferenceEntry<K, V> getEntry()
    {
      return null;
    }
    
    public MapMakerInternalMap.ValueReference<K, V> copyFor(ReferenceQueue<V> queue, V value, MapMakerInternalMap.ReferenceEntry<K, V> entry)
    {
      return this;
    }
    
    public boolean isComputingReference()
    {
      return false;
    }
    
    public V waitForValue()
    {
      return (V)get();
    }
    
    public void clear(MapMakerInternalMap.ValueReference<K, V> newValue) {}
  }
  
  private static final class ComputingValueReference<K, V>
    implements MapMakerInternalMap.ValueReference<K, V>
  {
    final Function<? super K, ? extends V> computingFunction;
    @GuardedBy("ComputingValueReference.this")
    volatile MapMakerInternalMap.ValueReference<K, V> computedReference = MapMakerInternalMap.unset();
    
    public ComputingValueReference(Function<? super K, ? extends V> computingFunction)
    {
      this.computingFunction = computingFunction;
    }
    
    public V get()
    {
      return null;
    }
    
    public MapMakerInternalMap.ReferenceEntry<K, V> getEntry()
    {
      return null;
    }
    
    public MapMakerInternalMap.ValueReference<K, V> copyFor(ReferenceQueue<V> queue, @Nullable V value, MapMakerInternalMap.ReferenceEntry<K, V> entry)
    {
      return this;
    }
    
    public boolean isComputingReference()
    {
      return true;
    }
    
    public V waitForValue()
      throws ExecutionException
    {
      if (this.computedReference == MapMakerInternalMap.UNSET)
      {
        boolean interrupted = false;
        try
        {
          synchronized (this)
          {
            while (this.computedReference == MapMakerInternalMap.UNSET) {
              try
              {
                wait();
              }
              catch (InterruptedException ie)
              {
                interrupted = true;
              }
            }
          }
        }
        finally
        {
          if (interrupted) {
            Thread.currentThread().interrupt();
          }
        }
      }
      return (V)this.computedReference.waitForValue();
    }
    
    public void clear(MapMakerInternalMap.ValueReference<K, V> newValue)
    {
      setValueReference(newValue);
    }
    
    V compute(K key, int hash)
      throws ExecutionException
    {
      V value;
      try
      {
        value = this.computingFunction.apply(key);
      }
      catch (Throwable t)
      {
        setValueReference(new ComputingConcurrentHashMap.ComputationExceptionReference(t));
        throw new ExecutionException(t);
      }
      setValueReference(new ComputingConcurrentHashMap.ComputedReference(value));
      return value;
    }
    
    void setValueReference(MapMakerInternalMap.ValueReference<K, V> valueReference)
    {
      synchronized (this)
      {
        if (this.computedReference == MapMakerInternalMap.UNSET)
        {
          this.computedReference = valueReference;
          notifyAll();
        }
      }
    }
  }
  
  Object writeReplace()
  {
    return new ComputingSerializationProxy(this.keyStrength, this.valueStrength, this.keyEquivalence, this.valueEquivalence, this.expireAfterWriteNanos, this.expireAfterAccessNanos, this.maximumSize, this.concurrencyLevel, this.removalListener, this, this.computingFunction);
  }
  
  static final class ComputingSerializationProxy<K, V>
    extends MapMakerInternalMap.AbstractSerializationProxy<K, V>
  {
    final Function<? super K, ? extends V> computingFunction;
    private static final long serialVersionUID = 4L;
    
    ComputingSerializationProxy(MapMakerInternalMap.Strength keyStrength, MapMakerInternalMap.Strength valueStrength, Equivalence<Object> keyEquivalence, Equivalence<Object> valueEquivalence, long expireAfterWriteNanos, long expireAfterAccessNanos, int maximumSize, int concurrencyLevel, MapMaker.RemovalListener<? super K, ? super V> removalListener, ConcurrentMap<K, V> delegate, Function<? super K, ? extends V> computingFunction)
    {
      super(valueStrength, keyEquivalence, valueEquivalence, expireAfterWriteNanos, expireAfterAccessNanos, maximumSize, concurrencyLevel, removalListener, delegate);
      
      this.computingFunction = computingFunction;
    }
    
    private void writeObject(ObjectOutputStream out)
      throws IOException
    {
      out.defaultWriteObject();
      writeMapTo(out);
    }
    
    private void readObject(ObjectInputStream in)
      throws IOException, ClassNotFoundException
    {
      in.defaultReadObject();
      MapMaker mapMaker = readMapMaker(in);
      this.delegate = mapMaker.makeComputingMap(this.computingFunction);
      readEntries(in);
    }
    
    Object readResolve()
    {
      return this.delegate;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\collect\ComputingConcurrentHashMap.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */