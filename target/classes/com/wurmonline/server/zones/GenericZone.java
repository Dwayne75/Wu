package com.wurmonline.server.zones;

import com.wurmonline.server.items.Item;

public abstract class GenericZone
{
  private Item zoneOwner;
  private float cachedQL;
  private int startX;
  private int startY;
  private int endX;
  private int endY;
  
  public GenericZone(Item i)
  {
    this.zoneOwner = i;
    updateZone();
  }
  
  public abstract float getStrengthForTile(int paramInt1, int paramInt2, boolean paramBoolean);
  
  public boolean containsTile(int tileX, int tileY)
  {
    if (this.zoneOwner == null) {
      return false;
    }
    if (this.cachedQL != getCurrentQL()) {
      updateZone();
    }
    if ((tileX >= this.startX) && (tileX <= this.endX) && 
      (tileY >= this.startY) && (tileY <= this.endY)) {
      return true;
    }
    return false;
  }
  
  public abstract void updateZone();
  
  public Item getZoneItem()
  {
    return this.zoneOwner;
  }
  
  public void setZoneItem(Item i)
  {
    this.zoneOwner = i;
  }
  
  public void setCachedQL(float ql)
  {
    this.cachedQL = ql;
  }
  
  public void setBounds(int sx, int sy, int ex, int ey)
  {
    this.startX = sx;
    this.startY = sy;
    this.endX = ex;
    this.endY = ey;
  }
  
  protected abstract float getCurrentQL();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\zones\GenericZone.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */