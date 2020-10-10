package com.sun.xml.xsom;

import com.sun.xml.xsom.visitor.XSContentTypeFunction;
import com.sun.xml.xsom.visitor.XSContentTypeVisitor;

public abstract interface XSContentType
  extends XSComponent
{
  public abstract XSSimpleType asSimpleType();
  
  public abstract XSParticle asParticle();
  
  public abstract XSContentType asEmpty();
  
  public abstract Object apply(XSContentTypeFunction paramXSContentTypeFunction);
  
  public abstract void visit(XSContentTypeVisitor paramXSContentTypeVisitor);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\XSContentType.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */