package org.fourthline.cling.support.avtransport;

import java.beans.PropertyChangeSupport;
import java.net.URI;
import org.fourthline.cling.binding.annotations.UpnpAction;
import org.fourthline.cling.binding.annotations.UpnpInputArgument;
import org.fourthline.cling.binding.annotations.UpnpService;
import org.fourthline.cling.binding.annotations.UpnpServiceId;
import org.fourthline.cling.binding.annotations.UpnpServiceType;
import org.fourthline.cling.binding.annotations.UpnpStateVariable;
import org.fourthline.cling.binding.annotations.UpnpStateVariables;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.AVTransportURI;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.AVTransportURIMetaData;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.CurrentMediaDuration;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.CurrentPlayMode;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.CurrentRecordQualityMode;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.CurrentTrack;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.CurrentTrackDuration;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.CurrentTrackMetaData;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.CurrentTrackURI;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.CurrentTransportActions;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.NextAVTransportURI;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.NextAVTransportURIMetaData;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.NumberOfTracks;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.PossiblePlaybackStorageMedia;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.PossibleRecordQualityModes;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.PossibleRecordStorageMedia;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.RecordMediumWriteStatus;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.RecordStorageMedium;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.TransportPlaySpeed;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.TransportState;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.TransportStatus;
import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.lastchange.LastChangeDelegator;
import org.fourthline.cling.support.model.DeviceCapabilities;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportSettings;

@UpnpService(serviceId=@UpnpServiceId("AVTransport"), serviceType=@UpnpServiceType(value="AVTransport", version=1), stringConvertibleTypes={LastChange.class})
@UpnpStateVariables({@UpnpStateVariable(name="TransportState", sendEvents=false, allowedValuesEnum=org.fourthline.cling.support.model.TransportState.class), @UpnpStateVariable(name="TransportStatus", sendEvents=false, allowedValuesEnum=org.fourthline.cling.support.model.TransportStatus.class), @UpnpStateVariable(name="PlaybackStorageMedium", sendEvents=false, defaultValue="NONE", allowedValuesEnum=org.fourthline.cling.support.model.StorageMedium.class), @UpnpStateVariable(name="RecordStorageMedium", sendEvents=false, defaultValue="NOT_IMPLEMENTED", allowedValuesEnum=org.fourthline.cling.support.model.StorageMedium.class), @UpnpStateVariable(name="PossiblePlaybackStorageMedia", sendEvents=false, datatype="string", defaultValue="NETWORK"), @UpnpStateVariable(name="PossibleRecordStorageMedia", sendEvents=false, datatype="string", defaultValue="NOT_IMPLEMENTED"), @UpnpStateVariable(name="CurrentPlayMode", sendEvents=false, defaultValue="NORMAL", allowedValuesEnum=org.fourthline.cling.support.model.PlayMode.class), @UpnpStateVariable(name="TransportPlaySpeed", sendEvents=false, datatype="string", defaultValue="1"), @UpnpStateVariable(name="RecordMediumWriteStatus", sendEvents=false, defaultValue="NOT_IMPLEMENTED", allowedValuesEnum=org.fourthline.cling.support.model.RecordMediumWriteStatus.class), @UpnpStateVariable(name="CurrentRecordQualityMode", sendEvents=false, defaultValue="NOT_IMPLEMENTED", allowedValuesEnum=org.fourthline.cling.support.model.RecordQualityMode.class), @UpnpStateVariable(name="PossibleRecordQualityModes", sendEvents=false, datatype="string", defaultValue="NOT_IMPLEMENTED"), @UpnpStateVariable(name="NumberOfTracks", sendEvents=false, datatype="ui4", defaultValue="0"), @UpnpStateVariable(name="CurrentTrack", sendEvents=false, datatype="ui4", defaultValue="0"), @UpnpStateVariable(name="CurrentTrackDuration", sendEvents=false, datatype="string"), @UpnpStateVariable(name="CurrentMediaDuration", sendEvents=false, datatype="string", defaultValue="00:00:00"), @UpnpStateVariable(name="CurrentTrackMetaData", sendEvents=false, datatype="string", defaultValue="NOT_IMPLEMENTED"), @UpnpStateVariable(name="CurrentTrackURI", sendEvents=false, datatype="string"), @UpnpStateVariable(name="AVTransportURI", sendEvents=false, datatype="string"), @UpnpStateVariable(name="AVTransportURIMetaData", sendEvents=false, datatype="string", defaultValue="NOT_IMPLEMENTED"), @UpnpStateVariable(name="NextAVTransportURI", sendEvents=false, datatype="string", defaultValue="NOT_IMPLEMENTED"), @UpnpStateVariable(name="NextAVTransportURIMetaData", sendEvents=false, datatype="string", defaultValue="NOT_IMPLEMENTED"), @UpnpStateVariable(name="RelativeTimePosition", sendEvents=false, datatype="string"), @UpnpStateVariable(name="AbsoluteTimePosition", sendEvents=false, datatype="string"), @UpnpStateVariable(name="RelativeCounterPosition", sendEvents=false, datatype="i4", defaultValue="2147483647"), @UpnpStateVariable(name="AbsoluteCounterPosition", sendEvents=false, datatype="i4", defaultValue="2147483647"), @UpnpStateVariable(name="CurrentTransportActions", sendEvents=false, datatype="string"), @UpnpStateVariable(name="A_ARG_TYPE_SeekMode", sendEvents=false, allowedValuesEnum=org.fourthline.cling.support.model.SeekMode.class), @UpnpStateVariable(name="A_ARG_TYPE_SeekTarget", sendEvents=false, datatype="string"), @UpnpStateVariable(name="A_ARG_TYPE_InstanceID", sendEvents=false, datatype="ui4")})
public abstract class AbstractAVTransportService
  implements LastChangeDelegator
{
  @UpnpStateVariable(eventMaximumRateMilliseconds=200)
  private final LastChange lastChange;
  protected final PropertyChangeSupport propertyChangeSupport;
  
  protected AbstractAVTransportService()
  {
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    this.lastChange = new LastChange(new AVTransportLastChangeParser());
  }
  
  protected AbstractAVTransportService(LastChange lastChange)
  {
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    this.lastChange = lastChange;
  }
  
  protected AbstractAVTransportService(PropertyChangeSupport propertyChangeSupport)
  {
    this.propertyChangeSupport = propertyChangeSupport;
    this.lastChange = new LastChange(new AVTransportLastChangeParser());
  }
  
  protected AbstractAVTransportService(PropertyChangeSupport propertyChangeSupport, LastChange lastChange)
  {
    this.propertyChangeSupport = propertyChangeSupport;
    this.lastChange = lastChange;
  }
  
  public LastChange getLastChange()
  {
    return this.lastChange;
  }
  
  public void appendCurrentState(LastChange lc, UnsignedIntegerFourBytes instanceId)
    throws Exception
  {
    MediaInfo mediaInfo = getMediaInfo(instanceId);
    TransportInfo transportInfo = getTransportInfo(instanceId);
    TransportSettings transportSettings = getTransportSettings(instanceId);
    PositionInfo positionInfo = getPositionInfo(instanceId);
    DeviceCapabilities deviceCaps = getDeviceCapabilities(instanceId);
    
    lc.setEventedValue(instanceId, new EventedValue[] { new AVTransportVariable.AVTransportURI(
    
      URI.create(mediaInfo.getCurrentURI())), new AVTransportVariable.AVTransportURIMetaData(mediaInfo
      .getCurrentURIMetaData()), new AVTransportVariable.CurrentMediaDuration(mediaInfo
      .getMediaDuration()), new AVTransportVariable.CurrentPlayMode(transportSettings
      .getPlayMode()), new AVTransportVariable.CurrentRecordQualityMode(transportSettings
      .getRecQualityMode()), new AVTransportVariable.CurrentTrack(positionInfo
      .getTrack()), new AVTransportVariable.CurrentTrackDuration(positionInfo
      .getTrackDuration()), new AVTransportVariable.CurrentTrackMetaData(positionInfo
      .getTrackMetaData()), new AVTransportVariable.CurrentTrackURI(
      URI.create(positionInfo.getTrackURI())), new AVTransportVariable.CurrentTransportActions(
      getCurrentTransportActions(instanceId)), new AVTransportVariable.NextAVTransportURI(
      URI.create(mediaInfo.getNextURI())), new AVTransportVariable.NextAVTransportURIMetaData(mediaInfo
      .getNextURIMetaData()), new AVTransportVariable.NumberOfTracks(mediaInfo
      .getNumberOfTracks()), new AVTransportVariable.PossiblePlaybackStorageMedia(deviceCaps
      .getPlayMedia()), new AVTransportVariable.PossibleRecordQualityModes(deviceCaps
      .getRecQualityModes()), new AVTransportVariable.PossibleRecordStorageMedia(deviceCaps
      .getRecMedia()), new AVTransportVariable.RecordMediumWriteStatus(mediaInfo
      .getWriteStatus()), new AVTransportVariable.RecordStorageMedium(mediaInfo
      .getRecordMedium()), new AVTransportVariable.TransportPlaySpeed(transportInfo
      .getCurrentSpeed()), new AVTransportVariable.TransportState(transportInfo
      .getCurrentTransportState()), new AVTransportVariable.TransportStatus(transportInfo
      .getCurrentTransportStatus()) });
  }
  
  public PropertyChangeSupport getPropertyChangeSupport()
  {
    return this.propertyChangeSupport;
  }
  
  public static UnsignedIntegerFourBytes getDefaultInstanceID()
  {
    return new UnsignedIntegerFourBytes(0L);
  }
  
  @UpnpAction
  public abstract void setAVTransportURI(@UpnpInputArgument(name="InstanceID") UnsignedIntegerFourBytes paramUnsignedIntegerFourBytes, @UpnpInputArgument(name="CurrentURI", stateVariable="AVTransportURI") String paramString1, @UpnpInputArgument(name="CurrentURIMetaData", stateVariable="AVTransportURIMetaData") String paramString2)
    throws AVTransportException;
  
  @UpnpAction
  public abstract void setNextAVTransportURI(@UpnpInputArgument(name="InstanceID") UnsignedIntegerFourBytes paramUnsignedIntegerFourBytes, @UpnpInputArgument(name="NextURI", stateVariable="AVTransportURI") String paramString1, @UpnpInputArgument(name="NextURIMetaData", stateVariable="AVTransportURIMetaData") String paramString2)
    throws AVTransportException;
  
  @UpnpAction(out={@org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="NrTracks", stateVariable="NumberOfTracks", getterName="getNumberOfTracks"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="MediaDuration", stateVariable="CurrentMediaDuration", getterName="getMediaDuration"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="CurrentURI", stateVariable="AVTransportURI", getterName="getCurrentURI"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="CurrentURIMetaData", stateVariable="AVTransportURIMetaData", getterName="getCurrentURIMetaData"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="NextURI", stateVariable="NextAVTransportURI", getterName="getNextURI"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="NextURIMetaData", stateVariable="NextAVTransportURIMetaData", getterName="getNextURIMetaData"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="PlayMedium", stateVariable="PlaybackStorageMedium", getterName="getPlayMedium"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="RecordMedium", stateVariable="RecordStorageMedium", getterName="getRecordMedium"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="WriteStatus", stateVariable="RecordMediumWriteStatus", getterName="getWriteStatus")})
  public abstract MediaInfo getMediaInfo(@UpnpInputArgument(name="InstanceID") UnsignedIntegerFourBytes paramUnsignedIntegerFourBytes)
    throws AVTransportException;
  
  @UpnpAction(out={@org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="CurrentTransportState", stateVariable="TransportState", getterName="getCurrentTransportState"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="CurrentTransportStatus", stateVariable="TransportStatus", getterName="getCurrentTransportStatus"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="CurrentSpeed", stateVariable="TransportPlaySpeed", getterName="getCurrentSpeed")})
  public abstract TransportInfo getTransportInfo(@UpnpInputArgument(name="InstanceID") UnsignedIntegerFourBytes paramUnsignedIntegerFourBytes)
    throws AVTransportException;
  
  @UpnpAction(out={@org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="Track", stateVariable="CurrentTrack", getterName="getTrack"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="TrackDuration", stateVariable="CurrentTrackDuration", getterName="getTrackDuration"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="TrackMetaData", stateVariable="CurrentTrackMetaData", getterName="getTrackMetaData"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="TrackURI", stateVariable="CurrentTrackURI", getterName="getTrackURI"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="RelTime", stateVariable="RelativeTimePosition", getterName="getRelTime"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="AbsTime", stateVariable="AbsoluteTimePosition", getterName="getAbsTime"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="RelCount", stateVariable="RelativeCounterPosition", getterName="getRelCount"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="AbsCount", stateVariable="AbsoluteCounterPosition", getterName="getAbsCount")})
  public abstract PositionInfo getPositionInfo(@UpnpInputArgument(name="InstanceID") UnsignedIntegerFourBytes paramUnsignedIntegerFourBytes)
    throws AVTransportException;
  
  @UpnpAction(out={@org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="PlayMedia", stateVariable="PossiblePlaybackStorageMedia", getterName="getPlayMediaString"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="RecMedia", stateVariable="PossibleRecordStorageMedia", getterName="getRecMediaString"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="RecQualityModes", stateVariable="PossibleRecordQualityModes", getterName="getRecQualityModesString")})
  public abstract DeviceCapabilities getDeviceCapabilities(@UpnpInputArgument(name="InstanceID") UnsignedIntegerFourBytes paramUnsignedIntegerFourBytes)
    throws AVTransportException;
  
  @UpnpAction(out={@org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="PlayMode", stateVariable="CurrentPlayMode", getterName="getPlayMode"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="RecQualityMode", stateVariable="CurrentRecordQualityMode", getterName="getRecQualityMode")})
  public abstract TransportSettings getTransportSettings(@UpnpInputArgument(name="InstanceID") UnsignedIntegerFourBytes paramUnsignedIntegerFourBytes)
    throws AVTransportException;
  
  @UpnpAction
  public abstract void stop(@UpnpInputArgument(name="InstanceID") UnsignedIntegerFourBytes paramUnsignedIntegerFourBytes)
    throws AVTransportException;
  
  @UpnpAction
  public abstract void play(@UpnpInputArgument(name="InstanceID") UnsignedIntegerFourBytes paramUnsignedIntegerFourBytes, @UpnpInputArgument(name="Speed", stateVariable="TransportPlaySpeed") String paramString)
    throws AVTransportException;
  
  @UpnpAction
  public abstract void pause(@UpnpInputArgument(name="InstanceID") UnsignedIntegerFourBytes paramUnsignedIntegerFourBytes)
    throws AVTransportException;
  
  @UpnpAction
  public abstract void record(@UpnpInputArgument(name="InstanceID") UnsignedIntegerFourBytes paramUnsignedIntegerFourBytes)
    throws AVTransportException;
  
  @UpnpAction
  public abstract void seek(@UpnpInputArgument(name="InstanceID") UnsignedIntegerFourBytes paramUnsignedIntegerFourBytes, @UpnpInputArgument(name="Unit", stateVariable="A_ARG_TYPE_SeekMode") String paramString1, @UpnpInputArgument(name="Target", stateVariable="A_ARG_TYPE_SeekTarget") String paramString2)
    throws AVTransportException;
  
  @UpnpAction
  public abstract void next(@UpnpInputArgument(name="InstanceID") UnsignedIntegerFourBytes paramUnsignedIntegerFourBytes)
    throws AVTransportException;
  
  @UpnpAction
  public abstract void previous(@UpnpInputArgument(name="InstanceID") UnsignedIntegerFourBytes paramUnsignedIntegerFourBytes)
    throws AVTransportException;
  
  @UpnpAction
  public abstract void setPlayMode(@UpnpInputArgument(name="InstanceID") UnsignedIntegerFourBytes paramUnsignedIntegerFourBytes, @UpnpInputArgument(name="NewPlayMode", stateVariable="CurrentPlayMode") String paramString)
    throws AVTransportException;
  
  @UpnpAction
  public abstract void setRecordQualityMode(@UpnpInputArgument(name="InstanceID") UnsignedIntegerFourBytes paramUnsignedIntegerFourBytes, @UpnpInputArgument(name="NewRecordQualityMode", stateVariable="CurrentRecordQualityMode") String paramString)
    throws AVTransportException;
  
  @UpnpAction(name="GetCurrentTransportActions", out={@org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="Actions", stateVariable="CurrentTransportActions")})
  public String getCurrentTransportActionsString(@UpnpInputArgument(name="InstanceID") UnsignedIntegerFourBytes instanceId)
    throws AVTransportException
  {
    try
    {
      return ModelUtil.toCommaSeparatedList(getCurrentTransportActions(instanceId));
    }
    catch (Exception ex) {}
    return "";
  }
  
  protected abstract TransportAction[] getCurrentTransportActions(UnsignedIntegerFourBytes paramUnsignedIntegerFourBytes)
    throws Exception;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\avtransport\AbstractAVTransportService.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */