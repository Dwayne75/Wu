package com.sun.xml.xsom.visitor;

import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSimpleType;

public abstract interface XSContentTypeFunction
{
  public abstract Object simpleType(XSSimpleType paramXSSimpleType);
  
  public abstract Object particle(XSParticle paramXSParticle);
  
  public abstract Object empty(XSContentType paramXSContentType);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\visitor\XSContentTypeFunction.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */