package com.sun.istack;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class XMLStreamReaderToContentHandler
{
  private final XMLStreamReader staxStreamReader;
  private final ContentHandler saxHandler;
  private final boolean eagerQuit;
  private final boolean fragment;
  private final String[] inscopeNamespaces;
  
  public XMLStreamReaderToContentHandler(XMLStreamReader staxCore, ContentHandler saxCore, boolean eagerQuit, boolean fragment)
  {
    this(staxCore, saxCore, eagerQuit, fragment, new String[0]);
  }
  
  public XMLStreamReaderToContentHandler(XMLStreamReader staxCore, ContentHandler saxCore, boolean eagerQuit, boolean fragment, String[] inscopeNamespaces)
  {
    this.staxStreamReader = staxCore;
    this.saxHandler = saxCore;
    this.eagerQuit = eagerQuit;
    this.fragment = fragment;
    this.inscopeNamespaces = inscopeNamespaces;
    assert (inscopeNamespaces.length % 2 == 0);
  }
  
  public void bridge()
    throws XMLStreamException
  {
    try
    {
      int depth = 0;
      
      int event = this.staxStreamReader.getEventType();
      if (event == 7) {
        while (!this.staxStreamReader.isStartElement()) {
          event = this.staxStreamReader.next();
        }
      }
      if (event != 1) {
        throw new IllegalStateException("The current event is not START_ELEMENT\n but " + event);
      }
      handleStartDocument();
      for (int i = 0; i < this.inscopeNamespaces.length; i += 2) {
        this.saxHandler.startPrefixMapping(this.inscopeNamespaces[i], this.inscopeNamespaces[(i + 1)]);
      }
      do
      {
        switch (event)
        {
        case 1: 
          depth++;
          handleStartElement();
          break;
        case 2: 
          handleEndElement();
          depth--;
          if ((depth != 0) || (!this.eagerQuit)) {
            break;
          }
          break;
        case 4: 
          handleCharacters();
          break;
        case 9: 
          handleEntityReference();
          break;
        case 3: 
          handlePI();
          break;
        case 5: 
          handleComment();
          break;
        case 11: 
          handleDTD();
          break;
        case 10: 
          handleAttribute();
          break;
        case 13: 
          handleNamespace();
          break;
        case 12: 
          handleCDATA();
          break;
        case 15: 
          handleEntityDecl();
          break;
        case 14: 
          handleNotationDecl();
          break;
        case 6: 
          handleSpace();
          break;
        case 7: 
        case 8: 
        default: 
          throw new InternalError("processing event: " + event);
        }
        event = this.staxStreamReader.next();
      } while (depth != 0);
      for (int i = 0; i < this.inscopeNamespaces.length; i += 2) {
        this.saxHandler.endPrefixMapping(this.inscopeNamespaces[i]);
      }
      handleEndDocument();
    }
    catch (SAXException e)
    {
      throw new XMLStreamException2(e);
    }
  }
  
  private void handleEndDocument()
    throws SAXException
  {
    if (this.fragment) {
      return;
    }
    this.saxHandler.endDocument();
  }
  
  private void handleStartDocument()
    throws SAXException
  {
    if (this.fragment) {
      return;
    }
    this.saxHandler.setDocumentLocator(new Locator()
    {
      public int getColumnNumber()
      {
        return XMLStreamReaderToContentHandler.this.staxStreamReader.getLocation().getColumnNumber();
      }
      
      public int getLineNumber()
      {
        return XMLStreamReaderToContentHandler.this.staxStreamReader.getLocation().getLineNumber();
      }
      
      public String getPublicId()
      {
        return XMLStreamReaderToContentHandler.this.staxStreamReader.getLocation().getPublicId();
      }
      
      public String getSystemId()
      {
        return XMLStreamReaderToContentHandler.this.staxStreamReader.getLocation().getSystemId();
      }
    });
    this.saxHandler.startDocument();
  }
  
  private void handlePI()
    throws XMLStreamException
  {
    try
    {
      this.saxHandler.processingInstruction(this.staxStreamReader.getPITarget(), this.staxStreamReader.getPIData());
    }
    catch (SAXException e)
    {
      throw new XMLStreamException2(e);
    }
  }
  
  private void handleCharacters()
    throws XMLStreamException
  {
    try
    {
      this.saxHandler.characters(this.staxStreamReader.getTextCharacters(), this.staxStreamReader.getTextStart(), this.staxStreamReader.getTextLength());
    }
    catch (SAXException e)
    {
      throw new XMLStreamException2(e);
    }
  }
  
  private void handleEndElement()
    throws XMLStreamException
  {
    QName qName = this.staxStreamReader.getName();
    try
    {
      String pfix = qName.getPrefix();
      String rawname = pfix + ':' + qName.getLocalPart();
      
      this.saxHandler.endElement(qName.getNamespaceURI(), qName.getLocalPart(), rawname);
      
      int nsCount = this.staxStreamReader.getNamespaceCount();
      for (int i = nsCount - 1; i >= 0; i--)
      {
        String prefix = this.staxStreamReader.getNamespacePrefix(i);
        if (prefix == null) {
          prefix = "";
        }
        this.saxHandler.endPrefixMapping(prefix);
      }
    }
    catch (SAXException e)
    {
      throw new XMLStreamException2(e);
    }
  }
  
  private void handleStartElement()
    throws XMLStreamException
  {
    try
    {
      int nsCount = this.staxStreamReader.getNamespaceCount();
      for (int i = 0; i < nsCount; i++) {
        this.saxHandler.startPrefixMapping(fixNull(this.staxStreamReader.getNamespacePrefix(i)), fixNull(this.staxStreamReader.getNamespaceURI(i)));
      }
      QName qName = this.staxStreamReader.getName();
      String prefix = qName.getPrefix();
      String rawname;
      String rawname;
      if ((prefix == null) || (prefix.length() == 0)) {
        rawname = qName.getLocalPart();
      } else {
        rawname = prefix + ':' + qName.getLocalPart();
      }
      Attributes attrs = getAttributes();
      this.saxHandler.startElement(qName.getNamespaceURI(), qName.getLocalPart(), rawname, attrs);
    }
    catch (SAXException e)
    {
      throw new XMLStreamException2(e);
    }
  }
  
  private static String fixNull(String s)
  {
    if (s == null) {
      return "";
    }
    return s;
  }
  
  private Attributes getAttributes()
  {
    AttributesImpl attrs = new AttributesImpl();
    
    int eventType = this.staxStreamReader.getEventType();
    if ((eventType != 10) && (eventType != 1)) {
      throw new InternalError("getAttributes() attempting to process: " + eventType);
    }
    for (int i = 0; i < this.staxStreamReader.getAttributeCount(); i++)
    {
      String uri = this.staxStreamReader.getAttributeNamespace(i);
      if (uri == null) {
        uri = "";
      }
      String localName = this.staxStreamReader.getAttributeLocalName(i);
      String prefix = this.staxStreamReader.getAttributePrefix(i);
      String qName;
      String qName;
      if ((prefix == null) || (prefix.length() == 0)) {
        qName = localName;
      } else {
        qName = prefix + ':' + localName;
      }
      String type = this.staxStreamReader.getAttributeType(i);
      String value = this.staxStreamReader.getAttributeValue(i);
      
      attrs.addAttribute(uri, localName, qName, type, value);
    }
    return attrs;
  }
  
  private void handleNamespace() {}
  
  private void handleAttribute() {}
  
  private void handleDTD() {}
  
  private void handleComment() {}
  
  private void handleEntityReference() {}
  
  private void handleSpace() {}
  
  private void handleNotationDecl() {}
  
  private void handleEntityDecl() {}
  
  private void handleCDATA() {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\istack\XMLStreamReaderToContentHandler.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */