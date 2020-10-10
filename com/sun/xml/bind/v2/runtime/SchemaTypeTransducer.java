package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.api.AccessorException;
import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public class SchemaTypeTransducer<V>
  extends FilterTransducer<V>
{
  private final QName schemaType;
  
  public SchemaTypeTransducer(Transducer<V> core, QName schemaType)
  {
    super(core);
    this.schemaType = schemaType;
  }
  
  public CharSequence print(V o)
    throws AccessorException
  {
    XMLSerializer w = XMLSerializer.getInstance();
    QName old = w.setSchemaType(this.schemaType);
    try
    {
      return this.core.print(o);
    }
    finally
    {
      w.setSchemaType(old);
    }
  }
  
  public void writeText(XMLSerializer w, V o, String fieldName)
    throws IOException, SAXException, XMLStreamException, AccessorException
  {
    QName old = w.setSchemaType(this.schemaType);
    try
    {
      this.core.writeText(w, o, fieldName);
    }
    finally
    {
      w.setSchemaType(old);
    }
  }
  
  public void writeLeafElement(XMLSerializer w, Name tagName, V o, String fieldName)
    throws IOException, SAXException, XMLStreamException, AccessorException
  {
    QName old = w.setSchemaType(this.schemaType);
    try
    {
      this.core.writeLeafElement(w, tagName, o, fieldName);
    }
    finally
    {
      w.setSchemaType(old);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\SchemaTypeTransducer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */