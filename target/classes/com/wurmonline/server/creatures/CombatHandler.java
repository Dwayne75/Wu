package com.wurmonline.server.creatures;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.Items;
import com.wurmonline.server.MessageServer;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.combat.ArmourTemplate;
import com.wurmonline.server.combat.ArmourTemplate.ArmourType;
import com.wurmonline.server.combat.Battle;
import com.wurmonline.server.combat.CombatConstants;
import com.wurmonline.server.combat.CombatEngine;
import com.wurmonline.server.combat.CombatMove;
import com.wurmonline.server.combat.SpecialMove;
import com.wurmonline.server.combat.Weapon;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.modifiers.DoubleValueModifier;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Titles.Title;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.spells.EnchantUtil;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.spells.SpellResist;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.utils.CreatureLineSegment;
import com.wurmonline.server.villages.GuardPlan;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.SoundNames;
import com.wurmonline.shared.util.MulticolorLineSegment;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class CombatHandler
  implements MiscConstants, TimeConstants, CombatConstants, SoundNames, CreatureTemplateIds
{
  private static final Logger logger = Logger.getLogger(CombatHandler.class.getName());
  private final Creature creature;
  private boolean addToSkills = false;
  public static final byte[] NO_COMBAT_OPTIONS = new byte[0];
  private List<ActionEntry> moveStack = null;
  private boolean turned = false;
  private byte currentStance = 15;
  private static SpecialMove[] specialmoves = null;
  private byte currentStrength = 1;
  private static final List<ActionEntry> standardDefences = new LinkedList();
  private static boolean hit = false;
  private static boolean miss = true;
  private static boolean crit = false;
  private int usedShieldThisRound = 0;
  private boolean receivedShieldSkill = false;
  private static boolean dead = false;
  private static boolean aiming = false;
  private static float chanceToHit = 0.0F;
  private static double attCheck = 0.0D;
  private static double attBonus = 0.0D;
  private static double defCheck = 0.0D;
  private static double defBonus = 0.0D;
  private static double damage = 0.0D;
  private static byte pos = 0;
  private static byte type = 0;
  private static Item defShield = null;
  private static Item defParryWeapon = null;
  private static Item defLeftWeapon = null;
  private static Skill defPrimWeaponSkill = null;
  private static Skills defenderSkills = null;
  private static String attString = "";
  private static String othersString = "";
  private static final List<ActionEntry> selectStanceList = new LinkedList();
  private static final String prones = "stancerebound";
  private static final String opens = "stanceopen";
  private static final String dodge = "dodge";
  private static final String fight = "fight";
  private static final String strike = "_strike";
  public static final float minShieldDam = 0.01F;
  private static boolean justOpen = false;
  private static double manouvreMod = 0.0D;
  private byte opportunityAttacks = 0;
  public static final float enemyTerritoryMod = 0.7F;
  private static float parryBonus = 1.0F;
  private byte battleratingPenalty = 0;
  private Set<DoubleValueModifier> parryModifiers;
  private Set<DoubleValueModifier> dodgeModifiers;
  private boolean sentAttacks = false;
  private static final float DODGE_MODIFIER = 3.0F;
  private boolean receivedFStyleSkill = false;
  private boolean receivedWeaponSkill = false;
  private boolean receivedSecWeaponSkill = false;
  private Set<Item> secattacks = null;
  private boolean hasSpiritFervor = false;
  private int lastShieldBashed = 0;
  private boolean hasRodEffect = false;
  private static final float poleArmDamageBonus = 1.7F;
  
  static
  {
    standardDefences.add(com.wurmonline.server.behaviours.Actions.actionEntrys['ĺ']);
    standardDefences.add(com.wurmonline.server.behaviours.Actions.actionEntrys['Ļ']);
    standardDefences.add(com.wurmonline.server.behaviours.Actions.actionEntrys['ļ']);
    standardDefences.add(com.wurmonline.server.behaviours.Actions.actionEntrys['Ľ']);
  }
  
  public CombatHandler(Creature _creature)
  {
    this.creature = _creature;
  }
  
  public static void resolveRound()
  {
    Players.getInstance().combatRound();
    Creatures.getInstance().combatRound();
  }
  
  public void shieldBash()
  {
    this.lastShieldBashed = 2;
    this.creature.getCommunicator().sendToggleShield(false);
  }
  
  public boolean mayShieldBash()
  {
    return this.lastShieldBashed <= 0;
  }
  
  public void calcAttacks(boolean newround)
  {
    if ((!this.creature.isDead()) && ((this.moveStack == null) || (newround) || (!this.sentAttacks)))
    {
      if (this.moveStack == null) {
        this.moveStack = new LinkedList();
      } else {
        this.moveStack.clear();
      }
      manouvreMod = this.creature.getMovementScheme().armourMod.getModifier();
      if ((this.creature.opponent != null) && (this.creature.getPrimWeapon() != null))
      {
        float knowl = getCombatKnowledgeSkill();
        if (!this.creature.isPlayer()) {
          knowl += 20.0F;
        }
        if (knowl > 50.0F) {
          this.moveStack.addAll(standardDefences);
        }
        float mycr = this.creature.getCombatHandler().getCombatRating(this.creature.opponent, this.creature.getPrimWeapon(), false);
        
        float oppcr = this.creature.opponent.getCombatHandler().getCombatRating(this.creature, this.creature.opponent
          .getPrimWeapon(), false);
        this.moveStack.addAll(getHighAttacks(this.creature.getPrimWeapon(), this.creature.isAutofight(), this.creature.opponent, mycr, oppcr, knowl));
        
        this.moveStack.addAll(getMidAttacks(this.creature.getPrimWeapon(), this.creature.isAutofight(), this.creature.opponent, mycr, oppcr, knowl));
        
        this.moveStack.addAll(getLowAttacks(this.creature.getPrimWeapon(), this.creature.isAutofight(), this.creature.opponent, mycr, oppcr, knowl));
      }
      if ((!this.sentAttacks) || (newround))
      {
        this.sentAttacks = true;
        if (!this.creature.isAutofight()) {
          this.creature.getCommunicator().sendCombatOptions(getOptions(this.moveStack, this.currentStance), (short)0);
        }
        sendSpecialMoves();
        if (this.creature.getShield() != null)
        {
          if (mayShieldBash()) {
            this.creature.getCommunicator().sendToggleShield(true);
          } else {
            this.creature.getCommunicator().sendToggleShield(false);
          }
        }
        else {
          this.creature.getCommunicator().sendToggleShield(false);
        }
      }
    }
  }
  
  public float getCombatKnowledgeSkill()
  {
    float knowl = 0.0F;
    int primarySkill = 10052;
    try
    {
      if (!this.creature.getPrimWeapon().isBodyPartAttached()) {
        primarySkill = this.creature.getPrimWeapon().getPrimarySkill();
      }
      Skill fightingSkill = this.creature.getSkills().getSkill(primarySkill);
      knowl = (float)fightingSkill.getKnowledge(this.creature.getPrimWeapon(), 0.0D);
    }
    catch (NoSuchSkillException localNoSuchSkillException) {}
    if ((knowl == 0.0F) && (!this.creature.isPlayer()))
    {
      Skill unarmed = this.creature.getFightingSkill();
      knowl = (float)unarmed.getKnowledge(0.0D);
    }
    if (this.creature.getPrimWeapon().isBodyPartAttached()) {
      knowl += this.creature.getBonusForSpellEffect((byte)24) / 5.0F;
    }
    Seat s = this.creature.getSeat();
    if (s != null) {
      knowl *= s.manouvre;
    }
    if (this.creature.isOnHostileHomeServer()) {
      knowl *= 0.525F;
    }
    return knowl;
  }
  
  private void sendSpecialMoves()
  {
    if ((this.creature.combatRound > 3) && (!this.creature.getPrimWeapon().isBodyPart()))
    {
      double fightskill = 0.0D;
      try
      {
        fightskill = this.creature.getSkills().getSkill(this.creature.getPrimWeapon().getPrimarySkill()).getKnowledge(0.0D);
        if (fightskill > 19.0D)
        {
          specialmoves = SpecialMove.getMovesForWeaponSkillAndStance(this.creature, this.creature.getPrimWeapon(), (int)fightskill);
          if (specialmoves.length > 0)
          {
            this.creature.getCommunicator().sendSpecialMove((short)-1, "");
            if (!this.creature.isAutofight()) {
              for (int sx = 0; sx < specialmoves.length; sx++) {
                this.creature.getCommunicator().sendSpecialMove((short)(197 + sx), specialmoves[sx]
                  .getName());
              }
            }
            selectSpecialMove();
          }
          else
          {
            this.creature.getCommunicator().sendSpecialMove((short)-1, "N/A");
          }
        }
        else
        {
          this.creature.getCommunicator().sendSpecialMove((short)-1, "N/A");
        }
      }
      catch (NoSuchSkillException nss)
      {
        this.creature.getCommunicator().sendSpecialMove((short)-1, "N/A");
      }
    }
    else
    {
      this.creature.getCommunicator().sendSpecialMove((short)-1, "N/A");
    }
  }
  
  private void selectSpecialMove()
  {
    if ((this.creature.isAutofight()) && (Server.rand.nextInt(3) == 0))
    {
      int sm = Server.rand.nextInt(specialmoves.length);
      try
      {
        float chance = getChanceToHit(this.creature.opponent, this.creature.getPrimWeapon());
        if (chance > 50.0F) {
          if (this.creature.getStatus().getStamina() > specialmoves[sm].getStaminaCost()) {
            this.creature.setAction(new Action(this.creature, -1L, this.creature.getWurmId(), (short)(197 + sm), this.creature
              .getPosX(), this.creature.getPosY(), this.creature
              .getPositionZ() + this.creature.getAltOffZ(), this.creature.getStatus().getRotation()));
          }
        }
      }
      catch (Exception fe)
      {
        logger.log(Level.WARNING, this.creature.getName() + " failed:" + fe.getMessage(), fe);
      }
    }
  }
  
  public void addBattleRatingPenalty(byte penalty)
  {
    if (this.battleratingPenalty == 0) {
      penalty = (byte)Math.max(penalty, 2);
    }
    this.battleratingPenalty = ((byte)Math.min(5, this.battleratingPenalty + penalty));
  }
  
  byte getBattleratingPenalty()
  {
    return this.battleratingPenalty;
  }
  
  public void setCurrentStance(int actNum, byte aStance)
  {
    this.currentStance = aStance;
    if (actNum > 0)
    {
      this.creature.sendStance(this.currentStance);
    }
    else if (aStance == 15)
    {
      this.creature.sendStance(this.currentStance);
    }
    else if (aStance == 8)
    {
      this.creature.playAnimation("stancerebound", true);
    }
    else if (aStance == 9)
    {
      this.creature.getStatus().setStunned(3.0F, false);
      this.creature.playAnimation("stanceopen", false);
    }
    else if (aStance == 0)
    {
      this.creature.sendStance(this.currentStance);
    }
  }
  
  public void setCurrentStance(byte aCurrentStance)
  {
    this.currentStance = aCurrentStance;
  }
  
  public byte getCurrentStance()
  {
    return this.currentStance;
  }
  
  public void sendStanceAnimation(byte aStance, boolean attack)
  {
    if (aStance == 8)
    {
      this.creature.sendToLoggers(this.creature.getName() + ": " + "stancerebound", (byte)2);
      this.creature.playAnimation("stancerebound", false);
    }
    else if (aStance == 9)
    {
      this.creature.getStatus().setStunned(3.0F, false);
      this.creature.playAnimation("stanceopen", false);
      this.creature.sendToLoggers(this.creature.getName() + ": " + "stanceopen", (byte)2);
    }
    else
    {
      StringBuilder sb = new StringBuilder();
      sb.append("fight");
      if (attack) {
        if (attString.equals("hit")) {
          sb.append("_strike");
        } else {
          sb.append("_" + attString);
        }
      }
      if ((!this.creature.isUnique()) || (this.creature.getHugeMoveCounter() == 2)) {
        this.creature.playAnimation(sb.toString(), !attack);
      }
      this.creature.sendToLoggers(this.creature.getName() + ": " + sb.toString(), (byte)2);
    }
  }
  
  public static final String getStanceDescription(byte currentStance)
  {
    StringBuilder sb = new StringBuilder();
    if (isHigh(currentStance)) {
      sb.append("higher ");
    } else if (isLow(currentStance)) {
      sb.append("lower ");
    } else {
      sb.append("mid ");
    }
    if (isLeft(currentStance)) {
      sb.append("left ");
    } else if (isRight(currentStance)) {
      sb.append("right ");
    } else {
      sb.append("center ");
    }
    return sb.toString();
  }
  
  private void addToList(List<ActionEntry> list, @Nullable Item weapon, short number, Creature opponent, float mycr, float oppcr, float primweaponskill)
  {
    float movechance;
    float movechance;
    if (this.creature.isPlayer()) {
      movechance = getMoveChance(this.creature, weapon, this.currentStance, com.wurmonline.server.behaviours.Actions.actionEntrys[number], mycr, oppcr, primweaponskill);
    } else {
      movechance = getMoveChance(this.creature, weapon, this.currentStance, com.wurmonline.server.behaviours.Actions.actionEntrys[number], mycr, oppcr, primweaponskill);
    }
    if (movechance > 0.0F) {
      list.add(new ActionEntry(number, (int)movechance + "%, " + com.wurmonline.server.behaviours.Actions.actionEntrys[number].getActionString(), "attack"));
    }
  }
  
  public static final int getAttackSkillCap(short action)
  {
    switch (action)
    {
    case 303: 
      return 0;
    case 291: 
      return 3;
    case 309: 
      return 2;
    case 300: 
      return 15;
    case 288: 
      return 13;
    case 306: 
      return 12;
    case 297: 
      return 9;
    case 294: 
      return 7;
    case 312: 
      return 5;
    }
    return 0;
  }
  
  private List<ActionEntry> getHighAttacks(@Nullable Item weapon, boolean auto, Creature opponent, float mycr, float oppcr, float primweaponskill)
  {
    LinkedList<ActionEntry> tempList = new LinkedList();
    if (primweaponskill > getAttackSkillCap((short)300)) {
      addToList(tempList, weapon, (short)300, opponent, mycr, oppcr, primweaponskill);
    }
    if (primweaponskill > getAttackSkillCap((short)288)) {
      addToList(tempList, weapon, (short)288, opponent, mycr, oppcr, primweaponskill);
    }
    if (primweaponskill > getAttackSkillCap((short)306)) {
      addToList(tempList, weapon, (short)306, opponent, mycr, oppcr, primweaponskill);
    }
    if ((!auto) && (tempList.size() > 0)) {
      tempList.addFirst(new ActionEntry((short)-tempList.size(), "High", "high"));
    }
    return tempList;
  }
  
  private List<ActionEntry> getMidAttacks(@Nullable Item weapon, boolean auto, Creature opponent, float mycr, float oppcr, float primweaponskill)
  {
    LinkedList<ActionEntry> tempList = new LinkedList();
    addToList(tempList, weapon, (short)303, opponent, mycr, oppcr, primweaponskill);
    if (primweaponskill > getAttackSkillCap((short)291)) {
      addToList(tempList, weapon, (short)291, opponent, mycr, oppcr, primweaponskill);
    }
    if (primweaponskill > getAttackSkillCap((short)309)) {
      addToList(tempList, weapon, (short)309, opponent, mycr, oppcr, primweaponskill);
    }
    if ((!auto) && (tempList.size() > 0)) {
      tempList.addFirst(new ActionEntry((short)-tempList.size(), "Mid", "Mid"));
    }
    return tempList;
  }
  
  private List<ActionEntry> getLowAttacks(@Nullable Item weapon, boolean auto, Creature opponent, float mycr, float oppcr, float primweaponskill)
  {
    LinkedList<ActionEntry> tempList = new LinkedList();
    if (primweaponskill > getAttackSkillCap((short)297)) {
      addToList(tempList, weapon, (short)297, opponent, mycr, oppcr, primweaponskill);
    }
    if (primweaponskill > getAttackSkillCap((short)294)) {
      addToList(tempList, weapon, (short)294, opponent, mycr, oppcr, primweaponskill);
    }
    if (primweaponskill > getAttackSkillCap((short)312)) {
      addToList(tempList, weapon, (short)312, opponent, mycr, oppcr, primweaponskill);
    }
    if ((!auto) && (tempList.size() > 0)) {
      tempList.addFirst(new ActionEntry((short)-tempList.size(), "Low", "Low"));
    }
    return tempList;
  }
  
  public static final float getMoveChance(Creature performer, @Nullable Item weapon, int stance, ActionEntry entry, float mycr, float oppcr, float primweaponskill)
  {
    float basechance = 100.0F - oppcr * 2.0F + mycr + primweaponskill;
    
    float cost = 0.0F;
    if (isHigh(stance))
    {
      if (entry.isAttackHigh()) {
        cost += 5.0F;
      } else if (entry.isAttackLow()) {
        cost += 10.0F;
      } else {
        cost += 3.0F;
      }
    }
    else if (isLow(stance))
    {
      if (entry.isAttackHigh()) {
        cost += 10.0F;
      } else if (entry.isAttackLow()) {
        cost += 5.0F;
      } else {
        cost += 3.0F;
      }
    }
    else if (entry.isAttackHigh()) {
      cost += 5.0F;
    } else if (entry.isAttackLow()) {
      cost += 5.0F;
    }
    if (isRight(stance))
    {
      if (entry.isAttackRight()) {
        cost += 3.0F;
      } else if (entry.isAttackLeft()) {
        cost += 10.0F;
      } else {
        cost += 3.0F;
      }
    }
    else if (isLeft(stance))
    {
      if (entry.isAttackRight()) {
        cost += 10.0F;
      } else if (entry.isAttackLeft()) {
        cost += 3.0F;
      } else {
        cost += 3.0F;
      }
    }
    else if (entry.isAttackLeft()) {
      cost += 5.0F;
    } else if (entry.isAttackRight()) {
      cost += 5.0F;
    } else {
      cost += 10.0F;
    }
    if ((entry.isAttackHigh()) && (!entry.isAttackLeft()) && (!entry.isAttackRight())) {
      cost += 3.0F;
    } else if ((entry.isAttackLow()) && (!entry.isAttackLeft()) && (!entry.isAttackRight())) {
      cost += 3.0F;
    }
    cost = (float)(cost * (1.0D - manouvreMod));
    if (weapon != null) {
      cost += Weapon.getBaseSpeedForWeapon(weapon);
    }
    if (performer.fightlevel >= 2) {
      cost -= 10.0F;
    }
    return Math.min(100.0F, Math.max(0.0F, basechance - cost));
  }
  
  public static final boolean isHigh(int stance)
  {
    return (stance == 6) || (stance == 1) || (stance == 7);
  }
  
  public static final boolean isLow(int stance)
  {
    return (stance == 4) || (stance == 3) || (stance == 10) || (stance == 8);
  }
  
  public static final boolean isLeft(int stance)
  {
    return (stance == 4) || (stance == 5) || (stance == 6);
  }
  
  public static final boolean isRight(int stance)
  {
    return (stance == 3) || (stance == 2) || (stance == 1) || (stance == 11);
  }
  
  public static final boolean isCenter(int stance)
  {
    return (stance == 0) || (stance == 9) || (stance == 13) || (stance == 14) || (stance == 12);
  }
  
  public static final boolean isDefend(int stance)
  {
    return (stance == 13) || (stance == 14) || (stance == 12) || (stance == 11);
  }
  
  public static boolean prerequisitesFail(Creature creature, Creature opponent, boolean opportunity, Item weapon)
  {
    return prerequisitesFail(creature, opponent, opportunity, weapon, false);
  }
  
  public static boolean prerequisitesFail(Creature creature, Creature opponent, boolean opportunity, Item weapon, boolean ignoreWeapon)
  {
    if (opponent.isDead())
    {
      creature.setTarget(-10L, true);
      return true;
    }
    if (opponent.equals(creature))
    {
      if (!opportunity)
      {
        creature.getCommunicator().sendCombatAlertMessage("You cannot attack yourself.");
        creature.setOpponent(null);
      }
      return true;
    }
    if ((!creature.isPlayer()) && (opponent.isPlayer())) {
      if ((creature.getHitched() != null) && (creature.getHitched().wurmid == opponent.getVehicle()))
      {
        creature.setOpponent(null);
        creature.setTarget(-10L, true);
        return true;
      }
    }
    if ((!opponent.isPlayer()) && (creature.isPlayer())) {
      if ((opponent.getHitched() != null) && (opponent.getHitched().wurmid == creature.getVehicle()))
      {
        opponent.setOpponent(null);
        opponent.setTarget(-10L, true);
        return true;
      }
    }
    if (!ignoreWeapon) {
      if (weapon == null)
      {
        if (!opportunity)
        {
          ArrayList<MulticolorLineSegment> segments = new ArrayList();
          segments.add(new MulticolorLineSegment("You have no weapon to attack ", (byte)0));
          segments.add(new CreatureLineSegment(opponent));
          segments.add(new MulticolorLineSegment(" with.", (byte)0));
          
          creature.getCommunicator().sendColoredMessageCombat(segments);
          
          creature.setOpponent(null);
        }
        return true;
      }
    }
    if (opponent.isBridgeBlockingAttack(creature, false)) {
      return true;
    }
    if (!GeneralUtilities.mayAttackSameLevel(creature, opponent))
    {
      if (creature.isOnSurface())
      {
        VolaTile t = Zones.getTileOrNull(creature.getTileX(), creature.getTileY(), creature.isOnSurface());
        if (t != null)
        {
          creature.sendToLoggers("Fighting " + opponent.getName() + " my z=" + creature.getPositionZ() + " opponent z=" + opponent.getPositionZ() + " structure=" + t.getStructure() + " diff=" + Math.abs(creature.getStatus().getPositionZ() - opponent.getStatus().getPositionZ()) * 10.0F);
          if (t.getStructure() != null) {
            return true;
          }
        }
      }
      if (opponent.isOnSurface())
      {
        VolaTile t = Zones.getTileOrNull(opponent.getTileX(), opponent.getTileY(), opponent.isOnSurface());
        if (t != null) {
          if (t.getStructure() != null) {
            return true;
          }
        }
      }
    }
    BlockingResult result = Blocking.getBlockerBetween(creature, opponent, 4);
    if (result != null)
    {
      boolean blocked = false;
      for (Blocker b : result.getBlockerArray())
      {
        if (!b.isDoor()) {
          blocked = true;
        }
        if (!b.canBeOpenedBy(creature, false)) {
          blocked = true;
        }
        if (!b.canBeOpenedBy(opponent, false)) {
          blocked = true;
        }
        if (blocked) {
          break;
        }
      }
      if (blocked)
      {
        creature.breakout();
        if (!opportunity)
        {
          creature.getCommunicator().sendNormalServerMessage("The " + result
            .getFirstBlocker().getName() + " blocks your attempt.");
          if (result.getFirstBlocker().isTile())
          {
            if ((opponent.opponent == creature) || (opponent.getTarget() == creature)) {
              opponent.setTarget(-10L, true);
            }
            if (creature.getTarget() == opponent) {
              creature.setTarget(-10L, true);
            }
          }
        }
        creature.setOpponent(null);
        creature.sendToLoggers("Blocker result when attacking " + opponent.getName() + " " + result
          .getFirstBlocker().getName(), (byte)2);
        return true;
      }
    }
    if (creature.isOnSurface() != opponent.isOnSurface())
    {
      boolean fail = false;
      boolean transition = false;
      if (opponent.getCurrentTile().isTransition)
      {
        transition = true;
        if (Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(opponent.getTileX(), opponent.getTileY())))) {
          fail = true;
        }
      }
      if ((!fail) && (creature.getCurrentTile().isTransition))
      {
        transition = true;
        if (Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(creature.getTileX(), creature.getTileY())))) {
          fail = true;
        }
      }
      if (!transition) {
        fail = true;
      }
      return fail;
    }
    return false;
  }
  
  private float lastTimeStamp = 1.0F;
  private float lastAttackPollDelta = 0.0F;
  private float waitTime = 0.0F;
  
  private final AttackAction getAttackAction(boolean isSecondary)
  {
    AttackAction attack = null;
    
    List<AttackAction> list = !isSecondary ? this.creature.getTemplate().getPrimaryAttacks() : this.creature.getTemplate().getSecondaryAttacks();
    List<AttackAction> valid = new ArrayList();
    for (AttackAction act : list)
    {
      UsedAttackData data = this.creature.getUsedAttackData(act);
      if (data == null) {
        valid.add(act);
      } else if ((data.getTime() <= 0.0F) && (data.getRounds() <= 0)) {
        valid.add(act);
      }
    }
    if (valid.size() > 0)
    {
      int index = Server.rand.nextInt(valid.size());
      attack = (AttackAction)valid.get(index);
    }
    return attack;
  }
  
  private boolean attack2(Creature opponent, int combatCounter, boolean opportunity, float actionCounter, Action act)
  {
    float delta = Math.max(0.0F, actionCounter - this.lastTimeStamp);
    float updateTime = Math.abs(delta - this.lastAttackPollDelta);
    
    this.lastAttackPollDelta = updateTime;
    this.creature.updateAttacksUsed(updateTime);
    if (delta <= this.waitTime) {
      return false;
    }
    if (opportunity) {
      this.creature.opportunityAttackCounter = 2;
    }
    this.lastAttackPollDelta = 0.0F;
    this.waitTime = 0.5F;
    
    AttackAction primaryAttack = getAttackAction(false);
    AttackAction secondaryAttack = getAttackAction(true);
    int[] tcmoves = this.creature.getCombatMoves();
    boolean canDoSpecials = (tcmoves != null) && (tcmoves.length > 0);
    this.creature.setSecondsToLogout(300);
    if (!opponent.isPlayer()) {
      this.creature.setSecondsToLogout(180);
    }
    boolean shouldDoSecondary = false;
    if ((primaryAttack != null) && (secondaryAttack != null) && (this.creature.combatRound > 1)) {
      shouldDoSecondary = Server.rand.nextInt(5) == 0;
    } else if ((primaryAttack == null) && (secondaryAttack != null)) {
      shouldDoSecondary = true;
    }
    boolean doSpecialAttack = false;
    if (!this.creature.isPlayer())
    {
      boolean changedStance = false;
      if (Server.rand.nextInt(10) == 0) {
        changedStance = checkStanceChange(this.creature, opponent);
      }
      if ((canDoSpecials) && (!changedStance)) {
        if ((primaryAttack == null) && (secondaryAttack == null)) {
          doSpecialAttack = true;
        } else {
          doSpecialAttack = Server.rand.nextInt(80) < 20;
        }
      }
    }
    Item weapon = null;
    if (((shouldDoSecondary) && (secondaryAttack != null) && (secondaryAttack.isUsingWeapon())) || ((!shouldDoSecondary) && (primaryAttack != null) && 
      (primaryAttack.isUsingWeapon()))) {
      weapon = this.creature.getPrimWeapon();
    }
    if (opportunity) {
      this.creature.opportunityAttackCounter = 2;
    }
    this.lastTimeStamp = actionCounter;
    if (prerequisitesFail(this.creature, opponent, opportunity, weapon, weapon == null)) {
      return true;
    }
    if ((act != null) && (act.justTickedSecond())) {
      this.creature.getCommunicator().sendCombatStatus(getDistdiff(this.creature, opponent, shouldDoSecondary ? secondaryAttack : primaryAttack), 
      
        getFootingModifier(weapon, opponent), this.currentStance);
    }
    if ((isProne()) || (isOpen())) {
      return false;
    }
    boolean lDead = false;
    this.creature.opponentCounter = 30;
    if ((actionCounter == 1.0F) && (!opportunity) && (this.creature.isMoving()) && (!opponent.isMoving()) && 
      (opponent.target == this.creature.getWurmId()))
    {
      opponent.attackTarget();
      if (opponent.opponent == this.creature)
      {
        this.creature.sendToLoggers("Opponent strikes first", (byte)2);
        
        ArrayList<MulticolorLineSegment> segments = new ArrayList();
        segments.add(new CreatureLineSegment(opponent));
        segments.add(new MulticolorLineSegment(" strike ", (byte)0));
        segments.add(new CreatureLineSegment(this.creature));
        segments.add(new MulticolorLineSegment(" as " + this.creature.getHeSheItString() + " approaches!", (byte)0));
        
        opponent.getCommunicator().sendColoredMessageCombat(segments);
        ((MulticolorLineSegment)segments.get(1)).setText(" strikes ");
        ((MulticolorLineSegment)segments.get(1)).setText(" as you approach. ");
        this.creature.getCommunicator().sendColoredMessageCombat(segments);
        
        lDead = opponent.getCombatHandler().attack(this.creature, combatCounter, true, 2.0F, null);
      }
    }
    else if ((opportunity) && (primaryAttack != null))
    {
      this.opportunityAttacks = ((byte)(this.opportunityAttacks + 1));
      this.creature.sendToLoggers("YOU OPPORTUNITY", (byte)2);
      opponent.sendToLoggers(this.creature.getName() + " OPPORTUNITY", (byte)2);
      if (opponent.spamMode())
      {
        ArrayList<MulticolorLineSegment> segments = new ArrayList();
        segments.add(new MulticolorLineSegment("You open yourself to an attack from ", (byte)7));
        segments.add(new CreatureLineSegment(this.creature));
        segments.add(new MulticolorLineSegment(".", (byte)7));
        
        opponent.getCommunicator().sendColoredMessageCombat(segments);
      }
      if (this.creature.spamMode())
      {
        ArrayList<MulticolorLineSegment> segments = new ArrayList();
        segments.add(new CreatureLineSegment(opponent));
        segments.add(new MulticolorLineSegment(" opens " + opponent.getHimHerItString() + "self up to an easy attack.", (byte)3));
        
        opponent.getCommunicator().sendColoredMessageCombat(segments);
      }
      lDead = attack(opponent, primaryAttack);
    }
    else if ((!lDead) && (primaryAttack != null) && (!shouldDoSecondary) && (!doSpecialAttack))
    {
      float time = getSpeed(primaryAttack, primaryAttack
        .isUsingWeapon() ? this.creature.getPrimWeapon() : null);
      this.creature.addToAttackUsed(primaryAttack, time, primaryAttack
        .getAttackValues().getRounds());
      lDead = attack(opponent, primaryAttack);
      this.waitTime = primaryAttack.getAttackValues().getWaitTime();
      if ((this.creature.isPlayer()) && (act != null) && (act.justTickedSecond())) {
        checkStanceChange(this.creature, opponent);
      }
    }
    else if ((!lDead) && (secondaryAttack != null) && (shouldDoSecondary) && (!doSpecialAttack))
    {
      float time = getSpeed(secondaryAttack, secondaryAttack.isUsingWeapon() ? this.creature
        .getPrimWeapon(false) : null);
      this.creature.addToAttackUsed(secondaryAttack, time, secondaryAttack
        .getAttackValues().getRounds());
      
      lDead = attack(opponent, secondaryAttack);
      
      this.waitTime = secondaryAttack.getAttackValues().getWaitTime();
      if ((this.creature.isPlayer()) && (act != null) && (act.justTickedSecond())) {
        checkStanceChange(this.creature, opponent);
      }
    }
    else if (!lDead)
    {
      if ((!this.creature.isPlayer()) && (this.creature.getTarget() != null)) {
        if (doSpecialAttack)
        {
          int[] cmoves = this.creature.getCombatMoves();
          if (cmoves.length > 0) {
            for (int lCmove : cmoves)
            {
              CombatMove c = CombatMove.getCombatMove(lCmove);
              if (Server.rand.nextFloat() < c.getRarity()) {
                if (this.creature.getHugeMoveCounter() == 0)
                {
                  this.creature.sendToLoggers("YOU COMBAT MOVE", (byte)2);
                  opponent.sendToLoggers(this.creature.getName() + " COMBAT MOVE", (byte)2);
                  this.creature.setHugeMoveCounter(2 + Server.rand.nextInt(4));
                  c.perform(this.creature);
                  this.waitTime = 2.0F;
                  break;
                }
              }
            }
          }
        }
      }
    }
    return lDead;
  }
  
  public void resetSecAttacks()
  {
    if (this.secattacks != null) {
      this.secattacks.clear();
    }
  }
  
  public boolean attack(Creature opponent, int combatCounter, boolean opportunity, float actionCounter, Action act)
  {
    opponent.addAttacker(this.creature);
    if (actionCounter == 1.0F) {
      this.lastTimeStamp = actionCounter;
    }
    if (Features.Feature.CREATURE_COMBAT_CHANGES.isEnabled()) {
      if (this.creature.getTemplate().isUsingNewAttacks()) {
        return attack2(opponent, combatCounter, opportunity, actionCounter, act);
      }
    }
    float delta = Math.max(0.0F, actionCounter - this.lastTimeStamp);
    if (delta < 0.1D) {
      return false;
    }
    if (opportunity) {
      this.creature.opportunityAttackCounter = 2;
    }
    this.lastTimeStamp = actionCounter;
    
    Item weapon = this.creature.getPrimWeapon();
    this.creature.setSecondsToLogout(300);
    if (!opponent.isPlayer()) {
      this.creature.setSecondsToLogout(180);
    }
    if (prerequisitesFail(this.creature, opponent, opportunity, weapon)) {
      return true;
    }
    if ((act != null) && (act.justTickedSecond())) {
      this.creature.getCommunicator().sendCombatStatus(getDistdiff(weapon, this.creature, opponent), 
        getFootingModifier(weapon, opponent), this.currentStance);
    }
    boolean lDead = false;
    if ((isProne()) || (isOpen())) {
      return false;
    }
    this.creature.opponentCounter = 30;
    if ((actionCounter == 1.0F) && (!opportunity) && (this.creature.isMoving()) && (!opponent.isMoving()) && 
      (opponent.target == this.creature.getWurmId()))
    {
      opponent.attackTarget();
      if (opponent.opponent == this.creature)
      {
        this.creature.sendToLoggers("Opponent strikes first", (byte)2);
        
        ArrayList<MulticolorLineSegment> segments = new ArrayList();
        segments.add(new CreatureLineSegment(opponent));
        segments.add(new MulticolorLineSegment(" strike ", (byte)0));
        segments.add(new CreatureLineSegment(this.creature));
        segments.add(new MulticolorLineSegment(" as " + this.creature.getHeSheItString() + " approaches!", (byte)0));
        
        opponent.getCommunicator().sendColoredMessageCombat(segments);
        ((MulticolorLineSegment)segments.get(1)).setText(" strikes ");
        ((MulticolorLineSegment)segments.get(1)).setText(" as you approach. ");
        this.creature.getCommunicator().sendColoredMessageCombat(segments);
        
        lDead = opponent.getCombatHandler().attack(this.creature, combatCounter, true, 2.0F, null);
      }
    }
    else if (opportunity)
    {
      this.opportunityAttacks = ((byte)(this.opportunityAttacks + 1));
      this.creature.sendToLoggers("YOU OPPORTUNITY", (byte)2);
      opponent.sendToLoggers(this.creature.getName() + " OPPORTUNITY", (byte)2);
      if (opponent.spamMode())
      {
        ArrayList<MulticolorLineSegment> segments = new ArrayList();
        segments.add(new MulticolorLineSegment("You open yourself to an attack from ", (byte)7));
        segments.add(new CreatureLineSegment(this.creature));
        segments.add(new MulticolorLineSegment(".", (byte)7));
        
        opponent.getCommunicator().sendColoredMessageCombat(segments);
      }
      if (this.creature.spamMode())
      {
        ArrayList<MulticolorLineSegment> segments = new ArrayList();
        segments.add(new CreatureLineSegment(opponent));
        segments.add(new MulticolorLineSegment(" opens " + opponent.getHimHerItString() + "self up to an easy attack.", (byte)3));
        
        this.creature.getCommunicator().sendColoredMessageCombat(segments);
      }
      if (Server.rand.nextInt(3) == 0)
      {
        Item[] secweapons = this.creature.getSecondaryWeapons();
        if (secweapons.length > 0) {
          weapon = secweapons[Server.rand.nextInt(secweapons.length)];
        }
      }
      lDead = attack(opponent, weapon, false);
    }
    else
    {
      boolean performedAttack = false;
      Item lSecweapon;
      float time;
      float timer;
      if ((!lDead) && (this.creature.combatRound > 1))
      {
        Item[] secweapons = this.creature.getSecondaryWeapons();
        for (lSecweapon : secweapons) {
          if (this.creature.opponent != null)
          {
            if (this.secattacks == null) {
              this.secattacks = new HashSet();
            }
            if (!this.secattacks.contains(lSecweapon)) {
              if (((lSecweapon.getTemplateId() != 12) && (lSecweapon.getTemplateId() != 17)) || (
                (this.creature.getHugeMoveCounter() == 0) && (Server.rand.nextBoolean())))
              {
                time = getSpeed(lSecweapon);
                timer = this.creature.addToWeaponUsed(lSecweapon, delta);
                boolean shouldAttack = timer > time;
                if ((!lDead) && (this.creature.combatRound % 2 == 1) && (shouldAttack))
                {
                  this.creature.deductFromWeaponUsed(lSecweapon, time);
                  this.creature.sendToLoggers("YOU SECONDARY " + lSecweapon.getName(), (byte)2);
                  opponent.sendToLoggers(this.creature.getName() + " SECONDARY " + lSecweapon.getName() + "(" + lSecweapon
                    .getWurmId() + ")", (byte)2);
                  this.creature.setHugeMoveCounter(2 + Server.rand.nextInt(4));
                  lDead = attack(opponent, lSecweapon, true);
                  
                  performedAttack = true;
                  
                  this.secattacks.add(lSecweapon);
                }
              }
            }
          }
        }
      }
      float time = getSpeed(weapon);
      float timer = this.creature.addToWeaponUsed(weapon, delta);
      boolean shouldAttack = timer > time;
      if ((!lDead) && (shouldAttack))
      {
        this.creature.deductFromWeaponUsed(weapon, time);
        this.creature.sendToLoggers("YOU PRIMARY " + weapon.getName(), (byte)2);
        opponent.sendToLoggers(this.creature.getName() + " PRIMARY " + weapon.getName(), (byte)2);
        lDead = attack(opponent, weapon, false);
        performedAttack = true;
        if ((this.creature.isPlayer()) && (act != null) && (act.justTickedSecond())) {
          checkStanceChange(this.creature, opponent);
        }
      }
      else if ((!performedAttack) && (!lDead))
      {
        if ((!this.creature.isPlayer()) && (this.creature.getTarget() != null)) {
          if (this.creature.getLayer() == opponent.getLayer()) {
            if (!checkStanceChange(this.creature, opponent))
            {
              int[] cmoves = this.creature.getCombatMoves();
              if (cmoves.length > 0)
              {
                lSecweapon = cmoves;time = lSecweapon.length;
                for (timer = 0; timer < time; timer++)
                {
                  int lCmove = lSecweapon[timer];
                  
                  CombatMove c = CombatMove.getCombatMove(lCmove);
                  if (Server.rand.nextFloat() < c.getRarity()) {
                    if (this.creature.getHugeMoveCounter() == 0)
                    {
                      this.creature.sendToLoggers("YOU COMBAT MOVE", (byte)2);
                      opponent.sendToLoggers(this.creature.getName() + " COMBAT MOVE", (byte)2);
                      
                      this.creature.setHugeMoveCounter(2 + Server.rand.nextInt(4));
                      c.perform(this.creature);
                      break;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return lDead;
  }
  
  public void clearRound()
  {
    this.opportunityAttacks = 0;
    this.receivedWeaponSkill = false;
    this.receivedSecWeaponSkill = false;
    this.receivedFStyleSkill = false;
    this.receivedShieldSkill = false;
    
    this.usedShieldThisRound = 0;
    if (this.lastShieldBashed > 0) {
      this.lastShieldBashed -= 1;
    }
    if (this.secattacks != null) {
      this.secattacks.clear();
    }
    this.turned = false;
    if (this.battleratingPenalty > 0)
    {
      this.battleratingPenalty = ((byte)Math.max(0, this.battleratingPenalty - 2));
      if ((this.battleratingPenalty == 0) && (this.creature.isPlayer())) {
        this.creature.getCommunicator().sendCombatNormalMessage("You concentrate better again.");
      }
    }
    if (this.creature.isFighting())
    {
      this.creature.combatRound += 1;
      if (!this.creature.opponent.isDead()) {
        calcAttacks(true);
      } else {
        this.moveStack = null;
      }
      this.creature.setStealth(false);
    }
  }
  
  public static final boolean isStanceParrying(byte defenderStance, byte attackerStance)
  {
    if ((attackerStance == 8) || (attackerStance == 9)) {
      return true;
    }
    if ((defenderStance == 8) || (defenderStance == 9)) {
      return false;
    }
    if (defenderStance == 11) {
      return (attackerStance == 3) || (attackerStance == 4) || (attackerStance == 10);
    }
    if (defenderStance == 12) {
      return (attackerStance == 1) || (attackerStance == 6) || (attackerStance == 7);
    }
    if (defenderStance == 14) {
      return (attackerStance == 5) || (attackerStance == 6) || (attackerStance == 4);
    }
    if (defenderStance == 13) {
      return (attackerStance == 2) || (attackerStance == 1) || (attackerStance == 3);
    }
    return false;
  }
  
  public static final boolean isStanceOpposing(byte defenderStance, byte attackerStance)
  {
    if ((attackerStance == 8) || (attackerStance == 9)) {
      return true;
    }
    if ((defenderStance == 8) || (defenderStance == 9)) {
      return false;
    }
    if (defenderStance == 1) {
      return attackerStance == 6;
    }
    if (defenderStance == 6) {
      return attackerStance == 1;
    }
    if (defenderStance == 4) {
      return attackerStance == 3;
    }
    if (defenderStance == 3) {
      return attackerStance == 4;
    }
    if (defenderStance == 5) {
      return attackerStance == 2;
    }
    if (defenderStance == 2) {
      return attackerStance == 5;
    }
    if (defenderStance == 7) {
      return attackerStance == 7;
    }
    if (defenderStance == 0) {
      return attackerStance == 0;
    }
    if (defenderStance == 10) {
      return attackerStance == 10;
    }
    return false;
  }
  
  private byte getWoundPos(byte aStance, Creature aCreature)
    throws Exception
  {
    return aCreature.getBody().getRandomWoundPos(aStance);
  }
  
  private static void resetFlags(Creature opponent)
  {
    hit = false;
    miss = false;
    crit = false;
    aiming = false;
    dead = false;
    chanceToHit = 0.0F;
    pos = 0;
    
    attCheck = 0.0D;
    attBonus = 0.0D;
    defCheck = 0.0D;
    defBonus = 0.0D;
    defShield = null;
    defenderSkills = opponent.getSkills();
    defParryWeapon = null;
    defLeftWeapon = null;
    defPrimWeaponSkill = null;
    type = 0;
    attString = "";
    damage = 0.0D;
    justOpen = false;
  }
  
  public float getSpeed(AttackAction act, Item weapon)
  {
    float timeMod = 0.5F;
    if (this.currentStrength == 0) {
      timeMod = 1.5F;
    }
    if ((act.isUsingWeapon()) && (weapon != null))
    {
      float calcspeed = getWeaponSpeed(act, weapon);
      
      calcspeed += timeMod;
      if (weapon.getSpellSpeedBonus() != 0.0F) {
        calcspeed = (float)(calcspeed - 0.5D * (weapon.getSpellSpeedBonus() / 100.0F));
      } else if (!weapon.isArtifact()) {
        if (this.creature.getBonusForSpellEffect((byte)39) > 0.0F) {
          calcspeed -= 0.5F;
        }
      }
      if ((weapon.isTwoHanded()) && (this.currentStrength == 3)) {
        calcspeed *= 0.9F;
      }
      if ((!Features.Feature.METALLIC_ITEMS.isEnabled()) && (weapon.getMaterial() == 57)) {
        calcspeed *= 0.9F;
      }
      if (this.creature.getStatus().getStamina() < 2000) {
        calcspeed += 1.0F;
      }
      calcspeed = (float)(calcspeed * (this.creature.getMovementScheme().getWebArmourMod() * -4.0D));
      if (this.creature.hasSpellEffect((byte)66)) {
        calcspeed *= 2.0F;
      }
      return Math.max(3.0F, calcspeed);
    }
    float calcspeed = getWeaponSpeed(act, null);
    
    calcspeed += timeMod;
    if (this.creature.getStatus().getStamina() < 2000) {
      calcspeed += 1.0F;
    }
    calcspeed = (float)(calcspeed * (this.creature.getMovementScheme().getWebArmourMod() * -4.0D));
    if (this.creature.hasSpellEffect((byte)66)) {
      calcspeed *= 2.0F;
    }
    return Math.max(3.0F, calcspeed);
  }
  
  public float getSpeed(Item weapon)
  {
    float timeMod = 0.5F;
    if (this.currentStrength == 0) {
      timeMod = 1.5F;
    }
    float calcspeed = getWeaponSpeed(weapon);
    
    calcspeed += timeMod;
    if (weapon.getSpellSpeedBonus() != 0.0F)
    {
      calcspeed = (float)(calcspeed - 0.5D * (weapon.getSpellSpeedBonus() / 100.0F));
    }
    else if ((!weapon.isArtifact()) && (this.creature.getBonusForSpellEffect((byte)39) > 0.0F))
    {
      float maxBonus = calcspeed * 0.1F;
      float percentBonus = this.creature.getBonusForSpellEffect((byte)39) / 100.0F;
      calcspeed -= maxBonus * percentBonus;
    }
    if ((weapon.isTwoHanded()) && (this.currentStrength == 3)) {
      calcspeed *= 0.9F;
    }
    if ((!Features.Feature.METALLIC_ITEMS.isEnabled()) && (weapon.getMaterial() == 57)) {
      calcspeed *= 0.9F;
    }
    if (this.creature.getStatus().getStamina() < 2000) {
      calcspeed += 1.0F;
    }
    float waMult = (float)(this.creature.getMovementScheme().getWebArmourMod() * -2.0D);
    calcspeed *= (1.0F + waMult);
    if (this.creature.hasSpellEffect((byte)66)) {
      calcspeed *= 2.0F;
    }
    return Math.max(3.0F, calcspeed);
  }
  
  public boolean isOpen()
  {
    return this.currentStance == 9;
  }
  
  public boolean isProne()
  {
    return this.currentStance == 8;
  }
  
  private boolean attack(Creature opponent, AttackAction attackAction)
  {
    resetFlags(opponent);
    if ((!(opponent instanceof Player)) || (!opponent.hasLink()))
    {
      if (!this.turned)
      {
        if ((opponent.getTarget() == null) || (opponent.getTarget() == this.creature)) {
          opponent.turnTowardsCreature(this.creature);
        }
        this.turned = true;
      }
      boolean switchOpp = false;
      if ((!opponent.isFighting()) && ((this.creature.isPlayer()) || (this.creature.isDominated()))) {
        switchOpp = true;
      }
      opponent.setTarget(this.creature.getWurmId(), switchOpp);
    }
    this.creature.getStatus().modifyStamina((int)(-80.0F * (1.0F + this.currentStrength * 0.5F)));
    this.addToSkills = true;
    Item weapon = null;
    weapon = this.creature.getPrimWeapon(!attackAction.isUsingWeapon());
    chanceToHit = getChanceToHit(opponent, weapon);
    
    type = attackAction.getAttackValues().getDamageType();
    
    float percent = checkShield(opponent, weapon);
    if (percent > 50.0F) {
      chanceToHit = 0.0F;
    } else if (percent > 0.0F) {
      chanceToHit *= (1.0F - percent / 100.0F);
    }
    float parrPercent = -1.0F;
    if ((opponent.getFightStyle() != 1) || (Server.rand.nextInt(3) == 0)) {
      if (chanceToHit > 0.0F)
      {
        parrPercent = checkDefenderParry(opponent, weapon);
        if (parrPercent > 60.0F) {
          chanceToHit = 0.0F;
        } else if (parrPercent > 0.0F) {
          chanceToHit *= (1.0F - parrPercent / 200.0F);
        }
      }
    }
    pos = 2;
    try
    {
      pos = getWoundPos(this.currentStance, opponent);
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, this.creature.getName() + " " + ex.getMessage(), ex);
    }
    attCheck = Server.rand.nextFloat() * 100.0F * (1.0D + this.creature.getVisionMod());
    
    String combatDetails = " CHANCE:" + chanceToHit + ", roll=" + attCheck;
    if ((this.creature.spamMode()) && (Servers.isThisATestServer())) {
      this.creature.getCommunicator().sendCombatSafeMessage(combatDetails);
    }
    this.creature.sendToLoggers("YOU" + combatDetails, (byte)2);
    opponent.sendToLoggers(this.creature.getName() + combatDetails, (byte)2);
    if (attCheck < chanceToHit)
    {
      if ((opponent.isPlayer()) && (!weapon.isArtifact()))
      {
        float critChance = attackAction.getAttackValues().getCriticalChance();
        if (isAtSoftSpot(opponent.getCombatHandler().getCurrentStance(), getCurrentStance())) {
          critChance += 0.05F;
        }
        if (Server.rand.nextFloat() < critChance) {
          crit = true;
        }
      }
    }
    else {
      miss = true;
    }
    if (!miss) {
      if (!crit)
      {
        boolean keepGoing = true;
        
        defCheck = Server.rand.nextFloat() * 100.0F * opponent.getCombatHandler().getDodgeMod();
        defCheck *= opponent.getStatus().getDodgeTypeModifier();
        if (opponent.getMovePenalty() != 0) {
          defCheck *= (1.0F + opponent.getMovePenalty() / 10.0F);
        }
        defCheck *= (1.0D - opponent.getMovementScheme().armourMod.getModifier());
        if (defCheck < opponent.getBodyControl() * ItemBonus.getDodgeBonus(opponent) / 3.0D)
        {
          if (opponent.getStatus().getDodgeTypeModifier() * 100.0F < opponent.getBodyControl() / 3.0D) {
            logger.log(Level.WARNING, opponent
              .getName() + " is impossible to hit except for crits: " + opponent
              .getCombatHandler().getDodgeMod() * 100.0D + " is always less than " + opponent
              .getBodyControl());
          }
          sendDodgeMessage(opponent);
          keepGoing = false;
          
          String dodgeDetails = "Dodge=" + defCheck + "<" + opponent.getBodyControl() / 3.0D + " dodgemod=" + opponent.getCombatHandler().getDodgeMod() + " dodgeType=" + opponent.getStatus().getDodgeTypeModifier() + " dodgeMovePenalty=" + opponent.getMovePenalty() + " armour=" + opponent.getMovementScheme().armourMod.getModifier();
          if ((this.creature.spamMode()) && (Servers.isThisATestServer())) {
            this.creature.getCommunicator().sendCombatSafeMessage(dodgeDetails);
          }
          this.creature.sendToLoggers(dodgeDetails, (byte)4);
          
          checkIfHitVehicle(this.creature, opponent);
        }
        if (keepGoing) {
          hit = true;
        }
      }
    }
    if ((hit) || (crit))
    {
      this.creature.sendToLoggers("YOU DAMAGE " + weapon.getName(), (byte)2);
      opponent.sendToLoggers(this.creature.getName() + " DAMAGE " + weapon.getName(), (byte)2);
      dead = setDamage(opponent, weapon, damage, pos, type);
    }
    if (dead) {
      setKillEffects(this.creature, opponent);
    }
    if (miss)
    {
      if (this.creature.spamMode()) {
        if ((chanceToHit > 0.0F) || ((percent > 0.0F) && (parrPercent > 0.0F)))
        {
          this.creature.getCommunicator().sendCombatNormalMessage("You miss with the " + weapon.getName() + ".");
          this.creature.sendToLoggers("YOU MISS " + weapon.getName(), (byte)2);
          opponent.sendToLoggers(this.creature.getName() + " MISS " + weapon.getName(), (byte)2);
        }
      }
      if ((!this.creature.isUnique()) && (attCheck - chanceToHit > 50.0D) && (Server.rand.nextInt(10) == 0))
      {
        justOpen = true;
        
        setCurrentStance(-1, (byte)9);
        
        ArrayList<MulticolorLineSegment> segments = new ArrayList();
        segments.add(new CreatureLineSegment(this.creature));
        segments.add(new MulticolorLineSegment(" makes a bad move and is an easy target!.", (byte)0));
        
        opponent.getCommunicator().sendColoredMessageCombat(segments);
        ((MulticolorLineSegment)segments.get(1)).setText(" make a bad move, making you an easy target.");
        this.creature.getCommunicator().sendColoredMessageCombat(segments);
        
        this.creature.getCurrentTile().checkOpportunityAttacks(this.creature);
        opponent.getCurrentTile().checkOpportunityAttacks(this.creature);
      }
      else if (Server.rand.nextInt(10) == 0)
      {
        checkIfHitVehicle(this.creature, opponent);
      }
    }
    this.addToSkills = false;
    
    getDamage(this.creature, attackAction, opponent);
    attString = attackAction.getAttackIdentifier().getAnimationString();
    sendStanceAnimation(this.currentStance, true);
    
    return dead;
  }
  
  private boolean attack(Creature opponent, Item weapon, boolean secondaryWeapon)
  {
    if (weapon.isWeaponBow()) {
      return false;
    }
    resetFlags(opponent);
    if ((!(opponent instanceof Player)) || (!opponent.hasLink()))
    {
      if (!this.turned)
      {
        if ((opponent.getTarget() == null) || (opponent.getTarget() == this.creature)) {
          opponent.turnTowardsCreature(this.creature);
        }
        this.turned = true;
      }
      boolean switchOpp = false;
      if ((!opponent.isFighting()) && ((this.creature.isPlayer()) || (this.creature.isDominated()))) {
        switchOpp = true;
      }
      opponent.setTarget(this.creature.getWurmId(), switchOpp);
    }
    this.creature.getStatus().modifyStamina((int)(-weapon.getWeightGrams() / 10.0F * (1.0F + this.currentStrength * 0.5F)));
    this.addToSkills = true;
    
    chanceToHit = getChanceToHit(opponent, weapon);
    
    getType(weapon, false);
    
    getDamage(this.creature, weapon, opponent);
    setAttString(this.creature, weapon, type);
    sendStanceAnimation(this.currentStance, true);
    
    float percent = checkShield(opponent, weapon);
    if (percent > 50.0F) {
      chanceToHit = 0.0F;
    } else if (percent > 0.0F) {
      chanceToHit *= (1.0F - percent / 100.0F);
    }
    float parrPercent = -1.0F;
    if ((opponent.getFightStyle() != 1) || (Server.rand.nextInt(3) == 0)) {
      if (chanceToHit > 0.0F)
      {
        parrPercent = checkDefenderParry(opponent, weapon);
        if (parrPercent > 60.0F) {
          chanceToHit = 0.0F;
        } else if (parrPercent > 0.0F) {
          chanceToHit *= (1.0F - parrPercent / 200.0F);
        }
      }
    }
    pos = 2;
    try
    {
      pos = getWoundPos(this.currentStance, opponent);
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, this.creature.getName() + " " + ex.getMessage(), ex);
    }
    attCheck = Server.rand.nextFloat() * 100.0F * (1.0D + this.creature.getVisionMod());
    
    String combatDetails = " CHANCE:" + chanceToHit + ", roll=" + attCheck;
    if ((this.creature.spamMode()) && (Servers.isThisATestServer())) {
      this.creature.getCommunicator().sendCombatSafeMessage(combatDetails);
    }
    this.creature.sendToLoggers("YOU" + combatDetails, (byte)2);
    opponent.sendToLoggers(this.creature.getName() + combatDetails, (byte)2);
    if (attCheck < chanceToHit)
    {
      if (opponent.isPlayer())
      {
        float critChance = Weapon.getCritChanceForWeapon(weapon);
        if (isAtSoftSpot(opponent.getCombatHandler().getCurrentStance(), getCurrentStance())) {
          critChance += 0.05F;
        }
        if ((!weapon.isArtifact()) && (Server.rand.nextFloat() < critChance)) {
          crit = true;
        }
      }
    }
    else {
      miss = true;
    }
    if (!miss) {
      if (!crit)
      {
        boolean keepGoing = true;
        
        defCheck = Server.rand.nextFloat() * 100.0F * opponent.getCombatHandler().getDodgeMod();
        defCheck *= opponent.getStatus().getDodgeTypeModifier();
        if (opponent.getMovePenalty() != 0) {
          defCheck *= (1.0F + opponent.getMovePenalty() / 10.0F);
        }
        defCheck *= (1.0D - opponent.getMovementScheme().armourMod.getModifier());
        if (defCheck < opponent.getBodyControl() / 3.0D)
        {
          if (opponent.getStatus().getDodgeTypeModifier() * 100.0F < opponent.getBodyControl() / 3.0D) {
            logger.log(Level.WARNING, opponent
              .getName() + " is impossible to hit except for crits: " + opponent
              .getCombatHandler().getDodgeMod() * 100.0D + " is always less than " + opponent
              .getBodyControl());
          }
          sendDodgeMessage(opponent);
          keepGoing = false;
          
          String dodgeDetails = "Dodge=" + defCheck + "<" + opponent.getBodyControl() / 3.0D + " dodgemod=" + opponent.getCombatHandler().getDodgeMod() + " dodgeType=" + opponent.getStatus().getDodgeTypeModifier() + " dodgeMovePenalty=" + opponent.getMovePenalty() + " armour=" + opponent.getMovementScheme().armourMod.getModifier();
          if ((this.creature.spamMode()) && (Servers.isThisATestServer())) {
            this.creature.getCommunicator().sendCombatSafeMessage(dodgeDetails);
          }
          this.creature.sendToLoggers(dodgeDetails, (byte)4);
          
          checkIfHitVehicle(this.creature, opponent);
        }
        if (keepGoing) {
          hit = true;
        }
      }
    }
    if ((hit) || (crit))
    {
      this.creature.sendToLoggers("YOU DAMAGE " + weapon.getName(), (byte)2);
      opponent.sendToLoggers(this.creature.getName() + " DAMAGE " + weapon.getName(), (byte)2);
      dead = setDamage(opponent, weapon, damage, pos, type);
    }
    if (dead) {
      setKillEffects(this.creature, opponent);
    }
    if (miss)
    {
      if (this.creature.spamMode()) {
        if ((chanceToHit > 0.0F) || ((percent > 0.0F) && (parrPercent > 0.0F)))
        {
          this.creature.getCommunicator().sendCombatNormalMessage("You miss with the " + weapon.getName() + ".");
          this.creature.sendToLoggers("YOU MISS " + weapon.getName(), (byte)2);
          opponent.sendToLoggers(this.creature.getName() + " MISS " + weapon.getName(), (byte)2);
        }
      }
      if ((!this.creature.isUnique()) && (attCheck - chanceToHit > 50.0D) && (Server.rand.nextInt(10) == 0))
      {
        justOpen = true;
        
        setCurrentStance(-1, (byte)9);
        
        ArrayList<MulticolorLineSegment> segments = new ArrayList();
        segments.add(new CreatureLineSegment(this.creature));
        segments.add(new MulticolorLineSegment(" makes a bad move and is an easy target!.", (byte)0));
        
        opponent.getCommunicator().sendColoredMessageCombat(segments);
        ((MulticolorLineSegment)segments.get(1)).setText(" make a bad move, making you an easy target.");
        this.creature.getCommunicator().sendColoredMessageCombat(segments);
        
        this.creature.getCurrentTile().checkOpportunityAttacks(this.creature);
        opponent.getCurrentTile().checkOpportunityAttacks(this.creature);
      }
      else if (Server.rand.nextInt(10) == 0)
      {
        checkIfHitVehicle(this.creature, opponent);
      }
    }
    this.addToSkills = false;
    
    return dead;
  }
  
  private static final void checkIfHitVehicle(Creature creature, Creature opponent)
  {
    if ((creature.isBreakFence()) && (opponent.getVehicle() > -10L))
    {
      Vehicle vehic = Vehicles.getVehicleForId(opponent.getVehicle());
      if (vehic != null) {
        if (!vehic.creature) {
          try
          {
            Item i = Items.getItem(opponent.getVehicle());
            
            Server.getInstance().broadCastAction(creature
              .getNameWithGenus() + " hits the " + i
              .getName() + " with huge force!", creature, 10, true);
            i.setDamage(i.getDamage() + (float)(damage / 300000.0D));
          }
          catch (NoSuchItemException localNoSuchItemException) {}
        }
      }
    }
  }
  
  public static void setAttString(Creature _creature, Item _weapon, byte _type)
  {
    attString = CombatEngine.getAttackString(_creature, _weapon, _type);
  }
  
  public static void setAttString(String string)
  {
    attString = string;
  }
  
  public boolean setDamage(Creature defender, Item attWeapon, double ddamage, byte position, byte _type)
  {
    float armourMod = defender.getArmourMod();
    
    float poisdam = 0.0F;
    if (attWeapon.getSpellVenomBonus() > 0.0F) {
      _type = 5;
    }
    if (attWeapon.enchantment == 90) {
      _type = 10;
    } else if (attWeapon.enchantment == 92) {
      _type = 8;
    } else if (attWeapon.enchantment == 91) {
      _type = 4;
    }
    float infection = 0.0F;
    if (attWeapon.getSpellExtraDamageBonus() > 0.0F)
    {
      float bloodthirstPower = attWeapon.getSpellExtraDamageBonus();
      if (Server.rand.nextFloat() * 100000.0F <= bloodthirstPower)
      {
        _type = 6;
        infection = bloodthirstPower / 1000.0F;
      }
    }
    boolean metalArmour = false;
    Item armour = null;
    float bounceWoundPower = 0.0F;
    float evasionChance = ArmourTemplate.calculateGlanceRate(defender.getArmourType(), armour, _type, armourMod);
    if ((armourMod == 1.0F) || (defender.isVehicle()) || (defender.isKingdomGuard()))
    {
      try
      {
        byte bodyPosition = ArmourTemplate.getArmourPosition(position);
        armour = defender.getArmour(bodyPosition);
        if (!defender.isKingdomGuard()) {
          armourMod = ArmourTemplate.calculateDR(armour, _type);
        } else {
          armourMod *= ArmourTemplate.calculateDR(armour, _type);
        }
        defender.sendToLoggers("YOU ARMORMOD " + armourMod, (byte)2);
        this.creature.sendToLoggers(defender.getName() + " ARMORMOD " + armourMod, (byte)2);
        if ((defender.isPlayer()) || (defender.isHorse())) {
          armour.setDamage(armour.getDamage() + 
            Math.max(0.01F, 
            
            Math.min(1.0F, 
            (float)(ddamage * Weapon.getMaterialArmourDamageBonus(attWeapon.getMaterial()) * ArmourTemplate.getArmourDamageModFor(armour, _type) / 1200000.0D) * armour
            .getDamageModifier())));
        }
        CombatEngine.checkEnchantDestruction(attWeapon, armour, defender);
        if (armour.isMetal()) {
          metalArmour = true;
        }
        if (!defender.isPlayer()) {
          evasionChance = ArmourTemplate.calculateCreatureGlanceRate(_type, armour);
        } else {
          evasionChance = ArmourTemplate.calculateGlanceRate(null, armour, _type, armourMod);
        }
        evasionChance *= (1.0F + ItemBonus.getGlanceBonusFor(armour.getArmourType(), _type, attWeapon, defender));
      }
      catch (NoArmourException localNoArmourException) {}catch (NoSpaceException nsp)
      {
        logger.log(Level.WARNING, defender.getName() + " no armour space on loc " + position);
      }
      if (((armour == null) || ((armour.getArmourType() != null) && (armour.getArmourType().getLimitFactor() >= 0.0F))) && 
        (defender.getBonusForSpellEffect((byte)22) > 0.0F))
      {
        if ((!CombatEngine.isEye(position)) || (defender.isUnique()))
        {
          float omod = 100.0F;
          float minmod = 0.6F;
          if (!defender.isPlayer())
          {
            omod = 300.0F;
            minmod = 0.7F;
          }
          else if (defender.getBonusForSpellEffect((byte)22) > 70.0F)
          {
            bounceWoundPower = defender.getBonusForSpellEffect((byte)22);
          }
          if (armourMod >= 1.0F)
          {
            armourMod = 0.3F + (float)(1.0D - Server.getBuffedQualityEffect(defender
              .getBonusForSpellEffect((byte)22) / omod)) * minmod;
            
            evasionChance = (float)Server.getBuffedQualityEffect(defender
              .getBonusForSpellEffect((byte)22) / 100.0F) / 3.0F;
          }
          else
          {
            armourMod = Math.min(armourMod, 0.3F + 
              (float)(1.0D - Server.getBuffedQualityEffect(defender
              .getBonusForSpellEffect((byte)22) / omod)) * minmod);
          }
        }
      }
      else if (defender.isReborn()) {
        armourMod = (float)(1.0D - Server.getBuffedQualityEffect(defender.getStrengthSkill() / 100.0D));
      }
    }
    if (defender.isUnique()) {
      evasionChance = 0.5F;
    }
    if (!attWeapon.isBodyPartAttached()) {
      if (this.creature.isPlayer())
      {
        boolean rust = defender.hasSpellEffect((byte)70);
        if (rust) {
          this.creature.getCommunicator().sendAlertServerMessage("Your " + attWeapon
            .getName() + " takes excessive damage from " + defender.getNameWithGenus() + ".");
        }
        float mod = rust ? 5.0F : 1.0F;
        attWeapon.setDamage(attWeapon.getDamage() + Math.min(1.0F, (float)(ddamage * armourMod / 1000000.0D)) * attWeapon
          .getDamageModifier() * mod);
      }
    }
    double defdamage = ddamage * ItemBonus.getDamReductionBonusFor(armour != null ? armour.getArmourType() : defender.getArmourType(), _type, attWeapon, defender);
    if (attWeapon.getSpellVenomBonus() > 0.0F) {
      defdamage *= (0.8F + 0.2F * (attWeapon.getSpellVenomBonus() / 100.0F));
    }
    if (defender.isPlayer())
    {
      if (((Player)defender).getAlcohol() > 50.0F) {
        defdamage *= 0.5D;
      }
      if (defender.fightlevel >= 5) {
        defdamage *= 0.5D;
      }
    }
    if (defender.hasTrait(2)) {
      defdamage *= 0.8999999761581421D;
    }
    float demiseBonus = EnchantUtil.getDemiseBonus(attWeapon, defender);
    if (demiseBonus > 0.0F) {
      defdamage *= (1.0F + demiseBonus);
    }
    if ((this.creature.hasSpellEffect((byte)67)) && (!attWeapon.isArtifact())) {
      crit = true;
    }
    if ((crit) && (!defender.isUnique())) {
      armourMod *= 1.5F;
    }
    if (defender.getTemplate().isTowerBasher()) {
      if ((this.creature.isSpiritGuard()) || (this.creature.isKingdomGuard()))
      {
        float mod = 1.0F / defender.getArmourMod();
        defdamage = Math.max((500 + Server.rand.nextInt(1000)) * mod, defdamage);
      }
    }
    if (Server.rand.nextFloat() < evasionChance)
    {
      if (this.creature.spamMode())
      {
        ArrayList<MulticolorLineSegment> segments = new ArrayList();
        segments.add(new MulticolorLineSegment("Your attack glances off ", (byte)0));
        segments.add(new CreatureLineSegment(defender));
        segments.add(new MulticolorLineSegment("'s armour.", (byte)0));
        
        this.creature.getCommunicator().sendColoredMessageCombat(segments);
      }
      if (defender.spamMode()) {
        defender.getCommunicator().sendCombatNormalMessage("The attack to the " + defender
          .getBody().getWoundLocationString(pos) + " glances off your armour.");
      }
      this.creature.sendToLoggers(defender.getName() + " GLANCE", (byte)2);
      defender.sendToLoggers("YOU GLANCE", (byte)2);
    }
    else if (defdamage * armourMod >= 500.0D)
    {
      if (this.creature.hasSpellEffect((byte)67)) {
        if (!attWeapon.isArtifact()) {
          this.creature.removeTrueStrike();
        }
      }
      if ((attWeapon != null) && (!attWeapon.isBodyPartRemoved())) {
        if (!attWeapon.isWeaponBow()) {
          try
          {
            int primweaponskill = 10052;
            if (!attWeapon.isBodyPartAttached()) {
              primweaponskill = attWeapon.getPrimarySkill();
            }
            try
            {
              Skill pwsk = this.creature.getSkills().getSkill(primweaponskill);
              
              double d1 = pwsk.skillCheck(pwsk.getKnowledge(), attWeapon, 0.0D, defender.isNoSkillFor(this.creature), (float)defdamage * armourMod / 1000.0F);
            }
            catch (NoSuchSkillException nss1)
            {
              this.creature.getSkills().learn(primweaponskill, 1.0F);
            }
          }
          catch (NoSuchSkillException localNoSuchSkillException1) {}
        }
      }
      if (Servers.isThisATestServer())
      {
        String message = String.format("Base Damage: %.1f, Armour DR: %.2f%%, Final Damage: %.1f. Critical: %s", new Object[] {
          Double.valueOf(defdamage), Float.valueOf((1.0F - armourMod) * 100.0F), Double.valueOf(defdamage * armourMod), Boolean.valueOf(crit) });
        if (this.creature.spamMode()) {
          this.creature.getCommunicator().sendCombatSafeMessage(message);
        }
        if (defender.spamMode()) {
          defender.getCommunicator().sendCombatAlertMessage(message);
        }
      }
      this.creature.sendToLoggers(defender.getName() + " DAMAGED " + defdamage * armourMod + " crit=" + crit, (byte)2);
      defender.sendToLoggers("YOU DAMAGED " + defdamage * armourMod + " crit=" + crit, (byte)2);
      
      Battle battle = defender.getBattle();
      
      dead = false;
      
      float champMod = defender.isChampion() ? 0.4F : 1.0F;
      if (armour != null) {
        if (armour.getSpellPainShare() > 0.0F)
        {
          bounceWoundPower = armour.getSpellPainShare();
          int rarityModifier = Math.max(1, armour.getRarity() * 5);
          SpellEffect speff = armour.getSpellEffect((byte)17);
          if (speff != null) {
            if (Server.rand.nextInt(Math.max(2, (int)(rarityModifier * speff.power * 80.0F))) == 0)
            {
              speff.setPower(speff.getPower() - 1.0F);
              if (speff.getPower() <= 0.0F)
              {
                ItemSpellEffects speffs = armour.getSpellEffects();
                if (speffs != null) {
                  speffs.removeSpellEffect(speff.type);
                }
              }
            }
          }
        }
      }
      if ((defender.isUnique()) && (this.creature.isUnique())) {
        if (defender.getStatus().damage > 10000)
        {
          defender.setTarget(-10L, true);
          this.creature.setTarget(-10L, true);
          defender.setOpponent(null);
          this.creature.setOpponent(null);
          try
          {
            defender.checkMove();
          }
          catch (Exception localException2) {}
          try
          {
            this.creature.checkMove();
          }
          catch (Exception localException3) {}
        }
      }
      if (defender.isSparring(this.creature))
      {
        if (defender.getStatus().damage + defdamage * armourMod * 2.0D > 65535.0D)
        {
          defender.setTarget(-10L, true);
          this.creature.setTarget(-10L, true);
          defender.setOpponent(null);
          this.creature.setOpponent(null);
          this.creature.getCommunicator().sendCombatSafeMessage("You win against " + defender
            .getName() + "! Congratulations!");
          defender.getCommunicator().sendCombatNormalMessage("You lose against " + this.creature
            .getName() + " who stops just before finishing you off!");
          Server.getInstance().broadCastAction(this.creature
            .getName() + " defeats " + defender.getName() + " while sparring!", this.creature, defender, 10);
          this.creature.getCommunicator().sendCombatOptions(NO_COMBAT_OPTIONS, (short)0);
          this.creature.getCommunicator().sendSpecialMove((short)-1, "N/A");
          this.creature.achievement(39);
          if (!Servers.localServer.PVPSERVER) {
            this.creature.achievement(8);
          }
          Item weapon = this.creature.getPrimWeapon();
          if (weapon != null)
          {
            if (weapon.isWeaponBow()) {
              this.creature.achievement(11);
            } else if (weapon.isWeaponSword()) {
              this.creature.achievement(14);
            } else if (weapon.isWeaponCrush()) {
              this.creature.achievement(17);
            } else if (weapon.isWeaponAxe()) {
              this.creature.achievement(20);
            } else if (weapon.isWeaponKnife()) {
              this.creature.achievement(25);
            }
            if (weapon.getTemplateId() == 314) {
              this.creature.achievement(27);
            } else if (weapon.getTemplateId() == 567) {
              this.creature.achievement(29);
            } else if (weapon.getTemplateId() == 20) {
              this.creature.achievement(30);
            }
          }
          return true;
        }
        if (bounceWoundPower > 0.0F) {
          if (defdamage * bounceWoundPower * champMod / 300.0D > 500.0D) {
            if (this.creature.getStatus().damage + defdamage * bounceWoundPower * champMod / 300.0D > 65535.0D)
            {
              defender.setTarget(-10L, true);
              this.creature.setTarget(-10L, true);
              defender.setOpponent(null);
              this.creature.setOpponent(null);
              defender.getCommunicator().sendCombatSafeMessage("You win against " + this.creature
                .getName() + "! Congratulations!");
              this.creature.getCommunicator().sendCombatNormalMessage("You lose against " + defender
                .getName() + " whose armour enchantment almost finished you off!");
              
              Server.getInstance().broadCastAction(defender
                .getName() + " defeats " + this.creature.getName() + " while sparring!", defender, this.creature, 10);
              
              this.creature.getCommunicator().sendCombatOptions(NO_COMBAT_OPTIONS, (short)0);
              this.creature.getCommunicator().sendSpecialMove((short)-1, "N/A");
              this.creature.achievement(39);
              if (!Servers.localServer.PVPSERVER) {
                this.creature.achievement(8);
              }
              Item weapon = this.creature.getPrimWeapon();
              if (weapon != null)
              {
                if (weapon.isWeaponBow()) {
                  this.creature.achievement(11);
                } else if (weapon.isWeaponSword()) {
                  this.creature.achievement(14);
                } else if (weapon.isWeaponCrush()) {
                  this.creature.achievement(17);
                } else if (weapon.isWeaponAxe()) {
                  this.creature.achievement(20);
                } else if (weapon.isWeaponKnife()) {
                  this.creature.achievement(25);
                }
                if (weapon.getTemplateId() == 314) {
                  this.creature.achievement(27);
                } else if (weapon.getTemplateId() == 567) {
                  this.creature.achievement(29);
                } else if (weapon.getTemplateId() == 20) {
                  this.creature.achievement(30);
                }
              }
              return true;
            }
          }
        }
      }
      if (defender.getStaminaSkill().getKnowledge() < 2.0D)
      {
        defender.die(false, "Combat Stam Check Fail");
        this.creature.achievement(223);
        dead = true;
      }
      else if (attWeapon.getWeaponSpellDamageBonus() > 0.0F)
      {
        defdamage += defdamage * attWeapon.getWeaponSpellDamageBonus() / 500.0D;
        dead = CombatEngine.addWound(this.creature, defender, _type, position, defdamage, armourMod, attString, battle, Server.rand
          .nextInt((int)Math.max(1.0F, attWeapon.getWeaponSpellDamageBonus())), poisdam, false, false, false, false);
        if ((attWeapon.isWeaponCrush()) && (attWeapon.getWeightGrams() > 4000) && 
          (armour != null) && (armour.getTemplateId() == 286)) {
          defender.achievement(49);
        }
      }
      else
      {
        int dmgBefore = defender.getStatus().damage;
        
        dead = CombatEngine.addWound(this.creature, defender, _type, position, defdamage, armourMod, attString, battle, infection, poisdam, false, false, false, false);
        
        float lifeTransferPower = Math.max(attWeapon.getSpellLifeTransferModifier(), attWeapon.getSpellEssenceDrainModifier() / 3.0F);
        if ((lifeTransferPower > 0.0F) && (dmgBefore != defender.getStatus().damage)) {
          if ((this.creature.getBody() != null) && (this.creature.getBody().getWounds() != null))
          {
            Wound[] w = this.creature.getBody().getWounds().getWounds();
            if (w.length > 0)
            {
              float mod = 500.0F;
              if (this.creature.isChampion()) {
                mod = 1000.0F;
              } else if ((this.creature.getCultist() != null) && (this.creature.getCultist().healsFaster())) {
                mod = 250.0F;
              }
              double toHeal = defdamage * lifeTransferPower / mod;
              double resistance = SpellResist.getSpellResistance(this.creature, 409);
              toHeal *= resistance;
              
              Wound targetWound = w[0];
              for (Wound wound : w) {
                if (wound.getSeverity() > targetWound.getSeverity()) {
                  targetWound = wound;
                }
              }
              SpellResist.addSpellResistance(this.creature, 409, Math.min(targetWound.getSeverity(), toHeal));
              
              targetWound.modifySeverity(-(int)toHeal);
            }
          }
        }
      }
      if (this.creature.isPlayer() != defender.isPlayer()) {
        if (defdamage > 10000.0D) {
          if (defender.fightlevel > 0)
          {
            Creature tmp3153_3152 = defender;tmp3153_3152.fightlevel = ((byte)(tmp3153_3152.fightlevel - 1));
            defender.getCommunicator().sendCombatNormalMessage("You lose some focus.");
            if (defender.isPlayer()) {
              defender.getCommunicator().sendFocusLevel(defender.getWurmId());
            }
          }
        }
      }
      if ((!dead) && (attWeapon.getSpellDamageBonus() > 0.0F)) {
        if ((attWeapon.getSpellDamageBonus() / 300.0F * defdamage > 500.0D) || (crit)) {
          dead = defender.addWoundOfType(this.creature, (byte)4, position, false, armourMod, false, attWeapon
            .getSpellDamageBonus() / 300.0F * defdamage, 0.0F, 0.0F, true, true);
        }
      }
      if ((!dead) && (attWeapon.getSpellFrostDamageBonus() > 0.0F)) {
        if ((attWeapon.getSpellFrostDamageBonus() / 300.0F * defdamage > 500.0D) || (crit)) {
          dead = defender.addWoundOfType(this.creature, (byte)8, position, false, armourMod, false, attWeapon
            .getSpellFrostDamageBonus() / 300.0F * defdamage, 0.0F, 0.0F, true, true);
        }
      }
      if ((!dead) && (attWeapon.getSpellEssenceDrainModifier() > 0.0F)) {
        if ((attWeapon.getSpellEssenceDrainModifier() / 1000.0F * defdamage > 500.0D) || (crit)) {
          dead = defender.addWoundOfType(this.creature, (byte)9, position, false, armourMod, false, attWeapon
            .getSpellEssenceDrainModifier() / 1000.0F * defdamage, 0.0F, 0.0F, true, true);
        }
      }
      if ((!dead) && (Weapon.getMaterialExtraWoundMod(attWeapon.getMaterial()) > 0.0F))
      {
        float extraDmg = Weapon.getMaterialExtraWoundMod(attWeapon.getMaterial());
        if ((extraDmg * defdamage > 500.0D) || (crit)) {
          dead = defender.addWoundOfType(this.creature, Weapon.getMaterialExtraWoundType(attWeapon.getMaterial()), position, false, armourMod, false, extraDmg * defdamage, 0.0F, 0.0F, false, true);
        }
      }
      if ((armour != null) || (bounceWoundPower > 0.0F)) {
        if (bounceWoundPower > 0.0F)
        {
          if (this.creature.isUnique())
          {
            if (armour != null) {
              defender.getCommunicator().sendCombatNormalMessage(this.creature
                .getNameWithGenus() + " ignores the effects of the " + armour.getName() + ".");
            }
          }
          else if (defdamage * bounceWoundPower * champMod / 300.0D > 500.0D) {
            CombatEngine.addBounceWound(defender, this.creature, _type, position, defdamage * bounceWoundPower * champMod / 300.0D, armourMod, 0.0F, 0.0F, false, true);
          }
        }
        else if ((armour != null) && (armour.getSpellSlowdown() > 0.0F))
        {
          if (this.creature.getMovementScheme().setWebArmourMod(true, armour.getSpellSlowdown()))
          {
            this.creature.setWebArmourModTime(armour.getSpellSlowdown() / 10.0F);
            this.creature.getCommunicator().sendCombatAlertMessage("Dark stripes spread along your " + attWeapon
              .getName() + " from " + defender
              .getNamePossessive() + " armour. You feel drained.");
          }
          int rm = Math.max(1, armour.getRarity() * 5);
          SpellEffect speff = armour.getSpellEffect((byte)46);
          if (speff != null) {
            if (Server.rand.nextInt(Math.max(2, (int)(rm * speff.power * 80.0F))) == 0)
            {
              speff.setPower(speff.getPower() - 1.0F);
              if (speff.getPower() <= 0.0F)
              {
                ItemSpellEffects speffs = armour.getSpellEffects();
                if (speffs != null) {
                  speffs.removeSpellEffect(speff.type);
                }
              }
            }
          }
        }
      }
      if (!Players.getInstance().isOverKilling(this.creature.getWurmId(), defender.getWurmId())) {
        if (attWeapon.getSpellExtraDamageBonus() > 0.0F) {
          if ((defender.isPlayer()) && (!defender.isNewbie()))
          {
            SpellEffect speff = attWeapon.getSpellEffect((byte)45);
            float mod = 1.0F;
            if (defdamage * armourMod * champMod < 5000.0D) {
              mod = (float)(defdamage * armourMod * champMod / 5000.0D);
            }
            if (speff != null) {
              speff.setPower(Math.min(10000.0F, speff.power + (dead ? 20.0F : 2.0F * mod)));
            }
          }
          else if ((!defender.isPlayer()) && (!defender.isGuard()) && (dead))
          {
            SpellEffect speff = attWeapon.getSpellEffect((byte)45);
            float mod = 1.0F;
            if ((speff.getPower() > 5000.0F) && (!Servers.isThisAnEpicOrChallengeServer())) {
              mod = Math.max(0.5F, 1.0F - (speff.getPower() - 5000.0F) / 5000.0F);
            }
            if (speff != null) {
              speff.setPower(Math.min(10000.0F, speff.power + defender.getBaseCombatRating() * mod));
            }
          }
        }
      }
      if (dead)
      {
        if (battle != null) {
          battle.addCasualty(this.creature, defender);
        }
        if (defender.isSparring(this.creature)) {
          if (defender.getStatus().damage + defdamage * armourMod * 2.0D > 65535.0D)
          {
            this.creature.achievement(39);
            if (!Servers.localServer.PVPSERVER) {
              this.creature.achievement(8);
            }
            Item weapon = this.creature.getPrimWeapon();
            if (weapon != null)
            {
              if (weapon.isWeaponBow()) {
                this.creature.achievement(11);
              } else if (weapon.isWeaponSword()) {
                this.creature.achievement(14);
              } else if (weapon.isWeaponCrush()) {
                this.creature.achievement(17);
              } else if (weapon.isWeaponAxe()) {
                this.creature.achievement(20);
              } else if (weapon.isWeaponKnife()) {
                this.creature.achievement(25);
              }
              if (weapon.getTemplateId() == 314) {
                this.creature.achievement(27);
              } else if (weapon.getTemplateId() == 567) {
                this.creature.achievement(29);
              } else if (weapon.getTemplateId() == 20) {
                this.creature.achievement(30);
              }
            }
            this.creature.getCommunicator().sendCombatSafeMessage("You accidentally slay " + defender
              .getName() + "! Congratulations!");
            defender.getCommunicator().sendCombatNormalMessage("You lose against " + this.creature
              .getName() + " who unfortunately fails to stop just before finishing you off!");
            
            Server.getInstance().broadCastAction(this.creature
              .getName() + " defeats and accidentally slays " + defender.getName() + " while sparring!", this.creature, defender, 10);
          }
        }
        if (this.creature.isDuelling(defender)) {
          this.creature.achievement(37);
        }
      }
      else if (defdamage > 30000.0D)
      {
        if (Server.rand.nextInt(100000) < defdamage)
        {
          Skill defBodyControl = null;
          try
          {
            defBodyControl = defender.getSkills().getSkill(104);
          }
          catch (NoSuchSkillException nss)
          {
            defBodyControl = defender.getSkills().learn(104, 1.0F);
          }
          if (defBodyControl.skillCheck(defdamage / 10000.0D, defender
            .getCombatHandler().getFootingModifier(attWeapon, this.creature) * 10.0F, false, 10.0F, defender, this.creature) < 0.0D)
          {
            defender.getCombatHandler().setCurrentStance(-1, (byte)8);
            defender.getStatus().setStunned((byte)(int)Math.max(3.0D, defdamage / 10000.0D), false);
            
            ArrayList<MulticolorLineSegment> segments = new ArrayList();
            segments.add(new CreatureLineSegment(defender));
            segments.add(new MulticolorLineSegment(" is knocked senseless from the hit.", (byte)0));
            
            this.creature.getCommunicator().sendColoredMessageCombat(segments);
            
            defender.getCommunicator().sendCombatNormalMessage("You are knocked senseless from the hit.");
            
            segments.clear();
            segments.add(new CreatureLineSegment(this.creature));
            segments.add(new MulticolorLineSegment(" knocks ", (byte)0));
            segments.add(new CreatureLineSegment(defender));
            segments.add(new MulticolorLineSegment(" senseless with " + this.creature.getHisHerItsString() + " hit!", (byte)0));
            
            MessageServer.broadcastColoredAction(segments, this.creature, defender, 5, true);
          }
        }
      }
      int numsound = Server.rand.nextInt(3);
      if (defdamage > 10000.0D)
      {
        if (numsound == 0) {
          SoundPlayer.playSound("sound.combat.fleshbone1", defender, 1.6F);
        } else if (numsound == 1) {
          SoundPlayer.playSound("sound.combat.fleshbone2", defender, 1.6F);
        } else if (numsound == 2) {
          SoundPlayer.playSound("sound.combat.fleshbone3", defender, 1.6F);
        }
      }
      else if (metalArmour)
      {
        if (numsound == 0) {
          SoundPlayer.playSound("sound.combat.fleshmetal1", defender, 1.6F);
        } else if (numsound == 1) {
          SoundPlayer.playSound("sound.combat.fleshmetal2", defender, 1.6F);
        } else if (numsound == 2) {
          SoundPlayer.playSound("sound.combat.fleshmetal3", defender, 1.6F);
        }
      }
      else if (numsound == 0) {
        SoundPlayer.playSound("sound.combat.fleshhit1", defender, 1.6F);
      } else if (numsound == 1) {
        SoundPlayer.playSound("sound.combat.fleshhit2", defender, 1.6F);
      } else if (numsound == 2) {
        SoundPlayer.playSound("sound.combat.fleshhit3", defender, 1.6F);
      }
      SoundPlayer.playSound(defender.getHitSound(), defender, 1.6F);
    }
    else
    {
      if ((aiming) || (this.creature.spamMode()))
      {
        ArrayList<MulticolorLineSegment> segments = new ArrayList();
        segments.add(new CreatureLineSegment(defender));
        segments.add(new MulticolorLineSegment(" takes no real damage from the hit to the " + defender
          .getBody().getWoundLocationString(position) + ".", (byte)0));
        
        this.creature.getCommunicator().sendColoredMessageCombat(segments);
      }
      if (defender.spamMode()) {
        defender.getCommunicator().sendCombatNormalMessage("You take no real damage from the blow to the " + defender
          .getBody().getWoundLocationString(position) + ".");
      }
      this.creature.sendToLoggers(defender.getName() + " NO DAMAGE", (byte)2);
      defender.sendToLoggers("YOU TAKE NO DAMAGE", (byte)2);
    }
    return dead;
  }
  
  private static void setDefenderWeaponSkill(Item defPrimWeapon)
  {
    int skillnum = -10;
    if (defPrimWeapon != null) {
      if (defPrimWeapon.isBodyPart()) {
        try
        {
          skillnum = 10052;
          defPrimWeaponSkill = defenderSkills.getSkill(skillnum);
        }
        catch (NoSuchSkillException nss)
        {
          if (skillnum != -10) {
            defPrimWeaponSkill = defenderSkills.learn(skillnum, 1.0F);
          }
        }
      } else {
        try
        {
          skillnum = defPrimWeapon.getPrimarySkill();
          defPrimWeaponSkill = defenderSkills.getSkill(skillnum);
        }
        catch (NoSuchSkillException nss)
        {
          if (skillnum != -10) {
            defPrimWeaponSkill = defenderSkills.learn(skillnum, 1.0F);
          }
        }
      }
    }
  }
  
  private static float getWeaponParryBonus(Item weapon)
  {
    if (weapon.isWeaponSword()) {
      return 2.0F;
    }
    return 1.0F;
  }
  
  private float checkDefenderParry(Creature defender, Item attWeapon)
  {
    defCheck = 0.0D;
    boolean parried = false;
    
    int parryTime = 200;
    if (defender.getFightStyle() == 2) {
      parryTime = 120;
    } else if (defender.getFightStyle() == 1) {
      parryTime = 360;
    }
    parryBonus = getParryBonus(defender.getCombatHandler().currentStance, this.currentStance);
    if (defender.fightlevel > 0) {
      parryBonus -= defender.fightlevel * 4 / 100.0F;
    }
    if (defender.getPrimWeapon() != null) {
      parryBonus *= Weapon.getMaterialParryBonus(defender.getPrimWeapon().getMaterial());
    }
    parryTime = (int)(parryTime * parryBonus);
    if (WurmCalendar.currentTime > defender.lastParry + Server.rand.nextInt(parryTime))
    {
      defParryWeapon = defender.getPrimWeapon();
      if (Weapon.getWeaponParryPercent(defParryWeapon) > 0.0F)
      {
        if ((defParryWeapon.isTwoHanded()) && (defShield != null))
        {
          defParryWeapon = null;
          parried = false;
        }
        else
        {
          parried = true;
        }
      }
      else {
        defParryWeapon = null;
      }
      if (((!parried) || (Server.rand.nextInt(3) == 0)) && (defShield == null))
      {
        defLeftWeapon = defender.getLefthandWeapon();
        if (defLeftWeapon != defParryWeapon)
        {
          if (defLeftWeapon != null) {
            if ((defLeftWeapon.getSizeZ() > defender.getSize() * 10) || 
              (Weapon.getWeaponParryPercent(defLeftWeapon) <= 0.0F)) {
              defLeftWeapon = null;
            }
          }
          if (defLeftWeapon != null) {
            if ((defParryWeapon != null) && (parried))
            {
              if (defLeftWeapon.getSizeZ() > defParryWeapon.getSizeZ()) {
                defParryWeapon = defLeftWeapon;
              }
            }
            else {
              defParryWeapon = defLeftWeapon;
            }
          }
        }
      }
      if ((defParryWeapon != null) && (Weapon.getWeaponParryPercent(defParryWeapon) > Server.rand.nextFloat()))
      {
        defCheck = -1.0D;
        if (defender.getStatus().getStamina() >= 300)
        {
          setDefenderWeaponSkill(defParryWeapon);
          if (defPrimWeaponSkill != null) {
            if ((!defender.isMoving()) || (defPrimWeaponSkill.getRealKnowledge() > 40.0D))
            {
              double pdiff = Math.max(1.0D, 
                (attCheck - defBonus + defParryWeapon.getWeightGrams() / 100.0F) / getWeaponParryBonus(defParryWeapon) * (1.0D - 
                getParryMod()));
              if (!defender.isPlayer()) {
                pdiff *= defender.getStatus().getParryTypeModifier();
              }
              defCheck = defPrimWeaponSkill.skillCheck(pdiff * ItemBonus.getParryBonus(defender, defParryWeapon), defParryWeapon, 0.0D, (this.creature.isNoSkillFor(defender)) || 
                (defParryWeapon.isWeaponBow()), 1.0F, defender, this.creature);
              defender.lastParry = WurmCalendar.currentTime;
              defender.getStatus().modifyStamina(-300.0F);
            }
          }
          if ((defCheck < 0.0D) && (Server.rand.nextInt(20) == 0)) {
            if ((defLeftWeapon != null) && (!defLeftWeapon.equals(defParryWeapon)))
            {
              setDefenderWeaponSkill(defLeftWeapon);
              if ((!defender.isMoving()) || (defPrimWeaponSkill.getRealKnowledge() > 40.0D))
              {
                double pdiff = Math.max(1.0D, (attCheck - defBonus + defLeftWeapon.getWeightGrams() / 100.0F) / 
                  getWeaponParryBonus(defLeftWeapon) * getParryMod());
                pdiff *= defender.getStatus().getParryTypeModifier();
                defCheck = defPrimWeaponSkill.skillCheck(pdiff * ItemBonus.getParryBonus(defender, defParryWeapon), defLeftWeapon, 0.0D, 
                  (this.creature.isNoSkillFor(defender)) || (defParryWeapon.isWeaponBow()), 1.0F, defender, this.creature);
                defender.lastParry = WurmCalendar.currentTime;
                defender.getStatus().modifyStamina(-300.0F);
              }
            }
          }
          if (defCheck > 0.0D) {
            setParryEffects(defender, attWeapon, defCheck);
          }
        }
      }
    }
    return (float)defCheck;
  }
  
  private void setParryEffects(Creature defender, Item attWeapon, double parryEff)
  {
    defender.lastParry = WurmCalendar.currentTime;
    if ((aiming) || (this.creature.spamMode()))
    {
      ArrayList<MulticolorLineSegment> segments = new ArrayList();
      segments.add(new CreatureLineSegment(defender));
      segments.add(new MulticolorLineSegment(" " + CombatEngine.getParryString(parryEff) + " parries with " + defParryWeapon
        .getNameWithGenus() + ".", (byte)0));
      
      this.creature.getCommunicator().sendColoredMessageCombat(segments);
    }
    if (defender.spamMode()) {
      defender.getCommunicator().sendCombatNormalMessage("You " + 
        CombatEngine.getParryString(parryEff) + " parry with your " + defParryWeapon.getName() + ".");
    }
    if ((!defParryWeapon.isBodyPart()) || (defParryWeapon.getAuxData() == 100))
    {
      float vulnerabilityModifier = 1.0F;
      if (defender.isPlayer())
      {
        if ((attWeapon.isMetal()) && (Weapon.isWeaponDamByMetal(defParryWeapon))) {
          vulnerabilityModifier = 4.0F;
        }
        if (defParryWeapon.isWeaponSword()) {
          defParryWeapon.setDamage(defParryWeapon.getDamage() + 1.0E-7F * (float)damage * defParryWeapon
            .getDamageModifier() * vulnerabilityModifier);
        } else {
          defParryWeapon.setDamage(defParryWeapon.getDamage() + 2.0E-7F * (float)damage * defParryWeapon
            .getDamageModifier() * vulnerabilityModifier);
        }
      }
      if (this.creature.isPlayer())
      {
        vulnerabilityModifier = 1.0F;
        if ((defParryWeapon.isMetal()) && (Weapon.isWeaponDamByMetal(attWeapon))) {
          vulnerabilityModifier = 4.0F;
        }
        if ((!attWeapon.isBodyPartAttached() ? 1 : 0) == 0) {
          attWeapon.setDamage(attWeapon.getDamage() + 1.0E-7F * (float)damage * attWeapon.getDamageModifier() * vulnerabilityModifier);
        }
      }
    }
    this.creature.sendToLoggers(defender.getName() + " PARRY " + parryEff, (byte)2);
    defender.sendToLoggers("YOU PARRY " + parryEff, (byte)2);
    String lSstring = getParrySound(Server.rand);
    SoundPlayer.playSound(lSstring, defender, 1.6F);
    CombatEngine.checkEnchantDestruction(attWeapon, defParryWeapon, defender);
    defender.playAnimation("parry.weapon", false);
  }
  
  static String getParrySound(Random aRandom)
  {
    int x = aRandom.nextInt(3);
    String lSstring;
    String lSstring;
    if (x == 0)
    {
      lSstring = "sound.combat.parry2";
    }
    else
    {
      String lSstring;
      if (x == 1) {
        lSstring = "sound.combat.parry3";
      } else {
        lSstring = "sound.combat.parry1";
      }
    }
    return lSstring;
  }
  
  public void increaseUseShieldCounter()
  {
    this.usedShieldThisRound += 1;
  }
  
  private float checkShield(Creature defender, Item weapon)
  {
    if (defender.getCombatHandler().usedShieldThisRound > 1) {
      return 0.0F;
    }
    defShield = defender.getShield();
    defCheck = 0.0D;
    
    float blockPercent = 0.0F;
    if (defShield != null)
    {
      Item defweapon = defender.getPrimWeapon();
      if ((defweapon != null) && (defweapon.isTwoHanded())) {
        return 0.0F;
      }
      Item defSecondWeapon = defender.getLefthandWeapon();
      if ((defSecondWeapon != null) && (defSecondWeapon.isTwoHanded())) {
        return 0.0F;
      }
      if (!defShield.isArtifact()) {
        defender.getCombatHandler().usedShieldThisRound += 1;
      }
      if (VirtualZone.isCreatureShieldedVersusTarget(this.creature, defender))
      {
        int skillnum = -10;
        Skill defShieldSkill = null;
        try
        {
          skillnum = defShield.getPrimarySkill();
          defShieldSkill = defenderSkills.getSkill(skillnum);
        }
        catch (NoSuchSkillException nss)
        {
          if (skillnum != -10) {
            defShieldSkill = defenderSkills.learn(skillnum, 1.0F);
          }
        }
        if (defShieldSkill != null)
        {
          if (pos == 9)
          {
            blockPercent = 100.0F;
            if ((defender.spamMode()) && (Servers.isThisATestServer())) {
              defender.getCommunicator().sendCombatNormalMessage("Blocking left underarm.");
            }
          }
          else if (((defender.getStatus().getStamina() >= 300) || (Server.rand.nextInt(10) == 0)) && (
            (!defender.isMoving()) || (defShieldSkill.getRealKnowledge() > 40.0D)))
          {
            double shieldModifier = (defShield.getSizeY() + defShield.getSizeZ()) / 2.0F * (defShield.getCurrentQualityLevel() / 100.0F);
            
            double diff = Math.max(1.0D, chanceToHit - shieldModifier) - defBonus;
            
            blockPercent = (float)defShieldSkill.skillCheck(diff, defShield, defShield.isArtifact() ? 50.0D : 0.0D, 
              (this.creature.isNoSkillFor(defender)) || (defender.getCombatHandler().receivedShieldSkill), (float)(damage / 1000.0D), defender, this.creature);
            
            defender.getCombatHandler().receivedShieldSkill = true;
            if ((defender.spamMode()) && (Servers.isThisATestServer())) {
              defender.getCommunicator().sendCombatNormalMessage("Shield parrying difficulty=" + diff + " including defensive bonus " + defBonus + " vs " + defShieldSkill
              
                .getKnowledge(defShield, 0.0D) + " " + defender.zoneBonus + ":" + defender
                .getMovePenalty() + " gave " + blockPercent + ">0");
            }
            defender.getStatus().modifyStamina((int)(-300.0F - defShield.getWeightGrams() / 20.0F));
          }
          if (blockPercent > 0.0F)
          {
            float damageMod;
            float damageMod;
            if ((!weapon.isBodyPart()) && (weapon.isWeaponCrush()))
            {
              damageMod = 1.5E-5F;
            }
            else
            {
              float damageMod;
              if (type == 0) {
                damageMod = 1.0E-6F;
              } else {
                damageMod = 5.0E-6F;
              }
            }
            if (defender.isPlayer()) {
              defShield.setDamage(defShield.getDamage() + 
                Math.max(0.01F, damageMod * (float)damage * defShield.getDamageModifier()));
            }
            sendShieldMessage(defender, weapon, blockPercent);
          }
        }
      }
    }
    return blockPercent;
  }
  
  private void sendShieldMessage(Creature defender, Item weapon, float blockPercent)
  {
    ArrayList<MulticolorLineSegment> segments = new ArrayList();
    segments.add(new CreatureLineSegment(defender));
    segments.add(new MulticolorLineSegment(" raises " + defender.getHisHerItsString() + " shield and parries your " + attString + ".", (byte)0));
    if ((aiming) || (this.creature.spamMode())) {
      this.creature.getCommunicator().sendColoredMessageCombat(segments);
    }
    if (defender.spamMode())
    {
      ((MulticolorLineSegment)segments.get(1)).setText(" raise your shield and parry against ");
      segments.add(new CreatureLineSegment(this.creature));
      segments.add(new MulticolorLineSegment("'s " + attString + ".", (byte)0));
      
      defender.getCommunicator().sendColoredMessageCombat(segments);
    }
    if (defShield.isWood()) {
      Methods.sendSound(defender, "sound.combat.shield.wood");
    } else {
      Methods.sendSound(defender, "sound.combat.shield.metal");
    }
    CombatEngine.checkEnchantDestruction(weapon, defShield, defender);
    this.creature.sendToLoggers(defender.getName() + " SHIELD " + blockPercent, (byte)2);
    defender.sendToLoggers("You SHIELD " + blockPercent, (byte)2);
    defender.playAnimation("parry.shield", false);
  }
  
  private void sendDodgeMessage(Creature defender)
  {
    double power = (float)(defender.getBodyControl() / 3.0D - defCheck);
    String sstring;
    String sstring;
    if (power > 20.0D)
    {
      sstring = "sound.combat.miss.heavy";
    }
    else
    {
      String sstring;
      if (power > 10.0D) {
        sstring = "sound.combat.miss.med";
      } else {
        sstring = "sound.combat.miss.light";
      }
    }
    SoundPlayer.playSound(sstring, this.creature, 1.6F);
    
    ArrayList<MulticolorLineSegment> segments = new ArrayList();
    segments.add(new CreatureLineSegment(defender));
    segments.add(new MulticolorLineSegment(" " + CombatEngine.getParryString(power) + " evades the blow to the " + defender
      .getBody().getWoundLocationString(pos) + ".", (byte)0));
    if ((aiming) || (this.creature.spamMode())) {
      this.creature.getCommunicator().sendColoredMessageCombat(segments);
    }
    if (defender.spamMode())
    {
      ((MulticolorLineSegment)segments.get(1)).setText(" " + CombatEngine.getParryString(power) + " evade the blow to the " + defender
        .getBody().getWoundLocationString(pos) + ".");
      defender.getCommunicator().sendColoredMessageCombat(segments);
    }
    this.creature.sendToLoggers(defender.getName() + " EVADE", (byte)2);
    defender.sendToLoggers("You EVADE", (byte)2);
    defender.playAnimation("dodge", false);
  }
  
  private final double getDamage(Creature _creature, AttackAction attk, Creature opponent)
  {
    Skill attStrengthSkill = null;
    try
    {
      attStrengthSkill = _creature.getSkills().getSkill(102);
    }
    catch (NoSuchSkillException nss)
    {
      attStrengthSkill = _creature.getSkills().learn(102, 1.0F);
      logger.log(Level.WARNING, _creature.getName() + " had no strength. Weird.");
    }
    Item weapon = _creature.getPrimWeapon(!attk.isUsingWeapon());
    if (!attk.isUsingWeapon())
    {
      damage = attk.getAttackValues().getBaseDamage() * 1000.0F * _creature.getStatus().getDamageTypeModifier();
      if (_creature.isPlayer())
      {
        Skill weaponLess = _creature.getWeaponLessFightingSkill();
        
        double modifier = 1.0D + 2.0D * weaponLess.getKnowledge() / 100.0D;
        damage *= modifier;
      }
      if ((damage < 10000.0D) && (_creature.getBonusForSpellEffect((byte)24) > 0.0F)) {
        if (_creature.isPlayer()) {
          damage += Server.getBuffedQualityEffect(_creature.getBonusForSpellEffect((byte)24) / 100.0F) * 15000.0D;
        } else {
          damage += Server.getBuffedQualityEffect(_creature.getBonusForSpellEffect((byte)24) / 100.0F) * 5000.0D;
        }
      }
      float randomizer = (50.0F + Server.rand.nextFloat() * 50.0F) / 100.0F;
      damage *= randomizer;
    }
    else
    {
      damage = Weapon.getModifiedDamageForWeapon(weapon, attStrengthSkill, opponent.getTemplate().getTemplateId() == 116) * 1000.0D;
      
      damage = damage + Server.getBuffedQualityEffect(weapon.getCurrentQualityLevel() / 100.0F) * Weapon.getBaseDamageForWeapon(weapon) * 2400.0D;
      
      damage *= Weapon.getMaterialDamageBonus(weapon.getMaterial());
      if ((!opponent.isPlayer()) && (opponent.isHunter())) {
        damage *= Weapon.getMaterialHunterDamageBonus(weapon.getMaterial());
      }
      damage *= ItemBonus.getWeaponDamageIncreaseBonus(_creature, weapon);
      
      damage *= (1.0F + weapon.getCurrentQualityLevel() / 100.0F * (weapon.getSpellExtraDamageBonus() / 30000.0F));
    }
    if ((_creature.getEnemyPresense() > 1200) && (opponent.isPlayer()) && (!weapon.isArtifact())) {
      damage *= 1.149999976158142D;
    }
    if ((!weapon.isArtifact()) && (this.hasRodEffect) && (opponent.isPlayer())) {
      damage *= 1.2000000476837158D;
    }
    Vehicle vehicle = Vehicles.getVehicleForId(opponent.getVehicle());
    boolean mildStack = false;
    if ((weapon.isWeaponPolearm()) && (((vehicle != null) && 
      (vehicle.isCreature())) || ((opponent.isRidden()) && (weapon.isWeaponPierce()))))
    {
      damage *= 1.7000000476837158D;
    }
    else if (weapon.isArtifact())
    {
      mildStack = true;
    }
    else if ((_creature.getCultist() != null) && (_creature.getCultist().doubleWarDamage()))
    {
      damage *= 1.5D;
      mildStack = true;
    }
    else if ((_creature.getDeity() != null) && (_creature.getDeity().isWarrior()))
    {
      if ((_creature.getFaith() >= 40.0F) && (_creature.getFavor() >= 20.0F))
      {
        damage *= 1.149999976158142D;
        mildStack = true;
      }
    }
    if (_creature.isPlayer())
    {
      if ((_creature.getFightStyle() != 2) || (attStrengthSkill.getRealKnowledge() < 20.0D)) {
        if (attStrengthSkill.getRealKnowledge() != 20.0D) {
          damage *= (1.0D + (attStrengthSkill.getRealKnowledge() - 20.0D) / 200.0D);
        }
      }
      if (this.currentStrength == 0)
      {
        Skill fstyle = null;
        try
        {
          fstyle = _creature.getSkills().getSkill(10054);
        }
        catch (NoSuchSkillException nss)
        {
          fstyle = _creature.getSkills().learn(10054, 1.0F);
        }
        if (fstyle.skillCheck(opponent.getBaseCombatRating() * 3.0F, 0.0D, (this.receivedFStyleSkill) || 
          (opponent.isNoSkillFor(_creature)), 10.0F, _creature, opponent) > 0.0D)
        {
          this.receivedFStyleSkill = true;
          damage *= 0.800000011920929D;
        }
        else
        {
          damage *= 0.5D;
        }
      }
      if (_creature.getStatus().getStamina() > 2000) {
        if ((this.currentStrength >= 1) && (!this.receivedFStyleSkill))
        {
          int num = 10053;
          if (this.currentStrength == 1) {
            num = 10055;
          }
          Skill fstyle = null;
          try
          {
            fstyle = _creature.getSkills().getSkill(num);
          }
          catch (NoSuchSkillException nss)
          {
            fstyle = _creature.getSkills().learn(num, 1.0F);
          }
          if (fstyle.skillCheck(opponent.getBaseCombatRating() * 3.0F, 0.0D, (this.receivedFStyleSkill) || 
            (opponent.isNoSkillFor(_creature)), 10.0F, _creature, opponent) > 0.0D)
          {
            this.receivedFStyleSkill = true;
            if (this.currentStrength > 1) {
              damage *= (1.0D + Server.getModifiedFloatEffect(fstyle.getRealKnowledge() / 100.0D) / (mildStack ? 8.0F : 4.0F));
            }
          }
        }
      }
      float knowl = 1.0F;
      try
      {
        Skill wSkill = _creature.getSkills().getSkill(weapon.getPrimarySkill());
        knowl = (float)wSkill.getRealKnowledge();
      }
      catch (NoSuchSkillException localNoSuchSkillException1) {}
      if (knowl < 50.0F) {
        damage = 0.800000011920929D * damage + 0.2D * (knowl / 50.0F * damage);
      }
    }
    else
    {
      damage *= (0.85F + this.currentStrength * 0.15F);
    }
    if (_creature.isStealth()) {
      if (_creature.opponent != null) {
        if (!_creature.isVisibleTo(opponent))
        {
          ArrayList<MulticolorLineSegment> segments = new ArrayList();
          segments.add(new CreatureLineSegment(_creature));
          segments.add(new MulticolorLineSegment(" backstab ", (byte)0));
          segments.add(new CreatureLineSegment(opponent));
          
          _creature.getCommunicator().sendColoredMessageCombat(segments);
          
          damage = Math.min(50000.0D, damage * 4.0D);
        }
      }
    }
    if (_creature.getCitizenVillage() != null) {
      if (_creature.getCitizenVillage().getFaithWarBonus() > 0.0F) {
        damage *= (1.0F + _creature.getCitizenVillage().getFaithWarBonus() / 100.0F);
      }
    }
    if (_creature.fightlevel >= 4) {
      damage *= 1.100000023841858D;
    }
    return damage;
  }
  
  private final double getDamage(Creature _creature, Item weapon, Creature opponent)
  {
    Skill attStrengthSkill = null;
    try
    {
      attStrengthSkill = _creature.getSkills().getSkill(102);
    }
    catch (NoSuchSkillException nss)
    {
      attStrengthSkill = _creature.getSkills().learn(102, 1.0F);
      logger.log(Level.WARNING, _creature.getName() + " had no strength. Weird.");
    }
    if (weapon.isBodyPartAttached())
    {
      damage = _creature.getCombatDamage(weapon) * 1000.0F * _creature.getStatus().getDamageTypeModifier();
      if (_creature.isPlayer())
      {
        Skill weaponLess = _creature.getWeaponLessFightingSkill();
        
        double modifier = 1.0D + 2.0D * weaponLess.getKnowledge() / 100.0D;
        damage *= modifier;
      }
      if ((damage < 10000.0D) && (_creature.getBonusForSpellEffect((byte)24) > 0.0F)) {
        if (_creature.isPlayer()) {
          damage += Server.getBuffedQualityEffect(_creature.getBonusForSpellEffect((byte)24) / 100.0F) * 15000.0D;
        } else {
          damage += Server.getBuffedQualityEffect(_creature.getBonusForSpellEffect((byte)24) / 100.0F) * 5000.0D;
        }
      }
      float randomizer = (50.0F + Server.rand.nextFloat() * 50.0F) / 100.0F;
      damage *= randomizer;
    }
    else
    {
      damage = Weapon.getModifiedDamageForWeapon(weapon, attStrengthSkill, opponent.getTemplate().getTemplateId() == 116) * 1000.0D;
      if (!Servers.isThisAnEpicOrChallengeServer()) {
        damage += weapon.getCurrentQualityLevel() / 100.0F * weapon.getSpellExtraDamageBonus();
      }
      damage = damage + Server.getBuffedQualityEffect(weapon.getCurrentQualityLevel() / 100.0F) * Weapon.getBaseDamageForWeapon(weapon) * 2400.0D;
      
      damage *= Weapon.getMaterialDamageBonus(weapon.getMaterial());
      if ((!opponent.isPlayer()) && (opponent.isHunter())) {
        damage *= Weapon.getMaterialHunterDamageBonus(weapon.getMaterial());
      }
      damage *= ItemBonus.getWeaponDamageIncreaseBonus(_creature, weapon);
      if (Servers.isThisAnEpicOrChallengeServer()) {
        damage *= (1.0F + weapon.getCurrentQualityLevel() / 100.0F * weapon.getSpellExtraDamageBonus() / 30000.0F);
      }
    }
    if ((_creature.getEnemyPresense() > 1200) && (opponent.isPlayer()) && (!weapon.isArtifact())) {
      damage *= 1.149999976158142D;
    }
    if ((!weapon.isArtifact()) && (this.hasRodEffect) && (opponent.isPlayer())) {
      damage *= 1.2000000476837158D;
    }
    Vehicle vehicle = Vehicles.getVehicleForId(opponent.getVehicle());
    boolean mildStack = false;
    if ((weapon.isWeaponPolearm()) && (((vehicle != null) && 
      (vehicle.isCreature())) || ((opponent.isRidden()) && (weapon.isWeaponPierce()))))
    {
      damage *= 1.7000000476837158D;
    }
    else if (weapon.isArtifact())
    {
      mildStack = true;
    }
    else if ((_creature.getCultist() != null) && (_creature.getCultist().doubleWarDamage()))
    {
      damage *= 1.5D;
      mildStack = true;
    }
    else if ((_creature.getDeity() != null) && (_creature.getDeity().isWarrior()))
    {
      if ((_creature.getFaith() >= 40.0F) && (_creature.getFavor() >= 20.0F))
      {
        damage *= 1.149999976158142D;
        mildStack = true;
      }
    }
    if (_creature.isPlayer())
    {
      if ((_creature.getFightStyle() != 2) || (attStrengthSkill.getRealKnowledge() < 20.0D)) {
        if (attStrengthSkill.getRealKnowledge() != 20.0D) {
          damage *= (1.0D + (attStrengthSkill.getRealKnowledge() - 20.0D) / 200.0D);
        }
      }
      if (this.currentStrength == 0)
      {
        Skill fstyle = null;
        try
        {
          fstyle = _creature.getSkills().getSkill(10054);
        }
        catch (NoSuchSkillException nss)
        {
          fstyle = _creature.getSkills().learn(10054, 1.0F);
        }
        if (fstyle.skillCheck(opponent.getBaseCombatRating() * 3.0F, 0.0D, (this.receivedFStyleSkill) || 
          (opponent.isNoSkillFor(_creature)), 10.0F, _creature, opponent) > 0.0D)
        {
          this.receivedFStyleSkill = true;
          damage *= 0.800000011920929D;
        }
        else
        {
          damage *= 0.5D;
        }
      }
      if (_creature.getStatus().getStamina() > 2000) {
        if ((this.currentStrength >= 1) && (!this.receivedFStyleSkill))
        {
          int num = 10053;
          if (this.currentStrength == 1) {
            num = 10055;
          }
          Skill fstyle = null;
          try
          {
            fstyle = _creature.getSkills().getSkill(num);
          }
          catch (NoSuchSkillException nss)
          {
            fstyle = _creature.getSkills().learn(num, 1.0F);
          }
          if (fstyle.skillCheck(opponent.getBaseCombatRating() * 3.0F, 0.0D, (this.receivedFStyleSkill) || 
            (opponent.isNoSkillFor(_creature)), 10.0F, _creature, opponent) > 0.0D)
          {
            this.receivedFStyleSkill = true;
            if (this.currentStrength > 1) {
              damage *= (1.0D + Server.getModifiedFloatEffect(fstyle.getRealKnowledge() / 100.0D) / (mildStack ? 8.0F : 4.0F));
            }
          }
        }
      }
      float knowl = 1.0F;
      try
      {
        Skill wSkill = _creature.getSkills().getSkill(weapon.getPrimarySkill());
        knowl = (float)wSkill.getRealKnowledge();
      }
      catch (NoSuchSkillException localNoSuchSkillException1) {}
      if (knowl < 50.0F) {
        damage = 0.800000011920929D * damage + 0.2D * (knowl / 50.0F * damage);
      }
    }
    else
    {
      damage *= (0.85F + this.currentStrength * 0.15F);
    }
    if (_creature.isStealth()) {
      if (_creature.opponent != null) {
        if (!_creature.isVisibleTo(opponent))
        {
          ArrayList<MulticolorLineSegment> segments = new ArrayList();
          segments.add(new CreatureLineSegment(_creature));
          segments.add(new MulticolorLineSegment(" backstab ", (byte)0));
          segments.add(new CreatureLineSegment(opponent));
          
          _creature.getCommunicator().sendColoredMessageCombat(segments);
          
          damage = Math.min(50000.0D, damage * 4.0D);
        }
      }
    }
    if (_creature.getCitizenVillage() != null) {
      if (_creature.getCitizenVillage().getFaithWarBonus() > 0.0F) {
        damage *= (1.0F + _creature.getCitizenVillage().getFaithWarBonus() / 100.0F);
      }
    }
    if (_creature.fightlevel >= 4) {
      damage *= 1.100000023841858D;
    }
    return damage;
  }
  
  public byte getType(Item weapon, boolean rawType)
  {
    byte woundType = this.creature.getCombatDamageType();
    if ((weapon.isWeaponSword()) || (weapon.getTemplateId() == 706))
    {
      if ((rawType) || (Server.rand.nextInt(2) == 0)) {
        woundType = 1;
      } else {
        woundType = 2;
      }
    }
    else if (weapon.getTemplateId() == 1115)
    {
      if ((rawType) || (Server.rand.nextInt(3) == 0)) {
        woundType = 2;
      } else {
        woundType = 0;
      }
    }
    else if (weapon.isWeaponSlash()) {
      woundType = 1;
    } else if (weapon.isWeaponPierce()) {
      woundType = 2;
    } else if (weapon.isWeaponCrush()) {
      woundType = 0;
    } else if (weapon.isBodyPart()) {
      if (weapon.getTemplateId() == 17) {
        woundType = 3;
      } else if (weapon.getTemplateId() == 12) {
        woundType = 0;
      }
    }
    type = woundType;
    return woundType;
  }
  
  private float getWeaponSpeed(Item _weapon)
  {
    float flspeed = 20.0F;
    float knowl = 0.0F;
    int spskillnum = 10052;
    if (_weapon.isBodyPartAttached())
    {
      flspeed = this.creature.getBodyWeaponSpeed(_weapon);
    }
    else
    {
      flspeed = Weapon.getBaseSpeedForWeapon(_weapon);
      try
      {
        spskillnum = _weapon.getPrimarySkill();
      }
      catch (NoSuchSkillException localNoSuchSkillException) {}
    }
    try
    {
      Skill wSkill = this.creature.getSkills().getSkill(spskillnum);
      knowl = (float)wSkill.getRealKnowledge();
    }
    catch (NoSuchSkillException localNoSuchSkillException1) {}
    if (!this.creature.isGhost()) {
      flspeed -= flspeed * 0.1F * knowl / 100.0F;
    }
    return flspeed;
  }
  
  private float getWeaponSpeed(AttackAction act, Item _weapon)
  {
    float flspeed = 20.0F;
    float knowl = 0.0F;
    int spskillnum = 10052;
    if (!act.isUsingWeapon())
    {
      flspeed = act.getAttackValues().getBaseSpeed();
    }
    else
    {
      flspeed = act.getAttackValues().getBaseSpeed();
      try
      {
        spskillnum = _weapon.getPrimarySkill();
      }
      catch (NoSuchSkillException localNoSuchSkillException) {}
    }
    try
    {
      Skill wSkill = this.creature.getSkills().getSkill(spskillnum);
      knowl = (float)wSkill.getRealKnowledge();
    }
    catch (NoSuchSkillException localNoSuchSkillException1) {}
    if (!this.creature.isGhost()) {
      flspeed -= flspeed * 0.1F * knowl / 100.0F;
    }
    return flspeed;
  }
  
  public final void setHasSpiritFervor(boolean hasFervor)
  {
    this.hasSpiritFervor = hasFervor;
  }
  
  public float getCombatRating(Creature opponent, Item weapon, boolean attacking)
  {
    float combatRating = this.creature.getBaseCombatRating();
    if (this.hasSpiritFervor) {
      combatRating += 1.0F;
    }
    if ((this.creature.isKing()) && (this.creature.isEligibleForKingdomBonus())) {
      combatRating += 3.0F;
    }
    if (this.creature.hasTrait(0)) {
      combatRating += 1.0F;
    }
    if (attacking) {
      combatRating += 1.0F + this.creature.getBonusForSpellEffect((byte)30) / 30.0F;
    } else {
      combatRating += 1.0F + this.creature.getBonusForSpellEffect((byte)28) / 30.0F;
    }
    if ((this.creature.getDeity() != null) && (this.creature.getFaith() > 70.0F))
    {
      MeshIO mesh = Server.surfaceMesh;
      if (this.creature.getLayer() < 0) {
        mesh = Server.caveMesh;
      }
      int tile = mesh.getTile(this.creature.getCurrentTile().getTileX(), this.creature.getCurrentTile().getTileY());
      byte type = Tiles.decodeType(tile);
      if (this.creature.getDeity().isFo())
      {
        Tiles.Tile theTile = Tiles.getTile(type);
        if ((theTile.isNormalTree()) || (theTile.isMyceliumTree()) || (type == Tiles.Tile.TILE_GRASS.id) || (type == Tiles.Tile.TILE_FIELD.id) || (type == Tiles.Tile.TILE_FIELD2.id) || (type == Tiles.Tile.TILE_DIRT.id) || (type == Tiles.Tile.TILE_TUNDRA.id)) {
          combatRating += 1.0F;
        }
      }
      if ((this.creature.getDeity().isMagranon()) || (this.creature.getDeity().isLibila())) {
        if (attacking) {
          combatRating += 2.0F;
        }
      }
      if (this.creature.getDeity().isVynora()) {
        if (!attacking)
        {
          short height = Tiles.decodeHeight(tile);
          if (height < 0) {
            combatRating += 2.0F;
          } else if (Terraforming.isRoad(type)) {
            combatRating += 2.0F;
          }
        }
      }
    }
    if ((this.creature.getCultist() != null) && (this.creature.getCultist().hasFearEffect())) {
      combatRating += 2.0F;
    }
    if (this.creature.isPlayer())
    {
      int antiGankBonus = Math.max(0, this.creature.getAttackers() - 1);
      combatRating += antiGankBonus;
      this.creature.sendToLoggers("Adding " + antiGankBonus + " to combat rating due to attackers.");
    }
    if ((this.creature.isHorse()) && (this.creature.getLeader() != null) && (this.creature.getLeader().isPlayer())) {
      combatRating -= 5.0F;
    }
    if (this.creature.hasSpellEffect((byte)97)) {
      combatRating -= 4.0F;
    }
    if (this.creature.isSpiritGuard()) {
      if (Servers.localServer.isChallengeServer())
      {
        if ((opponent.isPlayer()) && (opponent.getKingdomId() != this.creature.getKingdomId())) {
          combatRating = 10.0F;
        }
      }
      else if ((this.creature.getCitizenVillage() != null) && 
        (this.creature.getCitizenVillage().plan.isUnderSiege())) {
        combatRating += this.creature.getCitizenVillage().plan.getSiegeCount() / 3;
      }
    }
    float bon = weapon.getSpellNimbleness();
    if (bon > 0.0F) {
      combatRating += bon / 30.0F;
    }
    if ((this.creature.isPlayer()) && (opponent.isPlayer()))
    {
      if ((this.creature.isRoyalExecutioner()) && (this.creature.isEligibleForKingdomBonus())) {
        combatRating += 2.0F;
      } else if (this.creature.hasCrownInfluence()) {
        combatRating += 1.0F;
      }
      combatRating += Players.getInstance().getCRBonus(this.creature.getKingdomId());
      if (this.creature.isInOwnDuelRing())
      {
        if (opponent.getKingdomId() != this.creature.getKingdomId()) {
          combatRating += 4.0F;
        }
      }
      else if (opponent.isInOwnDuelRing()) {
        if (opponent.getKingdomId() != this.creature.getKingdomId()) {
          combatRating -= 4.0F;
        }
      }
      if ((Servers.localServer.PVPSERVER) && (this.creature.getNumberOfFollowers() > 1)) {
        combatRating -= 10.0F;
      }
    }
    if ((this.creature.isPlayer()) && (this.creature.hasBattleCampBonus())) {
      combatRating += 3.0F;
    }
    combatRating += ItemBonus.getCRBonus(this.creature);
    
    float crmod = 1.0F;
    if (attacking)
    {
      if ((this.creature.isPlayer()) && (this.currentStrength >= 1)) {
        if (this.creature.getStatus().getStamina() > 2000)
        {
          int num = 10053;
          if (this.currentStrength == 1) {
            num = 10055;
          }
          Skill def = null;
          try
          {
            def = this.creature.getSkills().getSkill(num);
          }
          catch (NoSuchSkillException nss)
          {
            def = this.creature.getSkills().learn(num, 1.0F);
          }
          if (def.skillCheck(this.creature.getBaseCombatRating() * 2.0F, 0.0D, true, 10.0F, this.creature, opponent) > 0.0D) {
            combatRating = (float)(combatRating + this.currentStrength / 2.0F * Server.getModifiedFloatEffect(def.getRealKnowledge() / 100.0D));
          }
        }
      }
    }
    else if ((this.creature.isPlayer()) && (this.currentStrength > 1))
    {
      Skill def = null;
      try
      {
        def = this.creature.getSkills().getSkill(10053);
      }
      catch (NoSuchSkillException nss)
      {
        def = this.creature.getSkills().learn(10053, 1.0F);
      }
      if (def.skillCheck(Server.getModifiedFloatEffect(70.0D), 0.0D, true, 10.0F, this.creature, opponent) < 0.0D) {
        combatRating = (float)(combatRating - this.currentStrength * Server.getModifiedFloatEffect((100.0D - def.getRealKnowledge()) / 100.0D));
      }
    }
    if (this.creature.isPlayer())
    {
      combatRating = (float)(combatRating - Weapon.getSkillPenaltyForWeapon(weapon));
      combatRating += this.creature.getCRCounterBonus();
    }
    if (this.creature.isPlayer()) {
      if (opponent.isPlayer()) {
        combatRating = (float)(combatRating + this.creature.getFightingSkill().getKnowledge(0.0D) / 5.0D);
      } else {
        combatRating = (float)(combatRating + this.creature.getFightingSkill().getRealKnowledge() / 10.0D);
      }
    }
    if (this.battleratingPenalty > 0) {
      combatRating -= this.battleratingPenalty;
    }
    crmod *= getFlankingModifier(opponent);
    crmod *= getHeightModifier(opponent);
    crmod *= getAlcMod();
    if (this.creature.getCitizenVillage() != null) {
      crmod *= (1.0F + this.creature.getCitizenVillage().getFaithWarBonus() / 100.0F);
    }
    combatRating *= crmod;
    if (this.creature.fightlevel >= 3) {
      combatRating += this.creature.fightlevel * 2;
    }
    if (this.creature.isPlayer()) {
      combatRating *= Servers.localServer.getCombatRatingModifier();
    }
    combatRating *= getFootingModifier(weapon, opponent);
    if (this.creature.isOnHostileHomeServer()) {
      combatRating *= 0.7F;
    }
    if (isOpen()) {
      combatRating *= 0.7F;
    } else if (isProne()) {
      combatRating *= 0.5F;
    } else {
      try
      {
        Action act = this.creature.getCurrentAction();
        if (act.isVulnerable())
        {
          combatRating *= 0.5F;
        }
        else if (this.creature.isLinked())
        {
          Creature linkedTo = this.creature.getCreatureLinkedTo();
          if (linkedTo != null) {
            try
            {
              linkedTo.getCurrentAction().isSpell();
              combatRating *= 0.7F;
            }
            catch (NoSuchActionException localNoSuchActionException) {}
          }
        }
      }
      catch (NoSuchActionException localNoSuchActionException1) {}
    }
    if (this.creature.hasAttackedUnmotivated()) {
      combatRating = Math.min(4.0F, combatRating);
    }
    return normcr(combatRating);
  }
  
  private float getAlcMod()
  {
    if (this.creature.isPlayer())
    {
      float alc = 0.0F;
      alc = ((Player)this.creature).getAlcohol();
      if (alc < 20.0F) {
        return (100.0F + alc) / 100.0F;
      }
      return Math.max(40.0F, 100.0F - alc) / 80.0F;
    }
    return 1.0F;
  }
  
  private float normcr(float combatRating)
  {
    return Math.min(100.0F, Math.max(1.0F, combatRating));
  }
  
  static float getParryBonus(byte defenderStance, byte attackerStance)
  {
    if (isStanceParrying(defenderStance, attackerStance)) {
      return 0.8F;
    }
    if (isStanceOpposing(defenderStance, attackerStance)) {
      return 0.9F;
    }
    return 1.0F;
  }
  
  public float getChanceToHit(Creature opponent, Item weapon)
  {
    setBonuses(weapon, opponent);
    float myCR = getCombatRating(opponent, weapon, true);
    float oppCR = opponent.getCombatHandler().getCombatRating(this.creature, opponent.getPrimWeapon(), false);
    if (this.creature.isPlayer())
    {
      float distdiff = Math.abs(getDistdiff(weapon, this.creature, opponent));
      if (distdiff > 10.0F) {
        myCR -= 1.0F;
      }
      if (distdiff > 20.0F) {
        myCR -= 1.0F;
      }
    }
    parryBonus = getParryBonus(opponent.getCombatHandler().currentStance, this.currentStance);
    if (opponent.fightlevel > 0) {
      parryBonus -= opponent.fightlevel * 1 / 100.0F;
    }
    double m = 1.0D;
    if (attBonus != 0.0D) {
      m = 1.0D + attBonus / 100.0D;
    }
    Seat s = opponent.getSeat();
    if (s != null) {
      m *= s.cover;
    }
    float chance = (float)(normcr(myCR) / (normcr(oppCR) + normcr(myCR)) * m * parryBonus);
    float rest = Math.max(0.01F, 1.0F - chance);
    return 100.0F * Math.max(0.01F, (float)Server.getBuffedQualityEffect(1.0F - rest));
  }
  
  public void setBonuses(Item weapon, Creature defender)
  {
    attBonus = this.creature.zoneBonus - this.creature.getMovePenalty() * 0.5D;
    if (this.currentStrength == 0) {
      attBonus -= 20.0D;
    }
    defBonus = defender.zoneBonus - defender.getMovePenalty();
    if (this.addToSkills) {
      if ((defender.isPlayer()) && (defender.getCombatHandler().currentStrength == 0))
      {
        Skill def = null;
        try
        {
          def = defender.getSkills().getSkill(10054);
        }
        catch (NoSuchSkillException nss)
        {
          def = defender.getSkills().learn(10054, 1.0F);
        }
        if (defender.getStatus().getStamina() > 2000) {
          if (def.skillCheck(this.creature.getBaseCombatRating() * 2.0F, 0.0D, (this.creature.isNoSkillFor(defender)) || 
            (defender.getCombatHandler().receivedFStyleSkill), 10.0F, defender, this.creature) > 0.0D)
          {
            defender.getCombatHandler().receivedFStyleSkill = true;
            
            defBonus += def.getKnowledge(0.0D) / 4.0D;
          }
        }
      }
    }
    if ((defender.getCombatHandler().currentStrength > 0) && ((defender instanceof Player))) {
      if (defender.isMoving()) {
        defBonus -= defender.getCombatHandler().currentStrength * 15;
      } else if (defender.getCombatHandler().currentStrength > 1) {
        defBonus -= defender.getCombatHandler().currentStrength * 7;
      }
    }
    if (defender.isOnHostileHomeServer()) {
      defBonus -= 20.0D;
    } else if ((this.creature.isMoving()) && ((this.creature instanceof Player))) {
      attBonus -= 15.0D;
    }
  }
  
  private static final float getDistdiff(Creature creature, Creature opponent, AttackAction atk)
  {
    if ((atk != null) && (!atk.isUsingWeapon()))
    {
      float idealDist = 10 + atk.getAttackValues().getAttackReach() * 3;
      float dist = Creature.rangeToInDec(creature, opponent);
      
      return idealDist - dist;
    }
    Item wpn = creature.getPrimWeapon();
    return getDistdiff(wpn, creature, opponent);
  }
  
  private static final float getDistdiff(Item weapon, Creature creature, Creature opponent)
  {
    float idealDist = 10 + Weapon.getReachForWeapon(weapon) * 3;
    
    float dist = Creature.rangeToInDec(creature, opponent);
    return idealDist - dist;
  }
  
  private float getFootingModifier(Item weapon, Creature opponent)
  {
    short[] steepness = Creature.getTileSteepness(this.creature.getCurrentTile().tilex, this.creature.getCurrentTile().tiley, this.creature
      .isOnSurface());
    
    float footingMod = 0.0F;
    float heightDiff = 0.0F;
    
    heightDiff = Math.max(-1.45F, this.creature.getStatus().getPositionZ() + this.creature.getAltOffZ()) - Math.max(-1.45F, opponent.getStatus().getPositionZ() + opponent.getAltOffZ());
    if (heightDiff > 0.5D) {
      footingMod = (float)(footingMod + 0.1D);
    } else if (heightDiff < -0.5D) {
      footingMod -= 0.1F;
    }
    if (this.creature.isSubmerged()) {
      return 1.0F;
    }
    if (this.creature.getVehicle() == -10L)
    {
      if (weapon != null) {
        if ((opponent.getVehicle() != -10L) && (weapon.isTwoHanded()) && (!weapon.isWeaponBow())) {
          footingMod += 0.3F;
        }
      }
      if (this.creature.getStatus().getPositionZ() <= -1.45F) {
        return 0.2F + footingMod;
      }
      if ((this.creature.isPlayer()) && ((steepness[1] > 20) || (steepness[1] < -20)))
      {
        Skill bcskill = null;
        try
        {
          bcskill = this.creature.getSkills().getSkill(104);
        }
        catch (NoSuchSkillException nss)
        {
          bcskill = this.creature.getSkills().learn(104, 1.0F);
        }
        if (bcskill != null) {
          if (bcskill.skillCheck(Math.abs(Math.max(Math.min(steepness[1], 99), -99)), this.creature.fightlevel * 10, true, 1.0F) > 0.0D) {
            return 1.0F + footingMod;
          }
        }
        if ((steepness[1] > 40) || (steepness[1] < -40))
        {
          if ((steepness[1] > 60) || (steepness[1] < -60))
          {
            if ((steepness[1] > 80) || (steepness[1] < -80))
            {
              if ((steepness[1] > 100) || (steepness[1] < -100)) {
                return 0.2F + footingMod;
              }
              return 0.4F + footingMod;
            }
            return 0.6F + footingMod;
          }
          return 0.8F + footingMod;
        }
        return 0.9F + footingMod;
      }
    }
    else if (opponent.isSubmerged())
    {
      footingMod = 0.0F;
    }
    return 1.0F + footingMod;
  }
  
  private float getDirectionTo(Creature opponent)
  {
    float defAngle = Creature.normalizeAngle(opponent.getStatus().getRotation());
    double newrot = Math.atan2(this.creature.getStatus().getPositionY() - opponent.getStatus().getPositionY(), this.creature
      .getStatus().getPositionX() - opponent.getStatus().getPositionX());
    float attAngle = (float)(newrot * 57.29577951308232D) + 90.0F;
    return Creature.normalizeAngle(attAngle - defAngle);
  }
  
  private float getFlankingModifier(Creature opponent)
  {
    if (opponent == null) {
      return 1.0F;
    }
    float attAngle = getDirectionTo(opponent);
    if (opponent.getVehicle() > -10L)
    {
      Vehicle vehic = Vehicles.getVehicleForId(opponent.getVehicle());
      if ((vehic != null) && (vehic.isCreature())) {
        try
        {
          Creature ridden = Server.getInstance().getCreature(opponent.getVehicle());
          attAngle = getDirectionTo(ridden);
        }
        catch (Exception ex)
        {
          logger.log(Level.INFO, "No creature for id " + opponent.getVehicle());
        }
      }
    }
    if ((attAngle > 140.0F) && (attAngle < 220.0F))
    {
      if ((attAngle > 160.0F) && (attAngle < 200.0F)) {
        return 1.25F;
      }
      return 1.1F;
    }
    return 1.0F;
  }
  
  private float getHeightModifier(Creature opponent)
  {
    if (opponent == null) {
      return 1.0F;
    }
    float diff = this.creature.getPositionZ() + this.creature.getAltOffZ() - (opponent.getPositionZ() + opponent.getAltOffZ());
    if (diff > 1.0F)
    {
      if (diff > 2.0F) {
        return 1.1F;
      }
      return 1.05F;
    }
    if (diff < -1.0F)
    {
      if (diff < -2.0F) {
        return 0.9F;
      }
      return 0.95F;
    }
    return 1.0F;
  }
  
  public static final byte getStanceForAction(ActionEntry entry)
  {
    if (entry.isAttackHigh())
    {
      if (entry.isAttackLeft()) {
        return 6;
      }
      if (entry.isAttackRight()) {
        return 1;
      }
      return 7;
    }
    if (entry.isAttackLow())
    {
      if (entry.isAttackLeft()) {
        return 4;
      }
      if (entry.isAttackRight()) {
        return 3;
      }
      return 10;
    }
    if (entry.isAttackLeft()) {
      return 5;
    }
    if (entry.isAttackRight()) {
      return 2;
    }
    if (entry.isDefend())
    {
      switch (entry.getNumber())
      {
      case 314: 
        return 12;
      case 315: 
        return 14;
      case 316: 
        return 11;
      case 317: 
        return 13;
      }
      return 0;
    }
    return 0;
  }
  
  public void setKillEffects(Creature performer, Creature defender)
  {
    defender.setOpponent(null);
    defender.setTarget(-10L, true);
    if (defender.getWurmId() == performer.target) {
      performer.setTarget(-10L, true);
    }
    defender.getCombatHandler().setCurrentStance(-1, (byte)15);
    performer.getCombatHandler().setCurrentStance(-1, (byte)15);
    if (performer.isUndead())
    {
      performer.healRandomWound(100);
      float nut = (50 + Server.rand.nextInt(49)) / 100.0F;
      performer.getStatus().refresh(nut, true);
    }
    if (performer.getCitizenVillage() != null) {
      performer.getCitizenVillage().removeTarget(defender);
    }
    if ((defender.isPlayer()) && (performer.isPlayer()))
    {
      if (!defender.isOkToKillBy(performer)) {
        if ((performer.hasAttackedUnmotivated()) && (performer.getReputation() < 0)) {
          performer.setReputation(performer.getReputation() - 10);
        } else {
          performer.setReputation(performer.getReputation() - 20);
        }
      }
      if (!defender.isFriendlyKingdom(performer.getKingdomId())) {
        if (!Players.getInstance().isOverKilling(performer.getWurmId(), defender.getWurmId()))
        {
          if ((performer.getKingdomTemplateId() == 3) || ((performer.getDeity() != null) && (performer.getDeity().isHateGod()))) {
            performer.maybeModifyAlignment(-5.0F);
          } else {
            performer.maybeModifyAlignment(5.0F);
          }
          if (performer.getCombatHandler().currentStrength == 0) {
            performer.achievement(43);
          }
        }
      }
    }
    else if ((!defender.isPlayer()) && (!performer.isPlayer()))
    {
      if ((defender.isPrey()) && (performer.isCarnivore())) {
        performer.getStatus().modifyHunger(-65000, 99.0F);
      }
    }
    if ((!defender.isPlayer()) && (!defender.isReborn()) && (performer.isPlayer()))
    {
      if ((defender.isKingdomGuard()) && (defender.getKingdomId() == performer.getKingdomId())) {
        performer.achievement(44);
      }
      try
      {
        int tid = defender.getTemplate().getTemplateId();
        if (CreatureTemplate.isDragon(tid)) {
          ((Player)performer).addTitle(Titles.Title.DragonSlayer);
        } else if ((tid == 11) || (tid == 27)) {
          ((Player)performer).addTitle(Titles.Title.TrollSlayer);
        } else if ((tid == 20) || (tid == 22)) {
          ((Player)performer).addTitle(Titles.Title.GiantSlayer);
        }
      }
      catch (Exception ex)
      {
        logger.log(Level.WARNING, defender.getName() + " and " + performer.getName() + ":" + ex.getMessage(), ex);
      }
      if ((performer.getDeity() != null) && (performer.getDeity().number == 2)) {
        performer.maybeModifyAlignment(0.5F);
      }
      if ((performer.getDeity() != null) && (performer.getDeity().number == 4)) {
        performer.maybeModifyAlignment(-0.5F);
      }
    }
    if (performer.getPrimWeapon() != null)
    {
      float ms = performer.getPrimWeapon().getSpellMindStealModifier();
      if ((ms > 0.0F) && (!defender.isPlayer()) && (defender.getKingdomId() != performer.getKingdomId()))
      {
        Skills s = defender.getSkills();
        int r = Server.rand.nextInt(s.getSkills().length);
        Skill toSteal = s.getSkills()[r];
        float skillStolen = ms / 100.0F * 0.1F;
        try
        {
          Skill owned = this.creature.getSkills().getSkill(toSteal.getNumber());
          if (owned.getKnowledge() < toSteal.getKnowledge())
          {
            double smod = (toSteal.getKnowledge() - owned.getKnowledge()) / 100.0D;
            
            owned.setKnowledge(owned.getKnowledge() + skillStolen * smod, false);
            this.creature.getCommunicator().sendSafeServerMessage("The " + performer
              .getPrimWeapon().getName() + " steals some " + toSteal.getName() + ".");
          }
        }
        catch (NoSuchSkillException localNoSuchSkillException) {}
      }
    }
  }
  
  private boolean checkStanceChange(Creature defender, Creature opponent)
  {
    if (defender.isFighting()) {
      if (defender.isPlayer())
      {
        if ((defender.isAutofight()) && (Server.rand.nextInt(10) == 0))
        {
          selectStance(defender, opponent);
          return true;
        }
      }
      else if (Server.rand.nextInt(5) == 0)
      {
        selectStance(defender, opponent);
        return true;
      }
    }
    return false;
  }
  
  private void selectStance(Creature defender, Creature player)
  {
    boolean selectNewStance = false;
    try
    {
      if ((!defender.getCurrentAction().isDefend()) && (!defender.getCurrentAction().isStanceChange())) {
        selectNewStance = true;
      }
    }
    catch (NoSuchActionException nsa)
    {
      selectNewStance = true;
    }
    if ((!defender.isPlayer()) && (selectNewStance)) {
      if (Server.rand.nextInt((int)(11.0F - Math.min(10.0F, defender.getAggressivity() * defender
        .getStatus().getAggTypeModifier() / 10.0F))) != 0) {
        selectNewStance = false;
      }
    }
    if (selectNewStance)
    {
      selectStanceList.clear();
      float mycr = -1.0F;
      float oppcr = -1.0F;
      float knowl = -1.0F;
      if (defender.isFighting())
      {
        if ((defender.mayRaiseFightLevel()) && (defender.getMindLogical().getKnowledge(0.0D) > 7.0D)) {
          if ((defender.isPlayer()) || (Server.rand.nextInt(100) < 30))
          {
            selectNewStance = false;
            selectStanceList.add(com.wurmonline.server.behaviours.Actions.actionEntrys['Ŕ']);
          }
          else
          {
            selectStanceList.add(com.wurmonline.server.behaviours.Actions.actionEntrys['Ŕ']);
          }
        }
        if (defender.isPlayer()) {
          if (getSpeed(defender.getPrimWeapon()) > Server.rand.nextInt(10)) {
            selectNewStance = false;
          }
        }
        if (selectNewStance)
        {
          mycr = defender.getCombatHandler().getCombatRating(player, defender.getPrimWeapon(), false);
          oppcr = player.getCombatHandler().getCombatRating(defender, player.getPrimWeapon(), false);
          
          knowl = getCombatKnowledgeSkill();
          if (knowl > 50.0F) {
            selectStanceList.addAll(standardDefences);
          }
          if (!defender.isPlayer()) {
            knowl += 20.0F;
          }
          selectStanceList.addAll(defender.getCombatHandler().getHighAttacks(null, true, player, mycr, oppcr, knowl));
          selectStanceList.addAll(defender.getCombatHandler().getMidAttacks(null, true, player, mycr, oppcr, knowl));
          selectStanceList.addAll(defender.getCombatHandler().getLowAttacks(null, true, player, mycr, oppcr, knowl));
        }
      }
      if (selectStanceList.size() > 0) {
        selectStanceFromList(defender, player, mycr, oppcr, knowl);
      }
      if ((!defender.isPlayer()) && (Server.rand.nextInt(10) == 0))
      {
        int randInt = Server.rand.nextInt(100);
        if (randInt <= Math.max(10.0F, (defender.getAggressivity() - 20) * defender.getStatus().getAggTypeModifier()))
        {
          if (defender.getFightStyle() != 1)
          {
            ArrayList<MulticolorLineSegment> segments = new ArrayList();
            segments.add(new CreatureLineSegment(defender));
            segments.add(new MulticolorLineSegment(" suddenly goes into a frenzy.", (byte)0));
            
            player.getCommunicator().sendColoredMessageCombat(segments);
            
            defender.setFightingStyle((byte)1);
          }
        }
        else if (randInt > Math.min(90.0F, (defender.getAggressivity() * defender.getStatus().getAggTypeModifier() + 20.0F) * defender
          .getStatus().getAggTypeModifier()))
        {
          if (defender.getFightStyle() != 2)
          {
            ArrayList<MulticolorLineSegment> segments = new ArrayList();
            segments.add(new CreatureLineSegment(defender));
            segments.add(new MulticolorLineSegment(" cowers.", (byte)0));
            
            player.getCommunicator().sendColoredMessageCombat(segments);
            
            defender.setFightingStyle((byte)2);
          }
        }
        else
        {
          if (defender.getFightStyle() == 1)
          {
            ArrayList<MulticolorLineSegment> segments = new ArrayList();
            segments.add(new CreatureLineSegment(defender));
            segments.add(new MulticolorLineSegment(" calms down a bit.", (byte)0));
            
            player.getCommunicator().sendColoredMessageCombat(segments);
          }
          else if (defender.getFightStyle() == 2)
          {
            ArrayList<MulticolorLineSegment> segments = new ArrayList();
            segments.add(new CreatureLineSegment(defender));
            segments.add(new MulticolorLineSegment(" seems a little more brave now.", (byte)0));
            
            player.getCommunicator().sendColoredMessageCombat(segments);
          }
          if (defender.getFightStyle() != 0) {
            defender.setFightingStyle((byte)0);
          }
        }
      }
    }
  }
  
  private static final ActionEntry getDefensiveActionEntry(byte opponentStance)
  {
    for (Iterator<ActionEntry> it = selectStanceList.listIterator(); it.hasNext();)
    {
      ActionEntry e = (ActionEntry)it.next();
      e = com.wurmonline.server.behaviours.Actions.actionEntrys[e.getNumber()];
      if (isStanceParrying(getStanceForAction(e), opponentStance)) {
        if (!isAtSoftSpot(getStanceForAction(e), opponentStance)) {
          return e;
        }
      }
    }
    return null;
  }
  
  private static final ActionEntry getOpposingActionEntry(byte opponentStance)
  {
    for (Iterator<ActionEntry> it = selectStanceList.listIterator(); it.hasNext();)
    {
      ActionEntry e = (ActionEntry)it.next();
      e = com.wurmonline.server.behaviours.Actions.actionEntrys[e.getNumber()];
      if (isStanceOpposing(getStanceForAction(e), opponentStance)) {
        if (!isAtSoftSpot(getStanceForAction(e), opponentStance)) {
          return e;
        }
      }
    }
    return null;
  }
  
  private static final ActionEntry getNonDefensiveActionEntry(byte opponentStance)
  {
    for (int x = 0; x < selectStanceList.size(); x++)
    {
      int num = Server.rand.nextInt(selectStanceList.size());
      ActionEntry e = (ActionEntry)selectStanceList.get(num);
      e = com.wurmonline.server.behaviours.Actions.actionEntrys[e.getNumber()];
      if ((!isStanceParrying(getStanceForAction(e), opponentStance)) && 
        (!isStanceOpposing(getStanceForAction(e), opponentStance))) {
        if (!isAtSoftSpot(getStanceForAction(e), opponentStance)) {
          return e;
        }
      }
    }
    return null;
  }
  
  private static final boolean isNextGoodStance(byte currentStance, byte nextStance, byte opponentStance)
  {
    if (isAtSoftSpot(nextStance, opponentStance)) {
      return false;
    }
    if (isAtSoftSpot(opponentStance, currentStance)) {
      return false;
    }
    if (isAtSoftSpot(opponentStance, nextStance)) {
      return true;
    }
    if (currentStance == 0) {
      return (nextStance == 5) || (nextStance == 2);
    }
    if (currentStance == 5) {
      return (nextStance == 6) || (nextStance == 4);
    }
    if (currentStance == 2) {
      return (nextStance == 1) || (nextStance == 3);
    }
    if ((currentStance == 1) || (currentStance == 6)) {
      return nextStance == 7;
    }
    if ((currentStance == 3) || (currentStance == 4)) {
      return nextStance == 10;
    }
    return false;
  }
  
  static final byte[] getSoftSpots(byte currentStance)
  {
    if (currentStance == 0) {
      return standardSoftSpots;
    }
    if (currentStance == 5) {
      return midLeftSoftSpots;
    }
    if (currentStance == 2) {
      return midRightSoftSpots;
    }
    if (currentStance == 1) {
      return upperRightSoftSpots;
    }
    if (currentStance == 6) {
      return upperLeftSoftSpots;
    }
    if (currentStance == 3) {
      return lowerRightSoftSpots;
    }
    if (currentStance == 4) {
      return lowerLeftSoftSpots;
    }
    return emptyByteArray;
  }
  
  private static final boolean isAtSoftSpot(byte stanceChecked, byte stanceUnderAttack)
  {
    byte[] opponentSoftSpots = getSoftSpots(stanceChecked);
    for (byte spot : opponentSoftSpots) {
      if (spot == stanceUnderAttack) {
        return true;
      }
    }
    return false;
  }
  
  private static final boolean existsBetterOffensiveStance(byte _currentStance, byte opponentStance)
  {
    if (isAtSoftSpot(opponentStance, _currentStance)) {
      return false;
    }
    boolean isOpponentAtSoftSpot = isAtSoftSpot(_currentStance, opponentStance);
    if ((isOpponentAtSoftSpot) || (
      (!isStanceParrying(_currentStance, opponentStance)) && (!isStanceOpposing(_currentStance, opponentStance))))
    {
      for (int x = 0; x < selectStanceList.size(); x++)
      {
        int num = Server.rand.nextInt(selectStanceList.size());
        ActionEntry e = (ActionEntry)selectStanceList.get(num);
        e = com.wurmonline.server.behaviours.Actions.actionEntrys[e.getNumber()];
        byte nextStance = getStanceForAction(e);
        if (isNextGoodStance(_currentStance, nextStance, opponentStance)) {
          return true;
        }
      }
      return false;
    }
    for (int x = 0; x < selectStanceList.size(); x++)
    {
      int num = Server.rand.nextInt(selectStanceList.size());
      ActionEntry e = (ActionEntry)selectStanceList.get(num);
      e = com.wurmonline.server.behaviours.Actions.actionEntrys[e.getNumber()];
      byte nextStance = getStanceForAction(e);
      if ((!isStanceParrying(_currentStance, nextStance)) && (!isStanceOpposing(_currentStance, nextStance))) {
        return true;
      }
    }
    return false;
  }
  
  private static final ActionEntry changeToBestOffensiveStance(byte _currentStance, byte opponentStance)
  {
    for (int x = 0; x < selectStanceList.size(); x++)
    {
      int num = Server.rand.nextInt(selectStanceList.size());
      ActionEntry e = (ActionEntry)selectStanceList.get(num);
      e = com.wurmonline.server.behaviours.Actions.actionEntrys[e.getNumber()];
      byte nextStance = getStanceForAction(e);
      if (isNextGoodStance(_currentStance, nextStance, opponentStance)) {
        return e;
      }
    }
    return null;
  }
  
  private final void selectStanceFromList(Creature defender, Creature opponent, float mycr, float oppcr, float knowl)
  {
    ActionEntry e = null;
    if ((defender.isPlayer()) || (defender.getMindLogical().getKnowledge(0.0D) > 17.0D))
    {
      if (oppcr - mycr > 3.0F) {
        if (Server.rand.nextInt(2) == 0)
        {
          if (defender.mayRaiseFightLevel()) {
            e = com.wurmonline.server.behaviours.Actions.actionEntrys['Ŕ'];
          }
        }
        else if (defender.opponent == opponent)
        {
          e = getDefensiveActionEntry(opponent.getCombatHandler().currentStance);
          if (e == null) {
            e = getOpposingActionEntry(opponent.getCombatHandler().currentStance);
          }
        }
      }
      if (e == null) {
        if ((defender.combatRound > 2) && (Server.rand.nextInt(2) == 0))
        {
          if (defender.mayRaiseFightLevel()) {
            e = com.wurmonline.server.behaviours.Actions.actionEntrys['Ŕ'];
          }
        }
        else if ((mycr - oppcr > 2.0F) || (defender.getCombatHandler().getSpeed(defender.getPrimWeapon()) < 3.0F))
        {
          if (existsBetterOffensiveStance(defender.getCombatHandler().currentStance, 
            opponent.getCombatHandler().currentStance))
          {
            e = changeToBestOffensiveStance(defender.getCombatHandler().currentStance, 
              opponent.getCombatHandler().currentStance);
            if (e == null) {
              e = getNonDefensiveActionEntry(opponent.getCombatHandler().currentStance);
            }
          }
        }
        else if (mycr >= oppcr) {
          if (defender.getStatus().damage < opponent.getStatus().damage)
          {
            if (existsBetterOffensiveStance(defender.getCombatHandler().currentStance, 
              opponent.getCombatHandler().currentStance))
            {
              e = changeToBestOffensiveStance(defender.getCombatHandler().currentStance, 
                opponent.getCombatHandler().currentStance);
              if (e == null) {
                e = getNonDefensiveActionEntry(opponent.getCombatHandler().currentStance);
              }
            }
          }
          else
          {
            e = getDefensiveActionEntry(opponent.getCombatHandler().currentStance);
            if (e == null) {
              e = getOpposingActionEntry(opponent.getCombatHandler().currentStance);
            }
          }
        }
      }
    }
    else if (e == null)
    {
      if ((!Server.rand.nextBoolean()) || (defender.getShield() == null))
      {
        int num = Server.rand.nextInt(selectStanceList.size());
        e = (ActionEntry)selectStanceList.get(num);
        e = com.wurmonline.server.behaviours.Actions.actionEntrys[e.getNumber()];
      }
      else
      {
        e = com.wurmonline.server.behaviours.Actions.actionEntrys[105];
      }
    }
    if ((e != null) && (e.getNumber() > 0)) {
      try
      {
        if (Creature.rangeTo(defender, opponent) <= e.getRange())
        {
          if (e.getNumber() == 105) {
            defender.setAction(new Action(defender, -1L, opponent.getWurmId(), e.getNumber(), defender
              .getPosX(), defender.getPosY(), defender.getPositionZ() + defender.getAltOffZ(), defender
              .getStatus().getRotation()));
          } else if ((e.isStanceChange()) && (e.getNumber() != 340))
          {
            if (getStanceForAction(e) != this.currentStance) {
              defender.setAction(new Action(defender, -1L, opponent.getWurmId(), e.getNumber(), defender
                .getPosX(), defender.getPosY(), defender.getPositionZ() + defender.getAltOffZ(), defender
                .getStatus()
                .getRotation()));
            }
          }
          else if ((defender.mayRaiseFightLevel()) && (e.getNumber() == 340)) {
            defender.setAction(new Action(defender, -1L, opponent.getWurmId(), e.getNumber(), defender
              .getPosX(), defender.getPosY(), defender.getPositionZ() + defender.getAltOffZ(), defender
              .getStatus().getRotation()));
          } else {
            defender.setAction(new Action(defender, -1L, opponent.getWurmId(), e.getNumber(), defender
              .getPosX(), defender.getPosY(), defender.getPositionZ() + defender.getAltOffZ(), defender
              .getStatus().getRotation()));
          }
        }
        else {
          logger.log(Level.INFO, defender.getName() + " too far away for stance " + e.getActionString() + " attacking " + opponent
            .getName() + " with range " + Creature.rangeTo(defender, opponent));
        }
      }
      catch (Exception fe)
      {
        logger.log(Level.WARNING, defender.getName() + " failed:" + fe.getMessage(), fe);
      }
    }
  }
  
  public static final byte[] getOptions(List<ActionEntry> list, byte currentStance)
  {
    if ((list == null) || (list.isEmpty())) {
      return NO_COMBAT_OPTIONS;
    }
    byte[] toReturn = new byte[31];
    for (ListIterator<ActionEntry> it = list.listIterator(); it.hasNext();)
    {
      ActionEntry act = (ActionEntry)it.next();
      int x = act.getNumber() - 287;
      if (act.isDefend()) {
        toReturn[x] = 50;
      } else if ((x >= 0) && (x <= 30)) {
        if (getStanceForAction(com.wurmonline.server.behaviours.Actions.actionEntrys[act.getNumber()]) != currentStance) {
          toReturn[x] = Byte.parseByte(act.getActionString().substring(0, act.getActionString().indexOf("%")));
        }
      }
    }
    return toReturn;
  }
  
  public void addParryModifier(DoubleValueModifier modifier)
  {
    if (this.parryModifiers == null) {
      this.parryModifiers = new HashSet();
    }
    this.parryModifiers.add(modifier);
  }
  
  public void removeParryModifier(DoubleValueModifier modifier)
  {
    if (this.parryModifiers != null) {
      this.parryModifiers.remove(modifier);
    }
  }
  
  private double getParryMod()
  {
    if (this.parryModifiers == null) {
      return 1.0D;
    }
    double doubleModifier = 1.0D;
    for (DoubleValueModifier lDoubleValueModifier : this.parryModifiers) {
      doubleModifier += lDoubleValueModifier.getModifier();
    }
    return doubleModifier;
  }
  
  public void addDodgeModifier(DoubleValueModifier modifier)
  {
    if (this.dodgeModifiers == null) {
      this.dodgeModifiers = new HashSet();
    }
    this.dodgeModifiers.add(modifier);
  }
  
  public void removeDodgeModifier(DoubleValueModifier modifier)
  {
    if (this.dodgeModifiers != null) {
      this.dodgeModifiers.remove(modifier);
    }
  }
  
  private double getDodgeMod()
  {
    float diff = this.creature.getTemplate().getWeight() / this.creature.getWeight();
    if (this.creature.isPlayer()) {
      diff = this.creature.getTemplate().getWeight() / (this.creature.getWeight() + this.creature.getBody().getBodyItem().getFullWeight() + this.creature.getInventory().getFullWeight());
    }
    diff = 0.8F + diff * 0.2F;
    if (this.dodgeModifiers == null) {
      return 1.0F * diff;
    }
    double doubleModifier = 1.0D;
    for (DoubleValueModifier lDoubleValueModifier : this.dodgeModifiers) {
      doubleModifier += lDoubleValueModifier.getModifier();
    }
    return doubleModifier * diff;
  }
  
  void setFightingStyle(byte style)
  {
    if (style == 2) {
      this.currentStrength = 0;
    } else if (style == 1) {
      this.currentStrength = 3;
    } else {
      this.currentStrength = 1;
    }
  }
  
  public List<ActionEntry> getMoveStack()
  {
    return this.moveStack;
  }
  
  void clearMoveStack()
  {
    if (this.moveStack != null)
    {
      this.moveStack.clear();
      this.moveStack = null;
    }
  }
  
  byte getOpportunityAttacks()
  {
    return this.opportunityAttacks;
  }
  
  public boolean isSentAttacks()
  {
    return this.sentAttacks;
  }
  
  public void setSentAttacks(boolean aSentAttacks)
  {
    this.sentAttacks = aSentAttacks;
  }
  
  public void setRodEffect(boolean effect)
  {
    this.hasRodEffect = effect;
    sendRodEffect();
  }
  
  public void sendRodEffect()
  {
    if (this.hasRodEffect) {
      this.creature.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.ROD_BEGUILING_EFFECT, 100000, 100.0F);
    } else {
      this.creature.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.ROD_BEGUILING_EFFECT);
    }
  }
  
  public static String getOthersString()
  {
    return othersString;
  }
  
  public static void setOthersString(String othersString)
  {
    othersString = othersString;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\creatures\CombatHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */