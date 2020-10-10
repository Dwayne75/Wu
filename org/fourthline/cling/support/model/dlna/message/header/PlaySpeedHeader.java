package org.fourthline.cling.support.model.dlna.message.header;

import org.fourthline.cling.model.message.header.InvalidHeaderException;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.TransportPlaySpeed;

public class PlaySpeedHeader
  extends DLNAHeader<AVTransportVariable.TransportPlaySpeed>
{
  public PlaySpeedHeader() {}
  
  public PlaySpeedHeader(AVTransportVariable.TransportPlaySpeed speed)
  {
    setValue(speed);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (s.length() != 0) {
      try
      {
        AVTransportVariable.TransportPlaySpeed t = new AVTransportVariable.TransportPlaySpeed(s);
        setValue(t);
        return;
      }
      catch (InvalidValueException localInvalidValueException) {}
    }
    throw new InvalidHeaderException("Invalid PlaySpeed header value: " + s);
  }
  
  public String getString()
  {
    return (String)((AVTransportVariable.TransportPlaySpeed)getValue()).getValue();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\message\header\PlaySpeedHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */