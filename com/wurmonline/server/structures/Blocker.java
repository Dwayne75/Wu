package com.wurmonline.server.structures;

import com.wurmonline.math.TilePos;
import com.wurmonline.math.Vector3f;
import com.wurmonline.server.creatures.Creature;

public abstract interface Blocker
{
  public static final int TYPE_NONE = 0;
  public static final int TYPE_FENCE = 1;
  public static final int TYPE_WALL = 2;
  public static final int TYPE_FLOOR = 3;
  public static final int TYPE_ALL = 4;
  public static final int TYPE_ALL_BUT_OPEN = 5;
  public static final int TYPE_MOVEMENT = 6;
  public static final int TYPE_TARGET_TILE = 7;
  public static final int TYPE_NOT_DOOR = 8;
  
  public abstract boolean isFence();
  
  public abstract boolean isStone();
  
  public abstract boolean isWood();
  
  public abstract boolean isMetal();
  
  public abstract boolean isWall();
  
  public abstract boolean isDoor();
  
  public abstract boolean isFloor();
  
  public abstract boolean isRoof();
  
  public abstract boolean isTile();
  
  public abstract boolean isStair();
  
  public abstract boolean canBeOpenedBy(Creature paramCreature, boolean paramBoolean);
  
  public abstract int getFloorLevel();
  
  public abstract String getName();
  
  public abstract Vector3f getNormal();
  
  public abstract Vector3f getCenterPoint();
  
  public abstract int getTileX();
  
  public abstract int getTileY();
  
  public abstract boolean isOnSurface();
  
  public abstract float getPositionX();
  
  public abstract float getPositionY();
  
  public abstract boolean isHorizontal();
  
  public abstract boolean isWithinFloorLevels(int paramInt1, int paramInt2);
  
  public abstract Vector3f isBlocking(Creature paramCreature, Vector3f paramVector3f1, Vector3f paramVector3f2, Vector3f paramVector3f3, int paramInt, long paramLong, boolean paramBoolean);
  
  public abstract float getBlockPercent(Creature paramCreature);
  
  public abstract float getDamageModifier();
  
  public abstract boolean setDamage(float paramFloat);
  
  public abstract float getDamage();
  
  public abstract long getId();
  
  public abstract long getTempId();
  
  public abstract float getMinZ();
  
  public abstract float getMaxZ();
  
  public abstract float getFloorZ();
  
  public abstract boolean isWithinZ(float paramFloat1, float paramFloat2, boolean paramBoolean);
  
  public abstract boolean isOnSouthBorder(TilePos paramTilePos);
  
  public abstract boolean isOnNorthBorder(TilePos paramTilePos);
  
  public abstract boolean isOnWestBorder(TilePos paramTilePos);
  
  public abstract boolean isOnEastBorder(TilePos paramTilePos);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\structures\Blocker.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */