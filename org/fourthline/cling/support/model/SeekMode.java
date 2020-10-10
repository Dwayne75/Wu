package org.fourthline.cling.support.model;

public enum SeekMode
{
  TRACK_NR("TRACK_NR"),  ABS_TIME("ABS_TIME"),  REL_TIME("REL_TIME"),  ABS_COUNT("ABS_COUNT"),  REL_COUNT("REL_COUNT"),  CHANNEL_FREQ("CHANNEL_FREQ"),  TAPE_INDEX("TAPE-INDEX"),  FRAME("FRAME");
  
  private String protocolString;
  
  private SeekMode(String protocolString)
  {
    this.protocolString = protocolString;
  }
  
  public String toString()
  {
    return this.protocolString;
  }
  
  public static SeekMode valueOrExceptionOf(String s)
    throws IllegalArgumentException
  {
    for (SeekMode seekMode : ) {
      if (seekMode.protocolString.equals(s)) {
        return seekMode;
      }
    }
    throw new IllegalArgumentException("Invalid seek mode string: " + s);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\SeekMode.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */