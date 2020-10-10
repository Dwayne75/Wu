package com.sun.tools.xjc.api;

import java.io.IOException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;

public abstract interface J2SJAXBModel
  extends JAXBModel
{
  public abstract QName getXmlTypeName(Reference paramReference);
  
  public abstract void generateSchema(SchemaOutputResolver paramSchemaOutputResolver, ErrorListener paramErrorListener)
    throws IOException;
  
  public abstract void generateEpisodeFile(Result paramResult);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\J2SJAXBModel.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */