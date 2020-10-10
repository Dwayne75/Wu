package org.fourthline.cling.model.message;

import java.net.InetAddress;

public class IncomingDatagramMessage<O extends UpnpOperation>
  extends UpnpMessage<O>
{
  private InetAddress sourceAddress;
  private int sourcePort;
  private InetAddress localAddress;
  
  public IncomingDatagramMessage(O operation, InetAddress sourceAddress, int sourcePort, InetAddress localAddress)
  {
    super(operation);
    this.sourceAddress = sourceAddress;
    this.sourcePort = sourcePort;
    this.localAddress = localAddress;
  }
  
  protected IncomingDatagramMessage(IncomingDatagramMessage<O> source)
  {
    super(source);
    this.sourceAddress = source.getSourceAddress();
    this.sourcePort = source.getSourcePort();
    this.localAddress = source.getLocalAddress();
  }
  
  public InetAddress getSourceAddress()
  {
    return this.sourceAddress;
  }
  
  public int getSourcePort()
  {
    return this.sourcePort;
  }
  
  public InetAddress getLocalAddress()
  {
    return this.localAddress;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\IncomingDatagramMessage.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */