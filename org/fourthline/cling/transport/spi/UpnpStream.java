package org.fourthline.cling.transport.spi;

import java.util.logging.Logger;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpResponse.Status;
import org.fourthline.cling.protocol.ProtocolCreationException;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.protocol.ReceivingSync;
import org.seamless.util.Exceptions;

public abstract class UpnpStream
  implements Runnable
{
  private static Logger log = Logger.getLogger(UpnpStream.class.getName());
  protected final ProtocolFactory protocolFactory;
  protected ReceivingSync syncProtocol;
  
  protected UpnpStream(ProtocolFactory protocolFactory)
  {
    this.protocolFactory = protocolFactory;
  }
  
  public ProtocolFactory getProtocolFactory()
  {
    return this.protocolFactory;
  }
  
  public StreamResponseMessage process(StreamRequestMessage requestMsg)
  {
    log.fine("Processing stream request message: " + requestMsg);
    try
    {
      this.syncProtocol = getProtocolFactory().createReceivingSync(requestMsg);
    }
    catch (ProtocolCreationException ex)
    {
      log.warning("Processing stream request failed - " + Exceptions.unwrap(ex).toString());
      return new StreamResponseMessage(UpnpResponse.Status.NOT_IMPLEMENTED);
    }
    log.fine("Running protocol for synchronous message processing: " + this.syncProtocol);
    this.syncProtocol.run();
    
    StreamResponseMessage responseMsg = this.syncProtocol.getOutputMessage();
    if (responseMsg == null)
    {
      log.finer("Protocol did not return any response message");
      return null;
    }
    log.finer("Protocol returned response: " + responseMsg);
    return responseMsg;
  }
  
  protected void responseSent(StreamResponseMessage responseMessage)
  {
    if (this.syncProtocol != null) {
      this.syncProtocol.responseSent(responseMessage);
    }
  }
  
  protected void responseException(Throwable t)
  {
    if (this.syncProtocol != null) {
      this.syncProtocol.responseException(t);
    }
  }
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ")";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\spi\UpnpStream.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */