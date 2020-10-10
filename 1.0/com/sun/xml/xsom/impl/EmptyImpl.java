package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.visitor.XSContentTypeFunction;
import com.sun.xml.xsom.visitor.XSContentTypeVisitor;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;

public class EmptyImpl
  extends ComponentImpl
  implements ContentTypeImpl
{
  public EmptyImpl()
  {
    super(null, null, null);
  }
  
  public XSSimpleType asSimpleType()
  {
    return null;
  }
  
  public XSParticle asParticle()
  {
    return null;
  }
  
  public XSContentType asEmpty()
  {
    return this;
  }
  
  public Object apply(XSContentTypeFunction function)
  {
    return function.empty(this);
  }
  
  public Object apply(XSFunction function)
  {
    return function.empty(this);
  }
  
  public void visit(XSVisitor visitor)
  {
    visitor.empty(this);
  }
  
  public void visit(XSContentTypeVisitor visitor)
  {
    visitor.empty(this);
  }
  
  public XSContentType getContentType()
  {
    return this;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\EmptyImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */