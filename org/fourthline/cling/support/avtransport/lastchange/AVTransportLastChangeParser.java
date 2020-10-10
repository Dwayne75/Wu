package org.fourthline.cling.support.avtransport.lastchange;

import java.util.Set;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.lastchange.LastChangeParser;

public class AVTransportLastChangeParser
  extends LastChangeParser
{
  public static final String NAMESPACE_URI = "urn:schemas-upnp-org:metadata-1-0/AVT/";
  public static final String SCHEMA_RESOURCE = "org/fourthline/cling/support/avtransport/metadata-1.01-avt.xsd";
  
  protected String getNamespace()
  {
    return "urn:schemas-upnp-org:metadata-1-0/AVT/";
  }
  
  protected Source[] getSchemaSources()
  {
    if (!ModelUtil.ANDROID_RUNTIME) {
      return new Source[] { new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/fourthline/cling/support/avtransport/metadata-1.01-avt.xsd")) };
    }
    return null;
  }
  
  protected Set<Class<? extends EventedValue>> getEventedVariables()
  {
    return AVTransportVariable.ALL;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\avtransport\lastchange\AVTransportLastChangeParser.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */