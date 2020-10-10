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

public class ComponentNameFunction
  implements XSFunction
{
  private NameGetter nameGetter = new NameGetter(null);
  
  public Object annotation(XSAnnotation ann)
  {
    return this.nameGetter.annotation(ann);
  }
  
  public Object attGroupDecl(XSAttGroupDecl decl)
  {
    String name = decl.getName();
    if (name == null) {
      name = "";
    }
    return name + " " + this.nameGetter.attGroupDecl(decl);
  }
  
  public Object attributeDecl(XSAttributeDecl decl)
  {
    String name = decl.getName();
    if (name == null) {
      name = "";
    }
    return name + " " + this.nameGetter.attributeDecl(decl);
  }
  
  public Object attributeUse(XSAttributeUse use)
  {
    return this.nameGetter.attributeUse(use);
  }
  
  public Object complexType(XSComplexType type)
  {
    String name = type.getName();
    if (name == null) {
      name = "anonymous";
    }
    return name + " " + this.nameGetter.complexType(type);
  }
  
  public Object schema(XSSchema schema)
  {
    return this.nameGetter.schema(schema) + " \"" + schema.getTargetNamespace() + "\"";
  }
  
  public Object facet(XSFacet facet)
  {
    String name = facet.getName();
    if (name == null) {
      name = "";
    }
    return name + " " + this.nameGetter.facet(facet);
  }
  
  public Object notation(XSNotation notation)
  {
    String name = notation.getName();
    if (name == null) {
      name = "";
    }
    return name + " " + this.nameGetter.notation(notation);
  }
  
  public Object simpleType(XSSimpleType simpleType)
  {
    String name = simpleType.getName();
    if (name == null) {
      name = "anonymous";
    }
    return name + " " + this.nameGetter.simpleType(simpleType);
  }
  
  public Object particle(XSParticle particle)
  {
    return this.nameGetter.particle(particle);
  }
  
  public Object empty(XSContentType empty)
  {
    return this.nameGetter.empty(empty);
  }
  
  public Object wildcard(XSWildcard wc)
  {
    return this.nameGetter.wildcard(wc);
  }
  
  public Object modelGroupDecl(XSModelGroupDecl decl)
  {
    String name = decl.getName();
    if (name == null) {
      name = "";
    }
    return name + " " + this.nameGetter.modelGroupDecl(decl);
  }
  
  public Object modelGroup(XSModelGroup group)
  {
    return this.nameGetter.modelGroup(group);
  }
  
  public Object elementDecl(XSElementDecl decl)
  {
    String name = decl.getName();
    if (name == null) {
      name = "";
    }
    return name + " " + this.nameGetter.elementDecl(decl);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\util\ComponentNameFunction.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */