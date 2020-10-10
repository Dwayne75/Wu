package com.sun.org.apache.xml.internal.resolver.tools;

import com.sun.org.apache.xml.internal.resolver.Catalog;
import com.sun.org.apache.xml.internal.resolver.CatalogManager;
import com.sun.org.apache.xml.internal.resolver.helpers.Debug;
import com.sun.org.apache.xml.internal.resolver.helpers.FileURL;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

public class ResolvingXMLFilter
  extends XMLFilterImpl
{
  public static boolean suppressExplanation = false;
  private CatalogManager catalogManager = CatalogManager.getStaticManager();
  private CatalogResolver catalogResolver = null;
  private CatalogResolver piCatalogResolver = null;
  private boolean allowXMLCatalogPI = false;
  private boolean oasisXMLCatalogPI = false;
  private URL baseURL = null;
  
  public ResolvingXMLFilter()
  {
    this.catalogResolver = new CatalogResolver(this.catalogManager);
  }
  
  public ResolvingXMLFilter(XMLReader parent)
  {
    super(parent);
    this.catalogResolver = new CatalogResolver(this.catalogManager);
  }
  
  public ResolvingXMLFilter(CatalogManager manager)
  {
    this.catalogManager = manager;
    this.catalogResolver = new CatalogResolver(this.catalogManager);
  }
  
  public ResolvingXMLFilter(XMLReader parent, CatalogManager manager)
  {
    super(parent);
    this.catalogManager = manager;
    this.catalogResolver = new CatalogResolver(this.catalogManager);
  }
  
  public Catalog getCatalog()
  {
    return this.catalogResolver.getCatalog();
  }
  
  public void parse(InputSource input)
    throws IOException, SAXException
  {
    this.allowXMLCatalogPI = true;
    
    setupBaseURI(input.getSystemId());
    try
    {
      super.parse(input);
    }
    catch (InternalError ie)
    {
      explain(input.getSystemId());
      throw ie;
    }
  }
  
  public void parse(String systemId)
    throws IOException, SAXException
  {
    this.allowXMLCatalogPI = true;
    
    setupBaseURI(systemId);
    try
    {
      super.parse(systemId);
    }
    catch (InternalError ie)
    {
      explain(systemId);
      throw ie;
    }
  }
  
  public InputSource resolveEntity(String publicId, String systemId)
  {
    this.allowXMLCatalogPI = false;
    String resolved = this.catalogResolver.getResolvedEntity(publicId, systemId);
    if ((resolved == null) && (this.piCatalogResolver != null)) {
      resolved = this.piCatalogResolver.getResolvedEntity(publicId, systemId);
    }
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
  
  public void notationDecl(String name, String publicId, String systemId)
    throws SAXException
  {
    this.allowXMLCatalogPI = false;
    super.notationDecl(name, publicId, systemId);
  }
  
  public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName)
    throws SAXException
  {
    this.allowXMLCatalogPI = false;
    super.unparsedEntityDecl(name, publicId, systemId, notationName);
  }
  
  public void startElement(String uri, String localName, String qName, Attributes atts)
    throws SAXException
  {
    this.allowXMLCatalogPI = false;
    super.startElement(uri, localName, qName, atts);
  }
  
  public void processingInstruction(String target, String pidata)
    throws SAXException
  {
    if (target.equals("oasis-xml-catalog"))
    {
      URL catalog = null;
      String data = pidata;
      
      int pos = data.indexOf("catalog=");
      if (pos >= 0)
      {
        data = data.substring(pos + 8);
        if (data.length() > 1)
        {
          String quote = data.substring(0, 1);
          data = data.substring(1);
          pos = data.indexOf(quote);
          if (pos >= 0)
          {
            data = data.substring(0, pos);
            try
            {
              if (this.baseURL != null) {
                catalog = new URL(this.baseURL, data);
              } else {
                catalog = new URL(data);
              }
            }
            catch (MalformedURLException mue) {}
          }
        }
      }
      if (this.allowXMLCatalogPI)
      {
        if (this.catalogManager.getAllowOasisXMLCatalogPI())
        {
          this.catalogManager.debug.message(4, "oasis-xml-catalog PI", pidata);
          if (catalog != null) {
            try
            {
              this.catalogManager.debug.message(4, "oasis-xml-catalog", catalog.toString());
              this.oasisXMLCatalogPI = true;
              if (this.piCatalogResolver == null) {
                this.piCatalogResolver = new CatalogResolver(true);
              }
              this.piCatalogResolver.getCatalog().parseCatalog(catalog.toString());
            }
            catch (Exception e)
            {
              this.catalogManager.debug.message(3, "Exception parsing oasis-xml-catalog: " + catalog.toString());
            }
          } else {
            this.catalogManager.debug.message(3, "PI oasis-xml-catalog unparseable: " + pidata);
          }
        }
        else
        {
          this.catalogManager.debug.message(4, "PI oasis-xml-catalog ignored: " + pidata);
        }
      }
      else {
        this.catalogManager.debug.message(3, "PI oasis-xml-catalog occurred in an invalid place: " + pidata);
      }
    }
    else
    {
      super.processingInstruction(target, pidata);
    }
  }
  
  private void setupBaseURI(String systemId)
  {
    URL cwd = null;
    try
    {
      cwd = FileURL.makeURL("basename");
    }
    catch (MalformedURLException mue)
    {
      cwd = null;
    }
    try
    {
      this.baseURL = new URL(systemId);
    }
    catch (MalformedURLException mue)
    {
      if (cwd != null) {
        try
        {
          this.baseURL = new URL(cwd, systemId);
        }
        catch (MalformedURLException mue2)
        {
          this.baseURL = null;
        }
      } else {
        this.baseURL = null;
      }
    }
  }
  
  private void explain(String systemId)
  {
    if (!suppressExplanation)
    {
      System.out.println("XMLReader probably encountered bad URI in " + systemId);
      System.out.println("For example, replace '/some/uri' with 'file:/some/uri'.");
    }
    suppressExplanation = true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\org\apache\xml\internal\resolver\tools\ResolvingXMLFilter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */