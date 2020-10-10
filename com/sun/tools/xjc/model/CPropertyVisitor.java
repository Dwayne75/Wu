package com.sun.tools.xjc.model;

public abstract interface CPropertyVisitor<V>
{
  public abstract V onElement(CElementPropertyInfo paramCElementPropertyInfo);
  
  public abstract V onAttribute(CAttributePropertyInfo paramCAttributePropertyInfo);
  
  public abstract V onValue(CValuePropertyInfo paramCValuePropertyInfo);
  
  public abstract V onReference(CReferencePropertyInfo paramCReferencePropertyInfo);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\CPropertyVisitor.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */