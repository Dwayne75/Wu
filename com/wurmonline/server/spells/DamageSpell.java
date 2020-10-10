package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;

public class DamageSpell
  extends ReligiousSpell
{
  DamageSpell(String aName, int aNum, int aCastingTime, int aCost, int aDifficulty, int aLevel, long aCooldown)
  {
    super(aName, aNum, aCastingTime, aCost, aDifficulty, aLevel, aCooldown);
  }
  
  public double calculateDamage(Creature target, double power, double baseDamage, double damagePerPower)
  {
    double damage = power * damagePerPower;
    damage += baseDamage;
    double resistance = SpellResist.getSpellResistance(target, getNumber());
    damage *= resistance;
    
    SpellResist.addSpellResistance(target, getNumber(), damage);
    
    return Spell.modifyDamage(target, damage);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\DamageSpell.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */