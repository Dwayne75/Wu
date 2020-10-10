package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;

public class LurkerDeep
  extends ItemEnchantment
{
  public static final int RANGE = 4;
  
  public LurkerDeep()
  {
    super("Lurker in the Deep", 457, 20, 30, 60, 31, 0L);
    this.targetPendulum = true;
    this.enchantment = 48;
    this.effectdesc = "will locate rare fish.";
    this.description = "locates rare fish";
  }
  
  boolean precondition(Skill castSkill, Creature performer, Item target)
  {
    if (target.getTemplateId() != 233)
    {
      performer.getCommunicator().sendNormalServerMessage("This would work well on a pendulum.", (byte)3);
      return false;
    }
    return mayBeEnchanted(target);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\LurkerDeep.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */