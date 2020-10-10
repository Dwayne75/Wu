package org.fourthline.cling.support.lastchange;

import java.util.Map.Entry;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.Datatype.Builtin;

public class EventedValueShort
  extends EventedValue<Short>
{
  public EventedValueShort(Short value)
  {
    super(value);
  }
  
  public EventedValueShort(Map.Entry<String, String>[] attributes)
  {
    super(attributes);
  }
  
  protected Datatype getDatatype()
  {
    return Datatype.Builtin.I2_SHORT.getDatatype();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\lastchange\EventedValueShort.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */