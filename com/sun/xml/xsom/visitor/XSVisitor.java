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

public abstract interface XSVisitor
  extends XSTermVisitor, XSContentTypeVisitor
{
  public abstract void annotation(XSAnnotation paramXSAnnotation);
  
  public abstract void attGroupDecl(XSAttGroupDecl paramXSAttGroupDecl);
  
  public abstract void attributeDecl(XSAttributeDecl paramXSAttributeDecl);
  
  public abstract void attributeUse(XSAttributeUse paramXSAttributeUse);
  
  public abstract void complexType(XSComplexType paramXSComplexType);
  
  public abstract void schema(XSSchema paramXSSchema);
  
  public abstract void facet(XSFacet paramXSFacet);
  
  public abstract void notation(XSNotation paramXSNotation);
  
  public abstract void identityConstraint(XSIdentityConstraint paramXSIdentityConstraint);
  
  public abstract void xpath(XSXPath paramXSXPath);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\visitor\XSVisitor.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */