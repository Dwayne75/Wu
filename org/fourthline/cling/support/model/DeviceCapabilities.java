package org.fourthline.cling.support.model;

import java.util.Map;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.action.ActionArgumentValue;

public class DeviceCapabilities
{
  private StorageMedium[] playMedia;
  private StorageMedium[] recMedia = { StorageMedium.NOT_IMPLEMENTED };
  private RecordQualityMode[] recQualityModes = { RecordQualityMode.NOT_IMPLEMENTED };
  
  public DeviceCapabilities(Map<String, ActionArgumentValue> args)
  {
    this(
      StorageMedium.valueOfCommaSeparatedList((String)((ActionArgumentValue)args.get("PlayMedia")).getValue()), 
      StorageMedium.valueOfCommaSeparatedList((String)((ActionArgumentValue)args.get("RecMedia")).getValue()), 
      RecordQualityMode.valueOfCommaSeparatedList((String)((ActionArgumentValue)args.get("RecQualityModes")).getValue()));
  }
  
  public DeviceCapabilities(StorageMedium[] playMedia)
  {
    this.playMedia = playMedia;
  }
  
  public DeviceCapabilities(StorageMedium[] playMedia, StorageMedium[] recMedia, RecordQualityMode[] recQualityModes)
  {
    this.playMedia = playMedia;
    this.recMedia = recMedia;
    this.recQualityModes = recQualityModes;
  }
  
  public StorageMedium[] getPlayMedia()
  {
    return this.playMedia;
  }
  
  public StorageMedium[] getRecMedia()
  {
    return this.recMedia;
  }
  
  public RecordQualityMode[] getRecQualityModes()
  {
    return this.recQualityModes;
  }
  
  public String getPlayMediaString()
  {
    return ModelUtil.toCommaSeparatedList(this.playMedia);
  }
  
  public String getRecMediaString()
  {
    return ModelUtil.toCommaSeparatedList(this.recMedia);
  }
  
  public String getRecQualityModesString()
  {
    return ModelUtil.toCommaSeparatedList(this.recQualityModes);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\DeviceCapabilities.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */