package com.wurmonline.server.structures;

import com.wurmonline.math.TilePos;
import com.wurmonline.math.Vector3f;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.TileBorderDirection;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.Permissions.Allow;
import com.wurmonline.server.players.Permissions.IAllow;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.FenceConstants;
import com.wurmonline.shared.constants.ItemMaterials;
import com.wurmonline.shared.constants.StructureConstantsEnum;
import com.wurmonline.shared.constants.StructureStateEnum;
import com.wurmonline.shared.constants.WallConstants;
import java.io.IOException;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Fence
  implements MiscConstants, FenceConstants, TimeConstants, Blocker, ItemMaterials, StructureSupport, Permissions.IAllow, CounterTypes
{
  public static final byte DIR_HORIZ = 0;
  public static final byte DIR_DOWNRIGHT = 1;
  public static final byte DIR_DOWN = 2;
  public static final byte DIR_DOWNLEFT = 3;
  private static final long lowHedgeGrowthInterval = 3L * (Servers.localServer.testServer ? 60000L : 86400000L);
  private static final long mediumHedgeGrowthInterval = 10L * (Servers.localServer.testServer ? 60000L : 86400000L);
  private final boolean deityFence;
  int number = -10;
  private static final Logger logger = Logger.getLogger(Fence.class.getName());
  float originalQL;
  float currentQL;
  float damage;
  StructureConstantsEnum type = StructureConstantsEnum.FENCE_PLAN_WOODEN;
  int tilex;
  int tiley;
  long lastUsed;
  StructureStateEnum state = StructureStateEnum.UNINITIALIZED;
  byte dir;
  int zoneId;
  private boolean surfaced = true;
  protected int color = -1;
  public static final float FENCE_DAMAGE_STATE_DIVIDER = 60.0F;
  int heightOffset = 0;
  int layer = 0;
  private static final Vector3f normalHoriz = new Vector3f(0.0F, 1.0F, 0.0F);
  private static final Vector3f normalVertical = new Vector3f(1.0F, 0.0F, 0.0F);
  private final Vector3f centerPoint;
  private int floorLevel = 0;
  Permissions permissions = new Permissions();
  private static final ConcurrentHashMap<Long, Fence> rubbleFences = new ConcurrentHashMap();
  
  public Fence(StructureConstantsEnum aType, int aTileX, int aTileY, int aHeightOffset, float aQualityLevel, Tiles.TileBorderDirection aDir, int aZoneId, int aLayer)
  {
    this.tilex = aTileX;
    this.tiley = aTileY;
    this.currentQL = aQualityLevel;
    this.originalQL = aQualityLevel;
    this.lastUsed = System.currentTimeMillis();
    this.type = aType;
    this.zoneId = aZoneId;
    this.dir = aDir.getCode();
    this.heightOffset = aHeightOffset;
    this.layer = aLayer;
    try
    {
      Zone zone = Zones.getZone(aZoneId);
      this.surfaced = zone.isOnSurface();
    }
    catch (NoSuchZoneException nsz)
    {
      logger.log(Level.WARNING, "ZoneId: " + aZoneId + ", fence: " + this.number + " - " + nsz.getMessage(), nsz);
    }
    this.deityFence = ((Servers.localServer.entryServer) && (this.originalQL > 99.0F));
    setFloorLevel();
    this.centerPoint = calculateCenterPoint();
    if (this.type == StructureConstantsEnum.FENCE_RUBBLE) {
      rubbleFences.put(Long.valueOf(getId()), this);
    }
  }
  
  private final Vector3f calculateCenterPoint()
  {
    return new Vector3f(isHorizontal() ? this.tilex * 4 + 2 : this.tilex * 4, isHorizontal() ? this.tiley * 4 : this.tiley * 4 + 2, getMinZ() + 1.5F);
  }
  
  public Fence(int aNum, StructureConstantsEnum aType, StructureStateEnum aState, int aColor, int aTileX, int aTileY, int aHeightOffset, float aQualityLevel, float aOrigQl, long aLastUsed, Tiles.TileBorderDirection aDir, int aZoneId, boolean aSurface, float aDamage, int aLayer, int aSettings)
  {
    this.number = aNum;
    this.state = aState;
    this.tilex = aTileX;
    this.tiley = aTileY;
    this.currentQL = aQualityLevel;
    this.originalQL = aOrigQl;
    this.lastUsed = aLastUsed;
    this.type = aType;
    this.zoneId = aZoneId;
    this.dir = aDir.getCode();
    this.color = aColor;
    this.surfaced = aSurface;
    this.damage = aDamage;
    this.layer = aLayer;
    this.deityFence = ((Servers.localServer.entryServer) && (this.originalQL > 99.0F));
    this.centerPoint = calculateCenterPoint();
    this.heightOffset = aHeightOffset;
    setSettings(aSettings);
    setFloorLevel();
    if (this.type == StructureConstantsEnum.FENCE_RUBBLE) {
      rubbleFences.put(Long.valueOf(getId()), this);
    }
  }
  
  public final boolean isFence()
  {
    return true;
  }
  
  public final boolean isTile()
  {
    return false;
  }
  
  public final boolean isWall()
  {
    return false;
  }
  
  public final boolean isFloor()
  {
    return false;
  }
  
  public final boolean isRoof()
  {
    return false;
  }
  
  public final boolean isStair()
  {
    return false;
  }
  
  public final Vector3f getNormal()
  {
    if (isHorizontal()) {
      return normalHoriz;
    }
    return normalVertical;
  }
  
  public final Vector3f getCenterPoint()
  {
    return this.centerPoint;
  }
  
  public final Vector3f isBlocking(Creature creature, Vector3f startPos, Vector3f endPos, Vector3f normal, int blockType, long target, boolean followGround)
  {
    if (target == getId()) {
      return null;
    }
    if (!isFinished()) {
      return null;
    }
    if (isBlocking(blockType, creature)) {
      return getIntersectionPoint(startPos, endPos, normal, creature, blockType, followGround);
    }
    return null;
  }
  
  public final Vector3f getIntersectionPoint(Vector3f startPos, Vector3f endPos, Vector3f normal, Creature c, int blockType, boolean followGround)
  {
    Vector3f spcopy = startPos.clone();
    Vector3f epcopy = endPos.clone();
    if (getFloorLevel() == 0) {
      if ((followGround) || (spcopy.z <= getMinZ()))
      {
        spcopy.z = (getMinZ() + 0.5F);
        if (followGround) {
          epcopy.z = (getMinZ() + 0.5F);
        }
      }
    }
    float u = getNormal().dot(getCenterPoint().subtract(spcopy)) / getNormal().dot(epcopy.subtract(spcopy));
    if ((u >= 0.0F) && (u <= 1.0F))
    {
      Vector3f diff = getCenterPoint().subtract(spcopy);
      if (isHorizontal())
      {
        float steps = diff.y / normal.y;
        Vector3f intersection = spcopy.add(normal.mult(steps));
        if (isWithinBounds(intersection, c, followGround)) {
          return intersection;
        }
      }
      else
      {
        float steps = diff.x / normal.x;
        Vector3f intersection = spcopy.add(normal.mult(steps));
        if (isWithinBounds(intersection, c, followGround)) {
          return intersection;
        }
      }
    }
    return null;
  }
  
  private final boolean isWithinBounds(Vector3f pointToCheck, Creature c, boolean followGround)
  {
    if (isHorizontal())
    {
      if ((pointToCheck.getY() >= this.tiley * 4 - 0.1F) && 
        (pointToCheck.getY() <= this.tiley * 4 + 0.1F)) {
        if ((pointToCheck.getX() >= this.tilex * 4) && 
          (pointToCheck.getX() <= this.tilex * 4 + 4)) {
          if (((followGround) && (getFloorLevel() == 0)) || (
            (pointToCheck.getZ() >= getMinZ()) && (pointToCheck.getZ() <= getMaxZ()))) {
            return true;
          }
        }
      }
    }
    else if ((pointToCheck.getX() >= this.tilex * 4 - 0.1F) && 
      (pointToCheck.getX() <= this.tilex * 4 + 0.1F)) {
      if ((pointToCheck.getY() >= this.tiley * 4) && 
        (pointToCheck.getY() <= this.tiley * 4 + 4)) {
        if (((followGround) && (getFloorLevel() == 0)) || (
          (pointToCheck.getZ() >= getMinZ()) && (pointToCheck.getZ() <= getMaxZ()))) {
          return true;
        }
      }
    }
    return false;
  }
  
  public boolean isLowHedge()
  {
    switch (Fence.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[this.type.ordinal()])
    {
    case 1: 
    case 2: 
    case 3: 
    case 4: 
    case 5: 
    case 6: 
    case 7: 
      return true;
    }
    return false;
  }
  
  public boolean isWalkthrough()
  {
    switch (Fence.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[this.type.ordinal()])
    {
    case 8: 
    case 9: 
    case 10: 
    case 11: 
      return true;
    }
    return isFlowerbed();
  }
  
  public boolean isFlowerbed()
  {
    switch (Fence.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[this.type.ordinal()])
    {
    case 12: 
    case 13: 
    case 14: 
    case 15: 
    case 16: 
    case 17: 
    case 18: 
      return true;
    }
    return false;
  }
  
  public boolean isLowFence()
  {
    switch (Fence.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[this.type.ordinal()])
    {
    case 8: 
    case 9: 
    case 10: 
    case 11: 
    case 19: 
    case 20: 
    case 21: 
    case 22: 
    case 23: 
    case 24: 
    case 25: 
    case 26: 
    case 27: 
    case 28: 
    case 29: 
    case 30: 
    case 31: 
    case 32: 
    case 33: 
    case 34: 
    case 35: 
    case 36: 
    case 37: 
    case 38: 
    case 39: 
    case 40: 
    case 41: 
    case 42: 
    case 43: 
    case 44: 
    case 45: 
      return true;
    }
    return false;
  }
  
  public boolean isMediumHedge()
  {
    switch (Fence.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[this.type.ordinal()])
    {
    case 46: 
    case 47: 
    case 48: 
    case 49: 
    case 50: 
    case 51: 
    case 52: 
      return true;
    }
    return false;
  }
  
  public boolean isHighHedge()
  {
    switch (Fence.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[this.type.ordinal()])
    {
    case 53: 
    case 54: 
    case 55: 
    case 56: 
    case 57: 
    case 58: 
    case 59: 
      return true;
    }
    return false;
  }
  
  public boolean isHedge()
  {
    return isHedge(this.type);
  }
  
  public static boolean isHedge(StructureConstantsEnum fenceType)
  {
    return (fenceType.value >= StructureConstantsEnum.HEDGE_FLOWER1_LOW.value) && (fenceType.value <= StructureConstantsEnum.HEDGE_FLOWER7_HIGH.value);
  }
  
  public boolean isMagic()
  {
    switch (Fence.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[this.type.ordinal()])
    {
    case 60: 
    case 61: 
    case 62: 
      return true;
    }
    return false;
  }
  
  public final int getColor()
  {
    return this.color;
  }
  
  public final Tiles.TileBorderDirection getDir()
  {
    if (this.dir == 0) {
      return Tiles.TileBorderDirection.DIR_HORIZ;
    }
    return Tiles.TileBorderDirection.DIR_DOWN;
  }
  
  public final int getDirAsByte()
  {
    return this.dir;
  }
  
  public final int getZoneId()
  {
    return this.zoneId;
  }
  
  public final int getTileX()
  {
    return this.tilex;
  }
  
  public final Village getVillage()
  {
    VolaTile t = getTile();
    if (t != null) {
      if (t.getVillage() != null) {
        return t.getVillage();
      }
    }
    t = getOtherTile();
    if (t != null) {
      if (t.getVillage() != null) {
        return t.getVillage();
      }
    }
    return null;
  }
  
  public final void pollMagicFences(long currTime)
  {
    if (isMagic()) {
      if (currTime - this.lastUsed > 1000L)
      {
        if (this.type == StructureConstantsEnum.FENCE_MAGIC_ICE)
        {
          if (Server.rand.nextInt(20) == 0)
          {
            VolaTile t = getTile();
            if (t != null)
            {
              Creature[] crets = t.getCreatures();
              for (Creature defender : crets) {
                if (!defender.isUnique())
                {
                  float dam = Math.max(3000.0F, this.damage * 100.0F);
                  defender.addWoundOfType(null, (byte)8, 0, true, 1.0F, true, dam, 0.0F, 0.0F, false, true);
                }
              }
            }
            t = getOtherTile();
            if (t != null)
            {
              Creature[] crets = t.getCreatures();
              for (Creature defender : crets) {
                if (!defender.isUnique())
                {
                  float dam = Math.max(3000.0F, this.damage * 100.0F);
                  defender.addWoundOfType(null, (byte)8, 0, true, 1.0F, true, dam, 0.0F, 0.0F, false, true);
                }
              }
            }
          }
        }
        else if (this.type == StructureConstantsEnum.FENCE_MAGIC_FIRE) {
          if (Server.rand.nextInt(20) == 0)
          {
            VolaTile t = getTile();
            if (t != null)
            {
              Creature[] crets = t.getCreatures();
              for (Creature defender : crets) {
                if (!defender.isUnique())
                {
                  float dam = Math.max(3000.0F, this.damage * 100.0F);
                  defender.addWoundOfType(null, (byte)4, 0, true, 1.0F, true, dam, 0.0F, 0.0F, false, true);
                }
              }
            }
            t = getOtherTile();
            if (t != null)
            {
              Creature[] crets = t.getCreatures();
              for (Creature defender : crets) {
                if (!defender.isUnique())
                {
                  float dam = Math.max(3000.0F, this.damage * 100.0F);
                  defender.addWoundOfType(null, (byte)4, 0, true, 1.0F, true, dam, 0.0F, 0.0F, false, true);
                }
              }
            }
          }
        }
        setLastUsed(currTime);
        if (!setDamage(getDamage() + 2.0F * (100.0F - getQualityLevel()) / 100.0F))
        {
          VolaTile tile = Zones.getTileOrNull(getTileX(), getTileY(), this.surfaced);
          if (tile != null) {
            tile.updateMagicalFence(this);
          }
        }
      }
    }
  }
  
  public final void poll(long currTime)
  {
    float mod = 1.0F;
    if (isHedge())
    {
      if (getDamage() > 0.0F) {
        setDamage(getDamage() - getQualityLevel() / 2.0F);
      }
      if (currTime - this.lastUsed > 86400000L) {
        if ((isLowHedge()) && (this.type != StructureConstantsEnum.HEDGE_FLOWER1_LOW))
        {
          if (currTime - this.lastUsed > lowHedgeGrowthInterval) {
            if (Server.rand.nextInt(10) < 7)
            {
              setDamage(0.0F);
              setType(StructureConstantsEnum.getEnumByValue((short)(this.type.value + 1)));
              try
              {
                save();
                VolaTile tile = Zones.getTileOrNull(getTileX(), getTileY(), this.surfaced);
                if (tile != null) {
                  tile.updateFence(this);
                }
              }
              catch (IOException iox)
              {
                logger.log(Level.WARNING, "Fence: " + this.number + " - " + iox.getMessage(), iox);
              }
            }
          }
        }
        else if ((isMediumHedge()) && (this.type != StructureConstantsEnum.HEDGE_FLOWER3_MEDIUM)) {
          if (currTime - this.lastUsed > mediumHedgeGrowthInterval) {
            if (Server.rand.nextInt(10) < 7)
            {
              setType(StructureConstantsEnum.getEnumByValue((short)(this.type.value + 1)));
              try
              {
                save();
                VolaTile tile = Zones.getTileOrNull(getTileX(), getTileY(), this.surfaced);
                if (tile != null) {
                  tile.updateFence(this);
                }
              }
              catch (IOException iox)
              {
                logger.log(Level.WARNING, "Fence: " + this.number + " - " + iox.getMessage(), iox);
              }
            }
          }
        }
      }
      return;
    }
    if (getType() == StructureConstantsEnum.FENCE_RUBBLE)
    {
      setDamage(this.damage + 4.0F);
      return;
    }
    Village v = getVillage();
    if (v != null)
    {
      if (v.moreThanMonthLeft()) {
        return;
      }
      if (!v.lessThanWeekLeft()) {
        mod = isFlowerbed() ? 2.0F : 10.0F;
      }
    }
    else if (Zones.getKingdom(this.tilex, this.tiley) == 0)
    {
      mod = 0.5F;
    }
    if ((float)(currTime - this.lastUsed) > 8.64E7F * mod)
    {
      setLastUsed(currTime);
      if ((!this.deityFence) && (!hasNoDecay())) {
        setDamage(this.damage + 0.1F * getDamageModifier());
      }
    }
  }
  
  public final boolean isOnPvPServer()
  {
    if (isHorizontal())
    {
      if (Zones.isOnPvPServer(this.tilex, this.tiley)) {
        return true;
      }
      if (Zones.isOnPvPServer(this.tilex, this.tiley - 1)) {
        return true;
      }
    }
    else
    {
      if (Zones.isOnPvPServer(this.tilex, this.tiley)) {
        return true;
      }
      if (Zones.isOnPvPServer(this.tilex - 1, this.tiley)) {
        return true;
      }
    }
    return false;
  }
  
  public final int getNumber()
  {
    return this.number;
  }
  
  public final float getRepairedDamage()
  {
    if ((this.type == StructureConstantsEnum.FENCE_WOODEN_GATE) || (this.type == StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE) || (this.type == StructureConstantsEnum.FENCE_PLAN_WOODEN) || (this.type == StructureConstantsEnum.FENCE_WOODEN) || (this.type == StructureConstantsEnum.FENCE_WOODEN_CRUDE_GATE) || (this.type == StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE_CRUDE)) {
      return 40.0F;
    }
    return 10.0F;
  }
  
  public final int getTileY()
  {
    return this.tiley;
  }
  
  public final float getDamageModifier()
  {
    if (isFlowerbed())
    {
      float mod = 5.0F * (this.damage / 100.0F);
      return 100.0F / Math.max(1.0F, this.currentQL * (100.0F - this.damage) / 100.0F) + mod;
    }
    return 100.0F / Math.max(1.0F, this.currentQL * (100.0F - this.damage) / 100.0F);
  }
  
  public final boolean isHorizontal()
  {
    return this.dir == 0;
  }
  
  public float getOriginalQualityLevel()
  {
    return this.originalQL;
  }
  
  public final VolaTile getTile()
  {
    try
    {
      Zone zone = Zones.getZone(this.tilex, this.tiley, this.surfaced);
      VolaTile toReturn = zone.getTileOrNull(this.tilex, this.tiley);
      if (toReturn != null)
      {
        if (toReturn.isTransition()) {
          return Zones.getZone(this.tilex, this.tiley, false).getOrCreateTile(this.tilex, this.tiley);
        }
        return toReturn;
      }
      logger.log(Level.WARNING, "Tile not in zone, this shouldn't happen " + this.tilex + ", " + this.tiley + ", fence: " + this.number);
    }
    catch (NoSuchZoneException nsz)
    {
      logger.log(Level.WARNING, "This shouldn't happen " + this.tilex + ", " + this.tiley + ", fence: " + this.number + " - " + nsz
        .getMessage(), nsz);
    }
    return null;
  }
  
  private VolaTile getOtherTile()
  {
    if (isHorizontal())
    {
      VolaTile toReturn = Zones.getOrCreateTile(this.tilex, this.tiley - 1, this.surfaced);
      return toReturn;
    }
    VolaTile toReturn = Zones.getOrCreateTile(this.tilex - 1, this.tiley, this.surfaced);
    return toReturn;
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
    Fence support = (Fence)other;
    return support.getId() == getId();
  }
  
  public int hashCode()
  {
    int prime = 31;
    int result = 1;
    result = 31 * result + (int)getId();
    return result;
  }
  
  public final long getId()
  {
    return Tiles.getBorderObjectId(getTileX(), getTileY(), getHeightOffset(), getLayer(), this.dir, (byte)7);
  }
  
  public boolean isTemporary()
  {
    return false;
  }
  
  public final long getOldId()
  {
    return (this.dir << 48) + (this.tilex << 32) + (this.tiley << 16) + 7L;
  }
  
  public final void setType(StructureConstantsEnum aType)
  {
    this.type = aType;
    this.lastUsed = System.currentTimeMillis();
  }
  
  public final StructureConstantsEnum getType()
  {
    return this.type;
  }
  
  public final StructureStateEnum getState()
  {
    return this.state;
  }
  
  public final boolean isFinished()
  {
    return this.state.state >= getFinishState().state;
  }
  
  public final float getPositionX()
  {
    if (!isHorizontal()) {
      return this.tilex * 4;
    }
    return (this.tilex * 4 + (this.tilex + 1) * 4) / 2.0F;
  }
  
  public final float getPositionY()
  {
    if (isHorizontal()) {
      return this.tiley * 4;
    }
    return (this.tiley * 4 + (this.tiley + 1) * 4) / 2.0F;
  }
  
  public final boolean isBlocking(int blockType, Creature creature)
  {
    if ((blockType == 5) && ((isLowHedge()) || (isLowFence()))) {
      return false;
    }
    if ((blockType == 4) && ((isWalkthrough()) || (isLowHedge()))) {
      return false;
    }
    if ((blockType == 6) && 
      (!WallConstants.isBlocking(getType()))) {
      return false;
    }
    if ((blockType == 6) || (blockType == 8)) {
      if (isFinished())
      {
        if (isDoor())
        {
          FenceGate gate = FenceGate.getFenceGate(getId());
          if (gate != null) {
            if (gate.canBeOpenedBy(creature, true)) {
              return false;
            }
          }
        }
      }
      else {
        return false;
      }
    }
    return isFinished();
  }
  
  public final boolean canBeOpenedBy(Creature creature, boolean wentThroughDoor)
  {
    if (isWalkthrough()) {
      return true;
    }
    if (isFinished())
    {
      if (isDoor())
      {
        FenceGate gate = FenceGate.getFenceGate(getId());
        if (gate != null) {
          if (gate.canBeOpenedBy(creature, true)) {
            return true;
          }
        }
      }
      return false;
    }
    return true;
  }
  
  public static final StructureStateEnum getFinishState(StructureConstantsEnum fenceType)
  {
    switch (Fence.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[fenceType.ordinal()])
    {
    case 63: 
    case 64: 
    case 65: 
    case 66: 
      return StructureStateEnum.STATE_10_NEEDED;
    case 67: 
    case 68: 
    case 69: 
    case 70: 
    case 71: 
    case 72: 
    case 73: 
    case 74: 
    case 75: 
    case 76: 
    case 77: 
    case 78: 
    case 79: 
    case 80: 
    case 81: 
    case 82: 
    case 83: 
    case 84: 
    case 85: 
    case 86: 
    case 87: 
    case 88: 
    case 89: 
    case 90: 
    case 91: 
    case 92: 
    case 93: 
      return StructureStateEnum.STATE_20_NEEDED;
    case 21: 
    case 33: 
    case 94: 
    case 95: 
    case 96: 
    case 97: 
    case 98: 
    case 99: 
    case 100: 
    case 101: 
    case 102: 
    case 103: 
    case 104: 
    case 105: 
      return StructureStateEnum.STATE_10_NEEDED;
    case 24: 
    case 106: 
      return StructureStateEnum.STATE_15_NEEDED;
    case 23: 
    case 25: 
    case 34: 
    case 35: 
    case 36: 
    case 37: 
    case 38: 
    case 39: 
    case 107: 
    case 108: 
    case 109: 
    case 110: 
    case 111: 
    case 112: 
    case 113: 
      return StructureStateEnum.STATE_15_NEEDED;
    case 40: 
    case 41: 
    case 42: 
    case 43: 
    case 44: 
    case 45: 
    case 114: 
    case 115: 
    case 116: 
    case 117: 
    case 118: 
    case 119: 
    case 120: 
      return StructureStateEnum.STATE_16_NEEDED;
    case 8: 
    case 121: 
      return StructureStateEnum.STATE_6_NEEDED;
    case 122: 
      return StructureStateEnum.STATE_4_NEEDED;
    case 27: 
      return StructureStateEnum.STATE_6_NEEDED;
    case 123: 
    case 124: 
    case 125: 
    case 126: 
    case 127: 
    case 128: 
    case 129: 
    case 130: 
    case 131: 
    case 132: 
    case 133: 
    case 134: 
    case 135: 
    case 136: 
    case 137: 
    case 138: 
    case 139: 
    case 140: 
    case 141: 
    case 142: 
    case 143: 
    case 144: 
    case 145: 
    case 146: 
    case 147: 
    case 148: 
      return StructureStateEnum.STATE_18_NEEDED;
    case 149: 
      return StructureStateEnum.STATE_15_NEEDED;
    }
    if (isIron(fenceType)) {
      return StructureStateEnum.STATE_11_NEEDED;
    }
    if (isWoven(fenceType)) {
      return StructureStateEnum.STATE_10_NEEDED;
    }
    return StructureStateEnum.STATE_4_NEEDED;
  }
  
  public final StructureStateEnum getFinishState()
  {
    return getFinishState(this.type);
  }
  
  public final boolean isPalisadeGate()
  {
    return this.type == StructureConstantsEnum.FENCE_PLAN_PALISADE_GATE;
  }
  
  public final void setState(StructureStateEnum newState)
  {
    if ((this.state.state >= getFinishState().state) && 
      (newState != StructureStateEnum.INITIALIZED)) {
      return;
    }
    this.state = newState;
    if (this.state.state >= getFinishState().state) {
      this.state = StructureStateEnum.FINISHED;
    }
    if (this.state.state < 0)
    {
      this.state = StructureStateEnum.FINISHED;
      logger.log(Level.WARNING, "Finish state set to " + newState + " at ", new Exception());
    }
  }
  
  public final boolean isSlate()
  {
    return (this.type == StructureConstantsEnum.FENCE_SLATE) || (this.type == StructureConstantsEnum.FENCE_SLATE_IRON) || (this.type == StructureConstantsEnum.FENCE_SLATE_IRON_GATE) || (this.type == StructureConstantsEnum.FENCE_PLAN_SLATE) || (this.type == StructureConstantsEnum.FENCE_PLAN_SLATE_IRON) || (this.type == StructureConstantsEnum.FENCE_PLAN_SLATE_IRON_GATE) || (this.type == StructureConstantsEnum.FENCE_PLAN_SLATE_TALL_STONE_WALL) || (this.type == StructureConstantsEnum.FENCE_PLAN_SLATE_PORTCULLIS) || (this.type == StructureConstantsEnum.FENCE_PLAN_SLATE_HIGH_IRON_FENCE) || (this.type == StructureConstantsEnum.FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE) || (this.type == StructureConstantsEnum.FENCE_PLAN_SLATE_STONE_PARAPET) || (this.type == StructureConstantsEnum.FENCE_PLAN_SLATE_CHAIN_FENCE) || (this.type == StructureConstantsEnum.FENCE_SLATE_TALL_STONE_WALL) || (this.type == StructureConstantsEnum.FENCE_SLATE_PORTCULLIS) || (this.type == StructureConstantsEnum.FENCE_SLATE_HIGH_IRON_FENCE) || (this.type == StructureConstantsEnum.FENCE_SLATE_HIGH_IRON_FENCE_GATE) || (this.type == StructureConstantsEnum.FENCE_SLATE_STONE_PARAPET) || (this.type == StructureConstantsEnum.FENCE_SLATE_CHAIN_FENCE);
  }
  
  public final boolean isRoundedStone()
  {
    return (this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE) || (this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE_IRON) || (this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE_IRON_GATE) || (this.type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE) || (this.type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_IRON) || (this.type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_IRON_GATE) || (this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE_TALL_STONE_WALL) || (this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE_PORTCULLIS) || (this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE_HIGH_IRON_FENCE) || (this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE) || (this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE_STONE_PARAPET) || (this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE_CHAIN_FENCE) || (this.type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL) || (this.type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_PORTCULLIS) || (this.type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE) || (this.type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE) || (this.type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET) || (this.type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE);
  }
  
  public final boolean isPottery()
  {
    return (this.type == StructureConstantsEnum.FENCE_POTTERY) || (this.type == StructureConstantsEnum.FENCE_POTTERY_IRON) || (this.type == StructureConstantsEnum.FENCE_POTTERY_IRON_GATE) || (this.type == StructureConstantsEnum.FENCE_PLAN_POTTERY) || (this.type == StructureConstantsEnum.FENCE_PLAN_POTTERY_IRON) || (this.type == StructureConstantsEnum.FENCE_PLAN_POTTERY_IRON_GATE) || (this.type == StructureConstantsEnum.FENCE_PLAN_POTTERY_TALL_STONE_WALL) || (this.type == StructureConstantsEnum.FENCE_PLAN_POTTERY_PORTCULLIS) || (this.type == StructureConstantsEnum.FENCE_PLAN_POTTERY_HIGH_IRON_FENCE) || (this.type == StructureConstantsEnum.FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE) || (this.type == StructureConstantsEnum.FENCE_PLAN_POTTERY_STONE_PARAPET) || (this.type == StructureConstantsEnum.FENCE_PLAN_POTTERY_CHAIN_FENCE) || (this.type == StructureConstantsEnum.FENCE_POTTERY_TALL_STONE_WALL) || (this.type == StructureConstantsEnum.FENCE_POTTERY_PORTCULLIS) || (this.type == StructureConstantsEnum.FENCE_POTTERY_HIGH_IRON_FENCE) || (this.type == StructureConstantsEnum.FENCE_POTTERY_HIGH_IRON_FENCE_GATE) || (this.type == StructureConstantsEnum.FENCE_POTTERY_STONE_PARAPET) || (this.type == StructureConstantsEnum.FENCE_POTTERY_CHAIN_FENCE);
  }
  
  public final boolean isSandstone()
  {
    return (this.type == StructureConstantsEnum.FENCE_SANDSTONE) || (this.type == StructureConstantsEnum.FENCE_SANDSTONE_IRON) || (this.type == StructureConstantsEnum.FENCE_SANDSTONE_IRON_GATE) || (this.type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE) || (this.type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_IRON) || (this.type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_IRON_GATE) || (this.type == StructureConstantsEnum.FENCE_SANDSTONE_TALL_STONE_WALL) || (this.type == StructureConstantsEnum.FENCE_SANDSTONE_PORTCULLIS) || (this.type == StructureConstantsEnum.FENCE_SANDSTONE_HIGH_IRON_FENCE) || (this.type == StructureConstantsEnum.FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE) || (this.type == StructureConstantsEnum.FENCE_SANDSTONE_STONE_PARAPET) || (this.type == StructureConstantsEnum.FENCE_SANDSTONE_CHAIN_FENCE) || (this.type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_TALL_STONE_WALL) || (this.type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_PORTCULLIS) || (this.type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE) || (this.type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE) || (this.type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_STONE_PARAPET) || (this.type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_CHAIN_FENCE);
  }
  
  public final boolean isPlasteredFence()
  {
    return (this.type == StructureConstantsEnum.FENCE_RENDERED) || (this.type == StructureConstantsEnum.FENCE_RENDERED_IRON) || (this.type == StructureConstantsEnum.FENCE_RENDERED_IRON_GATE) || (this.type == StructureConstantsEnum.FENCE_RENDERED_TALL_STONE_WALL) || (this.type == StructureConstantsEnum.FENCE_RENDERED_PORTCULLIS) || (this.type == StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE) || (this.type == StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE_GATE) || (this.type == StructureConstantsEnum.FENCE_RENDERED_STONE_PARAPET) || (this.type == StructureConstantsEnum.FENCE_RENDERED_CHAIN_FENCE);
  }
  
  public final boolean isPlastered()
  {
    return (this.type == StructureConstantsEnum.FENCE_RENDERED) || (this.type == StructureConstantsEnum.FENCE_RENDERED_IRON) || (this.type == StructureConstantsEnum.FENCE_RENDERED_IRON_GATE) || (this.type == StructureConstantsEnum.FENCE_PLAN_RENDERED) || (this.type == StructureConstantsEnum.FENCE_PLAN_RENDERED_IRON) || (this.type == StructureConstantsEnum.FENCE_PLAN_RENDERED_IRON_GATE) || (this.type == StructureConstantsEnum.FENCE_RENDERED_TALL_STONE_WALL) || (this.type == StructureConstantsEnum.FENCE_RENDERED_PORTCULLIS) || (this.type == StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE) || (this.type == StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE_GATE) || (this.type == StructureConstantsEnum.FENCE_RENDERED_STONE_PARAPET) || (this.type == StructureConstantsEnum.FENCE_RENDERED_CHAIN_FENCE) || (this.type == StructureConstantsEnum.FENCE_PLAN_RENDERED_TALL_STONE_WALL) || (this.type == StructureConstantsEnum.FENCE_PLAN_RENDERED_PORTCULLIS) || (this.type == StructureConstantsEnum.FENCE_PLAN_RENDERED_HIGH_IRON_FENCE) || (this.type == StructureConstantsEnum.FENCE_PLAN_RENDERED_HIGH_IRON_FENCE_GATE) || (this.type == StructureConstantsEnum.FENCE_PLAN_RENDERED_STONE_PARAPET) || (this.type == StructureConstantsEnum.FENCE_PLAN_RENDERED_CHAIN_FENCE);
  }
  
  public final boolean isMarble()
  {
    return (this.type == StructureConstantsEnum.FENCE_MARBLE) || (this.type == StructureConstantsEnum.FENCE_MARBLE_IRON) || (this.type == StructureConstantsEnum.FENCE_MARBLE_IRON_GATE) || (this.type == StructureConstantsEnum.FENCE_PLAN_MARBLE) || (this.type == StructureConstantsEnum.FENCE_PLAN_MARBLE_IRON) || (this.type == StructureConstantsEnum.FENCE_PLAN_MARBLE_IRON_GATE) || (this.type == StructureConstantsEnum.FENCE_MARBLE_TALL_STONE_WALL) || (this.type == StructureConstantsEnum.FENCE_MARBLE_PORTCULLIS) || (this.type == StructureConstantsEnum.FENCE_MARBLE_HIGH_IRON_FENCE) || (this.type == StructureConstantsEnum.FENCE_MARBLE_HIGH_IRON_FENCE_GATE) || (this.type == StructureConstantsEnum.FENCE_MARBLE_STONE_PARAPET) || (this.type == StructureConstantsEnum.FENCE_MARBLE_CHAIN_FENCE) || (this.type == StructureConstantsEnum.FENCE_PLAN_MARBLE_TALL_STONE_WALL) || (this.type == StructureConstantsEnum.FENCE_PLAN_MARBLE_PORTCULLIS) || (this.type == StructureConstantsEnum.FENCE_PLAN_MARBLE_HIGH_IRON_FENCE) || (this.type == StructureConstantsEnum.FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE) || (this.type == StructureConstantsEnum.FENCE_PLAN_MARBLE_STONE_PARAPET) || (this.type == StructureConstantsEnum.FENCE_PLAN_MARBLE_CHAIN_FENCE);
  }
  
  public final boolean isStoneFence()
  {
    return (this.type == StructureConstantsEnum.FENCE_STONE) || (this.type == StructureConstantsEnum.FENCE_IRON) || (this.type == StructureConstantsEnum.FENCE_IRON_GATE) || (this.type == StructureConstantsEnum.FENCE_STONEWALL_HIGH) || (this.type == StructureConstantsEnum.FENCE_IRON_HIGH) || (this.type == StructureConstantsEnum.FENCE_IRON_GATE_HIGH) || (this.type == StructureConstantsEnum.FENCE_STONE_PARAPET) || (this.type == StructureConstantsEnum.FENCE_PORTCULLIS) || (this.type == StructureConstantsEnum.FENCE_MEDIUM_CHAIN);
  }
  
  public final boolean isStone()
  {
    return isStone(this.type);
  }
  
  public final boolean isRubble()
  {
    return this.type == StructureConstantsEnum.FENCE_RUBBLE;
  }
  
  public static boolean isStone(StructureConstantsEnum fenceType)
  {
    switch (Fence.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[fenceType.ordinal()])
    {
    case 8: 
    case 21: 
    case 23: 
    case 25: 
    case 33: 
    case 67: 
    case 68: 
    case 69: 
    case 70: 
    case 71: 
    case 72: 
    case 73: 
    case 74: 
    case 75: 
    case 76: 
    case 77: 
    case 78: 
    case 79: 
    case 80: 
    case 81: 
    case 82: 
    case 83: 
    case 84: 
    case 85: 
    case 86: 
    case 87: 
    case 88: 
    case 89: 
    case 90: 
    case 91: 
    case 92: 
    case 93: 
    case 94: 
    case 95: 
    case 97: 
    case 99: 
    case 101: 
    case 103: 
    case 105: 
    case 107: 
    case 108: 
    case 121: 
    case 150: 
    case 151: 
      return true;
    }
    return false;
  }
  
  public final boolean isWood()
  {
    return isWood(this.type);
  }
  
  public static final boolean isWood(StructureConstantsEnum fenceType)
  {
    switch (Fence.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[fenceType.ordinal()])
    {
    case 9: 
    case 10: 
    case 19: 
    case 22: 
    case 24: 
    case 26: 
    case 27: 
    case 28: 
    case 29: 
    case 30: 
    case 31: 
    case 63: 
    case 64: 
    case 65: 
    case 66: 
    case 106: 
    case 122: 
    case 152: 
    case 153: 
    case 154: 
    case 155: 
    case 156: 
    case 157: 
    case 158: 
    case 159: 
    case 160: 
    case 161: 
      return true;
    }
    return isHedge(fenceType);
  }
  
  public final boolean isMetal()
  {
    return (this.type == StructureConstantsEnum.FENCE_IRON) || (this.type == StructureConstantsEnum.FENCE_IRON_GATE) || (this.type == StructureConstantsEnum.FENCE_IRON_HIGH) || (this.type == StructureConstantsEnum.FENCE_IRON_GATE_HIGH) || (this.type == StructureConstantsEnum.FENCE_MEDIUM_CHAIN) || (this.type == StructureConstantsEnum.FENCE_MARBLE_HIGH_IRON_FENCE) || (this.type == StructureConstantsEnum.FENCE_MARBLE_HIGH_IRON_FENCE_GATE) || (this.type == StructureConstantsEnum.FENCE_MARBLE_CHAIN_FENCE) || (this.type == StructureConstantsEnum.FENCE_SLATE_HIGH_IRON_FENCE) || (this.type == StructureConstantsEnum.FENCE_SLATE_HIGH_IRON_FENCE_GATE) || (this.type == StructureConstantsEnum.FENCE_SLATE_CHAIN_FENCE) || (this.type == StructureConstantsEnum.FENCE_POTTERY_HIGH_IRON_FENCE) || (this.type == StructureConstantsEnum.FENCE_POTTERY_HIGH_IRON_FENCE_GATE) || (this.type == StructureConstantsEnum.FENCE_POTTERY_CHAIN_FENCE) || (this.type == StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE) || (this.type == StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE_GATE) || (this.type == StructureConstantsEnum.FENCE_RENDERED_CHAIN_FENCE) || (this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE_HIGH_IRON_FENCE) || (this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE) || (this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE_CHAIN_FENCE) || (this.type == StructureConstantsEnum.FENCE_SANDSTONE_HIGH_IRON_FENCE) || (this.type == StructureConstantsEnum.FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE) || (this.type == StructureConstantsEnum.FENCE_SANDSTONE_CHAIN_FENCE);
  }
  
  public static final boolean isWoven(StructureConstantsEnum fenceType)
  {
    switch (Fence.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[fenceType.ordinal()])
    {
    case 9: 
    case 160: 
      return true;
    }
    return false;
  }
  
  public final boolean isWoven()
  {
    return isWoven(this.type);
  }
  
  public static final short[] getAllLowHedgeTypes()
  {
    return new short[] { StructureConstantsEnum.HEDGE_FLOWER1_LOW.value, StructureConstantsEnum.HEDGE_FLOWER2_LOW.value, StructureConstantsEnum.HEDGE_FLOWER3_LOW.value, StructureConstantsEnum.HEDGE_FLOWER4_LOW.value, StructureConstantsEnum.HEDGE_FLOWER5_LOW.value, StructureConstantsEnum.HEDGE_FLOWER6_LOW.value, StructureConstantsEnum.HEDGE_FLOWER7_LOW.value };
  }
  
  public static final StructureConstantsEnum getLowHedgeType(byte treeMaterial)
  {
    switch (treeMaterial)
    {
    case 46: 
      return StructureConstantsEnum.HEDGE_FLOWER1_LOW;
    case 51: 
      return StructureConstantsEnum.HEDGE_FLOWER2_LOW;
    case 50: 
      return StructureConstantsEnum.HEDGE_FLOWER3_LOW;
    case 47: 
      return StructureConstantsEnum.HEDGE_FLOWER4_LOW;
    case 48: 
      return StructureConstantsEnum.HEDGE_FLOWER5_LOW;
    case 39: 
      return StructureConstantsEnum.HEDGE_FLOWER6_LOW;
    case 41: 
      return StructureConstantsEnum.HEDGE_FLOWER7_LOW;
    }
    return StructureConstantsEnum.FENCE_PLAN_WOODEN;
  }
  
  public static final byte getMaterialForLowHedge(StructureConstantsEnum hedgeType)
  {
    switch (Fence.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[hedgeType.ordinal()])
    {
    case 1: 
      return 46;
    case 2: 
      return 51;
    case 3: 
      return 50;
    case 4: 
      return 47;
    case 5: 
      return 48;
    case 6: 
      return 39;
    case 7: 
      return 41;
    }
    return 46;
  }
  
  public static final short[] getAllFlowerbeds()
  {
    return new short[] { StructureConstantsEnum.FLOWERBED_YELLOW.value, StructureConstantsEnum.FLOWERBED_ORANGE_RED.value, StructureConstantsEnum.FLOWERBED_PURPLE.value, StructureConstantsEnum.FLOWERBED_WHITE.value, StructureConstantsEnum.FLOWERBED_BLUE.value, StructureConstantsEnum.FLOWERBED_GREENISH_YELLOW.value, StructureConstantsEnum.FLOWERBED_WHITE_DOTTED.value };
  }
  
  public static final int getFlowerTypeByFlowerbedType(StructureConstantsEnum flowerbedType)
  {
    switch (Fence.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[flowerbedType.ordinal()])
    {
    case 18: 
      return 498;
    case 14: 
      return 499;
    case 15: 
      return 500;
    case 16: 
      return 501;
    case 12: 
      return 502;
    case 13: 
      return 503;
    case 17: 
      return 504;
    }
    return 498;
  }
  
  public static final StructureConstantsEnum getFlowerbedType(int templateId)
  {
    switch (templateId)
    {
    case 498: 
      return StructureConstantsEnum.FLOWERBED_YELLOW;
    case 499: 
      return StructureConstantsEnum.FLOWERBED_ORANGE_RED;
    case 500: 
      return StructureConstantsEnum.FLOWERBED_PURPLE;
    case 501: 
      return StructureConstantsEnum.FLOWERBED_WHITE;
    case 502: 
      return StructureConstantsEnum.FLOWERBED_BLUE;
    case 503: 
      return StructureConstantsEnum.FLOWERBED_GREENISH_YELLOW;
    case 504: 
      return StructureConstantsEnum.FLOWERBED_WHITE_DOTTED;
    }
    return StructureConstantsEnum.FENCE_PLAN_WOODEN;
  }
  
  public static final StructureConstantsEnum getFencePlanType(int action)
  {
    if (action == 166) {
      return StructureConstantsEnum.FENCE_PLAN_WOODEN;
    }
    if (action == 520) {
      return StructureConstantsEnum.FENCE_PLAN_WOODEN_CRUDE;
    }
    if (action == 528) {
      return StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE_CRUDE;
    }
    if (action == 526) {
      return StructureConstantsEnum.FENCE_PLAN_GARDESGARD_LOW;
    }
    if (action == 527) {
      return StructureConstantsEnum.FENCE_PLAN_GARDESGARD_HIGH;
    }
    if (action == 529) {
      return StructureConstantsEnum.FENCE_PLAN_GARDESGARD_GATE;
    }
    if (action == 165) {
      return StructureConstantsEnum.FENCE_PLAN_PALISADE;
    }
    if (action == 163) {
      return StructureConstantsEnum.FENCE_PLAN_STONEWALL;
    }
    if (action == 167) {
      return StructureConstantsEnum.FENCE_PLAN_PALISADE_GATE;
    }
    if (action == 168) {
      return StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE;
    }
    if (action == 164) {
      return StructureConstantsEnum.FENCE_PLAN_STONEWALL_HIGH;
    }
    if (action == 477) {
      return StructureConstantsEnum.FENCE_PLAN_IRON;
    }
    if (action == 478) {
      return StructureConstantsEnum.FENCE_PLAN_WOVEN;
    }
    if (action == 479) {
      return StructureConstantsEnum.FENCE_PLAN_IRON_GATE;
    }
    if (action == 516) {
      return StructureConstantsEnum.FENCE_PLAN_WOODEN_PARAPET;
    }
    if (action == 517) {
      return StructureConstantsEnum.FENCE_PLAN_STONE_PARAPET;
    }
    if (action == 521) {
      return StructureConstantsEnum.FENCE_PLAN_STONE_IRON_PARAPET;
    }
    if (action == 541) {
      return StructureConstantsEnum.FENCE_PLAN_STONE;
    }
    if (action == 542) {
      return StructureConstantsEnum.FENCE_PLAN_CURB;
    }
    if (action == 543) {
      return StructureConstantsEnum.FENCE_PLAN_ROPE_LOW;
    }
    if (action == 544) {
      return StructureConstantsEnum.FENCE_PLAN_ROPE_HIGH;
    }
    if (action == 545) {
      return StructureConstantsEnum.FENCE_PLAN_IRON_HIGH;
    }
    if (action == 546) {
      return StructureConstantsEnum.FENCE_PLAN_IRON_GATE_HIGH;
    }
    if (action == 611) {
      return StructureConstantsEnum.FENCE_PLAN_MEDIUM_CHAIN;
    }
    if (action == 654) {
      return StructureConstantsEnum.FENCE_PLAN_PORTCULLIS;
    }
    if (action == 832) {
      return StructureConstantsEnum.FENCE_PLAN_SLATE;
    }
    if (action == 835) {
      return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE;
    }
    if (action == 838) {
      return StructureConstantsEnum.FENCE_PLAN_POTTERY;
    }
    if (action == 841) {
      return StructureConstantsEnum.FENCE_PLAN_SANDSTONE;
    }
    if (action == 844) {
      return StructureConstantsEnum.FENCE_PLAN_MARBLE;
    }
    if (action == 833) {
      return StructureConstantsEnum.FENCE_PLAN_SLATE_IRON;
    }
    if (action == 836) {
      return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_IRON;
    }
    if (action == 839) {
      return StructureConstantsEnum.FENCE_PLAN_POTTERY_IRON;
    }
    if (action == 842) {
      return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_IRON;
    }
    if (action == 845) {
      return StructureConstantsEnum.FENCE_PLAN_MARBLE_IRON;
    }
    if (action == 834) {
      return StructureConstantsEnum.FENCE_PLAN_SLATE_IRON_GATE;
    }
    if (action == 837) {
      return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_IRON_GATE;
    }
    if (action == 840) {
      return StructureConstantsEnum.FENCE_PLAN_POTTERY_IRON_GATE;
    }
    if (action == 843) {
      return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_IRON_GATE;
    }
    if (action == 846) {
      return StructureConstantsEnum.FENCE_PLAN_MARBLE_IRON_GATE;
    }
    if (action == 870) {
      return StructureConstantsEnum.FENCE_PLAN_SLATE_TALL_STONE_WALL;
    }
    if (action == 871) {
      return StructureConstantsEnum.FENCE_PLAN_SLATE_PORTCULLIS;
    }
    if (action == 872) {
      return StructureConstantsEnum.FENCE_PLAN_SLATE_HIGH_IRON_FENCE;
    }
    if (action == 873) {
      return StructureConstantsEnum.FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE;
    }
    if (action == 874) {
      return StructureConstantsEnum.FENCE_PLAN_SLATE_STONE_PARAPET;
    }
    if (action == 875) {
      return StructureConstantsEnum.FENCE_PLAN_SLATE_CHAIN_FENCE;
    }
    if (action == 876) {
      return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL;
    }
    if (action == 877) {
      return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_PORTCULLIS;
    }
    if (action == 878) {
      return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE;
    }
    if (action == 879) {
      return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE;
    }
    if (action == 880) {
      return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET;
    }
    if (action == 881) {
      return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE;
    }
    if (action == 882) {
      return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_TALL_STONE_WALL;
    }
    if (action == 883) {
      return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_PORTCULLIS;
    }
    if (action == 884) {
      return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE;
    }
    if (action == 885) {
      return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE;
    }
    if (action == 886) {
      return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_STONE_PARAPET;
    }
    if (action == 887) {
      return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_CHAIN_FENCE;
    }
    if (action == 888) {
      return StructureConstantsEnum.FENCE_PLAN_RENDERED_TALL_STONE_WALL;
    }
    if (action == 889) {
      return StructureConstantsEnum.FENCE_PLAN_RENDERED_PORTCULLIS;
    }
    if (action == 890) {
      return StructureConstantsEnum.FENCE_PLAN_RENDERED_HIGH_IRON_FENCE;
    }
    if (action == 891) {
      return StructureConstantsEnum.FENCE_PLAN_RENDERED_HIGH_IRON_FENCE_GATE;
    }
    if (action == 892) {
      return StructureConstantsEnum.FENCE_PLAN_RENDERED_STONE_PARAPET;
    }
    if (action == 893) {
      return StructureConstantsEnum.FENCE_PLAN_RENDERED_CHAIN_FENCE;
    }
    if (action == 894) {
      return StructureConstantsEnum.FENCE_PLAN_POTTERY_TALL_STONE_WALL;
    }
    if (action == 895) {
      return StructureConstantsEnum.FENCE_PLAN_POTTERY_PORTCULLIS;
    }
    if (action == 896) {
      return StructureConstantsEnum.FENCE_PLAN_POTTERY_HIGH_IRON_FENCE;
    }
    if (action == 897) {
      return StructureConstantsEnum.FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE;
    }
    if (action == 898) {
      return StructureConstantsEnum.FENCE_PLAN_POTTERY_STONE_PARAPET;
    }
    if (action == 899) {
      return StructureConstantsEnum.FENCE_PLAN_POTTERY_CHAIN_FENCE;
    }
    if (action == 900) {
      return StructureConstantsEnum.FENCE_PLAN_MARBLE_TALL_STONE_WALL;
    }
    if (action == 901) {
      return StructureConstantsEnum.FENCE_PLAN_MARBLE_PORTCULLIS;
    }
    if (action == 902) {
      return StructureConstantsEnum.FENCE_PLAN_MARBLE_HIGH_IRON_FENCE;
    }
    if (action == 903) {
      return StructureConstantsEnum.FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE;
    }
    if (action == 904) {
      return StructureConstantsEnum.FENCE_PLAN_MARBLE_STONE_PARAPET;
    }
    if (action == 905) {
      return StructureConstantsEnum.FENCE_PLAN_MARBLE_CHAIN_FENCE;
    }
    logger.log(Level.WARNING, "Fence plan for action " + action + " is not found!", new Exception());
    return StructureConstantsEnum.FENCE_PLAN_WOODEN;
  }
  
  public static final StructureConstantsEnum getFencePlanForType(StructureConstantsEnum type)
  {
    switch (Fence.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[type.ordinal()])
    {
    case 22: 
      return StructureConstantsEnum.FENCE_PLAN_WOODEN;
    case 31: 
      return StructureConstantsEnum.FENCE_PLAN_WOODEN_CRUDE;
    case 19: 
      return StructureConstantsEnum.FENCE_PLAN_GARDESGARD_LOW;
    case 157: 
      return StructureConstantsEnum.FENCE_PLAN_GARDESGARD_HIGH;
    case 28: 
      return StructureConstantsEnum.FENCE_PLAN_GARDESGARD_GATE;
    case 65: 
      return StructureConstantsEnum.FENCE_PLAN_PALISADE;
    case 21: 
      return StructureConstantsEnum.FENCE_PLAN_STONEWALL;
    case 24: 
      return StructureConstantsEnum.FENCE_PLAN_WOODEN_PARAPET;
    case 23: 
      return StructureConstantsEnum.FENCE_PLAN_STONE_PARAPET;
    case 25: 
      return StructureConstantsEnum.FENCE_PLAN_STONE_IRON_PARAPET;
    case 66: 
      return StructureConstantsEnum.FENCE_PLAN_PALISADE_GATE;
    case 29: 
      return StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE;
    case 30: 
      return StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE_CRUDE;
    case 68: 
      return StructureConstantsEnum.FENCE_PLAN_STONEWALL_HIGH;
    case 20: 
      return StructureConstantsEnum.FENCE_PLAN_IRON;
    case 9: 
      return StructureConstantsEnum.FENCE_PLAN_WOVEN;
    case 32: 
      return StructureConstantsEnum.FENCE_PLAN_IRON_GATE;
    case 33: 
      return StructureConstantsEnum.FENCE_PLAN_STONE;
    case 8: 
      return StructureConstantsEnum.FENCE_PLAN_CURB;
    case 10: 
      return StructureConstantsEnum.FENCE_PLAN_ROPE_LOW;
    case 26: 
      return StructureConstantsEnum.FENCE_PLAN_ROPE_HIGH;
    case 162: 
      return StructureConstantsEnum.FENCE_PLAN_IRON_HIGH;
    case 163: 
      return StructureConstantsEnum.FENCE_PLAN_IRON_GATE_HIGH;
    case 164: 
      return StructureConstantsEnum.FENCE_PLAN_MEDIUM_CHAIN;
    case 150: 
      return StructureConstantsEnum.FENCE_PLAN_PORTCULLIS;
    case 97: 
      return StructureConstantsEnum.FENCE_PLAN_SLATE;
    case 99: 
      return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE;
    case 101: 
      return StructureConstantsEnum.FENCE_PLAN_POTTERY;
    case 103: 
      return StructureConstantsEnum.FENCE_PLAN_SANDSTONE;
    case 151: 
      return StructureConstantsEnum.FENCE_PLAN_RENDERED;
    case 105: 
      return StructureConstantsEnum.FENCE_PLAN_MARBLE;
    case 165: 
      return StructureConstantsEnum.FENCE_PLAN_SLATE_IRON;
    case 166: 
      return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_IRON;
    case 167: 
      return StructureConstantsEnum.FENCE_PLAN_POTTERY_IRON;
    case 168: 
      return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_IRON;
    case 169: 
      return StructureConstantsEnum.FENCE_PLAN_RENDERED_IRON;
    case 170: 
      return StructureConstantsEnum.FENCE_PLAN_MARBLE_IRON;
    case 171: 
      return StructureConstantsEnum.FENCE_PLAN_SLATE_IRON_GATE;
    case 172: 
      return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_IRON_GATE;
    case 173: 
      return StructureConstantsEnum.FENCE_PLAN_POTTERY_IRON_GATE;
    case 174: 
      return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_IRON_GATE;
    case 175: 
      return StructureConstantsEnum.FENCE_PLAN_RENDERED_IRON_GATE;
    case 176: 
      return StructureConstantsEnum.FENCE_PLAN_MARBLE_IRON_GATE;
    case 70: 
      return StructureConstantsEnum.FENCE_PLAN_SLATE_TALL_STONE_WALL;
    case 76: 
      return StructureConstantsEnum.FENCE_PLAN_SLATE_PORTCULLIS;
    case 131: 
      return StructureConstantsEnum.FENCE_PLAN_SLATE_HIGH_IRON_FENCE;
    case 125: 
      return StructureConstantsEnum.FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE;
    case 35: 
      return StructureConstantsEnum.FENCE_PLAN_SLATE_STONE_PARAPET;
    case 40: 
      return StructureConstantsEnum.FENCE_PLAN_SLATE_CHAIN_FENCE;
    case 71: 
      return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL;
    case 77: 
      return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_PORTCULLIS;
    case 132: 
      return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE;
    case 126: 
      return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE;
    case 36: 
      return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET;
    case 41: 
      return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE;
    case 72: 
      return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_TALL_STONE_WALL;
    case 78: 
      return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_PORTCULLIS;
    case 133: 
      return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE;
    case 127: 
      return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE;
    case 34: 
      return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_STONE_PARAPET;
    case 42: 
      return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_CHAIN_FENCE;
    case 73: 
      return StructureConstantsEnum.FENCE_PLAN_RENDERED_TALL_STONE_WALL;
    case 79: 
      return StructureConstantsEnum.FENCE_PLAN_RENDERED_PORTCULLIS;
    case 134: 
      return StructureConstantsEnum.FENCE_PLAN_RENDERED_HIGH_IRON_FENCE;
    case 128: 
      return StructureConstantsEnum.FENCE_PLAN_RENDERED_HIGH_IRON_FENCE_GATE;
    case 37: 
      return StructureConstantsEnum.FENCE_PLAN_RENDERED_STONE_PARAPET;
    case 43: 
      return StructureConstantsEnum.FENCE_PLAN_RENDERED_CHAIN_FENCE;
    case 74: 
      return StructureConstantsEnum.FENCE_PLAN_POTTERY_TALL_STONE_WALL;
    case 80: 
      return StructureConstantsEnum.FENCE_PLAN_POTTERY_PORTCULLIS;
    case 135: 
      return StructureConstantsEnum.FENCE_PLAN_POTTERY_HIGH_IRON_FENCE;
    case 129: 
      return StructureConstantsEnum.FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE;
    case 38: 
      return StructureConstantsEnum.FENCE_PLAN_POTTERY_STONE_PARAPET;
    case 44: 
      return StructureConstantsEnum.FENCE_PLAN_POTTERY_CHAIN_FENCE;
    case 75: 
      return StructureConstantsEnum.FENCE_PLAN_MARBLE_TALL_STONE_WALL;
    case 81: 
      return StructureConstantsEnum.FENCE_PLAN_MARBLE_PORTCULLIS;
    case 136: 
      return StructureConstantsEnum.FENCE_PLAN_MARBLE_HIGH_IRON_FENCE;
    case 130: 
      return StructureConstantsEnum.FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE;
    case 39: 
      return StructureConstantsEnum.FENCE_PLAN_MARBLE_STONE_PARAPET;
    case 45: 
      return StructureConstantsEnum.FENCE_PLAN_MARBLE_CHAIN_FENCE;
    }
    logger.log(Level.WARNING, "Fence plan for type " + type + " is not found!", new Exception());
    
    return type;
  }
  
  public static final StructureConstantsEnum getFenceForPlan(StructureConstantsEnum type)
  {
    switch (Fence.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[type.ordinal()])
    {
    case 152: 
      return StructureConstantsEnum.FENCE_WOODEN;
    case 154: 
      return StructureConstantsEnum.FENCE_WOODEN_CRUDE;
    case 159: 
      return StructureConstantsEnum.FENCE_GARDESGARD_LOW;
    case 158: 
      return StructureConstantsEnum.FENCE_GARDESGARD_HIGH;
    case 156: 
      return StructureConstantsEnum.FENCE_GARDESGARD_GATE;
    case 63: 
      return StructureConstantsEnum.FENCE_PALISADE;
    case 94: 
      return StructureConstantsEnum.FENCE_STONEWALL;
    case 106: 
      return StructureConstantsEnum.FENCE_WOODEN_PARAPET;
    case 107: 
      return StructureConstantsEnum.FENCE_STONE_PARAPET;
    case 108: 
      return StructureConstantsEnum.FENCE_STONE_IRON_PARAPET;
    case 64: 
      return StructureConstantsEnum.FENCE_PALISADE_GATE;
    case 153: 
      return StructureConstantsEnum.FENCE_WOODEN_GATE;
    case 155: 
      return StructureConstantsEnum.FENCE_WOODEN_CRUDE_GATE;
    case 67: 
      return StructureConstantsEnum.FENCE_STONEWALL_HIGH;
    case 177: 
      return StructureConstantsEnum.FENCE_IRON;
    case 160: 
      return StructureConstantsEnum.FENCE_WOVEN;
    case 178: 
      return StructureConstantsEnum.FENCE_IRON_GATE;
    case 95: 
      return StructureConstantsEnum.FENCE_STONE;
    case 121: 
      return StructureConstantsEnum.FENCE_CURB;
    case 122: 
      return StructureConstantsEnum.FENCE_ROPE_LOW;
    case 27: 
      return StructureConstantsEnum.FENCE_ROPE_HIGH;
    case 124: 
      return StructureConstantsEnum.FENCE_IRON_GATE_HIGH;
    case 123: 
      return StructureConstantsEnum.FENCE_IRON_HIGH;
    case 114: 
      return StructureConstantsEnum.FENCE_MEDIUM_CHAIN;
    case 69: 
      return StructureConstantsEnum.FENCE_PORTCULLIS;
    case 96: 
      return StructureConstantsEnum.FENCE_SLATE;
    case 98: 
      return StructureConstantsEnum.FENCE_ROUNDED_STONE;
    case 100: 
      return StructureConstantsEnum.FENCE_POTTERY;
    case 102: 
      return StructureConstantsEnum.FENCE_SANDSTONE;
    case 179: 
      return StructureConstantsEnum.FENCE_RENDERED;
    case 104: 
      return StructureConstantsEnum.FENCE_MARBLE;
    case 180: 
      return StructureConstantsEnum.FENCE_SLATE_IRON;
    case 181: 
      return StructureConstantsEnum.FENCE_ROUNDED_STONE_IRON;
    case 182: 
      return StructureConstantsEnum.FENCE_POTTERY_IRON;
    case 183: 
      return StructureConstantsEnum.FENCE_SANDSTONE_IRON;
    case 184: 
      return StructureConstantsEnum.FENCE_RENDERED_IRON;
    case 185: 
      return StructureConstantsEnum.FENCE_MARBLE_IRON;
    case 186: 
      return StructureConstantsEnum.FENCE_SLATE_IRON_GATE;
    case 187: 
      return StructureConstantsEnum.FENCE_ROUNDED_STONE_IRON_GATE;
    case 188: 
      return StructureConstantsEnum.FENCE_POTTERY_IRON_GATE;
    case 189: 
      return StructureConstantsEnum.FENCE_SANDSTONE_IRON_GATE;
    case 190: 
      return StructureConstantsEnum.FENCE_RENDERED_IRON_GATE;
    case 191: 
      return StructureConstantsEnum.FENCE_MARBLE_IRON_GATE;
    case 82: 
      return StructureConstantsEnum.FENCE_SLATE_TALL_STONE_WALL;
    case 88: 
      return StructureConstantsEnum.FENCE_SLATE_PORTCULLIS;
    case 143: 
      return StructureConstantsEnum.FENCE_SLATE_HIGH_IRON_FENCE;
    case 137: 
      return StructureConstantsEnum.FENCE_SLATE_HIGH_IRON_FENCE_GATE;
    case 109: 
      return StructureConstantsEnum.FENCE_SLATE_STONE_PARAPET;
    case 115: 
      return StructureConstantsEnum.FENCE_SLATE_CHAIN_FENCE;
    case 83: 
      return StructureConstantsEnum.FENCE_ROUNDED_STONE_TALL_STONE_WALL;
    case 89: 
      return StructureConstantsEnum.FENCE_ROUNDED_STONE_PORTCULLIS;
    case 144: 
      return StructureConstantsEnum.FENCE_ROUNDED_STONE_HIGH_IRON_FENCE;
    case 138: 
      return StructureConstantsEnum.FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE;
    case 110: 
      return StructureConstantsEnum.FENCE_ROUNDED_STONE_STONE_PARAPET;
    case 116: 
      return StructureConstantsEnum.FENCE_ROUNDED_STONE_CHAIN_FENCE;
    case 84: 
      return StructureConstantsEnum.FENCE_SANDSTONE_TALL_STONE_WALL;
    case 90: 
      return StructureConstantsEnum.FENCE_SANDSTONE_PORTCULLIS;
    case 145: 
      return StructureConstantsEnum.FENCE_SANDSTONE_HIGH_IRON_FENCE;
    case 139: 
      return StructureConstantsEnum.FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE;
    case 149: 
      return StructureConstantsEnum.FENCE_SANDSTONE_STONE_PARAPET;
    case 117: 
      return StructureConstantsEnum.FENCE_SANDSTONE_CHAIN_FENCE;
    case 85: 
      return StructureConstantsEnum.FENCE_RENDERED_TALL_STONE_WALL;
    case 91: 
      return StructureConstantsEnum.FENCE_RENDERED_PORTCULLIS;
    case 146: 
      return StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE;
    case 140: 
      return StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE_GATE;
    case 111: 
      return StructureConstantsEnum.FENCE_RENDERED_STONE_PARAPET;
    case 118: 
      return StructureConstantsEnum.FENCE_RENDERED_CHAIN_FENCE;
    case 86: 
      return StructureConstantsEnum.FENCE_POTTERY_TALL_STONE_WALL;
    case 92: 
      return StructureConstantsEnum.FENCE_POTTERY_PORTCULLIS;
    case 147: 
      return StructureConstantsEnum.FENCE_POTTERY_HIGH_IRON_FENCE;
    case 141: 
      return StructureConstantsEnum.FENCE_POTTERY_HIGH_IRON_FENCE_GATE;
    case 112: 
      return StructureConstantsEnum.FENCE_POTTERY_STONE_PARAPET;
    case 119: 
      return StructureConstantsEnum.FENCE_POTTERY_CHAIN_FENCE;
    case 87: 
      return StructureConstantsEnum.FENCE_MARBLE_TALL_STONE_WALL;
    case 93: 
      return StructureConstantsEnum.FENCE_MARBLE_PORTCULLIS;
    case 148: 
      return StructureConstantsEnum.FENCE_MARBLE_HIGH_IRON_FENCE;
    case 142: 
      return StructureConstantsEnum.FENCE_MARBLE_HIGH_IRON_FENCE_GATE;
    case 113: 
      return StructureConstantsEnum.FENCE_MARBLE_STONE_PARAPET;
    case 120: 
      return StructureConstantsEnum.FENCE_MARBLE_CHAIN_FENCE;
    }
    logger.log(Level.WARNING, "Fence for type " + type + " is not found!", new Exception());
    
    return type;
  }
  
  public final int[] getItemTemplatesDealtForFence(StructureConstantsEnum aType, StructureStateEnum aState)
  {
    if (aState.state >= getFinishState().state)
    {
      if ((aType == StructureConstantsEnum.FENCE_PALISADE) || (aType == StructureConstantsEnum.FENCE_PALISADE_GATE))
      {
        int[] toReturn = new int[10];
        for (int x = 0; x < toReturn.length; x++) {
          toReturn[x] = 385;
        }
        return toReturn;
      }
      if (aType == StructureConstantsEnum.FENCE_STONEWALL)
      {
        int[] toReturn = new int[10];
        for (int x = 0; x < toReturn.length; x++) {
          toReturn[x] = 146;
        }
        return toReturn;
      }
      if (aType == StructureConstantsEnum.FENCE_STONE_PARAPET)
      {
        int[] toReturn = new int[15];
        for (int x = 0; x < toReturn.length; x++) {
          toReturn[x] = 132;
        }
        return toReturn;
      }
      if (aType == StructureConstantsEnum.FENCE_STONE_IRON_PARAPET)
      {
        int[] toReturn = new int[15];
        for (int x = 0; x < toReturn.length; x++) {
          toReturn[x] = 132;
        }
      }
      else
      {
        if (aType == StructureConstantsEnum.FENCE_WOODEN_PARAPET)
        {
          int[] toReturn = new int[15];
          for (int x = 0; x < toReturn.length; x++) {
            toReturn[x] = 22;
          }
          return toReturn;
        }
        if (aType == StructureConstantsEnum.FENCE_STONEWALL_HIGH)
        {
          int[] toReturn = new int[20];
          for (int x = 0; x < toReturn.length; x++) {
            toReturn[x] = 132;
          }
          return toReturn;
        }
        if ((aType == StructureConstantsEnum.FENCE_IRON) || (aType == StructureConstantsEnum.FENCE_IRON_GATE))
        {
          int[] toReturn = new int[1];
          for (int x = 0; x < toReturn.length; x++) {
            toReturn[x] = 46;
          }
          return toReturn;
        }
        if (aType == StructureConstantsEnum.FENCE_WOVEN)
        {
          int[] toReturn = new int[2];
          for (int x = 0; x < toReturn.length; x++) {
            toReturn[x] = 169;
          }
          return toReturn;
        }
        if (aType == StructureConstantsEnum.FENCE_RUBBLE) {
          return new int[] { 169 };
        }
        return new int[] { 23, 22, 22 };
      }
    }
    return EMPTY_INT_ARRAY;
  }
  
  public static final int[] getItemTemplatesNeededForFenceTotal(StructureConstantsEnum fenceType)
  {
    StructureStateEnum finishState = getFinishState(fenceType);
    switch (Fence.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[fenceType.ordinal()])
    {
    case 63: 
    case 64: 
      return new int[] { 385, finishState.state };
    case 94: 
      return new int[] { 146, finishState.state };
    case 154: 
    case 155: 
      return new int[] { 23, 2, 22, finishState.state - 2, 218, 1 };
    case 152: 
    case 153: 
      return new int[] { 23, 2, 22, finishState.state - 2, 218, 1 };
    case 114: 
      return new int[] { 132, finishState.state - 6, 859, 6 };
    case 115: 
      return new int[] { 1123, finishState.state - 6, 859, 6 };
    case 116: 
      return new int[] { 1122, finishState.state - 6, 859, 6 };
    case 117: 
      return new int[] { 1121, finishState.state - 6, 859, 6 };
    case 119: 
      return new int[] { 776, finishState.state - 6, 859, 6 };
    case 120: 
      return new int[] { 786, finishState.state - 6, 859, 6 };
    case 69: 
      return new int[] { 132, 15, 681, 2, 187, 2, 559, 1 };
    case 88: 
      return new int[] { 1123, 15, 681, 2, 187, 2, 559, 1 };
    case 89: 
      return new int[] { 1122, 15, 681, 2, 187, 2, 559, 1 };
    case 90: 
      return new int[] { 1121, 15, 681, 2, 187, 2, 559, 1 };
    case 92: 
      return new int[] { 776, 15, 681, 2, 187, 2, 559, 1 };
    case 93: 
      return new int[] { 786, 15, 681, 2, 187, 2, 559, 1 };
    case 106: 
      return new int[] { 23, 2, 22, finishState.state - 2, 218, finishState.state - 2 };
    case 107: 
      return new int[] { 132, finishState.state };
    case 109: 
      return new int[] { 1123, finishState.state };
    case 110: 
      return new int[] { 1122, finishState.state };
    case 149: 
      return new int[] { 1121, finishState.state };
    case 112: 
      return new int[] { 776, finishState.state };
    case 113: 
      return new int[] { 786, finishState.state };
    case 108: 
      return new int[] { 132, finishState.state - 1, 681, 1 };
    case 159: 
      return new int[] { 23, finishState.state, 218, 1 };
    case 156: 
    case 158: 
      return new int[] { 23, finishState.state, 218, 1 };
    case 67: 
      return new int[] { 132, finishState.state };
    case 82: 
      return new int[] { 1123, finishState.state };
    case 83: 
      return new int[] { 1122, finishState.state };
    case 84: 
      return new int[] { 1121, finishState.state };
    case 86: 
      return new int[] { 776, finishState.state };
    case 87: 
      return new int[] { 786, finishState.state };
    case 177: 
    case 178: 
      return new int[] { 132, finishState.state - 1, 681, 1 };
    case 180: 
    case 186: 
      return new int[] { 1123, finishState.state - 1, 681, 1 };
    case 181: 
    case 187: 
      return new int[] { 1122, finishState.state - 1, 681, 1 };
    case 182: 
    case 188: 
      return new int[] { 776, finishState.state - 1, 681, 1 };
    case 183: 
    case 189: 
      return new int[] { 1121, finishState.state - 1, 681, 1 };
    case 184: 
    case 190: 
      return new int[] { 1121, finishState.state - 11, 130, finishState.state - 1, 681, 1 };
    case 185: 
    case 191: 
      return new int[] { 786, finishState.state - 1, 681, 1 };
    case 123: 
    case 124: 
      return new int[] { 132, finishState.state - 2, 681, 2 };
    case 137: 
    case 143: 
      return new int[] { 1123, finishState.state - 2, 681, 2 };
    case 138: 
    case 144: 
      return new int[] { 1122, finishState.state - 2, 681, 2 };
    case 139: 
    case 145: 
      return new int[] { 1121, finishState.state - 2, 681, 2 };
    case 141: 
    case 147: 
      return new int[] { 776, finishState.state - 2, 681, 2 };
    case 142: 
    case 148: 
      return new int[] { 786, finishState.state - 2, 681, 2 };
    case 160: 
      return new int[] { 169, finishState.state };
    case 95: 
      return new int[] { 132, finishState.state };
    case 96: 
      return new int[] { 1123, finishState.state };
    case 98: 
      return new int[] { 1122, finishState.state };
    case 100: 
      return new int[] { 776, finishState.state };
    case 102: 
      return new int[] { 1121, finishState.state };
    case 179: 
      return new int[] { 132, finishState.state, 130, finishState.state };
    case 104: 
      return new int[] { 786, finishState.state };
    case 121: 
      return new int[] { 146, finishState.state };
    case 27: 
    case 122: 
      return new int[] { 23, 2, 319, finishState.state - 2 };
    }
    return new int[] { -1 };
  }
  
  public static final int[] getConstructionMaterialsNeededTotal(Fence fence)
  {
    if (fence.isFinished()) {
      return new int[] { -1 };
    }
    StructureStateEnum maxState = fence.getFinishState();
    StructureStateEnum currentState = fence.getState();
    StructureConstantsEnum type = fence.getType();
    int needed = maxState.state - currentState.state;
    if ((type == StructureConstantsEnum.FENCE_PLAN_PALISADE) || (type == StructureConstantsEnum.FENCE_PLAN_PALISADE_GATE)) {
      return new int[] { 385, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_STONEWALL) {
      return new int[] { 146, needed };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_WOODEN_CRUDE) || (type == StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE_CRUDE))
    {
      if (currentState == StructureStateEnum.UNINITIALIZED) {
        return new int[] { 23, 1 };
      }
      if (currentState == StructureStateEnum.INITIALIZED) {
        return new int[] { 23, 1, 22, needed - 1, 218, 1 };
      }
      if (currentState.state == StructureStateEnum.INITIALIZED.state + 1) {
        return new int[] { 22, needed, 218, 1 };
      }
      return new int[] { 22, needed };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_WOODEN) || (type == StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE))
    {
      if (currentState == StructureStateEnum.UNINITIALIZED) {
        return new int[] { 23, 1 };
      }
      if (currentState == StructureStateEnum.INITIALIZED) {
        return new int[] { 23, 1, 22, needed - 1, 218, 1 };
      }
      if (currentState.state == StructureStateEnum.INITIALIZED.state + 1) {
        return new int[] { 22, needed, 218, 1 };
      }
      return new int[] { 22, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_WOODEN_PARAPET)
    {
      if (currentState == StructureStateEnum.UNINITIALIZED) {
        return new int[] { 23, 1 };
      }
      if (currentState == StructureStateEnum.INITIALIZED) {
        return new int[] { 23, 1, 22, needed - 1, 218, needed - 1 };
      }
      if (currentState.state >= StructureStateEnum.INITIALIZED.state + 1) {
        return new int[] { 22, needed, 218, needed };
      }
      return new int[] { 22, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_STONE_PARAPET) {
      return new int[] { 132, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_SLATE_STONE_PARAPET) {
      return new int[] { 1123, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET) {
      return new int[] { 1122, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_STONE_PARAPET) {
      return new int[] { 1121, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_POTTERY_STONE_PARAPET) {
      return new int[] { 776, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_MARBLE_STONE_PARAPET) {
      return new int[] { 786, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_STONE_IRON_PARAPET)
    {
      if (currentState == StructureStateEnum.UNINITIALIZED) {
        return new int[] { 132, 1 };
      }
      if (currentState.state < maxState.state - 1) {
        return new int[] { 132, maxState.state - 1 - currentState.state, 681, 1 };
      }
      if (currentState.state == maxState.state - 1) {
        return new int[] { 681, 1 };
      }
      return new int[] { 132, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_GARDESGARD_LOW)
    {
      if ((currentState == StructureStateEnum.UNINITIALIZED) || (currentState == StructureStateEnum.INITIALIZED)) {
        return new int[] { 23, needed, 218, 1 };
      }
      if (currentState.state >= StructureStateEnum.INITIALIZED.state + 1) {
        return new int[] { 23, needed, 218, 1 };
      }
      return new int[] { 23, needed };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_GARDESGARD_HIGH) || (type == StructureConstantsEnum.FENCE_PLAN_GARDESGARD_GATE))
    {
      if ((currentState == StructureStateEnum.UNINITIALIZED) || (currentState == StructureStateEnum.INITIALIZED)) {
        return new int[] { 23, needed, 218, 1 };
      }
      if (currentState.state >= StructureStateEnum.INITIALIZED.state + 1) {
        return new int[] { 23, needed, 218, 1 };
      }
      return new int[] { 23, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_STONEWALL_HIGH) {
      return new int[] { 132, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_SLATE_TALL_STONE_WALL) {
      return new int[] { 1123, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL) {
      return new int[] { 1122, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_TALL_STONE_WALL) {
      return new int[] { 1121, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_POTTERY_TALL_STONE_WALL) {
      return new int[] { 776, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_MARBLE_TALL_STONE_WALL) {
      return new int[] { 786, needed };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_IRON) || (type == StructureConstantsEnum.FENCE_PLAN_IRON_GATE))
    {
      if (currentState.state < 10) {
        return new int[] { 132, 10 - currentState.state, 681, 1 };
      }
      return new int[] { 681, 1 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_SLATE_IRON) || (type == StructureConstantsEnum.FENCE_PLAN_SLATE_IRON_GATE))
    {
      if (currentState.state < 10) {
        return new int[] { 1123, 10 - currentState.state, 681, 1 };
      }
      return new int[] { 681, 1 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_IRON) || (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_IRON_GATE))
    {
      if (currentState.state < 10) {
        return new int[] { 1122, 10 - currentState.state, 681, 1 };
      }
      return new int[] { 681, 1 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_POTTERY_IRON) || (type == StructureConstantsEnum.FENCE_PLAN_POTTERY_IRON_GATE))
    {
      if (currentState.state < 10) {
        return new int[] { 776, 10 - currentState.state, 681, 1 };
      }
      return new int[] { 681, 1 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_IRON) || (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_IRON_GATE))
    {
      if (currentState.state < 10) {
        return new int[] { 1121, 10 - currentState.state, 681, 1 };
      }
      return new int[] { 681, 1 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_MARBLE_IRON) || (type == StructureConstantsEnum.FENCE_PLAN_MARBLE_IRON_GATE))
    {
      if (currentState.state < 10) {
        return new int[] { 786, 10 - currentState.state, 681, 1 };
      }
      return new int[] { 681, 1 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_IRON_HIGH) || (type == StructureConstantsEnum.FENCE_PLAN_IRON_GATE_HIGH))
    {
      if (currentState.state < fence.getFinishState().state - 2) {
        return new int[] { 132, 
          fence.getFinishState().state - 2 - currentState.state, 681, 2 };
      }
      return new int[] { 681, needed };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_SLATE_HIGH_IRON_FENCE) || (type == StructureConstantsEnum.FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE))
    {
      if (currentState.state < fence.getFinishState().state - 2) {
        return new int[] { 1123, 
          fence.getFinishState().state - 2 - currentState.state, 681, 2 };
      }
      return new int[] { 681, needed };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE) || (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE))
    {
      if (currentState.state < fence.getFinishState().state - 2) {
        return new int[] { 1122, 
          fence.getFinishState().state - 2 - currentState.state, 681, 2 };
      }
      return new int[] { 681, needed };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE) || (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE))
    {
      if (currentState.state < fence.getFinishState().state - 2) {
        return new int[] { 1121, 
          fence.getFinishState().state - 2 - currentState.state, 681, 2 };
      }
      return new int[] { 681, needed };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_POTTERY_HIGH_IRON_FENCE) || (type == StructureConstantsEnum.FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE))
    {
      if (currentState.state < fence.getFinishState().state - 2) {
        return new int[] { 776, 
          fence.getFinishState().state - 2 - currentState.state, 681, 2 };
      }
      return new int[] { 681, needed };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_MARBLE_HIGH_IRON_FENCE) || (type == StructureConstantsEnum.FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE))
    {
      if (currentState.state < fence.getFinishState().state - 2) {
        return new int[] { 786, 
          fence.getFinishState().state - 2 - currentState.state, 681, 2 };
      }
      return new int[] { 681, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_MEDIUM_CHAIN)
    {
      if (currentState.state < fence.getFinishState().state - 6) {
        return new int[] { 132, 
        
          fence.getFinishState().state - 6 - currentState.state, 859, 6 };
      }
      return new int[] { 859, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_SLATE_CHAIN_FENCE)
    {
      if (currentState.state < fence.getFinishState().state - 6) {
        return new int[] { 1123, 
        
          fence.getFinishState().state - 6 - currentState.state, 859, 6 };
      }
      return new int[] { 859, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE)
    {
      if (currentState.state < fence.getFinishState().state - 6) {
        return new int[] { 1122, 
        
          fence.getFinishState().state - 6 - currentState.state, 859, 6 };
      }
      return new int[] { 859, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_CHAIN_FENCE)
    {
      if (currentState.state < fence.getFinishState().state - 6) {
        return new int[] { 1121, 
        
          fence.getFinishState().state - 6 - currentState.state, 859, 6 };
      }
      return new int[] { 859, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_POTTERY_CHAIN_FENCE)
    {
      if (currentState.state < fence.getFinishState().state - 6) {
        return new int[] { 776, 
        
          fence.getFinishState().state - 6 - currentState.state, 859, 6 };
      }
      return new int[] { 859, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_MARBLE_CHAIN_FENCE)
    {
      if (currentState.state < fence.getFinishState().state - 6) {
        return new int[] { 786, 
        
          fence.getFinishState().state - 6 - currentState.state, 859, 6 };
      }
      return new int[] { 859, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_PORTCULLIS)
    {
      if (currentState.state < fence.getFinishState().state - 5) {
        return new int[] { 132, needed - 5, 681, 2, 187, 2, 559, 1 };
      }
      if (currentState.state < fence.getFinishState().state - 3) {
        return new int[] { 681, needed - 3, 187, 2, 559, 1 };
      }
      if (currentState.state < fence.getFinishState().state - 1) {
        return new int[] { 187, needed - 1, 559, 1 };
      }
      return new int[] { 559, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_SLATE_PORTCULLIS)
    {
      if (currentState.state < fence.getFinishState().state - 5) {
        return new int[] { 1123, needed - 5, 681, 2, 187, 2, 559, 1 };
      }
      if (currentState.state < fence.getFinishState().state - 3) {
        return new int[] { 681, needed - 3, 187, 2, 559, 1 };
      }
      if (currentState.state < fence.getFinishState().state - 1) {
        return new int[] { 187, needed - 1, 559, 1 };
      }
      return new int[] { 559, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_PORTCULLIS)
    {
      if (currentState.state < fence.getFinishState().state - 5) {
        return new int[] { 1122, needed - 5, 681, 2, 187, 2, 559, 1 };
      }
      if (currentState.state < fence.getFinishState().state - 3) {
        return new int[] { 681, needed - 3, 187, 2, 559, 1 };
      }
      if (currentState.state < fence.getFinishState().state - 1) {
        return new int[] { 187, needed - 1, 559, 1 };
      }
      return new int[] { 559, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_PORTCULLIS)
    {
      if (currentState.state < fence.getFinishState().state - 5) {
        return new int[] { 1121, needed - 5, 681, 2, 187, 2, 559, 1 };
      }
      if (currentState.state < fence.getFinishState().state - 3) {
        return new int[] { 681, needed - 3, 187, 2, 559, 1 };
      }
      if (currentState.state < fence.getFinishState().state - 1) {
        return new int[] { 187, needed - 1, 559, 1 };
      }
      return new int[] { 559, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_POTTERY_PORTCULLIS)
    {
      if (currentState.state < fence.getFinishState().state - 5) {
        return new int[] { 776, needed - 5, 681, 2, 187, 2, 559, 1 };
      }
      if (currentState.state < fence.getFinishState().state - 3) {
        return new int[] { 681, needed - 3, 187, 2, 559, 1 };
      }
      if (currentState.state < fence.getFinishState().state - 1) {
        return new int[] { 187, needed - 1, 559, 1 };
      }
      return new int[] { 559, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_MARBLE_PORTCULLIS)
    {
      if (currentState.state < fence.getFinishState().state - 5) {
        return new int[] { 786, needed - 5, 681, 2, 187, 2, 559, 1 };
      }
      if (currentState.state < fence.getFinishState().state - 3) {
        return new int[] { 681, needed - 3, 187, 2, 559, 1 };
      }
      if (currentState.state < fence.getFinishState().state - 1) {
        return new int[] { 187, needed - 1, 559, 1 };
      }
      return new int[] { 559, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_WOVEN) {
      return new int[] { 169, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_STONE) {
      return new int[] { 132, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_SLATE) {
      return new int[] { 1123, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE) {
      return new int[] { 1122, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_POTTERY) {
      return new int[] { 776, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE) {
      return new int[] { 1121, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_RENDERED) {
      return new int[] { 1122, needed, 130, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_MARBLE) {
      return new int[] { 786, needed };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_CURB) {
      return new int[] { 146, needed };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_ROPE_HIGH) || (type == StructureConstantsEnum.FENCE_PLAN_ROPE_LOW))
    {
      if (currentState == StructureStateEnum.UNINITIALIZED) {
        return new int[] { 23, 1 };
      }
      if (currentState == StructureStateEnum.INITIALIZED) {
        return new int[] { 23, 1, 319, needed - 1 };
      }
      return new int[] { 319, needed };
    }
    return new int[] { -1 };
  }
  
  public static final int[] getItemTemplatesNeededForFence(Fence fence)
  {
    StructureConstantsEnum type = fence.getType();
    if (fence.isFinished()) {
      return new int[] { -1 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_PALISADE) || (type == StructureConstantsEnum.FENCE_PLAN_PALISADE_GATE)) {
      return new int[] { 385 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_STONEWALL) {
      return new int[] { 146 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_MEDIUM_CHAIN)
    {
      StructureStateEnum state = fence.getState();
      if (state.state < fence.getFinishState().state - 6) {
        return new int[] { 132 };
      }
      return new int[] { 859 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_SLATE_CHAIN_FENCE)
    {
      StructureStateEnum state = fence.getState();
      if (state.state < fence.getFinishState().state - 6) {
        return new int[] { 1123 };
      }
      return new int[] { 859 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE)
    {
      StructureStateEnum state = fence.getState();
      if (state.state < fence.getFinishState().state - 6) {
        return new int[] { 1122 };
      }
      return new int[] { 859 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_CHAIN_FENCE)
    {
      StructureStateEnum state = fence.getState();
      if (state.state < fence.getFinishState().state - 6) {
        return new int[] { 1121 };
      }
      return new int[] { 859 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_POTTERY_CHAIN_FENCE)
    {
      StructureStateEnum state = fence.getState();
      if (state.state < fence.getFinishState().state - 6) {
        return new int[] { 776 };
      }
      return new int[] { 859 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_MARBLE_CHAIN_FENCE)
    {
      StructureStateEnum state = fence.getState();
      if (state.state < fence.getFinishState().state - 6) {
        return new int[] { 786 };
      }
      return new int[] { 859 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_PORTCULLIS)
    {
      StructureStateEnum state = fence.getState();
      int brickStage = fence.getFinishState().state - 5;
      int barStage = fence.getFinishState().state - 3;
      int wheelStage = fence.getFinishState().state - 1;
      if (state.state < brickStage) {
        return new int[] { 132 };
      }
      if (state.state < barStage) {
        return new int[] { 681 };
      }
      if (state.state < wheelStage) {
        return new int[] { 187 };
      }
      return new int[] { 559 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_SLATE_PORTCULLIS)
    {
      StructureStateEnum state = fence.getState();
      int brickStage = fence.getFinishState().state - 5;
      int barStage = fence.getFinishState().state - 3;
      int wheelStage = fence.getFinishState().state - 1;
      if (state.state < brickStage) {
        return new int[] { 1123 };
      }
      if (state.state < barStage) {
        return new int[] { 681 };
      }
      if (state.state < wheelStage) {
        return new int[] { 187 };
      }
      return new int[] { 559 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_PORTCULLIS)
    {
      StructureStateEnum state = fence.getState();
      int brickStage = fence.getFinishState().state - 5;
      int barStage = fence.getFinishState().state - 3;
      int wheelStage = fence.getFinishState().state - 1;
      if (state.state < brickStage) {
        return new int[] { 1122 };
      }
      if (state.state < barStage) {
        return new int[] { 681 };
      }
      if (state.state < wheelStage) {
        return new int[] { 187 };
      }
      return new int[] { 559 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_PORTCULLIS)
    {
      StructureStateEnum state = fence.getState();
      int brickStage = fence.getFinishState().state - 5;
      int barStage = fence.getFinishState().state - 3;
      int wheelStage = fence.getFinishState().state - 1;
      if (state.state < brickStage) {
        return new int[] { 1121 };
      }
      if (state.state < barStage) {
        return new int[] { 681 };
      }
      if (state.state < wheelStage) {
        return new int[] { 187 };
      }
      return new int[] { 559 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_POTTERY_PORTCULLIS)
    {
      StructureStateEnum state = fence.getState();
      int brickStage = fence.getFinishState().state - 5;
      int barStage = fence.getFinishState().state - 3;
      int wheelStage = fence.getFinishState().state - 1;
      if (state.state < brickStage) {
        return new int[] { 776 };
      }
      if (state.state < barStage) {
        return new int[] { 681 };
      }
      if (state.state < wheelStage) {
        return new int[] { 187 };
      }
      return new int[] { 559 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_MARBLE_PORTCULLIS)
    {
      StructureStateEnum state = fence.getState();
      int brickStage = fence.getFinishState().state - 5;
      int barStage = fence.getFinishState().state - 3;
      int wheelStage = fence.getFinishState().state - 1;
      if (state.state < brickStage) {
        return new int[] { 786 };
      }
      if (state.state < barStage) {
        return new int[] { 681 };
      }
      if (state.state < wheelStage) {
        return new int[] { 187 };
      }
      return new int[] { 559 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_WOODEN_CRUDE) || (type == StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE_CRUDE))
    {
      StructureStateEnum state = fence.getState();
      if ((state == StructureStateEnum.UNINITIALIZED) || (state == StructureStateEnum.INITIALIZED)) {
        return new int[] { 23 };
      }
      if (state.state == StructureStateEnum.INITIALIZED.state + 1) {
        return new int[] { 22, 218 };
      }
      return new int[] { 22 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_WOODEN) || (type == StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE))
    {
      StructureStateEnum state = fence.getState();
      if ((state == StructureStateEnum.UNINITIALIZED) || (state == StructureStateEnum.INITIALIZED)) {
        return new int[] { 23 };
      }
      if (state.state == StructureStateEnum.INITIALIZED.state + 1) {
        return new int[] { 22, 218 };
      }
      return new int[] { 22 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_WOODEN_PARAPET)
    {
      StructureStateEnum state = fence.getState();
      if ((state == StructureStateEnum.UNINITIALIZED) || (state == StructureStateEnum.INITIALIZED)) {
        return new int[] { 23 };
      }
      if (state.state >= StructureStateEnum.INITIALIZED.state + 1) {
        return new int[] { 22, 218 };
      }
      return new int[] { 22 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_STONE_PARAPET) {
      return new int[] { 132 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_SLATE_STONE_PARAPET) {
      return new int[] { 1123 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET) {
      return new int[] { 1122 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_STONE_PARAPET) {
      return new int[] { 1121 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_POTTERY_STONE_PARAPET) {
      return new int[] { 776 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_MARBLE_STONE_PARAPET) {
      return new int[] { 786 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_STONE_IRON_PARAPET)
    {
      StructureStateEnum state = fence.getState();
      if (state == StructureStateEnum.UNINITIALIZED) {
        return new int[] { 132 };
      }
      if (state.state < fence.getFinishState().state - 1) {
        return new int[] { 132 };
      }
      if (state.state == fence.getFinishState().state - 1) {
        return new int[] { 681 };
      }
      return new int[] { 132 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_GARDESGARD_LOW)
    {
      StructureStateEnum state = fence.getState();
      if ((state == StructureStateEnum.UNINITIALIZED) || (state == StructureStateEnum.INITIALIZED)) {
        return new int[] { 23 };
      }
      if (state.state == StructureStateEnum.INITIALIZED.state + 1) {
        return new int[] { 23, 218 };
      }
      return new int[] { 23 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_GARDESGARD_HIGH) || (type == StructureConstantsEnum.FENCE_PLAN_GARDESGARD_GATE))
    {
      StructureStateEnum state = fence.getState();
      if ((state == StructureStateEnum.UNINITIALIZED) || (state == StructureStateEnum.INITIALIZED)) {
        return new int[] { 23 };
      }
      if (state.state == StructureStateEnum.INITIALIZED.state + 1) {
        return new int[] { 23, 218 };
      }
      return new int[] { 23 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_STONEWALL_HIGH) {
      return new int[] { 132 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_SLATE_TALL_STONE_WALL) {
      return new int[] { 1123 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL) {
      return new int[] { 1122 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_TALL_STONE_WALL) {
      return new int[] { 1121 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_POTTERY_TALL_STONE_WALL) {
      return new int[] { 776 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_MARBLE_TALL_STONE_WALL) {
      return new int[] { 786 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_IRON) || (type == StructureConstantsEnum.FENCE_PLAN_IRON_GATE))
    {
      StructureStateEnum state = fence.getState();
      if (state.state < 10) {
        return new int[] { 132 };
      }
      return new int[] { 681 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_SLATE_IRON) || (type == StructureConstantsEnum.FENCE_PLAN_SLATE_IRON_GATE))
    {
      StructureStateEnum state = fence.getState();
      if (state.state < 10) {
        return new int[] { 1123 };
      }
      return new int[] { 681 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_IRON) || (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_IRON_GATE))
    {
      StructureStateEnum state = fence.getState();
      if (state.state < 10) {
        return new int[] { 1122 };
      }
      return new int[] { 681 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_POTTERY_IRON) || (type == StructureConstantsEnum.FENCE_PLAN_POTTERY_IRON_GATE))
    {
      StructureStateEnum state = fence.getState();
      if (state.state < 10) {
        return new int[] { 776 };
      }
      return new int[] { 681 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_IRON) || (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_IRON_GATE))
    {
      StructureStateEnum state = fence.getState();
      if (state.state < 10) {
        return new int[] { 1121 };
      }
      return new int[] { 681 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_RENDERED_IRON) || (type == StructureConstantsEnum.FENCE_PLAN_RENDERED_IRON_GATE))
    {
      StructureStateEnum state = fence.getState();
      if (state.state < 10) {
        return new int[] { 132 };
      }
      if (state.state < 20) {
        return new int[] { 130 };
      }
      return new int[] { 681 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_MARBLE_IRON) || (type == StructureConstantsEnum.FENCE_PLAN_MARBLE_IRON_GATE))
    {
      StructureStateEnum state = fence.getState();
      if (state.state < 10) {
        return new int[] { 786 };
      }
      return new int[] { 681 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_IRON_HIGH) || (type == StructureConstantsEnum.FENCE_PLAN_IRON_GATE_HIGH))
    {
      StructureStateEnum state = fence.getState();
      if (state.state < fence.getFinishState().state - 2) {
        return new int[] { 132 };
      }
      return new int[] { 681 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_SLATE_HIGH_IRON_FENCE) || (type == StructureConstantsEnum.FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE))
    {
      StructureStateEnum state = fence.getState();
      if (state.state < fence.getFinishState().state - 2) {
        return new int[] { 1123 };
      }
      return new int[] { 681 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE) || (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE))
    {
      StructureStateEnum state = fence.getState();
      if (state.state < fence.getFinishState().state - 2) {
        return new int[] { 1122 };
      }
      return new int[] { 681 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE) || (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE))
    {
      StructureStateEnum state = fence.getState();
      if (state.state < fence.getFinishState().state - 2) {
        return new int[] { 1121 };
      }
      return new int[] { 681 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_POTTERY_HIGH_IRON_FENCE) || (type == StructureConstantsEnum.FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE))
    {
      StructureStateEnum state = fence.getState();
      if (state.state < fence.getFinishState().state - 2) {
        return new int[] { 776 };
      }
      return new int[] { 681 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_MARBLE_HIGH_IRON_FENCE) || (type == StructureConstantsEnum.FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE))
    {
      StructureStateEnum state = fence.getState();
      if (state.state < fence.getFinishState().state - 2) {
        return new int[] { 786 };
      }
      return new int[] { 681 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_WOVEN) {
      return new int[] { 169 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_STONE) {
      return new int[] { 132 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_SLATE) {
      return new int[] { 1123 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE) {
      return new int[] { 1122 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_POTTERY) {
      return new int[] { 776 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE) {
      return new int[] { 1121 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_RENDERED) {
      return new int[] { 132 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_MARBLE) {
      return new int[] { 786 };
    }
    if (type == StructureConstantsEnum.FENCE_PLAN_CURB) {
      return new int[] { 146 };
    }
    if ((type == StructureConstantsEnum.FENCE_PLAN_ROPE_HIGH) || (type == StructureConstantsEnum.FENCE_PLAN_ROPE_LOW))
    {
      StructureStateEnum state = fence.getState();
      if ((state == StructureStateEnum.UNINITIALIZED) || (state == StructureStateEnum.INITIALIZED)) {
        return new int[] { 23 };
      }
      return new int[] { 319 };
    }
    logger.fine("hit default return");
    return new int[] { -1 };
  }
  
  public final boolean isDoor()
  {
    switch (Fence.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[this.type.ordinal()])
    {
    case 28: 
    case 29: 
    case 30: 
    case 32: 
    case 64: 
    case 66: 
    case 69: 
    case 76: 
    case 77: 
    case 78: 
    case 79: 
    case 80: 
    case 81: 
    case 124: 
    case 125: 
    case 126: 
    case 127: 
    case 128: 
    case 129: 
    case 130: 
    case 150: 
    case 153: 
    case 155: 
    case 156: 
    case 163: 
    case 171: 
    case 172: 
    case 173: 
    case 174: 
    case 175: 
    case 176: 
    case 178: 
      return true;
    }
    return false;
  }
  
  public final double getDifficulty()
  {
    switch (Fence.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[this.type.ordinal()])
    {
    case 9: 
    case 22: 
    case 152: 
    case 160: 
      return 1.0D;
    case 30: 
    case 63: 
    case 65: 
    case 155: 
      return 5.0D;
    case 19: 
    case 20: 
    case 21: 
    case 29: 
    case 32: 
    case 33: 
    case 64: 
    case 66: 
    case 94: 
    case 95: 
    case 96: 
    case 97: 
    case 98: 
    case 99: 
    case 100: 
    case 101: 
    case 102: 
    case 103: 
    case 104: 
    case 105: 
    case 151: 
    case 153: 
    case 159: 
    case 165: 
    case 166: 
    case 167: 
    case 168: 
    case 169: 
    case 170: 
    case 171: 
    case 172: 
    case 173: 
    case 174: 
    case 175: 
    case 176: 
    case 177: 
    case 178: 
    case 179: 
      return 10.0D;
    case 28: 
    case 156: 
    case 157: 
    case 158: 
      return 15.0D;
    case 24: 
    case 34: 
    case 35: 
    case 36: 
    case 37: 
    case 38: 
    case 39: 
    case 40: 
    case 41: 
    case 42: 
    case 43: 
    case 44: 
    case 45: 
    case 67: 
    case 68: 
    case 70: 
    case 71: 
    case 72: 
    case 73: 
    case 74: 
    case 75: 
    case 76: 
    case 77: 
    case 78: 
    case 79: 
    case 80: 
    case 81: 
    case 82: 
    case 83: 
    case 84: 
    case 85: 
    case 86: 
    case 87: 
    case 88: 
    case 89: 
    case 90: 
    case 91: 
    case 92: 
    case 93: 
    case 106: 
    case 109: 
    case 110: 
    case 111: 
    case 112: 
    case 113: 
    case 115: 
    case 116: 
    case 117: 
    case 118: 
    case 119: 
    case 120: 
    case 123: 
    case 124: 
    case 125: 
    case 126: 
    case 127: 
    case 128: 
    case 129: 
    case 130: 
    case 131: 
    case 132: 
    case 133: 
    case 134: 
    case 135: 
    case 136: 
    case 137: 
    case 138: 
    case 139: 
    case 140: 
    case 141: 
    case 142: 
    case 143: 
    case 144: 
    case 145: 
    case 146: 
    case 147: 
    case 148: 
    case 149: 
    case 162: 
    case 163: 
      return 20.0D;
    case 23: 
    case 107: 
      return 40.0D;
    case 25: 
    case 108: 
      return 50.0D;
    }
    if (isLowHedge()) {
      return 10.0D;
    }
    if (isMediumHedge()) {
      return 20.0D;
    }
    if (isHighHedge()) {
      return 40.0D;
    }
    return 1.0D;
  }
  
  public static final boolean isIron(StructureConstantsEnum fenceType)
  {
    switch (Fence.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[fenceType.ordinal()])
    {
    case 20: 
    case 32: 
    case 40: 
    case 41: 
    case 42: 
    case 43: 
    case 44: 
    case 45: 
    case 114: 
    case 115: 
    case 116: 
    case 117: 
    case 118: 
    case 119: 
    case 120: 
    case 123: 
    case 124: 
    case 125: 
    case 126: 
    case 127: 
    case 128: 
    case 129: 
    case 130: 
    case 131: 
    case 132: 
    case 133: 
    case 134: 
    case 135: 
    case 136: 
    case 137: 
    case 138: 
    case 139: 
    case 140: 
    case 141: 
    case 142: 
    case 143: 
    case 144: 
    case 145: 
    case 146: 
    case 147: 
    case 148: 
    case 162: 
    case 163: 
    case 164: 
    case 165: 
    case 166: 
    case 167: 
    case 168: 
    case 170: 
    case 171: 
    case 172: 
    case 173: 
    case 174: 
    case 175: 
    case 176: 
    case 177: 
    case 178: 
    case 180: 
    case 181: 
    case 182: 
    case 183: 
    case 185: 
    case 186: 
    case 187: 
    case 188: 
    case 189: 
    case 191: 
      return true;
    }
    return false;
  }
  
  public boolean isIron()
  {
    return isIron(this.type);
  }
  
  public static final int getSkillNumberNeededForFence(StructureConstantsEnum fenceType)
  {
    if (isWood(fenceType)) {
      return 1005;
    }
    return 1013;
  }
  
  public static final Skill getSkillNeededForFence(Creature performer, Fence fence)
  {
    Skill toReturn = null;
    Skills skills = performer.getSkills();
    if (skills == null) {
      return null;
    }
    if (fence.isWood()) {
      try
      {
        toReturn = performer.getSkills().getSkill(1005);
      }
      catch (NoSuchSkillException nss)
      {
        toReturn = skills.learn(1005, 1.0F);
      }
    } else {
      try
      {
        toReturn = performer.getSkills().getSkill(1013);
      }
      catch (NoSuchSkillException nss)
      {
        toReturn = skills.learn(1013, 1.0F);
      }
    }
    return toReturn;
  }
  
  public static final Fence[] getRubbleFences()
  {
    return (Fence[])rubbleFences.values().toArray(new Fence[rubbleFences.size()]);
  }
  
  public final void destroy()
  {
    try
    {
      if (this.type == StructureConstantsEnum.FENCE_RUBBLE) {
        rubbleFences.remove(Long.valueOf(getId()));
      }
      Zone zone = Zones.getZone(getZoneId());
      zone.removeFence(this);
      delete();
    }
    catch (NoSuchZoneException nsz)
    {
      logger.log(Level.WARNING, "Fence in nonexistant zone? Fence: " + this.number + " - " + nsz.getMessage(), nsz);
    }
  }
  
  public final float getDamageModifierForItem(Item item, boolean useForFence)
  {
    float mod = 0.0F;
    if ((this.type == StructureConstantsEnum.FENCE_PALISADE_GATE) || (this.type == StructureConstantsEnum.FENCE_PLAN_PALISADE_GATE) || (this.type == StructureConstantsEnum.FENCE_PALISADE) || (this.type == StructureConstantsEnum.FENCE_PLAN_PALISADE))
    {
      if (item.isWeaponAxe()) {
        mod = 0.02F;
      } else if (item.isWeaponCrush()) {
        mod = 0.007F;
      } else if (item.isWeaponSlash()) {
        mod = 0.01F;
      } else if (item.isWeaponPierce()) {
        mod = 0.005F;
      } else if (item.isWeaponMisc()) {
        mod = 0.002F;
      }
    }
    else if ((this.type == StructureConstantsEnum.FENCE_WOODEN_GATE) || (this.type == StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE) || (this.type == StructureConstantsEnum.FENCE_WOODEN_CRUDE_GATE) || (this.type == StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE_CRUDE) || (this.type == StructureConstantsEnum.FENCE_GARDESGARD_GATE) || (this.type == StructureConstantsEnum.FENCE_PLAN_GARDESGARD_GATE))
    {
      if (item.isWeaponAxe()) {
        mod = 0.03F;
      } else if (item.isWeaponCrush()) {
        mod = 0.02F;
      } else if (item.isWeaponSlash()) {
        mod = 0.02F;
      } else if (item.isWeaponPierce()) {
        mod = 0.01F;
      } else if (item.isWeaponMisc()) {
        mod = 0.007F;
      }
    }
    else if (isMagic())
    {
      if (item.getTemplateId() == 20) {
        mod = 0.07F;
      } else if (item.isWeaponCrush()) {
        mod = 0.03F;
      } else if (item.isWeaponAxe()) {
        mod = 0.015F;
      } else if (item.isWeaponSlash()) {
        mod = 0.01F;
      } else if (item.isWeaponPierce()) {
        mod = 0.01F;
      } else if (item.isWeaponMisc()) {
        mod = 0.01F;
      }
    }
    else if ((isHedge()) || (isFlowerbed()))
    {
      if (item.getTemplateId() == 7) {
        mod = 0.04F;
      } else if (item.isWeaponCrush()) {
        mod = 0.01F;
      } else if (item.isWeaponAxe()) {
        mod = 0.04F;
      } else if (item.isWeaponSlash()) {
        mod = 0.02F;
      } else if (item.isWeaponPierce()) {
        mod = 0.01F;
      } else if (item.isWeaponMisc()) {
        mod = 0.01F;
      }
      if (mod == 0.0F) {
        return mod;
      }
      if (isLowHedge()) {
        mod = (float)(mod + 0.01D);
      } else if (isMediumHedge()) {
        mod = (float)(mod + 0.005D);
      } else if (isFlowerbed()) {
        mod = (float)(mod + 0.01D);
      }
    }
    else if (isStone())
    {
      if (item.getTemplateId() == 20) {
        mod = 0.02F;
      } else if (item.getTemplateId() == 493) {
        mod = 0.01F;
      } else if (item.isWeaponCrush()) {
        mod = 0.01F;
      } else if (item.isWeaponAxe()) {
        mod = 0.005F;
      } else if (item.isWeaponSlash()) {
        mod = 0.005F;
      } else if (item.isWeaponPierce()) {
        mod = 0.002F;
      } else if (item.isWeaponMisc()) {
        mod = 0.001F;
      }
      if (mod == 0.0F) {
        return mod;
      }
      if (useForFence) {
        if ((this.type == StructureConstantsEnum.FENCE_CURB) || (this.type == StructureConstantsEnum.FENCE_PLAN_CURB)) {
          mod += 0.01F;
        }
      }
    }
    else if (isIron())
    {
      if (item.isWeaponCrush()) {
        mod = 0.02F;
      } else if (item.isWeaponAxe()) {
        mod = 0.01F;
      } else if (item.isWeaponSlash()) {
        mod = 0.005F;
      } else if (item.isWeaponPierce()) {
        mod = 0.002F;
      } else if (item.isWeaponMisc()) {
        mod = 0.001F;
      }
    }
    else if (isWoven())
    {
      if (item.isWeaponCrush()) {
        mod = 0.01F;
      } else if (item.isWeaponAxe()) {
        mod = 0.03F;
      } else if (item.isWeaponSlash()) {
        mod = 0.02F;
      } else if (item.isWeaponPierce()) {
        mod = 0.01F;
      } else if (item.isWeaponMisc()) {
        mod = 0.007F;
      }
    }
    else if ((this.type == StructureConstantsEnum.FENCE_ROPE_HIGH) || (this.type == StructureConstantsEnum.FENCE_PLAN_ROPE_HIGH) || (this.type == StructureConstantsEnum.FENCE_ROPE_LOW) || (this.type == StructureConstantsEnum.FENCE_PLAN_ROPE_LOW))
    {
      if (item.isWeaponCrush()) {
        mod = 0.01F;
      } else if (item.isWeaponAxe()) {
        mod = 0.03F;
      } else if (item.isWeaponSlash()) {
        mod = 0.02F;
      } else if (item.isWeaponPierce()) {
        mod = 0.01F;
      } else if (item.isWeaponMisc()) {
        mod = 0.007F;
      }
    }
    else if (item.isWeaponAxe())
    {
      mod = 0.03F;
    }
    else if (item.isWeaponCrush())
    {
      mod = 0.02F;
    }
    else if (item.isWeaponSlash())
    {
      mod = 0.015F;
    }
    else if (item.isWeaponPierce())
    {
      mod = 0.01F;
    }
    else if (item.isWeaponMisc())
    {
      mod = 0.007F;
    }
    return mod;
  }
  
  public final int getRepairItemTemplate()
  {
    int templateId = 22;
    if ((this.type == StructureConstantsEnum.FENCE_PLAN_STONEWALL) || (this.type == StructureConstantsEnum.FENCE_STONEWALL) || (this.type == StructureConstantsEnum.FENCE_CURB)) {
      templateId = 146;
    } else if (isMagic()) {
      templateId = 765;
    } else if (isSlate()) {
      templateId = 1123;
    } else if (isRoundedStone()) {
      templateId = 1122;
    } else if (isPottery()) {
      templateId = 776;
    } else if (isSandstone()) {
      templateId = 1121;
    } else if (isPlastered()) {
      templateId = 130;
    } else if (isMarble()) {
      templateId = 786;
    } else if ((isStone()) || (isIron())) {
      templateId = 132;
    } else if (isWoven()) {
      templateId = 169;
    } else if ((this.type == StructureConstantsEnum.FENCE_PALISADE) || (this.type == StructureConstantsEnum.FENCE_PALISADE_GATE) || (this.type == StructureConstantsEnum.FENCE_PLAN_PALISADE) || (this.type == StructureConstantsEnum.FENCE_PLAN_PALISADE_GATE)) {
      templateId = 9;
    } else if ((isHedge()) || (isFlowerbed())) {
      templateId = -1;
    }
    return templateId;
  }
  
  public final int getCover()
  {
    if (isFinished())
    {
      if (this.type == StructureConstantsEnum.FENCE_STONEWALL) {
        return 30;
      }
      if (this.type == StructureConstantsEnum.FENCE_STONE_PARAPET) {
        return 50;
      }
      if (this.type == StructureConstantsEnum.FENCE_STONE_IRON_PARAPET) {
        return 50;
      }
      if ((isStone()) || (this.type == StructureConstantsEnum.FENCE_PALISADE) || (this.type == StructureConstantsEnum.FENCE_PALISADE_GATE)) {
        return 100;
      }
      if (this.type == StructureConstantsEnum.FENCE_WOODEN_PARAPET) {
        return 50;
      }
      if (this.type == StructureConstantsEnum.FENCE_WOVEN) {
        return 0;
      }
      if (isLowHedge()) {
        return 0;
      }
      if (isMediumHedge()) {
        return 30;
      }
      if (isHighHedge()) {
        return 100;
      }
      if (isMagic()) {
        return 100;
      }
      return 20;
    }
    return Math.max(0, getState().state);
  }
  
  public final float getBlockPercent(Creature creature)
  {
    if (isFinished())
    {
      switch (Fence.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[this.type.ordinal()])
      {
      case 8: 
      case 9: 
      case 10: 
      case 11: 
      case 12: 
      case 13: 
      case 14: 
      case 15: 
      case 16: 
      case 17: 
      case 18: 
      case 40: 
      case 41: 
      case 42: 
      case 43: 
      case 44: 
      case 45: 
      case 164: 
        return 0.0F;
      case 1: 
      case 2: 
      case 3: 
      case 4: 
      case 5: 
      case 6: 
      case 7: 
      case 19: 
      case 20: 
      case 21: 
      case 26: 
      case 28: 
      case 32: 
      case 33: 
      case 46: 
      case 47: 
      case 48: 
      case 49: 
      case 50: 
      case 51: 
      case 52: 
      case 97: 
      case 99: 
      case 101: 
      case 103: 
      case 105: 
      case 151: 
      case 165: 
      case 166: 
      case 167: 
      case 168: 
      case 169: 
      case 170: 
      case 171: 
      case 172: 
      case 173: 
      case 174: 
      case 175: 
      case 176: 
        return 30.0F;
      case 157: 
        return 40.0F;
      case 23: 
      case 24: 
      case 25: 
      case 34: 
      case 35: 
      case 36: 
      case 37: 
      case 38: 
      case 39: 
      case 76: 
      case 77: 
      case 78: 
      case 79: 
      case 80: 
      case 81: 
      case 125: 
      case 126: 
      case 127: 
      case 128: 
      case 129: 
      case 130: 
      case 131: 
      case 132: 
      case 133: 
      case 134: 
      case 135: 
      case 136: 
      case 150: 
      case 162: 
      case 163: 
        return 75.0F;
      case 53: 
      case 54: 
      case 55: 
      case 56: 
      case 57: 
      case 58: 
      case 59: 
      case 60: 
      case 61: 
      case 62: 
      case 65: 
      case 66: 
      case 68: 
      case 70: 
      case 71: 
      case 72: 
      case 73: 
      case 74: 
      case 75: 
        return 100.0F;
      }
      return 20.0F;
    }
    return Math.max(0, getState().state);
  }
  
  public final boolean isItemRepair(Item item)
  {
    return item.getTemplateId() == getRepairItemTemplate();
  }
  
  public final float getCurrentQualityLevel()
  {
    return this.currentQL * Math.max(1.0F, 100.0F - this.damage) / 100.0F;
  }
  
  public static final Fence getFence(long fenceId)
  {
    int x = Tiles.decodeTileX(fenceId);
    int y = Tiles.decodeTileY(fenceId);
    int layer = Tiles.decodeLayer(fenceId);
    
    VolaTile tile = Zones.getTileOrNull(x, y, layer == 0);
    if (tile != null) {
      for (Fence f : tile.getFences()) {
        if ((f != null) && (f.getId() == fenceId)) {
          return f;
        }
      }
    }
    return null;
  }
  
  public abstract void setZoneId(int paramInt);
  
  public abstract void save()
    throws IOException;
  
  abstract void load()
    throws IOException;
  
  public abstract void improveOrigQualityLevel(float paramFloat);
  
  abstract boolean changeColor(int paramInt);
  
  public abstract void delete();
  
  public abstract void setLastUsed(long paramLong);
  
  public final String toString()
  {
    return "Fence [Tile: " + this.tilex + ", " + this.tiley + ", dir: " + this.dir + ", surfaced: " + this.surfaced + ", QL: " + this.currentQL + ", DMG: " + this.damage + ", type: " + this.type + ", state: " + this.state + ']';
  }
  
  public final int getHeightOffset()
  {
    return this.heightOffset;
  }
  
  public final boolean isOnFloorLevel(int level)
  {
    return level == this.floorLevel;
  }
  
  public final int getFloorLevel()
  {
    return this.floorLevel;
  }
  
  private final void setFloorLevel()
  {
    this.floorLevel = (this.heightOffset / 30);
  }
  
  public final byte getLayer()
  {
    return (byte)this.layer;
  }
  
  public boolean isOnSurface()
  {
    return this.layer == 0;
  }
  
  public final boolean isWithinFloorLevels(int maxFloorLevel, int minFloorLevel)
  {
    return (this.floorLevel <= maxFloorLevel) && (this.floorLevel >= minFloorLevel);
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
  
  public final int getEndX()
  {
    if (isHorizontal()) {
      return getStartX() + 1;
    }
    return getStartX();
  }
  
  public final int getEndY()
  {
    if (isHorizontal()) {
      return getStartY();
    }
    return getStartY() + 1;
  }
  
  public final boolean supports()
  {
    if ((isHedge()) || (isLowFence()) || (isMagic()) || (isFlowerbed())) {
      return false;
    }
    return true;
  }
  
  public float getFloorZ()
  {
    return this.heightOffset / 10;
  }
  
  public float getMinZ()
  {
    return 
    
      Math.min(Zones.getHeightForNode(getStartX(), getStartY(), getLayer()), Zones.getHeightForNode(getEndX(), getEndY(), getLayer())) + getFloorZ();
  }
  
  public float getMaxZ()
  {
    return 
    
      Math.max(Zones.getHeightForNode(getStartX(), getStartY(), getLayer()), Zones.getHeightForNode(getEndX(), getEndY(), getLayer())) + getFloorZ() + 3.0F;
  }
  
  public boolean isWithinZ(float maxZ, float minZ, boolean followGround)
  {
    return ((getFloorLevel() == 0) && (followGround)) || ((minZ <= getMaxZ()) && (maxZ >= getMinZ()));
  }
  
  public final boolean supports(StructureSupport support)
  {
    if (!supports()) {
      return false;
    }
    if (support.isFloor())
    {
      if ((getFloorLevel() == support.getFloorLevel()) || (getFloorLevel() == support.getFloorLevel() - 1)) {
        if (isHorizontal())
        {
          if (getStartX() == support.getStartX()) {
            if ((getStartY() == support.getStartY()) || (getStartY() == support.getEndY())) {
              return true;
            }
          }
        }
        else if (getStartY() == support.getStartY()) {
          if ((getStartX() == support.getStartX()) || (getStartX() == support.getEndX())) {
            return true;
          }
        }
      }
    }
    else
    {
      int levelMod = support.supports() ? -1 : 0;
      if ((support.getFloorLevel() >= getFloorLevel() + levelMod) && (support.getFloorLevel() <= getFloorLevel() + 1)) {
        if ((support.getMinX() == getMinX()) && (support.getMinY() == getMinY()) && 
          (isHorizontal() == support.isHorizontal())) {
          return true;
        }
      }
    }
    return false;
  }
  
  public boolean isSupportedByGround()
  {
    return getFloorLevel() == 0;
  }
  
  public long getTempId()
  {
    return -10L;
  }
  
  public String getTypeName()
  {
    switch (Fence.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[this.type.ordinal()])
    {
    case 63: 
    case 65: 
      return "wooden palisade";
    case 64: 
    case 66: 
      return "wooden palisade gate";
    case 21: 
    case 94: 
      return "low stone wall";
    case 67: 
    case 68: 
      return "tall stone wall";
    case 70: 
    case 82: 
      return "tall slate wall";
    case 71: 
    case 83: 
      return "tall rounded stone wall";
    case 72: 
    case 84: 
      return "tall sandstone wall";
    case 73: 
    case 85: 
      return "tall rendered wall";
    case 74: 
    case 86: 
      return "tall pottery wall";
    case 75: 
    case 87: 
      return "tall marble wall";
    case 33: 
    case 95: 
      return "stone fence";
    case 96: 
    case 97: 
      return "slate fence";
    case 98: 
    case 99: 
      return "rounded stone fence";
    case 100: 
    case 101: 
      return "pottery fence";
    case 102: 
    case 103: 
      return "sandstone fence";
    case 151: 
    case 179: 
      return "Rendered fence";
    case 104: 
    case 105: 
      return "marble fence";
    case 8: 
    case 121: 
      return "curb";
    case 29: 
    case 153: 
      return "wooden fence gate";
    case 22: 
    case 152: 
      return "wooden fence";
    case 31: 
    case 154: 
      return "crude wooden fence";
    case 30: 
    case 155: 
      return "crude wooden fence gate";
    case 28: 
    case 156: 
      return "roundpole fence gate";
    case 19: 
    case 159: 
      return "low roundpole fence";
    case 157: 
    case 158: 
      return "high roundpole fence";
    case 20: 
    case 177: 
      return "iron fence";
    case 165: 
    case 180: 
      return "slate iron fence";
    case 166: 
    case 181: 
      return "rounded stone iron fence";
    case 167: 
    case 182: 
      return "pottery iron fence";
    case 168: 
    case 183: 
      return "sandstone iron fence";
    case 169: 
    case 184: 
      return "plastered iron fence";
    case 170: 
    case 185: 
      return "marble iron fence";
    case 123: 
    case 162: 
      return "high iron fence";
    case 131: 
    case 143: 
      return "slate high iron fence";
    case 132: 
    case 144: 
      return "rounded stone high iron fence";
    case 133: 
    case 145: 
      return "sandstone high iron fence";
    case 134: 
    case 146: 
      return "rendered high iron fence";
    case 135: 
    case 147: 
      return "pottery high iron fence";
    case 136: 
    case 148: 
      return "marble high iron fence";
    case 32: 
    case 178: 
      return "iron fence gate";
    case 171: 
    case 186: 
      return "slate iron gate";
    case 172: 
    case 187: 
      return "rounded stone iron gate";
    case 173: 
    case 188: 
      return "pottery iron gate";
    case 174: 
    case 189: 
      return "sandstone iron gate";
    case 175: 
    case 190: 
      return "plastered iron gate";
    case 176: 
    case 191: 
      return "marble iron gate";
    case 124: 
    case 163: 
      return "high iron fence gate";
    case 125: 
    case 137: 
      return "slate high iron fence gate";
    case 126: 
    case 138: 
      return "rounded stone high iron fence gate";
    case 127: 
    case 139: 
      return "sandstone high iron fence gate";
    case 128: 
    case 140: 
      return "rendered high iron fence gate";
    case 129: 
    case 141: 
      return "pottery high iron fence gate";
    case 130: 
    case 142: 
      return "marble high iron fence gate";
    case 9: 
    case 160: 
      return "woven fence";
    case 10: 
    case 122: 
      return "low rope fence";
    case 26: 
    case 27: 
      return "high rope fence";
    case 24: 
    case 106: 
      return "wooden parapet";
    case 23: 
    case 107: 
      return "stone parapet";
    case 35: 
    case 109: 
      return "slate parapet";
    case 36: 
    case 110: 
      return "rounded stone parapet";
    case 34: 
    case 149: 
      return "sandstone parapet";
    case 37: 
    case 111: 
      return "rendered parapet";
    case 38: 
    case 112: 
      return "pottery parapet";
    case 39: 
    case 113: 
      return "marble parapet";
    case 12: 
      return "blue flowerbed";
    case 18: 
      return "yellow flowerbed";
    case 15: 
      return "purple flowerbed";
    case 16: 
      return "white flowerbed";
    case 17: 
      return "white-dotted flowerbed";
    case 13: 
      return "greenish-yellow flowerbed";
    case 14: 
      return "orange-red flowerbed";
    case 114: 
    case 164: 
      return "chain fence";
    case 40: 
    case 115: 
      return "slate chain fence";
    case 41: 
    case 116: 
      return "rounded stone chain fence";
    case 42: 
    case 117: 
      return "sandstone chain fence";
    case 43: 
    case 118: 
      return "rendered chain fence";
    case 44: 
    case 119: 
      return "pottery chain fence";
    case 45: 
    case 120: 
      return "marble chain fence";
    case 69: 
    case 150: 
      return "portcullis";
    case 76: 
    case 88: 
      return "slate portcullis";
    case 77: 
    case 89: 
      return "rounded stone portcullis";
    case 78: 
    case 90: 
      return "sandstone portcullis";
    case 79: 
    case 91: 
      return "rendered portcullis";
    case 80: 
    case 92: 
      return "pottery portcullis";
    case 81: 
    case 93: 
      return "marble portcullis";
    case 11: 
      return "rubble";
    case 161: 
      return "siege wall";
    }
    if (isHedge()) {
      return "hedge";
    }
    return "fence";
  }
  
  public final void setColor(int newcolor)
  {
    changeColor(newcolor);
  }
  
  Permissions getSettings()
  {
    return this.permissions;
  }
  
  public boolean isGate()
  {
    switch (Fence.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[this.type.ordinal()])
    {
    case 28: 
    case 29: 
    case 30: 
    case 32: 
    case 66: 
    case 76: 
    case 77: 
    case 78: 
    case 79: 
    case 80: 
    case 81: 
    case 125: 
    case 126: 
    case 127: 
    case 128: 
    case 129: 
    case 130: 
    case 150: 
    case 163: 
    case 171: 
    case 172: 
    case 173: 
    case 174: 
    case 175: 
    case 176: 
      return true;
    }
    return false;
  }
  
  void setSettings(int aSettings)
  {
    this.permissions.setPermissionBits(aSettings);
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
    return false;
  }
  
  public boolean canDisableLocking()
  {
    return isGate();
  }
  
  public boolean canDisableLockpicking()
  {
    return isGate();
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
    return !isHedge();
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
  
  public final String getName()
  {
    return getTypeName();
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
    return ((getStartX() == pos.x) || (getEndX() == pos.x)) && (getEndY() == pos.y + 1) && (getStartY() == pos.y + 1);
  }
  
  public final boolean isOnNorthBorder(TilePos pos)
  {
    return ((getStartX() == pos.x) || (getEndX() == pos.x)) && (getEndY() == pos.y) && (getStartY() == pos.y);
  }
  
  public final boolean isOnWestBorder(TilePos pos)
  {
    return (getStartX() == pos.x) && (getEndX() == pos.x) && ((getEndY() == pos.y) || (getStartY() == pos.y));
  }
  
  public final boolean isOnEastBorder(TilePos pos)
  {
    return (getStartX() == pos.x + 1) && (getEndX() == pos.x + 1) && ((getEndY() == pos.y) || (getStartY() == pos.y));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\structures\Fence.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */