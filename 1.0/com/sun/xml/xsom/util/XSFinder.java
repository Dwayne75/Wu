package com.sun.xml.xsom.util;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
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

public class XSFinder
  implements XSFunction
{
  public final boolean find(XSComponent c)
  {
    return ((Boolean)c.apply(this)).booleanValue();
  }
  
  public Object annotation(XSAnnotation ann)
  {
    return Boolean.FALSE;
  }
  
  public Object attGroupDecl(XSAttGroupDecl decl)
  {
    return Boolean.FALSE;
  }
  
  public Object attributeDecl(XSAttributeDecl decl)
  {
    return Boolean.FALSE;
  }
  
  public Object attributeUse(XSAttributeUse use)
  {
    return Boolean.FALSE;
  }
  
  public Object complexType(XSComplexType type)
  {
    return Boolean.FALSE;
  }
  
  public Object schema(XSSchema schema)
  {
    return Boolean.FALSE;
  }
  
  public Object facet(XSFacet facet)
  {
    return Boolean.FALSE;
  }
  
  public Object notation(XSNotation notation)
  {
    return Boolean.FALSE;
  }
  
  public Object simpleType(XSSimpleType simpleType)
  {
    return Boolean.FALSE;
  }
  
  public Object particle(XSParticle particle)
  {
    return Boolean.FALSE;
  }
  
  public Object empty(XSContentType empty)
  {
    return Boolean.FALSE;
  }
  
  public Object wildcard(XSWildcard wc)
  {
    return Boolean.FALSE;
  }
  
  public Object modelGroupDecl(XSModelGroupDecl decl)
  {
    return Boolean.FALSE;
  }
  
  public Object modelGroup(XSModelGroup group)
  {
    return Boolean.FALSE;
  }
  
  public Object elementDecl(XSElementDecl decl)
  {
    return Boolean.FALSE;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\util\XSFinder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */