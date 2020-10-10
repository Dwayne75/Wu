package org.fourthline.cling.support.model.dlna;

public enum DLNAOperations
{
  NONE(0),  RANGE(1),  TIMESEEK(16);
  
  private int code;
  
  private DLNAOperations(int code)
  {
    this.code = code;
  }
  
  public int getCode()
  {
    return this.code;
  }
  
  public static DLNAOperations valueOf(int code)
  {
    for (DLNAOperations errorCode : ) {
      if (errorCode.getCode() == code) {
        return errorCode;
      }
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\DLNAOperations.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */