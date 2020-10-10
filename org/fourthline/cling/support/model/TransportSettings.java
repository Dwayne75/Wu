package org.fourthline.cling.support.model;

public class TransportSettings
{
  private PlayMode playMode = PlayMode.NORMAL;
  private RecordQualityMode recQualityMode = RecordQualityMode.NOT_IMPLEMENTED;
  
  public TransportSettings() {}
  
  public TransportSettings(PlayMode playMode)
  {
    this.playMode = playMode;
  }
  
  public TransportSettings(PlayMode playMode, RecordQualityMode recQualityMode)
  {
    this.playMode = playMode;
    this.recQualityMode = recQualityMode;
  }
  
  public PlayMode getPlayMode()
  {
    return this.playMode;
  }
  
  public RecordQualityMode getRecQualityMode()
  {
    return this.recQualityMode;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\TransportSettings.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */