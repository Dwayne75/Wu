package org.fourthline.cling.support.model.dlna;

public enum DLNAFlags
{
  SENDER_PACED(Integer.MIN_VALUE),  TIME_BASED_SEEK(1073741824),  BYTE_BASED_SEEK(536870912),  FLAG_PLAY_CONTAINER(268435456),  S0_INCREASE(134217728),  SN_INCREASE(67108864),  RTSP_PAUSE(33554432),  STREAMING_TRANSFER_MODE(16777216),  INTERACTIVE_TRANSFERT_MODE(8388608),  BACKGROUND_TRANSFERT_MODE(4194304),  CONNECTION_STALL(2097152),  DLNA_V15(1048576);
  
  private int code;
  
  private DLNAFlags(int code)
  {
    this.code = code;
  }
  
  public int getCode()
  {
    return this.code;
  }
  
  public static DLNAFlags valueOf(int code)
  {
    for (DLNAFlags errorCode : ) {
      if (errorCode.getCode() == code) {
        return errorCode;
      }
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\DLNAFlags.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */