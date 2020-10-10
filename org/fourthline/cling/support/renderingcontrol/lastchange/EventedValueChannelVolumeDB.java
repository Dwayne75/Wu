package org.fourthline.cling.support.renderingcontrol.lastchange;

import java.util.Map.Entry;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytesDatatype;
import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.shared.AbstractMap.SimpleEntry;

public class EventedValueChannelVolumeDB
  extends EventedValue<ChannelVolumeDB>
{
  public EventedValueChannelVolumeDB(ChannelVolumeDB value)
  {
    super(value);
  }
  
  public EventedValueChannelVolumeDB(Map.Entry<String, String>[] attributes)
  {
    super(attributes);
  }
  
  protected ChannelVolumeDB valueOf(Map.Entry<String, String>[] attributes)
    throws InvalidValueException
  {
    Channel channel = null;
    Integer volumeDB = null;
    for (Map.Entry<String, String> attribute : attributes)
    {
      if (((String)attribute.getKey()).equals("channel")) {
        channel = Channel.valueOf((String)attribute.getValue());
      }
      if (((String)attribute.getKey()).equals("val")) {
        volumeDB = Integer.valueOf(new UnsignedIntegerTwoBytesDatatype()
          .valueOf((String)attribute.getValue())
          .getValue().intValue());
      }
    }
    return (channel != null) && (volumeDB != null) ? new ChannelVolumeDB(channel, volumeDB) : null;
  }
  
  public Map.Entry<String, String>[] getAttributes()
  {
    return new Map.Entry[] { new AbstractMap.SimpleEntry("val", new UnsignedIntegerTwoBytesDatatype().getString(new UnsignedIntegerTwoBytes(((ChannelVolumeDB)getValue()).getVolumeDB().intValue()))), new AbstractMap.SimpleEntry("channel", ((ChannelVolumeDB)getValue()).getChannel().name()) };
  }
  
  public String toString()
  {
    return ((ChannelVolumeDB)getValue()).toString();
  }
  
  protected Datatype getDatatype()
  {
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\renderingcontrol\lastchange\EventedValueChannelVolumeDB.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */