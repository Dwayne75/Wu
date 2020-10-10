package com.sun.xml.xsom.parser;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

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
      reader = new JAXPParser.XMLReaderEx(reader);
      
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
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\parser\JAXPParser.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */