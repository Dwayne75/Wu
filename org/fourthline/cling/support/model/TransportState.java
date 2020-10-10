package org.fourthline.cling.support.model;

public enum TransportState
{
  STOPPED,  PLAYING,  TRANSITIONING,  PAUSED_PLAYBACK,  PAUSED_RECORDING,  RECORDING,  NO_MEDIA_PRESENT,  CUSTOM;
  
  String value;
  
  private TransportState()
  {
    this.value = name();
  }
  
  public String getValue()
  {
    return this.value;
  }
  
  public TransportState setValue(String value)
  {
    this.value = value;
    return this;
  }
  
  public static TransportState valueOrCustomOf(String s)
  {
    try
    {
      return valueOf(s);
    }
    catch (IllegalArgumentException ex) {}
    return CUSTOM.setValue(s);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\TransportState.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */