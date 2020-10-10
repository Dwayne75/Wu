package com.wurmonline.server.structures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.MineDoorSettings.MinedoorPermissions;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.PermissionsByPlayer;
import com.wurmonline.server.players.PermissionsPlayerList;
import com.wurmonline.server.players.PermissionsPlayerList.ISettings;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DoorSettings
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(DoorSettings.class.getName());
  private static final String GET_ALL_SETTINGS = "SELECT * FROM DOORSETTINGS";
  private static final String ADD_PLAYER = "INSERT INTO DOORSETTINGS (SETTINGS,WURMID,PLAYERID) VALUES(?,?,?)";
  private static final String DELETE_SETTINGS = "DELETE FROM DOORSETTINGS WHERE WURMID=?";
  private static final String REMOVE_PLAYER = "DELETE FROM DOORSETTINGS WHERE WURMID=? AND PLAYERID=?";
  private static final String UPDATE_PLAYER = "UPDATE DOORSETTINGS SET SETTINGS=? WHERE WURMID=? AND PLAYERID=?";
  private static final int MAX_PLAYERS_PER_OBJECT = 1000;
  private static Map<Long, PermissionsPlayerList> objectSettings = new ConcurrentHashMap();
  
  public static void loadAll()
    throws IOException
  {
    logger.log(Level.INFO, "Loading all door (and gate) settings.");
    long start = System.nanoTime();
    long count = 0L;
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM DOORSETTINGS");
      rs = ps.executeQuery();
      while (rs.next())
      {
        long wurmId = rs.getLong("WURMID");
        long playerId = rs.getLong("PLAYERID");
        int settings = rs.getInt("SETTINGS");
        add(wurmId, playerId, settings);
        count += 1L;
      }
    }
    catch (SQLException ex)
    {
      long end;
      logger.log(Level.WARNING, "Failed to load settings for doors (and gates).", ex);
    }
    finally
    {
      long end;
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
      long end = System.nanoTime();
      logger.log(Level.INFO, "Loaded " + count + " door (and gate) settings. That took " + (float)(end - start) / 1000000.0F + " ms.");
    }
  }
  
  public static int getMaxAllowed()
  {
    return Servers.isThisATestServer() ? 10 : 1000;
  }
  
  private static PermissionsByPlayer add(long wurmId, long playerId, int settings)
  {
    Long id = Long.valueOf(wurmId);
    if (objectSettings.containsKey(id))
    {
      PermissionsPlayerList ppl = (PermissionsPlayerList)objectSettings.get(id);
      return ppl.add(playerId, settings);
    }
    PermissionsPlayerList ppl = new PermissionsPlayerList();
    objectSettings.put(id, ppl);
    return ppl.add(playerId, settings);
  }
  
  public static void addPlayer(long wurmId, long playerId, int settings)
  {
    PermissionsByPlayer pbp = add(wurmId, playerId, settings);
    if (pbp == null) {
      dbAddPlayer(wurmId, playerId, settings, true);
    } else if (pbp.getSettings() != settings) {
      dbAddPlayer(wurmId, playerId, settings, false);
    }
  }
  
  public static void removePlayer(long wurmId, long playerId)
  {
    Long id = Long.valueOf(wurmId);
    if (objectSettings.containsKey(id))
    {
      PermissionsPlayerList ppl = (PermissionsPlayerList)objectSettings.get(id);
      ppl.remove(playerId);
      dbRemovePlayer(wurmId, playerId);
      if (ppl.isEmpty()) {
        objectSettings.remove(id);
      }
    }
  }
  
  private static void dbAddPlayer(long wurmId, long playerId, int settings, boolean add)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      if (add) {
        ps = dbcon.prepareStatement("INSERT INTO DOORSETTINGS (SETTINGS,WURMID,PLAYERID) VALUES(?,?,?)");
      } else {
        ps = dbcon.prepareStatement("UPDATE DOORSETTINGS SET SETTINGS=? WHERE WURMID=? AND PLAYERID=?");
      }
      ps.setInt(1, settings);
      ps.setLong(2, wurmId);
      ps.setLong(3, playerId);
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to " + (add ? "add" : "update") + " player (" + playerId + ") for door with id " + wurmId, ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private static void dbRemovePlayer(long wurmId, long playerId)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("DELETE FROM DOORSETTINGS WHERE WURMID=? AND PLAYERID=?");
      ps.setLong(1, wurmId);
      ps.setLong(2, playerId);
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to remove player " + playerId + " from settings for door " + wurmId + ".", ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public static boolean exists(long wurmId)
  {
    Long id = Long.valueOf(wurmId);
    return objectSettings.containsKey(id);
  }
  
  public static void remove(long wurmId)
  {
    Long id = Long.valueOf(wurmId);
    if (objectSettings.containsKey(id))
    {
      dbRemove(wurmId);
      objectSettings.remove(id);
    }
  }
  
  private static void dbRemove(long wurmId)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("DELETE FROM DOORSETTINGS WHERE WURMID=?");
      ps.setLong(1, wurmId);
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to delete settings for door " + wurmId + ".", ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public static PermissionsPlayerList getPermissionsPlayerList(long wurmId)
  {
    Long id = Long.valueOf(wurmId);
    PermissionsPlayerList ppl = (PermissionsPlayerList)objectSettings.get(id);
    if (ppl == null) {
      return new PermissionsPlayerList();
    }
    return ppl;
  }
  
  private static boolean hasPermission(PermissionsPlayerList.ISettings is, Creature creature, int bit)
  {
    if (is.isOwner(creature)) {
      return bit != MineDoorSettings.MinedoorPermissions.EXCLUDE.getBit();
    }
    Long id = Long.valueOf(is.getWurmId());
    PermissionsPlayerList ppl = (PermissionsPlayerList)objectSettings.get(id);
    if (ppl == null) {
      return false;
    }
    if (ppl.exists(creature.getWurmId())) {
      return ppl.getPermissionsFor(creature.getWurmId()).hasPermission(bit);
    }
    if ((is.isCitizen(creature)) && 
      (ppl.exists(-30L))) {
      return ppl.getPermissionsFor(-30L).hasPermission(bit);
    }
    if ((is.isAllied(creature)) && 
      (ppl.exists(-20L))) {
      return ppl.getPermissionsFor(-20L).hasPermission(bit);
    }
    if ((is.isSameKingdom(creature)) && 
      (ppl.exists(-40L))) {
      return ppl.getPermissionsFor(-40L).hasPermission(bit);
    }
    return (ppl.exists(-50L)) && 
      (ppl.getPermissionsFor(-50L).hasPermission(bit));
  }
  
  public static boolean isGuest(PermissionsPlayerList.ISettings is, Creature creature)
  {
    return isGuest(is, creature.getWurmId());
  }
  
  public static boolean isGuest(PermissionsPlayerList.ISettings is, long playerId)
  {
    if (is.isOwner(playerId)) {
      return true;
    }
    Long id = Long.valueOf(is.getWurmId());
    PermissionsPlayerList ppl = (PermissionsPlayerList)objectSettings.get(id);
    if (ppl == null) {
      return false;
    }
    return ppl.exists(playerId);
  }
  
  public static boolean canManage(PermissionsPlayerList.ISettings is, Creature creature)
  {
    return hasPermission(is, creature, DoorSettings.GatePermissions.MANAGE.getBit());
  }
  
  public static boolean mayPass(PermissionsPlayerList.ISettings is, Creature creature)
  {
    if (creature.getPower() > 1) {
      return true;
    }
    return hasPermission(is, creature, DoorSettings.DoorPermissions.PASS.getBit());
  }
  
  public static boolean mayLock(PermissionsPlayerList.ISettings is, Creature creature)
  {
    if (creature.getPower() > 1) {
      return true;
    }
    return hasPermission(is, creature, DoorSettings.GatePermissions.LOCK.getBit());
  }
  
  public static boolean isExcluded(PermissionsPlayerList.ISettings is, Creature creature)
  {
    if (creature.getPower() > 1) {
      return false;
    }
    return hasPermission(is, creature, DoorSettings.DoorPermissions.EXCLUDE.getBit());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\structures\DoorSettings.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */