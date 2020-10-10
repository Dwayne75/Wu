package org.fourthline.cling.support.lastchange;

import java.util.Map.Entry;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.support.shared.AbstractMap.SimpleEntry;

public abstract class EventedValue<V>
{
  protected final V value;
  
  public EventedValue(V value)
  {
    this.value = value;
  }
  
  public EventedValue(Map.Entry<String, String>[] attributes)
  {
    try
    {
      this.value = valueOf(attributes);
    }
    catch (InvalidValueException ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  public String getName()
  {
    return getClass().getSimpleName();
  }
  
  public V getValue()
  {
    return (V)this.value;
  }
  
  public Map.Entry<String, String>[] getAttributes()
  {
    return new Map.Entry[] { new AbstractMap.SimpleEntry("val", toString()) };
  }
  
  protected V valueOf(Map.Entry<String, String>[] attributes)
    throws InvalidValueException
  {
    V v = null;
    for (Map.Entry<String, String> attribute : attributes) {
      if (((String)attribute.getKey()).equals("val")) {
        v = valueOf((String)attribute.getValue());
      }
    }
    return v;
  }
  
  protected V valueOf(String s)
    throws InvalidValueException
  {
    return (V)getDatatype().valueOf(s);
  }
  
  public String toString()
  {
    return getDatatype().getString(getValue());
  }
  
  protected abstract Datatype getDatatype();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\lastchange\EventedValue.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */