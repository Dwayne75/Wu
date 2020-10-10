package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.util.AttributesImpl;

public final class AttributesExImpl
  extends AttributesImpl
  implements AttributesEx
{
  public CharSequence getData(int idx)
  {
    return getValue(idx);
  }
  
  public CharSequence getData(String nsUri, String localName)
  {
    return getValue(nsUri, localName);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\AttributesExImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */