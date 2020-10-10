package com.sun.xml.xsom;

import com.sun.xml.xsom.visitor.XSWildcardFunction;
import com.sun.xml.xsom.visitor.XSWildcardVisitor;
import java.util.Collection;
import java.util.Iterator;

public abstract interface XSWildcard
  extends XSComponent, XSTerm
{
  public static final int LAX = 1;
  public static final int STRTICT = 2;
  public static final int SKIP = 3;
  
  public abstract int getMode();
  
  public abstract boolean acceptsNamespace(String paramString);
  
  public abstract void visit(XSWildcardVisitor paramXSWildcardVisitor);
  
  public abstract <T> T apply(XSWildcardFunction<T> paramXSWildcardFunction);
  
  public static abstract interface Union
    extends XSWildcard
  {
    public abstract Iterator<String> iterateNamespaces();
    
    public abstract Collection<String> getNamespaces();
  }
  
  public static abstract interface Other
    extends XSWildcard
  {
    public abstract String getOtherNamespace();
  }
  
  public static abstract interface Any
    extends XSWildcard
  {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\XSWildcard.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */