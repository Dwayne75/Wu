package org.fourthline.cling.model.types;

public enum NotificationSubtype
{
  ALIVE("ssdp:alive"),  UPDATE("ssdp:update"),  BYEBYE("ssdp:byebye"),  ALL("ssdp:all"),  DISCOVER("ssdp:discover"),  PROPCHANGE("upnp:propchange");
  
  private String headerString;
  
  private NotificationSubtype(String headerString)
  {
    this.headerString = headerString;
  }
  
  public String getHeaderString()
  {
    return this.headerString;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\NotificationSubtype.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */