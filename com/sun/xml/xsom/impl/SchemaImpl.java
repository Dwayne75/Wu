package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.ForeignAttributes;
import com.sun.xml.xsom.SCD;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.parser.SchemaDocument;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
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
  
  public SchemaDocument getSourceDocument()
  {
    return null;
  }
  
  public SchemaSetImpl getRoot()
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
  
  public XSAnnotation getAnnotation(boolean createIfNotExist)
  {
    if ((createIfNotExist) && (this.annotation == null)) {
      this.annotation = new AnnotationImpl();
    }
    return this.annotation;
  }
  
  public Locator getLocator()
  {
    return this.locator;
  }
  
  private final Map<String, XSAttributeDecl> atts = new HashMap();
  private final Map<String, XSAttributeDecl> attsView = Collections.unmodifiableMap(this.atts);
  
  public void addAttributeDecl(XSAttributeDecl newDecl)
  {
    this.atts.put(newDecl.getName(), newDecl);
  }
  
  public Map<String, XSAttributeDecl> getAttributeDecls()
  {
    return this.attsView;
  }
  
  public XSAttributeDecl getAttributeDecl(String name)
  {
    return (XSAttributeDecl)this.atts.get(name);
  }
  
  public Iterator<XSAttributeDecl> iterateAttributeDecls()
  {
    return this.atts.values().iterator();
  }
  
  private final Map<String, XSElementDecl> elems = new HashMap();
  private final Map<String, XSElementDecl> elemsView = Collections.unmodifiableMap(this.elems);
  
  public void addElementDecl(XSElementDecl newDecl)
  {
    this.elems.put(newDecl.getName(), newDecl);
  }
  
  public Map<String, XSElementDecl> getElementDecls()
  {
    return this.elemsView;
  }
  
  public XSElementDecl getElementDecl(String name)
  {
    return (XSElementDecl)this.elems.get(name);
  }
  
  public Iterator<XSElementDecl> iterateElementDecls()
  {
    return this.elems.values().iterator();
  }
  
  private final Map<String, XSAttGroupDecl> attGroups = new HashMap();
  private final Map<String, XSAttGroupDecl> attGroupsView = Collections.unmodifiableMap(this.attGroups);
  
  public void addAttGroupDecl(XSAttGroupDecl newDecl, boolean overwrite)
  {
    if ((overwrite) || (!this.attGroups.containsKey(newDecl.getName()))) {
      this.attGroups.put(newDecl.getName(), newDecl);
    }
  }
  
  public Map<String, XSAttGroupDecl> getAttGroupDecls()
  {
    return this.attGroupsView;
  }
  
  public XSAttGroupDecl getAttGroupDecl(String name)
  {
    return (XSAttGroupDecl)this.attGroups.get(name);
  }
  
  public Iterator<XSAttGroupDecl> iterateAttGroupDecls()
  {
    return this.attGroups.values().iterator();
  }
  
  private final Map<String, XSNotation> notations = new HashMap();
  private final Map<String, XSNotation> notationsView = Collections.unmodifiableMap(this.notations);
  
  public void addNotation(XSNotation newDecl)
  {
    this.notations.put(newDecl.getName(), newDecl);
  }
  
  public Map<String, XSNotation> getNotations()
  {
    return this.notationsView;
  }
  
  public XSNotation getNotation(String name)
  {
    return (XSNotation)this.notations.get(name);
  }
  
  public Iterator<XSNotation> iterateNotations()
  {
    return this.notations.values().iterator();
  }
  
  private final Map<String, XSModelGroupDecl> modelGroups = new HashMap();
  private final Map<String, XSModelGroupDecl> modelGroupsView = Collections.unmodifiableMap(this.modelGroups);
  
  public void addModelGroupDecl(XSModelGroupDecl newDecl, boolean overwrite)
  {
    if ((overwrite) || (!this.modelGroups.containsKey(newDecl.getName()))) {
      this.modelGroups.put(newDecl.getName(), newDecl);
    }
  }
  
  public Map<String, XSModelGroupDecl> getModelGroupDecls()
  {
    return this.modelGroupsView;
  }
  
  public XSModelGroupDecl getModelGroupDecl(String name)
  {
    return (XSModelGroupDecl)this.modelGroups.get(name);
  }
  
  public Iterator<XSModelGroupDecl> iterateModelGroupDecls()
  {
    return this.modelGroups.values().iterator();
  }
  
  private final Map<String, XSIdentityConstraint> idConstraints = new HashMap();
  private final Map<String, XSIdentityConstraint> idConstraintsView = Collections.unmodifiableMap(this.idConstraints);
  
  protected void addIdentityConstraint(IdentityConstraintImpl c)
  {
    this.idConstraints.put(c.getName(), c);
  }
  
  public Map<String, XSIdentityConstraint> getIdentityConstraints()
  {
    return this.idConstraintsView;
  }
  
  public XSIdentityConstraint getIdentityConstraint(String localName)
  {
    return (XSIdentityConstraint)this.idConstraints.get(localName);
  }
  
  private final Map<String, XSType> allTypes = new HashMap();
  private final Map<String, XSType> allTypesView = Collections.unmodifiableMap(this.allTypes);
  private final Map<String, XSSimpleType> simpleTypes = new HashMap();
  private final Map<String, XSSimpleType> simpleTypesView = Collections.unmodifiableMap(this.simpleTypes);
  
  public void addSimpleType(XSSimpleType newDecl, boolean overwrite)
  {
    if ((overwrite) || (!this.simpleTypes.containsKey(newDecl.getName())))
    {
      this.simpleTypes.put(newDecl.getName(), newDecl);
      this.allTypes.put(newDecl.getName(), newDecl);
    }
  }
  
  public Map<String, XSSimpleType> getSimpleTypes()
  {
    return this.simpleTypesView;
  }
  
  public XSSimpleType getSimpleType(String name)
  {
    return (XSSimpleType)this.simpleTypes.get(name);
  }
  
  public Iterator<XSSimpleType> iterateSimpleTypes()
  {
    return this.simpleTypes.values().iterator();
  }
  
  private final Map<String, XSComplexType> complexTypes = new HashMap();
  private final Map<String, XSComplexType> complexTypesView = Collections.unmodifiableMap(this.complexTypes);
  
  public void addComplexType(XSComplexType newDecl, boolean overwrite)
  {
    if ((overwrite) || (!this.complexTypes.containsKey(newDecl.getName())))
    {
      this.complexTypes.put(newDecl.getName(), newDecl);
      this.allTypes.put(newDecl.getName(), newDecl);
    }
  }
  
  public Map<String, XSComplexType> getComplexTypes()
  {
    return this.complexTypesView;
  }
  
  public XSComplexType getComplexType(String name)
  {
    return (XSComplexType)this.complexTypes.get(name);
  }
  
  public Iterator<XSComplexType> iterateComplexTypes()
  {
    return this.complexTypes.values().iterator();
  }
  
  public Map<String, XSType> getTypes()
  {
    return this.allTypesView;
  }
  
  public XSType getType(String name)
  {
    return (XSType)this.allTypes.get(name);
  }
  
  public Iterator<XSType> iterateTypes()
  {
    return this.allTypes.values().iterator();
  }
  
  public void visit(XSVisitor visitor)
  {
    visitor.schema(this);
  }
  
  public Object apply(XSFunction function)
  {
    return function.schema(this);
  }
  
  private List<ForeignAttributes> foreignAttributes = null;
  private List<ForeignAttributes> readOnlyForeignAttributes = null;
  
  public void addForeignAttributes(ForeignAttributesImpl fa)
  {
    if (this.foreignAttributes == null) {
      this.foreignAttributes = new ArrayList();
    }
    this.foreignAttributes.add(fa);
  }
  
  public List<ForeignAttributes> getForeignAttributes()
  {
    if (this.readOnlyForeignAttributes == null) {
      if (this.foreignAttributes == null) {
        this.readOnlyForeignAttributes = Collections.EMPTY_LIST;
      } else {
        this.readOnlyForeignAttributes = Collections.unmodifiableList(this.foreignAttributes);
      }
    }
    return this.readOnlyForeignAttributes;
  }
  
  public String getForeignAttribute(String nsUri, String localName)
  {
    for (ForeignAttributes fa : getForeignAttributes())
    {
      String v = fa.getValue(nsUri, localName);
      if (v != null) {
        return v;
      }
    }
    return null;
  }
  
  public Collection<XSComponent> select(String scd, NamespaceContext nsContext)
  {
    try
    {
      return SCD.create(scd, nsContext).select(this);
    }
    catch (ParseException e)
    {
      throw new IllegalArgumentException(e);
    }
  }
  
  public XSComponent selectSingle(String scd, NamespaceContext nsContext)
  {
    try
    {
      return SCD.create(scd, nsContext).selectSingle(this);
    }
    catch (ParseException e)
    {
      throw new IllegalArgumentException(e);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\SchemaImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */