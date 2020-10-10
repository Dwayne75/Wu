package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.api.Bridge;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

abstract class InternalBridge<T>
  extends Bridge<T>
{
  protected InternalBridge(JAXBContextImpl context)
  {
    super(context);
  }
  
  public JAXBContextImpl getContext()
  {
    return this.context;
  }
  
  abstract void marshal(T paramT, XMLSerializer paramXMLSerializer)
    throws IOException, SAXException, XMLStreamException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\InternalBridge.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */