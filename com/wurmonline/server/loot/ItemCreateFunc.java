package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import java.util.Optional;

public abstract interface ItemCreateFunc
{
  public abstract Optional<Item> create(Creature paramCreature1, Creature paramCreature2, LootItem paramLootItem);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\loot\ItemCreateFunc.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */