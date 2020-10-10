package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.v2.util.QNameMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.namespace.QName;

public final class NameBuilder
{
  private Map<String, Integer> uriIndexMap = new HashMap();
  private Set<String> nonDefaultableNsUris = new HashSet();
  private Map<String, Integer> localNameIndexMap = new HashMap();
  private QNameMap<Integer> elementQNameIndexMap = new QNameMap();
  private QNameMap<Integer> attributeQNameIndexMap = new QNameMap();
  
  public Name createElementName(QName name)
  {
    return createElementName(name.getNamespaceURI(), name.getLocalPart());
  }
  
  public Name createElementName(String nsUri, String localName)
  {
    return createName(nsUri, localName, false, this.elementQNameIndexMap);
  }
  
  public Name createAttributeName(QName name)
  {
    return createAttributeName(name.getNamespaceURI(), name.getLocalPart());
  }
  
  public Name createAttributeName(String nsUri, String localName)
  {
    assert (nsUri.intern() == nsUri);
    assert (localName.intern() == localName);
    if (nsUri.length() == 0) {
      return new Name(allocIndex(this.attributeQNameIndexMap, "", localName), -1, nsUri, allocIndex(this.localNameIndexMap, localName), localName, true);
    }
    this.nonDefaultableNsUris.add(nsUri);
    return createName(nsUri, localName, true, this.attributeQNameIndexMap);
  }
  
  private Name createName(String nsUri, String localName, boolean isAttribute, QNameMap<Integer> map)
  {
    assert (nsUri.intern() == nsUri);
    assert (localName.intern() == localName);
    
    return new Name(allocIndex(map, nsUri, localName), allocIndex(this.uriIndexMap, nsUri), nsUri, allocIndex(this.localNameIndexMap, localName), localName, isAttribute);
  }
  
  private int allocIndex(Map<String, Integer> map, String str)
  {
    Integer i = (Integer)map.get(str);
    if (i == null)
    {
      i = Integer.valueOf(map.size());
      map.put(str, i);
    }
    return i.intValue();
  }
  
  private int allocIndex(QNameMap<Integer> map, String nsUri, String localName)
  {
    Integer i = (Integer)map.get(nsUri, localName);
    if (i == null)
    {
      i = Integer.valueOf(map.size());
      map.put(nsUri, localName, i);
    }
    return i.intValue();
  }
  
  public NameList conclude()
  {
    boolean[] nsUriCannotBeDefaulted = new boolean[this.uriIndexMap.size()];
    for (Map.Entry<String, Integer> e : this.uriIndexMap.entrySet()) {
      nsUriCannotBeDefaulted[((Integer)e.getValue()).intValue()] = this.nonDefaultableNsUris.contains(e.getKey());
    }
    NameList r = new NameList(list(this.uriIndexMap), nsUriCannotBeDefaulted, list(this.localNameIndexMap), this.elementQNameIndexMap.size(), this.attributeQNameIndexMap.size());
    
    this.uriIndexMap = null;
    this.localNameIndexMap = null;
    return r;
  }
  
  private String[] list(Map<String, Integer> map)
  {
    String[] r = new String[map.size()];
    for (Map.Entry<String, Integer> e : map.entrySet()) {
      r[((Integer)e.getValue()).intValue()] = ((String)e.getKey());
    }
    return r;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\NameBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */