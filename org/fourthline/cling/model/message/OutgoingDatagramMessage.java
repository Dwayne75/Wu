package org.fourthline.cling.model.message;

import java.net.InetAddress;

public abstract class OutgoingDatagramMessage<O extends UpnpOperation>
  extends UpnpMessage<O>
{
  private InetAddress destinationAddress;
  private int destinationPort;
  private UpnpHeaders headers = new UpnpHeaders(false);
  
  protected OutgoingDatagramMessage(O operation, InetAddress destinationAddress, int destinationPort)
  {
    super(operation);
    this.destinationAddress = destinationAddress;
    this.destinationPort = destinationPort;
  }
  
  protected OutgoingDatagramMessage(O operation, UpnpMessage.BodyType bodyType, Object body, InetAddress destinationAddress, int destinationPort)
  {
    super(operation, bodyType, body);
    this.destinationAddress = destinationAddress;
    this.destinationPort = destinationPort;
  }
  
  public InetAddress getDestinationAddress()
  {
    return this.destinationAddress;
  }
  
  public int getDestinationPort()
  {
    return this.destinationPort;
  }
  
  public UpnpHeaders getHeaders()
  {
    return this.headers;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\OutgoingDatagramMessage.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */