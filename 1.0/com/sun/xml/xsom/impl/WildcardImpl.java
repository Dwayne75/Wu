package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSTermFunction;
import com.sun.xml.xsom.visitor.XSTermVisitor;
import com.sun.xml.xsom.visitor.XSVisitor;
import java.util.HashSet;
import java.util.Set;
import org.xml.sax.Locator;

public abstract class WildcardImpl
  extends ComponentImpl
  implements XSWildcard, Ref.Term
{
  private final int mode;
  
  protected WildcardImpl(SchemaImpl owner, AnnotationImpl _annon, Locator _loc, int _mode)
  {
    super(owner, _annon, _loc);
    this.mode = _mode;
  }
  
  public int getMode()
  {
    return this.mode;
  }
  
  public WildcardImpl union(SchemaImpl owner, WildcardImpl rhs)
  {
    if (((this instanceof WildcardImpl.Any)) || ((rhs instanceof WildcardImpl.Any))) {
      return new WildcardImpl.Any(owner, null, null, this.mode);
    }
    if (((this instanceof WildcardImpl.Finite)) && ((rhs instanceof WildcardImpl.Finite)))
    {
      Set values = new HashSet();
      values.addAll(WildcardImpl.Finite.access$000((WildcardImpl.Finite)this));
      values.addAll(WildcardImpl.Finite.access$000((WildcardImpl.Finite)rhs));
      return new WildcardImpl.Finite(owner, null, null, values, this.mode);
    }
    if (((this instanceof WildcardImpl.Other)) && ((rhs instanceof WildcardImpl.Other)))
    {
      if (WildcardImpl.Other.access$100((WildcardImpl.Other)this).equals(WildcardImpl.Other.access$100((WildcardImpl.Other)rhs))) {
        return new WildcardImpl.Other(owner, null, null, WildcardImpl.Other.access$100((WildcardImpl.Other)this), this.mode);
      }
      throw new UnsupportedOperationException("union is not expressible");
    }
    WildcardImpl.Finite f;
    WildcardImpl.Other o;
    WildcardImpl.Finite f;
    if ((this instanceof WildcardImpl.Other))
    {
      WildcardImpl.Other o = (WildcardImpl.Other)this;f = (WildcardImpl.Finite)rhs;
    }
    else
    {
      o = (WildcardImpl.Other)rhs;f = (WildcardImpl.Finite)this;
    }
    if (WildcardImpl.Finite.access$000(f).contains(WildcardImpl.Other.access$100(o))) {
      return new WildcardImpl.Any(owner, null, null, this.mode);
    }
    return new WildcardImpl.Other(owner, null, null, WildcardImpl.Other.access$100(o), this.mode);
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\WildcardImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */