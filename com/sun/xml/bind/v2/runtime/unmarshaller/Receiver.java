package com.sun.xml.bind.v2.runtime.unmarshaller;

import org.xml.sax.SAXException;

public abstract interface Receiver
{
  public abstract void receive(UnmarshallingContext.State paramState, Object paramObject)
    throws SAXException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\Receiver.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */