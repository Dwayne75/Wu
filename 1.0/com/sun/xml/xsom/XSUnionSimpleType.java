package com.sun.xml.xsom;

public abstract interface XSUnionSimpleType
  extends XSSimpleType
{
  public abstract XSSimpleType getMember(int paramInt);
  
  public abstract int getMemberSize();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\XSUnionSimpleType.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */