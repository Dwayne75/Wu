package com.sun.xml.xsom.visitor;

import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSWildcard;

public abstract interface XSTermFunction
{
  public abstract Object wildcard(XSWildcard paramXSWildcard);
  
  public abstract Object modelGroupDecl(XSModelGroupDecl paramXSModelGroupDecl);
  
  public abstract Object modelGroup(XSModelGroup paramXSModelGroup);
  
  public abstract Object elementDecl(XSElementDecl paramXSElementDecl);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\visitor\XSTermFunction.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */