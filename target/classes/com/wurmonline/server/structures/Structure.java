package com.wurmonline.server.structures;

import com.wurmonline.math.TilePos;
import com.wurmonline.server.Constants;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.MethodsStructure;
import com.wurmonline.server.creatures.AnimalSettings;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.Permissions.Allow;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.players.PermissionsPlayerList;
import com.wurmonline.server.players.PermissionsPlayerList.ISettings;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.tutorial.MissionTargets;
import com.wurmonline.server.utils.CoordUtils;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageRole;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.BridgeConstants.BridgeState;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.StructureConstants;
import com.wurmonline.shared.constants.StructureConstants.FloorType;
import com.wurmonline.shared.constants.StructureTypeEnum;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Structure
  implements MiscConstants, CounterTypes, TimeConstants, StructureConstants, PermissionsPlayerList.ISettings
{
  private static Logger logger = Logger.getLogger(Structure.class.getName());
  private long wurmId;
  Set<VolaTile> structureTiles;
  Set<BuildTile> buildTiles = new HashSet();
  int minX = 1 << Constants.meshSize;
  int maxX = 0;
  int minY = 1 << Constants.meshSize;
  int maxY = 0;
  protected boolean surfaced;
  private long creationDate;
  private byte roof;
  private String name;
  private boolean isLoading = false;
  private boolean hasLoaded = false;
  boolean finished = false;
  Set<Door> doors;
  long writid = -10L;
  boolean finalfinished = false;
  boolean allowsVillagers = false;
  boolean allowsAllies = false;
  boolean allowsKingdom = false;
  private String planner = "";
  long ownerId = -10L;
  private Permissions permissions = new Permissions();
  int villageId = -1;
  private byte structureType = 0;
  private long lastPolled = System.currentTimeMillis();
  public static final float DAMAGE_STATE_DIVIDER = 60.0F;
  
  Structure(byte theStructureType, String aName, long aId, int aStartX, int aStartY, boolean aSurfaced)
  {
    this.structureType = theStructureType;
    this.wurmId = aId;
    this.name = aName;
    this.structureTiles = new HashSet();
    if (aStartX > this.maxX) {
      this.maxX = aStartX;
    }
    if (aStartX < this.minX) {
      this.minX = aStartX;
    }
    if (aStartY > this.maxY) {
      this.maxY = aStartY;
    }
    if (aStartY < this.minY) {
      this.minY = aStartY;
    }
    this.surfaced = aSurfaced;
    this.creationDate = System.currentTimeMillis();
    try
    {
      Zone zone = Zones.getZone(aStartX, aStartY, aSurfaced);
      VolaTile tile = zone.getOrCreateTile(aStartX, aStartY);
      this.structureTiles.add(tile);
      if (theStructureType == 0)
      {
        tile.addBuildMarker(this);
        clearAllWallsAndMakeWallsForStructureBorder(tile);
      }
      else
      {
        tile.addStructure(this);
      }
    }
    catch (NoSuchZoneException nsz)
    {
      logger.log(Level.WARNING, "No such zone: " + aStartX + ", " + aStartY + ", StructureId: " + this.wurmId, nsz);
    }
  }
  
  Structure(byte theStructureType, String aName, long aId, boolean aIsSurfaced, byte _roof, boolean _finished, boolean finFinished, long _writid, String aPlanner, long aOwnerId, int aSettings, int aVillageId, boolean allowsCitizens, boolean allowAllies, boolean allowKingdom)
  {
    this.structureType = theStructureType;
    this.wurmId = aId;
    this.writid = _writid;
    this.name = aName;
    this.structureTiles = new HashSet();
    this.surfaced = aIsSurfaced;
    this.roof = _roof;
    this.finished = _finished;
    this.finalfinished = finFinished;
    this.allowsVillagers = allowsCitizens;
    this.allowsAllies = allowAllies;
    this.allowsKingdom = allowKingdom;
    this.planner = aPlanner;
    this.ownerId = aOwnerId;
    setSettings(aSettings);
    this.villageId = aVillageId;
    setMaxAndMin();
  }
  
  Structure(long id)
    throws IOException, NoSuchStructureException
  {
    this.wurmId = id;
    this.structureTiles = new HashSet();
    
    load();
    setMaxAndMin();
  }
  
  public final void addBuildTile(BuildTile toadd)
  {
    this.buildTiles.add(toadd);
  }
  
  public final void clearBuildTiles()
  {
    this.buildTiles.clear();
  }
  
  public final String getName()
  {
    return this.name;
  }
  
  public final void setPlanner(String newPlanner)
  {
    this.planner = newPlanner;
  }
  
  public final String getPlanner()
  {
    return this.planner;
  }
  
  final void setSettings(int newSettings)
  {
    this.permissions.setPermissionBits(newSettings);
  }
  
  public final Permissions getSettings()
  {
    return this.permissions;
  }
  
  public final void setName(String aName, boolean saveIt)
  {
    this.name = aName.substring(0, Math.min(255, aName.length()));
    VolaTile[] vtiles = getStructureTiles();
    for (int x = 0; x < vtiles.length; x++) {
      vtiles[x].changeStructureName(aName);
    }
    if (saveIt) {
      try
      {
        saveName();
      }
      catch (IOException e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public final String getTypeName()
  {
    return "Building";
  }
  
  public final String getObjectName()
  {
    return this.name;
  }
  
  public final boolean setObjectName(String newName, Creature creature)
  {
    if (this.writid != -10L) {
      try
      {
        Item writ = Items.getItem(getWritId());
        if (writ.getOwnerId() != creature.getWurmId()) {
          return false;
        }
        writ.setDescription(newName);
      }
      catch (NoSuchItemException nsi)
      {
        this.writid = -10L;
        try
        {
          saveWritId();
        }
        catch (IOException e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
    setName(newName, false);
    return true;
  }
  
  final void setRoof(byte aRoof)
  {
    this.roof = aRoof;
  }
  
  public final byte getRoof()
  {
    return this.roof;
  }
  
  public final long getOwnerId()
  {
    if (this.writid != -10L) {
      try
      {
        Item writ = Items.getItem(this.writid);
        if (this.ownerId != writ.getOwnerId())
        {
          this.ownerId = writ.getOwnerId();
          saveOwnerId();
        }
        return writ.getOwnerId();
      }
      catch (NoSuchItemException nsi)
      {
        setWritid(-10L, true);
      }
      catch (IOException ioe)
      {
        logger.log(Level.WARNING, ioe.getMessage(), ioe);
      }
    }
    if ((this.ownerId == -10L) && (this.planner.length() > 0))
    {
      PlayerInfo pInfo = PlayerInfoFactory.getPlayerInfoWithName(this.planner);
      if (pInfo != null)
      {
        this.ownerId = pInfo.wurmId;
        try
        {
          saveOwnerId();
        }
        catch (IOException ioe)
        {
          logger.log(Level.WARNING, ioe.getMessage(), ioe);
        }
      }
    }
    return this.ownerId;
  }
  
  final void setOwnerId(long newOwnerId)
  {
    this.ownerId = newOwnerId;
  }
  
  public final int getVillageId()
  {
    return this.villageId;
  }
  
  public final void setVillageId(int newVillageId)
  {
    this.villageId = newVillageId;
  }
  
  public final Village getManagedByVillage()
  {
    if (this.villageId >= 0) {
      try
      {
        return Villages.getVillage(this.villageId);
      }
      catch (NoSuchVillageException localNoSuchVillageException) {}
    }
    return null;
  }
  
  public final long getWritId()
  {
    return this.writid;
  }
  
  public boolean isEnemy(Creature creature)
  {
    if (creature.getPower() > 1) {
      return false;
    }
    if (isGuest(creature)) {
      return false;
    }
    Village vil = getPermissionsVillage();
    if (vil != null) {
      return vil.isEnemy(creature);
    }
    if (!isSameKingdom(creature)) {
      return true;
    }
    return false;
  }
  
  public boolean isEnemyAllowed(Creature creature, short action)
  {
    Village v = getVillage();
    if ((v != null) && (Actions.actionEntrys[action] != null))
    {
      if ((Actions.actionEntrys[action].isEnemyAllowedWhenNoGuards()) && (v.getGuards().length != 0)) {
        return false;
      }
      if (Actions.actionEntrys[action].isEnemyNeverAllowed()) {
        return false;
      }
      if (Actions.actionEntrys[action].isEnemyAlwaysAllowed()) {
        return true;
      }
    }
    return true;
  }
  
  public boolean mayLockPick(Creature creature)
  {
    if ((Servers.isThisAPvpServer()) && (isEnemyAllowed(creature, (short)101))) {
      return true;
    }
    Village v = getManagedByVillage() == null ? getVillage() : getManagedByVillage();
    if (v != null) {
      return v.getRoleFor(creature).mayPickLocks();
    }
    return (mayManage(creature)) || (Servers.isThisAPvpServer());
  }
  
  public boolean isCitizen(Creature creature)
  {
    Village vil = getPermissionsVillage();
    if (vil != null) {
      return vil.isCitizen(creature);
    }
    return false;
  }
  
  public boolean isActionAllowed(Creature performer, short action)
  {
    if (performer.getPower() > 1) {
      return true;
    }
    if ((isEnemy(performer)) && (!isEnemyAllowed(performer, action))) {
      return false;
    }
    if (Actions.isActionAttachLock(action)) {
      return (isEnemy(performer)) || (mayManage(performer));
    }
    if (Actions.isActionChangeBuilding(action)) {
      return mayManage(performer);
    }
    if (Actions.isActionLockPick(action)) {
      return mayLockPick(performer);
    }
    if ((Actions.isActionTake(action)) || 
      (Actions.isActionPullPushTurn(action)) || (671 == action) || (672 == action) || 
      
      (Actions.isActionPick(action))) {
      return (isEnemy(performer)) || (mayPickup(performer));
    }
    if (Actions.isActionPickupPlanted(action)) {
      return mayPickupPlanted(performer);
    }
    if (Actions.isActionPlaceMerchants(action)) {
      return mayPlaceMerchants(performer);
    }
    if ((Actions.isActionDestroy(action)) || 
      (Actions.isActionBuild(action)) || 
      (Actions.isActionDestroyFence(action)) || 
      (Actions.isActionPlanBuilding(action)) || 
      (Actions.isActionPack(action)) || 
      (Actions.isActionPave(action)) || 
      (Actions.isActionDestroyItem(action))) {
      return (isEnemy(performer)) || (mayModify(performer));
    }
    if ((Actions.isActionLoad(action)) || (Actions.isActionUnload(action))) {
      return mayLoad(performer);
    }
    if ((Actions.isActionDrop(action)) && (isEnemy(performer))) {
      return true;
    }
    if ((Actions.isActionImproveOrRepair(action)) || 
      (Actions.isActionDrop(action))) {
      return (isEnemy(performer)) || (mayPass(performer));
    }
    return true;
  }
  
  public boolean isAllied(Creature creature)
  {
    Village vil = getPermissionsVillage();
    if (vil != null) {
      return vil.isAlly(creature);
    }
    return false;
  }
  
  public boolean isSameKingdom(Creature creature)
  {
    Village vill = getPermissionsVillage();
    if (vill != null) {
      return vill.kingdom == creature.getKingdomId();
    }
    return Players.getInstance().getKingdomForPlayer(getOwnerId()) == creature.getKingdomId();
  }
  
  public Village getVillage()
  {
    Village village = Villages.getVillage(getMinX(), getMinY(), isSurfaced());
    if (village == null) {
      return null;
    }
    Village v = Villages.getVillage(getMinX(), getMaxY(), isSurfaced());
    if ((v == null) || (v.getId() != village.getId())) {
      return null;
    }
    v = Villages.getVillage(getMaxX(), getMaxY(), isSurfaced());
    if ((v == null) || (v.getId() != village.getId())) {
      return null;
    }
    v = Villages.getVillage(getMinX(), getMinY(), isSurfaced());
    if ((v == null) || (v.getId() != village.getId())) {
      return null;
    }
    return village;
  }
  
  private Village getPermissionsVillage()
  {
    Village vill = getManagedByVillage();
    if (vill != null) {
      return vill;
    }
    long wid = getOwnerId();
    if (wid != -10L) {
      return Villages.getVillageForCreature(wid);
    }
    return null;
  }
  
  private String getVillageName(Player player)
  {
    String sName = "";
    Village vill = getVillage();
    if (vill != null) {
      sName = vill.getName();
    } else {
      sName = player.getVillageName();
    }
    return sName;
  }
  
  public boolean canChangeName(Creature creature)
  {
    return (creature.getPower() > 1) || (isOwner(creature.getWurmId()));
  }
  
  public boolean canChangeOwner(Creature creature)
  {
    return ((isActualOwner(creature.getWurmId())) || (creature.getPower() > 1)) && (this.writid == -10L);
  }
  
  public String getWarning()
  {
    if (!isFinished()) {
      return "NEEDS TO BE COMPLETE FOR INTERIOR PERMISSIONS TO WORK";
    }
    return "";
  }
  
  public PermissionsPlayerList getPermissionsPlayerList()
  {
    return StructureSettings.getPermissionsPlayerList(getWurmId());
  }
  
  public Floor[] getFloors()
  {
    Set<Floor> floors = new HashSet();
    for (VolaTile tile : this.structureTiles)
    {
      Floor[] fArr = tile.getFloors();
      for (int x = 0; x < fArr.length; x++) {
        floors.add(fArr[x]);
      }
    }
    Floor[] toReturn = new Floor[floors.size()];
    return (Floor[])floors.toArray(toReturn);
  }
  
  public Floor[] getFloorsAtTile(int tilex, int tiley, int offsetHeightStart, int offsetHeightEnd)
  {
    Set<Floor> floors = new HashSet();
    for (VolaTile tile : this.structureTiles) {
      if ((tile.getTileX() == tilex) && (tile.getTileY() == tiley))
      {
        Floor[] fArr = tile.getFloors(offsetHeightStart, offsetHeightEnd);
        for (int x = 0; x < fArr.length; x++) {
          floors.add(fArr[x]);
        }
      }
    }
    Floor[] toReturn = new Floor[floors.size()];
    return (Floor[])floors.toArray(toReturn);
  }
  
  public final Wall[] getWalls()
  {
    Set<Wall> walls = new HashSet();
    for (VolaTile tile : this.structureTiles)
    {
      Wall[] wArr = tile.getWalls();
      for (int x = 0; x < wArr.length; x++) {
        walls.add(wArr[x]);
      }
    }
    Wall[] toReturn = new Wall[walls.size()];
    return (Wall[])walls.toArray(toReturn);
  }
  
  public final Wall[] getExteriorWalls()
  {
    Set<Wall> walls = new HashSet();
    for (VolaTile tile : this.structureTiles)
    {
      Wall[] wArr = tile.getExteriorWalls();
      for (int x = 0; x < wArr.length; x++) {
        walls.add(wArr[x]);
      }
    }
    Wall[] toReturn = new Wall[walls.size()];
    return (Wall[])walls.toArray(toReturn);
  }
  
  public BridgePart[] getBridgeParts()
  {
    Set<BridgePart> bridgeParts = new HashSet();
    for (VolaTile tile : this.structureTiles)
    {
      BridgePart[] fArr = tile.getBridgeParts();
      for (int x = 0; x < fArr.length; x++) {
        bridgeParts.add(fArr[x]);
      }
    }
    BridgePart[] toReturn = new BridgePart[bridgeParts.size()];
    return (BridgePart[])bridgeParts.toArray(toReturn);
  }
  
  public final VolaTile getTileFor(Wall wall)
  {
    for (int xx = 1; xx >= -1; xx--) {
      for (int yy = 1; yy >= -1; yy--) {
        try
        {
          Zone zone = Zones.getZone(wall.tilex + xx, wall.tiley + yy, this.surfaced);
          VolaTile tile = zone.getTileOrNull(wall.tilex + xx, wall.tiley + yy);
          if (tile != null)
          {
            Wall[] walls = tile.getWalls();
            for (int s = 0; s < walls.length; s++) {
              if (walls[s] == wall) {
                return tile;
              }
            }
          }
        }
        catch (NoSuchZoneException localNoSuchZoneException) {}
      }
    }
    return null;
  }
  
  public final void poll(long time)
  {
    if (time - this.lastPolled > 3600000L)
    {
      this.lastPolled = System.currentTimeMillis();
      if (!isFinalized())
      {
        if (time - this.creationDate > 10800000L)
        {
          logger.log(Level.INFO, "Deleting unfinished structure " + getName());
          totallyDestroy();
        }
      }
      else
      {
        boolean destroy = false;
        if (time - this.creationDate > 172800000L)
        {
          destroy = true;
          if (this.structureType == 0)
          {
            if (hasWalls()) {
              destroy = false;
            }
          }
          else if (getBridgeParts().length != 0) {
            destroy = false;
          }
        }
        if (destroy) {
          totallyDestroy();
        }
      }
    }
  }
  
  public final boolean hasWalls()
  {
    for (Iterator<VolaTile> it = this.structureTiles.iterator(); it.hasNext();)
    {
      VolaTile tile = (VolaTile)it.next();
      Wall[] wallArr = tile.getWalls();
      for (int x = 0; x < wallArr.length; x++) {
        if (wallArr[x].getType() != StructureTypeEnum.PLAN) {
          return true;
        }
      }
    }
    return false;
  }
  
  public final void totallyDestroy()
  {
    Players.getInstance().setStructureFinished(this.wurmId);
    if (isFinalized())
    {
      if ((getWritId() != -10L) || (this.structureType != 1)) {
        try
        {
          Item writ = Items.getItem(getWritId());
          try
          {
            Server.getInstance().getCreature(writ.getOwnerId());
            Items.destroyItem(getWritId());
          }
          catch (NoSuchCreatureException nsc)
          {
            Items.decay(getWritId(), null);
          }
          catch (NoSuchPlayerException nsp)
          {
            Items.decay(getWritId(), null);
          }
        }
        catch (NoSuchItemException localNoSuchItemException) {}
      }
      if (this.structureType == 0) {
        for (VolaTile vt : this.structureTiles)
        {
          VolaTile vtNorth = Zones.getTileOrNull(vt.getTileX(), vt.getTileY() - 1, vt.isOnSurface());
          if (vtNorth != null)
          {
            Structure structNorth = vtNorth.getStructure();
            if ((structNorth != null) && (structNorth.isTypeBridge()))
            {
              BridgePart[] bps = vtNorth.getBridgeParts();
              if ((bps.length == 1) && (bps[0].hasHouseSouthExit())) {
                structNorth.totallyDestroy();
              }
            }
          }
          VolaTile vtEast = Zones.getTileOrNull(vt.getTileX() + 1, vt.getTileY(), vt.isOnSurface());
          if (vtEast != null)
          {
            Structure structEast = vtEast.getStructure();
            if ((structEast != null) && (structEast.isTypeBridge()))
            {
              BridgePart[] bps = vtEast.getBridgeParts();
              if ((bps.length == 1) && (bps[0].hasHouseWestExit())) {
                structEast.totallyDestroy();
              }
            }
          }
          VolaTile vtSouth = Zones.getTileOrNull(vt.getTileX(), vt.getTileY() + 1, vt.isOnSurface());
          if (vtSouth != null)
          {
            Structure structSouth = vtSouth.getStructure();
            if ((structSouth != null) && (structSouth.isTypeBridge()))
            {
              BridgePart[] bps = vtSouth.getBridgeParts();
              if ((bps.length == 1) && (bps[0].hasHouseNorthExit())) {
                structSouth.totallyDestroy();
              }
            }
          }
          VolaTile vtWest = Zones.getTileOrNull(vt.getTileX() - 1, vt.getTileY(), vt.isOnSurface());
          if (vtWest != null)
          {
            Structure structWest = vtWest.getStructure();
            if ((structWest != null) && (structWest.isTypeBridge()))
            {
              BridgePart[] bps = vtWest.getBridgeParts();
              if ((bps.length == 1) && (bps[0].hasHouseEastExit())) {
                structWest.totallyDestroy();
              }
            }
          }
        }
      }
      MissionTargets.destroyStructureTargets(getWurmId(), null);
    }
    for (Iterator<VolaTile> it = this.structureTiles.iterator(); it.hasNext();)
    {
      VolaTile tile = (VolaTile)it.next();
      
      tile.deleteStructure(getWurmId());
    }
    remove();
    delete();
  }
  
  public final boolean hasBridgeEntrance()
  {
    for (VolaTile vt : this.structureTiles) {
      if (vt.isOnSurface())
      {
        VolaTile vtNorth = Zones.getTileOrNull(vt.getTileX(), vt.getTileY() - 1, vt.isOnSurface());
        if (vtNorth != null)
        {
          Structure structNorth = vtNorth.getStructure();
          if ((structNorth != null) && (structNorth.isTypeBridge()))
          {
            BridgePart[] bps = vtNorth.getBridgeParts();
            if ((bps.length == 1) && (bps[0].hasHouseSouthExit())) {
              return true;
            }
          }
        }
        VolaTile vtEast = Zones.getTileOrNull(vt.getTileX() + 1, vt.getTileY(), vt.isOnSurface());
        if (vtEast != null)
        {
          Structure structEast = vtEast.getStructure();
          if ((structEast != null) && (structEast.isTypeBridge()))
          {
            BridgePart[] bps = vtEast.getBridgeParts();
            if ((bps.length == 1) && (bps[0].hasHouseWestExit())) {
              return true;
            }
          }
        }
        VolaTile vtSouth = Zones.getTileOrNull(vt.getTileX(), vt.getTileY() + 1, vt.isOnSurface());
        if (vtSouth != null)
        {
          Structure structSouth = vtSouth.getStructure();
          if ((structSouth != null) && (structSouth.isTypeBridge()))
          {
            BridgePart[] bps = vtSouth.getBridgeParts();
            if ((bps.length == 1) && (bps[0].hasHouseNorthExit())) {
              return true;
            }
          }
        }
        VolaTile vtWest = Zones.getTileOrNull(vt.getTileX() - 1, vt.getTileY(), vt.isOnSurface());
        if (vtWest != null)
        {
          Structure structWest = vtWest.getStructure();
          if ((structWest != null) && (structWest.isTypeBridge()))
          {
            BridgePart[] bps = vtWest.getBridgeParts();
            if ((bps.length == 1) && (bps[0].hasHouseEastExit())) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }
  
  public final void remove()
  {
    if (this.structureTiles.size() > 0)
    {
      Zone[] zones = Zones.getZonesCoveredBy(this.minX, this.minY, this.maxX, this.maxY, this.surfaced);
      for (int x = 0; x < zones.length; x++) {
        zones[x].removeStructure(this);
      }
    }
    Structures.removeStructure(this.wurmId);
  }
  
  public final boolean canHavePermissions()
  {
    return true;
  }
  
  public final boolean mayShowPermissions(Creature creature)
  {
    return mayManage(creature);
  }
  
  public final boolean canManage(Creature creature)
  {
    if (StructureSettings.isExcluded(this, creature)) {
      return false;
    }
    if (StructureSettings.canManage(this, creature)) {
      return true;
    }
    if (creature.getCitizenVillage() == null) {
      return false;
    }
    Village vill = getManagedByVillage();
    if (vill == null) {
      return false;
    }
    if (!vill.isCitizen(creature)) {
      return false;
    }
    return vill.isActionAllowed((short)664, creature);
  }
  
  public boolean mayManage(Creature creature)
  {
    if (creature.getPower() > 1) {
      return true;
    }
    return canManage(creature);
  }
  
  public final boolean maySeeHistory(Creature creature)
  {
    if (creature.getPower() > 1) {
      return true;
    }
    return isOwner(creature);
  }
  
  public final boolean mayModify(Creature creature)
  {
    if (StructureSettings.isExcluded(this, creature)) {
      return false;
    }
    return StructureSettings.mayModify(this, creature);
  }
  
  final boolean isExcluded(Creature creature)
  {
    return StructureSettings.isExcluded(this, creature);
  }
  
  public final boolean mayPass(Creature creature)
  {
    if (StructureSettings.isExcluded(this, creature)) {
      return false;
    }
    return StructureSettings.mayPass(this, creature);
  }
  
  public final boolean mayPickup(Creature creature)
  {
    if (isEnemy(creature)) {
      return true;
    }
    if (StructureSettings.isExcluded(this, creature)) {
      return false;
    }
    return StructureSettings.mayPickup(this, creature);
  }
  
  public boolean isGuest(Creature creature)
  {
    return isGuest(creature.getWurmId());
  }
  
  public boolean isGuest(long playerId)
  {
    return StructureSettings.isGuest(this, playerId);
  }
  
  public final void addGuest(long guestId, int aSettings)
  {
    StructureSettings.addPlayer(getWurmId(), guestId, aSettings);
  }
  
  public final void removeGuest(long guestId)
  {
    StructureSettings.removePlayer(getWurmId(), guestId);
  }
  
  public final long getCreationDate()
  {
    return this.creationDate;
  }
  
  public final int getSize()
  {
    return this.structureTiles.size();
  }
  
  public final int getLimit()
  {
    return this.structureTiles.size() + getExteriorWalls().length;
  }
  
  public final int getLimitFor(int tilex, int tiley, boolean onSurface, boolean adding)
  {
    VolaTile newTile = Zones.getOrCreateTile(tilex, tiley, onSurface);
    
    int points = getLimit();
    if ((contains(tilex, tiley)) && (adding)) {
      return points;
    }
    int newTilePoints = 5;
    if (adding)
    {
      Set<VolaTile> neighbors = createNeighbourStructureTiles(this, newTile);
      newTilePoints -= neighbors.size();
      points -= neighbors.size();
      return points + newTilePoints;
    }
    if (contains(tilex, tiley))
    {
      Set<VolaTile> neighbors = createNeighbourStructureTiles(this, newTile);
      
      newTilePoints -= neighbors.size();
      points += neighbors.size();
      return points - newTilePoints;
    }
    return points;
  }
  
  private void setMaxAndMin()
  {
    this.maxX = 0;
    this.minX = (1 << Constants.meshSize);
    this.maxY = 0;
    this.minY = (1 << Constants.meshSize);
    Iterator<VolaTile> it;
    if (this.structureTiles != null) {
      for (it = this.structureTiles.iterator(); it.hasNext();)
      {
        VolaTile tile = (VolaTile)it.next();
        int xx = tile.getTileX();
        int yy = tile.getTileY();
        if (xx > this.maxX) {
          this.maxX = xx;
        }
        if (xx < this.minX) {
          this.minX = xx;
        }
        if (yy > this.maxY) {
          this.maxY = yy;
        }
        if (yy < this.minY) {
          this.minY = yy;
        }
      }
    }
  }
  
  static final StructureBounds getStructureBounds(List<Wall> structureWalls)
  {
    return null;
  }
  
  final StructureBounds secureOuterWalls(List<Wall> structureWalls)
  {
    TilePoint max = new TilePoint(0, 0);
    TilePoint min = new TilePoint(Zones.worldTileSizeX, Zones.worldTileSizeY);
    
    StructureBounds structBounds = new StructureBounds(max, min);
    for (Wall wall : structureWalls)
    {
      if (wall.getStartX() > structBounds.max.getTileX()) {
        structBounds.getMax().setTileX(wall.getStartX());
      }
      if (wall.getStartY() > structBounds.max.getTileY()) {
        structBounds.getMax().setTileY(wall.getStartY());
      }
      if (wall.getStartX() < structBounds.min.getTileX()) {
        structBounds.getMin().setTileX(wall.getStartX());
      }
      if (wall.getStartY() < structBounds.min.getTileY()) {
        structBounds.getMin().setTileY(wall.getStartY());
      }
    }
    return structBounds;
  }
  
  private void fixWalls(VolaTile tile)
  {
    for (Object localObject = this.buildTiles.iterator(); ((Iterator)localObject).hasNext();)
    {
      bt = (BuildTile)((Iterator)localObject).next();
      if ((bt.getTileX() == tile.getTileX()) && (bt.getTileY() == tile.getTileY())) {
        if (tile.isOnSurface() == (bt.getLayer() == 0)) {
          return;
        }
      }
    }
    localObject = tile.getWalls();BuildTile bt = localObject.length;
    for (BuildTile localBuildTile1 = 0; localBuildTile1 < bt; localBuildTile1++)
    {
      Wall wall = localObject[localBuildTile1];
      
      int x = tile.getTileX();
      int y = tile.getTileY();
      int newTileX = 0;
      int newTileY = 0;
      boolean found = false;
      Structure s = null;
      if (wall.isHorizontal())
      {
        s = Structures.getStructureForTile(x, y - 1, tile.isOnSurface());
        if ((s != null) && (s.isTypeHouse()))
        {
          newTileX = x;
          newTileY = y - 1;
          found = true;
        }
        s = Structures.getStructureForTile(x, y + 1, tile.isOnSurface());
        if ((s != null) && (s.isTypeHouse()))
        {
          newTileX = x;
          newTileY = y + 1;
          found = true;
        }
      }
      else
      {
        s = Structures.getStructureForTile(x - 1, y, tile.isOnSurface());
        if ((s != null) && (s.isTypeHouse()))
        {
          newTileX = x - 1;
          newTileY = y;
          found = true;
        }
        s = Structures.getStructureForTile(x + 1, y, tile.isOnSurface());
        if ((s != null) && (s.isTypeHouse()))
        {
          newTileX = x + 1;
          newTileY = y;
          found = true;
        }
      }
      if (!found)
      {
        logger.log(Level.WARNING, StringUtil.format("Wall with WALL.ID = %d is orphan, but belongs to structure %d. Does the structure exist?", new Object[] {
        
          Integer.valueOf(wall.getNumber()), Long.valueOf(wall.getStructureId()) }));
        return;
      }
      VolaTile t = Zones.getTileOrNull(newTileX, newTileY, tile.isOnSurface());
      
      tile.removeWall(wall, true);
      wall.setTile(newTileX, newTileY);
      t.addWall(wall);
      logger.log(Level.WARNING, StringUtil.format("fixWalls found a wall %d at %d,%d and moved it to %d,%d for structure %d", new Object[] {
      
        Integer.valueOf(wall.getNumber()), Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(newTileX), Integer.valueOf(newTileY), Long.valueOf(wall.getStructureId()) }));
    }
  }
  
  final boolean loadStructureTiles(List<Wall> structureWalls)
  {
    boolean toReturn = true;
    if (!this.buildTiles.isEmpty())
    {
      toReturn = false;
      for (BuildTile buildTile : this.buildTiles) {
        try
        {
          Zone zone = Zones.getZone(buildTile.getTileX(), buildTile.getTileY(), buildTile.getLayer() == 0);
          
          VolaTile tile = zone.getOrCreateTile(buildTile.getTileX(), buildTile.getTileY());
          
          addBuildTile(tile, true);
        }
        catch (NoSuchZoneException nsz)
        {
          logger.log(Level.WARNING, "Structure with id " + this.wurmId + " is built on the edge of the world at " + buildTile
            .getTileX() + ", " + buildTile.getTileY(), nsz);
        }
      }
    }
    int tilex = 0;
    int tiley = 0;
    for (Iterator<Wall> it = structureWalls.iterator(); it.hasNext();)
    {
      Wall wall = (Wall)it.next();
      try
      {
        tilex = wall.getTileX();
        tiley = wall.getTileY();
        Zone zone = Zones.getZone(tilex, tiley, isSurfaced());
        VolaTile tile = zone.getOrCreateTile(tilex, tiley);
        
        tile.addWall(wall);
        if (!this.structureTiles.contains(tile))
        {
          logger.log(Level.WARNING, "Wall with  WURMZONES.WALLS.ID =" + wall.getId() + " exists outside a structure! ");
          fixWalls(tile);
        }
      }
      catch (NoSuchZoneException nsz)
      {
        logger.log(Level.WARNING, "Failed to locate zone at " + tilex + ", " + tiley);
      }
      if ((wall.getType() == StructureTypeEnum.DOOR) || (wall.getType() == StructureTypeEnum.DOUBLE_DOOR) || 
        (wall.getType() == StructureTypeEnum.PORTCULLIS) || (wall.getType() == StructureTypeEnum.CANOPY_DOOR))
      {
        if (this.doors == null) {
          this.doors = new HashSet();
        }
        Door door = new DbDoor(wall);
        addDoor(door);
        door.addToTiles();
        try
        {
          door.load();
        }
        catch (IOException e)
        {
          logger.log(Level.WARNING, "Failed to load a door: " + e.getMessage(), e);
        }
      }
    }
    this.buildTiles.clear();
    return toReturn;
  }
  
  final boolean fillHoles()
  {
    int numTiles = this.structureTiles.size() + 3;
    
    Set<VolaTile> tilesToAdd = new HashSet();
    Set<VolaTile> tilesChecked = new HashSet();
    Set<VolaTile> tilesRemaining = new HashSet();
    tilesRemaining.addAll(this.structureTiles);
    
    int iterations = 0;
    while (iterations++ < numTiles)
    {
      for (VolaTile tile : tilesRemaining)
      {
        tilesChecked.add(tile);
        
        Wall[] walls = tile.getWalls();
        boolean checkNorth = true;
        boolean checkEast = true;
        boolean checkSouth = true;
        boolean checkWest = true;
        for (int x = 0; x < walls.length; x++) {
          if (!walls[x].isIndoor())
          {
            if (walls[x].getHeight() > 0) {
              logger.log(Level.INFO, "Wall at " + tile.getTileX() + "," + tile.getTileY() + " not indoor at height " + walls[x]
              
                .getHeight());
            }
            if (walls[x].isHorizontal())
            {
              if (walls[x].getStartY() == tile.getTileY()) {
                checkNorth = false;
              } else {
                checkSouth = false;
              }
            }
            else if (walls[x].getStartX() == tile.getTileX()) {
              checkWest = false;
            } else {
              checkEast = false;
            }
          }
        }
        if (checkNorth) {
          try
          {
            VolaTile t = Zones.getZone(tile.tilex, tile.tiley - 1, this.surfaced).getOrCreateTile(tile.tilex, tile.tiley - 1);
            if ((!this.structureTiles.contains(t)) && 
              (!tilesToAdd.contains(t))) {
              tilesToAdd.add(t);
            }
          }
          catch (NoSuchZoneException nsz)
          {
            logger.log(Level.WARNING, "CN Structure with id " + this.wurmId + " is built on the edge of the world at " + tile
            
              .getTileX() + ", " + tile.getTileY());
          }
        }
        if (checkEast) {
          try
          {
            VolaTile t = Zones.getZone(tile.tilex + 1, tile.tiley, this.surfaced).getOrCreateTile(tile.tilex + 1, tile.tiley);
            if ((!this.structureTiles.contains(t)) && 
              (!tilesToAdd.contains(t))) {
              tilesToAdd.add(t);
            }
          }
          catch (NoSuchZoneException nsz)
          {
            logger.log(Level.WARNING, "CE Structure with id " + this.wurmId + " is built on the edge of the world at " + tile
            
              .getTileX() + ", " + tile.getTileY());
          }
        }
        if (checkWest) {
          try
          {
            VolaTile t = Zones.getZone(tile.tilex - 1, tile.tiley, this.surfaced).getOrCreateTile(tile.tilex - 1, tile.tiley);
            if ((!this.structureTiles.contains(t)) && 
              (!tilesToAdd.contains(t))) {
              tilesToAdd.add(t);
            }
          }
          catch (NoSuchZoneException nsz)
          {
            logger.log(Level.WARNING, "CW Structure with id " + this.wurmId + " is built on the edge of the world at " + tile
            
              .getTileX() + ", " + tile.getTileY());
          }
        }
        if (checkSouth) {
          try
          {
            VolaTile t = Zones.getZone(tile.tilex, tile.tiley + 1, this.surfaced).getOrCreateTile(tile.tilex, tile.tiley + 1);
            if ((!this.structureTiles.contains(t)) && 
              (!tilesToAdd.contains(t))) {
              tilesToAdd.add(t);
            }
          }
          catch (NoSuchZoneException nsz)
          {
            logger.log(Level.WARNING, "CS Structure with id " + this.wurmId + " is built on the edge of the world at " + tile
            
              .getTileX() + ", " + tile.getTileY());
          }
        }
      }
      tilesRemaining.removeAll(tilesChecked);
      if (tilesToAdd.size() > 0)
      {
        for (VolaTile tile : tilesToAdd) {
          try
          {
            if (tile.getTileX() > this.maxX) {
              this.maxX = tile.getTileX();
            }
            if (tile.getTileX() < this.minX) {
              this.minX = tile.getTileX();
            }
            if (tile.getTileY() > this.maxY) {
              this.maxY = tile.getTileY();
            }
            if (tile.getTileY() < this.minY) {
              this.minY = tile.getTileY();
            }
            Zone zone = Zones.getZone(tile.getTileX(), tile.getTileY(), isSurfaced());
            zone.addStructure(this);
            this.structureTiles.add(tile);
            addNewBuildTile(tile.getTileX(), tile.getTileY(), tile.getLayer());
            
            tile.setStructureAtLoad(this);
          }
          catch (NoSuchZoneException nsz)
          {
            logger.log(Level.WARNING, "Structure with id " + this.wurmId + " is built on the edge of the world at " + tile
              .getTileX() + ", " + tile.getTileY(), nsz);
          }
        }
        tilesRemaining.addAll(tilesToAdd);
        tilesToAdd.clear();
      }
      else
      {
        return false;
      }
    }
    logger.log(Level.WARNING, "Iterations went over " + numTiles + " for " + getName() + " at " + getCenterX() + ", " + 
      getCenterY());
    return false;
  }
  
  static final boolean isEqual(Structure struct1, Structure struct2)
  {
    if (struct1 == null) {
      return false;
    }
    if (struct2 == null) {
      return false;
    }
    return struct1.getWurmId() == struct2.getWurmId();
  }
  
  static final Set<VolaTile> createNeighbourStructureTiles(Structure struct, VolaTile modifiedTile)
  {
    Set<VolaTile> toReturn = new HashSet();
    VolaTile t = Zones.getTileOrNull(modifiedTile.getTileX() + 1, modifiedTile.getTileY(), modifiedTile.isOnSurface());
    if ((t != null) && (isEqual(t.getStructure(), struct))) {
      toReturn.add(t);
    }
    t = Zones.getTileOrNull(modifiedTile.getTileX(), modifiedTile.getTileY() + 1, modifiedTile.isOnSurface());
    if ((t != null) && (isEqual(t.getStructure(), struct))) {
      toReturn.add(t);
    }
    t = Zones.getTileOrNull(modifiedTile.getTileX(), modifiedTile.getTileY() - 1, modifiedTile.isOnSurface());
    if ((t != null) && (isEqual(t.getStructure(), struct))) {
      toReturn.add(t);
    }
    t = Zones.getTileOrNull(modifiedTile.getTileX() - 1, modifiedTile.getTileY(), modifiedTile.isOnSurface());
    if ((t != null) && (isEqual(t.getStructure(), struct))) {
      toReturn.add(t);
    }
    return toReturn;
  }
  
  public static void adjustWallsAroundAddedStructureTile(Structure structure, int tilex, int tiley)
  {
    VolaTile newTile = Zones.getOrCreateTile(tilex, tiley, structure.isOnSurface());
    Set<VolaTile> neighbourTiles = createNeighbourStructureTiles(structure, newTile);
    structure.adjustSurroundingWallsAddedStructureTile(tilex, tiley, neighbourTiles);
  }
  
  public static void adjustWallsAroundRemovedStructureTile(Structure structure, int tilex, int tiley)
  {
    VolaTile newTile = Zones.getOrCreateTile(tilex, tiley, structure.isOnSurface());
    Set<VolaTile> neighbourTiles = createNeighbourStructureTiles(structure, newTile);
    structure.adjustSurroundingWallsRemovedStructureTile(tilex, tiley, neighbourTiles);
  }
  
  public void updateWallIsInner(Structure localStructure, VolaTile volaTile, Wall wall, boolean isInner)
  {
    if (localStructure.getWurmId() != getWurmId())
    {
      logger.log(Level.WARNING, "Warning structures too close to eachother: " + localStructure.getWurmId() + " and " + 
        getWurmId() + " at " + volaTile.getTileX() + "," + volaTile.getTileY());
      return;
    }
    if (wall.getHeight() > 0) {
      wall.setIndoor(true);
    } else {
      wall.setIndoor(isInner);
    }
    volaTile.updateWall(wall);
  }
  
  public void adjustSurroundingWallsAddedStructureTile(int tilex, int tiley, Set<VolaTile> neighbourTiles)
  {
    VolaTile newTile = Zones.getOrCreateTile(tilex, tiley, isOnSurface());
    for (VolaTile neighbourTile : neighbourTiles)
    {
      Structure localStructure = neighbourTile.getStructure();
      
      Wall[] walls = neighbourTile.getWalls();
      for (Wall wall : walls)
      {
        if ((wall.isHorizontal()) && 
          (wall.getStartY() == tiley) && (wall.getEndY() == tiley) && 
          (wall.getStartX() == tilex) && (wall.getEndX() == tilex + 1)) {
          if (isFree(tilex, tiley - 1)) {
            updateWallIsInner(localStructure, newTile, wall, false);
          } else {
            updateWallIsInner(localStructure, newTile, wall, true);
          }
        }
        if ((wall.isHorizontal()) && 
          (wall.getStartY() == tiley + 1) && (wall.getEndY() == tiley + 1) && 
          (wall.getStartX() == tilex) && (wall.getEndX() == tilex + 1)) {
          if (isFree(tilex, tiley + 1)) {
            updateWallIsInner(localStructure, newTile, wall, false);
          } else {
            updateWallIsInner(localStructure, newTile, wall, true);
          }
        }
        if ((!wall.isHorizontal()) && 
          (wall.getStartX() == tilex + 1) && (wall.getEndX() == tilex + 1) && 
          (wall.getStartY() == tiley) && (wall.getEndY() == tiley + 1)) {
          if (isFree(tilex + 1, tiley)) {
            updateWallIsInner(localStructure, newTile, wall, false);
          } else {
            updateWallIsInner(localStructure, newTile, wall, true);
          }
        }
        if ((!wall.isHorizontal()) && 
          (wall.getStartX() == tilex) && (wall.getEndX() == tilex) && 
          (wall.getStartY() == tiley) && (wall.getEndY() == tiley + 1)) {
          if (isFree(tilex - 1, tiley)) {
            updateWallIsInner(localStructure, newTile, wall, false);
          } else {
            updateWallIsInner(localStructure, newTile, wall, true);
          }
        }
      }
    }
  }
  
  public void adjustSurroundingWallsRemovedStructureTile(int tilex, int tiley, Set<VolaTile> neighbourTiles)
  {
    VolaTile removedTile = Zones.getOrCreateTile(tilex, tiley, isOnSurface());
    for (VolaTile neighbourTile : neighbourTiles)
    {
      Structure localStructure = neighbourTile.getStructure();
      
      Wall[] walls = neighbourTile.getWalls();
      for (Wall wall : walls)
      {
        if ((wall.isHorizontal()) && 
          (wall.getStartX() == tilex) && (wall.getEndX() == tilex + 1) && 
          (wall.getStartY() == tiley) && (wall.getEndY() == tiley)) {
          if (isFree(tilex, tiley - 1)) {
            logger.log(Level.WARNING, "Wall exist.");
          } else {
            updateWallIsInner(localStructure, removedTile, wall, false);
          }
        }
        if ((wall.isHorizontal()) && 
          (wall.getStartX() == tilex) && (wall.getEndX() == tilex + 1) && 
          (wall.getStartY() == tiley + 1) && (wall.getEndY() == tiley + 1)) {
          if (isFree(tilex, tiley + 1)) {
            logger.log(Level.WARNING, "Wall exist.");
          } else {
            updateWallIsInner(localStructure, removedTile, wall, false);
          }
        }
        if ((!wall.isHorizontal()) && 
          (wall.getStartX() == tilex + 1) && (wall.getEndX() == tilex + 1) && 
          (wall.getStartY() == tiley) && (wall.getEndY() == tiley + 1)) {
          if (isFree(tilex + 1, tiley)) {
            logger.log(Level.WARNING, "Walls exist.");
          } else {
            updateWallIsInner(localStructure, removedTile, wall, false);
          }
        }
        if ((!wall.isHorizontal()) && 
          (wall.getStartX() == tilex) && (wall.getEndX() == tilex) && 
          (wall.getStartY() == tiley) && (wall.getEndY() == tiley + 1)) {
          if (isFree(tilex - 1, tiley)) {
            logger.log(Level.WARNING, "Walls exist.");
          } else {
            updateWallIsInner(localStructure, removedTile, wall, false);
          }
        }
      }
    }
  }
  
  public void addMissingWallPlans(VolaTile tile)
  {
    boolean lacksNorth = true;
    boolean lacksSouth = true;
    boolean lacksWest = true;
    boolean lacksEast = true;
    for (Wall w : tile.getWallsForLevel(0)) {
      if (w.isHorizontal())
      {
        if (w.getStartY() == tile.tiley) {
          lacksNorth = false;
        }
        if (w.getStartY() == tile.tiley + 1) {
          lacksSouth = false;
        }
      }
      else
      {
        if (w.getStartX() == tile.tilex) {
          lacksWest = false;
        }
        if (w.getStartX() == tile.tilex + 1) {
          lacksEast = false;
        }
      }
    }
    if ((lacksWest) && (isFree(tile.tilex - 1, tile.tiley))) {
      tile.addWall(StructureTypeEnum.PLAN, tile.tilex, tile.tiley, tile.tilex, tile.tiley + 1, 10.0F, this.wurmId, false);
    }
    if ((lacksEast) && (isFree(tile.tilex + 1, tile.tiley))) {
      tile.addWall(StructureTypeEnum.PLAN, tile.tilex + 1, tile.tiley, tile.tilex + 1, tile.tiley + 1, 10.0F, this.wurmId, false);
    }
    if ((lacksNorth) && (isFree(tile.tilex, tile.tiley - 1))) {
      tile.addWall(StructureTypeEnum.PLAN, tile.tilex, tile.tiley, tile.tilex + 1, tile.tiley, 10.0F, this.wurmId, false);
    }
    if ((lacksSouth) && (isFree(tile.tilex, tile.tiley + 1))) {
      tile.addWall(StructureTypeEnum.PLAN, tile.tilex, tile.tiley + 1, tile.tilex + 1, tile.tiley + 1, 10.0F, this.wurmId, false);
    }
  }
  
  public static final VolaTile expandStructureToTile(Structure structure, VolaTile toAdd)
    throws NoSuchZoneException
  {
    structure.structureTiles.add(toAdd);
    toAdd.getZone().addStructure(structure);
    
    return toAdd;
  }
  
  public final void addBuildTile(VolaTile toAdd, boolean loading)
    throws NoSuchZoneException
  {
    if (toAdd.tilex > this.maxX) {
      this.maxX = toAdd.tilex;
    }
    if (toAdd.tilex < this.minX) {
      this.minX = toAdd.tilex;
    }
    if (toAdd.tiley > this.maxY) {
      this.maxY = toAdd.tiley;
    }
    if (toAdd.tiley < this.minY) {
      this.minY = toAdd.tiley;
    }
    if ((this.buildTiles.isEmpty()) && (isFinalized())) {
      addNewBuildTile(toAdd.tilex, toAdd.tiley, toAdd.getLayer());
    }
    expandStructureToTile(this, toAdd);
    if (this.structureType == 0) {
      toAdd.addBuildMarker(this);
    } else if (loading) {
      toAdd.setStructureAtLoad(this);
    }
  }
  
  private static final VolaTile getFirstNeighbourTileOrNull(VolaTile structureTile)
  {
    VolaTile t = Zones.getTileOrNull(structureTile.getTileX() + 1, structureTile.getTileY(), structureTile.isOnSurface());
    if ((t != null) && (t.getStructure() == structureTile.getStructure())) {
      return t;
    }
    t = Zones.getTileOrNull(structureTile.getTileX(), structureTile.getTileY() + 1, structureTile.isOnSurface());
    if ((t != null) && (t.getStructure() == structureTile.getStructure())) {
      return t;
    }
    t = Zones.getTileOrNull(structureTile.getTileX(), structureTile.getTileY() - 1, structureTile.isOnSurface());
    if ((t != null) && (t.getStructure() == structureTile.getStructure())) {
      return t;
    }
    t = Zones.getTileOrNull(structureTile.getTileX() - 1, structureTile.getTileY(), structureTile.isOnSurface());
    if ((t != null) && (t.getStructure() == structureTile.getStructure())) {
      return t;
    }
    return null;
  }
  
  public final boolean testRemove(VolaTile tileToCheck)
  {
    if (this.structureTiles.size() <= 2) {
      return true;
    }
    Set<VolaTile> remainingTiles = new HashSet();
    Set<VolaTile> removedTiles = new HashSet();
    remainingTiles.addAll(this.structureTiles);
    remainingTiles.remove(tileToCheck);
    
    VolaTile firstNeighbour = getFirstNeighbourTileOrNull(tileToCheck);
    if (firstNeighbour == null) {
      return true;
    }
    removedTiles.add(firstNeighbour);
    
    Set<VolaTile> tilesToRemove = new HashSet();
    for (;;)
    {
      for (Iterator localIterator1 = removedTiles.iterator(); localIterator1.hasNext();)
      {
        removed = (VolaTile)localIterator1.next();
        for (VolaTile remaining : remainingTiles) {
          if (removed.isNextTo(remaining)) {
            tilesToRemove.add(remaining);
          }
        }
      }
      VolaTile removed;
      if (tilesToRemove.isEmpty()) {
        return remainingTiles.isEmpty();
      }
      removedTiles.addAll(tilesToRemove);
      remainingTiles.removeAll(tilesToRemove);
      tilesToRemove.clear();
    }
  }
  
  public final boolean removeTileFromFinishedStructure(Creature performer, int tilex, int tiley, int layer)
  {
    if (this.structureTiles == null) {
      return false;
    }
    VolaTile toRemove = null;
    for (Iterator localIterator = this.structureTiles.iterator(); localIterator.hasNext();)
    {
      tile = (VolaTile)localIterator.next();
      
      xx = tile.getTileX();
      yy = tile.getTileY();
      if ((xx == tilex) && (yy == tiley))
      {
        toRemove = tile;
        break;
      }
    }
    VolaTile tile;
    if (!testRemove(toRemove)) {
      return false;
    }
    Wall[] walls = toRemove.getWalls();
    for (wall : walls)
    {
      toRemove.removeWall(wall, false);
      wall.delete();
    }
    Floor[] floors = toRemove.getFloors();
    int xx = floors;int yy = xx.length;
    for (Wall wall = 0; wall < yy; wall++)
    {
      Floor floor = xx[wall];
      
      toRemove.removeFloor(floor);
      floor.delete();
    }
    this.structureTiles.remove(toRemove);
    removeBuildTile(tilex, tiley, layer);
    MethodsStructure.removeBuildMarker(this, tilex, tiley);
    setMaxAndMin();
    
    VolaTile westTile = Zones.getTileOrNull(toRemove.getTileX() - 1, toRemove.getTileY(), toRemove.isOnSurface());
    if ((westTile != null) && (westTile.getStructure() == this)) {
      addMissingWallPlans(westTile);
    }
    VolaTile eastTile = Zones.getTileOrNull(toRemove.getTileX() + 1, toRemove.getTileY(), toRemove.isOnSurface());
    if ((eastTile != null) && (eastTile.getStructure() == this)) {
      addMissingWallPlans(eastTile);
    }
    VolaTile northTile = Zones.getTileOrNull(toRemove.getTileX(), toRemove.getTileY() - 1, toRemove.isOnSurface());
    if ((northTile != null) && (northTile.getStructure() == this)) {
      addMissingWallPlans(northTile);
    }
    VolaTile southTile = Zones.getTileOrNull(toRemove.getTileX(), toRemove.getTileY() + 1, toRemove.isOnSurface());
    if ((southTile != null) && (southTile.getStructure() == this)) {
      addMissingWallPlans(southTile);
    }
    adjustWallsAroundRemovedStructureTile(this, tilex, tiley);
    
    return true;
  }
  
  public final boolean removeTileFromPlannedStructure(Creature aPlanner, int tilex, int tiley)
  {
    boolean allowed = false;
    if (this.structureTiles == null) {
      return false;
    }
    VolaTile toRemove = null;
    for (Iterator localIterator = this.structureTiles.iterator(); localIterator.hasNext();)
    {
      tile = (VolaTile)localIterator.next();
      
      xx = tile.getTileX();
      yy = tile.getTileY();
      if ((xx == tilex) && (yy == tiley))
      {
        toRemove = tile;
        break;
      }
    }
    VolaTile tile;
    int xx;
    int yy;
    if (toRemove == null)
    {
      logger.warning("Tile " + tilex + "," + tiley + " was not part of structure '" + getWurmId() + "'");
      return false;
    }
    if (testRemove(toRemove))
    {
      allowed = true;
      
      Wall[] walls = toRemove.getWalls();
      for (Wall wall : walls)
      {
        toRemove.removeWall(wall, false);
        wall.delete();
      }
      this.structureTiles.remove(toRemove);
      
      MethodsStructure.removeBuildMarker(this, tilex, tiley);
      
      setMaxAndMin();
      
      VolaTile westTile = Zones.getTileOrNull(toRemove.getTileX() - 1, toRemove.getTileY(), toRemove.isOnSurface());
      if ((westTile != null) && (westTile.getStructure() == this)) {
        addMissingWallPlans(westTile);
      }
      VolaTile eastTile = Zones.getTileOrNull(toRemove.getTileX() + 1, toRemove.getTileY(), toRemove.isOnSurface());
      if ((eastTile != null) && (eastTile.getStructure() == this)) {
        addMissingWallPlans(eastTile);
      }
      VolaTile northTile = Zones.getTileOrNull(toRemove.getTileX(), toRemove.getTileY() - 1, toRemove.isOnSurface());
      if ((northTile != null) && (northTile.getStructure() == this)) {
        addMissingWallPlans(northTile);
      }
      VolaTile southTile = Zones.getTileOrNull(toRemove.getTileX(), toRemove.getTileY() + 1, toRemove.isOnSurface());
      if ((southTile != null) && (southTile.getStructure() == this)) {
        addMissingWallPlans(southTile);
      }
    }
    if (this.structureTiles.isEmpty())
    {
      aPlanner.setStructure(null);
      try
      {
        aPlanner.save();
      }
      catch (Exception iox)
      {
        logger.log(Level.WARNING, "Failed to save player " + aPlanner.getName() + ", StructureId: " + this.wurmId, iox);
      }
      Structures.removeStructure(this.wurmId);
    }
    return allowed;
  }
  
  public final void addDoor(Door door)
  {
    if (this.doors == null) {
      this.doors = new HashSet();
    }
    if (!this.doors.contains(door))
    {
      this.doors.add(door);
      door.setStructureId(this.wurmId);
    }
  }
  
  public final Door[] getAllDoors()
  {
    Door[] toReturn = new Door[0];
    if ((this.doors != null) && (this.doors.size() != 0)) {
      toReturn = (Door[])this.doors.toArray(new Door[this.doors.size()]);
    }
    return toReturn;
  }
  
  public final Door[] getAllDoors(boolean includeAll)
  {
    Set<Door> ldoors = new HashSet();
    if ((this.doors != null) && (this.doors.size() != 0)) {
      for (Door door : this.doors) {
        if ((includeAll) || (door.hasLock())) {
          ldoors.add(door);
        }
      }
    }
    return (Door[])ldoors.toArray(new Door[ldoors.size()]);
  }
  
  public final void removeDoor(Door door)
  {
    if (this.doors != null)
    {
      this.doors.remove(door);
      door.delete();
    }
  }
  
  public final void unlockAllDoors()
  {
    Door[] lDoors = getAllDoors();
    for (int x = 0; x < lDoors.length; x++) {
      lDoors[x].unlock(x == 0);
    }
  }
  
  public final void lockAllDoors()
  {
    Door[] lDoors = getAllDoors();
    for (int x = 0; x < lDoors.length; x++) {
      lDoors[x].lock(x == 0);
    }
  }
  
  public final boolean isLocked()
  {
    Door[] lDoors = getAllDoors();
    for (int x = 0; x < lDoors.length; x++) {
      if (!lDoors[x].isLocked()) {
        return false;
      }
    }
    return true;
  }
  
  public final boolean isLockable()
  {
    Door[] lDoors = getAllDoors();
    for (int x = 0; x < lDoors.length; x++) {
      try
      {
        lDoors[x].getLock();
      }
      catch (NoSuchLockException nsl)
      {
        return false;
      }
    }
    return true;
  }
  
  public final boolean isTypeBridge()
  {
    return this.structureType == 1;
  }
  
  public final boolean isTypeHouse()
  {
    return this.structureType == 0;
  }
  
  private void finalizeBuildPlanForTiles(long oldStructureId)
    throws IOException
  {
    for (Iterator<VolaTile> it = this.structureTiles.iterator(); it.hasNext();)
    {
      VolaTile tile = (VolaTile)it.next();
      
      tile.finalizeBuildPlan(oldStructureId, this.wurmId);
      
      addNewBuildTile(tile.tilex, tile.tiley, tile.getLayer());
      Wall[] walls = tile.getWalls();
      for (int x = 0; x < walls.length; x++)
      {
        walls[x].setStructureId(this.wurmId);
        walls[x].save();
      }
      Floor[] floors = tile.getFloors();
      for (int x = 0; x < floors.length; x++)
      {
        floors[x].setStructureId(this.wurmId);
        floors[x].save();
      }
      BridgePart[] bridgeParts = tile.getBridgeParts();
      for (int x = 0; x < bridgeParts.length; x++)
      {
        bridgeParts[x].setStructureId(this.wurmId);
        bridgeParts[x].save();
      }
    }
  }
  
  public final boolean makeFinal(Creature aOwner, String aName)
    throws IOException, NoSuchZoneException
  {
    int size = this.structureTiles.size();
    if (size > 0)
    {
      String sName;
      if (this.structureType == 1)
      {
        String sName = aName;
        Achievements.triggerAchievement(aOwner.getWurmId(), 557);
      }
      else
      {
        String sName;
        if (size <= 2)
        {
          sName = aName + "shed";
        }
        else
        {
          String sName;
          if (size <= 3)
          {
            sName = aName + "shack";
          }
          else
          {
            String sName;
            if (size <= 5)
            {
              sName = aName + "cottage";
            }
            else
            {
              String sName;
              if (size <= 6)
              {
                sName = aName + "house";
              }
              else
              {
                String sName;
                if (size <= 10)
                {
                  sName = aName + "villa";
                }
                else
                {
                  String sName;
                  if (size <= 20)
                  {
                    sName = aName + "mansion";
                  }
                  else
                  {
                    String sName;
                    if (size <= 30) {
                      sName = aName + "estate";
                    } else {
                      sName = aName + "stronghold";
                    }
                  }
                }
              }
            }
          }
        }
      }
      long oldStructureId = this.wurmId;
      this.wurmId = WurmId.getNextStructureId();
      Structures.removeStructure(oldStructureId);
      this.name = sName;
      
      Structures.addStructure(this);
      finalizeBuildPlanForTiles(oldStructureId);
      
      Zone northW = null;
      Zone northE = null;
      Zone southW = null;
      Zone southE = null;
      try
      {
        northW = Zones.getZone(this.minX, this.minY, this.surfaced);
        northW.addStructure(this);
      }
      catch (NoSuchZoneException localNoSuchZoneException) {}
      try
      {
        northE = Zones.getZone(this.maxX, this.minY, this.surfaced);
        if (northE != northW) {
          northE.addStructure(this);
        }
      }
      catch (NoSuchZoneException localNoSuchZoneException1) {}
      try
      {
        southE = Zones.getZone(this.maxX, this.maxY, this.surfaced);
        if ((southE != northE) && (southE != northW)) {
          southE.addStructure(this);
        }
      }
      catch (NoSuchZoneException localNoSuchZoneException2) {}
      try
      {
        southW = Zones.getZone(this.minX, this.maxY, this.surfaced);
        if ((southW != northE) && (southW != northW) && (southW != southE)) {
          southW.addStructure(this);
        }
      }
      catch (NoSuchZoneException localNoSuchZoneException3) {}
      this.writid = -10L;
      
      setPlanner(aOwner.getName());
      setOwnerId(aOwner.getWurmId());
      save();
      return true;
    }
    return false;
  }
  
  public void clearAllWallsAndMakeWallsForStructureBorder(VolaTile toAdd)
  {
    for (VolaTile tile : createNeighbourStructureTiles(this, toAdd)) {
      destroyWallsBorderingToTile(tile, toAdd);
    }
    addMissingWallPlans(toAdd);
  }
  
  private void destroyWallsBorderingToTile(VolaTile start, VolaTile target)
  {
    boolean destroy = false;
    for (Wall wall : start.getWalls())
    {
      destroy = false;
      if ((wall.isHorizontal()) && (wall.getMinX() == target.getTileX())) {
        if (wall.getMinY() == target.getTileY()) {
          destroy = true;
        } else if (wall.getMinY() == target.getTileY() + 1) {
          destroy = true;
        }
      }
      if ((!wall.isHorizontal()) && (wall.getMinY() == target.getTileY())) {
        if (wall.getMinX() == target.getTileX()) {
          destroy = true;
        } else if (wall.getMinX() == target.getTileX() + 1) {
          destroy = true;
        }
      }
      if (destroy)
      {
        start.removeWall(wall, false);
        wall.delete();
      }
    }
  }
  
  private boolean isFree(int x, int y)
  {
    return !contains(x, y);
  }
  
  public final boolean isFinished()
  {
    return this.finished;
  }
  
  public final boolean isFinalFinished()
  {
    return this.finalfinished;
  }
  
  public final boolean needsDoor()
  {
    int free = 0;
    for (Iterator<VolaTile> it = this.structureTiles.iterator(); it.hasNext();)
    {
      VolaTile tile = (VolaTile)it.next();
      Wall[] wallArr = tile.getWallsForLevel(0);
      for (int x = 0; x < wallArr.length; x++)
      {
        StructureTypeEnum type = wallArr[x].getType();
        if (type == StructureTypeEnum.DOOR) {
          return false;
        }
        if (type == StructureTypeEnum.DOUBLE_DOOR) {
          return false;
        }
        if (Wall.isArched(type)) {
          return false;
        }
        if (type == StructureTypeEnum.PORTCULLIS) {
          return false;
        }
        if (type == StructureTypeEnum.CANOPY_DOOR) {
          return false;
        }
        if (type == StructureTypeEnum.PLAN) {
          free++;
        }
      }
    }
    if (free < 2) {
      return true;
    }
    return false;
  }
  
  public final int getDoors()
  {
    int numdoors = 0;
    for (Iterator<VolaTile> it = this.structureTiles.iterator(); it.hasNext();)
    {
      VolaTile tile = (VolaTile)it.next();
      Wall[] wallArr = tile.getWalls();
      for (int x = 0; x < wallArr.length; x++)
      {
        StructureTypeEnum type = wallArr[x].getType();
        if (type == StructureTypeEnum.DOOR) {
          numdoors++;
        }
        if (type == StructureTypeEnum.DOUBLE_DOOR) {
          numdoors++;
        }
        if (Wall.isArched(type)) {
          numdoors++;
        }
        if (type == StructureTypeEnum.PORTCULLIS) {
          numdoors++;
        }
        if (type == StructureTypeEnum.CANOPY_DOOR) {
          numdoors++;
        }
      }
    }
    return numdoors;
  }
  
  public boolean updateStructureFinishFlag()
  {
    for (Iterator<VolaTile> it = this.structureTiles.iterator(); it.hasNext();)
    {
      VolaTile tile = (VolaTile)it.next();
      if (this.structureType == 0)
      {
        Wall[] wallArr = tile.getWalls();
        for (int x = 0; x < wallArr.length; x++) {
          if (!wallArr[x].isIndoor()) {
            if (!wallArr[x].isFinished())
            {
              setFinished(false);
              setFinalFinished(false);
              return false;
            }
          }
        }
      }
      else
      {
        BridgePart[] bridgeParts = tile.getBridgeParts();
        for (int x = 0; x < bridgeParts.length; x++) {
          if (!bridgeParts[x].isFinished())
          {
            setFinished(false);
            setFinalFinished(false);
            return false;
          }
        }
      }
    }
    setFinished(true);
    setFinalFinished(true);
    Players.getInstance().setStructureFinished(this.wurmId);
    return true;
  }
  
  public final boolean isFinalized()
  {
    return WurmId.getType(this.wurmId) == 4;
  }
  
  public final boolean contains(int tilex, int tiley)
  {
    if (this.structureTiles == null)
    {
      logger.log(Level.WARNING, "StructureTiles is null in building with id " + this.wurmId);
      return true;
    }
    for (VolaTile tile : this.structureTiles) {
      if (tilex == tile.tilex) {
        if (tiley == tile.tiley) {
          return true;
        }
      }
    }
    return false;
  }
  
  public final boolean isOnSurface()
  {
    return this.surfaced;
  }
  
  public final long getWurmId()
  {
    return this.wurmId;
  }
  
  public int getTemplateId()
  {
    return -10;
  }
  
  public int getMaxAllowed()
  {
    return AnimalSettings.getMaxAllowed();
  }
  
  public boolean isActualOwner(long playerId)
  {
    return getOwnerId() == playerId;
  }
  
  public boolean isOwner(Creature creature)
  {
    return isOwner(creature.getWurmId());
  }
  
  public boolean isOwner(long playerId)
  {
    if (isManaged())
    {
      Village vill = getManagedByVillage();
      if (vill != null) {
        return vill.isMayor(playerId);
      }
    }
    return isActualOwner(playerId);
  }
  
  public boolean mayPlaceMerchants(Creature creature)
  {
    if (StructureSettings.isExcluded(this, creature)) {
      return false;
    }
    return StructureSettings.mayPlaceMerchants(this, creature);
  }
  
  public boolean mayUseBed(Creature creature)
  {
    if (isOwner(creature)) {
      return true;
    }
    if (isGuest(creature)) {
      return true;
    }
    if ((allowsCitizens()) && (isInOwnerSettlement(creature))) {
      return true;
    }
    if ((allowsAllies()) && (isInOwnerAlliance(creature))) {
      return true;
    }
    return false;
  }
  
  public boolean mayPickupPlanted(Creature creature)
  {
    if (StructureSettings.isExcluded(this, creature)) {
      return false;
    }
    return StructureSettings.mayPickupPlanted(this, creature);
  }
  
  public boolean mayLoad(Creature creature)
  {
    if (isEnemy(creature)) {
      return true;
    }
    if (StructureSettings.isExcluded(this, creature)) {
      return false;
    }
    return StructureSettings.mayLoad(this, creature);
  }
  
  public boolean isInOwnerSettlement(Creature creature)
  {
    if (creature.getCitizenVillage() != null)
    {
      long wid = getOwnerId();
      if (wid != -10L)
      {
        Village creatorVillage = Villages.getVillageForCreature(wid);
        return (creatorVillage != null) && (creature.getCitizenVillage().getId() == creatorVillage.getId());
      }
    }
    return false;
  }
  
  public boolean isInOwnerAlliance(Creature creature)
  {
    if (creature.getCitizenVillage() != null)
    {
      long wid = getOwnerId();
      if (wid != -10L)
      {
        Village creatorVillage = Villages.getVillageForCreature(wid);
        return (creatorVillage != null) && (creature.getCitizenVillage().isAlly(creatorVillage));
      }
    }
    return false;
  }
  
  public final int getCenterX()
  {
    return this.minX + Math.max(1, this.maxX - this.minX) / 2;
  }
  
  public final int getCenterY()
  {
    return this.minY + Math.max(1, this.maxY - this.minY) / 2;
  }
  
  public final int getMaxX()
  {
    return this.maxX;
  }
  
  public final int getMaxY()
  {
    return this.maxY;
  }
  
  public final int getMinX()
  {
    return this.minX;
  }
  
  public final int getMinY()
  {
    return this.minY;
  }
  
  public final VolaTile[] getStructureTiles()
  {
    VolaTile[] tiles = new VolaTile[this.structureTiles.size()];
    return (VolaTile[])this.structureTiles.toArray(tiles);
  }
  
  public boolean allowsAllies()
  {
    return this.allowsAllies;
  }
  
  public boolean allowsKingdom()
  {
    return this.allowsKingdom;
  }
  
  public boolean allowsCitizens()
  {
    return this.allowsVillagers;
  }
  
  public boolean isManaged()
  {
    return this.permissions.hasPermission(Permissions.Allow.SETTLEMENT_MAY_MANAGE.getBit());
  }
  
  public boolean isManageEnabled(Player player)
  {
    if (player.getPower() > 1) {
      return true;
    }
    if (isManaged())
    {
      Village vil = getPermissionsVillage();
      if (vil != null) {
        return vil.isMayor(player);
      }
      return false;
    }
    return isOwner(player);
  }
  
  public void setIsManaged(boolean newIsManaged, Player player)
  {
    int oldId = this.villageId;
    if (newIsManaged)
    {
      Village v = getVillage();
      if (v != null)
      {
        this.villageId = v.getId();
      }
      else
      {
        Village cv = player.getCitizenVillage();
        if (cv != null) {
          this.villageId = cv.getId();
        } else {
          return;
        }
      }
    }
    else
    {
      this.villageId = -1;
    }
    if ((oldId != this.villageId) && (StructureSettings.exists(getWurmId())))
    {
      StructureSettings.remove(getWurmId());
      PermissionsHistories.addHistoryEntry(getWurmId(), System.currentTimeMillis(), -10L, "Auto", "Cleared Permissions");
    }
    this.permissions.setPermissionBit(Permissions.Allow.SETTLEMENT_MAY_MANAGE.getBit(), newIsManaged);
  }
  
  public String mayManageText(Player player)
  {
    String sName = getVillageName(player);
    if (sName.length() > 0) {
      return "Settlement \"" + sName + "\" may manage";
    }
    return sName;
  }
  
  public String mayManageHover(Player aPlayer)
  {
    return "";
  }
  
  public String messageOnTick()
  {
    return "This gives full control to the settlement";
  }
  
  public String questionOnTick()
  {
    return "Did you realy mean to do that?";
  }
  
  public String messageUnTick()
  {
    return "Doing this reverts the control back to the owner.";
  }
  
  public String questionUnTick()
  {
    return "Are you really positive you want to do that?";
  }
  
  public String getSettlementName()
  {
    String sName = "";
    Village vill = getPermissionsVillage();
    if (vill != null) {
      sName = vill.getName();
    }
    if (sName.length() > 0) {
      return "Citizens of \"" + sName + "\"";
    }
    return sName;
  }
  
  public String getAllianceName()
  {
    String aName = "";
    Village vill = getPermissionsVillage();
    if (vill != null) {
      aName = vill.getAllianceName();
    }
    if (aName.length() > 0) {
      return "Alliance of \"" + aName + "\"";
    }
    return "";
  }
  
  public String getKingdomName()
  {
    byte kingdom = 0;
    Village vill = getPermissionsVillage();
    if (vill != null) {
      kingdom = vill.kingdom;
    } else {
      kingdom = Players.getInstance().getKingdomForPlayer(getOwnerId());
    }
    return "Kingdom of \"" + Kingdoms.getNameFor(kingdom) + "\"";
  }
  
  public boolean canAllowEveryone()
  {
    return false;
  }
  
  public String getRolePermissionName()
  {
    return "";
  }
  
  final boolean hasLoaded()
  {
    return this.hasLoaded;
  }
  
  final void setHasLoaded(boolean aHasLoaded)
  {
    this.hasLoaded = aHasLoaded;
  }
  
  final boolean isLoading()
  {
    return this.isLoading;
  }
  
  final void setLoading(boolean aIsLoading)
  {
    this.isLoading = aIsLoading;
  }
  
  final boolean isSurfaced()
  {
    return this.surfaced;
  }
  
  public final byte getLayer()
  {
    if (this.surfaced) {
      return 0;
    }
    return -1;
  }
  
  final void setSurfaced(boolean aSurfaced)
  {
    this.surfaced = aSurfaced;
  }
  
  final void setStructureType(byte theStructureType)
  {
    this.structureType = theStructureType;
  }
  
  public final byte getStructureType()
  {
    return this.structureType;
  }
  
  final long getWritid()
  {
    return this.writid;
  }
  
  public final void setWritid(long aWritid, boolean save)
  {
    this.writid = aWritid;
    if (save) {
      try
      {
        saveWritId();
      }
      catch (IOException iox)
      {
        logger.log(Level.INFO, "Problems saving WritId " + aWritid + ", StructureId: " + this.wurmId + iox.getMessage(), iox);
      }
    }
  }
  
  public final Set<StructureSupport> getAllSupports()
  {
    Set<StructureSupport> toReturn = new HashSet();
    if (this.structureTiles == null) {
      return toReturn;
    }
    for (VolaTile tile : this.structureTiles) {
      toReturn.addAll(tile.getAllSupport());
    }
    return toReturn;
  }
  
  private static final void addAllGroundStructureSupportToSet(Set<StructureSupport> supportingSupports, Set<StructureSupport> remainingSupports)
  {
    Set<StructureSupport> toMove = new HashSet();
    for (StructureSupport remaining : remainingSupports) {
      if (remaining.isSupportedByGround()) {
        toMove.add(remaining);
      }
    }
    supportingSupports.addAll(toMove);
    remainingSupports.removeAll(toMove);
  }
  
  public final boolean wouldCreateFlyingStructureIfRemoved(StructureSupport supportToCheck)
  {
    Set<StructureSupport> allSupports = getAllSupports();
    
    Set<StructureSupport> supportingSupports = new HashSet();
    
    allSupports.remove(supportToCheck);
    
    StructureSupport match = null;
    for (Iterator localIterator1 = allSupports.iterator(); localIterator1.hasNext();)
    {
      csupport = (StructureSupport)localIterator1.next();
      if (csupport.getId() == supportToCheck.getId()) {
        match = csupport;
      }
    }
    StructureSupport csupport;
    if (match != null) {
      allSupports.remove(match);
    }
    addAllGroundStructureSupportToSet(supportingSupports, allSupports);
    Object toRemove = new HashSet();
    while (!allSupports.isEmpty())
    {
      for (csupport = supportingSupports.iterator(); csupport.hasNext();)
      {
        checked = (StructureSupport)csupport.next();
        for (StructureSupport remaining : allSupports) {
          if (checked.supports(remaining)) {
            ((Set)toRemove).add(remaining);
          }
        }
      }
      StructureSupport checked;
      if (((Set)toRemove).isEmpty()) {
        break;
      }
      supportingSupports.addAll((Collection)toRemove);
      allSupports.removeAll((Collection)toRemove);
      ((Set)toRemove).clear();
    }
    return !allSupports.isEmpty();
  }
  
  public static final int[] noEntrance = { -1, -1 };
  
  public final int[] getNortEntrance()
  {
    return new int[] { this.minX, this.minY - 1 };
  }
  
  public final int[] getSouthEntrance()
  {
    return new int[] { this.maxX, this.maxY + 1 };
  }
  
  public final int[] getWestEntrance()
  {
    return new int[] { this.minX - 1, this.minY };
  }
  
  public final int[] getEastEntrance()
  {
    return new int[] { this.maxX + 1, this.maxY };
  }
  
  public final boolean isHorizontal()
  {
    return (this.minX < this.maxX) && (this.minY == this.maxY);
  }
  
  public final boolean containsSettlement(int[] tileCoords, int layer)
  {
    if (tileCoords[0] == -1) {
      return false;
    }
    VolaTile t = Zones.getTileOrNull(tileCoords[0], tileCoords[1], layer == 0);
    if (t != null) {
      return t.getVillage() != null;
    }
    return false;
  }
  
  public final int[] findBestBridgeEntrance(Creature creature, int tilex, int tiley, int layer, long bridgeId, int currentPathFindCounter)
  {
    int lMaxX = isHorizontal() ? getMaxX() + 1 : getMaxX();
    int lMinX = isHorizontal() ? getMinX() - 1 : getMinX();
    int lMinY = isHorizontal() ? getMinY() : getMinY() - 1;
    int lMaxY = isHorizontal() ? getMaxY() : getMaxY() + 1;
    int[] min = { lMinX, lMinY };
    
    int[] max = { lMaxX, lMaxY };
    if ((!creature.isUnique()) && 
      (containsSettlement(min, layer)) && (containsSettlement(max, layer))) {
      return noEntrance;
    }
    boolean switchEntrance = (currentPathFindCounter > 5) && (Server.rand.nextBoolean());
    if ((isHorizontal() ? creature.getTileX() >= lMaxX : creature.getTileY() >= lMaxY) && 
      ((!containsSettlement(max, layer)) || (creature.isUnique())) && 
      (!switchEntrance)) {
      return max;
    }
    if ((isHorizontal() ? creature.getTileX() <= lMinX : creature.getTileY() <= lMinY) && 
      ((!containsSettlement(min, layer)) || (creature.isUnique())) && 
      (!switchEntrance)) {
      return min;
    }
    int diffMax = Math.abs(isHorizontal() ? creature.getTileX() - lMaxX : creature.getTileY() - lMaxY);
    int diffMin = isHorizontal() ? creature.getTileX() - lMinX : creature.getTileY() - lMinY;
    if (diffMax <= diffMin)
    {
      if (((!containsSettlement(max, layer)) || (creature.isUnique())) && 
        (!switchEntrance)) {
        return max;
      }
      return min;
    }
    if (diffMin <= diffMax)
    {
      if (((!containsSettlement(min, layer)) || (creature.isUnique())) && 
        (!switchEntrance)) {
        return min;
      }
      return max;
    }
    if (Server.rand.nextBoolean())
    {
      if (((!containsSettlement(max, layer)) || (creature.isUnique())) && 
        (!switchEntrance)) {
        return max;
      }
      return min;
    }
    if (((!containsSettlement(min, layer)) || (creature.isUnique())) && 
      (!switchEntrance)) {
      return min;
    }
    return max;
  }
  
  public boolean isBridgeJustPlans()
  {
    if (this.structureType != 1) {
      return false;
    }
    for (BridgePart bp : getBridgeParts()) {
      if (bp.getBridgePartState() != BridgeConstants.BridgeState.PLANNED) {
        return false;
      }
    }
    return true;
  }
  
  public boolean isBridgeGone()
  {
    if (isBridgeJustPlans())
    {
      for (BridgePart bp : getBridgeParts()) {
        bp.destroy();
      }
      totallyDestroy();
      return true;
    }
    return false;
  }
  
  void addDefaultAllyPermissions()
  {
    if (!getPermissionsPlayerList().exists(-20L))
    {
      int value = StructureSettings.StructurePermissions.PASS.getValue() + StructureSettings.StructurePermissions.PICKUP.getValue();
      addNewGuest(-20L, value);
    }
  }
  
  public void addDefaultCitizenPermissions()
  {
    if (!getPermissionsPlayerList().exists(-30L))
    {
      int value = StructureSettings.StructurePermissions.PASS.getValue() + StructureSettings.StructurePermissions.PICKUP.getValue();
      addNewGuest(-30L, value);
    }
  }
  
  void addDefaultKingdomPermissions()
  {
    if (!getPermissionsPlayerList().exists(-40L))
    {
      int value = StructureSettings.StructurePermissions.PASS.getValue();
      addNewGuest(-40L, value);
    }
  }
  
  public final void setWalkedOnBridge(long now)
  {
    long lastUsed = 0L;
    for (BridgePart bp : getBridgeParts()) {
      if (bp.isFinished()) {
        if (lastUsed < bp.getLastUsed()) {
          lastUsed = bp.getLastUsed();
        }
      }
    }
    if (lastUsed < now - 86400000L) {
      for (BridgePart bp : getBridgeParts()) {
        bp.setLastUsed(now);
      }
    }
  }
  
  public boolean setNewOwner(long playerId)
  {
    if (this.writid != -10L) {
      return false;
    }
    if ((!isManaged()) && (StructureSettings.exists(getWurmId())))
    {
      StructureSettings.remove(getWurmId());
      PermissionsHistories.addHistoryEntry(getWurmId(), System.currentTimeMillis(), -10L, "Auto", "Cleared Permissions");
    }
    this.ownerId = playerId;
    try
    {
      saveOwnerId();
      return true;
    }
    catch (IOException localIOException) {}
    return false;
  }
  
  public String getOwnerName()
  {
    getOwnerId();
    if (this.writid != -10L) {
      return "has writ";
    }
    return PlayerInfoFactory.getPlayerName(this.ownerId);
  }
  
  public boolean convertToNewPermissions()
  {
    boolean didConvert = false;
    PermissionsPlayerList ppl = StructureSettings.getPermissionsPlayerList(this.wurmId);
    if ((this.allowsAllies) && (!ppl.exists(-20L)))
    {
      addDefaultAllyPermissions();
      didConvert = true;
    }
    if ((this.allowsVillagers) && (!ppl.exists(-30L)))
    {
      addDefaultCitizenPermissions();
      didConvert = true;
    }
    if ((this.allowsKingdom) && (!ppl.exists(-40L)))
    {
      addDefaultKingdomPermissions();
      didConvert = true;
    }
    if (didConvert) {
      try
      {
        saveSettings();
      }
      catch (IOException e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
    for (Door d : getAllDoors()) {
      d.setIsManaged(true, null);
    }
    return didConvert;
  }
  
  public byte getKingdomId()
  {
    byte kingdom = 0;
    Village vill = getPermissionsVillage();
    if (vill != null) {
      kingdom = vill.kingdom;
    } else {
      kingdom = Players.getInstance().getKingdomForPlayer(getOwnerId());
    }
    return kingdom;
  }
  
  public static boolean isGroundFloorAtPosition(float x, float y, boolean isOnSurface)
  {
    TilePos tilePos = CoordUtils.WorldToTile(x, y);
    VolaTile tile = Zones.getOrCreateTile(tilePos, isOnSurface);
    if (tile != null)
    {
      Floor[] floors = tile.getFloors(0, 0);
      if ((floors != null) && (floors.length > 0) && (floors[0].getType() == StructureConstants.FloorType.FLOOR)) {
        return true;
      }
    }
    return false;
  }
  
  public boolean isDestroyed()
  {
    if (isTypeBridge()) {
      return (isBridgeJustPlans()) || (isBridgeGone());
    }
    Wall[] walls = getWalls();
    Floor[] floors = getFloors();
    boolean destroyed = true;
    for (Wall wall : walls) {
      if (!wall.isWallPlan()) {
        destroyed = false;
      }
    }
    for (Floor floor : floors) {
      if (!floor.isAPlan()) {
        destroyed = false;
      }
    }
    return destroyed;
  }
  
  abstract void setFinalFinished(boolean paramBoolean);
  
  public abstract void setFinished(boolean paramBoolean);
  
  public abstract void endLoading()
    throws IOException;
  
  abstract void load()
    throws IOException, NoSuchStructureException;
  
  public abstract void save()
    throws IOException;
  
  public abstract void saveWritId()
    throws IOException;
  
  public abstract void saveOwnerId()
    throws IOException;
  
  public abstract void saveSettings()
    throws IOException;
  
  public abstract void saveName()
    throws IOException;
  
  abstract void delete();
  
  abstract void removeStructureGuest(long paramLong);
  
  abstract void addNewGuest(long paramLong, int paramInt);
  
  public abstract void setAllowAllies(boolean paramBoolean);
  
  public abstract void setAllowVillagers(boolean paramBoolean);
  
  public abstract void setAllowKingdom(boolean paramBoolean);
  
  public String toString()
  {
    return "Structure [wurmId=" + this.wurmId + ", surfaced=" + this.surfaced + ", name=" + this.name + ", writid=" + this.writid + "]";
  }
  
  public abstract void removeBuildTile(int paramInt1, int paramInt2, int paramInt3);
  
  public abstract void addNewBuildTile(int paramInt1, int paramInt2, int paramInt3);
  
  public abstract void deleteAllBuildTiles();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\structures\Structure.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */