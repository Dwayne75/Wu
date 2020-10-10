package org.fourthline.cling.support.renderingcontrol.lastchange;

import org.fourthline.cling.support.model.Channel;

public class ChannelVolumeDB
{
  protected Channel channel;
  protected Integer volumeDB;
  
  public ChannelVolumeDB(Channel channel, Integer volumeDB)
  {
    this.channel = channel;
    this.volumeDB = volumeDB;
  }
  
  public Channel getChannel()
  {
    return this.channel;
  }
  
  public Integer getVolumeDB()
  {
    return this.volumeDB;
  }
  
  public String toString()
  {
    return "VolumeDB: " + getVolumeDB() + " (" + getChannel() + ")";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\renderingcontrol\lastchange\ChannelVolumeDB.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */