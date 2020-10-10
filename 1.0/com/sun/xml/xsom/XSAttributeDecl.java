package com.sun.xml.xsom;

import org.relaxng.datatype.ValidationContext;

public abstract interface XSAttributeDecl
  extends XSDeclaration
{
  public abstract XSSimpleType getType();
  
  public abstract String getDefaultValue();
  
  public abstract String getFixedValue();
  
  public abstract ValidationContext getContext();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\XSAttributeDecl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */