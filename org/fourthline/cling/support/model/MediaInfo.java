package org.fourthline.cling.support.model;

import java.util.Map;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

public class MediaInfo
{
  private String currentURI = "";
  private String currentURIMetaData = "";
  private String nextURI = "NOT_IMPLEMENTED";
  private String nextURIMetaData = "NOT_IMPLEMENTED";
  private UnsignedIntegerFourBytes numberOfTracks = new UnsignedIntegerFourBytes(0L);
  private String mediaDuration = "00:00:00";
  private StorageMedium playMedium = StorageMedium.NONE;
  private StorageMedium recordMedium = StorageMedium.NOT_IMPLEMENTED;
  private RecordMediumWriteStatus writeStatus = RecordMediumWriteStatus.NOT_IMPLEMENTED;
  
  public MediaInfo() {}
  
  public MediaInfo(Map<String, ActionArgumentValue> args)
  {
    this(
      (String)((ActionArgumentValue)args.get("CurrentURI")).getValue(), 
      (String)((ActionArgumentValue)args.get("CurrentURIMetaData")).getValue(), 
      (String)((ActionArgumentValue)args.get("NextURI")).getValue(), 
      (String)((ActionArgumentValue)args.get("NextURIMetaData")).getValue(), 
      
      (UnsignedIntegerFourBytes)((ActionArgumentValue)args.get("NrTracks")).getValue(), 
      (String)((ActionArgumentValue)args.get("MediaDuration")).getValue(), 
      StorageMedium.valueOrVendorSpecificOf((String)((ActionArgumentValue)args.get("PlayMedium")).getValue()), 
      StorageMedium.valueOrVendorSpecificOf((String)((ActionArgumentValue)args.get("RecordMedium")).getValue()), 
      RecordMediumWriteStatus.valueOrUnknownOf((String)((ActionArgumentValue)args.get("WriteStatus")).getValue()));
  }
  
  public MediaInfo(String currentURI, String currentURIMetaData)
  {
    this.currentURI = currentURI;
    this.currentURIMetaData = currentURIMetaData;
  }
  
  public MediaInfo(String currentURI, String currentURIMetaData, UnsignedIntegerFourBytes numberOfTracks, String mediaDuration, StorageMedium playMedium)
  {
    this.currentURI = currentURI;
    this.currentURIMetaData = currentURIMetaData;
    this.numberOfTracks = numberOfTracks;
    this.mediaDuration = mediaDuration;
    this.playMedium = playMedium;
  }
  
  public MediaInfo(String currentURI, String currentURIMetaData, UnsignedIntegerFourBytes numberOfTracks, String mediaDuration, StorageMedium playMedium, StorageMedium recordMedium, RecordMediumWriteStatus writeStatus)
  {
    this.currentURI = currentURI;
    this.currentURIMetaData = currentURIMetaData;
    this.numberOfTracks = numberOfTracks;
    this.mediaDuration = mediaDuration;
    this.playMedium = playMedium;
    this.recordMedium = recordMedium;
    this.writeStatus = writeStatus;
  }
  
  public MediaInfo(String currentURI, String currentURIMetaData, String nextURI, String nextURIMetaData, UnsignedIntegerFourBytes numberOfTracks, String mediaDuration, StorageMedium playMedium)
  {
    this.currentURI = currentURI;
    this.currentURIMetaData = currentURIMetaData;
    this.nextURI = nextURI;
    this.nextURIMetaData = nextURIMetaData;
    this.numberOfTracks = numberOfTracks;
    this.mediaDuration = mediaDuration;
    this.playMedium = playMedium;
  }
  
  public MediaInfo(String currentURI, String currentURIMetaData, String nextURI, String nextURIMetaData, UnsignedIntegerFourBytes numberOfTracks, String mediaDuration, StorageMedium playMedium, StorageMedium recordMedium, RecordMediumWriteStatus writeStatus)
  {
    this.currentURI = currentURI;
    this.currentURIMetaData = currentURIMetaData;
    this.nextURI = nextURI;
    this.nextURIMetaData = nextURIMetaData;
    this.numberOfTracks = numberOfTracks;
    this.mediaDuration = mediaDuration;
    this.playMedium = playMedium;
    this.recordMedium = recordMedium;
    this.writeStatus = writeStatus;
  }
  
  public String getCurrentURI()
  {
    return this.currentURI;
  }
  
  public String getCurrentURIMetaData()
  {
    return this.currentURIMetaData;
  }
  
  public String getNextURI()
  {
    return this.nextURI;
  }
  
  public String getNextURIMetaData()
  {
    return this.nextURIMetaData;
  }
  
  public UnsignedIntegerFourBytes getNumberOfTracks()
  {
    return this.numberOfTracks;
  }
  
  public String getMediaDuration()
  {
    return this.mediaDuration;
  }
  
  public StorageMedium getPlayMedium()
  {
    return this.playMedium;
  }
  
  public StorageMedium getRecordMedium()
  {
    return this.recordMedium;
  }
  
  public RecordMediumWriteStatus getWriteStatus()
  {
    return this.writeStatus;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\MediaInfo.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */