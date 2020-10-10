package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.shared.constants.AttitudeConstants;

public class WormBrains
  extends DamageSpell
  implements AttitudeConstants
{
  public static final int RANGE = 50;
  public static final double BASE_DAMAGE = 17500.0D;
  public static final double DAMAGE_PER_POWER = 120.0D;
  
  public WormBrains()
  {
    super("Worm Brains", 430, 15, 40, 40, 51, 60000L);
    this.targetCreature = true;
    this.offensive = true;
    this.description = "damages the target severely with internal damage";
    this.type = 2;
  }
  
  boolean precondition(Skill castSkill, Creature performer, Creature target)
  {
    if (((target.isHuman()) || (target.isDominated())) && (target.getAttitude(performer) != 2)) {
      if (performer.faithful)
      {
        performer.getCommunicator().sendNormalServerMessage(performer
          .getDeity().getName() + " would never accept your spell on " + target.getName() + ".", (byte)3);
        
        return false;
      }
    }
    return true;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Creature target)
  {
    if (((target.isHuman()) || (target.isDominated())) && (target.getAttitude(performer) != 2)) {
      performer.modifyFaith(-5.0F);
    }
    double damage = calculateDamage(target, power, 17500.0D, 120.0D);
    
    target.addWoundOfType(performer, (byte)9, 1, false, 1.0F, false, damage, 0.0F, 0.0F, false, true);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\WormBrains.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */