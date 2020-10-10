package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;

public final class DemiseLegendary
  extends ReligiousSpell
{
  public static final int RANGE = 4;
  
  DemiseLegendary()
  {
    super("Legendary Demise", 270, 30, 50, 50, 51, 0L);
    this.targetWeapon = true;
    this.enchantment = 12;
    this.effectdesc = "will deal increased damage to legendary creatures.";
    this.description = "increases damage dealt to legendary creatures";
    this.singleItemEnchant = true;
    this.type = 1;
  }
  
  boolean precondition(Skill castSkill, Creature performer, Item target)
  {
    return EnchantUtil.canEnchantDemise(performer, target);
  }
  
  boolean precondition(Skill castSkill, Creature performer, Creature target)
  {
    return false;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Item target)
  {
    target.enchant((byte)12);
    performer.getCommunicator().sendNormalServerMessage("The " + target
      .getName() + " will now be effective against legendary creatures.", (byte)2);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\DemiseLegendary.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */