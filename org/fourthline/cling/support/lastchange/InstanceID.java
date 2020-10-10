package org.fourthline.cling.support.lastchange;

import java.util.ArrayList;
import java.util.List;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

public class InstanceID
{
  protected UnsignedIntegerFourBytes id;
  protected List<EventedValue> values = new ArrayList();
  
  public InstanceID(UnsignedIntegerFourBytes id)
  {
    this(id, new ArrayList());
  }
  
  public InstanceID(UnsignedIntegerFourBytes id, List<EventedValue> values)
  {
    this.id = id;
    this.values = values;
  }
  
  public UnsignedIntegerFourBytes getId()
  {
    return this.id;
  }
  
  public List<EventedValue> getValues()
  {
    return this.values;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\lastchange\InstanceID.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */