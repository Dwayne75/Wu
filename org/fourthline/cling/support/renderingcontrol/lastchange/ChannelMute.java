package org.fourthline.cling.support.renderingcontrol.lastchange;

import org.fourthline.cling.support.model.Channel;

public class ChannelMute
{
  protected Channel channel;
  protected Boolean mute;
  
  public ChannelMute(Channel channel, Boolean mute)
  {
    this.channel = channel;
    this.mute = mute;
  }
  
  public Channel getChannel()
  {
    return this.channel;
  }
  
  public Boolean getMute()
  {
    return this.mute;
  }
  
  public String toString()
  {
    return "Mute: " + getMute() + " (" + getChannel() + ")";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\renderingcontrol\lastchange\ChannelMute.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */