package org.fourthline.cling.model.message;

public abstract class UpnpOperation
{
  private int httpMinorVersion = 1;
  
  public int getHttpMinorVersion()
  {
    return this.httpMinorVersion;
  }
  
  public void setHttpMinorVersion(int httpMinorVersion)
  {
    this.httpMinorVersion = httpMinorVersion;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\UpnpOperation.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */