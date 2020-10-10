package com.wurmonline.server.creatures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.Permissions.IPermission;
import com.wurmonline.server.players.PermissionsByPlayer;
import com.wurmonline.server.players.PermissionsPlayerList;
import com.wurmonline.server.players.PermissionsPlayerList.ISettings;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Village;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class AnimalSettings
  implements MiscConstants
{
  public static enum Animal0Permissions
    implements Permissions.IPermission
  {
    MANAGE(0, "Manage Item", "Manage", "Item", "Allows managing of these permissions."),  COMMANDER(1, "Commander", "Can", "Lead", "Allows leading of this animal."),  ACCESS_HOLD(3, "Manage Equipment", "Manage", "Equipment", "Allows adding or removing equipement from the animal without taming it."),  EXCLUDE(15, "Deny All", "Deny", "All", "Deny all access.");
    
    final byte bit;
    final String description;
    final String header1;
    final String header2;
    final String hover;
    
    private Animal0Permissions(int aBit, String aDescription, String aHeader1, String aHeader2, String aHover)
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
  
  public static enum Animal1Permissions
    implements Permissions.IPermission
  {
    MANAGE(0, "Manage Item", "Manage", "Item", "Allows managing of these permissions."),  COMMANDER(1, "Commander", "Can", "Ride", "Allows leading and riding of this animal."),  ACCESS_HOLD(3, "Manage Equipment", "Manage", "Equipment", "Allows adding or removing equipement from the animal without taming it."),  EXCLUDE(15, "Deny All", "Deny", "All", "Deny all access.");
    
    final byte bit;
    final String description;
    final String header1;
    final String header2;
    final String hover;
    
    private Animal1Permissions(int aBit, String aDescription, String aHeader1, String aHeader2, String aHover)
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
  
  public static enum Animal2Permissions
    implements Permissions.IPermission
  {
    MANAGE(0, "Manage Item", "Manage", "Item", "Allows managing of these permissions."),  COMMANDER(1, "Commander", "Can", "Ride", "Allows leading and riding of this animal."),  PASSENGER(2, "Passenger", "Can be", "Passenger", "Allows being a passenger on this animal."),  ACCESS_HOLD(3, "Manage Equipment", "Manage", "Equipment", "Allows adding or removing equipement from the animal without taming it."),  EXCLUDE(15, "Deny All", "Deny", "All", "Deny all access.");
    
    final byte bit;
    final String description;
    final String header1;
    final String header2;
    final String hover;
    
    private Animal2Permissions(int aBit, String aDescription, String aHeader1, String aHeader2, String aHover)
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
  
  public static enum WagonerPermissions
    implements Permissions.IPermission
  {
    MANAGE(0, "Manage Item", "Manage", "Item", "Allows managing of these permissions."),  CANUSE(6, "Can Use", "Can", "Use", "Allows sending of bulk items using this NPC."),  EXCLUDE(15, "Deny All", "Deny", "All", "Deny all access.");
    
    final byte bit;
    final String description;
    final String header1;
    final String header2;
    final String hover;
    
    private WagonerPermissions(int aBit, String aDescription, String aHeader1, String aHeader2, String aHover)
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
  
  private static final Logger logger = Logger.getLogger(AnimalSettings.class.getName());
  private static final String GET_ALL_SETTINGS = "SELECT * FROM ANIMALSETTINGS";
  private static final String ADD_PLAYER = "INSERT INTO ANIMALSETTINGS (SETTINGS,WURMID,PLAYERID) VALUES(?,?,?)";
  private static final String DELETE_SETTINGS = "DELETE FROM ANIMALSETTINGS WHERE WURMID=?";
  private static final String REMOVE_PLAYER = "DELETE FROM ANIMALSETTINGS WHERE WURMID=? AND PLAYERID=?";
  private static final String UPDATE_PLAYER = "UPDATE ANIMALSETTINGS SET SETTINGS=? WHERE WURMID=? AND PLAYERID=?";
  private static int MAX_PLAYERS_PER_OBJECT = 1000;
  private static Map<Long, PermissionsPlayerList> objectSettings = new ConcurrentHashMap();
  
  public static void loadAll()
    throws IOException
  {
    logger.log(Level.INFO, "Loading all animal settings.");
    long start = System.nanoTime();
    long count = 0L;
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getCreatureDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM ANIMALSETTINGS");
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
      logger.log(Level.WARNING, "Failed to load settings for animals.", ex);
    }
    finally
    {
      long end;
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
      long end = System.nanoTime();
      logger.log(Level.INFO, "Loaded " + count + " animal settings. That took " + (float)(end - start) / 1000000.0F + " ms.");
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
      logger.log(Level.WARNING, "Failed to remove player " + playerId + " from settings for animal " + wurmId + ".");
    }
  }
  
  private static void dbAddPlayer(long wurmId, long playerId, int settings, boolean add)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getCreatureDbCon();
      if (add) {
        ps = dbcon.prepareStatement("INSERT INTO ANIMALSETTINGS (SETTINGS,WURMID,PLAYERID) VALUES(?,?,?)");
      } else {
        ps = dbcon.prepareStatement("UPDATE ANIMALSETTINGS SET SETTINGS=? WHERE WURMID=? AND PLAYERID=?");
      }
      ps.setInt(1, settings);
      ps.setLong(2, wurmId);
      ps.setLong(3, playerId);
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to " + (add ? "add" : "update") + " player (" + playerId + ") for animal with id " + wurmId, ex);
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
      dbcon = DbConnector.getCreatureDbCon();
      ps = dbcon.prepareStatement("DELETE FROM ANIMALSETTINGS WHERE WURMID=? AND PLAYERID=?");
      ps.setLong(1, wurmId);
      ps.setLong(2, playerId);
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to remove player " + playerId + " from settings for animal " + wurmId + ".", ex);
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
      dbcon = DbConnector.getCreatureDbCon();
      ps = dbcon.prepareStatement("DELETE FROM ANIMALSETTINGS WHERE WURMID=?");
      ps.setLong(1, wurmId);
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to delete settings for animal " + wurmId + ".", ex);
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
  
  private static boolean hasPermission(PermissionsPlayerList.ISettings is, Creature creature, @Nullable Village brandVillage, int bit)
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
    if ((brandVillage != null) && (brandVillage.isActionAllowed((short)484, creature)) && 
      (ppl.exists(-60L))) {
      return ppl.getPermissionsFor(-60L).hasPermission(bit);
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
  
  public static boolean canManage(PermissionsPlayerList.ISettings is, Creature creature, @Nullable Village brandVillage)
  {
    return hasPermission(is, creature, brandVillage, Animal2Permissions.MANAGE.getBit());
  }
  
  public static boolean mayCommand(PermissionsPlayerList.ISettings is, Creature creature, @Nullable Village brandVillage)
  {
    if (creature.getPower() > 1) {
      return true;
    }
    return hasPermission(is, creature, brandVillage, Animal2Permissions.COMMANDER.getBit());
  }
  
  public static boolean mayPassenger(PermissionsPlayerList.ISettings is, Creature creature, @Nullable Village brandVillage)
  {
    if (creature.getPower() > 1) {
      return true;
    }
    return hasPermission(is, creature, brandVillage, Animal2Permissions.PASSENGER.getBit());
  }
  
  public static boolean mayAccessHold(PermissionsPlayerList.ISettings is, Creature creature, @Nullable Village brandVillage)
  {
    if (creature.getPower() > 1) {
      return true;
    }
    return hasPermission(is, creature, brandVillage, Animal2Permissions.ACCESS_HOLD.getBit());
  }
  
  public static boolean mayUse(PermissionsPlayerList.ISettings is, Creature creature, @Nullable Village brandVillage)
  {
    if (creature.getPower() > 1) {
      return true;
    }
    return hasPermission(is, creature, brandVillage, WagonerPermissions.CANUSE.getBit());
  }
  
  public static boolean publicMayUse(PermissionsPlayerList.ISettings is)
  {
    Long id = Long.valueOf(is.getWurmId());
    PermissionsPlayerList ppl = (PermissionsPlayerList)objectSettings.get(id);
    if (ppl == null) {
      return false;
    }
    return (ppl.exists(-50L)) && 
      (ppl.getPermissionsFor(-50L).hasPermission(WagonerPermissions.CANUSE.getBit()));
  }
  
  public static boolean isExcluded(PermissionsPlayerList.ISettings is, Creature creature)
  {
    if (creature.getPower() > 1) {
      return false;
    }
    return hasPermission(is, creature, null, Animal2Permissions.EXCLUDE.getBit());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\creatures\AnimalSettings.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */