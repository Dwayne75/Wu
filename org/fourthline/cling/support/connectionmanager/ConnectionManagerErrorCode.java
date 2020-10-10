package org.fourthline.cling.support.connectionmanager;

public enum ConnectionManagerErrorCode
{
  INCOMPATIBLE_PROTOCOL_INFO(701, "The connection cannot be established because the protocol info parameter is incompatible"),  INCOMPATIBLE_DIRECTIONS(702, "The connection cannot be established because the directions of the involved ConnectionManagers (source/sink) are incompatible"),  INSUFFICIENT_NETWORK_RESOURCES(703, "The connection cannot be established because there are insufficient network resources"),  LOCAL_RESTRICTIONS(704, "The connection cannot be established because of local restrictions in the device"),  ACCESS_DENIED(705, "The connection cannot be established because the client is not permitted."),  INVALID_CONNECTION_REFERENCE(706, "Not a valid connection established by this service"),  NOT_IN_NETWORK(707, "The connection cannot be established because the ConnectionManagers are not part of the same physical network.");
  
  private int code;
  private String description;
  
  private ConnectionManagerErrorCode(int code, String description)
  {
    this.code = code;
    this.description = description;
  }
  
  public int getCode()
  {
    return this.code;
  }
  
  public String getDescription()
  {
    return this.description;
  }
  
  public static ConnectionManagerErrorCode getByCode(int code)
  {
    for (ConnectionManagerErrorCode errorCode : ) {
      if (errorCode.getCode() == code) {
        return errorCode;
      }
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\connectionmanager\ConnectionManagerErrorCode.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */