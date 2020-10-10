package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.skills.Skill;

public final class DrainStamina
  extends ReligiousSpell
{
  public static final int RANGE = 12;
  
  DrainStamina()
  {
    super("Drain Stamina", 254, 9, 20, 20, 10, 0L);
    this.targetCreature = true;
    this.offensive = true;
    this.description = "drains stamina from a creature and returns it to you";
    this.type = 2;
  }
  
  boolean precondition(Skill castSkill, Creature performer, Creature target)
  {
    if (target.isReborn())
    {
      performer.getCommunicator().sendNormalServerMessage("You can not drain stamina from the " + target
        .getNameWithGenus() + ".", (byte)3);
      
      return false;
    }
    if (target.equals(performer)) {
      return false;
    }
    if (target.getStatus().getStamina() < 200)
    {
      performer.getCommunicator().sendNormalServerMessage(target
        .getNameWithGenus() + " does not have enough stamina to drain.", (byte)3);
      
      return false;
    }
    return true;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Creature target)
  {
    int stam = target.getStatus().getStamina();
    
    int staminaGained = (int)(Math.max(power, 20.0D) / 200.0D * stam * target.addSpellResistance((short)254));
    if (staminaGained > 1)
    {
      performer.getStatus().modifyStamina((int)(0.75D * staminaGained));
      target.getStatus().modifyStamina(-staminaGained);
      performer.getCommunicator().sendNormalServerMessage("You drain some stamina from " + target.getNameWithGenus() + ".", (byte)4);
      
      target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " drains you on stamina.", (byte)4);
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("You try to drain some stamina from " + target
        .getNameWithGenus() + " but fail.", (byte)3);
      
      target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " tries to drain you on stamina but fails.", (byte)4);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\DrainStamina.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */