package org.fourthline.cling.support.model.dlna;

import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.TransportPlaySpeed;

public class DLNAPlaySpeedAttribute
  extends DLNAAttribute<AVTransportVariable.TransportPlaySpeed[]>
{
  public DLNAPlaySpeedAttribute()
  {
    setValue(new AVTransportVariable.TransportPlaySpeed[0]);
  }
  
  public DLNAPlaySpeedAttribute(AVTransportVariable.TransportPlaySpeed[] speeds)
  {
    setValue(speeds);
  }
  
  public DLNAPlaySpeedAttribute(String[] speeds)
  {
    AVTransportVariable.TransportPlaySpeed[] sp = new AVTransportVariable.TransportPlaySpeed[speeds.length];
    try
    {
      for (int i = 0; i < speeds.length; i++) {
        sp[i] = new AVTransportVariable.TransportPlaySpeed(speeds[i]);
      }
    }
    catch (InvalidValueException invalidValueException)
    {
      throw new InvalidDLNAProtocolAttributeException("Can't parse DLNA play speeds.");
    }
    setValue(sp);
  }
  
  public void setString(String s, String cf)
    throws InvalidDLNAProtocolAttributeException
  {
    AVTransportVariable.TransportPlaySpeed[] value = null;
    if ((s != null) && (s.length() != 0))
    {
      String[] speeds = s.split(",");
      try
      {
        value = new AVTransportVariable.TransportPlaySpeed[speeds.length];
        for (int i = 0; i < speeds.length; i++) {
          value[i] = new AVTransportVariable.TransportPlaySpeed(speeds[i]);
        }
      }
      catch (InvalidValueException invalidValueException)
      {
        value = null;
      }
    }
    if (value == null) {
      throw new InvalidDLNAProtocolAttributeException("Can't parse DLNA play speeds from: " + s);
    }
    setValue(value);
  }
  
  public String getString()
  {
    String s = "";
    for (AVTransportVariable.TransportPlaySpeed speed : (AVTransportVariable.TransportPlaySpeed[])getValue()) {
      if (!((String)speed.getValue()).equals("1")) {
        s = s + (s.length() == 0 ? "" : ",") + speed;
      }
    }
    return s;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\DLNAPlaySpeedAttribute.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */