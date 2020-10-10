package javax.xml.bind.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

public class JAXBSource
  extends SAXSource
{
  private final Marshaller marshaller;
  private final Object contentObject;
  
  public JAXBSource(JAXBContext context, Object contentObject)
    throws JAXBException
  {
    this(context == null ? assertionFailed(Messages.format("JAXBSource.NullContext")) : context.createMarshaller(), contentObject == null ? assertionFailed(Messages.format("JAXBSource.NullContent")) : contentObject);
  }
  
  public JAXBSource(Marshaller marshaller, Object contentObject)
    throws JAXBException
  {
    if (marshaller == null) {
      throw new JAXBException(Messages.format("JAXBSource.NullMarshaller"));
    }
    if (contentObject == null) {
      throw new JAXBException(Messages.format("JAXBSource.NullContent"));
    }
    this.marshaller = marshaller;
    this.contentObject = contentObject;
    
    super.setXMLReader(this.pseudoParser);
    
    super.setInputSource(new InputSource());
  }
  
  private final XMLReader pseudoParser = new XMLReader()
  {
    private LexicalHandler lexicalHandler;
    private EntityResolver entityResolver;
    private DTDHandler dtdHandler;
    
    public boolean getFeature(String name)
      throws SAXNotRecognizedException
    {
      if (name.equals("http://xml.org/sax/features/namespaces")) {
        return true;
      }
      if (name.equals("http://xml.org/sax/features/namespace-prefixes")) {
        return false;
      }
      throw new SAXNotRecognizedException(name);
    }
    
    public void setFeature(String name, boolean value)
      throws SAXNotRecognizedException
    {
      if ((name.equals("http://xml.org/sax/features/namespaces")) && (value)) {
        return;
      }
      if ((name.equals("http://xml.org/sax/features/namespace-prefixes")) && (!value)) {
        return;
      }
      throw new SAXNotRecognizedException(name);
    }
    
    public Object getProperty(String name)
      throws SAXNotRecognizedException
    {
      if ("http://xml.org/sax/properties/lexical-handler".equals(name)) {
        return this.lexicalHandler;
      }
      throw new SAXNotRecognizedException(name);
    }
    
    public void setProperty(String name, Object value)
      throws SAXNotRecognizedException
    {
      if ("http://xml.org/sax/properties/lexical-handler".equals(name))
      {
        this.lexicalHandler = ((LexicalHandler)value);
        return;
      }
      throw new SAXNotRecognizedException(name);
    }
    
    public void setEntityResolver(EntityResolver resolver)
    {
      this.entityResolver = resolver;
    }
    
    public EntityResolver getEntityResolver()
    {
      return this.entityResolver;
    }
    
    public void setDTDHandler(DTDHandler handler)
    {
      this.dtdHandler = handler;
    }
    
    public DTDHandler getDTDHandler()
    {
      return this.dtdHandler;
    }
    
    private XMLFilterImpl repeater = new XMLFilterImpl();
    private ErrorHandler errorHandler;
    
    public void setContentHandler(ContentHandler handler)
    {
      this.repeater.setContentHandler(handler);
    }
    
    public ContentHandler getContentHandler()
    {
      return this.repeater.getContentHandler();
    }
    
    public void setErrorHandler(ErrorHandler handler)
    {
      this.errorHandler = handler;
    }
    
    public ErrorHandler getErrorHandler()
    {
      return this.errorHandler;
    }
    
    public void parse(InputSource input)
      throws SAXException
    {
      parse();
    }
    
    public void parse(String systemId)
      throws SAXException
    {
      parse();
    }
    
    public void parse()
      throws SAXException
    {
      try
      {
        JAXBSource.this.marshaller.marshal(JAXBSource.this.contentObject, this.repeater);
      }
      catch (JAXBException e)
      {
        SAXParseException se = new SAXParseException(e.getMessage(), null, null, -1, -1, e);
        if (this.errorHandler != null) {
          this.errorHandler.fatalError(se);
        }
        throw se;
      }
    }
  };
  
  private static Marshaller assertionFailed(String message)
    throws JAXBException
  {
    throw new JAXBException(message);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\bind\util\JAXBSource.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */