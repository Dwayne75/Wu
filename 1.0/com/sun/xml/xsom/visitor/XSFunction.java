package com.sun.xml.xsom.visitor;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSSchema;

public abstract interface XSFunction
  extends XSContentTypeFunction, XSTermFunction
{
  public abstract Object annotation(XSAnnotation paramXSAnnotation);
  
  public abstract Object attGroupDecl(XSAttGroupDecl paramXSAttGroupDecl);
  
  public abstract Object attributeDecl(XSAttributeDecl paramXSAttributeDecl);
  
  public abstract Object attributeUse(XSAttributeUse paramXSAttributeUse);
  
  public abstract Object complexType(XSComplexType paramXSComplexType);
  
  public abstract Object schema(XSSchema paramXSSchema);
  
  public abstract Object facet(XSFacet paramXSFacet);
  
  public abstract Object notation(XSNotation paramXSNotation);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\visitor\XSFunction.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */