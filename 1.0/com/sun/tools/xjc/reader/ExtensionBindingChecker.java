package com.sun.tools.xjc.reader;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.NamespaceSupport;
import org.xml.sax.helpers.XMLFilterImpl;

public class ExtensionBindingChecker
  extends XMLFilterImpl
{
  private final NamespaceSupport nsSupport = new NamespaceSupport();
  private int count = 0;
  private final Set enabledExtensions = new HashSet();
  private Locator locator;
  private int cutDepth = 0;
  private static final ContentHandler stub = new DefaultHandler();
  private ContentHandler next;
  private final String schemaLanguage;
  
  public ExtensionBindingChecker(String schemaLanguage, ErrorHandler handler)
  {
    this.schemaLanguage = schemaLanguage;
    setErrorHandler(handler);
  }
  
  protected boolean isSupportedExtension(String namespaceUri)
  {
    return namespaceUri.equals("http://java.sun.com/xml/ns/jaxb/xjc");
  }
  
  private boolean needsToBePruned(String uri)
  {
    if (uri.equals(this.schemaLanguage)) {
      return false;
    }
    if (uri.equals("http://java.sun.com/xml/ns/jaxb")) {
      return false;
    }
    if (this.enabledExtensions.contains(uri)) {
      return false;
    }
    return isSupportedExtension(uri);
  }
  
  public void startDocument()
    throws SAXException
  {
    super.startDocument();
    
    this.count = 0;
    this.cutDepth = 0;
    this.nsSupport.reset();
    this.enabledExtensions.clear();
  }
  
  public void startPrefixMapping(String prefix, String uri)
    throws SAXException
  {
    super.startPrefixMapping(prefix, uri);
    this.nsSupport.pushContext();
    this.nsSupport.declarePrefix(prefix, uri);
  }
  
  public void endPrefixMapping(String prefix)
    throws SAXException
  {
    super.endPrefixMapping(prefix);
    this.nsSupport.popContext();
  }
  
  public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
    throws SAXException
  {
    if (this.cutDepth == 0)
    {
      String v = atts.getValue("http://java.sun.com/xml/ns/jaxb", "extensionBindingPrefixes");
      if (v != null)
      {
        if (this.count != 0) {
          error(Messages.format("ExtensionBindingChecker.UnexpectedExtensionBindingPrefixes"));
        }
        StringTokenizer tokens = new StringTokenizer(v);
        while (tokens.hasMoreTokens())
        {
          String prefix = tokens.nextToken();
          String uri = this.nsSupport.getURI(prefix);
          if (uri == null)
          {
            error(Messages.format("ExtensionBindingChecker.UndeclaredPrefix", prefix));
          }
          else
          {
            if (!isSupportedExtension(uri)) {
              error(Messages.format("ExtensionBindingChecker.UnsupportedExtension", prefix));
            }
            this.enabledExtensions.add(uri);
          }
        }
      }
      if (needsToBePruned(namespaceURI))
      {
        if (isSupportedExtension(namespaceURI)) {
          warning(Messages.format("ExtensionBindingChecker.SupportedExtensionIgnored", namespaceURI));
        }
        super.setContentHandler(stub);
        this.cutDepth = 1;
      }
    }
    else
    {
      this.cutDepth += 1;
    }
    this.count += 1;
    super.startElement(namespaceURI, localName, qName, atts);
  }
  
  public void endElement(String namespaceURI, String localName, String qName)
    throws SAXException
  {
    super.endElement(namespaceURI, localName, qName);
    if (this.cutDepth != 0)
    {
      this.cutDepth -= 1;
      if (this.cutDepth == 0) {
        super.setContentHandler(this.next);
      }
    }
  }
  
  public void setDocumentLocator(Locator locator)
  {
    super.setDocumentLocator(locator);
    this.locator = locator;
  }
  
  public void setContentHandler(ContentHandler handler)
  {
    this.next = handler;
    if (getContentHandler() != stub) {
      super.setContentHandler(handler);
    }
  }
  
  private SAXParseException error(String msg)
    throws SAXException
  {
    SAXParseException spe = new SAXParseException(msg, this.locator);
    getErrorHandler().error(spe);
    return spe;
  }
  
  private void warning(String msg)
    throws SAXException
  {
    SAXParseException spe = new SAXParseException(msg, this.locator);
    getErrorHandler().warning(spe);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\ExtensionBindingChecker.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */