package com.wurmonline.server.structures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.MineDoorSettings.MinedoorPermissions;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.Permissions.IPermission;
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

public final class StructureSettings
  implements MiscConstants
{
  public static enum StructurePermissions
    implements Permissions.IPermission
  {
    MANAGE(0, "Manage Item", "Manage", "Item", "Allows managing of these permissions."),  PASS(1, "May Enter", "May", "Enter", "Allows entry even through locked doors (if door is Controlled By Building) and the abililty to improve and repair the building and items inside."),  MODIFY(2, "Modify Building", "Modify", "Building", "Allows destroying, building and rotating of floors, roofs and walls - needs deed permissions to add or remove tiles."),  PICKUP(3, "Pickup Items", "Pickup", "Items", "Allows picking up of items and Pull/Push/Turn (overrides deed setting), also allows Hauling Up and Down of items."),  PICKUP_PLANTED(4, "Pickup Planted", "Pickup", "Planted", "Allows picking up of planted items (overrides deed setting). Requires 'Pickup Items' as well."),  PLACE_MERCHANTS(5, "Place Merchants", "Place", "Merchants", "Allows planting of merchants and traders (overrides deed setting)."),  LOAD(6, "May Load", "May", "(Un)Load", "Allows (Un)loading of items (overrides deed setting). Requires 'Pickup Items' to load items they dont own, will also requires 'Pickup Planted' if item is planted."),  EXCLUDE(15, "Deny All", "Deny", "All", "Deny all access.");
    
    private final byte bit;
    private final String description;
    private final String header1;
    private final String header2;
    private final String hover;
    
    private StructurePermissions(int aBit, String aDescription, String aHeader1, String aHeader2, String aHover)
    {
      this.bit = ((byte)aBit);
      this.description = aDescription;
      this.header1 = aHeader1;
      this.header2 = aHeader2;
      this.hover = aHover;
    }
    
    public byte getBit()
    {
      return this.bit;
    }
    
    public int getValue()
    {
      return 1 << this.bit;
    }
    
    public String getDescription()
    {
      return this.description;
    }
    
    public String getHeader1()
    {
      return this.header1;
    }
    
    public String getHeader2()
    {
      return this.header2;
    }
    
    public String getHover()
    {
      return this.hover;
    }
    
    private static final Permissions.IPermission[] types = values();
    
    public static Permissions.IPermission[] getPermissions()
    {
      return types;
    }
  }
  
  private static final Logger logger = Logger.getLogger(StructureSettings.class.getName());
  private static final String GET_ALL_SETTINGS = "SELECT * FROM STRUCTUREGUESTS";
  private static final String ADD_PLAYER = "INSERT INTO STRUCTUREGUESTS (SETTINGS,STRUCTUREID,GUESTID) VALUES(?,?,?)";
  private static final String DELETE_SETTINGS = "DELETE FROM STRUCTUREGUESTS WHERE STRUCTUREID=?";
  private static final String REMOVE_PLAYER = "DELETE FROM STRUCTUREGUESTS WHERE STRUCTUREID=? AND GUESTID=?";
  private static final String UPDATE_PLAYER = "UPDATE STRUCTUREGUESTS SET SETTINGS=? WHERE STRUCTUREID=? AND GUESTID=?";
  private static int MAX_PLAYERS_PER_OBJECT = 1000;
  private static Map<Long, PermissionsPlayerList> objectSettings = new ConcurrentHashMap();
  
  public static void loadAll()
    throws IOException
  {
    logger.log(Level.INFO, "Loading all structure settings.");
    long start = System.nanoTime();
    long count = 0L;
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM STRUCTUREGUESTS");
      rs = ps.executeQuery();
      while (rs.next())
      {
        long wurmId = rs.getLong("STRUCTUREID");
        long guestId = rs.getLong("GUESTID");
        int settings = rs.getInt("SETTINGS");
        if (settings == 0) {
          settings = StructurePermissions.PASS.getValue() + StructurePermissions.LOAD.getValue() + StructurePermissions.MODIFY.getValue() + StructurePermissions.PICKUP.getValue() + StructurePermissions.PLACE_MERCHANTS.getValue();
        }
        add(wurmId, guestId, settings);
        count += 1L;
      }
    }
    catch (SQLException ex)
    {
      long end;
      logger.log(Level.WARNING, "Failed to load settings for structures.", ex);
    }
    finally
    {
      long end;
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
      long end = System.nanoTime();
      logger.log(Level.INFO, "Loaded " + count + " structure settings (guests). That took " + (float)(end - start) / 1000000.0F + " ms.");
    }
  }
  
  public static int getMaxAllowed()
  {
    return Servers.isThisATestServer() ? 10 : MAX_PLAYERS_PER_OBJECT;
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
    else
    {
      logger.log(Level.WARNING, "Failed to remove player " + playerId + " from settings for structure " + wurmId + ".");
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
        ps = dbcon.prepareStatement("INSERT INTO STRUCTUREGUESTS (SETTINGS,STRUCTUREID,GUESTID) VALUES(?,?,?)");
      } else {
        ps = dbcon.prepareStatement("UPDATE STRUCTUREGUESTS SET SETTINGS=? WHERE STRUCTUREID=? AND GUESTID=?");
      }
      ps.setInt(1, settings);
      ps.setLong(2, wurmId);
      ps.setLong(3, playerId);
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to " + (add ? "add" : "update") + " player (" + playerId + ") for structure with id " + wurmId, ex);
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
      ps = dbcon.prepareStatement("DELETE FROM STRUCTUREGUESTS WHERE STRUCTUREID=? AND GUESTID=?");
      ps.setLong(1, wurmId);
      ps.setLong(2, playerId);
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to remove player " + playerId + " from settings for structure " + wurmId + ".", ex);
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
      ps = dbcon.prepareStatement("DELETE FROM STRUCTUREGUESTS WHERE STRUCTUREID=?");
      ps.setLong(1, wurmId);
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to delete settings for structure " + wurmId + ".", ex);
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
    if (ppl.exists(playerId)) {
      return !ppl.getPermissionsFor(playerId).hasPermission(StructurePermissions.EXCLUDE.getBit());
    }
    return false;
  }
  
  public static boolean canManage(PermissionsPlayerList.ISettings is, Creature creature)
  {
    return hasPermission(is, creature, StructurePermissions.MANAGE.getBit());
  }
  
  public static boolean mayModify(PermissionsPlayerList.ISettings is, Creature creature)
  {
    if (creature.getPower() > 1) {
      return true;
    }
    return hasPermission(is, creature, StructurePermissions.MODIFY.getBit());
  }
  
  public static boolean mayPass(PermissionsPlayerList.ISettings is, Creature creature)
  {
    if (creature.getPower() > 1) {
      return true;
    }
    return hasPermission(is, creature, StructurePermissions.PASS.getBit());
  }
  
  public static boolean mayPickup(PermissionsPlayerList.ISettings is, Creature creature)
  {
    if (creature.getPower() > 1) {
      return true;
    }
    return hasPermission(is, creature, StructurePermissions.PICKUP.getBit());
  }
  
  public static boolean mayPickupPlanted(PermissionsPlayerList.ISettings is, Creature creature)
  {
    if (creature.getPower() > 1) {
      return true;
    }
    return hasPermission(is, creature, StructurePermissions.PICKUP_PLANTED.getBit());
  }
  
  public static boolean mayPlaceMerchants(PermissionsPlayerList.ISettings is, Creature creature)
  {
    if (creature.getPower() > 1) {
      return true;
    }
    return hasPermission(is, creature, StructurePermissions.PLACE_MERCHANTS.getBit());
  }
  
  public static boolean mayLoad(PermissionsPlayerList.ISettings is, Creature creature)
  {
    if (creature.getPower() > 1) {
      return true;
    }
    return hasPermission(is, creature, StructurePermissions.LOAD.getBit());
  }
  
  public static boolean isExcluded(PermissionsPlayerList.ISettings is, Creature creature)
  {
    if (creature.getPower() > 1) {
      return false;
    }
    return hasPermission(is, creature, StructurePermissions.EXCLUDE.getBit());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\structures\StructureSettings.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */