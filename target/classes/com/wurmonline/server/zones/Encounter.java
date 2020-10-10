package com.wurmonline.server.zones;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class Encounter
{
  private final Map<Integer, Integer> types = new HashMap();
  
  public void addType(int creatureTemplateId, int nums)
  {
    this.types.put(Integer.valueOf(creatureTemplateId), Integer.valueOf(nums));
  }
  
  public Map<Integer, Integer> getTypes()
  {
    return this.types;
  }
  
  public final String toString()
  {
    String toRet = "";
    for (Map.Entry<Integer, Integer> entry : this.types.entrySet()) {
      toRet = toRet + "Type " + entry.getKey() + " Numbers=" + entry.getValue() + ", ";
    }
    return toRet;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\zones\Encounter.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */