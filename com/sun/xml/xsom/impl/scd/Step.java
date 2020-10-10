package com.sun.xml.xsom.impl.scd;

import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.impl.UName;
import java.util.Iterator;

public abstract class Step<T extends XSComponent>
{
  public final Axis<? extends T> axis;
  int predicate = -1;
  
  protected Step(Axis<? extends T> axis)
  {
    this.axis = axis;
  }
  
  protected abstract Iterator<? extends T> filter(Iterator<? extends T> paramIterator);
  
  public final Iterator<T> evaluate(Iterator<XSComponent> nodeSet)
  {
    Iterator<T> r = new Iterators.Map(nodeSet)
    {
      protected Iterator<? extends T> apply(XSComponent contextNode)
      {
        return Step.this.filter(Step.this.axis.iterator(contextNode));
      }
    };
    r = new Iterators.Unique(r);
    if (this.predicate >= 0)
    {
      T item = null;
      for (int i = this.predicate; i > 0; i--)
      {
        if (!r.hasNext()) {
          return Iterators.empty();
        }
        item = (XSComponent)r.next();
      }
      return new Iterators.Singleton(item);
    }
    return r;
  }
  
  static final class Any
    extends Step<XSComponent>
  {
    public Any(Axis<? extends XSComponent> axis)
    {
      super();
    }
    
    protected Iterator<? extends XSComponent> filter(Iterator<? extends XSComponent> base)
    {
      return base;
    }
  }
  
  private static abstract class Filtered<T extends XSComponent>
    extends Step<T>
  {
    protected Filtered(Axis<? extends T> axis)
    {
      super();
    }
    
    protected Iterator<T> filter(Iterator<? extends T> base)
    {
      new Iterators.Filter(base)
      {
        protected boolean matches(T d)
        {
          return Step.Filtered.this.match(d);
        }
      };
    }
    
    protected abstract boolean match(T paramT);
  }
  
  static final class Named
    extends Step.Filtered<XSDeclaration>
  {
    private final String nsUri;
    private final String localName;
    
    public Named(Axis<? extends XSDeclaration> axis, UName n)
    {
      this(axis, n.getNamespaceURI(), n.getName());
    }
    
    public Named(Axis<? extends XSDeclaration> axis, String nsUri, String localName)
    {
      super();
      this.nsUri = nsUri;
      this.localName = localName;
    }
    
    protected boolean match(XSDeclaration d)
    {
      return (d.getName().equals(this.localName)) && (d.getTargetNamespace().equals(this.nsUri));
    }
  }
  
  static final class AnonymousType
    extends Step.Filtered<XSType>
  {
    public AnonymousType(Axis<? extends XSType> axis)
    {
      super();
    }
    
    protected boolean match(XSType node)
    {
      return node.isLocal();
    }
  }
  
  static final class Facet
    extends Step.Filtered<XSFacet>
  {
    private final String name;
    
    public Facet(Axis<XSFacet> axis, String facetName)
    {
      super();
      this.name = facetName;
    }
    
    protected boolean match(XSFacet f)
    {
      return f.getName().equals(this.name);
    }
  }
  
  static final class Schema
    extends Step.Filtered<XSSchema>
  {
    private final String uri;
    
    public Schema(Axis<XSSchema> axis, String uri)
    {
      super();
      this.uri = uri;
    }
    
    protected boolean match(XSSchema d)
    {
      return d.getTargetNamespace().equals(this.uri);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\scd\Step.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */