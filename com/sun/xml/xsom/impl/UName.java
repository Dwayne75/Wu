package com.sun.xml.xsom.impl;

import java.util.Comparator;

public final class UName
{
  private final String nsUri;
  private final String localName;
  private final String qname;
  
  public UName(String _nsUri, String _localName, String _qname)
  {
    if ((_nsUri == null) || (_localName == null) || (_qname == null)) {
      throw new NullPointerException(_nsUri + " " + _localName + " " + _qname);
    }
    this.nsUri = _nsUri.intern();
    this.localName = _localName.intern();
    this.qname = _qname.intern();
  }
  
  public UName(String nsUri, String localName)
  {
    this(nsUri, localName, localName);
  }
  
  public String getName()
  {
    return this.localName;
  }
  
  public String getNamespaceURI()
  {
    return this.nsUri;
  }
  
  public String getQualifiedName()
  {
    return this.qname;
  }
  
  public static final Comparator comparator = new Comparator()
  {
    public int compare(Object o1, Object o2)
    {
      UName lhs = (UName)o1;
      UName rhs = (UName)o2;
      int r = lhs.nsUri.compareTo(rhs.nsUri);
      if (r != 0) {
        return r;
      }
      return lhs.localName.compareTo(rhs.localName);
    }
  };
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\UName.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */