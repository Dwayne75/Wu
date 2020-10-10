package com.sun.tools.xjc.reader.internalizer;

import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.LocatorImpl;
import org.xml.sax.helpers.XMLFilterImpl;

class VersionChecker
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
          SAXParseException e = new SAXParseException(Messages.format("Internalizer.TwoVersionAttributes"), this.locator);
          
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
      SAXParseException e = new SAXParseException(Messages.format("Internalizer.VersionNotPresent"), this.rootTagStart);
      
      getErrorHandler().error(e);
    }
    if ((this.version != null) && (!this.version.equals("1.0")))
    {
      SAXParseException e = new SAXParseException(Messages.format("Internalizer.IncorrectVersion"), this.rootTagStart);
      
      getErrorHandler().error(e);
    }
  }
  
  public void setDocumentLocator(Locator locator)
  {
    super.setDocumentLocator(locator);
    this.locator = locator;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\internalizer\VersionChecker.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */