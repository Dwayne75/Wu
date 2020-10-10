package com.sun.tools.xjc.reader.xmlschema.parser;

import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import java.io.IOException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

class XMLEntityResolverImpl
  implements XMLEntityResolver
{
  private final EntityResolver entityResolver;
  
  XMLEntityResolverImpl(EntityResolver er)
  {
    if (er == null) {
      throw new NullPointerException();
    }
    this.entityResolver = er;
  }
  
  public XMLInputSource resolveEntity(XMLResourceIdentifier r)
    throws XNIException, IOException
  {
    String publicId = r.getPublicId();
    String systemId = r.getExpandedSystemId();
    if (publicId == null) {
      publicId = r.getNamespace();
    }
    try
    {
      InputSource is = this.entityResolver.resolveEntity(publicId, systemId);
      if (is == null) {
        return null;
      }
      XMLInputSource xis = new XMLInputSource(is.getPublicId(), is.getSystemId(), r.getBaseSystemId(), is.getByteStream(), is.getEncoding());
      
      xis.setCharacterStream(is.getCharacterStream());
      return xis;
    }
    catch (SAXException e)
    {
      throw new XNIException(e);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\parser\XMLEntityResolverImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */