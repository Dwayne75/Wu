package com.sun.xml.bind.unmarshaller;

import com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public abstract interface InfosetScanner<XmlNode>
{
  public abstract void scan(XmlNode paramXmlNode)
    throws SAXException;
  
  public abstract void setContentHandler(ContentHandler paramContentHandler);
  
  public abstract ContentHandler getContentHandler();
  
  public abstract XmlNode getCurrentElement();
  
  public abstract LocatorEx getLocator();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\unmarshaller\InfosetScanner.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */