package org.fourthline.cling.support.model;

import java.util.ArrayList;
import java.util.List;
import org.fourthline.cling.model.ModelUtil;

public enum RecordQualityMode
{
  EP("0:EP"),  LP("1:LP"),  SP("2:SP"),  BASIC("0:BASIC"),  MEDIUM("1:MEDIUM"),  HIGH("2:HIGH"),  NOT_IMPLEMENTED("NOT_IMPLEMENTED");
  
  private String protocolString;
  
  private RecordQualityMode(String protocolString)
  {
    this.protocolString = protocolString;
  }
  
  public String toString()
  {
    return this.protocolString;
  }
  
  public static RecordQualityMode valueOrExceptionOf(String s)
    throws IllegalArgumentException
  {
    for (RecordQualityMode recordQualityMode : ) {
      if (recordQualityMode.protocolString.equals(s)) {
        return recordQualityMode;
      }
    }
    throw new IllegalArgumentException("Invalid record quality mode string: " + s);
  }
  
  public static RecordQualityMode[] valueOfCommaSeparatedList(String s)
  {
    String[] strings = ModelUtil.fromCommaSeparatedList(s);
    if (strings == null) {
      return new RecordQualityMode[0];
    }
    List<RecordQualityMode> result = new ArrayList();
    for (String rqm : strings) {
      for (RecordQualityMode recordQualityMode : values()) {
        if (recordQualityMode.protocolString.equals(rqm)) {
          result.add(recordQualityMode);
        }
      }
    }
    return (RecordQualityMode[])result.toArray(new RecordQualityMode[result.size()]);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\RecordQualityMode.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */