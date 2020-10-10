package org.fourthline.cling.support.renderingcontrol.lastchange;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;
import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.lastchange.EventedValueShort;
import org.fourthline.cling.support.lastchange.EventedValueString;
import org.fourthline.cling.support.lastchange.EventedValueUnsignedIntegerTwoBytes;

public class RenderingControlVariable
{
  public static Set<Class<? extends EventedValue>> ALL = new HashSet() {};
  
  public static class PresetNameList
    extends EventedValueString
  {
    public PresetNameList(String s)
    {
      super();
    }
    
    public PresetNameList(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class Brightness
    extends EventedValueUnsignedIntegerTwoBytes
  {
    public Brightness(UnsignedIntegerTwoBytes value)
    {
      super();
    }
    
    public Brightness(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class Contrast
    extends EventedValueUnsignedIntegerTwoBytes
  {
    public Contrast(UnsignedIntegerTwoBytes value)
    {
      super();
    }
    
    public Contrast(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class Sharpness
    extends EventedValueUnsignedIntegerTwoBytes
  {
    public Sharpness(UnsignedIntegerTwoBytes value)
    {
      super();
    }
    
    public Sharpness(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class RedVideoGain
    extends EventedValueUnsignedIntegerTwoBytes
  {
    public RedVideoGain(UnsignedIntegerTwoBytes value)
    {
      super();
    }
    
    public RedVideoGain(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class BlueVideoGain
    extends EventedValueUnsignedIntegerTwoBytes
  {
    public BlueVideoGain(UnsignedIntegerTwoBytes value)
    {
      super();
    }
    
    public BlueVideoGain(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class GreenVideoGain
    extends EventedValueUnsignedIntegerTwoBytes
  {
    public GreenVideoGain(UnsignedIntegerTwoBytes value)
    {
      super();
    }
    
    public GreenVideoGain(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class RedVideoBlackLevel
    extends EventedValueUnsignedIntegerTwoBytes
  {
    public RedVideoBlackLevel(UnsignedIntegerTwoBytes value)
    {
      super();
    }
    
    public RedVideoBlackLevel(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class BlueVideoBlackLevel
    extends EventedValueUnsignedIntegerTwoBytes
  {
    public BlueVideoBlackLevel(UnsignedIntegerTwoBytes value)
    {
      super();
    }
    
    public BlueVideoBlackLevel(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class GreenVideoBlackLevel
    extends EventedValueUnsignedIntegerTwoBytes
  {
    public GreenVideoBlackLevel(UnsignedIntegerTwoBytes value)
    {
      super();
    }
    
    public GreenVideoBlackLevel(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class ColorTemperature
    extends EventedValueUnsignedIntegerTwoBytes
  {
    public ColorTemperature(UnsignedIntegerTwoBytes value)
    {
      super();
    }
    
    public ColorTemperature(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class HorizontalKeystone
    extends EventedValueShort
  {
    public HorizontalKeystone(Short value)
    {
      super();
    }
    
    public HorizontalKeystone(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class VerticalKeystone
    extends EventedValueShort
  {
    public VerticalKeystone(Short value)
    {
      super();
    }
    
    public VerticalKeystone(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class Mute
    extends EventedValueChannelMute
  {
    public Mute(ChannelMute value)
    {
      super();
    }
    
    public Mute(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class VolumeDB
    extends EventedValueChannelVolumeDB
  {
    public VolumeDB(ChannelVolumeDB value)
    {
      super();
    }
    
    public VolumeDB(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class Volume
    extends EventedValueChannelVolume
  {
    public Volume(ChannelVolume value)
    {
      super();
    }
    
    public Volume(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
  
  public static class Loudness
    extends EventedValueChannelLoudness
  {
    public Loudness(ChannelLoudness value)
    {
      super();
    }
    
    public Loudness(Map.Entry<String, String>[] attributes)
    {
      super();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\renderingcontrol\lastchange\RenderingControlVariable.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */