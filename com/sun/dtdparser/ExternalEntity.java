package com.sun.dtdparser;

import java.io.IOException;
import java.net.URL;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

final class ExternalEntity
  extends EntityDecl
{
  String systemId;
  String publicId;
  String notation;
  
  public ExternalEntity(InputEntity in) {}
  
  public InputSource getInputSource(EntityResolver r)
    throws IOException, SAXException
  {
    InputSource retval = r.resolveEntity(this.publicId, this.systemId);
    if (retval == null) {
      retval = Resolver.createInputSource(new URL(this.systemId), false);
    }
    return retval;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\dtdparser\ExternalEntity.class
 * Java compiler version: 3 (47.0)
 * JD-Core Version:       0.7.1
 */