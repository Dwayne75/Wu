package com.sun.xml.xsom.util;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.visitor.XSFunction;

public class XSFunctionFilter<T>
  implements XSFunction<T>
{
  protected XSFunction<T> core;
  
  public XSFunctionFilter(XSFunction<T> _core)
  {
    this.core = _core;
  }
  
  public XSFunctionFilter() {}
  
  public T annotation(XSAnnotation ann)
  {
    return (T)this.core.annotation(ann);
  }
  
  public T attGroupDecl(XSAttGroupDecl decl)
  {
    return (T)this.core.attGroupDecl(decl);
  }
  
  public T attributeDecl(XSAttributeDecl decl)
  {
    return (T)this.core.attributeDecl(decl);
  }
  
  public T attributeUse(XSAttributeUse use)
  {
    return (T)this.core.attributeUse(use);
  }
  
  public T complexType(XSComplexType type)
  {
    return (T)this.core.complexType(type);
  }
  
  public T schema(XSSchema schema)
  {
    return (T)this.core.schema(schema);
  }
  
  public T facet(XSFacet facet)
  {
    return (T)this.core.facet(facet);
  }
  
  public T notation(XSNotation notation)
  {
    return (T)this.core.notation(notation);
  }
  
  public T simpleType(XSSimpleType simpleType)
  {
    return (T)this.core.simpleType(simpleType);
  }
  
  public T particle(XSParticle particle)
  {
    return (T)this.core.particle(particle);
  }
  
  public T empty(XSContentType empty)
  {
    return (T)this.core.empty(empty);
  }
  
  public T wildcard(XSWildcard wc)
  {
    return (T)this.core.wildcard(wc);
  }
  
  public T modelGroupDecl(XSModelGroupDecl decl)
  {
    return (T)this.core.modelGroupDecl(decl);
  }
  
  public T modelGroup(XSModelGroup group)
  {
    return (T)this.core.modelGroup(group);
  }
  
  public T elementDecl(XSElementDecl decl)
  {
    return (T)this.core.elementDecl(decl);
  }
  
  public T identityConstraint(XSIdentityConstraint decl)
  {
    return (T)this.core.identityConstraint(decl);
  }
  
  public T xpath(XSXPath xpath)
  {
    return (T)this.core.xpath(xpath);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\util\XSFunctionFilter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */