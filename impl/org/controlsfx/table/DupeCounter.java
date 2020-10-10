package impl.org.controlsfx.table;

import java.util.HashMap;
import java.util.Optional;

final class DupeCounter<T>
{
  private final HashMap<T, Integer> counts = new HashMap();
  private final boolean enforceFloor;
  
  public DupeCounter(boolean enforceFloor)
  {
    this.enforceFloor = enforceFloor;
  }
  
  public int add(T value)
  {
    Integer prev = (Integer)this.counts.get(value);
    int newVal;
    if (prev == null)
    {
      int newVal = 1;
      this.counts.put(value, Integer.valueOf(newVal));
    }
    else
    {
      newVal = prev.intValue() + 1;
      this.counts.put(value, Integer.valueOf(newVal));
    }
    return newVal;
  }
  
  public int get(T value)
  {
    return ((Integer)Optional.ofNullable(this.counts.get(value)).orElse(Integer.valueOf(0))).intValue();
  }
  
  public int remove(T value)
  {
    Integer prev = (Integer)this.counts.get(value);
    if ((prev != null) && (prev.intValue() > 0))
    {
      int newVal = prev.intValue() - 1;
      if (newVal == 0) {
        this.counts.remove(value);
      } else {
        this.counts.put(value, Integer.valueOf(newVal));
      }
      return newVal;
    }
    if (this.enforceFloor) {
      throw new IllegalStateException();
    }
    return 0;
  }
  
  public String toString()
  {
    return this.counts.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\table\DupeCounter.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */