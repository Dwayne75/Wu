package com.wurmonline.server.loot;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import java.util.Optional;
import java.util.Random;

public class DefaultItemCreateFunc
  implements ItemCreateFunc
{
  public Optional<Item> create(Creature victim, Creature receiver, LootItem lootItem)
  {
    return ItemFactory.createItemOptional(lootItem.getItemTemplateId(), 50.0F + Server.rand.nextFloat() * 40.0F, victim.getName());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\loot\DefaultItemCreateFunc.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */