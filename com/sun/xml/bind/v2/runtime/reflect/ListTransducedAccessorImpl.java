package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.xml.bind.WhiteSpaceProcessor;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import javax.xml.bind.JAXBException;
import org.xml.sax.SAXException;

public final class ListTransducedAccessorImpl<BeanT, ListT, ItemT, PackT>
  extends DefaultTransducedAccessor<BeanT>
{
  private final Transducer<ItemT> xducer;
  private final Lister<BeanT, ListT, ItemT, PackT> lister;
  private final Accessor<BeanT, ListT> acc;
  
  public ListTransducedAccessorImpl(Transducer<ItemT> xducer, Accessor<BeanT, ListT> acc, Lister<BeanT, ListT, ItemT, PackT> lister)
  {
    this.xducer = xducer;
    this.lister = lister;
    this.acc = acc;
  }
  
  public boolean useNamespace()
  {
    return this.xducer.useNamespace();
  }
  
  public void declareNamespace(BeanT bean, XMLSerializer w)
    throws AccessorException, SAXException
  {
    ListT list = this.acc.get(bean);
    if (list != null)
    {
      ListIterator<ItemT> itr = this.lister.iterator(list, w);
      while (itr.hasNext()) {
        try
        {
          ItemT item = itr.next();
          if (item != null) {
            this.xducer.declareNamespace(item, w);
          }
        }
        catch (JAXBException e)
        {
          w.reportError(null, e);
        }
      }
    }
  }
  
  public String print(BeanT o)
    throws AccessorException, SAXException
  {
    ListT list = this.acc.get(o);
    if (list == null) {
      return null;
    }
    StringBuilder buf = new StringBuilder();
    XMLSerializer w = XMLSerializer.getInstance();
    ListIterator<ItemT> itr = this.lister.iterator(list, w);
    while (itr.hasNext()) {
      try
      {
        ItemT item = itr.next();
        if (item != null)
        {
          if (buf.length() > 0) {
            buf.append(' ');
          }
          buf.append(this.xducer.print(item));
        }
      }
      catch (JAXBException e)
      {
        w.reportError(null, e);
      }
    }
    return buf.toString();
  }
  
  private void processValue(BeanT bean, CharSequence s)
    throws AccessorException, SAXException
  {
    PackT pack = this.lister.startPacking(bean, this.acc);
    
    int idx = 0;
    int len = s.length();
    for (;;)
    {
      int p = idx;
      while ((p < len) && (!WhiteSpaceProcessor.isWhiteSpace(s.charAt(p)))) {
        p++;
      }
      CharSequence token = s.subSequence(idx, p);
      if (!token.equals("")) {
        this.lister.addToPack(pack, this.xducer.parse(token));
      }
      if (p == len) {
        break;
      }
      while ((p < len) && (WhiteSpaceProcessor.isWhiteSpace(s.charAt(p)))) {
        p++;
      }
      if (p == len) {
        break;
      }
      idx = p;
    }
    this.lister.endPacking(pack, bean, this.acc);
  }
  
  public void parse(BeanT bean, CharSequence lexical)
    throws AccessorException, SAXException
  {
    processValue(bean, lexical);
  }
  
  public boolean hasValue(BeanT bean)
    throws AccessorException
  {
    return this.acc.get(bean) != null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\ListTransducedAccessorImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */