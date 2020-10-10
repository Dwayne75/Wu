package com.wurmonline.server.creatures;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.zones.VolaTile;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SpellEffects
{
  private Creature creature;
  private final Map<Byte, SpellEffect> spellEffects;
  private static final Logger logger = Logger.getLogger(SpellEffects.class.getName());
  private static final SpellEffect[] EMPTY_SPELLS = new SpellEffect[0];
  
  SpellEffects(long _creatureId)
  {
    try
    {
      this.creature = Server.getInstance().getCreature(_creatureId);
    }
    catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException nsp)
    {
      logger.log(Level.INFO, nsp.getMessage(), nsp);
    }
    this.spellEffects = new HashMap();
    if (WurmId.getType(_creatureId) == 0)
    {
      SpellEffect[] speffs = SpellEffect.loadEffectsForPlayer(_creatureId);
      for (int x = 0; x < speffs.length; x++) {
        addSpellEffect(speffs[x]);
      }
    }
  }
  
  public Creature getCreature()
  {
    return this.creature;
  }
  
  public void addSpellEffect(SpellEffect effect)
  {
    SpellEffect old = getSpellEffect(effect.type);
    if ((old != null) && (old.power > effect.power))
    {
      effect.delete();
      return;
    }
    if (old != null)
    {
      old.delete();
      if (this.creature != null) {
        this.creature.sendUpdateSpellEffect(effect);
      }
    }
    else if (this.creature != null)
    {
      this.creature.sendAddSpellEffect(effect);
    }
    this.spellEffects.put(Byte.valueOf(effect.type), effect);
    if ((effect.type == 22) && (this.creature.getCurrentTile() != null)) {
      this.creature.getCurrentTile().setNewRarityShader(this.creature);
    }
  }
  
  public void sendAllSpellEffects()
  {
    if (this.creature != null) {
      for (SpellEffect sp : getEffects()) {
        this.creature.sendAddSpellEffect(sp);
      }
    }
  }
  
  public SpellEffect getSpellEffect(byte type)
  {
    Byte key = Byte.valueOf(type);
    if (this.spellEffects.containsKey(key)) {
      return (SpellEffect)this.spellEffects.get(key);
    }
    return null;
  }
  
  public SpellEffect[] getEffects()
  {
    if (this.spellEffects.size() > 0) {
      return (SpellEffect[])this.spellEffects.values().toArray(new SpellEffect[this.spellEffects.size()]);
    }
    return EMPTY_SPELLS;
  }
  
  public void poll()
  {
    SpellEffect[] effects = getEffects();
    for (int x = 0; x < effects.length; x++)
    {
      if (effects[x].type == 94) {
        if (Server.rand.nextInt(10) == 0)
        {
          Creature c = getCreature();
          try
          {
            c.addWoundOfType(null, (byte)4, c.getBody().getRandomWoundPos(), false, 0.0F, true, 
              Math.max(20.0F, effects[x].getPower()) * 50.0F, 0.0F, 0.0F, false, true);
            c.getCommunicator().sendAlertServerMessage("The pain from the heat is excruciating!");
          }
          catch (Exception e)
          {
            logger.log(Level.WARNING, c.getName() + ": " + e.getMessage());
          }
        }
      }
      if (effects[x].poll(this)) {
        if (effects[x].type == 22)
        {
          Creature c = getCreature();
          if (c.getCurrentTile() != null) {
            c.getCurrentTile().setNewRarityShader(c);
          }
        }
      }
    }
  }
  
  public SpellEffect removeSpellEffect(SpellEffect old)
  {
    if (old != null)
    {
      if (this.creature != null) {
        this.creature.removeSpellEffect(old);
      }
      old.delete();
      this.spellEffects.remove(Byte.valueOf(old.type));
    }
    return old;
  }
  
  void destroy(boolean keepHunted)
  {
    SpellEffect[] effects = getEffects();
    SpellEffect hunted = null;
    for (int x = 0; x < effects.length; x++) {
      if ((effects[x].type != 64) || (!keepHunted))
      {
        if (this.creature != null) {
          if (this.creature.getCommunicator() != null)
          {
            SpellEffectsEnum spellEffect = SpellEffectsEnum.getEnumByName(effects[x].getName());
            if (spellEffect != SpellEffectsEnum.NONE) {
              this.creature.getCommunicator().sendRemoveSpellEffect(effects[x].id, spellEffect);
            }
          }
        }
        effects[x].delete();
      }
      else if (effects[x].type == 64)
      {
        hunted = effects[x];
      }
    }
    this.spellEffects.clear();
    if (hunted == null)
    {
      if (!keepHunted) {
        this.creature = null;
      }
    }
    else {
      this.spellEffects.put(Byte.valueOf((byte)64), hunted);
    }
  }
  
  public void sleep()
  {
    this.spellEffects.clear();
    this.creature = null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\creatures\SpellEffects.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */