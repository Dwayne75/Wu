package com.sun.xml.bind.v2.runtime.unmarshaller;

import org.xml.sax.SAXException;

public abstract interface Intercepter
{
  public abstract Object intercept(UnmarshallingContext.State paramState, Object paramObject)
    throws SAXException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\Intercepter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */