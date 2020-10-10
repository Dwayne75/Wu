package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;
import java.util.Optional;

public abstract interface LootItemFunc
{
  public abstract Optional<LootItem> item(Creature paramCreature1, Creature paramCreature2, LootPool paramLootPool);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\loot\LootItemFunc.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */