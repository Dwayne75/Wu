package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public abstract class DefaultTransducedAccessor<T>
  extends TransducedAccessor<T>
{
  public abstract String print(T paramT)
    throws AccessorException, SAXException;
  
  public void writeLeafElement(XMLSerializer w, Name tagName, T o, String fieldName)
    throws SAXException, AccessorException, IOException, XMLStreamException
  {
    w.leafElement(tagName, print(o), fieldName);
  }
  
  public void writeText(XMLSerializer w, T o, String fieldName)
    throws AccessorException, SAXException, IOException, XMLStreamException
  {
    w.text(print(o), fieldName);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\DefaultTransducedAccessor.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */