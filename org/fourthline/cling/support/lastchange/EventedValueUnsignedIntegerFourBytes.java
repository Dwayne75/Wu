package org.fourthline.cling.support.lastchange;

import java.util.Map.Entry;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.Datatype.Builtin;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

public class EventedValueUnsignedIntegerFourBytes
  extends EventedValue<UnsignedIntegerFourBytes>
{
  public EventedValueUnsignedIntegerFourBytes(UnsignedIntegerFourBytes value)
  {
    super(value);
  }
  
  public EventedValueUnsignedIntegerFourBytes(Map.Entry<String, String>[] attributes)
  {
    super(attributes);
  }
  
  protected Datatype getDatatype()
  {
    return Datatype.Builtin.UI4.getDatatype();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\lastchange\EventedValueUnsignedIntegerFourBytes.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */