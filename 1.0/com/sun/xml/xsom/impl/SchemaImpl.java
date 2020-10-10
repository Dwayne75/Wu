package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.xml.sax.Locator;

public class SchemaImpl
  implements XSSchema
{
  protected final SchemaSetImpl parent;
  private final String targetNamespace;
  private XSAnnotation annotation;
  private final Locator locator;
  
  public SchemaImpl(SchemaSetImpl _parent, Locator loc, String tns)
  {
    if (tns == null) {
      throw new IllegalArgumentException();
    }
    this.targetNamespace = tns;
    this.parent = _parent;
    this.locator = loc;
  }
  
  public SchemaSetImpl getParent()
  {
    return this.parent;
  }
  
  public String getTargetNamespace()
  {
    return this.targetNamespace;
  }
  
  public XSSchema getOwnerSchema()
  {
    return this;
  }
  
  public void setAnnotation(XSAnnotation a)
  {
    this.annotation = a;
  }
  
  public XSAnnotation getAnnotation()
  {
    return this.annotation;
  }
  
  public Locator getLocator()
  {
    return this.locator;
  }
  
  private final Map atts = new HashMap();
  
  public void addAttributeDecl(XSAttributeDecl newDecl)
  {
    this.atts.put(newDecl.getName(), newDecl);
  }
  
  public XSAttributeDecl getAttributeDecl(String name)
  {
    return (XSAttributeDecl)this.atts.get(name);
  }
  
  public Iterator iterateAttributeDecls()
  {
    return this.atts.values().iterator();
  }
  
  private final Map elems = new HashMap();
  
  public void addElementDecl(XSElementDecl newDecl)
  {
    this.elems.put(newDecl.getName(), newDecl);
  }
  
  public XSElementDecl getElementDecl(String name)
  {
    return (XSElementDecl)this.elems.get(name);
  }
  
  public Iterator iterateElementDecls()
  {
    return this.elems.values().iterator();
  }
  
  private final Map attGroups = new HashMap();
  
  public void addAttGroupDecl(XSAttGroupDecl newDecl)
  {
    this.attGroups.put(newDecl.getName(), newDecl);
  }
  
  public XSAttGroupDecl getAttGroupDecl(String name)
  {
    return (XSAttGroupDecl)this.attGroups.get(name);
  }
  
  public Iterator iterateAttGroupDecls()
  {
    return this.attGroups.values().iterator();
  }
  
  private final Map notations = new HashMap();
  
  public void addNotation(XSNotation newDecl)
  {
    this.notations.put(newDecl.getName(), newDecl);
  }
  
  public XSNotation getNotation(String name)
  {
    return (XSNotation)this.notations.get(name);
  }
  
  public Iterator iterateNotations()
  {
    return this.notations.values().iterator();
  }
  
  private final Map modelGroups = new HashMap();
  
  public void addModelGroupDecl(XSModelGroupDecl newDecl)
  {
    this.modelGroups.put(newDecl.getName(), newDecl);
  }
  
  public XSModelGroupDecl getModelGroupDecl(String name)
  {
    return (XSModelGroupDecl)this.modelGroups.get(name);
  }
  
  public Iterator iterateModelGroupDecls()
  {
    return this.modelGroups.values().iterator();
  }
  
  private final Map simpleTypes = new HashMap();
  
  public void addSimpleType(XSSimpleType newDecl)
  {
    this.simpleTypes.put(newDecl.getName(), newDecl);
  }
  
  public XSSimpleType getSimpleType(String name)
  {
    return (XSSimpleType)this.simpleTypes.get(name);
  }
  
  public Iterator iterateSimpleTypes()
  {
    return this.simpleTypes.values().iterator();
  }
  
  private final Map complexTypes = new HashMap();
  
  public void addComplexType(XSComplexType newDecl)
  {
    this.complexTypes.put(newDecl.getName(), newDecl);
  }
  
  public XSComplexType getComplexType(String name)
  {
    return (XSComplexType)this.complexTypes.get(name);
  }
  
  public Iterator iterateComplexTypes()
  {
    return this.complexTypes.values().iterator();
  }
  
  public XSType getType(String name)
  {
    XSType r = (XSType)this.complexTypes.get(name);
    if (r != null) {
      return r;
    }
    return (XSType)this.simpleTypes.get(name);
  }
  
  public Iterator iterateTypes()
  {
    Iterator itr1 = iterateComplexTypes();
    Iterator itr2 = iterateSimpleTypes();
    
    return new SchemaImpl.1(this, itr1, itr2);
  }
  
  public void visit(XSVisitor visitor)
  {
    visitor.schema(this);
  }
  
  public Object apply(XSFunction function)
  {
    return function.schema(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\SchemaImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */