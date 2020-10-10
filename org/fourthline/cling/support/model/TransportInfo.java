package org.fourthline.cling.support.model;

import java.util.Map;
import org.fourthline.cling.model.action.ActionArgumentValue;

public class TransportInfo
{
  private TransportState currentTransportState = TransportState.NO_MEDIA_PRESENT;
  private TransportStatus currentTransportStatus = TransportStatus.OK;
  private String currentSpeed = "1";
  
  public TransportInfo() {}
  
  public TransportInfo(Map<String, ActionArgumentValue> args)
  {
    this(
      TransportState.valueOrCustomOf((String)((ActionArgumentValue)args.get("CurrentTransportState")).getValue()), 
      TransportStatus.valueOrCustomOf((String)((ActionArgumentValue)args.get("CurrentTransportStatus")).getValue()), 
      (String)((ActionArgumentValue)args.get("CurrentSpeed")).getValue());
  }
  
  public TransportInfo(TransportState currentTransportState)
  {
    this.currentTransportState = currentTransportState;
  }
  
  public TransportInfo(TransportState currentTransportState, String currentSpeed)
  {
    this.currentTransportState = currentTransportState;
    this.currentSpeed = currentSpeed;
  }
  
  public TransportInfo(TransportState currentTransportState, TransportStatus currentTransportStatus)
  {
    this.currentTransportState = currentTransportState;
    this.currentTransportStatus = currentTransportStatus;
  }
  
  public TransportInfo(TransportState currentTransportState, TransportStatus currentTransportStatus, String currentSpeed)
  {
    this.currentTransportState = currentTransportState;
    this.currentTransportStatus = currentTransportStatus;
    this.currentSpeed = currentSpeed;
  }
  
  public TransportState getCurrentTransportState()
  {
    return this.currentTransportState;
  }
  
  public TransportStatus getCurrentTransportStatus()
  {
    return this.currentTransportStatus;
  }
  
  public String getCurrentSpeed()
  {
    return this.currentSpeed;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\TransportInfo.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */