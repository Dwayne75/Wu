package org.fourthline.cling.support.renderingcontrol.lastchange;

import java.util.Set;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.lastchange.LastChangeParser;

public class RenderingControlLastChangeParser
  extends LastChangeParser
{
  public static final String NAMESPACE_URI = "urn:schemas-upnp-org:metadata-1-0/RCS/";
  public static final String SCHEMA_RESOURCE = "org/fourthline/cling/support/renderingcontrol/metadata-1.0-rcs.xsd";
  
  protected String getNamespace()
  {
    return "urn:schemas-upnp-org:metadata-1-0/RCS/";
  }
  
  protected Source[] getSchemaSources()
  {
    if (!ModelUtil.ANDROID_RUNTIME) {
      return new Source[] { new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/fourthline/cling/support/renderingcontrol/metadata-1.0-rcs.xsd")) };
    }
    return null;
  }
  
  protected Set<Class<? extends EventedValue>> getEventedVariables()
  {
    return RenderingControlVariable.ALL;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\renderingcontrol\lastchange\RenderingControlLastChangeParser.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */