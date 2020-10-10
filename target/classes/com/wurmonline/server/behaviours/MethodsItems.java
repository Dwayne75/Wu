package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.mesh.Tiles.TileBorderDirection;
import com.wurmonline.mesh.TreeData;
import com.wurmonline.mesh.TreeData.TreeType;
import com.wurmonline.server.Constants;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Point;
import com.wurmonline.server.Point4f;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.combat.CombatEngine;
import com.wurmonline.server.combat.Weapon;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.VisionArea;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.effects.Effect;
import com.wurmonline.server.effects.EffectFactory;
import com.wurmonline.server.epic.Hota;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.items.CreationEntry;
import com.wurmonline.server.items.CreationMatrix;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemMealData;
import com.wurmonline.server.items.ItemSettings;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.NotOwnedException;
import com.wurmonline.server.items.Possessions;
import com.wurmonline.server.items.Puppet;
import com.wurmonline.server.items.RuneUtilities.ModifierEffect;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.kingdom.GuardTower;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Abilities;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.Friend;
import com.wurmonline.server.players.Friend.Category;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.Titles.Title;
import com.wurmonline.server.questions.QuestionTypes;
import com.wurmonline.server.questions.SleepQuestion;
import com.wurmonline.server.skills.AffinitiesTimed;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.spells.Spells;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.TempFence;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.tutorial.PlayerTutorial;
import com.wurmonline.server.tutorial.PlayerTutorial.PlayerTrigger;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.utils.logging.TileEvent;
import com.wurmonline.server.villages.GuardPlan;
import com.wurmonline.server.villages.Reputation;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageStatus;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.weather.Weather;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.WaterType;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.BridgeConstants.BridgeType;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.ItemMaterials;
import com.wurmonline.shared.constants.SoundNames;
import com.wurmonline.shared.constants.StructureConstantsEnum;
import com.wurmonline.shared.exceptions.WurmServerException;
import com.wurmonline.shared.util.TerrainUtilities;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class MethodsItems
  implements MiscConstants, QuestionTypes, ItemTypes, CounterTypes, ItemMaterials, SoundNames, VillageStatus, TimeConstants
{
  private static final float spawnDamageMod = Servers.localServer.isChallengeOrEpicServer() ? 5.0E-4F : 0.005F;
  public static final String cvsversion = "$Id: MethodsItems.java,v 1.84 2007-04-19 23:05:18 root Exp $";
  private static final Logger logger = Logger.getLogger(MethodsItems.class.getName());
  static final byte PICKTYPE_NONE = 0;
  static final byte PICKTYPE_DOOR = 1;
  static final byte PICKTYPE_LARGEVEHICLE = 2;
  static final byte PICKTYPE_GATE = 3;
  private static final int MAX_STRAIGHT_SLOPE = 40;
  private static final int MAX_DIAGONAL_SLOPE = 56;
  
  static final TakeResultEnum take(Action act, Creature performer, Item target)
  {
    if ((target.isBusy()) && (act.getNumber() != 925))
    {
      TakeResultEnum.TARGET_IN_USE.setIndexText(performer.getWurmId(), new String[] { target.getName() });
      return TakeResultEnum.TARGET_IN_USE;
    }
    try
    {
      long ownId = target.getOwner();
      if ((ownId != -10L) && (act.getNumber() != 582)) {
        return TakeResultEnum.TARGET_HAS_NO_OWNER;
      }
      if (ownId == performer.getWurmId()) {
        return TakeResultEnum.PERFORMER_IS_OWNER;
      }
    }
    catch (Exception ex)
    {
      if ((target.isCoin()) || (performer.getPossessions().getInventory().mayCreatureInsertItem()) || 
        (performer.getPower() > 0))
      {
        if (target.mailed) {
          return TakeResultEnum.TARGET_IS_UNREACHABLE;
        }
        if (target.isLiquid()) {
          return TakeResultEnum.TARGET_IS_LIQUID;
        }
        if (((target.isBulkContainer()) || (target.isTent())) && (!target.isEmpty(true))) {
          return TakeResultEnum.TARGET_FILLED_BULK_CONTAINER;
        }
        if (target.isBulkItem()) {
          return TakeResultEnum.TARGET_BULK_ITEM;
        }
        if (target.isTent())
        {
          Vehicle vehicle = Vehicles.getVehicle(target);
          if (vehicle != null) {
            if ((vehicle.getDraggers() != null) && (vehicle.getDraggers().size() > 0)) {
              return TakeResultEnum.HITCHED;
            }
          }
        }
        float weight = target.getFullWeight();
        if ((weight == 0.0F) || (performer.canCarry(target.getFullWeight()))) {
          try
          {
            BlockingResult result = Blocking.getBlockerBetween(performer, target, 4);
            if (result == null)
            {
              if (!target.isNoTake())
              {
                boolean sameVehicle = false;
                Item top = target.getTopParentOrNull();
                if ((top != null) && (top.isVehicle())) {
                  if (top.getWurmId() == performer.getVehicle()) {
                    sameVehicle = true;
                  }
                }
                if ((sameVehicle) || (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 5.0F)))
                {
                  Zone tzone = Zones.getZone((int)target.getPosX() >> 2, (int)target.getPosY() >> 2, target
                    .isOnSurface());
                  VolaTile tile = tzone.getTileOrNull((int)target.getPosX() >> 2, 
                    (int)target.getPosY() >> 2);
                  if (tile != null)
                  {
                    Structure struct = tile.getStructure();
                    
                    VolaTile tile2 = performer.getCurrentTile();
                    if (tile2 != null)
                    {
                      if (tile.getStructure() != struct) {
                        if ((struct == null) || (!struct.isTypeBridge()))
                        {
                          performer.getCommunicator().sendNormalServerMessage("You can't reach the " + target
                            .getName() + " through the wall.");
                          return TakeResultEnum.TARGET_BLOCKED;
                        }
                      }
                    }
                    else if (struct != null) {
                      if (!struct.isTypeBridge())
                      {
                        performer.getCommunicator().sendNormalServerMessage("You can't reach the " + target
                          .getName() + " through the wall.");
                        return TakeResultEnum.TARGET_BLOCKED;
                      }
                    }
                  }
                  long toppar = target.getTopParent();
                  if (!isLootableBy(performer, target)) {
                    return TakeResultEnum.MAY_NOT_LOOT_THAT_ITEM;
                  }
                  boolean mayUseVehicle = true;
                  try
                  {
                    Item topparent = Items.getItem(toppar);
                    if (topparent.isDraggable()) {
                      mayUseVehicle = mayUseInventoryOfVehicle(performer, topparent);
                    }
                    if ((!mayUseVehicle) && (target.lastOwner != performer.getWurmId())) {
                      if (((topparent.isVehicle()) && (topparent.getLockId() != -10L)) || (
                        (Items.isItemDragged(topparent)) && 
                        (performer.getDraggedItem() != topparent)))
                      {
                        TakeResultEnum.VEHICLE_IS_WATCHED.setIndexText(performer.getWurmId(), new String[] {topparent
                          .getName() });
                        return TakeResultEnum.VEHICLE_IS_WATCHED;
                      }
                    }
                  }
                  catch (NoSuchItemException localNoSuchItemException) {}
                  boolean dryRun;
                  Reputation rep;
                  if (checkIfStealing(target, performer, act))
                  {
                    if (act.getNumber() != 100)
                    {
                      TakeResultEnum.NEEDS_TO_STEAL.setIndexText(performer.getWurmId(), new String[] { target.getName() });
                      return TakeResultEnum.NEEDS_TO_STEAL;
                    }
                    if (Action.checkLegalMode(performer)) {
                      return TakeResultEnum.IN_LEGAL_MODE;
                    }
                    if (!performer.maySteal()) {
                      return TakeResultEnum.MAY_NOT_STEAL;
                    }
                    if (target.getItems().size() > 0)
                    {
                      TakeResultEnum.NEED_TO_BE_EMPTY_BEFORE_THEFT.setIndexText(performer
                        .getWurmId(), new String[] {target
                        .getName() });
                      return TakeResultEnum.NEED_TO_BE_EMPTY_BEFORE_THEFT;
                    }
                    Skill stealing = null;
                    dryRun = false;
                    Village v = Zones.getVillage(target.getTileX(), target.getTileY(), true);
                    if (v != null)
                    {
                      rep = v.getReputationObject(performer.getWurmId());
                      if (rep != null) {
                        if ((rep.getValue() >= 0) && (rep.isPermanent())) {
                          dryRun = true;
                        }
                      }
                    }
                    if (setTheftEffects(performer, act, target))
                    {
                      stealing = performer.getStealSkill();
                      stealing.skillCheck(target.getQualityLevel(), 0.0D, dryRun, 10.0F);
                      return TakeResultEnum.PREVENTED_THEFT;
                    }
                    stealing = performer.getStealSkill();
                    stealing.skillCheck(target.getQualityLevel(), 0.0D, dryRun, 10.0F);
                  }
                  if ((target.getParentId() == -10L) || 
                    (WurmId.getType(target.getParentId()) == 6))
                  {
                    long targid = target.getWurmId();
                    if (toppar == targid) {
                      try
                      {
                        Creature[] watchers = target.getWatchers();
                        for (Creature lWatcher : watchers) {
                          lWatcher.getCommunicator().sendCloseInventoryWindow(targid);
                        }
                      }
                      catch (NoSuchCreatureException localNoSuchCreatureException) {}
                    }
                    if (WurmId.getType(target.getParentId()) == 6)
                    {
                      Item parent = Items.getItem(target.getParentId());
                      parent.dropItem(target.getWurmId(), true);
                    }
                    int x = (int)target.getPosX() >> 2;
                    int y = (int)target.getPosY() >> 2;
                    Zone zone = Zones.getZone(x, y, target.isOnSurface());
                    zone.removeItem(target);
                    if (performer.getDraggedItem() == target) {
                      stopDragging(performer, target);
                    }
                  }
                  else
                  {
                    Item parent = Items.getItem(target.getParentId());
                    parent.dropItem(target.getWurmId(), true);
                  }
                  if ((target.isPlanted()) && (
                    (target.isSign()) || (target.isStreetLamp()) || (target.isFlag()) || 
                    (target.isBulkContainer()) || (target.getTemplateId() == 742)))
                  {
                    target.setIsPlanted(false);
                    if (target.isAbility())
                    {
                      target.hatching = false;
                      target.setRarity((byte)0);
                    }
                  }
                  if (target == performer.getDraggedItem()) {
                    performer.setDraggedItem(null);
                  }
                  if (target.isMushroom()) {
                    if (target.getLastOwnerId() <= 0L) {
                      performer.achievement(139);
                    }
                  }
                  if (target.getTemplate().isContainerWithSubItems())
                  {
                    ArrayList<Item> toMove = new ArrayList();
                    for (Item i : target.getItems()) {
                      if (i.isPlacedOnParent()) {
                        toMove.add(i);
                      }
                    }
                    for (Item i : toMove)
                    {
                      target.dropItem(i.getWurmId(), true);
                      Zones.getZone(i.getTileX(), i.getTileY(), target.isOnSurface()).addItem(i);
                      performer.getCommunicator().sendNormalServerMessage("The " + i.getName() + " drops to the ground.");
                    }
                  }
                  performer.getPossessions().getInventory().insertItem(target);
                  
                  target.setOnBridge(-10L);
                  target.setLastMaintained(WurmCalendar.currentTime);
                  
                  return TakeResultEnum.SUCCESS;
                }
                TakeResultEnum.TOO_FAR_AWAY.setIndexText(performer.getWurmId(), new String[] { target.getName() });
                return TakeResultEnum.TOO_FAR_AWAY;
              }
            }
            else
            {
              TakeResultEnum.TARGET_BLOCKED.setIndexText(performer.getWurmId(), new String[] { target.getName(), result
                .getFirstBlocker().getName() });
              return TakeResultEnum.TARGET_BLOCKED;
            }
          }
          catch (NoSuchZoneException|NoSuchItemException e)
          {
            logger.log(Level.WARNING, e.getMessage(), e);
          }
        }
        performer.achievement(165);
        TakeResultEnum.CARRYING_TOO_MUCH.setIndexText(performer.getWurmId(), new String[] { target.getName() });
        return TakeResultEnum.CARRYING_TOO_MUCH;
      }
      else
      {
        return TakeResultEnum.INVENTORY_FULL;
      }
    }
    return TakeResultEnum.UNKNOWN_FAILURE;
  }
  
  static final boolean setTheftEffects(Creature performer, Action act, Item target)
  {
    return setTheftEffects(performer, act, target.getTileX(), target.getTileY(), target.isOnSurface());
  }
  
  static final boolean setTheftEffects(Creature performer, Action act, int tilex, int tiley, boolean surfaced)
  {
    boolean noticed = false;
    boolean deityUpset = true;
    Village village = Zones.getVillage(tilex, tiley, performer.isOnSurface());
    if (village != null)
    {
      if ((village.guards.size() > 0) && (village.checkGuards(act, performer)))
      {
        noticed = true;
        if (!village.isEnemy(performer.getCitizenVillage()))
        {
          performer.setUnmotivatedAttacker();
          if (performer.getKingdomTemplateId() != 3) {
            if (Servers.localServer.HOMESERVER)
            {
              performer.setReputation(Math.max(-3, performer.getReputation() - 35));
              performer.setStealth(false);
            }
            else
            {
              performer.setReputation(performer.getReputation() - 35);
            }
          }
          village.modifyReputations(act, performer);
        }
        else
        {
          deityUpset = false;
        }
        performer.getCommunicator().sendNormalServerMessage("A guard has noted you!", (byte)4);
      }
    }
    else
    {
      VolaTile tile = Zones.getTileOrNull(tilex, tiley, surfaced);
      if ((tile != null) && (performer.isFriendlyKingdom(tile.getKingdom())) && (tile.getKingdom() != 3))
      {
        Structure struct = tile.getStructure();
        if ((struct != null) && (struct.isFinished()))
        {
          if (Servers.localServer.HOMESERVER) {
            if (!performer.isOnPvPServer())
            {
              performer.setUnmotivatedAttacker();
              performer.setReputation(Math.max(-3, performer.getReputation() - 35));
              performer.getCommunicator().sendNormalServerMessage("You get the feeling someone noticed you!", (byte)4);
            }
          }
          for (VirtualZone vz : tile.getWatchers()) {
            try
            {
              if ((vz.getWatcher() != null) && (vz.getWatcher().getCurrentTile() != null)) {
                if (vz.getWatcher().isFriendlyKingdom(performer.getKingdomId()))
                {
                  performer.setUnmotivatedAttacker();
                  boolean cares = false;
                  if ((Servers.localServer.HOMESERVER) && (!performer.isOnPvPServer()))
                  {
                    cares = true;
                    if (vz.getWatcher().isPlayer()) {
                      if (vz.getWatcher().getWurmId() != performer.getWurmId())
                      {
                        performer.setStealth(false);
                        vz.getWatcher()
                          .getCommunicator()
                          .sendNormalServerMessage("You notice " + performer
                          .getName() + " trying to do something fishy!", (byte)4);
                      }
                    }
                    if (vz.getWatcher().isKingdomGuard()) {
                      cares = true;
                    }
                    if (!cares) {
                      cares = struct.isGuest(vz.getWatcher());
                    }
                    if (vz.getWatcher().getWurmId() != performer.getWurmId())
                    {
                      float dist = Math.max(
                        Math.abs(vz.getWatcher().getCurrentTile().tilex - tilex), 
                        Math.abs(vz.getWatcher().getCurrentTile().tiley - tiley));
                      if ((cares) && (dist <= 20.0F)) {
                        if ((cares) && 
                          (performer.getStealSkill().skillCheck(100 - 
                          Math.min(
                          Math.abs(vz.getWatcher().getCurrentTile().tilex - tilex), 
                          
                          Math.abs(vz.getWatcher().getCurrentTile().tiley - tiley)) * 4, 0.0D, false, 10.0F) < 0.0D))
                        {
                          noticed = true;
                          performer.setReputation(performer.getReputation() - 10);
                          
                          performer.getCommunicator().sendNormalServerMessage("You get the feeling someone noticed you!", (byte)4);
                          
                          vz.getWatcher()
                            .getCommunicator()
                            .sendNormalServerMessage("You notice " + performer
                            .getName() + " trying to do something fishy!", (byte)4);
                          
                          performer.setStealth(false);
                          break;
                        }
                      }
                    }
                  }
                }
              }
            }
            catch (Exception e)
            {
              logger.log(Level.WARNING, e.getMessage(), e);
            }
          }
        }
      }
    }
    if ((deityUpset) && (performer.getDeity() != null) && (!performer.getDeity().isLibila())) {
      if (Server.rand.nextInt(Math.max(1, (int)performer.getFaith())) < 5) {
        if (act.getNumber() != 101)
        {
          performer.getCommunicator().sendNormalServerMessage(
            performer.getDeity().name + " has noticed you and is upset at your thieving behaviour!", (byte)4);
          
          performer.modifyFaith(-0.25F);
          performer.maybeModifyAlignment(-5.0F);
        }
      }
    }
    return noticed;
  }
  
  public static final boolean checkIfStealing(Item target, Creature player, @Nullable Action act)
  {
    if (target.getOwnerId() == -10L)
    {
      if (player.getPower() > 0) {
        return false;
      }
      if (target.lastOwner == player.getWurmId()) {
        return false;
      }
      if (target.getTemplateId() == 128) {
        return false;
      }
      if (target.getParentId() != -10L) {
        try
        {
          Item topparent = Items.getItem(target.getTopParent());
          if ((topparent.isDraggable()) && (topparent.isLockable()) && 
            (mayUseInventoryOfVehicle(player, topparent))) {
            return false;
          }
          if ((topparent.isLocked()) && 
            (topparent.mayShowPermissions(player)) && (topparent.mayAccessHold(player))) {
            return false;
          }
          VolaTile parentTile = Zones.getTileOrNull(topparent.getTilePos(), target.isOnSurface());
          if ((parentTile != null) && (parentTile.getStructure() != null)) {
            try
            {
              if (parentTile.getStructure().mayPass(player))
              {
                Item current = target.getParentOrNull();
                boolean mayEatThis = false;
                while (current != null)
                {
                  if ((current.isLocked()) && (current.mayAccessHold(player))) {
                    mayEatThis = true;
                  } else if ((current.isLocked()) && (!current.mayAccessHold(player))) {
                    return true;
                  }
                  current = current.getParentOrNull();
                }
                if ((mayEatThis) && (act != null) && (act.getNumber() == 182)) {
                  return false;
                }
              }
              if (!mayTakeThingsFromStructure(player, target, parentTile.tilex, parentTile.tiley)) {
                if ((target.lastOwner != -10L) && (target.lastOwner != player.getWurmId())) {
                  return true;
                }
              }
            }
            catch (NoSuchStructureException localNoSuchStructureException1) {}
          }
        }
        catch (NoSuchItemException localNoSuchItemException) {}
      }
      if ((act != null) && (act.getNumber() == 74) && (target.isVehicle()) && 
        (target.mayShowPermissions(player)) && (target.mayDrag(player))) {
        return false;
      }
      if ((act != null) && (act.getNumber() == 606)) {
        return false;
      }
      if ((act != null) && (act.getNumber() == 189) && (target.isOilConsuming())) {
        return false;
      }
      if ((act != null) && (act.getNumber() == 183) && (target.mayAccessHold(player))) {
        return false;
      }
      if ((act == null) && (target.mayShowPermissions(player)) && (target.mayAccessHold(player))) {
        return false;
      }
      int tilex = target.getTileX();
      int tiley = target.getTileY();
      Village village = Zones.getVillage(tilex, tiley, target.isOnSurface());
      if (village != null)
      {
        if ((!player.isOnPvPServer()) || (
          (player.isFriendlyKingdom(village.kingdom)) && (!village.isEnemy(player.getCitizenVillage()))))
        {
          try
          {
            if (!mayTakeThingsFromStructure(player, target, tilex, tiley)) {
              if ((target.lastOwner != -10L) && (target.lastOwner != player.getWurmId())) {
                return true;
              }
            }
          }
          catch (NoSuchStructureException nss)
          {
            if ((act != null) && (act.getNumber() == 83))
            {
              if ((target.lastOwner != -10L) && (target.lastOwner != player.getWurmId()) && 
                (!Methods.isActionAllowed(player, (short)83, target))) {
                return true;
              }
            }
            else
            {
              if ((target.lastOwner != -10L) && (target.lastOwner != player.getWurmId()) && 
                (!village.isActionAllowed((short)6, player, false, 0, 0))) {
                return true;
              }
              if ((act != null) && (act.getNumber() == 6)) {
                if ((target.isPlanted()) && ((target.isSign()) || (target.isStreetLamp()) || (target.isFlag()) || 
                  (target.isBulkContainer()) || (target.getTemplateId() == 742))) {
                  if ((target.lastOwner != -10L) && (target.lastOwner != player.getWurmId()) && 
                    (!village.isActionAllowed((short)685, player, false, 0, 0))) {
                    return true;
                  }
                }
              }
            }
          }
          return isStealingInVicinity(target, player);
        }
      }
      else if (player.getKingdomTemplateId() != 3)
      {
        try
        {
          if (!mayTakeThingsFromStructure(player, target, tilex, tiley)) {
            if ((target.lastOwner != -10L) && (target.lastOwner != player.getWurmId())) {
              return true;
            }
          }
        }
        catch (NoSuchStructureException localNoSuchStructureException2) {}
        return isStealingInVicinity(target, player);
      }
    }
    return false;
  }
  
  public static final boolean isStealingInVicinity(Item target, Creature player)
  {
    if (!Servers.localServer.PVPSERVER) {
      if ((target.lastOwner != -10L) && (target.lastOwner != player.getWurmId()) && (player.getPower() < 2)) {
        if (WurmId.getType(target.lastOwner) == 0) {
          try
          {
            Player lastOwner = Players.getInstance().getPlayer(target.lastOwner);
            if ((lastOwner.getPower() < 2) && (
              (lastOwner.getCitizenVillage() == null) || 
              (lastOwner.getCitizenVillage() != player.getCitizenVillage()))) {
              if (!lastOwner.isFriend(player.getWurmId())) {
                if ((lastOwner.getCitizenVillage() == null) || 
                  (!lastOwner.getCitizenVillage().isAlly(player))) {
                  if ((lastOwner.getTeam() == null) || (lastOwner.getTeam() != player.getTeam())) {
                    if (lastOwner.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), target
                      .isVehicle() ? 100.0F : 10.0F)) {
                      return true;
                    }
                  }
                }
              }
            }
          }
          catch (NoSuchPlayerException localNoSuchPlayerException) {}
        }
      }
    }
    return false;
  }
  
  public static final boolean mayTakeThingsFromStructure(Creature performer, @Nullable Item item, int tilex, int tiley)
    throws NoSuchStructureException
  {
    boolean onSurface = false;
    if (item == null) {
      onSurface = performer.isOnSurface();
    } else {
      onSurface = item.isOnSurface();
    }
    VolaTile tile = Zones.getTileOrNull(tilex, tiley, onSurface);
    if (tile != null)
    {
      Structure struct = tile.getStructure();
      if (performer.isInPvPZone()) {
        return true;
      }
      if ((struct != null) && (struct.isFinished()) && (struct.isTypeHouse()))
      {
        if ((!struct.isEnemy(performer)) && (!struct.mayPass(performer))) {
          return false;
        }
        long wid = performer.getWurmId();
        if (wid != struct.getOwnerId())
        {
          if (!struct.isActionAllowed(performer, (short)6)) {
            return false;
          }
          if ((item != null) && (item.isPlanted())) {
            return struct.isActionAllowed(performer, (short)685);
          }
        }
        return true;
      }
    }
    throw new NoSuchStructureException("No structure");
  }
  
  public static final boolean isEnemiesNearby(Creature performer, int tileDist, boolean requireSameVillage)
  {
    int sx = Zones.safeTileX(performer.getTileX() - tileDist);
    int sy = Zones.safeTileY(performer.getTileY() - tileDist);
    int ex = Zones.safeTileX(performer.getTileX() + tileDist);
    int ey = Zones.safeTileY(performer.getTileY() + tileDist);
    for (int x = sx; x <= ex; x++) {
      for (int y = sy; y <= ey; y++)
      {
        VolaTile t = Zones.getTileOrNull(x, y, performer.isOnSurface());
        Creature localCreature1;
        Creature c;
        if (t != null)
        {
          Creature[] arrayOfCreature1 = t.getCreatures();int i = arrayOfCreature1.length;
          for (localCreature1 = 0; localCreature1 < i; localCreature1++)
          {
            c = arrayOfCreature1[localCreature1];
            if ((c.isPlayer()) && (!c.isFriendlyKingdom(performer.getKingdomId())) && (c.isPaying()) && (c.getPower() == 0)) {
              if (requireSameVillage)
              {
                if (c.getCurrentVillage() == performer.getCurrentVillage()) {
                  return true;
                }
              }
              else {
                return true;
              }
            }
          }
        }
        VolaTile oT = Zones.getTileOrNull(x, y, !performer.isOnSurface());
        if (oT != null)
        {
          Creature[] arrayOfCreature2 = oT.getCreatures();localCreature1 = arrayOfCreature2.length;
          for (c = 0; c < localCreature1; c++)
          {
            Creature c = arrayOfCreature2[c];
            if ((c.isPlayer()) && (!c.isFriendlyKingdom(performer.getKingdomId())) && (c.isPaying()) && (c.getPower() == 0)) {
              if (requireSameVillage)
              {
                if (c.getCurrentVillage() == performer.getCurrentVillage()) {
                  return true;
                }
              }
              else {
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }
  
  public static final boolean isLootableBy(Creature performer, Item target)
  {
    if (performer.getPower() <= 0)
    {
      if ((target.getTemplateId() == 272) && (target.getWasBrandedTo() != -10L)) {
        return target.mayCommand(performer);
      }
      long targid = target.getWurmId();
      long toppar = target.getTopParent();
      Item topparent = null;
      try
      {
        if (toppar != targid) {
          topparent = Items.getItem(toppar);
        } else {
          topparent = target;
        }
        if ((topparent.isTent()) || (topparent.isUseMaterialAndKingdom())) {
          if ((topparent.isNewbieItem()) && (!Servers.localServer.PVPSERVER))
          {
            if (topparent.lastOwner != performer.getWurmId()) {
              return false;
            }
          }
          else if (topparent.getLockId() > -10L) {
            if (topparent.lastOwner != performer.getWurmId()) {
              return false;
            }
          }
        }
        if (topparent.getTemplateId() == 272)
        {
          VolaTile ttile = Zones.getTileOrNull(topparent.getTileX(), topparent.getTileY(), topparent
            .isOnSurface());
          int dist = (ttile == null) || (ttile.getVillage() == null) ? 10 : 5;
          if ((Servers.localServer.isChallengeOrEpicServer()) && (isEnemiesNearby(performer, dist, true))) {
            return false;
          }
          if (!topparent.isCorpseLootable()) {
            if (WurmId.getType(topparent.lastOwner) == 0) {
              if (topparent.lastOwner != performer.getWurmId())
              {
                byte kingdom = Players.getInstance().getKingdomForPlayer(topparent.lastOwner);
                if ((kingdom != 3) && (kingdom == performer.getKingdomId()) && 
                  (!Servers.isThisAChaosServer()))
                {
                  PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(topparent.lastOwner);
                  if (pinf != null)
                  {
                    if (!pinf.isFlagSet(34))
                    {
                      Friend[] friends = pinf.getFriends();
                      for (Friend lFriend : friends) {
                        if ((lFriend.getFriendId() == performer.getWurmId()) && 
                          (lFriend.getCategory() == Friend.Category.Trusted)) {
                          return true;
                        }
                      }
                    }
                    try
                    {
                      Village citizenVillage = Villages.getVillageForCreature(topparent.lastOwner);
                      if (citizenVillage != null)
                      {
                        if ((citizenVillage.isCitizen(performer)) && 
                          (!pinf.isFlagSet(33))) {
                          return true;
                        }
                        if ((citizenVillage.isAlly(performer)) && 
                          (!pinf.isFlagSet(32))) {
                          return true;
                        }
                      }
                      Player p = Players.getInstance().getPlayer(topparent.lastOwner);
                      if (p.getCitizenVillage() != null)
                      {
                        if ((p.getCitizenVillage().isCitizen(performer)) && 
                          (!pinf.isFlagSet(33))) {
                          return true;
                        }
                        if ((p.getCitizenVillage().isAlly(performer)) && 
                          (!pinf.isFlagSet(32))) {
                          return true;
                        }
                      }
                    }
                    catch (NoSuchPlayerException localNoSuchPlayerException) {}
                  }
                  return false;
                }
              }
            }
          }
        }
      }
      catch (NoSuchItemException nsi)
      {
        logger.log(Level.INFO, "No top parent for " + target.getTopParent());
        return false;
      }
    }
    return true;
  }
  
  static final boolean mayDropDirt(Creature performer)
  {
    int dropTileX = (int)performer.getStatus().getPositionX() + 2 >> 2;
    int dropTileY = (int)performer.getStatus().getPositionY() + 2 >> 2;
    
    Point tiles = findDropTile(dropTileX, dropTileY, performer.isOnSurface() ? Server.surfaceMesh : Server.caveMesh);
    dropTileX = tiles.getX();
    dropTileY = tiles.getY();
    for (int x = 0; x >= -1; x--) {
      for (int y = 0; y >= -1; y--)
      {
        Village village = Zones.getVillage(dropTileX + x, dropTileY + y, performer.isOnSurface());
        if ((village != null) && (!village.isActionAllowed((short)37, performer))) {
          return false;
        }
        if (Zones.protectedTiles[(dropTileX + x)][(dropTileY + y)] != 0)
        {
          performer.getCommunicator().sendAlertServerMessage("The tile is protected by the gods.");
          return false;
        }
      }
    }
    if (Features.Feature.WAGONER.isEnabled()) {
      if (MethodsHighways.onWagonerCamp(dropTileX, dropTileY, performer.isOnSurface()))
      {
        performer.getCommunicator().sendNormalServerMessage("The wagoner whips you once and tells you never to try dropping that here again.");
        return false;
      }
    }
    if (Terraforming.wouldDestroyCobble(performer, dropTileX, dropTileY, true))
    {
      if (Features.Feature.HIGHWAYS.isEnabled()) {
        performer.getCommunicator().sendAlertServerMessage("The tile is protected by the highway.", (byte)3);
      } else {
        performer.getCommunicator().sendAlertServerMessage("This would destroy the pavement.", (byte)3);
      }
      return false;
    }
    return true;
  }
  
  public static void handlePlaceItem(Creature performer, long itemId, long parentId, float xPos, float yPos, float zPos, float rotation)
  {
    boolean largeItem = (performer.getPlacementItem() != null) && (performer.getPlacementItem().getWurmId() == itemId);
    if (!performer.isPlacingItem())
    {
      performer.getCommunicator().sendNormalServerMessage("An error occured while placing that item.");
      return;
    }
    if (!largeItem) {
      performer.setPlacingItem(false);
    }
    if (itemId == -10L)
    {
      performer.getCommunicator().sendNormalServerMessage("You decide against placing the item.");
      performer.setPlacingItem(false);
      return;
    }
    Item parent = null;
    if (parentId != -10L) {
      try
      {
        parent = Items.getItem(parentId);
      }
      catch (NoSuchItemException nsi)
      {
        parent = null;
      }
    }
    if ((parent != null) && (largeItem))
    {
      performer.getCommunicator().sendNormalServerMessage("You must place the item from your inventory to put it there.");
      performer.setPlacingItem(false);
      return;
    }
    float newPosX = Math.max(1.0F, parent != null ? parent.getPosX() + xPos : xPos);
    float newPosY = Math.max(1.0F, parent != null ? parent.getPosY() + yPos : yPos);
    newPosY = Math.min(Zones.worldMeterSizeY, newPosY);
    newPosX = Math.min(Zones.worldMeterSizeX, newPosX);
    if ((!Methods.isActionAllowed(performer, (short)(largeItem ? 99 : 7), (int)newPosX >> 2, (int)newPosY >> 2)) && (parent != null) && 
      (Methods.isActionAllowed(performer, (short)3, parent)))
    {
      performer.setPlacingItem(false);
      return;
    }
    float xDist = Math.abs(performer.getPosX() - newPosX);
    float yDist = Math.abs(performer.getPosY() - newPosY);
    if (((xDist > 4.0F) || (yDist > 4.0F)) && (performer.getPower() < 2))
    {
      performer.getCommunicator().sendNormalServerMessage("You cannot place the item that far away.");
      performer.setPlacingItem(false);
      return;
    }
    try
    {
      Item target = Items.getItem(itemId);
      boolean onSurface = performer.isOnSurface();
      BlockingResult result = Blocking.getBlockerBetween(performer, performer.getPosX(), performer.getPosY(), newPosX, newPosY, performer
        .getPositionZ(), performer.getPositionZ(), onSurface, onSurface, false, 4, -1L, performer
        .getBridgeId(), performer.getBridgeId(), false);
      if (result != null)
      {
        performer.getCommunicator().sendNormalServerMessage("You cannot reach that spot to place the " + target.getName() + ".");
        return;
      }
      if (target.isOnePerTile()) {
        if (!mayDropOnTile((int)newPosX >> 2, (int)newPosY >> 2, onSurface, performer.getFloorLevel()))
        {
          performer.getCommunicator().sendNormalServerMessage("You cannot place that item here, since there is not enough space on that tile.", (byte)3);
          
          performer.setPlacingItem(false);
          return;
        }
      }
      if (target.isTent()) {
        if (!mayDropTentOnTile(performer))
        {
          performer.getCommunicator().sendNormalServerMessage("You are not allowed to put your tent there.", (byte)3);
          performer.setPlacingItem(false);
          return;
        }
      }
      if (!onSurface) {
        if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(Zones.safeTileX((int)newPosX >> 2), 
          Zones.safeTileY((int)newPosY >> 2)))))
        {
          performer.getCommunicator().sendNormalServerMessage("You cannot place the " + target.getName() + " inside the wall.");
          performer.setPlacingItem(false);
          return;
        }
      }
      if (parent != null)
      {
        if ((!parent.testInsertItem(target)) || (!parent.mayCreatureInsertItem()))
        {
          performer.getCommunicator().sendNormalServerMessage("There is no room for the " + target.getName() + " on the " + parent.getName() + ".");
          performer.setPlacingItem(false);
          return;
        }
        if ((target.isUnfinished()) && (target.getRealTemplate() != null) && (target.getRealTemplate().getVolume() > parent.getFreeVolume()))
        {
          performer.getCommunicator().sendNormalServerMessage("There is no room for the " + target.getName() + " on the " + parent.getName() + ".");
          performer.setPlacingItem(false);
          return;
        }
        if (parent.getPlacedItemCount() >= parent.getMaxPlaceableItems())
        {
          performer.getCommunicator().sendNormalServerMessage("You cannot put the " + target.getName() + " there, that would mean too many items on the " + parent
            .getName() + ".");
          performer.setPlacingItem(false);
          return;
        }
      }
      if ((!performer.isWithinDistanceTo(newPosX, newPosY, zPos, 6.0F)) && (performer.getPower() < 2))
      {
        performer.getCommunicator().sendNormalServerMessage("You cannot place the item that far away.");
        performer.setPlacingItem(false);
        return;
      }
      if (largeItem)
      {
        float actualDist = (float)Math.sqrt(xDist * xDist + yDist * yDist);
        
        float totalTime = (float)(performer.getPlacementItem().getWeightGrams(true) / 100000.0F * actualDist * (20.0D / performer.getBodyStrength().getKnowledge(0.0D))) * 100.0F;
        try
        {
          performer.getCurrentAction().setTimeLeft((int)Math.max(50.0F, Math.min(900.0F, totalTime)));
          performer.getCurrentAction().resetCounter();
          performer.sendActionControl(Actions.actionEntrys['Ξ'].getActionString(), true, (int)Math.max(50.0F, Math.min(900.0F, totalTime)));
          float rot = target.isVehicle() ? rotation + 180.0F : rotation;
          while (rot > 360.0F) {
            rot -= 360.0F;
          }
          while (rot < 0.0F) {
            rot += 360.0F;
          }
          performer.setPendingPlacement(newPosX, newPosY, zPos, rot);
        }
        catch (NoSuchActionException e)
        {
          performer.getCommunicator().sendNormalServerMessage("An error occured while placing that item, please try again.");
          performer.setPlacingItem(false, null);
        }
      }
      else
      {
        try
        {
          Zone zone = Zones.getZone(Zones.safeTileX((int)newPosX >> 2), Zones.safeTileY((int)newPosY >> 2), onSurface);
          
          long lParentId = target.getParentId();
          if (lParentId != -10L)
          {
            Item currentParent = Items.getItem(lParentId);
            currentParent.dropItem(target.getWurmId(), false);
          }
          if (parent != null)
          {
            target.setPos(newPosX - parent.getPosX(), newPosY - parent.getPosY(), zPos, rotation, parent.onBridge());
            if (parent.insertItem(target, false, false, true))
            {
              if (((parent.isLight()) || (parent.isFire()) || (parent.getTemplate().isCooker())) && (target.isBurnable())) {
                performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " will take damage if the " + parent
                  .getName() + " is lit.", (byte)4);
              }
              if (parent.getTemplate().isContainerWithSubItems()) {
                target.setPlacedOnParent(true);
              }
              VolaTile vt = Zones.getTileOrNull(parent.getTileX(), parent.getTileY(), parent.isOnSurface());
              if (vt != null) {
                for (VirtualZone vz : vt.getWatchers()) {
                  if (vz.isVisible(parent, vt))
                  {
                    vz.getWatcher().getCommunicator().sendItem(target, -10L, false);
                    if ((target.isLight()) && (target.isOnFire())) {
                      vt.addLightSource(target);
                    }
                    if (target.getEffects().length > 0) {
                      for (Effect e : target.getEffects()) {
                        vz.addEffect(e, false);
                      }
                    }
                    if (target.getColor() != -1) {
                      vz.sendRepaint(target.getWurmId(), (byte)WurmColor.getColorRed(target.getColor()), 
                        (byte)WurmColor.getColorGreen(target.getColor()), (byte)WurmColor.getColorBlue(target.getColor()), (byte)-1, (byte)0);
                    }
                    if (target.getColor2() != -1) {
                      vz.sendRepaint(target.getWurmId(), (byte)WurmColor.getColorRed(target.getColor2()), 
                        (byte)WurmColor.getColorGreen(target.getColor2()), (byte)WurmColor.getColorBlue(target.getColor2()), (byte)-1, (byte)1);
                    }
                  }
                }
              }
              performer.achievement(509);
            }
          }
          else
          {
            target.setOnBridge(performer.getBridgeId());
            float npsz = Zones.calculatePosZ(newPosX, newPosY, null, onSurface, 
              (target.isFloating()) && (target.getCurrentQualityLevel() > 10.0F), performer.getPositionZ(), performer, target.onBridge());
            float rot = target.isVehicle() ? rotation + 180.0F : rotation;
            while (rot > 360.0F) {
              rot -= 360.0F;
            }
            while (rot < 0.0F) {
              rot += 360.0F;
            }
            target.setPos(newPosX, newPosY, npsz, rot, target.onBridge());
            target.setSurfaced(onSurface);
            zone.addItem(target);
          }
          SoundPlayer.playSound("sound.object.move.pushpull", target, 0.0F);
          performer.getCommunicator().sendNormalServerMessage("You place " + target.getNameWithGenus() + ".");
          Server.getInstance().broadCastAction(performer.getName() + " places " + target.getNameWithGenus() + ".", performer, 5);
          PlayerTutorial.firePlayerTrigger(performer.getWurmId(), PlayerTutorial.PlayerTrigger.PLACED_ITEM);
        }
        catch (NoSuchZoneException sex)
        {
          logger.log(Level.WARNING, sex.getMessage(), sex);
          performer.getCommunicator().sendNormalServerMessage("Unable to place the " + target.getName() + " there.");
        }
      }
    }
    catch (NoSuchItemException e)
    {
      logger.log(Level.INFO, "Unable to find item " + itemId + " from " + performer.getName() + " place item response.");
    }
  }
  
  static boolean placeItem(Creature performer, Item target, Action act, float counter)
  {
    if ((counter > 1.0F) && (!performer.isPlacingItem())) {
      return true;
    }
    if (!target.canBeDropped(true))
    {
      if (target.isHollow()) {
        performer.getCommunicator().sendNormalServerMessage("You are not allowed to drop that. Make sure it doesn't contain non-dropable items.", (byte)3);
      } else {
        performer.getCommunicator().sendNormalServerMessage("You are not allowed to drop that.", (byte)3);
      }
      return true;
    }
    if (target.isOnePerTile()) {
      if (!mayDropOnTile(performer))
      {
        performer.getCommunicator().sendNormalServerMessage("You cannot drop that item here, since there is not enough space in front of you.", (byte)3);
        
        return true;
      }
    }
    if (target.isTent()) {
      if (!mayDropTentOnTile(performer))
      {
        performer.getCommunicator().sendNormalServerMessage("You are not allowed to put your tent there.", (byte)3);
        return true;
      }
    }
    long ownId = target.getOwnerId();
    if (ownId == -10L) {
      return true;
    }
    if (ownId != performer.getWurmId())
    {
      logger.log(Level.WARNING, "Hmm " + performer.getName() + " tried to drop " + target.getName() + " which wasn't his.");
      
      return true;
    }
    if (!Methods.isActionAllowed(performer, (short)7)) {
      return true;
    }
    if (counter == 1.0F)
    {
      performer.getCommunicator().sendNormalServerMessage("You start to place " + target.getNameWithGenus() + ".");
      Server.getInstance().broadCastAction(performer.getName() + " starts to place " + target.getNameWithGenus() + ".", performer, 5);
      act.setTimeLeft(1200);
      performer.sendActionControl(act.getActionString(), true, act.getTimeLeft());
      performer.getCommunicator().sendPlaceItem(target);
      performer.setPlacingItem(true);
      
      return false;
    }
    if (performer.isPlacingItem())
    {
      if (counter * 10.0F >= act.getTimeLeft())
      {
        performer.getCommunicator().sendNormalServerMessage("You decide against placing " + target.getNameWithGenus() + ".");
        performer.getCommunicator().sendCancelPlacingItem();
        performer.setPlacingItem(false, null);
        
        return true;
      }
      return false;
    }
    return true;
  }
  
  static boolean placeLargeItem(Creature performer, Item target, Action act, float counter)
  {
    if ((counter > 1.0F) && ((!performer.isPlacingItem()) || (performer.getPlacementItem() != target))) {
      return true;
    }
    if (target.getParentId() != -10L)
    {
      performer.getCommunicator().sendNormalServerMessage("You can not place that right now.", (byte)3);
      return true;
    }
    if (performer.isGuest())
    {
      performer.getCommunicator().sendNormalServerMessage("Sorry, but we cannot allow our guests to place items.", (byte)3);
      return true;
    }
    if ((!target.isTurnable(performer)) || (!target.isMoveable(performer)))
    {
      performer.getCommunicator().sendNormalServerMessage("Sorry, but you are not allowed to place that.", (byte)3);
      return true;
    }
    if (checkIfStealing(target, performer, act))
    {
      if (!performer.maySteal())
      {
        performer.getCommunicator().sendNormalServerMessage("You need more body control to steal things.", (byte)3);
        return true;
      }
      performer.getCommunicator().sendNormalServerMessage("You have to steal the " + target.getName() + " instead.", (byte)3);
      return true;
    }
    if ((target.isCorpse()) && (target.getWasBrandedTo() != -10L) && (!target.mayCommand(performer)))
    {
      performer.getCommunicator().sendNormalServerMessage("You may not move the corpse as you do not have permissions.", (byte)3);
      return true;
    }
    if ((target.isCorpse()) && (Servers.localServer.isChallengeOrEpicServer()))
    {
      VolaTile ttile = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface());
      int distance = (ttile == null) || (ttile.getVillage() == null) ? 10 : 5;
      if (isEnemiesNearby(performer, distance, true))
      {
        performer.getCommunicator().sendNormalServerMessage("You may not move the corpse when there are enemies nearby.", (byte)3);
        return true;
      }
    }
    if (Items.isItemDragged(target))
    {
      performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is being dragged and may not be moved that way at the moment.", (byte)3);
      
      return true;
    }
    Vehicle vehicle = Vehicles.getVehicle(target);
    boolean performerIsAllowedToDriveVehicle = false;
    if (vehicle != null)
    {
      for (Seat lSeat : vehicle.seats) {
        if (lSeat.isOccupied())
        {
          performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is occupied and may not be moved that way at the moment.", (byte)3);
          
          return true;
        }
      }
      if ((vehicle.draggers != null) && (vehicle.draggers.size() > 0))
      {
        performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " may not be moved that way at the moment.", (byte)3);
        return true;
      }
      if ((VehicleBehaviour.mayDriveVehicle(performer, target, act)) && (VehicleBehaviour.canBeDriverOfVehicle(performer, vehicle))) {
        performerIsAllowedToDriveVehicle = true;
      }
    }
    if (target.isBoat()) {
      if (target.getData() != -1L)
      {
        performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " won't budge. It's moored.", (byte)3);
        return true;
      }
    }
    if ((performerIsAllowedToDriveVehicle != true) || (!target.isVehicle())) {
      if (!Methods.isActionAllowed(performer, (short)99, target)) {
        return true;
      }
    }
    if (counter == 1.0F)
    {
      performer.getCommunicator().sendNormalServerMessage("You start to place " + target.getNameWithGenus() + ".");
      Server.getInstance().broadCastAction(performer.getName() + " starts to place " + target.getNameWithGenus() + ".", performer, 5);
      act.setTimeLeft(1200);
      performer.sendActionControl(act.getActionString(), true, act.getTimeLeft());
      performer.getCommunicator().sendPlaceItem(target);
      performer.setPlacingItem(true, target);
      
      return false;
    }
    if (performer.isPlacingItem())
    {
      if (performer.getPendingPlacement() != null)
      {
        if (act.justTickedSecond())
        {
          float[] targetPoint = performer.getPendingPlacement();
          if (targetPoint == null)
          {
            performer.getCommunicator().sendNormalServerMessage("You somehow forget where you were moving " + target.getNameWithGenus() + " to.");
            performer.getCommunicator().sendCancelPlacingItem();
            performer.setPlacingItem(false, null);
            return true;
          }
          float percentComplete = Math.min(1.0F, (counter - 1.0F) * 10.0F / act.getTimeLeft());
          float[] diff = { (targetPoint[4] - targetPoint[0]) * percentComplete, (targetPoint[5] - targetPoint[1]) * percentComplete, (targetPoint[6] - targetPoint[2]) * percentComplete, (targetPoint[7] - targetPoint[3]) * percentComplete };
          
          float newPosX = targetPoint[0] + diff[0];
          float newPosY = targetPoint[1] + diff[1];
          float newPosZ = targetPoint[2] + diff[2];
          float rotation = targetPoint[3] + diff[3];
          boolean onSurface = performer.getPlacementItem().isOnSurface();
          try
          {
            Zone oldZone = Zones.getZone(target.getTileX(), target.getTileY(), target.isOnSurface());
            oldZone.removeItem(target, true, true);
            long lParentId = target.getParentId();
            if (lParentId != -10L)
            {
              Item parent = Items.getItem(lParentId);
              parent.dropItem(target.getWurmId(), false);
            }
            target.setOnBridge(performer.getBridgeId());
            float npsz = Zones.calculatePosZ(newPosX, newPosY, null, onSurface, 
              (target.isFloating()) && (target.getCurrentQualityLevel() > 10.0F), performer.getPositionZ(), performer, target.onBridge());
            target.setPos(newPosX, newPosY, npsz, rotation, target.onBridge());
            target.setSurfaced(onSurface);
            
            Zone newZone = Zones.getZone(target.getTileX(), target.getTileY(), onSurface);
            newZone.addItem(target, true, false, false);
            performer.getStatus().modifyStamina(-1000.0F);
          }
          catch (NoSuchZoneException|NoSuchItemException sex)
          {
            logger.log(Level.WARNING, sex.getMessage(), sex);
            performer.getCommunicator().sendNormalServerMessage("Unable to place the " + target.getName() + " there.");
          }
          if (percentComplete >= 1.0F)
          {
            performer.getCommunicator().sendNormalServerMessage("You finish placing " + target.getNameWithGenus() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " finishes placing " + target.getNameWithGenus() + ".", performer, 5);
            performer.getCommunicator().sendCancelPlacingItem();
            performer.setPlacingItem(false, null);
            
            return true;
          }
          return false;
        }
      }
      else if (counter * 10.0F >= act.getTimeLeft())
      {
        performer.getCommunicator().sendNormalServerMessage("You decide against placing " + target.getNameWithGenus() + ".");
        performer.getCommunicator().sendCancelPlacingItem();
        performer.setPlacingItem(false, null);
        
        return true;
      }
      return false;
    }
    return true;
  }
  
  static String[] drop(Creature performer, Item target, boolean onGround)
  {
    String[] fail = new String[0];
    if (!target.canBeDropped(true))
    {
      if (target.isHollow()) {
        performer.getCommunicator().sendSafeServerMessage("You are not allowed to drop that. Make sure it doesn't contain non-dropable items.");
      } else {
        performer.getCommunicator().sendSafeServerMessage("You are not allowed to drop that.", (byte)3);
      }
    }
    else {
      try
      {
        if (target.isOnePerTile()) {
          if (!mayDropOnTile(performer))
          {
            performer.getCommunicator().sendNormalServerMessage("You cannot drop that item here, since there is not enough space in front of you.", (byte)3);
            
            return fail;
          }
        }
        if (target.isBeingWorkedOn())
        {
          performer.getCommunicator().sendNormalServerMessage("You cannot drop that, since you are busy working with it.", (byte)3);
          
          return fail;
        }
        if (target.isTent()) {
          if (!mayDropTentOnTile(performer))
          {
            performer.getCommunicator().sendNormalServerMessage("You are not allowed to put your tent there.", (byte)3);
            
            return fail;
          }
        }
        long ownId = target.getOwnerId();
        if (ownId == -10L) {
          return fail;
        }
        if (ownId != performer.getWurmId())
        {
          logger.log(Level.WARNING, "Hmm " + performer.getName() + " tried to drop " + target.getName() + " which wasn't his.");
          
          return fail;
        }
        if (!Methods.isActionAllowed(performer, (short)7)) {
          return fail;
        }
        boolean dropAsPile = true;
        if ((!performer.isOnSurface()) || (performer.getCurrentTile().isTransition)) {
          dropAsPile = false;
        }
        if ((dropAsPile) && (onGround) && (
          (target.getTemplateId() == 26) || (target.getTemplateId() == 298)))
        {
          int dropTileX = (int)performer.getStatus().getPositionX() + 2 >> 2;
          int dropTileY = (int)performer.getStatus().getPositionY() + 2 >> 2;
          if ((dropTileX < 0) || (dropTileX > 1 << Constants.meshSize) || (dropTileY < 0) || (dropTileY > 1 << Constants.meshSize))
          {
            String tName = target.getName();
            Items.destroyItem(target.getWurmId());
            return new String[] { "The deep water absorbs the ", tName, " and it disappears in the currents.", " into deep water and it vanishs." };
          }
          MeshIO mesh = Server.surfaceMesh;
          if (!performer.isOnSurface()) {
            mesh = Server.caveMesh;
          }
          int digTile = mesh.getTile(dropTileX, dropTileY);
          
          short h = Tiles.decodeHeight(digTile);
          Point tiles = findDropTile(dropTileX, dropTileY, mesh);
          dropTileX = tiles.getX();
          dropTileY = tiles.getY();
          for (int x = 0; x >= -1; x--) {
            for (int y = 0; y >= -1; y--) {
              if (!Methods.isActionAllowed(performer, (short)37)) {
                return fail;
              }
            }
          }
          if (!mayDropDirt(performer)) {
            return fail;
          }
          for (int x = 0; x >= -1; x--) {
            for (int y = 0; y >= -1; y--)
            {
              VolaTile tile = Zones.getTileOrNull(dropTileX + x, dropTileY + y, performer.isOnSurface());
              if (tile != null)
              {
                Structure struct = tile.getStructure();
                int bridgeHeight;
                int tileHeight;
                if (struct != null) {
                  if ((struct.isTypeBridge()) && (tile.getBridgeParts() != null))
                  {
                    BridgePart bridgePart = tile.getBridgeParts()[0];
                    if (bridgePart.getType().isSupportType())
                    {
                      performer.getCommunicator().sendNormalServerMessage("The bridge support nearby prevents dropping dirt.", (byte)3);
                      
                      return fail;
                    }
                    if (((x == -1) && (bridgePart.hasEastExit())) || ((x == 0) && 
                      (bridgePart.hasWestExit())) || ((y == -1) && 
                      (bridgePart.hasSouthExit())) || ((y == 0) && 
                      (bridgePart.hasNorthExit())))
                    {
                      performer.getCommunicator().sendNormalServerMessage("You are too close to the end of the bridge to drop dirt here.", (byte)3);
                      
                      return fail;
                    }
                    bridgeHeight = bridgePart.getRealHeight();
                    tileHeight = Tiles.decodeHeight(mesh.getTile(dropTileX, dropTileY));
                    if (bridgeHeight - tileHeight < 25)
                    {
                      performer.getCommunicator().sendNormalServerMessage("You are too close to the bottom of the bridge to drop the dirt.", (byte)3);
                      
                      return fail;
                    }
                  }
                  else
                  {
                    performer.getCommunicator().sendNormalServerMessage("The dirt would flow down into a structure. You are not allowed to drop dirt on structures.", (byte)3);
                    
                    return fail;
                  }
                }
                Fence[] fences = tile.getFencesForLevel(0);
                for (Fence f : fences) {
                  if (((y == 0) && (f.isHorizontal())) || ((x == 0) && (!f.isHorizontal())))
                  {
                    performer.getCommunicator().sendNormalServerMessage("The dirt would flow down onto a fence. You are not allowed to drop dirt on fences.", (byte)3);
                    
                    return fail;
                  }
                }
              }
              if (!performer.isPaying())
              {
                int newTile = mesh.getTile(dropTileX + x, dropTileY + y);
                if (Terraforming.isRoad(Tiles.decodeType(newTile)))
                {
                  performer.getCommunicator().sendNormalServerMessage("The dirt would flow down onto a road. Only premium players are allowed to drop dirt on roads.", (byte)3);
                  
                  return fail;
                }
              }
            }
          }
          String tNameg = target.getNameWithGenus();
          boolean change = false;
          if (target.getWeightGrams() >= 20000) {
            change = true;
          }
          String tName = target.getName();
          for (int x = 0; x >= -1; x--) {
            for (int y = 0; y >= -1; y--)
            {
              int newTile = mesh.getTile(dropTileX + x, dropTileY + y);
              byte newType = Tiles.decodeType(newTile);
              if ((newType == Tiles.Tile.TILE_HOLE.id) || (Tiles.isMineDoor(newType)))
              {
                performer.getCommunicator().sendNormalServerMessage("The dirt would flow down into a mine.", (byte)3);
                
                return fail;
              }
              if (newType == Tiles.Tile.TILE_LAVA.id)
              {
                Items.destroyItem(target.getWurmId());
                
                return new String[] { "The ", tName, " disappears into the lava.", " which then disappears into the lava." };
              }
            }
          }
          Items.destroyItem(target.getWurmId());
          if (change)
          {
            for (int x = 0; x >= -1; x--) {
              for (int y = 0; y >= -1; y--)
              {
                boolean changed = false;
                int newTile = mesh.getTile(dropTileX + x, dropTileY + y);
                byte newType = Tiles.decodeType(newTile);
                byte oldType = newType;
                h = Tiles.decodeHeight(newTile);
                short mod = 0;
                if ((x == 0) && (y == 0))
                {
                  mod = 1;
                  changed = true;
                }
                short newHeight = (short)Math.min(32767, h + mod);
                if ((newType == Tiles.Tile.TILE_ROCK.id) || (newType == Tiles.Tile.TILE_DIRT.id) || (newType == Tiles.Tile.TILE_DIRT_PACKED.id) || (newType == Tiles.Tile.TILE_STEPPE.id) || (newType == Tiles.Tile.TILE_SAND.id) || (newType == Tiles.Tile.TILE_CLIFF.id))
                {
                  if (target.getTemplateId() == 298) {
                    newType = Tiles.Tile.TILE_SAND.id;
                  } else if (target.getTemplateId() == 26) {
                    newType = Tiles.Tile.TILE_DIRT.id;
                  }
                  if (oldType != newType) {
                    TileEvent.log(dropTileX + x, dropTileY + y, 0, performer.getWurmId(), 7);
                  }
                  mesh.setTile(dropTileX + x, dropTileY + y, 
                    Tiles.encode(newHeight, newType, (byte)0));
                  Server.modifyFlagsByTileType(dropTileX + x, dropTileY + y, newType);
                  Server.isDirtHeightLower(dropTileX + x, dropTileY + y, newHeight);
                  changed = true;
                }
                else if (mod != 0)
                {
                  mesh.setTile(dropTileX + x, dropTileY + y, 
                  
                    Tiles.encode(newHeight, newType, 
                    Tiles.decodeData(newTile)));
                  Server.modifyFlagsByTileType(dropTileX + x, dropTileY + y, newType);
                  Server.isDirtHeightLower(dropTileX + x, dropTileY + y, newHeight);
                  changed = true;
                }
                if (changed)
                {
                  try
                  {
                    Zone toCheckForChange = Zones.getZone(dropTileX + x, dropTileY + y, performer
                      .isOnSurface());
                    toCheckForChange.changeTile(dropTileX + x, dropTileY + y);
                  }
                  catch (NoSuchZoneException nsz)
                  {
                    logger.log(Level.INFO, "no such zone?: " + (dropTileX + x) + ", " + (dropTileY + y), nsz);
                  }
                  Players.getInstance().sendChangedTile(dropTileX + x, dropTileY + y, performer
                    .isOnSurface(), true);
                }
                Tiles.Tile theTile = Tiles.getTile(newType);
                if (theTile.isTree())
                {
                  byte data = Tiles.decodeData(newTile);
                  Zones.reposWildHive(dropTileX + x, dropTileY + y, theTile, data);
                }
              }
            }
            return new String[] { "You drop ", tNameg, ".", "." };
          }
          performer.getCommunicator().sendNormalServerMessage("You pour the " + tName + " on the ground. It's too little matter to change anything.");
        }
        else if (target.isLiquid())
        {
          String tName = target.getName();
          Items.destroyItem(target.getWurmId());
          performer.getCommunicator().sendNormalServerMessage("You pour the " + tName + " on the ground.");
          
          Server.getInstance().broadCastAction(performer
            .getName() + " pours some " + tName + " on the ground.", performer, 5);
        }
        else
        {
          if ((!target.isCoin()) && ((performer.getPower() == 0) || (Servers.localServer.testServer)))
          {
            int[] tilecoords = Item.getDropTile(performer);
            
            VolaTile t = Zones.getTileOrNull(tilecoords[0], tilecoords[1], performer.isOnSurface());
            if (t != null)
            {
              if (t.getNumberOfItems(t.getDropFloorLevel(performer.getFloorLevel())) > 99)
              {
                performer.getCommunicator().sendNormalServerMessage("That place is too littered with items already.", (byte)3);
                
                return fail;
              }
              if (target.isDecoration()) {
                if (t.getStructure() != null) {
                  if (t.getNumberOfDecorations(t.getDropFloorLevel(performer.getFloorLevel())) > 14)
                  {
                    performer.getCommunicator().sendNormalServerMessage("That place is too littered with decorations already.", (byte)3);
                    
                    return fail;
                  }
                }
              }
              if (target.isOutsideOnly()) {
                if (t.getStructure() != null)
                {
                  performer.getCommunicator().sendNormalServerMessage("You cannot drop that inside.", (byte)3);
                  
                  return fail;
                }
              }
            }
          }
          if (performer.getCurrentTile().getNumberOfItems(performer.getFloorLevel()) > 120)
          {
            performer.getCommunicator().sendNormalServerMessage("This area is too littered with items already.", (byte)3);
            
            return fail;
          }
          target.putItemInfrontof(performer);
          performer.checkTheftWarnQuestion();
          if (target.isTent()) {
            performer.getCommunicator().sendNormalServerMessage(target.examine(performer));
          }
          return new String[] { "You drop ", target
            .getNameWithGenus(), ".", "." };
        }
      }
      catch (NoSuchItemException nsi)
      {
        logger.log(Level.WARNING, nsi.getMessage(), nsi);
      }
      catch (NoSuchCreatureException nsc)
      {
        logger.log(Level.WARNING, "Failed to locate creature " + performer.getWurmId(), nsc);
      }
      catch (NoSuchPlayerException nsp)
      {
        logger.log(Level.WARNING, "Failed to locate player " + performer.getWurmId(), nsp);
      }
      catch (NoSuchZoneException nsz)
      {
        Items.destroyItem(target.getWurmId());
      }
    }
    return fail;
  }
  
  static boolean startFire(Creature performer, Item source, Item target, float counter)
  {
    boolean toReturn = false;
    int time = 200;
    Skills skills = performer.getSkills();
    Skill primSkill = null;
    Action act = null;
    try
    {
      act = performer.getCurrentAction();
    }
    catch (NoSuchActionException nsa)
    {
      logger.log(Level.WARNING, "This action doesn't exist? " + performer.getName(), nsa);
      return true;
    }
    try
    {
      primSkill = skills.getSkill(1010);
    }
    catch (Exception ex)
    {
      primSkill = skills.learn(1010, 1.0F);
    }
    if (target.isOnFire())
    {
      performer.getCommunicator().sendNormalServerMessage("The fire is already burning.", (byte)3);
      return true;
    }
    if (target.getTemplate().isTransportable()) {
      if (target.getTopParent() != target.getWurmId())
      {
        String message = StringUtil.format("The %s must be on the ground before you can light it.", new Object[] {target
        
          .getName() });
        performer.getCommunicator().sendNormalServerMessage(message, (byte)3);
        return true;
      }
    }
    if (counter == 1.0F)
    {
      if (target.getTemplateId() == 74) {
        if (target.getData2() > 0) {
          performer.getCommunicator().sendNormalServerMessage("The dale is already burning.", (byte)3);
        }
      }
      if (target.getTemplateId() != 1243)
      {
        Item kindling = performer.getCarriedItem(36);
        if (kindling == null)
        {
          performer.getCommunicator().sendNormalServerMessage("You need at least one kindling to start a fire.", (byte)3);
          
          return true;
        }
        int templateWeight = kindling.getTemplate().getWeightGrams();
        int currentWeight = kindling.getWeightGrams();
        if (currentWeight < templateWeight)
        {
          performer.getCommunicator().sendNormalServerMessage("The kindling contains too little material to light the " + target
            .getName() + ".  Try to combine any of them with a similar object to get larger pieces.");
          
          return true;
        }
      }
      else if (target.getAuxData() == 0)
      {
        performer.getCommunicator().sendNormalServerMessage("You need to add fuel the bee smoker first.");
        return true;
      }
      time = Actions.getStandardActionTime(performer, primSkill, source, 0.0D);
      act.setTimeLeft(time);
      performer.getCommunicator().sendNormalServerMessage("You start to light the " + target.getName() + ".");
      Server.getInstance().broadCastAction(performer.getName() + " starts to light the " + target.getName() + ".", performer, 5);
      
      performer.sendActionControl("Lighting " + target.getName(), true, time);
      performer.getStatus().modifyStamina(-1000.0F);
    }
    else
    {
      time = act.getTimeLeft();
    }
    if (act.mayPlaySound()) {
      SoundPlayer.playSound("sound.fire.lighting.flintsteel", performer, 1.0F);
    }
    if (act.currentSecond() == 5) {
      performer.getStatus().modifyStamina(-1000.0F);
    }
    if (counter * 10.0F > time)
    {
      if (target.getTemplateId() != 1243)
      {
        Item kindling = performer.getCarriedItem(36);
        if (kindling == null)
        {
          performer.getCommunicator().sendNormalServerMessage("You need at least one kindling to start a fire.", (byte)3);
          
          return true;
        }
        int templateWeight = kindling.getTemplate().getWeightGrams();
        int currentWeight = kindling.getWeightGrams();
        if (currentWeight < templateWeight)
        {
          performer.getCommunicator().sendNormalServerMessage("The kindling contains too little material to light the " + target
            .getName() + ".  Try to combine it with other kindling to get larger pieces.");
          
          return true;
        }
        kindling.setWeight(currentWeight - templateWeight, true);
      }
      else if (target.getAuxData() == 0)
      {
        performer.getCommunicator().sendNormalServerMessage("You need to add fuel the bee smoker first.");
        return true;
      }
      primSkill.skillCheck(Server.getWeather().getRain() * 10.0F, source.getCurrentQualityLevel(), false, counter);
      toReturn = setFire(performer, target);
    }
    return toReturn;
  }
  
  static boolean setFire(Creature performer, Item target)
  {
    if (target.getTemplate().isTransportable()) {
      if (target.getTopParent() != target.getWurmId())
      {
        String message = StringUtil.format("The %s must be on the ground before you can light it.", new Object[] {target
        
          .getName() });
        performer.getCommunicator().sendNormalServerMessage(message, (byte)3);
        return true;
      }
    }
    if ((target.getTemplateId() == 1243) && (target.getAuxData() == 0))
    {
      performer.getCommunicator().sendNormalServerMessage("You need to add fuel the bee smoker first.");
      return true;
    }
    performer.getCommunicator().sendNormalServerMessage("You light the " + target.getName() + ".");
    if (target.getTemplateId() == 178) {
      target.setTemperature((short)6000);
    } else {
      target.setTemperature((short)10000);
    }
    if (target.getTemplateId() == 889) {
      target.setAuxData((byte)Math.min(255, target.getAuxData() + 2));
    }
    if (target.getTemplateId() != 1243)
    {
      Effect effect = EffectFactory.getInstance().createFire(target.getWurmId(), target.getPosX(), target.getPosY(), target
        .getPosZ(), performer.isOnSurface());
      target.addEffect(effect);
    }
    return true;
  }
  
  static boolean gatherRiftResource(Creature performer, Item sourceTool, Item targetItem, float counter, Action act)
  {
    if (targetItem.deleted)
    {
      performer.getCommunicator().sendNormalServerMessage("The " + targetItem.getName() + " has nothing left to gather.", (byte)3);
      
      return true;
    }
    boolean toReturn = true;
    if (!performer.getInventory().mayCreatureInsertItem())
    {
      performer.getCommunicator().sendNormalServerMessage("You have no space left in your inventory to put what you might gather.");
      
      return true;
    }
    if (performer.isWithinDistanceTo(targetItem.getPosX(), targetItem.getPosY(), targetItem.getPosZ(), 4.0F))
    {
      toReturn = false;
      if (counter == 1.0F)
      {
        Skills skills = performer.getSkills();
        Skill relevantSkill = null;
        try
        {
          switch (act.getActionEntry().getNumber())
          {
          case 145: 
            relevantSkill = skills.getSkill(1008);
            break;
          case 96: 
            relevantSkill = skills.getSkill(1007);
            break;
          case 156: 
            relevantSkill = skills.getSkill(10032);
          }
        }
        catch (NoSuchSkillException nss)
        {
          switch (act.getActionEntry().getNumber())
          {
          case 145: 
            relevantSkill = skills.learn(1008, 1.0F);
          }
        }
        relevantSkill = skills.learn(1007, 1.0F);
        break label275;
        relevantSkill = skills.learn(10032, 1.0F);
        label275:
        act.setTimeLeft(Actions.getSlowActionTime(performer, relevantSkill, sourceTool, 0.0D) * 1);
        performer.getCommunicator().sendNormalServerMessage("You start to gather resources from " + targetItem.getNameWithGenus() + ".");
        Server.getInstance().broadCastAction(performer.getName() + " starts to gather resources from " + targetItem.getNameWithGenus() + ".", performer, 5);
        performer.sendActionControl("Gathering from " + targetItem.getName(), true, act.getTimeLeft());
        performer.getStatus().modifyStamina(-1000.0F);
      }
      else
      {
        if (act.mayPlaySound())
        {
          String soundName = "sound.work.mining1";
          int soundNum = Server.rand.nextInt(3);
          switch (act.getActionEntry().getNumber())
          {
          case 145: 
            if (soundNum == 1) {
              soundName = "sound.work.mining2";
            } else if (soundNum == 2) {
              soundName = "sound.work.mining3";
            }
            break;
          case 96: 
            if (soundNum == 0) {
              soundName = "sound.work.woodcutting1";
            } else if (soundNum == 1) {
              soundName = "sound.work.woodcutting2";
            } else if (soundNum == 2) {
              soundName = "sound.work.woodcutting3";
            }
            break;
          case 156: 
            if (soundNum == 0) {
              soundName = "sound.work.prospecting1";
            } else if (soundNum == 1) {
              soundName = "sound.work.prospecting2";
            } else if (soundNum == 2) {
              soundName = "sound.work.prospecting3";
            }
            break;
          }
          SoundPlayer.playSound(soundName, performer, 1.0F);
        }
        if (act.currentSecond() % 5 == 0)
        {
          sourceTool.setDamage(sourceTool.getDamage() + 0.0015F * sourceTool.getDamageModifier());
          performer.getStatus().modifyStamina(-3000.0F);
        }
      }
      if (counter * 10.0F > act.getTimeLeft())
      {
        if (act.getRarity() != 0) {
          performer.playPersonalSound("sound.fx.drumroll");
        }
        Skills skills = performer.getSkills();
        Skill relevantSkill = null;
        Skill toolSkill = null;
        try
        {
          switch (act.getActionEntry().getNumber())
          {
          case 145: 
            relevantSkill = skills.getSkill(1008);
            break;
          case 96: 
            relevantSkill = skills.getSkill(1007);
            break;
          case 156: 
            relevantSkill = skills.getSkill(10032);
          }
        }
        catch (NoSuchSkillException nss)
        {
          switch (act.getActionEntry().getNumber())
          {
          case 145: 
            relevantSkill = skills.learn(1008, 1.0F);
          }
        }
        relevantSkill = skills.learn(1007, 1.0F);
        break label839;
        relevantSkill = skills.learn(10032, 1.0F);
        try
        {
          label839:
          toolSkill = skills.getSkill(sourceTool.getPrimarySkill());
        }
        catch (Exception ex)
        {
          try
          {
            toolSkill = skills.learn(sourceTool.getPrimarySkill(), 1.0F);
          }
          catch (NoSuchSkillException nse)
          {
            logger.log(Level.WARNING, performer.getName() + " trying to gather resources with an item with no primary skill: " + sourceTool
              .getName());
          }
        }
        toReturn = true;
        float difficulty = 60.0F;
        double bonus = toolSkill.skillCheck(60.0D, sourceTool, 0.0D, false, counter) / 5.0D;
        double power = relevantSkill.skillCheck(60.0D, sourceTool, bonus, false, counter);
        float maxQL = targetItem.getCurrentQualityLevel();
        if ((targetItem.getTemplate().isRiftStoneDeco()) || 
          (targetItem.getTemplate().isRiftCrystalDeco())) {
          try
          {
            if (power > 0.0D)
            {
              float resPower = GeneralUtilities.calcRareQuality(power, act.getRarity(), sourceTool.getRarity());
              Item riftResource = ItemFactory.createItem(targetItem.getTemplate().isRiftCrystalDeco() ? 1103 : 1102, 
                Math.min(maxQL, resPower), act.getRarity(), performer.getName());
              performer.getInventory().insertItem(riftResource);
              performer.getCommunicator().sendNormalServerMessage("You get " + riftResource.getNameWithGenus() + ".", (byte)2);
              Server.getInstance().broadCastAction(performer.getName() + " finds " + riftResource.getNameWithGenus() + ".", performer, 5);
            }
            else
            {
              performer.getCommunicator().sendNormalServerMessage("You find it too difficult to get useful stone from the " + targetItem
                .getName() + ".", (byte)2);
            }
            float stonePower = GeneralUtilities.calcOreRareQuality(power, act.getRarity(), sourceTool.getRarity());
            Item stoneShards = ItemFactory.createItem(146, Math.min(maxQL, stonePower), performer.getPosX(), performer
              .getPosY(), Server.rand.nextFloat() * 360.0F, performer.isOnSurface(), act.getRarity(), -10L, null);
            if (targetItem.getWeightGrams() < stoneShards.getWeightGrams()) {
              stoneShards.setWeight(targetItem.getWeightGrams(), true);
            }
            targetItem.setWeight(targetItem.getWeightGrams() - stoneShards.getWeightGrams(), true);
            performer.getCommunicator().sendNormalServerMessage("You finish mining " + stoneShards.getName() + " from the " + targetItem
              .getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " mines " + stoneShards.getName() + " from the " + targetItem
              .getName() + ".", performer, 5);
          }
          catch (Exception ex)
          {
            logger.log(Level.WARNING, ex.getMessage());
            performer.getCommunicator().sendNormalServerMessage("You stumble at the last second and nothing happens.");
          }
        } else if (targetItem.getTemplate().isRiftPlantDeco()) {
          try
          {
            if (power > 0.0D)
            {
              float resPower = GeneralUtilities.calcRareQuality(power, act.getRarity(), sourceTool.getRarity());
              Item riftWood = ItemFactory.createItem(1104, Math.min(maxQL, resPower), act.getRarity(), performer.getName());
              if (targetItem.getTemplateId() == 1041) {
                riftWood.setMaterial((byte)38);
              } else if (targetItem.getTemplateId() == 1042) {
                riftWood.setMaterial((byte)40);
              } else if (targetItem.getTemplateId() == 1043) {
                riftWood.setMaterial((byte)41);
              } else if (targetItem.getTemplateId() == 1044) {
                riftWood.setMaterial((byte)51);
              }
              performer.getInventory().insertItem(riftWood);
              performer.getCommunicator().sendNormalServerMessage("You get " + riftWood.getNameWithGenus() + ".", (byte)2);
              Server.getInstance().broadCastAction(performer.getName() + " finds " + riftWood.getNameWithGenus() + ".", performer, 5);
            }
            else
            {
              performer.getCommunicator().sendNormalServerMessage("You find it too difficult to get useful wood from the " + targetItem
                .getName() + ".", (byte)2);
            }
            byte material = 0;
            if (targetItem.getTemplateId() == 1041) {
              material = 38;
            } else if (targetItem.getTemplateId() == 1042) {
              material = 40;
            } else if (targetItem.getTemplateId() == 1043) {
              material = 41;
            } else if (targetItem.getTemplateId() == 1044) {
              material = 51;
            }
            float woodPower = GeneralUtilities.calcRareQuality(power, act.getRarity(), sourceTool.getRarity());
            Item woodLog = ItemFactory.createItem(9, Math.min(maxQL, woodPower), performer.getPosX(), performer
              .getPosY(), Server.rand.nextFloat() * 360.0F, performer.isOnSurface(), material, act.getRarity(), -10L, null);
            woodLog.setLastOwnerId(performer.getWurmId());
            if (targetItem.getTemplateId() == 1044)
            {
              if (targetItem.getWeightGrams() < 24000)
              {
                float ratio = targetItem.getWeightGrams() / 24000.0F;
                woodLog.setWeight((int)(4000.0F * ratio), true);
                targetItem.setWeight(0, true);
              }
              else
              {
                woodLog.setWeight(4000, true);
                targetItem.setWeight(targetItem.getWeightGrams() - 24000, true);
              }
            }
            else
            {
              if (targetItem.getWeightGrams() < woodLog.getWeightGrams()) {
                woodLog.setWeight(targetItem.getWeightGrams(), true);
              }
              targetItem.setWeight(targetItem.getWeightGrams() - woodLog.getWeightGrams(), true);
            }
            performer.getCommunicator().sendNormalServerMessage("You finish chopping " + woodLog.getNameWithGenus() + " from the " + targetItem
              .getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " chops " + woodLog.getNameWithGenus() + " from the " + targetItem
              .getName() + ".", performer, 5);
          }
          catch (Exception ex)
          {
            logger.log(Level.WARNING, ex.getMessage());
            performer.getCommunicator().sendNormalServerMessage("You stumble at the last second and nothing happens.");
          }
        }
      }
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("You can't reach the " + targetItem.getName() + ".", (byte)3);
    }
    return toReturn;
  }
  
  static boolean mine(Creature performer, Item pickAxe, Item boulder, float counter, Action act)
  {
    if (boulder.deleted)
    {
      performer.getCommunicator().sendNormalServerMessage("The " + boulder.getName() + " contains no more ore.", (byte)3);
      
      return true;
    }
    boolean toReturn = true;
    if (boulder.getZoneId() != -10L)
    {
      if (performer.isWithinDistanceTo(boulder.getPosX(), boulder.getPosY(), boulder.getPosZ(), 4.0F))
      {
        toReturn = false;
        int time = 150;
        if (counter == 1.0F)
        {
          Skills skills = performer.getSkills();
          Skill mining = null;
          try
          {
            mining = skills.getSkill(1008);
          }
          catch (NoSuchSkillException nss)
          {
            mining = skills.learn(1008, 1.0F);
          }
          time = Actions.getStandardActionTime(performer, mining, pickAxe, 0.0D);
          act.setTimeLeft(time);
          performer.getCommunicator().sendNormalServerMessage("You start to mine the " + boulder.getName() + ".");
          
          Server.getInstance().broadCastAction(performer
            .getName() + " starts to mine " + boulder.getNameWithGenus() + ".", performer, 5);
          performer.sendActionControl("Mining " + boulder.getName(), true, time);
          performer.getStatus().modifyStamina(-400.0F);
        }
        else
        {
          time = act.getTimeLeft();
          if (act.mayPlaySound())
          {
            String sstring = "sound.work.mining1";
            int x = Server.rand.nextInt(3);
            if (x == 0) {
              sstring = "sound.work.mining2";
            } else if (x == 1) {
              sstring = "sound.work.mining3";
            }
            SoundPlayer.playSound(sstring, performer, 1.0F);
          }
          if (act.currentSecond() % 5 == 0)
          {
            pickAxe.setDamage(pickAxe.getDamage() + 0.0015F * pickAxe.getDamageModifier());
            performer.getStatus().modifyStamina(-7000.0F);
          }
        }
        if (counter * 10.0F > time)
        {
          Skills skills = performer.getSkills();
          Skill mining = null;
          try
          {
            mining = skills.getSkill(1008);
          }
          catch (NoSuchSkillException nss)
          {
            mining = skills.learn(1008, 1.0F);
          }
          Skill tool = null;
          try
          {
            tool = skills.getSkill(pickAxe.getPrimarySkill());
          }
          catch (Exception ex)
          {
            try
            {
              tool = skills.learn(pickAxe.getPrimarySkill(), 1.0F);
            }
            catch (NoSuchSkillException nse)
            {
              logger.log(Level.WARNING, performer.getName() + " trying to mine with an item with no primary skill: " + pickAxe
                .getName());
            }
          }
          toReturn = true;
          int itemTemplateCreated = 146;
          float diff = 1.0F;
          double bonus = tool.skillCheck(1.0D, pickAxe, 0.0D, false, counter) / 5.0D;
          double power = Math.max(1.0D, mining.skillCheck(1.0D, pickAxe, bonus, false, counter));
          if (mining.getKnowledge(0.0D) < power) {
            power = mining.getKnowledge(0.0D);
          }
          if (power > 0.0D)
          {
            int m = 100;
            power = Math.min(power, 100.0D);
            if (pickAxe.isCrude()) {
              power = 1.0D;
            }
            if (boulder.getTemplateId() == 692) {
              itemTemplateCreated = 693;
            } else if (boulder.getTemplateId() == 696) {
              itemTemplateCreated = 697;
            }
          }
          else
          {
            power = Math.max(1.0F, Server.rand.nextInt(10));
          }
          try
          {
            float modifier = 1.0F;
            if (pickAxe.getSpellEffects() != null) {
              modifier = pickAxe.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED);
            }
            float orePower = GeneralUtilities.calcOreRareQuality(power * modifier, act.getRarity(), pickAxe.getRarity());
            
            Item newItem = ItemFactory.createItem(itemTemplateCreated, orePower, performer
              .getPosX(), performer
              .getPosY(), Server.rand.nextFloat() * 360.0F, performer.isOnSurface(), act.getRarity(), -10L, null);
            
            newItem.setLastOwnerId(performer.getWurmId());
            newItem.setWeight((int)(newItem.getWeightGrams() * 0.25F), true);
            Items.destroyItem(boulder.getWurmId());
            performer.getCommunicator().sendNormalServerMessage("You mine some " + newItem.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " mines some " + newItem.getName() + ".", performer, 5);
          }
          catch (Exception ex)
          {
            logger.log(Level.WARNING, ex.getMessage());
            performer.getCommunicator().sendNormalServerMessage("You stumble at the last second and nothing happens.");
          }
        }
      }
      else
      {
        performer.getCommunicator().sendNormalServerMessage("You can't reach the " + boulder.getName() + " now.", (byte)3);
      }
    }
    else {
      performer.getCommunicator().sendNormalServerMessage("You can't reach the " + boulder.getName() + " now.", (byte)3);
    }
    return toReturn;
  }
  
  static boolean plantSign(Creature performer, Item sign, float counter, boolean inCorner, int cornerX, int cornerY, boolean onSurface, long bridgeId, boolean atFeet, long data)
  {
    if (cannotPlant(performer, sign)) {
      return true;
    }
    String planted = sign.getParentId() == -10L ? "secured" : "planted";
    String plant = sign.getParentId() == -10L ? "secure" : "plant";
    if ((counter == 1.0F) && (sign.isTrellis())) {
      if (!performer.isOnSurface()) {
        performer.getCommunicator().sendNormalServerMessage("The " + sign.getName() + " can only be harvested if " + planted + " on the surface.");
      }
    }
    if ((sign.isEnchantedTurret()) || (sign.isUnenchantedTurret())) {
      if (performer.getLayer() < 0)
      {
        performer.getCommunicator().sendNormalServerMessage("The " + sign
          .getName() + " can not be " + planted + " beneath ground.", (byte)3);
        return true;
      }
    }
    if ((inCorner) && (!performer.isWithinDistanceTo(cornerX << 2, cornerY << 2, 0.0F, 4.0F)))
    {
      performer.getCommunicator().sendNormalServerMessage("You are too far away to do that.", (byte)3);
      return true;
    }
    if ((counter == 1.0F) && (sign.isAbility())) {
      if (!surveyAbilitySigns(sign, performer))
      {
        performer.getCommunicator().sendNormalServerMessage("You cannot activate this item here.", (byte)3);
        
        return true;
      }
    }
    boolean toReturn = false;
    int time = Actions.getPlantActionTime(performer, sign);
    Action act = null;
    try
    {
      act = performer.getCurrentAction();
    }
    catch (NoSuchActionException nsa)
    {
      logger.log(Level.WARNING, "This action doesn't exist? " + performer.getName(), nsa);
      return true;
    }
    if (counter == 1.0F)
    {
      if ((performer instanceof Player))
      {
        Player p = (Player)performer;
        boolean ownVillage = false;
        if (performer.getCitizenVillage() != null) {
          if (performer.getCurrentVillage() == performer.getCitizenVillage()) {
            ownVillage = true;
          }
        }
        if ((sign.isPlantOneAWeek()) && (p.hasPlantedSign()) && (!ownVillage))
        {
          performer.getCommunicator().sendNormalServerMessage("You may only plant one " + sign
            .getName() + " outside your settlement per week.", (byte)3);
          
          return true;
        }
        try
        {
          Skills skills = p.getSkills();
          Skill dig = skills.getSkill(1009);
          if (dig.getRealKnowledge() < 10.0D)
          {
            performer.getCommunicator().sendNormalServerMessage("You need to have 10 in the skill digging to secure " + sign
              .getTemplate().getPlural() + " to the ground.", (byte)3);
            
            return true;
          }
        }
        catch (NoSuchSkillException nss)
        {
          performer.getCommunicator().sendNormalServerMessage("You need 10 digging to plant " + sign
            .getTemplate().getPlural() + ".", (byte)3);
          return true;
        }
      }
      if (!Methods.isActionAllowed(performer, (short)176)) {
        return true;
      }
      if ((sign.isSign()) && (sign.getDescription().length() == 0))
      {
        performer.getCommunicator().sendNormalServerMessage("Write something on the " + sign.getName() + " first.", (byte)3);
        
        return true;
      }
      int tile = performer.getCurrentTileNum();
      if (sign.getTemplateId() == 1342)
      {
        if (performer.getStatus().getBridgeId() != -10L)
        {
          performer.getCommunicator().sendNormalServerMessage(sign
            .getName() + " must be planted in water, not on a bridge.", (byte)3);
          return true;
        }
        int depth = FishEnums.getWaterDepth(performer.getPosX(), performer.getPosY(), performer.isOnSurface());
        if (depth < 2)
        {
          performer.getCommunicator().sendNormalServerMessage("The water is not deep enough for the " + sign.getName() + ".");
          return true;
        }
        if (depth > 30)
        {
          performer.getCommunicator().sendNormalServerMessage("The water is too deep for the " + sign.getName() + ".");
          return true;
        }
      }
      else if ((sign.getTemplateId() != 805) && (sign.getTemplateId() != 1396) && (performer.getStatus().getBridgeId() == -10L))
      {
        if (((sign.isRoadMarker()) && (Tiles.decodeHeight(tile) < -7)) || (
          (!sign.isRoadMarker()) && (Tiles.decodeHeight(tile) < 0)))
        {
          performer.getCommunicator().sendNormalServerMessage("The water is too deep to plant the " + sign
            .getName() + ".", (byte)3);
          return true;
        }
      }
      else if ((Tiles.decodeHeight(tile) < 0) && (sign.getTemplateId() != 805) && 
        (sign.getTemplateId() != 1396) && (!sign.isRoadMarker()) && (performer.getStatus().getBridgeId() == -10L))
      {
        performer.getCommunicator().sendNormalServerMessage("The water is too deep to plant the " + sign
          .getName() + ".", (byte)3);
        return true;
      }
      act.setTimeLeft(time);
      performer.getCommunicator().sendNormalServerMessage("You start to " + plant + " the " + sign.getName() + ".");
      if (sign.isAbility()) {
        performer.getCommunicator().sendAlertServerMessage("WARNING: This can NOT be aborted once the " + sign
          .getName() + " finishes planting!", (byte)2);
      }
      if (sign.isSign()) {
        Server.getInstance().broadCastAction(performer
          .getName() + " starts to " + plant + " a sign which says: " + sign.getDescription() + ".", performer, 5);
      } else {
        Server.getInstance().broadCastAction(performer.getName() + " starts to " + plant + " " + sign.getNameWithGenus() + ".", performer, 5);
      }
      performer.sendActionControl("Planting " + sign.getName(), true, time);
      performer.getStatus().modifyStamina(-400.0F);
    }
    else
    {
      time = act.getTimeLeft();
      if (act.currentSecond() % 5 == 0) {
        performer.getStatus().modifyStamina(-1000.0F);
      }
    }
    if (counter * 10.0F > time) {
      toReturn = plantSignFinish(performer, sign, inCorner, cornerX, cornerY, onSurface, bridgeId, atFeet, data);
    }
    return toReturn;
  }
  
  static boolean plantSignFinish(Creature performer, Item sign, boolean inCorner, int cornerX, int cornerY, boolean onSurface, long bridgeId, boolean atFeet, long data)
  {
    if (cannotPlant(performer, sign)) {
      return true;
    }
    if (sign.getTemplateId() == 1342) {
      atFeet = true;
    }
    String plant = sign.getParentId() != -10L ? "plant" : "secure";
    try
    {
      if (data != -1L) {
        sign.setData(data);
      }
      sign.setIsPlanted(true);
      if (sign.getParentId() != -10L)
      {
        if ((inCorner) || (atFeet)) {
          sign.putItemInCorner(performer, cornerX, cornerY, onSurface, bridgeId, atFeet);
        } else {
          sign.putItemInfrontof(performer);
        }
        if ((sign.isEnchantedTurret()) || (sign.isUnenchantedTurret()))
        {
          int dist = 8;
          for (int x = Zones.safeTileX(sign.getTileX() - 8); x < Zones.safeTileX(sign.getTileX() + 8); x++) {
            for (int y = Zones.safeTileY(sign.getTileY() - 8); y < Zones.safeTileY(sign.getTileY() + 8); y++)
            {
              VolaTile t = Zones.getTileOrNull(x, y, sign.isOnSurface());
              if (t != null) {
                if (t.hasOnePerTileItem(0))
                {
                  Item i = t.getOnePerTileItem(0);
                  if ((sign != i) && ((i.isEnchantedTurret()) || (i.isUnenchantedTurret())) && (i.isPlanted()))
                  {
                    performer.getCommunicator().sendNormalServerMessage("The " + i
                      .getName() + " is too close to " + sign.getName() + ".", (byte)3);
                    
                    sign.setIsPlanted(false);
                    return true;
                  }
                }
              }
            }
          }
        }
      }
      performer.getCommunicator().sendNormalServerMessage("You " + plant + " the " + sign.getName() + ".");
      Server.getInstance().broadCastAction(performer.getName() + " " + plant + "s the " + sign.getName() + ".", performer, 5);
      
      boolean ownVillage = false;
      if (performer.getCitizenVillage() != null) {
        if (performer.getCurrentVillage() == performer.getCitizenVillage()) {
          ownVillage = true;
        }
      }
      if ((sign.isPlantOneAWeek()) && (!ownVillage) && ((performer instanceof Player))) {
        ((Player)performer).plantSign();
      }
      if (sign.isAbility())
      {
        int signTileX = sign.getTileX();
        int signTileY = sign.getTileY();
        if (surveyAbilitySigns(sign, performer))
        {
          sign.setIsNoTake(true);
          sign.hatching = true;
        }
        else
        {
          performer.getCommunicator().sendNormalServerMessage("Nothing seems to happen. Maybe you're too close to a settlement or in enemy territory.");
        }
      }
    }
    catch (NoSuchZoneException nsz)
    {
      performer.getCommunicator().sendNormalServerMessage("You fail to " + plant + " the " + sign
        .getName() + ". Something is weird.");
      logger.log(Level.WARNING, performer.getName() + ": " + nsz.getMessage(), nsz);
    }
    catch (NoSuchCreatureException nsc)
    {
      performer.getCommunicator().sendNormalServerMessage("You fail to " + plant + " the " + sign
        .getName() + ". Something is weird.");
      logger.log(Level.WARNING, performer.getName() + ": " + nsc.getMessage(), nsc);
    }
    catch (NoSuchPlayerException nsp)
    {
      performer.getCommunicator().sendNormalServerMessage("You fail to " + plant + " the " + sign
        .getName() + ". Something is weird.");
      logger.log(Level.WARNING, performer.getName() + ": " + nsp.getMessage(), nsp);
    }
    catch (NoSuchItemException nst)
    {
      performer.getCommunicator().sendNormalServerMessage("You fail to " + plant + " the " + sign
        .getName() + ". Something is weird.");
      logger.log(Level.WARNING, performer.getName() + ": " + nst.getMessage(), nst);
    }
    return true;
  }
  
  static boolean surveyAbilitySigns(Item sign, Creature performer)
  {
    if ((performer != null) && (sign != null) && (sign.getTemplate() != null) && (sign.isAbility()))
    {
      boolean ok = false;
      int signTileX = sign.getTileX();
      int signTileY = sign.getTileY();
      if (sign.getTemplateId() == 1009)
      {
        if ((signTileX > 200) && (signTileX < Zones.worldTileSizeX - 200)) {
          if ((signTileY > 200) && (signTileY < Zones.worldTileSizeY - 200))
          {
            ok = true;
            for (int x = signTileX - 75; x < signTileX + 75; x++) {
              for (int y = signTileY - 75; y < signTileY + 75; y++)
              {
                byte kd = Zones.getKingdom(x, y);
                if ((kd != 0) && (kd != performer.getKingdomId()))
                {
                  ok = false;
                  break;
                }
                if (Villages.getVillageWithPerimeterAt(x, y, true) != null)
                {
                  ok = false;
                  break;
                }
                VolaTile t = Zones.getTileOrNull(x, y, sign.isOnSurface());
                if ((t != null) && (t.getStructure() != null)) {
                  ok = false;
                }
              }
            }
          }
        }
      }
      else if (sign.getTemplateId() == 805) {
        if ((signTileX > 200) && (signTileX < Zones.worldTileSizeX - 200)) {
          if ((signTileY > 200) && (signTileY < Zones.worldTileSizeY - 200))
          {
            ok = true;
            for (int x = signTileX - 30; x < signTileX + 30; x++)
            {
              for (int y = signTileY - 30; y < signTileY + 30; y++)
              {
                byte kd = Zones.getKingdom(x, y);
                if ((kd != 0) && (kd != performer.getKingdomId()))
                {
                  ok = false;
                  break;
                }
                if (Villages.getVillageWithPerimeterAt(x, y, true) != null)
                {
                  ok = false;
                  break;
                }
                VolaTile t = Zones.getTileOrNull(x, y, sign.isOnSurface());
                if ((t != null) && (t.getStructure() != null)) {
                  ok = false;
                }
                int tilenum = Zones.getTileIntForTile(x, y, 0);
                if (Tiles.decodeHeight(tilenum) >= 0)
                {
                  performer.getCommunicator().sendNormalServerMessage("You are too close to land.", (byte)3);
                  
                  ok = false;
                  break;
                }
              }
              if (!ok) {
                break;
              }
            }
          }
        }
      }
      return ok;
    }
    return false;
  }
  
  static boolean cannotPlant(Creature performer, Item sign)
  {
    String planted = sign.getParentId() == -10L ? "secured" : "planted";
    String plant = sign.getParentId() == -10L ? "secure" : "plant";
    if (sign.getCurrentQualityLevel() < 10.0F)
    {
      performer.getCommunicator().sendNormalServerMessage("The " + sign.getName() + " is of too poor quality to be " + planted + ".");
      return true;
    }
    if (sign.getDamage() > 70.0F)
    {
      performer.getCommunicator().sendNormalServerMessage("The " + sign.getName() + " is too heavily damaged to be " + planted + ".");
      return true;
    }
    if (sign.isPlanted())
    {
      performer.getCommunicator().sendNormalServerMessage("The " + sign.getName() + " is already " + planted + ".", (byte)3);
      
      return true;
    }
    if ((sign.isSurfaceOnly()) && (!performer.isOnSurface()))
    {
      performer.getCommunicator().sendNormalServerMessage("The " + sign.getName() + " can only be " + planted + " on the surface.");
      return true;
    }
    if (sign.isOutsideOnly())
    {
      VolaTile vt = Zones.getTileOrNull(performer.getTileX(), performer.getTileY(), performer.isOnSurface());
      if ((vt != null) && (vt.getStructure() != null))
      {
        performer.getCommunicator().sendNormalServerMessage("The " + sign.getName() + " can only be " + planted + " outside.");
        return true;
      }
    }
    if (sign.isFourPerTile())
    {
      VolaTile vt = Zones.getTileOrNull(performer.getTileX(), performer.getTileY(), performer.isOnSurface());
      if ((vt != null) && (vt.getFourPerTileCount(0) >= 4))
      {
        performer.getCommunicator().sendNormalServerMessage("You cannot " + plant + " " + sign
          .getNameWithGenus() + " as there are four here already.", (byte)3);
        
        return true;
      }
    }
    if (!sign.canBeDropped(true))
    {
      if (sign.isHollow()) {
        performer.getCommunicator().sendSafeServerMessage("You are not allowed to " + plant + " that. Make sure it doesn't contain non-dropable items.");
      } else {
        performer.getCommunicator().sendSafeServerMessage("You are not allowed to " + plant + " that.", (byte)3);
      }
      return true;
    }
    Item topParent = sign.getTopParentOrNull();
    if ((topParent != sign) && (!topParent.isInventory()))
    {
      performer.getCommunicator().sendSafeServerMessage("You can only secure an item that is on the ground.", (byte)3);
      
      return true;
    }
    if (sign.getParentId() == -10L)
    {
      if (sign.canHavePermissions()) {
        if (!sign.canManage(performer))
        {
          performer.getCommunicator().sendSafeServerMessage("You do not have permission to " + plant + " that.", (byte)3);
          
          return true;
        }
      }
      if ((sign.getLastOwnerId() != performer.getWurmId()) && (performer.getPower() <= 1))
      {
        performer.getCommunicator().sendSafeServerMessage("You do not have permission to " + plant + " that.", (byte)3);
        
        return true;
      }
    }
    else if (sign.isOnePerTile())
    {
      if (!mayDropOnTile(performer))
      {
        performer.getCommunicator().sendNormalServerMessage("You cannot " + plant + " that item here, since there is not enough space in front of you.", (byte)3);
        
        return true;
      }
    }
    if (sign.getTemplateId() == 1342)
    {
      int depth = FishEnums.getWaterDepth(sign.getPosX(), sign.getPosY(), sign.isOnSurface());
      if (depth < 2)
      {
        performer.getCommunicator().sendNormalServerMessage("The water is not deep enough for the " + sign.getName() + ".");
        return true;
      }
      if (depth > 30)
      {
        performer.getCommunicator().sendNormalServerMessage("The water is too deep for the " + sign.getName() + ".");
        return true;
      }
    }
    if (sign.getTemplateId() == 1396)
    {
      int depth = FishEnums.getWaterDepth(sign.getPosX(), sign.getPosY(), sign.isOnSurface());
      if (depth < 0)
      {
        performer.getCommunicator().sendNormalServerMessage("You may not plant that out of water.");
        return true;
      }
    }
    return false;
  }
  
  static boolean moveItem(Creature performer, Item item, float counter, short action, Action act)
  {
    boolean toReturn = false;
    int time = 150;
    if ((item.getParentId() != -10L) && ((item.getParentOrNull() != item.getTopParentOrNull()) || 
      (!item.getParentOrNull().getTemplate().hasViewableSubItems()) || (
      (item.getParentOrNull().getTemplate().isContainerWithSubItems()) && (!item.isPlacedOnParent()))))
    {
      performer.getCommunicator().sendNormalServerMessage("You can not turn that right now.", (byte)3);
      return true;
    }
    if (performer.isGuest())
    {
      performer.getCommunicator().sendNormalServerMessage("Sorry, but we cannot allow our guests to move items.", (byte)3);
      
      return true;
    }
    if (((action == 177) || (action == 178)) && (!item.isTurnable(performer)) && (!item.isGuardTower()))
    {
      performer.getCommunicator().sendNormalServerMessage("Sorry, but you are not allowed to turn that.", (byte)3);
      
      return true;
    }
    if (((action == 181) || (action == 99) || (action == 697) || (action == 696)) && 
      (checkIfStealing(item, performer, act)))
    {
      if (!performer.maySteal())
      {
        performer.getCommunicator().sendNormalServerMessage("You need more body control to steal things.", (byte)3);
        
        return true;
      }
      performer.getCommunicator().sendNormalServerMessage("You have to steal the " + item.getName() + " instead.", (byte)3);
      
      return true;
    }
    if ((item.isCorpse()) && (item.getWasBrandedTo() != -10L) && (!item.mayCommand(performer)))
    {
      performer.getCommunicator().sendNormalServerMessage("You may not move the corpse as you do not have permissions.", (byte)3);
      
      return true;
    }
    if ((item.isCorpse()) && (Servers.localServer.isChallengeOrEpicServer()))
    {
      VolaTile ttile = Zones.getTileOrNull(item.getTileX(), item.getTileY(), item.isOnSurface());
      int distance = (ttile == null) || (ttile.getVillage() == null) ? 10 : 5;
      if (isEnemiesNearby(performer, distance, true))
      {
        performer.getCommunicator().sendNormalServerMessage("You may not move the corpse when there are enemies nearby.", (byte)3);
        
        return true;
      }
    }
    if (item.getTemplateId() == 931) {
      if (Servers.localServer.PVPSERVER)
      {
        if ((action == 181) || (action == 697))
        {
          performer.getCommunicator().sendNormalServerMessage("The " + item
            .getName() + " can only be pushed.", (byte)3);
          return true;
        }
        if ((performer.getTileX() != item.getTileX()) || (performer.getTileY() != item.getTileY()))
        {
          performer.getCommunicator().sendNormalServerMessage("You need to stand right behind the " + item
            .getName() + " in order to move it.", (byte)3);
          
          return true;
        }
      }
    }
    if (Items.isItemDragged(item))
    {
      performer.getCommunicator().sendNormalServerMessage("The " + item
        .getName() + " is being dragged and may not be moved that way at the moment.", (byte)3);
      
      return true;
    }
    Vehicle vehicle = Vehicles.getVehicle(item);
    boolean performerIsAllowedToDriveVehicle = false;
    if (vehicle != null)
    {
      for (Seat lSeat : vehicle.seats) {
        if (lSeat.isOccupied())
        {
          performer.getCommunicator().sendNormalServerMessage("The " + item
            .getName() + " is occupied and may not be moved that way at the moment.", (byte)3);
          
          return true;
        }
      }
      if ((vehicle.draggers != null) && (vehicle.draggers.size() > 0))
      {
        performer.getCommunicator().sendNormalServerMessage("The " + item
          .getName() + " may not be moved that way at the moment.", (byte)3);
        return true;
      }
      if ((VehicleBehaviour.mayDriveVehicle(performer, item, act)) && 
        (VehicleBehaviour.canBeDriverOfVehicle(performer, vehicle))) {
        performerIsAllowedToDriveVehicle = true;
      }
    }
    if (item.isBoat()) {
      if (item.getData() != -1L)
      {
        performer.getCommunicator().sendNormalServerMessage("The " + item.getName() + " won't budge. It's moored.", (byte)3);
        
        return true;
      }
    }
    if ((performerIsAllowedToDriveVehicle != true) || (!item.isVehicle())) {
      if (!Methods.isActionAllowed(performer, action, item)) {
        return true;
      }
    }
    boolean insta = performer.getPower() > 0;
    if (counter == 1.0F)
    {
      time = Actions.getMoveActionTime(performer);
      act.setTimeLeft(time);
      String actString = "turn";
      if (action == 864) {
        actString = "move";
      }
      if ((action == 99) || (action == 696)) {
        actString = "push";
      } else if ((action == 181) || (action == 697)) {
        actString = "pull";
      }
      performer.getCommunicator().sendNormalServerMessage("You start to " + actString + " the " + item.getName() + ".");
      Server.getInstance().broadCastAction(performer
        .getName() + " starts to " + actString + " the " + item.getName() + ".", performer, 5);
      if (!insta)
      {
        performer.sendActionControl("Moving " + item.getName(), true, time);
        
        performer.getStatus().modifyStamina(-200.0F);
      }
    }
    else
    {
      time = act.getTimeLeft();
    }
    if ((counter * 10.0F > time) || (insta))
    {
      if ((action == 99) || (action == 181) || (action == 696) || (action == 697) || (action == 864)) {
        SoundPlayer.playSound("sound.object.move.pushpull", item, 0.0F);
      }
      performer.getStatus().modifyStamina(-250.0F);
      String actString = "turn";
      if (action == 864) {
        actString = "move";
      }
      if ((action == 99) || (action == 696)) {
        actString = "push";
      } else if ((action == 181) || (action == 697)) {
        actString = "pull";
      }
      try
      {
        float dir = item.getRotation();
        if ((action == 177) || (action == 178))
        {
          float mod = item.isFence() ? 90.0F : 22.5F;
          if (action == 177) {
            dir += mod;
          } else if (action == 178) {
            dir -= mod;
          }
          if (dir < 0.0F) {
            dir = 360.0F + dir;
          } else if (dir > 360.0F) {
            dir -= 360.0F;
          }
          Object next1;
          VolaTile next;
          if (item.isFence())
          {
            VolaTile next2 = Zones.getOrCreateTile(item.getTileX(), item.getTileY(), item
              .isOnSurface());
            if (next2 != null) {
              for (Creature c : next2.getCreatures()) {
                if ((!c.isFriendlyKingdom(performer.getKingdomId())) || (
                  (performer.getCitizenVillage() != null) && (performer.getCitizenVillage().isEnemy(c))))
                {
                  performer.getCommunicator().sendNormalServerMessage("There are enemies blocking your turning.", (byte)3);
                  
                  return true;
                }
              }
            }
            int offz = 0;
            try
            {
              offz = (int)((item.getPosZ() - Zones.calculateHeight(item.getPosX(), item.getPosY(), item
                .isOnSurface())) / 10.0F);
            }
            catch (NoSuchZoneException nsz)
            {
              logger.log(Level.WARNING, "Rotating fence item outside zones.");
            }
            float rot = Creature.normalizeAngle(item.getRotation());
            if ((rot >= 45.0F) && (rot < 135.0F))
            {
              VolaTile next1 = Zones.getOrCreateTile(item.getTileX() + 1, item.getTileY(), item
                .isOnSurface());
              if ((next1 != null) && (next1.getCreatures().length > 0))
              {
                performer.getCommunicator().sendNormalServerMessage("There are creatures blocking your turning.", (byte)3);
                
                return true;
              }
              VolaTile next = Zones.getOrCreateTile(item.getTileX() + 1, item.getTileY(), true);
              
              next.removeFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, item.getTileX() + 1, item.getTileY(), offz, item, Tiles.TileBorderDirection.DIR_DOWN, next
                .getZone().getId(), next.getLayer()));
            }
            else if ((rot >= 135.0F) && (rot < 225.0F))
            {
              VolaTile next1 = Zones.getOrCreateTile(item.getTileX(), item.getTileY() + 1, item
                .isOnSurface());
              if ((next1 != null) && (next1.getCreatures().length > 0))
              {
                performer.getCommunicator().sendNormalServerMessage("There are creatures blocking your turning.", (byte)3);
                
                return true;
              }
              VolaTile next = Zones.getOrCreateTile(item.getTileX(), item.getTileY() + 1, true);
              
              next.removeFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, item.getTileX(), item.getTileY() + 1, offz, item, Tiles.TileBorderDirection.DIR_HORIZ, next
                .getZone().getId(), next.getLayer()));
            }
            else if ((rot >= 225.0F) && (rot < 315.0F))
            {
              VolaTile next1 = Zones.getOrCreateTile(item.getTileX() - 1, item.getTileY(), item
                .isOnSurface());
              if ((next1 != null) && (next1.getCreatures().length > 0))
              {
                performer.getCommunicator().sendNormalServerMessage("There are creatures blocking your turning.", (byte)3);
                
                return true;
              }
              VolaTile next = Zones.getOrCreateTile(item.getTileX(), item.getTileY(), true);
              next.removeFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, item.getTileX(), item.getTileY(), offz, item, Tiles.TileBorderDirection.DIR_DOWN, next
              
                .getZone().getId(), next.getLayer()));
            }
            else
            {
              next1 = Zones.getOrCreateTile(item.getTileX(), item.getTileY() - 1, item
                .isOnSurface());
              if ((next1 != null) && (((VolaTile)next1).getCreatures().length > 0))
              {
                performer.getCommunicator().sendNormalServerMessage("There are creatures blocking your turning.", (byte)3);
                
                return true;
              }
              next = Zones.getOrCreateTile(item.getTileX(), item.getTileY(), true);
              next.removeFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, item.getTileX(), item.getTileY(), offz, item, Tiles.TileBorderDirection.DIR_HORIZ, next
              
                .getZone().getId(), next.getLayer()));
            }
          }
          item.setRotation(dir);
          if (item.isFence()) {
            if (item.isOnSurface())
            {
              int offz = 0;
              try
              {
                offz = (int)((item.getPosZ() - Zones.calculateHeight(item.getPosX(), item.getPosY(), item
                  .isOnSurface())) / 10.0F);
              }
              catch (NoSuchZoneException nsz)
              {
                logger.log(Level.WARNING, "Dropping fence item outside zones.");
              }
              float rot = Creature.normalizeAngle(item.getRotation());
              if ((rot >= 45.0F) && (rot < 135.0F))
              {
                VolaTile next = Zones.getOrCreateTile(item.getTileX() + 1, item.getTileY(), item
                  .isOnSurface());
                next.addFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, item.getTileX() + 1, item.getTileY(), offz, item, Tiles.TileBorderDirection.DIR_DOWN, next
                  .getZone().getId(), next.getLayer()));
              }
              else if ((rot >= 135.0F) && (rot < 225.0F))
              {
                VolaTile next = Zones.getOrCreateTile(item.getTileX(), item.getTileY() + 1, item
                  .isOnSurface());
                next.addFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, item.getTileX(), item.getTileY() + 1, offz, item, Tiles.TileBorderDirection.DIR_HORIZ, next
                  .getZone().getId(), next.getLayer()));
              }
              else if ((rot >= 225.0F) && (rot < 315.0F))
              {
                VolaTile next = Zones.getOrCreateTile(item.getTileX(), item.getTileY(), item
                  .isOnSurface());
                next.addFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, item.getTileX(), item.getTileY(), offz, item, Tiles.TileBorderDirection.DIR_DOWN, next
                
                  .getZone().getId(), next.getLayer()));
              }
              else
              {
                VolaTile next = Zones.getOrCreateTile(item.getTileX(), item.getTileY(), item
                  .isOnSurface());
                next.addFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, item.getTileX(), item.getTileY(), offz, item, Tiles.TileBorderDirection.DIR_HORIZ, next
                
                  .getZone().getId(), next.getLayer()));
              }
            }
          }
          Zone zone = Zones.getZone((int)item.getPosX() >> 2, (int)item.getPosY() >> 2, item.isOnSurface());
          if (performer.getVisionArea() != null) {
            performer.getVisionArea().broadCastUpdateSelectBar(item.getWurmId(), true);
          }
          if (item.isGuardTower())
          {
            VolaTile tile = zone.getOrCreateTile(item.getTileX(), item.getTileY());
            tile.makeInvisible(item);
            tile.makeVisible(item);
          }
          else
          {
            Item parent = item.getParentOrNull();
            if ((parent != null) && (parent.getTemplate().hasViewableSubItems()) && (
              (!parent.getTemplate().isContainerWithSubItems()) || (item.isPlacedOnParent())))
            {
              VolaTile vt = Zones.getTileOrNull(parent.getTileX(), parent.getTileY(), parent.isOnSurface());
              if (vt != null)
              {
                next1 = vt.getWatchers();next = next1.length;
                for (VolaTile localVolaTile1 = 0; localVolaTile1 < next; localVolaTile1++)
                {
                  VirtualZone vz = next1[localVolaTile1];
                  if (vz.isVisible(parent, vt)) {
                    vz.getWatcher().getCommunicator().sendItem(item, -10L, false);
                  }
                }
              }
            }
            else
            {
              zone.removeItem(item, true, true);
              zone.addItem(item, true, false, false);
            }
          }
        }
        else if (item.isMoveable(performer))
        {
          float iposx = item.getPosX();
          float iposy = item.getPosY();
          int ix = item.getTileX();
          int iy = item.getTileY();
          long bridgeId = item.getBridgeId();
          BlockingResult result;
          boolean changingTile;
          Zone zone;
          Effect[] effects;
          if ((action == 181) || (action == 697))
          {
            double rotRads = Math.atan2(performer.getStatus().getPositionY() - iposy, performer
              .getStatus().getPositionX() - iposx);
            float rot = (float)(rotRads * 57.29577951308232D) + 90.0F;
            float length = action == 181 ? 0.2F : (item.getTemplateId() == 938) || (item.getTemplateId() == 931) ? 4.0F : 0.04F;
            
            float xPosMod = (float)Math.sin(rot * 0.017453292F) * length;
            float yPosMod = -(float)Math.cos(rot * 0.017453292F) * length;
            float newPosX = iposx + xPosMod;
            float newPosY = iposy + yPosMod;
            int placedX = (int)newPosX >> 2;
            int placedY = (int)newPosY >> 2;
            boolean surf = item.isOnSurface();
            if ((surf) && (item.isSurfaceOnly()) && (Terraforming.isCaveEntrance(Zones.getTextureForTile(placedX, placedY, 0))))
            {
              performer.getCommunicator().sendNormalServerMessage("You cannot pull the " + item
                .getName() + " into a cave.", (byte)3);
              return true;
            }
            result = Blocking.getBlockerBetween(performer, iposx, iposy, newPosX, newPosY, performer
              .getPositionZ(), item.getPosZ(), performer.isOnSurface(), item.isOnSurface(), false, 6, -1L, performer
              .getBridgeId(), 
              Math.abs(performer.getPositionZ() - item.getPosZ()) < 2.0F ? performer.getBridgeId() : item
              .getBridgeId(), false);
            if (result != null)
            {
              performer.getCommunicator().sendNormalServerMessage("You cannot pull the " + item
                .getName() + " into the wall.", (byte)3);
              return true;
            }
            changingTile = (placedX != ix) || (placedY != iy);
            if (changingTile)
            {
              if ((item.isOnePerTile()) || (item.isFourPerTile()))
              {
                VolaTile t = Zones.getTileOrNull(placedX, placedY, performer.isOnSurface());
                if (t != null) {
                  if (((item.isOnePerTile()) && (t.hasOnePerTileItem(performer.getFloorLevel()))) || (
                    (item.isFourPerTile()) && (t.getFourPerTileCount(performer.getFloorLevel()) >= 4)))
                  {
                    performer.getCommunicator().sendNormalServerMessage("You cannot move that item here, since there is not enough space.", (byte)3);
                    
                    return true;
                  }
                }
              }
              if (item.getTemplateId() == 1309) {
                try
                {
                  Item waystone = Items.getItem(item.getData());
                  int dx = Math.abs(placedX - waystone.getTileX());
                  int dy = Math.abs(placedY - waystone.getTileY());
                  if ((dx > 1) || (dy > 1))
                  {
                    performer.getCommunicator().sendNormalServerMessage("You cannot move that item here, as it would be too far away from its associated waystone.", (byte)3);
                    
                    return true;
                  }
                }
                catch (NoSuchItemException e)
                {
                  logger.log(Level.WARNING, "Associated waystone missing! " + e.getMessage(), e);
                  performer.getCommunicator().sendNormalServerMessage("You cannot move that item here, as its associated waystone is missing.", (byte)3);
                  
                  return true;
                }
              }
            }
            if (item.isDecoration())
            {
              VolaTile t = Zones.getTileOrNull(placedX, placedY, performer.isOnSurface());
              if (t != null) {
                if (t.getStructure() != null) {
                  if (t.getNumberOfDecorations(t.getDropFloorLevel(performer.getFloorLevel())) > 14)
                  {
                    performer.getCommunicator().sendNormalServerMessage("That place is too littered with decorations already.", (byte)3);
                    
                    return true;
                  }
                }
              }
            }
            zone = Zones.getZone(item.getTileX(), item.getTileY(), item.isOnSurface());
            if (performer.getVisionArea() != null) {
              performer.getVisionArea().broadCastUpdateSelectBar(item.getWurmId(), true);
            }
            zone.removeItem(item, true, true);
            item.setPosXY(newPosX, newPosY);
            if ((changingTile) && (placedX == performer.getTileX()) && (placedY == performer.getTileY())) {
              item.setOnBridge(performer.getBridgeId());
            }
            Zone z = Zones.getZone((int)newPosX >> 2, (int)newPosY >> 2, performer.isOnSurface());
            z.addItem(item, true, false, false);
            if (surf != item.isOnSurface()) {
              z = Zones.getZone((int)newPosX >> 2, (int)newPosY >> 2, item.isOnSurface());
            }
            effects = item.getEffects();
            if (effects != null) {
              for (Effect lEffect : effects)
              {
                zone.removeEffect(lEffect);
                lEffect.setPosX(newPosX);
                lEffect.setPosY(newPosY);
                z.addEffect(lEffect, false);
              }
            }
          }
          if (action == 864)
          {
            Zone zone = Zones.getZone(item.getTileX(), item.getTileY(), item.isOnSurface());
            zone.removeItem(item, true, true);
            item.setPosXY(((int)item.getPosX() >> 2) * 4 + 2, ((int)item.getPosY() >> 2) * 4 + 2);
            zone.addItem(item, true, false, false);
          }
          else if ((action == 99) || (action == 696))
          {
            float rot = item.getTemplateId() == 931 ? item.getRotation() : performer.getStatus().getRotation();
            float length = action == 99 ? 0.2F : (item.getTemplateId() == 938) || (item.getTemplateId() == 931) ? 4.0F : 0.04F;
            
            float xPosMod = (float)Math.sin(rot * 0.017453292F) * length;
            float yPosMod = -(float)Math.cos(rot * 0.017453292F) * length;
            float newPosX = iposx + xPosMod;
            float newPosY = iposy + yPosMod;
            int placedX = (int)newPosX >> 2;
            int placedY = (int)newPosY >> 2;
            boolean surf = item.isOnSurface();
            if ((surf) && (item.isSurfaceOnly()) && (Terraforming.isCaveEntrance(Zones.getTextureForTile(placedX, placedY, 0))))
            {
              performer.getCommunicator().sendNormalServerMessage("You cannot push the " + item
                .getName() + " into a cave.", (byte)3);
              return true;
            }
            BlockingResult result = Blocking.getBlockerBetween(performer, iposx, iposy, newPosX, newPosY, performer
              .getPositionZ(), item.getPosZ(), performer.isOnSurface(), item.isOnSurface(), false, 6, -1L, performer
              .getBridgeId(), 
              Math.abs(performer.getPositionZ() - item.getPosZ()) < 2.0F ? performer.getBridgeId() : item
              .getBridgeId(), false);
            Blocker b;
            if (result != null)
            {
              boolean skip = false;
              if (item.getTemplateId() == 931) {
                if (result.getBlockerArray().length < 2)
                {
                  result = result.getBlockerArray();changingTile = result.length;
                  for (zone = 0; zone < changingTile; zone++)
                  {
                    b = result[zone];
                    if (b.getTempId() == item.getWurmId()) {
                      skip = true;
                    }
                  }
                }
              }
              if (!skip)
              {
                performer.getCommunicator().sendNormalServerMessage("You cannot push the " + item
                  .getName() + " into the wall.", (byte)3);
                return true;
              }
            }
            boolean changingTile = (placedX != ix) || (placedY != iy);
            if (changingTile)
            {
              if ((item.isOnePerTile()) || (item.isFourPerTile()))
              {
                VolaTile t = Zones.getTileOrNull(placedX, placedY, performer.isOnSurface());
                if (t != null)
                {
                  if (((item.isOnePerTile()) && (t.hasOnePerTileItem(performer.getFloorLevel()))) || (
                    (item.isFourPerTile()) && (t.getFourPerTileCount(performer.getFloorLevel()) >= 4)))
                  {
                    performer.getCommunicator().sendNormalServerMessage("You cannot move that item here, since there is not enough space.", (byte)3);
                    
                    return true;
                  }
                  if (item.isFence()) {
                    if (t.getCreatures().length > 0)
                    {
                      performer.getCommunicator().sendNormalServerMessage("You cannot move that item here, since the creatures block you.", (byte)3);
                      
                      return true;
                    }
                  }
                }
              }
              if (item.getTemplateId() == 1309) {
                try
                {
                  Item waystone = Items.getItem(item.getData());
                  int dx = Math.abs(placedX - waystone.getTileX());
                  int dy = Math.abs(placedY - waystone.getTileY());
                  if ((dx > 1) || (dy > 1))
                  {
                    performer.getCommunicator().sendNormalServerMessage("You cannot move that item there, as it would be too far away from its associated waystone.", (byte)3);
                    
                    return true;
                  }
                }
                catch (NoSuchItemException e)
                {
                  logger.log(Level.WARNING, "Associated waystone missing! " + e.getMessage(), e);
                  performer.getCommunicator().sendNormalServerMessage("You cannot move that item there, as its associated waystone is missing.", (byte)3);
                  
                  return true;
                }
              }
              if (bridgeId == -10L)
              {
                int floorLevel = item.getFloorLevel();
                BridgePart bridgePart = Zones.getBridgePartFor(placedX, placedY, item.isOnSurface());
                if ((bridgePart != null) && (bridgePart.hasAnExit())) {
                  if (placedY < iy)
                  {
                    if ((bridgePart.hasSouthExit()) && (bridgePart.getSouthExitFloorLevel() == floorLevel)) {
                      bridgeId = bridgePart.getStructureId();
                    }
                  }
                  else if (placedX > ix)
                  {
                    if ((bridgePart.hasWestExit()) && (bridgePart.getWestExitFloorLevel() == floorLevel)) {
                      bridgeId = bridgePart.getStructureId();
                    }
                  }
                  else if (placedY > iy)
                  {
                    if ((bridgePart.hasNorthExit()) && (bridgePart.getNorthExitFloorLevel() == floorLevel)) {
                      bridgeId = bridgePart.getStructureId();
                    }
                  }
                  else if (placedX < ix) {
                    if ((bridgePart.hasEastExit()) && (bridgePart.getEastExitFloorLevel() == floorLevel)) {
                      bridgeId = bridgePart.getStructureId();
                    }
                  }
                }
              }
              else
              {
                BridgePart newBridgePart = Zones.getBridgePartFor(placedX, placedY, item.isOnSurface());
                if (newBridgePart == null)
                {
                  BridgePart bridgePart = Zones.getBridgePartFor(ix, iy, item.isOnSurface());
                  if ((bridgePart == null) || (bridgePart.getStructureId() != bridgeId))
                  {
                    performer.getCommunicator().sendNormalServerMessage("Error: Item is on a bridge, but it isnt!.");
                    
                    return true;
                  }
                  int newFloorLevel = -1;
                  if (placedY < iy)
                  {
                    if (bridgePart.hasNorthExit()) {
                      newFloorLevel = bridgePart.getNorthExitFloorLevel();
                    }
                  }
                  else if (placedX > ix)
                  {
                    if (bridgePart.hasEastExit()) {
                      newFloorLevel = bridgePart.getEastExitFloorLevel();
                    }
                  }
                  else if (placedY > iy)
                  {
                    if (bridgePart.hasSouthExit()) {
                      newFloorLevel = bridgePart.getSouthExitFloorLevel();
                    }
                  }
                  else if (placedX < ix) {
                    if (bridgePart.hasWestExit()) {
                      newFloorLevel = bridgePart.getWestExitFloorLevel();
                    }
                  }
                  if (newFloorLevel == -1)
                  {
                    performer.getCommunicator().sendNormalServerMessage("Cannot find the floor level off the end of this bridge.");
                    
                    return true;
                  }
                  bridgeId = -10L;
                }
              }
            }
            if (item.isDecoration())
            {
              VolaTile t = Zones.getTileOrNull(placedX, placedY, performer.isOnSurface());
              if (t != null) {
                if (t.getStructure() != null) {
                  if (t.getNumberOfDecorations(t.getDropFloorLevel(performer.getFloorLevel())) > 14)
                  {
                    performer.getCommunicator().sendNormalServerMessage("That place is too littered with decorations already.", (byte)3);
                    
                    return true;
                  }
                }
              }
            }
            if (item.getTemplateId() == 1311)
            {
              VolaTile t = Zones.getTileOrNull(placedX, placedY, performer.isOnSurface());
              if (t != null) {
                if (t.getFourPerTileCount(performer.getFloorLevel()) >= 4)
                {
                  performer.getCommunicator().sendNormalServerMessage("You cannot move this item, there isn't enough room.");
                  
                  return true;
                }
              }
            }
            Zone zone = Zones.getZone(item.getTileX(), item.getTileY(), item.isOnSurface());
            if (item.getTemplateId() == 931)
            {
              VolaTile t = zone.getTileOrNull(item.getTileX(), item.getTileY());
              float newPosZ = Zones.calculatePosZ(newPosX, newPosY, t, performer.isOnSurface(), false, item
                .getPosZ(), performer, -10L);
              if ((newPosZ < -1.0F) && (item.getPosZ() >= -1.0F))
              {
                performer.getCommunicator().sendNormalServerMessage("That place is too deep. The " + item
                  .getName() + " would get stuck.", (byte)3);
                
                return true;
              }
              t.moveItem(item, newPosX, newPosY, newPosZ, item.getRotation(), performer.isOnSurface(), item
                .getPosZ());
            }
            else
            {
              zone.removeItem(item, true, true);
              item.setPosXY(newPosX, newPosY);
              item.setOnBridge(bridgeId);
              Zone z = Zones.getZone((int)newPosX >> 2, (int)newPosY >> 2, performer.isOnSurface());
              z.addItem(item, true, false, false);
              if (surf != item.isOnSurface()) {
                z = Zones.getZone((int)newPosX >> 2, (int)newPosY >> 2, item.isOnSurface());
              }
              Effect[] effects = item.getEffects();
              if (effects != null)
              {
                b = effects;effects = b.length;
                for (Effect[] arrayOfEffect2 = 0; arrayOfEffect2 < effects; arrayOfEffect2++)
                {
                  Effect lEffect = b[arrayOfEffect2];
                  
                  zone.removeEffect(lEffect);
                  lEffect.setPosX(newPosX);
                  lEffect.setPosY(newPosY);
                  z.addEffect(lEffect, false);
                }
              }
            }
          }
        }
        else
        {
          performer.getCommunicator().sendNormalServerMessage("The " + item
            .getName() + " won't budge. It is stuck.", (byte)3);
          return true;
        }
        performer.getCommunicator().sendNormalServerMessage("You " + actString + " the " + item.getName() + " a bit.");
        if ((action == 99) || (action == 696)) {
          actString = actString + "e";
        }
        Server.getInstance().broadCastAction(performer
          .getName() + " " + actString + "s the " + item.getName() + " a bit.", performer, 5);
      }
      catch (NoSuchZoneException nsz)
      {
        performer.getCommunicator().sendNormalServerMessage("You fail to " + actString + " the " + item
          .getName() + ". It must be stuck.");
        logger.log(Level.WARNING, performer.getName() + ": " + nsz.getMessage(), nsz);
      }
      toReturn = true;
    }
    return toReturn;
  }
  
  static boolean lock(Creature performer, Item lock, Item target, float counter, boolean replacing)
  {
    if (lock.isLocked())
    {
      performer.getCommunicator().sendNormalServerMessage("The " + lock.getName() + " is already in use.", (byte)3);
      
      return true;
    }
    if (((target.getLockId() == -10L) && (!replacing)) || (
      (target.getLockId() != -10L) && (replacing)))
    {
      if (!target.isOwner(performer)) {
        if (!Methods.isActionAllowed(performer, (short)161)) {
          return true;
        }
      }
      if (target.isBoat())
      {
        if (!lock.isBoatLock())
        {
          performer.getCommunicator().sendNormalServerMessage("You need to lock the " + target
            .getName() + " with a boat lock.", (byte)3);
          return true;
        }
        if (performer.getWurmId() != target.getLastOwnerId())
        {
          performer.getCommunicator().sendNormalServerMessage("You do not have the right to lock this boat. Are you really the captain?", (byte)3);
          
          return true;
        }
      }
      else if (!lock.mayLockItems())
      {
        performer.getCommunicator().sendNormalServerMessage("You can't lock the " + target
          .getName() + " with that.", (byte)3);
        return true;
      }
      if (!performer.hasAllKeysForLock(lock)) {
        performer.getCommunicator().sendAlertServerMessage("Security Warning: You do not have all the keys for that lock!", (byte)2);
      }
      long oldLockId = target.getLockId();
      if (oldLockId != -10L) {
        try
        {
          Item oldLock = Items.getItem(oldLockId);
          oldLock.setLocked(false);
          performer.getInventory().insertItem(oldLock);
        }
        catch (NoSuchItemException e)
        {
          logger.log(Level.WARNING, "Old lock was not found: " + e.getMessage(), e);
        }
      }
      lock.putInVoid();
      performer.getCommunicator().sendNormalServerMessage("You lock the " + target
        .getName() + " with the " + lock.getName() + ".");
      Server.getInstance().broadCastAction(performer
        .getName() + " locks the " + target.getName() + " with a " + lock.getName() + ".", performer, 5);
      target.setLockId(lock.getWurmId());
      int tilex = target.getTileX();
      int tiley = target.getTileY();
      SoundPlayer.playSound("sound.object.lockunlock", tilex, tiley, performer.isOnSurface(), 1.0F);
      
      lock.setLocked(true);
      PermissionsHistories.addHistoryEntry(target.getWurmId(), System.currentTimeMillis(), performer
        .getWurmId(), performer.getName(), replacing ? "Replaced Lock" : "Attached Lock");
      
      return true;
    }
    performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already locked.", (byte)3);
    
    return false;
  }
  
  static boolean unlock(Creature performer, Item key, Item target, float counter)
  {
    long lockId = target.getLockId();
    if (lockId == -10L)
    {
      performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is not locked.", (byte)3);
      
      return false;
    }
    try
    {
      Item lock = Items.getItem(lockId);
      long keyId = -10L;
      if (key != null) {
        keyId = key.getWurmId();
      }
      long[] keys = lock.getKeyIds();
      int tilex = target.getTileX();
      int tiley = target.getTileY();
      SoundPlayer.playSound("sound.object.lockunlock", tilex, tiley, performer.isOnSurface(), 1.0F);
      boolean hasKey = (performer.hasKeyForLock(lock)) || (target.isOwner(performer));
      boolean foundKey = hasKey;
      if (!foundKey) {
        for (long lKey : keys) {
          if (lKey == keyId)
          {
            foundKey = true;
            break;
          }
        }
      }
      if (foundKey)
      {
        performer.getCommunicator().sendNormalServerMessage("You unlock the " + target.getName() + ".");
        Server.getInstance().broadCastAction(performer.getName() + " unlocks the " + target.getName() + ".", performer, 5);
        
        target.setLockId(-10L);
        lock.setLocked(false);
        ItemSettings.remove(target.getWurmId());
        PermissionsHistories.addHistoryEntry(target.getWurmId(), System.currentTimeMillis(), performer
          .getWurmId(), performer.getName(), "Removed Lock");
        performer.getInventory().insertItem(lock, true);
        return true;
      }
      performer.getCommunicator().sendNormalServerMessage("The key does not fit.", (byte)3);
    }
    catch (NoSuchItemException nsi)
    {
      logger.log(Level.WARNING, "No such lock, but it should be locked." + nsi.getMessage(), nsi);
      return true;
    }
    return false;
  }
  
  public static final void checkLockpickBreakage(Creature performer, Item lockpick, int breakBonus, double power)
  {
    breakBonus += lockpick.getRarity() * 20;
    if (power > 0.0D)
    {
      if (Server.rand.nextInt(85 + breakBonus) <= (100.0F - lockpick.getCurrentQualityLevel()) / 5.0F)
      {
        performer.getCommunicator().sendNormalServerMessage("The lockpick breaks.", (byte)3);
        SoundPlayer.playSound("sound.object.lockpick.break.ogg", performer, 1.0F);
        Items.destroyItem(lockpick.getWurmId());
      }
      else
      {
        lockpick.setDamage(lockpick.getDamage() + (0.25F - lockpick.getRarity() * 0.05F) * lockpick
          .getDamageModifier());
      }
    }
    else if (Server.rand.nextInt(65 + breakBonus) <= (100.0F - lockpick.getCurrentQualityLevel()) / 5.0F)
    {
      performer.getCommunicator().sendNormalServerMessage("The lockpick breaks.", (byte)3);
      SoundPlayer.playSound("sound.object.lockpick.break.ogg", performer, 1.0F);
      Items.destroyItem(lockpick.getWurmId());
    }
    else
    {
      lockpick.setDamage(lockpick.getDamage() + (0.5F - lockpick.getRarity() * 0.1F) * lockpick.getDamageModifier());
    }
  }
  
  static boolean picklock(Creature performer, Item lockpick, Item target, float counter, Action act)
  {
    long lockId = target.getLockId();
    boolean done = false;
    if (lockId == -10L)
    {
      performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is not locked.", (byte)3);
      return true;
    }
    if (lockpick.getTemplateId() != 463)
    {
      performer.getCommunicator().sendNormalServerMessage("The " + lockpick.getName() + " can not be used as a lockpick.", (byte)3);
      return true;
    }
    if (target.getWurmId() == 5390789413122L) {
      if (target.getParentId() == 5390755858690L)
      {
        boolean ok = true;
        if (!performer.hasAbility(Abilities.getAbilityForItem(809, performer))) {
          ok = false;
        }
        if (!performer.hasAbility(Abilities.getAbilityForItem(808, performer))) {
          ok = false;
        }
        if (!performer.hasAbility(Abilities.getAbilityForItem(798, performer))) {
          ok = false;
        }
        if (!performer.hasAbility(Abilities.getAbilityForItem(810, performer))) {
          ok = false;
        }
        if (!performer.hasAbility(Abilities.getAbilityForItem(807, performer))) {
          ok = false;
        }
        if (!ok)
        {
          performer.getCommunicator().sendAlertServerMessage("There is some mysterious enchantment on this lock!");
          return true;
        }
      }
    }
    boolean insta = (performer.getPower() >= 5) || ((Servers.localServer.testServer) && (performer.getPower() > 1));
    Skill lockpicking = null;
    Skills skills = performer.getSkills();
    try
    {
      lockpicking = skills.getSkill(10076);
    }
    catch (NoSuchSkillException nss)
    {
      lockpicking = skills.learn(10076, 1.0F);
    }
    int time = 300;
    if (counter == 1.0F)
    {
      for (Player p : Players.getInstance().getPlayers()) {
        if (p.getWurmId() != performer.getWurmId()) {
          try
          {
            Action pact = p.getCurrentAction();
            if ((act.getNumber() == pact.getNumber()) && (act.getTarget() == pact.getTarget()))
            {
              performer.getCommunicator().sendNormalServerMessage("The " + target
                .getName() + " is already being picked by " + p.getName() + ".", (byte)3);
              return true;
            }
          }
          catch (NoSuchActionException localNoSuchActionException) {}
        }
      }
      if (!performer.isOnPvPServer()) {
        if (target.getOwnerId() == -10L)
        {
          boolean ok = false;
          if (target.getLastOwnerId() != -10L)
          {
            Village v = Villages.getVillageForCreature(target.getLastOwnerId());
            if (v != null)
            {
              if (performer.getCitizenVillage() == v) {
                ok = true;
              }
              if (v.isEnemy(performer.getCitizenVillage())) {
                ok = true;
              }
            }
            if (target.getLastOwnerId() == performer.getWurmId())
            {
              ok = true;
            }
            else if (!ok)
            {
              PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(target.getLastOwnerId());
              if (pinf != null)
              {
                Friend[] friends = pinf.getFriends();
                for (Friend f : friends) {
                  if ((f.getFriendId() == performer.getWurmId()) && (f.getCategory() == Friend.Category.Trusted)) {
                    ok = true;
                  }
                }
              }
            }
          }
          if (!ok)
          {
            performer.getCommunicator().sendNormalServerMessage("You are not allowed to pick the lock of that in these peaceful lands.", (byte)3);
            
            return true;
          }
        }
      }
      try
      {
        Item lock = Items.getItem(lockId);
        if (lock.getQualityLevel() - lockpick.getQualityLevel() > 20.0F)
        {
          performer.getCommunicator().sendNormalServerMessage("You need a more advanced lock pick for this high quality lock.", (byte)3);
          
          return true;
        }
      }
      catch (NoSuchItemException nsi)
      {
        performer.getCommunicator().sendNormalServerMessage("There is no lock to pick.", (byte)3);
        logger.log(Level.WARNING, "No such lock, but it should be locked." + nsi.getMessage(), nsi);
        return true;
      }
      time = Actions.getPickActionTime(performer, lockpicking, lockpick, 0.0D);
      
      act.setTimeLeft(time);
      
      performer.getCommunicator().sendNormalServerMessage("You start to pick the lock of the " + target
        .getName() + ".");
      Server.getInstance().broadCastAction(performer
        .getName() + " starts to pick the lock of the " + target.getName() + ".", performer, 5);
      performer.sendActionControl("picking lock", true, time);
      performer.getStatus().modifyStamina(-2000.0F);
    }
    else
    {
      time = act.getTimeLeft();
    }
    if (act.currentSecond() == 2) {
      checkLockpickBreakage(performer, lockpick, 100, 80.0D);
    }
    if ((counter * 10.0F > time) || (insta))
    {
      performer.getStatus().modifyStamina(-2000.0F);
      boolean dryRun = false;
      try
      {
        Item lock = Items.getItem(lockId);
        done = true;
        if ((target.getOwnerId() == performer.getWurmId()) || 
          (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 4.0F)))
        {
          boolean stealing = checkIfStealing(target, performer, act);
          
          double bonus = 100.0F * Item.getMaterialLockpickBonus(lockpick.getMaterial());
          int breakBonus = (int)(bonus * 2.0D);
          bonus -= lock.getRarity() * 10;
          bonus = Math.min(99.0D, bonus);
          if (stealing)
          {
            if (Action.checkLegalMode(performer)) {
              return true;
            }
            if (!performer.maySteal())
            {
              performer.getCommunicator().sendNormalServerMessage("You need more body control to pick locks.", (byte)3);
              return true;
            }
            if (setTheftEffects(performer, act, target))
            {
              double power = lockpicking.skillCheck(lock.getCurrentQualityLevel(), lockpick, bonus, false, 10.0F);
              
              checkLockpickBreakage(performer, lockpick, breakBonus, power);
              return true;
            }
          }
          double power = lockpicking.skillCheck(lock.getCurrentQualityLevel(), lockpick, bonus, false, 10.0F);
          float rarityMod = 1.0F;
          if (lock.getRarity() > 0) {
            rarityMod += lock.getRarity() * 0.2F;
          }
          if (target.getRarity() > 0) {
            rarityMod += target.getRarity() * 0.2F;
          }
          if (lockpick.getRarity() > 0) {
            rarityMod -= lockpick.getRarity() * 0.1F;
          }
          byte picktype = 0;
          if (target.isVehicle()) {
            if (target.getPosZ() < 1.0F) {
              if (target.getSizeZ() > 5) {
                picktype = 2;
              }
            }
          }
          float chance = getPickChance(target.getCurrentQualityLevel(), lockpick.getCurrentQualityLevel(), lock.getCurrentQualityLevel(), (float)lockpicking.getRealKnowledge(), picktype) / rarityMod * (1.0F + Item.getMaterialLockpickBonus(lockpick.getMaterial()));
          if (Server.rand.nextFloat() * 100.0F < chance)
          {
            performer.getCommunicator().sendNormalServerMessage("You pick the lock of the " + target
              .getName() + ".");
            Server.getInstance().broadCastAction(performer
              .getName() + " picks the lock of the " + target.getName() + ".", performer, 5);
            target.setLockId(-10L);
            ItemSettings.remove(target.getWurmId());
            PermissionsHistories.addHistoryEntry(target.getWurmId(), System.currentTimeMillis(), performer
              .getWurmId(), performer.getName(), "Lock Picked");
            lock.setLocked(false);
            performer.getInventory().insertItem(lock, true);
            SoundPlayer.playSound("sound.object.lockunlock", target, 0.2F);
            performer.achievement(111);
            if (target.isBoat()) {
              performer.achievement(108);
            }
          }
          else
          {
            performer.getCommunicator().sendNormalServerMessage("You fail to pick the lock of the " + target
              .getName() + ".", (byte)3);
            Server.getInstance().broadCastAction(performer
              .getName() + " silently curses as " + performer.getHeSheItString() + " fails to pick the lock of the " + target
              .getName() + ".", performer, 5);
          }
          if (power > 0.0D) {
            checkLockpickBreakage(performer, lockpick, breakBonus, 100.0D);
          } else {
            checkLockpickBreakage(performer, lockpick, breakBonus, -100.0D);
          }
        }
        else
        {
          performer.getCommunicator().sendNormalServerMessage("You are too far away from the " + target
            .getName() + " to pick the lock.", (byte)3);
        }
        return true;
      }
      catch (NoSuchItemException nsi)
      {
        performer.getCommunicator().sendNormalServerMessage("There is no lock to pick.", (byte)3);
        logger.log(Level.WARNING, "No such lock, but it should be locked." + nsi.getMessage(), nsi);
        return true;
      }
    }
    return done;
  }
  
  static final float getPickModChance(float chance)
  {
    return (1000.0F + chance * chance) / 11000.0F;
  }
  
  public static final float getPickChance(float containerQl, float pickQl, float lockQl, float skill, byte pickType)
  {
    float chance = getPickModChance(skill);
    float baseChance = Math.max(1.0F, Math.min(95.0F, 100.0F - lockQl));
    if ((skill > lockQl) && (pickType != 2)) {
      baseChance += skill - lockQl;
    }
    chance *= baseChance;
    if (pickType == 3) {
      chance *= 2.0F;
    }
    float contMod = 1.0F + (100.0F - containerQl) / 100.0F;
    
    chance *= contMod;
    if (pickType == 2) {
      chance /= 3.0F;
    }
    float mod = getPickModChance(pickQl);
    
    chance *= mod;
    
    return Math.max(0.001F, Math.min(99.0F, chance));
  }
  
  private static Point findDropTile(int tileX, int tileY, MeshIO mesh)
  {
    List<Point> slopes = new ArrayList();
    short h = Tiles.decodeHeight(mesh.getTile(tileX, tileY));
    for (int xx = 1; xx >= -1; xx--) {
      for (int yy = 1; yy >= -1; yy--) {
        if (GeneralUtilities.isValidTileLocation(tileX + xx, tileY + yy))
        {
          short th = Tiles.decodeHeight(mesh.getTile(tileX + xx, tileY + yy));
          if (((xx == 0) && (yy != 0)) || ((yy == 0) && (xx != 0))) {
            if (th <= h - 40) {
              slopes.add(new Point(tileX + xx, tileY + yy));
            }
          }
          if ((xx != 0) && (yy != 0)) {
            if (th <= h - 56) {
              slopes.add(new Point(tileX + xx, tileY + yy));
            }
          }
        }
      }
    }
    if (slopes.size() > 0)
    {
      int r = 0;
      if (slopes.size() > 1) {
        r = Server.rand.nextInt(slopes.size());
      }
      return findDropTile(((Point)slopes.get(r)).getX(), ((Point)slopes.get(r)).getY(), mesh);
    }
    return new Point(tileX, tileY);
  }
  
  static boolean takePile(Action act, Creature performer, Item origTarget)
  {
    boolean toReturn = true;
    try
    {
      long ownId = origTarget.getOwner();
      if (ownId != -10L) {
        return true;
      }
      if (ownId == performer.getWurmId()) {
        return true;
      }
    }
    catch (Exception ex)
    {
      try
      {
        Zone zone = Zones.getZone((int)origTarget.getPosX() >> 2, (int)origTarget.getPosY() >> 2, origTarget
          .isOnSurface());
        VolaTile tile = zone.getTileOrNull((int)origTarget.getPosX() >> 2, (int)origTarget.getPosY() >> 2);
        if (tile != null)
        {
          if (performer.getPower() == 0)
          {
            Structure struct = tile.getStructure();
            
            VolaTile tile2 = performer.getCurrentTile();
            if (tile2 != null)
            {
              if (tile2.getStructure() != struct)
              {
                performer.getCommunicator().sendNormalServerMessage("You can't reach the " + origTarget
                  .getName() + " through the wall.");
                return true;
              }
            }
            else if (struct != null)
            {
              performer.getCommunicator().sendNormalServerMessage("You can't reach the " + origTarget
                .getName() + " through the wall.");
              return true;
            }
          }
        }
        else
        {
          logger.log(Level.WARNING, performer.getName() + " scam?:No tile found in zone.");
          return true;
        }
      }
      catch (NoSuchZoneException nsz)
      {
        logger.log(Level.WARNING, performer.getName() + " scam?:" + nsz.getMessage(), nsz);
        return true;
      }
      Set<Item> items = origTarget.getItems();
      
      Item[] itemarr = (Item[])items.toArray(new Item[items.size()]);
      List<TakeResultEnum> printed = new ArrayList();
      Map<String, Integer> taken = new HashMap();
      int largestItem = 0;
      TakeResultEnum result;
      for (int i = 0; i < itemarr.length; i++)
      {
        result = take(act, performer, itemarr[i]);
        if (result.shouldPrint())
        {
          if (!printed.contains(result))
          {
            result.sendToPerformer(performer);
            printed.add(result);
          }
          if (result.abortsTakeFromPile()) {
            break;
          }
        }
        else
        {
          if (result.abortsTakeFromPile()) {
            break;
          }
          if (result == TakeResultEnum.SUCCESS)
          {
            if (!taken.containsKey(itemarr[i].getName())) {
              taken.put(itemarr[i].getName(), Integer.valueOf(1));
            } else {
              taken.put(itemarr[i].getName(), 
                Integer.valueOf(((Integer)taken.get(itemarr[i].getName())).intValue() + 1));
            }
            int size = itemarr[i].getSizeZ() / 10;
            if (size > largestItem) {
              largestItem = size;
            }
          }
        }
      }
      if (taken.size() > 0)
      {
        String takeString = "";
        for (String t : taken.keySet()) {
          if (takeString.isEmpty()) {
            takeString = StringUtil.format("%d %s", new Object[] { Integer.valueOf(((Integer)taken.get(t)).intValue()), t });
          } else {
            takeString = StringUtil.format("%s, %d %s", new Object[] { takeString, Integer.valueOf(((Integer)taken.get(t)).intValue()), t });
          }
        }
        performer.getCommunicator().sendNormalServerMessage("You get " + takeString + ".");
        Server.getInstance().broadCastAction(performer
          .getName() + " gets " + takeString + ".", performer, 
          Math.max(3, largestItem));
      }
    }
    return true;
  }
  
  public static void fillContainer(Item source, @Nullable Creature performer, boolean isBrackish)
  {
    if (source.isContainerLiquid())
    {
      if (source.isTraded())
      {
        if (performer != null) {
          performer.getCommunicator().sendNormalServerMessage("The container is traded.");
        }
      }
      else
      {
        Item contained = null;
        Item liquid = null;
        for (Iterator<Item> it = source.getItems().iterator(); it.hasNext();)
        {
          contained = (Item)it.next();
          if (((!contained.isFood()) && (!contained.isLiquid()) && (!contained.isRecipeItem())) || (
            (contained.isLiquid()) && (contained.getTemplateId() != 128)))
          {
            if (performer != null) {
              performer.getCommunicator().sendNormalServerMessage("That would destroy the liquid.");
            }
            return;
          }
          if (contained.isLiquid()) {
            liquid = contained;
          }
        }
        int volAvail = source.getFreeVolume();
        contained = liquid;
        if ((contained != null) && (contained.getRarity() != 0))
        {
          if (performer != null) {
            performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " would lose its rarity.");
          }
          return;
        }
        if (volAvail > 0)
        {
          int capac = volAvail;
          if (performer != null) {
            capac = performer.getCarryingCapacityLeft();
          }
          float fract = 1.0F;
          if (capac < volAvail)
          {
            fract = capac / volAvail;
            volAvail = (int)(fract * volAvail);
          }
          if (volAvail >= 1)
          {
            if (liquid != null)
            {
              int allWeight = liquid.getWeightGrams() + volAvail;
              
              float newQl = (100 * volAvail + contained.getCurrentQualityLevel() * contained.getWeightGrams()) / allWeight;
              
              liquid.setWeight(liquid.getWeightGrams() + volAvail, true);
              liquid.setQualityLevel(newQl);
              liquid.setDamage(0.0F);
              if (isBrackish) {
                liquid.setIsSalted(true);
              }
            }
            else
            {
              try
              {
                Item water = ItemFactory.createItem(128, 100.0F, (byte)26, (byte)0, null);
                water.setSizes(1, 1, 1);
                water.setWeight(volAvail, false);
                if (isBrackish) {
                  water.setIsSalted(true);
                }
                if (!source.insertItem(water))
                {
                  if (performer != null) {
                    performer.getCommunicator().sendNormalServerMessage("The container can't keep any of the water.");
                  }
                  Items.decay(water.getWurmId(), water.getDbStrings());
                  return;
                }
              }
              catch (NoSuchTemplateException nst)
              {
                logger.log(Level.WARNING, "No template for water?!", nst);
              }
              catch (FailedException fe)
              {
                logger.log(Level.WARNING, "Creation of water failed: ", fe);
              }
            }
            int tid = source.getTemplateId();
            String sound = "sound.liquid.fillcontainer";
            if ((tid == 190) || (tid == 189) || (tid == 768)) {
              sound = "sound.liquid.fillcontainer.barrel";
            } else if (tid == 421) {
              sound = "sound.liquid.fillcontainer.bucket";
            } else if (tid == 76) {
              sound = "sound.liquid.fillcontainer.jar";
            }
            if (performer != null)
            {
              Methods.sendSound(performer, sound);
              performer.getCommunicator().sendNormalServerMessage("You fill the " + source
                .getName() + " with " + (isBrackish ? "salty " : "") + "water.");
            }
          }
        }
        else if (performer != null)
        {
          performer.getCommunicator().sendNormalServerMessage("You wouldn't be able to carry the weight of the water.");
        }
      }
    }
    else if (performer != null) {
      performer.getCommunicator().sendNormalServerMessage("You cannot keep liquid in that.");
    }
  }
  
  public static void fillContainer(Action act, Item source, Item target, Creature performer, boolean quiet)
  {
    if (source.isContainerLiquid())
    {
      if ((!source.isTraded()) && (!target.isTraded()))
      {
        if (target.isNoTake())
        {
          performer.getCommunicator().sendSafeServerMessage("The " + target.getName() + " cannot be taken.");
          return;
        }
        Item contained = null;
        Item liquid = null;
        int volAvail = source.getFreeVolume();
        if (source.getOwnerId() == performer.getWurmId())
        {
          int canCarry = performer.getCarryingCapacityLeft();
          if (canCarry <= 0)
          {
            performer.getCommunicator().sendNormalServerMessage("You won't be able to carry that much.");
            return;
          }
          if (volAvail > canCarry) {
            volAvail = canCarry;
          }
        }
        for (Iterator<Item> it = source.getItems().iterator(); it.hasNext();)
        {
          contained = (Item)it.next();
          if (wouldDestroyLiquid(source, contained, target))
          {
            performer.getCommunicator().sendNormalServerMessage("That would destroy the liquid.");
            return;
          }
          if ((contained.isLiquid()) && (contained.getTemplateId() == target.getTemplateId()) && 
            (contained.getRealTemplateId() == target.getRealTemplateId())) {
            liquid = contained;
          }
        }
        contained = liquid;
        if ((contained != null) && (contained.getRarity() > target.getRarity())) {
          contained.setRarity(target.getRarity());
        }
        if (volAvail > 0)
        {
          if (checkIfStealing(target, performer, act))
          {
            if (Action.checkLegalMode(performer)) {
              return;
            }
            if (!performer.maySteal())
            {
              performer.getCommunicator().sendNormalServerMessage("You need more body control to steal things.");
              return;
            }
            if (setTheftEffects(performer, act, target)) {
              return;
            }
          }
          if (volAvail < target.getWeightGrams())
          {
            if ((!performer.canCarry(volAvail)) && (source.getOwnerId() == performer.getWurmId()))
            {
              performer.getCommunicator().sendNormalServerMessage("You won't be able to carry that much.");
              return;
            }
            if (contained == null)
            {
              try
              {
                Item splitItem = splitLiquid(target, volAvail, performer);
                source.insertItem(splitItem);
              }
              catch (FailedException fe)
              {
                logger.log(Level.WARNING, fe.getMessage(), fe);
                return;
              }
              catch (NoSuchTemplateException nst)
              {
                logger.log(Level.WARNING, nst.getMessage(), nst);
                return;
              }
            }
            else
            {
              boolean differentOwners = target.getOwnerId() != source.getOwnerId();
              
              target.setWeight(target.getWeightGrams() - volAvail, true, differentOwners);
              
              int allWeight = contained.getWeightGrams() + volAvail;
              
              float newQl = (target.getCurrentQualityLevel() * volAvail + contained.getCurrentQualityLevel() * contained.getWeightGrams()) / allWeight;
              if (contained.isColor()) {
                contained.setColor(WurmColor.mixColors(contained.color, contained.getWeightGrams(), target.color, volAvail, newQl));
              }
              float tmod = volAvail / allWeight;
              float contMod = contained.getWeightGrams() / allWeight;
              
              contained.setTemperature(
                (short)(int)(target.getTemperature() * tmod + contMod * contained.getTemperature()));
              contained.setQualityLevel(newQl);
              contained.setWeight(contained.getWeightGrams() + volAvail, true, differentOwners);
              contained.setDamage(0.0F);
            }
          }
          else
          {
            if ((!performer.canCarry(target.getWeightGrams())) && (source.getOwnerId() == performer.getWurmId()))
            {
              performer.getCommunicator().sendNormalServerMessage("You won't be able to carry that much.");
              return;
            }
            if (contained != null)
            {
              if ((contained.getTemplateId() == 417) || 
                (target.getTemplateId() == 417)) {
                if (contained.getRealTemplateId() != target.getRealTemplateId())
                {
                  String name1 = "fruit";
                  String name2 = "fruit";
                  ItemTemplate t = contained.getRealTemplate();
                  if (t != null) {
                    name1 = t.getName();
                  }
                  ItemTemplate t2 = target.getRealTemplate();
                  if (t2 != null) {
                    name2 = t2.getName();
                  }
                  if (!name1.equals(name2)) {
                    contained.setName(name1 + " and " + name2 + " juice");
                  }
                  contained.setRealTemplate(-10);
                }
              }
              int allWeight = target.getWeightGrams() + contained.getWeightGrams();
              
              float newQl = (target.getCurrentQualityLevel() * target.getWeightGrams() + contained.getCurrentQualityLevel() * contained.getWeightGrams()) / allWeight;
              if (contained.isColor()) {
                contained.setColor(WurmColor.mixColors(contained.color, contained.getWeightGrams(), target.color, target
                  .getWeightGrams(), newQl));
              }
              float tmod = target.getWeightGrams() / allWeight;
              float contMod = contained.getWeightGrams() / allWeight;
              
              contained.setTemperature(
                (short)(int)(target.getTemperature() * tmod + contMod * contained.getTemperature()));
              contained.setQualityLevel(newQl);
              contained.setDamage(0.0F);
              contained.setWeight(allWeight, true);
              Items.destroyItem(target.getWurmId());
            }
            else if (source.testInsertItem(target))
            {
              try
              {
                Item parent = target.getParent();
                parent.dropItem(target.getWurmId(), false);
              }
              catch (NoSuchItemException localNoSuchItemException) {}
              source.insertItem(target);
            }
            else
            {
              return;
            }
          }
          if (act.getNumber() != 345)
          {
            int tid = source.getTemplateId();
            String sound = "sound.liquid.fillcontainer";
            if ((tid == 190) || (tid == 189) || (tid == 768)) {
              sound = "sound.liquid.fillcontainer.barrel";
            } else if (tid == 421) {
              sound = "sound.liquid.fillcontainer.bucket";
            } else if (tid == 76) {
              sound = "sound.liquid.fillcontainer.jar";
            }
            Methods.sendSound(performer, sound);
          }
          if (!quiet)
          {
            int tilex = performer.getTileX();
            int tiley = (int)performer.getStatus().getPositionY() >> 2;
            VolaTile vtile = Zones.getOrCreateTile(tilex, tiley, performer.isOnSurface());
            
            performer.getCommunicator().sendNormalServerMessage("You fill the " + source
              .getName() + " with " + target.getName() + ".");
            vtile.broadCastAction(performer.getName() + " fills a " + source.getName() + " with " + target.getName() + ".", performer, false);
          }
        }
        else
        {
          return;
        }
      }
      else
      {
        performer.getCommunicator().sendNormalServerMessage("You are trading one of those items.");
      }
    }
    else {
      performer.getCommunicator().sendNormalServerMessage("You cannot keep liquid in that.");
    }
  }
  
  public static Item splitLiquid(Item target, int volAvail, Creature performer)
    throws FailedException, NoSuchTemplateException
  {
    Item splitItem = ItemFactory.createItem(target.getTemplateId(), target.getQualityLevel(), target.creator);
    
    splitItem.setWeight(volAvail, true);
    splitItem.setTemperature(target.getTemperature());
    splitItem.setAuxData(target.getAuxData());
    splitItem.setName(target.getActualName());
    splitItem.setCreator(target.creator);
    splitItem.setDamage(target.getDamage());
    splitItem.setMaterial(target.getMaterial());
    splitItem.setSizes(1, 1, 1);
    if (target.getRealTemplate() != null) {
      splitItem.setRealTemplate(target.getRealTemplateId());
    }
    if (target.descIsExam()) {
      splitItem.setDescription(target.examine(performer));
    } else {
      splitItem.setDescription(target.getDescription());
    }
    if (target.color != -1) {
      splitItem.setColor(target.color);
    }
    ItemMealData imd = ItemMealData.getItemMealData(target.getWurmId());
    if (imd != null) {
      ItemMealData.save(splitItem.getWurmId(), imd.getRecipeId(), imd.getCalories(), imd
        .getCarbs(), imd.getFats(), imd.getProteins(), imd.getBonus(), imd
        .getStages(), imd.getIngredients());
    }
    target.setWeight(target.getWeightGrams() - volAvail, true);
    return splitItem;
  }
  
  public static int eat(Creature performer, Item food)
  {
    float qlevel = food.getCurrentQualityLevel();
    int weight = food.getWeightGrams();
    float damage = food.getDamage();
    float hungerStilled = weight * qlevel * (100.0F - damage) / 100.0F;
    if (performer.getSize() == 5) {
      hungerStilled *= 0.5F;
    } else if (performer.getSize() == 4) {
      hungerStilled *= 0.7F;
    } else if (performer.getSize() == 2) {
      hungerStilled *= 5.0F;
    } else if (performer.getSize() == 1) {
      hungerStilled *= 10.0F;
    }
    if (food.getTemplateId() == 272)
    {
      int fat = food.getFat();
      if (fat > 0)
      {
        food.setDamage(food.getDamage() + 50.0F);
        food.setButchered();
      }
      else
      {
        hungerStilled = 0.0F;
      }
    }
    else
    {
      Items.destroyItem(food.getWurmId());
    }
    return (int)hungerStilled;
  }
  
  static boolean eat(Action act, Creature performer, Item food, float counter)
  {
    boolean done = false;
    float qlevel = food.getCurrentQualityLevel();
    int weight = food.getWeightGrams();
    int temp = food.getTemperature();
    String hotness = "";
    if (temp > 1000) {
      hotness = "hot ";
    }
    if (!food.isFood()) {
      return true;
    }
    if (food.getOwnerId() != performer.getWurmId())
    {
      if (!isLootableBy(performer, food))
      {
        performer.getCommunicator().sendNormalServerMessage("You may not loot that item.");
        return true;
      }
      if (food.isBanked())
      {
        performer.getCommunicator().sendNormalServerMessage("You can't eat from there.");
        return true;
      }
      if (checkIfStealing(food, performer, act))
      {
        if (Action.checkLegalMode(performer)) {
          return true;
        }
        if (!performer.maySteal())
        {
          performer.getCommunicator().sendNormalServerMessage("You need more body control to steal things.");
          return true;
        }
        if (setTheftEffects(performer, act, food)) {
          return true;
        }
      }
    }
    float bonus = 1.0F;
    if ((performer.getDeity() != null) && (performer.getDeity().isFoodBonus()) && (performer.getFaith() >= 20.0F) && 
      (performer.getFavor() >= 20.0F)) {
      bonus = 1.25F;
    }
    if (food.getDamage() > 90.0F)
    {
      performer.getCommunicator().sendNormalServerMessage("Eww.. the " + hotness + food
        .getName() + " tastes funny and won't feed you at all!");
      return true;
    }
    if (((food.getTemplateId() != 488) || (food.getRealTemplateId() != 488)) && 
      (food.getTemplateId() != 666) && (!food.isSource())) {
      if (performer.getStatus().getHunger() - 3000 < 0)
      {
        performer.getCommunicator().sendNormalServerMessage("You are so full, you cannot possibly eat anything else.");
        if (counter != 1.0F) {
          if ((food.isWrapped()) && ((food.canBeClothWrapped()) || (food.canBePapyrusWrapped()) || (food.canBeRawWrapped()))) {
            performer.getCommunicator().sendNormalServerMessage("You carefully re-wrap the " + hotness + food
              .getName(false) + " to keep it fresher.");
          }
        }
        return true;
      }
    }
    if (counter == 1.0F)
    {
      if (temp > 2500)
      {
        performer.getCommunicator().sendNormalServerMessage("The " + food.getName() + " is too hot to eat.");
        return true;
      }
      if ((food.isCheese()) && (food.isZombiefied())) {
        if (performer.getKingdomTemplateId() == 3)
        {
          performer.getCommunicator().sendNormalServerMessage("The " + food
            .getName() + " tastes weird, but good.");
        }
        else
        {
          performer.getCommunicator().sendNormalServerMessage("The " + food
            .getName() + " is horrible, and you can't eat it.");
          return true;
        }
      }
      if ((food.isWrapped()) && ((food.canBeClothWrapped()) || (food.canBePapyrusWrapped()) || (food.canBeRawWrapped()))) {
        performer.getCommunicator().sendNormalServerMessage("You carefully unwrap the " + hotness + food
          .getName(false) + " before eating it.");
      }
      int time = 0;
      time = (int)Math.min(performer.getStatus().getHunger() / (30.0F * qlevel), food.getWeightGrams() / 30.0F) * 10;
      
      Server.getInstance().broadCastAction(performer.getName() + " starts to eat " + food.getNameWithGenus() + ".", performer, 3);
      
      performer.sendActionControl("Eating " + hotness + food.getName(), true, time);
      if (qlevel > 50.0F)
      {
        performer.getCommunicator().sendNormalServerMessage("The " + hotness + food
          .getName() + " tastes " + food.getTasteString());
        if (food.getTasteString().contains("singing")) {
          performer.achievement(146);
        }
      }
    }
    if (((Player)performer).getAlcohol() > 90.0F) {
      if (Server.rand.nextInt(100) < ((Player)performer).getAlcohol())
      {
        performer.getCommunicator().sendNormalServerMessage("You miss the mouth and it ends up in your face. Who gives a damn?");
        
        Server.getInstance().broadCastAction(performer
          .getName() + " eats " + food.getNameWithGenus() + " with " + performer
          .getHisHerItsString() + " whole face instead of only with the mouth.", performer, 3);
        if (food.getTemplateId() != 666)
        {
          Items.destroyItem(food.getWurmId());
          SoundPlayer.playSound("sound.fish.splash", performer, 10.0F);
        }
        return true;
      }
    }
    if (food.getSpellFoodBonus() > 0.0F) {
      qlevel *= (1.0F + food.getSpellFoodBonus() / 100.0F);
    }
    float rarityMod = 0.0F;
    if (food.getRarity() > 0)
    {
      rarityMod = Math.max(1, food.getWeightGrams() / food.getTemplate().getWeightGrams());
      rarityMod = food.getRarity() * 10.0F / rarityMod;
      rarityMod /= 100.0F;
      rarityMod = Math.max(0.0F, Math.min(0.3F, rarityMod));
    }
    int fed = (int)(weight < 30 ? weight * qlevel * bonus : 30.0F * qlevel * bonus);
    if (food.getTemplateId() == 488) {
      performer.getStatus().modifyStamina((int)(qlevel * 100.0F));
    }
    if (weight < 30)
    {
      done = true;
      if (food.getTemplateId() == 666) {
        if (performer.isPlayer()) {
          if (((Player)performer).getSaveFile().getSleepLeft() < (((Player)performer).getSaveFile().isFlagSet(77) ? 5 : 4) * 3600L)
          {
            ((Player)performer).getSaveFile().addToSleep(3600);
          }
          else
          {
            performer.getCommunicator().sendNormalServerMessage("You just taste it, because eating it would be a waste right now.");
            
            return true;
          }
        }
      }
      modifyHunger(performer, food, weight, rarityMod, fed);
      if (food.isSource()) {
        performer.modifyKarma(weight);
      }
      if ((food.isWrapped()) && ((food.canBeClothWrapped()) || (food.canBePapyrusWrapped()) || (food.canBeRawWrapped()))) {
        performer.getCommunicator().sendNormalServerMessage("You throw away the old wrapping, litter-bug.");
      }
      Items.destroyItem(food.getWurmId());
    }
    else
    {
      if ((food.isCheese()) && (food.isZombiefied())) {
        if (performer.getKingdomId() == 3) {
          performer.healRandomWound((int)(qlevel / 20.0F));
        }
      }
      modifyHunger(performer, food, 30, rarityMod, fed);
      food.setWeight(weight - 30, true);
      if (food.isFish()) {
        food.setIsUnderWeight(true);
      }
      if (food.isSource()) {
        performer.modifyKarma(30);
      }
      if (food.getTemplateId() == 572) {
        if (WurmCalendar.currentTime - food.creationDate < 7200L) {
          if (act.currentSecond() % 5 == 0)
          {
            performer.getCommunicator().sendNormalServerMessage("The " + food
              .getName() + " is still alive! Its tentacles writhes about your face!");
            Server.getInstance().broadCastAction(performer
              .getName() + " eats a live " + food.getName() + "! Its tentacles writhes about " + performer
              .getHisHerItsString() + " face!", performer, 5);
          }
        }
      }
    }
    if (act.currentSecond() % 5 == 0) {
      SoundPlayer.playSound("sound.food.eat", performer, 1.0F);
    }
    if (performer.getStatus().getHunger() == 1)
    {
      if ((food.isWrapped()) && ((food.canBeClothWrapped()) || (food.canBePapyrusWrapped()) || (food.canBeRawWrapped()))) {
        performer.getCommunicator().sendNormalServerMessage("You carefully re-wrap the " + hotness + food
          .getName(false) + " to keep it fresher.");
      }
      done = true;
    }
    return done;
  }
  
  static void modifyHunger(Creature performer, Item food, int weight, float rarityMod, int fed)
  {
    float complexity = food.getFoodComplexity() * 50.0F;
    if (food.getTemplateId() == 1276)
    {
      complexity *= 0.1F;
      
      performer.getStatus().modifyThirst(-fed * 2 / 3, food
        .getCaloriesByWeight(weight) * complexity, food
        .getCarbsByWeight(weight) * complexity, food
        .getFatsByWeight(weight) * complexity, food
        .getProteinsByWeight(weight) * complexity);
      return;
    }
    performer.getStatus().modifyHunger(-fed, 
      Math.min(0.99F, food.getNutritionLevel() + rarityMod), food
      .getCaloriesByWeight(weight) * complexity, food
      .getCarbsByWeight(weight) * complexity, food
      .getFatsByWeight(weight) * complexity, food
      .getProteinsByWeight(weight) * complexity);
    
    AffinitiesTimed.addTimedAffinityFromBonus(performer, weight, food);
  }
  
  static boolean drinkChampagne(Creature performer, Item target)
  {
    Item parent = target.getParentOrNull();
    if (parent != null) {
      if (parent.isSealedByPlayer())
      {
        performer.getCommunicator().sendNormalServerMessage("You can't drink from there.");
        return true;
      }
    }
    if (target.getOwnerId() == performer.getWurmId())
    {
      if (target.getAuxData() < 10)
      {
        target.setAuxData((byte)(target.getAuxData() + 1));
        if (performer.isPlayer())
        {
          if (((Player)performer).getAlcohol() > 90.0F) {
            if (Server.rand.nextInt(100) < ((Player)performer).getAlcohol())
            {
              performer.getCommunicator().sendNormalServerMessage("You spill the " + target.getName() + " out. Who cares?");
              Server.getInstance().broadCastAction(performer.getName() + " throws the " + target.getName() + " over " + performer.getHisHerItsString() + " shoulder instead of drinking it.", performer, 3);
              return true;
            }
          }
          ((Player)performer).setRarityShader((byte)(target.getRarity() > 0 ? target.getRarity() : 1));
        }
        performer.getStatus().modifyStamina((int)(target.getQualityLevel() * (20 + target.getRarity() * 10)));
        addAlcohol(performer);
        performer.getStatus().modifyThirst((int)(-6554.0F * target.getQualityLevel() / 100.0F));
        performer.getCommunicator().sendNormalServerMessage("Ahh! The " + target.getName() + " tastes goood!");
      }
      else
      {
        performer.getCommunicator().sendNormalServerMessage("Sadly, the Champagne bottle is empty.");
        return true;
      }
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("You need to hold the Champagne to drink it.");
      return true;
    }
    return true;
  }
  
  static boolean drink(Action act, Creature performer, Item drink, float counter)
  {
    if ((performer.getStatus().getThirst() < 1000) && (!drink.isSource()) && (!drink.isAlcohol()))
    {
      performer.getCommunicator().sendNormalServerMessage("You are so bloated you cannot bring yourself to drink any thing else.");
      return true;
    }
    Item parent = drink.getParentOrNull();
    if (parent != null) {
      if (parent.isSealedByPlayer())
      {
        performer.getCommunicator().sendNormalServerMessage("You can't drink from there.");
        return true;
      }
    }
    boolean done = false;
    
    float fillmod = 6554.0F;
    float qlevel = drink.getCurrentQualityLevel();
    if (drink.getRarity() > 0)
    {
      float spamModifier = Math.max(1, drink.getWeightGrams() / drink.getTemplate().getWeightGrams());
      qlevel = Math.min(99.99F, qlevel + drink.getRarity() * 10.0F / spamModifier);
    }
    int temp = drink.getTemperature();
    int weight = drink.getWeightGrams();
    int template = drink.getTemplateId();
    if (template == 128)
    {
      qlevel = Math.max(90.0F, qlevel);
    }
    else
    {
      if (drink.getOwnerId() != performer.getWurmId())
      {
        if (drink.isBanked())
        {
          performer.getCommunicator().sendNormalServerMessage("You can't drink from there.");
          return true;
        }
        if (drink.getTemplateId() != 128)
        {
          if (!isLootableBy(performer, drink))
          {
            performer.getCommunicator().sendNormalServerMessage("You may not loot that item.");
            return true;
          }
          if (checkIfStealing(drink, performer, act))
          {
            if (Action.checkLegalMode(performer)) {
              return true;
            }
            if (!performer.maySteal())
            {
              performer.getCommunicator().sendNormalServerMessage("You need more body control to steal things.");
              return true;
            }
            if (setTheftEffects(performer, act, drink)) {
              return true;
            }
          }
        }
      }
      if (drink.isMilk())
      {
        if (qlevel > 5.0F) {
          qlevel = Math.min(100.0F, (float)(qlevel * 1.5D));
        }
      }
      else if (!drink.isDrinkable()) {
        qlevel = 0.0F;
      }
    }
    if ((counter == 1.0F) && (qlevel < 5.0F))
    {
      performer.getCommunicator().sendNormalServerMessage("Eww.. the " + drink
        .getName() + " tastes funny and won't quench your thirst at all!");
      done = true;
    }
    else
    {
      if ((performer.isPlayer()) && (((Player)performer).getAlcohol() > 90.0F)) {
        if (Server.rand.nextInt(100) < ((Player)performer).getAlcohol())
        {
          performer.getCommunicator().sendNormalServerMessage("You spill the " + drink.getName() + " out. Who cares?");
          Server.getInstance().broadCastAction(performer
            .getName() + " throws the " + drink.getName() + " over " + performer.getHisHerItsString() + " shoulder instead of drinking it.", performer, 3);
          
          Items.destroyItem(drink.getWurmId());
          return true;
        }
      }
      if (counter == 1.0F)
      {
        if ((temp > 600) && (template != 425))
        {
          performer.getCommunicator().sendNormalServerMessage("The " + drink.getName() + " is too hot to drink.");
          return true;
        }
        if ((drink.isMilk()) && (drink.isZombiefied()))
        {
          if (performer.getKingdomTemplateId() != 3)
          {
            performer.getCommunicator().sendNormalServerMessage("Eww.. the " + drink
              .getName() + " tastes horrible! You can't drink it.");
            return true;
          }
          performer.getCommunicator().sendNormalServerMessage("Hmm.. the " + drink
            .getName() + " tastes very special.");
        }
        int time = 10;
        time = (int)Math.min(performer.getStatus().getThirst() / (6554.0F * qlevel / 100.0F) * 10.0F, drink
          .getWeightGrams() / 200.0F);
        Server.getInstance().broadCastAction(performer.getName() + " drinks some " + drink.getName() + ".", performer, 3);
        
        performer.sendActionControl("Drinking " + drink.getName(), true, time);
        if (qlevel > 50.0F)
        {
          if (drink.getTemplateId() == 128)
          {
            if (temp < 300)
            {
              if (drink.isSalted()) {
                performer.getCommunicator().sendNormalServerMessage("The water is slightly salty but still cools you down.");
              } else {
                performer.getCommunicator().sendNormalServerMessage("The water is refreshing and it cools you down.");
              }
            }
            else if (drink.isSalted()) {
              performer.getCommunicator().sendNormalServerMessage("The water isn't exactly cold and has a salty taste.");
            } else {
              performer.getCommunicator().sendNormalServerMessage("The water isn't exactly cold but still refreshens you.");
            }
          }
          else {
            performer.getCommunicator().sendNormalServerMessage("The " + drink
              .getName() + " tastes " + drink.getTasteString());
          }
        }
        else if (drink.isSalted()) {
          performer.getCommunicator().sendNormalServerMessage("The water has a salty taste.");
        }
      }
      if ((drink.getTemplateId() == 427) || (drink.isAlcohol()))
      {
        performer.getStatus().modifyStamina((int)(qlevel * (20 + drink.getRarity() * 10)));
        if (drink.isAlcohol()) {
          if (performer.isPlayer())
          {
            Player player = (Player)performer;
            float drinkMod = 1.0F;
            drinkMod += drink.getCurrentQualityLevel() * 0.005F;
            if (drink.getWeightGrams() < 200) {
              drinkMod *= drink.getWeightGrams() / 200.0F;
            }
            float addAlcohol = drink.getAlcoholStrength() * 0.2F * drinkMod;
            player.setAlcohol(((Player)performer).getAlcohol() + addAlcohol);
            if (player.getAlcohol() > 20.0F) {
              if (player.getSaveFile().setAlcoholTime(player.getAlcoholAddiction() + 10L)) {
                player.getCommunicator().sendNormalServerMessage("You have just received the title '" + Titles.Title.Alcoholic
                  .getName(player.isNotFemale()) + "'!");
              }
            }
            if (player.getAlcohol() == 100.0F)
            {
              performer.getCommunicator().sendNormalServerMessage("You made it to the top! You are perfectly drunk!");
              
              Server.getInstance().broadCastAction(performer
                .getName() + " hits the record in drunkenness! " + performer.getHeSheItString() + " is perfectly drunk and can't drink any more!", performer, 3);
              
              player.addTitle(Titles.Title.Drunkard);
              player.achievement(296);
            }
            else if (((Player)performer).getAlcohol() >= 95.0F)
            {
              performer.getCommunicator().sendNormalServerMessage("You are setting some kind of record.");
              Server.getInstance().broadCastAction(performer
                .getName() + " has that dead, watery look in " + performer.getHisHerItsString() + " eyes now.", performer, 3);
            }
            else if (player.getAlcohol() >= 90.0F)
            {
              performer.getCommunicator().sendNormalServerMessage("You can barely walk.");
              Server.getInstance().broadCastAction(performer
                .getName() + " looks concentrated and sways back and forth.", performer, 3);
              performer.getMovementScheme().setDrunkMod(true);
            }
            else if (player.getAlcohol() >= 60.0F)
            {
              performer.getCommunicator().sendNormalServerMessage("You are really really drunk.");
              Server.getInstance().broadCastAction(performer.getName() + " is starting to drool.", performer, 3);
            }
            else if (player.getAlcohol() >= 30.0F)
            {
              performer.getCommunicator().sendNormalServerMessage("You are drunk.");
              Server.getInstance().broadCastAction(performer.getName() + " is verifyibly drunk now.", performer, 3);
              
              performer.achievement(567);
            }
            else if (player.getAlcohol() >= 20.0F)
            {
              performer.getCommunicator().sendNormalServerMessage("You are getting drunk.");
              Server.getInstance().broadCastAction(performer.getName() + " suddenly giggles uncontrollably.", performer, 3);
            }
            else if (player.getAlcohol() >= 10.0F)
            {
              performer.getCommunicator().sendNormalServerMessage("You are tipsy.");
            }
          }
        }
      }
      float complexity = drink.getFoodComplexity() * 20.0F;
      if (weight * (qlevel / 10.0F) <= 2000.0F)
      {
        performer.getStatus().modifyThirst((int)(-(weight * qlevel / 20000.0F) * 6554.0F), drink
          .getCaloriesByWeight(weight) * complexity, drink
          .getCarbsByWeight(weight) * complexity, drink
          .getFatsByWeight(weight) * complexity, drink
          .getProteinsByWeight(weight) * complexity);
        
        AffinitiesTimed.addTimedAffinityFromBonus(performer, weight, drink);
        done = true;
        Items.destroyItem(drink.getWurmId());
        if (drink.isSource()) {
          performer.modifyKarma(weight);
        }
      }
      else
      {
        if ((drink.isMilk()) && (drink.isZombiefied()) && (performer.getStatus().getThirst() > 1000)) {
          if ((performer.getKingdomId() == 3) && (Server.rand.nextInt(10) == 0)) {
            performer.healRandomWound((int)(qlevel / 10.0F));
          }
        }
        performer.getStatus().modifyThirst((int)(-6554.0F * qlevel / 100.0F), drink
          .getCaloriesByWeight(200) * complexity, drink
          .getCarbsByWeight(200) * complexity, drink
          .getFatsByWeight(200) * complexity, drink
          .getProteinsByWeight(200) * complexity);
        
        AffinitiesTimed.addTimedAffinityFromBonus(performer, 200, drink);
        drink.setWeight(weight - 200, false);
        if (drink.isSource()) {
          performer.modifyKarma(200);
        }
        if (drink.getWeightGrams() <= 0) {
          Items.destroyItem(drink.getWurmId());
        }
      }
      if (act.mayPlaySound()) {
        SoundPlayer.playSound("sound.liquid.drink", performer, 1.0F);
      }
    }
    if (performer.getStatus().getThirst() <= 1) {
      done = true;
    }
    return done;
  }
  
  public static final void addAlcohol(Creature performer)
  {
    if (performer.isPlayer())
    {
      Player player = (Player)performer;
      player.setAlcohol(((Player)performer).getAlcohol() + 3.0F);
      if (player.getAlcohol() > 20.0F) {
        if (player.getSaveFile().setAlcoholTime(player.getAlcoholAddiction() + 10L)) {
          player.getCommunicator().sendNormalServerMessage("You have just received the title '" + Titles.Title.Alcoholic.getName(player.isNotFemale()) + "'!");
        }
      }
      if (player.getAlcohol() == 100.0F)
      {
        performer.getCommunicator().sendNormalServerMessage("You made it to the top! You are perfectly drunk!");
        Server.getInstance().broadCastAction(performer.getName() + " hits the record in drunkenness! " + performer.getHeSheItString() + " is perfectly drunk and can't drink any more!", performer, 3);
        player.addTitle(Titles.Title.Drunkard);
        player.achievement(296);
      }
      else if (player.getAlcohol() >= 95.0F)
      {
        performer.getCommunicator().sendNormalServerMessage("You are setting some kind of record.");
        Server.getInstance().broadCastAction(performer.getName() + " has that dead, watery look in " + performer.getHisHerItsString() + " eyes now.", performer, 3);
      }
      else if (player.getAlcohol() >= 90.0F)
      {
        performer.getCommunicator().sendNormalServerMessage("You can barely walk.");
        Server.getInstance().broadCastAction(performer.getName() + " looks concentrated and sways back and forth.", performer, 3);
        performer.getMovementScheme().setDrunkMod(true);
      }
      else if (player.getAlcohol() >= 60.0F)
      {
        performer.getCommunicator().sendNormalServerMessage("You are really really drunk.");
        Server.getInstance().broadCastAction(performer.getName() + " is starting to drool.", performer, 3);
      }
      else if (player.getAlcohol() >= 30.0F)
      {
        performer.getCommunicator().sendNormalServerMessage("You are drunk.");
        Server.getInstance().broadCastAction(performer.getName() + " is verifyibly drunk now.", performer, 3);
      }
      else if (player.getAlcohol() >= 20.0F)
      {
        performer.getCommunicator().sendNormalServerMessage("You are getting drunk.");
        Server.getInstance().broadCastAction(performer.getName() + " suddenly giggles uncontrollably.", performer, 3);
      }
      else if (player.getAlcohol() >= 10.0F)
      {
        performer.getCommunicator().sendNormalServerMessage("You are tipsy.");
      }
    }
  }
  
  static boolean drink(Creature performer, int tilex, int tiley, int tile, float counter, Action act)
  {
    boolean done = false;
    if ((performer.isPlayer()) && (((Player)performer).getAlcohol() > 90.0F)) {
      if (Server.rand.nextInt(100) < ((Player)performer).getAlcohol())
      {
        performer.getCommunicator().sendNormalServerMessage("You fall into the water and crawl back up all soaked. Who gives a damn?");
        
        SoundPlayer.playSound("sound.fish.splash", performer, 10.0F);
        return true;
      }
    }
    if (counter == 1.0F)
    {
      int time = 10;
      time = (int)(performer.getStatus().getThirst() / 6554.0F) * 10;
      if (WaterType.isBrackish(tilex, tiley, performer.isOnSurface()))
      {
        Server.getInstance().broadCastAction(performer.getNameWithGenus() + " drinks some salty water.", performer, 3);
        performer.getCommunicator().sendNormalServerMessage("The water is slightly salty but still cools you down.");
      }
      else
      {
        Server.getInstance().broadCastAction(performer.getNameWithGenus() + " drinks some water.", performer, 3);
        performer.getCommunicator().sendNormalServerMessage("The water is refreshing and it cools you down.");
      }
      performer.sendActionControl("Drinking water", true, time);
    }
    performer.getStatus().modifyThirst(-6554.0F);
    if (act.mayPlaySound()) {
      SoundPlayer.playSound("sound.liquid.drink", performer, 10.0F);
    }
    if (performer.getStatus().getThirst() <= 1) {
      done = true;
    }
    return done;
  }
  
  static boolean startDragging(Action act, Creature performer, Item dragged)
  {
    if (dragged.isVehicle())
    {
      Vehicle vehic = Vehicles.getVehicle(dragged);
      if (vehic.pilotId != -10L)
      {
        performer.getCommunicator().sendNormalServerMessage("The " + dragged
          .getName() + " can not be dragged right now.");
        return true;
      }
      if (dragged.isMooredBoat())
      {
        performer.getCommunicator().sendNormalServerMessage("The " + dragged
          .getName() + " is moored and can not be dragged right now.");
        return true;
      }
      if (dragged.getWurmId() == performer.getVehicle())
      {
        performer.getCommunicator().sendNormalServerMessage("You can not drag the " + dragged.getName() + " now.");
        return true;
      }
      if ((dragged.getLockId() != -10L) && (VehicleBehaviour.hasKeyForVehicle(performer, dragged)) && 
        (!VehicleBehaviour.mayDriveVehicle(performer, dragged, act)))
      {
        performer.getCommunicator().sendNormalServerMessage("You are not allowed to drag the " + dragged
          .getName() + ".");
        return true;
      }
      if ((vehic.draggers != null) && (vehic.draggers.size() > 0))
      {
        performer.getCommunicator().sendNormalServerMessage("The " + dragged
          .getName() + " won't budge since it is already dragged.");
        return true;
      }
      if ((performer.isPlayer()) && 
        (System.currentTimeMillis() - ((Player)performer).lastStoppedDragging < 2000L))
      {
        performer.getCommunicator().sendNormalServerMessage("You need to take a breath first.");
        return true;
      }
    }
    if (dragged.getTemplateId() == 445) {
      if ((Servers.localServer.PVPSERVER) && (performer.getBodyStrength().getRealKnowledge() < 21.0D))
      {
        performer.getCommunicator().sendNormalServerMessage("The " + dragged.getName() + " is too heavy.");
        return true;
      }
    }
    if (!performer.isWithinDistanceTo(dragged.getPosX(), dragged.getPosY(), dragged.getPosZ(), 4.0F))
    {
      performer.getCommunicator().sendNormalServerMessage("You are too far away.");
      return true;
    }
    if (!Items.isItemDragged(dragged))
    {
      if (checkIfStealing(dragged, performer, act))
      {
        if (Action.checkLegalMode(performer)) {
          return true;
        }
        if (!performer.maySteal())
        {
          performer.getCommunicator().sendNormalServerMessage("You need more body control to steal things.");
          return true;
        }
        if (dragged.getItems().size() > 0)
        {
          performer.getCommunicator().sendNormalServerMessage("You must empty the " + dragged
            .getName() + " before you steal it.");
          return true;
        }
        if (setTheftEffects(performer, act, dragged)) {
          return true;
        }
      }
      if ((dragged.isBoat()) && (dragged.getCurrentQualityLevel() < 10.0F))
      {
        performer.getCommunicator().sendNormalServerMessage("The " + dragged
          .getName() + " is in too poor shape to be used.");
        return true;
      }
      TileEvent.log(dragged.getTileX(), dragged.getTileY(), dragged.isOnSurface() ? 0 : -1, performer.getWurmId(), 74);
      
      Items.startDragging(performer, dragged);
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("That item is already being dragged by someone.");
    }
    return true;
  }
  
  public static boolean stopDragging(Creature performer, Item dragged)
  {
    BlockingResult result = Blocking.getBlockerBetween(performer, dragged, 4);
    if (result == null) {
      if (performer.getDraggedItem() == dragged)
      {
        Items.stopDragging(dragged);
        if (performer.getVisionArea() != null) {
          performer.getVisionArea().broadCastUpdateSelectBar(dragged.getWurmId());
        }
      }
    }
    return true;
  }
  
  static final boolean yoyo(Creature performer, Item yoyo, float counter, Action act)
  {
    boolean toReturn = false;
    Skills skills = performer.getSkills();
    Skill yoyosk = null;
    String succ = "but fail";
    String point = ".";
    String trick = "hurricane";
    double diff = 5.0D;
    double check = 0.0D;
    try
    {
      yoyosk = skills.getSkill(10050);
    }
    catch (NoSuchSkillException nsk)
    {
      yoyosk = skills.learn(10050, 1.0F);
    }
    if (counter == 1.0F)
    {
      performer.getCommunicator().sendNormalServerMessage("You start to spin your " + yoyo.getName() + ".");
      Server.getInstance().broadCastAction(performer
        .getName() + " starts to spin " + performer.getHisHerItsString() + " " + yoyo.getName() + ".", performer, 5);
      
      performer.sendActionControl(Actions.actionEntrys['¾'].getVerbString(), true, 800);
    }
    else if (act.currentSecond() == 5)
    {
      performer.getCommunicator().sendNormalServerMessage("The " + yoyo.getName() + " has good speed now.");
      Server.getInstance().broadCastAction("The " + yoyo.getName() + " has good speed now.", performer, 5);
    }
    else if (act.currentSecond() == 10)
    {
      diff = 5 - yoyo.getRarity();
      check = yoyosk.skillCheck(diff, yoyo, 0.0D, false, 10.0F);
      trick = "sun";
    }
    else if (act.currentSecond() == 20)
    {
      diff = 10 - yoyo.getRarity();
      check = yoyosk.skillCheck(diff, yoyo, 0.0D, false, 10.0F);
      trick = "grind";
    }
    else if (act.currentSecond() == 30)
    {
      diff = 20 - yoyo.getRarity();
      check = yoyosk.skillCheck(diff, yoyo, 0.0D, false, 10.0F);
      trick = "cradle";
    }
    else if (act.currentSecond() == 40)
    {
      diff = 40 - yoyo.getRarity();
      check = yoyosk.skillCheck(diff, yoyo, 0.0D, false, 10.0F);
      trick = "suicide basilisk";
    }
    else if (act.currentSecond() == 50)
    {
      diff = 60.0F - yoyo.getRarity() * 1.5F;
      check = yoyosk.skillCheck(diff, yoyo, 0.0D, false, 10.0F);
      trick = "whip";
    }
    else if (act.currentSecond() == 60)
    {
      diff = 70 - yoyo.getRarity() * 2;
      check = yoyosk.skillCheck(diff, yoyo, 0.0D, false, 10.0F);
      trick = "orbit";
    }
    else if (act.currentSecond() == 70)
    {
      diff = 80.0F - yoyo.getRarity() * 2.5F;
      check = yoyosk.skillCheck(diff, yoyo, 0.0D, false, 10.0F);
      trick = "orbit over back";
    }
    else if (act.currentSecond() == 80)
    {
      diff = 90 - yoyo.getRarity() * 3;
      check = yoyosk.skillCheck(diff, yoyo, 0.0D, false, 10.0F);
      trick = "dragon knot";
      toReturn = true;
    }
    boolean destroyed = false;
    if (check != 0.0D)
    {
      if (check > 0.0D)
      {
        succ = "and succeed";
        point = "!";
      }
      else
      {
        toReturn = true;
      }
      performer.getCommunicator().sendNormalServerMessage("You try the '" + trick + "' " + succ + point);
      Server.getInstance().broadCastAction(performer.getName() + " tries the " + trick + " " + succ + "s" + point, performer, 5);
      if (yoyo.setDamage(yoyo.getDamage() + 0.005F * yoyo.getDamageModifier()))
      {
        destroyed = true;
        toReturn = true;
      }
    }
    if ((!destroyed) && (toReturn))
    {
      performer.getCommunicator().sendNormalServerMessage("You reel the " + yoyo.getName() + " in.");
      Server.getInstance().broadCastAction(performer.getName() + " reels the " + yoyo.getName() + " in.", performer, 5);
    }
    return toReturn;
  }
  
  public static final int getImproveTemplateId(Item item)
  {
    if (item.isNoImprove()) {
      return -10;
    }
    byte material = getImproveMaterial(item);
    if (material == 0) {
      return -10;
    }
    return Materials.getTemplateIdForMaterial(material);
  }
  
  public static final int getImproveSkill(Item item)
  {
    int material = item.getMaterial();
    if (material == 0) {
      return -10;
    }
    CreationEntry entry = CreationMatrix.getInstance().getCreationEntry(item.getTemplateId());
    if (entry != null)
    {
      if ((item.getTemplateId() == 623) && ((material == 7) || (material == 8) || (material == 96))) {
        return 10043;
      }
      return entry.getPrimarySkill();
    }
    if ((item.getTemplateId() == 430) || (item.getTemplateId() == 528) || 
      (item.getTemplateId() == 638)) {
      return 1013;
    }
    if (item.getTemplate().isStatue()) {
      return 10074;
    }
    return -10;
  }
  
  private static final int getImproveSkill(byte material, int templateId)
  {
    if (material == 0) {
      return -10;
    }
    CreationEntry entry = CreationMatrix.getInstance().getCreationEntry(templateId);
    if (entry != null) {
      return entry.getPrimarySkill();
    }
    if ((templateId == 430) || (templateId == 528) || (templateId == 638)) {
      return 1013;
    }
    return -10;
  }
  
  static void triggerImproveAchievements(Creature performer, Item target, Skill improve, boolean wasHighest, float oldQL)
  {
    performer.achievement(205);
    if ((target.getQualityLevel() > improve.getKnowledge(0.0D)) && (improve.getKnowledge(0.0D) > 30.0D)) {
      performer.achievement(217);
    }
    if ((!wasHighest) && 
      (Items.isHighestQLForTemplate(target.getTemplateId(), target.getQualityLevel(), target.getWurmId(), false))) {
      performer.achievement(317);
    }
    if ((target.getQualityLevel() >= 99.0F) && (oldQL < 99.0F))
    {
      performer.achievement(222);
    }
    else if ((target.getQualityLevel() >= 90.0F) && (oldQL < 90.0F))
    {
      performer.achievement(221);
    }
    else if ((target.getQualityLevel() >= 70.0F) && (oldQL < 70.0F))
    {
      performer.achievement(220);
      if (target.isTool()) {
        performer.achievement(565);
      }
    }
    else if ((target.getQualityLevel() >= 50.0F) && (oldQL < 50.0F))
    {
      performer.achievement(219);
      if (target.isTool()) {
        performer.achievement(543);
      }
      if (target.isWeapon()) {
        performer.achievement(554);
      }
    }
    else if ((target.getQualityLevel() >= 15.0F) && (oldQL < 15.0F))
    {
      if (target.isMetal()) {
        performer.achievement(520);
      }
    }
    if ((target.isArmour()) && (target.getQualityLevel() >= 80.0F) && (oldQL < 80.0F)) {
      performer.achievement(597);
    }
  }
  
  static final boolean improveItem(Action act, Creature performer, Item source, Item target, float counter)
  {
    boolean toReturn = false;
    
    boolean insta = performer.getPower() >= 5;
    if ((counter == 0.0F) || (counter == 1.0F) || (act.justTickedSecond()))
    {
      if (source.getWurmId() == target.getWurmId())
      {
        performer.getCommunicator().sendNormalServerMessage("You cannot improve the " + target
          .getName() + " using itself as a tool.");
        return true;
      }
      if (!target.isRepairable())
      {
        performer.getCommunicator().sendNormalServerMessage("You cannot improve that item.");
        return true;
      }
      if (target.getParentId() != -10L) {
        try
        {
          ItemTemplate temp = target.getRealTemplate();
          if ((temp != null) && (!temp.isVehicle()))
          {
            Item parent = target.getParent();
            if ((parent.getSizeX() < temp.getSizeX()) || (parent.getSizeY() < temp.getSizeY()) || 
              (parent.getSizeZ() <= temp.getSizeZ())) {
              if ((parent.getTemplateId() != 177) && (parent.getTemplateId() != 0))
              {
                performer.getCommunicator().sendNormalServerMessage("It's too tight to try and work on the " + target
                  .getName() + " in the " + parent
                  .getName() + ".");
                return true;
              }
            }
          }
        }
        catch (NoSuchItemException localNoSuchItemException) {}
      }
      if (target.creationState != 0)
      {
        performer.getCommunicator().sendNormalServerMessage("You can not improve the " + target
          .getName() + " by adding more material right now.");
        return true;
      }
      if (!insta)
      {
        if (target.getDamage() > 0.0F)
        {
          performer.getCommunicator().sendNormalServerMessage("Repair the " + target
            .getName() + " before you try to improve it.");
          return true;
        }
        if ((target.isMetal()) && (!target.isNoTake())) {
          if (target.getTemperature() < 3500)
          {
            performer.getCommunicator().sendNormalServerMessage("Metal needs to be glowing hot while smithing.");
            return true;
          }
        }
        if ((source.isCombine()) && (source.isMetal())) {
          if (source.getTemperature() < 3500)
          {
            performer.getCommunicator().sendNormalServerMessage("Metal needs to be glowing hot while smithing.");
            return true;
          }
        }
      }
    }
    Skills skills = performer.getSkills();
    Skill improve = null;
    int skillNum = getImproveSkill(target);
    if ((skillNum == -10) || (target.isNewbieItem()) || (target.isChallengeNewbieItem()))
    {
      performer.getCommunicator().sendNormalServerMessage("You cannot improve that item.");
      return true;
    }
    int time = 1000;
    int templateId = getImproveTemplateId(target);
    if (source.getTemplateId() == templateId)
    {
      try
      {
        improve = skills.getSkill(skillNum);
      }
      catch (NoSuchSkillException nss)
      {
        improve = skills.learn(skillNum, 1.0F);
      }
      Skill secondarySkill = null;
      try
      {
        secondarySkill = skills.getSkill(source.getPrimarySkill());
      }
      catch (Exception ex)
      {
        try
        {
          secondarySkill = skills.learn(source.getPrimarySkill(), 1.0F);
        }
        catch (Exception localException1) {}
      }
      double power = 0.0D;
      double bonus = 0.0D;
      if (performer.isPriest()) {
        bonus = -20.0D;
      }
      float runeModifier = 1.0F;
      if (target.getSpellEffects() != null) {
        runeModifier = target.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_IMPPERCENT);
      }
      float imbueEnhancement = 1.0F + source.getSkillSpellImprovement(skillNum) / 100.0F;
      double improveBonus = 0.23047D * imbueEnhancement * runeModifier;
      float improveItemBonus = ItemBonus.getImproveSkillMaxBonus(performer);
      double max = improve.getKnowledge(0.0D) * improveItemBonus + (100.0D - improve.getKnowledge(0.0D) * improveItemBonus) * improveBonus;
      
      double diff = Math.max(0.0D, max - target.getQualityLevel());
      float skillgainMod = 1.0F;
      if (diff <= 0.0D) {
        skillgainMod = 2.0F;
      }
      if (counter == 1.0F)
      {
        if ((source.isCombine()) || (templateId == 9)) {
          if (source.getCurrentQualityLevel() <= target.getQualityLevel())
          {
            performer.getCommunicator().sendNormalServerMessage("The " + source
              .getName() + " is in too poor shape to improve the " + target.getName() + ".");
            return true;
          }
        }
        performer.getCommunicator().sendNormalServerMessage("You start to improve the " + target.getName() + ".");
        Server.getInstance().broadCastAction(performer
          .getName() + " starts to improve " + target.getNameWithGenus() + ".", performer, 5);
        
        time = Actions.getImproveActionTime(performer, source);
        performer.sendActionControl(Actions.actionEntrys['À'].getVerbString(), true, time);
        act.setTimeLeft(time);
        if ((performer.getDeity() != null) && (performer.getDeity().isRepairer()) && 
          (performer.getFaith() >= 80.0F) && (performer.getFavor() >= 40.0F)) {
          bonus += 10.0D;
        }
        power = improve.skillCheck(target.getQualityLevel(), source, bonus, true, 1.0F);
        
        double mod = (100.0F - target.getQualityLevel()) / 20.0F / 100.0F * (Server.rand.nextFloat() + Server.rand.nextFloat() + Server.rand.nextFloat() + Server.rand.nextFloat()) / 2.0F;
        if (power < 0.0D)
        {
          act.setFailSecond((int)Math.max(20.0F, time * Server.rand.nextFloat()));
          act.setPower((float)(-mod * Math.max(1.0D, diff)));
        }
        else
        {
          if (diff <= 0.0D) {
            mod *= 0.009999999776482582D;
          }
          double regain = 1.0D;
          if (target.getQualityLevel() < target.getOriginalQualityLevel()) {
            regain = 2.0D;
          }
          diff *= regain;
          int tid = target.getTemplateId();
          if ((target.isArmour()) || (target.isCreatureWearableOnly()) || (target.isWeapon()) || (target.isShield()) || (tid == 455) || (tid == 454) || (tid == 456) || (tid == 453) || (tid == 451) || (tid == 452)) {
            mod *= 2.0D;
          }
          if ((tid == 455) || (tid == 454) || (tid == 456) || (tid == 453) || (tid == 451) || (tid == 452)) {
            mod *= 2.0D;
          }
          Titles.Title title = performer.getTitle();
          if ((title != null) && (title.getSkillId() == improve.getNumber()) && (
            (target.isArmour()) || (target.isCreatureWearableOnly()))) {
            mod *= 1.2999999523162842D;
          }
          if (target.getRarity() > 0) {
            mod *= (1.0F + target.getRarity() * 0.1F);
          }
          act.setPower((float)(mod * Math.max(1.0D, diff)));
        }
      }
      else
      {
        time = act.getTimeLeft();
        float failsec = act.getFailSecond();
        power = act.getPower();
        if (counter >= failsec)
        {
          if (secondarySkill != null) {
            bonus = Math.max(bonus, secondarySkill
            
              .skillCheck(target.getQualityLevel(), source, bonus, false, performer
              .isPriest() ? counter / 3.0F : counter / 2.0F));
          }
          if (performer.isPriest()) {
            bonus = Math.min(bonus, 0.0D);
          }
          improve.skillCheck(target.getQualityLevel(), source, bonus, false, performer.isPriest() ? counter / 2.0F : counter);
          if (power != 0.0D)
          {
            if (!target.isBodyPart()) {
              if (!target.isLiquid())
              {
                target.setDamage(target.getDamage() - act.getPower());
                performer.getCommunicator().sendNormalServerMessage("You damage the " + target
                  .getName() + " a little.");
                Server.getInstance().broadCastAction(performer
                  .getName() + " grunts as " + performer.getHeSheItString() + " damages " + target
                  .getNameWithGenus() + " a little.", performer, 5);
              }
              else
              {
                performer.getCommunicator().sendNormalServerMessage("You fail.");
                Server.getInstance().broadCastAction(performer
                  .getName() + " grunts as " + performer.getHeSheItString() + " fails.", performer, 5);
              }
            }
          }
          else
          {
            performer.getCommunicator().sendNormalServerMessage("You realize you almost damaged the " + target
              .getName() + " and stop.");
            Server.getInstance().broadCastAction(performer
              .getName() + " stops improving " + target.getNameWithGenus() + ".", performer, 5);
          }
          performer.getStatus().modifyStamina(-counter * 1000.0F);
          return true;
        }
      }
      if (act.mayPlaySound()) {
        sendImproveSound(performer, source, target, skillNum);
      }
      if ((counter * 10.0F > time) || (insta))
      {
        if (act.getRarity() != 0) {
          performer.playPersonalSound("sound.fx.drumroll");
        }
        float maxGain = 1.0F;
        byte rarity = target.getRarity();
        
        float rarityChance = 0.2F;
        if (target.getSpellEffects() != null) {
          rarityChance *= target.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RARITYIMP);
        }
        if ((act.getRarity() > rarity) && 
          (Server.rand.nextFloat() <= rarityChance)) {
          rarity = act.getRarity();
        }
        float switchImproveChance = 1.0F;
        if ((source.isCombine()) || (source.getTemplateId() == 9) || (source.getTemplateId() == 72) || 
          (source.isDragonArmour()))
        {
          float mod = 0.05F;
          if ((Servers.localServer.EPIC) && (source.isDragonArmour())) {
            mod = 0.01F;
          }
          int usedWeight = (int)Math.min(500.0F, Math.max(1.0F, target.getWeightGrams() * mod));
          if (source.getWeightGrams() < Math.min(source.getTemplate().getWeightGrams(), usedWeight))
          {
            maxGain = Math.min(1.0F, source.getWeightGrams() / usedWeight);
            switchImproveChance = source.getWeightGrams() / usedWeight;
          }
          source.setWeight(source.getWeightGrams() - usedWeight, true);
          if ((source.deleted) && 
            (source.getRarity() > rarity) && 
            (Server.rand.nextInt(100) == 0)) {
            rarity = source.getRarity();
          }
        }
        else if ((!source.isBodyPart()) && (!source.isLiquid()))
        {
          source.setDamage(source.getDamage() + 5.0E-4F * source.getDamageModifier());
        }
        if (secondarySkill != null) {
          bonus = Math.max(bonus, secondarySkill
          
            .skillCheck(target.getQualityLevel(), source, bonus, false, skillgainMod * (performer
            .isPriest() ? counter / 3.0F : counter / 2.0F)));
        }
        if (performer.isPriest()) {
          bonus = Math.min(bonus, 0.0D);
        }
        improve.skillCheck(target.getQualityLevel(), source, bonus, false, skillgainMod * (performer
          .isPriest() ? counter / 2.0F : counter));
        
        power = act.getPower();
        if (power > 0.0D)
        {
          performer.getCommunicator().sendNormalServerMessage("You improve the " + target.getName() + " a bit.");
          if (insta) {
            performer.getCommunicator().sendNormalServerMessage("before: " + target
              .getQualityLevel() + " now: " + (target.getQualityLevel() + power) + " power=" + power);
          }
          if (Servers.isThisATestServer()) {
            performer.getCommunicator().sendNormalServerMessage("switchImproveChance = " + switchImproveChance);
          }
          Server.getInstance().broadCastAction(performer
            .getName() + " improves " + target.getNameWithGenus() + " a bit.", performer, 5);
          
          byte newState = 0;
          if (switchImproveChance >= Server.rand.nextFloat()) {
            newState = (byte)Server.rand.nextInt(5);
          }
          if (Server.rand.nextFloat() * 20.0F > target.getQualityLevel()) {
            newState = 0;
          }
          Item toRarify = target;
          if (target.getTemplateId() == 128) {
            toRarify = source;
          }
          if (rarity > toRarify.getRarity())
          {
            toRarify.setRarity(rarity);
            for (Item sub : toRarify.getItems()) {
              if (sub != null) {
                if (sub.isComponentItem()) {
                  sub.setRarity(rarity);
                }
              }
            }
            if (toRarify.getRarity() > 2) {
              performer.achievement(300);
            } else if (toRarify.getRarity() == 1) {
              performer.achievement(301);
            } else if (toRarify.getRarity() == 2) {
              performer.achievement(302);
            }
          }
          if (newState != 0)
          {
            target.setCreationState(newState);
            String newString = getNeededCreationAction(getImproveMaterial(target), newState, target);
            performer.getCommunicator().sendNormalServerMessage(newString);
          }
          else if (skillNum != -10)
          {
            try
            {
              ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(templateId);
              performer.getCommunicator()
                .sendNormalServerMessage("The " + target
                .getName() + " could be improved with some more " + temp
                .getName() + ".");
            }
            catch (NoSuchTemplateException localNoSuchTemplateException1) {}
          }
          boolean wasHighest = Items.isHighestQLForTemplate(target.getTemplateId(), target.getQualityLevel(), target
            .getWurmId(), true);
          float oldQL = target.getQualityLevel();
          float modifier = 1.0F;
          if (target.getSpellEffects() != null) {
            modifier = target.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_IMPQL);
          }
          modifier *= target.getMaterialImpBonus();
          target.setQualityLevel(Math.min(100.0F, (float)(target.getQualityLevel() + power * maxGain * modifier)));
          if (target.getQualityLevel() > target.getOriginalQualityLevel())
          {
            target.setOriginalQualityLevel(target.getQualityLevel());
            triggerImproveAchievements(performer, target, improve, wasHighest, oldQL);
          }
        }
        else
        {
          if (insta) {
            performer.getCommunicator().sendNormalServerMessage("Dam before: " + target
              .getDamage() + " now: " + (target.getDamage() - power) + " power=" + power);
          }
          if (!target.isBodyPart()) {
            if (!target.isLiquid())
            {
              target.setDamage(target.getDamage() - (float)power);
              
              performer.getCommunicator().sendNormalServerMessage("You damage the " + target
                .getName() + " a little.");
              Server.getInstance().broadCastAction(performer
                .getName() + " grunts as " + performer.getHeSheItString() + " damages " + target
                .getNameWithGenus() + " a little.", performer, 5);
              performer.achievement(206);
            }
            else
            {
              performer.getCommunicator().sendNormalServerMessage("You fail.");
              Server.getInstance().broadCastAction(performer
                .getName() + " grunts as " + performer.getHeSheItString() + " fails.", performer, 5);
            }
          }
        }
        performer.getStatus().modifyStamina(-counter * 1000.0F);
        toReturn = true;
      }
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("You cannot improve the item with that.");
      toReturn = true;
    }
    return toReturn;
  }
  
  public static final boolean destroyItem(int action, Creature performer, Item destroyItem, Item target, boolean dealItems, float counter)
  {
    if (!Methods.isActionAllowed(performer, (short)action, target)) {
      return true;
    }
    if (target.getTemplateId() == 1432) {
      for (Item item : target.getItemsAsArray()) {
        if (item.getTemplateId() == 1436) {
          if (!item.isEmpty(true))
          {
            performer.getCommunicator().sendNormalServerMessage("You cannot destroy this coop with a creature inside.");
            
            return true;
          }
        }
      }
    }
    boolean toReturn = true;
    boolean ok = false;
    if (!target.isTraded())
    {
      if (target.getOwnerId() == performer.getWurmId()) {
        ok = true;
      } else if (target.getZoneId() != -10L) {
        if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 4.0F))
        {
          ok = true;
          if (((target.isKingdomMarker()) && (target.getKingdom() == performer.getKingdomId())) || (
            (target.isTent()) && (!Servers.localServer.PVPSERVER))) {
            if (performer.getWurmId() != target.lastOwner)
            {
              VolaTile t = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target
                .isOnSurface());
              if ((t == null) || (t.getVillage() == null) || (t.getVillage() != performer.getCitizenVillage()))
              {
                performer.getCommunicator().sendNormalServerMessage("You are not allowed to destroy the " + target
                  .getName() + ".");
                return true;
              }
            }
          }
          if ((target.isGuardTower()) && (target.getKingdom() != performer.getKingdomId()))
          {
            GuardTower tower = Kingdoms.getTower(target);
            if (tower.getGuardCount() > 0)
            {
              performer.getCommunicator().sendNormalServerMessage("A nearby guard stops you with a warning.");
              return true;
            }
          }
        }
        else
        {
          performer.getCommunicator().sendNormalServerMessage("You are too far away to do that.");
          return true;
        }
      }
    }
    else {
      ok = false;
    }
    if (!ok) {
      return true;
    }
    int time = 1000;
    
    boolean insta = (performer.getPower() >= 2) && (destroyItem.isWand());
    float mod = target.getDamageModifierForItem(destroyItem);
    if ((mod <= 0.0F) && (!insta))
    {
      performer.getCommunicator().sendNormalServerMessage("You will not do any damage to the " + target
        .getName() + " with that.");
      return true;
    }
    toReturn = false;
    Action act = null;
    String destString = "destroy";
    if (dealItems) {
      destString = "disassemble";
    }
    if (action == 757) {
      destString = "pry";
    }
    try
    {
      act = performer.getCurrentAction();
    }
    catch (NoSuchActionException nsa)
    {
      logger.log(Level.WARNING, "No Action for " + performer.getName() + "!", nsa);
      return true;
    }
    if (counter == 1.0F)
    {
      time = 300;
      if (action == 757) {
        time = 200;
      }
      if (Servers.localServer.isChallengeServer()) {
        if (!target.isEnchantedTurret()) {
          time /= 2;
        } else {
          time /= 3;
        }
      }
      performer.getCommunicator().sendNormalServerMessage("You start to " + destString + " the " + target
        .getName() + ".");
      Server.getInstance().broadCastAction(performer
        .getName() + " starts to " + destString + " " + target.getNameWithGenus() + ".", performer, 5);
      
      performer.sendActionControl(Actions.actionEntrys[action].getVerbString(), true, time);
      act.setTimeLeft(time);
      performer.getStatus().modifyStamina(-800.0F);
    }
    else
    {
      time = act.getTimeLeft();
    }
    if (act.currentSecond() % 5 == 0)
    {
      String s = "sound.destroyobject.wood.axe";
      if (destroyItem.isWeaponCrush()) {
        s = "sound.destroyobject.wood.maul";
      }
      if (target.isStone()) {
        if (destroyItem.isWeaponCrush()) {
          s = "sound.destroyobject.stone.maul";
        } else {
          s = "sound.destroyobject.stone.axe";
        }
      }
      if (target.isMetal()) {
        if (destroyItem.isWeaponCrush()) {
          s = "sound.destroyobject.metal.maul";
        } else {
          s = "sound.destroyobject.metal.axe";
        }
      }
      SoundPlayer.playSound(s, target, 0.5F);
      performer.getStatus().modifyStamina(-5000.0F);
      if ((destroyItem != null) && 
        (!destroyItem.isBodyPartAttached())) {
        destroyItem.setDamage(destroyItem.getDamage() + mod * destroyItem.getDamageModifier());
      }
    }
    if ((counter * 10.0F > time) || (insta))
    {
      Skills skills = performer.getSkills();
      Skill destroySkill = null;
      try
      {
        destroySkill = skills.getSkill(102);
      }
      catch (NoSuchSkillException nss)
      {
        destroySkill = skills.learn(102, 1.0F);
      }
      destroySkill.skillCheck(20.0D, destroyItem, 0.0D, false, Math.min(10.0F, counter));
      
      double damage = 0.0D;
      if ((insta) && (mod <= 0.0F))
      {
        damage = 100.0D;
        mod = 1.0F;
      }
      else
      {
        damage = Weapon.getModifiedDamageForWeapon(destroyItem, destroySkill) * 50.0D;
        damage /= target.getQualityLevel() / 10.0F;
        if ((target.getTemplateId() == 445) || (target.getTemplateId() == 1125)) {
          damage *= 50.0D;
        } else if (target.getTemplateId() == 937) {
          damage *= 25.0D;
        } else if (Servers.localServer.isChallengeServer()) {
          damage *= 5.0D;
        }
      }
      VolaTile tile = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface());
      if (target.isKingdomMarker())
      {
        if (target.getKingdom() != performer.getKingdomId())
        {
          mod *= 0.75F;
          if (!Features.Feature.TOWER_CHAINING.isEnabled())
          {
            GuardTower t = Kingdoms.getTower(target);
            if (t != null) {
              t.sendAttackWarning();
            }
          }
        }
        else if (performer.getWurmId() == target.getLastOwnerId())
        {
          mod = 2.0F;
        }
      }
      else if ((target.isRoadMarker()) && (action == 757) && (destroyItem.getTemplateId() == 1115))
      {
        damage *= 15.0D;
        mod *= 10.0F;
      }
      if ((tile != null) && (tile.getVillage() != null))
      {
        if (MethodsStructure.isCitizenAndMayPerformAction((short)83, performer, tile.getVillage()))
        {
          if ((!target.isKingdomMarker()) || (!Servers.localServer.PVPSERVER) || 
            (target.getLastOwnerId() == performer.getWurmId())) {
            damage *= 50.0D;
          }
        }
        else if (MethodsStructure.isAllyAndMayPerformAction((short)83, performer, tile.getVillage())) {
          if ((!target.isKingdomMarker()) || (!Servers.localServer.PVPSERVER) || 
            (target.getLastOwnerId() == performer.getWurmId())) {
            damage *= 25.0D;
          }
        }
      }
      else if (target.isStreetLamp()) {
        damage *= 20.0D;
      } else if ((target.isSign()) && (Servers.localServer.PVPSERVER)) {
        damage *= 10.0D;
      } else if (target.isMarketStall()) {
        damage *= 10.0D;
      } else if ((!target.isKingdomMarker()) && (!Servers.localServer.PVPSERVER) && (target.getLastOwnerId() == performer.getWurmId())) {
        damage *= 5.0D;
      }
      damage *= Weapon.getMaterialBashModifier(destroyItem.getMaterial());
      if ((performer.getCultist() != null) && (performer.getCultist().doubleStructDamage())) {
        damage *= 2.0D;
      }
      if (target.getTemplateId() == 521)
      {
        mod += 0.01F;
        if (Server.rand.nextInt(100) == 0) {
          if (target.getData1() > 0) {
            try
            {
              CreatureTemplate ctemplate = CreatureTemplateFactory.getInstance().getTemplate(target
                .getData1());
              byte sex = ctemplate.getSex();
              if ((sex == 0) && (!ctemplate.keepSex)) {
                if (Server.rand.nextInt(2) == 0) {
                  sex = 1;
                }
              }
              byte ctype = target.getAuxData();
              if (Server.rand.nextInt(40) == 0) {
                ctype = 99;
              }
              Creature.doNew(ctemplate.getTemplateId(), ctype, target.getPosX(), target.getPosY(), Server.rand
                .nextInt(360), target.isOnSurface() ? 0 : -1, "", sex);
            }
            catch (Exception ex)
            {
              logger.log(Level.WARNING, ex.getMessage(), ex);
            }
          }
        }
      }
      if (target.getTemplateId() == 731)
      {
        damage = 100.0D;
        mod = 1.0F;
      }
      float newDam = (float)(target.getDamage() + damage * mod);
      float oldDam = target.getDamage();
      
      String towerName = "Unknown";
      if ((Features.Feature.TOWER_CHAINING.isEnabled()) && (target.isKingdomMarker()))
      {
        GuardTower t = Kingdoms.getTower(target);
        if (t != null)
        {
          if (newDam >= 100.0F) {
            towerName = t.getName();
          }
          t.checkBashDamage(oldDam, newDam);
        }
      }
      if (target.isRoadMarker())
      {
        if (performer.fireTileLog()) {
          TileEvent.log(target.getTileX(), target.getTileY(), target.isOnSurface() ? 0 : -1, performer.getWurmId(), action);
        }
        if (newDam >= 100.0F) {
          target.setWhatHappened("bashed by " + performer.getName());
        }
      }
      if (target.setDamage(newDam))
      {
        performer.getCommunicator().sendNormalServerMessage("The " + target
          .getName() + " falls apart with a crash.");
        Server.getInstance().broadCastAction(performer
          .getName() + " damages " + target.getNameWithGenus() + " and it falls apart with a crash.", performer, 5);
        if ((target.isKingdomMarker()) && (Features.Feature.TOWER_CHAINING.isEnabled()))
        {
          Players.getInstance().broadCastDestroyInfo(performer, towerName + " has been destroyed.");
          Server.getInstance().broadCastEpicEvent(towerName + " has been destroyed.");
        }
        if (performer.getDeity() != null) {
          performer.performActionOkey(act);
        }
        MissionTriggers.activateTriggers(performer, destroyItem, 913, target.getWurmId(), (int)counter);
        if (target.getTemplateId() == 521) {
          if ((Servers.localServer.PVPSERVER) && (!Servers.isThisAChaosServer())) {
            performer.getFightingSkill().setKnowledge(performer
              .getFightingSkill().getRealKnowledge() + (100.0D - performer.getFightingSkill().getRealKnowledge()) * spawnDamageMod, false);
          }
        }
      }
      else
      {
        performer.getCommunicator().sendNormalServerMessage("You damage the " + target.getName() + ".");
        Server.getInstance().broadCastAction(performer.getName() + " damages " + target.getNameWithGenus() + ".", performer, 5);
      }
      toReturn = true;
    }
    return toReturn;
  }
  
  static final boolean filet(Action act, Creature performer, Item source, Item target, float counter)
  {
    boolean done = false;
    if ((target.isRoyal()) || (target.isIndestructible()))
    {
      performer.getCommunicator().sendNormalServerMessage("You mysteriously cut yourself when trying to filet that!");
      CombatEngine.addWound(performer, performer, (byte)1, 13, 2000 + Server.rand
        .nextInt(2000), 0.0F, "cut", null, 0.0F, 0.0F, false, false, false, false);
      
      return true;
    }
    int time = 200;
    if (target.getOwnerId() != performer.getWurmId())
    {
      performer.getCommunicator().sendNormalServerMessage("You need to be in possession of the " + target
        .getName() + " in order to filet it.");
      return true;
    }
    if (counter == 1.0F)
    {
      Skill butchering = performer.getSkills().getSkillOrLearn(10059);
      int nums = 2;
      int tid = 368;
      try
      {
        ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(tid);
        nums = target.getWeightGrams() / template.getWeightGrams();
        if (nums == 0)
        {
          performer.getCommunicator().sendNormalServerMessage("The " + target
            .getName() + " is too small to produce a filet.");
          done = true;
        }
      }
      catch (NoSuchTemplateException nst)
      {
        logger.log(Level.WARNING, "No template for filet?" + nst.getMessage(), nst);
        done = true;
      }
      if (!done)
      {
        performer.getCommunicator().sendNormalServerMessage("You start to filet the " + target.getName() + ".");
        Server.getInstance().broadCastAction(performer.getName() + " starts to filet a " + target.getName() + ".", performer, 5);
        
        time = Actions.getStandardActionTime(performer, butchering, source, 0.0D);
        act.setTimeLeft(time);
        performer.sendActionControl(Actions.actionEntrys['á'].getVerbString(), true, time);
        SoundPlayer.playSound("sound.butcherKnife", performer, 1.0F);
      }
    }
    else
    {
      time = act.getTimeLeft();
      if (act.currentSecond() % 5 == 0)
      {
        source.setDamage(source.getDamage() + 5.0E-4F * source.getDamageModifier());
        SoundPlayer.playSound("sound.butcherKnife", performer, 1.0F);
      }
    }
    if (counter * 10.0F >= time)
    {
      Skill butchering = performer.getSkills().getSkillOrLearn(10059);
      
      done = true;
      boolean filet = true;
      int nums = 2;
      try
      {
        int tid = 368;
        
        ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(tid);
        
        int tweight = template.getWeightGrams();
        nums = target.getWeightGrams() / tweight;
        if (nums == 0)
        {
          performer.getCommunicator().sendNormalServerMessage("The " + target
            .getName() + " is too small to produce a filet.");
          done = true;
          filet = false;
        }
        int invnums = performer.getInventory().getNumItemsNotCoins();
        if (invnums + nums >= 100)
        {
          performer.getCommunicator().sendNormalServerMessage("You can't make space in your inventory for the filets.");
          
          done = true;
          filet = false;
        }
        if (filet)
        {
          double bonus = 0.0D;
          try
          {
            int primarySkill = source.getPrimarySkill();
            Skill primskill = null;
            try
            {
              primskill = performer.getSkills().getSkill(primarySkill);
            }
            catch (Exception ex)
            {
              primskill = performer.getSkills().learn(primarySkill, 1.0F);
            }
            bonus = primskill.skillCheck(10.0D, 0.0D, false, counter);
          }
          catch (NoSuchSkillException localNoSuchSkillException) {}
          performer.getCommunicator().sendNormalServerMessage("You filet the " + target.getName() + ".");
          Server.getInstance().broadCastAction(performer.getName() + " filets " + target.getNameWithGenus() + ".", performer, 5);
          
          float ql = 0.0F;
          
          float imbueEnhancement = 1.0F;
          float max = target.getCurrentQualityLevel() * 1.0F;
          for (int x = 0; x < nums; x++)
          {
            ql = Math.max(1.0F, (float)butchering.skillCheck(target.getDamage(), source, bonus, nums > 10, 1.0F));
            try
            {
              Item fil = ItemFactory.createItem(tid, Math.min(max, ql), null);
              fil.setName("fillet of " + target.getActualName().toLowerCase());
              if (target.isMeat()) {
                fil.setMaterial(target.getMaterial());
              } else {
                fil.setRealTemplate(target.getTemplateId());
              }
              fil.setAuxData(target.getAuxData());
              if (target.getTemperature() > 200) {
                fil.setTemperature(target.getTemperature());
              }
              performer.getInventory().insertItem(fil);
            }
            catch (FailedException fe)
            {
              logger.log(Level.WARNING, performer.getName() + ":" + fe.getMessage(), fe);
            }
            catch (NoSuchTemplateException nst)
            {
              logger.log(Level.WARNING, "No template for filet?" + nst.getMessage(), nst);
            }
          }
          Items.destroyItem(target.getWurmId());
        }
      }
      catch (NoSuchTemplateException nst)
      {
        logger.log(Level.WARNING, "No template for filet?" + nst.getMessage(), nst);
        done = true;
      }
    }
    return done;
  }
  
  static final boolean filetFish(Action act, Creature performer, Item source, Item target, float counter)
  {
    if ((target.isRoyal()) || (target.isIndestructible()))
    {
      performer.getCommunicator().sendNormalServerMessage("You mysteriously cut yourself when trying to filet that!");
      CombatEngine.addWound(performer, performer, (byte)1, 13, 2000 + Server.rand
        .nextInt(2000), 0.0F, "cut", null, 0.0F, 0.0F, false, false, false, false);
      return true;
    }
    int time = 200;
    if (target.getOwnerId() != performer.getWurmId())
    {
      performer.getCommunicator().sendNormalServerMessage("You need to be in possession of the " + target
        .getName() + " in order to filet it.");
      return true;
    }
    if (counter == 1.0F)
    {
      try
      {
        ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(369);
        int tweight = template.getWeightGrams();
        int waste = tweight / 10;
        
        int nums = (target.getWeightGrams() + waste) / (tweight + waste);
        if (nums == 0)
        {
          performer.getCommunicator().sendNormalServerMessage("The " + target
            .getName() + " is too small to produce a filet.");
          return true;
        }
        time = nums * 10;
      }
      catch (NoSuchTemplateException nst)
      {
        performer.getCommunicator().sendNormalServerMessage("Something went horribly wrong with " + target
          .getName() + ". Please use /support.");
        logger.log(Level.WARNING, "No template for filet?" + nst.getMessage(), nst);
        return true;
      }
      performer.getCommunicator().sendNormalServerMessage("You start to filet the " + target.getName() + ".");
      Server.getInstance().broadCastAction(performer.getName() + " starts to filet a " + target.getName() + ".", performer, 5);
      
      act.setTimeLeft(time);
      performer.sendActionControl(Actions.actionEntrys['á'].getVerbString(), true, time);
      SoundPlayer.playSound("sound.butcherKnife", performer, 1.0F);
      return false;
    }
    time = act.getTimeLeft();
    if (act.mayPlaySound())
    {
      source.setDamage(source.getDamage() + 5.0E-4F * source.getDamageModifier());
      SoundPlayer.playSound("sound.butcherKnife", performer, 1.0F);
    }
    if (act.justTickedSecond())
    {
      int invnums = performer.getInventory().getNumItemsNotCoins();
      if (invnums + 1 >= 100)
      {
        performer.getCommunicator().sendNormalServerMessage("You can't make space in your inventory for the filet.");
        
        return true;
      }
      try
      {
        ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(369);
        int tweight = template.getWeightGrams();
        int waste = tweight / 10;
        
        int nums = (target.getWeightGrams() + waste) / (tweight + waste);
        if (nums == 0)
        {
          performer.getCommunicator().sendNormalServerMessage("The " + target
            .getName() + " is too small to produce a filet.");
          return true;
        }
      }
      catch (NoSuchTemplateException nst)
      {
        performer.getCommunicator().sendNormalServerMessage("Something went horribly wrong with " + target
          .getName() + ". Please use /support.");
        logger.log(Level.WARNING, "No template for filet?" + nst.getMessage(), nst);
        return true;
      }
      int waste;
      int tweight;
      int nums;
      ItemTemplate template;
      Skill butchering = performer.getSkills().getSkillOrLearn(10059);
      double bonus = 0.0D;
      try
      {
        Skill primskill = null;
        int primarySkill = source.getPrimarySkill();
        try
        {
          primskill = performer.getSkills().getSkill(primarySkill);
        }
        catch (Exception ex)
        {
          primskill = performer.getSkills().learn(primarySkill, 1.0F);
        }
        bonus = primskill.skillCheck(10.0D, 0.0D, false, 1.0F);
      }
      catch (NoSuchSkillException localNoSuchSkillException) {}
      performer.getCommunicator().sendNormalServerMessage("You filet the " + target.getName() + ".");
      Server.getInstance().broadCastAction(performer.getName() + " filets " + target.getNameWithGenus() + ".", performer, 5);
      
      float max = target.getCurrentQualityLevel();
      
      float ql = Math.max(1.0F, (float)butchering.skillCheck(target.getDamage(), source, bonus, counter > 10.0F, 1.0F));
      boolean destroyed = false;
      try
      {
        Item fil = ItemFactory.createItem(369, Math.min(max, ql), null);
        fil.setName("fillet of " + target.getActualName().toLowerCase());
        fil.setRealTemplate(target.getTemplateId());
        fil.setAuxData(target.getAuxData());
        fil.setIsUnderWeight(false);
        if (target.getTemperature() > 200) {
          fil.setTemperature(target.getTemperature());
        }
        performer.getInventory().insertItem(fil);
        target.setIsUnderWeight(true);
        destroyed = target.setWeight(target.getWeightGrams() - tweight - waste, true);
      }
      catch (FailedException fe)
      {
        logger.log(Level.WARNING, performer.getName() + ":" + fe.getMessage(), fe);
      }
      catch (NoSuchTemplateException nst)
      {
        logger.log(Level.WARNING, "No template for filet?" + nst.getMessage(), nst);
      }
      if (nums == 1)
      {
        int rand = Server.rand.nextInt(80 + (target != null ? source.getRarity() * 10 : 0));
        if (rand > 60) {
          if (target.getTemplateId() == 572)
          {
            if (invnums + 1 > 100)
            {
              performer.getCommunicator().sendNormalServerMessage("You can't make space in your inventory for the item you found in the " + target
                .getName() + ".");
              return true;
            }
            try
            {
              Item bonusItem = ItemFactory.createItem(752, Math.min(max, ql), null);
              performer.getInventory().insertItem(bonusItem);
            }
            catch (FailedException fe)
            {
              logger.log(Level.WARNING, performer.getName() + ":" + fe.getMessage(), fe);
            }
            catch (NoSuchTemplateException nst)
            {
              logger.log(Level.WARNING, "No template for inkSac?" + nst.getMessage(), nst);
            }
          }
        }
        if (!destroyed)
        {
          int rweight = target.getWeightGrams();
          if (rweight < tweight) {
            try
            {
              Items.destroyItem(target.getWurmId());
              Item bonusItem = ItemFactory.createItem(1363, Math.min(max, ql), null);
              performer.getInventory().insertItem(bonusItem);
            }
            catch (FailedException fe)
            {
              logger.log(Level.WARNING, performer.getName() + ":" + fe.getMessage(), fe);
            }
            catch (NoSuchTemplateException nst)
            {
              logger.log(Level.WARNING, "No template for inkSac?" + nst.getMessage(), nst);
            }
          }
        }
        return true;
      }
    }
    return false;
  }
  
  static final boolean chop(Action act, Creature performer, Item source, Item target, float counter)
  {
    boolean done = false;
    try
    {
      if ((!source.isWeaponAxe()) && (source.getTemplateId() != 24))
      {
        performer.getCommunicator().sendNormalServerMessage("You cannot chop with a " + source.getName() + ".");
        return true;
      }
      String action = source.getTemplateId() != 24 ? "chop" : "saw";
      if (target.getTemplateId() != 385)
      {
        performer.getCommunicator().sendNormalServerMessage("The " + target
          .getName() + " is not a huge log. You cannot " + action + " that into to smaller logs.");
        return true;
      }
      if ((target.getLastOwnerId() != performer.getWurmId()) && (!Methods.isActionAllowed(performer, (short)6, target))) {
        return true;
      }
      int time = 200;
      if (counter == 1.0F)
      {
        float posX = performer.getStatus().getPositionX();
        float posY = performer.getStatus().getPositionY();
        float rot = performer.getStatus().getRotation();
        
        float xPosMod = (float)Math.sin(rot * 0.017453292F);
        float yPosMod = -(float)Math.cos(rot * 0.017453292F);
        posX += xPosMod;
        posY += yPosMod;
        
        int placedX = (int)posX >> 2;
        int placedY = (int)posY >> 2;
        VolaTile t = Zones.getTileOrNull(placedX, placedY, performer.isOnSurface());
        if (t != null) {
          if ((target.getOwnerId() == -10L) && 
            (t.getNumberOfItems(performer.getFloorLevel()) > 99))
          {
            performer.getCommunicator().sendNormalServerMessage("There is no space to " + action + " wood here. Clear the area first.");
            
            return true;
          }
        }
        Skill woodcutting = performer.getSkills().getSkillOrLearn(1007);
        Skill primskill = performer.getSkills().getSkillOrLearn(source.getPrimarySkill());
        performer.getCommunicator().sendNormalServerMessage("You start to " + action + " up the " + target
          .getName() + ".");
        Server.getInstance().broadCastAction(performer
          .getName() + " starts to " + action + " up a " + target.getName() + ".", performer, 5);
        
        int treeAge = 5;
        
        time = (int)(Terraforming.calcTime(5, source, primskill, woodcutting) * Actions.getStaminaModiferFor(performer, 20000));
        time = Math.min(65535, time);
        
        act.setTimeLeft(time);
        performer.getStatus().modifyStamina(-1000.0F);
        performer.sendActionControl(source.getTemplateId() != 24 ? "Chopping" : "Sawing", true, time);
      }
      else
      {
        time = act.getTimeLeft();
        if ((act.justTickedSecond()) && (((time < 50) && (act.currentSecond() % 2 == 0)) || (act.currentSecond() % 5 == 0)))
        {
          source.setDamage(source.getDamage() + 0.001F * source.getDamageModifier());
          performer.getStatus().modifyStamina(-5000.0F);
        }
        if ((act.justTickedSecond()) && (counter * 10.0F < time - 30)) {
          if (source.getTemplateId() != 24)
          {
            if ((act.currentSecond() - 2) % 4 == 0)
            {
              String sstring = "sound.work.woodcutting1";
              int x = Server.rand.nextInt(3);
              if (x == 0) {
                sstring = "sound.work.woodcutting2";
              } else if (x == 1) {
                sstring = "sound.work.woodcutting3";
              }
              SoundPlayer.playSound(sstring, target, 1.0F);
            }
          }
          else if (((act.currentSecond() - 2) % 5 == 0) && (counter * 10.0F < time - 50))
          {
            String sstring = "sound.work.carpentry.saw";
            SoundPlayer.playSound("sound.work.carpentry.saw", target, 1.0F);
          }
        }
      }
      if (counter * 10.0F >= time)
      {
        if (act.getRarity() != 0) {
          performer.playPersonalSound("sound.fx.drumroll");
        }
        if (source.getTemplateId() != 24)
        {
          String sstring = "sound.work.woodcutting1";
          int x = Server.rand.nextInt(3);
          if (x == 0) {
            sstring = "sound.work.woodcutting2";
          } else if (x == 1) {
            sstring = "sound.work.woodcutting3";
          }
          SoundPlayer.playSound(sstring, target, 1.0F);
        }
        float posX = performer.getStatus().getPositionX();
        float posY = performer.getStatus().getPositionY();
        float rot = performer.getStatus().getRotation();
        
        float xPosMod = (float)Math.sin(rot * 0.017453292F) * 2.0F;
        float yPosMod = -(float)Math.cos(rot * 0.017453292F) * 2.0F;
        posX += xPosMod;
        posY += yPosMod;
        
        int placedX = (int)posX >> 2;
        int placedY = (int)posY >> 2;
        VolaTile t = Zones.getTileOrNull(placedX, placedY, performer.isOnSurface());
        if (t != null) {
          if ((target.getOwnerId() == -10L) && 
            (t.getNumberOfItems(performer.getFloorLevel()) > 99))
          {
            performer.getCommunicator().sendNormalServerMessage("There is no space to " + action + " wood here. Clear the area first.");
            
            return true;
          }
        }
        float tickCounter = counter;
        
        TreeData.TreeType tType = Materials.getTreeTypeForWood(target.getMaterial());
        double difficulty = tType.getDifficulty();
        Skill woodcutting = performer.getSkills().getSkillOrLearn(1007);
        try
        {
          int primarySkill = source.getPrimarySkill();
          Skill primskill = performer.getSkills().getSkillOrLearn(primarySkill);
          if ((primskill.getKnowledge() < 20.0D) || (primarySkill == 10003) || (primarySkill == 10008)) {
            primskill.skillCheck(difficulty, source, 0.0D, false, tickCounter);
          }
        }
        catch (NoSuchSkillException ss)
        {
          logger.log(Level.WARNING, "No primary skill for " + source.getName());
        }
        done = true;
        int nums = 1;
        try
        {
          ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(9);
          nums = target.getWeightGrams() / template.getWeightGrams();
          if (nums == 0)
          {
            performer.getCommunicator().sendNormalServerMessage("The " + target
              .getName() + " is too small to produce a log.");
            target.setTemplateId(9);
            
            done = true;
            if ((performer.getTutorialLevel() == 3) && (!performer.skippedTutorial())) {
              performer.missionFinished(true, true);
            }
          }
          else
          {
            performer.getCommunicator().sendNormalServerMessage("You create a smaller log from the " + target
              .getName() + ".");
            Server.getInstance()
              .broadCastAction(performer
              .getName() + " creates a smaller log from the " + target.getName() + ".", performer, 5);
            
            double power = woodcutting.skillCheck(difficulty, source, 0.0D, false, tickCounter);
            double cappedPower = Math.min(power, target.getCurrentQualityLevel());
            float ql = GeneralUtilities.calcRareQuality(cappedPower, act.getRarity(), source.getRarity(), target
              .getRarity());
            try
            {
              Item log = ItemFactory.createItem(9, ql, target
                .getMaterial(), act
                .getRarity(), null);
              if (target.getOwnerId() != -10L) {
                performer.getInventory().insertItem(log);
              } else {
                log.putItemInfrontof(performer);
              }
              log.setLastOwnerId(performer.getWurmId());
              target.setWeight(target.getWeightGrams() - log.getWeightGrams(), true);
              if (!target.deleted) {
                if (target.getWeightGrams() <= log.getTemplate().getWeightGrams()) {
                  if (target.getWeightGrams() < 1000)
                  {
                    Items.destroyItem(target.getWurmId());
                  }
                  else
                  {
                    Item spareLog = ItemFactory.createItem(9, target
                      .getCurrentQualityLevel(), target
                      .getMaterial(), target
                      .getRarity(), null);
                    spareLog.setWeight(target.getWeightGrams(), false);
                    Items.destroyItem(target.getWurmId());
                    if (log.getParentId() == performer.getInventory().getWurmId()) {
                      performer.getInventory().insertItem(spareLog);
                    } else {
                      spareLog.putItemInfrontof(performer);
                    }
                  }
                }
              }
              if ((performer.getTutorialLevel() == 3) && (!performer.skippedTutorial())) {
                performer.missionFinished(true, true);
              }
            }
            catch (FailedException fe)
            {
              logger.log(Level.WARNING, performer.getName() + ":" + fe.getMessage(), fe);
            }
            catch (NoSuchTemplateException nst)
            {
              logger.log(Level.WARNING, "No template for log?" + nst.getMessage(), nst);
            }
            catch (NoSuchItemException nsi)
            {
              logger.log(Level.WARNING, performer.getName() + " no such item?", nsi);
            }
            catch (NoSuchPlayerException nsp)
            {
              logger.log(Level.WARNING, performer.getName() + " no such player?", nsp);
            }
            catch (NoSuchCreatureException nsc)
            {
              logger.log(Level.WARNING, performer.getName() + " no such creature?", nsc);
            }
            catch (NoSuchZoneException nsz)
            {
              logger.log(Level.WARNING, performer.getName() + " no such zone?", nsz);
            }
          }
        }
        catch (NoSuchTemplateException nst)
        {
          logger.log(Level.WARNING, "No template for log?" + nst.getMessage(), nst);
        }
        PlayerTutorial.firePlayerTrigger(performer.getWurmId(), PlayerTutorial.PlayerTrigger.CREATE_LOG);
      }
    }
    catch (NoSuchSkillException e)
    {
      logger.log(Level.WARNING, e.getMessage(), e);
      done = true;
    }
    return done;
  }
  
  public static final byte getNewCreationState(byte material)
  {
    if (Materials.isWood(material)) {
      return (byte)Server.rand.nextInt(5);
    }
    if (Materials.isMetal(material)) {
      return (byte)Server.rand.nextInt(5);
    }
    if (Materials.isLeather(material)) {
      return (byte)Server.rand.nextInt(5);
    }
    if (Materials.isCloth(material)) {
      return (byte)Server.rand.nextInt(5);
    }
    if (Materials.isClay(material)) {
      return (byte)Server.rand.nextInt(5);
    }
    if (Materials.isStone(material)) {
      return (byte)Server.rand.nextInt(5);
    }
    return 0;
  }
  
  public static final byte getImproveMaterial(Item item)
  {
    if (!item.isImproveUsingTypeAsMaterial()) {
      return item.getMaterial();
    }
    if ((item.getTemplate().isCloth()) && (item.getMaterial() != 69)) {
      return 17;
    }
    return item.getMaterial();
  }
  
  public static final int getItemForImprovement(byte material, byte state)
  {
    int template = -10;
    if (Materials.isWood(material)) {
      switch (state)
      {
      case 1: 
        template = 8;
        break;
      case 2: 
        template = 63;
        break;
      case 3: 
        template = 388;
        break;
      case 4: 
        template = 313;
        break;
      default: 
        template = -10;break;
      }
    } else if (Materials.isMetal(material)) {
      switch (state)
      {
      case 1: 
        template = 296;
        break;
      case 2: 
        template = 62;
        break;
      case 3: 
        template = 128;
        break;
      case 4: 
        template = 313;
        break;
      default: 
        template = -10;break;
      }
    } else if (Materials.isLeather(material)) {
      switch (state)
      {
      case 1: 
        template = 215;
        break;
      case 2: 
        template = 390;
        break;
      case 3: 
        template = 392;
        break;
      case 4: 
        template = 63;
        break;
      default: 
        template = -10;break;
      }
    } else if (Materials.isCloth(material)) {
      switch (state)
      {
      case 1: 
        template = 215;
        break;
      case 2: 
        template = 394;
        break;
      case 3: 
        template = 128;
        break;
      case 4: 
        template = 215;
        break;
      default: 
        template = -10;break;
      }
    } else if (Materials.isStone(material)) {
      switch (state)
      {
      case 1: 
        template = 97;
        break;
      case 2: 
        template = 97;
        break;
      case 3: 
        template = 97;
        break;
      case 4: 
        template = 97;
        break;
      default: 
        template = -10;break;
      }
    } else if (Materials.isClay(material)) {
      switch (state)
      {
      case 1: 
        template = 14;
        break;
      case 2: 
        template = 128;
        break;
      case 3: 
        template = 396;
        break;
      case 4: 
        template = 397;
        break;
      default: 
        template = -10;
      }
    }
    return template;
  }
  
  public static final String getNeededCreationAction(byte material, byte state, Item item)
  {
    String todo = "";
    String fstring = "improve";
    if (item.getTemplateId() == 386) {
      fstring = "finish";
    }
    if (Materials.isWood(material)) {
      switch (state)
      {
      case 1: 
        todo = "You notice some notches you must carve away in order to " + fstring + " the " + item.getName() + ".";
        
        break;
      case 2: 
        todo = "You must use a mallet on the " + item.getName() + " in order to " + fstring + " it.";
        break;
      case 3: 
        todo = "You must use a file to smooth out the " + item.getName() + " in order to " + fstring + " it.";
        break;
      case 4: 
        todo = "You will want to polish the " + item.getName() + " with a pelt to " + fstring + " it.";
        break;
      default: 
        todo = "";break;
      }
    } else if (Materials.isMetal(material)) {
      switch (state)
      {
      case 1: 
        todo = "The " + item.getName() + (item.isNamePlural() ? " need" : " needs") + " to be sharpened with a whetstone.";
        break;
      case 2: 
        todo = "The " + item.getName() + (item.isNamePlural() ? " have" : " has") + " some dents that must be flattened by a hammer.";
        break;
      case 3: 
        todo = "You need to temper the " + item.getName() + " by dipping it in water while it's hot.";
        break;
      case 4: 
        todo = "You need to polish the " + item.getName() + " with a pelt.";
        break;
      default: 
        todo = "";break;
      }
    } else if (Materials.isLeather(material)) {
      switch (state)
      {
      case 1: 
        todo = "The " + item.getName() + (item.isNamePlural() ? " have" : " has") + " some holes and must be tailored with an iron needle to " + fstring + ".";
        
        break;
      case 2: 
        todo = "The " + item.getName() + (item.isNamePlural() ? " need" : " needs") + " some holes punched with an awl.";
        break;
      case 3: 
        todo = "The " + item.getName() + (item.isNamePlural() ? " have" : " has") + " some excess leather that needs to be cut away with a leather knife.";
        
        break;
      case 4: 
        todo = "A mallet must be used on the " + item.getName() + " in order to smooth out a quirk.";
        break;
      default: 
        todo = "";break;
      }
    } else if (Materials.isCloth(material)) {
      switch (state)
      {
      case 1: 
        todo = "The " + item.getName() + (item.isNamePlural() ? " have" : " has") + " an open seam that must be backstitched with an iron needle to " + fstring + ".";
        
        break;
      case 2: 
        todo = "The " + item.getName() + (item.isNamePlural() ? " have" : " has") + " some excess cloth that needs to be cut away with a scissors.";
        
        break;
      case 3: 
        todo = "The " + item.getName() + (item.isNamePlural() ? " have" : " has") + " some stains that must be washed away.";
        break;
      case 4: 
        todo = "The " + item.getName() + (item.isNamePlural() ? " have" : " has") + " a seam that needs to be hidden by slipstitching with an iron needle.";
        
        break;
      default: 
        todo = "";break;
      }
    } else if (Materials.isStone(material)) {
      switch (state)
      {
      case 1: 
      case 2: 
      case 3: 
      case 4: 
        todo = "The " + item.getName() + (item.isNamePlural() ? " have" : " has") + " some irregularities that must be removed with a stone chisel.";
        
        break;
      default: 
        todo = "";break;
      }
    } else if (Materials.isClay(material)) {
      switch (state)
      {
      case 1: 
        todo = "The " + item.getName() + (item.isNamePlural() ? " have" : " has") + " some flaws that must be removed by hand.";
        break;
      case 2: 
        todo = "The " + item.getName() + " needs water.";
        break;
      case 3: 
        todo = "The " + item.getName() + (item.isNamePlural() ? " have" : " has") + " some flaws that must be fixed with a clay shaper.";
        break;
      case 4: 
        todo = "The " + item.getName() + (item.isNamePlural() ? " have" : " has") + " some flaws that must be fixed with a spatula.";
        break;
      default: 
        todo = "";
      }
    }
    return todo;
  }
  
  public String getCreationActionString(byte material, byte state, Item used, Item item)
  {
    String todo = "";
    if (Materials.isWood(material)) {
      switch (state)
      {
      case 1: 
        todo = "You carve on the " + item.getName() + ".";
        break;
      case 2: 
        todo = "You hammer on the " + item.getName() + ".";
        break;
      case 3: 
        todo = "You file on the " + item.getName() + " meticulously.";
        break;
      case 4: 
        todo = "You polish the " + item.getName() + " carefully.";
        break;
      default: 
        todo = "";break;
      }
    } else if (Materials.isMetal(material)) {
      switch (state)
      {
      case 1: 
        todo = "You sharpen the " + item.getName() + ".";
        break;
      case 2: 
        todo = "You hammer on the " + item.getName() + ".";
        break;
      case 3: 
        todo = "You dip the " + item.getName() + " in the water.";
        break;
      case 4: 
        todo = "You polish the " + item.getName() + " carefully.";
        break;
      default: 
        todo = "";break;
      }
    } else if (Materials.isLeather(material)) {
      switch (state)
      {
      case 1: 
        todo = "You sew the " + item.getName() + ".";
        break;
      case 2: 
        todo = "You punch some holes in the " + item.getName() + ".";
        break;
      case 3: 
        todo = "You cut away some excess leather from the " + item.getName() + ".";
        break;
      case 4: 
        todo = "You hammer the " + item.getName() + " and flatten a bulge.";
        break;
      default: 
        todo = "";break;
      }
    } else if (Materials.isCloth(material)) {
      switch (state)
      {
      case 1: 
        todo = "You backstitch the " + item.getName() + " elegantly.";
        break;
      case 2: 
        todo = "You cut away some excess cloth from the " + item.getName() + ".";
        break;
      case 3: 
        todo = "You wash the " + item.getName() + ".";
        break;
      case 4: 
        todo = "You nimbly slipstitch the " + item.getName() + ".";
        break;
      default: 
        todo = "";break;
      }
    } else if (Materials.isStone(material)) {
      switch (state)
      {
      case 1: 
        todo = "You carefully chip away some rock from the " + item.getName() + ".";
        break;
      case 2: 
        todo = "You carefully chip away some rock from the " + item.getName() + ".";
        break;
      case 3: 
        todo = "You carefully chip away some rock from the " + item.getName() + ".";
        break;
      case 4: 
        todo = "You carefully chip away some rock from the " + item.getName() + ".";
        break;
      default: 
        todo = "";break;
      }
    } else if (Materials.isClay(material)) {
      switch (state)
      {
      case 1: 
        todo = "You skillfully fix some irregularities in the " + item.getName() + ".";
        break;
      case 2: 
        todo = "You add some water to the " + item.getName() + ".";
        break;
      case 3: 
        todo = "Meticulously you use the " + used.getName() + " to create the desired form of the " + item.getName() + ".";
        break;
      case 4: 
        todo = "You carefully use the " + used.getName() + " to remove some unnecessary clay from the " + item.getName() + ".";
        break;
      default: 
        todo = "";
      }
    }
    return todo;
  }
  
  private static final String getImproveActionString(byte material, byte state)
  {
    String todo = "fixing";
    if (Materials.isWood(material)) {
      switch (state)
      {
      case 1: 
        todo = "carving";
        break;
      case 2: 
        todo = "hammering";
        break;
      case 3: 
        todo = "filing";
        break;
      case 4: 
        todo = "polishing";
        break;
      default: 
        todo = "fixing";break;
      }
    } else if (Materials.isMetal(material)) {
      switch (state)
      {
      case 1: 
        todo = "sharpening";
        break;
      case 2: 
        todo = "hammering";
        break;
      case 3: 
        todo = "tempering";
        break;
      case 4: 
        todo = "polishing";
        break;
      default: 
        todo = "fixing";break;
      }
    } else if (Materials.isLeather(material)) {
      switch (state)
      {
      case 1: 
        todo = "sewing";
        break;
      case 2: 
        todo = "punching";
        break;
      case 3: 
        todo = "cutting";
        break;
      case 4: 
        todo = "hammering";
        break;
      default: 
        todo = "fixing";break;
      }
    } else if (Materials.isCloth(material)) {
      switch (state)
      {
      case 1: 
        todo = "backstitching";
        break;
      case 2: 
        todo = "cutting";
        break;
      case 3: 
        todo = "washing";
        break;
      case 4: 
        todo = "slipstitching";
        break;
      default: 
        todo = "fixing";break;
      }
    } else if (Materials.isStone(material)) {
      switch (state)
      {
      case 1: 
        todo = "chipping";
        break;
      case 2: 
        todo = "chipping";
        break;
      case 3: 
        todo = "chipping";
        break;
      case 4: 
        todo = "chipping";
        break;
      default: 
        todo = "fixing";break;
      }
    } else if (Materials.isClay(material)) {
      switch (state)
      {
      case 1: 
        todo = "molding";
        break;
      case 2: 
        todo = "watering";
        break;
      case 3: 
        todo = "molding";
        break;
      case 4: 
        todo = "molding";
        break;
      default: 
        todo = "fixing";
      }
    }
    return todo;
  }
  
  static final String getImproveAction(byte material, byte state)
  {
    String todo = "Fix";
    if (Materials.isWood(material)) {
      switch (state)
      {
      case 1: 
        todo = "Carve";
        break;
      case 2: 
        todo = "Hammer";
        break;
      case 3: 
        todo = "File";
        break;
      case 4: 
        todo = "Polish";
        break;
      default: 
        todo = "Fix";break;
      }
    } else if (Materials.isMetal(material)) {
      switch (state)
      {
      case 1: 
        todo = "Sharpen";
        break;
      case 2: 
        todo = "Hammer";
        break;
      case 3: 
        todo = "Temper";
        break;
      case 4: 
        todo = "Polish";
        break;
      default: 
        todo = "Fix";break;
      }
    } else if (Materials.isLeather(material)) {
      switch (state)
      {
      case 1: 
        todo = "Sew";
        break;
      case 2: 
        todo = "Punch";
        break;
      case 3: 
        todo = "Cut";
        break;
      case 4: 
        todo = "Hammer";
        break;
      default: 
        todo = "Fix";break;
      }
    } else if (Materials.isCloth(material)) {
      switch (state)
      {
      case 1: 
        todo = "Backstitch";
        break;
      case 2: 
        todo = "Cut";
        break;
      case 3: 
        todo = "Wash";
        break;
      case 4: 
        todo = "Slipstitch";
        break;
      default: 
        todo = "Fix";break;
      }
    } else if (Materials.isStone(material)) {
      switch (state)
      {
      case 1: 
        todo = "Chip";
        break;
      case 2: 
        todo = "Chip";
        break;
      case 3: 
        todo = "Chip";
        break;
      case 4: 
        todo = "Chip";
        break;
      default: 
        todo = "Fix";break;
      }
    } else if (Materials.isClay(material)) {
      switch (state)
      {
      case 1: 
        todo = "Mold";
        break;
      case 2: 
        todo = "Water";
        break;
      case 3: 
        todo = "Mold";
        break;
      case 4: 
        todo = "Mold";
        break;
      default: 
        todo = "Fix";
      }
    }
    return todo;
  }
  
  public static final String getRarityDesc(byte rarity)
  {
    switch (rarity)
    {
    case 1: 
      return " This is a very rare and interesting version of the item.";
    case 2: 
      return " This is a supreme example of the item, with fine details and slick design.";
    case 3: 
      return " This is a fantastic example of the item, with fascinating design details and perfect ideas for functionality.";
    }
    return "";
  }
  
  public static final String getRarityName(byte rarity)
  {
    switch (rarity)
    {
    case 1: 
      return "rare";
    case 2: 
      return "supreme";
    case 3: 
      return "fantastic";
    }
    return "";
  }
  
  private static final int getTemperWaterAmountFor(Item target)
  {
    if (target.getWeightGrams() > 10000) {
      return 1000;
    }
    if (target.getWeightGrams() < 2000) {
      return 200;
    }
    return target.getWeightGrams() / 10;
  }
  
  public static final boolean polishItem(Action act, Creature performer, Item source, Item target, float counter)
  {
    byte state = target.creationState;
    boolean improving = act.getNumber() == 192;
    if ((counter == 0.0F) || (counter == 1.0F) || (act.justTickedSecond()))
    {
      if ((state == 0) || (target.isNewbieItem()) || (target.isChallengeNewbieItem()))
      {
        performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " does not need that.");
        return true;
      }
      if (source.getWurmId() == target.getWurmId())
      {
        performer.getCommunicator().sendNormalServerMessage("You cannot improve the " + source
          .getName() + " using itself as a tool.");
        return true;
      }
      int templateId = getItemForImprovement(getImproveMaterial(target), state);
      if (templateId != source.getTemplateId())
      {
        performer.getCommunicator().sendNormalServerMessage("The " + target
          .getName() + " does not need the touch of " + source.getNameWithGenus() + ".");
        return true;
      }
      if ((templateId == 128) && ((target.isMetal()) || (target.isCloth()))) {
        if (source.getWeightGrams() < getTemperWaterAmountFor(target))
        {
          if (target.isCloth()) {
            performer.getCommunicator().sendNormalServerMessage("You need more water in order to wash the " + target
              .getName() + ".");
          } else {
            performer.getCommunicator().sendNormalServerMessage("You need more water in order to cool the " + target
              .getName() + ".");
          }
          return true;
        }
      }
      if ((target.getParentId() != -10L) && (target.getTemplateId() == 386)) {
        try
        {
          ItemTemplate temp = target.getRealTemplate();
          if ((temp != null) && (!temp.isVehicle()))
          {
            Item parent = target.getParent();
            if (parent.isNoWorkParent())
            {
              performer.getCommunicator().sendNormalServerMessage("You can't work with the " + target
                .getName() + " in the " + parent.getName() + ".");
              
              throw new NoSuchItemException("The " + target.getName() + " can't be modified in the " + parent.getName() + ".");
            }
            if ((parent.getContainerSizeX() < temp.getSizeX()) || 
              (parent.getContainerSizeY() < temp.getSizeY()) || 
              (parent.getContainerSizeZ() <= temp.getSizeZ())) {
              if ((parent.getTemplateId() != 177) && (parent.getTemplateId() != 0))
              {
                performer.getCommunicator().sendNormalServerMessage("It's too tight to try and work on the " + target
                  .getName() + " in the " + parent
                  .getName() + ".");
                return true;
              }
            }
          }
        }
        catch (NoSuchItemException localNoSuchItemException) {}
      }
    }
    Skills skills = performer.getSkills();
    Skill improve = null;
    int skillNum = -10;
    if (improving) {
      skillNum = getImproveSkill(target);
    } else {
      skillNum = getImproveSkill(target.getMaterial(), target.realTemplate);
    }
    if (skillNum == -10)
    {
      performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " can not be improved right now.");
      
      return true;
    }
    int time = 1000;
    
    boolean insta = performer.getPower() >= 5;
    try
    {
      improve = skills.getSkill(skillNum);
    }
    catch (NoSuchSkillException nss)
    {
      improve = skills.learn(skillNum, 1.0F);
    }
    if (!insta) {
      if (target.getDamage() > 0.0F)
      {
        performer.getCommunicator().sendNormalServerMessage("Repair the " + target
          .getName() + " before you try to " + (target.isUnfinished() ? "finish" : "improve") + " it.");
        return true;
      }
    }
    double power = 0.0D;
    double bonus = 0.0D;
    if (performer.isPriest()) {
      bonus = -20.0D;
    }
    Skill secondarySkill = null;
    try
    {
      secondarySkill = skills.getSkill(source.getPrimarySkill());
    }
    catch (Exception ex)
    {
      try
      {
        secondarySkill = skills.learn(source.getPrimarySkill(), 1.0F);
      }
      catch (Exception localException1) {}
    }
    float runeModifier = 1.0F;
    if (target.getSpellEffects() != null) {
      runeModifier = target.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_IMPPERCENT);
    }
    float imbueEnhancement = 1.0F + source.getSkillSpellImprovement(skillNum) / 100.0F;
    double improveBonus = 0.23047D * imbueEnhancement * runeModifier;
    double max = improve.getKnowledge(0.0D) + (100.0D - improve.getKnowledge(0.0D)) * improveBonus;
    
    double diff = Math.max(0.0D, max - target.getQualityLevel());
    float skillgainMod = 1.0F;
    if (diff <= 0.0D) {
      skillgainMod = 2.0F;
    }
    if (counter == 1.0F)
    {
      if ((!insta) && 
        (target.isMetal()) && (!target.isNoTake())) {
        if (target.getTemperature() < 3500)
        {
          performer.getCommunicator().sendNormalServerMessage("The " + target
            .getName() + " needs to be glowing hot to be improved.");
          return true;
        }
      }
      String improvestring = getImproveActionString(getImproveMaterial(target), state);
      performer.getCommunicator()
        .sendNormalServerMessage("You start " + improvestring + " the " + target.getName() + ".");
      Server.getInstance().broadCastAction(performer
        .getName() + " starts " + improvestring + " " + target.getNameWithGenus() + ".", performer, 5);
      
      time = Actions.getImproveActionTime(performer, source);
      performer.sendActionControl(improvestring, true, time);
      
      act.setTimeLeft(time);
      double impmod = 0.5D;
      if (improving) {
        impmod = 1.0D;
      }
      double mod = impmod * ((100.0F - target.getQualityLevel()) / 20.0F / 100.0F * (Server.rand.nextFloat() + Server.rand.nextFloat() + Server.rand.nextFloat() + Server.rand.nextFloat()) / 2.0F);
      if (improving)
      {
        power = improve.skillCheck(target.getQualityLevel(), source, bonus, true, 1.0F);
        if (power < 0.0D)
        {
          act.setFailSecond((int)Math.max(20.0F, time * Server.rand.nextFloat()));
          act.setPower((float)(-mod * Math.max(1.0D, diff)));
        }
        else
        {
          if (diff <= 0.0D) {
            mod *= 0.009999999776482582D;
          }
          double regain = 1.0D;
          if (target.getQualityLevel() < target.getOriginalQualityLevel()) {
            regain = 2.0D;
          }
          diff *= regain;
          int tid = target.getTemplateId();
          if ((target.isArmour()) || (target.isWeapon()) || (target.isShield()) || (tid == 455) || (tid == 454) || (tid == 456) || (tid == 453) || (tid == 451) || (tid == 452)) {
            mod *= 2.0D;
          }
          Titles.Title title = performer.getTitle();
          if ((title != null) && (title.getSkillId() == improve.getNumber()) && (
            (target.isArmour()) || (target.isCreatureWearableOnly()))) {
            mod *= 1.2999999523162842D;
          }
          act.setPower((float)(mod * Math.max(1.0D, diff)));
        }
      }
      else
      {
        double regain = 1.0D;
        if (target.getQualityLevel() < target.getOriginalQualityLevel()) {
          regain = 2.0D;
        }
        diff *= regain;
        int tid = target.getTemplateId();
        if ((target.isArmour()) || (target.isCreatureWearableOnly()) || (target.isWeapon()) || (target.isShield()) || (tid == 455) || (tid == 454) || (tid == 456) || (tid == 453) || (tid == 451) || (tid == 452)) {
          mod *= 2.0D;
        }
        Titles.Title title = performer.getTitle();
        if ((title != null) && (title.getSkillId() == improve.getNumber()) && (
          (target.isArmour()) || (target.isCreatureWearableOnly()))) {
          mod *= 1.2999999523162842D;
        }
        act.setPower((float)(mod * Math.max(1.0D, diff)));
      }
    }
    else
    {
      time = act.getTimeLeft();
      float failsec = act.getFailSecond();
      power = act.getPower();
      if (counter >= failsec)
      {
        if (secondarySkill != null) {
          bonus = Math.max(bonus, secondarySkill
          
            .skillCheck(target.getQualityLevel(), source, bonus, false, performer
            .isPriest() ? counter / 3.0F : counter / 2.0F));
        }
        if (performer.isPriest()) {
          bonus = Math.min(bonus, 0.0D);
        }
        improve.skillCheck(target.getQualityLevel(), source, bonus, false, performer.isPriest() ? counter / 2.0F : counter);
        if (power != 0.0D)
        {
          if (!target.isBodyPart()) {
            if (!target.isLiquid())
            {
              target.setDamage(target.getDamage() - act.getPower());
              performer.getCommunicator().sendNormalServerMessage("You damage the " + target
                .getName() + " a little.");
              Server.getInstance().broadCastAction(performer
                .getName() + " grunts as " + performer.getHeSheItString() + " damages " + target
                .getNameWithGenus() + " a little.", performer, 5);
            }
            else
            {
              performer.getCommunicator().sendNormalServerMessage("You fail.");
              Server.getInstance().broadCastAction(performer
                .getName() + " grunts as " + performer.getHeSheItString() + " fails.", performer, 5);
            }
          }
        }
        else
        {
          performer.getCommunicator().sendNormalServerMessage("You realize you almost damaged the " + target
            .getName() + " and stop.");
          Server.getInstance().broadCastAction(performer
            .getName() + " stops improving " + target.getNameWithGenus() + ".", performer, 5);
        }
        performer.getStatus().modifyStamina(-counter * 1000.0F);
        return true;
      }
    }
    if (act.mayPlaySound()) {
      sendImproveSound(performer, source, target, skillNum);
    }
    if ((counter * 10.0F > time) || (insta))
    {
      if (act.getRarity() != 0) {
        performer.playPersonalSound("sound.fx.drumroll");
      }
      float maxGain = 1.0F;
      if ((source.isLiquid()) && ((target.isMetal()) || (target.isUnfired()) || 
        (target.isCloth()) || (Materials.isCloth(getImproveMaterial(target)))))
      {
        if (source.getTemplateId() == 128)
        {
          source.setWeight(source.getWeightGrams() - getTemperWaterAmountFor(target), true);
        }
        else
        {
          int usedWeight = (int)Math.min(500.0D, Math.max(1.0D, target.getWeightGrams() * 0.05D));
          if (source.getWeightGrams() < usedWeight) {
            maxGain = Math.min(1.0F, source.getWeightGrams() / usedWeight);
          }
          source.setWeight(source.getWeightGrams() - usedWeight, true);
        }
      }
      else if (!source.isLiquid()) {
        source.setDamage(source.getDamage() + 0.0015F * source.getDamageModifier());
      }
      if (secondarySkill != null) {
        bonus = Math.max(bonus, secondarySkill
        
          .skillCheck(target.getQualityLevel(), source, bonus, false, skillgainMod * (performer
          .isPriest() ? counter / 3.0F : counter / 2.0F)));
      }
      if (performer.isPriest()) {
        bonus = Math.min(bonus, 0.0D);
      }
      improve.skillCheck(target.getQualityLevel(), source, bonus, false, skillgainMod * (performer
        .isPriest() ? counter / 2.0F : counter));
      String improvestring = getImproveActionString(getImproveMaterial(target), state);
      Server.getInstance().broadCastAction(performer
        .getName() + " ceases " + improvestring + " " + target.getNameWithGenus() + ".", performer, 5);
      
      power = act.getPower();
      if (power > 0.0D)
      {
        byte rarity = target.getRarity();
        
        float rarityChance = 0.2F;
        if (target.getSpellEffects() != null) {
          rarityChance *= target.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RARITYIMP);
        }
        if ((act.getRarity() > rarity) && 
          (Server.rand.nextFloat() <= rarityChance)) {
          rarity = act.getRarity();
        }
        byte newState = getNewCreationState(getImproveMaterial(target));
        while ((newState != 0) && (newState == state))
        {
          newState = getNewCreationState(getImproveMaterial(target));
          int temp = getItemForImprovement(getImproveMaterial(target), newState);
          if (temp == target.getTemplateId()) {
            newState = (byte)(newState - 1);
          }
        }
        target.setCreationState(newState);
        Item toRarify = target;
        if (target.getTemplateId() == 128) {
          toRarify = source;
        }
        if (rarity > toRarify.getRarity())
        {
          toRarify.setRarity(rarity);
          for (Item sub : toRarify.getItems()) {
            if (sub != null) {
              if (sub.isComponentItem()) {
                sub.setRarity(rarity);
              }
            }
          }
          if (toRarify.getRarity() > 2) {
            performer.achievement(300);
          } else if (toRarify.getRarity() == 1) {
            performer.achievement(301);
          } else if (toRarify.getRarity() == 2) {
            performer.achievement(302);
          }
        }
        if (newState != 0)
        {
          String newString = getNeededCreationAction(getImproveMaterial(target), newState, target);
          performer.getCommunicator().sendNormalServerMessage(newString);
        }
        else if (target.getTemplateId() == 386)
        {
          MissionTriggers.activateTriggers(performer, target, 148, 0L, 1);
          performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is finished!");
          
          target.setTemplateId(target.realTemplate);
          if (target.isUseOnGroundOnly()) {
            if (target.getOwnerId() != performer.getWurmId()) {
              try
              {
                target.putItemInfrontof(performer);
              }
              catch (Exception nsz)
              {
                logger.log(Level.INFO, performer.getName() + ": " + nsz.getMessage());
              }
            }
          }
          skillNum = getImproveSkill(target);
          int templateId = getImproveTemplateId(target);
          if (skillNum != -10) {
            try
            {
              ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(templateId);
              performer.getCommunicator().sendNormalServerMessage("The " + target
                .getName() + " could be improved with " + temp.getNameWithGenus() + ".");
            }
            catch (NoSuchTemplateException localNoSuchTemplateException) {}
          }
        }
        else
        {
          performer.getCommunicator().sendNormalServerMessage("You improve the " + target.getName() + " a bit.");
          
          skillNum = getImproveSkill(target);
          int templateId = getImproveTemplateId(target);
          if (skillNum != -10) {
            try
            {
              ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(templateId);
              performer.getCommunicator().sendNormalServerMessage("The " + target
                .getName() + " could be improved with " + temp.getNameWithGenus() + ".");
            }
            catch (NoSuchTemplateException localNoSuchTemplateException1) {}
          }
        }
        if (insta) {
          performer.getCommunicator().sendNormalServerMessage("before: " + target
            .getQualityLevel() + " now: " + (target.getQualityLevel() + power * maxGain) + " power=" + power + " maxGain=" + maxGain);
        }
        float oldQL = target.getQualityLevel();
        boolean wasHighest = Items.isHighestQLForTemplate(target.getTemplateId(), target.getQualityLevel(), target
          .getWurmId(), true);
        float modifier = 1.0F;
        if (target.getSpellEffects() != null) {
          modifier = target.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_IMPQL);
        }
        modifier *= target.getMaterialImpBonus();
        target.setQualityLevel(Math.min(100.0F, (float)(target.getQualityLevel() + power * maxGain * modifier)));
        if (target.getQualityLevel() > target.getOriginalQualityLevel())
        {
          target.setOriginalQualityLevel(target.getQualityLevel());
          triggerImproveAchievements(performer, target, improve, wasHighest, oldQL);
        }
      }
      else
      {
        if (insta) {
          performer.getCommunicator().sendNormalServerMessage("Dam before: " + target
            .getDamage() + " now: " + (target.getDamage() - power) + " power=" + power);
        }
        if (!target.isBodyPart()) {
          if (!target.isLiquid())
          {
            target.setDamage(target.getDamage() - (float)power);
            performer.getCommunicator()
              .sendNormalServerMessage("You damage the " + target.getName() + " a little.");
            Server.getInstance().broadCastAction(performer
              .getName() + " grunts as " + performer.getHeSheItString() + " damages " + target
              .getNameWithGenus() + " a little.", performer, 5);
          }
          else
          {
            performer.getCommunicator().sendNormalServerMessage("You fail.");
            Server.getInstance().broadCastAction(performer
              .getName() + " grunts as " + performer.getHeSheItString() + " fails.", performer, 5);
          }
        }
      }
      performer.getStatus().modifyStamina(-counter * 1000.0F);
      return true;
    }
    return false;
  }
  
  static final boolean temper(Action act, Creature performer, Item source, Item target, float counter)
  {
    byte state = source.creationState;
    boolean improving = act.getNumber() == 192;
    boolean insta = performer.getPower() >= 5;
    if ((counter == 0.0F) || (counter == 1.0F) || (act.justTickedSecond()))
    {
      if ((state == 0) || (source.isNewbieItem()) || (source.isChallengeNewbieItem()))
      {
        performer.getCommunicator().sendNormalServerMessage("The " + source
          .getName() + " does not need tempering right now.");
        return true;
      }
      int templateId = getItemForImprovement(source.getMaterial(), state);
      if (templateId != target.getTemplateId())
      {
        performer.getCommunicator().sendNormalServerMessage("The " + source
          .getName() + " does not need tempering right now.");
        return true;
      }
      if (!insta) {
        if (source.getDamage() > 0.0F)
        {
          performer.getCommunicator().sendNormalServerMessage("Repair the " + source
            .getName() + " before you temper it.");
          return true;
        }
      }
      if (target.getWeightGrams() < getTemperWaterAmountFor(source))
      {
        performer.getCommunicator().sendNormalServerMessage("You need more water in order to cool the " + source
          .getName() + ".");
        return true;
      }
      if ((source.getParentId() != -10L) && (source.getTemplateId() == 386)) {
        try
        {
          ItemTemplate temp = source.getRealTemplate();
          if ((temp != null) && (!temp.isVehicle()))
          {
            Item parent = source.getParent();
            if (parent.isNoWorkParent())
            {
              performer.getCommunicator().sendNormalServerMessage("You can't work with the " + source
                .getName() + " in the " + parent.getName() + ".");
              
              throw new NoSuchItemException("The " + source.getName() + " can't be modified in the " + parent.getName() + ".");
            }
            if ((parent.getContainerSizeX() < temp.getSizeX()) || 
              (parent.getContainerSizeY() < temp.getSizeY()) || 
              (parent.getContainerSizeZ() <= temp.getSizeZ())) {
              if ((parent.getTemplateId() != 177) && (parent.getTemplateId() != 0))
              {
                performer.getCommunicator().sendNormalServerMessage("It's too tight to try and work on the " + source
                  .getName() + " in the " + parent
                  .getName() + ".");
                return true;
              }
            }
          }
        }
        catch (NoSuchItemException localNoSuchItemException) {}
      }
    }
    Skills skills = performer.getSkills();
    Skill improve = null;
    int skillNum = -10;
    if (improving) {
      skillNum = getImproveSkill(source);
    } else {
      skillNum = getImproveSkill(source.getMaterial(), source.realTemplate);
    }
    if (skillNum == -10)
    {
      performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " can not be tempered with that.");
      return true;
    }
    int time = 1000;
    try
    {
      improve = skills.getSkill(skillNum);
    }
    catch (NoSuchSkillException nss)
    {
      improve = skills.learn(skillNum, 1.0F);
    }
    double power = 0.0D;
    double bonus = 0.0D;
    if (performer.isPriest()) {
      bonus -= 10.0D;
    }
    float runeModifier = 1.0F;
    if (source.getSpellEffects() != null) {
      runeModifier = source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_IMPPERCENT);
    }
    float imbueEnhancement = 1.0F + source.getSkillSpellImprovement(skillNum) / 100.0F;
    double improveBonus = 0.23047D * imbueEnhancement * runeModifier;
    double max = improve.getKnowledge(0.0D) + (100.0D - improve.getKnowledge(0.0D)) * improveBonus;
    
    double diff = Math.max(0.0D, max - source.getQualityLevel());
    float skillgainMod = 1.0F;
    if (diff <= 0.0D) {
      skillgainMod = 2.0F;
    }
    double regain;
    if (counter == 1.0F)
    {
      if ((!insta) && 
        (source.isMetal())) {
        if ((source.getTemperature() < 3500) && (!source.isNoTake()))
        {
          performer.getCommunicator().sendNormalServerMessage("The " + source
            .getName() + " needs to be glowing hot to be tempered.");
          return true;
        }
      }
      if (improving) {}
      String improvestring = getImproveActionString(getImproveMaterial(source), state);
      performer.getCommunicator()
        .sendNormalServerMessage("You start " + improvestring + " the " + source.getName() + ".");
      Server.getInstance().broadCastAction(performer
        .getName() + " starts " + improvestring + " " + source.getNameWithGenus() + ".", performer, 5);
      
      time = Actions.getImproveActionTime(performer, source);
      performer.sendActionControl(improvestring, true, time);
      
      act.setTimeLeft(time);
      double impmod = 0.5D;
      if (improving) {
        impmod = 1.0D;
      }
      double mod = impmod * ((100.0F - source.getQualityLevel()) / 20.0F / 100.0F * (Server.rand.nextFloat() + Server.rand.nextFloat() + Server.rand.nextFloat() + Server.rand.nextFloat()) / 2.0F);
      if (improving)
      {
        power = improve.skillCheck(source.getQualityLevel(), bonus, true, 1.0F);
        if (power < 0.0D)
        {
          act.setFailSecond((int)Math.max(20.0F, time * Server.rand.nextFloat()));
          act.setPower((float)(-mod * Math.max(1.0D, diff)));
        }
        else
        {
          if (diff <= 0.0D) {
            mod *= 0.009999999776482582D;
          }
          double regain = 1.0D;
          if (source.getQualityLevel() < source.getOriginalQualityLevel()) {
            regain = 2.0D;
          }
          diff *= regain;
          int tid = source.getTemplateId();
          if ((tid == 455) || (tid == 454) || (tid == 456) || (tid == 453) || (tid == 451) || (tid == 452)) {
            mod *= 2.0D;
          }
          act.setPower((float)(mod * Math.max(1.0D, diff)));
        }
      }
      else
      {
        regain = 1.0D;
        if (source.getQualityLevel() < source.getOriginalQualityLevel()) {
          regain = 5.0D;
        }
        diff = Math.min(diff, 10.0D * regain);
        int tid = source.getTemplateId();
        if ((tid == 455) || (tid == 454) || (tid == 456) || (tid == 453) || (tid == 451) || (tid == 452)) {
          mod *= 3.0D;
        }
        act.setPower((float)(mod * Math.max(1.0D, diff)));
      }
    }
    else
    {
      time = act.getTimeLeft();
      float failsec = act.getFailSecond();
      power = act.getPower();
      if (counter >= failsec)
      {
        improve.skillCheck(source.getQualityLevel(), bonus, false, performer.isPriest() ? counter / 2.0F : counter);
        if (power != 0.0D)
        {
          source.setDamage(source.getDamage() - act.getPower());
          performer.getCommunicator().sendNormalServerMessage("You damage the " + source.getName() + " a little.");
          Server.getInstance().broadCastAction(performer
            .getName() + " grunts as " + performer.getHeSheItString() + " damages " + source
            .getNameWithGenus() + " a little.", performer, 5);
        }
        else
        {
          performer.getCommunicator().sendNormalServerMessage("You realize you almost damaged the " + source
            .getName() + " and stop.");
          Server.getInstance().broadCastAction(performer
            .getName() + " stops improving " + source.getNameWithGenus() + ".", performer, 5);
        }
        performer.getStatus().modifyStamina(-counter * 1000.0F);
        return true;
      }
    }
    if (act.mayPlaySound()) {
      Methods.sendSound(performer, "sound.work.smithing.temper");
    }
    if ((counter * 10.0F > time) || (insta))
    {
      if (act.getRarity() != 0) {
        performer.playPersonalSound("sound.fx.drumroll");
      }
      if (target.getWeightGrams() < getTemperWaterAmountFor(source))
      {
        target.setWeight(target.getWeightGrams() - getTemperWaterAmountFor(source), true);
        performer.getCommunicator().sendNormalServerMessage("There is too little water to temper the metal. It boils away instantly.");
        
        return true;
      }
      target.setWeight(target.getWeightGrams() - getTemperWaterAmountFor(source), true);
      
      improve.skillCheck(source.getQualityLevel(), bonus, false, skillgainMod * (performer
        .isPriest() ? counter / 2.0F : counter));
      Server.getInstance().broadCastAction(performer.getName() + " tempers " + source.getNameWithGenus() + ".", performer, 5);
      
      power = act.getPower();
      if (power > 0.0D)
      {
        byte newState = getNewCreationState(source.getMaterial());
        int tempr = getItemForImprovement(source.getMaterial(), newState);
        if (tempr == source.getTemplateId()) {
          newState = (byte)(newState - 1);
        }
        source.setCreationState(newState);
        
        byte rarity = source.getRarity();
        
        float rarityChance = 0.2F;
        if (source.getSpellEffects() != null) {
          rarityChance *= source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RARITYIMP);
        }
        if ((act.getRarity() > rarity) && 
          (Server.rand.nextFloat() <= rarityChance)) {
          rarity = act.getRarity();
        }
        Item toRarify = source;
        if (source.getTemplateId() == 128) {
          toRarify = target;
        }
        if (rarity > toRarify.getRarity())
        {
          toRarify.setRarity(rarity);
          for (Item sub : toRarify.getItems()) {
            if (sub != null) {
              if (sub.isComponentItem()) {
                sub.setRarity(rarity);
              }
            }
          }
          if (toRarify.getRarity() > 2) {
            performer.achievement(300);
          } else if (toRarify.getRarity() == 1) {
            performer.achievement(301);
          } else if (toRarify.getRarity() == 2) {
            performer.achievement(302);
          }
        }
        if (newState != 0)
        {
          String newString = getNeededCreationAction(source.getMaterial(), newState, source);
          performer.getCommunicator().sendNormalServerMessage(newString);
        }
        else if (source.getTemplateId() == 386)
        {
          performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " is finished!");
          
          source.setTemplateId(source.realTemplate);
          skillNum = getImproveSkill(source);
          int templateId = getImproveTemplateId(source);
          if (skillNum != -10) {
            try
            {
              ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(templateId);
              performer.getCommunicator().sendNormalServerMessage("The " + source
                .getName() + " could be improved with " + temp.getNameWithGenus() + ".");
            }
            catch (NoSuchTemplateException localNoSuchTemplateException) {}
          }
        }
        else
        {
          performer.getCommunicator().sendNormalServerMessage("You improve the " + source.getName() + " a bit.");
          
          skillNum = getImproveSkill(source);
          int templateId = getImproveTemplateId(source);
          if (skillNum != -10) {
            try
            {
              ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(templateId);
              performer.getCommunicator().sendNormalServerMessage("The " + source
                .getName() + " could be improved with " + temp.getNameWithGenus() + ".");
            }
            catch (NoSuchTemplateException localNoSuchTemplateException1) {}
          }
        }
        if (insta) {
          performer.getCommunicator().sendNormalServerMessage("before: " + source
            .getQualityLevel() + " now: " + (source.getQualityLevel() + power) + " power=" + power);
        }
        float oldQL = source.getQualityLevel();
        boolean wasHighest = Items.isHighestQLForTemplate(source.getTemplateId(), source.getQualityLevel(), source
          .getWurmId(), true);
        float modifier = 1.0F;
        if (source.getSpellEffects() != null) {
          modifier = source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_IMPQL);
        }
        modifier *= target.getMaterialImpBonus();
        source.setQualityLevel(Math.min(100.0F, (float)(source.getQualityLevel() + power * modifier)));
        if (source.getQualityLevel() > source.getOriginalQualityLevel())
        {
          source.setOriginalQualityLevel(source.getQualityLevel());
          
          triggerImproveAchievements(performer, source, improve, wasHighest, oldQL);
        }
      }
      else
      {
        if (insta) {
          performer.getCommunicator().sendNormalServerMessage("Dam before: " + source
            .getDamage() + " now: " + (source.getDamage() - power) + " power=" + power);
        }
        source.setDamage(source.getDamage() - (float)power);
        
        performer.getCommunicator().sendNormalServerMessage("You damage the " + source.getName() + " a little.");
        Server.getInstance().broadCastAction(performer
          .getName() + " grunts as " + performer.getHeSheItString() + " damages " + source
          .getNameWithGenus() + " a little.", performer, 5);
        
        performer.achievement(206);
      }
      performer.getStatus().modifyStamina(-counter * 1000.0F);
      return true;
    }
    return false;
  }
  
  public static void sendImproveSound(Creature performer, Item source, Item target, int skillNum)
  {
    String sound = "";
    int stid = source.getTemplateId();
    if (stid == 296) {
      sound = "sound.work.smithing.whetstone";
    } else if ((stid == 313) || (stid == 171)) {
      sound = "sound.work.smithing.polish";
    } else if (stid == 24) {
      sound = "sound.work.carpentry.saw";
    } else if (stid == 8) {
      sound = "sound.work.carpentry.carvingknife";
    } else if (stid == 388) {
      sound = "sound.work.carpentry.rasp";
    } else if (target.isWood()) {
      sound = Server.rand.nextInt(2) == 0 ? "sound.work.carpentry.mallet1" : "sound.work.carpentry.mallet2";
    } else if (target.isMetal()) {
      sound = "sound.work.smithing.metal";
    } else if (target.isStone()) {
      if ((skillNum == 10074) || (stid == 97)) {
        sound = "sound.work.stonecutting";
      } else {
        sound = "sound.work.masonry";
      }
    }
    Methods.sendSound(performer, sound);
  }
  
  static final boolean seal(Creature performer, Item source, Item target, Action act)
  {
    boolean done = false;
    if ((!target.canBeSealedByPlayer()) && (!target.canBePeggedByPlayer()))
    {
      performer.getCommunicator().sendNormalServerMessage("The " + target
        .getName() + " cannot be sealed.");
      return true;
    }
    if (target.getItems().isEmpty())
    {
      performer.getCommunicator().sendNormalServerMessage("The " + target
        .getName() + " cannot be sealed as there is nothing in it.");
      return true;
    }
    Item liquid = null;
    for (Item item : target.getItems()) {
      if (item.isLiquid()) {
        liquid = item;
      }
    }
    if (liquid == null)
    {
      performer.getCommunicator().sendNormalServerMessage("The " + target
        .getName() + " cannot be sealed as there is no liquid in it.");
      return true;
    }
    if (act.currentSecond() == 1)
    {
      act.setTimeLeft(50);
      performer.getCommunicator().sendNormalServerMessage("You start to seal the " + target.getName() + ".");
      Server.getInstance().broadCastAction(performer
        .getName() + " starts to seal " + target.getNameWithGenus() + ".", performer, 5);
      performer.sendActionControl(Actions.actionEntrys['ˣ'].getVerbString(), true, act.getTimeLeft());
    }
    else
    {
      if (source.getTemplateId() == 561)
      {
        if (act.currentSecond() == 2) {
          performer.getCommunicator().sendNormalServerMessage("You position the peg over the hole on the top.");
        } else if (act.currentSecond() == 3) {
          performer.getCommunicator().sendNormalServerMessage("You press the peg in slowly so not to disturb the liquid.");
        } else if (act.currentSecond() == 4) {
          performer.getCommunicator().sendNormalServerMessage("You give the peg one final tap.");
        }
      }
      else if (act.currentSecond() == 2) {
        performer.getCommunicator().sendNormalServerMessage("You carefully wax the cloth.");
      } else if (act.currentSecond() == 3) {
        performer.getCommunicator().sendNormalServerMessage("You put the waxed cloth over the " + target.getName() + ".");
      } else if (act.currentSecond() == 4) {
        performer.getCommunicator().sendNormalServerMessage("You tie the string around " + target.getName() + ".");
      }
      int timeleft = act.getTimeLeft();
      if (act.getCounterAsFloat() * 10.0F >= timeleft)
      {
        target.closeAll();
        done = true;
        performer.getCommunicator().sendNormalServerMessage("You sealed the " + target.getName() + ".");
        target.setIsSealedByPlayer(true);
        if (source.getTemplateId() == 1255) {
          target.setData((int)source.getQualityLevel(), (int)source.getDamage());
        }
        Items.destroyItem(source.getWurmId());
      }
    }
    return done;
  }
  
  static final boolean breakSeal(Creature performer, Item target, Action act)
  {
    boolean done = false;
    Item liquid = null;
    for (Item item : target.getItemsAsArray()) {
      if (item.isLiquid())
      {
        liquid = item;
        break;
      }
    }
    if ((liquid != null) && (liquid.isFermenting()))
    {
      performer.getCommunicator().sendNormalServerMessage("The " + target
        .getName() + " is still fermenting, therefore it makes no sense to unseal at this time..");
      return true;
    }
    if (act.currentSecond() == 1)
    {
      act.setTimeLeft(50);
      performer.getCommunicator().sendNormalServerMessage("You start to unseal the " + target.getName() + ".");
      Server.getInstance().broadCastAction(performer
        .getName() + " starts to unseal " + target.getNameWithGenus() + ".", performer, 5);
      performer.sendActionControl(Actions.actionEntrys['ˤ'].getVerbString(), true, act.getTimeLeft());
    }
    else
    {
      if (target.canBePeggedByPlayer())
      {
        if (act.currentSecond() == 2) {
          performer.getCommunicator().sendNormalServerMessage("You put your hand on the peg on the top.");
        } else if (act.currentSecond() == 3) {
          performer.getCommunicator().sendNormalServerMessage("You carefully remove the peg from the top.");
        } else if (act.currentSecond() == 4) {
          performer.getCommunicator().sendNormalServerMessage("But you notice the peg is damaged so discard it.");
        }
      }
      else if (act.currentSecond() == 2) {
        performer.getCommunicator().sendNormalServerMessage("You remove the string.");
      } else if (act.currentSecond() == 3) {
        performer.getCommunicator().sendNormalServerMessage("You break the wax seal.");
      } else if (act.currentSecond() == 4) {
        performer.getCommunicator().sendNormalServerMessage("You try to get the wax seal off so it can be used again.");
      }
      int timeleft = act.getTimeLeft();
      if (act.getCounterAsFloat() * 10.0F >= timeleft)
      {
        target.closeAll();
        if (target.getData1() != -1)
        {
          boolean decayed = true;
          try
          {
            Item kit = ItemFactory.createItem(1255, target.getData1(), performer.getName());
            decayed = kit.setDamage(target.getData2() + 3 + Server.rand.nextInt(5), true);
            if (!decayed)
            {
              performer.getInventory().insertItem(kit);
              performer.getCommunicator().sendNormalServerMessage("You managed to get the wax sealing kit off, but damage it a bit.");
            }
          }
          catch (FailedException e)
          {
            logger.log(Level.WARNING, e.getMessage(), e);
          }
          catch (NoSuchTemplateException e)
          {
            logger.log(Level.WARNING, e.getMessage(), e);
          }
          target.setData(-1, -1);
          if (decayed) {
            performer.getCommunicator().sendNormalServerMessage("You unsealed the " + target.getName() + " by destroying the wax sealing.");
          }
        }
        else
        {
          performer.getCommunicator().sendNormalServerMessage("You unsealed the " + target.getName() + ".");
        }
        target.setIsSealedByPlayer(false);
        done = true;
      }
    }
    return done;
  }
  
  static final boolean removeSecuritySeal(Creature performer, Item target, Action act)
  {
    boolean done = false;
    if (act.currentSecond() == 1)
    {
      act.setTimeLeft(50);
      performer.getCommunicator().sendNormalServerMessage("You start to remove the security seal on the " + target.getName() + ".");
      Server.getInstance().broadCastAction(performer
        .getName() + " starts to remove the security seal on the " + target.getNameWithGenus() + ".", performer, 5);
      performer.sendActionControl(Actions.actionEntrys['ˤ'].getVerbString(), true, act.getTimeLeft());
    }
    else
    {
      int timeleft = act.getTimeLeft();
      if (act.getCounterAsFloat() * 10.0F >= timeleft)
      {
        target.closeAll();
        performer.getCommunicator().sendNormalServerMessage("You have removed the security seal from the " + target.getName() + ".");
        target.setIsSealedByPlayer(false);
        if (target.isCrate())
        {
          for (Item item : target.getItems())
          {
            item.setLastOwnerId(target.getLastOwnerId());
            item.setLastMaintained(WurmCalendar.currentTime);
          }
          target.setData(-10L);
        }
        done = true;
      }
    }
    return done;
  }
  
  static final boolean wrap(Creature performer, @Nullable Item source, Item target, Action act)
  {
    boolean done = true;
    if (target.isWrapped())
    {
      performer.getCommunicator().sendNormalServerMessage("The " + target
        .getName() + " is already wrapped.");
      return true;
    }
    if (!target.usesFoodState())
    {
      performer.getCommunicator().sendNormalServerMessage("You just cannot figure out how to wrap the " + target
        .getName() + ".");
      return true;
    }
    boolean insta = performer.getPower() >= 5;
    done = false;
    if (act.currentSecond() == 1)
    {
      act.setTimeLeft(50);
      if (source != null)
      {
        performer.getCommunicator().sendNormalServerMessage("You start to wrap the " + target.getName() + ".");
        Server.getInstance().broadCastAction(performer
          .getName() + " starts to wrap " + target.getNameWithGenus() + ".", performer, 5);
      }
      else
      {
        performer.getCommunicator().sendNormalServerMessage("You start to wrap the " + target.getName() + " using some grass, leaves and moss.");
        
        Server.getInstance().broadCastAction(performer.getName() + " starts to wrap " + target.getNameWithGenus() + " using some grass, leaves and moss.", performer, 5);
      }
      performer.sendActionControl(Actions.actionEntrys['ˣ'].getVerbString(), true, act.getTimeLeft());
    }
    else
    {
      int timeleft = act.getTimeLeft();
      if ((insta) || (act.getCounterAsFloat() * 10.0F >= timeleft))
      {
        done = true;
        performer.getCommunicator().sendNormalServerMessage("You wrapped the " + target.getName() + ".");
        target.setIsWrapped(true);
        if (source != null) {
          Items.destroyItem(source.getWurmId());
        }
      }
    }
    return done;
  }
  
  static final boolean unwrap(Creature performer, Item target, Action act)
  {
    boolean done = true;
    if (!target.isWrapped())
    {
      performer.getCommunicator().sendNormalServerMessage("The " + target
        .getName() + " is not wrapped.");
      return true;
    }
    boolean insta = performer.getPower() >= 5;
    done = false;
    if (act.currentSecond() == 1)
    {
      act.setTimeLeft(50);
      performer.getCommunicator().sendNormalServerMessage("You start to unwrap the " + target.getName() + ".");
      Server.getInstance().broadCastAction(performer
        .getName() + " starts to unwrap " + target.getNameWithGenus() + ".", performer, 5);
      performer.sendActionControl(Actions.actionEntrys['ˤ'].getVerbString(), true, act.getTimeLeft());
    }
    else
    {
      int timeleft = act.getTimeLeft();
      if ((insta) || (act.getCounterAsFloat() * 10.0F >= timeleft))
      {
        done = true;
        target.setIsWrapped(false);
        performer.getCommunicator().sendNormalServerMessage("You unwrapped the " + target.getName() + " and throw away the used wrapping.");
      }
    }
    return done;
  }
  
  public static boolean conquerTarget(Creature performer, Item target, Communicator comm, float counter, Action act)
  {
    if (target.isWarTarget())
    {
      if ((target.getKingdom() == performer.getKingdomId()) && (target.getData1() == 100))
      {
        comm.sendNormalServerMessage("The " + target
          .getName() + " is already conquered.");
        return true;
      }
      Kingdom pk = Kingdoms.getKingdom(performer.getKingdomId());
      if ((pk != null) && (pk.isAllied(target.getKingdom())))
      {
        comm.sendNormalServerMessage("The " + target
          .getName() + " is already conquered by your alliance.");
        return true;
      }
      if (performer.isOnSurface() != target.isOnSurface())
      {
        if (performer.isOnSurface()) {
          comm.sendNormalServerMessage("You need to be in cave to conquer the " + target
            .getName() + " (because it is).");
        } else {
          comm.sendNormalServerMessage("You need to be on surface to conquer the " + target
            .getName() + ".");
        }
        return true;
      }
      int time = 6000;
      if (performer.getPower() > 0) {
        time = 200;
      }
      if (target.getKingdom() == 0)
      {
        if (target.getData2() == performer.getKingdomId()) {
          time = (100 - target.getData1()) * 30;
        } else {
          time = target.getData1() * 30;
        }
      }
      else if (target.getData2() == performer.getKingdomId()) {
        time = (100 - target.getData1()) * 30;
      } else {
        time = target.getData1() * 30;
      }
      if (!Servers.localServer.HOMESERVER) {
        if (target.isWarTarget()) {
          time = (int)(time * (1.0F + Zones.getPercentLandForKingdom(performer.getKingdomId()) / 300.0F));
        }
      }
      act.setTimeLeft(time);
      Player player;
      if (counter == 1.0F)
      {
        for (player : Players.getInstance().getPlayers()) {
          if (player.getWurmId() != performer.getWurmId()) {
            try
            {
              Action acta = player.getCurrentAction();
              if ((acta.getNumber() == 504) && (acta.getTarget() == target.getWurmId()))
              {
                comm.sendNormalServerMessage("The " + target
                  .getName() + " can not be used by more than one person.");
                return true;
              }
            }
            catch (NoSuchActionException localNoSuchActionException) {}
          }
        }
        Long last = (Long)ItemBehaviour.conquers.get(Long.valueOf(target.getWurmId()));
        if (last != null) {
          if (System.currentTimeMillis() - last.longValue() < 3600000L) {
            comm.sendAlertServerMessage(String.format("You will have to wait %s if you want to receive battle rank for conquering the %s.", new Object[] {
              Server.getTimeFor(last.longValue() + 3600000L - System.currentTimeMillis()), target.getName() }));
          }
        }
        if (target.getKingdom() == performer.getKingdomId())
        {
          comm.sendNormalServerMessage("You start to secure the " + target.getName() + ".");
          performer.sendActionControl(Actions.actionEntrys['Ǹ'].getVerbString(), true, time);
          Server.getInstance().broadCastAction(performer
            .getName() + " starts securing the " + target.getName() + ".", performer, 10);
        }
        else
        {
          comm.sendNormalServerMessage("You start to conquer the " + target.getName() + ".");
          performer.sendActionControl(Actions.actionEntrys['Ǹ'].getVerbString(), true, time);
          Server.getInstance().broadCastAction(performer
            .getName() + " starts conquering the " + target.getName() + ".", performer, 10);
        }
      }
      if ((act.justTickedSecond()) && ((int)counter % 60 == 0))
      {
        String name = target.getName();
        if (target.getKingdom() != performer.getKingdomId())
        {
          Players.getInstance().broadCastConquerInfo(performer, name + " is being conquered.");
          Server.getInstance().broadCastEpicEvent(name + " is being conquered.");
        }
        if (target.getKingdom() == 0)
        {
          if (target.getData2() != performer.getKingdomId()) {
            target.setData1(Math.max(0, target.getData1() - 20));
          } else {
            target.setData1(Math.min(100, target.getData1() + 20));
          }
        }
        else if (target.getData2() == performer.getKingdomId()) {
          target.setData1(Math.min(100, target.getData1() + 20));
        } else {
          target.setData1(Math.max(0, target.getData1() - 20));
        }
        VolaTile t = Zones.getOrCreateTile(target.getTileX(), target.getTileY(), target.isOnSurface());
        t.updateTargetStatus(target.getWurmId(), (byte)target.getData2(), target.getData1());
      }
      if (((target.getData1() == 100) || (target.getData1() == 0)) && (counter > 60.0F))
      {
        boolean dealPoints = (target.getKingdom() != performer.getKingdomId()) && (target.getData1() == 100);
        Long last = (Long)ItemBehaviour.conquers.get(Long.valueOf(target.getWurmId()));
        if (last != null) {
          if (System.currentTimeMillis() - last.longValue() < 3600000L) {
            dealPoints = false;
          }
        }
        if (dealPoints)
        {
          ItemBehaviour.conquers.put(Long.valueOf(target.getWurmId()), Long.valueOf(System.currentTimeMillis()));
          Player[] arrayOfPlayer2 = Players.getInstance().getPlayers();player = arrayOfPlayer2.length;
          for (localNoSuchActionException = 0; localNoSuchActionException < player; localNoSuchActionException++)
          {
            Player p = arrayOfPlayer2[localNoSuchActionException];
            if ((p.isFriendlyKingdom(performer.getKingdomId())) && 
              (p.isWithinDistanceTo(target.getPosX(), target.getPosY(), p.getPositionZ(), 300.0F)))
            {
              p.modifyKarma(10);
              try
              {
                p.setRank(p.getRank());
              }
              catch (IOException localIOException) {}
            }
          }
        }
        if (target.getKingdom() != performer.getKingdomId())
        {
          if (target.getData1() == 100)
          {
            Players.getInstance().broadCastConquerInfo(performer, performer
              .getName() + " conquers " + target.getName() + ".");
            Server.getInstance().broadCastEpicEvent(performer
              .getName() + " conquers " + target.getName() + ".");
            performer.achievement(368);
            target.setData2(performer.getKingdomId());
            target.setAuxData(performer.getKingdomId());
            Kingdoms.addWarTargetKingdom(target);
            for (int x = 0; x < 2 + Server.rand.nextInt(4); x++) {
              try
              {
                GuardTower.spawnSoldier(target, performer.getKingdomId());
              }
              catch (Exception e)
              {
                logger.log(Level.WARNING, e.getMessage(), e);
              }
            }
            GuardTower.spawnCommander(target, performer.getKingdomId());
          }
          else if (target.getData1() == 0)
          {
            target.setData2(performer.getKingdomId());
            Players.getInstance().broadCastConquerInfo(performer, performer
              .getName() + " neutralizes " + target.getName() + ".");
            Server.getInstance().broadCastEpicEvent(performer
              .getName() + " neutralizes " + target.getName() + ".");
            comm.sendNormalServerMessage("You neutralize the " + target
              .getName() + ".");
            
            target.setAuxData((byte)0);
          }
        }
        else if (target.getData1() == 100)
        {
          comm.sendNormalServerMessage("You secure the " + target
            .getName() + ".");
          target.setData2(performer.getKingdomId());
        }
        else if (target.getData1() == 0)
        {
          target.setData2(performer.getKingdomId());
          Players.getInstance().broadCastConquerInfo(performer, performer
            .getName() + " neutralizes " + target.getName() + ".");
          Server.getInstance().broadCastEpicEvent(performer
            .getName() + " neutralizes " + target.getName() + ".");
          comm.sendNormalServerMessage("You neutralize the " + target
            .getName() + ".");
          
          target.setAuxData((byte)0);
        }
        VolaTile t = Zones.getOrCreateTile(target.getTileX(), target.getTileY(), target.isOnSurface());
        t.updateTargetStatus(target.getWurmId(), (byte)target.getData2(), target.getData1());
        return true;
      }
    }
    else
    {
      if (!performer.isPaying())
      {
        comm.sendNormalServerMessage("Due to exploitability, only premium players may conquer pillars.");
        return true;
      }
      if (performer.getFightingSkill().getRealKnowledge() < 20.0D)
      {
        comm.sendNormalServerMessage("You need fighting skill 20 in order to conquer pillars.");
        return true;
      }
      if (Servers.localServer.getNextHota() != Long.MAX_VALUE)
      {
        comm.sendNormalServerMessage("The Hunt is not on.");
        return true;
      }
      if (performer.getCitizenVillage() == null)
      {
        comm.sendNormalServerMessage("You have no alliance and can't assume control of the " + target.getName() + ".");
        return true;
      }
      if (target.getData1() != 0) {
        if (performer.getCitizenVillage() != null) {
          if ((target.getData1() == performer.getCitizenVillage().getAllianceNumber()) || 
            (target.getData1() == performer.getCitizenVillage().getId()))
          {
            comm.sendNormalServerMessage("Your alliance is already in control of the " + target
              .getName() + ".");
            Hota.addPillarTouched(performer, target);
            return true;
          }
        }
      }
      if (counter == 1.0F)
      {
        comm.sendNormalServerMessage("You start to conquer the " + target
          .getName() + ".");
        
        performer.sendActionControl(Actions.actionEntrys['Ǹ'].getVerbString(), true, 1000);
        Server.getInstance().broadCastAction(performer
          .getName() + " starts conquering the " + target.getName() + ".", performer, 10);
        Hota.addPillarTouched(performer, target);
      }
      if (counter > 90.0F) {
        if (target.getData1() != 0)
        {
          target.deleteAllEffects();
          target.setData1(0);
          Server.getInstance().broadCastSafe(performer
            .getName() + " neutralizes the " + target.getName() + ".");
          Server.getInstance().broadCastAction(performer
            .getName() + " neutralizes the " + target.getName() + ".", performer, 10);
          if (performer.getCitizenVillage() != null) {
            Hota.addPillarConquered(performer, target);
          }
        }
      }
      if (counter > 100.0F)
      {
        if (performer.getCitizenVillage() != null)
        {
          Server.getInstance().broadCastSafe(performer.getName() + " conquers the " + target.getName() + ".");
          target.addEffect(EffectFactory.getInstance().createFire(target.getWurmId(), target.getPosX(), target
            .getPosY(), target.getPosZ(), true));
          if (performer.getCitizenVillage().getAllianceNumber() == 0) {
            target.setData1(performer.getCitizenVillage().getId() + 2000000);
          } else {
            target.setData1(performer.getCitizenVillage().getAllianceNumber());
          }
          Hota.addPillarConquered(performer, target);
        }
        else
        {
          comm.sendNormalServerMessage("You can only conquer the pillar in the name of a settlement or alliance.");
        }
        return true;
      }
      return false;
    }
    return false;
  }
  
  static final boolean colorItem(Creature performer, Item colour, Item target, Action act, boolean primary)
  {
    boolean done = true;
    if (target.isIndestructible())
    {
      performer.getCommunicator().sendNormalServerMessage("The " + target
        .getName() + " is indestructible and the colour can not be replaced.");
      return true;
    }
    String sItem = "";
    if (primary)
    {
      if (target.getTemplateId() == 1396) {
        sItem = "barrel";
      }
      if (target.color != -1)
      {
        if (sItem.length() == 0) {
          performer.getCommunicator().sendNormalServerMessage("The " + target
            .getName() + " already has colour on it.");
        } else {
          performer.getCommunicator().sendNormalServerMessage("The " + target
            .getName() + " already has colour on it's " + sItem + ". Remove it first with a metal brush.");
        }
        return true;
      }
    }
    if (!primary)
    {
      if (target.getTemplateId() == 1396) {
        sItem = "lamp";
      } else {
        sItem = target.getSecondryItemName();
      }
      if (target.color2 != -1)
      {
        if (sItem.length() == 0) {
          performer.getCommunicator().sendNormalServerMessage("The " + target
            .getName() + " already has colour on it.");
        } else {
          performer.getCommunicator().sendNormalServerMessage("The " + target
            .getName() + " already has colour on it's " + sItem + ". Remove it first with some lye.");
        }
        return true;
      }
    }
    int dyeOverride = target.getTemplate().getDyePrimaryAmountGrams();
    if ((!primary) && (!target.isDragonArmour())) {
      if (target.getTemplate().getDyeSecondaryAmountGrams() > 0) {
        dyeOverride = target.getTemplate().getDyeSecondaryAmountGrams();
      } else if (dyeOverride > 0) {
        dyeOverride = (int)(dyeOverride * 0.3F);
      }
    }
    int colourNeeded;
    int colourNeeded;
    if (dyeOverride > 0) {
      colourNeeded = dyeOverride;
    } else {
      colourNeeded = (int)Math.max(1.0D, target.getSurfaceArea() * (primary ? 1.0D : 0.3D) / 25.0D);
    }
    boolean insta = performer.getPower() >= 5;
    String type;
    String type;
    if ((primary) || (target.isDragonArmour())) {
      type = "paint";
    } else {
      type = "dye";
    }
    if ((!insta) && (colourNeeded > colour.getWeightGrams()))
    {
      performer.getCommunicator().sendNormalServerMessage("You need more " + type + " to colour that item - at least " + colourNeeded + "g of " + type + ".");
    }
    else
    {
      done = false;
      if (act.currentSecond() == 1)
      {
        act.setTimeLeft(Math.max(50, colourNeeded / 50));
        if (((primary) || (target.isDragonArmour())) && (target.getTemplateId() != 1396)) {
          performer.getCommunicator().sendNormalServerMessage("You start to " + type + " the " + target.getName() + " (using " + colourNeeded + "g of " + type + ").");
        } else {
          performer.getCommunicator().sendNormalServerMessage("You start to " + type + " the " + target.getName() + "'s " + sItem + " (using " + colourNeeded + "g of " + type + ").");
        }
        Server.getInstance().broadCastAction(performer
          .getName() + " starts to " + type + " " + target.getNameWithGenus() + ".", performer, 5);
        String verb;
        String verb;
        if (primary) {
          verb = Actions.actionEntrys['ç'].getVerbString();
        } else {
          verb = Actions.actionEntrys['Λ'].getVerbString();
        }
        performer.sendActionControl(verb, true, act.getTimeLeft());
      }
      else
      {
        int timeleft = act.getTimeLeft();
        if ((insta) || (act.getCounterAsFloat() * 10.0F >= timeleft))
        {
          done = true;
          if (primary) {
            target.setColor(colour.getColor());
          } else {
            target.setColor2(colour.getColor());
          }
          colour.setWeight(colour.getWeightGrams() - colourNeeded, true);
          if ((primary) || (target.isDragonArmour()))
          {
            if (target.getTemplateId() == 1396) {
              performer.getCommunicator().sendNormalServerMessage("You paint the " + target.getName() + "'s " + sItem + ".");
            } else {
              performer.getCommunicator().sendNormalServerMessage("You paint the " + target.getName() + ".");
            }
          }
          else if (target.getTemplateId() == 1396) {
            performer.getCommunicator().sendNormalServerMessage("You paint the " + target.getName() + "'s " + sItem + ".");
          } else {
            performer.getCommunicator().sendNormalServerMessage("You dye the " + target.getName() + "'s " + sItem + ".");
          }
          if ((target.isBoat()) && (!primary)) {
            performer.achievement(494);
          }
          if (target.isArmour()) {
            performer.achievement(493);
          }
        }
      }
    }
    return done;
  }
  
  static final boolean improveColor(Creature performer, Item colorComponent, Item dye, Action act)
  {
    boolean done = true;
    boolean insta = performer.getPower() >= 5;
    performer.sendToLoggers(colorComponent.getName() + " weight: " + colorComponent.getWeightGrams() + ", " + dye.getName() + " weight: " + dye
      .getWeightGrams());
    if (colorComponent.getTemplate().getWeightGrams() > colorComponent.getWeightGrams())
    {
      performer.getCommunicator().sendNormalServerMessage("You need more " + colorComponent
        .getName() + " to improve the dye.");
    }
    else
    {
      done = false;
      if (act.currentSecond() == 1)
      {
        act.setTimeLeft(50);
        performer.getCommunicator().sendNormalServerMessage("You start to improve the " + dye.getName() + ".");
        Server.getInstance().broadCastAction(performer
          .getName() + " starts to improve " + dye.getNameWithGenus() + ".", performer, 5);
        performer.sendActionControl(Actions.actionEntrys['ě'].getVerbString(), true, act.getTimeLeft());
      }
      else
      {
        int timeleft = act.getTimeLeft();
        if ((insta) || (act.getCounterAsFloat() * 10.0F >= timeleft))
        {
          done = true;
          dye.setColor(WurmColor.getCompositeColor(dye.color, dye.getWeightGrams(), colorComponent.getTemplateId(), colorComponent
            .getQualityLevel()));
          
          colorComponent.setWeight(colorComponent.getWeightGrams() - colorComponent.getTemplate().getWeightGrams(), true);
          
          performer.getCommunicator().sendNormalServerMessage("You try to improve the " + dye.getName() + ".");
        }
      }
    }
    return done;
  }
  
  static final boolean removeColor(Creature performer, Item brush, Item target, Action act, boolean primary)
  {
    boolean done = true;
    if (target.isIndestructible())
    {
      performer.getCommunicator().sendNormalServerMessage("The " + target
        .getName() + " is indestructible and the colour can not be removed.");
      return true;
    }
    if ((target.isDragonArmour()) && (primary))
    {
      performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is too tough for the " + brush
        .getName() + " and the colour refuses to disappear.");
      return true;
    }
    boolean insta = performer.getPower() >= 5;
    int colourNeeded = 0;
    String sItem = "";
    if (target.getTemplateId() == 1396)
    {
      if (((primary) && (brush.getTemplateId() != 441)) || ((!primary) && 
        (brush.getTemplateId() != 73)))
      {
        performer.getCommunicator().sendNormalServerMessage("You cannot use the " + brush.getName() + " to do this.");
        return true;
      }
      if (primary) {
        sItem = "barrel";
      } else {
        sItem = "lamp";
      }
    }
    else
    {
      if ((primary) && (brush.getTemplateId() != 441))
      {
        performer.getCommunicator().sendNormalServerMessage("You cannot use the " + brush.getName() + " to do this.");
        return true;
      }
      if (!primary) {
        sItem = target.getSecondryItemName();
      }
    }
    if ((!insta) && (target.color == -1) && (primary))
    {
      performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " has no colour.");
      return true;
    }
    if (!primary) {
      if (brush.getTemplateId() == 441)
      {
        if ((!insta) && (target.color2 == -1))
        {
          performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " has no colour.");
          return true;
        }
        if ((target.isColorable()) && (target.getTemplateId() != 1396))
        {
          performer.getCommunicator().sendNormalServerMessage("You cannot use the " + brush.getName() + " to do this.");
          return true;
        }
      }
      else
      {
        if (brush.getTemplateId() != 73)
        {
          performer.getCommunicator().sendNormalServerMessage("You cannot use the " + brush.getName() + " to do this.");
          return true;
        }
        int dyeOverride = target.getTemplate().getDyePrimaryAmountGrams();
        if (!primary) {
          if (target.getTemplate().getDyeSecondaryAmountGrams() > 0) {
            dyeOverride = target.getTemplate().getDyeSecondaryAmountGrams();
          } else if (dyeOverride > 0) {
            dyeOverride = (int)(dyeOverride * 0.3F);
          }
        }
        if (dyeOverride > 0) {
          colourNeeded = dyeOverride;
        } else {
          colourNeeded = (int)Math.max(1.0D, target.getSurfaceArea() * (primary ? 1.0D : 0.3D) / 25.0D);
        }
        colourNeeded = Math.max(1, colourNeeded / 2);
        if ((!insta) && (colourNeeded > brush.getWeightGrams()))
        {
          performer.getCommunicator().sendNormalServerMessage("You need more lye (" + colourNeeded + "g) to bleach that item.");
          return true;
        }
      }
    }
    done = false;
    if (act.currentSecond() == 1)
    {
      String type;
      String type;
      if (brush.getTemplateId() == 441) {
        type = "brush";
      } else {
        type = "bleach";
      }
      act.setTimeLeft(150);
      performer.getCommunicator().sendNormalServerMessage("You start to " + type + " the " + target.getName() + ".");
      Server.getInstance().broadCastAction(performer
        .getName() + " starts to " + type + " " + target.getNameWithGenus() + ".", performer, 5);
      String verb;
      String verb;
      if ((brush.getTemplateId() == 441) && (target.getTemplateId() != 1396)) {
        verb = Actions.actionEntrys['è'].getVerbString();
      } else {
        verb = Actions.actionEntrys['Μ'].getVerbString();
      }
      performer.sendActionControl(verb, true, act.getTimeLeft());
    }
    else
    {
      int timeleft = act.getTimeLeft();
      if ((insta) || (act.getCounterAsFloat() * 10.0F >= timeleft))
      {
        done = true;
        if (primary)
        {
          target.setColor(-1);
          if (brush.getTemplateId() == 441) {
            brush.setDamage((float)(brush.getDamage() + 0.5D * brush.getDamageModifier()));
          } else {
            brush.setWeight(brush.getWeightGrams() - colourNeeded, true);
          }
          if (sItem.length() == 0) {
            performer.getCommunicator().sendNormalServerMessage("You remove the colour from the " + target
              .getName() + ".");
          } else {
            performer.getCommunicator().sendNormalServerMessage("You remove the colour from the " + target
              .getName() + "'s " + sItem + ".");
          }
        }
        else
        {
          target.setColor2(-1);
          if (brush.getTemplateId() == 441) {
            brush.setDamage((float)(brush.getDamage() + 0.5D * brush.getDamageModifier()));
          } else {
            brush.setWeight(brush.getWeightGrams() - colourNeeded, true);
          }
          if (sItem.length() == 0) {
            performer.getCommunicator().sendNormalServerMessage("You remove the colour from the " + target
              .getName() + ".");
          } else {
            performer.getCommunicator().sendNormalServerMessage("You remove the colour from the " + target
              .getName() + "'s " + sItem + ".");
          }
        }
      }
    }
    return done;
  }
  
  static final boolean attachBags(Creature performer, Item bags, Item saddle, Action act)
  {
    boolean done = true;
    if ((bags.getTemplateId() == 1333) && (saddle.getTemplateId() == 621))
    {
      if (saddle.getAuxData() == 0)
      {
        saddle.setAuxData((byte)1);
        if (bags.getRarity() > saddle.getRarity()) {
          saddle.setRarity(bags.getRarity());
        }
        Items.destroyItem(bags.getWurmId());
        Item topParent = saddle.getTopParentOrNull();
        if ((topParent != null) && (topParent.isHollow())) {
          try
          {
            Creature[] watchers = saddle.getWatchers();
            if (watchers != null)
            {
              long inventoryWindow = saddle.getTopParent();
              if (topParent.isInventory()) {
                inventoryWindow = -1L;
              }
              for (Creature watcher : watchers)
              {
                watcher.getCommunicator().sendRemoveFromInventory(saddle, inventoryWindow);
                watcher.getCommunicator().sendAddToInventory(saddle, inventoryWindow, -1L, -1);
              }
            }
          }
          catch (NoSuchCreatureException localNoSuchCreatureException) {}
        }
        performer.getCommunicator().sendNormalServerMessage("You add bags to the saddle.");
        Server.getInstance().broadCastAction(performer.getName() + " adds bags to a saddle.", performer, 5);
      }
      else
      {
        performer.getCommunicator().sendNormalServerMessage("The saddle already has bags attached.");
      }
    }
    else {
      performer.getCommunicator().sendNormalServerMessage("You can not attach the " + bags.getName() + " to the " + saddle.getName() + ".");
    }
    return done;
  }
  
  static final boolean removeBags(Creature performer, Item saddle, Action act)
  {
    boolean done = true;
    if (saddle.getTemplateId() == 621)
    {
      if (saddle.getAuxData() == 1)
      {
        if (saddle.getItemCount() > 0) {
          performer.getCommunicator().sendNormalServerMessage("You must empty the bags before removing them.");
        } else {
          try
          {
            saddle.setAuxData((byte)0);
            Item bags = ItemFactory.createItem(1333, 20.0F, (byte)16, (byte)0, null);
            Item inventory = performer.getInventory();
            inventory.insertItem(bags);
            Item topParent = saddle.getTopParentOrNull();
            if ((topParent != null) && (topParent.isHollow())) {
              try
              {
                Creature[] watchers = saddle.getWatchers();
                if (watchers != null)
                {
                  long inventoryWindow = saddle.getTopParent();
                  if (topParent.isInventory()) {
                    inventoryWindow = -1L;
                  }
                  for (Creature watcher : watchers)
                  {
                    watcher.getCommunicator().sendRemoveFromInventory(saddle, inventoryWindow);
                    watcher.getCommunicator().sendAddToInventory(saddle, inventoryWindow, -1L, -1);
                  }
                }
              }
              catch (NoSuchCreatureException localNoSuchCreatureException) {}
            }
            performer.getCommunicator().sendNormalServerMessage("You remove the bags from the saddle.");
            Server.getInstance().broadCastAction(performer
              .getName() + " removes the bags from a saddle.", performer, 5);
          }
          catch (FailedException fe)
          {
            logger.log(Level.WARNING, performer.getName() + " " + fe.getMessage(), fe);
          }
          catch (NoSuchTemplateException nst)
          {
            logger.log(Level.WARNING, performer.getName() + " " + nst.getMessage(), nst);
          }
        }
      }
      else {
        performer.getCommunicator().sendNormalServerMessage("The " + saddle.getName() + " does not have bags attached.");
      }
    }
    else {
      performer.getCommunicator().sendNormalServerMessage("You can not remove bags from the " + saddle.getName() + ".");
    }
    return done;
  }
  
  static final boolean string(Creature performer, Item bowstring, Item bow, Action act)
  {
    boolean done = true;
    if (bowstring.getTemplateId() == 457)
    {
      boolean insta = performer.getPower() >= 5;
      if (!bow.isBowUnstringed())
      {
        performer.getCommunicator().sendNormalServerMessage("The " + bow.getName() + " cannot be strung.");
      }
      else
      {
        done = false;
        try
        {
          Item parent = bow.getParent();
          if (parent.isBodyPartAttached()) {
            if (parent.getPlace() == 13)
            {
              Item rightHandWeapon = performer.getRighthandWeapon();
              if (rightHandWeapon != null)
              {
                performer.getCommunicator().sendNormalServerMessage("You can not string the " + bow
                  .getName() + " while wielding " + rightHandWeapon
                  .getNameWithGenus() + ".");
                return true;
              }
            }
            else if (parent.getPlace() == 14)
            {
              Item leftHandWeapon = performer.getLefthandWeapon();
              if (leftHandWeapon != null)
              {
                performer.getCommunicator().sendNormalServerMessage("You can not string the " + bow
                  .getName() + " while wielding " + leftHandWeapon
                  .getNameWithGenus() + ".");
                return true;
              }
            }
          }
        }
        catch (NoSuchItemException localNoSuchItemException) {}
        if (act.currentSecond() == 1)
        {
          act.setTimeLeft(150);
          performer.getCommunicator().sendNormalServerMessage("You start to string the " + bow.getName() + ".");
          Server.getInstance().broadCastAction(performer
            .getName() + " starts to string " + bow.getNameWithGenus() + ".", performer, 5);
          performer.sendActionControl(Actions.actionEntrys[''].getVerbString(), true, act.getTimeLeft());
        }
        else
        {
          int timeleft = act.getTimeLeft();
          if ((insta) || (act.getCounterAsFloat() * 10.0F >= timeleft))
          {
            if (bowstring.getRarity() != 0) {
              performer.playPersonalSound("sound.fx.drumroll");
            }
            int realTemplate = 447;
            if (bow.getTemplateId() == 461) {
              realTemplate = 449;
            } else if (bow.getTemplateId() == 460) {
              realTemplate = 448;
            }
            done = true;
            bow.setTemplateId(realTemplate);
            bow.setAuxData((byte)(int)bowstring.getCurrentQualityLevel());
            if ((bowstring.getRarity() != 0) && (Server.rand.nextInt(100) == 0) && 
              (bowstring.getRarity() > bow.getRarity())) {
              bow.setRarity(bowstring.getRarity());
            }
            Items.destroyItem(bowstring.getWurmId());
            performer.getCommunicator().sendNormalServerMessage("You string the " + bow.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " strings " + bow.getNameWithGenus() + ".", performer, 5);
          }
        }
      }
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("You cannot use the " + bowstring
        .getName() + " to string the bow.");
    }
    return done;
  }
  
  static final boolean stringRod(Creature performer, Item string, Item rod, Action act)
  {
    boolean done = true;
    if ((string.getTemplateId() == 150) || 
      (string.getTemplateId() == 151))
    {
      boolean insta = performer.getPower() >= 5;
      if (rod.getTemplateId() != 780)
      {
        performer.getCommunicator().sendNormalServerMessage("The " + rod.getName() + " cannot be stringed.");
      }
      else
      {
        done = false;
        if (act.currentSecond() == 1)
        {
          act.setTimeLeft(150);
          performer.getCommunicator().sendNormalServerMessage("You start to string the " + rod.getName() + ".");
          Server.getInstance().broadCastAction(performer
            .getName() + " starts to string " + rod.getNameWithGenus() + ".", performer, 5);
          performer.sendActionControl(Actions.actionEntrys[''].getVerbString(), true, act.getTimeLeft());
        }
        else
        {
          int timeleft = act.getTimeLeft();
          if ((insta) || (act.getCounterAsFloat() * 10.0F >= timeleft))
          {
            if (string.getRarity() != 0) {
              performer.playPersonalSound("sound.fx.drumroll");
            }
            int realTemplate = 94;
            if (string.getTemplateId() == 150) {
              realTemplate = 94;
            } else if (string.getTemplateId() == 151) {
              realTemplate = 152;
            }
            done = true;
            rod.setTemplateId(realTemplate);
            if ((string.getRarity() != 0) && (Server.rand.nextInt(100) == 0) && 
              (string.getRarity() > rod.getRarity())) {
              rod.setRarity(string.getRarity());
            }
            Items.destroyItem(string.getWurmId());
            performer.getCommunicator().sendNormalServerMessage("You string the " + rod.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " strings " + rod.getNameWithGenus() + ".", performer, 5);
          }
        }
      }
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("You cannot use the " + string
        .getName() + " to string the rod.");
    }
    return done;
  }
  
  static final boolean unstringBow(Creature performer, Item bow, Action act, float counter)
  {
    boolean done = true;
    if (bow.getTopParent() != performer.getInventory().getWurmId())
    {
      performer.getCommunicator().sendNormalServerMessage("You must first pick the " + bow
        .getTemplate().getName() + " up in order to do that.");
      return true;
    }
    if (bow.isBowUnstringed())
    {
      performer.getCommunicator().sendNormalServerMessage("The bow is already unstringed.");
    }
    else if (bow.isWeaponBow())
    {
      boolean insta = performer.getPower() >= 5;
      done = false;
      if (act.currentSecond() == 1)
      {
        act.setTimeLeft(150);
        performer.getCommunicator().sendNormalServerMessage("You start to unstring the " + bow.getName() + ".");
        Server.getInstance().broadCastAction(performer
          .getName() + " starts to unstring " + bow.getNameWithGenus() + ".", performer, 5);
        performer
          .sendActionControl(Actions.actionEntrys[''].getVerbString(), true, act.getTimeLeft());
      }
      else
      {
        int timeleft = act.getTimeLeft();
        if ((insta) || (act.getCounterAsFloat() * 10.0F >= timeleft))
        {
          done = true;
          byte ql = (byte)Math.max(2, bow.getAuxData() - 1);
          try
          {
            Item bowstring = ItemFactory.createItem(457, ql, null);
            int realTemplate = 459;
            if (bow.getTemplateId() == 449) {
              realTemplate = 461;
            } else if (bow.getTemplateId() == 448) {
              realTemplate = 460;
            }
            performer.getCommunicator().sendNormalServerMessage("You unstring the " + bow.getName() + ".");
            Server.getInstance().broadCastAction(performer
              .getName() + " unstrings " + performer.getHisHerItsString() + " " + bow.getName() + ".", performer, 5);
            
            bow.setTemplateId(realTemplate);
            bow.setAuxData((byte)0);
            performer.getInventory().insertItem(bowstring, true);
          }
          catch (NoSuchTemplateException nst)
          {
            logger.log(Level.WARNING, performer.getName() + ":" + bow.getName() + " " + nst.getMessage(), nst);
            performer.getCommunicator().sendNormalServerMessage("You fail. The string seems stuck.");
          }
          catch (FailedException nf)
          {
            logger.log(Level.WARNING, performer.getName() + ":" + bow.getName() + " " + nf.getMessage(), nf);
            performer.getCommunicator().sendNormalServerMessage("You fail. The string seems stuck.");
          }
        }
      }
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("You can not unstring that!");
    }
    return done;
  }
  
  static final boolean smear(Creature performer, Item potion, Item target, Action act, float counter)
  {
    boolean done = false;
    if (!Spell.mayBeEnchanted(target))
    {
      performer.getCommunicator().sendNormalServerMessage("You can't imbue that.");
      return true;
    }
    try
    {
      potionTemplate = ItemTemplateFactory.getInstance().getTemplate(potion.getTemplateId());
    }
    catch (NoSuchTemplateException e)
    {
      ItemTemplate potionTemplate;
      performer.getCommunicator().sendAlertServerMessage("ERROR: Could not load item template for" + potion
        .getName() + ". Please report this.");
      logger.warning("Could not locate template for " + potion.getName() + " with wid=" + potion
        .getWurmId() + ". " + e);
      return true;
    }
    ItemTemplate potionTemplate;
    if (potion.isLiquid()) {
      if (potion.getWeightGrams() < potionTemplate.getWeightGrams())
      {
        performer.getCommunicator().sendNormalServerMessage("The " + potion.getName() + " does not contain enough liquid to smear the " + target
          .getName());
        return true;
      }
    }
    if ((!done) && (counter == 1.0F))
    {
      byte enchantment = potion.getEnchantForPotion();
      if ((enchantment == 91) || (enchantment == 90) || (enchantment == 92)) {
        if (target.enchantment != 0)
        {
          performer.getCommunicator().sendNormalServerMessage("The " + target
            .getName() + " already has that kind of enchantment.");
          return true;
        }
      }
      ItemSpellEffects currentEffects = target.getSpellEffects();
      if (currentEffects == null) {
        currentEffects = new ItemSpellEffects(target.getWurmId());
      }
      SpellEffect eff = currentEffects.getSpellEffect(enchantment);
      if ((eff != null) && (enchantment == 98))
      {
        performer.getCommunicator().sendNormalServerMessage("The " + target
          .getName() + " is already protected against shattering.");
        return true;
      }
      if ((eff != null) && (eff.getPower() >= 100.0F))
      {
        performer.getCommunicator().sendNormalServerMessage("The power of the " + target
          .getName() + " is already at max.");
        return true;
      }
      act.setTimeLeft(200);
      performer.getCommunicator().sendNormalServerMessage("You start to smear the " + potion
        .getName() + " on the " + target.getName() + ".");
      Server.getInstance().broadCastAction(performer
        .getName() + " starts to smear " + potion.getNameWithGenus() + " on " + target
        .getNameWithGenus() + ".", performer, 5);
      performer.sendActionControl(Actions.actionEntrys['ɹ'].getVerbString(), true, act.getTimeLeft());
    }
    if ((!done) && (counter > act.getTimeLeft() / 10))
    {
      done = true;
      performer.getCommunicator().sendNormalServerMessage("You imbue " + target
        .getNameWithGenus() + " with " + potion.getNameWithGenus() + ".");
      Server.getInstance().broadCastAction(performer.getName() + " imbues " + target.getNameWithGenus() + ".", performer, 5);
      
      Skill alch = null;
      try
      {
        alch = performer.getSkills().getSkill(10042);
      }
      catch (NoSuchSkillException nss)
      {
        alch = performer.getSkills().learn(10042, 1.0F);
      }
      byte enchantment = potion.getEnchantForPotion();
      if ((enchantment == 91) || (enchantment == 90) || (enchantment == 92))
      {
        if (target.enchantment != 0)
        {
          performer.getCommunicator().sendNormalServerMessage("The " + target
            .getName() + " already has that kind of enchantment.");
          return true;
        }
        target.enchant(enchantment);
        String damtype = "fire";
        if (enchantment == 90) {
          damtype = "acid";
        } else if (enchantment == 92) {
          damtype = "frost";
        }
        performer.getCommunicator().sendNormalServerMessage("The " + target
          .getName() + " will now deal " + damtype + " damage.");
      }
      else
      {
        ItemSpellEffects spellEffects = target.getSpellEffects();
        if (spellEffects == null) {
          spellEffects = new ItemSpellEffects(target.getWurmId());
        }
        SpellEffect eff = spellEffects.getSpellEffect(enchantment);
        double skpower = alch.skillCheck(50.0D, potion.getCurrentQualityLevel(), false, 1.0F);
        double power = 5.0D + Math.max(20.0D, skpower) / 20.0D + potion.getCurrentQualityLevel() / 20.0F;
        if ((eff == null) && (potion.getTemplateId() == 1091))
        {
          float toReturnPower = 100.0F;
          
          performer.getCommunicator().sendNormalServerMessage("The " + target
            .getName() + " will now be protected against cracking and shattering.");
          eff = new SpellEffect(target.getWurmId(), enchantment, toReturnPower, 20000000);
          spellEffects.addSpellEffect(eff);
          Server.getInstance().broadCastAction(performer.getName() + " looks pleased.", performer, 5);
        }
        else if (eff == null)
        {
          performer.getCommunicator().sendNormalServerMessage("The " + target
            .getName() + " will now be more effective when used for the specific purpose.");
          eff = new SpellEffect(target.getWurmId(), enchantment, (float)power, 20000000);
          spellEffects.addSpellEffect(eff);
          Server.getInstance().broadCastAction(performer.getName() + " looks pleased.", performer, 5);
        }
        else if (eff.getPower() < 100.0F)
        {
          performer.getCommunicator().sendNormalServerMessage("You succeed in improving the power of the effectiveness of the " + target
            .getName() + ".");
          eff.setPower(Math.min(100.0F, eff.getPower() + (float)power));
          Server.getInstance().broadCastAction(performer.getName() + " looks pleased.", performer, 5);
        }
      }
      potion.setWeight(potion.getWeightGrams() - potionTemplate.getWeightGrams(), true);
    }
    return done;
  }
  
  static final boolean createOil(Creature performer, Item sourceSalt, Item target, Action act, float counter)
  {
    boolean done = true;
    Skill alch = null;
    try
    {
      alch = performer.getSkills().getSkill(10042);
    }
    catch (NoSuchSkillException nss)
    {
      alch = performer.getSkills().learn(10042, 1.0F);
    }
    int knowl = (int)alch.getKnowledge(0.0D);
    done = false;
    if (sourceSalt.getWeightGrams() < sourceSalt.getTemplate().getWeightGrams() * 10)
    {
      performer.getCommunicator().sendNormalServerMessage("The " + sourceSalt
        .getName() + " contains too little material.");
      return true;
    }
    if (target.getWeightGrams() < target.getTemplate().getWeightGrams())
    {
      performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " contains too little material.");
      return true;
    }
    if (!performer.getInventory().mayCreatureInsertItem())
    {
      performer.getCommunicator().sendNormalServerMessage("You do not have enough room in your inventory.");
      return true;
    }
    if ((!done) && (counter == 1.0F))
    {
      int time = 200;
      time = Math.max(50, time - knowl);
      act.setTimeLeft(time);
      performer.getCommunicator().sendNormalServerMessage("You start to create something from the " + sourceSalt
        .getName() + " and the " + target.getName() + ".");
      Server.getInstance().broadCastAction(performer
        .getName() + " starts to create something from " + sourceSalt.getNameWithGenus() + " and " + target
        .getNameWithGenus() + ".", performer, 5);
      performer.sendActionControl(Actions.actionEntrys['ě'].getVerbString(), true, act.getTimeLeft());
    }
    if ((!done) && (counter > act.getTimeLeft() / 10))
    {
      done = true;
      double power = alch.skillCheck(20.0D, sourceSalt, 0.0D, false, counter);
      if (power > 0.0D)
      {
        try
        {
          Item potion = ItemFactory.createItem(target.getPotionTemplateIdForBlood(), (float)power, null);
          performer.getCommunicator().sendNormalServerMessage("You create " + potion.getNameWithGenus() + ".");
          Server.getInstance().broadCastAction(performer.getName() + " creates " + potion.getNameWithGenus() + ".", performer, 5);
          
          int allWeight = sourceSalt.getTemplate().getWeightGrams() * 10 + target.getTemplate().getWeightGrams();
          potion.setWeight(allWeight, true);
          performer.getInventory().insertItem(potion, true);
        }
        catch (Exception ex)
        {
          performer.getCommunicator().sendNormalServerMessage("You fail to create anything useful.");
          Server.getInstance().broadCastAction(performer.getName() + " fails to create anything useful.", performer, 5);
        }
      }
      else
      {
        performer.getCommunicator().sendNormalServerMessage("You fail to create anything useful.");
        Server.getInstance().broadCastAction(performer.getName() + " fails to create anything useful.", performer, 5);
      }
      sourceSalt.setWeight(sourceSalt.getWeightGrams() - sourceSalt.getTemplate().getWeightGrams() * 10, true);
      target.setWeight(target.getWeightGrams() - target.getTemplate().getWeightGrams(), true);
    }
    return done;
  }
  
  static final boolean createSalve(Creature performer, Item source, Item target, Action act, float counter)
  {
    boolean done = true;
    Skill alch = null;
    try
    {
      alch = performer.getSkills().getSkill(10042);
    }
    catch (NoSuchSkillException nss)
    {
      alch = performer.getSkills().learn(10042, 1.0F);
    }
    int knowl = (int)alch.getKnowledge(0.0D);
    int pow = source.getAlchemyType() * target.getAlchemyType();
    done = false;
    if (source.getWeightGrams() < source.getTemplate().getWeightGrams())
    {
      performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " contains too little material.");
      return true;
    }
    if (target.getWeightGrams() < target.getTemplate().getWeightGrams())
    {
      performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " contains too little material.");
      return true;
    }
    if (!performer.getInventory().mayCreatureInsertItem())
    {
      performer.getCommunicator().sendNormalServerMessage("You do not have enough room in your inventory.");
      return true;
    }
    if ((!done) && (counter == 1.0F))
    {
      int time = 200;
      time = Math.max(50, time - knowl);
      act.setTimeLeft(time);
      performer.getCommunicator().sendNormalServerMessage("You start to create a healing cover of the " + source
        .getName() + ", the " + target.getName() + " and some grass, leaves and moss.");
      
      Server.getInstance().broadCastAction(performer
        .getName() + " starts to create something from " + source.getNameWithGenus() + ", " + target
        .getNameWithGenus() + " and some grass, leaves and moss.", performer, 5);
      performer.sendActionControl(Actions.actionEntrys['ě'].getVerbString(), true, act.getTimeLeft());
    }
    if ((!done) && (counter > act.getTimeLeft() / 10))
    {
      done = true;
      double power = alch.skillCheck(pow, source, 0.0D, false, counter);
      if (power > 0.0D)
      {
        try
        {
          Item cover = ItemFactory.createItem(481, (float)power, null);
          performer.getCommunicator().sendNormalServerMessage("You create a healing cover.");
          Server.getInstance().broadCastAction(performer.getName() + " creates a healing cover.", performer, 5);
          int allWeight = source.getTemplate().getWeightGrams() + target.getTemplate().getWeightGrams();
          cover.setAuxData((byte)pow);
          cover.setWeight(allWeight, true);
          cover.setDescription("made from " + source.getName().toLowerCase() + " and " + target
            .getName().toLowerCase());
          performer.getInventory().insertItem(cover, true);
          
          performer.achievement(546);
        }
        catch (Exception ex)
        {
          performer.getCommunicator().sendNormalServerMessage("You fail to create a healing cover.");
          Server.getInstance().broadCastAction(performer.getName() + " fails to create a healing cover.", performer, 5);
        }
      }
      else
      {
        performer.getCommunicator().sendNormalServerMessage("You fail to create a healing cover.");
        Server.getInstance().broadCastAction(performer.getName() + " fails to create a healing cover.", performer, 5);
      }
      source.setWeight(source.getWeightGrams() - source.getTemplate().getWeightGrams(), true);
      target.setWeight(target.getWeightGrams() - target.getTemplate().getWeightGrams(), true);
    }
    return done;
  }
  
  static final boolean createPotion(Creature performer, Item source, Item target, Action act, float counter, byte potionAuxData)
  {
    boolean done = true;
    Skill alch = performer.getSkills().getSkillOrLearn(10042);
    int knowl = (int)alch.getKnowledge(0.0D);
    done = false;
    
    int totalWeight = 0;
    Item[] contents = target.getAllItems(false);
    int templateWeight = 0;
    boolean allSame = false;
    float solidAverageQL = 0.0F;
    if (contents.length >= 1)
    {
      allSame = true;
      
      int lookingFor = contents[0].getTemplateId();
      templateWeight = contents[0].getTemplate().getWeightGrams();
      for (Item i : contents)
      {
        int solidWeight = i.getWeightGrams();
        if (i.getTemplateId() != lookingFor)
        {
          allSame = false;
          break;
        }
        float qlwt = solidAverageQL * totalWeight;
        qlwt += solidWeight * i.getCurrentQualityLevel();
        totalWeight += solidWeight;
        solidAverageQL = qlwt / totalWeight;
      }
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("There is nothing in the " + target
        .getName() + " to do anything with.");
      return true;
    }
    if (!allSame)
    {
      performer.getCommunicator().sendNormalServerMessage("There are contaminants in the " + target
        .getName() + ", so liquid would be ruined.");
      return true;
    }
    int numbItems = totalWeight / templateWeight;
    if (numbItems == 0)
    {
      performer.getCommunicator().sendNormalServerMessage("There is not enough of " + contents[0]
        .getName() + " to do anything with.");
      return true;
    }
    int neededLiquid = (int)(numbItems * 50.0F);
    if (neededLiquid > source.getWeightGrams())
    {
      performer.getCommunicator().sendNormalServerMessage("You dont have enough " + source
        .getName() + " for that much " + contents[0]
        .getName() + ".");
      return true;
    }
    if ((!done) && (counter == 1.0F))
    {
      int time = 200;
      time = Math.max(50, time - knowl);
      act.setTimeLeft(time);
      performer.getCommunicator().sendNormalServerMessage("You start to create some transmutation liquid using " + source
        .getName() + " on the " + contents[0].getName() + ".");
      Server.getInstance().broadCastAction(performer
        .getName() + " starts to create something from " + source.getNameWithGenus() + " and " + contents[0]
        .getNameWithGenus(), performer, 5);
      performer.sendActionControl(Actions.actionEntrys['ě'].getVerbString(), true, act.getTimeLeft());
    }
    if (act.currentSecond() == 2) {
      performer.getCommunicator().sendNormalServerMessage("You see the " + contents[0].getName() + " absorb the " + source.getName() + ".");
    } else if (act.currentSecond() == 4) {
      performer.getCommunicator().sendNormalServerMessage("Now the " + contents[0].getName() + " starts to effervesce.");
    } else if (act.currentSecond() == 6) {
      performer.getCommunicator().sendNormalServerMessage("The bubbles now obscure the " + contents[0].getName() + ".");
    } else if (act.currentSecond() == 8) {
      performer.getCommunicator().sendNormalServerMessage("The bubbles start receeding.");
    }
    if (act.currentSecond() % 5 == 0) {
      performer.getStatus().modifyStamina(-1000.0F);
    }
    if ((!done) && (counter > act.getTimeLeft() / 10))
    {
      int difficulty = getTransmutationLiquidDifficulty(potionAuxData);
      if (act.getRarity() != 0)
      {
        performer.playPersonalSound("sound.fx.drumroll");
        switch (act.getRarity())
        {
        case 1: 
          difficulty = (int)(difficulty * 0.8F);
          break;
        case 2: 
          difficulty = (int)(difficulty * 0.5F);
          break;
        case 3: 
          difficulty = (int)(difficulty * 0.2F);
        }
      }
      done = true;
      float alc = 0.0F;
      if (performer.isPlayer()) {
        alc = ((Player)performer).getAlcohol();
      }
      float bonus = (solidAverageQL + source.getCurrentQualityLevel()) / 2.0F;
      double power = alch.skillCheck(difficulty + alc, source, bonus, false, counter);
      switch (act.getRarity())
      {
      case 1: 
        power *= 1.2000000476837158D;
        break;
      case 2: 
        power *= 1.5D;
        break;
      case 3: 
        power *= 1.7999999523162842D;
      }
      power = Math.min(power, 100.0D);
      if (power > 0.0D)
      {
        try
        {
          Item potion = ItemFactory.createItem(654, (float)power, null);
          
          potion.setAuxData(potionAuxData);
          
          potion.setWeight(numbItems * templateWeight, false);
          potion.setDescription(getTransmutationLiquidDescription(potionAuxData));
          for (Item i : contents) {
            Items.destroyItem(i.getWurmId());
          }
          source.setWeight(source.getWeightGrams() - neededLiquid, true);
          
          target.insertItem(potion, true);
          performer.getCommunicator().sendNormalServerMessage("You create some transmutation liquid.");
          Server.getInstance().broadCastAction(performer.getName() + " creates some transmutation liquid.", performer, 5);
        }
        catch (Exception ex)
        {
          performer.getCommunicator().sendNormalServerMessage("You fail to create the transmutation liquid.");
          Server.getInstance().broadCastAction(performer.getName() + " fails to create the transmutation liquid.", performer, 5);
        }
      }
      else
      {
        performer.getCommunicator().sendNormalServerMessage("You fail to create the transmutation liquid.");
        Server.getInstance().broadCastAction(performer.getName() + " fails to create the transmutation liquid.", performer, 5);
      }
    }
    return done;
  }
  
  static final boolean askSleep(Action act, Creature performer, Item target, float counter)
  {
    if (!performer.isPlayer()) {
      return true;
    }
    if (performer.isGuest())
    {
      performer.getCommunicator().sendNormalServerMessage("Guests are not allowed to sleep in beds.");
      return true;
    }
    if (((Player)performer).getSaveFile().bed > 0L)
    {
      performer.getCommunicator().sendNormalServerMessage("You are already asleep. Or are you sleepwalking?");
      return true;
    }
    PlayerInfo info = PlayerInfoFactory.getPlayerSleepingInBed(target.getWurmId());
    if (info != null)
    {
      performer.getCommunicator().sendNormalServerMessage("The bed is already occupied by " + info.getName() + ".");
      return true;
    }
    VolaTile t = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface());
    if (t != null)
    {
      if ((t.getStructure() == null) || (!t.getStructure().isTypeHouse()))
      {
        performer.getCommunicator().sendNormalServerMessage("You would get no sleep outside tonight.");
        return true;
      }
      if (!t.getStructure().isFinished())
      {
        performer.getCommunicator().sendNormalServerMessage("The house is too windy to provide protection.");
        return true;
      }
      if (target.getWhenRented() > System.currentTimeMillis() - 86400000L)
      {
        if (target.getData() != performer.getWurmId())
        {
          PlayerInfo renter = PlayerInfoFactory.getPlayerInfoWithWurmId(target.getData());
          if (renter != null)
          {
            performer.getCommunicator().sendNormalServerMessage(renter.getName() + " has already rented that bed, so you are unable to use it.");
            return true;
          }
          target.setWhenRented(0L);
        }
      }
      else {
        target.setWhenRented(0L);
      }
      if (((target.getRentCost() > 0) && (target.getData() == performer.getWurmId()) ? 1 : 0) == 0) {
        if (!target.mayUseBed(performer))
        {
          performer.getCommunicator().sendNormalServerMessage("You are not allowed to sleep here.");
          return true;
        }
      }
    }
    else
    {
      logger.log(Level.WARNING, "Why is tile for bed at " + target
        .getTileX() + "," + target.getTileY() + "," + target.isOnSurface() + " null?");
    }
    SleepQuestion spm = new SleepQuestion(performer, "Go to sleep?", "Sleep:", target.getWurmId());
    spm.sendQuestion();
    return true;
  }
  
  static final boolean sleep(Action act, Creature performer, Item target, float counter)
  {
    boolean done = true;
    if (!performer.isPlayer()) {
      return true;
    }
    if ((((Player)performer).getSaveFile().bed > 0L) && (counter <= act.getPower()))
    {
      performer.getCommunicator().sendNormalServerMessage("You are already asleep. Or are you sleepwalking?");
      return true;
    }
    if (target.getTopParent() != target.getWurmId())
    {
      performer.getCommunicator().sendNormalServerMessage("The bed needs to be on the ground.");
      return true;
    }
    PlayerInfo info = PlayerInfoFactory.getPlayerSleepingInBed(target.getWurmId());
    if (info != null) {
      if (info.wurmId != performer.getWurmId())
      {
        performer.getCommunicator().sendNormalServerMessage("The bed is already occupied by " + info
          .getName() + ".");
        performer.achievement(100);
        return true;
      }
    }
    VolaTile t = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface());
    if (t != null)
    {
      if (performer.getPower() <= 0)
      {
        if ((t.getStructure() == null) || (!t.getStructure().isTypeHouse()))
        {
          performer.getCommunicator().sendNormalServerMessage("You would get no sleep outside tonight.");
          return true;
        }
        if (!t.getStructure().isFinished())
        {
          performer.getCommunicator().sendNormalServerMessage("The house is too windy to provide protection.");
          return true;
        }
        if (((target.getRentCost() > 0) && (target.getData() == performer.getWurmId()) ? 1 : 0) == 0) {
          if (!target.mayUseBed(performer))
          {
            performer.getCommunicator().sendNormalServerMessage("You are not allowed to sleep here.");
            return true;
          }
        }
        if (target.getData() != performer.getWurmId()) {}
      }
      done = false;
    }
    else
    {
      logger.log(Level.WARNING, "Why is tile for bed at " + target.getTileX() + "," + target.getTileY() + "," + target
        .isOnSurface() + " null?");
    }
    if (counter == 1.0F)
    {
      int timetologout = ((Player)performer).getSecondsToLogout() * 10;
      act.setPower(((Player)performer).getSecondsToLogout());
      performer.getCommunicator().sendNormalServerMessage("You start to go to sleep.");
      Server.getInstance().broadCastAction(performer
        .getName() + " starts to go to sleep in " + target.getNameWithGenus() + ".", performer, 5);
      performer.sendActionControl("Sleep - logging off", true, timetologout);
    }
    if (act.currentSecond() > act.getPower()) {
      if (act.justTickedSecond())
      {
        performer.getCommunicator().sendShutDown("You went to sleep. Sweet dreams.", true);
        ((Player)performer).getSaveFile().setBed(target.getWurmId());
        ((Player)performer).setLogout();
        Server.getInstance().broadCastAction(performer
          .getName() + " goes to sleep in " + target.getNameWithGenus() + ".", performer, 5);
        target.setDamage(target.getDamage() + 0.01F * target.getDamageModifier());
        if (act.currentSecond() > act.getPower() + 2.0F)
        {
          done = true;
          if (performer.getCurrentTile() != null) {
            performer.getCurrentTile().deleteCreature(performer);
          }
          Players.getInstance().logoutPlayer((Player)performer);
          
          performer.achievement(547);
        }
      }
    }
    return done;
  }
  
  public static boolean mayDropOnTile(int tileX, int tileY, boolean surfaced, int floorLevel)
  {
    VolaTile t = Zones.getTileOrNull(tileX, tileY, surfaced);
    if (t != null)
    {
      if (t.hasOnePerTileItem(t.getDropFloorLevel(floorLevel))) {
        return false;
      }
      if (t.getNumberOfItems(t.getDropFloorLevel(floorLevel)) >= 100) {
        return false;
      }
      if (t.getFourPerTileCount(floorLevel) >= 4) {
        return false;
      }
    }
    return true;
  }
  
  public static boolean mayDropOnTile(Creature performer)
  {
    float posX = performer.getStatus().getPositionX();
    float posY = performer.getStatus().getPositionY();
    float rot = performer.getStatus().getRotation();
    
    float xPosMod = (float)Math.sin(rot * 0.017453292F) * 2.0F;
    float yPosMod = -(float)Math.cos(rot * 0.017453292F) * 2.0F;
    posX += xPosMod;
    posY += yPosMod;
    
    int placedX = (int)posX >> 2;
    int placedY = (int)posY >> 2;
    
    return mayDropOnTile(placedX, placedY, performer.isOnSurface(), performer.getFloorLevel());
  }
  
  public static byte[] getAllNormalWoodTypes()
  {
    byte[] creationWoodTypes = { 14, 37, 38, 39, 40, 41, 42, 43, 44, 45, 63, 64, 65, 66, 88, 51, 46, 47, 48, 49, 50, 71, 90, 91 };
    
    return creationWoodTypes;
  }
  
  public static byte[] getAllMetalTypes()
  {
    byte[] metalTypes = { 7, 8, 9, 10, 11, 12, 13, 30, 31, 34, 56, 57, 67, 96 };
    
    return metalTypes;
  }
  
  public static boolean mayDropTentOnTile(Creature performer)
  {
    for (int x = performer.getTileX() - 1; x <= performer.getTileX() + 1; x++) {
      for (int y = performer.getTileY() - 1; y <= performer.getTileY() + 1; y++)
      {
        VolaTile t = Zones.getTileOrNull(Zones.safeTileX(x), Zones.safeTileY(y), performer.isOnSurface());
        if (t != null)
        {
          if ((t.getVillage() != null) && (t.getVillage() != performer.getCitizenVillage())) {
            return false;
          }
          if (Villages.getVillageWithPerimeterAt(Zones.safeTileX(x), Zones.safeTileY(y), true) != null) {
            return false;
          }
        }
        if (performer.isOnSurface())
        {
          int tile = Server.surfaceMesh.getTile(Zones.safeTileX(x), Zones.safeTileY(y));
          if (Tiles.decodeHeight(tile) < 1) {
            return false;
          }
        }
      }
    }
    return true;
  }
  
  static final boolean setRent(Action act, Creature performer, Item target)
  {
    if (!target.isBed())
    {
      performer.getCommunicator().sendNormalServerMessage("You may not rent that!");
      return true;
    }
    VolaTile t = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface());
    if (t != null)
    {
      if (t.getStructure() == null)
      {
        performer.getCommunicator().sendNormalServerMessage("There is no structure here, so you can not charge for the bed.");
        
        return true;
      }
      if (t.getStructure().isOwner(performer))
      {
        if (act.getNumber() == 319)
        {
          target.setAuxData((byte)1);
          performer.getCommunicator().sendNormalServerMessage("You set the rent to 1 copper.");
        }
        else if (act.getNumber() == 320)
        {
          target.setAuxData((byte)2);
          performer.getCommunicator().sendNormalServerMessage("You set the rent to 10 copper.");
        }
        else if (act.getNumber() == 321)
        {
          target.setAuxData((byte)3);
          performer.getCommunicator().sendNormalServerMessage("You set the rent to 1 silver.");
        }
        else if (act.getNumber() == 322)
        {
          target.setAuxData((byte)4);
          performer.getCommunicator().sendNormalServerMessage("You set the rent to 10 silver.");
        }
        else if (act.getNumber() == 365)
        {
          target.setAuxData((byte)5);
          performer.getCommunicator().sendNormalServerMessage("You set the rent to 10 iron.");
        }
        else if (act.getNumber() == 366)
        {
          target.setAuxData((byte)6);
          performer.getCommunicator().sendNormalServerMessage("You set the rent to 25 iron.");
        }
        else if (act.getNumber() == 367)
        {
          target.setAuxData((byte)7);
          performer.getCommunicator().sendNormalServerMessage("You set the rent to 50 iron.");
        }
        else if (act.getNumber() == 323)
        {
          target.setAuxData((byte)0);
          performer.getCommunicator().sendNormalServerMessage("The bed may now only be used by you and your guests.");
        }
      }
      else {
        performer.getCommunicator().sendNormalServerMessage("You do not own this house, so you can not charge for the bed.");
      }
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("There is no structure here, so you can not charge for the bed.");
    }
    return true;
  }
  
  static final boolean rent(Action act, Creature performer, Item target)
  {
    if (!target.isBed())
    {
      performer.getCommunicator().sendNormalServerMessage("You may not rent that!");
      return true;
    }
    if (performer.isGuest())
    {
      performer.getCommunicator().sendNormalServerMessage("Guests are not allowed to sleep in or hire beds.");
      return true;
    }
    if (performer.isPlayer()) {
      try
      {
        long oldbed = ((Player)performer).getSaveFile().bed;
        if ((oldbed > 0L) && (oldbed != target.getWurmId()))
        {
          Item beds = Items.getItem(oldbed);
          beds.setData(0L);
          beds.setWhenRented(0L);
          performer.getCommunicator().sendNormalServerMessage("Your old bed is now available for someone else to rent.");
        }
      }
      catch (NoSuchItemException localNoSuchItemException) {}
    }
    PlayerInfo pinf = PlayerInfoFactory.getPlayerSleepingInBed(target.getWurmId());
    if (pinf != null)
    {
      performer.getCommunicator().sendNormalServerMessage("Some kind of mysterious haze lingers over the bed, and you notice that the bed is occupied by the spirit of " + pinf
      
        .getName() + ".");
      return true;
    }
    if ((target.getData() > 0L) && (target.getData() != performer.getWurmId()))
    {
      PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(target.getData());
      if (info != null)
      {
        if (target.getWhenRented() > System.currentTimeMillis() - 86400000L)
        {
          performer.getCommunicator().sendNormalServerMessage(info.getName() + " has already rented that bed.");
          return true;
        }
        target.setWhenRented(0L);
      }
    }
    VolaTile t = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface());
    if (t != null)
    {
      if (t.getStructure() == null)
      {
        performer.getCommunicator().sendNormalServerMessage("There is no structure here, so you can not rent the bed.");
        return true;
      }
      if (t.getStructure().getOwnerId() == performer.getWurmId())
      {
        performer.getCommunicator().sendNormalServerMessage("You don't need to rent the bed in order to sleep in it.");
        return true;
      }
      Village vill = t.getVillage();
      long rentMoney = target.getRentCost();
      if (rentMoney > 0L)
      {
        LoginServerWebConnection conn = new LoginServerWebConnection();
        try
        {
          if (charge(performer, rentMoney))
          {
            if ((vill != null) && 
              (vill.plan != null))
            {
              vill.plan.addMoney(rentMoney / 2L);
              rentMoney /= 2L;
            }
            PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(t.getStructure().getOwnerId());
            if (info != null) {
              conn.addMoney(t.getStructure().getOwnerId(), info.getName(), rentMoney, "Rented bed");
            }
            target.setData(performer.getWurmId());
            target.setWhenRented(System.currentTimeMillis());
            performer.getCommunicator().sendNormalServerMessage("You rent the bed.");
            if (info != null) {
              try
              {
                Player p = Players.getInstance().getPlayer(info.wurmId);
                p.getCommunicator().sendNormalServerMessage("You rent a bed in " + t
                  .getStructure().getName() + " to " + performer.getName() + ".");
              }
              catch (NoSuchPlayerException localNoSuchPlayerException) {}
            }
          }
          else
          {
            performer.getCommunicator().sendNormalServerMessage("You cannot rent the bed right now. Make sure you have enough money in the bank.");
            
            return true;
          }
        }
        catch (Exception ex)
        {
          performer.getCommunicator().sendNormalServerMessage(ex.getMessage());
          return true;
        }
        return false;
      }
      logger.log(Level.INFO, "No Rent cost=" + target.getRentCost());
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("There is no structure here, so you can not charge for the bed.");
    }
    return true;
  }
  
  private static final boolean charge(Creature responder, long coinsNeeded)
    throws FailedException
  {
    Item[] items = responder.getInventory().getAllItems(false);
    List<Item> coins = new LinkedList();
    long value = 0L;
    for (Item lItem : items) {
      if (lItem.isCoin())
      {
        coins.add(lItem);
        value += Economy.getValueFor(lItem.getTemplateId());
      }
    }
    if (value < coinsNeeded)
    {
      Change change = new Change(coinsNeeded);
      throw new FailedException("You need " + change.getChangeString() + " coins.");
    }
    long curv = 0L;
    for (Object it = coins.listIterator(); ((ListIterator)it).hasNext();)
    {
      Item coin = (Item)((ListIterator)it).next();
      curv += Economy.getValueFor(coin.getTemplateId());
      try
      {
        Item parent = coin.getParent();
        parent.dropItem(coin.getWurmId(), false);
        Economy.getEconomy().returnCoin(coin, "Charged");
      }
      catch (NoSuchItemException nsi)
      {
        logger.log(Level.WARNING, responder
          .getName() + ":  Failed to locate the container for coin " + coin.getName() + ". Value returned is " + new Change(curv)
          .getChangeString() + " coins.");
        newCoins = Economy.getEconomy().getCoinsFor(Economy.getValueFor(coin.getTemplateId()));
        inventory = responder.getInventory();
        for (Item lNewCoin : newCoins) {
          inventory.insertItem(lNewCoin);
        }
        throw new FailedException("Failed to locate the container for coin " + coin.getName() + ". This is serious and should be reported. Returned " + new Change(curv).getChangeString() + " coins.");
      }
      if (curv >= coinsNeeded) {
        break;
      }
    }
    Item[] newCoins;
    Item inventory;
    if (curv > coinsNeeded)
    {
      Item[] newCoins = Economy.getEconomy().getCoinsFor(curv - coinsNeeded);
      Item inventory = responder.getInventory();
      nsi = newCoins;newCoins = nsi.length;
      for (inventory = 0; inventory < newCoins; inventory++)
      {
        Item lNewCoin = nsi[inventory];
        inventory.insertItem(lNewCoin);
      }
    }
    return true;
  }
  
  static final boolean watchSpyglass(Creature performer, Item spyglass, Action act, float counter)
  {
    boolean done = false;
    if (act.currentSecond() == 1)
    {
      performer.getCommunicator().sendNormalServerMessage("You start spying through the " + spyglass.getName() + ".");
      Server.getInstance().broadCastAction(performer
        .getName() + " starts spying through " + spyglass.getNameWithGenus() + ".", performer, 5);
      performer.sendActionControl(Actions.actionEntrys['ŉ'].getVerbString(), true, 300);
    }
    else if (act.currentSecond() >= 30)
    {
      done = true;
      performer.getCommunicator().sendNormalServerMessage("You stop spying through the " + spyglass.getName() + ".");
      Server.getInstance().broadCastAction(performer
        .getName() + " stops spying through " + spyglass.getNameWithGenus() + ".", performer, 5);
    }
    return done;
  }
  
  public static final boolean mayUseInventoryOfVehicle(Creature performer, Item vehicle)
  {
    if (performer.getWurmId() == vehicle.lastOwner) {
      return true;
    }
    return vehicle.mayAccessHold(performer);
  }
  
  public static final boolean hasKeyForContainer(Creature performer, Item target)
  {
    try
    {
      Item lock = Items.getItem(target.getLockId());
      if ((!lock.getLocked()) || (target.isOwner(performer))) {
        return true;
      }
      long[] keys = lock.getKeyIds();
      for (long keyId : keys)
      {
        Item key = Items.getItem(keyId);
        if (key.getTopParent() == performer.getInventory().getWurmId()) {
          return true;
        }
      }
    }
    catch (NoSuchItemException e)
    {
      logger.log(Level.WARNING, "Item not found for key " + e.getMessage(), e);
    }
    return false;
  }
  
  static boolean useBellTower(Creature performer, Item bellTower, Action act, float counter)
  {
    boolean toReturn = false;
    if (counter == 1.0F)
    {
      performer.getCommunicator().sendNormalServerMessage("You start swinging the bell.");
      Server.getInstance().broadCastAction(performer.getName() + " starts to swing the bell.", performer, 5);
      performer.sendActionControl("playing", true, 600);
      act.setTimeLeft(600);
      performer.getStatus().modifyStamina(-1000.0F);
    }
    else if (act.currentSecond() % 10 == 0)
    {
      performer.getStatus().modifyStamina(-1000.0F);
      String soundName = "sound.bell.dong.1";
      int sound = (int)(bellTower.getCurrentQualityLevel() / 10.0F);
      if (sound < 3) {
        soundName = "sound.bell.dong.1";
      } else if (sound < 5) {
        soundName = "sound.bell.dong.2";
      } else if (sound < 7) {
        soundName = "sound.bell.dong.3";
      } else if (sound < 8) {
        soundName = "sound.bell.dong.4";
      } else {
        soundName = "sound.bell.dong.5";
      }
      SoundPlayer.playSound(soundName, bellTower, 3.0F);
      bellTower.setDamage(bellTower.getDamage() + 0.001F);
    }
    if (counter > 60.0F) {
      toReturn = true;
    }
    return toReturn;
  }
  
  static boolean usePendulum(Creature performer, Item pendulum, Action act, float counter)
  {
    boolean toReturn = false;
    int time = 200;
    Skills skills = performer.getSkills();
    Skill primSkill = null;
    try
    {
      primSkill = skills.getSkill(106);
    }
    catch (Exception ex)
    {
      primSkill = skills.learn(106, 1.0F);
    }
    if (counter == 1.0F)
    {
      time = Actions.getStandardActionTime(performer, primSkill, pendulum, 0.0D);
      act.setTimeLeft(time);
      performer.getCommunicator().sendNormalServerMessage("You concentrate on the " + pendulum.getName() + ".");
      Server.getInstance().broadCastAction(performer.getName() + " concentrates on the " + pendulum.getName() + ".", performer, 5);
      
      performer.sendActionControl("Concentrating", true, time);
      performer.getStatus().modifyStamina(-1000.0F);
    }
    else
    {
      time = act.getTimeLeft();
    }
    if (act.currentSecond() == 5) {
      performer.getStatus().modifyStamina(-1000.0F);
    }
    if (counter * 10.0F > time)
    {
      toReturn = true;
      if (pendulum.isLocateItem()) {
        Locates.useLocateItem(performer, pendulum, primSkill);
      } else {
        Locates.locateSpring(performer, pendulum, primSkill);
      }
      pendulum.setDamage(pendulum.getDamage() + 0.01F);
    }
    return toReturn;
  }
  
  static boolean puppetSpeak(Creature performerOne, Item puppetOne, Item puppetTwo, Action act, float counter)
  {
    if (puppetTwo == null)
    {
      performerOne.getCommunicator().sendNormalServerMessage("The " + puppetOne
        .getName() + " needs someone to speak with.");
      return true;
    }
    try
    {
      puppetOne.getOwner();
      puppetTwo.getOwner();
      if (puppetOne.equals(puppetTwo))
      {
        performerOne.getCommunicator().sendNormalServerMessage("The " + puppetOne
          .getName() + " is not interested in speaking to itself.");
        return true;
      }
    }
    catch (NotOwnedException noe)
    {
      performerOne.getCommunicator().sendNormalServerMessage("Both the " + puppetOne
        .getName() + " and the " + puppetTwo.getName() + " need to be held.");
      return true;
    }
    boolean toReturn = false;
    int time = 0;
    try
    {
      performerTwo = Server.getInstance().getCreature(puppetTwo.getOwnerId());
    }
    catch (NoSuchCreatureException nsc)
    {
      Creature performerTwo;
      performerOne.getCommunicator().sendNormalServerMessage("The " + puppetTwo.getName() + " must be played by someone.");
      return true;
    }
    catch (NoSuchPlayerException nsp)
    {
      performerOne.getCommunicator().sendNormalServerMessage("The " + puppetTwo.getName() + " must be played by someone.");
      return true;
    }
    Creature performerTwo;
    if ((performerOne.getPower() > 0) && (performerOne.getPower() < 5) && (Servers.localServer.testServer))
    {
      performerOne.getCommunicator().sendNormalServerMessage("Nothing happens.");
      return true;
    }
    if ((performerTwo.getPower() > 0) && (performerTwo.getPower() < 5) && (Servers.localServer.testServer))
    {
      performerOne.getCommunicator().sendNormalServerMessage("Nothing happens.");
      return true;
    }
    int conversationCounter = 0;
    if (counter == 1.0F)
    {
      if (!puppetTwo.isPuppet())
      {
        performerOne.getCommunicator().sendNormalServerMessage("The " + puppetOne
          .getName() + " does not want to speak to " + puppetTwo.getName() + ".");
        return true;
      }
      if (!Puppet.mayPuppetMaster(performerOne))
      {
        performerOne.getCommunicator().sendNormalServerMessage("You are still mentally exhausted from your last show.");
        return true;
      }
      int nums = Puppet.getConversationLength(true, act, puppetOne, puppetTwo, performerOne, performerTwo, 0);
      int nums2 = Puppet.getConversationLength(true, act, puppetTwo, puppetOne, performerTwo, performerOne, 0);
      int seconds = nums + nums2;
      
      time = (seconds + 2) * 5 * 10;
      
      act.setPower(conversationCounter);
      if (performerTwo.equals(performerOne))
      {
        performerOne.getCommunicator().sendNormalServerMessage("You start playing with the " + puppetOne
          .getName() + " and " + puppetTwo.getName() + ".");
        Server.getInstance().broadCastAction(performerOne
          .getName() + " starts playing with " + puppetOne.getNameWithGenus() + " " + puppetTwo
          .getNameWithGenus() + ".", performerOne, 5);
      }
      else
      {
        if (!Puppet.mayPuppetMaster(performerTwo))
        {
          performerTwo.getCommunicator().sendNormalServerMessage("You are still mentally exhausted from your last show.");
          
          return true;
        }
        if (performerTwo.getPower() < 5) {
          Puppet.startPuppeteering(performerTwo);
        }
        performerTwo.sendActionControl("Puppeteering", true, time);
        performerOne.getCommunicator().sendNormalServerMessage("You start playing with the " + puppetOne
          .getName() + " and the " + puppetTwo.getName() + ".");
        performerTwo.getCommunicator().sendNormalServerMessage(performerOne
          .getName() + " starts playing with your " + puppetTwo.getName() + ".");
        Server.getInstance().broadCastAction(performerOne
          .getName() + " starts a show with " + performerTwo.getName() + ".", performerOne, performerTwo, 5);
      }
      if (performerOne.getPower() < 5) {
        Puppet.startPuppeteering(performerOne);
      }
      performerOne.sendActionControl("Puppeteering", true, time);
    }
    else
    {
      conversationCounter = (int)act.getPower();
    }
    if (act.currentSecond() % 5 == 0)
    {
      performerOne.getStatus().modifyStamina(-500.0F);
      performerTwo.getStatus().modifyStamina(-500.0F);
      if (act.currentSecond() % 10 == 0)
      {
        toReturn = Puppet.sendConversationString(act, puppetTwo, puppetOne, performerTwo, performerOne, conversationCounter);
        if (toReturn)
        {
          toReturn = false;
          if (act.getTimeLeft() > 0)
          {
            if (act.getTimeLeft() >= 100)
            {
              toReturn = true;
              act.setTimeLeft(125);
            }
          }
          else {
            act.setTimeLeft(25);
          }
        }
        puppetTwo.setDamage(puppetTwo.getDamage() + puppetTwo.getDamageModifier() * 0.02F);
      }
      else
      {
        toReturn = Puppet.sendConversationString(act, puppetOne, puppetTwo, performerOne, performerTwo, conversationCounter);
        if (toReturn)
        {
          toReturn = false;
          if (act.getTimeLeft() > 0)
          {
            if (act.getTimeLeft() < 100)
            {
              toReturn = true;
              act.setTimeLeft(125);
            }
          }
          else {
            act.setTimeLeft(100);
          }
        }
        puppetOne.setDamage(puppetOne.getDamage() + puppetOne.getDamageModifier() * 0.02F);
      }
      if ((act.getFailSecond() == 1.0F) || (act.currentSecond() % 10 == 0) || 
        (puppetOne.getTemplateId() == puppetTwo.getTemplateId()))
      {
        act.setFailSecond(10.0F);
        
        conversationCounter++;
      }
      act.setPower(conversationCounter);
      if (toReturn) {
        if (!performerTwo.equals(performerOne)) {
          performerTwo.sendActionControl("Puppeteering", false, 0);
        }
      }
    }
    return toReturn;
  }
  
  public static String getColorDesc(int color)
  {
    return 
      " Colors: R=" + WurmColor.getColorRed(color) + ", G=" + WurmColor.getColorGreen(color) + ", B=" + WurmColor.getColorBlue(color) + ".";
  }
  
  public static String getImpDesc(@Nonnull Creature performer, @Nonnull Item item)
  {
    String toReturn = "";
    if (performer.getPlayingTime() < 604800000L) {
      toReturn = " It can not be improved.";
    }
    if ((item.isRepairable()) && (item.creationState == 0) && (!item.isNoImprove()))
    {
      int skillNum = getImproveSkill(item);
      int templateId = getImproveTemplateId(item);
      if (skillNum != -10) {
        try
        {
          ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(templateId);
          toReturn = " It could be improved with " + temp.getNameWithGenus() + ".";
        }
        catch (NoSuchTemplateException localNoSuchTemplateException) {}
      }
    }
    else if (item.creationState != 0)
    {
      toReturn = ' ' + getNeededCreationAction(getImproveMaterial(item), item.creationState, item);
    }
    return toReturn;
  }
  
  public static List<String> getEnhancementStrings(Item item)
  {
    List<String> strings = new ArrayList();
    if (item.enchantment != 0)
    {
      Spell ench = Spells.getEnchantment(item.enchantment);
      if (ench != null) {
        strings.add("It is enchanted with " + ench.name + ", and " + ench.effectdesc);
      }
    }
    ItemSpellEffects eff = item.getSpellEffects();
    if (eff != null)
    {
      SpellEffect[] speffs = eff.getEffects();
      for (int x = 0; x < speffs.length; x++) {
        if (speffs[x].type < -10L) {
          strings.add("A single " + speffs[x].getName() + " has been attached to it, so it " + speffs[x].getLongDesc());
        } else {
          strings.add(speffs[x].getName() + " has been cast on it, so it " + speffs[x].getLongDesc() + " [" + (int)speffs[x].power + "]");
        }
      }
    }
    return strings;
  }
  
  public static final void sendEnchantmentStrings(Item item, Creature creature)
  {
    if (creature == null) {
      return;
    }
    if (item == null) {
      return;
    }
    for (String s : getEnhancementStrings(item)) {
      creature.getCommunicator().sendNormalServerMessage(s);
    }
  }
  
  public static byte getTransmutationLiquidAuxByteFor(Item source, Item target)
  {
    if (source.getTemplateId() == 417)
    {
      if (Features.Feature.TRANSFORM_TO_RESOURCE_TILES.isEnabled())
      {
        if ((target.getTemplateId() == 220) && (source.getRealTemplateId() == 6)) {
          return 1;
        }
        if ((target.getTemplateId() == 204) && (source.getRealTemplateId() == 410)) {
          return 2;
        }
        if ((target.getTemplateId() == 46) && (source.getRealTemplateId() == 409)) {
          return 3;
        }
        if ((target.getTemplateId() == 47) && (source.getRealTemplateId() == 1283)) {
          return 7;
        }
      }
      if ((target.getTemplateId() == 48) && (source.getRealTemplateId() == 410)) {
        return 4;
      }
      if ((target.getTemplateId() == 479) && (source.getRealTemplateId() == 409)) {
        return 5;
      }
      if ((target.getTemplateId() == 49) && (source.getRealTemplateId() == 6)) {
        return 6;
      }
      if ((target.getTemplateId() == 46) && (source.getRealTemplateId() == 1196)) {
        return 8;
      }
    }
    return 0;
  }
  
  public static String getTransmutationLiquidDescription(byte auxData)
  {
    if (Features.Feature.TRANSFORM_TO_RESOURCE_TILES.isEnabled()) {
      switch (auxData)
      {
      case 1: 
        return "Transmutes sand to clay";
      case 2: 
        return "Transmutes grass or mycelium to peat";
      case 3: 
        return "Transmutes steppe to tar";
      case 7: 
        return "Transmutes moss to tundra";
      }
    }
    switch (auxData)
    {
    case 4: 
      return "Transmutes clay to dirt";
    case 5: 
      return "Transmutes peat to dirt";
    case 6: 
      return "Transmutes tar to dirt";
    case 8: 
      return "Transmutes tundra to dirt";
    }
    return "";
  }
  
  public static int getTransmutationLiquidDifficulty(byte auxData)
  {
    switch (auxData)
    {
    case 1: 
      return 30;
    case 2: 
      return 20;
    case 3: 
      return 10;
    case 4: 
      return 35;
    case 5: 
      return 25;
    case 6: 
      return 15;
    case 7: 
      return 5;
    case 8: 
      return 10;
    }
    return 1;
  }
  
  static int getTransmutationSolidTemplateWeightGrams(byte auxData)
  {
    int templateId = 46;
    switch (auxData)
    {
    case 1: 
      templateId = 220;
      break;
    case 2: 
      templateId = 204;
      break;
    case 3: 
      templateId = 46;
      break;
    case 4: 
      templateId = 48;
      break;
    case 5: 
      templateId = 479;
      break;
    case 6: 
      templateId = 49;
      break;
    case 7: 
      templateId = 47;
      break;
    case 8: 
      templateId = 46;
      break;
    default: 
      templateId = 46;
    }
    try
    {
      ItemTemplate it = ItemTemplateFactory.getInstance().getTemplate(templateId);
      return it.getWeightGrams();
    }
    catch (NoSuchTemplateException e)
    {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
    return 1000;
  }
  
  static byte getTransmutedToTileType(byte auxData)
  {
    switch (auxData)
    {
    case 1: 
      return Tiles.Tile.TILE_CLAY.id;
    case 2: 
      return Tiles.Tile.TILE_PEAT.id;
    case 3: 
      return Tiles.Tile.TILE_TAR.id;
    case 4: 
      return Tiles.Tile.TILE_DIRT.id;
    case 5: 
      return Tiles.Tile.TILE_DIRT.id;
    case 6: 
      return Tiles.Tile.TILE_DIRT.id;
    case 7: 
      return Tiles.Tile.TILE_TUNDRA.id;
    case 8: 
      return Tiles.Tile.TILE_DIRT.id;
    }
    return Tiles.Tile.TILE_DIRT.id;
  }
  
  static float getTransmutationMod(Creature performer, int tilex, int tiley, byte auxData, boolean reverting)
  {
    float mod = 1.0F;
    switch (auxData)
    {
    case 1: 
      if (Servers.isThisAPvpServer()) {
        mod *= 1.5F;
      }
      break;
    case 2: 
      if (Servers.isThisAPvpServer()) {
        mod *= 1.5F;
      }
      break;
    case 3: 
      if (Servers.isThisAPvpServer()) {
        mod *= 1.5F;
      }
      break;
    case 4: 
      if (reverting) {
        mod /= 2.0F;
      }
      break;
    case 5: 
      if (reverting) {
        mod /= 2.0F;
      }
      break;
    case 6: 
      if (reverting) {
        mod /= 2.0F;
      }
      break;
    case 7: 
      mod /= 5.0F;
      if (reverting) {
        mod /= 2.0F;
      }
      break;
    case 8: 
      mod /= 5.0F;
      if (Servers.isThisAPvpServer()) {
        mod *= 1.5F;
      }
      break;
    }
    Village vp = Villages.getVillageWithPerimeterAt(tilex, tiley, true);
    if ((vp != null) && ((vp.isCitizen(performer)) || (vp.isAlly(performer)) || (vp.isEnemy(performer)))) {
      mod /= 1.5F;
    }
    return mod;
  }
  
  public static boolean wouldDestroyLiquid(Item container, Item contained, Item target)
  {
    if ((container.getTemplate().isContainerWithSubItems()) && (contained.isPlacedOnParent())) {
      return false;
    }
    if ((!contained.isFood()) && (!contained.isLiquid()) && (!contained.isRecipeItem())) {
      return true;
    }
    if (((contained.isFood()) || (contained.isRecipeItem())) && (!contained.isLiquid())) {
      return false;
    }
    ItemTemplate ct = contained.isBulkItem() ? contained.getRealTemplate() : contained.getTemplate();
    int cid = ct.getTemplateId();
    
    ItemTemplate rt = target.isBulkItem() ? target.getRealTemplate() : target.getTemplate();
    int tid = rt.getTemplateId();
    if ((rt.isFood()) && (!rt.isLiquid())) {
      return false;
    }
    if (container.isFoodMaker()) {
      if ((container.getTemplateId() != 768) || (!rt.isLiquid()) || (!contained.isLiquid())) {
        if ((contained.getTemplate().isLiquidCooking()) && (rt.isLiquidCooking())) {
          if (container.getTemplateId() != 76)
          {
            if (container.getTemplateId() != 75) {
              return false;
            }
            if ((rt.getFoodGroup() == 1263) && (cid == tid)) {
              return false;
            }
            if ((tid == 1212) || (cid == 1212)) {
              return false;
            }
          }
        }
      }
    }
    if (cid != tid) {
      return true;
    }
    if (contained.getAuxData() != target.getAuxData()) {
      return true;
    }
    if (contained.getBless() != target.getBless()) {
      return true;
    }
    if (contained.getRarity() != target.getRarity()) {
      return true;
    }
    if ((!target.isBulkItem()) && (contained.getRealTemplateId() != target.getRealTemplateId())) {
      return true;
    }
    return false;
  }
  
  public static void setSizes(@Nullable Item container, int newWeight, Item item)
  {
    float mod = newWeight / item.getWeightGrams();
    int maxSizeMod = 4;
    ItemTemplate template = item.getTemplate();
    int sizeX = 0;
    int sizeY = 0;
    int sizeZ = 0;
    if (mod > 64.0F)
    {
      sizeX = template.getSizeX() * 4;
      sizeY = template.getSizeY() * 4;
      sizeZ = template.getSizeZ() * 4;
    }
    else if (mod > 16.0F)
    {
      mod = mod / 4.0F * 4.0F;
      sizeX = (int)(template.getSizeX() * mod);
      sizeY = template.getSizeY() * 4;
      sizeZ = template.getSizeZ() * 4;
    }
    else if (mod > 4.0F)
    {
      sizeX = template.getSizeX();
      sizeY = (int)(template.getSizeY() * mod);
      sizeZ = template.getSizeZ() * 4;
      mod /= 4.0F;
    }
    else
    {
      sizeX = Math.max(1, (int)(template.getSizeX() * mod));
      sizeY = Math.max(1, (int)(template.getSizeY() * mod));
      sizeZ = Math.max(1, (int)(template.getSizeZ() * mod));
    }
    if (container != null)
    {
      sizeX = Math.min(sizeX, container.getSizeX());
      sizeY = Math.min(sizeY, container.getSizeY());
      sizeZ = Math.min(sizeZ, container.getSizeZ());
    }
    item.setSizes(sizeX, sizeY, sizeZ);
  }
  
  public static Point4f getHivePos(int tilex, int tiley, Tiles.Tile theTile, byte data)
  {
    byte age = FoliageAge.getAgeAsByte(data);
    float treex = 2.0F;
    float treey = 2.0F;
    if (!TreeData.isCentre(data))
    {
      treex = TerrainUtilities.getTreePosX(tilex, tiley) * 4.0F;
      treey = TerrainUtilities.getTreePosY(tilex, tiley) * 4.0F;
    }
    float posx = tilex * 4 + treex;
    float posy = tiley * 4 + treey;
    float dir = Creature.normalizeAngle(TerrainUtilities.getTreeRotation(tilex, tiley));
    try
    {
      float basePosZ = Zones.calculateHeight(posx, posy, true);
      float tht = theTile.getTreeImageHeight(data);
      float posz = basePosZ + tht * (age + 1) * 0.4F;
      return new Point4f(posx, posy, posz, dir);
    }
    catch (NoSuchZoneException e)
    {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
    return new Point4f(posx, posy, 0.0F, dir);
  }
  
  public static float getAverageQL(@Nullable Item source, Item target)
  {
    int count = 0;
    float totalQL = 0.0F;
    if (source != null) {
      if ((!source.isCookingTool()) && (!source.isRecipeItem()))
      {
        count++;
        float rarityMult = 1.0F + source.getRarity() * source.getRarity() * 0.1F;
        totalQL += source.getCurrentQualityLevel() * rarityMult;
      }
    }
    if ((target.isFoodMaker()) || (target.getTemplate().isCooker()))
    {
      Item[] items = target.getItemsAsArray();
      for (Item item : items)
      {
        count++;
        float rarityMult = 1.0F + item.getRarity() * item.getRarity() * 0.1F;
        totalQL += item.getCurrentQualityLevel() * rarityMult;
      }
      float rarityMult = 1.0F + target.getRarity() * target.getRarity() * 0.1F;
      totalQL += target.getCurrentQualityLevel() * rarityMult / Math.max(1, 11 - items.length);
    }
    else
    {
      count++;
      float rarityMult = 1.0F + target.getRarity() * target.getRarity() * 0.1F;
      totalQL += target.getCurrentQualityLevel() * rarityMult;
    }
    if (count > 0) {
      return Math.min(99.995F, Math.max(1.0F, totalQL / count));
    }
    return 1.0F;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\behaviours\MethodsItems.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */