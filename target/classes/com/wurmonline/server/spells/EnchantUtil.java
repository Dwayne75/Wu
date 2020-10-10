package com.wurmonline.server.spells;

import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.shared.constants.Enchants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

public class EnchantUtil
  implements Enchants
{
  protected static final Logger logger = Logger.getLogger(EnchantUtil.class.getName());
  public static ArrayList<ArrayList<Byte>> enchantGroups = new ArrayList();
  
  public static float getDemiseBonus(Item item, Creature defender)
  {
    if (item.enchantment != 0) {
      if (item.enchantment == 11)
      {
        if ((defender.isAnimal()) && (!defender.isUnique())) {
          return 0.03F;
        }
      }
      else if (item.enchantment == 9)
      {
        if ((defender.isPlayer()) || (defender.isHuman())) {
          return 0.03F;
        }
      }
      else if (item.enchantment == 10)
      {
        if ((defender.isMonster()) && (!defender.isUnique())) {
          return 0.03F;
        }
      }
      else if (item.enchantment == 12) {
        if (defender.isUnique()) {
          return 0.03F;
        }
      }
    }
    return 0.0F;
  }
  
  public static float getJewelryDamageIncrease(Creature attacker, byte woundType)
  {
    byte jewelryEnchant = 0;
    switch (woundType)
    {
    case 4: 
      jewelryEnchant = 2;
      break;
    case 5: 
      jewelryEnchant = 1;
      break;
    case 8: 
      jewelryEnchant = 3;
      break;
    case 10: 
      jewelryEnchant = 4;
    }
    if (jewelryEnchant == 0) {
      return 1.0F;
    }
    Item[] bodyItems = attacker.getBody().getContainersAndWornItems();
    float damageMultiplier = 1.0F;
    float totalPower = 0.0F;
    int activeJewelry = 0;
    for (Item bodyItem : bodyItems) {
      if ((bodyItem.isEnchantableJewelry()) && (bodyItem.getBonusForSpellEffect(jewelryEnchant) > 0.0F))
      {
        activeJewelry++;
        
        totalPower += (bodyItem.getCurrentQualityLevel() + bodyItem.getBonusForSpellEffect(jewelryEnchant)) / 2.0F;
      }
    }
    if (totalPower > 0.0F)
    {
      float increase = 0.025F * activeJewelry + 0.025F * (totalPower / 100.0F);
      
      increase *= 2.0F / (activeJewelry + 1);
      damageMultiplier *= (1.0F + increase);
    }
    return damageMultiplier;
  }
  
  public static float getJewelryResistModifier(Creature defender, byte woundType)
  {
    byte jewelryEnchant = 0;
    switch (woundType)
    {
    case 4: 
      jewelryEnchant = 7;
      break;
    case 5: 
      jewelryEnchant = 8;
      break;
    case 8: 
      jewelryEnchant = 6;
      break;
    case 10: 
      jewelryEnchant = 5;
    }
    if (jewelryEnchant == 0) {
      return 1.0F;
    }
    Item[] bodyItems = defender.getBody().getContainersAndWornItems();
    float damageMultiplier = 1.0F;
    float totalPower = 0.0F;
    int activeJewelry = 0;
    for (Item bodyItem : bodyItems) {
      if ((bodyItem.isEnchantableJewelry()) && (bodyItem.getBonusForSpellEffect(jewelryEnchant) > 0.0F))
      {
        activeJewelry++;
        
        totalPower += (bodyItem.getCurrentQualityLevel() + bodyItem.getBonusForSpellEffect(jewelryEnchant)) / 2.0F;
      }
    }
    if (totalPower > 0.0F)
    {
      float reduction = 0.025F * activeJewelry + 0.05F * (totalPower / 100.0F);
      
      reduction *= 2.0F / (activeJewelry + 1);
      damageMultiplier *= (1.0F - reduction);
    }
    return damageMultiplier;
  }
  
  public static SpellEffect hasNegatingEffect(Item target, byte enchantment)
  {
    if (target.getSpellEffects() != null) {
      for (ArrayList<Byte> group : enchantGroups) {
        if (group.contains(Byte.valueOf(enchantment))) {
          for (localIterator2 = group.iterator(); localIterator2.hasNext();)
          {
            byte ench = ((Byte)localIterator2.next()).byteValue();
            if (ench != enchantment) {
              if (target.getBonusForSpellEffect(ench) > 0.0F) {
                return target.getSpellEffect(ench);
              }
            }
          }
        }
      }
    }
    Iterator localIterator2;
    return null;
  }
  
  public static boolean canEnchantDemise(Creature performer, Item target)
  {
    if (!Spell.mayBeEnchanted(target))
    {
      performer.getCommunicator().sendNormalServerMessage("The spell will not work on that.", (byte)3);
      
      return false;
    }
    if (target.enchantment != 0)
    {
      performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already enchanted.", (byte)3);
      
      return false;
    }
    if (target.getCurrentQualityLevel() < 70.0F)
    {
      performer.getCommunicator().sendNormalServerMessage("The " + target
        .getName() + " is of too low quality for this enchantment.", (byte)3);
      return false;
    }
    return true;
  }
  
  public static void sendInvalidTargetMessage(Creature performer, Spell spell)
  {
    StringBuilder str = new StringBuilder();
    str.append("You can only target ");
    
    ArrayList<String> targets = new ArrayList();
    if (spell.isTargetArmour()) {
      targets.add("armour");
    }
    if (spell.isTargetWeapon()) {
      targets.add("weapons");
    }
    if (spell.isTargetJewelry()) {
      targets.add("jewelry");
    }
    if (spell.isTargetPendulum()) {
      targets.add("pendulums");
    }
    if (targets.size() == 0)
    {
      logger.warning("Spell " + spell.getName() + " has no valid targets.");
    }
    else if (targets.size() == 1)
    {
      str.append((String)targets.get(0));
    }
    else if (targets.size() == 2)
    {
      str.append((String)targets.get(0)).append(" or ").append((String)targets.get(1));
    }
    else
    {
      StringBuilder allTargets = new StringBuilder();
      for (String target : targets)
      {
        if (allTargets.length() > 0) {
          allTargets.append(", ");
        }
        allTargets.append(target);
      }
      str.append(allTargets);
    }
    str.append(".");
    performer.getCommunicator().sendNormalServerMessage(str.toString());
  }
  
  public static void sendCannotBeEnchantedMessage(Creature performer)
  {
    performer.getCommunicator().sendNormalServerMessage("The spell will not work on that.", (byte)3);
  }
  
  public static void sendNegatingEffectMessage(String name, Creature performer, Item target, SpellEffect negatingEffect)
  {
    performer.getCommunicator().sendNormalServerMessage(String.format("The %s is already enchanted with %s, which would negate the effect of %s.", new Object[] {target
    
      .getName(), negatingEffect.getName(), name }), (byte)3);
  }
  
  public static void initializeEnchantGroups()
  {
    ArrayList<Byte> speedEffectGroup = new ArrayList();
    speedEffectGroup.add(Byte.valueOf((byte)47));
    speedEffectGroup.add(Byte.valueOf((byte)16));
    speedEffectGroup.add(Byte.valueOf((byte)32));
    enchantGroups.add(speedEffectGroup);
    
    ArrayList<Byte> skillgainEffectGroup = new ArrayList();
    skillgainEffectGroup.add(Byte.valueOf((byte)47));
    skillgainEffectGroup.add(Byte.valueOf((byte)13));
    enchantGroups.add(skillgainEffectGroup);
    
    ArrayList<Byte> weaponDamageEffectGroup = new ArrayList();
    weaponDamageEffectGroup.add(Byte.valueOf((byte)45));
    weaponDamageEffectGroup.add(Byte.valueOf((byte)63));
    weaponDamageEffectGroup.add(Byte.valueOf((byte)14));
    weaponDamageEffectGroup.add(Byte.valueOf((byte)33));
    weaponDamageEffectGroup.add(Byte.valueOf((byte)26));
    weaponDamageEffectGroup.add(Byte.valueOf((byte)18));
    weaponDamageEffectGroup.add(Byte.valueOf((byte)27));
    enchantGroups.add(weaponDamageEffectGroup);
    
    ArrayList<Byte> armourEffectGroup = new ArrayList();
    armourEffectGroup.add(Byte.valueOf((byte)17));
    armourEffectGroup.add(Byte.valueOf((byte)46));
    enchantGroups.add(armourEffectGroup);
    
    ArrayList<Byte> mailboxEffectGroup = new ArrayList();
    mailboxEffectGroup.add(Byte.valueOf((byte)20));
    mailboxEffectGroup.add(Byte.valueOf((byte)44));
    enchantGroups.add(mailboxEffectGroup);
    
    ArrayList<Byte> jewelryEffectGroup = new ArrayList();
    jewelryEffectGroup.add(Byte.valueOf((byte)29));
    jewelryEffectGroup.add(Byte.valueOf((byte)1));
    jewelryEffectGroup.add(Byte.valueOf((byte)5));
    jewelryEffectGroup.add(Byte.valueOf((byte)4));
    jewelryEffectGroup.add(Byte.valueOf((byte)8));
    jewelryEffectGroup.add(Byte.valueOf((byte)2));
    jewelryEffectGroup.add(Byte.valueOf((byte)6));
    jewelryEffectGroup.add(Byte.valueOf((byte)3));
    jewelryEffectGroup.add(Byte.valueOf((byte)7));
    enchantGroups.add(jewelryEffectGroup);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\EnchantUtil.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */