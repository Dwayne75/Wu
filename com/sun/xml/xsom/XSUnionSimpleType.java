package com.sun.xml.xsom;

public abstract interface XSUnionSimpleType
  extends XSSimpleType, Iterable<XSSimpleType>
{
  public abstract XSSimpleType getMember(int paramInt);
  
  public abstract int getMemberSize();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\XSUnionSimpleType.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */