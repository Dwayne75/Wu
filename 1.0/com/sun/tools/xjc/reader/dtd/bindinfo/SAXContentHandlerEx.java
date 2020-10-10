package com.sun.tools.xjc.reader.dtd.bindinfo;

import org.dom4j.DocumentFactory;
import org.dom4j.io.SAXContentHandler;
import org.xml.sax.Locator;

class SAXContentHandlerEx
  extends SAXContentHandler
{
  private final Locator[] loc;
  
  public static SAXContentHandlerEx create()
  {
    return new SAXContentHandlerEx(new Locator[1]);
  }
  
  private SAXContentHandlerEx(Locator[] loc)
  {
    super(DocumentFactory.getInstance(), new SAXContentHandlerEx.MyElementHandler(loc));
    this.loc = loc;
  }
  
  public void setDocumentLocator(Locator _loc)
  {
    this.loc[0] = _loc;
    super.setDocumentLocator(_loc);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\dtd\bindinfo\SAXContentHandlerEx.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */