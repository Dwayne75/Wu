package org.fourthline.cling.transport.impl;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.message.IncomingDatagramMessage;
import org.fourthline.cling.model.message.OutgoingDatagramMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpOperation;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.UpnpRequest.Method;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.transport.spi.DatagramProcessor;
import org.seamless.http.Headers;

public class DatagramProcessorImpl
  implements DatagramProcessor
{
  private static Logger log = Logger.getLogger(DatagramProcessor.class.getName());
  
  public IncomingDatagramMessage read(InetAddress receivedOnAddress, DatagramPacket datagram)
    throws UnsupportedDataException
  {
    try
    {
      if (log.isLoggable(Level.FINER))
      {
        log.finer("===================================== DATAGRAM BEGIN ============================================");
        log.finer(new String(datagram.getData(), "UTF-8"));
        log.finer("-===================================== DATAGRAM END =============================================");
      }
      ByteArrayInputStream is = new ByteArrayInputStream(datagram.getData());
      
      String[] startLine = Headers.readLine(is).split(" ");
      if (startLine[0].startsWith("HTTP/1.")) {
        return readResponseMessage(receivedOnAddress, datagram, is, Integer.valueOf(startLine[1]).intValue(), startLine[2], startLine[0]);
      }
      return readRequestMessage(receivedOnAddress, datagram, is, startLine[0], startLine[2]);
    }
    catch (Exception ex)
    {
      throw new UnsupportedDataException("Could not parse headers: " + ex, ex, datagram.getData());
    }
  }
  
  public DatagramPacket write(OutgoingDatagramMessage message)
    throws UnsupportedDataException
  {
    StringBuilder statusLine = new StringBuilder();
    
    UpnpOperation operation = message.getOperation();
    if ((operation instanceof UpnpRequest))
    {
      UpnpRequest requestOperation = (UpnpRequest)operation;
      statusLine.append(requestOperation.getHttpMethodName()).append(" * ");
      statusLine.append("HTTP/1.").append(operation.getHttpMinorVersion()).append("\r\n");
    }
    else if ((operation instanceof UpnpResponse))
    {
      UpnpResponse responseOperation = (UpnpResponse)operation;
      statusLine.append("HTTP/1.").append(operation.getHttpMinorVersion()).append(" ");
      statusLine.append(responseOperation.getStatusCode()).append(" ").append(responseOperation.getStatusMessage());
      statusLine.append("\r\n");
    }
    else
    {
      throw new UnsupportedDataException("Message operation is not request or response, don't know how to process: " + message);
    }
    StringBuilder messageData = new StringBuilder();
    messageData.append(statusLine);
    
    messageData.append(message.getHeaders().toString()).append("\r\n");
    if (log.isLoggable(Level.FINER))
    {
      log.finer("Writing message data for: " + message);
      log.finer("---------------------------------------------------------------------------------");
      log.finer(messageData.toString().substring(0, messageData.length() - 2));
      log.finer("---------------------------------------------------------------------------------");
    }
    try
    {
      byte[] data = messageData.toString().getBytes("US-ASCII");
      
      log.fine("Writing new datagram packet with " + data.length + " bytes for: " + message);
      return new DatagramPacket(data, data.length, message.getDestinationAddress(), message.getDestinationPort());
    }
    catch (UnsupportedEncodingException ex)
    {
      throw new UnsupportedDataException("Can't convert message content to US-ASCII: " + ex.getMessage(), ex, messageData);
    }
  }
  
  protected IncomingDatagramMessage readRequestMessage(InetAddress receivedOnAddress, DatagramPacket datagram, ByteArrayInputStream is, String requestMethod, String httpProtocol)
    throws Exception
  {
    UpnpHeaders headers = new UpnpHeaders(is);
    
    UpnpRequest upnpRequest = new UpnpRequest(UpnpRequest.Method.getByHttpName(requestMethod));
    upnpRequest.setHttpMinorVersion(httpProtocol.toUpperCase(Locale.ROOT).equals("HTTP/1.1") ? 1 : 0);
    IncomingDatagramMessage requestMessage = new IncomingDatagramMessage(upnpRequest, datagram.getAddress(), datagram.getPort(), receivedOnAddress);
    
    requestMessage.setHeaders(headers);
    
    return requestMessage;
  }
  
  protected IncomingDatagramMessage readResponseMessage(InetAddress receivedOnAddress, DatagramPacket datagram, ByteArrayInputStream is, int statusCode, String statusMessage, String httpProtocol)
    throws Exception
  {
    UpnpHeaders headers = new UpnpHeaders(is);
    
    UpnpResponse upnpResponse = new UpnpResponse(statusCode, statusMessage);
    upnpResponse.setHttpMinorVersion(httpProtocol.toUpperCase(Locale.ROOT).equals("HTTP/1.1") ? 1 : 0);
    IncomingDatagramMessage responseMessage = new IncomingDatagramMessage(upnpResponse, datagram.getAddress(), datagram.getPort(), receivedOnAddress);
    
    responseMessage.setHeaders(headers);
    
    return responseMessage;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\impl\DatagramProcessorImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */