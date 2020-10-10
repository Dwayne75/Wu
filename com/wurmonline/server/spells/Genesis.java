package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;

public class Genesis
  extends DamageSpell
{
  public static final int RANGE = 4;
  public static final double BASE_DAMAGE = 32767.5D;
  public static final double DAMAGE_PER_POWER = 491.5125D;
  
  public Genesis()
  {
    super("Genesis", 408, 10, 30, 40, 70, 30000L);
    this.targetCreature = true;
    this.description = "cleanses a creature of a single negative trait";
    this.type = 1;
  }
  
  boolean precondition(Skill castSkill, Creature performer, Creature target)
  {
    if (target != performer) {
      return performer.getDeity() != null;
    }
    return false;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Creature target)
  {
    if (target.isReborn())
    {
      performer.getCommunicator().sendNormalServerMessage("You rid the " + target.getName() + " of its evil spirit and it collapses.");
      double damage = calculateDamage(target, power, 32767.5D, 491.5125D);
      target.addWoundOfType(performer, (byte)9, 2, false, 1.0F, false, damage, 0.0F, 0.0F, false, true);
    }
    else if (target.removeRandomNegativeTrait())
    {
      performer.getCommunicator().sendNormalServerMessage("You rid the " + target.getName() + " of an evil spirit. It will now produce healthier offspring.");
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " was not possessed by any evil spirit.", (byte)3);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\Genesis.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */