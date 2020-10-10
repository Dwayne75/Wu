package com.wurmonline.server.zones;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Stairs
{
  public static final Map<Integer, Set<Integer>> stairTiles = new ConcurrentHashMap();
  
  public static final void addStair(int volatileId, int floorLevel)
  {
    Set<Integer> stairSet = (Set)stairTiles.get(Integer.valueOf(volatileId));
    if (stairSet == null) {
      stairSet = new HashSet();
    }
    stairSet.add(Integer.valueOf(floorLevel));
    stairTiles.put(Integer.valueOf(volatileId), stairSet);
  }
  
  public static final boolean hasStair(int volatileId, int floorLevel)
  {
    Set<Integer> stairSet = (Set)stairTiles.get(Integer.valueOf(volatileId));
    if (stairSet == null) {
      return false;
    }
    return 
      stairSet.contains(Integer.valueOf(floorLevel));
  }
  
  public static final void removeStair(int volatileId, int floorLevel)
  {
    Set<Integer> stairSet = (Set)stairTiles.get(Integer.valueOf(volatileId));
    if (stairSet == null) {
      return;
    }
    stairSet.remove(Integer.valueOf(floorLevel));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\zones\Stairs.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */