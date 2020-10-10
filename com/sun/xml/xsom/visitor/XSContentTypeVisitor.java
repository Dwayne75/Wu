package com.sun.xml.xsom.visitor;

import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSimpleType;

public abstract interface XSContentTypeVisitor
{
  public abstract void simpleType(XSSimpleType paramXSSimpleType);
  
  public abstract void particle(XSParticle paramXSParticle);
  
  public abstract void empty(XSContentType paramXSContentType);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\visitor\XSContentTypeVisitor.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */