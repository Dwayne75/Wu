package org.fourthline.cling.support.model;

import java.util.ArrayList;
import java.util.List;
import org.fourthline.cling.model.ModelUtil;

public enum TransportAction
{
  Play,  Stop,  Pause,  Seek,  Next,  Previous,  Record;
  
  private TransportAction() {}
  
  public static TransportAction[] valueOfCommaSeparatedList(String s)
  {
    String[] strings = ModelUtil.fromCommaSeparatedList(s);
    if (strings == null) {
      return new TransportAction[0];
    }
    List<TransportAction> result = new ArrayList();
    for (String taString : strings) {
      for (TransportAction ta : values()) {
        if (ta.name().equals(taString)) {
          result.add(ta);
        }
      }
    }
    return (TransportAction[])result.toArray(new TransportAction[result.size()]);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\TransportAction.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */