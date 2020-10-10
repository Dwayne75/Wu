package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.IDResolver;
import com.sun.xml.bind.api.ClassResolver;
import com.sun.xml.bind.unmarshaller.DOMScanner;
import com.sun.xml.bind.unmarshaller.InfosetScanner;
import com.sun.xml.bind.unmarshaller.Messages;
import com.sun.xml.bind.v2.runtime.AssociationMap;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller.Listener;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.bind.helpers.AbstractUnmarshallerImpl;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public final class UnmarshallerImpl
  extends AbstractUnmarshallerImpl
  implements ValidationEventHandler
{
  protected final JAXBContextImpl context;
  private Schema schema;
  public final UnmarshallingContext coordinator;
  private Unmarshaller.Listener externalListener;
  private AttachmentUnmarshaller attachmentUnmarshaller;
  private IDResolver idResolver = new DefaultIDResolver();
  
  public UnmarshallerImpl(JAXBContextImpl context, AssociationMap assoc)
  {
    this.context = context;
    this.coordinator = new UnmarshallingContext(this, assoc);
    try
    {
      setEventHandler(this);
    }
    catch (JAXBException e)
    {
      throw new AssertionError(e);
    }
  }
  
  public UnmarshallerHandler getUnmarshallerHandler()
  {
    return getUnmarshallerHandler(true, null);
  }
  
  private SAXConnector getUnmarshallerHandler(boolean intern, JaxBeanInfo expectedType)
  {
    XmlVisitor h = createUnmarshallerHandler(null, false, expectedType);
    if (intern) {
      h = new InterningXmlVisitor(h);
    }
    return new SAXConnector(h, null);
  }
  
  public final XmlVisitor createUnmarshallerHandler(InfosetScanner scanner, boolean inplace, JaxBeanInfo expectedType)
  {
    this.coordinator.reset(scanner, inplace, expectedType, this.idResolver);
    XmlVisitor unmarshaller = this.coordinator;
    if (this.schema != null) {
      unmarshaller = new ValidatingUnmarshaller(this.schema, unmarshaller);
    }
    if ((this.attachmentUnmarshaller != null) && (this.attachmentUnmarshaller.isXOPPackage())) {
      unmarshaller = new MTOMDecorator(this, unmarshaller, this.attachmentUnmarshaller);
    }
    return unmarshaller;
  }
  
  private static final DefaultHandler dummyHandler = new DefaultHandler();
  public static final String FACTORY = "com.sun.xml.bind.ObjectFactory";
  
  public static boolean needsInterning(XMLReader reader)
  {
    try
    {
      reader.setFeature("http://xml.org/sax/features/string-interning", true);
    }
    catch (SAXException e) {}
    try
    {
      if (reader.getFeature("http://xml.org/sax/features/string-interning")) {
        return false;
      }
    }
    catch (SAXException e) {}
    return true;
  }
  
  protected Object unmarshal(XMLReader reader, InputSource source)
    throws JAXBException
  {
    return unmarshal0(reader, source, null);
  }
  
  protected <T> JAXBElement<T> unmarshal(XMLReader reader, InputSource source, Class<T> expectedType)
    throws JAXBException
  {
    if (expectedType == null) {
      throw new IllegalArgumentException();
    }
    return (JAXBElement)unmarshal0(reader, source, getBeanInfo(expectedType));
  }
  
  private Object unmarshal0(XMLReader reader, InputSource source, JaxBeanInfo expectedType)
    throws JAXBException
  {
    SAXConnector connector = getUnmarshallerHandler(needsInterning(reader), expectedType);
    
    reader.setContentHandler(connector);
    
    reader.setErrorHandler(this.coordinator);
    try
    {
      reader.parse(source);
    }
    catch (IOException e)
    {
      throw new UnmarshalException(e);
    }
    catch (SAXException e)
    {
      throw createUnmarshalException(e);
    }
    Object result = connector.getResult();
    
    reader.setContentHandler(dummyHandler);
    reader.setErrorHandler(dummyHandler);
    
    return result;
  }
  
  public <T> JAXBElement<T> unmarshal(Source source, Class<T> expectedType)
    throws JAXBException
  {
    if ((source instanceof SAXSource))
    {
      SAXSource ss = (SAXSource)source;
      
      XMLReader reader = ss.getXMLReader();
      if (reader == null) {
        reader = getXMLReader();
      }
      return unmarshal(reader, ss.getInputSource(), expectedType);
    }
    if ((source instanceof StreamSource)) {
      return unmarshal(getXMLReader(), streamSourceToInputSource((StreamSource)source), expectedType);
    }
    if ((source instanceof DOMSource)) {
      return unmarshal(((DOMSource)source).getNode(), expectedType);
    }
    throw new IllegalArgumentException();
  }
  
  public Object unmarshal0(Source source, JaxBeanInfo expectedType)
    throws JAXBException
  {
    if ((source instanceof SAXSource))
    {
      SAXSource ss = (SAXSource)source;
      
      XMLReader reader = ss.getXMLReader();
      if (reader == null) {
        reader = getXMLReader();
      }
      return unmarshal0(reader, ss.getInputSource(), expectedType);
    }
    if ((source instanceof StreamSource)) {
      return unmarshal0(getXMLReader(), streamSourceToInputSource((StreamSource)source), expectedType);
    }
    if ((source instanceof DOMSource)) {
      return unmarshal0(((DOMSource)source).getNode(), expectedType);
    }
    throw new IllegalArgumentException();
  }
  
  public final ValidationEventHandler getEventHandler()
  {
    try
    {
      return super.getEventHandler();
    }
    catch (JAXBException e)
    {
      throw new AssertionError();
    }
  }
  
  public final boolean hasEventHandler()
  {
    return getEventHandler() != this;
  }
  
  public <T> JAXBElement<T> unmarshal(Node node, Class<T> expectedType)
    throws JAXBException
  {
    if (expectedType == null) {
      throw new IllegalArgumentException();
    }
    return (JAXBElement)unmarshal0(node, getBeanInfo(expectedType));
  }
  
  public final Object unmarshal(Node node)
    throws JAXBException
  {
    return unmarshal0(node, null);
  }
  
  @Deprecated
  public final Object unmarshal(SAXSource source)
    throws JAXBException
  {
    return super.unmarshal(source);
  }
  
  public final Object unmarshal0(Node node, JaxBeanInfo expectedType)
    throws JAXBException
  {
    try
    {
      DOMScanner scanner = new DOMScanner();
      
      InterningXmlVisitor handler = new InterningXmlVisitor(createUnmarshallerHandler(null, false, expectedType));
      scanner.setContentHandler(new SAXConnector(handler, scanner));
      if ((node instanceof Element)) {
        scanner.scan((Element)node);
      } else if ((node instanceof Document)) {
        scanner.scan((Document)node);
      } else {
        throw new IllegalArgumentException("Unexpected node type: " + node);
      }
      return handler.getContext().getResult();
    }
    catch (SAXException e)
    {
      throw createUnmarshalException(e);
    }
  }
  
  public Object unmarshal(XMLStreamReader reader)
    throws JAXBException
  {
    return unmarshal0(reader, null);
  }
  
  public <T> JAXBElement<T> unmarshal(XMLStreamReader reader, Class<T> expectedType)
    throws JAXBException
  {
    if (expectedType == null) {
      throw new IllegalArgumentException();
    }
    return (JAXBElement)unmarshal0(reader, getBeanInfo(expectedType));
  }
  
  public Object unmarshal0(XMLStreamReader reader, JaxBeanInfo expectedType)
    throws JAXBException
  {
    if (reader == null) {
      throw new IllegalArgumentException(Messages.format("Unmarshaller.NullReader"));
    }
    int eventType = reader.getEventType();
    if ((eventType != 1) && (eventType != 7)) {
      throw new IllegalStateException(Messages.format("Unmarshaller.IllegalReaderState", Integer.valueOf(eventType)));
    }
    XmlVisitor h = createUnmarshallerHandler(null, false, expectedType);
    StAXConnector connector = StAXStreamConnector.create(reader, h);
    try
    {
      connector.bridge();
    }
    catch (XMLStreamException e)
    {
      throw handleStreamException(e);
    }
    return h.getContext().getResult();
  }
  
  public <T> JAXBElement<T> unmarshal(XMLEventReader reader, Class<T> expectedType)
    throws JAXBException
  {
    if (expectedType == null) {
      throw new IllegalArgumentException();
    }
    return (JAXBElement)unmarshal0(reader, getBeanInfo(expectedType));
  }
  
  public Object unmarshal(XMLEventReader reader)
    throws JAXBException
  {
    return unmarshal0(reader, null);
  }
  
  private Object unmarshal0(XMLEventReader reader, JaxBeanInfo expectedType)
    throws JAXBException
  {
    if (reader == null) {
      throw new IllegalArgumentException(Messages.format("Unmarshaller.NullReader"));
    }
    try
    {
      XMLEvent event = reader.peek();
      if ((!event.isStartElement()) && (!event.isStartDocument())) {
        throw new IllegalStateException(Messages.format("Unmarshaller.IllegalReaderState", Integer.valueOf(event.getEventType())));
      }
      boolean isZephyr = reader.getClass().getName().equals("com.sun.xml.stream.XMLReaderImpl");
      XmlVisitor h = createUnmarshallerHandler(null, false, expectedType);
      if (!isZephyr) {
        h = new InterningXmlVisitor(h);
      }
      new StAXEventConnector(reader, h).bridge();
      return h.getContext().getResult();
    }
    catch (XMLStreamException e)
    {
      throw handleStreamException(e);
    }
  }
  
  public Object unmarshal0(InputStream input, JaxBeanInfo expectedType)
    throws JAXBException
  {
    return unmarshal0(getXMLReader(), new InputSource(input), expectedType);
  }
  
  private static JAXBException handleStreamException(XMLStreamException e)
  {
    Throwable ne = e.getNestedException();
    if ((ne instanceof JAXBException)) {
      return (JAXBException)ne;
    }
    if ((ne instanceof SAXException)) {
      return new UnmarshalException(ne);
    }
    return new UnmarshalException(e);
  }
  
  public Object getProperty(String name)
    throws PropertyException
  {
    if (name.equals(IDResolver.class.getName())) {
      return this.idResolver;
    }
    return super.getProperty(name);
  }
  
  public void setProperty(String name, Object value)
    throws PropertyException
  {
    if (name.equals("com.sun.xml.bind.ObjectFactory"))
    {
      this.coordinator.setFactories(value);
      return;
    }
    if (name.equals(IDResolver.class.getName()))
    {
      this.idResolver = ((IDResolver)value);
      return;
    }
    if (name.equals(ClassResolver.class.getName()))
    {
      this.coordinator.classResolver = ((ClassResolver)value);
      return;
    }
    super.setProperty(name, value);
  }
  
  public void setSchema(Schema schema)
  {
    this.schema = schema;
  }
  
  public Schema getSchema()
  {
    return this.schema;
  }
  
  public AttachmentUnmarshaller getAttachmentUnmarshaller()
  {
    return this.attachmentUnmarshaller;
  }
  
  public void setAttachmentUnmarshaller(AttachmentUnmarshaller au)
  {
    this.attachmentUnmarshaller = au;
  }
  
  /**
   * @deprecated
   */
  public boolean isValidating()
  {
    throw new UnsupportedOperationException();
  }
  
  /**
   * @deprecated
   */
  public void setValidating(boolean validating)
  {
    throw new UnsupportedOperationException();
  }
  
  public <A extends XmlAdapter> void setAdapter(Class<A> type, A adapter)
  {
    if (type == null) {
      throw new IllegalArgumentException();
    }
    this.coordinator.putAdapter(type, adapter);
  }
  
  public <A extends XmlAdapter> A getAdapter(Class<A> type)
  {
    if (type == null) {
      throw new IllegalArgumentException();
    }
    if (this.coordinator.containsAdapter(type)) {
      return this.coordinator.getAdapter(type);
    }
    return null;
  }
  
  public UnmarshalException createUnmarshalException(SAXException e)
  {
    return super.createUnmarshalException(e);
  }
  
  public boolean handleEvent(ValidationEvent event)
  {
    return event.getSeverity() != 2;
  }
  
  private static InputSource streamSourceToInputSource(StreamSource ss)
  {
    InputSource is = new InputSource();
    is.setSystemId(ss.getSystemId());
    is.setByteStream(ss.getInputStream());
    is.setCharacterStream(ss.getReader());
    
    return is;
  }
  
  public <T> JaxBeanInfo<T> getBeanInfo(Class<T> clazz)
    throws JAXBException
  {
    return this.context.getBeanInfo(clazz, true);
  }
  
  public Unmarshaller.Listener getListener()
  {
    return this.externalListener;
  }
  
  public void setListener(Unmarshaller.Listener listener)
  {
    this.externalListener = listener;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\UnmarshallerImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */