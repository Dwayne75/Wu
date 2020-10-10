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

public class XSFinder
  implements XSFunction<Boolean>
{
  public final boolean find(XSComponent c)
  {
    return ((Boolean)c.apply(this)).booleanValue();
  }
  
  public Boolean annotation(XSAnnotation ann)
  {
    return Boolean.FALSE;
  }
  
  public Boolean attGroupDecl(XSAttGroupDecl decl)
  {
    return Boolean.FALSE;
  }
  
  public Boolean attributeDecl(XSAttributeDecl decl)
  {
    return Boolean.FALSE;
  }
  
  public Boolean attributeUse(XSAttributeUse use)
  {
    return Boolean.FALSE;
  }
  
  public Boolean complexType(XSComplexType type)
  {
    return Boolean.FALSE;
  }
  
  public Boolean schema(XSSchema schema)
  {
    return Boolean.FALSE;
  }
  
  public Boolean facet(XSFacet facet)
  {
    return Boolean.FALSE;
  }
  
  public Boolean notation(XSNotation notation)
  {
    return Boolean.FALSE;
  }
  
  public Boolean simpleType(XSSimpleType simpleType)
  {
    return Boolean.FALSE;
  }
  
  public Boolean particle(XSParticle particle)
  {
    return Boolean.FALSE;
  }
  
  public Boolean empty(XSContentType empty)
  {
    return Boolean.FALSE;
  }
  
  public Boolean wildcard(XSWildcard wc)
  {
    return Boolean.FALSE;
  }
  
  public Boolean modelGroupDecl(XSModelGroupDecl decl)
  {
    return Boolean.FALSE;
  }
  
  public Boolean modelGroup(XSModelGroup group)
  {
    return Boolean.FALSE;
  }
  
  public Boolean elementDecl(XSElementDecl decl)
  {
    return Boolean.FALSE;
  }
  
  public Boolean identityConstraint(XSIdentityConstraint decl)
  {
    return Boolean.FALSE;
  }
  
  public Boolean xpath(XSXPath xpath)
  {
    return Boolean.FALSE;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\util\XSFinder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */