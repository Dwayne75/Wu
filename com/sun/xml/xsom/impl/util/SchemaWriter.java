package com.sun.xml.xsom.impl.util;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
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
import com.sun.xml.xsom.visitor.XSTermVisitor;
import com.sun.xml.xsom.visitor.XSVisitor;
import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Iterator;

public class SchemaWriter
  implements XSVisitor, XSSimpleTypeVisitor
{
  private final Writer out;
  private int indent;
  
  public SchemaWriter(Writer _out)
  {
    this.out = _out;
  }
  
  private void println(String s)
  {
    try
    {
      for (int i = 0; i < this.indent; i++) {
        this.out.write("  ");
      }
      this.out.write(s);
      this.out.write(10);
      
      this.out.flush();
    }
    catch (IOException e)
    {
      this.hadError = true;
    }
  }
  
  private void println()
  {
    println("");
  }
  
  private boolean hadError = false;
  
  public boolean checkError()
  {
    try
    {
      this.out.flush();
    }
    catch (IOException e)
    {
      this.hadError = true;
    }
    return this.hadError;
  }
  
  public void visit(XSSchemaSet s)
  {
    Iterator itr = s.iterateSchema();
    while (itr.hasNext())
    {
      schema((XSSchema)itr.next());
      println();
    }
  }
  
  public void schema(XSSchema s)
  {
    if (s.getTargetNamespace().equals("http://www.w3.org/2001/XMLSchema")) {
      return;
    }
    println(MessageFormat.format("<schema targetNamespace=\"{0}\">", new Object[] { s.getTargetNamespace() }));
    
    this.indent += 1;
    
    Iterator itr = s.iterateAttGroupDecls();
    while (itr.hasNext()) {
      attGroupDecl((XSAttGroupDecl)itr.next());
    }
    itr = s.iterateAttributeDecls();
    while (itr.hasNext()) {
      attributeDecl((XSAttributeDecl)itr.next());
    }
    itr = s.iterateComplexTypes();
    while (itr.hasNext()) {
      complexType((XSComplexType)itr.next());
    }
    itr = s.iterateElementDecls();
    while (itr.hasNext()) {
      elementDecl((XSElementDecl)itr.next());
    }
    itr = s.iterateModelGroupDecls();
    while (itr.hasNext()) {
      modelGroupDecl((XSModelGroupDecl)itr.next());
    }
    itr = s.iterateSimpleTypes();
    while (itr.hasNext()) {
      simpleType((XSSimpleType)itr.next());
    }
    this.indent -= 1;
    println("</schema>");
  }
  
  public void attGroupDecl(XSAttGroupDecl decl)
  {
    println(MessageFormat.format("<attGroup name=\"{0}\">", new Object[] { decl.getName() }));
    
    this.indent += 1;
    
    Iterator itr = decl.iterateAttGroups();
    while (itr.hasNext()) {
      dumpRef((XSAttGroupDecl)itr.next());
    }
    itr = decl.iterateDeclaredAttributeUses();
    while (itr.hasNext()) {
      attributeUse((XSAttributeUse)itr.next());
    }
    this.indent -= 1;
    println("</attGroup>");
  }
  
  public void dumpRef(XSAttGroupDecl decl)
  {
    println(MessageFormat.format("<attGroup ref=\"'{'{0}'}'{1}\"/>", new Object[] { decl.getTargetNamespace(), decl.getName() }));
  }
  
  public void attributeUse(XSAttributeUse use)
  {
    XSAttributeDecl decl = use.getDecl();
    
    String additionalAtts = "";
    if (use.isRequired()) {
      additionalAtts = additionalAtts + " use=\"required\"";
    }
    if ((use.getFixedValue() != null) && (use.getDecl().getFixedValue() == null)) {
      additionalAtts = additionalAtts + " fixed=\"" + use.getFixedValue() + '"';
    }
    if ((use.getDefaultValue() != null) && (use.getDecl().getDefaultValue() == null)) {
      additionalAtts = additionalAtts + " default=\"" + use.getDefaultValue() + '"';
    }
    if (decl.isLocal()) {
      dump(decl, additionalAtts);
    } else {
      println(MessageFormat.format("<attribute ref=\"'{'{0}'}'{1}{2}\"/>", new Object[] { decl.getTargetNamespace(), decl.getName(), additionalAtts }));
    }
  }
  
  public void attributeDecl(XSAttributeDecl decl)
  {
    dump(decl, "");
  }
  
  private void dump(XSAttributeDecl decl, String additionalAtts)
  {
    XSSimpleType type = decl.getType();
    
    println(MessageFormat.format("<attribute name=\"{0}\"{1}{2}{3}{4}{5}>", new Object[] { decl.getName(), additionalAtts, type.isLocal() ? "" : MessageFormat.format(" type=\"'{'{0}'}'{1}\"", new Object[] { type.getTargetNamespace(), type.getName() }), " fixed=\"" + decl.getFixedValue() + '"', " default=\"" + decl.getDefaultValue() + '"', type.isLocal() ? "" : " /" }));
    if (type.isLocal())
    {
      this.indent += 1;
      simpleType(type);
      this.indent -= 1;
      println("</attribute>");
    }
  }
  
  public void simpleType(XSSimpleType type)
  {
    println(MessageFormat.format("<simpleType{0}>", new Object[] { " name=\"" + type.getName() + '"' }));
    
    this.indent += 1;
    
    type.visit(this);
    
    this.indent -= 1;
    println("</simpleType>");
  }
  
  public void listSimpleType(XSListSimpleType type)
  {
    XSSimpleType itemType = type.getItemType();
    if (itemType.isLocal())
    {
      println("<list>");
      this.indent += 1;
      simpleType(itemType);
      this.indent -= 1;
      println("</list>");
    }
    else
    {
      println(MessageFormat.format("<list itemType=\"'{'{0}'}'{1}\" />", new Object[] { itemType.getTargetNamespace(), itemType.getName() }));
    }
  }
  
  public void unionSimpleType(XSUnionSimpleType type)
  {
    int len = type.getMemberSize();
    StringBuffer ref = new StringBuffer();
    for (int i = 0; i < len; i++)
    {
      XSSimpleType member = type.getMember(i);
      if (member.isGlobal()) {
        ref.append(MessageFormat.format(" '{'{0}'}'{1}", new Object[] { member.getTargetNamespace(), member.getName() }));
      }
    }
    if (ref.length() == 0) {
      println("<union>");
    } else {
      println("<union memberTypes=\"" + ref + "\">");
    }
    this.indent += 1;
    for (int i = 0; i < len; i++)
    {
      XSSimpleType member = type.getMember(i);
      if (member.isLocal()) {
        simpleType(member);
      }
    }
    this.indent -= 1;
    println("</union>");
  }
  
  public void restrictionSimpleType(XSRestrictionSimpleType type)
  {
    if (type.getBaseType() == null)
    {
      if (!type.getName().equals("anySimpleType")) {
        throw new InternalError();
      }
      if (!"http://www.w3.org/2001/XMLSchema".equals(type.getTargetNamespace())) {
        throw new InternalError();
      }
      return;
    }
    XSSimpleType baseType = type.getSimpleBaseType();
    
    println(MessageFormat.format("<restriction{0}>", new Object[] { " base=\"{" + baseType.getTargetNamespace() + '}' + baseType.getName() + '"' }));
    
    this.indent += 1;
    if (baseType.isLocal()) {
      simpleType(baseType);
    }
    Iterator itr = type.iterateDeclaredFacets();
    while (itr.hasNext()) {
      facet((XSFacet)itr.next());
    }
    this.indent -= 1;
    println("</restriction>");
  }
  
  public void facet(XSFacet facet)
  {
    println(MessageFormat.format("<{0} value=\"{1}\"/>", new Object[] { facet.getName(), facet.getValue() }));
  }
  
  public void notation(XSNotation notation)
  {
    println(MessageFormat.format("<notation name='\"0}\" public =\"{1}\" system=\"{2}\" />", new Object[] { notation.getName(), notation.getPublicId(), notation.getSystemId() }));
  }
  
  public void complexType(XSComplexType type)
  {
    println(MessageFormat.format("<complexType{0}>", new Object[] { " name=\"" + type.getName() + '"' }));
    
    this.indent += 1;
    if (type.getContentType().asSimpleType() != null)
    {
      println("<simpleContent>");
      this.indent += 1;
      
      XSType baseType = type.getBaseType();
      if (type.getDerivationMethod() == 2)
      {
        println(MessageFormat.format("<restriction base=\"<{0}>{1}\">", new Object[] { baseType.getTargetNamespace(), baseType.getName() }));
        
        this.indent += 1;
        
        dumpComplexTypeAttribute(type);
        
        this.indent -= 1;
        println("</restriction>");
      }
      else
      {
        println(MessageFormat.format("<extension base=\"<{0}>{1}\">", new Object[] { baseType.getTargetNamespace(), baseType.getName() }));
        if ((type.isGlobal()) && (type.getTargetNamespace().equals(baseType.getTargetNamespace())) && (type.getName().equals(baseType.getName())))
        {
          this.indent += 1;
          println("<redefine>");
          this.indent += 1;
          baseType.visit(this);
          this.indent -= 1;
          println("</redefine>");
          this.indent -= 1;
        }
        this.indent += 1;
        
        dumpComplexTypeAttribute(type);
        
        this.indent -= 1;
        println("</extension>");
      }
      this.indent -= 1;
      println("</simpleContent>");
    }
    else
    {
      println("<complexContent>");
      this.indent += 1;
      
      XSComplexType baseType = type.getBaseType().asComplexType();
      if (type.getDerivationMethod() == 2)
      {
        println(MessageFormat.format("<restriction base=\"'{'{0}'}'{1}\">", new Object[] { baseType.getTargetNamespace(), baseType.getName() }));
        
        this.indent += 1;
        
        type.getContentType().visit(this);
        dumpComplexTypeAttribute(type);
        
        this.indent -= 1;
        println("</restriction>");
      }
      else
      {
        println(MessageFormat.format("<extension base=\"'{'{0}'}'{1}\">", new Object[] { baseType.getTargetNamespace(), baseType.getName() }));
        if ((type.isGlobal()) && (type.getTargetNamespace().equals(baseType.getTargetNamespace())) && (type.getName().equals(baseType.getName())))
        {
          this.indent += 1;
          println("<redefine>");
          this.indent += 1;
          baseType.visit(this);
          this.indent -= 1;
          println("</redefine>");
          this.indent -= 1;
        }
        this.indent += 1;
        
        type.getExplicitContent().visit(this);
        dumpComplexTypeAttribute(type);
        
        this.indent -= 1;
        println("</extension>");
      }
      this.indent -= 1;
      println("</complexContent>");
    }
    this.indent -= 1;
    println("</complexType>");
  }
  
  private void dumpComplexTypeAttribute(XSComplexType type)
  {
    Iterator itr = type.iterateAttGroups();
    while (itr.hasNext()) {
      dumpRef((XSAttGroupDecl)itr.next());
    }
    itr = type.iterateDeclaredAttributeUses();
    while (itr.hasNext()) {
      attributeUse((XSAttributeUse)itr.next());
    }
  }
  
  public void elementDecl(XSElementDecl decl)
  {
    elementDecl(decl, "");
  }
  
  private void elementDecl(XSElementDecl decl, String extraAtts)
  {
    XSType type = decl.getType();
    
    println(MessageFormat.format("<element name=\"{0}\"{1}{2}{3}>", new Object[] { decl.getName(), " type=\"{" + type.getTargetNamespace() + '}' + type.getName() + '"', extraAtts, type.isLocal() ? "" : "/" }));
    if (type.isLocal())
    {
      this.indent += 1;
      if (type.isLocal()) {
        type.visit(this);
      }
      this.indent -= 1;
      println("</element>");
    }
  }
  
  public void modelGroupDecl(XSModelGroupDecl decl)
  {
    println(MessageFormat.format("<group name=\"{0}\">", new Object[] { decl.getName() }));
    
    this.indent += 1;
    
    modelGroup(decl.getModelGroup());
    
    this.indent -= 1;
    println("</group>");
  }
  
  public void modelGroup(XSModelGroup group)
  {
    modelGroup(group, "");
  }
  
  private void modelGroup(XSModelGroup group, String extraAtts)
  {
    println(MessageFormat.format("<{0}{1}>", new Object[] { group.getCompositor(), extraAtts }));
    
    this.indent += 1;
    
    int len = group.getSize();
    for (int i = 0; i < len; i++) {
      particle(group.getChild(i));
    }
    this.indent -= 1;
    println(MessageFormat.format("</{0}>", new Object[] { group.getCompositor() }));
  }
  
  public void particle(XSParticle part)
  {
    StringBuffer buf = new StringBuffer();
    
    int i = part.getMaxOccurs();
    if (i == -1) {
      buf.append(" maxOccurs=\"unbounded\"");
    } else if (i != 1) {
      buf.append(" maxOccurs=\"" + i + '"');
    }
    i = part.getMinOccurs();
    if (i != 1) {
      buf.append(" minOccurs=\"" + i + '"');
    }
    final String extraAtts = buf.toString();
    
    part.getTerm().visit(new XSTermVisitor()
    {
      public void elementDecl(XSElementDecl decl)
      {
        if (decl.isLocal()) {
          SchemaWriter.this.elementDecl(decl, extraAtts);
        } else {
          SchemaWriter.this.println(MessageFormat.format("<element ref=\"'{'{0}'}'{1}\"{2}/>", new Object[] { decl.getTargetNamespace(), decl.getName(), extraAtts }));
        }
      }
      
      public void modelGroupDecl(XSModelGroupDecl decl)
      {
        SchemaWriter.this.println(MessageFormat.format("<group ref=\"'{'{0}'}'{1}\"{2}/>", new Object[] { decl.getTargetNamespace(), decl.getName(), extraAtts }));
      }
      
      public void modelGroup(XSModelGroup group)
      {
        SchemaWriter.this.modelGroup(group, extraAtts);
      }
      
      public void wildcard(XSWildcard wc)
      {
        SchemaWriter.this.wildcard(wc, extraAtts);
      }
    });
  }
  
  public void wildcard(XSWildcard wc)
  {
    wildcard(wc, "");
  }
  
  private void wildcard(XSWildcard wc, String extraAtts)
  {
    println(MessageFormat.format("<any/>", new Object[] { extraAtts }));
  }
  
  public void annotation(XSAnnotation ann) {}
  
  public void identityConstraint(XSIdentityConstraint decl) {}
  
  public void xpath(XSXPath xp) {}
  
  public void empty(XSContentType t) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\util\SchemaWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */