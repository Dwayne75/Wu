package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.WhiteSpaceProcessor;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshallerHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public final class SAXConnector
  implements UnmarshallerHandler
{
  private LocatorEx loc;
  private final StringBuilder buffer = new StringBuilder();
  private final XmlVisitor next;
  private final UnmarshallingContext context;
  private final XmlVisitor.TextPredictor predictor;
  
  private static final class TagNameImpl
    extends TagName
  {
    String qname;
    
    public String getQname()
    {
      return this.qname;
    }
  }
  
  private final TagNameImpl tagName = new TagNameImpl(null);
  
  public SAXConnector(XmlVisitor next, LocatorEx externalLocator)
  {
    this.next = next;
    this.context = next.getContext();
    this.predictor = next.getPredictor();
    this.loc = externalLocator;
  }
  
  public Object getResult()
    throws JAXBException, IllegalStateException
  {
    return this.context.getResult();
  }
  
  public UnmarshallingContext getContext()
  {
    return this.context;
  }
  
  public void setDocumentLocator(Locator locator)
  {
    if (this.loc != null) {
      return;
    }
    this.loc = new LocatorExWrapper(locator);
  }
  
  public void startDocument()
    throws SAXException
  {
    this.next.startDocument(this.loc, null);
  }
  
  public void endDocument()
    throws SAXException
  {
    this.next.endDocument();
  }
  
  public void startPrefixMapping(String prefix, String uri)
    throws SAXException
  {
    this.next.startPrefixMapping(prefix, uri);
  }
  
  public void endPrefixMapping(String prefix)
    throws SAXException
  {
    this.next.endPrefixMapping(prefix);
  }
  
  public void startElement(String uri, String local, String qname, Attributes atts)
    throws SAXException
  {
    if ((uri == null) || (uri.length() == 0)) {
      uri = "";
    }
    if ((local == null) || (local.length() == 0)) {
      local = qname;
    }
    if ((qname == null) || (qname.length() == 0)) {
      qname = local;
    }
    processText(true);
    
    this.tagName.uri = uri;
    this.tagName.local = local;
    this.tagName.qname = qname;
    this.tagName.atts = atts;
    this.next.startElement(this.tagName);
  }
  
  public void endElement(String uri, String localName, String qName)
    throws SAXException
  {
    processText(false);
    this.tagName.uri = uri;
    this.tagName.local = localName;
    this.tagName.qname = qName;
    this.next.endElement(this.tagName);
  }
  
  public final void characters(char[] buf, int start, int len)
  {
    if (this.predictor.expectText()) {
      this.buffer.append(buf, start, len);
    }
  }
  
  public final void ignorableWhitespace(char[] buf, int start, int len)
  {
    characters(buf, start, len);
  }
  
  public void processingInstruction(String target, String data) {}
  
  public void skippedEntity(String name) {}
  
  private void processText(boolean ignorable)
    throws SAXException
  {
    if ((this.predictor.expectText()) && ((!ignorable) || (!WhiteSpaceProcessor.isWhiteSpace(this.buffer)))) {
      this.next.text(this.buffer);
    }
    this.buffer.setLength(0);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\SAXConnector.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */