package org.fourthline.cling.model.profile;

import java.net.InetAddress;
import org.fourthline.cling.model.message.Connection;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.fourthline.cling.model.message.header.UserAgentHeader;
import org.seamless.http.RequestInfo;

public class RemoteClientInfo
  extends ClientInfo
{
  protected final Connection connection;
  protected final UpnpHeaders extraResponseHeaders = new UpnpHeaders();
  
  public RemoteClientInfo()
  {
    this(null);
  }
  
  public RemoteClientInfo(StreamRequestMessage requestMessage)
  {
    this(requestMessage != null ? requestMessage.getConnection() : null, requestMessage != null ? requestMessage
      .getHeaders() : new UpnpHeaders());
  }
  
  public RemoteClientInfo(Connection connection, UpnpHeaders requestHeaders)
  {
    super(requestHeaders);
    this.connection = connection;
  }
  
  public Connection getConnection()
  {
    return this.connection;
  }
  
  public boolean isRequestCancelled()
  {
    return !getConnection().isOpen();
  }
  
  public void throwIfRequestCancelled()
    throws InterruptedException
  {
    if (isRequestCancelled()) {
      throw new InterruptedException("Client's request cancelled");
    }
  }
  
  public InetAddress getRemoteAddress()
  {
    return getConnection().getRemoteAddress();
  }
  
  public InetAddress getLocalAddress()
  {
    return getConnection().getLocalAddress();
  }
  
  public UpnpHeaders getExtraResponseHeaders()
  {
    return this.extraResponseHeaders;
  }
  
  public void setResponseUserAgent(String userAgent)
  {
    setResponseUserAgent(new UserAgentHeader(userAgent));
  }
  
  public void setResponseUserAgent(UserAgentHeader userAgentHeader)
  {
    getExtraResponseHeaders().add(UpnpHeader.Type.USER_AGENT, userAgentHeader);
  }
  
  public boolean isWMPRequest()
  {
    return RequestInfo.isWMPRequest(getRequestUserAgent());
  }
  
  public boolean isXbox360Request()
  {
    return RequestInfo.isXbox360Request(
      getRequestUserAgent(), 
      getRequestHeaders().getFirstHeaderString(UpnpHeader.Type.SERVER));
  }
  
  public boolean isPS3Request()
  {
    return RequestInfo.isPS3Request(
      getRequestUserAgent(), 
      getRequestHeaders().getFirstHeaderString(UpnpHeader.Type.EXT_AV_CLIENT_INFO));
  }
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ") Remote Address: " + getRemoteAddress();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\profile\RemoteClientInfo.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */