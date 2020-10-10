package com.sun.xml.bind.v2.runtime.unmarshaller;

import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

abstract class StAXConnector
{
  protected final XmlVisitor visitor;
  protected final UnmarshallingContext context;
  protected final XmlVisitor.TextPredictor predictor;
  public abstract void bridge()
    throws XMLStreamException;
  
  private final class TagNameImpl
    extends TagName
  {
    private TagNameImpl() {}
    
    public String getQname()
    {
      return StAXConnector.this.getCurrentQName();
    }
  }
  
  protected final TagName tagName = new TagNameImpl(null);
  
  protected StAXConnector(XmlVisitor visitor)
  {
    this.visitor = visitor;
    this.context = visitor.getContext();
    this.predictor = visitor.getPredictor();
  }
  
  protected abstract Location getCurrentLocation();
  
  protected abstract String getCurrentQName();
  
  protected final void handleStartDocument(NamespaceContext nsc)
    throws SAXException
  {
    this.visitor.startDocument(new LocatorEx()
    {
      public ValidationEventLocator getLocation()
      {
        return new ValidationEventLocatorImpl(this);
      }
      
      public int getColumnNumber()
      {
        return StAXConnector.this.getCurrentLocation().getColumnNumber();
      }
      
      public int getLineNumber()
      {
        return StAXConnector.this.getCurrentLocation().getLineNumber();
      }
      
      public String getPublicId()
      {
        return StAXConnector.this.getCurrentLocation().getPublicId();
      }
      
      public String getSystemId()
      {
        return StAXConnector.this.getCurrentLocation().getSystemId();
      }
    }, nsc);
  }
  
  protected final void handleEndDocument()
    throws SAXException
  {
    this.visitor.endDocument();
  }
  
  protected static String fixNull(String s)
  {
    if (s == null) {
      return "";
    }
    return s;
  }
  
  protected final String getQName(String prefix, String localName)
  {
    if ((prefix == null) || (prefix.length() == 0)) {
      return localName;
    }
    return prefix + ':' + localName;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\StAXConnector.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */