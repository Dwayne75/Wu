package org.fourthline.cling.support.model.dlna;

public enum DLNAConversionIndicator
{
  NONE(0),  TRANSCODED(1);
  
  private int code;
  
  private DLNAConversionIndicator(int code)
  {
    this.code = code;
  }
  
  public int getCode()
  {
    return this.code;
  }
  
  public static DLNAConversionIndicator valueOf(int code)
  {
    for (DLNAConversionIndicator errorCode : ) {
      if (errorCode.getCode() == code) {
        return errorCode;
      }
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\DLNAConversionIndicator.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */