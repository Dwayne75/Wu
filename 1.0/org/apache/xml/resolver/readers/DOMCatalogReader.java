package org.apache.xml.resolver.readers;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogException;
import org.apache.xml.resolver.helpers.Debug;
import org.apache.xml.resolver.helpers.Namespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class DOMCatalogReader
  implements CatalogReader
{
  protected Hashtable namespaceMap = new Hashtable();
  
  public void setCatalogParser(String paramString1, String paramString2, String paramString3)
  {
    if (paramString1 == null) {
      this.namespaceMap.put(paramString2, paramString3);
    } else {
      this.namespaceMap.put("{" + paramString1 + "}" + paramString2, paramString3);
    }
  }
  
  public String getCatalogParser(String paramString1, String paramString2)
  {
    if (paramString1 == null) {
      return (String)this.namespaceMap.get(paramString2);
    }
    return (String)this.namespaceMap.get("{" + paramString1 + "}" + paramString2);
  }
  
  public void readCatalog(Catalog paramCatalog, InputStream paramInputStream)
    throws IOException, CatalogException
  {
    DocumentBuilderFactory localDocumentBuilderFactory = null;
    DocumentBuilder localDocumentBuilder = null;
    localDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
    localDocumentBuilderFactory.setNamespaceAware(false);
    localDocumentBuilderFactory.setValidating(false);
    try
    {
      localDocumentBuilder = localDocumentBuilderFactory.newDocumentBuilder();
    }
    catch (ParserConfigurationException localParserConfigurationException)
    {
      throw new CatalogException(6);
    }
    Document localDocument = null;
    try
    {
      localDocument = localDocumentBuilder.parse(paramInputStream);
    }
    catch (SAXException localSAXException)
    {
      throw new CatalogException(5);
    }
    Element localElement = localDocument.getDocumentElement();
    String str1 = Namespaces.getNamespaceURI(localElement);
    String str2 = Namespaces.getLocalName(localElement);
    String str3 = getCatalogParser(str1, str2);
    if (str3 == null)
    {
      if (str1 == null) {
        Debug.message(1, "No Catalog parser for " + str2);
      } else {
        Debug.message(1, "No Catalog parser for {" + str1 + "}" + str2);
      }
      return;
    }
    DOMCatalogParser localDOMCatalogParser = null;
    try
    {
      localDOMCatalogParser = (DOMCatalogParser)Class.forName(str3).newInstance();
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      Debug.message(1, "Cannot load XML Catalog Parser class", str3);
      throw new CatalogException(6);
    }
    catch (InstantiationException localInstantiationException)
    {
      Debug.message(1, "Cannot instantiate XML Catalog Parser class", str3);
      throw new CatalogException(6);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      Debug.message(1, "Cannot access XML Catalog Parser class", str3);
      throw new CatalogException(6);
    }
    catch (ClassCastException localClassCastException)
    {
      Debug.message(1, "Cannot cast XML Catalog Parser class", str3);
      throw new CatalogException(6);
    }
    for (Node localNode = localElement.getFirstChild(); localNode != null; localNode = localNode.getNextSibling()) {
      localDOMCatalogParser.parseCatalogEntry(paramCatalog, localNode);
    }
  }
  
  public void readCatalog(Catalog paramCatalog, String paramString)
    throws MalformedURLException, IOException, CatalogException
  {
    URL localURL = new URL(paramString);
    URLConnection localURLConnection = localURL.openConnection();
    readCatalog(paramCatalog, localURLConnection.getInputStream());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\org\apache\xml\resolver\readers\DOMCatalogReader.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */