package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import org.xml.sax.Locator;

public class SchemaSetImpl
  implements XSSchemaSet
{
  private final Map schemas;
  private final Vector schemas2;
  public final EmptyImpl empty;
  public final SchemaSetImpl.AnySimpleType anySimpleType;
  public final SchemaSetImpl.AnyType anyType;
  
  public SchemaImpl createSchema(String targetNamespace, Locator location)
  {
    SchemaImpl obj = (SchemaImpl)this.schemas.get(targetNamespace);
    if (obj == null)
    {
      obj = new SchemaImpl(this, location, targetNamespace);
      this.schemas.put(targetNamespace, obj);
      this.schemas2.add(obj);
    }
    return obj;
  }
  
  public int getSchemaSize()
  {
    return this.schemas.size();
  }
  
  public XSSchema getSchema(String targetNamespace)
  {
    return (XSSchema)this.schemas.get(targetNamespace);
  }
  
  public XSSchema getSchema(int idx)
  {
    return (XSSchema)this.schemas2.get(idx);
  }
  
  public Iterator iterateSchema()
  {
    return this.schemas2.iterator();
  }
  
  public XSSimpleType getSimpleType(String ns, String localName)
  {
    XSSchema schema = getSchema(ns);
    if (schema == null) {
      return null;
    }
    return schema.getSimpleType(localName);
  }
  
  public XSElementDecl getElementDecl(String ns, String localName)
  {
    XSSchema schema = getSchema(ns);
    if (schema == null) {
      return null;
    }
    return schema.getElementDecl(localName);
  }
  
  public XSAttributeDecl getAttributeDecl(String ns, String localName)
  {
    XSSchema schema = getSchema(ns);
    if (schema == null) {
      return null;
    }
    return schema.getAttributeDecl(localName);
  }
  
  public XSModelGroupDecl getModelGroupDecl(String ns, String localName)
  {
    XSSchema schema = getSchema(ns);
    if (schema == null) {
      return null;
    }
    return schema.getModelGroupDecl(localName);
  }
  
  public XSAttGroupDecl getAttGroupDecl(String ns, String localName)
  {
    XSSchema schema = getSchema(ns);
    if (schema == null) {
      return null;
    }
    return schema.getAttGroupDecl(localName);
  }
  
  public XSComplexType getComplexType(String ns, String localName)
  {
    XSSchema schema = getSchema(ns);
    if (schema == null) {
      return null;
    }
    return schema.getComplexType(localName);
  }
  
  public Iterator iterateElementDecls()
  {
    return new SchemaSetImpl.1(this);
  }
  
  public Iterator iterateTypes()
  {
    return new SchemaSetImpl.2(this);
  }
  
  public Iterator iterateAttributeDecls()
  {
    return new SchemaSetImpl.3(this);
  }
  
  public Iterator iterateAttGroupDecls()
  {
    return new SchemaSetImpl.4(this);
  }
  
  public Iterator iterateModelGroupDecls()
  {
    return new SchemaSetImpl.5(this);
  }
  
  public Iterator iterateSimpleTypes()
  {
    return new SchemaSetImpl.6(this);
  }
  
  public Iterator iterateComplexTypes()
  {
    return new SchemaSetImpl.7(this);
  }
  
  public Iterator iterateNotations()
  {
    return new SchemaSetImpl.8(this);
  }
  
  public XSContentType getEmpty()
  {
    return this.empty;
  }
  
  public XSSimpleType getAnySimpleType()
  {
    return this.anySimpleType;
  }
  
  public XSComplexType getAnyType()
  {
    return this.anyType;
  }
  
  public SchemaSetImpl()
  {
    this.schemas = new HashMap();
    this.schemas2 = new Vector();
    
    this.empty = new EmptyImpl();
    
    this.anySimpleType = new SchemaSetImpl.AnySimpleType(this);
    
    this.anyType = new SchemaSetImpl.AnyType(this);
  }
  
  private static final Iterator emptyIterator = new SchemaSetImpl.9();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\SchemaSetImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */