package com.sun.tools.xjc.reader.xmlschema;

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
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.visitor.XSVisitor;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class RefererFinder
  implements XSVisitor
{
  private final Set<Object> visited = new HashSet();
  private final Map<XSComponent, Set<XSComponent>> referers = new HashMap();
  
  public Set<XSComponent> getReferer(XSComponent src)
  {
    Set<XSComponent> r = (Set)this.referers.get(src);
    if (r == null) {
      return Collections.emptySet();
    }
    return r;
  }
  
  public void schemaSet(XSSchemaSet xss)
  {
    if (!this.visited.add(xss)) {
      return;
    }
    for (XSSchema xs : xss.getSchemas()) {
      schema(xs);
    }
  }
  
  public void schema(XSSchema xs)
  {
    if (!this.visited.add(xs)) {
      return;
    }
    for (XSComplexType ct : xs.getComplexTypes().values()) {
      complexType(ct);
    }
    for (XSElementDecl e : xs.getElementDecls().values()) {
      elementDecl(e);
    }
  }
  
  public void elementDecl(XSElementDecl e)
  {
    if (!this.visited.add(e)) {
      return;
    }
    refer(e, e.getType());
    e.getType().visit(this);
  }
  
  public void complexType(XSComplexType ct)
  {
    if (!this.visited.add(ct)) {
      return;
    }
    refer(ct, ct.getBaseType());
    ct.getBaseType().visit(this);
    ct.getContentType().visit(this);
  }
  
  public void modelGroupDecl(XSModelGroupDecl decl)
  {
    if (!this.visited.add(decl)) {
      return;
    }
    modelGroup(decl.getModelGroup());
  }
  
  public void modelGroup(XSModelGroup group)
  {
    if (!this.visited.add(group)) {
      return;
    }
    for (XSParticle p : group.getChildren()) {
      particle(p);
    }
  }
  
  public void particle(XSParticle particle)
  {
    particle.getTerm().visit(this);
  }
  
  public void simpleType(XSSimpleType simpleType) {}
  
  public void annotation(XSAnnotation ann) {}
  
  public void attGroupDecl(XSAttGroupDecl decl) {}
  
  public void attributeDecl(XSAttributeDecl decl) {}
  
  public void attributeUse(XSAttributeUse use) {}
  
  public void facet(XSFacet facet) {}
  
  public void notation(XSNotation notation) {}
  
  public void identityConstraint(XSIdentityConstraint decl) {}
  
  public void xpath(XSXPath xp) {}
  
  public void wildcard(XSWildcard wc) {}
  
  public void empty(XSContentType empty) {}
  
  private void refer(XSComponent source, XSType target)
  {
    Set<XSComponent> r = (Set)this.referers.get(target);
    if (r == null)
    {
      r = new HashSet();
      this.referers.put(target, r);
    }
    r.add(source);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\RefererFinder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */