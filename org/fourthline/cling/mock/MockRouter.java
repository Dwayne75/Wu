package org.fourthline.cling.mock;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.enterprise.inject.Alternative;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.NetworkAddress;
import org.fourthline.cling.model.message.IncomingDatagramMessage;
import org.fourthline.cling.model.message.OutgoingDatagramMessage;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.RouterException;
import org.fourthline.cling.transport.spi.InitializationException;
import org.fourthline.cling.transport.spi.UpnpStream;

@Alternative
public class MockRouter
  implements Router
{
  public int counter = -1;
  public List<IncomingDatagramMessage> incomingDatagramMessages = new ArrayList();
  public List<OutgoingDatagramMessage> outgoingDatagramMessages = new ArrayList();
  public List<UpnpStream> receivedUpnpStreams = new ArrayList();
  public List<StreamRequestMessage> sentStreamRequestMessages = new ArrayList();
  public List<byte[]> broadcastedBytes = new ArrayList();
  protected UpnpServiceConfiguration configuration;
  protected ProtocolFactory protocolFactory;
  
  public MockRouter(UpnpServiceConfiguration configuration, ProtocolFactory protocolFactory)
  {
    this.configuration = configuration;
    this.protocolFactory = protocolFactory;
  }
  
  public UpnpServiceConfiguration getConfiguration()
  {
    return this.configuration;
  }
  
  public ProtocolFactory getProtocolFactory()
  {
    return this.protocolFactory;
  }
  
  public boolean enable()
    throws RouterException
  {
    return false;
  }
  
  public boolean disable()
    throws RouterException
  {
    return false;
  }
  
  public void shutdown()
    throws RouterException
  {}
  
  public boolean isEnabled()
    throws RouterException
  {
    return false;
  }
  
  public void handleStartFailure(InitializationException ex)
    throws InitializationException
  {}
  
  public List<NetworkAddress> getActiveStreamServers(InetAddress preferredAddress)
    throws RouterException
  {
    try
    {
      return Arrays.asList(new NetworkAddress[] { new NetworkAddress(
      
        InetAddress.getByName("127.0.0.1"), 0) });
    }
    catch (UnknownHostException ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  public void received(IncomingDatagramMessage msg)
  {
    this.incomingDatagramMessages.add(msg);
  }
  
  public void received(UpnpStream stream)
  {
    this.receivedUpnpStreams.add(stream);
  }
  
  public void send(OutgoingDatagramMessage msg)
    throws RouterException
  {
    this.outgoingDatagramMessages.add(msg);
  }
  
  public StreamResponseMessage send(StreamRequestMessage msg)
    throws RouterException
  {
    this.sentStreamRequestMessages.add(msg);
    this.counter += 1;
    
    return getStreamResponseMessages() != null ? getStreamResponseMessages()[this.counter] : getStreamResponseMessage(msg);
  }
  
  public void broadcast(byte[] bytes)
  {
    this.broadcastedBytes.add(bytes);
  }
  
  public void resetStreamRequestMessageCounter()
  {
    this.counter = -1;
  }
  
  public List<IncomingDatagramMessage> getIncomingDatagramMessages()
  {
    return this.incomingDatagramMessages;
  }
  
  public List<OutgoingDatagramMessage> getOutgoingDatagramMessages()
  {
    return this.outgoingDatagramMessages;
  }
  
  public List<UpnpStream> getReceivedUpnpStreams()
  {
    return this.receivedUpnpStreams;
  }
  
  public List<StreamRequestMessage> getSentStreamRequestMessages()
  {
    return this.sentStreamRequestMessages;
  }
  
  public List<byte[]> getBroadcastedBytes()
  {
    return this.broadcastedBytes;
  }
  
  public StreamResponseMessage[] getStreamResponseMessages()
  {
    return null;
  }
  
  public StreamResponseMessage getStreamResponseMessage(StreamRequestMessage request)
  {
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\mock\MockRouter.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */