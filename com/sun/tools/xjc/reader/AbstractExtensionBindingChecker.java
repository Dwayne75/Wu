package com.sun.tools.xjc.reader;

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.util.SubtreeCutter;
import com.sun.xml.bind.v2.util.EditDistance;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.NamespaceSupport;

public abstract class AbstractExtensionBindingChecker
  extends SubtreeCutter
{
  protected final NamespaceSupport nsSupport = new NamespaceSupport();
  protected final Set<String> enabledExtensions = new HashSet();
  private final Set<String> recognizableExtensions = new HashSet();
  private Locator locator;
  protected final String schemaLanguage;
  protected final boolean allowExtensions;
  private final Options options;
  
  public AbstractExtensionBindingChecker(String schemaLanguage, Options options, ErrorHandler handler)
  {
    this.schemaLanguage = schemaLanguage;
    this.allowExtensions = (options.compatibilityMode != 1);
    this.options = options;
    setErrorHandler(handler);
    for (Plugin plugin : options.getAllPlugins()) {
      this.recognizableExtensions.addAll(plugin.getCustomizationURIs());
    }
    this.recognizableExtensions.add("http://java.sun.com/xml/ns/jaxb/xjc");
  }
  
  protected final void checkAndEnable(String uri)
    throws SAXException
  {
    if (!isRecognizableExtension(uri))
    {
      String nearest = EditDistance.findNearest(uri, this.recognizableExtensions);
      
      error(Messages.ERR_UNSUPPORTED_EXTENSION.format(new Object[] { uri, nearest }));
    }
    else if (!isSupportedExtension(uri))
    {
      Plugin owner = null;
      for (Plugin p : this.options.getAllPlugins()) {
        if (p.getCustomizationURIs().contains(uri))
        {
          owner = p;
          break;
        }
      }
      if (owner != null) {
        error(Messages.ERR_PLUGIN_NOT_ENABLED.format(new Object[] { owner.getOptionName(), uri }));
      } else {
        error(Messages.ERR_UNSUPPORTED_EXTENSION.format(new Object[] { uri }));
      }
    }
    this.enabledExtensions.add(uri);
  }
  
  protected final void verifyTagName(String namespaceURI, String localName, String qName)
    throws SAXException
  {
    if (this.options.pluginURIs.contains(namespaceURI))
    {
      boolean correct = false;
      for (Plugin p : this.options.activePlugins) {
        if (p.isCustomizationTagName(namespaceURI, localName))
        {
          correct = true;
          break;
        }
      }
      if (!correct)
      {
        error(Messages.ERR_ILLEGAL_CUSTOMIZATION_TAGNAME.format(new Object[] { qName }));
        startCutting();
      }
    }
  }
  
  protected final boolean isSupportedExtension(String namespaceUri)
  {
    return (namespaceUri.equals("http://java.sun.com/xml/ns/jaxb/xjc")) || (this.options.pluginURIs.contains(namespaceUri));
  }
  
  protected final boolean isRecognizableExtension(String namespaceUri)
  {
    return this.recognizableExtensions.contains(namespaceUri);
  }
  
  public void setDocumentLocator(Locator locator)
  {
    super.setDocumentLocator(locator);
    this.locator = locator;
  }
  
  public void startDocument()
    throws SAXException
  {
    super.startDocument();
    
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
  
  protected final SAXParseException error(String msg)
    throws SAXException
  {
    SAXParseException spe = new SAXParseException(msg, this.locator);
    getErrorHandler().error(spe);
    return spe;
  }
  
  protected final void warning(String msg)
    throws SAXException
  {
    SAXParseException spe = new SAXParseException(msg, this.locator);
    getErrorHandler().warning(spe);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\AbstractExtensionBindingChecker.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */