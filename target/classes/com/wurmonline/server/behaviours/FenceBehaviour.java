package com.wurmonline.server.behaviours;

import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.questions.ManageObjectList.Type;
import com.wurmonline.server.questions.ManagePermissions;
import com.wurmonline.server.questions.MissionManager;
import com.wurmonline.server.questions.PermissionsHistory;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.structures.DoorSettings.DoorPermissions;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.FenceGate;
import com.wurmonline.server.structures.NoSuchLockException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.utils.logging.TileEvent;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.FenceConstants;
import com.wurmonline.shared.constants.StructureConstantsEnum;
import com.wurmonline.shared.constants.StructureStateEnum;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

final class FenceBehaviour
  extends Behaviour
  implements FenceConstants, ItemTypes, MiscConstants
{
  private static final Logger logger = Logger.getLogger(FenceBehaviour.class.getName());
  
  FenceBehaviour()
  {
    super((short)22);
  }
  
  FenceBehaviour(short type)
  {
    super(type);
  }
  
  @Nonnull
  public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Item subject, @Nonnull Fence target)
  {
    List<ActionEntry> toReturn = super.getBehavioursFor(performer, target);
    
    long targetId = target.getId();
    FenceGate gate = FenceGate.getFenceGate(targetId);
    int templateId = subject.getTemplateId();
    if (!target.isFinished())
    {
      toReturn.add(Actions.actionEntrys['«']);
      toReturn.add(Actions.actionEntrys['ɟ']);
    }
    else
    {
      if ((target.isItemRepair(subject)) && (!target.isFlowerbed()))
      {
        if (target.getDamage() > 0.0F) {
          if (((!Servers.localServer.challengeServer) || (performer.getEnemyPresense() <= 0)) && 
            (!target.isNoRepair())) {
            if (!toReturn.contains(Actions.actionEntrys['Á'])) {
              toReturn.add(Actions.actionEntrys['Á']);
            }
          }
        }
        if ((target.getQualityLevel() < 100.0F) && (!target.isNoImprove()) && (target.getDamage() == 0.0F)) {
          if (!toReturn.contains(Actions.actionEntrys['À'])) {
            toReturn.add(Actions.actionEntrys['À']);
          }
        }
      }
      else if (templateId == 676)
      {
        if (subject.getOwnerId() == performer.getWurmId()) {
          toReturn.add(Actions.actionEntrys['ǘ']);
        }
      }
      else if ((subject.isContainerLiquid()) && (target.isFlowerbed()))
      {
        Item[] items = subject.getItemsAsArray();
        for (Item item : items) {
          if (item.getTemplateId() == 128)
          {
            toReturn.add(Actions.actionEntrys['ȵ']);
            break;
          }
        }
      }
      if (target.isFinished())
      {
        VolaTile fenceTile = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface());
        Structure structure = fenceTile == null ? null : fenceTile.getStructure();
        if ((structure == null) || (MethodsStructure.mayModifyStructure(performer, structure, fenceTile, (short)683))) {
          if ((target.isStoneFence()) && ((subject.getTemplateId() == 130) || (
            (subject.isWand()) && (performer.getPower() >= 4)))) {
            toReturn.add(new ActionEntry((short)847, "Render fence", "rendering"));
          } else if ((target.isPlasteredFence()) && ((subject.getTemplateId() == 1115) || (
            (subject.isWand()) && (performer.getPower() >= 4)))) {
            toReturn.add(new ActionEntry((short)847, "Remove render", "removing"));
          }
        }
      }
      if ((!performer.isGuest()) && (!target.isIndestructible()))
      {
        Skills skills = performer.getSkills();
        Skill str = skills.getSkillOrLearn(102);
        if (str.getKnowledge(0.0D) > 21.0D) {
          if (!target.isRubble()) {
            toReturn.add(Actions.actionEntrys['¬']);
          }
        }
      }
      if ((subject.isColor()) && (!target.isNotPaintable())) {
        toReturn.add(Actions.actionEntrys['ç']);
      }
      if (target.isDoor()) {
        toReturn.addAll(getBehavioursForGate(performer, subject, target, gate));
      }
      if ((subject.isTrellis()) && (performer.getFloorLevel() == 0))
      {
        toReturn.add(new ActionEntry((short)-3, "Plant", "Plant options"));
        toReturn.add(Actions.actionEntrys['˪']);
        toReturn.add(new ActionEntry((short)176, "In center", "planting"));
        toReturn.add(Actions.actionEntrys['˫']);
      }
    }
    if (MethodsStructure.isCorrectToolForBuilding(performer, templateId))
    {
      if ((!target.isHedge()) && (!target.isFinished())) {
        toReturn.add(Actions.actionEntrys['ª']);
      }
    }
    else if (templateId == 267) {
      if (target.isHedge())
      {
        toReturn.add(Actions.actionEntrys['ŵ']);
        if (performer.getPower() > 0) {
          toReturn.add(Actions.actionEntrys['¼']);
        }
      }
    }
    if ((target.isHedge()) && ((subject.isWeaponSlash()) || (subject.getTemplateId() == 24))) {
      toReturn.add(Actions.actionEntrys[96]);
    }
    if (((templateId == 315) || (templateId == 176)) && 
      (performer.getPower() >= 2))
    {
      toReturn.add(Actions.actionEntrys['´']);
      if (target.getDamage() > 0.0F) {
        if ((!Servers.localServer.challengeServer) || (performer.getEnemyPresense() <= 0)) {
          toReturn.add(Actions.actionEntrys['Á']);
        }
      }
      if (target.getQualityLevel() < 100.0F) {
        toReturn.add(Actions.actionEntrys['À']);
      }
      if ((templateId == 176) && (Servers.isThisATestServer()) && (!target.isMagic())) {
        toReturn.add(Actions.actionEntrys['Ʌ']);
      }
      toReturn.add(Actions.actionEntrys['ʬ']);
      if (target.isHedge())
      {
        toReturn.add(Actions.actionEntrys['ŵ']);
        toReturn.add(Actions.actionEntrys['¼']);
        toReturn.add(Actions.actionEntrys[96]);
      }
    }
    else if (templateId == 441)
    {
      if ((target.getColor() != -1) && (!target.isNotPaintable())) {
        toReturn.add(Actions.actionEntrys['è']);
      }
    }
    if (MissionTriggers.getMissionTriggersWith(templateId, 473, targetId).length > 0) {
      toReturn.add(Actions.actionEntrys['Ǚ']);
    }
    if (MissionTriggers.getMissionTriggersWith(templateId, 474, targetId).length > 0) {
      toReturn.add(Actions.actionEntrys['ǚ']);
    }
    addEmotes(toReturn);
    
    addWarStuff(toReturn, performer, target);
    
    return toReturn;
  }
  
  private static void addWarStuff(@Nonnull List<ActionEntry> toReturn, @Nonnull Creature performer, @Nonnull Fence fence)
  {
    Village targVill = fence.getVillage();
    Village village = performer.getCitizenVillage();
    if ((village == null) || (!village.mayDoDiplomacy(performer)) || (targVill == null)) {
      return;
    }
    if (village == targVill) {
      return;
    }
    boolean atPeace = village.mayDeclareWarOn(targVill);
    if (atPeace)
    {
      toReturn.add(new ActionEntry((short)-1, "Village", "Village options", emptyIntArr));
      toReturn.add(Actions.actionEntrys['Ñ']);
    }
  }
  
  @Nonnull
  public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Fence target)
  {
    List<ActionEntry> toReturn = super.getBehavioursFor(performer, target);
    
    FenceGate gate = FenceGate.getFenceGate(target.getId());
    if (!target.isFinished()) {
      toReturn.add(Actions.actionEntrys['ɟ']);
    } else if (target.isDoor()) {
      toReturn.addAll(getBehavioursForGate(performer, null, target, gate));
    }
    addEmotes(toReturn);
    
    return toReturn;
  }
  
  public boolean action(@Nonnull Action act, @Nonnull Creature performer, @Nonnull Item source, boolean onSurface, @Nonnull Fence target, short action, float counter)
  {
    boolean done = true;
    FenceGate gate = FenceGate.getFenceGate(target.getId());
    
    Communicator comm = performer.getCommunicator();
    switch (action)
    {
    case 1: 
      if ((source.getTemplateId() == 176) && (performer.getPower() >= 2))
      {
        done = true;
        action(act, performer, onSurface, target, action, counter);
        comm.sendNormalServerMessage("Startx=" + target
          .getTileX() + ", Starty=" + target.getTileY() + " dir=" + target
          .getDir());
      }
      else
      {
        done = action(act, performer, onSurface, target, action, counter);
      }
      break;
    case 607: 
      comm.sendAddFenceToCreationWindow(target, -10L);
      return true;
    case 209: 
      done = action(act, performer, onSurface, target, action, counter);
      break;
    case 170: 
      if ((target.getLayer() != performer.getLayer()) && (Servers.isThisAPvpServer()))
      {
        performer.getCommunicator().sendNormalServerMessage("You cannot continue that, you are too far away.");
        return true;
      }
      if (!MethodsStructure.isCorrectToolForBuilding(performer, source.getTemplateId())) {
        break label2763;
      }
      done = MethodsStructure.continueFence(performer, target, source, counter, action, act);
      if (!done) {
        break label2763;
      }
      if (!target.isFinished()) {
        comm.sendAddFenceToCreationWindow(target, target.getId());
      } else {
        comm.sendRemoveFromCreationWindow(target.getId());
      }
      break;
    case 581: 
      if ((source.getTemplateId() == 176) && (Servers.isThisATestServer()))
      {
        decay(target, performer);
        done = true;
      }
      else
      {
        done = super.action(act, performer, onSurface, target, action, counter);
      }
      break;
    case 565: 
      if (target.isFlowerbed()) {
        done = waterFlower(act, performer, source, target, counter);
      } else {
        done = super.action(act, performer, onSurface, target, action, counter);
      }
      break;
    case 373: 
      done = Terraforming.pruneHedge(act, performer, source, target, onSurface, counter);
      break;
    case 188: 
      if (performer.getPower() <= 0) {
        break label2763;
      }
      if ((!target.isHighHedge()) && (target.getType() != StructureConstantsEnum.HEDGE_FLOWER1_LOW) && 
        (target.getType() != StructureConstantsEnum.HEDGE_FLOWER3_MEDIUM))
      {
        target.setDamage(0.0F);
        target.setType(StructureConstantsEnum.getEnumByValue((short)(byte)(target.getType().value + 1)));
        try
        {
          target.save();
          VolaTile tile = Zones.getTileOrNull(target.getTileX(), target.getTileY(), onSurface);
          if (tile != null) {
            tile.updateFence(target);
          }
        }
        catch (IOException iox)
        {
          logger.log(Level.WARNING, iox.getMessage(), iox);
        }
        TileEvent.log(target.getTileX(), target.getTileY(), 0, performer.getWurmId(), 188);
      }
      else
      {
        comm.sendNormalServerMessage("You can't grow that hedge any further, clown.");
      }
      break;
    case 96: 
      if ((!target.isHedge()) || ((!source.isWeaponSlash()) && (source.getTemplateId() != 24) && (!source.isWand()))) {
        break label2763;
      }
      done = Terraforming.chopHedge(act, performer, source, target, onSurface, counter); break;
    case 171: 
      done = MethodsStructure.removeFencePlan(performer, source, target, counter, action, act);
      break;
    case 193: 
      if (!target.isFlowerbed())
      {
        if (((!Servers.localServer.challengeServer) || (performer.getEnemyPresense() <= 0)) && 
          (!target.isNoRepair())) {
          done = MethodsStructure.repairFence(act, performer, source, target, counter);
        } else {
          done = true;
        }
      }
      else {
        done = super.action(act, performer, onSurface, target, action, counter);
      }
      break;
    case 192: 
      if ((target.isFinished()) && 
        (target.isItemRepair(source)) && (!target.isNoImprove())) {
        done = MethodsStructure.improveFence(act, performer, source, target, counter);
      } else {
        done = true;
      }
      break;
    case 172: 
      done = true;
      if ((target.isRubble()) || (target.isIndestructible())) {
        break label2763;
      }
      try
      {
        Skill str = performer.getSkills().getSkill(102);
        if (str.getKnowledge(0.0D) > 21.0D) {
          done = MethodsStructure.destroyFence(action, performer, source, target, false, counter);
        }
      }
      catch (NoSuchSkillException nss)
      {
        logger.log(Level.WARNING, "Weird, " + performer.getName() + " has no strength!");
      }
    case 173: 
      done = true;
      if (target.isIndestructible()) {
        break label2763;
      }
      try
      {
        Skill str = performer.getSkills().getSkill(102);
        if (str.getKnowledge(0.0D) > 21.0D) {
          done = MethodsStructure.destroyFence(action, performer, source, target, true, counter);
        }
      }
      catch (NoSuchSkillException nss)
      {
        logger.log(Level.WARNING, "Weird, " + performer.getName() + " has no strength!");
      }
    case 231: 
      if (target.isFinished())
      {
        if ((target.isNotPaintable()) || (!Methods.isActionAllowed(performer, action)))
        {
          comm.sendNormalServerMessage("You are not allowed to paint this fence.");
          return true;
        }
        done = MethodsStructure.colorFence(performer, source, target, act);
        break label2763;
      }
      comm.sendNormalServerMessage("Finish the wall first.");
      return true;
    case 232: 
      if ((target.isNotPaintable()) || (!Methods.isActionAllowed(performer, action)))
      {
        comm.sendNormalServerMessage("You are not allowed to remove the paint from this wall.");
        return true;
      }
      done = MethodsStructure.removeColor(performer, source, target, act);
      break;
    case 180: 
      if (performer.getPower() >= 2) {
        MethodsStructure.instaDestroyFence(performer, target);
      } else {
        done = super.action(act, performer, onSurface, target, action, counter);
      }
      break;
    case 161: 
      if ((source.isLock()) && (source.isLocked()))
      {
        comm.sendNormalServerMessage("The " + source.getName() + " is already in use.");
        return true;
      }
      if (source.getTemplateId() != 252) {
        done = super.action(act, performer, onSurface, target, action, counter);
      } else if (target.isDoor()) {
        if ((!target.isFinished()) || (target.isNotLockable()))
        {
          comm.sendNormalServerMessage("This fence is not finished yet. Attach the lock when it is finished.");
        }
        else
        {
          if (gate == null)
          {
            logger.log(Level.WARNING, "No gate found for fence with id " + target.getId());
            return true;
          }
          try
          {
            long lockid = gate.getLockId();
            if (lockid == source.getWurmId())
            {
              comm.sendNormalServerMessage("You may not attach the lock to this gate twice. Are you crazy or supernatural?");
              
              return true;
            }
          }
          catch (NoSuchLockException localNoSuchLockException) {}
          if (((Methods.isActionAllowed(performer, action)) && (gate.mayAttachLock(performer)) ? 1 : 0) == 0)
          {
            comm.sendNormalServerMessage("You may not attach the lock to this gate as you do not have permission to do so.");
            
            return true;
          }
          boolean insta = (Servers.isThisATestServer()) && (performer.getPower() > 0);
          Village village = null;
          Skill carpentry = performer.getSkills().getSkillOrLearn(1005);
          int time = 10;
          
          done = false;
          if (counter == 1.0F)
          {
            time = (int)Math.max(100.0D, (100.0D - carpentry.getKnowledge(source, 0.0D)) * 5.0D);
            try
            {
              performer.getCurrentAction().setTimeLeft(time);
            }
            catch (NoSuchActionException nsa)
            {
              logger.log(Level.INFO, "This action does not exist?", nsa);
            }
            comm.sendNormalServerMessage("You start to attach the lock.");
            Server.getInstance()
              .broadCastAction(performer.getName() + " starts to attach a lock.", performer, 5);
            
            performer.sendActionControl(Actions.actionEntrys['¡'].getVerbString(), true, time);
          }
          else
          {
            try
            {
              time = performer.getCurrentAction().getTimeLeft();
            }
            catch (NoSuchActionException nsa)
            {
              logger.log(Level.INFO, "This action does not exist?", nsa);
            }
            if ((counter * 10.0F > time) || (insta))
            {
              carpentry.skillCheck(100.0F - source.getCurrentQualityLevel(), 0.0D, false, counter);
              
              long parentId = source.getParentId();
              if (parentId != -10L) {
                try
                {
                  Items.getItem(parentId).dropItem(source.getWurmId(), true);
                }
                catch (NoSuchItemException nsi)
                {
                  logger.log(Level.INFO, performer
                    .getName() + " tried to build with nonexistant nail.");
                }
              }
              done = true;
              try
              {
                village = gate.getVillage();
                long lockid = gate.getLockId();
                if (lockid != source.getLockId()) {
                  try
                  {
                    Item oldlock = Items.getItem(lockid);
                    if (village != null) {
                      oldlock.removeKey(village.getDeedId());
                    }
                    oldlock.setLocked(false);
                    performer.getInventory().insertItem(oldlock, true);
                  }
                  catch (NoSuchItemException nsy)
                  {
                    logger.log(Level.WARNING, "Weird. Lock id exists, but not the item.", nsy);
                  }
                }
              }
              catch (NoSuchLockException localNoSuchLockException1) {}
              if (village != null) {
                source.addKey(village.getDeedId());
              }
              if (source.getLastOwnerId() != performer.getWurmId()) {
                logger.log(Level.INFO, "Weird. Lock has wrong last owner.");
              }
              source.setLastOwnerId(performer.getWurmId());
              gate.setLock(source.getWurmId());
              source.setLocked(true);
              PermissionsHistories.addHistoryEntry(gate.getWurmId(), 
                System.currentTimeMillis(), performer
                .getWurmId(), performer
                .getName(), "Attached lock to gate");
              
              comm.sendNormalServerMessage("You attach the lock to the gate.");
              Server.getInstance()
                .broadCastAction(performer.getName() + " attaches a lock to the gate.", performer, 5);
              if (village != null)
              {
                gate.setIsManaged(true, (Player)performer);
                
                gate.addGuest(performer.getWurmId(), DoorSettings.DoorPermissions.PASS.getValue());
              }
            }
          }
        }
      }
      break;
    case 28: 
      done = true;
      if (target.isNotLockable()) {
        break label2763;
      }
      try
      {
        if (gate != null)
        {
          Item lock = gate.getLock();
          if ((gate.mayLock(performer)) || (performer.hasKeyForLock(lock)))
          {
            lock.lock();
            PermissionsHistories.addHistoryEntry(gate.getWurmId(), 
              System.currentTimeMillis(), performer
              .getWurmId(), performer
              .getName(), "Locked gate");
            
            comm.sendNormalServerMessage("You lock the gate.");
            Server.getInstance()
              .broadCastAction(performer.getName() + " locks the gate.", performer, 5);
          }
        }
        else
        {
          logger.log(Level.WARNING, "No gate found for fence with id " + target.getId());
          return true;
        }
      }
      catch (NoSuchLockException localNoSuchLockException2) {}
    case 102: 
      done = true;
      if (target.isNotLockable()) {
        break label2763;
      }
      try
      {
        if (gate != null)
        {
          Item lock = gate.getLock();
          if ((gate.mayLock(performer)) || (performer.hasKeyForLock(lock)))
          {
            lock.unlock();
            PermissionsHistories.addHistoryEntry(gate.getWurmId(), 
              System.currentTimeMillis(), performer
              .getWurmId(), performer
              .getName(), "Unlocked gate");
            
            comm.sendNormalServerMessage("You unlock the gate.");
            Server.getInstance()
              .broadCastAction(performer.getName() + " unlocks the gate.", performer, 5);
          }
        }
        else
        {
          logger.log(Level.WARNING, "No gate found for fence with id " + target.getId());
          return true;
        }
      }
      catch (NoSuchLockException localNoSuchLockException3) {}
    case 101: 
      done = true;
      if (target.isNotLockpickable()) {
        break label2763;
      }
      if (gate == null) {
        break label2763;
      }
      done = MethodsStructure.picklock(performer, source, gate, gate.getFence().getName(), counter, act); break;
    case 472: 
      done = true;
      if ((source.getTemplateId() != 676) || (source.getOwnerId() != performer.getWurmId())) {
        break label2763;
      }
      MissionManager m = new MissionManager(performer, "Manage missions", "Select action", target.getId(), target.getName(), source.getWurmId());
      m.sendQuestion();
      break;
    case 667: 
      if ((gate != null) && ((gate.mayManage(performer)) || (gate.isActualOwner(performer.getWurmId()))))
      {
        ManagePermissions mp = new ManagePermissions(performer, ManageObjectList.Type.GATE, FenceGate.getFenceGate(target.getId()), false, -10L, false, null, "");
        
        mp.sendQuestion();
      }
      done = true;
      break;
    case 691: 
      if ((gate == null) || (!gate.maySeeHistory(performer))) {
        break label2763;
      }
      PermissionsHistory ph = new PermissionsHistory(performer, target.getId());
      ph.sendQuestion();
      break;
    case 684: 
      done = true;
      if (((source.getTemplateId() == 315) || (source.getTemplateId() == 176)) && 
        (performer.getPower() >= 2)) {
        Methods.sendItemRestrictionManagement(performer, target, target.getId());
      } else {
        logger.log(Level.WARNING, performer
          .getName() + " hacking the protocol by trying to set the restrictions of " + target + ", counter: " + counter + '!');
      }
      break;
    case 176: 
    case 746: 
    case 747: 
      if (source.isTrellis()) {
        done = Terraforming.plantTrellis(performer, source, target.getTileX(), target.getTileY(), onSurface, target
          .getDir(), action, counter, act);
      } else {
        done = true;
      }
      break;
    case 847: 
      if ((target.isStoneFence()) && ((source.getTemplateId() == 130) || (
        (source.isWand()) && (performer.getPower() >= 4)))) {
        return toggleRenderFence(performer, source, target, act, counter);
      }
      if ((target.isPlasteredFence()) && ((source.getTemplateId() == 1115) || (
        (source.isWand()) && (performer.getPower() >= 4)))) {
        return toggleRenderFence(performer, source, target, act, counter);
      }
      break;
    }
    done = super.action(act, performer, onSurface, target, action, counter);
    label2763:
    return done;
  }
  
  static final boolean toggleRenderFence(Creature performer, Item tool, Fence fence, Action act, float counter)
  {
    boolean insta = (tool.isWand()) && (performer.getPower() >= 4);
    VolaTile fenceTile = getFenceTile(fence);
    if (fenceTile == null) {
      return true;
    }
    Structure structure = fenceTile.getStructure();
    if ((!insta) && (structure != null) && (!MethodsStructure.mayModifyStructure(performer, structure, fenceTile, (short)683)))
    {
      performer.getCommunicator().sendNormalServerMessage("You are not allowed to modify the structure.");
      
      return true;
    }
    if (!Methods.isActionAllowed(performer, (short)116, fenceTile.getTileX(), fenceTile.getTileY())) {
      return true;
    }
    if ((fence.isStoneFence()) && (!insta)) {
      if (tool.getWeightGrams() < 5000)
      {
        performer.getCommunicator().sendNormalServerMessage("It takes 5kg of " + tool
          .getName() + " to render the " + fence.getName() + ".");
        return true;
      }
    }
    int time = 40;
    if (counter == 1.0F)
    {
      String render = fence.isStoneFence() ? "render" : "remove the render from";
      String rendering = fence.isStoneFence() ? "rending" : "removing the render from";
      act.setTimeLeft(time);
      performer.sendActionControl(rendering + " the fence", true, time);
      performer.getCommunicator().sendNormalServerMessage(StringUtil.format("You start to " + render + " the %s.", new Object[] { fence.getName() }));
      Server.getInstance().broadCastAction(
        StringUtil.format("%s starts to " + render + " the %s.", new Object[] {performer.getName(), fence.getName() }), performer, 5);
      return false;
    }
    time = act.getTimeLeft();
    if ((counter * 10.0F > time) || (insta))
    {
      String render = fence.isStoneFence() ? "render" : "remove the render from";
      String renders = fence.isStoneFence() ? "renders" : "removes the render from";
      performer.getCommunicator().sendNormalServerMessage(StringUtil.format("You " + render + " the %s.", new Object[] { fence.getName() }));
      Server.getInstance().broadCastAction(
        StringUtil.format("%s " + renders + " the %s.", new Object[] {performer.getName(), fence.getName() }), performer, 5);
      if ((fence.isStoneFence()) && (!insta)) {
        tool.setWeight(tool.getWeightGrams() - 5000, true);
      }
      if (fence.getType() == StructureConstantsEnum.FENCE_STONE) {
        fence.setType(StructureConstantsEnum.FENCE_RENDERED);
      } else if (fence.getType() == StructureConstantsEnum.FENCE_IRON) {
        fence.setType(StructureConstantsEnum.FENCE_RENDERED_IRON);
      } else if (fence.getType() == StructureConstantsEnum.FENCE_IRON_GATE) {
        fence.setType(StructureConstantsEnum.FENCE_RENDERED_IRON_GATE);
      } else if (fence.getType() == StructureConstantsEnum.FENCE_RENDERED) {
        fence.setType(StructureConstantsEnum.FENCE_STONE);
      } else if (fence.getType() == StructureConstantsEnum.FENCE_RENDERED_IRON) {
        fence.setType(StructureConstantsEnum.FENCE_IRON);
      } else if (fence.getType() == StructureConstantsEnum.FENCE_RENDERED_IRON_GATE) {
        fence.setType(StructureConstantsEnum.FENCE_IRON_GATE);
      } else if (fence.getType() == StructureConstantsEnum.FENCE_IRON_HIGH) {
        fence.setType(StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE);
      } else if (fence.getType() == StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE) {
        fence.setType(StructureConstantsEnum.FENCE_IRON_HIGH);
      } else if (fence.getType() == StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE_GATE) {
        fence.setType(StructureConstantsEnum.FENCE_IRON_GATE_HIGH);
      } else if (fence.getType() == StructureConstantsEnum.FENCE_IRON_GATE_HIGH) {
        fence.setType(StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE_GATE);
      } else if (fence.getType() == StructureConstantsEnum.FENCE_RENDERED_PORTCULLIS) {
        fence.setType(StructureConstantsEnum.FENCE_PORTCULLIS);
      } else if (fence.getType() == StructureConstantsEnum.FENCE_PORTCULLIS) {
        fence.setType(StructureConstantsEnum.FENCE_RENDERED_PORTCULLIS);
      } else if (fence.getType() == StructureConstantsEnum.FENCE_RENDERED_STONE_PARAPET) {
        fence.setType(StructureConstantsEnum.FENCE_STONE_PARAPET);
      } else if (fence.getType() == StructureConstantsEnum.FENCE_STONE_PARAPET) {
        fence.setType(StructureConstantsEnum.FENCE_RENDERED_STONE_PARAPET);
      } else if (fence.getType() == StructureConstantsEnum.FENCE_RENDERED_CHAIN_FENCE) {
        fence.setType(StructureConstantsEnum.FENCE_MEDIUM_CHAIN);
      } else if (fence.getType() == StructureConstantsEnum.FENCE_MEDIUM_CHAIN) {
        fence.setType(StructureConstantsEnum.FENCE_RENDERED_CHAIN_FENCE);
      } else if (fence.getType() == StructureConstantsEnum.FENCE_RENDERED_TALL_STONE_WALL) {
        fence.setType(StructureConstantsEnum.FENCE_STONEWALL_HIGH);
      } else if (fence.getType() == StructureConstantsEnum.FENCE_STONEWALL_HIGH) {
        fence.setType(StructureConstantsEnum.FENCE_RENDERED_TALL_STONE_WALL);
      }
      try
      {
        fence.save();
      }
      catch (IOException e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
      fenceTile.updateFence(fence);
      
      return true;
    }
    return false;
  }
  
  @Nullable
  static final VolaTile getFenceTile(Fence fence)
  {
    int tilex = fence.getStartX();
    int tiley = fence.getStartY();
    for (int xx = 1; xx >= -1; xx--) {
      for (int yy = 1; yy >= -1; yy--) {
        try
        {
          Zone zone = Zones.getZone(tilex + xx, tiley + yy, fence.isOnSurface());
          VolaTile tile = zone.getTileOrNull(tilex + xx, tiley + yy);
          if (tile != null)
          {
            Fence[] fences = tile.getFences();
            for (int s = 0; s < fences.length; s++) {
              if (fences[s].getId() == fence.getId()) {
                return tile;
              }
            }
          }
        }
        catch (NoSuchZoneException localNoSuchZoneException) {}
      }
    }
    return null;
  }
  
  private static void decay(Fence fence, Creature performer)
  {
    if (fence.isMagic()) {
      return;
    }
    long decayTime = 86400000L;
    if (fence.isHedge()) {
      if (fence.isLowHedge())
      {
        decayTime *= 3L;
      }
      else if (fence.isMediumHedge())
      {
        decayTime *= 10L;
      }
      else
      {
        Village vill = fence.getVillage();
        if (vill != null)
        {
          if (vill.moreThanMonthLeft())
          {
            performer.getCommunicator().sendNormalServerMessage("There is more then a month left of upkeep, no decay will take place.");
            
            return;
          }
          if (vill.lessThanWeekLeft()) {
            decayTime *= (fence.isFlowerbed() ? 2 : 10);
          }
        }
        else if (Zones.getKingdom(fence.getTileX(), fence.getTileY()) == 0)
        {
          decayTime = ((float)decayTime * 0.5F);
        }
      }
    }
    fence.setLastUsed(WurmCalendar.currentTime - decayTime - 10L);
    fence.poll(WurmCalendar.currentTime);
  }
  
  private static boolean waterFlower(@Nonnull Action act, @Nonnull Creature performer, @Nonnull Item waterSource, @Nonnull Fence flowerbed, float counter)
  {
    int time = 0;
    Item water = null;
    for (Item item : waterSource.getItemsAsArray()) {
      if (item.getTemplateId() == 128)
      {
        water = item;
        break;
      }
    }
    Communicator comm = performer.getCommunicator();
    if (water == null)
    {
      comm.sendNormalServerMessage("You need water to water the flowerbed.", (byte)3);
      return true;
    }
    if (water.getWeightGrams() < 100)
    {
      comm.sendNormalServerMessage("You need more water in order to water the flowerbed.", (byte)3);
      return true;
    }
    if (flowerbed.getDamage() == 0.0F)
    {
      comm.sendNormalServerMessage("This flowerbed is in no need of watering.", (byte)3);
      return true;
    }
    Skill gardening = performer.getSkills().getSkillOrLearn(10045);
    if (counter == 1.0F)
    {
      time = Actions.getStandardActionTime(performer, gardening, waterSource, 0.0D);
      act.setTimeLeft(time);
      comm.sendNormalServerMessage("You start watering the flowerbed.");
      Server.getInstance().broadCastAction(performer.getName() + " starts to water a flowerbed.", performer, 5);
      performer.sendActionControl(Actions.actionEntrys['ȵ'].getVerbString(), true, time);
      return false;
    }
    time = act.getTimeLeft();
    if (counter * 10.0F <= time) {
      return false;
    }
    double power = gardening.skillCheck(15.0D, 0.0D, false, counter);
    if (power > 0.0D)
    {
      float dmgChange = 20.0F * (float)(power / 100.0D);
      flowerbed.setDamage(Math.max(0.0F, flowerbed.getDamage() - dmgChange));
      water.setWeight(water.getWeightGrams() - 100, true);
      comm.sendNormalServerMessage("You successfully watered the flowerbed.");
      return true;
    }
    int waterReduction = 100;
    if (power >= -20.0D)
    {
      comm.sendNormalServerMessage("You accidentally miss the flowerbed and pour the water on the ground instead.", (byte)3);
    }
    else if ((power > -50.0D) && (power < -20.0D))
    {
      comm.sendNormalServerMessage("You spill water all over your clothes.", (byte)3);
    }
    else
    {
      comm.sendNormalServerMessage("For some inexplicable reason you poured all of the water on the ground, how you thought it would help you will never know.");
      
      waterReduction = Math.min(water.getWeightGrams(), 200);
    }
    water.setWeight(water.getWeightGrams() - waterReduction, true);
    
    return true;
  }
  
  public boolean action(@Nonnull Action act, @Nonnull Creature performer, boolean onSurface, @Nonnull Fence target, short action, float counter)
  {
    boolean done = false;
    FenceGate gate = FenceGate.getFenceGate(target.getId());
    Communicator comm = performer.getCommunicator();
    switch (action)
    {
    case 1: 
      StructureStateEnum state = target.getState();
      StructureConstantsEnum type = target.getType();
      String toSend;
      String toSend;
      if (target.isFinished()) {
        toSend = getFinishedString(performer, target, gate, type);
      } else {
        toSend = getUnfinishedString(target, state, type);
      }
      if (toSend.length() > 0)
      {
        comm.sendNormalServerMessage(toSend);
        comm.sendNormalServerMessage("QL=" + target.getQualityLevel() + ", dam=" + target.getDamage());
      }
      done = true;
      break;
    case 607: 
      comm.sendAddFenceToCreationWindow(target, -10L);
      done = true;
      break;
    case 28: 
      done = true;
      if (gate != null) {
        try
        {
          Item lock = gate.getLock();
          if ((gate.mayLock(performer)) || (performer.hasKeyForLock(lock)))
          {
            lock.lock();
            comm.sendNormalServerMessage("You lock the gate.");
            Server.getInstance()
              .broadCastAction(performer.getNameWithGenus() + " locks the gate.", performer, 5);
          }
        }
        catch (NoSuchLockException localNoSuchLockException) {}
      }
      break;
    case 102: 
      done = true;
      try
      {
        Item lock = gate.getLock();
        if ((gate.mayLock(performer)) || (performer.hasKeyForLock(lock)))
        {
          lock.unlock();
          comm.sendNormalServerMessage("You unlock the gate.");
          Server.getInstance()
            .broadCastAction(performer.getNameWithGenus() + " unlocks the gate.", performer, 5);
        }
      }
      catch (NoSuchLockException localNoSuchLockException1) {}
    case 209: 
      done = true;
      if (performer.getCitizenVillage() == null) {
        comm.sendAlertServerMessage("You are no longer a citizen of a village.");
      } else if (target.getVillage() == null) {
        comm.sendAlertServerMessage(target.getName() + " is no longer in a village.");
      } else if (!performer.getCitizenVillage().mayDeclareWarOn(target.getVillage())) {
        comm.sendAlertServerMessage(target.getName() + " is already at war with your village.");
      } else {
        Methods.sendWarDeclarationQuestion(performer, target.getVillage());
      }
      break;
    case 667: 
      done = true;
      if ((gate != null) && (
        (gate.mayManage(performer)) || 
        (gate.isActualOwner(performer.getWurmId()))))
      {
        ManagePermissions mp = new ManagePermissions(performer, ManageObjectList.Type.GATE, FenceGate.getFenceGate(target.getId()), false, -10L, false, null, "");
        
        mp.sendQuestion();
      }
      break;
    case 691: 
      done = true;
      if ((gate != null) && (gate.maySeeHistory(performer)))
      {
        PermissionsHistory ph = new PermissionsHistory(performer, target.getId());
        ph.sendQuestion();
      }
      break;
    case 193: 
      comm.sendNormalServerMessage("'Repair' requires an active item.");
      done = true;
    }
    return done;
  }
  
  private static String getMaterialName(ItemTemplate template)
  {
    switch (template.getTemplateId())
    {
    case 218: 
      return "small iron " + template.getName();
    case 217: 
      return "large iron " + template.getName();
    }
    return template.getName();
  }
  
  private List<ActionEntry> getBehavioursForGate(Creature performer, @Nullable Item subject, Fence target, @Nonnull FenceGate gate)
  {
    List<ActionEntry> toReturn = new LinkedList();
    List<ActionEntry> permissions = new LinkedList();
    if ((gate.mayManage(performer)) || (gate.isActualOwner(performer.getWurmId()))) {
      permissions.add(new ActionEntry((short)667, "Manage Gate", "managing permissions"));
    }
    if (gate.maySeeHistory(performer)) {
      permissions.add(new ActionEntry((short)691, "History of Gate", "viewing"));
    }
    if (!permissions.isEmpty())
    {
      if (permissions.size() > 1)
      {
        Collections.sort(permissions);
        toReturn.add(new ActionEntry((short)-permissions.size(), "Permissions", "viewing"));
      }
      toReturn.addAll(permissions);
    }
    try
    {
      Item lock = gate.getLock();
      if (!target.isNotLockable()) {
        if ((gate.mayLock(performer)) || (performer.hasKeyForLock(lock))) {
          if (lock.isLocked()) {
            toReturn.add(Actions.actionEntrys[102]);
          } else {
            toReturn.add(Actions.actionEntrys[28]);
          }
        }
      }
      if (performer.isWithinDistanceTo(target.getTileX(), target.getTileY(), 1)) {
        if ((subject != null) && (subject.getTemplateId() == 463) && (!target.isNotLockpickable())) {
          MethodsStructure.addLockPickEntry(performer, subject, gate, false, lock, toReturn);
        }
      }
    }
    catch (NoSuchLockException localNoSuchLockException) {}
    if ((!target.isNotLockable()) && (subject != null) && (subject.getTemplateId() == 252)) {
      if (gate.mayAttachLock(performer)) {
        toReturn.add(Actions.actionEntrys['¡']);
      }
    }
    return toReturn;
  }
  
  @Nonnull
  private static String getUnfinishedString(@Nonnull Fence target, StructureStateEnum state, StructureConstantsEnum type)
  {
    String toSend = "You see an unfinished fence.";
    switch (FenceBehaviour.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[type.ordinal()])
    {
    case 1: 
    case 2: 
      toSend = "You see an unfinished wooden palisade.";
      break;
    case 3: 
    case 4: 
    case 5: 
      toSend = "You see an unfinished stone wall.";
      break;
    case 6: 
      toSend = "You see an unfinished stone parapet.";
      break;
    case 7: 
      toSend = "You see an unfinished stone and iron parapet.";
      break;
    case 8: 
      toSend = "You see an unfinished wooden parapet.";
      break;
    case 9: 
    case 10: 
    case 11: 
      toSend = "You see an unfinished wooden fence.";
      break;
    case 12: 
    case 13: 
      toSend = "You see an unfinished wooden fence gate.";
      break;
    case 14: 
    case 15: 
      toSend = "You see an unfinished wooden palisade gate.";
      break;
    case 16: 
    case 17: 
      toSend = "You see an unfinished crude wooden fence gate.";
      break;
    case 18: 
    case 19: 
      toSend = "You see an unfinished wooden roundpole fence gate.";
      break;
    case 20: 
    case 21: 
      toSend = "You see an unfinished tall stone wall.";
      break;
    case 22: 
      toSend = "You see an unfinished chain fence.";
      break;
    case 23: 
      toSend = "You see an unfinished portcullis.";
      break;
    case 24: 
      toSend = "You see an unfinished iron fence.";
      break;
    case 25: 
      toSend = "You see an unfinished iron fence gate.";
      break;
    case 26: 
      toSend = "You see an unfinished woven fence.";
      break;
    case 27: 
      toSend = "You see an unfinished stone fence.";
      break;
    case 28: 
      toSend = "You see an unfinished curb.";
      break;
    case 29: 
      toSend = "You see an unfinished low rope fence.";
      break;
    case 30: 
      toSend = "You see an unfinished high rope fence.";
      break;
    case 31: 
      toSend = "You see an unfinished high roundpole fence.";
      break;
    case 32: 
      toSend = "You see an unfinished low roundpole fence.";
    }
    int[] tNeeded = Fence.getConstructionMaterialsNeededTotal(target);
    if (tNeeded.length <= 0) {
      return toSend;
    }
    if (tNeeded[0] == -1)
    {
      logger.log(Level.WARNING, "Weird. This shouldn't happen. The fence is finished, of type " + type + " and state " + state, new Exception());
      
      return toSend;
    }
    try
    {
      toSend = toSend + " Total materials needed ";
      for (int x = 0; x < tNeeded.length - 1; x += 2)
      {
        toSend = toSend + tNeeded[(x + 1)] + " ";
        ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(tNeeded[x]);
        if ((!template.getName().endsWith("s")) && (tNeeded[(x + 1)] > 1)) {
          toSend = toSend + getMaterialName(template) + "s";
        } else {
          toSend = toSend + getMaterialName(template);
        }
        if (x < tNeeded.length - 2) {
          toSend = toSend + " and ";
        }
      }
      toSend = toSend + ".";
      if (tNeeded.length > 2)
      {
        toSend = toSend + " Current stage needs 1 ";
        int[] materials = Fence.getItemTemplatesNeededForFence(target);
        for (int i = 0; i < materials.length; i++)
        {
          ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(materials[i]);
          
          toSend = toSend + getMaterialName(template);
          if (i < materials.length - 1) {
            toSend = toSend + " and ";
          }
        }
        toSend = toSend + ".";
      }
    }
    catch (NoSuchTemplateException nst)
    {
      logger.log(Level.WARNING, "Failed to locate template: " + nst.getMessage(), nst);
    }
    return toSend;
  }
  
  @Nonnull
  private static String getFinishedString(@Nonnull Creature performer, @Nonnull Fence target, @Nullable FenceGate gate, StructureConstantsEnum type)
  {
    String toSend = "";
    
    Communicator comm = performer.getCommunicator();
    
    toSend = getDescription(target, gate, type);
    
    comm.sendNormalServerMessage(toSend);
    toSend = "";
    if (target.isGate())
    {
      sendGateDescription(target, gate, comm);
      
      sendLockDescription(target, gate, performer, comm);
      if (performer.getPower() > 0) {
        comm.sendNormalServerMessage("State=" + target.getState() + " inner x=" + gate
          .getInnerTile().getTileX() + ", " + gate
          .getInnerTile().getTileY() + ", outer: " + gate
          .getOuterTile().getTileX() + ", y=" + gate
          .getOuterTile().getTileY());
      }
    }
    if (target.getColor() != -1) {
      comm.sendNormalServerMessage("Colors: R=" + 
        WurmColor.getColorRed(target.getColor()) + ", G=" + 
        WurmColor.getColorGreen(target.getColor()) + ", B=" + 
        WurmColor.getColorBlue(target.getColor()) + ".");
    }
    toSend = "";
    
    comm.sendNormalServerMessage("QL=" + target.getQualityLevel() + ", dam=" + target.getDamage());
    
    return toSend;
  }
  
  private static String getDescription(Fence target, FenceGate gate, StructureConstantsEnum type)
  {
    String toSend = "";
    if (!target.isGate()) {
      toSend = getFenceDescription(type);
    } else {
      toSend = getFenceGateDescription(gate, type);
    }
    if ((target.isFlowerbed()) && (toSend.isEmpty())) {
      toSend = "A flowerbed filled with unknown flowers.";
    }
    if (target.isLowHedge())
    {
      if (type != StructureConstantsEnum.HEDGE_FLOWER1_LOW) {
        toSend = "This low hedge is growing steadily.";
      } else {
        toSend = "This pretty lavender hedge will probably not grow further.";
      }
      return toSend;
    }
    if (target.isMediumHedge()) {
      return "This medium sized hedge is growing steadily.";
    }
    if (target.isHighHedge()) {
      return "This hedge seems to be at peak height.";
    }
    if (toSend.isEmpty())
    {
      toSend = "Unknown fence type.\n";
      logger.log(Level.WARNING, "Missing fence description for type: " + type);
    }
    return toSend;
  }
  
  private static void sendLockDescription(Fence target, FenceGate gate, Creature performer, Communicator comm)
  {
    try
    {
      String toSend = "";
      Item lock = gate.getLock();
      String lockStrength = lock.getLockStrength();
      
      comm.sendNormalServerMessage("You see a gate with a lock. The lock is of " + lockStrength + " quality.");
      if (performer.getPower() >= 5) {
        comm.sendNormalServerMessage("Lock WurmId=" + lock.getWurmId() + ", dam=" + lock.getDamage());
      }
      if (gate.getLockCounter() > 0) {
        comm.sendNormalServerMessage("The gate is picked open and will shut in " + gate.getLockCounterTime());
      } else if (lock.isLocked()) {
        toSend = toSend + "It is locked.";
      } else {
        toSend = toSend + "It is unlocked.";
      }
      comm.sendNormalServerMessage(toSend);
      if (performer.getPower() > 1)
      {
        String ownerName = "unknown";
        PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(lock.getLastOwnerId());
        if (info != null) {
          ownerName = info.getName();
        }
        comm.sendNormalServerMessage("Last lock owner: " + ownerName);
      }
    }
    catch (NoSuchLockException localNoSuchLockException) {}
  }
  
  private static void sendGateDescription(Fence target, FenceGate gate, Communicator comm)
  {
    String name = gate.getName();
    String toSend = "";
    
    toSend = toSend + "A plaque is attached to it:";
    comm.sendNormalServerMessage(toSend);
    comm.sendNormalServerMessage("-----------------");
    
    toSend = "";
    if (name.length() > 0)
    {
      toSend = toSend + "Named: \"" + name + "\"";
      comm.sendNormalServerMessage(toSend);
    }
    boolean showOwner = true;
    if (gate.isManaged()) {
      try
      {
        Village vc = Villages.getVillage(gate.getVillageId());
        comm.sendNormalServerMessage(vc.getMotto());
        showOwner = false;
      }
      catch (NoSuchVillageException e)
      {
        gate.setIsManaged(false, null);
        target.savePermissions();
      }
    }
    if (showOwner)
    {
      long ownerId = gate.getOwnerId();
      if (ownerId != -10L)
      {
        String owner = PlayerInfoFactory.getPlayerName(ownerId);
        comm.sendNormalServerMessage("Owner:" + owner);
      }
    }
    comm.sendNormalServerMessage("-----------------");
  }
  
  private static String getFenceGateDescription(FenceGate gate, StructureConstantsEnum type)
  {
    Village village = gate != null ? gate.getVillage() : null;
    boolean noVillage = village == null;
    String villageName = noVillage ? null : village.getName();
    String toSend = "";
    switch (FenceBehaviour.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[type.ordinal()])
    {
    case 15: 
      if (noVillage) {
        toSend = "You see a wooden palisade gate.\n";
      } else {
        toSend = "You see a wooden palisade gate in the settlement of " + villageName + ".";
      }
      break;
    case 13: 
      if (noVillage) {
        toSend = "You see a wooden fence gate.\n";
      } else {
        toSend = "You see a wooden fence gate in the settlement of " + villageName + ".\n";
      }
      break;
    case 33: 
    case 34: 
    case 35: 
    case 36: 
    case 37: 
    case 38: 
    case 39: 
    case 40: 
    case 41: 
    case 42: 
    case 43: 
    case 44: 
    case 45: 
    case 46: 
      if (noVillage) {
        toSend = "You see an iron fence gate.\n";
      } else {
        toSend = "You see an iron fence gate in the settlement of " + villageName + ".\n";
      }
      break;
    case 17: 
      if (noVillage) {
        toSend = "You see a crude wooden fence gate.\n";
      } else {
        toSend = "You see a crude wooden fence gate in the settlement of " + villageName + ".\n";
      }
      break;
    case 19: 
      if (noVillage) {
        toSend = "You see a wooden roundpole fence gate.\n";
      } else {
        toSend = "You see a wooden roundpole fence gate in the settlement of " + villageName + ".\n";
      }
      break;
    case 47: 
    case 48: 
    case 49: 
    case 50: 
    case 51: 
    case 52: 
    case 53: 
      if (noVillage) {
        toSend = "You see a portcullis.\n";
      } else {
        toSend = "You see a portcullis in the settlement of " + villageName + ".\n";
      }
      break;
    }
    return toSend;
  }
  
  private static String getFenceDescription(StructureConstantsEnum type)
  {
    String toSend = "";
    switch (FenceBehaviour.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstantsEnum[type.ordinal()])
    {
    case 2: 
      toSend = "You see a sturdy wooden palisade.";
      break;
    case 4: 
      toSend = "You see a strong stone wall.";
      break;
    case 10: 
      toSend = "You see a wooden fence.";
      break;
    case 54: 
      toSend = "You see a crude wooden fence.";
      break;
    case 55: 
      toSend = "You see a low wooden roundpole fence.";
      break;
    case 56: 
      toSend = "You see a high wooden roundpole fence";
      break;
    case 57: 
    case 58: 
    case 59: 
    case 60: 
    case 61: 
    case 62: 
    case 63: 
      toSend = "You see an iron fence.";
      break;
    case 64: 
    case 65: 
    case 66: 
    case 67: 
    case 68: 
    case 69: 
    case 70: 
      toSend = "You see an high iron fence";
      break;
    case 71: 
      toSend = "This woven fence is purely a decoration and stops neither creature nor man.";
      break;
    case 72: 
      toSend = "You see a wooden parapet.";
      break;
    case 5: 
    case 73: 
    case 74: 
    case 75: 
    case 76: 
    case 77: 
    case 78: 
      toSend = "You see a strong stone parapet.";
      break;
    case 79: 
      toSend = "You see a strong parapet made from stone and iron.";
      break;
    case 80: 
    case 81: 
    case 82: 
    case 83: 
    case 84: 
    case 85: 
    case 86: 
      toSend = "You see a chain fence.";
      break;
    case 21: 
    case 87: 
    case 88: 
    case 89: 
    case 90: 
    case 91: 
    case 92: 
      toSend = "You see a strong tall stone wall.";
      break;
    case 93: 
      toSend = "You see a stone fence.";
      break;
    case 94: 
    case 95: 
    case 96: 
    case 97: 
    case 98: 
    case 99: 
      toSend = "You see a strong fence.";
      break;
    case 100: 
      toSend = "You see a curb.";
      break;
    case 101: 
      toSend = "You see a low rope fence.";
      break;
    case 102: 
      toSend = "You see a high rope fence.";
      break;
    case 103: 
      toSend = "This stone wall is magic! You can see how it slowly crumbles as the weave disperses the Source.";
      break;
    case 104: 
      toSend = "This wall of fire is magic! You can see how it slowly dissipates as the weave disperses the Source.";
      break;
    case 105: 
      toSend = "This ice wall is magic! You can see how it slowly melts as the weave disperses the Source.";
      break;
    case 106: 
      toSend = "A flowerbed filled with crooked but beautiful blue flowers.";
      break;
    case 107: 
      toSend = "A flowerbed filled with greenish-yellow furry flowers.";
      break;
    case 108: 
      toSend = "A flowerbed filled with long-stemmed orange-red flowers with thick, pointy leaves.";
      break;
    case 109: 
      toSend = "A flowerbed filled with purple fluffy flowers.";
      break;
    case 110: 
      toSend = "A flowerbed filled with thick-stemmed white flowers with long leaves.";
      break;
    case 111: 
      toSend = "A flowerbed filled with uncommon white-dotted flowers.";
      break;
    case 112: 
      toSend = "A flowerbed filled with yellow prickly flowers.";
    }
    return toSend;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\behaviours\FenceBehaviour.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */