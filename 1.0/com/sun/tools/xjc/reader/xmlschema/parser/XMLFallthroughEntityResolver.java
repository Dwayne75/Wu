package com.sun.tools.xjc.reader.xmlschema.parser;

import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import java.io.IOException;

public class XMLFallthroughEntityResolver
  implements XMLEntityResolver
{
  private final XMLEntityResolver first;
  private final XMLEntityResolver second;
  
  public XMLFallthroughEntityResolver(XMLEntityResolver _first, XMLEntityResolver _second)
  {
    this.first = _first;
    this.second = _second;
  }
  
  public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
    throws XNIException, IOException
  {
    XMLInputSource xis = this.first.resolveEntity(resourceIdentifier);
    if (xis != null) {
      return xis;
    }
    return this.second.resolveEntity(resourceIdentifier);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\parser\XMLFallthroughEntityResolver.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */