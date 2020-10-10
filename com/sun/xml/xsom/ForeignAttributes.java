package com.sun.xml.xsom;

import org.relaxng.datatype.ValidationContext;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

public abstract interface ForeignAttributes
  extends Attributes
{
  public abstract ValidationContext getContext();
  
  public abstract Locator getLocator();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\ForeignAttributes.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */