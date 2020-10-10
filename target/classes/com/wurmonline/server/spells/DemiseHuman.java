package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;

public final class DemiseHuman
  extends ReligiousSpell
{
  public static final int RANGE = 4;
  
  DemiseHuman()
  {
    super("Human Demise", 267, 30, 60, 80, 61, 0L);
    this.targetWeapon = true;
    this.enchantment = 9;
    this.effectdesc = "will deal increased damage to players and human creatures.";
    this.description = "increases damage dealt to players and human creatures";
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
    target.enchant((byte)9);
    performer.getCommunicator().sendNormalServerMessage("The " + target
      .getName() + " will now be effective against humans.", (byte)2);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\DemiseHuman.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */