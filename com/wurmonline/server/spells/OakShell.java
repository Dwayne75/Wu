package com.wurmonline.server.spells;

import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;

public class OakShell
  extends CreatureEnchantment
{
  public static final int RANGE = 4;
  
  public OakShell()
  {
    super("Oakshell", 404, 10, 20, 19, 35, 30000L);
    this.enchantment = 22;
    this.effectdesc = "increased natural armour.";
    this.description = "increases the natural armour of a creature or player, does not stack with armour";
    this.type = 2;
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\OakShell.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */