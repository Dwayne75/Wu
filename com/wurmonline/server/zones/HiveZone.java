package com.wurmonline.server.zones;

import com.wurmonline.server.items.Item;
import java.util.logging.Logger;

public final class HiveZone
{
  private static final Logger logger = Logger.getLogger(HiveZone.class.getName());
  private Item hive;
  private final int areaRadius;
  
  public HiveZone(Item hive)
  {
    this.hive = hive;
    this.areaRadius = (1 + (int)Math.sqrt(hive.getCurrentQualityLevel()));
  }
  
  public int getStrengthForTile(int tileX, int tileY, boolean surfaced)
  {
    return 
      this.areaRadius - Math.max(Math.abs(this.hive.getTileX() - tileX), Math.abs(this.hive.getTileY() - tileY));
  }
  
  public int getStartX()
  {
    return this.hive.getTileX() - this.areaRadius;
  }
  
  public int getStartY()
  {
    return this.hive.getTileY() - this.areaRadius;
  }
  
  public int getEndX()
  {
    return this.hive.getTileX() + this.areaRadius;
  }
  
  public int getEndY()
  {
    return this.hive.getTileY() + this.areaRadius;
  }
  
  public boolean containsTile(int tileX, int tileY)
  {
    return (tileX > getStartX()) && (tileX < getEndX()) && 
      (tileY > getStartY()) && (tileY < getEndY());
  }
  
  public boolean isCloseToTile(int tileX, int tileY)
  {
    return getDistanceFrom(tileX, tileY) < 10 + this.areaRadius;
  }
  
  public Item getCurrentHive()
  {
    return this.hive;
  }
  
  public boolean hasHive(int tilex, int tiley)
  {
    return (this.hive.getTileX() == tilex) && (this.hive.getTileY() == tiley);
  }
  
  public int getDistanceFrom(int tilex, int tiley)
  {
    return Math.max(Math.abs(this.hive.getTileX() - tilex), Math.abs(this.hive.getTileY() - tiley));
  }
  
  public boolean isClose(int tilex, int tiley)
  {
    return getDistanceFrom(tilex, tiley) < 2;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\zones\HiveZone.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */