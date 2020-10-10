package org.seamless.xml;

import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

public class CatalogResourceResolver
  implements LSResourceResolver
{
  private static Logger log = Logger.getLogger(CatalogResourceResolver.class.getName());
  private final Map<URI, URL> catalog;
  
  public CatalogResourceResolver(Map<URI, URL> catalog)
  {
    this.catalog = catalog;
  }
  
  public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI)
  {
    log.finest("Trying to resolve system identifier URI in catalog: " + systemId);
    URL systemURL;
    if ((systemURL = (URL)this.catalog.get(URI.create(systemId))) != null)
    {
      log.finest("Loading catalog resource: " + systemURL);
      try
      {
        Input i = new Input(systemURL.openStream());
        i.setBaseURI(baseURI);
        i.setSystemId(systemId);
        i.setPublicId(publicId);
        return i;
      }
      catch (Exception ex)
      {
        throw new RuntimeException(ex);
      }
    }
    log.info("System identifier not found in catalog, continuing with default resolution (this most likely means remote HTTP request!): " + systemId);
    
    return null;
  }
  
  private static final class Input
    implements LSInput
  {
    InputStream in;
    
    public Input(InputStream in)
    {
      this.in = in;
    }
    
    public Reader getCharacterStream()
    {
      return null;
    }
    
    public void setCharacterStream(Reader characterStream) {}
    
    public InputStream getByteStream()
    {
      return this.in;
    }
    
    public void setByteStream(InputStream byteStream) {}
    
    public String getStringData()
    {
      return null;
    }
    
    public void setStringData(String stringData) {}
    
    public String getSystemId()
    {
      return null;
    }
    
    public void setSystemId(String systemId) {}
    
    public String getPublicId()
    {
      return null;
    }
    
    public void setPublicId(String publicId) {}
    
    public String getBaseURI()
    {
      return null;
    }
    
    public void setBaseURI(String baseURI) {}
    
    public String getEncoding()
    {
      return null;
    }
    
    public void setEncoding(String encoding) {}
    
    public boolean getCertifiedText()
    {
      return false;
    }
    
    public void setCertifiedText(boolean certifiedText) {}
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\xml\CatalogResourceResolver.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */