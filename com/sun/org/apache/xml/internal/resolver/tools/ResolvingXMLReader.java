package com.sun.org.apache.xml.internal.resolver.tools;

import com.sun.org.apache.xml.internal.resolver.CatalogManager;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class ResolvingXMLReader
  extends ResolvingXMLFilter
{
  public static boolean namespaceAware = true;
  public static boolean validating = false;
  
  public ResolvingXMLReader()
  {
    SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setNamespaceAware(namespaceAware);
    spf.setValidating(validating);
    try
    {
      SAXParser parser = spf.newSAXParser();
      setParent(parser.getXMLReader());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  public ResolvingXMLReader(CatalogManager manager)
  {
    super(manager);
    SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setNamespaceAware(namespaceAware);
    spf.setValidating(validating);
    try
    {
      SAXParser parser = spf.newSAXParser();
      setParent(parser.getXMLReader());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\org\apache\xml\internal\resolver\tools\ResolvingXMLReader.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */