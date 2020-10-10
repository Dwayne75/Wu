package com.wurmonline.server.zones;

import com.wurmonline.server.items.Item;

public class InfluenceZone
  extends GenericZone
{
  public InfluenceZone(Item i)
  {
    super(i);
  }
  
  public float getStrengthForTile(int tileX, int tileY, boolean surfaced)
  {
    if (getZoneItem() == null) {
      return 0.0F;
    }
    int xDiff = Math.abs(getZoneItem().getTileX() - tileX);
    int yDiff = Math.abs(getZoneItem().getTileY() - tileY);
    
    return getCurrentQL() - Math.max(xDiff, yDiff);
  }
  
  public void updateZone()
  {
    if (getZoneItem() == null)
    {
      setCachedQL(0.0F);
      return;
    }
    int dist = (int)getZoneItem().getCurrentQualityLevel();
    setBounds(getZoneItem().getTileX() - dist, getZoneItem().getTileY() - dist, 
      getZoneItem().getTileX() + dist, getZoneItem().getTileY() + dist);
    
    setCachedQL(getZoneItem().getCurrentQualityLevel());
  }
  
  protected float getCurrentQL()
  {
    if (getZoneItem() == null) {
      return 0.0F;
    }
    return getZoneItem().getCurrentQualityLevel();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\zones\InfluenceZone.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */