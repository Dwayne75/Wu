package com.sun.tools.xjc.reader.dtd.bindinfo;

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.reader.AbstractExtensionBindingChecker;
import java.util.Set;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

final class DTDExtensionBindingChecker
  extends AbstractExtensionBindingChecker
{
  public DTDExtensionBindingChecker(String schemaLanguage, Options options, ErrorHandler handler)
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
    if (uri.equals("http://java.sun.com/xml/ns/jaxb/xjc")) {
      return false;
    }
    return this.enabledExtensions.contains(uri);
  }
  
  public void startElement(String uri, String localName, String qName, Attributes atts)
    throws SAXException
  {
    if ((!isCutting()) && 
      (!uri.equals("")))
    {
      checkAndEnable(uri);
      
      verifyTagName(uri, localName, qName);
      if (needsToBePruned(uri)) {
        startCutting();
      }
    }
    super.startElement(uri, localName, qName, atts);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\dtd\bindinfo\DTDExtensionBindingChecker.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */