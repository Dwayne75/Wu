package org.fourthline.cling.transport.spi;

import java.net.DatagramPacket;
import java.net.InetAddress;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.message.IncomingDatagramMessage;
import org.fourthline.cling.model.message.OutgoingDatagramMessage;

public abstract interface DatagramProcessor
{
  public abstract IncomingDatagramMessage read(InetAddress paramInetAddress, DatagramPacket paramDatagramPacket)
    throws UnsupportedDataException;
  
  public abstract DatagramPacket write(OutgoingDatagramMessage paramOutgoingDatagramMessage)
    throws UnsupportedDataException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\spi\DatagramProcessor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */