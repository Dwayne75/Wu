package com.sun.xml.xsom;

import org.relaxng.datatype.ValidationContext;

public abstract interface XSAttributeUse
  extends XSComponent
{
  public abstract boolean isRequired();
  
  public abstract XSAttributeDecl getDecl();
  
  public abstract String getDefaultValue();
  
  public abstract String getFixedValue();
  
  public abstract ValidationContext getContext();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\XSAttributeUse.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */