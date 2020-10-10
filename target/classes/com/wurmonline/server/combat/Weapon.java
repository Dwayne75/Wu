package com.wurmonline.server.combat;

import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class Weapon
  implements MiscConstants
{
  private final int itemid;
  private final float damage;
  private final float speed;
  private final float critchance;
  private final int reach;
  private final int weightGroup;
  private final float parryPercent;
  private final double skillPenalty;
  private static float randomizer = 0.0F;
  private static final Map<Integer, Weapon> weapons = new HashMap();
  private static Weapon toCheck = null;
  private boolean damagedByMetal = false;
  private static final float critChanceMod = 5.0F;
  private static final float strengthModifier = Servers.localServer.isChallengeOrEpicServer() ? 1000.0F : 300.0F;
  
  public Weapon(int _itemid, float _damage, float _speed, float _critchance, int _reach, int _weightGroup, float _parryPercent, double _skillPenalty)
  {
    this.itemid = _itemid;
    this.damage = _damage;
    this.speed = _speed;
    this.critchance = (_critchance / 5.0F);
    this.reach = _reach;
    this.weightGroup = _weightGroup;
    this.parryPercent = _parryPercent;
    this.skillPenalty = _skillPenalty;
    weapons.put(Integer.valueOf(this.itemid), this);
  }
  
  public static final float getBaseDamageForWeapon(Item weapon)
  {
    if (weapon == null) {
      return 0.0F;
    }
    toCheck = (Weapon)weapons.get(Integer.valueOf(weapon.getTemplateId()));
    if (toCheck != null) {
      return toCheck.damage;
    }
    return 0.0F;
  }
  
  public static final double getModifiedDamageForWeapon(Item weapon, Skill strength)
  {
    return getModifiedDamageForWeapon(weapon, strength, false);
  }
  
  public static final double getModifiedDamageForWeapon(Item weapon, Skill strength, boolean fullDam)
  {
    if (fullDam) {
      randomizer = 1.0F;
    } else {
      randomizer = (50.0F + Server.rand.nextFloat() * 50.0F) / 100.0F;
    }
    double damreturn = 1.0D;
    if (weapon.isBodyPartAttached()) {
      damreturn = getBaseDamageForWeapon(weapon);
    } else {
      damreturn = getBaseDamageForWeapon(weapon) * weapon.getCurrentQualityLevel() / 100.0F;
    }
    damreturn *= (1.0D + strength.getKnowledge(0.0D) / strengthModifier);
    damreturn *= randomizer;
    return damreturn;
  }
  
  public static final float getBaseSpeedForWeapon(Item weapon)
  {
    if ((weapon == null) || (weapon.isBodyPartAttached())) {
      return 1.0F;
    }
    float materialMod = 1.0F;
    if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
      switch (weapon.getMaterial())
      {
      case 57: 
        materialMod = 0.9F;
        break;
      case 7: 
        materialMod = 1.05F;
        break;
      case 67: 
        materialMod = 0.95F;
        break;
      case 34: 
        materialMod = 0.96F;
        break;
      case 13: 
        materialMod = 0.95F;
        break;
      case 96: 
        materialMod = 1.025F;
      }
    }
    toCheck = (Weapon)weapons.get(Integer.valueOf(weapon.getTemplateId()));
    if (toCheck != null) {
      return toCheck.speed * materialMod;
    }
    return 20.0F * materialMod;
  }
  
  public static final float getRarityCritMod(byte rarity)
  {
    switch (rarity)
    {
    case 0: 
      return 1.0F;
    case 1: 
      return 1.1F;
    case 2: 
      return 1.3F;
    case 3: 
      return 1.5F;
    }
    return 1.0F;
  }
  
  public static final float getCritChanceForWeapon(Item weapon)
  {
    if ((weapon == null) || (weapon.isBodyPartAttached())) {
      return 0.01F;
    }
    toCheck = (Weapon)weapons.get(Integer.valueOf(weapon.getTemplateId()));
    if (toCheck != null) {
      return toCheck.critchance * getRarityCritMod(weapon.getRarity());
    }
    return 0.0F;
  }
  
  public static final int getReachForWeapon(Item weapon)
  {
    if ((weapon == null) || (weapon.isBodyPartAttached())) {
      return 1;
    }
    toCheck = (Weapon)weapons.get(Integer.valueOf(weapon.getTemplateId()));
    if (toCheck != null) {
      return toCheck.reach;
    }
    return 1;
  }
  
  public static final int getWeightGroupForWeapon(Item weapon)
  {
    if ((weapon == null) || (weapon.isBodyPartAttached())) {
      return 1;
    }
    toCheck = (Weapon)weapons.get(Integer.valueOf(weapon.getTemplateId()));
    if (toCheck != null) {
      return toCheck.weightGroup;
    }
    return 10;
  }
  
  public static final double getSkillPenaltyForWeapon(Item weapon)
  {
    if ((weapon == null) || (weapon.isBodyPartAttached())) {
      return 0.0D;
    }
    toCheck = (Weapon)weapons.get(Integer.valueOf(weapon.getTemplateId()));
    if (toCheck != null) {
      return toCheck.skillPenalty;
    }
    return 7.0D;
  }
  
  public static final float getWeaponParryPercent(Item weapon)
  {
    if (weapon == null) {
      return 0.0F;
    }
    if (weapon.isBodyPart()) {
      return 0.0F;
    }
    toCheck = (Weapon)weapons.get(Integer.valueOf(weapon.getTemplateId()));
    if (toCheck != null) {
      return toCheck.parryPercent;
    }
    return 0.0F;
  }
  
  void setDamagedByMetal(boolean aDamagedByMetal)
  {
    this.damagedByMetal = aDamagedByMetal;
  }
  
  public static final boolean isWeaponDamByMetal(Item weapon)
  {
    if (weapon == null) {
      return false;
    }
    if ((weapon.isBodyPart()) && (weapon.isBodyPartRemoved())) {
      return true;
    }
    toCheck = (Weapon)weapons.get(Integer.valueOf(weapon.getTemplateId()));
    if (toCheck != null) {
      return toCheck.damagedByMetal;
    }
    return false;
  }
  
  public static double getMaterialDamageBonus(byte material)
  {
    if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
      switch (material)
      {
      case 56: 
        return 1.100000023841858D;
      case 30: 
        return 0.9900000095367432D;
      case 31: 
        return 0.9850000143051147D;
      case 10: 
        return 0.6499999761581421D;
      case 7: 
        return 0.9750000238418579D;
      case 12: 
        return 0.5D;
      case 67: 
        return 1.0499999523162842D;
      case 34: 
        return 0.925000011920929D;
      case 13: 
        return 0.8999999761581421D;
      }
    } else if (material == 56) {
      return 1.100000023841858D;
    }
    return 1.0D;
  }
  
  public static double getMaterialHunterDamageBonus(byte material)
  {
    if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
      switch (material)
      {
      case 8: 
        return 1.100000023841858D;
      case 96: 
        return 1.0499999523162842D;
      }
    }
    return 1.0D;
  }
  
  public static double getMaterialArmourDamageBonus(byte material)
  {
    if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
      switch (material)
      {
      case 30: 
        return 1.0499999523162842D;
      case 31: 
        return 1.0750000476837158D;
      case 7: 
        return 1.0499999523162842D;
      case 9: 
        return 1.024999976158142D;
      }
    }
    return 1.0D;
  }
  
  public static float getMaterialParryBonus(byte material)
  {
    if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
      switch (material)
      {
      case 8: 
        return 1.025F;
      case 34: 
        return 1.05F;
      }
    }
    return 1.0F;
  }
  
  public static float getMaterialExtraWoundMod(byte material)
  {
    if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
      switch (material)
      {
      case 10: 
        return 0.3F;
      case 12: 
        return 0.75F;
      }
    }
    return 0.0F;
  }
  
  public static byte getMaterialExtraWoundType(byte material)
  {
    if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
      switch (material)
      {
      case 10: 
        return 5;
      case 12: 
        return 5;
      }
    }
    return 5;
  }
  
  public static double getMaterialBashModifier(byte material)
  {
    if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
      switch (material)
      {
      case 56: 
        return 1.0750000476837158D;
      case 30: 
        return 1.0499999523162842D;
      case 31: 
        return 1.024999976158142D;
      case 10: 
        return 0.8999999761581421D;
      case 57: 
        return 1.100000023841858D;
      case 7: 
        return 1.100000023841858D;
      case 12: 
        return 1.2000000476837158D;
      case 67: 
        return 1.0750000476837158D;
      case 8: 
        return 1.100000023841858D;
      case 9: 
        return 1.0499999523162842D;
      case 34: 
        return 0.8999999761581421D;
      case 13: 
        return 0.8500000238418579D;
      case 96: 
        return 1.100000023841858D;
      }
    }
    return 1.0D;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\combat\Weapon.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */