package com.wurmonline.server.spells;

import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.SpellEffectsEnum;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

public class SpellResist
  implements TimeConstants
{
  protected static Logger logger = Logger.getLogger(SpellResist.class.getName());
  protected static ArrayList<Creature> resistingCreatures = new ArrayList();
  public Creature creature;
  public long lastUpdated;
  public double currentResistance;
  public long fullyExpires;
  public SpellEffectsEnum spellEffect = SpellEffectsEnum.RES_HEAL;
  public double recoveryPerSecond = 0.016666666666666666D;
  protected static final int GROUP_HEALING = 0;
  protected static final int GROUP_DRAIN_HEALTH = 1;
  protected static final int GROUP_FUNGUS_TRAP = 2;
  protected static final int GROUP_ICE_PILLAR = 3;
  protected static final int GROUP_FIRE_PILLAR = 4;
  protected static final int GROUP_TENTACLES = 5;
  protected static final int GROUP_PAIN_RAIN = 6;
  protected static final int GROUP_ROTTING_GUT = 7;
  protected static final int GROUP_TORNADO = 8;
  protected static final int GROUP_HEAVY_NUKE = 9;
  protected static final int GROUP_FIREHEART = 10;
  protected static final int GROUP_SHARDOFICE = 11;
  protected static final int GROUP_SCORN_OF_LIBILA = 12;
  protected static final int GROUP_HUMID_DRIZZLE = 13;
  protected static final int GROUP_SMITE = 14;
  protected static final int GROUP_LOCATE = 15;
  protected static final int GROUP_WRATH_MAGRANON = 16;
  protected static final int GROUP_DISPEL = 17;
  protected static final double HEALING_RECOVERY_SECOND = 8.0E-4D;
  
  public SpellResist(Creature creature)
  {
    this.creature = creature;
    this.lastUpdated = System.currentTimeMillis();
    this.currentResistance = 1.0D;
    this.fullyExpires = this.lastUpdated;
  }
  
  public double scalePower(double power)
  {
    return power;
  }
  
  public static SpellResist getNewResistanceForGroup(Creature creature, int group)
  {
    switch (group)
    {
    case 0: 
      return new SpellResist.HealingResist(creature);
    case 1: 
      return new SpellResist.DrainHealthResist(creature);
    case 2: 
      return new SpellResist.FungusTrapResist(creature);
    case 3: 
      return new SpellResist.IcePillarResist(creature);
    case 4: 
      return new SpellResist.FirePillarResist(creature);
    case 5: 
      return new SpellResist.TentaclesResist(creature);
    case 6: 
      return new SpellResist.PainRainResist(creature);
    case 7: 
      return new SpellResist.RottingGutResist(creature);
    case 8: 
      return new SpellResist.TornadoResist(creature);
    case 9: 
      return new SpellResist.HeavyNukeResist(creature);
    case 10: 
      return new SpellResist.FireHeartResist(creature);
    case 11: 
      return new SpellResist.ShardOfIceResist(creature);
    case 12: 
      return new SpellResist.ScornOfLibilaResist(creature);
    case 13: 
      return new SpellResist.HumidDrizzleResist(creature);
    case 14: 
      return new SpellResist.SmiteResist(creature);
    case 15: 
      return new SpellResist.LocateResist(creature);
    case 16: 
      return new SpellResist.WrathMagranonResist(creature);
    case 17: 
      return new SpellResist.DispelResist(creature);
    }
    logger.warning(String.format("Could not find a proper SpellResist instance for resist group %s.", new Object[] { Integer.valueOf(group) }));
    return new SpellResist.HealingResist(creature);
  }
  
  public static int getSpellGroup(int spellNumber)
  {
    switch (spellNumber)
    {
    case 246: 
    case 247: 
    case 248: 
    case 249: 
    case 408: 
    case 409: 
    case 438: 
      return 0;
    case 255: 
      return 1;
    case 433: 
      return 2;
    case 414: 
      return 3;
    case 420: 
      return 4;
    case 418: 
      return 5;
    case 432: 
      return 6;
    case 428: 
      return 7;
    case 413: 
      return 8;
    case 430: 
    case 931: 
    case 932: 
      return 9;
    case 424: 
      return 10;
    case 485: 
      return 11;
    case 448: 
      return 12;
    case 407: 
      return 13;
    case 252: 
      return 14;
    case 419: 
    case 451: 
      return 15;
    case 441: 
      return 16;
    case 450: 
      return 17;
    }
    logger.warning(String.format("Could not find a proper SpellResist group for spell number %s.", new Object[] { Integer.valueOf(spellNumber) }));
    return 0;
  }
  
  protected static double updateSpellResistance(Creature creature, SpellResist res, double additionalResistance)
  {
    long timeDelta = System.currentTimeMillis() - res.lastUpdated;
    double secondsPassed = timeDelta / 1000.0D;
    res.currentResistance = Math.min(1.0D, res.currentResistance + secondsPassed * res.recoveryPerSecond);
    res.currentResistance = Math.max(0.0D, res.currentResistance - additionalResistance);
    res.lastUpdated = System.currentTimeMillis();
    double secondsUntilFullyHealed = (1.0D - res.currentResistance) / res.recoveryPerSecond;
    res.fullyExpires = ((System.currentTimeMillis() + secondsUntilFullyHealed * 1000.0D));
    if (res.spellEffect != null)
    {
      creature.getCommunicator().sendAddStatusEffect(res.spellEffect, (int)secondsUntilFullyHealed);
      if (!resistingCreatures.contains(creature)) {
        resistingCreatures.add(creature);
      }
    }
    return res.currentResistance;
  }
  
  public static double getSpellResistance(Creature creature, int spellNumber)
  {
    HashMap<Integer, SpellResist> resistances = creature.getSpellResistances();
    int group = getSpellGroup(spellNumber);
    if (resistances.containsKey(Integer.valueOf(group)))
    {
      SpellResist res = (SpellResist)resistances.get(Integer.valueOf(group));
      return updateSpellResistance(creature, res, 0.0D);
    }
    return 1.0D;
  }
  
  public static void addSpellResistance(Creature creature, int spellNumber, double power)
  {
    HashMap<Integer, SpellResist> resistances = creature.getSpellResistances();
    int group = getSpellGroup(spellNumber);
    if (resistances.containsKey(Integer.valueOf(group)))
    {
      SpellResist res = (SpellResist)resistances.get(Integer.valueOf(group));
      double reduction = res.scalePower(power);
      double castPower = 0.0D;
      if (group == 9) {
        switch (spellNumber)
        {
        case 430: 
          castPower = power * 100.0D / 17500.0D + 12000.0D;
          reduction = castPower * res.recoveryPerSecond * 3.0D;
          break;
        case 931: 
          castPower = power * 100.0D / 25000.0D + 7500.0D;
          reduction = castPower * res.recoveryPerSecond * 3.0D;
          break;
        case 932: 
          castPower = power * 100.0D / 10000.0D + 25000.0D;
          reduction = castPower * res.recoveryPerSecond * 3.5D;
        }
      }
      updateSpellResistance(creature, res, reduction);
    }
    else
    {
      SpellResist res = getNewResistanceForGroup(creature, group);
      double reduction = res.scalePower(power);
      updateSpellResistance(creature, res, reduction);
      resistances.put(Integer.valueOf(group), res);
    }
  }
  
  public static long lastPolledSpellResist = 0L;
  public static final long pollSpellResistTime = 1000L;
  
  public static void onServerPoll()
  {
    long now = System.currentTimeMillis();
    if (lastPolledSpellResist + 1000L <= now)
    {
      ArrayList<Creature> crets = new ArrayList(resistingCreatures);
      for (Creature cret : crets)
      {
        HashMap<Integer, SpellResist> resistances = cret.getSpellResistances();
        Iterator localIterator2;
        if (!resistances.isEmpty())
        {
          Set<Integer> nums = new HashSet(resistances.keySet());
          for (localIterator2 = nums.iterator(); localIterator2.hasNext();)
          {
            int num = ((Integer)localIterator2.next()).intValue();
            
            SpellResist res = (SpellResist)resistances.get(Integer.valueOf(num));
            if (res.fullyExpires <= System.currentTimeMillis())
            {
              if (res.creature != null) {
                res.creature.getCommunicator().sendRemoveSpellEffect(res.spellEffect);
              }
              resistances.remove(Integer.valueOf(num));
            }
          }
        }
        else
        {
          resistingCreatures.remove(cret);
        }
      }
      lastPolledSpellResist = System.currentTimeMillis();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\SpellResist.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */