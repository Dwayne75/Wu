package com.sun.tools.xjc.reader.internalizer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.LocatorImpl;
import org.xml.sax.helpers.XMLFilterImpl;

public class VersionChecker
  extends XMLFilterImpl
{
  private String version = null;
  private boolean seenRoot = false;
  private boolean seenBindings = false;
  private Locator locator;
  private Locator rootTagStart;
  
  public VersionChecker(XMLReader parent)
  {
    setParent(parent);
  }
  
  public VersionChecker(ContentHandler handler, ErrorHandler eh, EntityResolver er)
  {
    setContentHandler(handler);
    if (eh != null) {
      setErrorHandler(eh);
    }
    if (er != null) {
      setEntityResolver(er);
    }
  }
  
  public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
    throws SAXException
  {
    super.startElement(namespaceURI, localName, qName, atts);
    if (!this.seenRoot)
    {
      this.seenRoot = true;
      this.rootTagStart = new LocatorImpl(this.locator);
      
      this.version = atts.getValue("http://java.sun.com/xml/ns/jaxb", "version");
      if (namespaceURI.equals("http://java.sun.com/xml/ns/jaxb"))
      {
        String version2 = atts.getValue("", "version");
        if ((this.version != null) && (version2 != null))
        {
          SAXParseException e = new SAXParseException(Messages.format("Internalizer.TwoVersionAttributes", new Object[0]), this.locator);
          
          getErrorHandler().error(e);
        }
        if (this.version == null) {
          this.version = version2;
        }
      }
    }
    if ("http://java.sun.com/xml/ns/jaxb".equals(namespaceURI)) {
      this.seenBindings = true;
    }
  }
  
  public void endDocument()
    throws SAXException
  {
    super.endDocument();
    if ((this.seenBindings) && (this.version == null))
    {
      SAXParseException e = new SAXParseException(Messages.format("Internalizer.VersionNotPresent", new Object[0]), this.rootTagStart);
      
      getErrorHandler().error(e);
    }
    if ((this.version != null) && (!VERSIONS.contains(this.version)))
    {
      SAXParseException e = new SAXParseException(Messages.format("Internalizer.IncorrectVersion", new Object[0]), this.rootTagStart);
      
      getErrorHandler().error(e);
    }
  }
  
  public void setDocumentLocator(Locator locator)
  {
    super.setDocumentLocator(locator);
    this.locator = locator;
  }
  
  private static final Set<String> VERSIONS = new HashSet(Arrays.asList(new String[] { "1.0", "2.0", "2.1" }));
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\internalizer\VersionChecker.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */