package com.sun.xml.bind.v2.runtime;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public final class AssociationMap<XmlNode>
{
  static final class Entry<XmlNode>
  {
    private XmlNode element;
    private Object inner;
    private Object outer;
    
    public XmlNode element()
    {
      return (XmlNode)this.element;
    }
    
    public Object inner()
    {
      return this.inner;
    }
    
    public Object outer()
    {
      return this.outer;
    }
  }
  
  private final Map<XmlNode, Entry<XmlNode>> byElement = new IdentityHashMap();
  private final Map<Object, Entry<XmlNode>> byPeer = new IdentityHashMap();
  private final Set<XmlNode> usedNodes = new HashSet();
  
  public void addInner(XmlNode element, Object inner)
  {
    Entry<XmlNode> e = (Entry)this.byElement.get(element);
    if (e != null)
    {
      if (e.inner != null) {
        this.byPeer.remove(e.inner);
      }
      e.inner = inner;
    }
    else
    {
      e = new Entry();
      e.element = element;
      e.inner = inner;
    }
    this.byElement.put(element, e);
    
    Entry<XmlNode> old = (Entry)this.byPeer.put(inner, e);
    if (old != null)
    {
      if (old.outer != null) {
        this.byPeer.remove(old.outer);
      }
      if (old.element != null) {
        this.byElement.remove(old.element);
      }
    }
  }
  
  public void addOuter(XmlNode element, Object outer)
  {
    Entry<XmlNode> e = (Entry)this.byElement.get(element);
    if (e != null)
    {
      if (e.outer != null) {
        this.byPeer.remove(e.outer);
      }
      e.outer = outer;
    }
    else
    {
      e = new Entry();
      e.element = element;
      e.outer = outer;
    }
    this.byElement.put(element, e);
    
    Entry<XmlNode> old = (Entry)this.byPeer.put(outer, e);
    if (old != null)
    {
      old.outer = null;
      if (old.inner == null) {
        this.byElement.remove(old.element);
      }
    }
  }
  
  public void addUsed(XmlNode n)
  {
    this.usedNodes.add(n);
  }
  
  public Entry<XmlNode> byElement(Object e)
  {
    return (Entry)this.byElement.get(e);
  }
  
  public Entry<XmlNode> byPeer(Object o)
  {
    return (Entry)this.byPeer.get(o);
  }
  
  public Object getInnerPeer(XmlNode element)
  {
    Entry e = byElement(element);
    if (e == null) {
      return null;
    }
    return e.inner;
  }
  
  public Object getOuterPeer(XmlNode element)
  {
    Entry e = byElement(element);
    if (e == null) {
      return null;
    }
    return e.outer;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\AssociationMap.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */