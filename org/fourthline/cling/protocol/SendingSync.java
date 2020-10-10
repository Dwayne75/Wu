package org.fourthline.cling.protocol;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.transport.RouterException;

public abstract class SendingSync<IN extends StreamRequestMessage, OUT extends StreamResponseMessage>
  extends SendingAsync
{
  private final IN inputMessage;
  protected OUT outputMessage;
  
  protected SendingSync(UpnpService upnpService, IN inputMessage)
  {
    super(upnpService);
    this.inputMessage = inputMessage;
  }
  
  public IN getInputMessage()
  {
    return this.inputMessage;
  }
  
  public OUT getOutputMessage()
  {
    return this.outputMessage;
  }
  
  protected final void execute()
    throws RouterException
  {
    this.outputMessage = executeSync();
  }
  
  protected abstract OUT executeSync()
    throws RouterException;
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ")";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\protocol\SendingSync.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */