package com.sun.xml.bind.v2.runtime;

import com.sun.istack.NotNull;
import com.sun.xml.bind.api.AccessorException;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public class InlineBinaryTransducer<V>
  extends FilterTransducer<V>
{
  public InlineBinaryTransducer(Transducer<V> core)
  {
    super(core);
  }
  
  @NotNull
  public CharSequence print(@NotNull V o)
    throws AccessorException
  {
    XMLSerializer w = XMLSerializer.getInstance();
    boolean old = w.setInlineBinaryFlag(true);
    try
    {
      return this.core.print(o);
    }
    finally
    {
      w.setInlineBinaryFlag(old);
    }
  }
  
  public void writeText(XMLSerializer w, V o, String fieldName)
    throws IOException, SAXException, XMLStreamException, AccessorException
  {
    boolean old = w.setInlineBinaryFlag(true);
    try
    {
      this.core.writeText(w, o, fieldName);
    }
    finally
    {
      w.setInlineBinaryFlag(old);
    }
  }
  
  public void writeLeafElement(XMLSerializer w, Name tagName, V o, String fieldName)
    throws IOException, SAXException, XMLStreamException, AccessorException
  {
    boolean old = w.setInlineBinaryFlag(true);
    try
    {
      this.core.writeLeafElement(w, tagName, o, fieldName);
    }
    finally
    {
      w.setInlineBinaryFlag(old);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\InlineBinaryTransducer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */