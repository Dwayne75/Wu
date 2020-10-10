package com.sun.xml.xsom.visitor;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSXPath;

public abstract interface XSFunction<T>
  extends XSContentTypeFunction<T>, XSTermFunction<T>
{
  public abstract T annotation(XSAnnotation paramXSAnnotation);
  
  public abstract T attGroupDecl(XSAttGroupDecl paramXSAttGroupDecl);
  
  public abstract T attributeDecl(XSAttributeDecl paramXSAttributeDecl);
  
  public abstract T attributeUse(XSAttributeUse paramXSAttributeUse);
  
  public abstract T complexType(XSComplexType paramXSComplexType);
  
  public abstract T schema(XSSchema paramXSSchema);
  
  public abstract T facet(XSFacet paramXSFacet);
  
  public abstract T notation(XSNotation paramXSNotation);
  
  public abstract T identityConstraint(XSIdentityConstraint paramXSIdentityConstraint);
  
  public abstract T xpath(XSXPath paramXSXPath);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\visitor\XSFunction.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */