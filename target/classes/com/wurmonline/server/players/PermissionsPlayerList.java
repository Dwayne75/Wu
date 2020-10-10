package com.wurmonline.server.players;

import com.wurmonline.server.MiscConstants;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionsPlayerList
  implements MiscConstants
{
  private Map<Long, PermissionsByPlayer> playerPermissions = new ConcurrentHashMap();
  
  public void remove(long aPlayerId)
  {
    this.playerPermissions.remove(Long.valueOf(aPlayerId));
  }
  
  public boolean isEmpty()
  {
    return this.playerPermissions.isEmpty();
  }
  
  public int size()
  {
    return this.playerPermissions.size();
  }
  
  public PermissionsByPlayer[] getPermissionsByPlayer()
  {
    return (PermissionsByPlayer[])this.playerPermissions.values().toArray(new PermissionsByPlayer[this.playerPermissions.size()]);
  }
  
  public PermissionsByPlayer add(long aPlayerId, int aPermissions)
  {
    return (PermissionsByPlayer)this.playerPermissions.put(Long.valueOf(aPlayerId), new PermissionsByPlayer(aPlayerId, aPermissions));
  }
  
  public boolean hasPermission(long playerId, int bit)
  {
    Long id = Long.valueOf(playerId);
    PermissionsByPlayer playerPerm = (PermissionsByPlayer)this.playerPermissions.get(id);
    if (playerPerm == null) {
      return false;
    }
    return playerPerm.hasPermission(bit);
  }
  
  public PermissionsByPlayer getPermissionsByPlayer(long playerId)
  {
    Long id = Long.valueOf(playerId);
    return (PermissionsByPlayer)this.playerPermissions.get(id);
  }
  
  public Permissions getPermissionsFor(long playerId)
  {
    Long id = Long.valueOf(playerId);
    PermissionsByPlayer playerPerm = (PermissionsByPlayer)this.playerPermissions.get(id);
    if (playerPerm == null)
    {
      PermissionsByPlayer everyone = (PermissionsByPlayer)this.playerPermissions.get(Long.valueOf(-10L));
      if (everyone == null) {
        return new Permissions();
      }
      return everyone.getPermissions();
    }
    return playerPerm.getPermissions();
  }
  
  public boolean exists(long playerId)
  {
    return this.playerPermissions.containsKey(Long.valueOf(playerId));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\players\PermissionsPlayerList.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */