package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;

public abstract interface ItemMessageFunc
{
  public abstract void message(Creature paramCreature1, Creature paramCreature2, Item paramItem);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\loot\ItemMessageFunc.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */