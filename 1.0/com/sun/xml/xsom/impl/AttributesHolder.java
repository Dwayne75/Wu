package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeUse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.xml.sax.Locator;

public abstract class AttributesHolder
  extends DeclarationImpl
{
  protected AttributesHolder(SchemaImpl _parent, AnnotationImpl _annon, Locator loc, String _name, boolean _anonymous)
  {
    super(_parent, _annon, loc, _parent.getTargetNamespace(), _name, _anonymous);
  }
  
  protected final Map attributes = new TreeMap(UName.comparator);
  
  public abstract void setWildcard(WildcardImpl paramWildcardImpl);
  
  public void addAttributeUse(UName name, AttributeUseImpl a)
  {
    this.attributes.put(name, a);
  }
  
  protected final Set prohibitedAtts = new HashSet();
  
  public void addProhibitedAttribute(UName name)
  {
    this.prohibitedAtts.add(name);
  }
  
  public Iterator iterateAttributeUses()
  {
    List v = new ArrayList();
    v.addAll(this.attributes.values());
    Iterator itr = iterateAttGroups();
    while (itr.hasNext())
    {
      Iterator jtr = ((XSAttGroupDecl)itr.next()).iterateAttributeUses();
      while (jtr.hasNext()) {
        v.add(jtr.next());
      }
    }
    return v.iterator();
  }
  
  public XSAttributeUse getDeclaredAttributeUse(String nsURI, String localName)
  {
    return (XSAttributeUse)this.attributes.get(new UName(nsURI, localName));
  }
  
  public Iterator iterateDeclaredAttributeUses()
  {
    return this.attributes.values().iterator();
  }
  
  protected final Set attGroups = new HashSet();
  
  public void addAttGroup(Ref.AttGroup a)
  {
    this.attGroups.add(a);
  }
  
  public Iterator iterateAttGroups()
  {
    return new AttributesHolder.1(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\AttributesHolder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */