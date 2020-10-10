package com.sun.xml.bind;

import java.util.concurrent.Callable;
import javax.xml.bind.ValidationEventHandler;
import org.xml.sax.SAXException;

public abstract class IDResolver
{
  public void startDocument(ValidationEventHandler eventHandler)
    throws SAXException
  {}
  
  public void endDocument()
    throws SAXException
  {}
  
  public abstract void bind(String paramString, Object paramObject)
    throws SAXException;
  
  public abstract Callable<?> resolve(String paramString, Class paramClass)
    throws SAXException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\IDResolver.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */