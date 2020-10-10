package com.sun.xml.xsom.impl.parser;

import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.impl.Ref.AttGroup;
import com.sun.xml.xsom.impl.Ref.Attribute;
import com.sun.xml.xsom.impl.Ref.ComplexType;
import com.sun.xml.xsom.impl.Ref.Element;
import com.sun.xml.xsom.impl.Ref.IdentityConstraint;
import com.sun.xml.xsom.impl.Ref.SimpleType;
import com.sun.xml.xsom.impl.Ref.Term;
import com.sun.xml.xsom.impl.Ref.Type;
import com.sun.xml.xsom.impl.SchemaImpl;
import com.sun.xml.xsom.impl.UName;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public abstract class DelayedRef
  implements Patch
{
  protected final XSSchemaSet schema;
  private PatcherManager manager;
  private UName name;
  private Locator source;
  
  DelayedRef(PatcherManager _manager, Locator _source, SchemaImpl _schema, UName _name)
  {
    this.schema = _schema.getRoot();
    this.manager = _manager;
    this.name = _name;
    this.source = _source;
    if (this.name == null) {
      throw new InternalError();
    }
    this.manager.addPatcher(this);
  }
  
  public void run()
    throws SAXException
  {
    if (this.ref == null) {
      resolve();
    }
    this.manager = null;
    this.name = null;
    this.source = null;
  }
  
  private Object ref = null;
  
  protected abstract Object resolveReference(UName paramUName);
  
  protected abstract String getErrorProperty();
  
  protected final Object _get()
  {
    if (this.ref == null) {
      throw new InternalError("unresolved reference");
    }
    return this.ref;
  }
  
  private void resolve()
    throws SAXException
  {
    this.ref = resolveReference(this.name);
    if (this.ref == null) {
      this.manager.reportError(Messages.format(getErrorProperty(), new Object[] { this.name.getQualifiedName() }), this.source);
    }
  }
  
  public void redefine(XSDeclaration d)
  {
    if ((!d.getTargetNamespace().equals(this.name.getNamespaceURI())) || (!d.getName().equals(this.name.getName()))) {
      return;
    }
    this.ref = d;
    this.manager = null;
    this.name = null;
    this.source = null;
  }
  
  public static class Type
    extends DelayedRef
    implements Ref.Type
  {
    public Type(PatcherManager manager, Locator loc, SchemaImpl schema, UName name)
    {
      super(loc, schema, name);
    }
    
    protected Object resolveReference(UName name)
    {
      Object o = this.schema.getSimpleType(name.getNamespaceURI(), name.getName());
      if (o != null) {
        return o;
      }
      return this.schema.getComplexType(name.getNamespaceURI(), name.getName());
    }
    
    protected String getErrorProperty()
    {
      return "UndefinedType";
    }
    
    public XSType getType()
    {
      return (XSType)super._get();
    }
  }
  
  public static class SimpleType
    extends DelayedRef
    implements Ref.SimpleType
  {
    public SimpleType(PatcherManager manager, Locator loc, SchemaImpl schema, UName name)
    {
      super(loc, schema, name);
    }
    
    public XSSimpleType getType()
    {
      return (XSSimpleType)_get();
    }
    
    protected Object resolveReference(UName name)
    {
      return this.schema.getSimpleType(name.getNamespaceURI(), name.getName());
    }
    
    protected String getErrorProperty()
    {
      return "UndefinedSimpleType";
    }
  }
  
  public static class ComplexType
    extends DelayedRef
    implements Ref.ComplexType
  {
    public ComplexType(PatcherManager manager, Locator loc, SchemaImpl schema, UName name)
    {
      super(loc, schema, name);
    }
    
    protected Object resolveReference(UName name)
    {
      return this.schema.getComplexType(name.getNamespaceURI(), name.getName());
    }
    
    protected String getErrorProperty()
    {
      return "UndefinedCompplexType";
    }
    
    public XSComplexType getType()
    {
      return (XSComplexType)super._get();
    }
  }
  
  public static class Element
    extends DelayedRef
    implements Ref.Element
  {
    public Element(PatcherManager manager, Locator loc, SchemaImpl schema, UName name)
    {
      super(loc, schema, name);
    }
    
    protected Object resolveReference(UName name)
    {
      return this.schema.getElementDecl(name.getNamespaceURI(), name.getName());
    }
    
    protected String getErrorProperty()
    {
      return "UndefinedElement";
    }
    
    public XSElementDecl get()
    {
      return (XSElementDecl)super._get();
    }
    
    public XSTerm getTerm()
    {
      return get();
    }
  }
  
  public static class ModelGroup
    extends DelayedRef
    implements Ref.Term
  {
    public ModelGroup(PatcherManager manager, Locator loc, SchemaImpl schema, UName name)
    {
      super(loc, schema, name);
    }
    
    protected Object resolveReference(UName name)
    {
      return this.schema.getModelGroupDecl(name.getNamespaceURI(), name.getName());
    }
    
    protected String getErrorProperty()
    {
      return "UndefinedModelGroup";
    }
    
    public XSModelGroupDecl get()
    {
      return (XSModelGroupDecl)super._get();
    }
    
    public XSTerm getTerm()
    {
      return get();
    }
  }
  
  public static class AttGroup
    extends DelayedRef
    implements Ref.AttGroup
  {
    public AttGroup(PatcherManager manager, Locator loc, SchemaImpl schema, UName name)
    {
      super(loc, schema, name);
    }
    
    protected Object resolveReference(UName name)
    {
      return this.schema.getAttGroupDecl(name.getNamespaceURI(), name.getName());
    }
    
    protected String getErrorProperty()
    {
      return "UndefinedAttributeGroup";
    }
    
    public XSAttGroupDecl get()
    {
      return (XSAttGroupDecl)super._get();
    }
  }
  
  public static class Attribute
    extends DelayedRef
    implements Ref.Attribute
  {
    public Attribute(PatcherManager manager, Locator loc, SchemaImpl schema, UName name)
    {
      super(loc, schema, name);
    }
    
    protected Object resolveReference(UName name)
    {
      return this.schema.getAttributeDecl(name.getNamespaceURI(), name.getName());
    }
    
    protected String getErrorProperty()
    {
      return "UndefinedAttribute";
    }
    
    public XSAttributeDecl getAttribute()
    {
      return (XSAttributeDecl)super._get();
    }
  }
  
  public static class IdentityConstraint
    extends DelayedRef
    implements Ref.IdentityConstraint
  {
    public IdentityConstraint(PatcherManager manager, Locator loc, SchemaImpl schema, UName name)
    {
      super(loc, schema, name);
    }
    
    protected Object resolveReference(UName name)
    {
      return this.schema.getIdentityConstraint(name.getNamespaceURI(), name.getName());
    }
    
    protected String getErrorProperty()
    {
      return "UndefinedIdentityConstraint";
    }
    
    public XSIdentityConstraint get()
    {
      return (XSIdentityConstraint)super._get();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\parser\DelayedRef.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */