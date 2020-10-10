package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.SCD;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;
import com.sun.xml.xsom.parser.SchemaDocument;
import com.sun.xml.xsom.util.ComponentNameFunction;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import org.xml.sax.Locator;

public abstract class ComponentImpl
  implements XSComponent
{
  protected final SchemaDocumentImpl ownerDocument;
  private AnnotationImpl annotation;
  private final Locator locator;
  private Object foreignAttributes;
  
  protected ComponentImpl(SchemaDocumentImpl _owner, AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl fa)
  {
    this.ownerDocument = _owner;
    this.annotation = _annon;
    this.locator = _loc;
    this.foreignAttributes = fa;
  }
  
  public SchemaImpl getOwnerSchema()
  {
    if (this.ownerDocument == null) {
      return null;
    }
    return this.ownerDocument.getSchema();
  }
  
  public XSSchemaSet getRoot()
  {
    if (this.ownerDocument == null) {
      return null;
    }
    return getOwnerSchema().getRoot();
  }
  
  public SchemaDocument getSourceDocument()
  {
    return this.ownerDocument;
  }
  
  public final XSAnnotation getAnnotation()
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
  
  public final Locator getLocator()
  {
    return this.locator;
  }
  
  public List<ForeignAttributesImpl> getForeignAttributes()
  {
    Object t = this.foreignAttributes;
    if (t == null) {
      return Collections.EMPTY_LIST;
    }
    if ((t instanceof List)) {
      return (List)t;
    }
    t = this.foreignAttributes = convertToList((ForeignAttributesImpl)t);
    return (List)t;
  }
  
  public String getForeignAttribute(String nsUri, String localName)
  {
    for (ForeignAttributesImpl fa : getForeignAttributes())
    {
      String v = fa.getValue(nsUri, localName);
      if (v != null) {
        return v;
      }
    }
    return null;
  }
  
  private List<ForeignAttributesImpl> convertToList(ForeignAttributesImpl fa)
  {
    List<ForeignAttributesImpl> lst = new ArrayList();
    while (fa != null)
    {
      lst.add(fa);
      fa = fa.next;
    }
    return Collections.unmodifiableList(lst);
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
  
  public String toString()
  {
    return (String)apply(new ComponentNameFunction());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\ComponentImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */