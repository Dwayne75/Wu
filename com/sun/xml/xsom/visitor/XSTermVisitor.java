package com.sun.xml.xsom.visitor;

import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSWildcard;

public abstract interface XSTermVisitor
{
  public abstract void wildcard(XSWildcard paramXSWildcard);
  
  public abstract void modelGroupDecl(XSModelGroupDecl paramXSModelGroupDecl);
  
  public abstract void modelGroup(XSModelGroup paramXSModelGroup);
  
  public abstract void elementDecl(XSElementDecl paramXSElementDecl);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\visitor\XSTermVisitor.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */