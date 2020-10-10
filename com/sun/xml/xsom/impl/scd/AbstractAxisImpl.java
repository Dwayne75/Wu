package com.sun.xml.xsom.impl.scd;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.visitor.XSFunction;
import java.util.Iterator;

abstract class AbstractAxisImpl<T extends XSComponent>
  implements Axis<T>, XSFunction<Iterator<T>>
{
  protected final Iterator<T> singleton(T t)
  {
    return Iterators.singleton(t);
  }
  
  protected final Iterator<T> union(T... items)
  {
    return new Iterators.Array(items);
  }
  
  protected final Iterator<T> union(Iterator<? extends T> first, Iterator<? extends T> second)
  {
    return new Iterators.Union(first, second);
  }
  
  public Iterator<T> iterator(XSComponent contextNode)
  {
    return (Iterator)contextNode.apply(this);
  }
  
  public String getName()
  {
    return toString();
  }
  
  public Iterator<T> iterator(Iterator<? extends XSComponent> contextNodes)
  {
    new Iterators.Map(contextNodes)
    {
      protected Iterator<? extends T> apply(XSComponent u)
      {
        return AbstractAxisImpl.this.iterator(u);
      }
    };
  }
  
  public boolean isModelGroup()
  {
    return false;
  }
  
  public Iterator<T> annotation(XSAnnotation ann)
  {
    return empty();
  }
  
  public Iterator<T> attGroupDecl(XSAttGroupDecl decl)
  {
    return empty();
  }
  
  public Iterator<T> attributeDecl(XSAttributeDecl decl)
  {
    return empty();
  }
  
  public Iterator<T> attributeUse(XSAttributeUse use)
  {
    return empty();
  }
  
  public Iterator<T> complexType(XSComplexType type)
  {
    XSParticle p = type.getContentType().asParticle();
    if (p != null) {
      return particle(p);
    }
    return empty();
  }
  
  public Iterator<T> schema(XSSchema schema)
  {
    return empty();
  }
  
  public Iterator<T> facet(XSFacet facet)
  {
    return empty();
  }
  
  public Iterator<T> notation(XSNotation notation)
  {
    return empty();
  }
  
  public Iterator<T> identityConstraint(XSIdentityConstraint decl)
  {
    return empty();
  }
  
  public Iterator<T> xpath(XSXPath xpath)
  {
    return empty();
  }
  
  public Iterator<T> simpleType(XSSimpleType simpleType)
  {
    return empty();
  }
  
  public Iterator<T> particle(XSParticle particle)
  {
    return empty();
  }
  
  public Iterator<T> empty(XSContentType empty)
  {
    return empty();
  }
  
  public Iterator<T> wildcard(XSWildcard wc)
  {
    return empty();
  }
  
  public Iterator<T> modelGroupDecl(XSModelGroupDecl decl)
  {
    return empty();
  }
  
  public Iterator<T> modelGroup(XSModelGroup group)
  {
    new Iterators.Map(group.iterator())
    {
      protected Iterator<? extends T> apply(XSParticle p)
      {
        return AbstractAxisImpl.this.particle(p);
      }
    };
  }
  
  public Iterator<T> elementDecl(XSElementDecl decl)
  {
    return empty();
  }
  
  protected final Iterator<T> empty()
  {
    return Iterators.empty();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\scd\AbstractAxisImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */