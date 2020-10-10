package com.wurmonline.server.structures;

import com.wurmonline.mesh.Tiles.TileBorderDirection;
import com.wurmonline.server.Items;
import com.wurmonline.server.items.Item;
import com.wurmonline.shared.constants.StructureConstantsEnum;
import com.wurmonline.shared.constants.StructureStateEnum;
import java.io.IOException;

public final class TempFence
  extends Fence
{
  private Item fenceItem;
  
  public TempFence(StructureConstantsEnum aType, int aTileX, int aTileY, int aHeightOffset, Item item, Tiles.TileBorderDirection aDir, int aZoneId, int aLayer)
  {
    super(aType, aTileX, aTileY, aHeightOffset, item.getQualityLevel(), aDir, aZoneId, aLayer);
    this.fenceItem = item;
    this.state = StructureStateEnum.FINISHED;
  }
  
  public void setZoneId(int zid)
  {
    this.zoneId = zid;
  }
  
  public void save()
    throws IOException
  {}
  
  void load()
    throws IOException
  {}
  
  public float getQualityLevel()
  {
    return this.fenceItem.getQualityLevel();
  }
  
  public float getOriginalQualityLevel()
  {
    return this.fenceItem.getOriginalQualityLevel();
  }
  
  public float getDamage()
  {
    return this.fenceItem.getDamage();
  }
  
  public boolean setDamage(float newDam)
  {
    return this.fenceItem.setDamage(this.fenceItem.getDamage() + newDam);
  }
  
  public boolean isTemporary()
  {
    return true;
  }
  
  public boolean setQualityLevel(float newQl)
  {
    return this.fenceItem.setQualityLevel(newQl);
  }
  
  public void improveOrigQualityLevel(float newQl)
  {
    this.fenceItem.setOriginalQualityLevel(newQl);
  }
  
  public void delete()
  {
    Items.destroyItem(this.fenceItem.getWurmId());
  }
  
  public void setLastUsed(long aLastUsed)
  {
    this.fenceItem.setLastMaintained(aLastUsed);
  }
  
  public final long getTempId()
  {
    return this.fenceItem.getWurmId();
  }
  
  public void savePermissions() {}
  
  boolean changeColor(int aNewcolor)
  {
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\structures\TempFence.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */