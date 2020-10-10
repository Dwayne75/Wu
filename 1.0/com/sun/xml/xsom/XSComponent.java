package com.sun.xml.xsom;

import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;
import org.xml.sax.Locator;

public abstract interface XSComponent
{
  public abstract XSAnnotation getAnnotation();
  
  public abstract Locator getLocator();
  
  public abstract XSSchema getOwnerSchema();
  
  public abstract void visit(XSVisitor paramXSVisitor);
  
  public abstract Object apply(XSFunction paramXSFunction);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\XSComponent.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */