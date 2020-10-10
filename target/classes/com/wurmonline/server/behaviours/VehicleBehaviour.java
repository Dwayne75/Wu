package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.PlonkData;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.MountAction;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.VisionArea;
import com.wurmonline.server.items.DbItem;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSettings;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.questions.ManageObjectList.Type;
import com.wurmonline.server.questions.ManagePermissions;
import com.wurmonline.server.questions.PermissionsHistory;
import com.wurmonline.server.questions.SetDestinationQuestion;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.utils.logging.TileEvent;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class VehicleBehaviour
  extends ItemBehaviour
{
  private static final Logger logger = Logger.getLogger(VehicleBehaviour.class.getName());
  private static Vehicle vehicle;
  private static boolean addedPassenger;
  private static boolean addedDriver;
  
  public VehicleBehaviour()
  {
    super((short)41);
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item target)
  {
    List<ActionEntry> toReturn = new LinkedList();
    
    toReturn.addAll(super.getBehavioursFor(performer, target));
    toReturn.addAll(getVehicleBehaviours(performer, target));
    
    return toReturn;
  }
  
  private List<ActionEntry> getVehicleBehaviours(Creature performer, Item target)
  {
    List<ActionEntry> toReturn = new LinkedList();
    int x;
    if ((performer.getVehicle() == -10L) && (target.getOwnerId() == -10L)) {
      if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 8.0F)) {
        if ((performer.isSwimming()) || 
          (performer.getFloorLevel() == target.getFloorLevel()) || (performer.getPower() > 0))
        {
          VolaTile t = Zones.getTileOrNull(target.getTileX(), target.getTileY(), performer.isOnSurface());
          if ((t == null) || 
            (performer.getCurrentTile().getStructure() == t.getStructure()) || 
            ((performer.getCurrentTile().getStructure() != null) && 
            (performer.getCurrentTile().getStructure().isTypeBridge())) || (
            (t.getStructure() != null) && (t.getStructure().isTypeBridge())))
          {
            BlockingResult result = Blocking.getBlockerBetween(performer, target, 4);
            if (result == null)
            {
              boolean havePermission = hasPermission(performer, target);
              addedPassenger = ((havePermission) || (target.mayPassenger(performer)) ? 1 : 0) == 0;
              addedDriver = ((havePermission) || (target.mayCommand(performer)) ? 1 : 0) == 0;
              vehicle = Vehicles.getVehicle(target);
              if (vehicle != null)
              {
                if (!vehicle.isUnmountable()) {
                  for (x = 0; x < vehicle.seats.length; x++) {
                    if ((!addedDriver) && (!vehicle.seats[x].isOccupied()) && (vehicle.seats[x].type == 0))
                    {
                      if (!Items.isItemDragged(target)) {
                        toReturn.add(Actions.actionEntrys['ŋ']);
                      }
                      addedDriver = true;
                    }
                    else if ((!addedPassenger) && (!vehicle.seats[x].isOccupied()) && (vehicle.seats[x].type == 1))
                    {
                      if (vehicle.isChair())
                      {
                        toReturn.add(Actions.actionEntrys['ʽ']);
                        if ((vehicle.seats.length == 2) && 
                          (vehicle.seats[0].getType() == 1) && 
                          (vehicle.seats[1].getType() == 1))
                        {
                          if (!vehicle.seats[0].isOccupied()) {
                            toReturn.add(Actions.actionEntrys['ʾ']);
                          }
                          if (!vehicle.seats[1].isOccupied()) {
                            toReturn.add(Actions.actionEntrys['ʿ']);
                          }
                        }
                      }
                      else
                      {
                        toReturn.add(Actions.actionEntrys['Ō']);
                      }
                      addedPassenger = true;
                      if (addedDriver) {
                        break;
                      }
                    }
                  }
                }
                if (vehicle.hitched.length > 0) {
                  if (performer.getFollowers().length > 0) {
                    if ((!Items.isItemDragged(target)) && 
                      (target.getTopParent() == target.getWurmId()) && (
                      (!target.isTent()) || (target.getLastOwnerId() == performer.getWurmId()))) {
                      toReturn.add(Actions.actionEntrys['Ź']);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    if (performer.getVehicle() != -10L)
    {
      if ((performer.getVehicle() == target.getWurmId()) && (target.isBoat()))
      {
        if (target.getData() != -1L) {
          if (performer.isVehicleCommander()) {
            toReturn.add(Actions.actionEntrys['ũ']);
          }
        }
        if (target.getExtra() != -1L) {
          toReturn.add(new ActionEntry((short)944, "Detach keep net", "detaching"));
        }
        toReturn.add(Actions.actionEntrys['ſ']);
        if (((Features.Feature.BOAT_DESTINATION.isEnabled()) || (performer.getPower() >= 2)) && 
          (performer.isVehicleCommander())) {
          toReturn.add(Actions.actionEntrys['ˍ']);
        }
      }
      Vehicle vehicle = Vehicles.getVehicleForId(performer.getVehicle());
      if (vehicle.isChair()) {
        toReturn.add(Actions.actionEntrys['˄']);
      } else {
        toReturn.add(Actions.actionEntrys['ō']);
      }
    }
    if (target.getExtra() != -1L) {
      if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 
        (target.isVehicle()) && (!target.isTent()) ? Math.max(6, target.getSizeZ() / 100) : 6.0F))
      {
        boolean watching = false;
        try
        {
          Item keepnet = Items.getItem(target.getExtra());
          Creature[] watchers = keepnet.getWatchers();
          for (Creature lWatcher : watchers) {
            if (lWatcher == performer)
            {
              watching = true;
              break;
            }
          }
          if (watching) {
            toReturn.add(Actions.actionEntrys['έ']);
          }
        }
        catch (NoSuchItemException|NoSuchCreatureException localNoSuchItemException) {}
        if (!watching) {
          toReturn.add(Actions.actionEntrys['ά']);
        }
      }
    }
    List<ActionEntry> permissions = new LinkedList();
    if (target.mayManage(performer)) {
      if (target.getTemplateId() == 186) {
        permissions.add(Actions.actionEntrys['ʯ']);
      } else if (target.getTemplateId() == 539) {
        permissions.add(Actions.actionEntrys['ʙ']);
      } else if (target.getTemplateId() == 850) {
        permissions.add(Actions.actionEntrys['ʝ']);
      } else if (target.getTemplateId() == 853) {
        permissions.add(new ActionEntry((short)669, "Manage Ship Carrier", "managing"));
      } else if (target.getTemplateId() == 1410) {
        permissions.add(new ActionEntry((short)669, "Manage creature transporter", "managing"));
      } else if (target.isBoat()) {
        permissions.add(Actions.actionEntrys['ʜ']);
      }
    }
    if (target.maySeeHistory(performer)) {
      if (target.getTemplateId() == 186) {
        permissions.add(new ActionEntry((short)691, "History of Small Cart", "viewing"));
      } else if (target.getTemplateId() == 539) {
        permissions.add(new ActionEntry((short)691, "History of Large Cart", "viewing"));
      } else if (target.getTemplateId() == 850) {
        permissions.add(new ActionEntry((short)691, "History of Wagon", "viewing"));
      } else if (target.getTemplateId() == 853) {
        permissions.add(new ActionEntry((short)691, "History of Ship Carrier", "viewing"));
      } else if (target.getTemplateId() == 1410) {
        permissions.add(new ActionEntry((short)691, "History of Creature Transporter", "viewing"));
      } else if (target.isBoat()) {
        permissions.add(new ActionEntry((short)691, "History of Ship", "viewing"));
      }
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
    if (target.getWurmId() == performer.getVehicle())
    {
      if (performer.isVehicleCommander())
      {
        vehicle = Vehicles.getVehicle(target);
        if (vehicle != null) {
          if ((vehicle.getDraggers() != null) && (vehicle.getDraggers().size() > 0))
          {
            toReturn.add(new ActionEntry((short)-1, "Animals", "Animal options"));
            toReturn.add(Actions.actionEntrys['ź']);
          }
        }
      }
    }
    else if (performer.getVehicle() == -10L) {
      if ((hasKeyForVehicle(performer, target)) || (mayDriveVehicle(performer, target, null)))
      {
        vehicle = Vehicles.getVehicle(target);
        if (vehicle != null) {
          if ((vehicle.getDraggers() != null) && (vehicle.getDraggers().size() > 0))
          {
            toReturn.add(new ActionEntry((short)-1, "Animals", "Animal options"));
            toReturn.add(Actions.actionEntrys['ź']);
          }
        }
      }
    }
    return toReturn;
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target)
  {
    List<ActionEntry> toReturn = new LinkedList();
    toReturn.addAll(super.getBehavioursFor(performer, source, target));
    toReturn.addAll(getVehicleBehaviours(performer, target));
    if ((performer.getVehicle() == target.getWurmId()) && (target.isBoat()))
    {
      if (target.getData() == -1L) {
        if (source.isAnchor()) {
          toReturn.add(Actions.actionEntrys['Ũ']);
        }
      }
      if (((target.getTemplateId() == 490) || (target.getTemplateId() == 491)) && (target.getExtra() == -1L)) {
        if (source.getTemplateId() == 1342) {
          toReturn.add(new ActionEntry((short)943, "Attach keep net", "attaching"));
        }
      }
      if ((source.isDredgingTool()) && (target.isBoat()) && 
        (performer.getVehicle() == target.getWurmId()) && (performer.isOnSurface())) {
        toReturn.add(Actions.actionEntrys['Ū']);
      }
      if ((source.getTemplateId() == 1344) || (source.getTemplateId() == 1346)) {
        toReturn.add(Actions.actionEntrys[' ']);
      }
      if ((source.getTemplateId() == 1344) || (source.getTemplateId() == 1346)) {
        toReturn.add(new ActionEntry((short)285, "Lore", "thinking"));
      }
    }
    return toReturn;
  }
  
  public static final boolean hasKeyForVehicle(Creature performer, Item aVehicle)
  {
    if (aVehicle.isTent())
    {
      if (aVehicle.getLastOwnerId() == performer.getWurmId()) {
        return true;
      }
      return false;
    }
    long lockId = aVehicle.getLockId();
    if (lockId != -10L) {
      try
      {
        Item lock = Items.getItem(lockId);
        if (performer.hasKeyForLock(lock)) {
          return true;
        }
        return false;
      }
      catch (NoSuchItemException localNoSuchItemException) {}
    }
    VolaTile vt = Zones.getTileOrNull(aVehicle.getTileX(), aVehicle.getTileY(), aVehicle.isOnSurface());
    Village vill = vt == null ? null : vt.getVillage();
    return (vill == null) || (vill.isActionAllowed((short)6, performer));
  }
  
  public boolean action(Action act, Creature performer, Item target, short action, float counter)
  {
    boolean done = true;
    if ((action == 331) || (action == 332) || (action == 701) || (action == 702) || (action == 703))
    {
      if (target.getTopParent() != target.getWurmId())
      {
        performer.getCommunicator().sendNormalServerMessage(
          StringUtil.format("You can't embark upon the %s right now, it needs to be on the ground.", new Object[] {target
          .getName() }));
        return true;
      }
      if ((performer.getFloorLevel() != target.getFloorLevel()) && (!performer.isSwimming()))
      {
        performer.getCommunicator().sendNormalServerMessage("You are too far away now.");
        return done;
      }
      if (!GeneralUtilities.isOnSameLevel(performer, target)) {
        performer.getCommunicator().sendNormalServerMessage("You must be on the same level to embark.");
      }
      if (performer.isClimbing())
      {
        performer.getCommunicator().sendNormalServerMessage("You need to stop climbing first.");
        return done;
      }
      if (Math.abs(target.getPosZ() - performer.getPositionZ()) > 4.0F)
      {
        performer.getCommunicator().sendNormalServerMessage("You need to get closer to the " + target.getName() + ".");
        return done;
      }
      if ((target.isChair()) && (target.getPosZ() < 0.0F))
      {
        performer.getCommunicator().sendNormalServerMessage("You cannot sit on " + target.getName() + ". It is too wet.");
        return done;
      }
      if (target.isOwnedByWagoner())
      {
        performer.getCommunicator().sendNormalServerMessage("You cannot use the " + target.getName() + "  as a wagoner owns it.");
        return done;
      }
      if ((performer.getPower() < 2) && (action == 331) && (targetHasActiveQueen(target, performer))) {
        performer.getCommunicator().sendNormalServerMessage("The bees sting you.");
      }
      if ((performer.getVehicle() == -10L) && (target.getOwnerId() == -10L)) {
        if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 8.0F))
        {
          if (target.getCurrentQualityLevel() < 10.0F)
          {
            performer.getCommunicator().sendNormalServerMessage("The " + target
              .getName() + " is in too poor shape to be used.");
            return true;
          }
          if (target.isOnSurface() != performer.isOnSurface())
          {
            if (performer.isOnSurface()) {
              performer.getCommunicator().sendNormalServerMessage("You need to enter the cave first.");
            } else {
              performer.getCommunicator().sendNormalServerMessage("You need to leave the cave first.");
            }
            return true;
          }
          if (performer.getBridgeId() != target.getBridgeId())
          {
            performer.getCommunicator().sendNormalServerMessage("You need to be in the same structure as the " + target
              .getName() + ".");
            return true;
          }
          vehicle = Vehicles.getVehicle(target);
          if (vehicle != null)
          {
            String vehicName = Vehicle.getVehicleName(vehicle);
            if (action == 331)
            {
              if ((hasKeyForVehicle(performer, target)) || (mayDriveVehicle(performer, target, act)))
              {
                if (!Items.isItemDragged(target))
                {
                  if (canBeDriverOfVehicle(performer, vehicle))
                  {
                    addedDriver = false;
                    for (int x = 0; x < vehicle.seats.length; x++) {
                      if ((!vehicle.seats[x].isOccupied()) && (vehicle.seats[x].type == 0))
                      {
                        float r = -(target.getRotation() + 180.0F) * 3.1415927F / 180.0F;
                        float s = (float)Math.sin(r);
                        float c = (float)Math.cos(r);
                        float xo = s * -vehicle.seats[x].offx - c * -vehicle.seats[x].offy;
                        float yo = c * -vehicle.seats[x].offx + s * -vehicle.seats[x].offy;
                        float newposx = target.getPosX() + xo;
                        float newposy = target.getPosY() + yo;
                        BlockingResult result = Blocking.getBlockerBetween(performer, performer
                          .getPosX(), performer.getPosY(), newposx, newposy, performer
                          .getPositionZ(), target.getPosZ(), performer.isOnSurface(), target
                          .isOnSurface(), false, 4, -1L, performer
                          .getBridgeId(), target.getBridgeId(), false);
                        if (result == null)
                        {
                          TileEvent.log(target.getTileX(), target.getTileY(), target
                            .isOnSurface() ? 0 : -1, performer
                            .getWurmId(), action);
                          addedDriver = true;
                          vehicle.seats[x].occupy(vehicle, performer);
                          vehicle.pilotId = performer.getWurmId();
                          performer.setVehicleCommander(true);
                          MountAction m = new MountAction(null, target, vehicle, x, true, vehicle.seats[x].offz);
                          
                          performer.setMountAction(m);
                          performer.setVehicle(target.getWurmId(), true, (byte)0);
                          if ((performer.isPlayer()) && (((Player)performer).getAlcohol() > 5.0F)) {
                            if (target.isBoat()) {
                              performer.achievement(133);
                            } else if (!target.isChair()) {
                              performer.achievement(134);
                            }
                          }
                          switch (target.getTemplateId())
                          {
                          case 540: 
                            performer.achievement(54);
                            break;
                          case 542: 
                            performer.achievement(55);
                            break;
                          case 541: 
                            performer.achievement(56);
                            break;
                          case 490: 
                            performer.achievement(57);
                            break;
                          case 491: 
                            performer.achievement(58);
                            break;
                          case 543: 
                            performer.achievement(59);
                            break;
                          }
                          if (vehicle.hasDestinationSet())
                          {
                            ServerEntry entry = vehicle.getDestinationServer();
                            if ((!Servers.mayEnterServer(performer, entry)) || (((entry.PVPSERVER) || 
                              (entry.isChaosServer())) && 
                              (((Player)performer).isBlockingPvP())))
                            {
                              vehicle.clearDestination();
                              performer.getCommunicator()
                                .sendAlertServerMessage("The previous course is unavailable and has been cleared.");
                            }
                            else
                            {
                              performer.getCommunicator().sendAlertServerMessage("The " + vehicName + " is on a course for " + entry
                                .getName() + ".");
                            }
                          }
                          if ((!target.isBoat()) || (PlonkData.ON_A_BOAT.hasSeenThis(performer))) {
                            break;
                          }
                          PlonkData.ON_A_BOAT.trigger(performer); break;
                        }
                      }
                    }
                    if (!addedDriver)
                    {
                      String text = "You may not %s the %s as a %s right now.The space is occupied or unreachable.";
                      
                      performer.getCommunicator().sendNormalServerMessage(
                        StringUtil.format("You may not %s the %s as a %s right now.The space is occupied or unreachable.", new Object[] { vehicle.embarkString, vehicName, vehicle.pilotName }));
                    }
                  }
                  else
                  {
                    String text = "You are not smart enough to figure out how to be the %s of the %s. You need %.2f in %s.";
                    
                    performer.getCommunicator().sendNormalServerMessage(
                      StringUtil.format("You are not smart enough to figure out how to be the %s of the %s. You need %.2f in %s.", new Object[] { vehicle.pilotName, vehicName, 
                      
                      Float.valueOf(vehicle.skillNeeded), 
                      SkillSystem.getNameFor(100) }));
                  }
                }
                else
                {
                  String text = "You may not %s the %s as a %s right now. It is being dragged.";
                  performer.getCommunicator().sendNormalServerMessage(
                    StringUtil.format("You may not %s the %s as a %s right now. It is being dragged.", new Object[] { vehicle.embarkString, vehicName, vehicle.pilotName }));
                }
              }
              else
              {
                String text = "You are not allowed to %s the %s as a %s.";
                performer.getCommunicator().sendNormalServerMessage(
                  StringUtil.format("You are not allowed to %s the %s as a %s.", new Object[] { vehicle.embarkString, vehicName, vehicle.pilotName }));
              }
            }
            else if ((action == 332) || (action == 701) || (action == 702) || (action == 703)) {
              actionEmbarkPassenger(performer, target, action);
            }
          }
          else if ((action == 701) || (action == 702) || (action == 703))
          {
            performer.getCommunicator().sendNormalServerMessage("You can't sit on that right now.");
          }
          else
          {
            performer.getCommunicator().sendNormalServerMessage("You can't embark on that right now.");
          }
        }
        else
        {
          performer.getCommunicator().sendNormalServerMessage("You are too far away now.");
        }
      }
    }
    else if (Actions.isActionDisembark(action))
    {
      if (performer.getVehicle() != -10L)
      {
        if (performer.getVisionArea() != null) {
          performer.getVisionArea().broadCastUpdateSelectBar(performer.getWurmId(), true);
        }
        performer.disembark(true);
      }
    }
    else if (action == 361)
    {
      done = actionRaiseAnchor(performer, target, counter, done);
    }
    else if (action == 944)
    {
      done = actionDetachKeepnet(act, performer, target, counter, done);
    }
    else if (action == 940)
    {
      if (target.getExtra() != -1L) {
        if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 8.0F)) {
          try
          {
            Item keepnet = Items.getItem(target.getExtra());
            if (performer.addItemWatched(keepnet))
            {
              performer.getCommunicator().sendOpenInventoryWindow(keepnet.getWurmId(), keepnet.getName());
              keepnet.addWatcher(keepnet.getWurmId(), performer);
              keepnet.sendContainedItems(keepnet.getWurmId(), performer);
            }
            performer.getCommunicator().sendUpdateSelectBar(target.getWurmId(), false);
          }
          catch (NoSuchItemException e)
          {
            logger.log(Level.WARNING, e.getMessage(), e);
          }
        }
      }
    }
    else
    {
      if (action == 941)
      {
        if (target.getExtra() != -1L) {
          try
          {
            Item keepnet = Items.getItem(target.getExtra());
            keepnet.close(performer);
            performer.getCommunicator().sendUpdateSelectBar(target.getWurmId(), false);
          }
          catch (NoSuchItemException e)
          {
            logger.log(Level.WARNING, e.getMessage(), e);
          }
        }
        return true;
      }
      if ((action == 717) && ((Features.Feature.BOAT_DESTINATION.isEnabled()) || (performer.getPower() >= 2)))
      {
        SetDestinationQuestion sdq = new SetDestinationQuestion(performer, target);
        sdq.sendQuestion();
        done = true;
      }
      else if ((action == 717) && (Features.Feature.BOAT_DESTINATION.isEnabled()))
      {
        SetDestinationQuestion sdq = new SetDestinationQuestion(performer, target);
        sdq.sendQuestion();
        done = true;
      }
      else if (action == 378)
      {
        boolean ok = false;
        if (target.getWurmId() == performer.getVehicle())
        {
          if (performer.isVehicleCommander()) {
            ok = true;
          }
        }
        else if (performer.getVehicle() == -10L) {
          if ((hasKeyForVehicle(performer, target)) || (mayDriveVehicle(performer, target, act))) {
            ok = true;
          }
        }
        if (!ok)
        {
          performer.getCommunicator().sendNormalServerMessage("You are not allowed to do that.");
          return true;
        }
        if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 8.0F))
        {
          vehicle = Vehicles.getVehicle(target);
          if (vehicle != null) {
            if (vehicle.draggers != null) {
              if (vehicle.draggers.size() > 0)
              {
                Creature[] crets = (Creature[])vehicle.draggers.toArray(new Creature[vehicle.draggers.size()]);
                for (int x = 0; x < crets.length; x++)
                {
                  if (!vehicle.positionDragger(crets[x], performer))
                  {
                    performer.getCommunicator().sendNormalServerMessage("You can't unhitch the " + crets[x]
                      .getName() + " here. Please move the vehicle.");
                    return true;
                  }
                  Zone z = null;
                  try
                  {
                    z = Zones.getZone(crets[x].getTileX(), crets[x].getTileY(), crets[x]
                      .isOnSurface());
                    crets[x].getStatus().savePosition(crets[x].getWurmId(), true, z.getId(), true);
                  }
                  catch (Exception localException) {}
                  Creatures.getInstance().setLastLed(crets[x].getWurmId(), performer.getWurmId());
                  vehicle.removeDragger(crets[x]);
                  VolaTile t = crets[x].getCurrentTile();
                  if (t != null)
                  {
                    t.sendAttachCreature(crets[x].getWurmId(), -10L, 0.0F, 0.0F, 0.0F, 0);
                    if (z != null) {
                      try
                      {
                        z.removeCreature(crets[x], true, false);
                        z.addCreature(crets[x].getWurmId());
                      }
                      catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
                    }
                  }
                }
              }
            }
          }
        }
        else
        {
          performer.getCommunicator().sendNormalServerMessage("You are too far away to do that.");
          return true;
        }
      }
      else if (action == 383)
      {
        if (performer.getVehicle() == target.getWurmId())
        {
          Item[] its = target.getAllItems(true);
          performer.getCommunicator().sendNormalServerMessage("The " + target
            .getName() + " contains " + its.length + " items.");
        }
        else
        {
          performer.getCommunicator().sendNormalServerMessage("You miscalculate.. 200-43.. is that 12? No. No that can't be it. Damn it's so hard.");
        }
        done = true;
      }
      else if (action == 687)
      {
        if (target.mayManage(performer))
        {
          ManagePermissions mp = new ManagePermissions(performer, ManageObjectList.Type.SMALL_CART, target, false, -10L, false, null, "");
          
          mp.sendQuestion();
        }
      }
      else if (action == 665)
      {
        if (target.mayManage(performer))
        {
          ManagePermissions mp = new ManagePermissions(performer, ManageObjectList.Type.LARGE_CART, target, false, -10L, false, null, "");
          
          mp.sendQuestion();
        }
      }
      else if (action == 669)
      {
        if (target.mayManage(performer))
        {
          ManageObjectList.Type molt = ManageObjectList.Type.WAGON;
          if (target.getTemplateId() == 853) {
            molt = ManageObjectList.Type.SHIP_CARRIER;
          } else if (target.getTemplateId() == 1410) {
            molt = ManageObjectList.Type.CREATURE_CARRIER;
          }
          ManagePermissions mp = new ManagePermissions(performer, molt, target, false, -10L, false, null, "");
          
          mp.sendQuestion();
        }
      }
      else if (action == 668)
      {
        if (target.mayManage(performer))
        {
          ManagePermissions mp = new ManagePermissions(performer, ManageObjectList.Type.SHIP, target, false, -10L, false, null, "");
          
          mp.sendQuestion();
        }
      }
      else if (action == 691)
      {
        if (target.maySeeHistory(performer))
        {
          PermissionsHistory ph = new PermissionsHistory(performer, target.getWurmId());
          ph.sendQuestion();
        }
      }
      else if (action == 377)
      {
        if ((performer.getDraggedItem() != null) && (performer.getDraggedItem() == target))
        {
          performer.getCommunicator().sendNormalServerMessage("You must stop dragging the " + target
            .getName() + " before you hitch creatures to it.");
          return true;
        }
        if (target.getTopParent() != target.getWurmId())
        {
          String message = "The %s needs to be on the ground before you can hitch anything to it.";
          performer.getCommunicator().sendNormalServerMessage(
            StringUtil.format("The %s needs to be on the ground before you can hitch anything to it.", new Object[] {target.getName() }));
          return true;
        }
        if (Items.isItemDragged(target))
        {
          performer.getCommunicator().sendNormalServerMessage("The " + target
            .getName() + " is dragged and you can not hitch creatures to it.");
          return true;
        }
        if ((target.getCurrentQualityLevel() < 10.0F) && (!target.isTent()))
        {
          performer.getCommunicator().sendNormalServerMessage("The " + target
            .getName() + " is in too poor shape to be dragged by animals.");
          return true;
        }
        if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 8.0F))
        {
          if ((hasKeyForVehicle(performer, target)) || (mayDriveVehicle(performer, target, act)))
          {
            if ((!target.isTent()) || (target.getLastOwnerId() == performer.getWurmId()))
            {
              vehicle = Vehicles.getVehicle(target);
              actionHitch(performer, target);
            }
          }
          else {
            performer.getCommunicator().sendNormalServerMessage("You can't mount that right now so you can't hitch either.");
          }
        }
        else {
          performer.getCommunicator().sendNormalServerMessage("You can't reach that right now.");
        }
        done = true;
      }
      else
      {
        done = super.action(act, performer, target, action, counter);
      }
    }
    return done;
  }
  
  private static final boolean targetHasActiveQueen(Item carrier, Creature performer)
  {
    for (Item item : carrier.getItemsAsArray()) {
      if ((item.getTemplateId() == 1175) && (item.hasQueen()) && (!WurmCalendar.isSeasonWinter())) {
        if (performer.getBestBeeSmoker() == null)
        {
          performer.addWoundOfType(null, (byte)5, 2, true, 1.0F, false, 4000.0F + Server.rand
            .nextFloat() * 3000.0F, 0.0F, 0.0F, false, false);
          return true;
        }
      }
    }
    return false;
  }
  
  private void actionEmbarkPassenger(Creature performer, Item target, short action)
  {
    if ((vehicle.hasDestinationSet()) && (vehicle.getDestinationServer().PVPSERVER) && (((Player)performer).isBlockingPvP()))
    {
      performer.getCommunicator().sendAlertServerMessage("The " + Vehicle.getVehicleName(vehicle) + " is on a course for hostile territory, but you have elected to avoid hostility. You may change this preference in your profile.");
      
      return;
    }
    if ((hasKeyForVehicle(performer, target)) || (mayEmbarkVehicle(performer, target)))
    {
      if ((performer.getDraggedItem() == null) || (performer.getDraggedItem().getWurmId() != target.getWurmId()))
      {
        addedPassenger = false;
        TileEvent.log(target.getTileX(), target.getTileY(), target.isOnSurface() ? 0 : -1, performer.getWurmId(), action);
        
        boolean wallInWay = false;
        if ((action == 332) || (action == 701)) {
          for (int x = 0; x < vehicle.seats.length; x++)
          {
            wallInWay = addPassenger(performer, vehicle, target, x) ? true : wallInWay;
            if (addedPassenger) {
              break;
            }
          }
        } else if (action == 702) {
          wallInWay = addPassenger(performer, vehicle, target, 0) ? true : wallInWay;
        } else if (action == 703) {
          wallInWay = addPassenger(performer, vehicle, target, 1) ? true : wallInWay;
        }
        if (!addedPassenger)
        {
          performer.getCommunicator().sendNormalServerMessage(
            StringUtil.format("You may not %s the %s as a passenger right now.", new Object[] { vehicle.embarkString, 
            
            Vehicle.getVehicleName(vehicle) }));
          if (wallInWay) {
            performer.getCommunicator().sendNormalServerMessage("The wall is in the way. You can not reach a seat.");
          } else {
            performer.getCommunicator().sendNormalServerMessage("The seats are all occupied.");
          }
        }
      }
      else
      {
        performer.getCommunicator().sendNormalServerMessage(
          StringUtil.format("You may not %s the %s since you are dragging it.", new Object[] { vehicle.embarkString, 
          
          Vehicle.getVehicleName(vehicle) }));
      }
    }
    else {
      performer.getCommunicator().sendNormalServerMessage("You are not allowed to " + vehicle.embarkString + " the " + target
        .getName() + ".");
    }
  }
  
  public static boolean addPassenger(Creature performer, Vehicle vehicle, Item target, int seatNum)
  {
    boolean wallInWay = false;
    if ((seatNum >= 0) && (seatNum < vehicle.seats.length) && 
      (!vehicle.seats[seatNum].isOccupied()) && (vehicle.seats[seatNum].type == 1))
    {
      float r = -(target.getRotation() + 180.0F) * 3.1415927F / 180.0F;
      float s = (float)Math.sin(r);
      float c = (float)Math.cos(r);
      float xo = s * -vehicle.seats[seatNum].offx - c * -vehicle.seats[seatNum].offy;
      float yo = c * -vehicle.seats[seatNum].offx + s * -vehicle.seats[seatNum].offy;
      float newposx = target.getPosX() + xo;
      float newposy = target.getPosY() + yo;
      
      BlockingResult result = Blocking.getBlockerBetween(performer, performer.getPosX(), performer
        .getPosY(), newposx, newposy, performer.getPositionZ(), target.getPosZ(), performer
        .isOnSurface(), target.isOnSurface(), false, 4, -1L, performer
        .getBridgeId(), target.getBridgeId(), false);
      if (result == null)
      {
        addedPassenger = true;
        vehicle.seats[seatNum].occupy(vehicle, performer);
        MountAction m = new MountAction(null, target, vehicle, seatNum, false, vehicle.seats[seatNum].offz);
        
        performer.setMountAction(m);
        performer.setVehicle(target.getWurmId(), true, (byte)1);
        if ((performer.isPlayer()) && (((Player)performer).getAlcohol() > 5.0F) && 
          (target.isBoat())) {
          performer.achievement(133);
        }
        if (vehicle.hasDestinationSet())
        {
          ServerEntry entry = vehicle.getDestinationServer();
          if ((entry.PVPSERVER) && ((!entry.EPIC) || (Server.getInstance().isPS())))
          {
            byte pKingdom = ((Player)performer).getSaveFile().getChaosKingdom() == 0 ? 4 : ((Player)performer).getSaveFile().getChaosKingdom();
            String toWhere = "The " + vehicle.getName() + " will be heading to " + entry.getName() + ", which is hostile territory";
            if (pKingdom != performer.getKingdomId()) {
              toWhere = toWhere + " and you will join the " + Kingdoms.getNameFor(pKingdom) + " kingdom until you return";
            }
            performer.getCommunicator().sendAlertServerMessage(toWhere + ".");
            vehicle.alertPassengerOfEnemies(performer, entry, true);
          }
          else
          {
            performer.getCommunicator().sendAlertServerMessage("The " + vehicle
              .getName() + " will be heading to " + entry.getName() + ".");
          }
        }
        if ((target.isBoat()) && (!PlonkData.ON_A_BOAT.hasSeenThis(performer))) {
          PlonkData.ON_A_BOAT.trigger(performer);
        }
      }
      else
      {
        wallInWay = true;
      }
    }
    return wallInWay;
  }
  
  private boolean actionRaiseAnchor(Creature performer, Item target, float counter, boolean done)
  {
    if (performer.getVehicle() != -10L) {
      if ((performer.getVehicle() == target.getWurmId()) && (target.isBoat())) {
        if ((target.getData() != -1L) && (performer.isVehicleCommander())) {
          try
          {
            Item anchor = Items.getItem(target.getData());
            done = false;
            if (counter == 1.0F)
            {
              performer.getCommunicator().sendNormalServerMessage("You start to raise the " + anchor
                .getName() + ".");
              Server.getInstance().broadCastAction(performer
                .getName() + " starts to raise the " + anchor.getName() + ".", performer, 5);
              performer.sendActionControl(Actions.actionEntrys['ũ']
                .getVerbString(), true, 100);
            }
            if (counter > 10.0F)
            {
              done = true;
              performer.getInventory().insertItem(anchor, true);
              target.setData(-1L);
              performer.getCommunicator().sendNormalServerMessage("You raise the " + anchor.getName() + ".");
              Server.getInstance().broadCastAction(performer.getName() + " raises the " + anchor.getName() + ".", performer, 5);
              
              Vehicle veh = Vehicles.getVehicle(target);
              try
              {
                Creature pilot = Server.getInstance().getCreature(veh.getPilotId());
                pilot.getMovementScheme().addWindImpact(veh.getWindImpact());
                pilot.getMovementScheme().setMooredMod(false);
              }
              catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
            }
          }
          catch (NoSuchItemException nsi)
          {
            logger.log(Level.INFO, "No such anchor item.");
          }
        }
      }
    }
    return done;
  }
  
  private boolean actionDetachKeepnet(Action act, Creature performer, Item target, float counter, boolean done)
  {
    if (performer.getVehicle() != -10L) {
      if ((performer.getVehicle() == target.getWurmId()) && (target.isBoat())) {
        if (target.getExtra() != -1L) {
          try
          {
            Item keepnet = Items.getItem(target.getExtra());
            if (!keepnet.isEmpty(false))
            {
              performer.getCommunicator().sendNormalServerMessage("The " + keepnet
                .getName() + " must be empty to be detached.");
              return true;
            }
            done = false;
            int time = 20;
            if (counter == 1.0F)
            {
              performer.getCommunicator().sendNormalServerMessage("You start to detach the " + keepnet
                .getName() + ".");
              Server.getInstance().broadCastAction(performer
                .getName() + " starts to detach a " + keepnet.getName() + ".", performer, 5);
              performer.sendActionControl(Actions.actionEntrys['ΰ']
                .getVerbString(), true, time);
              act.setTimeLeft(time);
            }
            time = act.getTimeLeft();
            if (counter * 10.0F > time)
            {
              try
              {
                for (Creature c : keepnet.getWatchers())
                {
                  keepnet.close(c);
                  c.getCommunicator().sendUpdateSelectBar(target.getWurmId(), false);
                }
              }
              catch (NoSuchCreatureException localNoSuchCreatureException1) {}
              done = true;
              performer.getInventory().insertItem(keepnet, true);
              target.setExtra(-1L);
              keepnet.setData(-1L);
              
              VolaTile vt = Zones.getOrCreateTile(target.getTileX(), target.getTileY(), target.isOnSurface());
              vt.sendBoatDetachment(target.getWurmId(), keepnet.getTemplateId(), (byte)1);
              
              performer.getCommunicator().sendNormalServerMessage("You detach the " + keepnet.getName() + ".");
              Server.getInstance().broadCastAction(performer.getName() + " detachs the " + keepnet.getName() + ".", performer, 5);
            }
          }
          catch (NoSuchItemException nsi)
          {
            logger.log(Level.INFO, "No such keepnet item.");
          }
        }
      }
    }
    return done;
  }
  
  private void actionHitch(Creature performer, Item target)
  {
    if (vehicle.hitched.length > 0)
    {
      if (vehicle.hasHumanDragger())
      {
        performer.getCommunicator().sendNormalServerMessage("Someone is dragging the " + target.getName().toLowerCase() + " already.");
        return;
      }
      if (vehicle.mayAddDragger())
      {
        Creature[] folls = performer.getFollowers();
        if (folls.length > 0) {
          for (int x = 0; x < folls.length; x++) {
            if (folls[x].isOnSurface() == target.isOnSurface())
            {
              if (!folls[x].isRidden())
              {
                if ((folls[x].isDomestic()) || (folls[x].getStatus().getBattleRatingTypeModifier() <= 1.2F))
                {
                  if (isStrongEnoughToDrag(folls[x], target))
                  {
                    if (folls[x].canUseWithEquipment())
                    {
                      if (vehicle.addDragger(folls[x]))
                      {
                        folls[x].setLeader(null);
                        folls[x].setHitched(vehicle, false);
                        folls[x].setVisible(false);
                        performer.getCommunicator()
                          .sendNormalServerMessage(
                          StringUtil.format("You hitch the %s to the %s.", new Object[] {
                          
                          StringUtil.toLowerCase(folls[x].getName()), 
                          StringUtil.toLowerCase(target.getName()) }));
                        
                        Seat driverseat = vehicle.getHitchSeatFor(folls[x].getWurmId());
                        float _r = (-target.getRotation() + 180.0F) * 3.1415927F / 180.0F;
                        float _s = (float)Math.sin(_r);
                        float _c = (float)Math.cos(_r);
                        float xo = _s * -driverseat.offx - _c * -driverseat.offy;
                        float yo = _c * -driverseat.offx + _s * -driverseat.offy;
                        float nPosX = target.getPosX() + xo;
                        float nPosY = target.getPosY() + yo;
                        float nPosZ = target.getPosZ() + driverseat.offz;
                        
                        folls[x].getStatus().setPositionX(nPosX);
                        folls[x].getStatus().setPositionY(nPosY);
                        folls[x].setRotation(-target.getRotation());
                        folls[x].getMovementScheme().setPosition(folls[x].getStatus().getPositionX(), folls[x]
                          .getStatus().getPositionY(), nPosZ, folls[x]
                          .getStatus().getRotation(), folls[x].getLayer());
                        folls[x].getCurrentTile().sendAttachCreature(folls[x].getWurmId(), target
                          .getWurmId(), vehicle.getHitchSeatFor(folls[x].getWurmId()).offx, 
                          vehicle.getHitchSeatFor(folls[x].getWurmId()).offy, 
                          vehicle.getHitchSeatFor(folls[x].getWurmId()).offz, vehicle
                          .getSeatNumberFor(vehicle.getHitchSeatFor(folls[x].getWurmId())));
                        folls[x].setVisible(true);
                        break;
                      }
                      performer.getCommunicator().sendNormalServerMessage(
                        StringUtil.format("The %s could not be hitched right now.", new Object[] {
                        StringUtil.toLowerCase(folls[x].getName()) }));
                    }
                    else
                    {
                      performer.getCommunicator().sendNormalServerMessage(
                        StringUtil.format("The %s looks confused by its equipment and refuses to move.", new Object[] {
                        StringUtil.toLowerCase(folls[x].getName()) }));
                    }
                  }
                  else {
                    performer.getCommunicator().sendNormalServerMessage(
                      StringUtil.format("The %s is too weak.", new Object[] {
                      StringUtil.toLowerCase(folls[x].getName()) }));
                  }
                }
                else {
                  performer.getCommunicator().sendNormalServerMessage(
                    StringUtil.format("The %s is too unruly to be hitched.", new Object[] {
                    StringUtil.toLowerCase(folls[x].getName()) }));
                }
              }
              else {
                performer.getCommunicator().sendNormalServerMessage(
                  StringUtil.format("The %s is ridden and may not drag now.", new Object[] {
                  StringUtil.toLowerCase(folls[x].getName()) }));
              }
            }
            else {
              performer.getCommunicator().sendNormalServerMessage(
                StringUtil.format("The %s is not close enough to the %s.", new Object[] {
                
                StringUtil.toLowerCase(folls[x].getName()), 
                StringUtil.toLowerCase(target.getName()) }));
            }
          }
        } else {
          performer.getCommunicator().sendNormalServerMessage(
            StringUtil.format("You have no creature to hitch to the %s.", new Object[] {
            
            StringUtil.toLowerCase(target.getName()) }));
        }
      }
      else
      {
        performer.getCommunicator().sendNormalServerMessage(
          StringUtil.format("The %s has no spaces left to hitch to.", new Object[] {
          
          StringUtil.toLowerCase(target.getName()) }));
      }
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage(
        StringUtil.format("The %s has no spaces to hitch to.", new Object[] {
        
        StringUtil.toLowerCase(target.getName()) }));
    }
  }
  
  public boolean isStrongEnoughToDrag(Creature creature, Item aVehicle)
  {
    return creature.getStrengthSkill() > aVehicle.getTemplate().getWeightGrams() / 10000.0F;
  }
  
  public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter)
  {
    boolean done = true;
    if ((action == 331) || (action == 332) || (action == 333) || (action == 708) || (action == 361) || (action == 383) || (action == 378) || (action == 687) || (action == 665) || (action == 669) || (action == 668) || (action == 691) || (action == 944) || (action == 940))
    {
      done = action(act, performer, target, action, counter);
    }
    else
    {
      if ((action == 671) || (action == 672))
      {
        if ((source.getTemplateId() == 319) || (source.getTemplateId() == 1029)) {
          return CargoTransportationMethods.haul(performer, target, counter, action, act);
        }
        return true;
      }
      if (action == 360)
      {
        if ((performer.getVehicle() == target.getWurmId()) && (target.isBoat())) {
          if (target.getData() == -1L) {
            if (source.isAnchor())
            {
              done = false;
              if (counter == 1.0F)
              {
                performer.getCommunicator().sendNormalServerMessage("You start to moor the " + target
                  .getName() + ".");
                Server.getInstance().broadCastAction(performer
                  .getName() + " starts to moor the " + target.getName() + ".", performer, 5);
                performer.sendActionControl(Actions.actionEntrys['Ũ'].getVerbString(), true, 10);
              }
              if (counter > 1.0F)
              {
                done = true;
                source.putInVoid();
                target.setData(source.getWurmId());
                Vehicle veh = Vehicles.getVehicle(target);
                try
                {
                  Creature pilot = Server.getInstance().getCreature(veh.getPilotId());
                  pilot.getMovementScheme().addWindImpact((byte)0);
                  pilot.getMovementScheme().setMooredMod(true);
                }
                catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
                target.savePosition();
                performer.getCommunicator().sendNormalServerMessage("You moor the " + target.getName() + ".");
                Server.getInstance().broadCastAction(performer.getName() + " moors the " + target.getName() + ".", performer, 5);
                if (Item.getMaterialAnchorBonus(source.getMaterial()) < 1.0F) {
                  performer.getCommunicator().sendNormalServerMessage("You're unsure if the " + source.getName() + " is of a heavy enough material to completely stop the " + target
                    .getName() + " from drifting.");
                }
              }
            }
          }
        }
      }
      else if (action == 943)
      {
        if ((performer.getVehicle() == target.getWurmId()) && ((target.getTemplateId() == 490) || (target.getTemplateId() == 491))) {
          if (target.getExtra() == -1L) {
            if (source.getTemplateId() == 1342)
            {
              done = false;
              int time = 20;
              if (counter == 1.0F)
              {
                performer.getCommunicator().sendNormalServerMessage("You start to attach the " + source
                  .getName() + " to the " + target.getName() + ".");
                Server.getInstance().broadCastAction(performer
                  .getName() + " starts to attach a " + source.getName() + ".", performer, 5);
                performer.sendActionControl(Actions.actionEntrys['ί'].getVerbString(), true, time);
                
                act.setTimeLeft(time);
              }
              time = act.getTimeLeft();
              if (counter * 10.0F > time)
              {
                done = true;
                source.putInVoid();
                target.setExtra(source.getWurmId());
                source.setData(target.getWurmId());
                ((DbItem)target).maybeUpdateKeepnetPos();
                
                VolaTile vt = Zones.getOrCreateTile(target.getTileX(), target.getTileY(), target.isOnSurface());
                vt.sendBoatAttachment(target.getWurmId(), source.getTemplateId(), source.getMaterial(), (byte)1, source.getAuxData());
                
                performer.getCommunicator().sendNormalServerMessage("You attach the " + source.getName() + " to the " + target
                  .getName() + ".");
                Server.getInstance().broadCastAction(performer.getName() + " attaches a " + source.getName() + " to the " + target
                  .getName() + ".", performer, 5);
              }
            }
          }
        }
      }
      else if (action == 362)
      {
        done = true;
        if ((performer.getVehicle() == target.getWurmId()) && (target.isBoat())) {
          if (source.isDredgingTool())
          {
            if (performer.isOnSurface()) {
              try
              {
                Item boat = Items.getItem(performer.getVehicle());
                if ((boat.isOnSurface()) && (boat.getPosZ() <= 0.0F))
                {
                  int tile = Server.surfaceMesh.getTile(boat.getTileX(), boat.getTileY());
                  if (!Terraforming.isNonDiggableTile(Tiles.decodeType(tile))) {
                    done = Terraforming.dig(performer, source, boat.getTileX(), boat.getTileY(), tile, counter, false, performer
                      .isOnSurface() ? Server.surfaceMesh : Server.caveMesh);
                  } else {
                    performer.getCommunicator().sendNormalServerMessage("You may not dredge here.");
                  }
                }
                else
                {
                  performer.getCommunicator().sendNormalServerMessage("You may not dredge here.");
                }
              }
              catch (NoSuchItemException localNoSuchItemException) {}
            } else {
              performer.getCommunicator().sendNormalServerMessage("You may not dredge here.");
            }
          }
          else {
            performer.getCommunicator().sendNormalServerMessage("You may not use that.");
          }
        }
      }
      else if (action == 160)
      {
        try
        {
          Item boat = Items.getItem(performer.getVehicle());
          int tile = Server.surfaceMesh.getTile(boat.getTileX(), boat.getTileY());
          if (!performer.isOnSurface()) {
            tile = Server.caveMesh.getTile(boat.getTileX(), boat.getTileY());
          }
          if ((source.getTemplateId() == 1344) || (source.getTemplateId() == 1346)) {
            done = MethodsFishing.fish(performer, source, boat.getTileX(), boat.getTileY(), tile, counter, act);
          } else {
            done = true;
          }
        }
        catch (NoSuchItemException localNoSuchItemException1) {}
      }
      else if (action == 285)
      {
        done = true;
        if ((source.getTemplateId() == 1344) || (source.getTemplateId() == 1346)) {
          try
          {
            Item boat = Items.getItem(performer.getVehicle());
            done = MethodsFishing.showFishTable(performer, source, boat.getTileX(), boat.getTileY(), counter, act);
          }
          catch (NoSuchItemException localNoSuchItemException2) {}
        }
      }
      else
      {
        done = super.action(act, performer, source, target, action, counter);
      }
    }
    return done;
  }
  
  public static boolean canBeDriverOfVehicle(Creature aPerformer, Vehicle aVehicle)
  {
    boolean toReturn = false;
    if ((aVehicle != null) && (aPerformer != null))
    {
      if (aVehicle.isUnmountable()) {
        return false;
      }
      Skill checkSkill = null;
      if (aVehicle.creature) {
        try
        {
          checkSkill = aPerformer.getSkills().getSkill(104);
        }
        catch (NoSuchSkillException nss)
        {
          logger.log(Level.WARNING, aPerformer.getName() + " no body control.");
          checkSkill = aPerformer.getSkills().learn(104, 1.0F);
        }
      } else {
        try
        {
          checkSkill = aPerformer.getSkills().getSkill(100);
        }
        catch (NoSuchSkillException nss)
        {
          logger.log(Level.WARNING, aPerformer.getName() + " no mind logic.");
          checkSkill = aPerformer.getSkills().learn(100, 1.0F);
        }
      }
      if (checkSkill.getRealKnowledge() > aVehicle.skillNeeded) {
        toReturn = true;
      }
    }
    return toReturn;
  }
  
  public static boolean mayDriveVehicle(Creature aPerformer, Item aVehicle, @Nullable Action act)
  {
    if ((aPerformer == null) || (aVehicle == null))
    {
      logger.warning("null arguments - Performer: " + aPerformer + ", Vehicle: " + aVehicle);
      return false;
    }
    if (aVehicle.isTent()) {
      return false;
    }
    if ((aPerformer.getWurmId() == aVehicle.lastOwner) || (aVehicle.lastOwner == -10L)) {
      return true;
    }
    if (aVehicle.mayCommand(aPerformer)) {
      return true;
    }
    if ((aVehicle.isInPvPZone()) && (MethodsItems.checkIfStealing(aVehicle, aPerformer, act))) {
      return true;
    }
    return false;
  }
  
  public static boolean mayEmbarkVehicle(Creature aPerformer, Item aVehicle)
  {
    if ((aPerformer == null) || (aVehicle == null))
    {
      logger.warning("null arguments - Performer: " + aPerformer + ", Vehicle: " + aVehicle);
      return false;
    }
    if (aVehicle.isTent()) {
      return false;
    }
    if (aVehicle.isChair()) {
      return true;
    }
    return aVehicle.mayPassenger(aPerformer);
  }
  
  public static final boolean isFriendAndMayMount(Creature aPerformer, Item aVehicle)
  {
    if (aVehicle.isTent()) {
      return false;
    }
    if (ItemSettings.mayCommand(aVehicle, aPerformer)) {
      return true;
    }
    return false;
  }
  
  public static boolean mayDriveVehicle(Creature aPerformer, Creature aVehicle)
  {
    if ((aPerformer == null) || (aVehicle == null))
    {
      logger.warning("null arguments - Performer: " + aPerformer + ", Vehicle: " + aVehicle);
      return false;
    }
    if ((aPerformer.getWurmId() == aVehicle.dominator) || (aVehicle.getLeader() == aPerformer))
    {
      if ((!Servers.isThisAPvpServer()) && (aVehicle.isBranded())) {
        return aVehicle.mayCommand(aPerformer);
      }
      return true;
    }
    return false;
  }
  
  public static boolean mayEmbarkVehicle(Creature aPerformer, Creature aVehicle)
  {
    if ((aPerformer == null) || (aVehicle == null))
    {
      logger.warning("null arguments - Performer: " + aPerformer + ", Vehicle: " + aVehicle);
      return false;
    }
    if ((aVehicle.dominator != -10L) || (aVehicle.getLeader() != null))
    {
      if ((!Servers.isThisAPvpServer()) && (aVehicle.isBranded())) {
        return aVehicle.mayPassenger(aPerformer);
      }
      return true;
    }
    return false;
  }
  
  static boolean hasPermission(Creature performer, Item target)
  {
    if (target.isChair())
    {
      if (target.isOwnedByWagoner()) {
        return false;
      }
      return true;
    }
    if (!target.isLocked())
    {
      VolaTile vt = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface());
      Village vill = vt == null ? null : vt.getVillage();
      return (vill == null) || (vill.isActionAllowed((short)6, performer));
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\behaviours\VehicleBehaviour.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */