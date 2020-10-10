package org.kohsuke.rngom.xml.sax;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class JAXPXMLReaderCreator
  implements XMLReaderCreator
{
  private final SAXParserFactory spf;
  
  public JAXPXMLReaderCreator(SAXParserFactory spf)
  {
    this.spf = spf;
  }
  
  public JAXPXMLReaderCreator()
  {
    this.spf = SAXParserFactory.newInstance();
    this.spf.setNamespaceAware(true);
  }
  
  public XMLReader createXMLReader()
    throws SAXException
  {
    try
    {
      return this.spf.newSAXParser().getXMLReader();
    }
    catch (ParserConfigurationException e)
    {
      throw new SAXException(e);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\xml\sax\JAXPXMLReaderCreator.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */