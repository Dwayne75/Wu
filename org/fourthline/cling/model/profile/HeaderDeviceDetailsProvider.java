package org.fourthline.cling.model.profile;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.meta.DeviceDetails;

public class HeaderDeviceDetailsProvider
  implements DeviceDetailsProvider
{
  private final DeviceDetails defaultDeviceDetails;
  private final Map<Key, DeviceDetails> headerDetails;
  
  public static class Key
  {
    final String headerName;
    final String valuePattern;
    final Pattern pattern;
    
    public Key(String headerName, String valuePattern)
    {
      this.headerName = headerName;
      this.valuePattern = valuePattern;
      this.pattern = Pattern.compile(valuePattern, 2);
    }
    
    public String getHeaderName()
    {
      return this.headerName;
    }
    
    public String getValuePattern()
    {
      return this.valuePattern;
    }
    
    public boolean isValuePatternMatch(String value)
    {
      return this.pattern.matcher(value).matches();
    }
  }
  
  public HeaderDeviceDetailsProvider(DeviceDetails defaultDeviceDetails)
  {
    this(defaultDeviceDetails, null);
  }
  
  public HeaderDeviceDetailsProvider(DeviceDetails defaultDeviceDetails, Map<Key, DeviceDetails> headerDetails)
  {
    this.defaultDeviceDetails = defaultDeviceDetails;
    this.headerDetails = (headerDetails != null ? headerDetails : new HashMap());
  }
  
  public DeviceDetails getDefaultDeviceDetails()
  {
    return this.defaultDeviceDetails;
  }
  
  public Map<Key, DeviceDetails> getHeaderDetails()
  {
    return this.headerDetails;
  }
  
  public DeviceDetails provide(RemoteClientInfo info)
  {
    if ((info == null) || (info.getRequestHeaders().isEmpty())) {
      return getDefaultDeviceDetails();
    }
    for (Iterator localIterator1 = getHeaderDetails().keySet().iterator(); localIterator1.hasNext();)
    {
      key = (Key)localIterator1.next();
      List<String> headerValues;
      if ((headerValues = info.getRequestHeaders().get(key.getHeaderName())) != null) {
        for (String headerValue : headerValues) {
          if (key.isValuePatternMatch(headerValue)) {
            return (DeviceDetails)getHeaderDetails().get(key);
          }
        }
      }
    }
    Key key;
    return getDefaultDeviceDetails();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\profile\HeaderDeviceDetailsProvider.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */