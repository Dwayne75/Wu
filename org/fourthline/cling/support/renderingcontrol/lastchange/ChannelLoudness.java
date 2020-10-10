package org.fourthline.cling.support.renderingcontrol.lastchange;

import org.fourthline.cling.support.model.Channel;

public class ChannelLoudness
{
  protected Channel channel;
  protected Boolean loudness;
  
  public ChannelLoudness(Channel channel, Boolean loudness)
  {
    this.channel = channel;
    this.loudness = loudness;
  }
  
  public Channel getChannel()
  {
    return this.channel;
  }
  
  public Boolean getLoudness()
  {
    return this.loudness;
  }
  
  public String toString()
  {
    return "Loudness: " + getLoudness() + " (" + getChannel() + ")";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\renderingcontrol\lastchange\ChannelLoudness.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */