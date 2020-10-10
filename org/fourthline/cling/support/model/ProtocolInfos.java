package org.fourthline.cling.support.model;

import java.util.ArrayList;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.types.InvalidValueException;

public class ProtocolInfos
  extends ArrayList<ProtocolInfo>
{
  public ProtocolInfos(ProtocolInfo... info)
  {
    for (ProtocolInfo protocolInfo : info) {
      add(protocolInfo);
    }
  }
  
  public ProtocolInfos(String s)
    throws InvalidValueException
  {
    String[] infos = ModelUtil.fromCommaSeparatedList(s);
    if (infos != null) {
      for (String info : infos) {
        add(new ProtocolInfo(info));
      }
    }
  }
  
  public String toString()
  {
    return ModelUtil.toCommaSeparatedList(toArray(new ProtocolInfo[size()]));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\ProtocolInfos.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */