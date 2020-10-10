package org.fourthline.cling.transport.spi;

import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.message.gena.IncomingEventRequestMessage;
import org.fourthline.cling.model.message.gena.OutgoingEventRequestMessage;

public abstract interface GENAEventProcessor
{
  public abstract void writeBody(OutgoingEventRequestMessage paramOutgoingEventRequestMessage)
    throws UnsupportedDataException;
  
  public abstract void readBody(IncomingEventRequestMessage paramIncomingEventRequestMessage)
    throws UnsupportedDataException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\spi\GENAEventProcessor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */