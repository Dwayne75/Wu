package org.fourthline.cling.support.model;

public enum RecordMediumWriteStatus
{
  WRITABLE,  PROTECTED,  NOT_WRITABLE,  UNKNOWN,  NOT_IMPLEMENTED;
  
  private RecordMediumWriteStatus() {}
  
  public static RecordMediumWriteStatus valueOrUnknownOf(String s)
  {
    if (s == null) {
      return UNKNOWN;
    }
    try
    {
      return valueOf(s);
    }
    catch (IllegalArgumentException ex) {}
    return UNKNOWN;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\RecordMediumWriteStatus.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */