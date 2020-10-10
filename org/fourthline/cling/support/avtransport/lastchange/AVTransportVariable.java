package org.fourthline.cling.support.avtransport.lastchange;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.lastchange.EventedValueEnum;
import org.fourthline.cling.support.lastchange.EventedValueEnumArray;
import org.fourthline.cling.support.lastchange.EventedValueString;
import org.fourthline.cling.support.lastchange.EventedValueURI;
import org.fourthline.cling.support.lastchange.EventedValueUnsignedIntegerFourBytes;
import org.fourthline.cling.support.model.PlayMode;
import org.fourthline.cling.support.model.RecordMediumWriteStatus;
import org.fourthline.cling.support.model.RecordQualityMode;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportState;
import org.fourthline.cling.support.model.TransportStatus;

public class AVTransportVariable
{
  public static Set<Class<? extends EventedValue>> ALL = new HashSet() {};
  
  public static class TransportState
    extends EventedValueEnum<TransportState>
  {
    public TransportState(TransportState avTransportState)
    {
      super();
    }
    
    public TransportState(Map.Entry<String, String>[] attributes)
    {
      super();
    }
    
    protected TransportState enumValueOf(String s)
    {
      return TransportState.valueOf(s);
    }
  }
  
  public static class TransportStatus
    extends EventedValueEnum<TransportStatus>
  {
    public TransportStatus(TransportStatus transportStatus)
    {
      super();
    }
    
    public TransportStatus(Map.Entry<String, String>[] attributes)
    {
      super();
    }
    
    protected TransportStatus enumValueOf(String s)
    {
      return TransportStatus.valueOf(s);
    }
  }
  
  public static class RecordStorageMedium
    extends EventedValueEnum<StorageMedium>
  {
    public RecordStorageMedium(StorageMedium storageMedium)
    {
      super();
    }
    
    public RecordStorageMedium(Map.Entry<String, String>[] attributes)
    {
      super();
    }
    
    protected StorageMedium enumValueOf(String s)
    {
      return StorageMedium.valueOf(s);
    }
  }
  
  public static class PossibleRecordStorageMedia
    extends EventedValueEnumArray<StorageMedium>
  {
    public PossibleRecordStorageMedia(StorageMedium[] e)
    {
      super();
    }
    
    public PossibleRecordStorageMedia(Map.Entry<String, String>[] attributes)
    {
      super();
    }
    
    protected StorageMedium[] enumValueOf(String[] names)
    {
      List<StorageMedium> list = new ArrayList();
      for (String s : names) {
        list.add(StorageMedium.valueOf(s));
      }
      return (StorageMedium[])list.toArray(new StorageMedium[list.size()]);
    }
  }
  
  public static class PossiblePlaybackStorageMedia
    extends AVTransportVariable.PossibleRecordStorageMedia
  {
    public PossiblePlaybackStorageMedia(StorageMedium[] e)
    {
      super();
    }
    
    public PossiblePlaybackStorageMedia(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class CurrentPlayMode
    extends EventedValueEnum<PlayMode>
  {
    public CurrentPlayMode(PlayMode playMode)
    {
      super();
    }
    
    public CurrentPlayMode(Map.Entry<String, String>[] attributes)
    {
      super();
    }
    
    protected PlayMode enumValueOf(String s)
    {
      return PlayMode.valueOf(s);
    }
  }
  
  public static class TransportPlaySpeed
    extends EventedValueString
  {
    static final Pattern pattern = Pattern.compile("^-?\\d+(/\\d+)?$", 2);
    
    public TransportPlaySpeed(String value)
    {
      super();
      if (!pattern.matcher(value).matches()) {
        throw new InvalidValueException("Can't parse TransportPlaySpeed speeds.");
      }
    }
    
    public TransportPlaySpeed(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class RecordMediumWriteStatus
    extends EventedValueEnum<RecordMediumWriteStatus>
  {
    public RecordMediumWriteStatus(RecordMediumWriteStatus recordMediumWriteStatus)
    {
      super();
    }
    
    public RecordMediumWriteStatus(Map.Entry<String, String>[] attributes)
    {
      super();
    }
    
    protected RecordMediumWriteStatus enumValueOf(String s)
    {
      return RecordMediumWriteStatus.valueOf(s);
    }
  }
  
  public static class CurrentRecordQualityMode
    extends EventedValueEnum<RecordQualityMode>
  {
    public CurrentRecordQualityMode(RecordQualityMode recordQualityMode)
    {
      super();
    }
    
    public CurrentRecordQualityMode(Map.Entry<String, String>[] attributes)
    {
      super();
    }
    
    protected RecordQualityMode enumValueOf(String s)
    {
      return RecordQualityMode.valueOf(s);
    }
  }
  
  public static class PossibleRecordQualityModes
    extends EventedValueEnumArray<RecordQualityMode>
  {
    public PossibleRecordQualityModes(RecordQualityMode[] e)
    {
      super();
    }
    
    public PossibleRecordQualityModes(Map.Entry<String, String>[] attributes)
    {
      super();
    }
    
    protected RecordQualityMode[] enumValueOf(String[] names)
    {
      List<RecordQualityMode> list = new ArrayList();
      for (String s : names) {
        list.add(RecordQualityMode.valueOf(s));
      }
      return (RecordQualityMode[])list.toArray(new RecordQualityMode[list.size()]);
    }
  }
  
  public static class NumberOfTracks
    extends EventedValueUnsignedIntegerFourBytes
  {
    public NumberOfTracks(UnsignedIntegerFourBytes value)
    {
      super();
    }
    
    public NumberOfTracks(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class CurrentTrack
    extends EventedValueUnsignedIntegerFourBytes
  {
    public CurrentTrack(UnsignedIntegerFourBytes value)
    {
      super();
    }
    
    public CurrentTrack(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class CurrentTrackDuration
    extends EventedValueString
  {
    public CurrentTrackDuration(String value)
    {
      super();
    }
    
    public CurrentTrackDuration(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class CurrentMediaDuration
    extends EventedValueString
  {
    public CurrentMediaDuration(String value)
    {
      super();
    }
    
    public CurrentMediaDuration(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class CurrentTrackMetaData
    extends EventedValueString
  {
    public CurrentTrackMetaData(String value)
    {
      super();
    }
    
    public CurrentTrackMetaData(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class CurrentTrackURI
    extends EventedValueURI
  {
    public CurrentTrackURI(URI value)
    {
      super();
    }
    
    public CurrentTrackURI(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class AVTransportURI
    extends EventedValueURI
  {
    public AVTransportURI(URI value)
    {
      super();
    }
    
    public AVTransportURI(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class NextAVTransportURI
    extends EventedValueURI
  {
    public NextAVTransportURI(URI value)
    {
      super();
    }
    
    public NextAVTransportURI(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class AVTransportURIMetaData
    extends EventedValueString
  {
    public AVTransportURIMetaData(String value)
    {
      super();
    }
    
    public AVTransportURIMetaData(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class NextAVTransportURIMetaData
    extends EventedValueString
  {
    public NextAVTransportURIMetaData(String value)
    {
      super();
    }
    
    public NextAVTransportURIMetaData(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class CurrentTransportActions
    extends EventedValueEnumArray<TransportAction>
  {
    public CurrentTransportActions(TransportAction[] e)
    {
      super();
    }
    
    public CurrentTransportActions(Map.Entry<String, String>[] attributes)
    {
      super();
    }
    
    protected TransportAction[] enumValueOf(String[] names)
    {
      if (names == null) {
        return new TransportAction[0];
      }
      List<TransportAction> list = new ArrayList();
      for (String s : names) {
        list.add(TransportAction.valueOf(s));
      }
      return (TransportAction[])list.toArray(new TransportAction[list.size()]);
    }
  }
  
  public static class RelativeTimePosition
    extends EventedValueString
  {
    public RelativeTimePosition(String value)
    {
      super();
    }
    
    public RelativeTimePosition(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class AbsoluteTimePosition
    extends EventedValueString
  {
    public AbsoluteTimePosition(String value)
    {
      super();
    }
    
    public AbsoluteTimePosition(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class RelativeCounterPosition
    extends EventedValueString
  {
    public RelativeCounterPosition(String value)
    {
      super();
    }
    
    public RelativeCounterPosition(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class AbsoluteCounterPosition
    extends EventedValueString
  {
    public AbsoluteCounterPosition(String value)
    {
      super();
    }
    
    public AbsoluteCounterPosition(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\avtransport\lastchange\AVTransportVariable.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */