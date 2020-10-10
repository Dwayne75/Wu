package com.sun.xml.bind.v2.schemagen;

import com.sun.xml.bind.Util;
import java.io.IOException;
import java.util.logging.Logger;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;

final class FoolProofResolver
  extends SchemaOutputResolver
{
  private static final Logger logger = Util.getClassLogger();
  private final SchemaOutputResolver resolver;
  
  public FoolProofResolver(SchemaOutputResolver resolver)
  {
    assert (resolver != null);
    this.resolver = resolver;
  }
  
  public Result createOutput(String namespaceUri, String suggestedFileName)
    throws IOException
  {
    logger.entering(getClass().getName(), "createOutput", new Object[] { namespaceUri, suggestedFileName });
    Result r = this.resolver.createOutput(namespaceUri, suggestedFileName);
    if (r != null)
    {
      String sysId = r.getSystemId();
      logger.finer("system ID = " + sysId);
      if (sysId == null) {
        throw new AssertionError("system ID cannot be null");
      }
    }
    logger.exiting(getClass().getName(), "createOutput", r);
    return r;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\FoolProofResolver.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */