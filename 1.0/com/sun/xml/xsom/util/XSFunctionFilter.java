package com.sun.xml.xsom.util;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSFunction;

public class XSFunctionFilter
  implements XSFunction
{
  protected XSFunction core;
  
  public XSFunctionFilter(XSFunction _core)
  {
    this.core = _core;
  }
  
  public XSFunctionFilter() {}
  
  public Object annotation(XSAnnotation ann)
  {
    return this.core.annotation(ann);
  }
  
  public Object attGroupDecl(XSAttGroupDecl decl)
  {
    return this.core.attGroupDecl(decl);
  }
  
  public Object attributeDecl(XSAttributeDecl decl)
  {
    return this.core.attributeDecl(decl);
  }
  
  public Object attributeUse(XSAttributeUse use)
  {
    return this.core.attributeUse(use);
  }
  
  public Object complexType(XSComplexType type)
  {
    return this.core.complexType(type);
  }
  
  public Object schema(XSSchema schema)
  {
    return this.core.schema(schema);
  }
  
  public Object facet(XSFacet facet)
  {
    return this.core.facet(facet);
  }
  
  public Object notation(XSNotation notation)
  {
    return this.core.notation(notation);
  }
  
  public Object simpleType(XSSimpleType simpleType)
  {
    return this.core.simpleType(simpleType);
  }
  
  public Object particle(XSParticle particle)
  {
    return this.core.particle(particle);
  }
  
  public Object empty(XSContentType empty)
  {
    return this.core.empty(empty);
  }
  
  public Object wildcard(XSWildcard wc)
  {
    return this.core.wildcard(wc);
  }
  
  public Object modelGroupDecl(XSModelGroupDecl decl)
  {
    return this.core.modelGroupDecl(decl);
  }
  
  public Object modelGroup(XSModelGroup group)
  {
    return this.core.modelGroup(group);
  }
  
  public Object elementDecl(XSElementDecl decl)
  {
    return this.core.elementDecl(decl);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\util\XSFunctionFilter.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */