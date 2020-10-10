package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.CaveTile;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.mesh.Tiles.TileBorderDirection;
import com.wurmonline.server.Constants;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.combat.CombatConstants;
import com.wurmonline.server.creatures.Brand;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.items.NotOwnedException;
import com.wurmonline.server.items.RuneUtilities.ModifierEffect;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.GmInterface;
import com.wurmonline.server.questions.GmTool;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.spells.Spells;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.NoSuchWallException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.support.Tickets;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchTileException;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.StructureConstantsEnum;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class Action
  implements MiscConstants, CounterTypes, CombatConstants, TimeConstants
{
  private static final String YOU_ARE_NOW_TOO_FAR_AWAY_TO = "You are now too far away to ";
  private static final String YOU_ARE_TOO_FAR_AWAY_TO_DO_THAT = "You are too far away to do that.";
  public static final String MESSAGEPLACEHOLDER = "This is a placeholder message.";
  static final String NOT_ALLOWED_DEED_ACTION_BY_LEGAL_PLAYER_MESSAGE = "That would be illegal here. You can check the settlement token for the local laws.";
  static final String NOT_ALLOWED_ACTION_BY_LEGAL_PLAYER_MESSAGE = "That would be illegal. ";
  static final String NOT_SAME_BRIDGE = "You need to be on the same bridge in order to do that. ";
  public static final String NOT_ALLOWED_ACTION_ON_FREEDOM_MESSAGE = "That would be very bad for your karma and is disallowed on this server.";
  private static final String NOT_ALLOWED_ACTION_TREE_ON_FREEDOM_MESSAGE = "This action is not allowed here, because the tree is on a player owned deed that has disallowed it.";
  static final String NOT_ALLOWED_ACTION_TILE_ON_FREEDOM_MESSAGE = "This action is not allowed here, because the tile is on a player owned deed that has disallowed it.";
  static final String GUARD_WARNS_A_PLAYER_MESSAGE = "A guard has noted you and stops you with a warning.";
  static final String NO_SENSE = "That action makes no sense here.";
  static final short BUILD_ACTIONS = 20000;
  public static final short CREATE_ACTIONS = 10000;
  public static final short RECIPE_ACTIONS = 8000;
  private static final Logger logger = Logger.getLogger(Action.class.getName());
  private Item destroyedItem;
  private Creature tempCreature = null;
  private final Creature performer;
  private long subject;
  private long lastPolledAction = 0L;
  private long target = -10L;
  private long[] targets;
  private int numbTargets = 0;
  private final Behaviour behaviour;
  private short action;
  private boolean done = false;
  private byte rarity = 0;
  private float posX;
  private float posY;
  private final float posZ;
  private boolean personalAction = false;
  private int tilex = -1;
  private int tiley = -1;
  private int tilez = -1;
  private boolean onSurface = true;
  private int tile = -1;
  private int heightOffset = -1;
  private final float rot;
  private float counter = 0.0F;
  private int targetType;
  private int tenthOfSecondsLeftOnAction = 0;
  private float failSecond = 2.14748365E9F;
  private float power = 0.1F;
  private String actionString = "";
  private Wall wall = null;
  private Fence fence = null;
  private boolean isSpell = false;
  private boolean isOffensive = false;
  private int triggerCounter = 0;
  private int currentSecond = -1;
  private int lastSecond = 0;
  private boolean justTickedSecond = true;
  private float nextTick = 10.0F;
  private int tickCount = 0;
  private long data = 0L;
  private byte auxByte = 0;
  private boolean manualInvulnerable = false;
  private Spell spell;
  
  public float getNextTick()
  {
    return this.nextTick;
  }
  
  public void setNextTick(float aNextTick)
  {
    this.nextTick = aNextTick;
  }
  
  public void incNextTick(float aIncTick)
  {
    this.nextTick += aIncTick;
  }
  
  public int getTickCount()
  {
    return this.tickCount;
  }
  
  public void setTickCount(int aTickCount)
  {
    this.tickCount = aTickCount;
  }
  
  public void incTickCount()
  {
    this.tickCount += 1;
  }
  
  public long getData()
  {
    return this.data;
  }
  
  public void setData(long newData)
  {
    this.data = newData;
  }
  
  public byte getAuxByte()
  {
    return this.auxByte;
  }
  
  public void setAuxByte(byte newByte)
  {
    this.auxByte = newByte;
  }
  
  public Action(Creature aPerformer, long aSubj, long _target, short act, float aPosX, float aPosY, float aPosZ, float aRot)
    throws NoSuchPlayerException, NoSuchCreatureException, NoSuchItemException, NoSuchBehaviourException, FailedException, NoSuchWallException
  {
    this.performer = aPerformer;
    this.subject = aSubj;
    
    this.target = _target;
    this.numbTargets = 1;
    
    this.action = act;
    this.posX = aPosX;
    this.posY = aPosY;
    this.posZ = aPosZ;
    this.rot = aRot;
    this.targetType = WurmId.getType(this.target);
    if (this.action >= 0)
    {
      if ((needsFood()) && (aPerformer.getPower() <= 1)) {
        if (aPerformer.getStatus().getHunger() > 60000) {
          throw new FailedException("You are too hungry.");
        }
      }
      boolean isEmote = (this.action >= 2000) && (this.action < 8000);
      if (!isEmote)
      {
        this.isSpell = Actions.actionEntrys[getNumber()].isSpell();
        this.isOffensive = Actions.actionEntrys[getNumber()].isOffensive();
        if (this.isSpell)
        {
          this.spell = Spells.getSpell(getNumber());
          if ((this.spell != null) && (this.spell.isReligiousSpell()))
          {
            if ((!this.performer.isSpellCaster()) && (!this.performer.isSummoner()))
            {
              if (aPerformer.getDeity() == null) {
                throw new FailedException("You do not follow a deity.");
              }
              if (!aPerformer.isPriest()) {
                throw new FailedException("You are not a priest.");
              }
              if (!aPerformer.getDeity().hasSpell(this.spell)) {
                throw new FailedException(aPerformer.getDeity().getName() + " does not bestow the power of '" + this.spell.getName() + "'.");
              }
              if (this.spell.level > (int)aPerformer.getFaith()) {
                throw new FailedException("You must reach a higher level of faith to cast this spell.");
              }
            }
          }
          else if (!aPerformer.knowsKarmaSpell(getNumber())) {
            throw new FailedException("You do not know how to cast '" + this.spell.getName() + "'.");
          }
        }
        if ((aPerformer.isPlayer()) && (!isQuick())) {
          ((Player)aPerformer).resetInactivity(true);
        }
        if (aPerformer.isGuest()) {
          if (!isActionGuest(this.action)) {
            throw new FailedException("Guests may mostly go around and look at things. You need to register to do other things.");
          }
        }
        if ((getActionEntry().isStanceChange()) || (getActionEntry().isDefend()) || (getNumber() == 105))
        {
          if (aPerformer.getTarget() != null)
          {
            this.subject = -1L;
            this.target = aPerformer.target;
            try
            {
              Creature tc = Server.getInstance().getCreature(this.target);
              if (Creature.rangeTo(aPerformer, tc) > Actions.actionEntrys[getNumber()].getRange()) {
                if (aPerformer.opponent != null) {
                  this.target = aPerformer.opponent.getWurmId();
                }
              }
            }
            catch (NoSuchPlayerException localNoSuchPlayerException) {}catch (NoSuchCreatureException localNoSuchCreatureException) {}
          }
          else if (this.action != 340)
          {
            this.subject = -1L;
            this.target = aPerformer.getWurmId();
          }
          else
          {
            throw new FailedException("You need to be in combat in order to focus.");
          }
        }
        else if (getNumber() == 342)
        {
          if (this.target == -10L) {
            this.target = aPerformer.target;
          }
        }
        else if ((getNumber() >= 197) && (getNumber() <= 208)) {
          if (((this.targetType != 1) && (this.targetType != 0)) || 
            (this.target == aPerformer.getWurmId())) {
            if (aPerformer.getTarget() != null)
            {
              this.target = aPerformer.target;
            }
            else
            {
              this.subject = -1L;
              this.target = aPerformer.getWurmId();
            }
          }
        }
      }
      if (isFatigue())
      {
        if ((this.performer.getVehicle() != -10L) && (!Actions.isActionAllowedOnVehicle(this.action))) {
          if (Actions.isActionAllowedOnBoat(this.action)) {
            try
            {
              Item vehicle = Items.getItem(this.performer.getVehicle());
              if (!vehicle.isBoat()) {
                throw new FailedException("You need to be on solid ground to do that.");
              }
              if (vehicle.getPosZ() > 0.0F) {
                throw new FailedException("The boat must be on water to do that from the boat.");
              }
            }
            catch (NoSuchItemException e)
            {
              throw new FailedException("You need to be on solid ground to do that.");
            }
          } else {
            throw new FailedException("You need to be on solid ground to do that.");
          }
        }
        if (aPerformer.getFatigueLeft() <= 0) {
          throw new FailedException("You are too mentally exhausted to do that now.");
        }
        if ((this.performer.getBridgeId() != -10L) && (this.action == 109)) {
          throw new FailedException("You can't track on a bridge.");
        }
      }
    }
    this.behaviour = getBehaviour(this.target, aPerformer.isOnSurface());
    this.onSurface = getIsOnSurface(this.target, aPerformer.isOnSurface());
    this.targetType = WurmId.getType(this.target);
    if ((!aPerformer.isActionFaithful(this)) && (!aPerformer.isChampion())) {
      throw new FailedException(aPerformer.getDeity().name + " does not accept that action.");
    }
    if ((WurmId.getType(this.subject) == 8) || (WurmId.getType(this.subject) == 32)) {
      this.subject = -1L;
    }
    if (this.action < 8000)
    {
      if (this.subject != -1L) {
        if ((WurmId.getType(this.subject) != 2) && (WurmId.getType(this.subject) != 6) && 
          (WurmId.getType(this.subject) != 19) && (WurmId.getType(this.subject) != 20)) {}
      }
    }
    else {
      try
      {
        Item item = Items.getItem(this.subject);
        if (!item.deleted)
        {
          long owner = -10L;
          try
          {
            if ((getNumber() < 0) || 
              (getNumber() >= Actions.actionEntrys.length) || 
              (!getActionEntry().isUseItemOnGroundAction())) {
              owner = item.getOwner();
            }
            if ((this.isSpell) && (this.spell != null)) {
              if ((!this.performer.isSpellCaster()) && (!this.performer.isSummoner())) {
                if ((this.spell.isReligiousSpell()) && (aPerformer.getDeity() != null))
                {
                  if (!item.isHolyItem(aPerformer.getDeity())) {
                    throw new FailedException("You must activate your deity's channeling item.");
                  }
                }
                else if (this.spell.isSorcerySpell()) {
                  if (!item.isMagicStaff()) {
                    if (((item.getTemplateId() == 176) && (aPerformer.getPower() >= 2) && (Servers.isThisATestServer()) ? 1 : 0) == 0) {
                      throw new FailedException("You must activate a magical staff to use this spell.");
                    }
                  }
                }
              }
            }
            if (getNumber() == -1) {
              if ((item.isWeaponBow()) && (this.performer.isArcheryMode())) {
                this.action = 124;
              }
            }
            if (item.mailed) {
              throw new FailedException("You cannot use " + item.getName() + " right now.");
            }
            if ((owner != aPerformer.getWurmId()) && (!item.isTraded()) && 
              (getNumber() >= 0) && 
              (getNumber() < Actions.actionEntrys.length) && 
              (!getActionEntry().isUseItemOnGroundAction()))
            {
              aPerformer.getCommunicator().sendSafeServerMessage("You are using an item that belongs to someone else! Please report this bug.");
              
              logger.warning(aPerformer.getName() + " tries to use an item, " + item.getWurmId() + " which is owned by someone else, " + owner + "!");
              
              throw new NoSuchItemException(aPerformer.getName() + " tries to use an item which is owned by someone else!");
            }
            if ((item.isUseOnGroundOnly()) && (this.target > 0L) && 
              (!isEmote()) && (getActionEntry().isBlockedByUseOnGroundOnly()))
            {
              if (isQuick()) {
                try
                {
                  if ((this.targetType == 2) || (this.targetType == 19) || (this.targetType == 20) || (this.targetType == 6))
                  {
                    Item targ = Items.getItem(this.target);
                    
                    throw new FailedException("You need to use the " + targ.getName() + " on the " + item.getName() + " while it is on the ground.");
                  }
                }
                catch (NoSuchItemException localNoSuchItemException1) {}
              } else {
                throw new FailedException("The " + item.getName() + " needs to be on the ground when used.");
              }
            }
            else if (item.isTraded())
            {
              if ((this.action != 1) && (this.action != 87) && (!isActionEmote(this.action))) {
                throw new FailedException("You cannot use " + item.getName() + " while trading it.");
              }
            }
            else if (item.isBanked()) {
              if ((this.action != 1) && (this.action != 87) && (!isActionEmote(this.action))) {
                throw new FailedException("You cannot use " + item.getName() + " while it is banked.");
              }
            }
            if ((item.getTemplateId() == 176) || (item.getTemplateId() == 315)) {
              if ((aPerformer.getPower() < 1) && 
                (!Players.isArtist(this.performer.getWurmId(), false, false)))
              {
                logger.warning(aPerformer + " tried to use a wand, " + item + ", but their power is " + aPerformer
                  .getPower() + ", action: " + this.action);
                throw new FailedException("You cannot use the " + item.getName() + ".");
              }
            }
          }
          catch (NotOwnedException nex)
          {
            if (logger.isLoggable(Level.FINER)) {
              logger.log(Level.WARNING, aPerformer.getName() + " tries to use " + item.getName() + " on the ground.", nex);
            }
            aPerformer.getCommunicator().sendSafeServerMessage("You must carry the " + item
              .getName() + " to use it.");
            throw new NoSuchItemException(aPerformer.getName() + " tries to use an item on the ground.");
          }
        }
        else
        {
          if (logger.isLoggable(Level.FINER)) {
            logger.finer(item + " deleted");
          }
          throw new NoSuchItemException("Item deleted");
        }
      }
      catch (NoSuchItemException nsex)
      {
        this.subject = -1L;
      }
    }
    if (this.targetType == 5)
    {
      if (this.action == -1) {
        this.action = 1;
      }
      if (this.action == 162) {
        this.action = 193;
      }
      this.tilex = Tiles.decodeTileX(this.target);
      this.tiley = Tiles.decodeTileY(this.target);
      this.heightOffset = Tiles.decodeHeightOffset(this.target);
      this.onSurface = (Tiles.decodeLayer(this.target) == 0);
      for (int xx = 1; xx >= -1; xx--) {
        for (int yy = 1; yy >= -1; yy--) {
          try
          {
            Zone zone = Zones.getZone(this.tilex + xx, this.tiley + yy, this.onSurface);
            VolaTile lTile = zone.getTileOrNull(this.tilex + xx, this.tiley + yy);
            if (lTile != null)
            {
              Wall[] walls = lTile.getWalls();
              for (int s = 0; s < walls.length; s++) {
                if (walls[s].getId() == this.target)
                {
                  this.wall = walls[s];
                  break;
                }
              }
            }
          }
          catch (NoSuchZoneException localNoSuchZoneException1) {}
        }
      }
      if (this.wall == null) {
        throw new NoSuchWallException("No wall with id " + this.target);
      }
      try
      {
        VolaTile t = this.wall.getOrCreateInnerTile(this.onSurface);
        this.tilex = t.tilex;
        this.tiley = t.tiley;
      }
      catch (NoSuchTileException nst)
      {
        logger.log(Level.WARNING, "tile at " + this.tilex + ", " + this.tiley + " " + nst.getMessage(), nst);
      }
      catch (NoSuchZoneException nsz)
      {
        logger.log(Level.WARNING, "tile at " + this.tilex + ", " + this.tiley + " " + nsz.getMessage(), nsz);
      }
      if ((this.action >= 8000) || (this.action < 2000))
      {
        if (!aPerformer.isWithinTileDistanceTo((int)(this.target >> 32) & 0xFFFF, (int)(this.target >> 16) & 0xFFFF, this.heightOffset, 1)) {
          if ((this.action > 2000) || (!getActionEntry().isIgnoresRange())) {
            throw new FailedException("You are too far away to do that.");
          }
        }
        if ((!Actions.isActionManage(this.action)) && 
          (!Methods.isActionAllowed(aPerformer, this.action, this.wall.getTileX(), this.wall.getTileY()))) {
          throw new FailedException("This is a placeholder message.");
        }
      }
    }
    else
    {
      float maxDist;
      if ((this.targetType == 2) || (this.targetType == 6) || (this.targetType == 19) || (this.targetType == 20))
      {
        if (this.action == -1) {
          this.action = 1;
        }
        Item targ = Items.getItem(this.target);
        this.tilex = ((int)targ.getPosX() >> 2);
        this.tiley = ((int)targ.getPosY() >> 2);
        if (targ.isTraded())
        {
          if ((this.action != 1) && (this.action != 87) && (!isActionEmote(this.action))) {
            throw new FailedException("You cannot use " + targ.getName() + " while trading it.");
          }
        }
        else if (targ.isBanked()) {
          if ((this.action != 1) && (this.action != 87) && (!isActionEmote(this.action))) {
            throw new FailedException("You cannot use " + targ.getName() + " while it is banked.");
          }
        }
        if (targ.mailed) {
          throw new FailedException("You cannot use " + targ.getName() + " right now.");
        }
        if ((this.action == 851) && (targ.isBusy()) && 
          (targ.getTemplateId() == 1125) && (!targ.isUnfinished())) {
          throw new FailedException("You cannot use the " + targ.getName() + " right now as it is already being used.");
        }
        if ((aPerformer.getCurrentTile().getStructure() != null) && 
          (!aPerformer.isWithinDistanceToZ(targ.getPosZ(), 4.0F, true)) && 
          (!isActionEmote(this.action)) && 
          (!getActionEntry().isIgnoresRange()) && 
          (!Actions.isActionAllowedThroughFloors(this.action))) {
          throw new FailedException("You are too far away to do that.");
        }
        if (isSameBridge(this.action))
        {
          Item parent = Items.getItem(targ.getTopParent());
          if (parent.getBridgeId() != -10L)
          {
            BridgePart bridgePart = Zones.getBridgePartFor(parent.getTileX(), parent.getTileY(), parent.isOnSurface());
            if ((bridgePart == null) || (bridgePart.getStructureId() != parent.getBridgeId())) {
              parent.setOnBridge(-10L);
            }
          }
          if ((this.performer.getBridgeId() != parent.getBridgeId()) && (this.performer.getWurmId() != targ.getOwnerId())) {
            if ((this.performer.getBridgeId() == -10L) && (parent.getBridgeId() != -10L))
            {
              int ix = parent.getTileX();
              int iy = parent.getTileY();
              int px = this.performer.getTileX();
              int py = this.performer.getTileY();
              if ((ix != px) && (iy != py)) {
                throw new FailedException("You need to be on the same bridge in order to do that. ");
              }
              BridgePart bridgePart = Zones.getBridgePartFor(ix, iy, parent.isOnSurface());
              if ((bridgePart != null) && (bridgePart.hasAnExit()))
              {
                if (iy < py)
                {
                  if (!bridgePart.hasSouthExit()) {
                    throw new FailedException("You need to be on the same bridge in order to do that. ");
                  }
                }
                else if (ix > px)
                {
                  if (!bridgePart.hasWestExit()) {
                    throw new FailedException("You need to be on the same bridge in order to do that. ");
                  }
                }
                else if (iy > py)
                {
                  if (!bridgePart.hasNorthExit()) {
                    throw new FailedException("You need to be on the same bridge in order to do that. ");
                  }
                }
                else if (ix < px)
                {
                  if (!bridgePart.hasEastExit()) {
                    throw new FailedException("You need to be on the same bridge in order to do that. ");
                  }
                }
                else {
                  throw new FailedException("You need to be on the same bridge in order to do that. ");
                }
              }
              else {
                throw new FailedException("You need to be on the same bridge in order to do that. ");
              }
            }
            else if ((this.performer.getBridgeId() != -10L) && (parent.getBridgeId() == -10L))
            {
              int ix = parent.getTileX();
              int iy = parent.getTileY();
              int px = this.performer.getTileX();
              int py = this.performer.getTileY();
              
              BridgePart bridgePart = Zones.getBridgePartFor(px, py, parent.isOnSurface());
              if ((bridgePart != null) && (bridgePart.hasAnExit()))
              {
                if (iy < py)
                {
                  if (!bridgePart.hasNorthExit()) {
                    throw new FailedException("You need to be on the same bridge in order to do that. ");
                  }
                }
                else if (ix > px)
                {
                  if (!bridgePart.hasEastExit()) {
                    throw new FailedException("You need to be on the same bridge in order to do that. ");
                  }
                }
                else if (iy > py)
                {
                  if (!bridgePart.hasSouthExit()) {
                    throw new FailedException("You need to be on the same bridge in order to do that. ");
                  }
                }
                else if (ix < px)
                {
                  if (!bridgePart.hasWestExit()) {
                    throw new FailedException("You need to be on the same bridge in order to do that. ");
                  }
                }
                else {
                  throw new FailedException("You need to be on the same bridge in order to do that. ");
                }
              }
              else {
                throw new FailedException("You need to be on the same bridge in order to do that. ");
              }
            }
          }
        }
        try
        {
          long towner = targ.getOwner();
          if ((this.action == 7) && (targ.isBusy())) {
            throw new FailedException("You are using that item.");
          }
          if (towner != aPerformer.getWurmId())
          {
            if ((this.action != 1) && (this.action != 87) && (!isActionEmote(this.action)) && (this.action != 185)) {
              throw new FailedException("You need to carry that item to use it.");
            }
          }
          else if ((this.action == 7) && (
            (targ.getTemplateId() == 26) || (targ.getTemplateId() == 298)))
          {
            if (!MethodsItems.mayDropDirt(aPerformer)) {
              throw new FailedException("You are not allowed to drop dirt there.");
            }
          }
          else
          {
            if ((targ.isTraded()) && (this.action != 1) && (this.action != 87) && 
              (!isActionEmote(this.action))) {
              throw new FailedException("You may not tamper with items you are trading.");
            }
            if ((targ.isUseOnGroundOnly()) && (!isQuick()) && (this.action != 100) && (this.action != 176) && (this.action != 180) && (this.action != 633) && (this.action != 925) && (this.action != 926)) {
              throw new FailedException("You may only use that item while it is on the ground.");
            }
          }
          if (towner == this.performer.getWurmId()) {
            this.personalAction = true;
          }
          if (this.action == 162) {
            this.personalAction = true;
          }
        }
        catch (NotOwnedException nso)
        {
          if (targ.getZoneId() < 0) {
            logger.log(Level.WARNING, aPerformer
              .getName() + " interacting with a " + targ.getName() + "(id=" + targ.getWurmId() + ") not in the world action=" + this.action);
          }
          float iposX = targ.getPosX();
          float iposY = targ.getPosY();
          float iposZ = targ.getPosZ();
          boolean ok = aPerformer.isOnSurface() == targ.isOnSurface();
          if (!ok)
          {
            VolaTile lTile = Zones.getOrCreateTile((int)iposX >> 2, (int)iposY >> 2, targ.isOnSurface());
            if ((lTile.isTransition) && (this.action != 74) && (this.action != 3) && (this.action != 6) && (this.action != 100)) {
              if (Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile((int)iposX >> 2, (int)iposY >> 2)))) {
                ok = false;
              } else {
                ok = true;
              }
            }
          }
          if (!ok)
          {
            VolaTile lTile = Zones.getOrCreateTile(aPerformer.getTileX(), aPerformer.getTileY(), aPerformer
              .isOnSurface());
            if ((lTile.isTransition) && (this.action != 74) && (this.action != 3) && (this.action != 6) && (this.action != 100)) {
              if (Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(aPerformer.getTileX(), aPerformer
                .getTileY())))) {
                ok = false;
              } else {
                ok = true;
              }
            }
          }
          if (!ok) {
            if (aPerformer.getPower() >= 2) {
              if ((this.action == 185) || (this.action == 179)) {
                ok = true;
              }
            }
          }
          if (ok)
          {
            if ((targ.isUseOnGroundOnly()) && (!isActionEmote(this.action)) && (getActionEntry().isBlockedByUseOnGroundOnly()) && 
              ((!isActionRecipe(this.action)) || (targ.getTemplateId() != 768)) && 
              (targ.getParentId() != -10L) && (!isQuick()) && (this.action != 192) && (this.action != 162) && (this.action != 100) && (this.action != 176) && (this.action != 925) && (this.action != 926)) {
              if ((aPerformer.getPower() > 0) && (this.action != 180)) {
                throw new FailedException("You may only use that item while it is on the ground.");
              }
            }
            if (isActionEmote(this.action))
            {
              if (!aPerformer.isWithinDistanceTo(iposX, iposY, iposZ, Emotes.emoteEntrys[(this.action - 2000)]
                .getRange())) {
                throw new FailedException("You are too far away to do that.");
              }
            }
            else if (this.action < 2000)
            {
              maxDist = Actions.actionEntrys[this.action].getRange();
              if (targ.isVehicle())
              {
                Vehicle vehicle = Vehicles.getVehicle(targ);
                if (vehicle != null) {
                  maxDist = Math.max(maxDist, vehicle.getMaxAllowedLoadDistance());
                }
              }
              if ((aPerformer.getPower() < 5) && 
                (targ.getTopParent() != aPerformer.getVehicle()) && 
                (!getActionEntry().isIgnoresRange()) && 
                (!aPerformer.isWithinDistanceTo(iposX, iposY, iposZ, maxDist))) {
                throw new FailedException("You are too far away to do that.");
              }
              if (Actions.actionEntrys[getNumber()].isPoliced())
              {
                if ((MethodsItems.checkIfStealing(targ, aPerformer, this)) && 
                  (checkLegalMode(aPerformer))) {
                  throw new NoSuchItemException("This is a placeholder message.");
                }
                if (targ.isHollow())
                {
                  Item[] items = targ.getAllItems(false);
                  for (int x = 0; x < items.length; x++) {
                    if ((MethodsItems.checkIfStealing(items[x], aPerformer, this)) && 
                      (checkLegalMode(aPerformer))) {
                      throw new NoSuchItemException("This is a placeholder message.");
                    }
                  }
                }
              }
              else if (this.action == 362)
              {
                Village village = Zones.getVillage(this.tilex, this.tiley, aPerformer.isOnSurface());
                if (village != null) {
                  if (!village.isActionAllowed(this.action, aPerformer))
                  {
                    if (!Zones.isOnPvPServer(this.tilex, this.tiley)) {
                      throw new FailedException("This action is not allowed here, because the tile is on a player owned deed that has disallowed it.");
                    }
                    failCheckEnemy(aPerformer, village);
                  }
                }
              }
            }
            else if (this.action >= 8000)
            {
              if ((!targ.isNoTake()) && (!targ.isUseOnGroundOnly())) {
                throw new FailedException("You need to carry that item to work with it.");
              }
            }
          }
          else
          {
            throw new NoSuchItemException("You are too far away from " + targ.getNameWithGenus() + ".");
          }
        }
        if ((targ.getBless() != null) && (aPerformer.getDeity() != null) && 
          (aPerformer.getDeity().accepts(targ.getBless().alignment))) {
          if (this.isOffensive)
          {
            if (aPerformer.faithful) {
              throw new FailedException(aPerformer.getDeity() + " would not approve of that since the " + targ.getName() + " has " + aPerformer.getDeity().getHisHerItsString() + " blessings.");
            }
            try
            {
              if (targ.isDomainItem())
              {
                aPerformer.getCommunicator().sendAlertServerMessage(
                  aPerformer.getDeity().name + " noticed you and is outraged that you desecrate " + aPerformer
                  .getDeity().getHisHerItsString() + " altars.");
                aPerformer.setFavor(aPerformer.getFavor() - 10.0F);
                aPerformer.modifyFaith(-1.0F);
                VolaTile t = aPerformer.getCurrentTile();
                if (t != null) {
                  aPerformer.calculateZoneBonus(t.tilex, t.tiley, aPerformer.isOnSurface());
                }
              }
              else if (Server.rand.nextInt(100) > aPerformer.getFaith() - 10.0F)
              {
                aPerformer.getCommunicator().sendAlertServerMessage(
                  aPerformer.getDeity().name + " noticed you and is upset since the " + targ.getName() + " has " + aPerformer
                  .getDeity().getHisHerItsString() + " blessings.");
                aPerformer.setFavor(aPerformer.getFavor() - 10.0F);
                aPerformer.modifyFaith(-0.25F);
                VolaTile t = aPerformer.getCurrentTile();
                if (t != null) {
                  aPerformer.calculateZoneBonus(t.tilex, t.tiley, aPerformer.isOnSurface());
                }
              }
            }
            catch (IOException iox)
            {
              logger.log(Level.WARNING, aPerformer.getName(), iox);
              throw new FailedException("An error occured while trying to attack. Please contact the administrators.");
            }
          }
        }
      }
      else if ((this.targetType == 1) || (WurmId.getType(this.target) == 0))
      {
        this.personalAction = true;
        if (this.action == -1)
        {
          this.action = 1;
          if (this.target != aPerformer.getWurmId()) {
            try
            {
              Creature cret = Server.getInstance().getCreature(this.target);
              if ((this.performer.isArcheryMode()) && ((this.target == aPerformer.target) || 
                (cret.getAttitude(aPerformer) == 2)))
              {
                this.action = 342;
                try
                {
                  Item bow = this.performer.getEquippedWeapon((byte)14, true);
                  if ((bow != null) && (bow.isWeaponBow()))
                  {
                    this.action = 124;
                  }
                  else
                  {
                    bow = this.performer.getEquippedWeapon((byte)13, true);
                    if ((bow != null) && (bow.isWeaponBow())) {
                      this.action = 124;
                    }
                  }
                }
                catch (NoSpaceException spc)
                {
                  logger.log(Level.WARNING, aPerformer.getName() + ": " + spc.getMessage(), spc);
                }
              }
              if (this.action == 1) {
                if (cret.getAttitude(aPerformer) == 2) {
                  this.action = 326;
                }
              }
            }
            catch (NoSuchCreatureException localNoSuchCreatureException1) {}catch (NoSuchPlayerException localNoSuchPlayerException1) {}
          }
        }
        if (this.action == 11) {
          this.action = 331;
        }
        Creature tcret = Server.getInstance().getCreature(this.target);
        boolean ok = aPerformer.isOnSurface() == tcret.isOnSurface();
        if (isSameBridge(this.action)) {
          if (this.performer.getBridgeId() != tcret.getBridgeId()) {
            throw new FailedException("You need to be on the same bridge in order to do that. ");
          }
        }
        if (!ok)
        {
          ok = (this.action == 185) || (this.action == 1);
          if (!ok)
          {
            ok = true;
            boolean transition = false;
            if (tcret.getCurrentTile().isTransition)
            {
              transition = true;
              if (Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(tcret.getTileX(), tcret.getTileY())))) {
                ok = false;
              }
            }
            if ((ok) && (aPerformer.getCurrentTile().isTransition))
            {
              transition = true;
              if (Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(aPerformer.getTileX(), aPerformer
                .getTileY())))) {
                ok = false;
              }
            }
            if (!transition) {
              ok = false;
            }
          }
        }
        if (ok)
        {
          float iposX = tcret.getStatus().getPositionX();
          float iposY = tcret.getStatus().getPositionY();
          float iposZ = tcret.getStatus().getPositionZ() + tcret.getAltOffZ();
          if ((this.action >= 2000) && (this.action < 8000))
          {
            if (!aPerformer.isWithinDistanceTo(iposX, iposY, iposZ, Emotes.emoteEntrys[(this.action - 2000)]
              .getRange())) {
              throw new FailedException("You are too far away to do that.");
            }
          }
          else
          {
            if ((!getActionEntry().isIgnoresRange()) && 
              (Creature.rangeTo(aPerformer, tcret) > Actions.actionEntrys[getNumber()].getRange()) && ((!getActionEntry().isSpell()) || (!this.performer.isSpellCaster()))) {
              throw new FailedException("You are too far away to do that.");
            }
            if (((aPerformer instanceof Player)) || (aPerformer.isDominated()))
            {
              if ((this.isSpell) && (tcret.getPower() > aPerformer.getPower())) {
                throw new FailedException("Your spell dissolves in mid-air.");
              }
              if ((!aPerformer.equals(tcret)) && ((this.isOffensive) || (isActionAttack(this.action)) || (isActionShoot(this.action))))
              {
                Creature realPerformer = aPerformer;
                if (aPerformer.isDominated()) {
                  if (aPerformer.getDominator() != null) {
                    if (tcret != aPerformer.getDominator()) {
                      realPerformer = aPerformer.getDominator();
                    }
                  }
                }
                if (realPerformer.isPlayer()) {
                  if (!tcret.isDuelOrSpar(realPerformer))
                  {
                    if ((!realPerformer.isOnPvPServer()) || (!tcret.isOnPvPServer()))
                    {
                      if (tcret.isPlayer()) {
                        throw new FailedException("That would be very bad for your karma and is disallowed on this server.");
                      }
                      if ((tcret.isDominated()) || (tcret.getHitched() != null) || (
                        (tcret.getLeader() != null) && (tcret.getLeader().isPlayer()))) {
                        throw new FailedException("That would be very bad for your karma and is disallowed on this server.");
                      }
                    }
                    if ((realPerformer.getCitizenVillage() == null) || 
                      (tcret.getCurrentVillage() != realPerformer.getCitizenVillage())) {
                      if (realPerformer.getKingdomTemplateId() != 3) {
                        if (tcret.isRidden())
                        {
                          if ((realPerformer.isLegal()) || (!tcret.isOnPvPServer())) {
                            for (Long riderLong : tcret.getRiders()) {
                              try
                              {
                                Creature rider = Server.getInstance().getCreature(riderLong
                                  .longValue());
                                
                                boolean rok = !realPerformer.isFriendlyKingdom(rider.getKingdomId());
                                if (rider.isOkToKillBy(realPerformer)) {
                                  rok = true;
                                }
                                if (!rok)
                                {
                                  this.performer.setTarget(-10L, true);
                                  this.performer.getCommunicator().sendNormalServerMessage("That would be illegal. ");
                                  
                                  throw new FailedException("That would be illegal. ");
                                }
                              }
                              catch (NoSuchCreatureException localNoSuchCreatureException2) {}catch (NoSuchPlayerException localNoSuchPlayerException2) {}
                            }
                          }
                        }
                        else if (tcret.getHitched() != null)
                        {
                          if (!tcret.getHitched().isCreature()) {
                            try
                            {
                              Item i = Items.getItem(tcret.getHitched().wurmid);
                              long ownid = i.getLastOwnerId();
                              try
                              {
                                byte kingd = Players.getInstance().getKingdomForPlayer(ownid);
                                if ((realPerformer.isFriendlyKingdom(kingd)) && 
                                  (!realPerformer.hasBeenAttackedBy(ownid))) {
                                  if ((realPerformer.isLegal()) || 
                                    (!tcret.isOnPvPServer()))
                                  {
                                    boolean rok = false;
                                    try
                                    {
                                      Creature c = Server.getInstance().getCreature(ownid);
                                      if (c.isOkToKillBy(realPerformer)) {
                                        rok = true;
                                      }
                                    }
                                    catch (Exception localException) {}
                                    if (!rok)
                                    {
                                      this.performer.setTarget(-10L, true);
                                      this.performer
                                        .getCommunicator()
                                        .sendNormalServerMessage("That would be illegal. ");
                                      
                                      throw new FailedException("That would be illegal. ");
                                    }
                                  }
                                }
                              }
                              catch (Exception localException1) {}
                            }
                            catch (NoSuchItemException nsi)
                            {
                              logger.log(Level.INFO, tcret.getHitched().wurmid + " no such item:", nsi);
                            }
                          }
                        }
                        else if (tcret.isDominated())
                        {
                          if (realPerformer.isFriendlyKingdom(tcret.getKingdomId())) {
                            if ((realPerformer.isLegal()) || (!tcret.isOnPvPServer()))
                            {
                              boolean iok = false;
                              try
                              {
                                Creature owner = Server.getInstance().getCreature(tcret.dominator);
                                if (owner.isOkToKillBy(realPerformer)) {
                                  iok = true;
                                }
                              }
                              catch (NoSuchCreatureException localNoSuchCreatureException3) {}
                              if (!iok)
                              {
                                this.performer.setTarget(-10L, true);
                                this.performer.getCommunicator().sendNormalServerMessage("That would be illegal. ");
                                
                                throw new FailedException("That would be illegal. ");
                              }
                            }
                          }
                        }
                        else if (tcret.getCurrentVillage() != null)
                        {
                          Brand brand = Creatures.getInstance().getBrand(tcret.getWurmId());
                          if (brand != null) {
                            try
                            {
                              Village villageBrand = Villages.getVillage(
                                (int)brand.getBrandId());
                              if (tcret.getCurrentVillage() == villageBrand) {
                                if ((realPerformer.getCitizenVillage() != tcret.getCurrentVillage()) && (
                                  (realPerformer.isLegal()) || (!tcret.isOnPvPServer())))
                                {
                                  this.performer.setTarget(-10L, true);
                                  this.performer.getCommunicator().sendNormalServerMessage("That would be illegal. ");
                                  
                                  throw new FailedException("That would be illegal. ");
                                }
                              }
                            }
                            catch (NoSuchVillageException nsv)
                            {
                              brand.deleteBrand();
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
              else if (((this.action != 106) || (!tcret.mayCommand(aPerformer))) && ((this.action != 331) || 
                (!tcret.mayCommand(aPerformer))) && ((this.action != 332) || 
                (!tcret.mayPassenger(aPerformer))))
              {
                this.tilex = ((int)iposX >> 2);
                this.tiley = ((int)iposY >> 2);
                Village village = Zones.getVillage(this.tilex, this.tiley, tcret.isOnSurface());
                if ((village != null) && (!Actions.isActionManage(this.action))) {
                  if ((this.action == 106) || (this.action == 331) || (this.action == 332))
                  {
                    if (tcret.getDominator() != aPerformer)
                    {
                      boolean owner = Creatures.getInstance().wasLastLed(this.performer.getWurmId(), tcret
                        .getWurmId());
                      Brand brand = Creatures.getInstance().getBrand(tcret.getWurmId());
                      if (brand != null) {
                        try
                        {
                          Village v = Villages.getVillage((int)brand.getBrandId());
                          if ((v != null) && (v == this.performer.getCitizenVillage())) {
                            owner = true;
                          }
                        }
                        catch (NoSuchVillageException nsv)
                        {
                          brand.deleteBrand();
                        }
                      }
                      if (!owner) {
                        if (!village.isActionAllowed(this.action, aPerformer, false, 0, 0))
                        {
                          if ((!this.performer.isOnPvPServer()) || (!tcret.isOnPvPServer())) {
                            throw new FailedException("That would be very bad for your karma and is disallowed on this server.");
                          }
                          failCheckEnemy(aPerformer, village);
                        }
                      }
                    }
                  }
                  else if (!tcret.isDuelOrSpar(aPerformer)) {
                    if (!village.isActionAllowed(this.action, aPerformer, false, 0, 0)) {
                      failCheckEnemy(aPerformer, village);
                    }
                  }
                }
              }
            }
          }
        }
        else
        {
          throw new FailedException("You are too far away from " + tcret.getNameWithGenus() + ".");
        }
      }
      else if (this.targetType == 7)
      {
        if (this.action == -1) {
          this.action = 1;
        }
        if (this.action == 162) {
          this.action = 193;
        }
        this.tilex = Tiles.decodeTileX(this.target);
        this.tiley = Tiles.decodeTileY(this.target);
        boolean onSurface = Tiles.decodeLayer(this.target) == 0;
        VolaTile _tile = null;
        _tile = Zones.getTileOrNull(this.tilex, this.tiley, onSurface);
        if (_tile != null) {
          this.fence = _tile.getFence(this.target);
        }
        if (this.fence == null) {
          throw new NoSuchWallException("No fence with id " + this.target);
        }
        if ((this.action >= 8000) || (this.action < 2000))
        {
          if ((!aPerformer.isWithinTileDistanceTo(this.tilex, this.tiley, this.heightOffset, 1)) && 
            (!getActionEntry().isIgnoresRange())) {
            throw new FailedException("You are too far away to do that.");
          }
          Village village = this.fence.getVillage();
          if ((village != null) && (!Actions.isActionManage(this.action))) {
            if (!village.isActionAllowed(this.action, aPerformer))
            {
              if ((!this.fence.isOnPvPServer()) || (Servers.isThisAChaosServer())) {
                throw new FailedException("That would be very bad for your karma and is disallowed on this server.");
              }
              failCheckEnemy(aPerformer, village);
            }
          }
        }
      }
      else if (this.targetType == 23)
      {
        if (this.action == -1) {
          this.action = 1;
        }
        if (this.action == 162) {
          this.action = 193;
        }
        if ((this.action >= 8000) || (this.action < 2000) || (this.action == 508) || (this.action == 509) || (this.action == 507)) {
          if (!aPerformer.isWithinTileDistanceTo((int)(this.target >> 32) & 0xFFFF, (int)(this.target >> 16) & 0xFFFF, this.heightOffset, 1)) {
            if (((this.action > 2000) && (this.action < 8000)) || (!getActionEntry().isIgnoresRange())) {
              throw new FailedException("You are too far away to do that.");
            }
          }
        }
        this.tilex = Tiles.decodeTileX(this.target);
        this.tiley = Tiles.decodeTileY(this.target);
      }
      else if (this.targetType == 28)
      {
        if (this.action == -1) {
          this.action = 1;
        }
        if (this.action == 162) {
          this.action = 193;
        }
        if ((this.action >= 8000) || (this.action < 2000)) {
          if (!aPerformer.isWithinTileDistanceTo((int)(this.target >> 32) & 0xFFFF, (int)(this.target >> 16) & 0xFFFF, this.heightOffset, 1)) {
            throw new FailedException("You are too far away to do that.");
          }
        }
        this.tilex = Tiles.decodeTileX(this.target);
        this.tiley = Tiles.decodeTileY(this.target);
      }
      else if (!checkValidTileTarget(aPerformer))
      {
        if (this.targetType == 14)
        {
          if (this.action == -1) {
            this.action = 1;
          }
        }
        else if (this.targetType == 25)
        {
          if (this.action == -1) {
            this.action = 587;
          }
        }
        else if ((this.action >= 2000) && (this.action < 8000))
        {
          if (!aPerformer.isWithinDistanceTo(aPosX, aPosY, aPosZ, Emotes.emoteEntrys[(this.action - 2000)].getRange())) {
            throw new FailedException("You are too far away to do that.");
          }
        }
        else if (this.action < 2000)
        {
          if (this.action == -1) {
            this.action = 1;
          }
          if ((!getActionEntry().isIgnoresRange()) && 
            (!aPerformer.isWithinDistanceTo(aPosX, aPosY, aPosZ, Actions.actionEntrys[this.action].getRange()))) {
            throw new FailedException("You are too far away to do that.");
          }
        }
      }
    }
  }
  
  public Action(Creature aPerformer, long aSubj, long[] _targets, short act, float aPosX, float aPosY, float aPosZ, float aRot)
    throws NoSuchItemException, NoSuchCreatureException, NoSuchPlayerException, NoSuchBehaviourException, FailedException
  {
    this.performer = aPerformer;
    this.subject = aSubj;
    
    this.targets = _targets;
    this.numbTargets = this.targets.length;
    this.target = _targets[0];
    
    this.action = act;
    this.posX = aPosX;
    this.posY = aPosY;
    this.posZ = aPosZ;
    this.rot = aRot;
    this.targetType = WurmId.getType(this.target);
    
    this.behaviour = getBehaviour(this.target, aPerformer.isOnSurface());
    this.onSurface = getIsOnSurface(this.target, aPerformer.isOnSurface());
    this.targetType = WurmId.getType(this.target);
    if (this.action == -1) {
      this.action = 1;
    }
    if (isFatigue())
    {
      if ((this.performer.getVehicle() != -10L) && (!Actions.isActionAllowedOnVehicle(this.action))) {
        if (Actions.isActionAllowedOnBoat(this.action)) {
          try
          {
            Item vehicle = Items.getItem(this.performer.getVehicle());
            if (!vehicle.isBoat()) {
              throw new FailedException("You need to be on solid ground to do that.");
            }
            if (vehicle.getPosZ() > 0.0F) {
              throw new FailedException("The boat must be on water to do that from the boat.");
            }
          }
          catch (NoSuchItemException e)
          {
            throw new FailedException("You need to be on solid ground to do that.");
          }
        } else {
          throw new FailedException("You need to be on solid ground to do that.");
        }
      }
      if (aPerformer.getFatigueLeft() <= 0) {
        throw new FailedException("You are too mentally exhausted to do that now.");
      }
    }
    for (int x = 0; x < this.targets.length; x++)
    {
      Item targ = Items.getItem(this.targets[x]);
      this.tilex = ((int)targ.getPosX() >> 2);
      this.tiley = ((int)targ.getPosY() >> 2);
      if ((targ.isTraded()) || (targ.isBanked())) {
        throw new FailedException("You cannot use " + targ.getName() + " while trading it.");
      }
      if (targ.mailed) {
        throw new FailedException("You cannot use " + targ.getName() + " right now.");
      }
      try
      {
        long towner = targ.getOwner();
        if ((this.action == 7) && (targ.isBusy())) {
          throw new FailedException("You are using one of those items.");
        }
        if (towner != aPerformer.getWurmId()) {
          throw new FailedException("You need to carry that item to use it.");
        }
        if ((this.action == 7) && (
          (targ.getTemplateId() == 26) || (targ.getTemplateId() == 298)))
        {
          if (!MethodsItems.mayDropDirt(aPerformer)) {
            throw new FailedException("You are not allowed to drop dirt there.");
          }
        }
        else if (targ.isTraded()) {
          throw new FailedException("You may not tamper with items you are trading.");
        }
        if (towner == this.performer.getWurmId()) {
          this.personalAction = true;
        }
        if (this.action == 162) {
          this.personalAction = true;
        }
      }
      catch (NotOwnedException nso)
      {
        if (targ.getZoneId() < 0) {
          logger.log(Level.WARNING, aPerformer
            .getName() + " interacting with a " + targ.getName() + "(id=" + targ.getWurmId() + ") not in the world action=" + this.action);
        }
        float iposX = targ.getPosX();
        float iposY = targ.getPosY();
        boolean ok = aPerformer.isOnSurface() == targ.isOnSurface();
        if (!ok)
        {
          VolaTile lTile = Zones.getOrCreateTile((int)iposX >> 2, (int)iposY >> 2, targ.isOnSurface());
          if (lTile.isTransition) {
            if (Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile((int)iposX >> 2, (int)iposY >> 2)))) {
              ok = false;
            } else {
              ok = true;
            }
          }
        }
        if (!ok)
        {
          VolaTile lTile = Zones.getOrCreateTile(aPerformer.getTileX(), aPerformer.getTileY(), aPerformer
            .isOnSurface());
          if (lTile.isTransition) {
            if (Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(aPerformer.getTileX(), aPerformer
              .getTileY())))) {
              ok = false;
            } else {
              ok = true;
            }
          }
        }
        if (!ok) {
          throw new NoSuchItemException("You are too far away from " + targ.getNameWithGenus() + ".");
        }
      }
    }
  }
  
  private void failCheckEnemy(Creature aPerformer, Village village)
    throws FailedException
  {
    if (!village.isEnemy(aPerformer))
    {
      if (aPerformer.isLegal()) {
        throw new FailedException("That would be illegal here. You can check the settlement token for the local laws.");
      }
      if ((Actions.actionEntrys[this.action].isEnemyAllowedWhenNoGuards()) && (village.getGuards().length > 0)) {
        throw new FailedException("A guard has noted you and stops you with a warning.");
      }
      if (Actions.actionEntrys[this.action].isEnemyNeverAllowed()) {
        throw new FailedException("That action makes no sense here.");
      }
    }
    else
    {
      if ((Actions.actionEntrys[this.action].isEnemyAllowedWhenNoGuards()) && (village.getGuards().length > 0)) {
        throw new FailedException("A guard has noted you and stops you with a warning.");
      }
      if (Actions.actionEntrys[this.action].isEnemyNeverAllowed()) {
        throw new FailedException("That action makes no sense here.");
      }
    }
  }
  
  public final boolean checkValidTileTarget(Creature aPerformer)
    throws FailedException
  {
    if (this.targetType == 3)
    {
      if (this.action == -1) {
        this.action = 1;
      }
      MeshIO mesh = Server.surfaceMesh;
      this.tilex = Tiles.decodeTileX(this.target);
      this.tiley = Tiles.decodeTileY(this.target);
      this.tile = mesh.getTile(this.tilex, this.tiley);
      this.tilez = ((int)(Tiles.decodeHeight(this.tile) / 10.0F));
      if ((this.action >= 8000) || (this.action < 2000))
      {
        if ((aPerformer.getPower() < 5) && 
          (!aPerformer.isWithinTileDistanceTo(this.tilex, this.tiley, this.tilez, Actions.actionEntrys[this.action].getRange() / 4))) {
          if ((this.action > 8000) || (!getActionEntry().isIgnoresRange())) {
            throw new FailedException("You are too far away to do that.");
          }
        }
        Village village = null;
        int encodedTile = Server.surfaceMesh.getTile(this.tilex, this.tiley);
        if (getActionEntry().isCornerAction())
        {
          int digTilex = (int)this.performer.getStatus().getPositionX() + 2 >> 2;
          int digTiley = (int)this.performer.getStatus().getPositionY() + 2 >> 2;
          encodedTile = Server.surfaceMesh.getTile(digTilex, digTiley);
          village = Zones.getVillage(digTilex, digTiley, aPerformer.isOnSurface());
          if (village == null)
          {
            digTilex = (int)this.performer.getStatus().getPositionX() - 2 >> 2;
            village = Zones.getVillage(digTilex, digTiley, aPerformer.isOnSurface());
          }
          if (village == null)
          {
            digTiley = (int)this.performer.getStatus().getPositionY() - 2 >> 2;
            village = Zones.getVillage(digTilex, digTiley, aPerformer.isOnSurface());
          }
          if (village == null)
          {
            digTilex = (int)this.performer.getStatus().getPositionX() + 2 >> 2;
            village = Zones.getVillage(digTilex, digTiley, aPerformer.isOnSurface());
          }
        }
        else
        {
          village = Zones.getVillage(this.tilex, this.tiley, aPerformer.isOnSurface());
        }
        short checkAction = this.action;
        if (checkAction == 186) {
          try
          {
            Item item = Items.getItem(this.subject);
            if (item.getTemplateId() == 266) {
              checkAction = 660;
            }
          }
          catch (NoSuchItemException localNoSuchItemException) {}
        }
        if (village != null) {
          if ((!Actions.isActionManage(this.action)) && 
            (!village.isActionAllowed(checkAction, aPerformer, true, encodedTile, 0)))
          {
            if (!Zones.isOnPvPServer(this.tilex, this.tiley))
            {
              if (this.behaviour.getType() == 7) {
                throw new FailedException("This action is not allowed here, because the tree is on a player owned deed that has disallowed it.");
              }
              throw new FailedException("This action is not allowed here, because the tile is on a player owned deed that has disallowed it.");
            }
            failCheckEnemy(aPerformer, village);
          }
        }
      }
      return true;
    }
    if (this.targetType == 12)
    {
      if (this.action == -1) {
        this.action = 1;
      }
      if (this.action == 532) {
        this.action = 865;
      }
      if (this.action == 150) {
        this.action = 533;
      }
      this.tilex = Tiles.decodeTileX(this.target);
      this.tiley = Tiles.decodeTileY(this.target);
      Tiles.TileBorderDirection dir = Tiles.decodeDirection(this.target);
      this.heightOffset = Tiles.decodeHeightOffset(this.target);
      if (MethodsStructure.doesTileBorderContainWallOrFence(this.tilex, this.tiley, this.heightOffset, dir, aPerformer.isOnSurface(), true)) {
        throw new FailedException("There is a fence or wall there. Action not allowed.");
      }
      if ((this.action >= 8000) || (this.action < 2000))
      {
        if ((!aPerformer.isWithinTileDistanceTo(this.tilex, this.tiley, this.heightOffset, 1)) && 
          (!getActionEntry().isIgnoresRange())) {
          throw new FailedException("You are too far away to do that.");
        }
        VolaTile vtile = null;
        Structure structure = MethodsStructure.getStructureOrNullAtTileBorder(this.tilex, this.tiley, dir, this.performer.isOnSurface(), vtile);
        if ((structure == null) || (!structure.isTypeHouse()))
        {
          Village village = Zones.getVillage(this.tilex, this.tiley, true);
          if ((village != null) && (!Actions.isActionManage(this.action))) {
            if (!village.isActionAllowed(this.action, aPerformer))
            {
              if (!Zones.isOnPvPServer(this.tilex, this.tiley)) {
                throw new FailedException("This action is not allowed here, because the tile is on a player owned deed that has disallowed it.");
              }
              failCheckEnemy(aPerformer, village);
            }
          }
          if (dir == Tiles.TileBorderDirection.DIR_DOWN)
          {
            village = Zones.getVillage(this.tilex - 1, this.tiley, true);
            if ((village != null) && (!Actions.isActionManage(this.action))) {
              if (!village.isActionAllowed(this.action, aPerformer))
              {
                if (!Zones.isOnPvPServer(this.tilex - 1, this.tiley)) {
                  throw new FailedException("This action is not allowed here, because the tile is on a player owned deed that has disallowed it.");
                }
                failCheckEnemy(aPerformer, village);
              }
            }
          }
          else if (dir == Tiles.TileBorderDirection.DIR_HORIZ)
          {
            village = Zones.getVillage(this.tilex, this.tiley - 1, true);
            if ((village != null) && (!Actions.isActionManage(this.action))) {
              if (!village.isActionAllowed(this.action, aPerformer))
              {
                if (!Zones.isOnPvPServer(this.tilex, this.tiley - 1)) {
                  throw new FailedException("This action is not allowed here, because the tile is on a player owned deed that has disallowed it.");
                }
                failCheckEnemy(aPerformer, village);
              }
            }
          }
        }
      }
      return true;
    }
    if (this.targetType == 17)
    {
      if (this.action == -1) {
        this.action = 1;
      }
      this.tilex = Tiles.decodeTileX(this.target);
      this.tiley = Tiles.decodeTileY(this.target);
      int dir = CaveTile.decodeCaveTileDir(getTarget());
      MeshIO mesh = Server.caveMesh;
      this.tile = mesh.getTile(this.tilex, this.tiley);
      this.heightOffset = ((int)(Tiles.decodeHeight(this.tile) / 10.0F));
      if ((this.action >= 8000) || (this.action < 2000)) {
        if ((!getActionEntry().isIgnoresRange()) && 
          (!aPerformer.isWithinTileDistanceTo(this.tilex, this.tiley, this.heightOffset, Actions.actionEntrys[this.action]
          .getRange() / 4))) {
          throw new FailedException("You are too far away to do that.");
        }
      }
      Village village = Zones.getVillage(this.tilex, this.tiley, false);
      if ((village != null) && (!Actions.isActionManage(this.action)))
      {
        if (!village.isActionAllowed(this.action, aPerformer, false, this.tile, dir))
        {
          if (!Zones.isOnPvPServer(this.tilex, this.tiley)) {
            throw new FailedException("This action is not allowed here, because the tile is on a player owned deed that has disallowed it.");
          }
          failCheckEnemy(aPerformer, village);
        }
        byte type = Tiles.decodeType(this.tile);
        if ((this.action == 145) && (dir != 1) && ((this.tile == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id) || 
          (Tiles.isReinforcedFloor(type)) || (Tiles.isRoadType(type)))) {
          if (!village.isActionAllowed((short)229, aPerformer, true, this.tile, dir))
          {
            if (!Zones.isOnPvPServer(this.tilex, this.tiley)) {
              throw new FailedException("This action is not allowed here, because the tile is on a player owned deed that has disallowed it.");
            }
            failCheckEnemy(aPerformer, village);
          }
        }
      }
      return true;
    }
    return false;
  }
  
  public static final boolean isActionGuest(int actnum)
  {
    return (isActionEmote(actnum)) || (actnum == 1) || (actnum == 17) || (actnum == 77) || (actnum == 71);
  }
  
  public static final boolean isActionEmote(int actnum)
  {
    return (actnum >= 2000) && (actnum < 8000);
  }
  
  public static final boolean isActionRecipe(int actnum)
  {
    return (actnum >= 8000) && (actnum < 10000);
  }
  
  public static final boolean isActionAttack(int actnum)
  {
    return actnum == 114;
  }
  
  public static final boolean isStanceChange(short actnum)
  {
    return (actnum < 8000) && (Actions.actionEntrys[actnum].isStanceChange());
  }
  
  public final int getTargetType()
  {
    return this.targetType;
  }
  
  public boolean isOffensive()
  {
    return this.isOffensive;
  }
  
  public boolean isSpell()
  {
    return this.isSpell;
  }
  
  public final boolean isOpportunity()
  {
    return getActionEntry().isOpportunity();
  }
  
  public final boolean isVulnerable()
  {
    if (isManualInvulnerable()) {
      return false;
    }
    return getActionEntry().isVulnerable();
  }
  
  public static final boolean isActionShoot(int actnum)
  {
    return ((actnum >= 124) && (actnum <= 131)) || (actnum == 342);
  }
  
  public final boolean isBuildHouseWallAction()
  {
    return (isBuildWallAction()) || (isBuildDoorAction()) || 
      (isBuildDoubleDoorAction()) || (isBuildWindowAction()) || 
      (isBuildArchedWallAction()) || (isBuildPortcullisAction()) || 
      (isBuildBarredWall()) || (isBuildBalcony()) || 
      (isBuildJetty()) || (isBuildOriel()) || 
      (isBuildCanopyDoor()) || (isBuildScaffolding());
  }
  
  public final boolean isBuildWallAction()
  {
    return (this.action == 612) || (this.action == 617) || (this.action == 622) || (this.action == 648) || (this.action == 772) || (this.action == 784) || (this.action == 796) || (this.action == 808) || (this.action == 820);
  }
  
  public final boolean isBuildDoorAction()
  {
    return (this.action == 614) || (this.action == 619) || (this.action == 624) || (this.action == 651) || (this.action == 775) || (this.action == 787) || (this.action == 799) || (this.action == 811) || (this.action == 823);
  }
  
  public final boolean isBuildDoubleDoorAction()
  {
    return (this.action == 615) || (this.action == 620) || (this.action == 625) || (this.action == 652) || (this.action == 776) || (this.action == 788) || (this.action == 800) || (this.action == 812) || (this.action == 824);
  }
  
  public final boolean isBuildPortcullisAction()
  {
    return (this.action == 655) || (this.action == 657) || (this.action == 658) || (this.action == 778) || (this.action == 790) || (this.action == 802) || (this.action == 814) || (this.action == 826);
  }
  
  public final boolean isBuildBarredWall()
  {
    return (this.action == 656) || (this.action == 779) || (this.action == 791) || (this.action == 803) || (this.action == 815) || (this.action == 827);
  }
  
  public final boolean isBuildBalcony()
  {
    return this.action == 676;
  }
  
  public final boolean isBuildJetty()
  {
    return this.action == 677;
  }
  
  public final boolean isBuildOriel()
  {
    return (this.action == 678) || (this.action == 681) || (this.action == 780) || (this.action == 792) || (this.action == 804) || (this.action == 816) || (this.action == 828);
  }
  
  public final boolean isBuildCanopyDoor()
  {
    return this.action == 679;
  }
  
  public final boolean isBuildScaffolding()
  {
    return this.action == 869;
  }
  
  public final boolean isBuildNormalArchAction()
  {
    return (this.action == 616) || (this.action == 621) || (this.action == 626) || (this.action == 653) || (this.action == 777) || (this.action == 789) || (this.action == 801) || (this.action == 813) || (this.action == 825);
  }
  
  public final boolean isBuildLeftArchAction()
  {
    return (this.action == 760) || (this.action == 763) || (this.action == 766) || (this.action == 769) || (this.action == 781) || (this.action == 793) || (this.action == 805) || (this.action == 817) || (this.action == 829);
  }
  
  public final boolean isBuildRightArchAction()
  {
    return (this.action == 761) || (this.action == 764) || (this.action == 767) || (this.action == 770) || (this.action == 782) || (this.action == 794) || (this.action == 806) || (this.action == 818) || (this.action == 830);
  }
  
  public final boolean isBuildTArchAction()
  {
    return (this.action == 762) || (this.action == 765) || (this.action == 768) || (this.action == 771) || (this.action == 783) || (this.action == 795) || (this.action == 807) || (this.action == 819) || (this.action == 831);
  }
  
  public final boolean isBuildArchedWallAction()
  {
    return (isBuildNormalArchAction()) || (isBuildLeftArchAction()) || (isBuildRightArchAction()) || (isBuildTArchAction());
  }
  
  public final boolean isBuildWindowAction()
  {
    return (this.action == 613) || (this.action == 618) || (this.action == 623) || (this.action == 650) || (this.action == 649) || (this.action == 680) || (this.action == 773) || (this.action == 785) || (this.action == 797) || (this.action == 809) || (this.action == 821) || (this.action == 774) || (this.action == 786) || (this.action == 798) || (this.action == 810) || (this.action == 822);
  }
  
  public final ActionEntry getActionEntry()
  {
    return Actions.actionEntrys[getNumber()];
  }
  
  public Creature getPerformer()
  {
    return this.performer;
  }
  
  public void setDestroyedItem(@Nullable Item item)
  {
    this.destroyedItem = item;
  }
  
  public Item getDestroyedItem()
  {
    return this.destroyedItem;
  }
  
  public void setCreature(@Nullable Creature creature)
  {
    this.tempCreature = creature;
  }
  
  @Nullable
  public Creature getCreature()
  {
    return this.tempCreature;
  }
  
  public int getPriority()
  {
    if ((this.action >= 2000) && (this.action < 8000)) {
      return Emotes.emoteEntrys[(getNumber() - 2000)].getPriority();
    }
    return Actions.actionEntrys[getNumber()].getPriority();
  }
  
  public int currentSecond()
  {
    return this.currentSecond;
  }
  
  public int getSecond()
  {
    if (this.currentSecond == -1) {
      return this.lastSecond;
    }
    return this.currentSecond;
  }
  
  public boolean mayPlaySound()
  {
    return currentSecond() % 5 == 0;
  }
  
  private void maybeUpdateSecond()
  {
    int current = (int)this.counter;
    
    this.justTickedSecond = false;
    if (current == this.currentSecond)
    {
      this.lastSecond = this.currentSecond;
      this.currentSecond = -1;
    }
    else if (this.lastSecond != current)
    {
      this.currentSecond = current;
      this.justTickedSecond = true;
    }
  }
  
  public boolean isInterruptedAtMove()
  {
    if ((this.action >= 2000) && (this.action < 8000)) {
      return true;
    }
    if ((this.isSpell) || (this.action >= 8000) || (Actions.actionEntrys[getNumber()].isNoMove())) {
      return true;
    }
    return false;
  }
  
  static boolean getIsOnSurface(long id, boolean surfaced)
  {
    boolean isOnSurface = surfaced;
    int targetType = WurmId.getType(id);
    if (targetType == 3) {
      isOnSurface = true;
    } else if (targetType == 17) {
      isOnSurface = false;
    }
    return isOnSurface;
  }
  
  public static Behaviour getBehaviour(long id, boolean surfaced)
    throws NoSuchBehaviourException, NoSuchPlayerException, NoSuchCreatureException, NoSuchItemException
  {
    Behaviour behaviour = null;
    int targetType = WurmId.getType(id);
    if (targetType == 30)
    {
      behaviour = Behaviours.getInstance().getBehaviour((short)53);
    }
    else if (targetType == 27)
    {
      behaviour = Behaviours.getInstance().getBehaviour((short)54);
    }
    else if (targetType == 3)
    {
      int x = Tiles.decodeTileX(id);
      int y = Tiles.decodeTileY(id);
      if ((x >= 0) && (x < 1 << Constants.meshSize) && (y >= 0) && (y < 1 << Constants.meshSize))
      {
        MeshIO mesh = Server.surfaceMesh;
        int tile = mesh.getTile(x, y);
        byte type = Tiles.decodeType(tile);
        Tiles.Tile theTile = Tiles.getTile(type);
        if (theTile.isTree()) {
          behaviour = Behaviours.getInstance().getBehaviour((short)7);
        } else if (theTile.isBush()) {
          behaviour = Behaviours.getInstance().getBehaviour((short)7);
        } else if ((type == Tiles.Tile.TILE_GRASS.id) || (type == Tiles.Tile.TILE_MYCELIUM.id) || (type == Tiles.Tile.TILE_LAWN.id) || (type == Tiles.Tile.TILE_MYCELIUM_LAWN.id)) {
          behaviour = Behaviours.getInstance().getBehaviour((short)8);
        } else if (type == Tiles.Tile.TILE_KELP.id) {
          behaviour = Behaviours.getInstance().getBehaviour((short)8);
        } else if (type == Tiles.Tile.TILE_REED.id) {
          behaviour = Behaviours.getInstance().getBehaviour((short)8);
        } else if ((type == Tiles.Tile.TILE_ROCK.id) || (type == Tiles.Tile.TILE_CLIFF.id)) {
          behaviour = Behaviours.getInstance().getBehaviour((short)9);
        } else if (type == Tiles.Tile.TILE_DIRT.id) {
          behaviour = Behaviours.getInstance().getBehaviour((short)15);
        } else if ((type == Tiles.Tile.TILE_FIELD.id) || (type == Tiles.Tile.TILE_FIELD2.id)) {
          behaviour = Behaviours.getInstance().getBehaviour((short)17);
        } else {
          behaviour = Behaviours.getInstance().getBehaviour((short)5);
        }
      }
      else
      {
        throw new NoSuchBehaviourException("Out of the map [" + x + "," + y + "]. Allowing no actions.");
      }
    }
    else if ((targetType == 1) || (targetType == 0))
    {
      Creature targetCreature = Server.getInstance().getCreature(id);
      behaviour = targetCreature.getBehaviour();
    }
    else if ((targetType == 2) || (targetType == 6) || (targetType == 19) || (targetType == 20))
    {
      Item targetItem = Items.getItem(id);
      behaviour = targetItem.getBehaviour();
    }
    else if (targetType == 5)
    {
      behaviour = Behaviours.getInstance().getBehaviour((short)20);
    }
    else if (targetType == 7)
    {
      behaviour = Behaviours.getInstance().getBehaviour((short)22);
    }
    else if ((targetType == 8) || (targetType == 32))
    {
      behaviour = Behaviours.getInstance().getBehaviour((short)27);
    }
    else if (targetType == 17)
    {
      int x = (int)(id >> 32) & 0xFFFF;
      int y = (int)(id >> 16) & 0xFFFF;
      if ((x >= 0) && (x < 1 << Constants.meshSize) && (y >= 0) && (y < 1 << Constants.meshSize))
      {
        MeshIO mesh = Server.caveMesh;
        int tile = mesh.getTile(x, y);
        
        byte type = Tiles.decodeType(tile);
        if (Tiles.isSolidCave(type)) {
          behaviour = Behaviours.getInstance().getBehaviour((short)38);
        } else if ((type == Tiles.Tile.TILE_CAVE.id) || (Tiles.isRoadType(type)) || (Tiles.isReinforcedFloor(type)) || (type == Tiles.Tile.TILE_CAVE_EXIT.id) || (type == Tiles.Tile.TILE_ROCK.id)) {
          behaviour = Behaviours.getInstance().getBehaviour((short)39);
        } else {
          behaviour = Behaviours.getInstance().getBehaviour((short)5);
        }
      }
      else
      {
        throw new NoSuchBehaviourException("Out of the map. Allowing no actions.");
      }
    }
    else if (targetType == 12)
    {
      int x = Tiles.decodeTileX(id);
      int y = Tiles.decodeTileY(id);
      
      Tiles.TileBorderDirection dir = Tiles.decodeDirection(id);
      
      boolean onSurface = Tiles.decodeLayer(id) == 0;
      if (MethodsStructure.getStructureOrNullAtTileBorder(x, y, dir, onSurface) != null) {
        behaviour = Behaviours.getInstance().getBehaviour((short)6);
      } else {
        behaviour = Behaviours.getInstance().getBehaviour((short)32);
      }
    }
    else if (targetType == 14)
    {
      behaviour = Behaviours.getInstance().getBehaviour((short)36);
    }
    else if (targetType == 18)
    {
      behaviour = Behaviours.getInstance().getBehaviour((short)42);
    }
    else if (targetType == 22)
    {
      behaviour = Behaviours.getInstance().getBehaviour((short)43);
    }
    else if (targetType == 23)
    {
      behaviour = Behaviours.getInstance().getBehaviour((short)45);
    }
    else if (targetType == 25)
    {
      behaviour = Behaviours.getInstance().getBehaviour((short)50);
    }
    else if (targetType == 24)
    {
      behaviour = Behaviours.getInstance().getBehaviour((short)0);
    }
    else if (targetType == 28)
    {
      Tiles.TileBorderDirection dir = Tiles.decodeDirection(id);
      if (dir == Tiles.TileBorderDirection.CORNER) {
        behaviour = Behaviours.getInstance().getBehaviour((short)60);
      } else {
        behaviour = Behaviours.getInstance().getBehaviour((short)51);
      }
    }
    return behaviour;
  }
  
  public float getPosX()
  {
    return this.posX;
  }
  
  public float getPosY()
  {
    return this.posY;
  }
  
  public float getPosZ()
  {
    return this.posZ;
  }
  
  public void setPosX(float newPosX)
  {
    this.posX = newPosX;
  }
  
  public void setPosY(float newPosY)
  {
    this.posY = newPosY;
  }
  
  public float getRot()
  {
    return this.rot;
  }
  
  public int getTileX()
  {
    return this.tilex;
  }
  
  public int getTileY()
  {
    return this.tiley;
  }
  
  public boolean isOnSurface()
  {
    return this.onSurface;
  }
  
  void setActionString(String aActionString)
  {
    this.actionString = aActionString;
  }
  
  public int getTimeLeft()
  {
    return this.tenthOfSecondsLeftOnAction;
  }
  
  public float getFailSecond()
  {
    return this.failSecond;
  }
  
  public void setFailSecond(float second)
  {
    this.failSecond = second;
  }
  
  public float getPower()
  {
    return this.power;
  }
  
  public void setPower(float pow)
  {
    this.power = pow;
  }
  
  public void setTimeLeft(int _tenthOfSecondsLeftOnAction)
  {
    this.tenthOfSecondsLeftOnAction = _tenthOfSecondsLeftOnAction;
  }
  
  public String getActionString()
  {
    if (this.actionString.equals("")) {
      try
      {
        if (this.action < 2000) {
          return Actions.getVerbForAction(this.action);
        }
      }
      catch (Exception ex)
      {
        logger.log(Level.WARNING, ex.getMessage(), ex);
      }
    }
    return this.actionString;
  }
  
  static final boolean checkLegalMode(Creature performer)
  {
    if (!performer.isOnPvPServer())
    {
      performer.getCommunicator().sendNormalServerMessage("That would be very bad for your karma and is disallowed on this server.", (byte)3);
      return true;
    }
    Village v = performer.getCurrentVillage();
    boolean hasGuards = false;
    if (v != null) {
      hasGuards = v.guards.size() > 0;
    }
    if ((performer.isLegal()) && (hasGuards))
    {
      performer.getCommunicator().sendNormalServerMessage("That would be illegal here. You can check the settlement token for the local laws.", (byte)3);
      return true;
    }
    if ((performer.getDeity() != null) && (performer.faithful) && (!performer.getDeity().isLibila()))
    {
      performer.getCommunicator().sendNormalServerMessage("Your deity would never allow stealing.", (byte)3);
      return true;
    }
    return false;
  }
  
  public short getNumber()
  {
    return getNumber(this.action);
  }
  
  public static final short getNumber(short _action)
  {
    if (_action >= 20000) {
      return 116;
    }
    if (_action >= 10000) {
      return 148;
    }
    if (_action >= 8000) {
      return 148;
    }
    return _action;
  }
  
  public long getTarget()
  {
    return this.target;
  }
  
  public boolean justTickedSecond()
  {
    return this.justTickedSecond;
  }
  
  void setTarget(long aTarget)
  {
    this.target = aTarget;
  }
  
  public Behaviour getBehaviour()
  {
    return this.behaviour;
  }
  
  boolean isQuick()
  {
    return isQuick(this.action);
  }
  
  public static boolean isQuick(int actNum)
  {
    if (actNum < 8000)
    {
      if (actNum >= 2000) {
        return true;
      }
      try
      {
        return Actions.actionEntrys[actNum].isQuickSkillLess();
      }
      catch (ArrayIndexOutOfBoundsException ai)
      {
        logger.log(Level.WARNING, "Arrayindexexception for action: " + actNum, ai);
        return false;
      }
    }
    return false;
  }
  
  public final boolean isEquipAction()
  {
    switch (getNumber())
    {
    case 582: 
    case 583: 
    case 584: 
      return true;
    }
    return false;
  }
  
  public static boolean isStackable(int actNum)
  {
    if (actNum < 8000)
    {
      if (actNum >= 2000) {
        return true;
      }
      try
      {
        return Actions.actionEntrys[actNum].isStackable();
      }
      catch (ArrayIndexOutOfBoundsException ai)
      {
        logger.log(Level.WARNING, "Arrayindexexception for action: " + actNum, ai);
        return false;
      }
    }
    return false;
  }
  
  public static boolean isStackableFight(int actNum)
  {
    if (actNum < 8000)
    {
      if (actNum >= 2000) {
        return true;
      }
      try
      {
        return Actions.actionEntrys[actNum].isStackableFight();
      }
      catch (ArrayIndexOutOfBoundsException ai)
      {
        logger.log(Level.WARNING, "Arrayindexexception for action: " + actNum, ai);
        return false;
      }
    }
    return false;
  }
  
  public static int getBlockingNumber(int actNum)
  {
    if (actNum < 8000)
    {
      if (actNum >= 2000) {
        return 5;
      }
      try
      {
        return Actions.actionEntrys[actNum].getBlockType();
      }
      catch (ArrayIndexOutOfBoundsException ai)
      {
        logger.log(Level.WARNING, "Arrayindexexception for action: " + actNum, ai);
        return 4;
      }
    }
    return 4;
  }
  
  private boolean needsFood()
  {
    if (this.action < 8000)
    {
      if (this.action >= 2000) {
        return false;
      }
      try
      {
        return Actions.actionEntrys[this.action].needsFood();
      }
      catch (ArrayIndexOutOfBoundsException ai)
      {
        logger.log(Level.WARNING, "Arrayindexexception for action: " + this.action, ai);
        return false;
      }
    }
    return false;
  }
  
  private boolean isFatigue()
  {
    if (this.action < 8000)
    {
      if (this.action >= 2000) {
        return false;
      }
      try
      {
        return Actions.actionEntrys[this.action].isFatigue();
      }
      catch (ArrayIndexOutOfBoundsException ai)
      {
        logger.log(Level.WARNING, "Arrayindexexception for action: " + this.action, ai);
        return false;
      }
    }
    return true;
  }
  
  public final boolean isStanceChange()
  {
    return Actions.actionEntrys[getNumber(this.action)].isStanceChange();
  }
  
  public boolean isDefend()
  {
    return Actions.actionEntrys[getNumber()].isDefend();
  }
  
  public final boolean isSameBridge(int actNum)
  {
    if (actNum < 8000)
    {
      if (actNum >= 2000) {
        return false;
      }
      try
      {
        return Actions.actionEntrys[actNum].isSameBridgeOnly();
      }
      catch (ArrayIndexOutOfBoundsException ai)
      {
        logger.log(Level.WARNING, "Arrayindexexception for action: " + actNum, ai);
        return false;
      }
    }
    return false;
  }
  
  public boolean isEmote()
  {
    return isActionEmote(this.action);
  }
  
  public float getCounterAsFloat()
  {
    return this.counter;
  }
  
  public void resetCounter()
  {
    this.counter = 2.0F;
  }
  
  boolean poll()
  {
    try
    {
      if (isFatigue()) {
        if (this.performer.getFatigueLeft() <= 0)
        {
          this.performer.getCommunicator().sendNormalServerMessage("You are too mentally exhausted to do that now.");
          return true;
        }
      }
      if (this.isSpell)
      {
        if ((this.spell == null) || ((this.spell.religious) && (!this.performer.isPriest()) && (this.performer.getPower() == 0)))
        {
          this.performer.getCommunicator().sendNormalServerMessage("You fail to find any power to channel.");
          return true;
        }
        if (this.spell.offensive) {
          this.performer.removeIllusion();
        }
      }
      this.performer.getStatus().setNormalRegen(false);
      Item item = null;
      if ((this.subject != -1L) && (!isEmote()) && 
        (getActionEntry().getUseActiveItem() != 2)) {
        try
        {
          item = Items.getItem(this.subject);
          if (item.deleted)
          {
            if (!isQuick()) {
              this.performer.getCurrentTile().sendStopUseItem(this.performer);
            }
            if (getNumber() == 329) {
              this.performer.getCommunicator().sendStopUseBinoculars();
            }
            if ((getNumber() == 748) || (getNumber() == 759)) {
              this.performer.getCommunicator().sendHideLinks();
            }
            return true;
          }
        }
        catch (NoSuchItemException nex)
        {
          if (!isQuick()) {
            this.performer.getCurrentTile().sendStopUseItem(this.performer);
          }
          if (getNumber() == 329) {
            this.performer.getCommunicator().sendStopUseBinoculars();
          }
          if ((getNumber() == 748) || (getNumber() == 759)) {
            this.performer.getCommunicator().sendHideLinks();
          }
          return true;
        }
      }
      if ((item == null) && (!isEmote()) && (getActionEntry().getUseActiveItem() == 1)) {
        if (this.performer.isPlayer() == true)
        {
          this.performer.getCommunicator().sendNormalServerMessage("'" + getActionEntry().getActionString() + "' requires an active item.");
          return true;
        }
      }
      if ((this.counter == 0.0F) || (this.lastPolledAction == 0L))
      {
        this.counter = 1.0F;
        this.lastPolledAction = System.currentTimeMillis();
      }
      else
      {
        float change = (float)(System.currentTimeMillis() - this.lastPolledAction) / 1000.0F;
        if (change == 0.0F) {
          return false;
        }
        this.counter += Math.max(change, 1.0E-4F);
        this.lastPolledAction = System.currentTimeMillis();
      }
      maybeUpdateSecond();
      VolaTile t;
      if (this.done)
      {
        boolean toReturn = true;
        if ((this.action != 142) && (this.action != 148) && (this.action != 192) && (this.action != 350) && ((this.action < 496) || (this.action > 502))) {
          if (((this.action >= 2000) && (this.action < 8000)) || (!getActionEntry().isFatigue()) || (this.counter > 1.0F))
          {
            toReturn = MissionTriggers.activateTriggers(this.performer, item, this.action, this.target, this.triggerCounter);
            if ((!toReturn) && (this.targetType == 5))
            {
              t = Zones.getTileOrNull(this.tilex, this.tiley, this.performer.isOnSurface());
              if (t != null) {
                if (t.getStructure() != null) {
                  toReturn = MissionTriggers.activateTriggers(this.performer, item, this.action, t.getStructure()
                    .getWurmId(), this.triggerCounter);
                }
              }
            }
            if ((this.justTickedSecond) || (this.triggerCounter == 0)) {
              this.triggerCounter += 1;
            }
          }
        }
        return toReturn;
      }
      if ((!this.personalAction) && (!this.performer.isWithinDistanceTo(this.posX, this.posY, this.posZ, 12.0F, 2.0F)))
      {
        this.performer.getCommunicator().sendNormalServerMessage(stop(true));
        this.done = true;
        if ((this.action == 925) || (this.action == 926)) {
          this.performer.getCommunicator().sendCancelPlacingItem();
        } else if (this.action == 160) {
          MethodsFishing.playerOutOfRange(getPerformer(), this);
        }
      }
      else if (this.action == 157)
      {
        this.performer.setFightingStyle((byte)1);
        this.done = true;
      }
      else if (this.action == 159)
      {
        this.performer.setFightingStyle((byte)2);
        this.done = true;
      }
      else if (this.action == 158)
      {
        this.performer.setFightingStyle((byte)0);
        this.done = true;
      }
      else if (this.action == 84)
      {
        this.performer.setSpam(!this.performer.spamMode());
        this.done = true;
      }
      else if (this.action == 35)
      {
        this.performer.setLegal(!this.performer.isLegal());
        this.done = true;
      }
      else if (this.action == 36)
      {
        this.performer.setFaithMode(!this.performer.faithful);
        this.done = true;
      }
      else if (this.action == 341)
      {
        this.performer.setTarget(-10L, true);
        this.done = true;
      }
      else if (this.action == 38)
      {
        this.done = true;
        try
        {
          this.performer.setClimbing(true);
          this.performer.getStatus().sendStateString();
        }
        catch (IOException iox)
        {
          logger.log(Level.WARNING, "Failed to set climbing for " + this.performer.getName() + ": ", iox);
        }
      }
      else if (this.action == 39)
      {
        this.done = true;
        try
        {
          this.performer.setClimbing(false);
          this.performer.getStatus().sendStateString();
        }
        catch (IOException iox)
        {
          logger.log(Level.WARNING, "Failed to stop climbing for " + this.performer.getName() + ": ", iox);
        }
      }
      else if (this.action == 467)
      {
        this.done = true;
        if ((this.performer.getPower() >= 2) || (this.performer.mayMute()))
        {
          GmInterface gmi = new GmInterface(this.performer, this.performer.getWurmId());
          gmi.sendQuestion();
        }
      }
      else if (this.action == 534)
      {
        this.done = true;
        if (this.performer.getPower() >= 2)
        {
          GmTool gmt = new GmTool(this.performer, this.target);
          gmt.sendQuestion();
        }
      }
      else if ((this.action == 582) || (this.action == 583) || (this.action == 584))
      {
        AutoEquipMethods.autoEquip(this.target, this.performer, this.action, this);
        this.done = true;
      }
      else
      {
        Item localItem1;
        Item equipment;
        if (this.action == 585)
        {
          this.done = true;
          try
          {
            Item equip = Items.getItem(this.target);
            if (equip.isBodyPartAttached())
            {
              t = this.performer.getBody().getContainersAndWornItems();int i = t.length;
              for (localItem1 = 0; localItem1 < i; localItem1++)
              {
                equipment = t[localItem1];
                try
                {
                  if ((equipment.isArmour()) && (equipment.getParent().getWurmId() != this.performer.getBody().getId())) {
                    AutoEquipMethods.unequip(equipment, this.performer);
                  }
                }
                catch (NoSuchItemException e)
                {
                  logger.warning(String.format("Creature %s somehow had armour %s equipped without a parent. [Wurm ID %s]", new Object[] {this.performer
                    .getName(), equipment.getName(), Long.valueOf(equipment.getWurmId()) }));
                }
              }
            }
            else
            {
              AutoEquipMethods.unequip(this.target, this.performer);
            }
          }
          catch (NoSuchItemException e)
          {
            logger.warning(String.format("Creature %s tried unequipping an invalid item. [Wurm ID %s]", new Object[] {this.performer
              .getName(), Long.valueOf(this.target) }));
          }
        }
        else if (this.action == 693)
        {
          this.done = true;
          this.performer.setFlag(42, true);
          this.performer.achievement(141);
          this.performer.getCommunicator().sendNormalServerMessage("You decide to skip the tutorial.");
        }
        else if (this.action == 723)
        {
          this.done = AutoEquipMethods.timedDragEquip(this.performer, this.subject, this.target, this, this.counter);
        }
        else if (this.action == 724)
        {
          this.done = AutoEquipMethods.timedAutoEquip(this.performer, this.target, (short)724, this, this.counter);
        }
        else
        {
          if ((this.counter == 1.0F) && (this.performer.isPlayer())) {
            if ((this.performer.getPower() == 0) && (this.performer.isPriest()) && (this.performer.getDeity() != null) && 
              (!this.performer.isChampion())) {
              if ((getNumber() < 2000) || (getNumber() > 8000))
              {
                ActionEntry entry = Actions.actionEntrys[getNumber()];
                if ((entry.isNonReligion()) || 
                  ((this.performer.getDeity().number == 4) && (entry.isNonLibila())) || (
                  (this.performer.getDeity().number != 4) && (entry.isNonWhiteReligion()))) {
                  if (!entry.isAllowed(this.performer)) {
                    if (this.performer.faithful)
                    {
                      this.performer.getCommunicator().sendNormalServerMessage(
                        this.performer.getDeity().name + " would not approve of that.");
                      this.done = true;
                    }
                    else
                    {
                      this.performer.modifyFaith(-0.1F);
                    }
                  }
                }
              }
            }
          }
          if ((currentSecond() == 2) && (this.performer.isPlayer())) {
            if (isOpportunity())
            {
              this.performer.getCurrentTile().checkOpportunityAttacks(this.performer);
              if (this.performer.isDead()) {
                this.done = true;
              }
            }
          }
          if (!this.done)
          {
            if (isFatigue()) {
              if (justTickedSecond())
              {
                this.performer.decreaseFatigue();
                if (!this.isOffensive) {
                  this.performer.checkWorkMusic();
                }
              }
            }
            if ((currentSecond() == 1) && (this.performer.isStealth())) {
              if ((!isQuick()) || (isEmote())) {
                this.performer.setStealth(false);
              }
            }
            if (item != null) {
              if (currentSecond() == 1)
              {
                if (!isQuick()) {
                  if ((item.getOwnerId() == this.performer.getWurmId()) || (
                    (getNumber() >= 0) && 
                    (getNumber() < Actions.actionEntrys.length) && 
                    (!getActionEntry().isUseItemOnGroundAction()))) {
                    if (this.action == 182)
                    {
                      if ((this.target != -10L) && (this.targetType == 2)) {
                        try
                        {
                          Item targ = Items.getItem(this.target);
                          this.performer.getCurrentTile().sendUseItem(this.performer, targ.getModelName(), targ
                            .getRarity(), WurmColor.getColorRed(targ.getColor()), WurmColor.getColorGreen(targ.getColor()), 
                            WurmColor.getColorBlue(targ.getColor()), WurmColor.getColorRed(targ.getColor2()), WurmColor.getColorGreen(targ.getColor2()), 
                            WurmColor.getColorBlue(targ.getColor2()));
                        }
                        catch (NoSuchItemException localNoSuchItemException1) {}
                      }
                    }
                    else if (getActionEntry().getUseActiveItem() != 2) {
                      try
                      {
                        Item targ = Items.getItem(this.target);
                        boolean sendTarget = false;
                        if (!targ.isUseOnGroundOnly()) {
                          if ((targ.isTool()) && (!item.isTool())) {
                            sendTarget = true;
                          }
                        }
                        if (!sendTarget) {
                          this.performer.getCurrentTile().sendUseItem(this.performer, item.getModelName(), item
                            .getRarity(), WurmColor.getColorRed(item.getColor()), WurmColor.getColorGreen(item.getColor()), 
                            WurmColor.getColorBlue(item.getColor()), WurmColor.getColorRed(item.getColor2()), WurmColor.getColorGreen(item.getColor2()), 
                            WurmColor.getColorBlue(item.getColor2()));
                        } else {
                          this.performer.getCurrentTile().sendUseItem(this.performer, targ.getModelName(), targ
                            .getRarity(), WurmColor.getColorRed(targ.getColor()), WurmColor.getColorGreen(targ.getColor()), 
                            WurmColor.getColorBlue(targ.getColor()), WurmColor.getColorRed(targ.getColor2()), WurmColor.getColorGreen(targ.getColor2()), 
                            WurmColor.getColorBlue(targ.getColor2()));
                        }
                      }
                      catch (NoSuchItemException nsi)
                      {
                        this.performer.getCurrentTile().sendUseItem(this.performer, item.getModelName(), item
                          .getRarity(), WurmColor.getColorRed(item.getColor()), WurmColor.getColorGreen(item.getColor()), 
                          WurmColor.getColorBlue(item.getColor()), WurmColor.getColorRed(item.getColor2()), WurmColor.getColorGreen(item.getColor2()), 
                          WurmColor.getColorBlue(item.getColor2()));
                      }
                    }
                  }
                }
              }
              else if (currentSecond() == 2) {
                if (getNumber() == 329) {
                  this.performer.getCommunicator().sendUseBinoculars();
                }
              }
            }
            short checkAction;
            if (this.targetType == 3)
            {
              if ((this.counter == 1.0F) || (justTickedSecond()))
              {
                BlockingResult result = Blocking.getBlockerBetween(this.performer, this.target, true, 
                  getBlockingNumber(this.action), this.performer.getBridgeId(), -10L);
                if ((result != null) && (result.getTotalCover() >= 100.0F))
                {
                  this.performer.getCommunicator().sendNormalServerMessage("The " + result
                    .getFirstBlocker().getName() + " is in the way.");
                  this.done = true;
                }
                else if (!this.performer.isOnSurface())
                {
                  this.performer.getCommunicator().sendNormalServerMessage("The cave entrance is in the way.");
                  
                  this.done = true;
                }
              }
              if (!this.done)
              {
                this.tile = Server.surfaceMesh.getTile(this.tilex, this.tiley);
                if (getNumber() >= 2000)
                {
                  Emotes.emoteAt(this.action, this.performer, this.tilex, this.tiley, this.heightOffset, this.tile);
                  this.done = true;
                }
                else if ((Actions.actionEntrys[getNumber()].isQuickSkillLess()) && (this.action != 189))
                {
                  this.behaviour.action(this, this.performer, this.tilex, this.tiley, this.onSurface, this.tile, this.action, this.counter);
                  this.done = true;
                }
                else if (item != null)
                {
                  item.setBusy(true);
                  this.done = this.behaviour.action(this, this.performer, item, this.tilex, this.tiley, this.onSurface, this.heightOffset, this.tile, this.action, this.counter);
                }
                else
                {
                  this.done = this.behaviour.action(this, this.performer, this.tilex, this.tiley, this.onSurface, this.tile, this.action, this.counter);
                }
                if ((this.action >= 8000) || (this.action < 2000)) {
                  if ((this.counter == 1.0F) || (currentSecond() % 5 == 0))
                  {
                    int encodedTile = this.tile;
                    if (getActionEntry().isCornerAction())
                    {
                      int digTilex = (int)this.performer.getStatus().getPositionX() + 2 >> 2;
                      int digTiley = (int)this.performer.getStatus().getPositionY() + 2 >> 2;
                      encodedTile = Server.surfaceMesh.getTile(digTilex, digTiley);
                    }
                    Village village = Zones.getVillage(this.tilex, this.tiley, true);
                    if ((village != null) && (!Actions.isActionManage(this.action))) {
                      if ((this.action != 174) || (!village.isEnemy(this.performer)))
                      {
                        checkAction = this.action;
                        if (checkAction == 186) {
                          if ((item != null) && (item.getTemplateId() == 266)) {
                            checkAction = 660;
                          }
                        }
                        if ((!Actions.isActionManage(this.action)) && 
                          (!Methods.isActionAllowed(this.performer, checkAction, true, this.tilex, this.tiley, encodedTile, 0)) && 
                          (warnedPlayer(village))) {
                          this.done = true;
                        }
                      }
                    }
                  }
                }
              }
            }
            else if ((this.targetType == 1) || (this.targetType == 0))
            {
              try
              {
                Creature tcret = Server.getInstance().getCreature(this.target);
                if ((tcret != this.performer) && ((this.counter == 1.0F) || (justTickedSecond())))
                {
                  BlockingResult result = isActionShoot(this.action) ? Blocking.getRangedBlockerBetween(this.performer, tcret) : Blocking.getBlockerBetween(this.performer, tcret, 
                    getBlockingNumber(this.action));
                  if (result != null) {
                    if (isActionShoot(this.action))
                    {
                      Blocker[] arrayOfBlocker1 = result.getBlockerArray();localItem1 = arrayOfBlocker1.length;
                      for (equipment = 0; equipment < localItem1; equipment++)
                      {
                        Blocker b = arrayOfBlocker1[equipment];
                        if (b.getBlockPercent(this.performer) >= 100.0F)
                        {
                          if (this.performer.getPower() > 0) {
                            this.performer.getCommunicator().sendNormalServerMessage("The " + result
                              .getFirstBlocker().getName() + " at fl " + result
                              .getFirstBlocker().getFloorLevel() + "," + result
                              .getFirstBlocker().getId() + " is in the way.");
                          } else {
                            this.performer.getCommunicator().sendNormalServerMessage("The " + result
                              .getFirstBlocker().getName() + " is in the way.");
                          }
                          this.done = true;
                        }
                      }
                    }
                    else if (result.getTotalCover() >= 100.0F)
                    {
                      this.performer.getCommunicator().sendNormalServerMessage("The " + result
                        .getFirstBlocker().getName() + " is in the way.");
                      this.done = true;
                    }
                  }
                }
                if ((!this.done) && (tcret.getTemplateId() != 119)) {
                  if (getNumber() >= 2000)
                  {
                    Emotes.emoteAt(this.action, this.performer, tcret);
                    this.done = true;
                  }
                  else
                  {
                    boolean ok = this.performer.isOnSurface() == tcret.isOnSurface();
                    if (!ok)
                    {
                      ok = (this.action == 185) || (this.action == 1);
                      if (!ok)
                      {
                        ok = true;
                        boolean transition = false;
                        if (tcret.getCurrentTile().isTransition)
                        {
                          transition = true;
                          if (Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(tcret.getTileX(), tcret
                            .getTileY())))) {
                            ok = false;
                          }
                        }
                        if ((ok) && (this.performer.getCurrentTile().isTransition))
                        {
                          transition = true;
                          if (Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(this.performer
                            .getTileX(), this.performer.getTileY())))) {
                            ok = false;
                          }
                        }
                        if (!transition) {
                          ok = false;
                        }
                      }
                    }
                    if (ok)
                    {
                      if (!getActionEntry().isIgnoresRange()) {
                        if (Creature.rangeTo(this.performer, tcret) > Actions.actionEntrys[getNumber()].getRange())
                        {
                          this.performer.getCommunicator().sendNormalServerMessage("You are now too far away to " + Actions.actionEntrys[this.action]
                            .getActionString().toLowerCase() + " " + tcret
                            .getNameWithGenus() + ".");
                          this.performer.setOpponent(null);
                          this.done = true;
                          break label4199;
                        }
                      }
                      if (this.isOffensive)
                      {
                        if (!tcret.isDuelOrSpar(this.performer)) {
                          if (((!tcret.isOnPvPServer()) || (!this.performer.isOnPvPServer())) && 
                            (tcret.isPlayer()) && (this.performer.isPlayer()))
                          {
                            if ((tcret.getCitizenVillage() == null) || 
                              (!tcret.getCitizenVillage().isEnemy(this.performer)))
                            {
                              this.performer.getCommunicator().sendNormalServerMessage("That would be very bad for your karma and is disallowed on this server.");
                              
                              this.done = true;
                            }
                          }
                          else
                          {
                            this.tilex = tcret.getTileX();
                            this.tiley = tcret.getTileY();
                            Village village = Zones.getVillage(this.tilex, this.tiley, tcret.isOnSurface());
                            if ((village != null) && (!Actions.isActionManage(this.action))) {
                              if (((!isActionAttack(this.action)) && (!isActionShoot(this.action))) || ((this.counter <= 1.0F) || ((this.isSpell) && 
                                (currentSecond() <= 1))))
                              {
                                if ((this.performer.isFriendlyKingdom(tcret.getKingdomId())) && 
                                  (!this.performer.isFighting()) && 
                                  (!village.mayAttack(this.performer, tcret))) {
                                  if ((!this.performer.isOnPvPServer()) || (!tcret.isOnPvPServer()))
                                  {
                                    this.performer.getCommunicator().sendNormalServerMessage("That would be very bad for your karma and is disallowed on this server.");
                                    
                                    this.done = true;
                                  }
                                  else if (!tcret.isInvulnerable())
                                  {
                                    if (this.performer.mayAttack(tcret)) {
                                      if (((!isActionShoot(this.action)) || (!this.performer.isLegal())) && 
                                        (village.getReputation(tcret) > -30) && 
                                        (village.checkGuards(this, this.performer))) {
                                        village.modifyReputations(this, this.performer);
                                      }
                                    }
                                  }
                                }
                              }
                              else if (currentSecond() % 5 == 0) {
                                if (village.checkGuards(this, this.performer)) {
                                  village.resolveDispute(this.performer, tcret);
                                }
                              }
                            }
                          }
                        }
                      }
                      else if (currentSecond() % 5 == 0) {
                        if (!tcret.isDuelOrSpar(this.performer)) {
                          if ((!Methods.isActionAllowed(this.performer, this.action, true, this.tilex, this.tiley, 0, 0)) && 
                            (warnedPlayer())) {
                            this.done = true;
                          }
                        }
                      }
                      label4199:
                      if (!this.done)
                      {
                        if (Actions.actionEntrys[this.action].isQuickSkillLess())
                        {
                          this.behaviour.action(this, this.performer, tcret, this.action, this.counter);
                          this.done = true;
                        }
                        else if (this.action == 114)
                        {
                          this.done = this.behaviour.action(this, this.performer, tcret, this.action, this.counter);
                        }
                        else if (item != null)
                        {
                          if (!Actions.actionEntrys[this.action].isIgnoresRange()) {
                            if (Creature.rangeTo(this.performer, tcret) > Actions.actionEntrys[this.action].getRange())
                            {
                              this.performer.getCommunicator().sendNormalServerMessage("You are now too far away to " + Actions.actionEntrys[this.action]
                              
                                .getActionString() + " " + tcret
                                .getNameWithGenus() + ".");
                              this.done = true;
                              break label4451;
                            }
                          }
                          item.setBusy(true);
                          this.done = this.behaviour.action(this, this.performer, item, tcret, this.action, this.counter);
                        }
                        else
                        {
                          this.done = this.behaviour.action(this, this.performer, tcret, this.action, this.counter);
                        }
                        label4451:
                        if (tcret.isDead()) {
                          this.done = true;
                        }
                      }
                    }
                    else if (!getActionEntry().isIgnoresRange())
                    {
                      this.performer.getCommunicator().sendSafeServerMessage("You are too far away from " + tcret
                        .getNameWithGenus() + ".", (byte)3);
                      this.done = true;
                    }
                  }
                }
              }
              catch (NoSuchCreatureException nex)
              {
                this.done = true;
              }
              catch (NoSuchPlayerException nsp)
              {
                this.done = true;
              }
            }
            else if (this.numbTargets > 1)
            {
              try
              {
                Item[] targetItems = new Item[this.numbTargets];
                for (int x = 0; x < this.numbTargets; x++) {
                  targetItems[x] = Items.getItem(this.targets[x]);
                }
                this.done = this.behaviour.action(this, this.performer, targetItems, this.action, this.counter);
              }
              catch (NoSuchItemException nsi)
              {
                this.done = true;
              }
            }
            else if ((this.targetType == 2) || (this.targetType == 6) || (this.targetType == 19) || (this.targetType == 20))
            {
              try
              {
                Item targetItem = Items.getItem(this.target);
                if ((targetItem.getOwnerId() != this.performer.getWurmId()) && ((this.counter == 1.0F) || (justTickedSecond())))
                {
                  BlockingResult result = isActionShoot(this.action) ? Blocking.getRangedBlockerBetween(this.performer, targetItem) : Blocking.getBlockerBetween(this.performer, targetItem, 
                    getBlockingNumber(this.action));
                  boolean blocked = false;
                  if ((result != null) && (result.getTotalCover() >= 100.0F))
                  {
                    blocked = true;
                    if ((result.getFirstBlocker() == result.getLastBlocker()) && (result.getFirstBlocker() != null)) {
                      if (result.getFirstBlocker().isFence())
                      {
                        Fence f = (Fence)result.getFirstBlocker();
                        if ((f != null) && (f.getType() == StructureConstantsEnum.FENCE_SIEGEWALL)) {
                          blocked = false;
                        }
                      }
                    }
                  }
                  if (blocked)
                  {
                    this.performer.getCommunicator().sendNormalServerMessage("The " + result
                      .getFirstBlocker().getName() + " is in the way.");
                    throw new NoSuchItemException("The " + result.getFirstBlocker().getName() + " is in the way.");
                  }
                }
                try
                {
                  long towner;
                  long towner;
                  if ((getNumber() < 0) || 
                    (getNumber() >= Actions.actionEntrys.length) || 
                    (!getActionEntry().isUseItemOnGroundAction())) {
                    towner = targetItem.getOwner();
                  } else {
                    towner = -10L;
                  }
                  if ((towner != this.performer.getWurmId()) && (getNumber() >= 0) && 
                    (getNumber() < Actions.actionEntrys.length) && 
                    (!getActionEntry().isUseItemOnGroundAction()))
                  {
                    if ((this.action != 1) && (this.action != 87) && (!isActionEmote(this.action)) && (this.action != 185)) {
                      throw new NoSuchItemException("You are not the owner of that any longer.");
                    }
                  }
                  else if ((this.action == 7) && (
                    (targetItem.getTemplateId() == 26) || (targetItem.getTemplateId() == 298)))
                  {
                    if (!MethodsItems.mayDropDirt(this.performer)) {
                      throw new NoSuchItemException("You are not allowed to drop dirt there.");
                    }
                  }
                  else
                  {
                    if ((targetItem.isTraded()) && (this.action != 1) && (this.action != 87) && 
                      (!isActionEmote(this.action))) {
                      throw new NoSuchItemException("You may not tamper with items you are trading.");
                    }
                    if ((targetItem.isUseOnGroundOnly()) && (!isQuick()) && (this.action != 100) && (this.action != 176) && (this.action != 925) && (this.action != 926)) {
                      if ((this.performer.getPower() > 0) && (this.action != 180) && (this.action != 633)) {
                        throw new NoSuchItemException("You may only use that item while it is on the ground.");
                      }
                    }
                  }
                }
                catch (NotOwnedException localNotOwnedException) {}
                if (getNumber() >= 2000)
                {
                  Emotes.emoteAt(this.action, this.performer, targetItem);
                  this.done = true;
                }
                else if ((Actions.actionEntrys[getNumber()].isQuickSkillLess()) && (this.action < 8000))
                {
                  if ((item != null) && ((this.action == 117) || (this.action == 28) || (this.action == 102) || (this.action == 3) || (this.action == 76) || (this.action == 93) || (this.action == 54))) {
                    this.behaviour.action(this, this.performer, item, targetItem, this.action, this.counter);
                  } else {
                    this.behaviour.action(this, this.performer, targetItem, this.action, this.counter);
                  }
                  this.done = true;
                }
                else if (item == null)
                {
                  targetItem.setBusy(true);
                  this.done = this.behaviour.action(this, this.performer, targetItem, this.action, this.counter);
                }
                else
                {
                  item.setBusy(true);
                  targetItem.setBusy(true);
                  this.done = this.behaviour.action(this, this.performer, item, targetItem, this.action, this.counter);
                }
                if (this.done) {
                  if (targetItem != null) {
                    targetItem.setBusy(false);
                  }
                }
              }
              catch (NoSuchItemException nsi)
              {
                this.done = true;
              }
            }
            else if (this.targetType == 5)
            {
              if ((this.counter == 1.0F) || (justTickedSecond()))
              {
                BlockingResult result = Blocking.getBlockerBetween(this.performer, this.wall, 
                  getBlockingNumber(this.action));
                if ((result != null) && (result.getTotalCover() >= 100.0F))
                {
                  this.performer.getCommunicator().sendNormalServerMessage("The " + result
                    .getFirstBlocker().getName() + " is in the way.");
                  this.done = true;
                }
              }
              if (!this.done) {
                if (getNumber() >= 2000)
                {
                  Emotes.emoteAt(this.action, this.performer, this.wall);
                  this.done = true;
                }
                else if (item == null)
                {
                  this.done = this.behaviour.action(this, this.performer, this.wall, this.action, this.counter);
                }
                else
                {
                  item.setBusy(true);
                  this.done = this.behaviour.action(this, this.performer, item, this.wall, this.action, this.counter);
                }
              }
              if ((this.action >= 8000) || (this.action < 2000)) {
                if (currentSecond() % 5 == 0) {
                  if ((!Methods.isActionAllowed(this.performer, this.action, true, this.tilex, this.tiley, 0, 0)) && 
                    (warnedPlayer())) {
                    this.done = true;
                  }
                }
              }
            }
            else if (this.targetType == 7)
            {
              this.fence = Fence.getFence(this.target);
              if (this.fence != null)
              {
                if ((this.counter == 1.0F) || (justTickedSecond()))
                {
                  BlockingResult result = Blocking.getBlockerBetween(this.performer, this.fence, 
                    getBlockingNumber(this.action));
                  if ((result != null) && (result.getTotalCover() >= 100.0F))
                  {
                    this.performer.getCommunicator().sendNormalServerMessage("The " + result
                      .getFirstBlocker().getName() + " is in the way.");
                    this.done = true;
                  }
                }
                if (!this.done) {
                  if (getNumber() >= 2000)
                  {
                    Emotes.emoteAt(this.action, this.performer, this.fence);
                    this.done = true;
                  }
                  else if (item == null)
                  {
                    this.done = this.behaviour.action(this, this.performer, this.onSurface, this.fence, this.action, this.counter);
                  }
                  else
                  {
                    item.setBusy(true);
                    this.done = this.behaviour.action(this, this.performer, item, this.onSurface, this.fence, this.action, this.counter);
                  }
                }
                if ((this.action < 2000) || (this.action >= 8000)) {
                  if (currentSecond() % 5 == 0) {
                    if ((!Methods.isActionAllowed(this.performer, this.action, true, this.tilex, this.tiley, 0, 0)) && 
                      (warnedPlayer())) {
                      this.done = true;
                    }
                  }
                }
              }
              else
              {
                this.done = true;
              }
            }
            if (this.targetType == 17)
            {
              this.tile = Server.caveMesh.getTile(this.tilex, this.tiley);
              int dir = CaveTile.decodeCaveTileDir(this.target);
              if ((this.counter == 1.0F) || (justTickedSecond()))
              {
                int ceilingHeight = 0;
                if (dir == 1)
                {
                  int meshtile = Server.caveMesh.getTile(this.tilex, this.tiley);
                  ceilingHeight = Tiles.decodeData(meshtile) & 0xFF;
                }
                BlockingResult result = Blocking.getBlockerBetween(this.performer, this.target, false, 
                  getBlockingNumber(this.action), this.performer.getBridgeId(), -10L, ceilingHeight);
                if ((result != null) && (result.getTotalCover() >= 100.0F))
                {
                  this.performer.getCommunicator().sendNormalServerMessage("The " + result
                    .getFirstBlocker().getName() + " is in the way.");
                  this.done = true;
                }
              }
              if (!this.done) {
                if (getNumber() >= 2000)
                {
                  Emotes.emoteAt(this.action, this.performer, this.tilex, this.tiley, this.heightOffset, this.tile);
                  this.done = true;
                }
                else if ((Actions.actionEntrys[getNumber()].isQuickSkillLess()) && (this.action != 189))
                {
                  this.behaviour.action(this, this.performer, this.tilex, this.tiley, this.onSurface, this.tile, dir, this.action, this.counter);
                  this.done = true;
                }
                else if (item != null)
                {
                  item.setBusy(true);
                  if (this.performer == null) {
                    logger.log(Level.WARNING, "performer is null");
                  } else if (this.behaviour == null) {
                    logger.log(Level.WARNING, "behaviour is null");
                  }
                  this.done = this.behaviour.action(this, this.performer, item, this.tilex, this.tiley, this.onSurface, this.heightOffset, this.tile, dir, this.action, this.counter);
                }
                else
                {
                  this.done = this.behaviour.action(this, this.performer, this.tilex, this.tiley, this.onSurface, this.tile, dir, this.action, this.counter);
                }
              }
              if ((this.action >= 8000) || (this.action < 2000)) {
                if (currentSecond() % 5 == 0) {
                  if ((!Methods.isActionAllowed(this.performer, this.action, true, this.tilex, this.tiley, this.tile, dir)) && 
                    (warnedPlayer())) {
                    this.done = true;
                  }
                }
              }
            }
            else if (this.targetType == 12)
            {
              this.onSurface = (Tiles.decodeLayer(this.target) == 0);
              if ((this.counter == 1.0F) || (justTickedSecond()))
              {
                BlockingResult result = Blocking.getBlockerBetween(this.performer, this.target, this.onSurface, 
                  getBlockingNumber(this.action), this.performer.getBridgeId(), -10L);
                if ((result != null) && (result.getTotalCover() >= 100.0F))
                {
                  Blocker[] blockers = result.getBlockerArray();
                  if ((blockers.length != 1) || (!blockers[0].isRoof()))
                  {
                    this.performer.getCommunicator().sendNormalServerMessage("The " + result
                      .getFirstBlocker().getName() + " is in the way.");
                    this.done = true;
                  }
                }
              }
              if (!this.done)
              {
                Tiles.TileBorderDirection dir = Tiles.decodeDirection(this.target);
                if (item != null)
                {
                  item.setBusy(true);
                  if (this.performer == null) {
                    logger.log(Level.WARNING, "performer is null");
                  } else if (this.behaviour == null) {
                    logger.log(Level.WARNING, "behaviour is null");
                  }
                  this.done = this.behaviour.action(this, this.performer, item, this.tilex, this.tiley, this.onSurface, this.heightOffset, dir, this.target, this.action, this.counter);
                }
                else
                {
                  this.done = this.behaviour.action(this, this.performer, this.tilex, this.tiley, this.onSurface, dir, this.target, this.action, this.counter);
                }
                if ((this.action < 2000) || (this.action >= 8000)) {
                  if (currentSecond() % 5 == 0)
                  {
                    if ((!Methods.isActionAllowed(this.performer, this.action, true, this.tilex, this.tiley, 0, 0)) && 
                      (warnedPlayer())) {
                      this.done = true;
                    }
                    if (dir == Tiles.TileBorderDirection.DIR_DOWN)
                    {
                      if ((!Methods.isActionAllowed(this.performer, this.action, true, this.tilex - 1, this.tiley, 0, 0)) && 
                        (warnedPlayer())) {
                        this.done = true;
                      }
                    }
                    else if (dir == Tiles.TileBorderDirection.DIR_HORIZ) {
                      if ((!Methods.isActionAllowed(this.performer, this.action, true, this.tilex, this.tiley - 1, 0, 0)) && 
                        (warnedPlayer())) {
                        this.done = true;
                      }
                    }
                  }
                }
              }
            }
            else if (this.targetType == 27)
            {
              this.onSurface = (Tiles.decodeLayer(this.target) == 0);
              if ((this.counter == 1.0F) || (justTickedSecond()))
              {
                BlockingResult result = Blocking.getBlockerBetween(this.performer, this.target, this.onSurface, 
                  getBlockingNumber(this.action), this.performer.getBridgeId(), -10L);
                if ((result != null) && (result.getTotalCover() >= 100.0F))
                {
                  this.performer.getCommunicator().sendNormalServerMessage("The " + result
                    .getFirstBlocker().getName() + " is in the way.");
                  this.done = true;
                }
              }
              if (!this.done)
              {
                this.tilex = Tiles.decodeTileX(this.target);
                this.tiley = Tiles.decodeTileY(this.target);
                if ((!this.personalAction) && (!this.performer.isWithinDistanceTo(this.tilex << 2, this.tiley << 2, this.posZ, 4.0F, 0.0F)))
                {
                  this.performer.getCommunicator().sendNormalServerMessage("You are now too far away for " + 
                    getActionString().toLowerCase() + ".");
                  this.done = true;
                }
              }
              if (!this.done)
              {
                this.heightOffset = Tiles.decodeHeightOffset(this.target);
                if (this.onSurface) {
                  this.tile = Server.surfaceMesh.getTile(this.tilex, this.tiley);
                } else {
                  this.tile = Server.caveMesh.getTile(this.tilex, this.tiley);
                }
                if (item != null)
                {
                  item.setBusy(true);
                  if (this.performer == null) {
                    logger.log(Level.WARNING, "performer is null");
                  } else if (this.behaviour == null) {
                    logger.log(Level.WARNING, "behaviour is null");
                  }
                  this.done = this.behaviour.action(this, this.performer, item, this.tilex, this.tiley, this.onSurface, true, this.tile, this.heightOffset, this.action, this.counter);
                }
                else
                {
                  this.done = this.behaviour.action(this, this.performer, this.tilex, this.tiley, this.onSurface, true, this.tile, this.heightOffset, this.action, this.counter);
                }
                if ((this.action < 2000) || (this.action >= 8000)) {
                  if (currentSecond() % 5 == 0) {
                    if ((!Methods.isActionAllowed(this.performer, this.action, true, this.tilex, this.tiley, 0, 0)) && 
                      (warnedPlayer())) {
                      this.done = true;
                    }
                  }
                }
              }
            }
            else if ((this.targetType == 8) || (this.targetType == 32))
            {
              try
              {
                Wounds wounds = this.performer.getBody().getWounds();
                boolean found = false;
                if (wounds != null)
                {
                  Wound wound = wounds.getWound(this.target);
                  if (wound != null)
                  {
                    Creature tcret = wound.getCreature();
                    if (tcret != null)
                    {
                      if ((tcret != this.performer) && ((this.counter == 1.0F) || (justTickedSecond())))
                      {
                        BlockingResult result = Blocking.getBlockerBetween(this.performer, tcret, 
                          getBlockingNumber(this.action));
                        if ((result != null) && (result.getTotalCover() >= 100.0F))
                        {
                          this.performer.getCommunicator().sendNormalServerMessage("The " + result
                            .getFirstBlocker().getName() + " is in the way.");
                          this.done = true;
                        }
                        if (isSameBridge(this.action)) {
                          if (this.performer.getBridgeId() != tcret.getBridgeId())
                          {
                            this.performer.getCommunicator().sendNormalServerMessage("You need to be on the same bridge in order to do that. ");
                            this.done = true;
                          }
                        }
                      }
                      if (!this.done)
                      {
                        found = true;
                        if (getNumber() >= 2000)
                        {
                          Emotes.emoteAt(this.action, this.performer, wound);
                          this.done = true;
                        }
                        else if (item == null)
                        {
                          this.done = this.behaviour.action(this, this.performer, wound, this.action, this.counter);
                        }
                        else
                        {
                          item.setBusy(true);
                          this.done = this.behaviour.action(this, this.performer, item, wound, this.action, this.counter);
                        }
                      }
                    }
                  }
                }
                if (!found)
                {
                  Wound wound = Wounds.getAnyWound(this.target);
                  if (wound != null)
                  {
                    Creature tcret = wound.getCreature();
                    if (tcret != null)
                    {
                      if ((tcret != this.performer) && ((this.counter == 1.0F) || (justTickedSecond())))
                      {
                        BlockingResult result = Blocking.getBlockerBetween(this.performer, tcret, 
                          getBlockingNumber(this.action));
                        if ((result != null) && (result.getTotalCover() >= 100.0F))
                        {
                          this.performer.getCommunicator().sendNormalServerMessage("The " + result
                            .getFirstBlocker().getName() + " is in the way.");
                          this.done = true;
                        }
                        if (isSameBridge(this.action)) {
                          if (this.performer.getBridgeId() != tcret.getBridgeId())
                          {
                            this.performer.getCommunicator().sendNormalServerMessage("You need to be on the same bridge in order to do that. ");
                            this.done = true;
                          }
                        }
                      }
                      if (!this.done) {
                        if (getNumber() >= 2000)
                        {
                          Emotes.emoteAt(this.action, this.performer, wound);
                          this.done = true;
                        }
                        else
                        {
                          if (!getActionEntry().isIgnoresRange()) {
                            if (Creature.rangeTo(this.performer, tcret) > Actions.actionEntrys[this.action].getRange())
                            {
                              this.performer.getCommunicator().sendNormalServerMessage("You are now too far away to " + Actions.actionEntrys[this.action]
                              
                                .getActionString().toLowerCase() + " " + tcret
                                .getNameWithGenus() + ".");
                              this.done = true;
                              break label8350;
                            }
                          }
                          if (item == null)
                          {
                            this.done = this.behaviour.action(this, this.performer, wound, this.action, this.counter);
                          }
                          else
                          {
                            item.setBusy(true);
                            this.done = this.behaviour.action(this, this.performer, item, wound, this.action, this.counter);
                          }
                        }
                      }
                    }
                    else
                    {
                      this.done = true;
                    }
                  }
                  else
                  {
                    label8350:
                    this.done = true;
                  }
                }
              }
              catch (Exception ex)
              {
                this.done = true;
              }
            }
            else if (this.targetType == 14)
            {
              int planetId = (int)(this.target >> 16) & 0xFFFF;
              if (item == null) {
                this.done = this.behaviour.action(this, this.performer, planetId, this.action, this.counter);
              } else {
                this.done = this.behaviour.action(this, this.performer, item, planetId, this.action, this.counter);
              }
            }
            else if (this.targetType == 18)
            {
              int skillid = (int)(this.target >> 32) & 0xFFFFFFFF;
              if ((skillid == 2147483644) || (skillid == 2147483645) || (skillid == 2147483642) || (skillid == 2147483643) || (skillid == Integer.MAX_VALUE) || (skillid == 2147483646)) {
                this.done = true;
              } else {
                try
                {
                  Skill skill = this.performer.getSkills().getSkill(skillid);
                  if (item == null) {
                    this.done = this.behaviour.action(this, this.performer, skill, this.action, this.counter);
                  } else {
                    this.done = this.behaviour.action(this, this.performer, item, skill, this.action, this.counter);
                  }
                }
                catch (NoSuchSkillException localNoSuchSkillException) {}
              }
            }
            else if (this.targetType == 22)
            {
              int missionId = (int)(this.target >> 8) & 0xFFFFFFFF;
              if (item == null) {
                this.done = this.behaviour.action(this, this.performer, missionId, this.action, this.counter);
              } else {
                this.done = this.behaviour.action(this, this.performer, item, missionId, this.action, this.counter);
              }
            }
            else if (this.targetType == 25)
            {
              int ticketId = Tickets.decodeTicketId(this.target);
              this.done = this.behaviour.action(this, this.performer, ticketId, this.action, this.counter);
            }
            else if (this.targetType == 23)
            {
              this.tilex = Tiles.decodeTileX(this.target);
              this.tiley = Tiles.decodeTileY(this.target);
              byte layer = Tiles.decodeLayer(this.target);
              MeshIO mesh;
              MeshIO mesh;
              if (layer == 0) {
                mesh = Server.surfaceMesh;
              } else {
                mesh = Server.caveMesh;
              }
              this.tile = mesh.getTile(this.tilex, this.tiley);
              int htOffset = Floor.getHeightOffsetFromWurmId(this.target);
              this.tilez = ((int)(Tiles.decodeHeight(this.tile) / 10.0F));
              Floor[] floors = Zones.getFloorsAtTile(this.tilex, this.tiley, htOffset, htOffset, layer);
              if (floors == null)
              {
                this.performer.getCommunicator().sendNormalServerMessage("Could not find that floor.");
                this.done = true;
              }
              else if (floors.length == 1)
              {
                if ((this.counter == 1.0F) || (justTickedSecond()))
                {
                  BlockingResult result = Blocking.getBlockerBetween(this.performer, floors[0], 
                    getBlockingNumber(this.action));
                  if (result != null)
                  {
                    this.performer.getCommunicator().sendNormalServerMessage("The " + result
                      .getFirstBlocker().getName() + " is in the way.");
                    this.done = true;
                  }
                }
                if (!this.done) {
                  if (getNumber() >= 2000)
                  {
                    Emotes.emoteAt(this.action, this.performer, floors[0]);
                    this.done = true;
                  }
                  else
                  {
                    this.done = this.behaviour.action(this, this.performer, item, this.onSurface, floors[0], this.tile, this.action, this.counter);
                  }
                }
              }
              else
              {
                this.performer.getCommunicator().sendNormalServerMessage("Your sensitive mind notice a wrongness in the fabric of space.");
                
                this.done = true;
              }
            }
            else if (this.targetType == 28)
            {
              MeshIO mesh = Server.surfaceMesh;
              this.tile = mesh.getTile(this.tilex, this.tiley);
              this.tilex = Tiles.decodeTileX(this.target);
              this.tiley = Tiles.decodeTileY(this.target);
              this.tile = mesh.getTile(this.tilex, this.tiley);
              int ht = Tiles.decodeHeight(this.tile);
              int layer = Tiles.decodeLayer(this.target);
              
              this.tilez = ((int)(ht / 10.0F));
              BridgePart[] bridgeParts = Zones.getBridgePartsAtTile(this.tilex, this.tiley, layer == 0);
              if (bridgeParts == null)
              {
                this.performer.getCommunicator().sendNormalServerMessage("Could not find that bridge part.");
                this.done = true;
              }
              else
              {
                BridgePart foundPart = null;
                if (bridgeParts.length > 1)
                {
                  this.performer.getCommunicator().sendNormalServerMessage("Found more than 1 bridge part on tile.");
                  this.done = true;
                }
                else
                {
                  foundPart = bridgeParts[0];
                }
                if (!this.done) {
                  if (getNumber() >= 2000)
                  {
                    Emotes.emoteAt(this.action, this.performer, foundPart);
                    this.done = true;
                  }
                  else if (item == null)
                  {
                    this.done = this.behaviour.action(this, this.performer, this.onSurface, foundPart, this.tile, this.action, this.counter);
                  }
                  else
                  {
                    this.done = this.behaviour.action(this, this.performer, item, this.onSurface, foundPart, this.tile, this.action, this.counter);
                  }
                }
              }
            }
          }
        }
      }
      if (this.done)
      {
        if (getNumber() == 329) {
          this.performer.getCommunicator().sendStopUseBinoculars();
        }
        if ((getNumber() == 748) || (getNumber() == 759)) {
          this.performer.getCommunicator().sendHideLinks();
        }
        if (item != null)
        {
          item.setBusy(false);
          if (!isQuick()) {
            this.performer.getCurrentTile().sendStopUseItem(this.performer);
          }
          if (item.isRefreshedOnUse()) {
            item.setLastMaintained(WurmCalendar.currentTime);
          }
        }
        if ((this.targetType == 2) || (this.targetType == 6) || (this.targetType == 19) || (this.targetType == 20)) {
          try
          {
            Item targetItem = Items.getItem(this.target);
            targetItem.setBusy(false);
            if (targetItem.isRefreshedOnUse()) {
              targetItem.setLastMaintained(WurmCalendar.currentTime);
            }
          }
          catch (NoSuchItemException localNoSuchItemException2) {}
        }
      }
      if (this.done)
      {
        boolean toReturn = true;
        if ((this.action != 142) && (this.action != 148) && (this.action != 192) && (this.action != 350) && ((this.action < 496) || (this.action > 502))) {
          if (((this.action >= 2000) && (this.action < 8000)) || (!getActionEntry().isFatigue()) || (this.counter > 1.0F))
          {
            toReturn = MissionTriggers.activateTriggers(this.performer, item, this.action, this.target, this.triggerCounter);
            if ((!toReturn) && (this.targetType == 5))
            {
              VolaTile t = Zones.getTileOrNull(this.tilex, this.tiley, this.performer.isOnSurface());
              if (t != null) {
                if (t.getStructure() != null) {
                  toReturn = MissionTriggers.activateTriggers(this.performer, item, this.action, t.getStructure()
                    .getWurmId(), this.triggerCounter);
                }
              }
            }
            if ((this.action == 236) || (this.action == 235) || (this.action == 237) || (this.action == 7) || (this.action == 6) || (this.action == 342) || (this.action == 745) || (this.action == 4) || (this.action == 3) || (this.action == 1) || (this.action == 343) || (this.action == 344))
            {
              StringBuffer acts = new StringBuffer(Actions.actionEntrys[getNumber()].getAnimationString().toLowerCase().replace(" ", ""));
              if (Actions.actionEntrys[getNumber()].isSpell()) {
                acts.insert(0, "spell.");
              }
              this.performer.playAnimation(acts.toString(), false, this.target);
            }
            if ((this.justTickedSecond) || (this.triggerCounter == 0)) {
              this.triggerCounter += 1;
            }
          }
        }
        if ((toReturn) && (justTickedSecond() ? currentSecond() > 2 : this.lastSecond > 2))
        {
          this.performer.achievement(106);
          decayEnchants();
        }
        return toReturn;
      }
      if (currentSecond() == 1) {
        if (((getNumber() < 2000) || (getNumber() > 8000)) && (!isActionAttack(getNumber()))) {
          if ((this.targetType == 2) || (this.targetType == 6) || (this.targetType == 19) || (this.targetType == 20))
          {
            if ((this.action == 239) || (this.action == 238))
            {
              this.performer.playAnimation(Actions.actionEntrys['í'].getActionString().toLowerCase()
                .replace(" ", ""), false, this.target);
            }
            else
            {
              StringBuffer acts = new StringBuffer(Actions.actionEntrys[getNumber()].getActionString().toLowerCase().replace(" ", ""));
              if (Actions.actionEntrys[getNumber()].isSpell()) {
                acts.insert(0, "spell.");
              }
              this.performer.playAnimation(acts.toString(), false, this.target);
            }
          }
          else
          {
            StringBuffer acts = new StringBuffer(Actions.actionEntrys[getNumber()].getAnimationString());
            if (Actions.actionEntrys[getNumber()].isSpell()) {
              acts.insert(0, "spell.");
            }
            if (this.performer.getLayer() < 0)
            {
              if ((getNumber() == 532) || (getNumber() == 150)) {
                acts.append(".cave");
              }
              if (this.targetType == 17) {
                if ((getNumber() == 532) || (getNumber() == 150) || (getNumber() == 145))
                {
                  int dir = CaveTile.decodeCaveTileDir(this.target);
                  if (dir == 1) {
                    acts.append(".ceiling");
                  }
                }
              }
            }
            if ((this.targetType == 23) && (FloorBehaviour.isConstructionAction(getNumber())))
            {
              acts.setLength(0);
              acts.append("buildfloor");
              
              int tfl = Tiles.decodeFloorLevel(this.target);
              if (tfl > this.performer.getFloorLevel()) {
                acts.append(".above");
              }
            }
            if (this.targetType == 28)
            {
              acts.setLength(0);
              acts.append("buildbridge");
            }
            if ((getNumber() == 183) && ((this.targetType == 3) || (this.targetType == 17))) {
              acts.append(".ground");
            }
            if (getNumber() == 160)
            {
              Optional<Item> i = Items.getItemOptional(this.subject);
              if ((i.isPresent()) && (((Item)i.get()).getTemplateId() == 1343))
              {
                acts.append(".net");
                this.performer.playAnimation(acts.toString(), false, this.target);
              }
            }
            else
            {
              this.performer.playAnimation(acts.toString(), false, this.target);
            }
          }
        }
      }
    }
    catch (NullPointerException ex)
    {
      logger.log(Level.WARNING, "NullPointer while polling an action " + this.performer
        .getName() + " due to " + ex.getMessage(), ex);
      this.done = true;
    }
    catch (ArrayIndexOutOfBoundsException aio)
    {
      logger.log(Level.WARNING, "Array index out of bounds while polling an action " + this.performer.getName() + " due to " + aio
        .getMessage(), aio);
      this.done = true;
    }
    if ((this.done) && (justTickedSecond() ? currentSecond() > 2 : this.lastSecond > 2)) {
      this.performer.achievement(106);
    }
    return this.done;
  }
  
  public final boolean warnedPlayer()
  {
    Village village = Zones.getVillage(this.tilex, this.tiley, true);
    return warnedPlayer(village);
  }
  
  public final boolean warnedPlayer(Village village)
  {
    if (village != null) {
      if (village.checkGuards(this, this.performer))
      {
        this.performer.getCommunicator().sendNormalServerMessage("A guard has noted you and stops you with a warning.", (byte)4);
        
        village.modifyReputations(this, this.performer);
        if (village.getReputation(this.performer) <= -30) {
          village.addTarget(this.performer);
        }
        return true;
      }
    }
    return false;
  }
  
  public long getSubjectId()
  {
    return this.subject;
  }
  
  @Nullable
  public Item getSubject()
  {
    Item item = null;
    if ((this.subject != -1L) && (!isEmote()) && 
      (getActionEntry().getUseActiveItem() != 2)) {
      try
      {
        item = Items.getItem(this.subject);
      }
      catch (NoSuchItemException localNoSuchItemException) {}
    }
    return item;
  }
  
  public final void setRarity(byte newRarity)
  {
    this.rarity = newRarity;
  }
  
  public final byte getRarity()
  {
    return this.rarity;
  }
  
  public String stop(boolean farAway)
  {
    decayEnchants();
    
    String toReturn = "You stop.";
    int num = getNumber();
    if (num < 2000)
    {
      toReturn = "You stop " + Actions.actionEntrys[getNumber()].getVerbString().toLowerCase() + ".";
      if (this.isOffensive) {
        this.performer.setOpponent(null);
      }
    }
    if (num == 329) {
      this.performer.getCommunicator().sendStopUseBinoculars();
    }
    if ((getNumber() == 748) || (getNumber() == 759)) {
      this.performer.getCommunicator().sendHideLinks();
    } else if (num == 136) {
      this.performer.getCommunicator().sendToggle(3, false);
    } else if (num == 742) {
      if (this.performer.isPlayer()) {
        ((Player)this.performer).setIsWritingRecipe(false);
      }
    }
    if ((num == 118) && 
      (this.performer.isFrozen())) {
      this.performer.toggleFrozen(this.performer);
    }
    if (num == 160) {
      MethodsFishing.destroyFishCreature(this);
    }
    if (num == 353)
    {
      if (this.performer.getKingdomId() == 1)
      {
        if (Methods.getJennElector() != null) {
          Methods.getJennElector().submerge();
        }
        Methods.resetJennElector();
      }
      if (this.performer.getKingdomId() == 3) {
        Methods.resetHotsElector();
      }
      if (this.performer.getKingdomId() == 2) {
        Methods.resetMolrStone();
      }
    }
    if (this.destroyedItem != null) {
      Items.decay(this.destroyedItem.getWurmId(), this.destroyedItem.getDbStrings());
    }
    if (this.tempCreature != null) {
      this.tempCreature.destroy();
    }
    if (this.subject != -1L) {
      try
      {
        if (!isQuick()) {
          this.performer.getCurrentTile().sendStopUseItem(this.performer);
        }
        Item item = Items.getItem(this.subject);
        
        item.setBusy(false);
        if (item.isRefreshedOnUse()) {
          item.setLastMaintained(WurmCalendar.currentTime);
        }
        if ((item.getTemplate().isRune()) && (getNumber() == 118)) {
          toReturn = "You stop attaching.";
        }
      }
      catch (NoSuchItemException localNoSuchItemException) {}
    }
    if (((this.targetType == 2) || (this.targetType == 6) || (this.targetType == 19) || (this.targetType == 20)) && (this.destroyedItem == null)) {
      try
      {
        Item targetItem = Items.getItem(this.target);
        targetItem.setBusy(false);
        if (targetItem.isRefreshedOnUse()) {
          targetItem.setLastMaintained(WurmCalendar.currentTime);
        }
      }
      catch (NoSuchItemException localNoSuchItemException1) {}
    }
    if (farAway) {
      if (num < 2000) {
        toReturn = "You are now too far away to " + Actions.actionEntrys[num].getActionString().toLowerCase() + ".";
      }
    }
    return toReturn;
  }
  
  private void decayEnchants()
  {
    try
    {
      item = Items.getItem(this.subject);
    }
    catch (NoSuchItemException e)
    {
      Item item;
      return;
    }
    Item item;
    int rarityModifier = Math.max(1, item.getRarity() * 5);
    ItemSpellEffects eff = item.getSpellEffects();
    if (eff != null)
    {
      float runeModifier = eff.getRuneEffect(RuneUtilities.ModifierEffect.ENCH_ENCHRETENTION);
      double actionSeconds = this.counter;
      
      SpellEffect[] effs = eff.getEffects();
      for (int x = 0; x < effs.length; x++) {
        if (effs[x].type >= 0)
        {
          double enchantResistance = rarityModifier * effs[x].power * 30.0F * runeModifier;
          
          double enchantLossChance = 1.0D - Math.pow(1.0D - 1.0D / enchantResistance, Math.max(1.5D, actionSeconds / 5.0D - 0.5D));
          enchantLossChance *= Math.min(actionSeconds / 10.0D, 1.0D) * Math.min(actionSeconds / 10.0D, 1.0D);
          if (Server.rand.nextDouble() < enchantLossChance)
          {
            effs[x].setPower(effs[x].power - 1.0F);
            if (effs[x].power <= 0.0F) {
              eff.removeSpellEffect(effs[x].type);
            }
          }
        }
      }
    }
  }
  
  public boolean isManualInvulnerable()
  {
    return this.manualInvulnerable;
  }
  
  public void setManualInvulnerable(boolean aManualInvulnerable)
  {
    this.manualInvulnerable = aManualInvulnerable;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\behaviours\Action.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */