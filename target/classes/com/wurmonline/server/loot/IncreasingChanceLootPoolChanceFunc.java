package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class IncreasingChanceLootPoolChanceFunc
  implements LootPoolChanceFunc
{
  protected static final Logger logger = Logger.getLogger(IncreasingChanceLootPoolChanceFunc.class.getName());
  private ConcurrentHashMap<Long, Double> progressiveChances = new ConcurrentHashMap();
  private double percentIncrease = 0.0010000000474974513D;
  
  public IncreasingChanceLootPoolChanceFunc setPercentIncrease(double increase)
  {
    this.percentIncrease = increase;
    return this;
  }
  
  public boolean chance(Creature victim, Creature receiver, LootPool pool)
  {
    double r = pool.getRandom().nextDouble();
    double bonus = ((Double)Optional.ofNullable(this.progressiveChances.get(Long.valueOf(receiver.getWurmId()))).orElse(Double.valueOf(0.0D))).doubleValue();
    boolean success = r < pool.getLootPoolChance() + bonus;
    if (!success)
    {
      this.progressiveChances.put(Long.valueOf(receiver.getWurmId()), Double.valueOf(bonus + this.percentIncrease));
      logger.info(receiver.getName() + " failed loot pool chance for " + pool.getName() + ". Increasing chance by " + this.percentIncrease + " to " + (pool
        .getLootPoolChance() + ((Double)this.progressiveChances.get(Long.valueOf(receiver.getWurmId()))).doubleValue()));
    }
    else
    {
      this.progressiveChances.remove(Long.valueOf(receiver.getWurmId()));
      logger.info(receiver.getName() + " succeeded loot pool chance for " + pool.getName() + ". Clearing increases.");
    }
    return success;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\loot\IncreasingChanceLootPoolChanceFunc.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */