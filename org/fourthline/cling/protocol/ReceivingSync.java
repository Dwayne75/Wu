package org.fourthline.cling.protocol;

import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.profile.RemoteClientInfo;
import org.fourthline.cling.transport.RouterException;

public abstract class ReceivingSync<IN extends StreamRequestMessage, OUT extends StreamResponseMessage>
  extends ReceivingAsync<IN>
{
  private static final Logger log = Logger.getLogger(UpnpService.class.getName());
  protected final RemoteClientInfo remoteClientInfo;
  protected OUT outputMessage;
  
  protected ReceivingSync(UpnpService upnpService, IN inputMessage)
  {
    super(upnpService, inputMessage);
    this.remoteClientInfo = new RemoteClientInfo(inputMessage);
  }
  
  public OUT getOutputMessage()
  {
    return this.outputMessage;
  }
  
  protected final void execute()
    throws RouterException
  {
    this.outputMessage = executeSync();
    if ((this.outputMessage != null) && (getRemoteClientInfo().getExtraResponseHeaders().size() > 0))
    {
      log.fine("Setting extra headers on response message: " + getRemoteClientInfo().getExtraResponseHeaders().size());
      this.outputMessage.getHeaders().putAll(getRemoteClientInfo().getExtraResponseHeaders());
    }
  }
  
  protected abstract OUT executeSync()
    throws RouterException;
  
  public void responseSent(StreamResponseMessage responseMessage) {}
  
  public void responseException(Throwable t) {}
  
  public RemoteClientInfo getRemoteClientInfo()
  {
    return this.remoteClientInfo;
  }
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ")";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\protocol\ReceivingSync.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */