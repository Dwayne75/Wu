package org.fourthline.cling.support.model;

import java.util.logging.Logger;

public enum Protocol
{
  ALL("*"),  HTTP_GET("http-get"),  RTSP_RTP_UDP("rtsp-rtp-udp"),  INTERNAL("internal"),  IEC61883("iec61883"),  XBMC_GET("xbmc-get"),  OTHER("other");
  
  private static final Logger LOG = Logger.getLogger(Protocol.class.getName());
  private String protocolString;
  
  private Protocol(String protocolString)
  {
    this.protocolString = protocolString;
  }
  
  public String toString()
  {
    return this.protocolString;
  }
  
  public static Protocol value(String s)
  {
    for (Protocol protocol : ) {
      if (protocol.toString().equals(s)) {
        return protocol;
      }
    }
    LOG.info("Unsupported OTHER protocol string: " + s);
    return OTHER;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\Protocol.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */