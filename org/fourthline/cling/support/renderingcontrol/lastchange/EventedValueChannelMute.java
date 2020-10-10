package org.fourthline.cling.support.renderingcontrol.lastchange;

import java.util.Map.Entry;
import org.fourthline.cling.model.types.BooleanDatatype;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.shared.AbstractMap.SimpleEntry;

public class EventedValueChannelMute
  extends EventedValue<ChannelMute>
{
  public EventedValueChannelMute(ChannelMute value)
  {
    super(value);
  }
  
  public EventedValueChannelMute(Map.Entry<String, String>[] attributes)
  {
    super(attributes);
  }
  
  protected ChannelMute valueOf(Map.Entry<String, String>[] attributes)
    throws InvalidValueException
  {
    Channel channel = null;
    Boolean mute = null;
    for (Map.Entry<String, String> attribute : attributes)
    {
      if (((String)attribute.getKey()).equals("channel")) {
        channel = Channel.valueOf((String)attribute.getValue());
      }
      if (((String)attribute.getKey()).equals("val")) {
        mute = new BooleanDatatype().valueOf((String)attribute.getValue());
      }
    }
    return (channel != null) && (mute != null) ? new ChannelMute(channel, mute) : null;
  }
  
  public Map.Entry<String, String>[] getAttributes()
  {
    return new Map.Entry[] { new AbstractMap.SimpleEntry("val", new BooleanDatatype().getString(((ChannelMute)getValue()).getMute())), new AbstractMap.SimpleEntry("channel", ((ChannelMute)getValue()).getChannel().name()) };
  }
  
  public String toString()
  {
    return ((ChannelMute)getValue()).toString();
  }
  
  protected Datatype getDatatype()
  {
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\renderingcontrol\lastchange\EventedValueChannelMute.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */