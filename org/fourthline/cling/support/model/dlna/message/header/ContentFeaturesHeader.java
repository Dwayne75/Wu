package org.fourthline.cling.support.model.dlna.message.header;

import java.util.EnumMap;
import org.fourthline.cling.model.message.header.InvalidHeaderException;
import org.fourthline.cling.support.model.dlna.DLNAAttribute;
import org.fourthline.cling.support.model.dlna.DLNAAttribute.Type;

public class ContentFeaturesHeader
  extends DLNAHeader<EnumMap<DLNAAttribute.Type, DLNAAttribute>>
{
  public ContentFeaturesHeader()
  {
    setValue(new EnumMap(DLNAAttribute.Type.class));
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (s.length() != 0)
    {
      String[] atts = s.split(";");
      for (String att : atts)
      {
        String[] attNameValue = att.split("=");
        if (attNameValue.length == 2)
        {
          DLNAAttribute.Type type = DLNAAttribute.Type.valueOfAttributeName(attNameValue[0]);
          if (type != null)
          {
            DLNAAttribute dlnaAttrinute = DLNAAttribute.newInstance(type, attNameValue[1], "");
            ((EnumMap)getValue()).put(type, dlnaAttrinute);
          }
        }
      }
    }
  }
  
  public String getString()
  {
    String s = "";
    for (DLNAAttribute.Type type : DLNAAttribute.Type.values())
    {
      String value = ((EnumMap)getValue()).containsKey(type) ? ((DLNAAttribute)((EnumMap)getValue()).get(type)).getString() : null;
      if ((value != null) && (value.length() != 0)) {
        s = s + (s.length() == 0 ? "" : ";") + type.getAttributeName() + "=" + value;
      }
    }
    return s;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\message\header\ContentFeaturesHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */