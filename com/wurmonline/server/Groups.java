package com.wurmonline.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Groups
{
  private static final Map<String, Group> groups = new ConcurrentHashMap();
  
  public static void addGroup(Group group)
  {
    groups.put(group.getName(), group);
  }
  
  public static void removeGroup(String name)
  {
    groups.remove(name);
  }
  
  public static void renameGroup(String oldName, String newName)
  {
    Group g = (Group)groups.remove(oldName);
    if (g != null)
    {
      g.setName(newName);
      groups.put(newName, g);
    }
  }
  
  public static Group getGroup(String name)
    throws NoSuchGroupException
  {
    Group toReturn = (Group)groups.get(name);
    if (toReturn == null) {
      throw new NoSuchGroupException(name);
    }
    return toReturn;
  }
  
  public static final Team getTeamForOfflineMember(long wurmid)
  {
    for (Group g : groups.values()) {
      if ((g.isTeam()) && (g.containsOfflineMember(wurmid))) {
        return (Team)g;
      }
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\Groups.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */