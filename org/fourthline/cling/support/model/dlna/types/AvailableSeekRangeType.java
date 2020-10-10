package org.fourthline.cling.support.model.dlna.types;

import org.fourthline.cling.model.types.BytesRange;

public class AvailableSeekRangeType
{
  private Mode modeFlag;
  private NormalPlayTimeRange normalPlayTimeRange;
  private BytesRange bytesRange;
  
  public static enum Mode
  {
    MODE_0,  MODE_1;
    
    private Mode() {}
  }
  
  public AvailableSeekRangeType(Mode modeFlag, NormalPlayTimeRange nptRange)
  {
    this.modeFlag = modeFlag;
    this.normalPlayTimeRange = nptRange;
  }
  
  public AvailableSeekRangeType(Mode modeFlag, BytesRange byteRange)
  {
    this.modeFlag = modeFlag;
    this.bytesRange = byteRange;
  }
  
  public AvailableSeekRangeType(Mode modeFlag, NormalPlayTimeRange nptRange, BytesRange byteRange)
  {
    this.modeFlag = modeFlag;
    this.normalPlayTimeRange = nptRange;
    this.bytesRange = byteRange;
  }
  
  public NormalPlayTimeRange getNormalPlayTimeRange()
  {
    return this.normalPlayTimeRange;
  }
  
  public BytesRange getBytesRange()
  {
    return this.bytesRange;
  }
  
  public Mode getModeFlag()
  {
    return this.modeFlag;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\types\AvailableSeekRangeType.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */