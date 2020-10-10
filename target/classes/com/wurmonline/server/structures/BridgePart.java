package com.wurmonline.server.structures;

import com.wurmonline.math.Vector3f;
import com.wurmonline.mesh.MeshIO;
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
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.BridgeConstants.BridgeMaterial;
import com.wurmonline.shared.constants.BridgeConstants.BridgeState;
import com.wurmonline.shared.constants.BridgeConstants.BridgeType;
import com.wurmonline.shared.constants.SoundNames;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BridgePart
  implements MiscConstants, TimeConstants, Blocker, IFloor, SoundNames, Permissions.IAllow
{
  private static final Logger logger = Logger.getLogger(Wall.class.getName());
  private long structureId = -10L;
  private int number = -10;
  float originalQL;
  float currentQL;
  float damage;
  private int tilex;
  private int tiley;
  private int realHeight;
  long lastUsed;
  private BridgeConstants.BridgeType type;
  private BridgeConstants.BridgeMaterial material;
  private BridgeConstants.BridgeState bridgePartState;
  protected byte dbState = -1;
  private byte dir = 0;
  private byte slope = 0;
  private int northExit = -1;
  private int eastExit = -1;
  private int southExit = -1;
  private int westExit = -1;
  byte roadType = 0;
  int layer = 0;
  private int materialCount = -1;
  private static final Set<DbBridgePart> bridgeParts = new HashSet();
  private static final String GETALLBRIDGEPARTS = "SELECT * FROM BRIDGEPARTS";
  private static final Vector3f normal = new Vector3f(0.0F, 0.0F, 1.0F);
  private static Rectangle2D verticalBlocker;
  Permissions permissions = new Permissions();
  private Vector3f centerPoint;
  
  public BridgePart(int id, BridgeConstants.BridgeType floorType, int aTileX, int aTileY, byte aDbState, int aHeightOffset, float ql, long structure, BridgeConstants.BridgeMaterial floorMaterial, float origQl, float dam, int materialcount, long lastmaint, byte aDir, byte aSlope, int aNorthExit, int aEastExit, int aSouthExit, int aWestExit, byte roadType, int layer)
  {
    setNumber(id);
    this.type = floorType;
    this.tilex = aTileX;
    this.tiley = aTileY;
    this.dbState = aDbState;
    this.bridgePartState = BridgeConstants.BridgeState.fromByte(this.dbState);
    this.realHeight = aHeightOffset;
    this.currentQL = ql;
    this.originalQL = origQl;
    this.damage = dam;
    this.structureId = structure;
    this.material = floorMaterial;
    this.materialCount = materialcount;
    this.lastUsed = lastmaint;
    this.dir = aDir;
    this.slope = aSlope;
    this.northExit = aNorthExit;
    this.eastExit = aEastExit;
    this.southExit = aSouthExit;
    this.westExit = aWestExit;
    this.roadType = roadType;
    this.layer = layer;
  }
  
  public BridgePart(BridgeConstants.BridgeType floorType, int aTileX, int aTileY, int height, float ql, long structure, BridgeConstants.BridgeMaterial floorMaterial, byte aDir, byte aSlope, int aNorthExit, int aEastExit, int aSouthExit, int aWestExit, byte roadType, int layer)
  {
    this.type = floorType;
    this.tilex = aTileX;
    this.tiley = aTileY;
    this.bridgePartState = BridgeConstants.BridgeState.PLANNED;
    this.dbState = this.bridgePartState.getCode();
    this.damage = 0.0F;
    this.realHeight = height;
    this.currentQL = ql;
    this.originalQL = ql;
    this.structureId = structure;
    this.material = floorMaterial;
    this.materialCount = 0;
    this.dir = aDir;
    this.slope = aSlope;
    this.northExit = aNorthExit;
    this.eastExit = aEastExit;
    this.southExit = aSouthExit;
    this.westExit = aWestExit;
    this.roadType = roadType;
    this.layer = layer;
    try
    {
      save();
    }
    catch (IOException e)
    {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
  }
  
  public boolean isFloor()
  {
    return true;
  }
  
  public boolean isRoof()
  {
    return false;
  }
  
  public final boolean isStair()
  {
    return false;
  }
  
  public final Vector3f getNormal()
  {
    return normal;
  }
  
  private final Vector3f calculateCenterPoint()
  {
    return new Vector3f(this.tilex * 4 + 2, this.tiley * 4 + 2, (getRealHeight() + this.slope) / 10.0F);
  }
  
  private final Rectangle2D getVerticalBlocker()
  {
    if (this.slope == 0) {
      return null;
    }
    if (verticalBlocker == null)
    {
      if ((this.dir == 0) || (this.dir == 4)) {
        if (this.slope < 0) {
          verticalBlocker = new Rectangle2D.Float(this.tilex * 4, this.tiley * 4, 4.0F, Math.abs(this.slope / 10.0F));
        } else {
          verticalBlocker = new Rectangle2D.Float(this.tilex * 4, (this.tiley + 1) * 4, 4.0F, Math.abs(this.slope / 10.0F));
        }
      }
      if ((this.dir == 6) || (this.dir == 2)) {
        if (this.slope < 0) {
          verticalBlocker = new Rectangle2D.Float(this.tilex * 4, this.tiley * 4, 4.0F, Math.abs(this.slope / 10.0F));
        } else {
          verticalBlocker = new Rectangle2D.Float((this.tilex + 1) * 4, this.tiley * 4, 4.0F, Math.abs(this.slope / 10.0F));
        }
      }
    }
    return verticalBlocker;
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
  
  public int getTileY()
  {
    return this.tiley;
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
    return this.realHeight;
  }
  
  public int getHeight()
  {
    if (isOnSurface())
    {
      int ht = Tiles.decodeHeight(Server.surfaceMesh.getTile(this.tilex, this.tiley));
      return this.realHeight - ht;
    }
    int ht = Tiles.decodeHeight(Server.caveMesh.getTile(this.tilex, this.tiley));
    return this.realHeight - ht;
  }
  
  public int getRealHeight()
  {
    return this.realHeight;
  }
  
  public byte getDir()
  {
    return this.dir;
  }
  
  public byte getSlope()
  {
    return this.slope;
  }
  
  public boolean hasHouseNorthExit()
  {
    return this.northExit > 0;
  }
  
  public boolean hasHouseEastExit()
  {
    return this.eastExit > 0;
  }
  
  public boolean hasHouseSouthExit()
  {
    return this.southExit > 0;
  }
  
  public boolean hasHouseWestExit()
  {
    return this.westExit > 0;
  }
  
  public boolean hasHouseExit()
  {
    return (hasHouseNorthExit()) || (hasHouseEastExit()) || (hasHouseSouthExit()) || (hasHouseWestExit());
  }
  
  public boolean hasNorthExit()
  {
    return this.northExit > -1;
  }
  
  public boolean hasEastExit()
  {
    return this.eastExit > -1;
  }
  
  public boolean hasSouthExit()
  {
    return this.southExit > -1;
  }
  
  public boolean hasWestExit()
  {
    return this.westExit > -1;
  }
  
  public boolean hasAnExit()
  {
    return (hasNorthExit()) || (hasEastExit()) || (hasSouthExit()) || (hasWestExit());
  }
  
  public boolean isFinished()
  {
    return this.bridgePartState == BridgeConstants.BridgeState.COMPLETED;
  }
  
  public final boolean isMetal()
  {
    return false;
  }
  
  public final boolean isWood()
  {
    return this.material == BridgeConstants.BridgeMaterial.WOOD;
  }
  
  public final boolean isStone()
  {
    return this.material == BridgeConstants.BridgeMaterial.BRICK;
  }
  
  public final boolean isSlate()
  {
    return false;
  }
  
  public final boolean isThatch()
  {
    return false;
  }
  
  public final boolean isMarble()
  {
    return this.material == BridgeConstants.BridgeMaterial.MARBLE;
  }
  
  public final boolean isSandstone()
  {
    return this.material == BridgeConstants.BridgeMaterial.SANDSTONE;
  }
  
  public BridgeConstants.BridgeType getType()
  {
    return this.type;
  }
  
  public BridgeConstants.BridgeMaterial getMaterial()
  {
    return this.material;
  }
  
  public int minRequiredSkill()
  {
    switch (BridgePart.1.$SwitchMap$com$wurmonline$shared$constants$BridgeConstants$BridgeMaterial[this.material.ordinal()])
    {
    case 1: 
      return 10;
    case 2: 
      return 10;
    case 3: 
      return 30;
    case 4: 
    case 5: 
    case 6: 
    case 7: 
    case 8: 
    case 9: 
      return 40;
    }
    return 99;
  }
  
  public abstract void save()
    throws IOException;
  
  public long getId()
  {
    return Tiles.getBridgePartId(this.tilex, this.tiley, this.realHeight, (byte)this.layer, (byte)0);
  }
  
  public static int getHeightOffsetFromWurmId(long wurmId)
  {
    return Tiles.decodeHeightOffset(wurmId);
  }
  
  public BridgeConstants.BridgeState getBridgePartState()
  {
    return this.bridgePartState;
  }
  
  public String getFloorStageAsString()
  {
    return this.bridgePartState.getDescription();
  }
  
  public long getLastUsed()
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
  
  public void incBridgePartStage()
  {
    byte currentStage = this.bridgePartState.getCode();
    byte nextStage = (byte)(currentStage + 1);
    this.bridgePartState = BridgeConstants.BridgeState.fromByte(nextStage);
    setState(nextStage);
    setMaterialCount(0);
  }
  
  public void setBridgePartState(BridgeConstants.BridgeState aBridgeState)
  {
    if ((aBridgeState.isBeingBuilt()) && (this.bridgePartState == BridgeConstants.BridgeState.PLANNED)) {
      setDamage(0.0F);
    }
    this.bridgePartState = aBridgeState;
    switch (BridgePart.1.$SwitchMap$com$wurmonline$shared$constants$BridgeConstants$BridgeState[this.bridgePartState.ordinal()])
    {
    case 1: 
      setState(BridgeConstants.BridgeState.COMPLETED.getCode());
      break;
    case 2: 
      setState(BridgeConstants.BridgeState.PLANNED.getCode());
      break;
    default: 
      setState(this.bridgePartState.getCode());
    }
  }
  
  public int getMaterialCount()
  {
    return this.materialCount;
  }
  
  public int getNorthExit()
  {
    return this.northExit;
  }
  
  public int getEastExit()
  {
    return this.eastExit;
  }
  
  public int getSouthExit()
  {
    return this.southExit;
  }
  
  public int getWestExit()
  {
    return this.westExit;
  }
  
  public int getNorthExitFloorLevel()
  {
    if (this.northExit == -1) {
      return -1;
    }
    return this.northExit / 30;
  }
  
  public int getEastExitFloorLevel()
  {
    if (this.eastExit == -1) {
      return -1;
    }
    return this.eastExit / 30;
  }
  
  public int getSouthExitFloorLevel()
  {
    if (this.southExit == -1) {
      return -1;
    }
    return this.southExit / 30;
  }
  
  public int getWestExitFloorLevel()
  {
    if (this.westExit == -1) {
      return -1;
    }
    return this.westExit / 30;
  }
  
  public void setMaterialCount(int count)
  {
    this.materialCount = count;
  }
  
  public byte getRoadType()
  {
    return this.roadType;
  }
  
  public byte getLayer()
  {
    return (byte)this.layer;
  }
  
  public abstract void saveRoadType(byte paramByte);
  
  public abstract void delete();
  
  public String getFullName()
  {
    if (this.bridgePartState == BridgeConstants.BridgeState.PLANNED) {
      return "Planned " + getName();
    }
    if (this.bridgePartState.isBeingBuilt()) {
      return "Unfinished " + getName();
    }
    return getName();
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
    Vector3f inter = getIntersectionPoint(startPos, endPos, aNormal, creature, blockType);
    return inter;
  }
  
  public final boolean isDoor()
  {
    return false;
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
    return Math.min(100, Math.max(0, this.bridgePartState.getCode() * 14));
  }
  
  public final boolean isWithinFloorLevels(int maxFloorLevel, int minFloorLevel)
  {
    int maxHt = (maxFloorLevel + 1) * 30;
    int minHt = minFloorLevel * 30;
    
    int ht = getRealHeight();
    if ((getType().isSupportType()) && (ht > maxHt)) {
      return true;
    }
    return (ht > minHt) && (ht < maxHt);
  }
  
  public float getFloorZ()
  {
    return 0.0F;
  }
  
  public float getMinZ()
  {
    return getRealHeight() / 10.0F;
  }
  
  public float getMaxZ()
  {
    return getMinZ() + 0.25F + this.slope / 10.0F;
  }
  
  public boolean isWithinZ(float maxZ, float minZ, boolean followGround)
  {
    return (minZ <= getMaxZ()) && (maxZ >= getMinZ());
  }
  
  public final Vector3f getFloorIntersection(Vector3f startPos, Vector3f endPos, Vector3f aNormal, Creature creature, int blockType)
  {
    Vector3f diff = getCenterPoint().subtract(startPos);
    float steps = diff.z / aNormal.z;
    Vector3f intersection = startPos.add(aNormal.mult(steps));
    Vector3f diffend = endPos.subtract(startPos);
    Vector3f interDiff = intersection.subtract(startPos);
    if (diffend.length() < interDiff.length()) {
      return null;
    }
    float u = getNormal().dot(getCenterPoint().subtract(startPos)) / getNormal().dot(endPos.subtract(startPos));
    if (isWithinFloorBounds(intersection, creature, blockType))
    {
      if ((u >= 0.0F) && (u <= 1.0F)) {
        return intersection;
      }
      return null;
    }
    return null;
  }
  
  public final Vector3f getVerticalIntersection(Vector3f startPos, Vector3f endPos, Vector3f aNormal, Creature creature)
  {
    if (getFloorLevel() == 0) {
      if (startPos.z <= getMinZ()) {
        startPos.z = (getMinZ() + 0.5F);
      }
    }
    Vector3f diff = getCenterPoint().subtract(startPos);
    
    Vector3f diffend = endPos.subtract(startPos);
    if (isHorizontal())
    {
      float steps = diff.y / normal.y;
      Vector3f intersection = startPos.add(normal.mult(steps));
      Vector3f interDiff = intersection.subtract(startPos);
      if (diffend.length() + 0.01F < interDiff.length()) {
        return null;
      }
      if (isWithinVerticalBounds(intersection, creature))
      {
        float u = getNormal().dot(getCenterPoint().subtract(startPos)) / getNormal().dot(endPos.subtract(startPos));
        if ((u >= 0.0F) && (u <= 1.0F)) {
          return intersection;
        }
        return null;
      }
    }
    else
    {
      float steps = diff.x / normal.x;
      Vector3f intersection = startPos.add(normal.mult(steps));
      Vector3f interDiff = intersection.subtract(startPos);
      if (diffend.length() < interDiff.length()) {
        return null;
      }
      if (isWithinVerticalBounds(intersection, creature))
      {
        float u = getNormal().dot(getCenterPoint().subtract(startPos)) / getNormal().dot(endPos.subtract(startPos));
        if ((u >= 0.0F) && (u <= 1.0F)) {
          return intersection;
        }
        return null;
      }
    }
    return null;
  }
  
  public final Vector3f getIntersectionPoint(Vector3f startPos, Vector3f endPos, Vector3f aNormal, Creature creature, int blockType)
  {
    Vector3f intersection = getFloorIntersection(startPos, endPos, aNormal, creature, blockType);
    if (intersection == null)
    {
      if (blockType == 6) {
        return null;
      }
      getVerticalIntersection(startPos, endPos, aNormal, creature);
    }
    return intersection;
  }
  
  private final boolean isWithinFloorBounds(Vector3f pointToCheck, Creature creature, int blockType)
  {
    if ((pointToCheck.getY() >= this.tiley * 4) && 
      (pointToCheck.getY() <= (this.tiley + 1) * 4)) {
      if ((pointToCheck.getX() >= this.tilex * 4) && (pointToCheck.getX() <= (this.tilex + 1) * 4))
      {
        if (Servers.isThisATestServer()) {
          logger.info("WithinBounds?:" + getName() + " height checked:" + pointToCheck.getZ() + " against bridge real height:" + 
            getRealHeight() / 10.0F + " (" + this.tilex + "," + this.tiley + ")");
        }
        if ((getType().isSupportType()) && (blockType == 4))
        {
          if (pointToCheck.getZ() <= getMinZ() + 0.25F) {
            return true;
          }
        }
        else {
          return (pointToCheck.getZ() >= getMinZ()) && (pointToCheck.getZ() <= getMinZ() + 0.25F);
        }
      }
    }
    return false;
  }
  
  private final boolean isWithinVerticalBounds(Vector3f pointToCheck, Creature creature)
  {
    Rectangle2D rect = getVerticalBlocker();
    if (rect == null) {
      return false;
    }
    if ((this.dir == 0) || (this.dir == 4))
    {
      if ((pointToCheck.getY() >= rect.getY() - 0.10000000149011612D) && 
        (pointToCheck.getY() <= rect.getY() + 0.10000000149011612D)) {
        if ((pointToCheck.getX() >= rect.getX()) && (pointToCheck.getX() <= rect.getX() + rect.getWidth()) && 
          (pointToCheck.getZ() >= getMinZ()) && (pointToCheck.getZ() <= getMinZ() + rect.getHeight())) {
          return true;
        }
      }
    }
    else if ((pointToCheck.getX() >= rect.getX() - 0.10000000149011612D) && 
      (pointToCheck.getX() <= rect.getX() + 0.10000000149011612D)) {
      if ((pointToCheck.getY() >= rect.getY()) && (pointToCheck.getY() <= rect.getY() + rect.getWidth()) && 
        (pointToCheck.getZ() >= getMinZ()) && (pointToCheck.getZ() <= getMinZ() + rect.getHeight())) {
        return true;
      }
    }
    return false;
  }
  
  public static final void loadAllBridgeParts()
    throws IOException
  {
    logger.log(Level.INFO, "Loading all bridge parts.");
    long s = System.nanoTime();
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM BRIDGEPARTS");
      rs = ps.executeQuery();
      while (rs.next())
      {
        int id = rs.getInt("ID");
        BridgeConstants.BridgeType floorType = BridgeConstants.BridgeType.fromByte(rs.getByte("TYPE"));
        BridgeConstants.BridgeMaterial floorMaterial = BridgeConstants.BridgeMaterial.fromByte(rs.getByte("MATERIAL"));
        byte state = rs.getByte("STATE");
        
        int stageCount = rs.getInt("STAGECOUNT");
        int x = rs.getInt("TILEX");
        int y = rs.getInt("TILEY");
        long structureId = rs.getLong("STRUCTURE");
        int h = rs.getInt("HEIGHTOFFSET");
        float currentQL = rs.getFloat("CURRENTQL");
        float origQL = rs.getFloat("ORIGINALQL");
        float dam = rs.getFloat("DAMAGE");
        byte dir = rs.getByte("DIR");
        byte slope = rs.getByte("SLOPE");
        long last = rs.getLong("LASTMAINTAINED");
        int northExit = rs.getInt("NORTHEXIT");
        int eastExit = rs.getInt("EASTEXIT");
        int southExit = rs.getInt("SOUTHEXIT");
        int westExit = rs.getInt("WESTEXIT");
        byte roadType = rs.getByte("ROADTYPE");
        int layer = rs.getInt("LAYER");
        
        bridgeParts.add(new DbBridgePart(id, floorType, x, y, state, h, currentQL, structureId, floorMaterial, origQL, dam, stageCount, last, dir, slope, northExit, eastExit, southExit, westExit, roadType, layer));
      }
    }
    catch (SQLException sqx)
    {
      long e;
      logger.log(Level.WARNING, "Failed to load bridge parts!" + sqx.getMessage(), sqx);
      throw new IOException(sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
      long e = System.nanoTime();
      logger.log(Level.INFO, "Loaded " + bridgeParts.size() + " bridge parts. That took " + (float)(e - s) / 1000000.0F + " ms.");
    }
  }
  
  public static final Set<BridgePart> getBridgePartsFor(long structureId)
  {
    Set<BridgePart> toReturn = new HashSet();
    for (BridgePart bridgePart : bridgeParts) {
      if (bridgePart.getStructureId() == structureId) {
        toReturn.add(bridgePart);
      }
    }
    return toReturn;
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
    return this.bridgePartState == BridgeConstants.BridgeState.PLANNED;
  }
  
  public void revertToPlan()
  {
    MethodsHighways.removeNearbyMarkers(this);
    setBridgePartState(BridgeConstants.BridgeState.PLANNED);
    setDamage(0.0F);
    setQualityLevel(1.0F);
    saveRoadType((byte)0);
    try
    {
      save();
    }
    catch (IOException e)
    {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
    VolaTile volaTile = Zones.getOrCreateTile(getTileX(), getTileY(), this.layer == 0);
    volaTile.updateBridgePart(this);
  }
  
  public void destroyOrRevertToPlan()
  {
    revertToPlan();
  }
  
  Structure getStructure()
  {
    Structure struct = null;
    try
    {
      struct = Structures.getStructure(getStructureId());
    }
    catch (NoSuchStructureException e)
    {
      logger.log(Level.WARNING, " Failed to find Structures.getStructure(" + getStructureId() + " for a BridgePart about to be deleted: " + e.getMessage(), e);
    }
    return struct;
  }
  
  public void destroy()
  {
    delete();
    
    bridgeParts.remove(this);
    
    VolaTile volaTile = Zones.getOrCreateTile(getTileX(), getTileY(), this.layer == 0);
    volaTile.removeBridgePart(this);
  }
  
  public final float getDamageModifierForItem(Item item)
  {
    float mod;
    float mod;
    float mod;
    float mod;
    float mod;
    switch (BridgePart.1.$SwitchMap$com$wurmonline$shared$constants$BridgeConstants$BridgeMaterial[this.material.ordinal()])
    {
    case 1: 
      float mod;
      if (item.isWeaponSlash()) {
        mod = 0.03F;
      } else {
        mod = 0.007F;
      }
      break;
    case 3: 
      float mod;
      if (item.isWeaponCrush()) {
        mod = 0.01F;
      } else {
        mod = 0.002F;
      }
      break;
    case 4: 
    case 5: 
    case 6: 
    case 7: 
    case 8: 
    case 9: 
      float mod;
      if (item.isWeaponCrush()) {
        mod = 0.005F;
      } else {
        mod = 0.001F;
      }
      break;
    case 2: 
      float mod;
      if (item.isWeaponAxe()) {
        mod = 0.03F;
      } else {
        mod = 0.007F;
      }
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
    return 0;
  }
  
  public void buildProgress(int numSteps)
  {
    if (getBridgePartState().isBeingBuilt()) {
      setMaterialCount(getMaterialCount() + numSteps);
    } else {
      logger.log(Level.WARNING, "buildProgress method called on bridge part when bridge part was not in buildable state: " + 
        getId() + " " + this.bridgePartState
        .toString());
    }
  }
  
  public final VolaTile getTile()
  {
    try
    {
      Zone zone = Zones.getZone(this.tilex, this.tiley, this.layer == 0);
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
  
  public final float getModByMaterial()
  {
    switch (BridgePart.1.$SwitchMap$com$wurmonline$shared$constants$BridgeConstants$BridgeMaterial[this.material.ordinal()])
    {
    case 1: 
      return 4.0F;
    case 3: 
      return 10.0F;
    case 4: 
    case 5: 
    case 6: 
    case 7: 
    case 8: 
    case 9: 
      return 12.0F;
    case 2: 
      return 7.0F;
    }
    return 1.0F;
  }
  
  public final float getDamageModifier()
  {
    return 100.0F / Math.max(1.0F, this.currentQL * (100.0F - this.damage) / 100.0F);
  }
  
  public final boolean poll(long currTime, Structure struct)
  {
    if (struct == null) {
      return true;
    }
    if (currTime - struct.getCreationDate() <= (Servers.localServer.testServer ? 3600000L : 86400000L) * 2L) {
      return false;
    }
    if (isAPlan()) {
      return false;
    }
    HighwayPos highwaypos = MethodsHighways.getHighwayPos(this);
    if ((highwaypos != null) && (MethodsHighways.onHighway(highwaypos))) {
      return false;
    }
    float mod = 1.0F;
    Village v = getVillage();
    if (v != null)
    {
      if (v.moreThanMonthLeft()) {
        return false;
      }
      if (!v.lessThanWeekLeft()) {
        mod = 10.0F;
      }
    }
    else if (Zones.getKingdom(this.tilex, this.tiley) == 0)
    {
      mod = 0.5F;
    }
    if (((float)(currTime - this.lastUsed) > (Servers.localServer.testServer ? 8.64E7F + 60000.0F * mod : 6.048E8F + 8.64E7F * mod)) && 
      (!hasNoDecay()))
    {
      setLastUsed(currTime);
      if (setDamage(this.damage + getDamageModifier() * (0.1F + getModByMaterial() / 1000.0F))) {
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
    switch (BridgePart.1.$SwitchMap$com$wurmonline$shared$constants$BridgeConstants$BridgeMaterial[this.material.ordinal()])
    {
    case 3: 
      return 132;
    case 4: 
      return 786;
    case 5: 
      return 1123;
    case 6: 
      return 1122;
    case 7: 
      return 776;
    case 8: 
      return 1121;
    case 9: 
      return 132;
    case 1: 
      return 22;
    case 2: 
      return 22;
    }
    return 22;
  }
  
  public String getSoundByMaterial()
  {
    switch (BridgePart.1.$SwitchMap$com$wurmonline$shared$constants$BridgeConstants$BridgeMaterial[getMaterial().ordinal()])
    {
    case 3: 
    case 4: 
    case 5: 
    case 6: 
    case 7: 
    case 8: 
    case 9: 
      return "sound.work.masonry";
    case 2: 
      return Server.rand.nextInt(2) == 0 ? "sound.work.carpentry.mallet1" : "sound.work.carpentry.mallet2";
    }
    return Server.rand.nextInt(2) == 0 ? "sound.work.carpentry.mallet1" : "sound.work.carpentry.mallet2";
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
  
  public int getNumberOfExtensions()
  {
    int extensions = 0;
    if (this.type.isSupportType())
    {
      int htOff = getHeightOffset() + Math.abs(getSlope());
      int lowestCorner = (int)(Zones.getLowestCorner(getTileX(), getTileY(), 0) * 10.0F);
      
      int extensionOffset = (int)getMaterial().getExtensionOffset() * 10;
      int extensionTop = htOff - extensionOffset;
      for (int ht = extensionTop; ht > lowestCorner; ht -= 30) {
        extensions++;
      }
    }
    return extensions;
  }
  
  public final boolean supports(StructureSupport support)
  {
    if (!supports()) {
      return false;
    }
    return false;
  }
  
  public final boolean equals(StructureSupport support)
  {
    return support.getId() == getId();
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
    return true;
  }
  
  public String toString()
  {
    return "BridgePart [number=" + this.number + ", structureId=" + this.structureId + ", type=" + this.type + "]";
  }
  
  public boolean isOnSurface()
  {
    return this.layer == 0;
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
    return false;
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
  
  public String getName()
  {
    return this.material.getName() + " " + this.type.getName();
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
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\structures\BridgePart.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */