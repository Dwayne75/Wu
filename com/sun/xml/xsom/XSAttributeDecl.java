package com.sun.xml.xsom;

public abstract interface XSAttributeDecl
  extends XSDeclaration
{
  public abstract XSSimpleType getType();
  
  public abstract XmlString getDefaultValue();
  
  public abstract XmlString getFixedValue();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\XSAttributeDecl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */