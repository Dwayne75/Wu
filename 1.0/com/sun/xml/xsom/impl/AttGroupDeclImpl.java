package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.impl.parser.DelayedRef.AttGroup;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.xml.sax.Locator;

public class AttGroupDeclImpl
  extends AttributesHolder
  implements XSAttGroupDecl
{
  private WildcardImpl wildcard;
  
  public AttGroupDeclImpl(SchemaImpl _parent, AnnotationImpl _annon, Locator _loc, String _name, WildcardImpl _wildcard)
  {
    this(_parent, _annon, _loc, _name);
    setWildcard(_wildcard);
  }
  
  public AttGroupDeclImpl(SchemaImpl _parent, AnnotationImpl _annon, Locator _loc, String _name)
  {
    super(_parent, _annon, _loc, _name, false);
  }
  
  public void setWildcard(WildcardImpl wc)
  {
    this.wildcard = wc;
  }
  
  public XSWildcard getAttributeWildcard()
  {
    return this.wildcard;
  }
  
  public XSAttributeUse getAttributeUse(String nsURI, String localName)
  {
    UName name = new UName(nsURI, localName);
    XSAttributeUse o = null;
    
    Iterator itr = iterateAttGroups();
    while ((itr.hasNext()) && (o == null)) {
      o = ((XSAttGroupDecl)itr.next()).getAttributeUse(nsURI, localName);
    }
    if (o == null) {
      o = (XSAttributeUse)this.attributes.get(name);
    }
    return o;
  }
  
  public void redefine(AttGroupDeclImpl ag)
  {
    for (Iterator itr = this.attGroups.iterator(); itr.hasNext();)
    {
      DelayedRef.AttGroup r = (DelayedRef.AttGroup)itr.next();
      r.redefine(ag);
    }
  }
  
  public void visit(XSVisitor visitor)
  {
    visitor.attGroupDecl(this);
  }
  
  public Object apply(XSFunction function)
  {
    return function.attGroupDecl(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\AttGroupDeclImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */