package com.sun.xml.bind.v2.runtime.reflect;

import javax.xml.bind.JAXBException;
import org.xml.sax.SAXException;

public abstract interface ListIterator<E>
{
  public abstract boolean hasNext();
  
  public abstract E next()
    throws SAXException, JAXBException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\ListIterator.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */