package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.api.AccessorException;
import java.io.IOException;
import javax.activation.MimeType;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public final class MimeTypedTransducer<V>
  extends FilterTransducer<V>
{
  private final MimeType expectedMimeType;
  
  public MimeTypedTransducer(Transducer<V> core, MimeType expectedMimeType)
  {
    super(core);
    this.expectedMimeType = expectedMimeType;
  }
  
  public CharSequence print(V o)
    throws AccessorException
  {
    XMLSerializer w = XMLSerializer.getInstance();
    MimeType old = w.setExpectedMimeType(this.expectedMimeType);
    try
    {
      return this.core.print(o);
    }
    finally
    {
      w.setExpectedMimeType(old);
    }
  }
  
  public void writeText(XMLSerializer w, V o, String fieldName)
    throws IOException, SAXException, XMLStreamException, AccessorException
  {
    MimeType old = w.setExpectedMimeType(this.expectedMimeType);
    try
    {
      this.core.writeText(w, o, fieldName);
    }
    finally
    {
      w.setExpectedMimeType(old);
    }
  }
  
  public void writeLeafElement(XMLSerializer w, Name tagName, V o, String fieldName)
    throws IOException, SAXException, XMLStreamException, AccessorException
  {
    MimeType old = w.setExpectedMimeType(this.expectedMimeType);
    try
    {
      this.core.writeLeafElement(w, tagName, o, fieldName);
    }
    finally
    {
      w.setExpectedMimeType(old);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\MimeTypedTransducer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */