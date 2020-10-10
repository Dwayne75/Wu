package org.fourthline.cling.support.model.dlna.message.header;

import org.fourthline.cling.model.ServiceReference;
import org.fourthline.cling.model.message.header.InvalidHeaderException;

public class PeerManagerHeader
  extends DLNAHeader<ServiceReference>
{
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (s.length() != 0) {
      try
      {
        ServiceReference serviceReference = new ServiceReference(s);
        if ((serviceReference.getUdn() != null) && (serviceReference.getServiceId() != null))
        {
          setValue(serviceReference);
          return;
        }
      }
      catch (Exception localException) {}
    }
    throw new InvalidHeaderException("Invalid PeerManager header value: " + s);
  }
  
  public String getString()
  {
    return ((ServiceReference)getValue()).toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\message\header\PeerManagerHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */