package org.fourthline.cling.support.lastchange;

import java.util.Map.Entry;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.InvalidValueException;

public abstract class EventedValueEnum<E extends Enum>
  extends EventedValue<E>
{
  public EventedValueEnum(E e)
  {
    super(e);
  }
  
  public EventedValueEnum(Map.Entry<String, String>[] attributes)
  {
    super(attributes);
  }
  
  protected E valueOf(String s)
    throws InvalidValueException
  {
    return enumValueOf(s);
  }
  
  protected abstract E enumValueOf(String paramString);
  
  public String toString()
  {
    return ((Enum)getValue()).name();
  }
  
  protected Datatype getDatatype()
  {
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\lastchange\EventedValueEnum.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */