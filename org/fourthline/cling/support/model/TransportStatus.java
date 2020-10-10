package org.fourthline.cling.support.model;

public enum TransportStatus
{
  OK,  ERROR_OCCURRED,  CUSTOM;
  
  String value;
  
  private TransportStatus()
  {
    this.value = name();
  }
  
  public String getValue()
  {
    return this.value;
  }
  
  public TransportStatus setValue(String value)
  {
    this.value = value;
    return this;
  }
  
  public static TransportStatus valueOrCustomOf(String s)
  {
    try
    {
      return valueOf(s);
    }
    catch (IllegalArgumentException ex) {}
    return CUSTOM.setValue(s);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\TransportStatus.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */