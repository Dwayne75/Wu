package com.sun.xml.xsom;

import java.util.List;

public abstract interface XSIdentityConstraint
  extends XSComponent
{
  public static final short KEY = 0;
  public static final short KEYREF = 1;
  public static final short UNIQUE = 2;
  
  public abstract XSElementDecl getParent();
  
  public abstract String getName();
  
  public abstract String getTargetNamespace();
  
  public abstract short getCategory();
  
  public abstract XSXPath getSelector();
  
  public abstract List<XSXPath> getFields();
  
  public abstract XSIdentityConstraint getReferencedKey();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\XSIdentityConstraint.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */