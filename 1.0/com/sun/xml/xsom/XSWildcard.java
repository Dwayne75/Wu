package com.sun.xml.xsom;

import com.sun.xml.xsom.visitor.XSWildcardFunction;
import com.sun.xml.xsom.visitor.XSWildcardVisitor;

public abstract interface XSWildcard
  extends XSComponent, XSTerm
{
  public static final int LAX = 1;
  public static final int STRTICT = 2;
  public static final int SKIP = 3;
  
  public abstract int getMode();
  
  public abstract boolean acceptsNamespace(String paramString);
  
  public abstract void visit(XSWildcardVisitor paramXSWildcardVisitor);
  
  public abstract Object apply(XSWildcardFunction paramXSWildcardFunction);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\XSWildcard.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */