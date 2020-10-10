package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSWildcard.Any;
import com.sun.xml.xsom.XSWildcard.Other;
import com.sun.xml.xsom.XSWildcard.Union;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSTermFunction;
import com.sun.xml.xsom.visitor.XSTermFunctionWithParam;
import com.sun.xml.xsom.visitor.XSTermVisitor;
import com.sun.xml.xsom.visitor.XSVisitor;
import com.sun.xml.xsom.visitor.XSWildcardFunction;
import com.sun.xml.xsom.visitor.XSWildcardVisitor;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.xml.sax.Locator;

public abstract class WildcardImpl
  extends ComponentImpl
  implements XSWildcard, Ref.Term
{
  private final int mode;
  
  protected WildcardImpl(SchemaDocumentImpl owner, AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl _fa, int _mode)
  {
    super(owner, _annon, _loc, _fa);
    this.mode = _mode;
  }
  
  public int getMode()
  {
    return this.mode;
  }
  
  public WildcardImpl union(SchemaDocumentImpl owner, WildcardImpl rhs)
  {
    if (((this instanceof Any)) || ((rhs instanceof Any))) {
      return new Any(owner, null, null, null, this.mode);
    }
    if (((this instanceof Finite)) && ((rhs instanceof Finite)))
    {
      Set<String> values = new HashSet();
      values.addAll(((Finite)this).names);
      values.addAll(((Finite)rhs).names);
      return new Finite(owner, null, null, null, values, this.mode);
    }
    if (((this instanceof Other)) && ((rhs instanceof Other)))
    {
      if (((Other)this).otherNamespace.equals(((Other)rhs).otherNamespace)) {
        return new Other(owner, null, null, null, ((Other)this).otherNamespace, this.mode);
      }
      return new Other(owner, null, null, null, "", this.mode);
    }
    Finite f;
    Other o;
    Finite f;
    if ((this instanceof Other))
    {
      Other o = (Other)this;f = (Finite)rhs;
    }
    else
    {
      o = (Other)rhs;f = (Finite)this;
    }
    if (f.names.contains(o.otherNamespace)) {
      return new Any(owner, null, null, null, this.mode);
    }
    return new Other(owner, null, null, null, o.otherNamespace, this.mode);
  }
  
  public static final class Any
    extends WildcardImpl
    implements XSWildcard.Any
  {
    public Any(SchemaDocumentImpl owner, AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl _fa, int _mode)
    {
      super(_annon, _loc, _fa, _mode);
    }
    
    public boolean acceptsNamespace(String namespaceURI)
    {
      return true;
    }
    
    public void visit(XSWildcardVisitor visitor)
    {
      visitor.any(this);
    }
    
    public Object apply(XSWildcardFunction function)
    {
      return function.any(this);
    }
  }
  
  public static final class Other
    extends WildcardImpl
    implements XSWildcard.Other
  {
    private final String otherNamespace;
    
    public Other(SchemaDocumentImpl owner, AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl _fa, String otherNamespace, int _mode)
    {
      super(_annon, _loc, _fa, _mode);
      this.otherNamespace = otherNamespace;
    }
    
    public String getOtherNamespace()
    {
      return this.otherNamespace;
    }
    
    public boolean acceptsNamespace(String namespaceURI)
    {
      return !namespaceURI.equals(this.otherNamespace);
    }
    
    public void visit(XSWildcardVisitor visitor)
    {
      visitor.other(this);
    }
    
    public Object apply(XSWildcardFunction function)
    {
      return function.other(this);
    }
  }
  
  public static final class Finite
    extends WildcardImpl
    implements XSWildcard.Union
  {
    private final Set<String> names;
    private final Set<String> namesView;
    
    public Finite(SchemaDocumentImpl owner, AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl _fa, Set<String> ns, int _mode)
    {
      super(_annon, _loc, _fa, _mode);
      this.names = ns;
      this.namesView = Collections.unmodifiableSet(this.names);
    }
    
    public Iterator<String> iterateNamespaces()
    {
      return this.names.iterator();
    }
    
    public Collection<String> getNamespaces()
    {
      return this.namesView;
    }
    
    public boolean acceptsNamespace(String namespaceURI)
    {
      return this.names.contains(namespaceURI);
    }
    
    public void visit(XSWildcardVisitor visitor)
    {
      visitor.union(this);
    }
    
    public Object apply(XSWildcardFunction function)
    {
      return function.union(this);
    }
  }
  
  public final void visit(XSVisitor visitor)
  {
    visitor.wildcard(this);
  }
  
  public final void visit(XSTermVisitor visitor)
  {
    visitor.wildcard(this);
  }
  
  public Object apply(XSTermFunction function)
  {
    return function.wildcard(this);
  }
  
  public <T, P> T apply(XSTermFunctionWithParam<T, P> function, P param)
  {
    return (T)function.wildcard(this, param);
  }
  
  public Object apply(XSFunction function)
  {
    return function.wildcard(this);
  }
  
  public boolean isWildcard()
  {
    return true;
  }
  
  public boolean isModelGroupDecl()
  {
    return false;
  }
  
  public boolean isModelGroup()
  {
    return false;
  }
  
  public boolean isElementDecl()
  {
    return false;
  }
  
  public XSWildcard asWildcard()
  {
    return this;
  }
  
  public XSModelGroupDecl asModelGroupDecl()
  {
    return null;
  }
  
  public XSModelGroup asModelGroup()
  {
    return null;
  }
  
  public XSElementDecl asElementDecl()
  {
    return null;
  }
  
  public XSTerm getTerm()
  {
    return this;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\WildcardImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */