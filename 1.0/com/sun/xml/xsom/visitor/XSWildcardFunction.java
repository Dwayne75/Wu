package com.sun.xml.xsom.visitor;

import com.sun.xml.xsom.XSWildcard.Any;
import com.sun.xml.xsom.XSWildcard.Other;
import com.sun.xml.xsom.XSWildcard.Union;

public abstract interface XSWildcardFunction
{
  public abstract Object any(XSWildcard.Any paramAny);
  
  public abstract Object other(XSWildcard.Other paramOther);
  
  public abstract Object union(XSWildcard.Union paramUnion);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\visitor\XSWildcardFunction.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */