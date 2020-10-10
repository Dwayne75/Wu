package com.sun.xml.xsom.visitor;

import com.sun.xml.xsom.XSWildcard.Any;
import com.sun.xml.xsom.XSWildcard.Other;
import com.sun.xml.xsom.XSWildcard.Union;

public abstract interface XSWildcardFunction<T>
{
  public abstract T any(XSWildcard.Any paramAny);
  
  public abstract T other(XSWildcard.Other paramOther);
  
  public abstract T union(XSWildcard.Union paramUnion);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\visitor\XSWildcardFunction.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */