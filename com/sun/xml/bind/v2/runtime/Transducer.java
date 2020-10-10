package com.sun.xml.bind.v2.runtime;

import com.sun.istack.NotNull;
import com.sun.xml.bind.api.AccessorException;
import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public abstract interface Transducer<ValueT>
{
  public abstract boolean isDefault();
  
  public abstract boolean useNamespace();
  
  public abstract void declareNamespace(ValueT paramValueT, XMLSerializer paramXMLSerializer)
    throws AccessorException;
  
  @NotNull
  public abstract CharSequence print(@NotNull ValueT paramValueT)
    throws AccessorException;
  
  public abstract ValueT parse(CharSequence paramCharSequence)
    throws AccessorException, SAXException;
  
  public abstract void writeText(XMLSerializer paramXMLSerializer, ValueT paramValueT, String paramString)
    throws IOException, SAXException, XMLStreamException, AccessorException;
  
  public abstract void writeLeafElement(XMLSerializer paramXMLSerializer, Name paramName, @NotNull ValueT paramValueT, String paramString)
    throws IOException, SAXException, XMLStreamException, AccessorException;
  
  public abstract QName getTypeName(@NotNull ValueT paramValueT);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\Transducer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */