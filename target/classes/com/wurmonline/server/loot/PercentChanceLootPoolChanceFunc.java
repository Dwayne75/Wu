package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;
import java.util.Random;
import java.util.logging.Logger;

public class PercentChanceLootPoolChanceFunc
  implements LootPoolChanceFunc
{
  protected static final Logger logger = Logger.getLogger(PercentChanceLootPoolChanceFunc.class.getName());
  
  public boolean chance(Creature victim, Creature receiver, LootPool pool)
  {
    double r = pool.getRandom().nextDouble();
    boolean success = r < pool.getLootPoolChance();
    if (!success) {
      logger.info(receiver.getName() + " failed loot pool chance for " + pool.getName() + ": " + r + " not less than " + pool.getLootPoolChance());
    }
    return success;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\loot\PercentChanceLootPoolChanceFunc.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */