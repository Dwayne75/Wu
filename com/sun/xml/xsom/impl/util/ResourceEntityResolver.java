package com.sun.xml.xsom.impl.util;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class ResourceEntityResolver
  implements EntityResolver
{
  private final Class base;
  
  public ResourceEntityResolver(Class _base)
  {
    this.base = _base;
  }
  
  public InputSource resolveEntity(String publicId, String systemId)
  {
    return new InputSource(this.base.getResourceAsStream(systemId));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\util\ResourceEntityResolver.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */