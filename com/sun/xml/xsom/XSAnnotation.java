package com.sun.xml.xsom;

import org.xml.sax.Locator;

public abstract interface XSAnnotation
{
  public abstract Object getAnnotation();
  
  public abstract Object setAnnotation(Object paramObject);
  
  public abstract Locator getLocator();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\XSAnnotation.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */