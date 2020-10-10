package org.fourthline.cling.model.profile;

import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.fourthline.cling.model.message.header.UserAgentHeader;

public class ClientInfo
{
  protected final UpnpHeaders requestHeaders;
  
  public ClientInfo()
  {
    this(new UpnpHeaders());
  }
  
  public ClientInfo(UpnpHeaders requestHeaders)
  {
    this.requestHeaders = requestHeaders;
  }
  
  public UpnpHeaders getRequestHeaders()
  {
    return this.requestHeaders;
  }
  
  public String getRequestUserAgent()
  {
    return getRequestHeaders().getFirstHeaderString(UpnpHeader.Type.USER_AGENT);
  }
  
  public void setRequestUserAgent(String userAgent)
  {
    getRequestHeaders().add(UpnpHeader.Type.USER_AGENT, new UserAgentHeader(userAgent));
  }
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ") User-Agent: " + getRequestUserAgent();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\profile\ClientInfo.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */