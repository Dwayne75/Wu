package com.sun.tools.xjc.reader.xmlschema;

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
import com.sun.xml.xsom.visitor.XSSimpleTypeVisitor;
import com.sun.xml.xsom.visitor.XSVisitor;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.xml.namespace.QName;

class UnusedCustomizationChecker
  implements XSVisitor, XSSimpleTypeVisitor
{
  private final BGMBuilder builder;
  
  UnusedCustomizationChecker(BGMBuilder _builder)
  {
    this.builder = _builder;
  }
  
  void run()
  {
    for (Iterator itr = this.builder.schemas.iterateSchema(); itr.hasNext();)
    {
      XSSchema s = (XSSchema)itr.next();
      
      schema(s);
      run(s.iterateAttGroupDecls());
      run(s.iterateAttributeDecls());
      run(s.iterateComplexTypes());
      run(s.iterateElementDecls());
      run(s.iterateModelGroupDecls());
      run(s.iterateNotations());
      run(s.iterateSimpleTypes());
    }
  }
  
  private void run(Iterator itr)
  {
    while (itr.hasNext()) {
      ((XSComponent)itr.next()).visit(this);
    }
  }
  
  private Set visitedComponents = new HashSet();
  static final String ERR_UNACKNOWLEDGED_CUSTOMIZATION = "UnusedCustomizationChecker.UnacknolwedgedCustomization";
  
  private boolean check(XSComponent c)
  {
    if (!this.visitedComponents.add(c)) {
      return false;
    }
    BIDeclaration[] decls = this.builder.getBindInfo(c).getDecls();
    for (int i = 0; i < decls.length; i++) {
      if (!decls[i].isAcknowledged())
      {
        this.builder.errorReporter.error(decls[i].getLocation(), "UnusedCustomizationChecker.UnacknolwedgedCustomization", decls[i].getName().getLocalPart());
        
        decls[i].markAsAcknowledged();
      }
    }
    return true;
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
    if (check(decl)) {
      decl.getType().visit(this);
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
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\UnusedCustomizationChecker.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */