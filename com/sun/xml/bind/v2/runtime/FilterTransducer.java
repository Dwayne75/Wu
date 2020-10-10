package com.sun.xml.bind.v2.runtime;

import com.sun.istack.NotNull;
import com.sun.xml.bind.api.AccessorException;
import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public abstract class FilterTransducer<T>
  implements Transducer<T>
{
  protected final Transducer<T> core;
  
  protected FilterTransducer(Transducer<T> core)
  {
    this.core = core;
  }
  
  public final boolean isDefault()
  {
    return false;
  }
  
  public boolean useNamespace()
  {
    return this.core.useNamespace();
  }
  
  public void declareNamespace(T o, XMLSerializer w)
    throws AccessorException
  {
    this.core.declareNamespace(o, w);
  }
  
  @NotNull
  public CharSequence print(@NotNull T o)
    throws AccessorException
  {
    return this.core.print(o);
  }
  
  public T parse(CharSequence lexical)
    throws AccessorException, SAXException
  {
    return (T)this.core.parse(lexical);
  }
  
  public void writeText(XMLSerializer w, T o, String fieldName)
    throws IOException, SAXException, XMLStreamException, AccessorException
  {
    this.core.writeText(w, o, fieldName);
  }
  
  public void writeLeafElement(XMLSerializer w, Name tagName, T o, String fieldName)
    throws IOException, SAXException, XMLStreamException, AccessorException
  {
    this.core.writeLeafElement(w, tagName, o, fieldName);
  }
  
  public QName getTypeName(T instance)
  {
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\FilterTransducer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */