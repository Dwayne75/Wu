package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeRef;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.NameBuilder;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.ListTransducedAccessorImpl;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.LeafPropertyLoader;
import com.sun.xml.bind.v2.util.QNameMap;
import java.io.IOException;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

final class ListElementProperty<BeanT, ListT, ItemT>
  extends ArrayProperty<BeanT, ListT, ItemT>
{
  private final Name tagName;
  private final TransducedAccessor<BeanT> xacc;
  
  public ListElementProperty(JAXBContextImpl grammar, RuntimeElementPropertyInfo prop)
  {
    super(grammar, prop);
    
    assert (prop.isValueList());
    assert (prop.getTypes().size() == 1);
    RuntimeTypeRef ref = (RuntimeTypeRef)prop.getTypes().get(0);
    
    this.tagName = grammar.nameBuilder.createElementName(ref.getTagName());
    
    Transducer xducer = ref.getTransducer();
    
    this.xacc = new ListTransducedAccessorImpl(xducer, this.acc, this.lister);
  }
  
  public PropertyKind getKind()
  {
    return PropertyKind.ELEMENT;
  }
  
  public void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<ChildLoader> handlers)
  {
    handlers.put(this.tagName, new ChildLoader(new LeafPropertyLoader(this.xacc), null));
  }
  
  public void serializeBody(BeanT o, XMLSerializer w, Object outerPeer)
    throws SAXException, AccessorException, IOException, XMLStreamException
  {
    ListT list = this.acc.get(o);
    if (list != null) {
      if (this.xacc.useNamespace())
      {
        w.startElement(this.tagName, null);
        this.xacc.declareNamespace(o, w);
        w.endNamespaceDecls(list);
        w.endAttributes();
        this.xacc.writeText(w, o, this.fieldName);
        w.endElement();
      }
      else
      {
        this.xacc.writeLeafElement(w, this.tagName, o, this.fieldName);
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\property\ListElementProperty.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */