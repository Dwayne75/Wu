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

public class ComponentNameFunction
  implements XSFunction<String>
{
  private NameGetter nameGetter = new NameGetter(null);
  
  public String annotation(XSAnnotation ann)
  {
    return this.nameGetter.annotation(ann);
  }
  
  public String attGroupDecl(XSAttGroupDecl decl)
  {
    String name = decl.getName();
    if (name == null) {
      name = "";
    }
    return name + " " + this.nameGetter.attGroupDecl(decl);
  }
  
  public String attributeDecl(XSAttributeDecl decl)
  {
    String name = decl.getName();
    if (name == null) {
      name = "";
    }
    return name + " " + this.nameGetter.attributeDecl(decl);
  }
  
  public String attributeUse(XSAttributeUse use)
  {
    return this.nameGetter.attributeUse(use);
  }
  
  public String complexType(XSComplexType type)
  {
    String name = type.getName();
    if (name == null) {
      name = "anonymous";
    }
    return name + " " + this.nameGetter.complexType(type);
  }
  
  public String schema(XSSchema schema)
  {
    return this.nameGetter.schema(schema) + " \"" + schema.getTargetNamespace() + "\"";
  }
  
  public String facet(XSFacet facet)
  {
    String name = facet.getName();
    if (name == null) {
      name = "";
    }
    return name + " " + this.nameGetter.facet(facet);
  }
  
  public String notation(XSNotation notation)
  {
    String name = notation.getName();
    if (name == null) {
      name = "";
    }
    return name + " " + this.nameGetter.notation(notation);
  }
  
  public String simpleType(XSSimpleType simpleType)
  {
    String name = simpleType.getName();
    if (name == null) {
      name = "anonymous";
    }
    return name + " " + this.nameGetter.simpleType(simpleType);
  }
  
  public String particle(XSParticle particle)
  {
    return this.nameGetter.particle(particle);
  }
  
  public String empty(XSContentType empty)
  {
    return this.nameGetter.empty(empty);
  }
  
  public String wildcard(XSWildcard wc)
  {
    return this.nameGetter.wildcard(wc);
  }
  
  public String modelGroupDecl(XSModelGroupDecl decl)
  {
    String name = decl.getName();
    if (name == null) {
      name = "";
    }
    return name + " " + this.nameGetter.modelGroupDecl(decl);
  }
  
  public String modelGroup(XSModelGroup group)
  {
    return this.nameGetter.modelGroup(group);
  }
  
  public String elementDecl(XSElementDecl decl)
  {
    String name = decl.getName();
    if (name == null) {
      name = "";
    }
    return name + " " + this.nameGetter.elementDecl(decl);
  }
  
  public String identityConstraint(XSIdentityConstraint decl)
  {
    return decl.getName() + " " + this.nameGetter.identityConstraint(decl);
  }
  
  public String xpath(XSXPath xpath)
  {
    return this.nameGetter.xpath(xpath);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\util\ComponentNameFunction.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */