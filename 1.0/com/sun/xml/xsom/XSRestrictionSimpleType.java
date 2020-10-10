package com.sun.xml.xsom;

import java.util.Iterator;

public abstract interface XSRestrictionSimpleType
  extends XSSimpleType
{
  public abstract Iterator iterateDeclaredFacets();
  
  public abstract XSFacet getDeclaredFacet(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\XSRestrictionSimpleType.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */