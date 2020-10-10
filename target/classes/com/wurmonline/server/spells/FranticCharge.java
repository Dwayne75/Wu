package com.wurmonline.server.spells;

import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;

public class FranticCharge
  extends CreatureEnchantment
{
  public static final int RANGE = 4;
  
  public FranticCharge()
  {
    super("Frantic Charge", 423, 5, 20, 30, 30, 0L);
    this.targetCreature = true;
    this.enchantment = 39;
    this.effectdesc = "faster attack and movement speed.";
    this.description = "increases attack and movement speed of a player";
  }
  
  boolean precondition(Skill castSkill, Creature performer, Creature target)
  {
    if (super.precondition(castSkill, performer, target))
    {
      if ((Servers.isThisAPvpServer()) && (!target.isPlayer()))
      {
        performer.getCommunicator().sendNormalServerMessage("You cannot cast " + getName() + " on " + target.getNameWithGenus());
        return false;
      }
    }
    else {
      return false;
    }
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\FranticCharge.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */