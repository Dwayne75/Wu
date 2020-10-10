package com.sun.xml.xsom;

public abstract interface XSAttributeUse
  extends XSComponent
{
  public abstract boolean isRequired();
  
  public abstract XSAttributeDecl getDecl();
  
  public abstract XmlString getDefaultValue();
  
  public abstract XmlString getFixedValue();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\XSAttributeUse.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */