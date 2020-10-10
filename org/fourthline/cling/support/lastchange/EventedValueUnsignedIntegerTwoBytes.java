package org.fourthline.cling.support.lastchange;

import java.util.Map.Entry;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.Datatype.Builtin;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;

public class EventedValueUnsignedIntegerTwoBytes
  extends EventedValue<UnsignedIntegerTwoBytes>
{
  public EventedValueUnsignedIntegerTwoBytes(UnsignedIntegerTwoBytes value)
  {
    super(value);
  }
  
  public EventedValueUnsignedIntegerTwoBytes(Map.Entry<String, String>[] attributes)
  {
    super(attributes);
  }
  
  protected Datatype getDatatype()
  {
    return Datatype.Builtin.UI2.getDatatype();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\lastchange\EventedValueUnsignedIntegerTwoBytes.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */