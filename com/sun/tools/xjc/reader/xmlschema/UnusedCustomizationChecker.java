package com.sun.tools.xjc.reader.xmlschema;

import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttContainer;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSUnionSimpleType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.visitor.XSSimpleTypeVisitor;
import com.sun.xml.xsom.visitor.XSVisitor;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

class UnusedCustomizationChecker
  extends BindingComponent
  implements XSVisitor, XSSimpleTypeVisitor
{
  private final BGMBuilder builder = (BGMBuilder)Ring.get(BGMBuilder.class);
  private final SimpleTypeBuilder stb = (SimpleTypeBuilder)Ring.get(SimpleTypeBuilder.class);
  private final Set<XSComponent> visitedComponents = new HashSet();
  
  void run()
  {
    for (XSSchema s : ((XSSchemaSet)Ring.get(XSSchemaSet.class)).getSchemas())
    {
      schema(s);
      run(s.getAttGroupDecls());
      run(s.getAttributeDecls());
      run(s.getComplexTypes());
      run(s.getElementDecls());
      run(s.getModelGroupDecls());
      run(s.getNotations());
      run(s.getSimpleTypes());
    }
  }
  
  private void run(Map<String, ? extends XSComponent> col)
  {
    for (XSComponent c : col.values()) {
      c.visit(this);
    }
  }
  
  private boolean check(XSComponent c)
  {
    if (!this.visitedComponents.add(c)) {
      return false;
    }
    for (BIDeclaration decl : this.builder.getBindInfo(c).getDecls()) {
      check(decl, c);
    }
    checkExpectedContentTypes(c);
    
    return true;
  }
  
  private void checkExpectedContentTypes(XSComponent c)
  {
    if (c.getForeignAttribute("http://www.w3.org/2005/05/xmlmime", "expectedContentTypes") == null) {
      return;
    }
    if ((c instanceof XSParticle)) {
      return;
    }
    if (!this.stb.isAcknowledgedXmimeContentTypes(c)) {
      getErrorReporter().warning(c.getLocator(), "UnusedCustomizationChecker.WarnUnusedExpectedContentTypes", new Object[0]);
    }
  }
  
  private void check(BIDeclaration decl, XSComponent c)
  {
    if (!decl.isAcknowledged())
    {
      getErrorReporter().error(decl.getLocation(), "UnusedCustomizationChecker.UnacknolwedgedCustomization", new Object[] { decl.getName().getLocalPart() });
      
      getErrorReporter().error(c.getLocator(), "UnusedCustomizationChecker.UnacknolwedgedCustomization.Relevant", new Object[0]);
      
      decl.markAsAcknowledged();
    }
    for (BIDeclaration d : decl.getChildren()) {
      check(d, c);
    }
  }
  
  public void annotation(XSAnnotation ann) {}
  
  public void attGroupDecl(XSAttGroupDecl decl)
  {
    if (check(decl)) {
      attContainer(decl);
    }
  }
  
  public void attributeDecl(XSAttributeDecl decl)
  {
    if (check(decl)) {
      decl.getType().visit(this);
    }
  }
  
  public void attributeUse(XSAttributeUse use)
  {
    if (check(use)) {
      use.getDecl().visit(this);
    }
  }
  
  public void complexType(XSComplexType type)
  {
    if (check(type))
    {
      type.getContentType().visit(this);
      attContainer(type);
    }
  }
  
  private void attContainer(XSAttContainer cont)
  {
    for (Iterator itr = cont.iterateAttGroups(); itr.hasNext();) {
      ((XSAttGroupDecl)itr.next()).visit(this);
    }
    for (Iterator itr = cont.iterateDeclaredAttributeUses(); itr.hasNext();) {
      ((XSAttributeUse)itr.next()).visit(this);
    }
    XSWildcard wc = cont.getAttributeWildcard();
    if (wc != null) {
      wc.visit(this);
    }
  }
  
  public void schema(XSSchema schema)
  {
    check(schema);
  }
  
  public void facet(XSFacet facet)
  {
    check(facet);
  }
  
  public void notation(XSNotation notation)
  {
    check(notation);
  }
  
  public void wildcard(XSWildcard wc)
  {
    check(wc);
  }
  
  public void modelGroupDecl(XSModelGroupDecl decl)
  {
    if (check(decl)) {
      decl.getModelGroup().visit(this);
    }
  }
  
  public void modelGroup(XSModelGroup group)
  {
    if (check(group)) {
      for (int i = 0; i < group.getSize(); i++) {
        group.getChild(i).visit(this);
      }
    }
  }
  
  public void elementDecl(XSElementDecl decl)
  {
    if (check(decl))
    {
      decl.getType().visit(this);
      for (XSIdentityConstraint id : decl.getIdentityConstraints()) {
        id.visit(this);
      }
    }
  }
  
  public void simpleType(XSSimpleType simpleType)
  {
    if (check(simpleType)) {
      simpleType.visit(this);
    }
  }
  
  public void particle(XSParticle particle)
  {
    if (check(particle)) {
      particle.getTerm().visit(this);
    }
  }
  
  public void empty(XSContentType empty)
  {
    check(empty);
  }
  
  public void listSimpleType(XSListSimpleType type)
  {
    if (check(type)) {
      type.getItemType().visit(this);
    }
  }
  
  public void restrictionSimpleType(XSRestrictionSimpleType type)
  {
    if (check(type)) {
      type.getBaseType().visit(this);
    }
  }
  
  public void unionSimpleType(XSUnionSimpleType type)
  {
    if (check(type)) {
      for (int i = 0; i < type.getMemberSize(); i++) {
        type.getMember(i).visit(this);
      }
    }
  }
  
  public void identityConstraint(XSIdentityConstraint id)
  {
    if (check(id))
    {
      id.getSelector().visit(this);
      for (XSXPath xp : id.getFields()) {
        xp.visit(this);
      }
    }
  }
  
  public void xpath(XSXPath xp)
  {
    check(xp);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\UnusedCustomizationChecker.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */