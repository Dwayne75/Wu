package com.sun.xml.xsom;

import com.sun.xml.xsom.visitor.XSTermFunction;
import com.sun.xml.xsom.visitor.XSTermVisitor;

public abstract interface XSTerm
  extends XSComponent
{
  public abstract void visit(XSTermVisitor paramXSTermVisitor);
  
  public abstract Object apply(XSTermFunction paramXSTermFunction);
  
  public abstract boolean isWildcard();
  
  public abstract boolean isModelGroupDecl();
  
  public abstract boolean isModelGroup();
  
  public abstract boolean isElementDecl();
  
  public abstract XSWildcard asWildcard();
  
  public abstract XSModelGroupDecl asModelGroupDecl();
  
  public abstract XSModelGroup asModelGroup();
  
  public abstract XSElementDecl asElementDecl();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\XSTerm.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */