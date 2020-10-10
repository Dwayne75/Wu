package com.wurmonline.server.zones;

import com.wurmonline.server.items.Item;

public class TurretZone
  extends GenericZone
{
  public static final float DISTMOD_QLMULTIPLIER = 5.0F;
  public static final int DISTMOD_TURRET = 3;
  public static final int DISTMOD_ARCHERYTOWER = 5;
  
  public TurretZone(Item i)
  {
    super(i);
    updateZone();
  }
  
  public float getStrengthForTile(int tileX, int tileY, boolean surfaced)
  {
    if (getZoneItem() == null) {
      return 0.0F;
    }
    if (getZoneItem().getTemplateId() == 934) {
      return 0.0F;
    }
    if (getZoneItem().isOnSurface() != surfaced) {
      return 0.0F;
    }
    if (!containsTile(tileX, tileY)) {
      return 0.0F;
    }
    int xDiff = Math.abs(tileX - getZoneItem().getTileX()) * 4;
    int yDiff = Math.abs(tileY - getZoneItem().getTileY()) * 4;
    
    float actDist = (float)Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    
    return getCurrentQL() - actDist;
  }
  
  public void updateZone()
  {
    if (getZoneItem() == null) {
      return;
    }
    float ql = getCurrentQL();
    float distanceModifier = ql / 100.0F * 5.0F;
    int dist = (int)((getZoneItem().isEnchantedTurret() ? 3 : 5) * distanceModifier);
    
    setBounds(getZoneItem().getTileX() - dist, getZoneItem().getTileY() - dist, 
      getZoneItem().getTileX() + dist, getZoneItem().getTileY() + dist);
    
    setCachedQL(ql);
  }
  
  protected float getCurrentQL()
  {
    if (getZoneItem() == null) {
      return 0.0F;
    }
    if ((getZoneItem().isEnchantedTurret()) && (!getZoneItem().isPlanted())) {
      return 0.0F;
    }
    return getZoneItem().getCurrentQualityLevel();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\zones\TurretZone.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */