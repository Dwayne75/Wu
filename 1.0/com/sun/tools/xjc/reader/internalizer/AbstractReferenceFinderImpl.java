package com.sun.tools.xjc.reader.internalizer;

import java.io.IOException;
import java.net.URL;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.XMLFilterImpl;

public abstract class AbstractReferenceFinderImpl
  extends XMLFilterImpl
{
  protected final DOMForest parent;
  private Locator locator;
  
  protected AbstractReferenceFinderImpl(DOMForest _parent)
  {
    this.parent = _parent;
  }
  
  protected abstract String findExternalResource(String paramString1, String paramString2, Attributes paramAttributes);
  
  public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
    throws SAXException
  {
    super.startElement(namespaceURI, localName, qName, atts);
    
    String relativeRef = findExternalResource(namespaceURI, localName, atts);
    if (relativeRef == null) {
      return;
    }
    try
    {
      String ref = new URL(new URL(this.locator.getSystemId()), relativeRef).toExternalForm();
      
      this.parent.parse(ref);
    }
    catch (IOException e)
    {
      SAXParseException spe = new SAXParseException(Messages.format("AbstractReferenceFinderImpl.UnableToParse", relativeRef), this.locator, e);
      
      fatalError(spe);
      throw spe;
    }
  }
  
  public void setDocumentLocator(Locator locator)
  {
    super.setDocumentLocator(locator);
    this.locator = locator;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\internalizer\AbstractReferenceFinderImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */