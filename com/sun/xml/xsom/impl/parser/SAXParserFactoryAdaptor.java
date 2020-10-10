package com.sun.xml.xsom.impl.parser;

import com.sun.xml.xsom.parser.XMLParser;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderAdapter;

public class SAXParserFactoryAdaptor
  extends SAXParserFactory
{
  private final XMLParser parser;
  
  public SAXParserFactoryAdaptor(XMLParser _parser)
  {
    this.parser = _parser;
  }
  
  public SAXParser newSAXParser()
    throws ParserConfigurationException, SAXException
  {
    return new SAXParserImpl(null);
  }
  
  public void setFeature(String name, boolean value) {}
  
  public boolean getFeature(String name)
  {
    return false;
  }
  
  private class SAXParserImpl
    extends SAXParser
  {
    private final SAXParserFactoryAdaptor.XMLReaderImpl reader = new SAXParserFactoryAdaptor.XMLReaderImpl(SAXParserFactoryAdaptor.this, null);
    
    private SAXParserImpl() {}
    
    /**
     * @deprecated
     */
    public Parser getParser()
      throws SAXException
    {
      return new XMLReaderAdapter(this.reader);
    }
    
    public XMLReader getXMLReader()
      throws SAXException
    {
      return this.reader;
    }
    
    public boolean isNamespaceAware()
    {
      return true;
    }
    
    public boolean isValidating()
    {
      return false;
    }
    
    public void setProperty(String name, Object value) {}
    
    public Object getProperty(String name)
    {
      return null;
    }
  }
  
  private class XMLReaderImpl
    extends XMLFilterImpl
  {
    private XMLReaderImpl() {}
    
    public void parse(InputSource input)
      throws IOException, SAXException
    {
      SAXParserFactoryAdaptor.this.parser.parse(input, this, this, this);
    }
    
    public void parse(String systemId)
      throws IOException, SAXException
    {
      SAXParserFactoryAdaptor.this.parser.parse(new InputSource(systemId), this, this, this);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\parser\SAXParserFactoryAdaptor.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */