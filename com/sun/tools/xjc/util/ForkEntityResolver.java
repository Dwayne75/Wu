package com.sun.tools.xjc.util;

import java.io.IOException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ForkEntityResolver
  implements EntityResolver
{
  private final EntityResolver lhs;
  private final EntityResolver rhs;
  
  public ForkEntityResolver(EntityResolver lhs, EntityResolver rhs)
  {
    this.lhs = lhs;
    this.rhs = rhs;
  }
  
  public InputSource resolveEntity(String publicId, String systemId)
    throws SAXException, IOException
  {
    InputSource is = this.lhs.resolveEntity(publicId, systemId);
    if (is != null) {
      return is;
    }
    return this.rhs.resolveEntity(publicId, systemId);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\util\ForkEntityResolver.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */