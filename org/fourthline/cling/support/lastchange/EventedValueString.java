package org.fourthline.cling.support.lastchange;

import java.util.Map.Entry;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.Datatype.Builtin;

public class EventedValueString
  extends EventedValue<String>
{
  public EventedValueString(String value)
  {
    super(value);
  }
  
  public EventedValueString(Map.Entry<String, String>[] attributes)
  {
    super(attributes);
  }
  
  protected Datatype getDatatype()
  {
    return Datatype.Builtin.STRING.getDatatype();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\lastchange\EventedValueString.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */