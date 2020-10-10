package com.sun.xml.bind.v2.runtime.unmarshaller;

import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

final class StAXEventConnector
  extends StAXConnector
{
  private final XMLEventReader staxEventReader;
  private XMLEvent event;
  private final AttributesImpl attrs = new AttributesImpl();
  private final StringBuilder buffer = new StringBuilder();
  private boolean seenText;
  
  public StAXEventConnector(XMLEventReader staxCore, XmlVisitor visitor)
  {
    super(visitor);
    this.staxEventReader = staxCore;
  }
  
  public void bridge()
    throws XMLStreamException
  {
    try
    {
      int depth = 0;
      
      this.event = this.staxEventReader.peek();
      if ((!this.event.isStartDocument()) && (!this.event.isStartElement())) {
        throw new IllegalStateException();
      }
      do
      {
        this.event = this.staxEventReader.nextEvent();
      } while (!this.event.isStartElement());
      handleStartDocument(this.event.asStartElement().getNamespaceContext());
      for (;;)
      {
        switch (this.event.getEventType())
        {
        case 1: 
          handleStartElement(this.event.asStartElement());
          depth++;
          break;
        case 2: 
          depth--;
          handleEndElement(this.event.asEndElement());
          if (depth != 0) {
            break;
          }
          break;
        case 4: 
        case 6: 
        case 12: 
          handleCharacters(this.event.asCharacters());
        }
        this.event = this.staxEventReader.nextEvent();
      }
      handleEndDocument();
      this.event = null;
    }
    catch (SAXException e)
    {
      throw new XMLStreamException(e);
    }
  }
  
  protected Location getCurrentLocation()
  {
    return this.event.getLocation();
  }
  
  protected String getCurrentQName()
  {
    QName qName;
    QName qName;
    if (this.event.isEndElement()) {
      qName = this.event.asEndElement().getName();
    } else {
      qName = this.event.asStartElement().getName();
    }
    return getQName(qName.getPrefix(), qName.getLocalPart());
  }
  
  private void handleCharacters(Characters event)
    throws SAXException, XMLStreamException
  {
    if (!this.predictor.expectText()) {
      return;
    }
    this.seenText = true;
    XMLEvent next;
    for (;;)
    {
      next = this.staxEventReader.peek();
      if (!isIgnorable(next)) {
        break;
      }
      this.staxEventReader.nextEvent();
    }
    if (isTag(next))
    {
      this.visitor.text(event.getData());
      return;
    }
    this.buffer.append(event.getData());
    for (;;)
    {
      next = this.staxEventReader.peek();
      if (isIgnorable(next))
      {
        this.staxEventReader.nextEvent();
      }
      else
      {
        if (isTag(next))
        {
          this.visitor.text(this.buffer);
          this.buffer.setLength(0);
          return;
        }
        this.buffer.append(next.asCharacters().getData());
        this.staxEventReader.nextEvent();
      }
    }
  }
  
  private boolean isTag(XMLEvent event)
  {
    int eventType = event.getEventType();
    return (eventType == 1) || (eventType == 2);
  }
  
  private boolean isIgnorable(XMLEvent event)
  {
    int eventType = event.getEventType();
    return (eventType == 5) || (eventType == 3);
  }
  
  private void handleEndElement(EndElement event)
    throws SAXException
  {
    if ((!this.seenText) && (this.predictor.expectText())) {
      this.visitor.text("");
    }
    QName qName = event.getName();
    this.tagName.uri = fixNull(qName.getNamespaceURI());
    this.tagName.local = qName.getLocalPart();
    this.visitor.endElement(this.tagName);
    for (Iterator<Namespace> i = event.getNamespaces(); i.hasNext();)
    {
      String prefix = fixNull(((Namespace)i.next()).getPrefix());
      this.visitor.endPrefixMapping(prefix);
    }
    this.seenText = false;
  }
  
  private void handleStartElement(StartElement event)
    throws SAXException
  {
    for (Iterator i = event.getNamespaces(); i.hasNext();)
    {
      Namespace ns = (Namespace)i.next();
      this.visitor.startPrefixMapping(fixNull(ns.getPrefix()), fixNull(ns.getNamespaceURI()));
    }
    QName qName = event.getName();
    this.tagName.uri = fixNull(qName.getNamespaceURI());
    String localName = qName.getLocalPart();
    this.tagName.uri = fixNull(qName.getNamespaceURI());
    this.tagName.local = localName;
    this.tagName.atts = getAttributes(event);
    this.visitor.startElement(this.tagName);
    
    this.seenText = false;
  }
  
  private Attributes getAttributes(StartElement event)
  {
    this.attrs.clear();
    for (Iterator i = event.getAttributes(); i.hasNext();)
    {
      Attribute staxAttr = (Attribute)i.next();
      
      QName name = staxAttr.getName();
      String uri = fixNull(name.getNamespaceURI());
      String localName = name.getLocalPart();
      String prefix = name.getPrefix();
      String qName;
      String qName;
      if ((prefix == null) || (prefix.length() == 0)) {
        qName = localName;
      } else {
        qName = prefix + ':' + localName;
      }
      String type = staxAttr.getDTDType();
      String value = staxAttr.getValue();
      
      this.attrs.addAttribute(uri, localName, qName, type, value);
    }
    return this.attrs;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\StAXEventConnector.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */