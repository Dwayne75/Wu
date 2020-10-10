package com.sun.xml.xsom;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract interface XSRestrictionSimpleType
  extends XSSimpleType
{
  public abstract Iterator<XSFacet> iterateDeclaredFacets();
  
  public abstract Collection<? extends XSFacet> getDeclaredFacets();
  
  public abstract XSFacet getDeclaredFacet(String paramString);
  
  public abstract List<XSFacet> getDeclaredFacets(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\XSRestrictionSimpleType.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */