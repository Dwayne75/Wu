package com.sun.xml.bind.v2.util;

import com.sun.xml.bind.v2.runtime.Name;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.xml.namespace.QName;

public final class QNameMap<V>
{
  private static final int DEFAULT_INITIAL_CAPACITY = 16;
  private static final int MAXIMUM_CAPACITY = 1073741824;
  transient Entry<V>[] table = new Entry[16];
  transient int size;
  private int threshold;
  private static final float DEFAULT_LOAD_FACTOR = 0.75F;
  private Set<Entry<V>> entrySet = null;
  
  public QNameMap()
  {
    this.threshold = 12;
    this.table = new Entry[16];
  }
  
  public void put(String namespaceUri, String localname, V value)
  {
    assert (localname != null);
    assert (namespaceUri != null);
    
    assert (localname == localname.intern());
    assert (namespaceUri == namespaceUri.intern());
    
    int hash = hash(localname);
    int i = indexFor(hash, this.table.length);
    for (Entry<V> e = this.table[i]; e != null; e = e.next) {
      if ((e.hash == hash) && (localname == e.localName) && (namespaceUri == e.nsUri))
      {
        e.value = value;
        return;
      }
    }
    addEntry(hash, namespaceUri, localname, value, i);
  }
  
  public void put(QName name, V value)
  {
    put(name.getNamespaceURI(), name.getLocalPart(), value);
  }
  
  public void put(Name name, V value)
  {
    put(name.nsUri, name.localName, value);
  }
  
  public V get(String nsUri, String localPart)
  {
    Entry<V> e = getEntry(nsUri, localPart);
    if (e == null) {
      return null;
    }
    return (V)e.value;
  }
  
  public V get(QName name)
  {
    return (V)get(name.getNamespaceURI(), name.getLocalPart());
  }
  
  public int size()
  {
    return this.size;
  }
  
  public QNameMap<V> putAll(QNameMap<? extends V> map)
  {
    int numKeysToBeAdded = map.size();
    if (numKeysToBeAdded == 0) {
      return this;
    }
    if (numKeysToBeAdded > this.threshold)
    {
      int targetCapacity = numKeysToBeAdded;
      if (targetCapacity > 1073741824) {
        targetCapacity = 1073741824;
      }
      int newCapacity = this.table.length;
      while (newCapacity < targetCapacity) {
        newCapacity <<= 1;
      }
      if (newCapacity > this.table.length) {
        resize(newCapacity);
      }
    }
    for (Entry<? extends V> e : map.entrySet()) {
      put(e.nsUri, e.localName, e.getValue());
    }
    return this;
  }
  
  private static int hash(String x)
  {
    int h = x.hashCode();
    
    h += (h << 9 ^ 0xFFFFFFFF);
    h ^= h >>> 14;
    h += (h << 4);
    h ^= h >>> 10;
    return h;
  }
  
  private static int indexFor(int h, int length)
  {
    return h & length - 1;
  }
  
  private void addEntry(int hash, String nsUri, String localName, V value, int bucketIndex)
  {
    Entry<V> e = this.table[bucketIndex];
    this.table[bucketIndex] = new Entry(hash, nsUri, localName, value, e);
    if (this.size++ >= this.threshold) {
      resize(2 * this.table.length);
    }
  }
  
  private void resize(int newCapacity)
  {
    Entry[] oldTable = this.table;
    int oldCapacity = oldTable.length;
    if (oldCapacity == 1073741824)
    {
      this.threshold = Integer.MAX_VALUE;
      return;
    }
    Entry[] newTable = new Entry[newCapacity];
    transfer(newTable);
    this.table = newTable;
    this.threshold = newCapacity;
  }
  
  private void transfer(Entry<V>[] newTable)
  {
    Entry<V>[] src = this.table;
    int newCapacity = newTable.length;
    for (int j = 0; j < src.length; j++)
    {
      Entry<V> e = src[j];
      if (e != null)
      {
        src[j] = null;
        do
        {
          Entry<V> next = e.next;
          int i = indexFor(e.hash, newCapacity);
          e.next = newTable[i];
          newTable[i] = e;
          e = next;
        } while (e != null);
      }
    }
  }
  
  public Entry<V> getOne()
  {
    for (Entry<V> e : this.table) {
      if (e != null) {
        return e;
      }
    }
    return null;
  }
  
  public Collection<QName> keySet()
  {
    Set<QName> r = new HashSet();
    for (Entry<V> e : entrySet()) {
      r.add(e.createQName());
    }
    return r;
  }
  
  private abstract class HashIterator<E>
    implements Iterator<E>
  {
    QNameMap.Entry<V> next;
    int index;
    
    HashIterator()
    {
      QNameMap.Entry<V>[] t = QNameMap.this.table;
      int i = t.length;
      QNameMap.Entry<V> n = null;
      while ((QNameMap.this.size != 0) && 
        (i > 0) && ((n = t[(--i)]) == null)) {}
      this.next = n;
      this.index = i;
    }
    
    public boolean hasNext()
    {
      return this.next != null;
    }
    
    QNameMap.Entry<V> nextEntry()
    {
      QNameMap.Entry<V> e = this.next;
      if (e == null) {
        throw new NoSuchElementException();
      }
      QNameMap.Entry<V> n = e.next;
      QNameMap.Entry<V>[] t = QNameMap.this.table;
      int i = this.index;
      while ((n == null) && (i > 0)) {
        n = t[(--i)];
      }
      this.index = i;
      this.next = n;
      return e;
    }
    
    public void remove()
    {
      throw new UnsupportedOperationException();
    }
  }
  
  public boolean containsKey(String nsUri, String localName)
  {
    return getEntry(nsUri, localName) != null;
  }
  
  public boolean isEmpty()
  {
    return this.size == 0;
  }
  
  public static final class Entry<V>
  {
    public final String nsUri;
    public final String localName;
    V value;
    final int hash;
    Entry<V> next;
    
    Entry(int h, String nsUri, String localName, V v, Entry<V> n)
    {
      this.value = v;
      this.next = n;
      this.nsUri = nsUri;
      this.localName = localName;
      this.hash = h;
    }
    
    public QName createQName()
    {
      return new QName(this.nsUri, this.localName);
    }
    
    public V getValue()
    {
      return (V)this.value;
    }
    
    public V setValue(V newValue)
    {
      V oldValue = this.value;
      this.value = newValue;
      return oldValue;
    }
    
    public boolean equals(Object o)
    {
      if (!(o instanceof Entry)) {
        return false;
      }
      Entry e = (Entry)o;
      String k1 = this.nsUri;
      String k2 = e.nsUri;
      String k3 = this.localName;
      String k4 = e.localName;
      if ((k1 == k2) || ((k1 != null) && (k1.equals(k2)) && ((k3 == k4) || ((k3 != null) && (k3.equals(k4))))))
      {
        Object v1 = getValue();
        Object v2 = e.getValue();
        if ((v1 == v2) || ((v1 != null) && (v1.equals(v2)))) {
          return true;
        }
      }
      return false;
    }
    
    public int hashCode()
    {
      return this.localName.hashCode() ^ (this.value == null ? 0 : this.value.hashCode());
    }
    
    public String toString()
    {
      return '"' + this.nsUri + "\",\"" + this.localName + "\"=" + getValue();
    }
  }
  
  public Set<Entry<V>> entrySet()
  {
    Set<Entry<V>> es = this.entrySet;
    return es != null ? es : (this.entrySet = new EntrySet(null));
  }
  
  private Iterator<Entry<V>> newEntryIterator()
  {
    return new EntryIterator(null);
  }
  
  private class EntryIterator
    extends QNameMap<V>.HashIterator<QNameMap.Entry<V>>
  {
    private EntryIterator()
    {
      super();
    }
    
    public QNameMap.Entry<V> next()
    {
      return nextEntry();
    }
  }
  
  private class EntrySet
    extends AbstractSet<QNameMap.Entry<V>>
  {
    private EntrySet() {}
    
    public Iterator<QNameMap.Entry<V>> iterator()
    {
      return QNameMap.this.newEntryIterator();
    }
    
    public boolean contains(Object o)
    {
      if (!(o instanceof QNameMap.Entry)) {
        return false;
      }
      QNameMap.Entry<V> e = (QNameMap.Entry)o;
      QNameMap.Entry<V> candidate = QNameMap.this.getEntry(e.nsUri, e.localName);
      return (candidate != null) && (candidate.equals(e));
    }
    
    public boolean remove(Object o)
    {
      throw new UnsupportedOperationException();
    }
    
    public int size()
    {
      return QNameMap.this.size;
    }
  }
  
  private Entry<V> getEntry(String nsUri, String localName)
  {
    assert (nsUri == nsUri.intern());
    assert (localName == localName.intern());
    
    int hash = hash(localName);
    int i = indexFor(hash, this.table.length);
    Entry<V> e = this.table[i];
    while ((e != null) && ((localName != e.localName) || (nsUri != e.nsUri))) {
      e = e.next;
    }
    return e;
  }
  
  public String toString()
  {
    StringBuilder buf = new StringBuilder();
    buf.append('{');
    for (Entry<V> e : entrySet())
    {
      if (buf.length() > 1) {
        buf.append(',');
      }
      buf.append('[');
      buf.append(e);
      buf.append(']');
    }
    buf.append('}');
    return buf.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\util\QNameMap.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */