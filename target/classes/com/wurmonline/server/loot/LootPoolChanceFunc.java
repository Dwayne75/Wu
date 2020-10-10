package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;

public abstract interface LootPoolChanceFunc
{
  public abstract boolean chance(Creature paramCreature1, Creature paramCreature2, LootPool paramLootPool);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\loot\LootPoolChanceFunc.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */