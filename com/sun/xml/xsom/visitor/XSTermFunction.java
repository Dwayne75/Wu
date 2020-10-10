package com.sun.xml.xsom.visitor;

import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSWildcard;

public abstract interface XSTermFunction<T>
{
  public abstract T wildcard(XSWildcard paramXSWildcard);
  
  public abstract T modelGroupDecl(XSModelGroupDecl paramXSModelGroupDecl);
  
  public abstract T modelGroup(XSModelGroup paramXSModelGroup);
  
  public abstract T elementDecl(XSElementDecl paramXSElementDecl);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\visitor\XSTermFunction.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */