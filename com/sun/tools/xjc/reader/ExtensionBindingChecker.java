package com.sun.tools.xjc.reader;

import com.sun.tools.xjc.Options;
import java.util.Set;
import java.util.StringTokenizer;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.NamespaceSupport;

public final class ExtensionBindingChecker
  extends AbstractExtensionBindingChecker
{
  private int count = 0;
  
  public ExtensionBindingChecker(String schemaLanguage, Options options, ErrorHandler handler)
  {
    super(schemaLanguage, options, handler);
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
    return isRecognizableExtension(uri);
  }
  
  public void startDocument()
    throws SAXException
  {
    super.startDocument();
    this.count = 0;
  }
  
  public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
    throws SAXException
  {
    if (!isCutting())
    {
      String v = atts.getValue("http://java.sun.com/xml/ns/jaxb", "extensionBindingPrefixes");
      if (v != null)
      {
        if (this.count != 0) {
          error(Messages.ERR_UNEXPECTED_EXTENSION_BINDING_PREFIXES.format(new Object[0]));
        }
        if (!this.allowExtensions) {
          error(Messages.ERR_VENDOR_EXTENSION_DISALLOWED_IN_STRICT_MODE.format(new Object[0]));
        }
        StringTokenizer tokens = new StringTokenizer(v);
        while (tokens.hasMoreTokens())
        {
          String prefix = tokens.nextToken();
          String uri = this.nsSupport.getURI(prefix);
          if (uri == null) {
            error(Messages.ERR_UNDECLARED_PREFIX.format(new Object[] { prefix }));
          } else {
            checkAndEnable(uri);
          }
        }
      }
      if (needsToBePruned(namespaceURI))
      {
        if (isRecognizableExtension(namespaceURI)) {
          warning(Messages.ERR_SUPPORTED_EXTENSION_IGNORED.format(new Object[] { namespaceURI }));
        }
        startCutting();
      }
      else
      {
        verifyTagName(namespaceURI, localName, qName);
      }
    }
    this.count += 1;
    super.startElement(namespaceURI, localName, qName, atts);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\ExtensionBindingChecker.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */