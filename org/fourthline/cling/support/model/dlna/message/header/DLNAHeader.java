package org.fourthline.cling.support.model.dlna.message.header;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.message.header.InvalidHeaderException;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.seamless.util.Exceptions;

public abstract class DLNAHeader<T>
  extends UpnpHeader<T>
{
  private static final Logger log = Logger.getLogger(DLNAHeader.class.getName());
  
  public static enum Type
  {
    TimeSeekRange("TimeSeekRange.dlna.org", new Class[] { TimeSeekRangeHeader.class }),  XSeekRange("X-Seek-Range", new Class[] { TimeSeekRangeHeader.class }),  PlaySpeed("PlaySpeed.dlna.org", new Class[] { PlaySpeedHeader.class }),  AvailableSeekRange("availableSeekRange.dlna.org", new Class[] { AvailableSeekRangeHeader.class }),  GetAvailableSeekRange("getAvailableSeekRange.dlna.org", new Class[] { GetAvailableSeekRangeHeader.class }),  GetContentFeatures("getcontentFeatures.dlna.org", new Class[] { GetContentFeaturesHeader.class }),  ContentFeatures("contentFeatures.dlna.org", new Class[] { ContentFeaturesHeader.class }),  TransferMode("transferMode.dlna.org", new Class[] { TransferModeHeader.class }),  FriendlyName("friendlyName.dlna.org", new Class[] { FriendlyNameHeader.class }),  PeerManager("peerManager.dlna.org", new Class[] { PeerManagerHeader.class }),  AvailableRange("Available-Range.dlna.org", new Class[] { AvailableRangeHeader.class }),  SCID("scid.dlna.org", new Class[] { SCIDHeader.class }),  RealTimeInfo("realTimeInfo.dlna.org", new Class[] { RealTimeInfoHeader.class }),  ScmsFlag("scmsFlag.dlna.org", new Class[] { ScmsFlagHeader.class }),  WCT("WCT.dlna.org", new Class[] { WCTHeader.class }),  MaxPrate("Max-Prate.dlna.org", new Class[] { MaxPrateHeader.class }),  EventType("Event-Type.dlna.org", new Class[] { EventTypeHeader.class }),  Supported("Supported", new Class[] { SupportedHeader.class }),  BufferInfo("Buffer-Info.dlna.org", new Class[] { BufferInfoHeader.class }),  RTPH264DeInterleaving("rtp-h264-deint-buf-cap.dlna.org", new Class[] { BufferBytesHeader.class }),  RTPAACDeInterleaving("rtp-aac-deint-buf-cap.dlna.org", new Class[] { BufferBytesHeader.class }),  RTPAMRDeInterleaving("rtp-amr-deint-buf-cap.dlna.org", new Class[] { BufferBytesHeader.class }),  RTPAMRWBPlusDeInterleaving("rtp-amrwbplus-deint-buf-cap.dlna.org", new Class[] { BufferBytesHeader.class }),  PRAGMA("PRAGMA", new Class[] { PragmaHeader.class });
    
    private static Map<String, Type> byName = new HashMap() {};
    private String httpName;
    private Class<? extends DLNAHeader>[] headerTypes;
    
    @SafeVarargs
    private Type(String httpName, Class<? extends DLNAHeader>... headerClass)
    {
      this.httpName = httpName;
      this.headerTypes = headerClass;
    }
    
    public String getHttpName()
    {
      return this.httpName;
    }
    
    public Class<? extends DLNAHeader>[] getHeaderTypes()
    {
      return this.headerTypes;
    }
    
    public boolean isValidHeaderType(Class<? extends DLNAHeader> clazz)
    {
      for (Class<? extends DLNAHeader> permissibleType : getHeaderTypes()) {
        if (permissibleType.isAssignableFrom(clazz)) {
          return true;
        }
      }
      return false;
    }
    
    public static Type getByHttpName(String httpName)
    {
      if (httpName == null) {
        return null;
      }
      return (Type)byName.get(httpName);
    }
  }
  
  public static DLNAHeader newInstance(Type type, String headerValue)
  {
    DLNAHeader upnpHeader = null;
    for (int i = 0; (i < type.getHeaderTypes().length) && (upnpHeader == null); i++)
    {
      Class<? extends DLNAHeader> headerClass = type.getHeaderTypes()[i];
      try
      {
        log.finest("Trying to parse '" + type + "' with class: " + headerClass.getSimpleName());
        upnpHeader = (DLNAHeader)headerClass.newInstance();
        if (headerValue != null) {
          upnpHeader.setString(headerValue);
        }
      }
      catch (InvalidHeaderException ex)
      {
        log.finest("Invalid header value for tested type: " + headerClass.getSimpleName() + " - " + ex.getMessage());
        upnpHeader = null;
      }
      catch (Exception ex)
      {
        log.severe("Error instantiating header of type '" + type + "' with value: " + headerValue);
        log.log(Level.SEVERE, "Exception root cause: ", Exceptions.unwrap(ex));
      }
    }
    return upnpHeader;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\message\header\DLNAHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */