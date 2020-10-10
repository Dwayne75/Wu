package com.sun.xml.xsom.visitor;

import com.sun.xml.xsom.XSWildcard.Any;
import com.sun.xml.xsom.XSWildcard.Other;
import com.sun.xml.xsom.XSWildcard.Union;

public abstract interface XSWildcardVisitor
{
  public abstract void any(XSWildcard.Any paramAny);
  
  public abstract void other(XSWildcard.Other paramOther);
  
  public abstract void union(XSWildcard.Union paramUnion);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\visitor\XSWildcardVisitor.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */