package org.seamless.xml;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class SAXParser
{
  private static final Logger log = Logger.getLogger(SAXParser.class.getName());
  public static final URI XML_SCHEMA_NAMESPACE = URI.create("http://www.w3.org/2001/xml.xsd");
  public static final URL XML_SCHEMA_RESOURCE = Thread.currentThread().getContextClassLoader().getResource("org/seamless/schemas/xml.xsd");
  private final XMLReader xr;
  
  public SAXParser()
  {
    this(null);
  }
  
  public SAXParser(DefaultHandler handler)
  {
    this.xr = create();
    if (handler != null) {
      this.xr.setContentHandler(handler);
    }
  }
  
  public void setContentHandler(ContentHandler handler)
  {
    this.xr.setContentHandler(handler);
  }
  
  protected XMLReader create()
  {
    try
    {
      if (getSchemaSources() != null)
      {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setSchema(createSchema(getSchemaSources()));
        XMLReader xmlReader = factory.newSAXParser().getXMLReader();
        xmlReader.setErrorHandler(getErrorHandler());
        return xmlReader;
      }
      return XMLReaderFactory.createXMLReader();
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  protected Schema createSchema(Source[] schemaSources)
  {
    try
    {
      SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
      schemaFactory.setResourceResolver(new CatalogResourceResolver(new HashMap() {}));
      return schemaFactory.newSchema(schemaSources);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  protected Source[] getSchemaSources()
  {
    return null;
  }
  
  protected ErrorHandler getErrorHandler()
  {
    return new SimpleErrorHandler();
  }
  
  public void parse(InputSource source)
    throws ParserException
  {
    try
    {
      this.xr.parse(source);
    }
    catch (Exception ex)
    {
      throw new ParserException(ex);
    }
  }
  
  public class SimpleErrorHandler
    implements ErrorHandler
  {
    public SimpleErrorHandler() {}
    
    public void warning(SAXParseException e)
      throws SAXException
    {
      throw new SAXException(e);
    }
    
    public void error(SAXParseException e)
      throws SAXException
    {
      throw new SAXException(e);
    }
    
    public void fatalError(SAXParseException e)
      throws SAXException
    {
      throw new SAXException(e);
    }
  }
  
  public static class Handler<I>
    extends DefaultHandler
  {
    protected SAXParser parser;
    protected I instance;
    protected Handler parent;
    protected StringBuilder characters = new StringBuilder();
    protected Attributes attributes;
    
    public Handler(I instance)
    {
      this(instance, null, null);
    }
    
    public Handler(I instance, SAXParser parser)
    {
      this(instance, parser, null);
    }
    
    public Handler(I instance, Handler parent)
    {
      this(instance, parent.getParser(), parent);
    }
    
    public Handler(I instance, SAXParser parser, Handler parent)
    {
      this.instance = instance;
      this.parser = parser;
      this.parent = parent;
      if (parser != null) {
        parser.setContentHandler(this);
      }
    }
    
    public I getInstance()
    {
      return (I)this.instance;
    }
    
    public SAXParser getParser()
    {
      return this.parser;
    }
    
    public Handler getParent()
    {
      return this.parent;
    }
    
    protected void switchToParent()
    {
      if ((this.parser != null) && (this.parent != null))
      {
        this.parser.setContentHandler(this.parent);
        this.attributes = null;
      }
    }
    
    public String getCharacters()
    {
      return this.characters.toString();
    }
    
    public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException
    {
      this.characters = new StringBuilder();
      this.attributes = new AttributesImpl(attributes);
      SAXParser.log.finer(getClass().getSimpleName() + " starting: " + localName);
    }
    
    public void characters(char[] ch, int start, int length)
      throws SAXException
    {
      this.characters.append(ch, start, length);
    }
    
    public void endElement(String uri, String localName, String qName)
      throws SAXException
    {
      if (isLastElement(uri, localName, qName))
      {
        SAXParser.log.finer(getClass().getSimpleName() + ": last element, switching to parent: " + localName);
        switchToParent();
        return;
      }
      SAXParser.log.finer(getClass().getSimpleName() + " ending: " + localName);
    }
    
    protected boolean isLastElement(String uri, String localName, String qName)
    {
      return false;
    }
    
    protected Attributes getAttributes()
    {
      return this.attributes;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\xml\SAXParser.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */