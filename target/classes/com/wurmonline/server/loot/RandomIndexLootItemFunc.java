package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class RandomIndexLootItemFunc
  implements LootItemFunc
{
  public Optional<LootItem> item(Creature victim, Creature receiver, LootPool pool)
  {
    return Optional.ofNullable(pool.getLootItems().get(pool.getRandom().nextInt(pool.getLootItems().size())));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\loot\RandomIndexLootItemFunc.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */