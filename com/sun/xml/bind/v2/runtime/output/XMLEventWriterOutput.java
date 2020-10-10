package com.sun.xml.bind.v2.runtime.output;

import com.sun.xml.bind.v2.runtime.XMLSerializer;
import java.io.IOException;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import org.xml.sax.SAXException;

public class XMLEventWriterOutput
  extends XmlOutputAbstractImpl
{
  private final XMLEventWriter out;
  private final XMLEventFactory ef;
  private final Characters sp;
  
  public XMLEventWriterOutput(XMLEventWriter out)
  {
    this.out = out;
    this.ef = XMLEventFactory.newInstance();
    this.sp = this.ef.createCharacters(" ");
  }
  
  public void startDocument(XMLSerializer serializer, boolean fragment, int[] nsUriIndex2prefixIndex, NamespaceContextImpl nsContext)
    throws IOException, SAXException, XMLStreamException
  {
    super.startDocument(serializer, fragment, nsUriIndex2prefixIndex, nsContext);
    if (!fragment) {
      this.out.add(this.ef.createStartDocument());
    }
  }
  
  public void endDocument(boolean fragment)
    throws IOException, SAXException, XMLStreamException
  {
    if (!fragment)
    {
      this.out.add(this.ef.createEndDocument());
      this.out.flush();
    }
    super.endDocument(fragment);
  }
  
  public void beginStartTag(int prefix, String localName)
    throws IOException, XMLStreamException
  {
    this.out.add(this.ef.createStartElement(this.nsContext.getPrefix(prefix), this.nsContext.getNamespaceURI(prefix), localName));
    
    NamespaceContextImpl.Element nse = this.nsContext.getCurrent();
    if (nse.count() > 0) {
      for (int i = nse.count() - 1; i >= 0; i--)
      {
        String uri = nse.getNsUri(i);
        if ((uri.length() != 0) || (nse.getBase() != 1)) {
          this.out.add(this.ef.createNamespace(nse.getPrefix(i), uri));
        }
      }
    }
  }
  
  public void attribute(int prefix, String localName, String value)
    throws IOException, XMLStreamException
  {
    Attribute att;
    Attribute att;
    if (prefix == -1) {
      att = this.ef.createAttribute(localName, value);
    } else {
      att = this.ef.createAttribute(this.nsContext.getPrefix(prefix), this.nsContext.getNamespaceURI(prefix), localName, value);
    }
    this.out.add(att);
  }
  
  public void endStartTag()
    throws IOException, SAXException
  {}
  
  public void endTag(int prefix, String localName)
    throws IOException, SAXException, XMLStreamException
  {
    this.out.add(this.ef.createEndElement(this.nsContext.getPrefix(prefix), this.nsContext.getNamespaceURI(prefix), localName));
  }
  
  public void text(String value, boolean needsSeparatingWhitespace)
    throws IOException, SAXException, XMLStreamException
  {
    if (needsSeparatingWhitespace) {
      this.out.add(this.sp);
    }
    this.out.add(this.ef.createCharacters(value));
  }
  
  public void text(Pcdata value, boolean needsSeparatingWhitespace)
    throws IOException, SAXException, XMLStreamException
  {
    text(value.toString(), needsSeparatingWhitespace);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\output\XMLEventWriterOutput.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */