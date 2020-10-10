package com.sun.xml.xsom;

import com.sun.xml.xsom.visitor.XSTermFunction;
import com.sun.xml.xsom.visitor.XSTermFunctionWithParam;
import com.sun.xml.xsom.visitor.XSTermVisitor;

public abstract interface XSTerm
  extends XSComponent
{
  public abstract void visit(XSTermVisitor paramXSTermVisitor);
  
  public abstract <T> T apply(XSTermFunction<T> paramXSTermFunction);
  
  public abstract <T, P> T apply(XSTermFunctionWithParam<T, P> paramXSTermFunctionWithParam, P paramP);
  
  public abstract boolean isWildcard();
  
  public abstract boolean isModelGroupDecl();
  
  public abstract boolean isModelGroup();
  
  public abstract boolean isElementDecl();
  
  public abstract XSWildcard asWildcard();
  
  public abstract XSModelGroupDecl asModelGroupDecl();
  
  public abstract XSModelGroup asModelGroup();
  
  public abstract XSElementDecl asElementDecl();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\XSTerm.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */