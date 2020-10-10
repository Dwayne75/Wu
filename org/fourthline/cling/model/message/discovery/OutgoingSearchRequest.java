package org.fourthline.cling.model.message.discovery;

import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.message.OutgoingDatagramMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.UpnpRequest.Method;
import org.fourthline.cling.model.message.header.HostHeader;
import org.fourthline.cling.model.message.header.MANHeader;
import org.fourthline.cling.model.message.header.MXHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.fourthline.cling.model.types.NotificationSubtype;

public class OutgoingSearchRequest
  extends OutgoingDatagramMessage<UpnpRequest>
{
  private UpnpHeader searchTarget;
  
  public OutgoingSearchRequest(UpnpHeader searchTarget, int mxSeconds)
  {
    super(new UpnpRequest(UpnpRequest.Method.MSEARCH), 
    
      ModelUtil.getInetAddressByName("239.255.255.250"), 1900);
    
    this.searchTarget = searchTarget;
    
    getHeaders().add(UpnpHeader.Type.MAN, new MANHeader(NotificationSubtype.DISCOVER.getHeaderString()));
    getHeaders().add(UpnpHeader.Type.MX, new MXHeader(Integer.valueOf(mxSeconds)));
    getHeaders().add(UpnpHeader.Type.ST, searchTarget);
    getHeaders().add(UpnpHeader.Type.HOST, new HostHeader());
  }
  
  public UpnpHeader getSearchTarget()
  {
    return this.searchTarget;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\discovery\OutgoingSearchRequest.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */