package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public final class TransducedAccessor_method_Integer
  extends DefaultTransducedAccessor
{
  public String print(Object o)
  {
    return DatatypeConverterImpl._printInt(((Bean)o).get_int());
  }
  
  public void parse(Object o, CharSequence lexical)
  {
    ((Bean)o).set_int(DatatypeConverterImpl._parseInt(lexical));
  }
  
  public boolean hasValue(Object o)
  {
    return true;
  }
  
  public void writeLeafElement(XMLSerializer w, Name tagName, Object o, String fieldName)
    throws SAXException, AccessorException, IOException, XMLStreamException
  {
    w.leafElement(tagName, ((Bean)o).get_int(), fieldName);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\opt\TransducedAccessor_method_Integer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */