package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;

public final class DemiseAnimal
  extends ReligiousSpell
{
  public static final int RANGE = 4;
  
  DemiseAnimal()
  {
    super("Animal Demise", 269, 30, 40, 50, 43, 0L);
    this.targetWeapon = true;
    this.enchantment = 11;
    this.effectdesc = "will deal increased damage to animals.";
    this.description = "increases damage dealt to animals";
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
    target.enchant((byte)11);
    performer.getCommunicator().sendNormalServerMessage("The " + target
      .getName() + " will now be effective against animals.", (byte)2);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\DemiseAnimal.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */