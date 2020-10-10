package com.sun.xml.xsom.parser;

import com.sun.xml.xsom.impl.parser.Messages;
import java.io.IOException;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

public class JAXPParser
  implements XMLParser
{
  private final SAXParserFactory factory;
  
  public JAXPParser(SAXParserFactory factory)
  {
    factory.setNamespaceAware(true);
    this.factory = factory;
  }
  
  public JAXPParser()
  {
    this(SAXParserFactory.newInstance());
  }
  
  public void parse(InputSource source, ContentHandler handler, ErrorHandler errorHandler, EntityResolver entityResolver)
    throws SAXException, IOException
  {
    try
    {
      XMLReader reader = this.factory.newSAXParser().getXMLReader();
      reader = new XMLReaderEx(reader);
      
      reader.setContentHandler(handler);
      if (errorHandler != null) {
        reader.setErrorHandler(errorHandler);
      }
      if (entityResolver != null) {
        reader.setEntityResolver(entityResolver);
      }
      reader.parse(source);
    }
    catch (ParserConfigurationException e)
    {
      SAXParseException spe = new SAXParseException(e.getMessage(), null, e);
      errorHandler.fatalError(spe);
      throw spe;
    }
  }
  
  private static class XMLReaderEx
    extends XMLFilterImpl
  {
    private Locator locator;
    
    XMLReaderEx(XMLReader parent)
    {
      setParent(parent);
    }
    
    public InputSource resolveEntity(String publicId, String systemId)
      throws SAXException
    {
      try
      {
        InputSource is = null;
        if (getEntityResolver() != null) {
          is = getEntityResolver().resolveEntity(publicId, systemId);
        }
        if (is != null) {
          return is;
        }
        is = new InputSource(new URL(systemId).openStream());
        is.setSystemId(systemId);
        is.setPublicId(publicId);
        return is;
      }
      catch (IOException e)
      {
        SAXParseException spe = new SAXParseException(Messages.format("EntityResolutionFailure", new Object[] { systemId, e.toString() }), this.locator, e);
        if (getErrorHandler() != null) {
          getErrorHandler().fatalError(spe);
        }
        throw spe;
      }
    }
    
    public void setDocumentLocator(Locator locator)
    {
      super.setDocumentLocator(locator);
      this.locator = locator;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\parser\JAXPParser.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */