package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.skills.Skill;

public class SixthSense
  extends CreatureEnchantment
{
  public static final int RANGE = 4;
  
  SixthSense()
  {
    super("Sixth Sense", 376, 10, 15, 20, 6, 0L);
    this.targetCreature = true;
    this.enchantment = 21;
    this.effectdesc = "detect hidden dangers.";
    this.description = "detect hidden creatures and traps";
    this.type = 0;
  }
  
  boolean precondition(Skill castSkill, Creature performer, Creature target)
  {
    if (!target.isPlayer())
    {
      performer.getCommunicator().sendNormalServerMessage("You can only cast that on a person.");
      return false;
    }
    if (target.isReborn()) {
      return false;
    }
    if (!target.equals(performer))
    {
      if (performer.getDeity() != null)
      {
        if (target.getDeity() != null)
        {
          if (target.getDeity().isHateGod()) {
            return performer.isFaithful();
          }
          return true;
        }
        return true;
      }
      return true;
    }
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\SixthSense.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */