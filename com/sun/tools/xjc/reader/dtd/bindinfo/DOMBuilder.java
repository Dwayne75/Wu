package com.sun.tools.xjc.reader.dtd.bindinfo;

import com.sun.xml.bind.marshaller.SAX2DOMEx;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

final class DOMBuilder
  extends SAX2DOMEx
{
  private Locator locator;
  
  public DOMBuilder()
    throws ParserConfigurationException
  {}
  
  public void setDocumentLocator(Locator locator)
  {
    super.setDocumentLocator(locator);
    this.locator = locator;
  }
  
  public void startElement(String namespace, String localName, String qName, Attributes attrs)
  {
    super.startElement(namespace, localName, qName, attrs);
    DOMLocator.setLocationInfo(getCurrentElement(), this.locator);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\dtd\bindinfo\DOMBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */