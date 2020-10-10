package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;

public final class Mend
  extends ReligiousSpell
{
  public static final int RANGE = 4;
  
  Mend()
  {
    super("Mend", 251, 20, 29, 20, 29, 0L);
    this.targetItem = true;
    this.description = "removes damage from an item at the cost of some quality";
    this.type = 1;
  }
  
  boolean precondition(Skill castSkill, Creature performer, Item target)
  {
    if ((!mayBeEnchanted(target)) || (target.isFood()))
    {
      performer.getCommunicator().sendNormalServerMessage("You cannot mend that.", (byte)3);
      return false;
    }
    if (target.getDamage() <= 0.0F)
    {
      performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is not damaged.", (byte)3);
      
      return false;
    }
    if (target.getQualityLevel() <= 2.0F)
    {
      performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " would break under the power of the spell.", (byte)3);
      
      return false;
    }
    return true;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Item target)
  {
    float oldDamage = target.getDamage();
    float qlReduction = 2.0F;
    if (oldDamage < 20.0F) {
      qlReduction *= oldDamage / 20.0F;
    }
    target.setDamage(Math.max(oldDamage - 20.0F, 0.0F));
    target.setQualityLevel(Math.max(target.getQualityLevel() - qlReduction, 0.0F));
  }
  
  void doNegativeEffect(Skill castSkill, double power, Creature performer, Item target)
  {
    if (power < -80.0D)
    {
      performer.getCommunicator().sendNormalServerMessage("You fail miserably and the spell has the opposite effect.", (byte)3);
      
      target.setDamage(target.getDamage() + 1.0F);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\Mend.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */