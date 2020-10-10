package com.wurmonline.server.spells;

import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Logger;

public final class SpellGenerator
  implements SpellTypes
{
  private static final Logger logger = Logger.getLogger(SpellGenerator.class.getName());
  
  public static void createSpells()
  {
    logger.info("Starting to create spells");
    long start = System.nanoTime();
    
    EnchantUtil.initializeEnchantGroups();
    
    Deity fo = Deities.getDeity(1);
    Deity magranon = Deities.getDeity(2);
    Deity libila = Deities.getDeity(4);
    Deity vynora = Deities.getDeity(3);
    
    Spell[] SPELLS_COMMON = { Spells.SPELL_BLESS, Spells.SPELL_BREAK_ALTAR, Spells.SPELL_DISPEL, Spells.SPELL_LOCATE_ARTIFACT, Spells.SPELL_LOCATE_SOUL, Spells.SPELL_NOLOCATE, Spells.SPELL_PURGE, Spells.SPELL_TANGLEWEAVE, Spells.SPELL_VESSEL };
    
    Deity[] arrayOfDeity1 = Deities.getDeities();int i = arrayOfDeity1.length;
    Deity deity;
    Spell localSpell1;
    Spell spell;
    for (Deity localDeity1 = 0; localDeity1 < i; localDeity1++)
    {
      deity = arrayOfDeity1[localDeity1];
      
      Spell[] arrayOfSpell1 = SPELLS_COMMON;int j = arrayOfSpell1.length;
      for (localSpell1 = 0; localSpell1 < j; localSpell1++)
      {
        spell = arrayOfSpell1[localSpell1];
        
        deity.addSpell(spell);
      }
    }
    ignoreSpells.addAll(Arrays.asList(SPELLS_COMMON));
    
    Spell[] SPELLS_COMMON_PVE = { Spells.SPELL_SUMMON_SOUL };
    if ((!Servers.isThisAPvpServer()) || (Servers.isThisATestServer()))
    {
      Deity[] arrayOfDeity2 = Deities.getDeities();localDeity1 = arrayOfDeity2.length;
      for (deity = 0; deity < localDeity1; deity++)
      {
        Deity deity = arrayOfDeity2[deity];
        
        Spell[] arrayOfSpell2 = SPELLS_COMMON_PVE;localSpell1 = arrayOfSpell2.length;
        for (spell = 0; spell < localSpell1; spell++)
        {
          Spell spell = arrayOfSpell2[spell];
          
          deity.addSpell(spell);
        }
      }
    }
    ignoreSpells.addAll(Arrays.asList(SPELLS_COMMON_PVE));
    
    fo.addSpell(Spells.SPELL_BEARPAWS);
    fo.addSpell(Spells.SPELL_CHARM_ANIMAL);
    fo.addSpell(Spells.SPELL_CLEANSE);
    fo.addSpell(Spells.SPELL_COURIER);
    fo.addSpell(Spells.SPELL_CURE_LIGHT);
    fo.addSpell(Spells.SPELL_CURE_MEDIUM);
    fo.addSpell(Spells.SPELL_CURE_SERIOUS);
    fo.addSpell(Spells.SPELL_DIRT);
    fo.addSpell(Spells.SPELL_FOREST_GIANT_STRENGTH);
    fo.addSpell(Spells.SPELL_GENESIS);
    fo.addSpell(Spells.SPELL_HEAL);
    fo.addSpell(Spells.SPELL_HOLY_CROP);
    fo.addSpell(Spells.SPELL_HUMID_DRIZZLE);
    fo.addSpell(Spells.SPELL_LIFE_TRANSFER);
    fo.addSpell(Spells.SPELL_LIGHT_OF_FO);
    fo.addSpell(Spells.SPELL_LURKER_IN_THE_WOODS);
    fo.addSpell(Spells.SPELL_MORNING_FOG);
    fo.addSpell(Spells.SPELL_OAKSHELL);
    fo.addSpell(Spells.SPELL_PROTECT_ACID);
    fo.addSpell(Spells.SPELL_REFRESH);
    fo.addSpell(Spells.SPELL_SIXTH_SENSE);
    fo.addSpell(Spells.SPELL_TOXIN);
    fo.addSpell(Spells.SPELL_VENOM);
    fo.addSpell(Spells.SPELL_WARD);
    fo.addSpell(Spells.SPELL_WILD_GROWTH);
    fo.addSpell(Spells.SPELL_WILLOWSPINE);
    
    magranon.addSpell(Spells.SPELL_BLAZE);
    magranon.addSpell(Spells.SPELL_DEMISE_ANIMAL);
    magranon.addSpell(Spells.SPELL_DEMISE_LEGENDARY);
    magranon.addSpell(Spells.SPELL_DEMISE_MONSTER);
    magranon.addSpell(Spells.SPELL_DISINTEGRATE);
    magranon.addSpell(Spells.SPELL_DOMINATE);
    magranon.addSpell(Spells.SPELL_FIREHEART);
    magranon.addSpell(Spells.SPELL_FIRE_PILLAR);
    magranon.addSpell(Spells.SPELL_FLAMING_AURA);
    magranon.addSpell(Spells.SPELL_FOCUSED_WILL);
    magranon.addSpell(Spells.SPELL_FRANTIC_CHARGE);
    magranon.addSpell(Spells.SPELL_GOAT_SHAPE);
    magranon.addSpell(Spells.SPELL_INFERNO);
    magranon.addSpell(Spells.SPELL_LIGHT_TOKEN);
    magranon.addSpell(Spells.SPELL_LURKER_IN_THE_DARK);
    magranon.addSpell(Spells.SPELL_LURKER_IN_THE_WOODS);
    magranon.addSpell(Spells.SPELL_MASS_STAMINA);
    magranon.addSpell(Spells.SPELL_MOLE_SENSES);
    magranon.addSpell(Spells.SPELL_PROTECT_FROST);
    magranon.addSpell(Spells.SPELL_RITUAL_OF_THE_SUN);
    magranon.addSpell(Spells.SPELL_SIXTH_SENSE);
    magranon.addSpell(Spells.SPELL_SMITE);
    magranon.addSpell(Spells.SPELL_STRONGWALL);
    magranon.addSpell(Spells.SPELL_SUNDER);
    magranon.addSpell(Spells.SPELL_WEB_ARMOUR);
    magranon.addSpell(Spells.SPELL_WRATH_OF_MAGRANON);
    
    libila.addSpell(Spells.SPELL_AURA_SHARED_PAIN);
    libila.addSpell(Spells.SPELL_BLESSINGS_OF_THE_DARK);
    libila.addSpell(Spells.SPELL_BLOODTHIRST);
    libila.addSpell(Spells.SPELL_CORROSION);
    libila.addSpell(Spells.SPELL_CORRUPT);
    libila.addSpell(Spells.SPELL_DARK_MESSENGER);
    libila.addSpell(Spells.SPELL_DEMISE_ANIMAL);
    libila.addSpell(Spells.SPELL_DEMISE_HUMAN);
    libila.addSpell(Spells.SPELL_DEMISE_LEGENDARY);
    libila.addSpell(Spells.SPELL_DEMISE_MONSTER);
    libila.addSpell(Spells.SPELL_DRAIN_HEALTH);
    libila.addSpell(Spells.SPELL_DRAIN_STAMINA);
    libila.addSpell(Spells.SPELL_ESSENCE_DRAIN);
    libila.addSpell(Spells.SPELL_FUNGUS_TRAP);
    libila.addSpell(Spells.SPELL_HELL_STRENGTH);
    libila.addSpell(Spells.SPELL_LAND_OF_THE_DEAD);
    libila.addSpell(Spells.SPELL_LURKER_IN_THE_DARK);
    libila.addSpell(Spells.SPELL_PAIN_RAIN);
    libila.addSpell(Spells.SPELL_PHANTASMS);
    libila.addSpell(Spells.SPELL_PROTECT_POISON);
    libila.addSpell(Spells.SPELL_REBIRTH);
    libila.addSpell(Spells.SPELL_RITE_OF_DEATH);
    libila.addSpell(Spells.SPELL_ROTTING_GUT);
    libila.addSpell(Spells.SPELL_ROTTING_TOUCH);
    libila.addSpell(Spells.SPELL_SCORN_OF_LIBILA);
    libila.addSpell(Spells.SPELL_TRUEHIT);
    libila.addSpell(Spells.SPELL_WEAKNESS);
    libila.addSpell(Spells.SPELL_WEB_ARMOUR);
    libila.addSpell(Spells.SPELL_WORM_BRAINS);
    libila.addSpell(Spells.SPELL_ZOMBIE_INFESTATION);
    if (Servers.localServer.PVPSERVER) {
      libila.addSpell(Spells.SPELL_STRONGWALL);
    }
    vynora.addSpell(Spells.SPELL_AURA_SHARED_PAIN);
    vynora.addSpell(Spells.SPELL_CIRCLE_OF_CUNNING);
    vynora.addSpell(Spells.SPELL_DEMISE_ANIMAL);
    vynora.addSpell(Spells.SPELL_DEMISE_HUMAN);
    vynora.addSpell(Spells.SPELL_DEMISE_LEGENDARY);
    vynora.addSpell(Spells.SPELL_DEMISE_MONSTER);
    vynora.addSpell(Spells.SPELL_EXCEL);
    vynora.addSpell(Spells.SPELL_FROSTBRAND);
    vynora.addSpell(Spells.SPELL_GLACIAL);
    vynora.addSpell(Spells.SPELL_HYPOTHERMIA);
    vynora.addSpell(Spells.SPELL_ICE_PILLAR);
    vynora.addSpell(Spells.SPELL_LURKER_IN_THE_DEEP);
    vynora.addSpell(Spells.SPELL_MEND);
    vynora.addSpell(Spells.SPELL_MIND_STEALER);
    vynora.addSpell(Spells.SPELL_NIMBLENESS);
    vynora.addSpell(Spells.SPELL_OPULENCE);
    vynora.addSpell(Spells.SPELL_PROTECT_FIRE);
    vynora.addSpell(Spells.SPELL_REVEAL_CREATURES);
    vynora.addSpell(Spells.SPELL_REVEAL_SETTLEMENTS);
    vynora.addSpell(Spells.SPELL_RITE_OF_SPRING);
    vynora.addSpell(Spells.SPELL_SHARD_OF_ICE);
    vynora.addSpell(Spells.SPELL_SIXTH_SENSE);
    vynora.addSpell(Spells.SPELL_TENTACLES);
    vynora.addSpell(Spells.SPELL_TORNADO);
    vynora.addSpell(Spells.SPELL_WIND_OF_AGES);
    vynora.addSpell(Spells.SPELL_WISDOM_OF_VYNORA);
    
    generateDemigodSpells();
    
    long end = System.nanoTime();
    logger.info("Generating spells took " + (float)(end - start) / 1000000.0F + " ms");
  }
  
  protected static HashSet<Spell> ignoreSpells = new HashSet();
  
  private static class SpellGroup
  {
    protected Deity deity;
    protected ArrayList<Integer> spells = new ArrayList();
    
    SpellGroup(Deity deity)
    {
      this.deity = deity;
    }
    
    public void addSpellChance(Spell spell, int amount)
    {
      for (int i = 0; i < amount; i++) {
        this.spells.add(Integer.valueOf(spell.getNumber()));
      }
    }
    
    public void addEmptyChance(int amount)
    {
      for (int i = 0; i < amount; i++) {
        this.spells.add(Integer.valueOf(-10));
      }
    }
    
    public int getRandomSpell(Random rand)
    {
      if (this.spells.size() > 0)
      {
        int index = rand.nextInt(this.spells.size());
        
        return ((Integer)this.spells.get(index)).intValue();
      }
      SpellGenerator.logger.warning(String.format("Attempting to obtain a random spell for deity %s failed because no spells available.", new Object[] {this.deity
        .getName() }));
      return -10;
    }
  }
  
  private static int rollTamingGroup(Deity deity, Random rand)
  {
    SpellGroup taming = new SpellGroup(deity);
    if (deity.isBefriendCreature()) {
      taming.addSpellChance(Spells.SPELL_CHARM_ANIMAL, 3);
    } else {
      taming.addSpellChance(Spells.SPELL_CHARM_ANIMAL, 2);
    }
    if (deity.isHateGod()) {
      taming.addSpellChance(Spells.SPELL_REBIRTH, 2);
    }
    if (deity.isWarrior()) {
      taming.addSpellChance(Spells.SPELL_DOMINATE, 3);
    } else {
      taming.addSpellChance(Spells.SPELL_DOMINATE, 2);
    }
    taming.addEmptyChance(2);
    
    return taming.getRandomSpell(rand);
  }
  
  private static int rollAreaHealingGroup(Deity deity, Random rand)
  {
    SpellGroup areaHealing = new SpellGroup(deity);
    if (!deity.isHateGod()) {
      if (deity.isWarrior()) {
        areaHealing.addSpellChance(Spells.SPELL_LIGHT_OF_FO, 1);
      } else if (deity.isHealer()) {
        areaHealing.addSpellChance(Spells.SPELL_LIGHT_OF_FO, 4);
      } else {
        areaHealing.addSpellChance(Spells.SPELL_LIGHT_OF_FO, 3);
      }
    }
    if (deity.isHateGod()) {
      if (deity.isWarrior()) {
        areaHealing.addSpellChance(Spells.SPELL_SCORN_OF_LIBILA, 1);
      } else if (deity.isHealer()) {
        areaHealing.addSpellChance(Spells.SPELL_SCORN_OF_LIBILA, 4);
      } else {
        areaHealing.addSpellChance(Spells.SPELL_SCORN_OF_LIBILA, 3);
      }
    }
    if (deity.isMountainGod()) {
      areaHealing.addSpellChance(Spells.SPELL_MASS_STAMINA, 3);
    } else {
      areaHealing.addSpellChance(Spells.SPELL_MASS_STAMINA, 1);
    }
    areaHealing.addEmptyChance(2);
    
    return areaHealing.getRandomSpell(rand);
  }
  
  private static int rollAreaCombatGroup(Deity deity, Random rand)
  {
    SpellGroup areaCombat = new SpellGroup(deity);
    if ((deity.hasSpell(Spells.SPELL_LIGHT_OF_FO)) || (deity.hasSpell(Spells.SPELL_SCORN_OF_LIBILA)))
    {
      if (deity.isWarrior()) {
        return -10;
      }
      areaCombat.addSpellChance(Spells.SPELL_FIRE_PILLAR, 1);
      areaCombat.addSpellChance(Spells.SPELL_ICE_PILLAR, 1);
      if (deity.isHateGod())
      {
        areaCombat.addSpellChance(Spells.SPELL_FUNGUS_TRAP, 1);
        areaCombat.addSpellChance(Spells.SPELL_PAIN_RAIN, 1);
      }
    }
    else
    {
      if (deity.isWarrior()) {
        areaCombat.addSpellChance(Spells.SPELL_FIRE_PILLAR, 4);
      } else {
        areaCombat.addSpellChance(Spells.SPELL_FIRE_PILLAR, 2);
      }
      if (deity.isWarrior()) {
        areaCombat.addSpellChance(Spells.SPELL_ICE_PILLAR, 4);
      } else {
        areaCombat.addSpellChance(Spells.SPELL_ICE_PILLAR, 2);
      }
      if (deity.isHateGod())
      {
        if (deity.isWarrior()) {
          areaCombat.addSpellChance(Spells.SPELL_FUNGUS_TRAP, 3);
        } else {
          areaCombat.addSpellChance(Spells.SPELL_FUNGUS_TRAP, 2);
        }
        if (deity.isWarrior()) {
          areaCombat.addSpellChance(Spells.SPELL_PAIN_RAIN, 3);
        } else {
          areaCombat.addSpellChance(Spells.SPELL_PAIN_RAIN, 2);
        }
      }
    }
    areaCombat.addEmptyChance(2);
    
    return areaCombat.getRandomSpell(rand);
  }
  
  private static int rollWeaponEnchantGroup(Deity deity, Random rand)
  {
    SpellGroup weaponEnchant = new SpellGroup(deity);
    if (deity.isForestGod()) {
      weaponEnchant.addSpellChance(Spells.SPELL_VENOM, 4);
    } else {
      weaponEnchant.addSpellChance(Spells.SPELL_VENOM, 1);
    }
    if (deity.isHateGod()) {
      weaponEnchant.addSpellChance(Spells.SPELL_BLOODTHIRST, 4);
    } else {
      weaponEnchant.addSpellChance(Spells.SPELL_BLOODTHIRST, 2);
    }
    weaponEnchant.addSpellChance(Spells.SPELL_ROTTING_TOUCH, 2);
    weaponEnchant.addSpellChance(Spells.SPELL_FROSTBRAND, 2);
    weaponEnchant.addSpellChance(Spells.SPELL_FLAMING_AURA, 2);
    
    return weaponEnchant.getRandomSpell(rand);
  }
  
  private static int rollWeaponAugmentGroup(Deity deity, Random rand)
  {
    SpellGroup weaponAugment = new SpellGroup(deity);
    if (deity.isHealer()) {
      weaponAugment.addSpellChance(Spells.SPELL_LIFE_TRANSFER, 4);
    } else if (deity.isForestGod()) {
      weaponAugment.addSpellChance(Spells.SPELL_LIFE_TRANSFER, 3);
    } else {
      weaponAugment.addSpellChance(Spells.SPELL_LIFE_TRANSFER, 2);
    }
    if (deity.isHateGod()) {
      weaponAugment.addSpellChance(Spells.SPELL_ESSENCE_DRAIN, 4);
    } else {
      weaponAugment.addSpellChance(Spells.SPELL_ESSENCE_DRAIN, 2);
    }
    if (deity.isWaterGod()) {
      weaponAugment.addSpellChance(Spells.SPELL_NIMBLENESS, 5);
    } else {
      weaponAugment.addSpellChance(Spells.SPELL_NIMBLENESS, 2);
    }
    weaponAugment.addEmptyChance(1);
    
    return weaponAugment.getRandomSpell(rand);
  }
  
  private static int rollIndustrialEnchantGroup(Deity deity, Random rand)
  {
    SpellGroup industrialEnchant = new SpellGroup(deity);
    if (deity.isLearner()) {
      industrialEnchant.addSpellChance(Spells.SPELL_CIRCLE_OF_CUNNING, 4);
    } else if (deity.isHateGod()) {
      industrialEnchant.addSpellChance(Spells.SPELL_CIRCLE_OF_CUNNING, 1);
    } else {
      industrialEnchant.addSpellChance(Spells.SPELL_CIRCLE_OF_CUNNING, 2);
    }
    if (deity.isHateGod()) {
      industrialEnchant.addSpellChance(Spells.SPELL_BLESSINGS_OF_THE_DARK, 4);
    }
    if ((deity.isLearner()) || (deity.isRoadProtector())) {
      industrialEnchant.addSpellChance(Spells.SPELL_BLESSINGS_OF_THE_DARK, 4);
    } else {
      industrialEnchant.addSpellChance(Spells.SPELL_BLESSINGS_OF_THE_DARK, 2);
    }
    if (deity.isRoadProtector()) {
      industrialEnchant.addSpellChance(Spells.SPELL_WIND_OF_AGES, 4);
    } else {
      industrialEnchant.addSpellChance(Spells.SPELL_WIND_OF_AGES, 2);
    }
    industrialEnchant.addEmptyChance(1);
    
    return industrialEnchant.getRandomSpell(rand);
  }
  
  private static int rollCreatureEnchantGroup(Deity deity, Random rand)
  {
    SpellGroup creatureEnchant = new SpellGroup(deity);
    if (deity.isForestGod()) {
      creatureEnchant.addSpellChance(Spells.SPELL_OAKSHELL, 1);
    }
    if (deity.isWarrior()) {
      creatureEnchant.addSpellChance(Spells.SPELL_OAKSHELL, 1);
    } else if (deity.isBefriendCreature()) {
      creatureEnchant.addSpellChance(Spells.SPELL_OAKSHELL, 4);
    } else {
      creatureEnchant.addSpellChance(Spells.SPELL_OAKSHELL, 2);
    }
    if (deity.isWaterGod()) {
      creatureEnchant.addSpellChance(Spells.SPELL_EXCEL, 1);
    }
    if (deity.hasSpell(Spells.SPELL_NIMBLENESS)) {
      creatureEnchant.addSpellChance(Spells.SPELL_EXCEL, 3);
    } else {
      creatureEnchant.addSpellChance(Spells.SPELL_EXCEL, 1);
    }
    if (deity.hasSpell(Spells.SPELL_BLESSINGS_OF_THE_DARK)) {
      creatureEnchant.addSpellChance(Spells.SPELL_TRUEHIT, 3);
    } else if (deity.isHateGod()) {
      creatureEnchant.addSpellChance(Spells.SPELL_TRUEHIT, 2);
    } else {
      creatureEnchant.addSpellChance(Spells.SPELL_TRUEHIT, 1);
    }
    creatureEnchant.addEmptyChance(1);
    
    return creatureEnchant.getRandomSpell(rand);
  }
  
  private static int rollJewelryOffenseGroup(Deity deity, Random rand)
  {
    SpellGroup jewelryOffense = new SpellGroup(deity);
    if (deity.isForestGod()) {
      jewelryOffense.addSpellChance(Spells.SPELL_TOXIN, 3);
    } else {
      jewelryOffense.addSpellChance(Spells.SPELL_TOXIN, 1);
    }
    if (deity.isMountainGod()) {
      jewelryOffense.addSpellChance(Spells.SPELL_BLAZE, 3);
    } else {
      jewelryOffense.addSpellChance(Spells.SPELL_BLAZE, 1);
    }
    if (deity.isWaterGod()) {
      jewelryOffense.addSpellChance(Spells.SPELL_GLACIAL, 3);
    } else {
      jewelryOffense.addSpellChance(Spells.SPELL_GLACIAL, 1);
    }
    if (deity.isHateGod()) {
      jewelryOffense.addSpellChance(Spells.SPELL_CORROSION, 3);
    } else {
      jewelryOffense.addSpellChance(Spells.SPELL_CORROSION, 1);
    }
    return jewelryOffense.getRandomSpell(rand);
  }
  
  private static int rollJewelryDefenseGroup(Deity deity, Random rand)
  {
    SpellGroup jewelryDefense = new SpellGroup(deity);
    
    jewelryDefense.addSpellChance(Spells.SPELL_PROTECT_ACID, 1);
    jewelryDefense.addSpellChance(Spells.SPELL_PROTECT_FIRE, 1);
    jewelryDefense.addSpellChance(Spells.SPELL_PROTECT_FROST, 1);
    jewelryDefense.addSpellChance(Spells.SPELL_PROTECT_POISON, 1);
    
    return jewelryDefense.getRandomSpell(rand);
  }
  
  private static int rollCombatDamageGroup(Deity deity, Random rand)
  {
    SpellGroup combatDamage = new SpellGroup(deity);
    if (deity.isMountainGod()) {
      combatDamage.addSpellChance(Spells.SPELL_FIREHEART, 3);
    } else {
      combatDamage.addSpellChance(Spells.SPELL_FIREHEART, 1);
    }
    if (deity.isHateGod()) {
      combatDamage.addSpellChance(Spells.SPELL_ROTTING_GUT, 3);
    } else {
      combatDamage.addSpellChance(Spells.SPELL_ROTTING_GUT, 1);
    }
    if (deity.isWaterGod()) {
      combatDamage.addSpellChance(Spells.SPELL_SHARD_OF_ICE, 3);
    } else {
      combatDamage.addSpellChance(Spells.SPELL_SHARD_OF_ICE, 1);
    }
    if ((!deity.isWarrior()) && ((deity.isForestGod()) || (deity.isWaterGod()))) {
      combatDamage.addEmptyChance(2);
    }
    return combatDamage.getRandomSpell(rand);
  }
  
  private static int rollMajorDamageGroup(Deity deity, Random rand)
  {
    SpellGroup majorDamage = new SpellGroup(deity);
    if (deity.isMountainGod()) {
      majorDamage.addSpellChance(Spells.SPELL_INFERNO, 3);
    } else {
      majorDamage.addSpellChance(Spells.SPELL_INFERNO, 1);
    }
    if (deity.isHateGod()) {
      majorDamage.addSpellChance(Spells.SPELL_WORM_BRAINS, 3);
    } else {
      majorDamage.addSpellChance(Spells.SPELL_WORM_BRAINS, 1);
    }
    if (deity.isWaterGod()) {
      majorDamage.addSpellChance(Spells.SPELL_HYPOTHERMIA, 3);
    } else {
      majorDamage.addSpellChance(Spells.SPELL_HYPOTHERMIA, 1);
    }
    if ((!deity.isWarrior()) && ((deity.isForestGod()) || (deity.isWaterGod()))) {
      majorDamage.addEmptyChance(2);
    }
    return majorDamage.getRandomSpell(rand);
  }
  
  private static int rollTileChangeGroup(Deity deity, Random rand)
  {
    SpellGroup tileChange = new SpellGroup(deity);
    if (deity.isHateGod())
    {
      tileChange.addSpellChance(Spells.SPELL_CORRUPT, 1);
    }
    else if (deity.isForestGod())
    {
      tileChange.addSpellChance(Spells.SPELL_CLEANSE, 1);
    }
    else
    {
      tileChange.addSpellChance(Spells.SPELL_CLEANSE, 1);
      tileChange.addEmptyChance(2);
    }
    return tileChange.getRandomSpell(rand);
  }
  
  private static int rollRiteGroup(Deity deity, Random rand)
  {
    SpellGroup rite = new SpellGroup(deity);
    if (deity.isHateGod()) {
      rite.addSpellChance(Spells.SPELL_RITE_OF_DEATH, 1);
    } else if (deity.isForestGod()) {
      rite.addSpellChance(Spells.SPELL_HOLY_CROP, 1);
    } else if (deity.isMountainGod()) {
      rite.addSpellChance(Spells.SPELL_RITUAL_OF_THE_SUN, 1);
    } else if (deity.isWaterGod()) {
      rite.addSpellChance(Spells.SPELL_RITE_OF_SPRING, 1);
    } else {
      logger.warning(String.format("Deity %s has no template deity.", new Object[] { deity.getName() }));
    }
    return rite.getRandomSpell(rand);
  }
  
  private static int rollDisintegrate(Deity deity, Random rand)
  {
    SpellGroup disintegrate = new SpellGroup(deity);
    if (deity.isWarrior()) {
      disintegrate.addSpellChance(Spells.SPELL_DISINTEGRATE, 4);
    } else if (deity.isMountainGod()) {
      disintegrate.addSpellChance(Spells.SPELL_DISINTEGRATE, 3);
    } else {
      disintegrate.addSpellChance(Spells.SPELL_DISINTEGRATE, 2);
    }
    disintegrate.addEmptyChance(3);
    
    return disintegrate.getRandomSpell(rand);
  }
  
  private static int rollStrongwall(Deity deity, Random rand)
  {
    SpellGroup strongwall = new SpellGroup(deity);
    if (deity.hasSpell(Spells.SPELL_DISINTEGRATE)) {
      return -10;
    }
    if (deity.isMountainGod()) {
      strongwall.addSpellChance(Spells.SPELL_STRONGWALL, 8);
    } else {
      strongwall.addSpellChance(Spells.SPELL_STRONGWALL, 2);
    }
    strongwall.addEmptyChance(2);
    
    return strongwall.getRandomSpell(rand);
  }
  
  private static void addDeitySpell(String group, Deity deity, int spellId)
  {
    if (spellId > 0)
    {
      deity.addSpell(Spells.getSpell(spellId));
      logger.info(String.format("%s obtains spell %s from %s group.", new Object[] {deity
        .getName(), Spells.getSpell(spellId).getName(), group }));
    }
    else
    {
      logger.info(String.format("%s obtains no spell from %s group.", new Object[] {deity
        .getName(), group }));
    }
  }
  
  private static void generateDemigodSpells()
  {
    ignoreSpells.add(Spells.SPELL_CHARM_ANIMAL);
    ignoreSpells.add(Spells.SPELL_REBIRTH);
    ignoreSpells.add(Spells.SPELL_DOMINATE);
    
    ignoreSpells.add(Spells.SPELL_LIGHT_OF_FO);
    ignoreSpells.add(Spells.SPELL_SCORN_OF_LIBILA);
    ignoreSpells.add(Spells.SPELL_MASS_STAMINA);
    
    ignoreSpells.add(Spells.SPELL_FIRE_PILLAR);
    ignoreSpells.add(Spells.SPELL_ICE_PILLAR);
    ignoreSpells.add(Spells.SPELL_FUNGUS_TRAP);
    ignoreSpells.add(Spells.SPELL_PAIN_RAIN);
    
    ignoreSpells.add(Spells.SPELL_BLOODTHIRST);
    ignoreSpells.add(Spells.SPELL_FLAMING_AURA);
    ignoreSpells.add(Spells.SPELL_FROSTBRAND);
    ignoreSpells.add(Spells.SPELL_ROTTING_TOUCH);
    ignoreSpells.add(Spells.SPELL_VENOM);
    
    ignoreSpells.add(Spells.SPELL_LIFE_TRANSFER);
    ignoreSpells.add(Spells.SPELL_NIMBLENESS);
    
    ignoreSpells.add(Spells.SPELL_BLESSINGS_OF_THE_DARK);
    ignoreSpells.add(Spells.SPELL_CIRCLE_OF_CUNNING);
    ignoreSpells.add(Spells.SPELL_WIND_OF_AGES);
    
    ignoreSpells.add(Spells.SPELL_EXCEL);
    ignoreSpells.add(Spells.SPELL_OAKSHELL);
    ignoreSpells.add(Spells.SPELL_TRUEHIT);
    
    ignoreSpells.add(Spells.SPELL_BLAZE);
    ignoreSpells.add(Spells.SPELL_CORROSION);
    ignoreSpells.add(Spells.SPELL_GLACIAL);
    ignoreSpells.add(Spells.SPELL_TOXIN);
    
    ignoreSpells.add(Spells.SPELL_PROTECT_ACID);
    ignoreSpells.add(Spells.SPELL_PROTECT_FIRE);
    ignoreSpells.add(Spells.SPELL_PROTECT_FROST);
    ignoreSpells.add(Spells.SPELL_PROTECT_POISON);
    
    ignoreSpells.add(Spells.SPELL_FIREHEART);
    ignoreSpells.add(Spells.SPELL_ROTTING_GUT);
    ignoreSpells.add(Spells.SPELL_SHARD_OF_ICE);
    
    ignoreSpells.add(Spells.SPELL_HYPOTHERMIA);
    ignoreSpells.add(Spells.SPELL_INFERNO);
    ignoreSpells.add(Spells.SPELL_WORM_BRAINS);
    
    ignoreSpells.add(Spells.SPELL_HOLY_CROP);
    ignoreSpells.add(Spells.SPELL_RITE_OF_DEATH);
    ignoreSpells.add(Spells.SPELL_RITE_OF_SPRING);
    ignoreSpells.add(Spells.SPELL_RITUAL_OF_THE_SUN);
    
    ignoreSpells.add(Spells.SPELL_DISINTEGRATE);
    ignoreSpells.add(Spells.SPELL_STRONGWALL);
    ignoreSpells.add(Spells.SPELL_CORRUPT);
    ignoreSpells.add(Spells.SPELL_CLEANSE);
    for (Deity deity : Deities.getDeities()) {
      if (deity.isCustomDeity())
      {
        Random rand = deity.initializeAndGetRand();
        
        addDeitySpell("Taming", deity, rollTamingGroup(deity, rand));
        
        addDeitySpell("Area Healing", deity, rollAreaHealingGroup(deity, rand));
        
        addDeitySpell("Area Combat", deity, rollAreaCombatGroup(deity, rand));
        
        addDeitySpell("Weapon Enchant", deity, rollWeaponEnchantGroup(deity, rand));
        
        addDeitySpell("Weapon Augment", deity, rollWeaponAugmentGroup(deity, rand));
        
        addDeitySpell("Industrial Enchant", deity, rollIndustrialEnchantGroup(deity, rand));
        
        addDeitySpell("Creature Enchant", deity, rollCreatureEnchantGroup(deity, rand));
        
        addDeitySpell("Jewelry Offense", deity, rollJewelryOffenseGroup(deity, rand));
        
        addDeitySpell("Jewelry Defense", deity, rollJewelryDefenseGroup(deity, rand));
        
        addDeitySpell("Combat Damage", deity, rollCombatDamageGroup(deity, rand));
        
        addDeitySpell("Major Damage", deity, rollMajorDamageGroup(deity, rand));
        
        addDeitySpell("Tile Change", deity, rollTileChangeGroup(deity, rand));
        
        addDeitySpell("Rite", deity, rollRiteGroup(deity, rand));
        
        addDeitySpell("Disintegrate", deity, rollDisintegrate(deity, rand));
        addDeitySpell("Strongwall", deity, rollStrongwall(deity, rand));
        for (Spell spell : Spells.getAllSpells()) {
          if (!spell.isKarmaSpell())
          {
            if ((!ignoreSpells.contains(spell)) && (spell.deityCanHaveSpell(deity.getNumber()))) {
              deity.addSpell(spell);
            }
            if ((Servers.isThisAPvpServer()) && (spell.getNumber() == Spells.SPELL_DISINTEGRATE.getNumber()) && (!deity.hasSpell(spell))) {
              deity.addSpell(spell);
            }
          }
        }
        if (deity.getNumber() == 31)
        {
          deity.removeSpell(Spells.SPELL_LIFE_TRANSFER);
          deity.addSpell(Spells.SPELL_BLOODTHIRST);
          deity.addSpell(Spells.SPELL_WEB_ARMOUR);
        }
      }
    }
  }
  
  private static void generateLegacyDemigodSpells()
  {
    for (Deity deity : ) {
      if (deity.getNumber() > 4)
      {
        Random rand = deity.initializeAndGetRand();
        for (Spell spell : Spells.getAllSpells()) {
          if (!spell.isKarmaSpell())
          {
            boolean blocked = false;
            if (spell == Spells.SPELL_REBIRTH) {
              if (deity.hasSpell(Spells.SPELL_OAKSHELL)) {
                blocked = true;
              } else if (spell.isSpellBlocked(deity.getNumber(), Spells.SPELL_OAKSHELL.getNumber())) {
                blocked = true;
              }
            }
            if (spell == Spells.SPELL_SCORN_OF_LIBILA) {
              if (deity.hasSpell(Spells.SPELL_SCORN_OF_LIBILA)) {
                blocked = true;
              } else if (spell.isSpellBlocked(deity.getNumber(), Spells.SPELL_LIGHT_OF_FO.getNumber())) {
                blocked = true;
              }
            }
            if (spell == Spells.SPELL_FIRE_PILLAR) {
              if ((deity.hasSpell(Spells.SPELL_ICE_PILLAR)) || (deity.hasSpell(Spells.SPELL_PAIN_RAIN))) {
                blocked = true;
              } else if (spell.isSpellBlocked(deity.getNumber(), Spells.SPELL_ICE_PILLAR.getNumber())) {
                blocked = true;
              } else if (spell.isSpellBlocked(deity.getNumber(), Spells.SPELL_PAIN_RAIN.getNumber())) {
                blocked = true;
              }
            }
            if (spell == Spells.SPELL_ICE_PILLAR) {
              if (deity.hasSpell(Spells.SPELL_PAIN_RAIN)) {
                blocked = true;
              } else if (spell.isSpellBlocked(deity.getNumber(), Spells.SPELL_PAIN_RAIN.getNumber())) {
                blocked = true;
              }
            }
            if (spell == Spells.SPELL_ROTTING_GUT) {
              if (deity.hasSpell(Spells.SPELL_HEAL)) {
                blocked = true;
              } else if (spell.isSpellBlocked(deity.getNumber(), Spells.SPELL_HEAL.getNumber())) {
                blocked = true;
              }
            }
            if (spell == Spells.SPELL_WORM_BRAINS) {
              if (deity.hasSpell(Spells.SPELL_HEAL)) {
                blocked = true;
              } else if (spell.isSpellBlocked(deity.getNumber(), Spells.SPELL_HEAL.getNumber())) {
                blocked = true;
              }
            }
            if ((!blocked) && (spell.deityCanHaveSpell(deity.getNumber()))) {
              if (!deity.hasSpell(spell)) {
                deity.addSpell(spell);
              }
            }
          }
        }
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\SpellGenerator.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */