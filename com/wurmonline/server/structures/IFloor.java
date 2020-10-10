package com.wurmonline.server.structures;

import com.wurmonline.server.items.Item;
import com.wurmonline.server.zones.VolaTile;

public abstract interface IFloor
  extends StructureSupport
{
  public abstract float getDamageModifierForItem(Item paramItem);
  
  public abstract long getStructureId();
  
  public abstract VolaTile getTile();
  
  public abstract boolean isOnPvPServer();
  
  public abstract int getTileX();
  
  public abstract int getTileY();
  
  public abstract float getCurrentQualityLevel();
  
  public abstract float getDamage();
  
  public abstract boolean setDamage(float paramFloat);
  
  public abstract float getQualityLevel();
  
  public abstract void destroyOrRevertToPlan();
  
  public abstract boolean isAPlan();
  
  public abstract boolean isThatch();
  
  public abstract boolean isStone();
  
  public abstract boolean isSandstone();
  
  public abstract boolean isSlate();
  
  public abstract boolean isMarble();
  
  public abstract boolean isMetal();
  
  public abstract boolean isWood();
  
  public abstract boolean isFinished();
  
  public abstract int getRepairItemTemplate();
  
  public abstract boolean setQualityLevel(float paramFloat);
  
  public abstract void setLastUsed(long paramLong);
  
  public abstract boolean isOnSurface();
  
  public abstract boolean equals(StructureSupport paramStructureSupport);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\structures\IFloor.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */