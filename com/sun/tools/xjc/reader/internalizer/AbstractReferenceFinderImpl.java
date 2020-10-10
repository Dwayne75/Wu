package com.sun.tools.xjc.reader.internalizer;

import com.sun.istack.SAXParseException2;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
      String ref = new URI(this.locator.getSystemId()).resolve(new URI(relativeRef)).toString();
      
      this.parent.parse(ref, false);
    }
    catch (URISyntaxException e)
    {
      String msg = e.getMessage();
      if (new File(relativeRef).exists()) {
        msg = Messages.format("ERR_FILENAME_IS_NOT_URI", new Object[0]) + ' ' + msg;
      }
      SAXParseException spe = new SAXParseException2(Messages.format("AbstractReferenceFinderImpl.UnableToParse", new Object[] { relativeRef, msg }), this.locator, e);
      
      fatalError(spe);
      throw spe;
    }
    catch (IOException e)
    {
      SAXParseException spe = new SAXParseException2(Messages.format("AbstractReferenceFinderImpl.UnableToParse", new Object[] { relativeRef, e.getMessage() }), this.locator, e);
      
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\internalizer\AbstractReferenceFinderImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */