package com.wurmonline.server.creatures;

import com.wurmonline.math.Vector2f;
import com.wurmonline.math.Vector3f;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.PlonkData;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.bodys.BodyFactory;
import com.wurmonline.server.combat.CombatConstants;
import com.wurmonline.server.creatures.ai.CreatureAI;
import com.wurmonline.server.creatures.ai.CreatureAIData;
import com.wurmonline.server.creatures.ai.Order;
import com.wurmonline.server.creatures.ai.Path;
import com.wurmonline.server.effects.Effect;
import com.wurmonline.server.effects.EffectFactory;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.Trade;
import com.wurmonline.server.modifiers.DoubleValueModifier;
import com.wurmonline.server.modifiers.ModifierTypes;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.players.SpellResistance;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.tutorial.PlayerTutorial;
import com.wurmonline.server.tutorial.PlayerTutorial.PlayerTrigger;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CreatureTypes;
import com.wurmonline.shared.constants.ProtoConstants;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public abstract class CreatureStatus
  implements MiscConstants, CombatConstants, TimeConstants, ModifierTypes, ProtoConstants, CreatureTypes
{
  Creature statusHolder;
  private static Logger logger = Logger.getLogger(CreatureStatus.class.getName());
  private static final int FAT_INCREASE_LEVEL = 120;
  private static final int FAT_DECREASE_LEVEL = 1;
  private CreaturePos position = null;
  private byte diseaseCounter = 0;
  boolean moving = false;
  private boolean unconscious = false;
  public boolean visible = true;
  private Trade trade = null;
  CreatureTemplate template;
  byte sex = 0;
  long inventoryId = -10L;
  long bodyId = -10L;
  Body body;
  int thirst;
  int hunger;
  int stamina = 65535;
  float nutrition = 0.0F;
  private static int DAILY_CALORIES = 2000;
  private static int DAILY_CARBS = 300;
  private static int DAILY_FATS = 80;
  private static int DAILY_PROTEINS = 50;
  private static float CCFP_MAX_PERCENTAGE = 1.0F;
  private static float CCFP_REDUCE_AMOUNT = 3.3333334E-5F;
  float calories = 0.25F;
  float carbs = 0.25F;
  float fats = 0.25F;
  float proteins = 0.25F;
  public int damage;
  long buildingId = -10L;
  private int lastSentThirst = 0;
  private int lastSentHunger = 0;
  public int lastSentStamina = 0;
  private int lastSentDamage = 0;
  private boolean normalRegen = true;
  private float stunned = 0.0F;
  private Set<DoubleValueModifier> modifiers = null;
  private Path path = null;
  public static final int MOVE_MOD_LIMIT = 2000;
  private final DoubleValueModifier moveMod = new DoubleValueModifier(-0.5D);
  public byte kingdom = 0;
  boolean dead = false;
  public long lastPolledAge = 0L;
  public int age = 0;
  public byte fat = 50;
  private int fatCounter = Server.rand.nextInt(1000);
  public SpellEffects spellEffects = null;
  boolean reborn = false;
  public float loyalty = 0.0F;
  long lastPolledLoyalty = 0L;
  boolean stealth = false;
  boolean offline = false;
  boolean stayOnline = false;
  protected int detectInvisCounter = 0;
  byte modtype = 0;
  private float lastDamPercSent = -1.0F;
  protected long traits = 0L;
  protected BitSet traitbits = new BitSet(64);
  protected long mother = -10L;
  protected long father = -10L;
  public byte disease = 0;
  protected long lastGroomed = System.currentTimeMillis();
  private boolean statusExists = false;
  private boolean changed = false;
  
  CreatureStatus(Creature creature, float posX, float posY, float aRot, int aLayer)
    throws Exception
  {
    this.statusHolder = creature;
    this.template = creature.template;
    if (this.template != null) {
      this.body = BodyFactory.getBody(creature, this.template.getBodyType(), this.template.getCentimetersHigh(), this.template
        .getCentimetersLong(), this.template.getCentimetersWide());
    }
    setPosition(CreaturePos.getPosition(creature.getWurmId()));
    if (getPosition() == null)
    {
      int zid = 0;
      try
      {
        zid = Zones.getZoneIdFor((int)posX >> 2, (int)posY >> 2, aLayer >= 0);
      }
      catch (NoSuchZoneException ex)
      {
        logger.log(Level.INFO, this.statusHolder.getWurmId() + "," + this.statusHolder.getName() + ": " + ex.getMessage(), ex);
      }
      setPosition(new CreaturePos(creature.getWurmId(), posX, posY, 0.0F, aRot, zid, aLayer, -10L, true));
    }
    if (this.template != null)
    {
      getPosition().setPosZ(Zones.calculateHeight(getPosition().getPosX(), getPosition().getPosY(), isOnSurface()), false);
      if (isOnSurface()) {
        if (!creature.isSubmerged()) {
          getPosition().setPosZ(Math.max(-1.25F, getPosition().getPosZ()), false);
        } else if (getPosition().getPosZ() < 0.0F) {
          if (creature.isFloating()) {
            getPosition().setPosZ(creature.getTemplate().offZ, false);
          } else {
            getPosition().setPosZ(getPosition().getPosZ() / 2.0F, false);
          }
        }
      }
    }
  }
  
  CreatureStatus() {}
  
  public final void addModifier(DoubleValueModifier modifier)
  {
    if (this.modifiers == null) {
      this.modifiers = new HashSet();
    }
    this.modifiers.add(modifier);
  }
  
  public final void createNewBody()
    throws Exception
  {
    this.body = BodyFactory.getBody(this.statusHolder, this.template.getBodyType(), this.template.getCentimetersHigh(), this.template
      .getCentimetersLong(), this.template.getCentimetersWide());
    this.body.createBodyParts();
    Item bodypart = this.body.getBodyItem();
    this.bodyId = bodypart.getWurmId();
  }
  
  public final void createNewPossessions()
    throws Exception
  {
    this.inventoryId = this.statusHolder.createPossessions();
    setChanged(true);
    logger.log(Level.INFO, "New inventory id for " + this.statusHolder.getName() + " is " + this.inventoryId);
    setInventoryId(this.inventoryId);
  }
  
  public final Path getPath()
  {
    return this.path;
  }
  
  public final void setPath(@Nullable Path newPath)
  {
    if (newPath == null)
    {
      if (this.statusHolder.isDominated()) {
        if (this.statusHolder.hasOrders())
        {
          Order order = this.statusHolder.getFirstOrder();
          if (order.isTile()) {
            if (order.isResolved(this.statusHolder.getCurrentTile().tilex, this.statusHolder.getCurrentTile().tiley, this.statusHolder
              .getLayer())) {
              this.statusHolder.removeOrder(order);
            }
          }
        }
      }
      if (this.path != null) {
        this.path.clear();
      }
      this.statusHolder.pathRecalcLength = 0;
    }
    this.path = newPath;
  }
  
  public final void removeModifier(DoubleValueModifier modifier)
  {
    if (this.modifiers != null) {
      this.modifiers.remove(modifier);
    }
  }
  
  final double getModifierValuesFor(int type)
  {
    double toReturn = 0.0D;
    if (this.modifiers != null) {
      for (DoubleValueModifier val : this.modifiers) {
        if (val.getType() == type) {
          toReturn += val.getModifier();
        }
      }
    }
    return toReturn;
  }
  
  private void increaseFat()
  {
    if (this.fat < 125)
    {
      setChanged(true);
      this.fat = ((byte)Math.min(125, this.fat + 1));
      if (this.statusHolder.isPlayer()) {
        if (this.fat == 125) {
          this.statusHolder.achievement(149);
        }
      }
      if (((this.fat == 120) || (this.fat == 1)) && (!this.statusHolder.isPlayer())) {
        this.statusHolder.refreshVisible();
      }
    }
  }
  
  private int checkDisease()
  {
    int nums = this.statusHolder.getCurrentTile().getCreatures().length;
    if (this.diseaseCounter == 8)
    {
      float villRatio = 10.0F;
      if (this.statusHolder.getCurrentVillage() != null) {
        villRatio = this.statusHolder.getCurrentVillage().getCreatureRatio();
      }
      if ((nums < 3) && ((villRatio >= Village.OPTIMUMCRETRATIO) || 
        (Server.rand.nextInt((int)Math.max(1.0F, Village.OPTIMUMCRETRATIO - villRatio)) == 0)))
      {
        if (this.disease > 0)
        {
          byte mod = 2;
          if (System.currentTimeMillis() - this.lastGroomed < 172800000L) {
            mod = 5;
          }
          if (this.hunger < 10000) {
            mod = (byte)(mod + 5);
          }
          if (this.fat > 100) {
            mod = (byte)(mod + 5);
          }
          if (this.fat < 2) {
            mod = (byte)(int)(mod - 1.0F);
          }
          this.statusHolder.setDisease((byte)Math.max(0, this.disease - mod));
        }
      }
      else if (this.modtype != 11) {
        if ((!this.statusHolder.isKingdomGuard()) && (!this.statusHolder.isSpiritGuard()) && 
          (!this.statusHolder.isUnique()) && (
          (!this.statusHolder.isPlayer()) || (Server.rand.nextInt(4) == 0)))
        {
          float healthMod = 1.0F;
          if (isTraitBitSet(19)) {
            healthMod += 1.0F;
          }
          if (isTraitBitSet(20)) {
            healthMod -= 0.5F;
          }
          if (System.currentTimeMillis() - this.lastGroomed < 172800000L) {
            healthMod -= 0.3F;
          }
          if (this.hunger < 10000) {
            healthMod -= 0.1F;
          } else if (this.hunger > 60000) {
            healthMod += 0.1F;
          }
          if (this.fat > 100) {
            healthMod -= 0.1F;
          }
          if (this.fat < 2) {
            healthMod += 0.1F;
          }
          if (this.disease > 0) {
            this.statusHolder.setDisease((byte)(int)Math.min(120.0F, this.disease + 5 * nums * healthMod));
          } else if ((nums > 2) && (Server.rand.nextInt(100) < nums * healthMod)) {
            this.statusHolder.setDisease((byte)1);
          }
        }
      }
    }
    return nums;
  }
  
  protected void decreaseCCFPValues()
  {
    this.calories = Math.max(this.calories - CCFP_REDUCE_AMOUNT, 0.0F);
    this.carbs = Math.max(this.carbs - CCFP_REDUCE_AMOUNT, 0.0F);
    this.fats = Math.max(this.fats - CCFP_REDUCE_AMOUNT, 0.0F);
    this.proteins = Math.max(this.proteins - CCFP_REDUCE_AMOUNT, 0.0F);
  }
  
  protected void clearCCFPValues()
  {
    this.calories = 0.0F;
    this.carbs = 0.0F;
    this.fats = 0.0F;
    this.proteins = 0.0F;
    sendHunger();
  }
  
  protected void clearHunger()
  {
    this.hunger = 45000;
    sendHunger();
  }
  
  protected void clearThirst()
  {
    this.thirst = 65535;
    sendThirst();
  }
  
  protected boolean decreaseFat()
  {
    int originalfat = this.fat;
    int nums = checkDisease();
    if ((this.fat > 3) && (this.fat < 121))
    {
      byte newfatMod = -2;
      if (nums > 2) {
        newfatMod = (byte)(newfatMod - 2);
      }
      if (System.currentTimeMillis() - this.lastGroomed > 172800000L) {
        newfatMod = (byte)(newfatMod - 3);
      }
      if ((this.modtype != 11) && (this.disease > 0)) {
        newfatMod = (byte)(newfatMod - 2);
      }
      this.fat = ((byte)Math.max(0, this.fat + newfatMod));
      if (this.statusHolder.isPlayer()) {
        if (this.fat == 0) {
          this.statusHolder.achievement(148);
        }
      }
      this.statusHolder.achievement(147);
    }
    else
    {
      this.fat = ((byte)Math.max(0, this.fat - 1));
      if (this.statusHolder.isPlayer()) {
        if (this.fat == 0) {
          this.statusHolder.achievement(148);
        }
      }
    }
    if (((this.fat == 120) || (this.fat == 1)) && (!this.statusHolder.isPlayer())) {
      this.statusHolder.refreshVisible();
    }
    if (originalfat != this.fat) {
      setChanged(true);
    }
    if (this.fat == 0) {
      return false;
    }
    return true;
  }
  
  public final boolean pollFat()
  {
    if (Server.rand.nextBoolean()) {
      this.fatCounter += 1;
    }
    if (this.fatCounter > (this.statusHolder.isPlayer() ? 50 : 1500))
    {
      this.diseaseCounter = ((byte)(this.diseaseCounter + 1));
      
      this.fatCounter = 0;
      if (this.hunger < 10000) {
        if (this.nutrition > (this.statusHolder.isPlayer() ? 0.4F : 0.1F))
        {
          increaseFat();
          checkDisease();
          break label262;
        }
      }
      if (this.hunger >= 60000)
      {
        decreaseFat();
        if (!this.statusHolder.isPlayer())
        {
          if (((this.statusHolder.isHerbivore()) || (this.statusHolder.isOmnivore())) && (!this.statusHolder.isUnique()))
          {
            if ((this.fat < 1) && (!this.statusHolder.isInvulnerable())) {
              return true;
            }
            this.hunger = 10000;
          }
          else if (Server.rand.nextInt(10) == 0)
          {
            this.hunger = 10000;
          }
        }
        else if (this.fat > 0)
        {
          this.statusHolder.getCommunicator().sendNormalServerMessage("Your hunger goes away as you fast.");
          this.nutrition = Math.max(0.1F, this.nutrition - 0.2F);
          this.hunger = 10000;
        }
      }
      else
      {
        checkDisease();
      }
      label262:
      if ((this.diseaseCounter == 8) && (this.disease > 50))
      {
        boolean canSpread = (this.statusHolder.getHitched() == null) || (!this.statusHolder.isMoving());
        if (canSpread) {
          this.statusHolder.getCurrentTile().checkDiseaseSpread();
        }
        if ((this.disease > 100) && (Server.rand.nextBoolean()) && (!this.statusHolder.isInvulnerable())) {
          return true;
        }
      }
      if (this.diseaseCounter == 8) {
        this.diseaseCounter = 0;
      }
    }
    return false;
  }
  
  final String getFatString()
  {
    if ((this.disease > 0) && (this.modtype != 11)) {
      return "diseased ";
    }
    if (this.statusHolder.isNeedFood())
    {
      if (this.fat <= 1) {
        return "starving ";
      }
      if (this.fat > 120) {
        return "fat ";
      }
    }
    return "";
  }
  
  public final String getBodyType()
  {
    double strength = this.statusHolder.getStrengthSkill();
    if (strength < 21.0D)
    {
      if (this.fat < 10) {
        return this.statusHolder.getHeSheItString() + " is only skin and bones.";
      }
      if (this.fat < 30) {
        return this.statusHolder.getHeSheItString() + " is very thin.";
      }
      if (this.fat > 100) {
        return this.statusHolder.getHeSheItString() + " is extremely well nourished.";
      }
      if (this.fat > 70) {
        return this.statusHolder.getHeSheItString() + " is a bit round.";
      }
      return this.statusHolder.getHeSheItString() + " has a normal build.";
    }
    if (strength < 25.0D)
    {
      if (this.fat < 10) {
        return this.statusHolder.getHeSheItString() + " has muscles, but looks a bit undernourished.";
      }
      if (this.fat < 30) {
        return this.statusHolder.getHeSheItString() + " is strong but lacks body fat.";
      }
      if (this.fat > 100) {
        return this.statusHolder.getHeSheItString() + " is strong and has a good reserve of fat.";
      }
      if (this.fat > 70) {
        return this.statusHolder.getHeSheItString() + " is strong and well nourished.";
      }
      return this.statusHolder.getHeSheItString() + " is well defined.";
    }
    if (this.fat < 10) {
      return this.statusHolder.getHeSheItString() + " has large muscles, but looks a bit undernourished.";
    }
    if (this.fat < 30) {
      return this.statusHolder.getHeSheItString() + " looks very strong, but lacks body fat.";
    }
    if (this.fat > 100) {
      return this.statusHolder.getHeSheItString() + " is very strong and has a good reserve of fat.";
    }
    if (this.fat > 70) {
      return this.statusHolder.getHeSheItString() + " is very strong and well nourished.";
    }
    return this.statusHolder.getHeSheItString() + " is very well defined.";
  }
  
  public final void setLayer(int aLayer)
  {
    getPosition().setLayer(aLayer);
  }
  
  public final boolean isOnSurface()
  {
    return getPosition().getLayer() >= 0;
  }
  
  public final CreatureTemplate getTemplate()
  {
    return this.template;
  }
  
  public final byte getSex()
  {
    return this.sex;
  }
  
  public final void setBuildingId(long id)
  {
    this.buildingId = id;
  }
  
  public final long getBuildingId()
  {
    return this.buildingId;
  }
  
  public final Body getBody()
  {
    return this.body;
  }
  
  public final void setSex(byte aSex)
  {
    this.sex = aSex;
    setChanged(true);
  }
  
  public final void setPositionX(float pos)
  {
    getPosition().setPosX(pos);
    this.statusHolder.updateEffects();
  }
  
  public final void setPositionY(float pos)
  {
    getPosition().setPosY(pos);
    this.statusHolder.updateEffects();
  }
  
  public final void setPositionZ(float pos)
  {
    setPositionZ(pos, false);
    this.statusHolder.updateEffects();
  }
  
  public final void setPositionZ(float pos, boolean force)
  {
    getPosition().setPosZ(pos, force);
    this.statusHolder.updateEffects();
  }
  
  public final void setPositionXYZ(float posX, float posY, float posZ)
  {
    getPosition().setPosX(posX);
    getPosition().setPosY(posY);
    getPosition().setPosZ(posZ, false);
    this.statusHolder.updateEffects();
  }
  
  public final void setRotation(float r)
  {
    getPosition().setRotation(Creature.normalizeAngle(r));
  }
  
  public final boolean isMoving()
  {
    return this.moving;
  }
  
  public final void setMoving(boolean aMoving)
  {
    this.moving = aMoving;
    if (aMoving) {
      PlayerTutorial.firePlayerTrigger(this.statusHolder.getWurmId(), PlayerTutorial.PlayerTrigger.MOVED_PLAYER);
    }
  }
  
  public final boolean hasNormalRegen()
  {
    return this.normalRegen;
  }
  
  public final void setNormalRegen(boolean nregen)
  {
    this.normalRegen = nregen;
  }
  
  public final void setUnconscious(boolean aUnconscious)
  {
    this.unconscious = aUnconscious;
  }
  
  public final boolean isUnconscious()
  {
    return this.unconscious;
  }
  
  public final Vector2f getPosition2f()
  {
    return getPosition().getPos2f();
  }
  
  public final Vector3f getPosition3f()
  {
    return getPosition().getPos3f();
  }
  
  public final float getPositionX()
  {
    return getPosition().getPosX();
  }
  
  public final float getPositionY()
  {
    return getPosition().getPosY();
  }
  
  public final long getInventoryId()
  {
    return this.inventoryId;
  }
  
  public final float getPositionZ()
  {
    return getPosition().getPosZ();
  }
  
  public final float getRotation()
  {
    return getPosition().getRotation();
  }
  
  public final long getBridgeId()
  {
    return getPosition().getBridgeId();
  }
  
  public final byte getDir()
  {
    return (byte)(((int)getRotation() + 45) / 90 * 2 % 8);
  }
  
  public final long getBodyId()
  {
    return this.bodyId;
  }
  
  public final float getStunned()
  {
    return this.stunned;
  }
  
  public final void setStunned(float stunTime)
  {
    setStunned(stunTime, true);
  }
  
  public final void setStunned(float stunTime, boolean applyResistance)
  {
    if ((this.stunned > 0.0F) && (stunTime <= 0.0F))
    {
      this.statusHolder.getCombatHandler().setCurrentStance(-1, (byte)0);
      this.statusHolder.getMovementScheme().setFightMoveMod(false);
      if (!this.statusHolder.isDead()) {
        Server.getInstance().broadCastAction(this.statusHolder
          .getNameWithGenus() + " regains " + this.statusHolder.getHisHerItsString() + " bearings.", this.statusHolder, 2, true);
      }
      this.statusHolder.getCommunicator().sendStunned(false);
      this.statusHolder.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.STUNNED);
      this.stunned = Math.max(0.0F, stunTime);
    }
    else if ((this.stunned <= 0.0F) && (stunTime > 0.0F))
    {
      SpellResistance currSpellRes = null;
      if (applyResistance) {
        currSpellRes = this.statusHolder.getSpellResistance((short)SpellEffectsEnum.STUNNED.getTypeId());
      }
      float successChance = 1.0F;
      if ((applyResistance) && (this.statusHolder.isPlayer())) {
        if (currSpellRes == null)
        {
          this.statusHolder.addSpellResistance((short)SpellEffectsEnum.STUNNED.getTypeId());
          currSpellRes = this.statusHolder.getSpellResistance((short)SpellEffectsEnum.STUNNED.getTypeId());
          if (currSpellRes != null) {
            currSpellRes.setResistance(0.5F, 0.023F);
          }
        }
        else
        {
          successChance = 1.0F - currSpellRes.getResistance();
        }
      }
      if ((Server.rand.nextFloat() < successChance) || (!applyResistance))
      {
        this.statusHolder.getCombatHandler().setCurrentStance(-1, (byte)8);
        this.statusHolder.maybeInterruptAction(200000);
        this.statusHolder.getMovementScheme().setFightMoveMod(true);
        this.statusHolder.getCommunicator().sendStunned(true);
        this.statusHolder.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.STUNNED, (int)stunTime, stunTime);
        this.stunned = Math.max(0.0F, stunTime);
        this.statusHolder.playAnimation("stun", false);
      }
      else
      {
        Server.getInstance().broadCastAction(this.statusHolder.getNameWithGenus() + " brushes away the stunned feeling.", this.statusHolder, 2, true);
      }
    }
    else
    {
      this.stunned = Math.max(0.0F, stunTime);
    }
    sendStateString();
  }
  
  public final boolean modifyWounds(int dam)
  {
    boolean _dead = false;
    if (this.statusHolder.getTemplate().getCreatureAI() != null) {
      dam = this.statusHolder.getTemplate().getCreatureAI().woundDamageChanged(this.statusHolder, dam);
    }
    setChanged(true);
    this.damage += dam;
    if (this.damage >= 65535)
    {
      if (this.statusHolder.getPower() >= 3)
      {
        if (logger.isLoggable(Level.FINE)) {
          logger.fine("Deity with id=" + this.statusHolder.getWurmId() + ", damage has reached or exceeded the maximum so it is time heal. Damage was: " + this.damage + ", creature/player: " + this.statusHolder);
        }
        this.damage = 0;
        this.statusHolder.getBody().healFully();
        this.statusHolder.getCommunicator().sendCombatAlertMessage("You died but were instantly healed.");
      }
      else
      {
        if (logger.isLoggable(Level.FINE)) {
          logger.fine("Creature/Player with id=" + this.statusHolder.getWurmId() + ", damage has reached or exceeded the maximum so it is time to die. Damage: " + this.damage + ", creature/player: " + this.statusHolder);
        }
        this.damage = 65535;
        this.statusHolder.die(false, "Damage");
        _dead = true;
      }
    }
    else if (this.damage < 0) {
      this.damage = 0;
    }
    if (!this.statusHolder.isUnique()) {
      this.stamina = Math.min(this.stamina, 65535 - this.damage);
    }
    sendStamina();
    
    return _dead;
  }
  
  public final void removeWounds()
  {
    this.damage = 0;
    sendStamina();
  }
  
  protected final void setTraitBits(long bits)
  {
    for (int x = 0; x < 64; x++) {
      if ((x != 28) || (this.statusHolder.getName().contains("traitor")))
      {
        if ((x == 28) && (this.statusHolder.getName().contains("traitor")))
        {
          Effect traitorEffect = EffectFactory.getInstance().createGenericEffect(this.statusHolder.getWurmId(), "traitor", this.statusHolder
            .getPosX(), this.statusHolder.getPosY(), this.statusHolder.getPositionZ() + this.statusHolder.getHalfHeightDecimeters() / 10.0F, this.statusHolder
            .isOnSurface(), -1.0F, this.statusHolder.getStatus().getRotation());
          this.statusHolder.addEffect(traitorEffect);
        }
        if (x == 0)
        {
          if ((bits & 1L) == 1L) {
            this.traitbits.set(x, true);
          } else {
            this.traitbits.set(x, false);
          }
        }
        else if ((bits >> x & 1L) == 1L) {
          this.traitbits.set(x, true);
        } else {
          this.traitbits.set(x, false);
        }
      }
    }
  }
  
  protected final long getTraitBits()
  {
    long ret = 0L;
    for (int x = 0; x < 64; x++) {
      if (this.traitbits.get(x)) {
        ret += (1L << x);
      }
    }
    return ret;
  }
  
  public final int traitsCount()
  {
    int cnt = 0;
    for (int x = 0; x < 64; x++) {
      if ((this.traitbits.get(x)) && (Traits.getTraitString(x).length() > 0)) {
        cnt++;
      }
    }
    return cnt;
  }
  
  public final boolean isTraitBitSet(int setting)
  {
    return this.traitbits.get(setting);
  }
  
  protected final boolean removeRandomNegativeTrait()
  {
    if (this.traits == 0L) {
      return false;
    }
    for (int x = 0; x < 64; x++) {
      if ((Traits.isTraitNegative(x)) && (isTraitBitSet(x)))
      {
        setTraitBit(x, false);
        return true;
      }
    }
    return false;
  }
  
  public final void setTraitBit(int setting, boolean value)
  {
    this.traitbits.set(setting, value);
    this.traits = getTraitBits();
    try
    {
      setInheritance(this.traits, this.mother, this.father);
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, iox.getMessage());
    }
  }
  
  public final float getStaminaSkill()
  {
    try
    {
      return (float)this.statusHolder.getSkills().getSkill(103).getKnowledge(0.0D);
    }
    catch (NoSuchSkillException nss)
    {
      logger.log(Level.WARNING, "Creature has no stamina :" + this.statusHolder.getName() + "," + nss.getMessage(), nss);
      this.statusHolder.getSkills().learn(103, 20.0F);
    }
    return 20.0F;
  }
  
  public final void resetCreatureStamina()
  {
    int currMax = 65535 - this.damage;
    int oldStamina = this.stamina;
    this.stamina = currMax;
    if (this.stamina != oldStamina) {
      setChanged(true);
    }
    checkStaminaEffects(oldStamina);
  }
  
  public final void modifyStamina(float staminaPoints)
  {
    if ((!this.statusHolder.isUnique()) || (this.statusHolder.getPower() >= 4))
    {
      if (((staminaPoints > 0.0F) && (this.stamina < 65535)) || ((staminaPoints < 0.0F) && (this.stamina > 1)))
      {
        float staminaMod = getStaminaSkill() / 100.0F;
        int currMax = 65535 - this.damage;
        int oldStamina = this.stamina;
        if (staminaPoints > 0.0F)
        {
          this.stamina = ((int)(this.stamina + staminaPoints * Math.max(0.0D, 1.0F + staminaMod + getModifierValuesFor(1))));
        }
        else if (staminaPoints < 0.0F)
        {
          if (this.hunger < 10000) {
            staminaMod = (float)(staminaMod + 0.05D);
          }
          if ((this.statusHolder.getCultist() != null) && (this.statusHolder.getCultist().usesNoStamina())) {
            staminaMod += 0.3F;
          }
          float caloriesModifier = 1.0F / (1.0F + Math.min(this.calories, 1.0F) / 3.0F);
          staminaMod = (1.0F - staminaMod) * caloriesModifier;
          this.stamina = ((int)(this.stamina + Math.min(staminaPoints * staminaMod * ItemBonus.getStaminaReductionBonus(this.statusHolder), staminaPoints * 0.01F)));
        }
        this.stamina = Math.max(1, this.stamina);
        this.stamina = Math.min(this.stamina, currMax);
        if (crossedStatusBorder(oldStamina, this.stamina)) {
          sendStateString();
        }
        if (oldStamina != this.stamina) {
          setChanged(true);
        }
        sendStamina();
        checkStaminaEffects(oldStamina);
        if (staminaPoints < 0.0F) {
          modifyThirst(Math.max(1.0F, -staminaPoints / 1000.0F));
        }
      }
    }
    else
    {
      if (this.stamina != 65535) {
        setChanged(true);
      }
      this.stamina = 65535;
    }
  }
  
  public final void modifyStamina2(float staminaPercent)
  {
    if (!this.statusHolder.isUnique())
    {
      int currMax = 65535 - this.damage;
      int oldStamina = this.stamina;
      this.stamina = ((int)(this.stamina + staminaPercent * currMax));
      this.stamina = Math.max(1, this.stamina);
      this.stamina = Math.min(this.stamina, currMax);
      if (crossedStatusBorder(oldStamina, this.stamina)) {
        sendStateString();
      }
      if (oldStamina != this.stamina) {
        setChanged(true);
      }
      sendStamina();
      checkStaminaEffects(oldStamina);
    }
    else
    {
      if (this.stamina != 65535) {
        setChanged(true);
      }
      this.stamina = 65535;
    }
  }
  
  public final void checkStaminaEffects(int oldStamina)
  {
    if ((this.stamina < 2000) && (oldStamina >= 2000)) {
      this.statusHolder.getMovementScheme().addModifier(this.moveMod);
    } else if ((this.stamina >= 2000) && (oldStamina < 2000)) {
      this.statusHolder.getMovementScheme().removeModifier(this.moveMod);
    }
  }
  
  public final void sendStamina()
  {
    if ((this.stamina > this.lastSentStamina + 100) || (this.stamina < this.lastSentStamina - 100) || (this.damage > this.lastSentDamage + 100) || (this.damage < this.lastSentDamage - 100))
    {
      this.lastSentStamina = this.stamina;
      this.lastSentDamage = this.damage;
      
      this.statusHolder.getCommunicator().sendStamina(this.stamina, this.damage);
      
      float damp = calcDamPercent();
      if (damp != this.lastDamPercSent)
      {
        this.statusHolder.sendDamage(damp);
        this.lastDamPercSent = damp;
      }
      if ((this.statusHolder.isPlayer()) && (damp < 90.0F)) {
        PlonkData.FIRST_DAMAGE.trigger(this.statusHolder);
      }
      if ((this.statusHolder.isPlayer()) && (calcStaminaPercent() < 30.0F)) {
        PlonkData.LOW_STAMINA.trigger(this.statusHolder);
      }
    }
  }
  
  public final float calcDamPercent()
  {
    if (this.damage == 0) {
      return 100.0F;
    }
    return Math.max(1.0F, (65535 - this.damage) / 65535.0F * 100.0F);
  }
  
  public final float calcStaminaPercent()
  {
    if (this.stamina == 65535) {
      return 100.0F;
    }
    return this.stamina / 65535.0F * 100.0F;
  }
  
  public final int modifyHunger(int hungerModification, float newNutritionLevel)
  {
    return modifyHunger(hungerModification, newNutritionLevel, -1.0F, -1.0F, -1.0F, -1.0F);
  }
  
  public final int modifyHunger(int hungerModification, float newNutritionLevel, float addCalories, float addCarbs, float addFats, float addProteins)
  {
    int oldHunger = this.hunger;
    
    int localHungerModification = hungerModification;
    if (localHungerModification > 0) {
      localHungerModification = (int)(localHungerModification * (1.0F - Math.min(this.proteins, 1.0F) / 3.0F));
    }
    this.hunger = Math.min(65535, Math.max(1, this.hunger + localHungerModification));
    if (this.hunger < 65535)
    {
      int realHungerModification = this.hunger - oldHunger;
      if (realHungerModification < 0)
      {
        newNutritionLevel = Math.min(newNutritionLevel * 1.1F, 0.99F);
        if (this.nutrition > 0.0F)
        {
          float oldNutPercent = Math.max(1.0F, 65535 - oldHunger) / Math.max(1.0F, 65535 - this.hunger);
          float newNutPercent = -realHungerModification / Math.max(1.0F, 65535 - this.hunger);
          this.nutrition = (oldNutPercent * this.nutrition + newNutPercent * newNutritionLevel);
        }
        else
        {
          this.nutrition = newNutritionLevel;
        }
      }
      if (addCalories >= 0.0F) {
        addToCCFPValues(addCalories, addCarbs, addFats, addProteins);
      }
    }
    else
    {
      this.nutrition = 0.0F;
    }
    if (crossedStatusBorder(oldHunger, this.hunger)) {
      sendStateString();
    }
    if ((this.hunger < this.lastSentHunger - 100) || (this.hunger > this.lastSentHunger + 100))
    {
      this.lastSentHunger = this.hunger;
      sendHunger();
      if (this.statusHolder.isPlayer()) {
        if (PlonkData.HUNGRY.hasSeenThis(this.statusHolder))
        {
          float hungerPercent = 100.0F - 100.0F * (this.hunger / 65535.0F);
          if (hungerPercent <= 50.0F) {
            PlonkData.HUNGRY.trigger(this.statusHolder);
          }
        }
      }
    }
    return this.hunger;
  }
  
  void addToCCFPValues(float addCalories, float addCarbs, float addFats, float addProteins)
  {
    this.calories = Math.min(this.calories + addCalories / DAILY_CALORIES, CCFP_MAX_PERCENTAGE);
    this.carbs = Math.min(this.carbs + addCarbs / DAILY_CARBS, CCFP_MAX_PERCENTAGE);
    this.fats = Math.min(this.fats + addFats / DAILY_FATS, CCFP_MAX_PERCENTAGE);
    this.proteins = Math.min(this.proteins + addProteins / DAILY_PROTEINS, CCFP_MAX_PERCENTAGE);
  }
  
  public final boolean refresh(float newNutrition, boolean fullStamina)
  {
    this.hunger = 0;
    this.thirst = 0;
    if (fullStamina)
    {
      int oldStam = this.stamina;
      
      this.nutrition = 0.99F;
      this.stamina = 65535;
      setChanged(true);
      sendStamina();
      sendStateString();
      checkStaminaEffects(oldStam);
      return true;
    }
    if (this.nutrition < 0.5F)
    {
      setChanged(true);
      
      this.nutrition = 0.5F;
      sendStateString();
      return true;
    }
    return false;
  }
  
  public void setMaxCCFP()
  {
    this.calories = CCFP_MAX_PERCENTAGE;
    this.carbs = CCFP_MAX_PERCENTAGE;
    this.fats = CCFP_MAX_PERCENTAGE;
    this.proteins = CCFP_MAX_PERCENTAGE;
  }
  
  private boolean crossedStatusBorder(int oldnumber, int newnumber)
  {
    if ((oldnumber >= 65535) && (newnumber < 65535)) {
      return true;
    }
    if ((oldnumber > 60000) && (newnumber <= 60000)) {
      return true;
    }
    if ((oldnumber > 45000) && (newnumber <= 45000)) {
      return true;
    }
    if ((oldnumber > 20000) && (newnumber <= 20000)) {
      return true;
    }
    if ((oldnumber > 10000) && (newnumber <= 10000)) {
      return true;
    }
    if ((oldnumber > 1000) && (newnumber <= 1000)) {
      return true;
    }
    if ((oldnumber > 1) && (newnumber <= 1)) {
      return true;
    }
    if ((oldnumber < 1) && (newnumber >= 1)) {
      return true;
    }
    if ((oldnumber < 1000) && (newnumber >= 1000)) {
      return true;
    }
    if ((oldnumber < 10000) && (newnumber >= 10000)) {
      return true;
    }
    if ((oldnumber < 20000) && (newnumber >= 20000)) {
      return true;
    }
    if ((oldnumber < 45000) && (newnumber >= 45000)) {
      return true;
    }
    if ((oldnumber < 60000) && (newnumber >= 60000)) {
      return true;
    }
    if ((oldnumber < 65535) && (newnumber >= 65535)) {
      return true;
    }
    return false;
  }
  
  public float getCaloriesAsPercent()
  {
    return Math.min(this.calories * 100.0F, 100.0F);
  }
  
  public float getCarbsAsPercent()
  {
    return Math.min(this.carbs * 100.0F, 100.0F);
  }
  
  public float getFatsAsPercent()
  {
    return Math.min(this.fats * 100.0F, 100.0F);
  }
  
  public float getProteinsAsPercent()
  {
    return Math.min(this.proteins * 100.0F, 100.0F);
  }
  
  public final void sendHunger()
  {
    this.statusHolder.getCommunicator().sendHunger(this.hunger, this.nutrition, 
      getCaloriesAsPercent(), getCarbsAsPercent(), getFatsAsPercent(), getProteinsAsPercent());
  }
  
  public final int modifyThirst(float thirstModification)
  {
    return modifyThirst(thirstModification, -1.0F, -1.0F, -1.0F, 1.0F);
  }
  
  public final int modifyThirst(float thirstModification, float addCalories, float addCarbs, float addFats, float addProteins)
  {
    int oldThirst = this.thirst;
    float realThirstModification = thirstModification;
    if (realThirstModification > 0.0F) {
      realThirstModification *= (1.0F - Math.min(this.carbs, 1.0F) / 3.0F);
    }
    this.thirst = ((int)(this.thirst + realThirstModification));
    this.thirst = Math.max(1, this.thirst);
    this.thirst = Math.min(65535, this.thirst);
    if (crossedStatusBorder(oldThirst, this.thirst)) {
      sendStateString();
    }
    if ((this.thirst < this.lastSentThirst - 100) || (this.thirst > this.lastSentThirst + 100))
    {
      this.lastSentThirst = this.thirst;
      sendThirst();
      if (!PlonkData.THIRSTY.hasSeenThis(this.statusHolder))
      {
        float thirstPercent = 100.0F - 100.0F * (this.thirst / 65535.0F);
        if (thirstPercent <= 50.0F) {
          PlonkData.THIRSTY.trigger(this.statusHolder);
        }
      }
    }
    if (addCalories >= 0.0F) {
      addToCCFPValues(addCalories, addCarbs, addFats, addProteins);
    }
    return this.thirst;
  }
  
  public final void sendThirst()
  {
    this.statusHolder.getCommunicator().sendThirst(this.thirst);
  }
  
  public final int getHunger()
  {
    return this.hunger;
  }
  
  public final float getNutritionlevel()
  {
    return this.nutrition;
  }
  
  public final float getCalories()
  {
    return this.calories;
  }
  
  public final float getCarbs()
  {
    return this.carbs;
  }
  
  public final float getFats()
  {
    return this.fats;
  }
  
  public final float getProteins()
  {
    return this.proteins;
  }
  
  public final int getThirst()
  {
    return this.thirst;
  }
  
  public final int getStamina()
  {
    return this.stamina;
  }
  
  final void setTrade(@Nullable Trade _trade)
  {
    this.trade = _trade;
  }
  
  public final Trade getTrade()
  {
    return this.trade;
  }
  
  public final boolean isTrading()
  {
    return this.trade != null;
  }
  
  public final void sendStateString()
  {
    if (this.statusHolder.isPlayer())
    {
      List<String> strings = new LinkedList();
      if (this.stunned > 0.0F) {
        strings.add("Stunned");
      }
      String agg = getAggressiveness();
      if (agg.length() > 0) {
        strings.add(agg);
      }
      if (!this.visible) {
        if (this.statusHolder.getPower() > 0) {
          strings.add("Invisible");
        }
      }
      if (this.disease > 0) {
        strings.add("Diseased");
      }
      if (this.statusHolder.opponent != null) {
        strings.add("Opponent:" + this.statusHolder.opponent.getName());
      }
      if (this.statusHolder.getTarget() != null) {
        strings.add("Target:" + this.statusHolder.getTarget().getName());
      }
      if (this.detectInvisCounter > 0) {
        strings.add("Alerted");
      }
      if (this.stealth) {
        strings.add("Stealthmode");
      }
      if (this.statusHolder.getCRCounterBonus() > 0) {
        strings.add("Sharp");
      }
      if (this.statusHolder.isDead()) {
        strings.add("Dead");
      }
      if (this.statusHolder.damageCounter > 0) {
        strings.add("Hurting");
      }
      if (this.statusHolder.getFarwalkerSeconds() > 0) {
        strings.add("Unstoppable");
      }
      if (this.statusHolder.linkedTo != -10L) {
        try
        {
          Creature c = Server.getInstance().getCreature(this.statusHolder.linkedTo);
          strings.add("Link: " + c.getName());
        }
        catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
      }
      StringBuilder stbuf = new StringBuilder();
      for (ListIterator<String> it = strings.listIterator(); it.hasNext();)
      {
        String next = (String)it.next();
        it.remove();
        stbuf.append(next);
        if (strings.size() > 0) {
          stbuf.append(", ");
        }
      }
      this.statusHolder.getCommunicator().sendStatus(stbuf.toString());
    }
  }
  
  public final boolean canEat()
  {
    return this.hunger >= 10000;
  }
  
  public final boolean isHungry()
  {
    return this.hunger >= 45000;
  }
  
  private String getAggressiveness()
  {
    byte fightStyle = this.statusHolder.getFightStyle();
    if (fightStyle == 1) {
      return "Aggressive";
    }
    if (fightStyle == 2) {
      return "Defensive";
    }
    return "";
  }
  
  final boolean pollAge(int maxAge)
  {
    boolean rebornPoll = false;
    if ((this.reborn) && (this.mother == -10L)) {
      if (WurmCalendar.currentTime - this.lastPolledAge > 604800L) {
        rebornPoll = true;
      }
    }
    boolean fasterGrowth = (this.statusHolder.getTemplate().getTemplateId() == 65) || (this.statusHolder.getTemplate().getTemplateId() == 117) || (this.statusHolder.getTemplate().getTemplateId() == 118);
    if ((WurmCalendar.currentTime - this.lastPolledAge > ((Servers.localServer.PVPSERVER) && (this.age < 8) && (fasterGrowth) ? 259200L : 2419200L)) || 
      ((isTraitBitSet(29)) && (WurmCalendar.currentTime - this.lastPolledAge > 345600L)) || (rebornPoll))
    {
      if (((this.statusHolder.isGhost()) || (this.statusHolder.isKingdomGuard()) || (this.statusHolder.isUnique())) && ((!this.reborn) || (this.mother == -10L))) {
        this.age = Math.max(this.age, 11);
      }
      int newAge = this.age + 1;
      boolean updated = false;
      if ((!this.statusHolder.isCaredFor()) && (!isTraitBitSet(29)) && ((newAge >= maxAge) || (
        (isTraitBitSet(13)) && (newAge >= Math.max(1, maxAge - Server.rand
        .nextInt(maxAge / 2)))))) {
        return true;
      }
      if (!rebornPoll)
      {
        if (newAge > (isTraitBitSet(29) ? 36 : isTraitBitSet(21) ? 75 : 50)) {
          if ((!this.statusHolder.isGhost()) && (!this.statusHolder.isHuman()) && (!this.statusHolder.isUnique()) && 
            (!this.statusHolder.isCaredFor())) {
            return true;
          }
        }
        if ((newAge - 1 >= 5) && (!this.reborn)) {
          if ((getTemplate().getAdultMaleTemplateId() > -1) || 
            (getTemplate().getAdultFemaleTemplateId() > -1))
          {
            int newtemplateId = getTemplate().getAdultMaleTemplateId();
            if ((this.sex == 1) && (getTemplate().getAdultFemaleTemplateId() > -1)) {
              newtemplateId = getTemplate().getAdultFemaleTemplateId();
            }
            if (newtemplateId != getTemplate().getTemplateId())
            {
              newAge = 1;
              try
              {
                updateAge(newAge);
                updated = true;
              }
              catch (IOException iox)
              {
                logger.log(Level.WARNING, iox.getMessage(), iox);
              }
              try
              {
                setChanged(true);
                CreatureTemplate newTemplate = CreatureTemplateFactory.getInstance().getTemplate(newtemplateId);
                
                this.template = newTemplate;
                this.statusHolder.template = this.template;
                if (!this.statusHolder.isNpc())
                {
                  if ((this.statusHolder.getMother() == -10L) || ((!this.statusHolder.isHorse()) && (!this.statusHolder.isUnicorn()) && (!this.reborn))) {
                    try
                    {
                      if (this.statusHolder.getName().endsWith("traitor")) {
                        this.statusHolder.setName(this.template.getName() + " traitor");
                      } else {
                        this.statusHolder.setName(this.template.getName());
                      }
                    }
                    catch (Exception ex)
                    {
                      logger.log(Level.WARNING, ex.getMessage(), ex);
                    }
                  }
                  if ((!this.reborn) || (this.mother == -10L)) {
                    try
                    {
                      this.statusHolder.skills.delete();
                      this.statusHolder.skills.clone(newTemplate.getSkills().getSkills());
                      this.statusHolder.skills.save();
                    }
                    catch (Exception ex)
                    {
                      logger.log(Level.WARNING, ex.getMessage(), ex);
                    }
                  }
                }
                save();
                this.statusHolder.refreshVisible();
              }
              catch (NoSuchCreatureTemplateException nsc)
              {
                logger.log(Level.WARNING, this.statusHolder
                  .getName() + ", " + this.statusHolder.getWurmId() + ": " + nsc.getMessage(), nsc);
              }
              catch (IOException iox)
              {
                logger.log(Level.WARNING, this.statusHolder
                  .getName() + ", " + this.statusHolder.getWurmId() + ": " + iox.getMessage(), iox);
              }
            }
          }
        }
      }
      if (!updated) {
        try
        {
          updateAge(newAge);
          if ((this.statusHolder.getHitched() != null) && (!this.statusHolder.getHitched().isAnySeatOccupied(false))) {
            if ((!this.statusHolder.isDomestic()) && (getBattleRatingTypeModifier() > 1.2F))
            {
              Server.getInstance().broadCastMessage(this.statusHolder.getName() + " stops dragging a " + 
                Vehicle.getVehicleName(this.statusHolder.getHitched()) + ".", this.statusHolder
                .getTileX(), this.statusHolder.getTileY(), this.statusHolder.isOnSurface(), 5);
              if (this.statusHolder.getHitched().removeDragger(this.statusHolder)) {
                this.statusHolder.setHitched(null, false);
              }
            }
          }
        }
        catch (IOException iox)
        {
          logger.log(Level.WARNING, iox.getMessage(), iox);
        }
      }
    }
    return false;
  }
  
  public final boolean isChampion()
  {
    return this.modtype == 99;
  }
  
  public final String getAgeString()
  {
    if (this.age < 3) {
      return "young";
    }
    if (this.age < 8) {
      return "adolescent";
    }
    if (this.age < 12) {
      return "mature";
    }
    if (this.age < 30) {
      return "aged";
    }
    if (this.age < 40) {
      return "old";
    }
    return "venerable";
  }
  
  final boolean hasCustomColor()
  {
    if (this.modtype > 0)
    {
      switch (this.modtype)
      {
      case 1: 
        return true;
      case 2: 
        return true;
      case 3: 
        return true;
      case 4: 
        return true;
      case 5: 
        return true;
      case 6: 
        return true;
      case 7: 
        return true;
      case 8: 
        return true;
      case 9: 
        return true;
      case 10: 
        return true;
      case 11: 
        return true;
      }
      return false;
    }
    return false;
  }
  
  final byte getColorRed()
  {
    return -1;
  }
  
  final byte getColorGreen()
  {
    return -1;
  }
  
  final byte getColorBlue()
  {
    return -1;
  }
  
  public abstract void setVehicle(long paramLong, byte paramByte);
  
  final float getSizeMod()
  {
    float aiDataModifier = 1.0F;
    if (this.statusHolder.getCreatureAIData() != null) {
      aiDataModifier = this.statusHolder.getCreatureAIData().getSizeModifier();
    }
    float floatToRet = 1.0F;
    if ((!this.statusHolder.isVehicle()) && (this.modtype != 0)) {
      switch (this.modtype)
      {
      case 3: 
        floatToRet = 1.4F;
        break;
      case 4: 
        floatToRet = 2.0F;
        break;
      case 6: 
        floatToRet = 2.0F;
        break;
      case 7: 
        floatToRet = 0.8F;
        break;
      case 8: 
        floatToRet = 0.9F;
        break;
      case 9: 
        floatToRet = 1.5F;
        break;
      case 10: 
        floatToRet = 1.3F;
        break;
      case 99: 
        floatToRet = 3.0F;
        break;
      case -1: 
        floatToRet = 0.5F;
        break;
      case -2: 
        floatToRet = 0.25F;
        break;
      case -3: 
        floatToRet = 0.125F;
      }
    }
    if ((this.statusHolder.getHitched() == null) && (this.statusHolder.getTemplate().getTemplateId() == 82) && 
      (!this.statusHolder.getNameWithoutPrefixes().equalsIgnoreCase(this.statusHolder.getTemplate().getName()))) {
      floatToRet = 2.0F;
    }
    if ((!this.statusHolder.isVehicle()) && (this.statusHolder.hasTrait(28))) {
      floatToRet *= 1.5F;
    }
    return floatToRet * getAgeSizeModifier() * aiDataModifier;
  }
  
  private float getAgeSizeModifier()
  {
    if ((this.statusHolder.isHuman()) || (this.statusHolder.isGhost()) || (this.template.getAdultFemaleTemplateId() > -1) || 
      (this.template.getAdultMaleTemplateId() > -1)) {
      return 1.0F;
    }
    if (this.age < 3) {
      return 0.7F;
    }
    if (this.age < 8) {
      return 0.8F;
    }
    if (this.age < 12) {
      return 0.9F;
    }
    if (this.age < 40) {
      return 1.0F;
    }
    return 0.95F;
  }
  
  final String getTypeString()
  {
    if (this.modtype > 0)
    {
      switch (this.modtype)
      {
      case 1: 
        return "fierce ";
      case 2: 
        return "angry ";
      case 3: 
        return "raging ";
      case 4: 
        return "slow ";
      case 5: 
        return "alert ";
      case 6: 
        return "greenish ";
      case 7: 
        return "lurking ";
      case 8: 
        return "sly ";
      case 9: 
        return "hardened ";
      case 10: 
        return "scared ";
      case 11: 
        return "diseased ";
      case 99: 
        return "champion ";
      }
      return "";
    }
    return "";
  }
  
  public static final byte getModtypeForString(String corpseString)
  {
    if (corpseString.contains(" fierce ")) {
      return 1;
    }
    if (corpseString.contains(" angry ")) {
      return 2;
    }
    if (corpseString.contains(" raging ")) {
      return 3;
    }
    if (corpseString.contains(" slow ")) {
      return 4;
    }
    if (corpseString.contains(" alert ")) {
      return 5;
    }
    if (corpseString.contains(" greenish ")) {
      return 6;
    }
    if (corpseString.contains(" lurking ")) {
      return 7;
    }
    if (corpseString.contains(" sly ")) {
      return 8;
    }
    if (corpseString.contains(" hardened ")) {
      return 9;
    }
    if (corpseString.contains(" scared ")) {
      return 10;
    }
    if (corpseString.contains(" diseased ")) {
      return 11;
    }
    if (corpseString.contains(" champion ")) {
      return 99;
    }
    return 0;
  }
  
  final boolean setStealth(boolean st)
  {
    if (this.stealth != st)
    {
      this.stealth = st;
      sendStateString();
      this.statusHolder.getCommunicator().sendToggle(3, this.stealth);
      return true;
    }
    return false;
  }
  
  final boolean modifyLoyalty(float mod)
  {
    setLoyalty(this.loyalty + mod);
    return this.loyalty <= 0.0F;
  }
  
  final boolean pollLoyalty()
  {
    if (this.loyalty > 0.0F)
    {
      long timeBetweenPolls = 86400000L;
      if ((this.statusHolder.isAggHuman()) && (
        (this.statusHolder.getBaseCombatRating() > 20.0F) || (this.statusHolder.getSoulStrengthVal() > 40.0D))) {
        timeBetweenPolls = 3600000L;
      }
      if (System.currentTimeMillis() - this.lastPolledLoyalty > timeBetweenPolls)
      {
        setLastPolledLoyalty();
        if ((!this.statusHolder.isReborn()) || (this.statusHolder.getMother() != -10L))
        {
          int sstrngth = (int)this.statusHolder.getSoulStrengthVal();
          sstrngth += (isTraitBitSet(11) ? 20 : 0);
          if (Server.rand.nextInt(50) < sstrngth)
          {
            if (logger.isLoggable(Level.FINEST)) {
              logger.finest(this.statusHolder.getName() + " decreasing loyalty (" + this.loyalty + ") by " + 
                Math.min(-5, sstrngth / -5));
            }
            if (modifyLoyalty(Math.min(-5, sstrngth / -5)))
            {
              if (logger.isLoggable(Level.FINER)) {
                logger.finer(this.statusHolder.getName() + " loyalty became " + this.loyalty + ". Turned!");
              }
              return true;
            }
          }
        }
      }
      return false;
    }
    return true;
  }
  
  public final void pollDetectInvis()
  {
    if (this.detectInvisCounter > 0)
    {
      this.detectInvisCounter -= 1;
      if ((this.detectInvisCounter == 0) || (this.detectInvisCounter % 60 == 0)) {
        setDetectionSecs();
      }
      if (this.detectInvisCounter == 0) {
        this.statusHolder.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.DETECT_INVIS);
      }
    }
  }
  
  public final void setDetectInvisCounter(int detectInvisSecs)
  {
    this.detectInvisCounter = detectInvisSecs;
    if (this.statusHolder.isPlayer()) {
      if (this.detectInvisCounter > 0) {
        this.statusHolder.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.DETECT_INVIS, this.detectInvisCounter, 100.0F);
      } else {
        this.statusHolder.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.DETECT_INVIS);
      }
    }
  }
  
  public final int getDetectInvisCounter()
  {
    return this.detectInvisCounter;
  }
  
  public final float getBattleRatingTypeModifier()
  {
    float floatToRet = 1.0F;
    switch (this.modtype)
    {
    case 1: 
      floatToRet = 1.6F;
      break;
    case 3: 
      floatToRet = 2.0F;
      break;
    case 2: 
    case 9: 
      floatToRet = 1.3F;
      break;
    case 4: 
    case 11: 
      floatToRet = 0.5F;
      break;
    case 6: 
      floatToRet = 3.0F;
      break;
    case 99: 
      floatToRet = 6.0F;
      break;
    default: 
      return floatToRet * getAgeBRModifier();
    }
    return floatToRet * getAgeBRModifier();
  }
  
  final float getAgeBRModifier()
  {
    if (this.age < 3) {
      return 0.9F;
    }
    if (this.age < 8) {
      return 1.0F;
    }
    if (this.age < 12) {
      return 1.1F;
    }
    if (this.age < 30) {
      return 1.2F;
    }
    if (this.age < 40) {
      return 1.3F;
    }
    return 1.4F;
  }
  
  final float getMovementTypeModifier()
  {
    float floatToRet = 1.0F;
    switch (this.modtype)
    {
    case 11: 
      floatToRet = 0.5F;
      break;
    case 3: 
      floatToRet = 1.2F;
      break;
    case 2: 
    case 10: 
      floatToRet = 1.1F;
      break;
    case 4: 
      floatToRet = 0.7F;
      break;
    case 5: 
      floatToRet = 1.4F;
      break;
    case 99: 
      floatToRet = 1.4F;
      break;
    default: 
      return Math.min(1.6F, floatToRet * getAgeMoveModifier());
    }
    return floatToRet * getAgeMoveModifier();
  }
  
  private float getAgeMoveModifier()
  {
    if (this.age < 3) {
      return 0.9F;
    }
    if (this.age < 8) {
      return 1.0F;
    }
    if (this.age < 12) {
      return 1.1F;
    }
    if (this.age < 30) {
      return 1.3F;
    }
    if (this.age < 40) {
      return 1.0F;
    }
    return 0.8F;
  }
  
  final float getDamageTypeModifier()
  {
    float floatToRet = 1.0F;
    switch (this.modtype)
    {
    case 6: 
      floatToRet = 2.0F;
      break;
    case 1: 
      floatToRet = 1.1F;
      break;
    case 3: 
      floatToRet = 1.3F;
      break;
    case 2: 
    case 9: 
      floatToRet = 1.2F;
      break;
    case 10: 
    case 11: 
      floatToRet = 0.7F;
      break;
    case 8: 
      floatToRet = 0.5F;
      break;
    case 99: 
      floatToRet = 2.0F;
      break;
    default: 
      return floatToRet * getAgeDamageModifier();
    }
    return floatToRet * getAgeDamageModifier();
  }
  
  private float getAgeDamageModifier()
  {
    if (this.age < 3) {
      return 0.8F;
    }
    if (this.age < 8) {
      return 0.9F;
    }
    if (this.age < 12) {
      return 1.0F;
    }
    if (this.age < 30) {
      return 1.1F;
    }
    if (this.age < 40) {
      return 1.2F;
    }
    return 1.5F;
  }
  
  final float getParryTypeModifier()
  {
    float floatToRet = 1.0F;
    switch (this.modtype)
    {
    case 1: 
      floatToRet = 1.2F;
      break;
    case 3: 
    case 7: 
      floatToRet = 1.5F;
      break;
    case 2: 
      floatToRet = 1.1F;
      break;
    case 10: 
      floatToRet = 0.3F;
      break;
    case 8: 
      floatToRet = 0.1F;
      break;
    case 6: 
    case 9: 
      floatToRet = 0.7F;
      break;
    case 99: 
      floatToRet = 1.2F;
      break;
    default: 
      return floatToRet * getAgeParryModifier();
    }
    return floatToRet * getAgeParryModifier();
  }
  
  private float getAgeParryModifier()
  {
    if (this.age < 3) {
      return 1.2F;
    }
    if (this.age < 8) {
      return 1.1F;
    }
    if (this.age < 12) {
      return 0.8F;
    }
    if (this.age < 30) {
      return 1.0F;
    }
    if (this.age < 40) {
      return 1.2F;
    }
    return 1.3F;
  }
  
  final float getDodgeTypeModifier()
  {
    float floatToRet = 1.0F;
    switch (this.modtype)
    {
    case 1: 
      floatToRet = 0.94F;
      break;
    case 2: 
      floatToRet = 0.97F;
      break;
    case 10: 
      floatToRet = 0.9F;
      break;
    case 6: 
    case 9: 
      floatToRet = 0.95F;
      break;
    case 7: 
      floatToRet = 1.5F;
      break;
    case 5: 
    case 8: 
      floatToRet = 0.85F;
      break;
    case 4: 
    case 11: 
      floatToRet = 1.7F;
      break;
    case 99: 
      floatToRet = 0.9F;
      break;
    default: 
      return floatToRet * getAgeDodgeModifier();
    }
    return floatToRet * getAgeDodgeModifier();
  }
  
  private float getAgeDodgeModifier()
  {
    if (this.age < 3) {
      return 1.0F;
    }
    if (this.age < 8) {
      return 1.1F;
    }
    if (this.age < 12) {
      return 1.2F;
    }
    if (this.age < 30) {
      return 1.1F;
    }
    if (this.age < 40) {
      return 1.4F;
    }
    return 1.5F;
  }
  
  public final float getAggTypeModifier()
  {
    float floatToRet = 1.0F;
    switch (this.modtype)
    {
    case 1: 
    case 6: 
      floatToRet = 1.3F;
      break;
    case 3: 
      floatToRet = 1.5F;
      break;
    case 2: 
    case 7: 
      floatToRet = 1.1F;
      break;
    case 10: 
    case 11: 
      floatToRet = 0.5F;
      break;
    case 4: 
    case 5: 
    case 8: 
    case 9: 
    default: 
      return floatToRet * getAgeAggModifier();
    }
    return floatToRet * getAgeAggModifier();
  }
  
  private float getAgeAggModifier()
  {
    if (this.age < 3) {
      return 0.8F;
    }
    if (this.age < 8) {
      return 1.3F;
    }
    if (this.age < 12) {
      return 1.2F;
    }
    if (this.age < 30) {
      return 1.0F;
    }
    if (this.age < 40) {
      return 0.8F;
    }
    return 0.6F;
  }
  
  public final int getLayer()
  {
    return getPosition().getLayer();
  }
  
  public final int getZoneId()
  {
    return getPosition().getZoneId();
  }
  
  abstract void setLoyalty(float paramFloat);
  
  public abstract void load()
    throws Exception;
  
  public abstract void savePosition(long paramLong, boolean paramBoolean1, int paramInt, boolean paramBoolean2)
    throws IOException;
  
  public abstract boolean save()
    throws IOException;
  
  public abstract void setKingdom(byte paramByte)
    throws IOException;
  
  public abstract void setDead(boolean paramBoolean)
    throws IOException;
  
  abstract void updateAge(int paramInt)
    throws IOException;
  
  abstract void setDominator(long paramLong);
  
  public abstract void setReborn(boolean paramBoolean);
  
  public abstract void setLastPolledLoyalty();
  
  abstract void setOffline(boolean paramBoolean);
  
  abstract boolean setStayOnline(boolean paramBoolean);
  
  abstract void setDetectionSecs();
  
  abstract void setType(byte paramByte);
  
  abstract void updateFat()
    throws IOException;
  
  abstract void setInheritance(long paramLong1, long paramLong2, long paramLong3)
    throws IOException;
  
  public abstract void setInventoryId(long paramLong)
    throws IOException;
  
  abstract void saveCreatureName(String paramString)
    throws IOException;
  
  abstract void setLastGroomed(long paramLong);
  
  abstract void setDisease(byte paramByte);
  
  public boolean isStatusExists()
  {
    return this.statusExists;
  }
  
  public void setStatusExists(boolean aStatusExists)
  {
    this.statusExists = aStatusExists;
  }
  
  public CreaturePos getPosition()
  {
    return this.position;
  }
  
  public void setPosition(CreaturePos aPosition)
  {
    this.position = aPosition;
    if ((aPosition != null) && 
      (aPosition.getRotation() > 1000.0F)) {
      aPosition.setRotation(aPosition.getRotation() % 360.0F);
    }
  }
  
  public boolean isChanged()
  {
    return this.changed;
  }
  
  public void setChanged(boolean aChanged)
  {
    this.changed = aChanged;
  }
  
  public byte getModType()
  {
    return this.modtype;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\creatures\CreatureStatus.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */