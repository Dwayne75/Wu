package org.fourthline.cling.model.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.fourthline.cling.model.Validatable;
import org.fourthline.cling.model.ValidationError;

public class StateVariableAllowedValueRange
  implements Validatable
{
  private static final Logger log = Logger.getLogger(StateVariableAllowedValueRange.class.getName());
  private final long minimum;
  private final long maximum;
  private final long step;
  
  public StateVariableAllowedValueRange(long minimum, long maximum)
  {
    this(minimum, maximum, 1L);
  }
  
  public StateVariableAllowedValueRange(long minimum, long maximum, long step)
  {
    if (minimum > maximum)
    {
      log.warning("UPnP specification violation, allowed value range minimum '" + minimum + "' is greater than maximum '" + maximum + "', switching values.");
      
      this.minimum = maximum;
      this.maximum = minimum;
    }
    else
    {
      this.minimum = minimum;
      this.maximum = maximum;
    }
    this.step = step;
  }
  
  public long getMinimum()
  {
    return this.minimum;
  }
  
  public long getMaximum()
  {
    return this.maximum;
  }
  
  public long getStep()
  {
    return this.step;
  }
  
  public boolean isInRange(long value)
  {
    return (value >= getMinimum()) && (value <= getMaximum()) && (value % this.step == 0L);
  }
  
  public List<ValidationError> validate()
  {
    return new ArrayList();
  }
  
  public String toString()
  {
    return "Range Min: " + getMinimum() + " Max: " + getMaximum() + " Step: " + getStep();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\meta\StateVariableAllowedValueRange.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */