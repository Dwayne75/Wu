package org.fourthline.cling.support.renderingcontrol.lastchange;

import org.fourthline.cling.support.model.Channel;

public class ChannelVolume
{
  protected Channel channel;
  protected Integer volume;
  
  public ChannelVolume(Channel channel, Integer volume)
  {
    this.channel = channel;
    this.volume = volume;
  }
  
  public Channel getChannel()
  {
    return this.channel;
  }
  
  public Integer getVolume()
  {
    return this.volume;
  }
  
  public String toString()
  {
    return "Volume: " + getVolume() + " (" + getChannel() + ")";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\renderingcontrol\lastchange\ChannelVolume.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */