package com.wurmonline.server.items;

import com.wurmonline.server.Items;
import com.wurmonline.server.creatures.Creature;

final class ItemContainerRequirement
  extends CreationRequirement
{
  ItemContainerRequirement(int aNumber, int aResourceTemplateId, int aResourceNumber, int aVolumeNeeded, boolean aConsume)
  {
    super(aNumber, aResourceTemplateId, aResourceNumber, aConsume);
    setVolumeNeeded(aVolumeNeeded);
  }
  
  boolean fill(Creature performer, Item container)
  {
    if (canBeFilled(container)) {
      if (willBeConsumed())
      {
        int found = 0;
        Item[] items = container.getAllItems(false);
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
    return false;
  }
  
  private boolean canBeFilled(Item container)
  {
    int found = 0;
    Item[] items = container.getAllItems(false);
    for (int i = 0; i < items.length; i++) {
      if (items[i].getTemplateId() == getResourceTemplateId())
      {
        found++;
        if (found == getResourceNumber()) {
          return true;
        }
      }
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\items\ItemContainerRequirement.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */