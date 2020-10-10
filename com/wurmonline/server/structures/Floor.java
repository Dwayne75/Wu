package com.wurmonline.server.structures;

import com.wurmonline.math.TilePos;
import com.wurmonline.math.Vector3f;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.highways.HighwayPos;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.Permissions.Allow;
import com.wurmonline.server.players.Permissions.IAllow;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.StructureConstants.FloorMaterial;
import com.wurmonline.shared.constants.StructureConstants.FloorState;
import com.wurmonline.shared.constants.StructureConstants.FloorType;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Floor
  implements MiscConstants, TimeConstants, Blocker, IFloor, Permissions.IAllow
{
  private static final Logger logger = Logger.getLogger(Wall.class.getName());
  private long structureId = -10L;
  private int number = -10;
  float originalQL;
  float currentQL;
  float damage;
  private int tilex;
  private int tiley;
  private int heightOffset;
  long lastUsed;
  private StructureConstants.FloorType type = StructureConstants.FloorType.FLOOR;
  private StructureConstants.FloorMaterial material = StructureConstants.FloorMaterial.WOOD;
  private StructureConstants.FloorState floorState = StructureConstants.FloorState.PLANNING;
  protected byte dbState = -1;
  private byte layer = 0;
  private int floorLevel = 0;
  private int color = -1;
  private byte direction = 0;
  private static final byte FLOOR_DBSTATE_PLANNED = -1;
  private static final byte FLOOR_DBSTATE_UNINITIALIZED = 0;
  private static final Map<Long, Set<Floor>> floors = new HashMap();
  private static final String GETALLFLOORS = "SELECT * FROM FLOORS";
  private static final Vector3f normal = new Vector3f(0.0F, 0.0F, 1.0F);
  Permissions permissions = new Permissions();
  private Vector3f centerPoint;
  
  public Floor(int id, StructureConstants.FloorType floorType, int aTileX, int aTileY, byte adbState, int aheightOffset, float ql, long structure, StructureConstants.FloorMaterial floorMaterial, int aLayer, float origQl, float dam, long lastmaint, byte dir)
  {
    setNumber(id);
    this.type = floorType;
    this.tilex = aTileX;
    this.tiley = aTileY;
    this.dbState = adbState;
    this.floorState = StructureConstants.FloorState.fromByte(this.dbState);
    this.heightOffset = aheightOffset;
    this.currentQL = ql;
    this.originalQL = origQl;
    this.damage = dam;
    this.structureId = structure;
    this.material = floorMaterial;
    this.layer = ((byte)(aLayer & 0xFF));
    this.lastUsed = lastmaint;
    this.direction = dir;
    setFloorLevel();
  }
  
  public Floor(StructureConstants.FloorType floorType, int aTileX, int aTileY, int height, float ql, long structure, StructureConstants.FloorMaterial floorMaterial, int aLayer)
  {
    this.type = floorType;
    this.tilex = aTileX;
    this.tiley = aTileY;
    this.heightOffset = height;
    this.currentQL = ql;
    this.structureId = structure;
    this.material = floorMaterial;
    this.layer = ((byte)(aLayer & 0xFF));
    setFloorLevel();
  }
  
  public boolean isFloor()
  {
    return true;
  }
  
  public final boolean isStair()
  {
    return (isFinished()) && (this.type.isStair());
  }
  
  public final Vector3f getNormal()
  {
    return normal;
  }
  
  private final Vector3f calculateCenterPoint()
  {
    return new Vector3f(this.tilex * 4 + 2, this.tiley * 4 + 2, getMinZ() + (
      isRoof() ? 1.0F : 0.125F));
  }
  
  public final Vector3f getCenterPoint()
  {
    if (this.centerPoint == null) {
      this.centerPoint = calculateCenterPoint();
    }
    return this.centerPoint;
  }
  
  public int getTileX()
  {
    return this.tilex;
  }
  
  void setTilex(int aTilex)
  {
    this.tilex = aTilex;
  }
  
  public int getTileY()
  {
    return this.tiley;
  }
  
  void setTiley(int aTiley)
  {
    this.tiley = aTiley;
  }
  
  public final float getPositionX()
  {
    return this.tilex * 4;
  }
  
  public final float getPositionY()
  {
    return this.tiley * 4;
  }
  
  public int getHeightOffset()
  {
    return this.heightOffset;
  }
  
  void setHeightOffset(int aHeightOffset)
  {
    this.heightOffset = aHeightOffset;
  }
  
  public byte getLayer()
  {
    return this.layer;
  }
  
  public byte getDir()
  {
    return this.direction;
  }
  
  public boolean leavingStairOnTop(int tilexDiff, int tileyDiff)
  {
    if (isStair())
    {
      if (getDir() == 0) {
        return tileyDiff < 0;
      }
      if (getDir() == 2) {
        return tilexDiff > 0;
      }
      if (getDir() == 6) {
        return tilexDiff < 0;
      }
      if (getDir() == 4) {
        return tileyDiff > 0;
      }
    }
    return false;
  }
  
  public void setLayer(byte newLayer)
  {
    this.layer = newLayer;
  }
  
  public boolean isOnSurface()
  {
    return this.layer == 0;
  }
  
  public void rotate(int change)
  {
    this.direction = ((byte)((this.direction + 8 + change) % 8));
    try
    {
      save();
    }
    catch (IOException e)
    {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
    VolaTile volaTile = Zones.getOrCreateTile(getTileX(), getTileY(), getLayer() >= 0);
    volaTile.updateFloor(this);
  }
  
  public boolean isFinished()
  {
    return this.floorState == StructureConstants.FloorState.COMPLETED;
  }
  
  public final boolean isMetal()
  {
    return (this.material == StructureConstants.FloorMaterial.METAL_COPPER) || (this.material == StructureConstants.FloorMaterial.METAL_GOLD) || (this.material == StructureConstants.FloorMaterial.METAL_IRON) || (this.material == StructureConstants.FloorMaterial.METAL_SILVER) || (this.material == StructureConstants.FloorMaterial.METAL_STEEL);
  }
  
  public final boolean isWood()
  {
    return (this.material == StructureConstants.FloorMaterial.WOOD) || (this.material == StructureConstants.FloorMaterial.THATCH) || (this.material == StructureConstants.FloorMaterial.STANDALONE);
  }
  
  public final boolean isStone()
  {
    return (this.material == StructureConstants.FloorMaterial.STONE_BRICK) || (this.material == StructureConstants.FloorMaterial.STONE_SLAB);
  }
  
  public final boolean isSlate()
  {
    return this.material == StructureConstants.FloorMaterial.SLATE_SLAB;
  }
  
  public final boolean isMarble()
  {
    return this.material == StructureConstants.FloorMaterial.MARBLE_SLAB;
  }
  
  public final boolean isSandstone()
  {
    return this.material == StructureConstants.FloorMaterial.SANDSTONE_SLAB;
  }
  
  public final boolean isGold()
  {
    return this.material == StructureConstants.FloorMaterial.METAL_GOLD;
  }
  
  public final boolean isSilver()
  {
    return this.material == StructureConstants.FloorMaterial.METAL_SILVER;
  }
  
  public final boolean isIron()
  {
    return this.material == StructureConstants.FloorMaterial.METAL_IRON;
  }
  
  public final boolean isSteel()
  {
    return this.material == StructureConstants.FloorMaterial.METAL_STEEL;
  }
  
  public final boolean isCopper()
  {
    return this.material == StructureConstants.FloorMaterial.METAL_COPPER;
  }
  
  public final boolean isThatch()
  {
    return this.material == StructureConstants.FloorMaterial.THATCH;
  }
  
  public final boolean isClay()
  {
    return this.material == StructureConstants.FloorMaterial.CLAY_BRICK;
  }
  
  public final boolean isSolid()
  {
    return (isFinished()) && (!isStair());
  }
  
  public StructureConstants.FloorType getType()
  {
    return this.type;
  }
  
  public void setType(StructureConstants.FloorType newType)
  {
    this.type = newType;
  }
  
  public StructureConstants.FloorMaterial getMaterial()
  {
    return this.material;
  }
  
  public void setMaterial(StructureConstants.FloorMaterial newMaterial)
  {
    this.material = newMaterial;
  }
  
  public abstract void save()
    throws IOException;
  
  public long getId()
  {
    return Tiles.getFloorId(this.tilex, this.tiley, this.heightOffset, getLayer());
  }
  
  public static int getHeightOffsetFromWurmId(long wurmId)
  {
    return (int)(wurmId >> 48) & 0xFFFF;
  }
  
  public StructureConstants.FloorState getFloorState()
  {
    return this.floorState;
  }
  
  long getLastUsed()
  {
    return this.lastUsed;
  }
  
  public abstract void setLastUsed(long paramLong);
  
  int getNumber()
  {
    return this.number;
  }
  
  void setNumber(int aNumber)
  {
    this.number = aNumber;
  }
  
  public float getOriginalQL()
  {
    return this.originalQL;
  }
  
  public float getCurrentQL()
  {
    return this.currentQL;
  }
  
  public byte getState()
  {
    return this.dbState;
  }
  
  protected abstract void setState(byte paramByte);
  
  public void setFloorState(StructureConstants.FloorState aFloorState)
  {
    this.floorState = aFloorState;
    switch (this.floorState)
    {
    case BUILDING: 
      if (getState() <= 0) {
        setState((byte)0);
      }
      break;
    case COMPLETED: 
      setState(StructureConstants.FloorState.COMPLETED.getCode());
      break;
    case PLANNING: 
      setState((byte)-1);
      break;
    }
  }
  
  int getColor()
  {
    return this.color;
  }
  
  void setColor(int aColor)
  {
    this.color = aColor;
  }
  
  public abstract void delete();
  
  public String getName()
  {
    switch (this.type)
    {
    case DOOR: 
      return "hatch";
    case OPENING: 
      return "opening";
    case ROOF: 
      return "roof";
    case FLOOR: 
      return "floor";
    case STAIRCASE: 
      return "staircase";
    case WIDE_STAIRCASE: 
      return "wide staircase";
    case WIDE_STAIRCASE_RIGHT: 
      return "wide staircase with banisters on right";
    case WIDE_STAIRCASE_LEFT: 
      return "wide staircase with banisters on left";
    case WIDE_STAIRCASE_BOTH: 
      return "wide staircase with banisiters on both sides";
    case RIGHT_STAIRCASE: 
      return "right staircase";
    case LEFT_STAIRCASE: 
      return "left staircase";
    case CLOCKWISE_STAIRCASE: 
      return "clockwise spiral staircase";
    case CLOCKWISE_STAIRCASE_WITH: 
      return "clockwise spiral staircase with banisters";
    case ANTICLOCKWISE_STAIRCASE: 
      return "counter clockwise spiral staircase";
    case ANTICLOCKWISE_STAIRCASE_WITH: 
      return "counter clockwise spiral staircase with banisters";
    }
    return "unknown";
  }
  
  public final boolean isOpening()
  {
    return this.type == StructureConstants.FloorType.OPENING;
  }
  
  public boolean isHorizontal()
  {
    return false;
  }
  
  public final Vector3f isBlocking(Creature creature, Vector3f startPos, Vector3f endPos, Vector3f aNormal, int blockType, long target, boolean followGround)
  {
    if (target == getId()) {
      return null;
    }
    if (isAPlan()) {
      return null;
    }
    if (isOpening()) {
      return null;
    }
    Vector3f inter = getIntersectionPoint(startPos, endPos, aNormal, creature);
    return inter;
  }
  
  public final boolean isDoor()
  {
    return this.type == StructureConstants.FloorType.DOOR;
  }
  
  public final boolean isRoof()
  {
    return this.type == StructureConstants.FloorType.ROOF;
  }
  
  public final boolean isTile()
  {
    return false;
  }
  
  public final boolean canBeOpenedBy(Creature creature, boolean wentThroughDoor)
  {
    return true;
  }
  
  public final float getBlockPercent(Creature creature)
  {
    if (isFinished()) {
      return 100.0F;
    }
    return Math.max(0, getState());
  }
  
  public final boolean isWithinFloorLevels(int maxFloorLevel, int minFloorLevel)
  {
    return (this.floorLevel <= maxFloorLevel) && (this.floorLevel >= minFloorLevel);
  }
  
  public final Vector3f getIntersectionPoint(Vector3f startPos, Vector3f endPos, Vector3f aNormal, Creature creature)
  {
    if (isWithinBounds(startPos, creature)) {
      return startPos.clone();
    }
    float zPlane = getMinZ();
    if (Math.abs(startPos.z - getMinZ()) > Math.abs(startPos.z - getMaxZ())) {
      zPlane = getMaxZ();
    }
    float xPlane = getMinX() * 4;
    if (Math.abs(startPos.x - getMinX() * 4) > Math.abs(startPos.x - (getMinX() * 4 + 4))) {
      xPlane += 4.0F;
    }
    float yPlane = getMinY() * 4;
    if (Math.abs(startPos.y - getMinY() * 4) > Math.abs(startPos.y - (getMinY() * 4 + 4))) {
      yPlane += 4.0F;
    }
    for (int i = 0; i < 3; i++)
    {
      float planeVal = i == 1 ? xPlane : i == 0 ? zPlane : yPlane;
      
      Vector3f centerPoint = getCenterPoint().clone();
      switch (i)
      {
      case 0: 
        centerPoint.setZ(planeVal);
        break;
      case 1: 
        centerPoint.setX(planeVal);
        break;
      case 2: 
        centerPoint.setY(planeVal);
      }
      Vector3f diff = startPos.subtract(centerPoint);
      float diffVal = i == 1 ? diff.x : i == 0 ? diff.z : diff.y;
      float normalVal = i == 1 ? aNormal.x : i == 0 ? aNormal.z : aNormal.y;
      if (normalVal != 0.0F)
      {
        float steps = diffVal / normalVal;
        Vector3f intersection = startPos.add(aNormal.mult(-steps));
        Vector3f diffend = endPos.subtract(startPos);
        Vector3f interDiff = intersection.subtract(startPos);
        if (interDiff.length() < diffend.length()) {
          if (isWithinBounds(intersection, creature))
          {
            float u = aNormal.dot(centerPoint.subtract(startPos)) / aNormal.dot(endPos.subtract(startPos));
            if ((u >= 0.0F) && (u <= 1.0F)) {
              return intersection;
            }
          }
        }
      }
    }
    return null;
  }
  
  private final boolean isWithinBounds(Vector3f pointToCheck, Creature creature)
  {
    if ((pointToCheck.getY() >= this.tiley * 4) && 
      (pointToCheck.getY() <= (this.tiley + 1) * 4)) {
      if ((pointToCheck.getX() >= this.tilex * 4) && (pointToCheck.getX() <= (this.tilex + 1) * 4)) {
        if (isWithinZ(pointToCheck.getZ(), pointToCheck.getZ(), (creature != null) && (creature.followsGround()))) {
          return true;
        }
      }
    }
    return false;
  }
  
  public static final void loadAllFloors()
    throws IOException
  {
    logger.log(Level.INFO, "Loading all floors.");
    long s = System.nanoTime();
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM FLOORS");
      rs = ps.executeQuery();
      while (rs.next())
      {
        StructureConstants.FloorType floorType = StructureConstants.FloorType.fromByte(rs.getByte("TYPE"));
        StructureConstants.FloorMaterial floorMaterial = StructureConstants.FloorMaterial.fromByte(rs.getByte("MATERIAL"));
        
        long sid = rs.getLong("STRUCTURE");
        Set<Floor> flset = (Set)floors.get(Long.valueOf(sid));
        if (flset == null)
        {
          flset = new HashSet();
          floors.put(Long.valueOf(sid), flset);
        }
        flset.add(new DbFloor(rs.getInt("ID"), floorType, rs.getInt("TILEX"), rs.getInt("TILEY"), rs.getByte("STATE"), rs
        
          .getInt("HEIGHTOFFSET"), rs
          .getFloat("CURRENTQL"), sid, floorMaterial, rs.getInt("LAYER"), rs
          .getFloat("ORIGINALQL"), rs.getFloat("DAMAGE"), rs.getLong("LASTMAINTAINED"), rs.getByte("DIR")));
      }
    }
    catch (SQLException sqx)
    {
      long e;
      logger.log(Level.WARNING, "Failed to load walls!" + sqx.getMessage(), sqx);
      throw new IOException(sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
      long e = System.nanoTime();
      logger.log(Level.INFO, "Loaded " + floors.size() + " floors. That took " + (float)(e - s) / 1000000.0F + " ms.");
    }
  }
  
  public static final Set<Floor> getFloorsFor(long structureId)
  {
    return (Set)floors.get(Long.valueOf(structureId));
  }
  
  public long getStructureId()
  {
    return this.structureId;
  }
  
  void setStructureId(long aStructureId)
  {
    this.structureId = aStructureId;
  }
  
  public boolean isAPlan()
  {
    return this.floorState == StructureConstants.FloorState.PLANNING;
  }
  
  public void revertToPlan()
  {
    MethodsHighways.removeNearbyMarkers(this);
    setFloorState(StructureConstants.FloorState.PLANNING);
    try
    {
      save();
    }
    catch (IOException e)
    {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
    VolaTile volaTile = Zones.getOrCreateTile(getTileX(), getTileY(), getLayer() >= 0);
    volaTile.updateFloor(this);
  }
  
  public void destroyOrRevertToPlan()
  {
    Structure struct = null;
    try
    {
      struct = Structures.getStructure(getStructureId());
    }
    catch (NoSuchStructureException e)
    {
      logger.log(Level.WARNING, " Failed to find Structures.getStructure(" + getStructureId() + " for a Floor about to be deleted: " + e
        .getMessage(), e);
    }
    if ((struct != null) && (struct.wouldCreateFlyingStructureIfRemoved(this))) {
      revertToPlan();
    } else {
      destroy();
    }
  }
  
  public void destroy()
  {
    delete();
    
    Set<Floor> flset = (Set)floors.get(Long.valueOf(getStructureId()));
    if (flset != null) {
      flset.remove(this);
    }
    VolaTile volaTile = Zones.getOrCreateTile(getTileX(), getTileY(), getLayer() >= 0);
    volaTile.removeFloor(this);
  }
  
  public final float getDamageModifierForItem(Item item)
  {
    float mod;
    float mod;
    float mod;
    float mod;
    float mod;
    switch (this.material)
    {
    case METAL_COPPER: 
    case METAL_GOLD: 
    case METAL_SILVER: 
      float mod;
      if (item.isWeaponCrush()) {
        mod = 0.03F;
      } else {
        mod = 0.007F;
      }
      break;
    case METAL_IRON: 
    case METAL_STEEL: 
      float mod;
      if (item.isWeaponCrush()) {
        mod = 0.02F;
      } else {
        mod = 0.007F;
      }
      break;
    case CLAY_BRICK: 
    case SLATE_SLAB: 
    case STONE_BRICK: 
    case STONE_SLAB: 
    case MARBLE_SLAB: 
    case SANDSTONE_SLAB: 
      float mod;
      if (item.isWeaponCrush()) {
        mod = 0.03F;
      } else {
        mod = 0.007F;
      }
      break;
    case THATCH: 
    case WOOD: 
    case STANDALONE: 
      mod = 0.03F;
      break;
    default: 
      mod = 0.0F;
    }
    return mod;
  }
  
  public final boolean isOnPvPServer()
  {
    if (Zones.isOnPvPServer(this.tilex, this.tiley)) {
      return true;
    }
    return false;
  }
  
  public final int getFloorLevel()
  {
    return this.floorLevel;
  }
  
  private final void setFloorLevel()
  {
    this.floorLevel = (this.heightOffset / 30);
  }
  
  public void buildProgress(int numSteps)
  {
    if (numSteps > 127) {
      numSteps = 127;
    }
    if (getFloorState() == StructureConstants.FloorState.BUILDING) {
      setState((byte)(getState() + numSteps));
    } else {
      logger.log(Level.WARNING, "buildProgress method called on floor when floor was not in buildable state: " + 
        getId() + " " + this.floorState
        .toString());
    }
  }
  
  public final VolaTile getTile()
  {
    try
    {
      Zone zone = Zones.getZone(this.tilex, this.tiley, getLayer() == 0);
      VolaTile toReturn = zone.getTileOrNull(this.tilex, this.tiley);
      if (toReturn != null)
      {
        if (toReturn.isTransition()) {
          return Zones.getZone(this.tilex, this.tiley, false).getOrCreateTile(this.tilex, this.tiley);
        }
        return toReturn;
      }
      logger.log(Level.WARNING, "Tile not in zone, this shouldn't happen " + this.tilex + ", " + this.tiley);
    }
    catch (NoSuchZoneException nsz)
    {
      logger.log(Level.WARNING, "This shouldn't happen " + this.tilex + ", " + this.tiley, nsz);
    }
    return null;
  }
  
  public final Village getVillage()
  {
    VolaTile t = getTile();
    if (t != null) {
      if (t.getVillage() != null) {
        return t.getVillage();
      }
    }
    return null;
  }
  
  public final float getDamageModifier()
  {
    return 100.0F / Math.max(1.0F, this.currentQL * (100.0F - this.damage) / 100.0F);
  }
  
  public final boolean poll(long currTime, VolaTile t, Structure struct)
  {
    if (struct == null) {
      return true;
    }
    HighwayPos highwaypos = MethodsHighways.getHighwayPos(this);
    if ((highwaypos != null) && (MethodsHighways.onHighway(highwaypos))) {
      return false;
    }
    if (currTime - struct.getCreationDate() <= 172800000L) {
      return false;
    }
    float mod = 1.0F;
    Village village = getVillage();
    if (village != null)
    {
      if (village.moreThanMonthLeft()) {
        return false;
      }
      if (!village.lessThanWeekLeft()) {
        mod *= 10.0F;
      }
    }
    else if (Zones.getKingdom(this.tilex, this.tiley) == 0)
    {
      mod *= 0.5F;
    }
    if ((t != null) && (!t.isOnSurface())) {
      mod *= 0.75F;
    }
    if (((float)(currTime - this.lastUsed) > (Servers.localServer.testServer ? 60000.0F * mod : 8.64E7F * mod)) && (!hasNoDecay()))
    {
      long ownerId = struct.getOwnerId();
      if (ownerId == -10L)
      {
        this.damage += 20.0F + Server.rand.nextFloat() * 10.0F;
      }
      else
      {
        boolean ownerIsInactive = false;
        long aMonth = Servers.isThisATestServer() ? 86400000L : 2419200000L;
        PlayerInfo pInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(ownerId);
        if (pInfo == null) {
          ownerIsInactive = true;
        } else if ((pInfo.lastLogin == 0L) && (pInfo.lastLogout < System.currentTimeMillis() - 3L * aMonth)) {
          ownerIsInactive = true;
        }
        if (ownerIsInactive) {
          this.damage += 3.0F;
        }
        if ((village == null) && (t != null))
        {
          Village v = Villages.getVillageWithPerimeterAt(t.tilex, t.tiley, t.isOnSurface());
          if (v != null) {
            if (!v.isCitizen(ownerId)) {
              if (ownerIsInactive) {
                this.damage += 3.0F;
              }
            }
          }
        }
      }
      setLastUsed(currTime);
      if (setDamage(this.damage + 0.1F * getDamageModifier())) {
        return true;
      }
    }
    return false;
  }
  
  public final float getCurrentQualityLevel()
  {
    return this.currentQL * Math.max(1.0F, 100.0F - this.damage) / 100.0F;
  }
  
  public final int getRepairItemTemplate()
  {
    if (isWood()) {
      return 22;
    }
    if (isStone()) {
      return 132;
    }
    if (isSlate()) {
      return 770;
    }
    if (isMarble()) {
      return 786;
    }
    if (isSandstone()) {
      return 1121;
    }
    if (isGold()) {
      return 44;
    }
    if (isSilver()) {
      return 45;
    }
    if (isIron()) {
      return 46;
    }
    if (isSteel()) {
      return 205;
    }
    if (isCopper()) {
      return 47;
    }
    if (isThatch()) {
      return 756;
    }
    if (isClay()) {
      return 130;
    }
    return 22;
  }
  
  public final int getStartX()
  {
    return getTileX();
  }
  
  public final int getStartY()
  {
    return getTileY();
  }
  
  public final int getMinX()
  {
    return getTileX();
  }
  
  public final int getMinY()
  {
    return getTileY();
  }
  
  public final boolean supports()
  {
    return true;
  }
  
  public final boolean supports(StructureSupport support)
  {
    if (!supports()) {
      return false;
    }
    if (support.isFloor())
    {
      if (getFloorLevel() == support.getFloorLevel()) {
        if (getStartX() == support.getStartX())
        {
          if ((getEndY() == support.getStartY()) || (getStartY() == support.getEndY())) {
            return true;
          }
        }
        else if (getStartY() == support.getStartY()) {
          if ((getEndX() == support.getStartX()) || (getStartX() == support.getEndX())) {
            return true;
          }
        }
      }
    }
    else if (!support.supports())
    {
      if (support.getFloorLevel() == getFloorLevel()) {
        return isOnSideOfThis(support);
      }
    }
    else if ((support.getFloorLevel() >= getFloorLevel() - 1) && (support.getFloorLevel() <= getFloorLevel())) {
      return isOnSideOfThis(support);
    }
    return false;
  }
  
  public float getFloorZ()
  {
    return this.heightOffset / 10.0F;
  }
  
  public float getMinZ()
  {
    return Zones.getHeightForNode(this.tilex, this.tiley, getLayer()) + getFloorZ();
  }
  
  public float getMaxZ()
  {
    return getMinZ() + (isRoof() ? 2.0F : 0.25F);
  }
  
  public boolean isWithinZ(float maxZ, float minZ, boolean followGround)
  {
    return (getFloorLevel() > 0) && (minZ <= getMaxZ()) && (maxZ >= getMinZ());
  }
  
  public final boolean equals(StructureSupport support)
  {
    if (this == support) {
      return true;
    }
    if (support == null) {
      return false;
    }
    return support.getId() == getId();
  }
  
  public final boolean equals(Object other)
  {
    if (this == other) {
      return true;
    }
    if (other == null) {
      return false;
    }
    if (getClass() != other.getClass()) {
      return false;
    }
    Floor support = (Floor)other;
    return support.getId() == getId();
  }
  
  public int hashCode()
  {
    int prime = 31;
    int result = 1;
    result = 31 * result + (int)getId();
    return result;
  }
  
  private final boolean isOnSideOfThis(StructureSupport support)
  {
    if (support.isHorizontal())
    {
      if ((support.getMinX() == getMinX()) && (
        (support.getMinY() == getMinY()) || (support.getMinY() == getMinY() + 1))) {
        return true;
      }
    }
    else if ((support.getMinY() == getMinY()) && (
      (support.getMinX() == getMinX()) || (support.getMinX() == getMinX() + 1))) {
      return true;
    }
    return false;
  }
  
  public final int getEndX()
  {
    return getStartX() + 1;
  }
  
  public final int getEndY()
  {
    return getStartY() + 1;
  }
  
  public boolean isSupportedByGround()
  {
    return getFloorLevel() == 0;
  }
  
  public String toString()
  {
    return "Floor [number=" + this.number + ", structureId=" + this.structureId + ", type=" + this.type + "]";
  }
  
  public long getTempId()
  {
    return -10L;
  }
  
  public boolean canBeAlwaysLit()
  {
    return false;
  }
  
  public boolean canBeAutoFilled()
  {
    return false;
  }
  
  public boolean canBeAutoLit()
  {
    return false;
  }
  
  public final boolean canBePeggedByPlayer()
  {
    return false;
  }
  
  public boolean canBePlanted()
  {
    return false;
  }
  
  public final boolean canBeSealedByPlayer()
  {
    return false;
  }
  
  public boolean canChangeCreator()
  {
    return false;
  }
  
  public boolean canDisableDecay()
  {
    return true;
  }
  
  public boolean canDisableDestroy()
  {
    return true;
  }
  
  public boolean canDisableDrag()
  {
    return false;
  }
  
  public boolean canDisableDrop()
  {
    return false;
  }
  
  public boolean canDisableEatAndDrink()
  {
    return false;
  }
  
  public boolean canDisableImprove()
  {
    return true;
  }
  
  public boolean canDisableLocking()
  {
    return false;
  }
  
  public boolean canDisableLockpicking()
  {
    return false;
  }
  
  public boolean canDisableMoveable()
  {
    return false;
  }
  
  public final boolean canDisableOwnerMoveing()
  {
    return false;
  }
  
  public final boolean canDisableOwnerTurning()
  {
    return false;
  }
  
  public boolean canDisablePainting()
  {
    return false;
  }
  
  public boolean canDisablePut()
  {
    return false;
  }
  
  public boolean canDisableRepair()
  {
    return true;
  }
  
  public boolean canDisableRuneing()
  {
    return false;
  }
  
  public boolean canDisableSpellTarget()
  {
    return false;
  }
  
  public boolean canDisableTake()
  {
    return false;
  }
  
  public boolean canDisableTurning()
  {
    return true;
  }
  
  public boolean canHaveCourier()
  {
    return false;
  }
  
  public boolean canHaveDakrMessenger()
  {
    return false;
  }
  
  public String getCreatorName()
  {
    return null;
  }
  
  public float getDamage()
  {
    return this.damage;
  }
  
  public float getQualityLevel()
  {
    return this.currentQL;
  }
  
  public boolean hasCourier()
  {
    return this.permissions.hasPermission(Permissions.Allow.HAS_COURIER.getBit());
  }
  
  public boolean hasDarkMessenger()
  {
    return this.permissions.hasPermission(Permissions.Allow.HAS_DARK_MESSENGER.getBit());
  }
  
  public boolean hasNoDecay()
  {
    return this.permissions.hasPermission(Permissions.Allow.DECAY_DISABLED.getBit());
  }
  
  public boolean isAlwaysLit()
  {
    return this.permissions.hasPermission(Permissions.Allow.ALWAYS_LIT.getBit());
  }
  
  public boolean isAutoFilled()
  {
    return this.permissions.hasPermission(Permissions.Allow.AUTO_FILL.getBit());
  }
  
  public boolean isAutoLit()
  {
    return this.permissions.hasPermission(Permissions.Allow.AUTO_LIGHT.getBit());
  }
  
  public boolean isIndestructible()
  {
    return this.permissions.hasPermission(Permissions.Allow.NO_BASH.getBit());
  }
  
  public boolean isNoDrag()
  {
    return this.permissions.hasPermission(Permissions.Allow.NO_DRAG.getBit());
  }
  
  public boolean isNoDrop()
  {
    return this.permissions.hasPermission(Permissions.Allow.NO_DROP.getBit());
  }
  
  public boolean isNoEatOrDrink()
  {
    return this.permissions.hasPermission(Permissions.Allow.NO_EAT_OR_DRINK.getBit());
  }
  
  public boolean isNoImprove()
  {
    return this.permissions.hasPermission(Permissions.Allow.NO_IMPROVE.getBit());
  }
  
  public boolean isNoMove()
  {
    return this.permissions.hasPermission(Permissions.Allow.NOT_MOVEABLE.getBit());
  }
  
  public boolean isNoPut()
  {
    return this.permissions.hasPermission(Permissions.Allow.NO_PUT.getBit());
  }
  
  public boolean isNoRepair()
  {
    return this.permissions.hasPermission(Permissions.Allow.NO_REPAIR.getBit());
  }
  
  public boolean isNoTake()
  {
    return this.permissions.hasPermission(Permissions.Allow.NO_TAKE.getBit());
  }
  
  public boolean isNotLockable()
  {
    return this.permissions.hasPermission(Permissions.Allow.NOT_LOCKABLE.getBit());
  }
  
  public boolean isNotLockpickable()
  {
    return this.permissions.hasPermission(Permissions.Allow.NOT_LOCKPICKABLE.getBit());
  }
  
  public boolean isNotPaintable()
  {
    return this.permissions.hasPermission(Permissions.Allow.NOT_PAINTABLE.getBit());
  }
  
  public boolean isNotRuneable()
  {
    return true;
  }
  
  public boolean isNotSpellTarget()
  {
    return this.permissions.hasPermission(Permissions.Allow.NO_SPELLS.getBit());
  }
  
  public boolean isNotTurnable()
  {
    return this.permissions.hasPermission(Permissions.Allow.NOT_TURNABLE.getBit());
  }
  
  public boolean isOwnerMoveable()
  {
    return this.permissions.hasPermission(Permissions.Allow.OWNER_MOVEABLE.getBit());
  }
  
  public boolean isOwnerTurnable()
  {
    return this.permissions.hasPermission(Permissions.Allow.OWNER_TURNABLE.getBit());
  }
  
  public boolean isPlanted()
  {
    return this.permissions.hasPermission(Permissions.Allow.PLANTED.getBit());
  }
  
  public final boolean isSealedByPlayer()
  {
    if (this.permissions.hasPermission(Permissions.Allow.SEALED_BY_PLAYER.getBit())) {
      return true;
    }
    return false;
  }
  
  public void setCreator(String aNewCreator) {}
  
  public abstract boolean setDamage(float paramFloat);
  
  public void setHasCourier(boolean aCourier)
  {
    this.permissions.setPermissionBit(Permissions.Allow.HAS_COURIER.getBit(), aCourier);
  }
  
  public void setHasDarkMessenger(boolean aDarkmessenger)
  {
    this.permissions.setPermissionBit(Permissions.Allow.HAS_DARK_MESSENGER.getBit(), aDarkmessenger);
  }
  
  public void setHasNoDecay(boolean aNoDecay)
  {
    this.permissions.setPermissionBit(Permissions.Allow.DECAY_DISABLED.getBit(), aNoDecay);
  }
  
  public void setIsAlwaysLit(boolean aAlwaysLit)
  {
    this.permissions.setPermissionBit(Permissions.Allow.ALWAYS_LIT.getBit(), aAlwaysLit);
  }
  
  public void setIsAutoFilled(boolean aAutoFill)
  {
    this.permissions.setPermissionBit(Permissions.Allow.AUTO_FILL.getBit(), aAutoFill);
  }
  
  public void setIsAutoLit(boolean aAutoLight)
  {
    this.permissions.setPermissionBit(Permissions.Allow.AUTO_LIGHT.getBit(), aAutoLight);
  }
  
  public void setIsIndestructible(boolean aNoDestroy)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_BASH.getBit(), aNoDestroy);
  }
  
  public void setIsNoDrag(boolean aNoDrag)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_DRAG.getBit(), aNoDrag);
  }
  
  public void setIsNoDrop(boolean aNoDrop)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_DROP.getBit(), aNoDrop);
  }
  
  public void setIsNoEatOrDrink(boolean aNoEatOrDrink)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_EAT_OR_DRINK.getBit(), aNoEatOrDrink);
  }
  
  public void setIsNoImprove(boolean aNoImprove)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_IMPROVE.getBit(), aNoImprove);
  }
  
  public void setIsNoMove(boolean aNoMove)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NOT_MOVEABLE.getBit(), aNoMove);
  }
  
  public void setIsNoPut(boolean aNoPut)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_PUT.getBit(), aNoPut);
  }
  
  public void setIsNoRepair(boolean aNoRepair)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_REPAIR.getBit(), aNoRepair);
  }
  
  public void setIsNoTake(boolean aNoTake)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_TAKE.getBit(), aNoTake);
  }
  
  public void setIsNotLockable(boolean aNoLock)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NOT_LOCKABLE.getBit(), aNoLock);
  }
  
  public void setIsNotLockpickable(boolean aNoLockpick)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NOT_LOCKPICKABLE.getBit(), aNoLockpick);
  }
  
  public void setIsNotPaintable(boolean aNoPaint)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NOT_PAINTABLE.getBit(), aNoPaint);
  }
  
  public void setIsNotRuneable(boolean aNoRune)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NOT_RUNEABLE.getBit(), aNoRune);
  }
  
  public void setIsNotSpellTarget(boolean aNoSpells)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_SPELLS.getBit(), aNoSpells);
  }
  
  public void setIsNotTurnable(boolean aNoTurn)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NOT_TURNABLE.getBit(), aNoTurn);
  }
  
  public void setIsOwnerMoveable(boolean aOwnerMove)
  {
    this.permissions.setPermissionBit(Permissions.Allow.OWNER_MOVEABLE.getBit(), aOwnerMove);
  }
  
  public void setIsOwnerTurnable(boolean aOwnerTurn)
  {
    this.permissions.setPermissionBit(Permissions.Allow.OWNER_TURNABLE.getBit(), aOwnerTurn);
  }
  
  public void setIsPlanted(boolean aPlant)
  {
    this.permissions.setPermissionBit(Permissions.Allow.PLANTED.getBit(), aPlant);
  }
  
  public void setIsSealedByPlayer(boolean aSealed)
  {
    this.permissions.setPermissionBit(Permissions.Allow.SEALED_BY_PLAYER.getBit(), aSealed);
  }
  
  public abstract boolean setQualityLevel(float paramFloat);
  
  public void setOriginalQualityLevel(float newQL) {}
  
  public abstract void savePermissions();
  
  public final boolean isOnSouthBorder(TilePos pos)
  {
    return false;
  }
  
  public final boolean isOnNorthBorder(TilePos pos)
  {
    return false;
  }
  
  public final boolean isOnWestBorder(TilePos pos)
  {
    return false;
  }
  
  public final boolean isOnEastBorder(TilePos pos)
  {
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\structures\Floor.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */