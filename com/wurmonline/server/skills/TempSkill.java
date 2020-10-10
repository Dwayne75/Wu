package com.wurmonline.server.skills;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class TempSkill
  extends Skill
{
  private static Logger logger = Logger.getLogger(TempSkill.class.getName());
  
  public TempSkill(int aNumber, double aStartValue, Skills aParent)
  {
    super(aNumber, aStartValue, aParent);
  }
  
  public TempSkill(long aId, Skills aParent, int aNumber, double aKnowledge, double aMinimum, long aLastused)
  {
    super(aId, aParent, aNumber, aKnowledge, aMinimum, aLastused);
  }
  
  public TempSkill(long aId, Skills aParent)
    throws IOException
  {
    super(aId, aParent);
  }
  
  void save()
    throws IOException
  {}
  
  void load()
    throws IOException
  {}
  
  void saveValue(boolean aPlayer)
    throws IOException
  {}
  
  public void setJoat(boolean aJoat)
    throws IOException
  {}
  
  public void setNumber(int newNumber)
    throws IOException
  {
    long pid = this.parent.getId();
    if (WurmId.getType(pid) == 0) {
      try
      {
        Player player = Players.getInstance().getPlayer(pid);
        Skill realSkill = player.getSkills().learn(this.number, (float)this.knowledge, false);
        realSkill.setNumber(newNumber);
      }
      catch (NoSuchPlayerException nsp)
      {
        logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsp);
      }
    } else {
      try
      {
        Creature creature = Creatures.getInstance().getCreature(pid);
        Skill realSkill = creature.getSkills().learn(this.number, (float)this.knowledge, false);
        realSkill.setNumber(newNumber);
      }
      catch (NoSuchCreatureException nsp)
      {
        logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsp);
      }
    }
  }
  
  protected void alterSkill(double advanceMultiplicator, boolean decay, float times)
  {
    alterSkill(advanceMultiplicator, decay, times, false, 1.0D);
  }
  
  protected void alterSkill(double advanceMultiplicator, boolean decay, float times, boolean useNewSystem, double skillDivider)
  {
    long pid = this.parent.getId();
    if (WurmId.getType(pid) == 0) {
      try
      {
        Player player = Players.getInstance().getPlayer(pid);
        Skill realSkill = player.getSkills().learn(this.number, (float)this.knowledge, false);
        realSkill.alterSkill(advanceMultiplicator, decay, times, useNewSystem, skillDivider);
      }
      catch (NoSuchPlayerException nsp)
      {
        logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsp);
      }
    } else {
      try
      {
        Creature creature = Creatures.getInstance().getCreature(pid);
        Skill realSkill = creature.getSkills().learn(this.number, (float)this.knowledge, false);
        realSkill.alterSkill(advanceMultiplicator, decay, times, useNewSystem, skillDivider);
      }
      catch (NoSuchCreatureException nsc)
      {
        logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsc);
      }
    }
  }
  
  public void setKnowledge(double aKnowledge, boolean load)
  {
    long pid = this.parent.getId();
    if (WurmId.getType(pid) == 0) {
      try
      {
        Player player = Players.getInstance().getPlayer(pid);
        Skill realSkill = player.getSkills().learn(this.number, (float)this.knowledge, false);
        realSkill.setKnowledge(aKnowledge, load);
      }
      catch (NoSuchPlayerException nsp)
      {
        logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsp);
      }
    } else {
      try
      {
        Creature creature = Creatures.getInstance().getCreature(pid);
        Skill realSkill = creature.getSkills().learn(this.number, (float)this.knowledge, false);
        realSkill.setKnowledge(aKnowledge, load);
      }
      catch (NoSuchCreatureException nsp)
      {
        logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsp);
      }
    }
  }
  
  public void setKnowledge(double aKnowledge, boolean load, boolean setMinimum)
  {
    long pid = this.parent.getId();
    if (WurmId.getType(pid) == 0) {
      try
      {
        Player player = Players.getInstance().getPlayer(pid);
        Skill realSkill = player.getSkills().learn(this.number, (float)this.knowledge, false);
        realSkill.setKnowledge(aKnowledge, load, setMinimum);
      }
      catch (NoSuchPlayerException nsp)
      {
        logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsp);
      }
    } else {
      try
      {
        Creature creature = Creatures.getInstance().getCreature(pid);
        Skill realSkill = creature.getSkills().learn(this.number, (float)this.knowledge, false);
        realSkill.setKnowledge(aKnowledge, load, setMinimum);
      }
      catch (NoSuchCreatureException nsp)
      {
        logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsp);
      }
    }
  }
  
  public double skillCheck(double check, double bonus, boolean test, float times)
  {
    return skillCheck(check, bonus, test, 10.0F, true, 1.100000023841858D, null, null);
  }
  
  public double skillCheck(double check, double bonus, boolean test, float times, boolean useNewSystem, double skillDivider)
  {
    return skillCheck(check, bonus, test, 10.0F, true, 1.100000023841858D, null, null);
  }
  
  public double skillCheck(double check, double bonus, boolean test, float times, @Nullable Creature skillowner, @Nullable Creature opponent)
  {
    return skillCheck(check, bonus, test, 10.0F, true, 1.100000023841858D, skillowner, opponent);
  }
  
  public double skillCheck(double check, double bonus, boolean test, float times, boolean useNewSystem, double skillDivider, @Nullable Creature skillowner, @Nullable Creature opponent)
  {
    if (skillowner != null)
    {
      Skill realSkill = skillowner.getSkills().learn(this.number, (float)this.knowledge, false);
      return realSkill.skillCheck(check, bonus, test, 10.0F, true, 1.100000023841858D, skillowner, opponent);
    }
    long pid = this.parent.getId();
    if (WurmId.getType(pid) == 0) {
      try
      {
        Player player = Players.getInstance().getPlayer(pid);
        Skill realSkill = player.getSkills().learn(this.number, (float)this.knowledge, false);
        return realSkill.skillCheck(check, bonus, test, 10.0F, true, 1.100000023841858D, skillowner, opponent);
      }
      catch (NoSuchPlayerException nsp)
      {
        logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsp);
        return 0.0D;
      }
    }
    try
    {
      Creature creature = Creatures.getInstance().getCreature(pid);
      Skill realSkill = creature.getSkills().learn(this.number, (float)this.knowledge, false);
      return realSkill.skillCheck(check, bonus, test, 10.0F, true, 1.100000023841858D, skillowner, opponent);
    }
    catch (NoSuchCreatureException nsp)
    {
      logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsp);
    }
    return 0.0D;
  }
  
  public double skillCheck(double check, Item item, double bonus, boolean test, float times, @Nullable Creature skillowner, @Nullable Creature opponent)
  {
    return skillCheck(check, item, bonus, test, 10.0F, true, 1.100000023841858D, skillowner, opponent);
  }
  
  public double skillCheck(double check, Item item, double bonus, boolean test, float times, boolean useNewSystem, double skillDivider, @Nullable Creature skillowner, @Nullable Creature opponent)
  {
    if (skillowner != null)
    {
      Skill realSkill = skillowner.getSkills().learn(this.number, (float)this.knowledge, false);
      return realSkill.skillCheck(check, item, bonus, test, 10.0F, true, 1.100000023841858D, skillowner, opponent);
    }
    long pid = this.parent.getId();
    if (WurmId.getType(pid) == 0) {
      try
      {
        Player player = Players.getInstance().getPlayer(pid);
        Skill realSkill = player.getSkills().learn(this.number, (float)this.knowledge, false);
        return realSkill.skillCheck(check, item, bonus, test, 10.0F, true, 1.100000023841858D, skillowner, opponent);
      }
      catch (NoSuchPlayerException nsp)
      {
        logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsp);
        return 0.0D;
      }
    }
    try
    {
      Creature creature = Creatures.getInstance().getCreature(pid);
      Skill realSkill = creature.getSkills().learn(this.number, (float)this.knowledge, false);
      return realSkill.skillCheck(check, item, bonus, test, 10.0F, true, 1.100000023841858D, skillowner, opponent);
    }
    catch (NoSuchCreatureException nsp)
    {
      logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsp);
    }
    return 0.0D;
  }
  
  public double skillCheck(double check, Item item, double bonus, boolean test, float times)
  {
    return skillCheck(check, item, bonus, test, 10.0F, true, 1.100000023841858D, null, null);
  }
  
  public double skillCheck(double check, Item item, double bonus, boolean test, float times, boolean useNewSystem, double skillDivider)
  {
    return skillCheck(check, item, bonus, test, 10.0F, true, 1.100000023841858D, null, null);
  }
  
  public final boolean isTemporary()
  {
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\skills\TempSkill.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */