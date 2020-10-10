package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.WhiteSpaceProcessor;
import java.lang.reflect.Constructor;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class StAXStreamConnector
  extends StAXConnector
{
  private final XMLStreamReader staxStreamReader;
  
  public static StAXConnector create(XMLStreamReader reader, XmlVisitor visitor)
  {
    Class readerClass = reader.getClass();
    if ((FI_STAX_READER_CLASS != null) && (FI_STAX_READER_CLASS.isAssignableFrom(readerClass)) && (FI_CONNECTOR_CTOR != null)) {
      try
      {
        return (StAXConnector)FI_CONNECTOR_CTOR.newInstance(new Object[] { reader, visitor });
      }
      catch (Exception t) {}
    }
    boolean isZephyr = readerClass.getName().equals("com.sun.xml.stream.XMLReaderImpl");
    if (!isZephyr) {
      if (!checkImplementaionNameOfSjsxp(reader)) {
        if ((!getBoolProp(reader, "org.codehaus.stax2.internNames")) || (!getBoolProp(reader, "org.codehaus.stax2.internNsUris"))) {
          visitor = new InterningXmlVisitor(visitor);
        }
      }
    }
    if ((STAX_EX_READER_CLASS != null) && (STAX_EX_READER_CLASS.isAssignableFrom(readerClass))) {
      try
      {
        return (StAXConnector)STAX_EX_CONNECTOR_CTOR.newInstance(new Object[] { reader, visitor });
      }
      catch (Exception t) {}
    }
    return new StAXStreamConnector(reader, visitor);
  }
  
  private static boolean checkImplementaionNameOfSjsxp(XMLStreamReader reader)
  {
    try
    {
      Object name = reader.getProperty("http://java.sun.com/xml/stream/properties/implementation-name");
      return (name != null) && (name.equals("sjsxp"));
    }
    catch (Exception e) {}
    return false;
  }
  
  private static boolean getBoolProp(XMLStreamReader r, String n)
  {
    try
    {
      Object o = r.getProperty(n);
      if ((o instanceof Boolean)) {
        return ((Boolean)o).booleanValue();
      }
      return false;
    }
    catch (Exception e) {}
    return false;
  }
  
  protected final StringBuilder buffer = new StringBuilder();
  protected boolean textReported = false;
  
  protected StAXStreamConnector(XMLStreamReader staxStreamReader, XmlVisitor visitor)
  {
    super(visitor);
    this.staxStreamReader = staxStreamReader;
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
      handleStartDocument(this.staxStreamReader.getNamespaceContext());
      for (;;)
      {
        switch (event)
        {
        case 1: 
          handleStartElement();
          depth++;
          break;
        case 2: 
          depth--;
          handleEndElement();
          if (depth != 0) {
            break;
          }
          break;
        case 4: 
        case 6: 
        case 12: 
          handleCharacters();
        }
        event = this.staxStreamReader.next();
      }
      this.staxStreamReader.next();
      
      handleEndDocument();
    }
    catch (SAXException e)
    {
      throw new XMLStreamException(e);
    }
  }
  
  protected Location getCurrentLocation()
  {
    return this.staxStreamReader.getLocation();
  }
  
  protected String getCurrentQName()
  {
    return getQName(this.staxStreamReader.getPrefix(), this.staxStreamReader.getLocalName());
  }
  
  private void handleEndElement()
    throws SAXException
  {
    processText(false);
    
    this.tagName.uri = fixNull(this.staxStreamReader.getNamespaceURI());
    this.tagName.local = this.staxStreamReader.getLocalName();
    this.visitor.endElement(this.tagName);
    
    int nsCount = this.staxStreamReader.getNamespaceCount();
    for (int i = nsCount - 1; i >= 0; i--) {
      this.visitor.endPrefixMapping(fixNull(this.staxStreamReader.getNamespacePrefix(i)));
    }
  }
  
  private void handleStartElement()
    throws SAXException
  {
    processText(true);
    
    int nsCount = this.staxStreamReader.getNamespaceCount();
    for (int i = 0; i < nsCount; i++) {
      this.visitor.startPrefixMapping(fixNull(this.staxStreamReader.getNamespacePrefix(i)), fixNull(this.staxStreamReader.getNamespaceURI(i)));
    }
    this.tagName.uri = fixNull(this.staxStreamReader.getNamespaceURI());
    this.tagName.local = this.staxStreamReader.getLocalName();
    this.tagName.atts = this.attributes;
    
    this.visitor.startElement(this.tagName);
  }
  
  private final Attributes attributes = new Attributes()
  {
    public int getLength()
    {
      return StAXStreamConnector.this.staxStreamReader.getAttributeCount();
    }
    
    public String getURI(int index)
    {
      String uri = StAXStreamConnector.this.staxStreamReader.getAttributeNamespace(index);
      if (uri == null) {
        return "";
      }
      return uri;
    }
    
    public String getLocalName(int index)
    {
      return StAXStreamConnector.this.staxStreamReader.getAttributeLocalName(index);
    }
    
    public String getQName(int index)
    {
      String prefix = StAXStreamConnector.this.staxStreamReader.getAttributePrefix(index);
      if ((prefix == null) || (prefix.length() == 0)) {
        return getLocalName(index);
      }
      return prefix + ':' + getLocalName(index);
    }
    
    public String getType(int index)
    {
      return StAXStreamConnector.this.staxStreamReader.getAttributeType(index);
    }
    
    public String getValue(int index)
    {
      return StAXStreamConnector.this.staxStreamReader.getAttributeValue(index);
    }
    
    public int getIndex(String uri, String localName)
    {
      for (int i = getLength() - 1; i >= 0; i--) {
        if ((localName.equals(getLocalName(i))) && (uri.equals(getURI(i)))) {
          return i;
        }
      }
      return -1;
    }
    
    public int getIndex(String qName)
    {
      for (int i = getLength() - 1; i >= 0; i--) {
        if (qName.equals(getQName(i))) {
          return i;
        }
      }
      return -1;
    }
    
    public String getType(String uri, String localName)
    {
      int index = getIndex(uri, localName);
      if (index < 0) {
        return null;
      }
      return getType(index);
    }
    
    public String getType(String qName)
    {
      int index = getIndex(qName);
      if (index < 0) {
        return null;
      }
      return getType(index);
    }
    
    public String getValue(String uri, String localName)
    {
      int index = getIndex(uri, localName);
      if (index < 0) {
        return null;
      }
      return getValue(index);
    }
    
    public String getValue(String qName)
    {
      int index = getIndex(qName);
      if (index < 0) {
        return null;
      }
      return getValue(index);
    }
  };
  
  protected void handleCharacters()
    throws XMLStreamException, SAXException
  {
    if (this.predictor.expectText()) {
      this.buffer.append(this.staxStreamReader.getTextCharacters(), this.staxStreamReader.getTextStart(), this.staxStreamReader.getTextLength());
    }
  }
  
  private void processText(boolean ignorable)
    throws SAXException
  {
    if ((this.predictor.expectText()) && ((!ignorable) || (!WhiteSpaceProcessor.isWhiteSpace(this.buffer)))) {
      if (this.textReported) {
        this.textReported = false;
      } else {
        this.visitor.text(this.buffer);
      }
    }
    this.buffer.setLength(0);
  }
  
  private static final Class FI_STAX_READER_CLASS = ;
  private static final Constructor<? extends StAXConnector> FI_CONNECTOR_CTOR = initFastInfosetConnectorClass();
  
  private static Class initFIStAXReaderClass()
  {
    try
    {
      Class fisr = UnmarshallerImpl.class.getClassLoader().loadClass("org.jvnet.fastinfoset.stax.FastInfosetStreamReader");
      
      Class sdp = UnmarshallerImpl.class.getClassLoader().loadClass("com.sun.xml.fastinfoset.stax.StAXDocumentParser");
      if (fisr.isAssignableFrom(sdp)) {
        return sdp;
      }
      return null;
    }
    catch (Throwable e) {}
    return null;
  }
  
  private static Constructor<? extends StAXConnector> initFastInfosetConnectorClass()
  {
    try
    {
      if (FI_STAX_READER_CLASS == null) {
        return null;
      }
      Class c = UnmarshallerImpl.class.getClassLoader().loadClass("com.sun.xml.bind.v2.runtime.unmarshaller.FastInfosetConnector");
      
      return c.getConstructor(new Class[] { FI_STAX_READER_CLASS, XmlVisitor.class });
    }
    catch (Throwable e) {}
    return null;
  }
  
  private static final Class STAX_EX_READER_CLASS = initStAXExReader();
  private static final Constructor<? extends StAXConnector> STAX_EX_CONNECTOR_CTOR = initStAXExConnector();
  
  private static Class initStAXExReader()
  {
    try
    {
      return UnmarshallerImpl.class.getClassLoader().loadClass("org.jvnet.staxex.XMLStreamReaderEx");
    }
    catch (Throwable e) {}
    return null;
  }
  
  private static Constructor<? extends StAXConnector> initStAXExConnector()
  {
    try
    {
      Class c = UnmarshallerImpl.class.getClassLoader().loadClass("com.sun.xml.bind.v2.runtime.unmarshaller.StAXExConnector");
      return c.getConstructor(new Class[] { STAX_EX_READER_CLASS, XmlVisitor.class });
    }
    catch (Throwable e) {}
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\StAXStreamConnector.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */