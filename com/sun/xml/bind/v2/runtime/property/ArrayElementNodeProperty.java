package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

final class ArrayElementNodeProperty<BeanT, ListT, ItemT>
  extends ArrayElementProperty<BeanT, ListT, ItemT>
{
  public ArrayElementNodeProperty(JAXBContextImpl p, RuntimeElementPropertyInfo prop)
  {
    super(p, prop);
  }
  
  public void serializeItem(JaxBeanInfo expected, ItemT item, XMLSerializer w)
    throws SAXException, IOException, XMLStreamException
  {
    if (item == null) {
      w.writeXsiNilTrue();
    } else {
      w.childAsXsiType(item, this.fieldName, expected);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\property\ArrayElementNodeProperty.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */