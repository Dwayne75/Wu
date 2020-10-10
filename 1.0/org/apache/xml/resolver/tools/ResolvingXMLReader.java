package org.apache.xml.resolver.tools;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.helpers.XMLFilterImpl;

public class ResolvingXMLReader
  extends ResolvingXMLFilter
{
  public ResolvingXMLReader()
  {
    SAXParserFactory localSAXParserFactory = SAXParserFactory.newInstance();
    try
    {
      SAXParser localSAXParser = localSAXParserFactory.newSAXParser();
      setParent(localSAXParser.getXMLReader());
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\org\apache\xml\resolver\tools\ResolvingXMLReader.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */