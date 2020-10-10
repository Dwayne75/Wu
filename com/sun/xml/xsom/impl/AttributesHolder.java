package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;
import com.sun.xml.xsom.impl.scd.Iterators.Adapter;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xml.sax.Locator;

public abstract class AttributesHolder
  extends DeclarationImpl
{
  protected AttributesHolder(SchemaDocumentImpl _parent, AnnotationImpl _annon, Locator loc, ForeignAttributesImpl _fa, String _name, boolean _anonymous)
  {
    super(_parent, _annon, loc, _fa, _parent.getTargetNamespace(), _name, _anonymous);
  }
  
  protected final Map<UName, AttributeUseImpl> attributes = new LinkedHashMap();
  
  public abstract void setWildcard(WildcardImpl paramWildcardImpl);
  
  public void addAttributeUse(UName name, AttributeUseImpl a)
  {
    this.attributes.put(name, a);
  }
  
  protected final Set<UName> prohibitedAtts = new HashSet();
  
  public void addProhibitedAttribute(UName name)
  {
    this.prohibitedAtts.add(name);
  }
  
  public List<XSAttributeUse> getAttributeUses()
  {
    List<XSAttributeUse> v = new ArrayList();
    v.addAll(this.attributes.values());
    for (XSAttGroupDecl agd : getAttGroups()) {
      v.addAll(agd.getAttributeUses());
    }
    return v;
  }
  
  public Iterator<XSAttributeUse> iterateAttributeUses()
  {
    return getAttributeUses().iterator();
  }
  
  public XSAttributeUse getDeclaredAttributeUse(String nsURI, String localName)
  {
    return (XSAttributeUse)this.attributes.get(new UName(nsURI, localName));
  }
  
  public Iterator<AttributeUseImpl> iterateDeclaredAttributeUses()
  {
    return this.attributes.values().iterator();
  }
  
  public Collection<AttributeUseImpl> getDeclaredAttributeUses()
  {
    return this.attributes.values();
  }
  
  protected final Set<Ref.AttGroup> attGroups = new HashSet();
  
  public void addAttGroup(Ref.AttGroup a)
  {
    this.attGroups.add(a);
  }
  
  public Iterator<XSAttGroupDecl> iterateAttGroups()
  {
    new Iterators.Adapter(this.attGroups.iterator())
    {
      protected XSAttGroupDecl filter(Ref.AttGroup u)
      {
        return u.get();
      }
    };
  }
  
  public Set<XSAttGroupDecl> getAttGroups()
  {
    new AbstractSet()
    {
      public Iterator<XSAttGroupDecl> iterator()
      {
        return AttributesHolder.this.iterateAttGroups();
      }
      
      public int size()
      {
        return AttributesHolder.this.attGroups.size();
      }
    };
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\AttributesHolder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */