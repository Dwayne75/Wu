package com.wurmonline.server.items;

import com.wurmonline.server.Items;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

public final class ItemVicinityRequirement
  extends CreationRequirement
{
  public ItemVicinityRequirement(int aNumber, int aResourceTemplateId, int aNumberNeeded, boolean aConsume, int aDistance)
  {
    super(aNumber, aResourceTemplateId, aNumberNeeded, aConsume);
    setDistance(aDistance);
  }
  
  public boolean fill(Creature performer, Item creation)
  {
    boolean toReturn = false;
    VolaTile tile = performer.getCurrentTile();
    if (tile == null) {
      return false;
    }
    VolaTile[] tiles = Zones.getTilesSurrounding(tile.tilex, tile.tiley, performer.isOnSurface(), getDistance());
    if (canBeFilled(tiles)) {
      if (willBeConsumed())
      {
        int found = 0;
        for (int x = 0; x < tiles.length; x++)
        {
          Item[] items = tiles[x].getItems();
          for (int i = 0; i < items.length; i++) {
            if (items[i].getTemplateId() == getResourceTemplateId())
            {
              found++;
              Items.destroyItem(items[i].getWurmId());
              if (found == getResourceNumber()) {
                return true;
              }
            }
          }
        }
      }
      else
      {
        toReturn = true;
      }
    }
    return toReturn;
  }
  
  public boolean canBeFilled(VolaTile[] tiles)
  {
    int found = 0;
    for (int x = 0; x < tiles.length; x++)
    {
      Item[] items = tiles[x].getItems();
      for (int i = 0; i < items.length; i++) {
        if (items[i].getTemplateId() == getResourceTemplateId())
        {
          found++;
          if (found == getResourceNumber()) {
            return true;
          }
        }
      }
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\items\ItemVicinityRequirement.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */