package org.fourthline.cling.support.model;

public class VolumeDBRange
{
  private Integer minValue;
  private Integer maxValue;
  
  public VolumeDBRange(Integer minValue, Integer maxValue)
  {
    this.minValue = minValue;
    this.maxValue = maxValue;
  }
  
  public Integer getMinValue()
  {
    return this.minValue;
  }
  
  public Integer getMaxValue()
  {
    return this.maxValue;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\VolumeDBRange.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */