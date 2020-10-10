package com.sun.org.apache.xml.internal.resolver.tools;

import com.sun.org.apache.xml.internal.resolver.Catalog;
import com.sun.org.apache.xml.internal.resolver.CatalogManager;
import com.sun.org.apache.xml.internal.resolver.helpers.Debug;
import com.sun.org.apache.xml.internal.resolver.helpers.FileURL;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class CatalogResolver
  implements EntityResolver, URIResolver
{
  public boolean namespaceAware = true;
  public boolean validating = false;
  private Catalog catalog = null;
  private CatalogManager catalogManager = CatalogManager.getStaticManager();
  
  public CatalogResolver()
  {
    initializeCatalogs(false);
  }
  
  public CatalogResolver(boolean privateCatalog)
  {
    initializeCatalogs(privateCatalog);
  }
  
  public CatalogResolver(CatalogManager manager)
  {
    this.catalogManager = manager;
    initializeCatalogs(!this.catalogManager.getUseStaticCatalog());
  }
  
  private void initializeCatalogs(boolean privateCatalog)
  {
    this.catalog = this.catalogManager.getCatalog();
  }
  
  public Catalog getCatalog()
  {
    return this.catalog;
  }
  
  public String getResolvedEntity(String publicId, String systemId)
  {
    String resolved = null;
    if (this.catalog == null)
    {
      this.catalogManager.debug.message(1, "Catalog resolution attempted with null catalog; ignored");
      return null;
    }
    if (systemId != null) {
      try
      {
        resolved = this.catalog.resolveSystem(systemId);
      }
      catch (MalformedURLException me)
      {
        this.catalogManager.debug.message(1, "Malformed URL exception trying to resolve", publicId);
        
        resolved = null;
      }
      catch (IOException ie)
      {
        this.catalogManager.debug.message(1, "I/O exception trying to resolve", publicId);
        resolved = null;
      }
    }
    if (resolved == null)
    {
      if (publicId != null) {
        try
        {
          resolved = this.catalog.resolvePublic(publicId, systemId);
        }
        catch (MalformedURLException me)
        {
          this.catalogManager.debug.message(1, "Malformed URL exception trying to resolve", publicId);
        }
        catch (IOException ie)
        {
          this.catalogManager.debug.message(1, "I/O exception trying to resolve", publicId);
        }
      }
      if (resolved != null) {
        this.catalogManager.debug.message(2, "Resolved public", publicId, resolved);
      }
    }
    else
    {
      this.catalogManager.debug.message(2, "Resolved system", systemId, resolved);
    }
    return resolved;
  }
  
  public InputSource resolveEntity(String publicId, String systemId)
  {
    String resolved = getResolvedEntity(publicId, systemId);
    if (resolved != null) {
      try
      {
        InputSource iSource = new InputSource(resolved);
        iSource.setPublicId(publicId);
        
        URL url = new URL(resolved);
        InputStream iStream = url.openStream();
        iSource.setByteStream(iStream);
        
        return iSource;
      }
      catch (Exception e)
      {
        this.catalogManager.debug.message(1, "Failed to create InputSource", resolved);
        return null;
      }
    }
    return null;
  }
  
  public Source resolve(String href, String base)
    throws TransformerException
  {
    String uri = href;
    String fragment = null;
    int hashPos = href.indexOf("#");
    if (hashPos >= 0)
    {
      uri = href.substring(0, hashPos);
      fragment = href.substring(hashPos + 1);
    }
    String result = null;
    try
    {
      result = this.catalog.resolveURI(href);
    }
    catch (Exception e) {}
    if (result == null) {
      try
      {
        URL url = null;
        if (base == null)
        {
          url = new URL(uri);
          result = url.toString();
        }
        else
        {
          URL baseURL = new URL(base);
          url = href.length() == 0 ? baseURL : new URL(baseURL, uri);
          result = url.toString();
        }
      }
      catch (MalformedURLException mue)
      {
        String absBase = makeAbsolute(base);
        if (!absBase.equals(base)) {
          return resolve(href, absBase);
        }
        throw new TransformerException("Malformed URL " + href + "(base " + base + ")", mue);
      }
    }
    this.catalogManager.debug.message(2, "Resolved URI", href, result);
    
    SAXSource source = new SAXSource();
    source.setInputSource(new InputSource(result));
    setEntityResolver(source);
    return source;
  }
  
  private void setEntityResolver(SAXSource source)
    throws TransformerException
  {
    XMLReader reader = source.getXMLReader();
    if (reader == null)
    {
      SAXParserFactory spFactory = SAXParserFactory.newInstance();
      spFactory.setNamespaceAware(true);
      try
      {
        reader = spFactory.newSAXParser().getXMLReader();
      }
      catch (ParserConfigurationException ex)
      {
        throw new TransformerException(ex);
      }
      catch (SAXException ex)
      {
        throw new TransformerException(ex);
      }
    }
    reader.setEntityResolver(this);
    source.setXMLReader(reader);
  }
  
  private String makeAbsolute(String uri)
  {
    if (uri == null) {
      uri = "";
    }
    try
    {
      URL url = new URL(uri);
      return url.toString();
    }
    catch (MalformedURLException mue)
    {
      try
      {
        URL fileURL = FileURL.makeURL(uri);
        return fileURL.toString();
      }
      catch (MalformedURLException mue2) {}
    }
    return uri;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\org\apache\xml\internal\resolver\tools\CatalogResolver.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */