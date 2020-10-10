package org.fourthline.cling.support.model.dlna.types;

import org.fourthline.cling.model.types.BytesRange;

public class TimeSeekRangeType
{
  private NormalPlayTimeRange normalPlayTimeRange;
  private BytesRange bytesRange;
  
  public TimeSeekRangeType(NormalPlayTimeRange nptRange)
  {
    this.normalPlayTimeRange = nptRange;
  }
  
  public TimeSeekRangeType(NormalPlayTimeRange nptRange, BytesRange byteRange)
  {
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
  
  public void setBytesRange(BytesRange bytesRange)
  {
    this.bytesRange = bytesRange;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\types\TimeSeekRangeType.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */