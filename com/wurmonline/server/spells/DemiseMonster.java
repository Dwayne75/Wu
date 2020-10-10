package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;

public final class DemiseMonster
  extends ReligiousSpell
{
  public static final int RANGE = 4;
  
  DemiseMonster()
  {
    super("Monster Demise", 268, 30, 40, 40, 41, 0L);
    this.targetWeapon = true;
    this.enchantment = 10;
    this.effectdesc = "will deal increased damage to monstrous creatures.";
    this.description = "increases damage dealt to monstrous creatures";
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
    target.enchant((byte)10);
    performer.getCommunicator().sendNormalServerMessage("The " + target
      .getName() + " will now be effective against monsters.", (byte)2);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\DemiseMonster.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */