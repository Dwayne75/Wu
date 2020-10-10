package com.wurmonline.server.skills;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.RuneUtilities.ModifierEffect;
import com.wurmonline.server.modifiers.DoubleValueModifier;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Titles.Title;
import com.wurmonline.server.players.Titles.TitleType;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Skill
  implements MiscConstants, CounterTypes, TimeConstants, Comparable<Skill>
{
  public long lastUsed;
  protected double knowledge = 1.0D;
  private static final double regainMultiplicator = 3.0D;
  public double minimum;
  boolean joat = false;
  int number;
  private static final double maxBonus = 70.0D;
  public static final Logger affinityDebug = Logger.getLogger("affinities");
  private static int totalAffinityChecks = 0;
  private static int totalAffinitiesGiven = 0;
  Skills parent;
  private static Logger logger = Logger.getLogger(Skill.class.getName());
  public int affinity = 0;
  private static final float affinityMultiplier = 0.1F;
  public long id = -10L;
  private Set<DoubleValueModifier> modifiers = null;
  private byte saveCounter = 0;
  private static Random random = new Random();
  private static final byte[][] chances = calculateChances();
  private static final double skillMod = Servers.localServer.EPIC ? 3.0D : 1.5D;
  private static final double maxSkillGain = 1.0D;
  private boolean basicPersonal = false;
  private boolean noCurve = false;
  protected static final boolean isChallenge = Servers.localServer.isChallengeServer();
  
  Skill(int aNumber, double startValue, Skills aParent)
  {
    this.number = aNumber;
    this.knowledge = Math.max(1.0D, startValue);
    this.minimum = startValue;
    this.parent = aParent;
    if (aParent.isPersonal())
    {
      if (WurmId.getType(aParent.getId()) == 0)
      {
        this.id = (isTemporary() ? WurmId.getNextTemporarySkillId() : WurmId.getNextPlayerSkillId());
        if ((SkillSystem.getTypeFor(aNumber) == 0) || 
          (SkillSystem.getTypeFor(this.number) == 1))
        {
          this.knowledge = Math.max(1.0D, startValue);
          this.minimum = this.knowledge;
          this.basicPersonal = true;
          this.noCurve = true;
        }
      }
      else
      {
        this.id = (isTemporary() ? WurmId.getNextTemporarySkillId() : WurmId.getNextCreatureSkillId());
      }
      if (this.number == 10076) {
        this.noCurve = true;
      }
    }
  }
  
  Skill(long _id, int _number, double _knowledge, double _minimum, long _lastused)
  {
    this.id = _id;
    this.number = _number;
    this.knowledge = _knowledge;
    this.minimum = _minimum;
    this.lastUsed = _lastused;
  }
  
  public boolean isDirty()
  {
    return this.saveCounter > 0;
  }
  
  Skill(long _id, Skills _parent, int _number, double _knowledge, double _minimum, long _lastused)
  {
    this.id = _id;
    this.parent = _parent;
    this.number = _number;
    this.knowledge = _knowledge;
    this.minimum = _minimum;
    this.lastUsed = _lastused;
    if (WurmId.getType(this.parent.getId()) == 0)
    {
      if ((SkillSystem.getTypeFor(this.number) == 0) || 
        (SkillSystem.getTypeFor(this.number) == 1))
      {
        this.basicPersonal = true;
        this.noCurve = true;
      }
      if (this.number == 10076) {
        this.noCurve = true;
      }
    }
  }
  
  public int compareTo(Skill otherSkill)
  {
    return getName().compareTo(otherSkill.getName());
  }
  
  private static final byte[][] calculateChances()
  {
    logger.log(Level.INFO, "Calculating skill chances...");
    long start = System.nanoTime();
    byte[][] toReturn = (byte[][])null;
    try
    {
      toReturn = DbSkill.loadSkillChances();
      if (toReturn == null) {
        throw new WurmServerException("Load failed. Creating chances.");
      }
      logger.log(Level.INFO, "Loaded skill chances succeeded.");
    }
    catch (Exception ex)
    {
      float lElapsedTime;
      toReturn = new byte[101][101];
      for (int x = 0; x < 101; x++) {
        for (int y = 0; y < 101; y++) {
          if (x == 0)
          {
            toReturn[x][y] = 0;
          }
          else if (y == 0)
          {
            toReturn[x][y] = 99;
          }
          else
          {
            float succeed = 0.0F;
            for (int t = 0; t < 1000; t++) {
              succeed += 1.0F;
            }
            succeed /= 10.0F;
            toReturn[x][y] = ((byte)(int)succeed);
          }
        }
      }
      Thread t = new Skill.1();
      
      t.setPriority(3);
      t.start();
    }
    finally
    {
      float lElapsedTime;
      float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
      logger.info("Done. Loading/Calculating skill chances from the database took " + lElapsedTime + " millis.");
    }
    return toReturn;
  }
  
  Skill(long aId, Skills aParent)
    throws IOException
  {
    this.id = aId;
    this.parent = aParent;
    load();
  }
  
  public void addModifier(DoubleValueModifier modifier)
  {
    if (this.modifiers == null) {
      this.modifiers = new HashSet();
    }
    this.modifiers.add(modifier);
  }
  
  public void removeModifier(DoubleValueModifier modifier)
  {
    if (this.modifiers != null) {
      this.modifiers.remove(modifier);
    }
  }
  
  private boolean ignoresEnemy()
  {
    return SkillSystem.ignoresEnemies(this.number);
  }
  
  public double getModifierValues()
  {
    double toReturn = 0.0D;
    Iterator<DoubleValueModifier> it;
    if (this.modifiers != null) {
      for (it = this.modifiers.iterator(); it.hasNext();) {
        toReturn += ((DoubleValueModifier)it.next()).getModifier();
      }
    }
    return toReturn;
  }
  
  void setParent(Skills skills)
  {
    this.parent = skills;
  }
  
  public String getName()
  {
    return SkillSystem.getNameFor(this.number);
  }
  
  public int getNumber()
  {
    return this.number;
  }
  
  public long getId()
  {
    return this.id;
  }
  
  public double getKnowledge()
  {
    return this.knowledge;
  }
  
  public double getKnowledge(double bonus)
  {
    if (bonus > 70.0D) {
      bonus = 70.0D;
    }
    double bonusKnowledge = this.knowledge;
    if ((this.number == 102) || (this.number == 105))
    {
      long parentId = this.parent.getId();
      if (parentId != -10L) {
        try
        {
          Creature holder = Server.getInstance().getCreature(parentId);
          
          float hellStrength = holder.getBonusForSpellEffect((byte)40);
          float forestGiantStrength = holder.getBonusForSpellEffect((byte)25);
          if (hellStrength > 0.0F)
          {
            double pow = 0.8D;
            double target = Math.pow(this.knowledge / 100.0D, 0.8D) * 100.0D;
            double diff = target - this.knowledge;
            bonusKnowledge += diff * hellStrength / 100.0D;
          }
          else if ((forestGiantStrength > 0.0F) && (this.number == 102))
          {
            double pow = 0.6D;
            double target = Math.pow(this.knowledge / 100.0D, 0.6D) * 100.0D;
            double diff = target - this.knowledge;
            bonusKnowledge += diff * forestGiantStrength / 100.0D;
          }
          float ws = holder.getBonusForSpellEffect((byte)41);
          if (ws > 0.0F) {
            bonusKnowledge *= 0.800000011920929D;
          }
        }
        catch (NoSuchPlayerException localNoSuchPlayerException) {}catch (NoSuchCreatureException localNoSuchCreatureException) {}
      }
    }
    if (bonus != 0.0D)
    {
      double linearMax = (100.0D + bonusKnowledge) / 2.0D;
      
      double diffToMaxChange = Math.min(bonusKnowledge, linearMax - bonusKnowledge);
      double newBon = diffToMaxChange * bonus / 100.0D;
      bonusKnowledge += newBon;
    }
    bonusKnowledge = Math.max(1.0D, bonusKnowledge * (1.0D + getModifierValues()));
    if (!this.parent.paying)
    {
      if ((!this.basicPersonal) || (Servers.localServer.PVPSERVER)) {
        return Math.min(bonusKnowledge, 20.0D);
      }
      return Math.min(bonusKnowledge, 30.0D);
    }
    if (this.noCurve) {
      return bonusKnowledge;
    }
    return Server.getModifiedPercentageEffect(bonusKnowledge);
  }
  
  public double getKnowledge(Item item, double bonus)
  {
    if ((item == null) || (item.isBodyPart())) {
      return getKnowledge(bonus);
    }
    if (this.number == 1023) {
      try
      {
        int primweaponskill = item.getPrimarySkill();
        Skill pw = null;
        try
        {
          pw = this.parent.getSkill(primweaponskill);
          bonus += pw.getKnowledge(item, 0.0D);
        }
        catch (NoSuchSkillException nss)
        {
          pw = this.parent.learn(primweaponskill, 1.0F);
          bonus += pw.getKnowledge(item, 0.0D);
        }
      }
      catch (NoSuchSkillException localNoSuchSkillException1) {}
    }
    double bonusKnowledge = 0.0D;
    double ql = item.getCurrentQualityLevel();
    if (bonus > 70.0D) {
      bonus = 70.0D;
    }
    if (ql <= this.knowledge)
    {
      bonusKnowledge = (this.knowledge + ql) / 2.0D;
    }
    else
    {
      double diff = ql - this.knowledge;
      bonusKnowledge = this.knowledge + this.knowledge * diff / 100.0D;
    }
    if (this.number == 102)
    {
      long parentId = this.parent.getId();
      if (parentId != -10L) {
        try
        {
          Creature holder = Server.getInstance().getCreature(parentId);
          
          float hs = holder.getBonusForSpellEffect((byte)40);
          if (hs > 0.0F)
          {
            if (this.knowledge < 40.0D)
            {
              double diff = 40.0D - this.knowledge;
              bonusKnowledge += diff * hs / 100.0D;
            }
          }
          else
          {
            float x = holder.getBonusForSpellEffect((byte)25);
            if (x > 0.0F) {
              if (this.knowledge < 40.0D)
              {
                double diff = 40.0D - this.knowledge;
                bonusKnowledge += diff * x / 100.0D;
              }
            }
          }
          float ws = holder.getBonusForSpellEffect((byte)41);
          if (ws > 0.0F) {
            bonusKnowledge *= 0.800000011920929D;
          }
        }
        catch (NoSuchPlayerException nsp)
        {
          logger.log(Level.WARNING, nsp.getMessage(), nsp);
        }
        catch (NoSuchCreatureException localNoSuchCreatureException) {}
      }
    }
    if (bonus != 0.0D)
    {
      double linearMax = (100.0D + bonusKnowledge) / 2.0D;
      
      double diffToMaxChange = Math.min(bonusKnowledge, linearMax - bonusKnowledge);
      double newBon = diffToMaxChange * bonus / 100.0D;
      bonusKnowledge += newBon;
    }
    bonusKnowledge = Math.max(1.0D, bonusKnowledge * (1.0D + getModifierValues()));
    if (!this.parent.paying)
    {
      if ((!this.basicPersonal) || (Servers.localServer.PVPSERVER)) {
        return Math.min(bonusKnowledge, 20.0D);
      }
      return Math.min(bonusKnowledge, 30.0D);
    }
    if (this.basicPersonal) {
      return bonusKnowledge;
    }
    return Server.getModifiedPercentageEffect(bonusKnowledge);
  }
  
  public final double getRealKnowledge()
  {
    if (this.parent.paying) {
      return getKnowledge();
    }
    if ((!this.basicPersonal) || (Servers.localServer.PVPSERVER)) {
      return Math.min(getKnowledge(), 20.0D);
    }
    return Math.min(getKnowledge(), 30.0D);
  }
  
  public void setKnowledge(double aKnowledge, boolean load)
  {
    setKnowledge(aKnowledge, load, false);
  }
  
  public void setKnowledge(double aKnowledge, boolean load, boolean setMinimum)
  {
    if (aKnowledge < 100.0D)
    {
      double oldknowledge = this.knowledge;
      this.knowledge = Math.max(Math.min(aKnowledge, 100.0D), 1.0D);
      
      checkTitleChange(oldknowledge, this.knowledge);
      if (!load)
      {
        if (setMinimum) {
          this.minimum = this.knowledge;
        }
        try
        {
          save();
        }
        catch (IOException iox)
        {
          logger.log(Level.INFO, "Failed to save skill " + this.id, iox);
        }
        long parentId = this.parent.getId();
        if (parentId != -10L) {
          if (WurmId.getType(parentId) == 0) {
            try
            {
              Player holder = Players.getInstance().getPlayer(parentId);
              
              double bonusKnowledge = this.knowledge;
              if (this.number == 102)
              {
                float hs = holder.getBonusForSpellEffect((byte)40);
                if (hs > 0.0F)
                {
                  if (this.knowledge < 40.0D)
                  {
                    double diff = 40.0D - this.knowledge;
                    bonusKnowledge = this.knowledge + diff * hs / 100.0D;
                  }
                }
                else
                {
                  float x = holder.getBonusForSpellEffect((byte)25);
                  if (x > 0.0F) {
                    if (this.knowledge < 40.0D)
                    {
                      double diff = 40.0D - this.knowledge;
                      bonusKnowledge = this.knowledge + diff * x / 100.0D;
                    }
                  }
                }
                float ws = holder.getBonusForSpellEffect((byte)41);
                if (ws > 0.0F) {
                  bonusKnowledge *= 0.800000011920929D;
                }
              }
              if ((!this.parent.paying) && (!this.basicPersonal)) {
                bonusKnowledge = Math.min(20.0D, bonusKnowledge);
              } else if ((!this.parent.paying) && (bonusKnowledge > 20.0D)) {
                bonusKnowledge = Math.min(getKnowledge(0.0D), bonusKnowledge);
              }
              holder.getCommunicator().sendUpdateSkill(this.number, (float)bonusKnowledge, 
                isTemporary() ? 0 : this.affinity);
            }
            catch (NoSuchPlayerException nsp)
            {
              logger.log(Level.WARNING, nsp.getMessage(), nsp);
            }
          }
        }
      }
    }
  }
  
  public double getMinimumValue()
  {
    return this.minimum;
  }
  
  @Nonnull
  public int[] getDependencies()
  {
    return SkillSystem.getDependenciesFor(this.number);
  }
  
  public int[] getUniqueDependencies()
  {
    int[] fDeps = getDependencies();
    Set<Integer> lst = new HashSet();
    for (int i = 0; i < fDeps.length; i++)
    {
      Integer val = Integer.valueOf(fDeps[i]);
      if (!lst.contains(val)) {
        lst.add(val);
      }
    }
    int[] deps = new int[lst.size()];
    int ind = 0;
    for (Integer i : lst)
    {
      deps[ind] = i.intValue();
      ind++;
    }
    return deps;
  }
  
  public double getDifficulty(boolean checkPriest)
  {
    return SkillSystem.getDifficultyFor(this.number, checkPriest);
  }
  
  public short getType()
  {
    return SkillSystem.getTypeFor(this.number);
  }
  
  public double skillCheck(double check, double bonus, boolean test, float times, boolean useNewSystem, double skillDivider)
  {
    return skillCheck(check, bonus, test, times, useNewSystem, skillDivider, null, null);
  }
  
  public double skillCheck(double check, double bonus, boolean test, float times)
  {
    return skillCheck(check, bonus, test, 10.0F, true, 2.0D);
  }
  
  public double skillCheck(double check, double bonus, boolean test, float times, @Nullable Creature skillowner, @Nullable Creature opponent)
  {
    return skillCheck(check, bonus, test, 10.0F, true, 2.0D, skillowner, opponent);
  }
  
  public double skillCheck(double check, double bonus, boolean test, float times, boolean useNewSystem, double skillDivider, @Nullable Creature skillowner, @Nullable Creature opponent)
  {
    if ((skillowner != null) && (opponent != null)) {
      if ((this.number == 10055) || (this.number == 10053) || (this.number != 10054)) {}
    }
    touch();
    double power = checkAdvance(check, null, bonus, test, times, useNewSystem, skillDivider);
    if (WurmId.getType(this.parent.getId()) == 0) {
      try
      {
        save();
      }
      catch (IOException localIOException) {}
    }
    return power;
  }
  
  public double skillCheck(double check, Item item, double bonus, boolean test, float times, @Nullable Creature skillowner, @Nullable Creature opponent)
  {
    return skillCheck(check, item, bonus, test, 10.0F, true, 2.0D, skillowner, opponent);
  }
  
  public double skillCheck(double check, Item item, double bonus, boolean test, float times, boolean useNewSystem, double skillDivider, @Nullable Creature skillowner, @Nullable Creature opponent)
  {
    if ((skillowner != null) && (opponent != null)) {}
    touch();
    double power = checkAdvance(check, item, bonus, test, times, useNewSystem, skillDivider);
    if (WurmId.getType(this.parent.getId()) == 0) {
      try
      {
        save();
      }
      catch (IOException localIOException) {}
    }
    return power;
  }
  
  public double skillCheck(double check, Item item, double bonus, boolean test, float times, boolean useNewSystem, double skillDivider)
  {
    return skillCheck(check, item, bonus, test, times, useNewSystem, skillDivider, null, null);
  }
  
  public double skillCheck(double check, Item item, double bonus, boolean test, float times)
  {
    return skillCheck(check, item, bonus, test, 10.0F, true, 2.0D, null, null);
  }
  
  public long getDecayTime()
  {
    return SkillSystem.getDecayTimeFor(this.number);
  }
  
  public void touch()
  {
    if (SkillSystem.getTickTimeFor(getNumber()) <= 0L) {
      this.lastUsed = System.currentTimeMillis();
    }
  }
  
  long getLastUsed()
  {
    return this.lastUsed;
  }
  
  boolean mayUpdateTimedSkill()
  {
    return System.currentTimeMillis() - this.lastUsed < SkillSystem.getTickTimeFor(getNumber());
  }
  
  void checkDecay() {}
  
  private void decay(boolean saved)
  {
    float decrease = 0.0F;
    if (getType() == 1)
    {
      alterSkill(-(100.0D - this.knowledge) / (getDifficulty(false) * this.knowledge), true, 1.0F);
    }
    else if (getType() == 0)
    {
      decrease = -0.1F;
      if (this.affinity > 0) {
        decrease = -0.1F + 0.05F * this.affinity;
      }
      if (saved) {
        alterSkill(decrease / 2.0F, true, 1.0F);
      } else {
        alterSkill(decrease, true, 1.0F);
      }
    }
    else
    {
      decrease = -0.25F;
      if (this.affinity > 0) {
        decrease = -0.25F + 0.025F * this.affinity;
      }
      if (saved) {
        alterSkill(decrease / 2.0F, true, 1.0F);
      } else {
        alterSkill(decrease, true, 1.0F);
      }
    }
  }
  
  public double getParentBonus()
  {
    double bonus = 0.0D;
    int[] dep = getDependencies();
    for (int x = 0; x < dep.length; x++)
    {
      short sType = SkillSystem.getTypeFor(dep[x]);
      if (sType == 2) {
        try
        {
          Skill enhancer = this.parent.getSkill(dep[x]);
          double ebonus = enhancer.getKnowledge(0.0D);
          bonus += ebonus;
        }
        catch (NoSuchSkillException ex)
        {
          logger.log(Level.WARNING, "Skill.checkAdvance(): Skillsystem bad. Skill '" + getName() + "' has no enhance parent with number " + dep[x] + ". Learning!", ex);
          
          this.parent.learn(dep[x], 1.0F);
        }
      }
    }
    return bonus;
  }
  
  public double getChance(double check, @Nullable Item item, double bonus)
  {
    bonus += getParentBonus();
    double skill = this.knowledge;
    if ((bonus != 0.0D) || (item != null)) {
      if (item == null) {
        skill = getKnowledge(bonus);
      } else {
        skill = getKnowledge(item, bonus);
      }
    }
    if (skill < 1.0D) {
      skill = 1.0D;
    }
    if (check < 1.0D) {
      check = 1.0D;
    }
    if ((item != null) && (item.getSpellEffects() != null))
    {
      float skillBonus = (float)((100.0D - skill) * (item.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_SKILLCHECKBONUS) - 1.0F));
      skill += skillBonus;
    }
    return getGaussianChance(skill, check);
  }
  
  public static final double getGaussianChance(double skill, double difficulty)
  {
    if ((skill > 99.0D) || (difficulty > 99.0D)) {
      return Math.max(0.0D, Math.min(100.0D, ((skill * skill * skill - difficulty * difficulty * difficulty) / 50000.0D + (skill - difficulty)) / 2.0D + 50.0D + 0.5D * (skill - difficulty)));
    }
    return chances[((int)skill)][((int)difficulty)];
  }
  
  public static final float rollGaussian(float skill, float difficulty, long parentId, String name)
  {
    float slide = (skill * skill * skill - difficulty * difficulty * difficulty) / 50000.0F + (skill - difficulty);
    
    float w = 30.0F - Math.abs(skill - difficulty) / 4.0F;
    int attempts = 0;
    
    float result = 0.0F;
    do
    {
      result = (float)random.nextGaussian() * (w + Math.abs(slide) / 6.0F) + slide;
      
      float rejectCutoff = (float)random.nextGaussian() * (w - Math.abs(slide) / 6.0F) + slide;
      if (slide > 0.0F)
      {
        if (result > rejectCutoff + Math.max(100.0F - slide, 0.0F)) {
          result = -1000.0F;
        }
      }
      else if (result < rejectCutoff - Math.max(100.0F + slide, 0.0F)) {
        result = -1000.0F;
      }
      attempts++;
      if (attempts == 100)
      {
        if (result > 100.0F) {
          return 90.0F + Server.rand.nextFloat() * 5.0F;
        }
        if (result < -100.0F) {
          return -90.0F - Server.rand.nextFloat() * 5.0F;
        }
      }
    } while ((result < -100.0F) || (result > 100.0F));
    return result;
  }
  
  private double checkAdvance(double check, @Nullable Item item, double bonus, boolean dryRun, float times, boolean useNewSystem, double skillDivider)
  {
    if (!dryRun) {
      dryRun = mayUpdateTimedSkill();
    }
    check = Math.max(1.0D, check);
    short skillType = SkillSystem.getTypeFor(this.number);
    
    int[] dep = getUniqueDependencies();
    for (int x = 0; x < dep.length; x++)
    {
      short sType = SkillSystem.getTypeFor(dep[x]);
      if (sType == 2) {
        try
        {
          Skill enhancer = this.parent.getSkill(dep[x]);
          
          double ebonus = Math.max(0.0D, enhancer
            .skillCheck(check, 0.0D, dryRun, times, useNewSystem, skillDivider) / 10.0D);
          bonus += ebonus;
        }
        catch (NoSuchSkillException ex)
        {
          Creature cret = null;
          try
          {
            cret = Server.getInstance().getCreature(this.parent.getId());
          }
          catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
          String name = "Unknown creature";
          if (cret != null) {
            name = cret.getName();
          }
          logger.log(Level.WARNING, name + " - Skill.checkAdvance(): Skillsystem bad. Skill '" + getName() + "' has no enhance parent with number " + dep[x], ex);
          
          this.parent.learn(dep[x], 1.0F);
        }
      } else {
        try
        {
          Skill par = this.parent.getSkill(dep[x]);
          if (par.getNumber() != 1023) {
            par.skillCheck(check, 0.0D, dryRun, times, useNewSystem, skillDivider);
          }
        }
        catch (NoSuchSkillException ex)
        {
          Creature cret = null;
          try
          {
            cret = Server.getInstance().getCreature(this.parent.getId());
          }
          catch (NoSuchCreatureException localNoSuchCreatureException1) {}catch (NoSuchPlayerException localNoSuchPlayerException1) {}
          String name = "Unknown creature";
          if (cret != null) {
            name = cret.getName();
          }
          logger.log(Level.WARNING, name + ": Skill.checkAdvance(): Skillsystem bad. Skill '" + getName() + "' has no limiting parent with number " + dep[x], ex);
          
          this.parent.learn(dep[x], 1.0F);
        }
      }
    }
    bonus = Math.min(70.0D, bonus);
    double skill = this.knowledge;
    double learnMod = 1.0D;
    if (item == null)
    {
      skill = getKnowledge(bonus);
    }
    else
    {
      skill = getKnowledge(item, bonus);
      if (item.getSpellSkillBonus() > 0.0F) {
        learnMod += item.getSpellSkillBonus() / 100.0F;
      }
    }
    if ((item != null) && (item.getSpellEffects() != null))
    {
      float skillBonus = (float)((100.0D - skill) * (item.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_SKILLCHECKBONUS) - 1.0F));
      skill += skillBonus;
    }
    double power = rollGaussian((float)skill, (float)check, this.parent.getId(), getName());
    if (!dryRun) {
      if (useNewSystem)
      {
        double divs = skillDivider;
        
        doSkillGainNew(check, power, learnMod, times, divs);
      }
      else
      {
        doSkillGainOld(power, learnMod, times);
      }
    }
    if (power > 0.0D)
    {
      Player p = Players.getInstance().getPlayerOrNull(this.parent.getId());
      if (p != null)
      {
        totalAffinityChecks += 1;
        if (p.shouldGiveAffinity(this.affinity, (skillType == 1) || (skillType == 0)))
        {
          if (this.affinity == 0) {
            p.getCommunicator().sendNormalServerMessage("You realize that you have developed an affinity for " + 
              SkillSystem.getNameFor(this.number).toLowerCase() + ".", (byte)2);
          } else {
            p.getCommunicator().sendNormalServerMessage("You realize that your affinity for " + 
              SkillSystem.getNameFor(this.number).toLowerCase() + " has grown stronger.", (byte)2);
          }
          Affinities.setAffinity(p.getWurmId(), this.number, this.affinity + 1, false);
          totalAffinitiesGiven += 1;
          affinityDebug.log(Level.INFO, p.getName() + " gained affinity for skill " + SkillSystem.getNameFor(this.number) + " from skill usage. New affinity: " + this.affinity + ". Total checks this restart: " + totalAffinityChecks + " Total affinities given this restart: " + totalAffinitiesGiven);
        }
      }
    }
    return power;
  }
  
  private final void doSkillGainNew(double check, double power, double learnMod, float times, double skillDivider)
  {
    double bonus = 1.0D;
    double diff = Math.abs(check - this.knowledge);
    short sType = SkillSystem.getTypeFor(this.number);
    boolean awardBonus = true;
    if ((sType == 1) || (sType == 0)) {
      awardBonus = false;
    }
    if ((diff <= 15.0D) && (awardBonus)) {
      bonus = 1.0D + 0.10000000149011612D * (diff / 15.0D);
    }
    if (power < 0.0D)
    {
      if (this.knowledge < 20.0D) {
        alterSkill((100.0D - this.knowledge) / (getDifficulty(this.parent.priest) * this.knowledge * this.knowledge) * learnMod * bonus, false, times, true, skillDivider);
      }
    }
    else {
      alterSkill((100.0D - this.knowledge) / (getDifficulty(this.parent.priest) * this.knowledge * this.knowledge) * learnMod * bonus, false, times, true, skillDivider);
    }
  }
  
  private final void doSkillGainOld(double power, double learnMod, float times)
  {
    if (power >= 0.0D) {
      if (this.knowledge < 20.0D)
      {
        alterSkill((100.0D - this.knowledge) / (getDifficulty(this.parent.priest) * this.knowledge * this.knowledge) * learnMod, false, times);
      }
      else if ((power > 0.0D) && (power < 40.0D))
      {
        alterSkill((100.0D - this.knowledge) / (getDifficulty(this.parent.priest) * this.knowledge * this.knowledge) * learnMod, false, times);
      }
      else if ((this.number == 10055) || (this.number == 10053) || (this.number == 10054))
      {
        Creature cret = null;
        try
        {
          cret = Server.getInstance().getCreature(this.parent.getId());
          if (cret.loggerCreature1 > 0L) {
            logger.log(Level.INFO, cret
            
              .getName() + " POWER=" + power);
          }
        }
        catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
      }
    }
  }
  
  protected void alterSkill(double advanceMultiplicator, boolean decay, float times)
  {
    alterSkill(advanceMultiplicator, decay, times, false, 1.0D);
  }
  
  protected void alterSkill(double advanceMultiplicator, boolean decay, float times, boolean useNewSystem, double skillDivider)
  {
    if (this.parent.hasSkillGain)
    {
      times = Math.min(SkillSystem.getTickTimeFor(getNumber()) > 0L ? 100.0F : 30.0F, times);
      advanceMultiplicator *= times * Servers.localServer.getSkillGainRate();
      this.lastUsed = System.currentTimeMillis();
      boolean isplayer = false;
      long pid = this.parent.getId();
      if (WurmId.getType(pid) == 0) {
        isplayer = true;
      }
      double oldknowledge = this.knowledge;
      if (decay)
      {
        if (isplayer)
        {
          if (this.knowledge <= 70.0D) {
            return;
          }
          double villageMod = 1.0D;
          try
          {
            Player player = Players.getInstance().getPlayer(pid);
            
            villageMod = player.getVillageSkillModifier();
          }
          catch (NoSuchPlayerException nsp)
          {
            logger.log(Level.WARNING, "Player with id " + this.id + " is decaying skills while not online?", nsp);
          }
          this.knowledge = Math.max(1.0D, this.knowledge + advanceMultiplicator * villageMod);
        }
        else
        {
          this.knowledge = Math.max(1.0D, this.knowledge + advanceMultiplicator);
        }
      }
      else
      {
        advanceMultiplicator *= skillMod;
        if ((this.number == 10086) && (Servers.localServer.isChallengeOrEpicServer()) && 
          (!Server.getInstance().isPS())) {
          advanceMultiplicator *= 2.0D;
        }
        if (isplayer) {
          try
          {
            Player player = Players.getInstance().getPlayer(pid);
            advanceMultiplicator *= (1.0F + ItemBonus.getSkillGainBonus(player, getNumber()));
            int currstam = player.getStatus().getStamina();
            float staminaMod = 1.0F;
            if (currstam <= 400) {
              staminaMod = 0.1F;
            }
            if ((player.getCultist() != null) && (player.getCultist().levelElevenSkillgain())) {
              staminaMod *= 1.25F;
            }
            if (player.getDeity() != null)
            {
              if ((player.mustChangeTerritory()) && (!player.isFighting()))
              {
                staminaMod = 0.1F;
                if (Server.rand.nextInt(100) == 0) {
                  player.getCommunicator().sendAlertServerMessage("You sense a lack of energy. Rumours have it that " + 
                    player.getDeity().name + " wants " + player
                    .getDeity().getHisHerItsString() + " champions to move between kingdoms and seek out the enemy.");
                }
              }
              if (player.getDeity().isLearner())
              {
                if ((player.getFaith() > 20.0F) && (player.getFavor() >= 10.0F)) {
                  staminaMod += 0.1F;
                }
              }
              else if (player.getDeity().isWarrior()) {
                if ((player.getFaith() > 20.0F) && (player.getFavor() >= 20.0F)) {
                  if (isFightingSkill()) {
                    staminaMod += 0.25F;
                  }
                }
              }
            }
            staminaMod += Math.max(player.getStatus().getNutritionlevel() / 10.0F - 0.05F, 0.0F);
            if ((player.isFighting()) && (currstam <= 400)) {
              staminaMod = 0.0F;
            }
            advanceMultiplicator *= staminaMod;
            if ((player.getEnemyPresense() > Player.minEnemyPresence) && 
              (!ignoresEnemy())) {
              advanceMultiplicator *= 0.800000011920929D;
            }
            if ((this.knowledge < this.minimum) || ((this.basicPersonal) && (this.knowledge < 20.0D))) {
              advanceMultiplicator *= 3.0D;
            }
            if (player.hasSleepBonus()) {
              advanceMultiplicator *= 2.0D;
            }
            int taffinity = this.affinity + (AffinitiesTimed.isTimedAffinity(pid, getNumber()) ? 1 : 0);
            advanceMultiplicator *= (1.0F + taffinity * 0.1F);
            if (player.getMovementScheme().samePosCounts > 20) {
              advanceMultiplicator = 0.0D;
            }
            if ((!player.isPaying()) && (this.knowledge >= 20.0D))
            {
              advanceMultiplicator = 0.0D;
              if ((!player.isPlayerAssistant()) && (Server.rand.nextInt(500) == 0)) {
                player.getCommunicator().sendNormalServerMessage("You may only gain skill beyond level 20 if you have a premium account.", (byte)2);
              }
            }
            if ((this.number == 10055) || (this.number == 10053) || (this.number == 10054)) {
              if (player.loggerCreature1 > 0L) {
                logger.log(Level.INFO, player
                
                  .getName() + " advancing " + 
                  Math.min(1.0D, advanceMultiplicator * this.knowledge / skillDivider) + "!");
              }
            }
          }
          catch (NoSuchPlayerException nsp)
          {
            advanceMultiplicator = 0.0D;
            logger.log(Level.WARNING, "Player with id " + this.id + " is learning skills while not online?", nsp);
          }
        }
        if (useNewSystem)
        {
          double maxSkillRate = 40.0D;
          double rateMod = 1.0D;
          short sType = SkillSystem.getTypeFor(this.number);
          if ((sType == 1) || (sType == 0))
          {
            maxSkillRate = 60.0D;
            rateMod = 0.8D;
          }
          double skillRate = Math.min(maxSkillRate, skillDivider * (1.0D + this.knowledge / (100.0D - 90.0D * (this.knowledge / 110.0D))) * rateMod);
          
          this.knowledge = Math.max(1.0D, this.knowledge + Math.min(1.0D, advanceMultiplicator * this.knowledge / skillRate));
        }
        else
        {
          this.knowledge = Math.max(1.0D, this.knowledge + Math.min(1.0D, advanceMultiplicator * this.knowledge));
        }
        if (this.minimum < this.knowledge) {
          this.minimum = this.knowledge;
        }
        checkTitleChange(oldknowledge, this.knowledge);
      }
      try
      {
        if (((oldknowledge != this.knowledge) && ((this.saveCounter == 0) || (this.knowledge > 50.0D))) || (decay)) {
          saveValue(isplayer);
        }
        this.saveCounter = ((byte)(this.saveCounter + 1));
        if (this.saveCounter == 10) {
          this.saveCounter = 0;
        }
      }
      catch (IOException ex)
      {
        logger.log(Level.WARNING, "Failed to save skill " + 
          getName() + "(" + getNumber() + ") for creature " + this.parent.getId(), ex);
      }
      if (pid != -10L) {
        if (isplayer) {
          try
          {
            Player holder = Players.getInstance().getPlayer(pid);
            float weakMod = 1.0F;
            
            double bonusKnowledge = this.knowledge;
            float ws = holder.getBonusForSpellEffect((byte)41);
            if (ws > 0.0F) {
              weakMod = 0.8F;
            }
            if ((this.number == 102) && (this.knowledge < 40.0D))
            {
              float x = holder.getBonusForSpellEffect((byte)25);
              if (x > 0.0F)
              {
                double diff = 40.0D - this.knowledge;
                bonusKnowledge = this.knowledge + diff * x / 100.0D;
              }
              else
              {
                float hs = holder.getBonusForSpellEffect((byte)40);
                if (hs > 0.0F)
                {
                  double diff = 40.0D - this.knowledge;
                  bonusKnowledge = this.knowledge + diff * hs / 100.0D;
                }
              }
            }
            bonusKnowledge *= weakMod;
            if (isplayer)
            {
              int diff = (int)this.knowledge - (int)oldknowledge;
              if (diff > 0) {
                holder.achievement(371, diff);
              }
            }
            if ((!this.parent.paying) && (!this.basicPersonal)) {
              bonusKnowledge = Math.min(20.0D, bonusKnowledge);
            } else if ((!this.parent.paying) && (bonusKnowledge > 20.0D)) {
              bonusKnowledge = Math.min(getKnowledge(0.0D), bonusKnowledge);
            }
            holder.getCommunicator().sendUpdateSkill(this.number, (float)bonusKnowledge, isTemporary() ? 0 : this.affinity);
            if ((this.number != 2147483644) && (this.number != 2147483642)) {
              holder.resetInactivity(true);
            }
          }
          catch (NoSuchPlayerException nsp)
          {
            logger.log(Level.WARNING, pid + ":" + nsp.getMessage(), nsp);
          }
        }
      }
    }
  }
  
  public boolean isTemporary()
  {
    return false;
  }
  
  public boolean isFightingSkill()
  {
    return SkillSystem.isFightingSkill(this.number);
  }
  
  public void checkInitialTitle()
  {
    if (getNumber() == 10067)
    {
      long pid = this.parent.getId();
      if (WurmId.getType(pid) == 0)
      {
        if (this.knowledge >= 20.0D)
        {
          Player p = Players.getInstance().getPlayerOrNull(pid);
          if (p != null) {
            p.maybeTriggerAchievement(605, true);
          }
        }
        if (this.knowledge >= 50.0D)
        {
          Player p = Players.getInstance().getPlayerOrNull(pid);
          if (p != null) {
            p.maybeTriggerAchievement(617, true);
          }
        }
      }
    }
    if (this.knowledge >= 50.0D)
    {
      long pid = this.parent.getId();
      if (WurmId.getType(pid) == 0)
      {
        Titles.Title title = Titles.Title.getTitle(this.number, Titles.TitleType.NORMAL);
        if (title != null) {
          try
          {
            Players.getInstance().getPlayer(pid).addTitle(title);
          }
          catch (NoSuchPlayerException nsp)
          {
            logger.log(Level.WARNING, pid + ":" + nsp.getMessage(), nsp);
          }
        }
      }
      Player p = Players.getInstance().getPlayerOrNull(pid);
      if (p != null) {
        p.maybeTriggerAchievement(555, true);
      }
    }
    if (this.knowledge >= 70.0D)
    {
      long pid = this.parent.getId();
      if (WurmId.getType(pid) == 0)
      {
        Titles.Title title = Titles.Title.getTitle(this.number, Titles.TitleType.MINOR);
        if (title != null) {
          try
          {
            Players.getInstance().getPlayer(pid).addTitle(title);
          }
          catch (NoSuchPlayerException nsp)
          {
            logger.log(Level.WARNING, pid + ":" + nsp.getMessage(), nsp);
          }
        }
      }
      Player p = Players.getInstance().getPlayerOrNull(pid);
      if (p != null) {
        p.maybeTriggerAchievement(564, true);
      }
      if ((p != null) && (getNumber() == 10066)) {
        p.maybeTriggerAchievement(633, true);
      }
    }
    if (this.knowledge >= 90.0D)
    {
      long pid = this.parent.getId();
      if (WurmId.getType(pid) == 0)
      {
        Titles.Title title = Titles.Title.getTitle(this.number, Titles.TitleType.MASTER);
        if (title != null) {
          try
          {
            Players.getInstance().getPlayer(pid).addTitle(title);
          }
          catch (NoSuchPlayerException nsp)
          {
            logger.log(Level.WARNING, pid + ":" + nsp.getMessage(), nsp);
          }
        }
      }
      Player p = Players.getInstance().getPlayerOrNull(pid);
      if (p != null) {
        p.maybeTriggerAchievement(590, true);
      }
    }
    if (this.knowledge >= 99.99999615D)
    {
      long pid = this.parent.getId();
      if (WurmId.getType(pid) == 0)
      {
        Titles.Title title = Titles.Title.getTitle(this.number, Titles.TitleType.LEGENDARY);
        if (title != null) {
          try
          {
            Players.getInstance().getPlayer(pid).addTitle(title);
          }
          catch (NoSuchPlayerException nsp)
          {
            logger.log(Level.WARNING, pid + ":" + nsp.getMessage(), nsp);
          }
        }
      }
    }
  }
  
  void checkTitleChange(double oldknowledge, double newknowledge)
  {
    if ((getNumber() == 10067) && (oldknowledge < 20.0D) && (newknowledge >= 20.0D))
    {
      long pid = this.parent.getId();
      if (WurmId.getType(pid) == 0) {
        try
        {
          Player p = Players.getInstance().getPlayer(pid);
          p.maybeTriggerAchievement(605, true);
        }
        catch (NoSuchPlayerException nsp)
        {
          logger.log(Level.WARNING, pid + ":" + nsp.getMessage(), nsp);
        }
      }
    }
    if ((oldknowledge < 50.0D) && (newknowledge >= 50.0D))
    {
      long pid = this.parent.getId();
      if (WurmId.getType(pid) == 0)
      {
        Titles.Title title = Titles.Title.getTitle(this.number, Titles.TitleType.NORMAL);
        if (title != null) {
          try
          {
            Player p = Players.getInstance().getPlayer(pid);
            p.addTitle(title);
            p.achievement(555);
            if (getNumber() == 10067) {
              p.maybeTriggerAchievement(617, true);
            }
          }
          catch (NoSuchPlayerException nsp)
          {
            logger.log(Level.WARNING, pid + ":" + nsp.getMessage(), nsp);
          }
        }
        int count = 0;
        for (Skill s : this.parent.getSkills()) {
          if (s.getKnowledge() >= 50.0D) {
            count++;
          }
        }
        if (count >= 10) {
          try
          {
            Player p = Players.getInstance().getPlayer(pid);
            p.maybeTriggerAchievement(598, true);
          }
          catch (NoSuchPlayerException localNoSuchPlayerException2) {}
        }
      }
    }
    if ((oldknowledge < 70.0D) && (newknowledge >= 70.0D))
    {
      long pid = this.parent.getId();
      if (WurmId.getType(pid) == 0)
      {
        Titles.Title title = Titles.Title.getTitle(this.number, Titles.TitleType.MINOR);
        if (title != null) {
          try
          {
            Player p = Players.getInstance().getPlayer(pid);
            p.addTitle(title);
            p.achievement(564);
            if (getNumber() == 10066) {
              p.maybeTriggerAchievement(633, true);
            }
          }
          catch (NoSuchPlayerException nsp)
          {
            logger.log(Level.WARNING, pid + ":" + nsp.getMessage(), nsp);
          }
        }
      }
    }
    if ((oldknowledge < 90.0D) && (newknowledge >= 90.0D))
    {
      long pid = this.parent.getId();
      if (WurmId.getType(pid) == 0)
      {
        Titles.Title title = Titles.Title.getTitle(this.number, Titles.TitleType.MASTER);
        if (title != null) {
          try
          {
            Player p = Players.getInstance().getPlayer(pid);
            p.addTitle(title);
            p.achievement(590);
          }
          catch (NoSuchPlayerException nsp)
          {
            logger.log(Level.WARNING, pid + ":" + nsp.getMessage(), nsp);
          }
        }
      }
    }
    if ((oldknowledge < 99.99999615D) && (newknowledge >= 99.99999615D))
    {
      long pid = this.parent.getId();
      if (WurmId.getType(pid) == 0)
      {
        Titles.Title title = Titles.Title.getTitle(this.number, Titles.TitleType.LEGENDARY);
        if (title != null) {
          try
          {
            Players.getInstance().getPlayer(pid).addTitle(title);
          }
          catch (NoSuchPlayerException nsp)
          {
            logger.log(Level.WARNING, pid + ":" + nsp.getMessage(), nsp);
          }
        }
      }
    }
  }
  
  public void setAffinity(int aff)
  {
    this.affinity = aff;
    long pid = this.parent.getId();
    if (WurmId.getType(pid) == 0) {
      if (!isTemporary()) {
        try
        {
          Player holder = Players.getInstance().getPlayer(pid);
          float weakMod = 1.0F;
          
          double bonusKnowledge = this.knowledge;
          float ws = holder.getBonusForSpellEffect((byte)41);
          if (ws > 0.0F) {
            weakMod = 0.8F;
          }
          if ((this.number == 102) && (this.knowledge < 40.0D))
          {
            float x = holder.getBonusForSpellEffect((byte)25);
            if (x > 0.0F)
            {
              double diff = 40.0D - this.knowledge;
              bonusKnowledge = this.knowledge + diff * x / 100.0D;
            }
            else
            {
              float hs = holder.getBonusForSpellEffect((byte)40);
              if (hs > 0.0F)
              {
                double diff = 40.0D - this.knowledge;
                bonusKnowledge = this.knowledge + diff * hs / 100.0D;
              }
            }
          }
          bonusKnowledge *= weakMod;
          if ((!this.parent.paying) && (!this.basicPersonal)) {
            bonusKnowledge = Math.min(20.0D, bonusKnowledge);
          } else if ((!this.parent.paying) && (bonusKnowledge > 20.0D)) {
            bonusKnowledge = Math.min(getKnowledge(0.0D), bonusKnowledge);
          }
          holder.getCommunicator().sendUpdateSkill(this.number, (float)bonusKnowledge, this.affinity);
        }
        catch (NoSuchPlayerException nsp)
        {
          logger.log(Level.WARNING, nsp.getMessage(), nsp);
        }
      }
    }
  }
  
  abstract void save()
    throws IOException;
  
  abstract void load()
    throws IOException;
  
  abstract void saveValue(boolean paramBoolean)
    throws IOException;
  
  public abstract void setJoat(boolean paramBoolean)
    throws IOException;
  
  public abstract void setNumber(int paramInt)
    throws IOException;
  
  public boolean hasLowCreationGain()
  {
    switch (getNumber())
    {
    case 1010: 
    case 10034: 
    case 10036: 
    case 10037: 
    case 10041: 
    case 10042: 
    case 10083: 
    case 10091: 
      return false;
    }
    return true;
  }
  
  public void maybeSetMinimum()
  {
    if (this.minimum < this.knowledge)
    {
      this.minimum = this.knowledge;
      try
      {
        save();
      }
      catch (IOException iox)
      {
        logger.log(Level.INFO, "Failed to save skill " + this.id, iox);
      }
    }
  }
  
  public static int getTotalAffinityChecks()
  {
    return totalAffinityChecks;
  }
  
  public static int getTotalAffinitiesGiven()
  {
    return totalAffinitiesGiven;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\skills\Skill.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */