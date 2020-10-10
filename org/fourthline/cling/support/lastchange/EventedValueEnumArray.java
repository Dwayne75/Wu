package org.fourthline.cling.support.lastchange;

import java.util.Map.Entry;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.InvalidValueException;

public abstract class EventedValueEnumArray<E extends Enum>
  extends EventedValue<E[]>
{
  public EventedValueEnumArray(E[] e)
  {
    super(e);
  }
  
  public EventedValueEnumArray(Map.Entry<String, String>[] attributes)
  {
    super(attributes);
  }
  
  protected E[] valueOf(String s)
    throws InvalidValueException
  {
    return enumValueOf(ModelUtil.fromCommaSeparatedList(s));
  }
  
  protected abstract E[] enumValueOf(String[] paramArrayOfString);
  
  public String toString()
  {
    return ModelUtil.toCommaSeparatedList((Object[])getValue());
  }
  
  protected Datatype getDatatype()
  {
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\lastchange\EventedValueEnumArray.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */