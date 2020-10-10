package com.sun.xml.xsom.visitor;

import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSWildcard;

public abstract interface XSTermFunctionWithParam<T, P>
{
  public abstract T wildcard(XSWildcard paramXSWildcard, P paramP);
  
  public abstract T modelGroupDecl(XSModelGroupDecl paramXSModelGroupDecl, P paramP);
  
  public abstract T modelGroup(XSModelGroup paramXSModelGroup, P paramP);
  
  public abstract T elementDecl(XSElementDecl paramXSElementDecl, P paramP);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\visitor\XSTermFunctionWithParam.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */