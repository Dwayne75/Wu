package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.CaveTile;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.mesh.Tiles.TileBorderDirection;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.NoSuchWallException;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.support.Tickets;
import com.wurmonline.server.tutorial.Mission;
import com.wurmonline.server.tutorial.MissionPerformed;
import com.wurmonline.server.tutorial.Missions;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BehaviourDispatcher
  implements CounterTypes, ItemTypes, MiscConstants
{
  private static final Logger logger = Logger.getLogger(BehaviourDispatcher.class.getName());
  private static List<ActionEntry> availableActions = null;
  private static final List<ActionEntry> emptyActions = new LinkedList();
  
  public static void requestSelectionActions(Creature creature, Communicator comm, byte requestId, long subject, long target)
    throws NoSuchBehaviourException, NoSuchPlayerException, NoSuchCreatureException, NoSuchItemException, NoSuchWallException
  {
    if (!creature.isTeleporting())
    {
      Item item = null;
      availableActions = null;
      if ((WurmId.getType(subject) == 8) || (WurmId.getType(subject) == 18) || (WurmId.getType(subject) == 32)) {
        subject = -1L;
      }
      int targetType = WurmId.getType(target);
      Behaviour behaviour = Action.getBehaviour(target, creature.isOnSurface());
      boolean onSurface = Action.getIsOnSurface(target, creature.isOnSurface());
      if ((subject != -1L) && (
        (WurmId.getType(subject) == 2) || (WurmId.getType(subject) == 6) || 
        (WurmId.getType(subject) == 19) || (WurmId.getType(subject) == 20))) {
        try
        {
          item = Items.getItem(subject);
        }
        catch (NoSuchItemException nsi)
        {
          subject = -10L;
          item = null;
        }
      }
      if (targetType == 3)
      {
        RequestParam param = requestActionForTiles(creature, target, onSurface, item, behaviour);
        param.filterForSelectBar();
        sendRequestResponse(requestId, comm, param, true);
      }
      else if ((targetType == 1) || (targetType == 0))
      {
        RequestParam param = requestActionForCreaturesPlayers(creature, target, item, targetType, behaviour);
        param.filterForSelectBar();
        sendRequestResponse(requestId, comm, param, true);
      }
      else if ((targetType == 2) || (targetType == 6) || (targetType == 19) || (targetType == 20))
      {
        RequestParam param = requestActionForItemsBodyIdsCoinIds(creature, target, item, behaviour);
        param.filterForSelectBar();
        sendRequestResponse(requestId, comm, param, true);
      }
      else if (targetType == 5)
      {
        RequestParam param = requestActionForWalls(creature, target, item, behaviour);
        param.filterForSelectBar();
        sendRequestResponse(requestId, comm, param, true);
      }
      else if (targetType == 7)
      {
        RequestParam param = requestActionForFences(creature, target, item, behaviour);
        param.filterForSelectBar();
        sendRequestResponse(requestId, comm, param, true);
      }
      else if (targetType == 12)
      {
        RequestParam param = requestActionForTileBorder(creature, target, item, behaviour);
        param.filterForSelectBar();
        sendRequestResponse(requestId, comm, param, true);
      }
      else if (targetType == 17)
      {
        RequestParam param = requestActionForCaveTiles(creature, target, item, behaviour);
        param.filterForSelectBar();
        sendRequestResponse(requestId, comm, param, true);
      }
      else if (targetType == 23)
      {
        RequestParam param = requestActionForFloors(creature, target, onSurface, item, behaviour);
        param.filterForSelectBar();
        sendRequestResponse(requestId, comm, param, true);
      }
      else if (targetType == 24)
      {
        RequestParam param = requestActionForIllusions(creature, target, item, targetType, behaviour);
        param.filterForSelectBar();
        sendRequestResponse(requestId, comm, param, true);
      }
      else
      {
        RequestParam param = new RequestParam(new LinkedList(), "");
        sendRequestResponse(requestId, comm, param, true);
      }
    }
    else
    {
      comm.sendAlertServerMessage("You are teleporting and cannot perform actions right now.");
    }
  }
  
  public static void requestActions(Creature creature, Communicator comm, byte requestId, long subject, long target)
    throws NoSuchPlayerException, NoSuchCreatureException, NoSuchItemException, NoSuchBehaviourException, NoSuchWallException
  {
    if (!creature.isTeleporting())
    {
      Item item = null;
      availableActions = null;
      if ((WurmId.getType(subject) == 8) || (WurmId.getType(subject) == 18) || (WurmId.getType(subject) == 32)) {
        subject = -1L;
      }
      int targetType = WurmId.getType(target);
      Behaviour behaviour = Action.getBehaviour(target, creature.isOnSurface());
      
      boolean onSurface = Action.getIsOnSurface(target, creature.isOnSurface());
      if ((subject != -1L) && (
        (WurmId.getType(subject) == 2) || (WurmId.getType(subject) == 6) || 
        (WurmId.getType(subject) == 19) || (WurmId.getType(subject) == 20))) {
        try
        {
          item = Items.getItem(subject);
        }
        catch (NoSuchItemException nsi)
        {
          subject = -10L;
          item = null;
        }
      }
      if (targetType == 3)
      {
        RequestParam param = requestActionForTiles(creature, target, onSurface, item, behaviour);
        sendRequestResponse(requestId, comm, param, false);
      }
      else if ((targetType == 1) || (targetType == 0))
      {
        RequestParam param = requestActionForCreaturesPlayers(creature, target, item, targetType, behaviour);
        sendRequestResponse(requestId, comm, param, false);
      }
      else if ((targetType == 2) || (targetType == 6) || (targetType == 19) || (targetType == 20))
      {
        RequestParam param = requestActionForItemsBodyIdsCoinIds(creature, target, item, behaviour);
        sendRequestResponse(requestId, comm, param, false);
      }
      else if (targetType == 5)
      {
        RequestParam param = requestActionForWalls(creature, target, item, behaviour);
        sendRequestResponse(requestId, comm, param, false);
      }
      else if (targetType == 7)
      {
        RequestParam param = requestActionForFences(creature, target, item, behaviour);
        sendRequestResponse(requestId, comm, param, false);
      }
      else if ((targetType == 8) || (targetType == 32))
      {
        requestActionForWounds(creature, comm, requestId, target, item, behaviour);
      }
      else if (targetType == 12)
      {
        RequestParam param = requestActionForTileBorder(creature, target, item, behaviour);
        sendRequestResponse(requestId, comm, param, false);
      }
      else if (targetType == 14)
      {
        requestActionForPlanets(creature, comm, requestId, target, item, behaviour);
      }
      else if (targetType == 30)
      {
        requestActionForMenu(creature, comm, requestId, target, behaviour);
      }
      else if (targetType == 17)
      {
        RequestParam param = requestActionForCaveTiles(creature, target, item, behaviour);
        sendRequestResponse(requestId, comm, param, false);
      }
      else if (targetType == 18)
      {
        requestActionForSkillIds(comm, requestId, target);
      }
      else if (targetType == 23)
      {
        RequestParam param = requestActionForFloors(creature, target, onSurface, item, behaviour);
        sendRequestResponse(requestId, comm, param, false);
      }
      else if (targetType == 22)
      {
        requestActionForMissionPerformed(creature, comm, requestId, target, behaviour);
      }
      else if (targetType == 24)
      {
        RequestParam param = requestActionForIllusions(creature, target, item, targetType, behaviour);
        sendRequestResponse(requestId, comm, param, false);
      }
      else if (targetType == 27)
      {
        RequestParam param = requestActionForTileCorner(creature, target, onSurface, item, behaviour);
        sendRequestResponse(requestId, comm, param, false);
      }
      else if (targetType == 28)
      {
        requestActionForBridgeParts(creature, comm, requestId, target, onSurface, item, behaviour);
      }
      else if (targetType == 25)
      {
        requestActionForTickets(creature, comm, requestId, target, behaviour);
      }
    }
    else
    {
      comm.sendAlertServerMessage("You are teleporting and cannot perform actions right now.");
    }
  }
  
  private static void sendRequestResponse(byte requestId, Communicator comm, RequestParam response, boolean sendToSelectBar)
  {
    if (!sendToSelectBar) {
      comm.sendAvailableActions(requestId, response.getAvailableActions(), response.getHelpString());
    } else {
      comm.sendAvailableSelectBarActions(requestId, response.getAvailableActions());
    }
  }
  
  public static final RequestParam requestActionForTiles(Creature creature, long target, boolean onSurface, Item item, Behaviour behaviour)
  {
    int x = Tiles.decodeTileX(target);
    int y = Tiles.decodeTileY(target);
    
    int tile = Server.surfaceMesh.getTile(x, y);
    if (item == null) {
      availableActions = behaviour.getBehavioursFor(creature, x, y, onSurface, tile);
    } else {
      availableActions = behaviour.getBehavioursFor(creature, item, x, y, onSurface, tile);
    }
    byte type = Tiles.decodeType(tile);
    Tiles.Tile t = Tiles.getTile(type);
    
    return new RequestParam(availableActions, t.tiledesc.replaceAll(" ", "_"));
  }
  
  private static final RequestParam requestActionForTileCorner(Creature creature, long target, boolean onSurface, Item item, Behaviour behaviour)
  {
    int x = Tiles.decodeTileX(target);
    int y = Tiles.decodeTileY(target);
    int heightOffset = Tiles.decodeHeightOffset(target);
    int tile = Server.surfaceMesh.getTile(x, y);
    if (item == null) {
      availableActions = behaviour.getBehavioursFor(creature, x, y, onSurface, true, tile, heightOffset);
    } else {
      availableActions = behaviour.getBehavioursFor(creature, item, x, y, onSurface, true, tile, heightOffset);
    }
    byte type = Tiles.decodeType(tile);
    Tiles.Tile t = Tiles.getTile(type);
    
    return new RequestParam(availableActions, t.tiledesc.replaceAll(" ", "_"));
  }
  
  public static final RequestParam requestActionForCreaturesPlayers(Creature creature, long target, Item item, int targetType, Behaviour behaviour)
    throws NoSuchPlayerException, NoSuchCreatureException
  {
    Creature targetc = Server.getInstance().getCreature(target);
    if (targetc.getTemplateId() == 119) {
      return new RequestParam(new ArrayList(), "Fishing");
    }
    if (item == null) {
      availableActions = behaviour.getBehavioursFor(creature, targetc);
    } else {
      availableActions = behaviour.getBehavioursFor(creature, item, targetc);
    }
    if (targetType == 1) {
      return new RequestParam(availableActions, targetc.getTemplate().getName().replaceAll(" ", "_"));
    }
    return new RequestParam(availableActions, "Player:" + targetc.getName().replaceAll(" ", "_"));
  }
  
  public static final RequestParam requestActionForItemsBodyIdsCoinIds(Creature creature, long target, Item item, Behaviour behaviour)
    throws NoSuchItemException
  {
    Item targetItem = Items.getItem(target);
    long ownerId = targetItem.getOwnerId();
    if ((ownerId == -10L) || (ownerId == creature.getWurmId()) || (targetItem.isTraded()))
    {
      if (item == null) {
        availableActions = behaviour.getBehavioursFor(creature, targetItem);
      } else {
        availableActions = behaviour.getBehavioursFor(creature, item, targetItem);
      }
      if ((targetItem.isKingdomMarker()) && (targetItem.isNoTake())) {
        return new RequestParam(availableActions, targetItem.getTemplate().getName().replaceAll(" ", "_"));
      }
      String name = "";
      if ((targetItem.getTemplate().sizeString != null) && (!targetItem.getTemplate().sizeString.isEmpty())) {
        name = StringUtil.format("%s%s", new Object[] { targetItem.getTemplate().sizeString, targetItem.getTemplate().getName() }).replaceAll(" ", "_");
      } else {
        name = targetItem.getTemplate().getName().replaceAll(" ", "_");
      }
      return new RequestParam(availableActions, name);
    }
    if (ownerId != -10L)
    {
      availableActions = new LinkedList();
      availableActions.addAll(Actions.getDefaultItemActions());
      if ((targetItem.isKingdomMarker()) && (targetItem.isNoTake())) {
        return new RequestParam(availableActions, targetItem.getTemplate().getName().replaceAll(" ", "_"));
      }
      String name = "";
      if (targetItem.getTemplate().sizeString.length() > 0) {
        name = StringUtil.format("%s%s", new Object[] { targetItem.getTemplate().sizeString, targetItem.getTemplate().getName() }).replaceAll(" ", "_");
      } else {
        name = targetItem.getTemplate().getName().replaceAll(" ", "_");
      }
      return new RequestParam(availableActions, name);
    }
    return new RequestParam(new LinkedList(), "");
  }
  
  private static final RequestParam requestActionForWalls(Creature creature, long target, Item item, Behaviour behaviour)
    throws NoSuchWallException
  {
    int x = Tiles.decodeTileX(target);
    int y = Tiles.decodeTileY(target);
    
    boolean onSurface = Tiles.decodeLayer(target) == 0;
    Wall wall = null;
    for (int xx = 1; xx >= -1; xx--) {
      for (int yy = 1; yy >= -1; yy--) {
        try
        {
          Zone zone = Zones.getZone(x + xx, y + yy, onSurface);
          VolaTile tile = zone.getTileOrNull(x + xx, y + yy);
          if (tile != null)
          {
            Wall[] walls = tile.getWalls();
            for (int s = 0; s < walls.length; s++) {
              if (walls[s].getId() == target)
              {
                wall = walls[s];
                break;
              }
            }
          }
        }
        catch (NoSuchZoneException localNoSuchZoneException) {}
      }
    }
    if (wall == null) {
      throw new NoSuchWallException("No wall with id " + target);
    }
    if (item == null) {
      availableActions = behaviour.getBehavioursFor(creature, wall);
    } else {
      availableActions = behaviour.getBehavioursFor(creature, item, wall);
    }
    return new RequestParam(availableActions, wall.getIdName());
  }
  
  private static final RequestParam requestActionForFences(Creature creature, long target, Item item, Behaviour behaviour)
  {
    int x = Tiles.decodeTileX(target);
    int y = Tiles.decodeTileY(target);
    boolean onSurface = Tiles.decodeLayer(target) == 0;
    Fence fence = null;
    VolaTile tile = Zones.getTileOrNull(x, y, onSurface);
    if (tile != null) {
      fence = tile.getFence(target);
    }
    if (fence == null)
    {
      logger.log(Level.WARNING, "Checking for fence with id " + target + " in other tiles. ");
      for (int tx = x - 1; tx <= x + 1; tx++) {
        for (int ty = y - 1; ty <= y + 1; ty++)
        {
          tile = Zones.getTileOrNull(tx, ty, onSurface);
          if (tile != null)
          {
            fence = tile.getFence(target);
            if (fence != null) {
              try
              {
                Zone zone = Zones.getZone(tx, ty, true);
                logger.log(Level.INFO, "Found fence in zone " + zone.getId() + " fence has id " + fence
                  .getId() + " and tilex=" + fence.getTileX() + ", tiley=" + fence
                  .getTileY() + " dir=" + fence.getDir());
                Zone correctZone = Zones.getZone(x, y, true);
                logger.log(Level.INFO, "We looked for it in zone " + correctZone.getId());
                if (!zone.equals(correctZone))
                {
                  logger.log(Level.INFO, "Correcting the mistake.");
                  zone.removeFence(fence);
                  fence.setZoneId(correctZone.getId());
                  correctZone.addFence(fence);
                  tile.broadCast("The server tried to remedy a fence problem here. Please report if anything happened.");
                }
              }
              catch (NoSuchZoneException nsz)
              {
                logger.log(Level.WARNING, "Weird: " + nsz.getMessage(), nsz);
              }
            }
          }
        }
      }
    }
    if (fence != null)
    {
      if (item == null) {
        availableActions = behaviour.getBehavioursFor(creature, fence);
      } else {
        availableActions = behaviour.getBehavioursFor(creature, item, fence);
      }
      return new RequestParam(availableActions, fence.getName().replaceAll(" ", "_"));
    }
    logger.log(Level.WARNING, "Failed to locate fence with id " + target + ".");
    return new RequestParam(new LinkedList(), "");
  }
  
  private static void requestActionForWounds(Creature creature, Communicator comm, byte requestId, long target, Item item, Behaviour behaviour)
  {
    try
    {
      boolean found = false;
      Wounds wounds = creature.getBody().getWounds();
      if (wounds != null)
      {
        Wound wound = wounds.getWound(target);
        if (wound != null)
        {
          found = true;
          if (item == null) {
            availableActions = behaviour.getBehavioursFor(creature, wound);
          } else {
            availableActions = behaviour.getBehavioursFor(creature, item, wound);
          }
          comm.sendAvailableActions(requestId, availableActions, wound.getDescription().replaceAll(", bandaged", "").replaceAll(" ", "_"));
        }
      }
      if (!found)
      {
        Wound wound = Wounds.getAnyWound(target);
        if (wound != null)
        {
          if (item == null) {
            availableActions = behaviour.getBehavioursFor(creature, wound);
          } else {
            availableActions = behaviour.getBehavioursFor(creature, item, wound);
          }
          comm.sendAvailableActions(requestId, availableActions, wound.getDescription().replaceAll(", bandaged", "").replaceAll(" ", "_"));
        }
      }
    }
    catch (Exception ex)
    {
      if (logger.isLoggable(Level.FINE)) {
        logger.log(Level.FINE, ex.getMessage(), ex);
      }
    }
  }
  
  private static final RequestParam requestActionForTileBorder(Creature creature, long target, Item item, Behaviour behaviour)
  {
    int x = Tiles.decodeTileX(target);
    int y = Tiles.decodeTileY(target);
    int heightOffset = Tiles.decodeHeightOffset(target);
    Tiles.TileBorderDirection dir = Tiles.decodeDirection(target);
    boolean onSurface = Tiles.decodeLayer(target) == 0;
    if (MethodsStructure.doesTileBorderContainWallOrFence(x, y, heightOffset, dir, onSurface, true)) {
      availableActions = behaviour.getBehavioursFor(creature, x, y, onSurface, dir, true, heightOffset);
    } else if (item != null) {
      availableActions = behaviour.getBehavioursFor(creature, item, x, y, onSurface, dir, true, heightOffset);
    } else {
      availableActions = behaviour.getBehavioursFor(creature, x, y, onSurface, dir, true, heightOffset);
    }
    return new RequestParam(availableActions, "Tile_Border");
  }
  
  private static void requestActionForPlanets(Creature creature, Communicator comm, byte requestId, long target, Item item, Behaviour behaviour)
  {
    int planetId = (int)(target >> 16) & 0xFFFF;
    if (item == null) {
      availableActions = behaviour.getBehavioursFor(creature, planetId);
    } else {
      availableActions = behaviour.getBehavioursFor(creature, item, planetId);
    }
    comm.sendAvailableActions(requestId, availableActions, PlanetBehaviour.getName(planetId));
  }
  
  private static void requestActionForMenu(Creature creature, Communicator comm, byte requestId, long target, Behaviour behaviour)
  {
    int planetId = (int)(target >> 16) & 0xFFFF;
    availableActions = behaviour.getBehavioursFor(creature, planetId);
    comm.sendAvailableActions(requestId, availableActions, "");
  }
  
  private static final RequestParam requestActionForCaveTiles(Creature creature, long target, Item item, Behaviour behaviour)
  {
    int x = Tiles.decodeTileX(target);
    int y = Tiles.decodeTileY(target);
    int dir = CaveTile.decodeCaveTileDir(target);
    
    int tile = Server.caveMesh.getTile(x, y);
    if (item == null) {
      availableActions = behaviour.getBehavioursFor(creature, x, y, false, tile, dir);
    } else {
      availableActions = behaviour.getBehavioursFor(creature, item, x, y, false, tile, dir);
    }
    return new RequestParam(availableActions, Tiles.getTile(Tiles.decodeType(tile)).tiledesc.replaceAll(" ", "_"));
  }
  
  private static void requestActionForSkillIds(Communicator comm, byte requestId, long target)
  {
    int skillid = (int)(target >> 32) & 0xFFFFFFFF;
    
    String name = "unknown";
    if (skillid == 2147483644)
    {
      comm.sendAvailableActions(requestId, emptyActions, "Favor");
    }
    else if (skillid == 2147483645)
    {
      comm.sendAvailableActions(requestId, emptyActions, "Faith");
    }
    else if (skillid == 2147483642)
    {
      comm.sendAvailableActions(requestId, emptyActions, "Alignment");
    }
    else if (skillid == 2147483643)
    {
      comm.sendAvailableActions(requestId, emptyActions, "Religion");
    }
    else if (skillid == Integer.MAX_VALUE)
    {
      comm.sendAvailableActions(requestId, emptyActions, "Skills");
    }
    else if (skillid == 2147483646)
    {
      comm.sendAvailableActions(requestId, emptyActions, "Characteristics");
    }
    else
    {
      name = SkillSystem.getNameFor(skillid);
      comm.sendAvailableActions(requestId, emptyActions, name.replaceAll(" ", "_"));
    }
  }
  
  private static final RequestParam requestActionForFloors(Creature creature, long target, boolean onSurface, Item item, Behaviour behaviour)
  {
    int x = Tiles.decodeTileX(target);
    int y = Tiles.decodeTileY(target);
    int heightOffset = Tiles.decodeHeightOffset(target);
    
    String fString = "unknown";
    
    Floor[] floors = Zones.getFloorsAtTile(x, y, heightOffset, heightOffset, onSurface ? 0 : -1);
    if (floors == null)
    {
      logger.log(Level.WARNING, "No such floor " + target + " (" + x + "," + y + " heightOffset=" + heightOffset + ")");
      
      return new RequestParam(new LinkedList(), "");
    }
    if (floors.length > 1) {
      logger.log(Level.WARNING, "Found more than 1 floor at " + x + "," + y + " heightOffset" + heightOffset);
    }
    Floor floor = floors[0];
    fString = floor.getName();
    if (item == null) {
      availableActions = behaviour.getBehavioursFor(creature, onSurface, floor);
    } else {
      availableActions = behaviour.getBehavioursFor(creature, item, onSurface, floor);
    }
    creature.sendToLoggers("Requesting floor " + floor.getId() + " target requested=" + target + " " + floor
      .getHeightOffset());
    
    return new RequestParam(availableActions, fString);
  }
  
  private static void requestActionForBridgeParts(Creature creature, Communicator comm, byte requestId, long target, boolean onSurface, Item item, Behaviour behaviour)
  {
    int x = Tiles.decodeTileX(target);
    int y = Tiles.decodeTileY(target);
    int ht = Tiles.decodeHeight(Server.surfaceMesh.getTile(x, y));
    int heightOffset = Tiles.decodeHeightOffset(target) - ht;
    String fString = "unknown";
    
    BridgePart[] bridgeParts = Zones.getBridgePartsAtTile(x, y, onSurface);
    if (bridgeParts == null)
    {
      logger.log(Level.WARNING, "No such Bridge Part " + target + " (" + x + "," + y + " heightOffset=" + heightOffset + ")");
    }
    else
    {
      if (bridgeParts.length > 1) {
        logger.log(Level.WARNING, "Found more than 1 bridge part at " + x + "," + y + " heightOffset" + heightOffset);
      }
      BridgePart bridgePart = bridgeParts[0];
      fString = bridgePart.getName();
      if (item == null) {
        availableActions = behaviour.getBehavioursFor(creature, onSurface, bridgePart);
      } else {
        availableActions = behaviour.getBehavioursFor(creature, item, onSurface, bridgePart);
      }
      creature.sendToLoggers("Requesting bridge part " + bridgePart.getId() + " target requested=" + target + " " + bridgePart
        .getHeightOffset());
      comm.sendAvailableActions(requestId, availableActions, fString);
    }
  }
  
  private static void requestActionForMissionPerformed(Creature creature, Communicator comm, byte requestId, long target, Behaviour behaviour)
  {
    int missionId = MissionPerformed.decodeMissionId(target);
    Mission m = Missions.getMissionWithId(missionId);
    String mString = "unknown";
    if (m != null) {
      mString = m.getName();
    }
    comm.sendAvailableActions(requestId, behaviour.getBehavioursFor(creature, missionId), "Mission:" + mString);
  }
  
  private static final RequestParam requestActionForIllusions(Creature creature, long target, Item item, int targetType, Behaviour behaviour)
    throws NoSuchPlayerException, NoSuchCreatureException
  {
    long wid = Creature.getWurmIdForIllusion(target);
    return requestActionForCreaturesPlayers(creature, wid, item, targetType, behaviour);
  }
  
  private static void requestActionForTickets(Creature creature, Communicator comm, byte requestId, long target, Behaviour behaviour)
  {
    int ticketId = Tickets.decodeTicketId(target);
    comm.sendAvailableActions(requestId, behaviour.getBehavioursFor(creature, ticketId), "Ticket:" + ticketId);
  }
  
  public static void action(Creature creature, Communicator comm, long subject, long target, short action)
    throws NoSuchPlayerException, NoSuchCreatureException, NoSuchItemException, NoSuchBehaviourException, NoSuchWallException, FailedException
  {
    String s = "unknown";
    try
    {
      s = Actions.getVerbForAction(action);
    }
    catch (Exception e)
    {
      s = "" + action;
    }
    if (creature.isUndead()) {
      if ((action != 326) && (action != 1) && (!Action.isActionAttack(action)) && 
        (!Action.isStanceChange(action)) && (action != 523) && (action != 522))
      {
        creature.getCommunicator().sendNormalServerMessage("Unnn..");
        return;
      }
    }
    creature.sendToLoggers("Received action number " + s + ", target " + target + ", source " + subject + ", action " + action, (byte)2);
    if (creature.isFrozen())
    {
      creature.sendToLoggers("Frozen. Ignoring.", (byte)2);
      throw new FailedException("Frozen");
    }
    if (creature.isTeleporting())
    {
      comm.sendAlertServerMessage("You are teleporting and cannot perform actions right now.");
      throw new FailedException("Teleporting");
    }
    if (action == 149)
    {
      try
      {
        if ((creature.getCurrentAction().isSpell()) || (!creature.getCurrentAction().isOffensive()) || 
          (!creature.isFighting())) {
          creature.stopCurrentAction();
        }
      }
      catch (NoSuchActionException localNoSuchActionException) {}
    }
    else
    {
      float x = creature.getStatus().getPositionX();
      float y = creature.getStatus().getPositionY();
      float z = creature.getStatus().getPositionZ() + creature.getAltOffZ();
      
      Action toSet = new Action(creature, subject, target, action, x, y, z, creature.getStatus().getRotation());
      if (toSet.isQuick())
      {
        toSet.poll();
      }
      else if ((toSet.isStanceChange()) && (toSet.getNumber() != 340))
      {
        if (!toSet.poll()) {
          creature.setAction(toSet);
        }
      }
      else
      {
        toSet.setRarity(creature.getRarity());
        creature.setAction(toSet);
      }
    }
  }
  
  public static void action(Creature creature, Communicator comm, long subject, long[] targets, short action)
    throws FailedException, NoSuchPlayerException, NoSuchCreatureException, NoSuchItemException, NoSuchBehaviourException
  {
    String s = "unknown";
    try
    {
      s = Actions.getVerbForAction(action);
    }
    catch (Exception e)
    {
      s = "" + action;
    }
    if (creature.isUndead())
    {
      creature.getCommunicator().sendNormalServerMessage("Unnn..");
      return;
    }
    String tgts = "";
    for (int x = 0; x < targets.length; x++)
    {
      if (tgts.length() > 0) {
        tgts = tgts + ", ";
      }
      tgts = tgts + targets[x];
    }
    creature.sendToLoggers("Received action number " + s + ", target " + tgts, (byte)2);
    if (creature.isFrozen())
    {
      creature.sendToLoggers("Frozen. Ignoring.", (byte)2);
      throw new FailedException("Frozen");
    }
    if (creature.isTeleporting())
    {
      comm.sendAlertServerMessage("You are teleporting and cannot perform actions right now.");
      throw new FailedException("Teleporting");
    }
    float x = creature.getStatus().getPositionX();
    float y = creature.getStatus().getPositionY();
    float z = creature.getStatus().getPositionZ() + creature.getAltOffZ();
    
    Action toSet = new Action(creature, subject, targets, action, x, y, z, creature.getStatus().getRotation());
    if (toSet.isQuick())
    {
      toSet.poll();
    }
    else
    {
      toSet.setRarity(creature.getRarity());
      creature.setAction(toSet);
    }
  }
  
  public static class RequestParam
  {
    private final String helpString;
    private List<ActionEntry> availableActions;
    
    public RequestParam(List<ActionEntry> actions, String help)
    {
      this.availableActions = actions;
      this.helpString = help;
    }
    
    public final List<ActionEntry> getAvailableActions()
    {
      return this.availableActions;
    }
    
    public final String getHelpString()
    {
      return this.helpString;
    }
    
    public void filterForSelectBar()
    {
      for (int i = this.availableActions.size() - 1; i >= 0; i--)
      {
        ActionEntry entry = (ActionEntry)this.availableActions.get(i);
        if (!entry.isShowOnSelectBar()) {
          this.availableActions.remove(i);
        }
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\behaviours\BehaviourDispatcher.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */