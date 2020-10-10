package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;

public class ItemEnchantment
  extends ReligiousSpell
{
  public static final int RANGE = 4;
  
  ItemEnchantment(String aName, int aNum, int aCastingTime, int aCost, int aDifficulty, int aLevel, long aCooldown)
  {
    super(aName, aNum, aCastingTime, aCost, aDifficulty, aLevel, aCooldown);
  }
  
  boolean precondition(Skill castSkill, Creature performer, Item target)
  {
    if (!mayBeEnchanted(target))
    {
      EnchantUtil.sendCannotBeEnchantedMessage(performer);
      return false;
    }
    SpellEffect negatingEffect = EnchantUtil.hasNegatingEffect(target, getEnchantment());
    if (negatingEffect != null)
    {
      EnchantUtil.sendNegatingEffectMessage(getName(), performer, target, negatingEffect);
      return false;
    }
    return true;
  }
  
  boolean precondition(Skill castSkill, Creature performer, Creature target)
  {
    return false;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Item target)
  {
    enchantItem(performer, target, getEnchantment(), (float)power);
  }
  
  void doNegativeEffect(Skill castSkill, double power, Creature performer, Item target)
  {
    checkDestroyItem(power, performer, target);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\ItemEnchantment.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */