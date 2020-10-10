package com.sun.xml.bind.v2.runtime.unmarshaller;

import org.xml.sax.Attributes;

public abstract interface AttributesEx
  extends Attributes
{
  public abstract CharSequence getData(int paramInt);
  
  public abstract CharSequence getData(String paramString1, String paramString2);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\AttributesEx.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */