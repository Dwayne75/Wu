package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.xml.bind.marshaller.SAX2DOMEx;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.DomHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;
import org.xml.sax.helpers.XMLFilterImpl;

final class DomHandlerEx
  implements DomHandler<DomAndLocation, ResultImpl>
{
  public static final class DomAndLocation
  {
    public final Element element;
    public final Locator loc;
    
    public DomAndLocation(Element element, Locator loc)
    {
      this.element = element;
      this.loc = loc;
    }
  }
  
  public ResultImpl createUnmarshaller(ValidationEventHandler errorHandler)
  {
    return new ResultImpl();
  }
  
  public DomAndLocation getElement(ResultImpl r)
  {
    return new DomAndLocation(((Document)r.s2d.getDOM()).getDocumentElement(), r.location);
  }
  
  public Source marshal(DomAndLocation domAndLocation, ValidationEventHandler errorHandler)
  {
    return new DOMSource(domAndLocation.element);
  }
  
  public static final class ResultImpl
    extends SAXResult
  {
    final SAX2DOMEx s2d;
    Locator location = null;
    
    ResultImpl()
    {
      try
      {
        this.s2d = new SAX2DOMEx();
      }
      catch (ParserConfigurationException e)
      {
        throw new AssertionError(e);
      }
      XMLFilterImpl f = new XMLFilterImpl()
      {
        public void setDocumentLocator(Locator locator)
        {
          super.setDocumentLocator(locator);
          DomHandlerEx.ResultImpl.this.location = new LocatorImpl(locator);
        }
      };
      f.setContentHandler(this.s2d);
      
      setHandler(f);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\bindinfo\DomHandlerEx.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */