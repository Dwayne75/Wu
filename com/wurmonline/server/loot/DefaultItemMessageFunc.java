package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.zones.VolaTile;

public class DefaultItemMessageFunc
  implements ItemMessageFunc
{
  public void message(Creature victim, Creature receiver, Item item)
  {
    receiver.getCommunicator().sendSafeServerMessage("You loot " + item.getNameWithGenus() + " from the corpse.", (byte)2);
    if (receiver.getCurrentTile() != null) {
      receiver.getCurrentTile().broadCastAction(receiver.getName() + " picks up " + item.getNameWithGenus() + " from the corpse.", receiver, false);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\loot\DefaultItemMessageFunc.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */