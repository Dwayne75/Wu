package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.RoofFloorEnum;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.StructureConstants.FloorMaterial;
import com.wurmonline.shared.constants.StructureConstants.FloorState;
import com.wurmonline.shared.constants.StructureConstants.FloorType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FloorBehaviour
  extends TileBehaviour
{
  FloorBehaviour()
  {
    super((short)45);
  }
  
  private static final Logger logger = Logger.getLogger(FloorBehaviour.class.getName());
  
  public List<ActionEntry> getBehavioursFor(Creature performer, boolean onSurface, Floor floor)
  {
    List<ActionEntry> toReturn = new LinkedList();
    if (!floor.isFinished()) {
      toReturn.add(Actions.actionEntrys['ɟ']);
    }
    toReturn.addAll(Actions.getDefaultTileActions());
    
    toReturn.addAll(super.getTileAndFloorBehavioursFor(performer, null, floor.getTileX(), floor.getTileY(), Tiles.Tile.TILE_DIRT.id));
    if (floor.getType() == StructureConstants.FloorType.OPENING) {
      if (floor.isFinished())
      {
        if (floor.getFloorLevel() == performer.getFloorLevel()) {
          toReturn.add(Actions.actionEntrys['ȋ']);
        } else if (floor.getFloorLevel() == performer.getFloorLevel() + 1) {
          toReturn.add(Actions.actionEntrys['Ȋ']);
        }
      }
      else if (floor.getFloorLevel() == performer.getFloorLevel()) {
        toReturn.add(Actions.actionEntrys['ȋ']);
      }
    }
    VolaTile floorTile = Zones.getOrCreateTile(floor.getTileX(), floor.getTileY(), floor.getLayer() >= 0);
    Structure structure = null;
    try
    {
      structure = Structures.getStructure(floor.getStructureId());
    }
    catch (NoSuchStructureException e)
    {
      logger.log(Level.WARNING, e.getMessage(), e);
      return toReturn;
    }
    if (MethodsStructure.mayModifyStructure(performer, structure, floorTile, (short)177))
    {
      toReturn.add(new ActionEntry((short)-2, "Rotate", "rotating"));
      toReturn.add(new ActionEntry((short)177, "Turn clockwise", "turning"));
      toReturn.add(new ActionEntry((short)178, "Turn counterclockwise", "turning"));
    }
    return toReturn;
  }
  
  public static final List<ActionEntry> getCompletedFloorsBehaviour(boolean andStaircases, boolean onSurface)
  {
    List<ActionEntry> plantypes = new ArrayList();
    plantypes.add(Actions.actionEntrys['Ǽ']);
    plantypes.add(Actions.actionEntrys['ȃ']);
    if (andStaircases)
    {
      plantypes.add(Actions.actionEntrys['ʓ']);
      plantypes.add(Actions.actionEntrys['ˀ']);
      plantypes.add(Actions.actionEntrys['ˉ']);
      plantypes.add(Actions.actionEntrys['ˊ']);
      plantypes.add(Actions.actionEntrys['ˋ']);
      plantypes.add(Actions.actionEntrys['ˁ']);
      plantypes.add(Actions.actionEntrys['˂']);
      plantypes.add(Actions.actionEntrys['˅']);
      plantypes.add(Actions.actionEntrys['ˆ']);
      plantypes.add(Actions.actionEntrys['ˇ']);
      plantypes.add(Actions.actionEntrys['ˈ']);
    }
    plantypes.add(Actions.actionEntrys['ǽ']);
    if (onSurface) {
      plantypes.add(Actions.actionEntrys['ǻ']);
    }
    Collections.sort(plantypes);
    
    List<ActionEntry> toReturn = new ArrayList(5);
    toReturn.add(new ActionEntry((short)-plantypes.size(), "Plan", "planning"));
    toReturn.addAll(plantypes);
    return toReturn;
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item source, boolean onSurface, Floor floor)
  {
    List<ActionEntry> toReturn = new LinkedList();
    if (!floor.isFinished()) {
      toReturn.add(Actions.actionEntrys['ɟ']);
    }
    toReturn.addAll(super.getTileAndFloorBehavioursFor(performer, source, floor.getTileX(), floor.getTileY(), Tiles.Tile.TILE_DIRT.id));
    if (floor.getType() == StructureConstants.FloorType.OPENING) {
      if (floor.isFinished())
      {
        if (floor.getFloorLevel() == performer.getFloorLevel()) {
          toReturn.add(Actions.actionEntrys['ȋ']);
        } else if (floor.getFloorLevel() == performer.getFloorLevel() + 1) {
          toReturn.add(Actions.actionEntrys['Ȋ']);
        }
      }
      else if (floor.getFloorLevel() == performer.getFloorLevel()) {
        toReturn.add(Actions.actionEntrys['ȋ']);
      }
    }
    VolaTile floorTile = Zones.getOrCreateTile(floor.getTileX(), floor.getTileY(), floor.isOnSurface());
    Structure structure = null;
    try
    {
      structure = Structures.getStructure(floor.getStructureId());
    }
    catch (NoSuchStructureException e)
    {
      logger.log(Level.WARNING, e.getMessage(), e);
      toReturn.addAll(Actions.getDefaultItemActions());
      return toReturn;
    }
    if (MethodsStructure.mayModifyStructure(performer, structure, floorTile, (short)169))
    {
      switch (floor.getFloorState())
      {
      case BUILDING: 
        toReturn.add(new ActionEntry((short)169, "Continue building", "building"));
        break;
      case PLANNING: 
        if (floor.getType() == StructureConstants.FloorType.ROOF)
        {
          List<RoofFloorEnum> list = RoofFloorEnum.getRoofsByTool(source);
          if (list.size() > 0)
          {
            toReturn.add(new ActionEntry((short)-list.size(), "Build", "building"));
            for (RoofFloorEnum en : list) {
              toReturn.add(en.createActionEntry());
            }
          }
        }
        else
        {
          List<RoofFloorEnum> list = RoofFloorEnum.getFloorByToolAndType(source, floor.getType());
          if (list.size() > 0)
          {
            toReturn.add(new ActionEntry((short)-list.size(), "Build", "building"));
            for (RoofFloorEnum en : list) {
              toReturn.add(en.createActionEntry());
            }
          }
        }
        break;
      case COMPLETED: 
        if (floor.getType() != StructureConstants.FloorType.ROOF) {
          toReturn.addAll(getCompletedFloorsBehaviour(true, floor.isOnSurface()));
        }
        break;
      }
      toReturn.add(new ActionEntry((short)-2, "Rotate", "rotating"));
      toReturn.add(new ActionEntry((short)177, "Turn clockwise", "turning"));
      toReturn.add(new ActionEntry((short)178, "Turn counterclockwise", "turning"));
    }
    if (!source.isTraded())
    {
      if (source.getTemplateId() == floor.getRepairItemTemplate()) {
        if (floor.getDamage() > 0.0F)
        {
          if (((!Servers.localServer.challengeServer) || (performer.getEnemyPresense() <= 0)) && 
            (!floor.isNoRepair())) {
            toReturn.add(Actions.actionEntrys['Á']);
          }
        }
        else if (floor.getQualityLevel() < 100.0F) {
          if (!floor.isNoImprove()) {
            toReturn.add(Actions.actionEntrys['À']);
          }
        }
      }
      toReturn.addAll(Actions.getDefaultItemActions());
      if (!floor.isIndestructible()) {
        if (floor.getType() == StructureConstants.FloorType.ROOF) {
          toReturn.add(Actions.actionEntrys['ȍ']);
        } else if (!MethodsHighways.onHighway(floor)) {
          toReturn.add(Actions.actionEntrys['Ȍ']);
        }
      }
      if (((source.getTemplateId() == 315) || (source.getTemplateId() == 176)) && 
        (performer.getPower() >= 2)) {
        toReturn.add(Actions.actionEntrys['ʬ']);
      }
    }
    return toReturn;
  }
  
  private boolean buildAction(Action act, Creature performer, Item source, Floor floor, short action, float counter)
  {
    switch (floor.getFloorState())
    {
    case PLANNING: 
      boolean autoAdvance = (performer.getPower() >= 2) && (source.getTemplateId() == 176);
      Skill craftSkill = null;
      try
      {
        craftSkill = performer.getSkills().getSkill(1005);
      }
      catch (NoSuchSkillException nss)
      {
        craftSkill = performer.getSkills().learn(1005, 1.0F);
      }
      StructureConstants.FloorMaterial newMaterial = StructureConstants.FloorMaterial.fromByte((byte)(action - 20000));
      
      StructureConstants.FloorMaterial oldMaterial = floor.getMaterial();
      floor.setMaterial(newMaterial);
      if (!isOkToBuild(performer, source, floor, floor.getFloorLevel(), floor
        .isRoof()))
      {
        floor.setMaterial(oldMaterial);
        return true;
      }
      if ((!autoAdvance) && (!advanceNextState(performer, floor, act, true)))
      {
        String message = buildRequiredMaterialString(floor, false);
        performer.getCommunicator().sendNormalServerMessage("You need " + message + " to start building that.");
        
        floor.setMaterial(oldMaterial);
        return true;
      }
      floor.setFloorState(StructureConstants.FloorState.BUILDING);
      float oldql = floor.getQualityLevel();
      float qlevel = MethodsStructure.calculateNewQualityLevel(act.getPower(), craftSkill.getKnowledge(0.0D), oldql, 
        getTotalMaterials(floor));
      floor.setQualityLevel(qlevel);
      try
      {
        floor.save();
      }
      catch (IOException e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
      if (floor.getMaterial() == StructureConstants.FloorMaterial.STANDALONE) {
        performer.getCommunicator().sendNormalServerMessage("You plan a " + floor
          .getName() + ".");
      } else if (floor.getType() == StructureConstants.FloorType.ROOF) {
        performer.getCommunicator().sendNormalServerMessage("You plan a " + floor
          .getName() + " made of " + getMaterialDescription(floor) + ".");
      } else {
        performer.getCommunicator().sendNormalServerMessage("You plan a " + floor
          .getName() + " made of " + floor.getMaterial().getName().toLowerCase() + ".");
      }
      return floorBuilding(act, performer, source, floor, action, counter);
    case BUILDING: 
      if (floorBuilding(act, performer, source, floor, action, counter))
      {
        performer.getCommunicator().sendAddFloorRoofToCreationWindow(floor, floor.getId());
        return true;
      }
      return false;
    case COMPLETED: 
      logger.log(Level.WARNING, "FloorBehaviour buildAction on a completed floor, it should not happen?!");
      performer.getCommunicator().sendNormalServerMessage("You failed to find anything to do with that.");
      return true;
    }
    logger.log(Level.WARNING, "Enum value added to FloorState but not to a switch statement in method FloorBehaviour.action()");
    
    return false;
  }
  
  static boolean advanceNextState(Creature performer, Floor floor, Action act, boolean justCheckIfItemsArePresent)
  {
    List<BuildMaterial> mats = getRequiredMaterialsAtState(floor);
    if (takeItemsFromCreature(performer, floor, act, mats, justCheckIfItemsArePresent)) {
      return true;
    }
    if ((performer.getPower() >= 4) && (!justCheckIfItemsArePresent))
    {
      performer.getCommunicator().sendNormalServerMessage("You magically summon some necessary materials.");
      return true;
    }
    return false;
  }
  
  static boolean takeItemsFromCreature(Creature performer, Floor floor, Action act, List<BuildMaterial> mats, boolean justCheckIfItemsArePresent)
  {
    Item[] inventoryItems = performer.getInventory().getAllItems(false);
    Item[] bodyItems = performer.getBody().getAllItems();
    List<Item> takeItemsOnSuccess = new ArrayList();
    Item item;
    for (item : inventoryItems) {
      for (BuildMaterial mat : mats) {
        if (mat.getNeededQuantity() > 0) {
          if ((item.getTemplateId() == mat.getTemplateId()) && (item.getWeightGrams() >= mat.getWeightGrams()))
          {
            takeItemsOnSuccess.add(item);
            mat.setNeededQuantity(0);
            break;
          }
        }
      }
    }
    Item item;
    for (item : bodyItems) {
      for (BuildMaterial mat : mats) {
        if (mat.getNeededQuantity() > 0) {
          if ((item.getTemplateId() == mat.getTemplateId()) && (item.getWeightGrams() >= mat.getWeightGrams()))
          {
            takeItemsOnSuccess.add(item);
            mat.setNeededQuantity(0);
            break;
          }
        }
      }
    }
    float divider = 1.0F;
    for (Iterator localIterator1 = mats.iterator(); localIterator1.hasNext();)
    {
      mat = (BuildMaterial)localIterator1.next();
      
      divider += ((BuildMaterial)mat).getTotalQuantityRequired();
      if (((BuildMaterial)mat).getNeededQuantity() > 0) {
        return false;
      }
    }
    Object mat;
    float qlevel = 0.0F;
    if (!justCheckIfItemsArePresent) {
      for (mat = takeItemsOnSuccess.iterator(); ((Iterator)mat).hasNext();)
      {
        Item item = (Item)((Iterator)mat).next();
        
        act.setPower(item.getCurrentQualityLevel() / divider);
        performer.sendToLoggers("Adding " + item.getCurrentQualityLevel() + ", divider=" + divider + "=" + act
          .getPower());
        qlevel += item.getCurrentQualityLevel() / 21.0F;
        if (item.isCombine()) {
          item.setWeight(item.getWeightGrams() - item.getTemplate().getWeightGrams(), true);
        } else {
          Items.destroyItem(item.getWurmId());
        }
      }
    }
    act.setPower(qlevel);
    return true;
  }
  
  public static int getSkillForRoof(StructureConstants.FloorMaterial material)
  {
    switch (material)
    {
    case WOOD: 
      return 1005;
    case CLAY_BRICK: 
      return 1013;
    case SLATE_SLAB: 
      return 1013;
    case STONE_BRICK: 
      return 1013;
    case SANDSTONE_SLAB: 
      return 1013;
    case STONE_SLAB: 
      return 1013;
    case MARBLE_SLAB: 
      return 1013;
    case THATCH: 
      return 10092;
    case METAL_IRON: 
      return 10015;
    case METAL_COPPER: 
      return 10015;
    case METAL_STEEL: 
      return 10015;
    case METAL_SILVER: 
      return 10015;
    case METAL_GOLD: 
      return 10015;
    case STANDALONE: 
      return 1005;
    }
    return 1005;
  }
  
  public static int getSkillForFloor(StructureConstants.FloorMaterial material)
  {
    switch (material)
    {
    case WOOD: 
      return 1005;
    case CLAY_BRICK: 
      return 10031;
    case SLATE_SLAB: 
      return 10031;
    case STONE_BRICK: 
      return 10031;
    case SANDSTONE_SLAB: 
      return 10031;
    case STONE_SLAB: 
      return 10031;
    case MARBLE_SLAB: 
      return 10031;
    case THATCH: 
      return 10092;
    case METAL_IRON: 
      return 10015;
    case METAL_COPPER: 
      return 10015;
    case METAL_STEEL: 
      return 10015;
    case METAL_SILVER: 
      return 10015;
    case METAL_GOLD: 
      return 10015;
    case STANDALONE: 
      return 1005;
    }
    return 1005;
  }
  
  static byte getFinishedState(Floor floor)
  {
    byte numStates = 0;
    List<BuildMaterial> mats = getRequiredMaterialsFor(floor);
    for (BuildMaterial mat : mats) {
      if (numStates < mat.getTotalQuantityRequired()) {
        numStates = (byte)mat.getTotalQuantityRequired();
      }
    }
    if (numStates <= 0) {
      numStates = 1;
    }
    return numStates;
  }
  
  static byte getTotalMaterials(Floor floor)
  {
    int total = 0;
    List<BuildMaterial> mats = getRequiredMaterialsFor(floor);
    for (BuildMaterial mat : mats)
    {
      int totalReq = mat.getTotalQuantityRequired();
      if (totalReq > total) {
        total = totalReq;
      }
    }
    return (byte)total;
  }
  
  public static final List<BuildMaterial> getRequiredMaterialsForRoof(StructureConstants.FloorMaterial material)
  {
    List<BuildMaterial> toReturn = new ArrayList();
    try
    {
      switch (material)
      {
      case WOOD: 
        toReturn.add(new BuildMaterial(790, 10));
        toReturn.add(new BuildMaterial(218, 2));
        break;
      case STONE_BRICK: 
        toReturn.add(new BuildMaterial(132, 10));
        toReturn.add(new BuildMaterial(492, 10));
        break;
      case CLAY_BRICK: 
        toReturn.add(new BuildMaterial(778, 10));
        toReturn.add(new BuildMaterial(492, 10));
        break;
      case THATCH: 
        toReturn.add(new BuildMaterial(756, 10));
        toReturn.add(new BuildMaterial(444, 10));
        break;
      case SLATE_SLAB: 
        toReturn.add(new BuildMaterial(784, 10));
        toReturn.add(new BuildMaterial(492, 5));
        break;
      case SANDSTONE_SLAB: 
      case STONE_SLAB: 
      case MARBLE_SLAB: 
      default: 
        logger.log(Level.WARNING, "Someone tried to make a roof but the material choice was not supported (" + material
        
          .toString() + ")");
      }
    }
    catch (NoSuchTemplateException nste)
    {
      logger.log(Level.WARNING, "FloorBehaviour.getRequiredMaterialsAtState trying to use material that have a non existing template.", nste);
    }
    return toReturn;
  }
  
  public static List<BuildMaterial> getRequiredMaterialsForFloor(StructureConstants.FloorType type, StructureConstants.FloorMaterial material)
  {
    List<BuildMaterial> toReturn = new ArrayList();
    try
    {
      if (type == StructureConstants.FloorType.OPENING)
      {
        switch (material)
        {
        case WOOD: 
          toReturn.add(new BuildMaterial(22, 5));
          toReturn.add(new BuildMaterial(218, 1));
          break;
        case STONE_BRICK: 
          toReturn.add(new BuildMaterial(132, 5));
          toReturn.add(new BuildMaterial(492, 5));
          break;
        case SANDSTONE_SLAB: 
          toReturn.add(new BuildMaterial(1124, 2));
          toReturn.add(new BuildMaterial(492, 10));
          break;
        case STONE_SLAB: 
          toReturn.add(new BuildMaterial(406, 2));
          toReturn.add(new BuildMaterial(492, 10));
          break;
        case CLAY_BRICK: 
          toReturn.add(new BuildMaterial(776, 5));
          toReturn.add(new BuildMaterial(492, 5));
          break;
        case SLATE_SLAB: 
          toReturn.add(new BuildMaterial(771, 2));
          toReturn.add(new BuildMaterial(492, 10));
          break;
        case MARBLE_SLAB: 
          toReturn.add(new BuildMaterial(787, 2));
          toReturn.add(new BuildMaterial(492, 10));
          break;
        case STANDALONE: 
          toReturn.add(new BuildMaterial(23, 2));
          toReturn.add(new BuildMaterial(218, 1));
          break;
        case THATCH: 
        case METAL_IRON: 
        case METAL_COPPER: 
        case METAL_STEEL: 
        case METAL_SILVER: 
        case METAL_GOLD: 
        default: 
          logger.log(Level.WARNING, "Someone tried to make a floor with an opening but the material choice was not supported (" + material
          
            .toString() + ")");
          break;
        }
      }
      else if (type == StructureConstants.FloorType.WIDE_STAIRCASE)
      {
        toReturn.add(new BuildMaterial(22, 30));
        toReturn.add(new BuildMaterial(217, 2));
      }
      else if ((type == StructureConstants.FloorType.WIDE_STAIRCASE_RIGHT) || (type == StructureConstants.FloorType.WIDE_STAIRCASE_LEFT))
      {
        toReturn.add(new BuildMaterial(22, 30));
        toReturn.add(new BuildMaterial(23, 5));
        toReturn.add(new BuildMaterial(218, 1));
        toReturn.add(new BuildMaterial(217, 2));
      }
      else if (type == StructureConstants.FloorType.WIDE_STAIRCASE_BOTH)
      {
        toReturn.add(new BuildMaterial(22, 30));
        toReturn.add(new BuildMaterial(23, 10));
        toReturn.add(new BuildMaterial(218, 1));
        toReturn.add(new BuildMaterial(217, 2));
      }
      else
      {
        if ((type == StructureConstants.FloorType.STAIRCASE) || (type == StructureConstants.FloorType.RIGHT_STAIRCASE) || (type == StructureConstants.FloorType.LEFT_STAIRCASE)) {}
        switch (material)
        {
        case WOOD: 
          toReturn.add(new BuildMaterial(22, 20));
          toReturn.add(new BuildMaterial(23, 10));
          toReturn.add(new BuildMaterial(218, 2));
          toReturn.add(new BuildMaterial(217, 1));
          break;
        case STONE_BRICK: 
          toReturn.add(new BuildMaterial(132, 5));
          toReturn.add(new BuildMaterial(492, 5));
          toReturn.add(new BuildMaterial(22, 15));
          toReturn.add(new BuildMaterial(23, 10));
          toReturn.add(new BuildMaterial(218, 1));
          toReturn.add(new BuildMaterial(217, 1));
          break;
        case SANDSTONE_SLAB: 
          toReturn.add(new BuildMaterial(1124, 2));
          toReturn.add(new BuildMaterial(492, 10));
          toReturn.add(new BuildMaterial(22, 15));
          toReturn.add(new BuildMaterial(23, 10));
          toReturn.add(new BuildMaterial(218, 1));
          toReturn.add(new BuildMaterial(217, 1));
          break;
        case STONE_SLAB: 
          toReturn.add(new BuildMaterial(406, 2));
          toReturn.add(new BuildMaterial(492, 10));
          toReturn.add(new BuildMaterial(22, 15));
          toReturn.add(new BuildMaterial(23, 10));
          toReturn.add(new BuildMaterial(218, 1));
          toReturn.add(new BuildMaterial(217, 1));
          break;
        case CLAY_BRICK: 
          toReturn.add(new BuildMaterial(776, 5));
          toReturn.add(new BuildMaterial(492, 5));
          toReturn.add(new BuildMaterial(22, 15));
          toReturn.add(new BuildMaterial(23, 10));
          toReturn.add(new BuildMaterial(218, 1));
          toReturn.add(new BuildMaterial(217, 1));
          break;
        case SLATE_SLAB: 
          toReturn.add(new BuildMaterial(771, 2));
          toReturn.add(new BuildMaterial(492, 10));
          toReturn.add(new BuildMaterial(22, 15));
          toReturn.add(new BuildMaterial(23, 10));
          toReturn.add(new BuildMaterial(218, 1));
          toReturn.add(new BuildMaterial(217, 1));
          break;
        case MARBLE_SLAB: 
          toReturn.add(new BuildMaterial(787, 2));
          toReturn.add(new BuildMaterial(492, 10));
          toReturn.add(new BuildMaterial(22, 15));
          toReturn.add(new BuildMaterial(23, 10));
          toReturn.add(new BuildMaterial(218, 1));
          toReturn.add(new BuildMaterial(217, 1));
          break;
        case STANDALONE: 
          toReturn.add(new BuildMaterial(22, 15));
          toReturn.add(new BuildMaterial(23, 10));
          toReturn.add(new BuildMaterial(218, 1));
          toReturn.add(new BuildMaterial(217, 1));
          break;
        case THATCH: 
        case METAL_IRON: 
        case METAL_COPPER: 
        case METAL_STEEL: 
        case METAL_SILVER: 
        case METAL_GOLD: 
        default: 
          logger.log(Level.WARNING, "Someone tried to make a staircase with an opening but the material choice was not supported (" + material
          
            .toString() + ")");
          break;
          if ((type == StructureConstants.FloorType.CLOCKWISE_STAIRCASE) || (type == StructureConstants.FloorType.ANTICLOCKWISE_STAIRCASE)) {}
          switch (material)
          {
          case WOOD: 
            toReturn.add(new BuildMaterial(22, 20));
            toReturn.add(new BuildMaterial(218, 2));
            toReturn.add(new BuildMaterial(217, 1));
            toReturn.add(new BuildMaterial(132, 20));
            toReturn.add(new BuildMaterial(492, 20));
            break;
          case STONE_BRICK: 
            toReturn.add(new BuildMaterial(22, 15));
            toReturn.add(new BuildMaterial(218, 1));
            toReturn.add(new BuildMaterial(217, 1));
            toReturn.add(new BuildMaterial(132, 25));
            toReturn.add(new BuildMaterial(492, 25));
            break;
          case SANDSTONE_SLAB: 
            toReturn.add(new BuildMaterial(1124, 2));
            toReturn.add(new BuildMaterial(22, 15));
            toReturn.add(new BuildMaterial(218, 1));
            toReturn.add(new BuildMaterial(217, 1));
            toReturn.add(new BuildMaterial(132, 20));
            toReturn.add(new BuildMaterial(492, 30));
            break;
          case STONE_SLAB: 
            toReturn.add(new BuildMaterial(406, 2));
            toReturn.add(new BuildMaterial(22, 15));
            toReturn.add(new BuildMaterial(218, 1));
            toReturn.add(new BuildMaterial(217, 1));
            toReturn.add(new BuildMaterial(132, 20));
            toReturn.add(new BuildMaterial(492, 30));
            break;
          case CLAY_BRICK: 
            toReturn.add(new BuildMaterial(776, 5));
            toReturn.add(new BuildMaterial(22, 15));
            toReturn.add(new BuildMaterial(218, 1));
            toReturn.add(new BuildMaterial(217, 1));
            toReturn.add(new BuildMaterial(132, 20));
            toReturn.add(new BuildMaterial(492, 25));
            break;
          case SLATE_SLAB: 
            toReturn.add(new BuildMaterial(771, 2));
            toReturn.add(new BuildMaterial(22, 15));
            toReturn.add(new BuildMaterial(218, 1));
            toReturn.add(new BuildMaterial(217, 1));
            toReturn.add(new BuildMaterial(132, 20));
            toReturn.add(new BuildMaterial(492, 30));
            break;
          case MARBLE_SLAB: 
            toReturn.add(new BuildMaterial(787, 2));
            toReturn.add(new BuildMaterial(22, 15));
            toReturn.add(new BuildMaterial(218, 1));
            toReturn.add(new BuildMaterial(217, 1));
            toReturn.add(new BuildMaterial(132, 20));
            toReturn.add(new BuildMaterial(492, 30));
            break;
          default: 
            logger.log(Level.WARNING, "Someone tried to make a spiral staircase but the material choice was not supported (" + material
            
              .toString() + ")");
            break;
            if ((type == StructureConstants.FloorType.CLOCKWISE_STAIRCASE_WITH) || (type == StructureConstants.FloorType.ANTICLOCKWISE_STAIRCASE_WITH)) {}
            switch (material)
            {
            case WOOD: 
              toReturn.add(new BuildMaterial(22, 20));
              toReturn.add(new BuildMaterial(23, 15));
              toReturn.add(new BuildMaterial(218, 5));
              toReturn.add(new BuildMaterial(217, 1));
              toReturn.add(new BuildMaterial(132, 20));
              toReturn.add(new BuildMaterial(492, 20));
              break;
            case STONE_BRICK: 
              toReturn.add(new BuildMaterial(22, 15));
              toReturn.add(new BuildMaterial(23, 15));
              toReturn.add(new BuildMaterial(218, 4));
              toReturn.add(new BuildMaterial(217, 1));
              toReturn.add(new BuildMaterial(132, 25));
              toReturn.add(new BuildMaterial(492, 25));
              break;
            case SANDSTONE_SLAB: 
              toReturn.add(new BuildMaterial(1124, 2));
              toReturn.add(new BuildMaterial(22, 15));
              toReturn.add(new BuildMaterial(23, 15));
              toReturn.add(new BuildMaterial(218, 4));
              toReturn.add(new BuildMaterial(217, 1));
              toReturn.add(new BuildMaterial(132, 20));
              toReturn.add(new BuildMaterial(492, 30));
              break;
            case STONE_SLAB: 
              toReturn.add(new BuildMaterial(406, 2));
              toReturn.add(new BuildMaterial(22, 15));
              toReturn.add(new BuildMaterial(23, 15));
              toReturn.add(new BuildMaterial(218, 4));
              toReturn.add(new BuildMaterial(217, 1));
              toReturn.add(new BuildMaterial(132, 20));
              toReturn.add(new BuildMaterial(492, 30));
              break;
            case CLAY_BRICK: 
              toReturn.add(new BuildMaterial(776, 5));
              toReturn.add(new BuildMaterial(22, 15));
              toReturn.add(new BuildMaterial(23, 15));
              toReturn.add(new BuildMaterial(218, 4));
              toReturn.add(new BuildMaterial(217, 1));
              toReturn.add(new BuildMaterial(132, 20));
              toReturn.add(new BuildMaterial(492, 25));
              break;
            case SLATE_SLAB: 
              toReturn.add(new BuildMaterial(771, 2));
              toReturn.add(new BuildMaterial(22, 15));
              toReturn.add(new BuildMaterial(23, 15));
              toReturn.add(new BuildMaterial(218, 4));
              toReturn.add(new BuildMaterial(217, 1));
              toReturn.add(new BuildMaterial(132, 20));
              toReturn.add(new BuildMaterial(492, 30));
              break;
            case MARBLE_SLAB: 
              toReturn.add(new BuildMaterial(787, 2));
              toReturn.add(new BuildMaterial(22, 15));
              toReturn.add(new BuildMaterial(23, 15));
              toReturn.add(new BuildMaterial(218, 4));
              toReturn.add(new BuildMaterial(217, 1));
              toReturn.add(new BuildMaterial(132, 20));
              toReturn.add(new BuildMaterial(492, 30));
              break;
            default: 
              logger.log(Level.WARNING, "Someone tried to make a staircase with an opening but the material choice was not supported (" + material
              
                .toString() + ")");
              break;
              switch (material)
              {
              case WOOD: 
                toReturn.add(new BuildMaterial(22, 10));
                toReturn.add(new BuildMaterial(218, 2));
                break;
              case STONE_BRICK: 
                toReturn.add(new BuildMaterial(132, 10));
                toReturn.add(new BuildMaterial(492, 10));
                break;
              case SANDSTONE_SLAB: 
                toReturn.add(new BuildMaterial(1124, 2));
                toReturn.add(new BuildMaterial(492, 10));
                break;
              case STONE_SLAB: 
                toReturn.add(new BuildMaterial(406, 2));
                toReturn.add(new BuildMaterial(492, 10));
                break;
              case CLAY_BRICK: 
                toReturn.add(new BuildMaterial(776, 10));
                toReturn.add(new BuildMaterial(492, 10));
                break;
              case SLATE_SLAB: 
                toReturn.add(new BuildMaterial(771, 2));
                toReturn.add(new BuildMaterial(492, 10));
                break;
              case MARBLE_SLAB: 
                toReturn.add(new BuildMaterial(787, 2));
                toReturn.add(new BuildMaterial(492, 10));
                break;
              default: 
                logger.log(Level.WARNING, "Someone tried to make a floor but the material choice was not supported (" + material
                
                  .toString() + ")");
              }
              break;
            }
            break;
          }
          break;
        }
      }
    }
    catch (NoSuchTemplateException nste)
    {
      logger.log(Level.WARNING, "FloorBehaviour.getRequiredMaterialsAtState trying to use material that have a non existing template.", nste);
    }
    return toReturn;
  }
  
  static List<BuildMaterial> getRequiredMaterialsFor(Floor floor)
  {
    if (floor.getType() == StructureConstants.FloorType.ROOF) {
      return getRequiredMaterialsForRoof(floor.getMaterial());
    }
    return getRequiredMaterialsForFloor(floor.getType(), floor.getMaterial());
  }
  
  static List<BuildMaterial> getRequiredMaterialsForFloor(Floor floor)
  {
    List<BuildMaterial> toReturn = getRequiredMaterialsForFloor(floor.getType(), floor.getMaterial());
    return toReturn;
  }
  
  public static List<BuildMaterial> getRequiredMaterialsAtState(Floor floor)
  {
    if (floor.getType() == StructureConstants.FloorType.ROOF) {
      return getRequiredMaterialsAtStateForRoof(floor);
    }
    return getRequiredMaterialsAtStateForFloor(floor);
  }
  
  public static List<BuildMaterial> getRequiredMaterialsAtStateForRoof(Floor floor)
  {
    List<BuildMaterial> mats = getRequiredMaterialsForRoof(floor.getMaterial());
    for (BuildMaterial mat : mats)
    {
      int qty = mat.getTotalQuantityRequired();
      if (floor.getState() > 0) {
        qty -= floor.getState();
      } else if (qty < 0) {
        qty = 0;
      }
      mat.setNeededQuantity(qty);
    }
    return mats;
  }
  
  public static List<BuildMaterial> getRequiredMaterialsAtStateForFloor(Floor floor)
  {
    List<BuildMaterial> mats = getRequiredMaterialsForFloor(floor.getType(), floor.getMaterial());
    for (BuildMaterial mat : mats)
    {
      int qty = mat.getTotalQuantityRequired();
      if (floor.getState() > 0) {
        qty -= floor.getState();
      } else if (qty < 0) {
        qty = 0;
      }
      mat.setNeededQuantity(qty);
    }
    return mats;
  }
  
  static final boolean isOkToBuild(Creature performer, Item tool, Floor floor, int floorLevel, boolean roof)
  {
    if (tool == null)
    {
      performer.getCommunicator().sendNormalServerMessage("You need to activate a building tool if you want to build something.");
      
      return false;
    }
    if (floor == null)
    {
      performer.getCommunicator().sendNormalServerMessage("You fail to focus, and cannot find that floor.");
      
      return false;
    }
    StructureConstants.FloorMaterial floorMaterial = floor.getMaterial();
    String nameOfWhatIsBeingBuilt = floor.getName();
    if (!hasValidTool(floor.getMaterial(), tool))
    {
      performer.getCommunicator().sendNormalServerMessage("You need to activate the correct building tool if you want to build that.");
      
      return false;
    }
    Skill buildSkill = getBuildSkill(floor.getType(), floorMaterial, performer);
    if (!mayPlanAtLevel(performer, floorLevel, buildSkill, roof)) {
      return false;
    }
    if (buildSkill.getKnowledge(0.0D) < getRequiredBuildSkillForFloorType(floorMaterial))
    {
      if (floor.getMaterial() == StructureConstants.FloorMaterial.STANDALONE) {
        performer.getCommunicator().sendNormalServerMessage("You need higher " + buildSkill
          .getName() + " skill to build " + nameOfWhatIsBeingBuilt + " with " + floor
          .getMaterial().getName() + ".");
      } else {
        performer.getCommunicator().sendNormalServerMessage("You need higher " + buildSkill
          .getName() + " skill to build " + floor.getMaterial().getName() + " " + nameOfWhatIsBeingBuilt + ".");
      }
      return false;
    }
    return true;
  }
  
  public static final boolean mayPlanAtLevel(Creature performer, int floorLevel, Skill buildSkill, boolean roof)
  {
    return mayPlanAtLevel(performer, floorLevel, buildSkill, roof, true);
  }
  
  public static final boolean mayPlanAtLevel(Creature performer, int floorLevel, Skill buildSkill, boolean roof, boolean sendMessage)
  {
    if (buildSkill.getKnowledge(0.0D) < getRequiredBuildSkillForFloorLevel(floorLevel, roof))
    {
      if (sendMessage) {
        performer.getCommunicator().sendNormalServerMessage("You need higher " + buildSkill
          .getName() + " skill to build at that height.");
      }
      return false;
    }
    return true;
  }
  
  private static final boolean floorBuilding(Action act, Creature performer, Item source, Floor floor, short action, float counter)
  {
    if (performer.isFighting())
    {
      performer.getCommunicator().sendNormalServerMessage("You cannot do that while in combat.");
      return true;
    }
    if (!isOkToBuild(performer, source, floor, floor.getFloorLevel(), floor.isRoof()))
    {
      performer.getCommunicator().sendActionResult(false);
      return true;
    }
    int time = 10;
    
    boolean insta = ((Servers.isThisATestServer()) || (performer.getPower() >= 4)) && (performer.getPower() > 1) && ((source.getTemplateId() == 315) || (source.getTemplateId() == 176));
    if (floor.isFinished())
    {
      performer.getCommunicator().sendNormalServerMessage("The " + floor.getName() + " is finished already.");
      performer.getCommunicator().sendActionResult(false);
      return true;
    }
    if (!Methods.isActionAllowed(performer, (short)116, floor.getTileX(), floor.getTileY())) {
      return true;
    }
    if (counter == 1.0F)
    {
      try
      {
        structure = Structures.getStructure(floor.getStructureId());
      }
      catch (NoSuchStructureException e)
      {
        Structure structure;
        logger.log(Level.WARNING, e.getMessage(), e);
        performer.getCommunicator().sendNormalServerMessage("Your sensitive mind notices a wrongness in the fabric of space.");
        
        performer.getCommunicator().sendActionResult(false);
        return true;
      }
      Structure structure;
      if (!MethodsStructure.mayModifyStructure(performer, structure, floor.getTile(), action))
      {
        performer.getCommunicator().sendNormalServerMessage("You need permission in order to make modifications to this structure.");
        
        performer.getCommunicator().sendActionResult(false);
        return true;
      }
      if ((!advanceNextState(performer, floor, act, true)) && (!insta))
      {
        String message = buildRequiredMaterialString(floor, false);
        if (floor.getType() == StructureConstants.FloorType.WIDE_STAIRCASE) {
          performer.getCommunicator().sendNormalServerMessage("You need " + message + " to start building the " + floor
            .getName() + " with " + floor
            .getMaterial().getName().toLowerCase() + ".");
        } else {
          performer.getCommunicator().sendNormalServerMessage("You need " + message + " to start building the " + floor
            .getName() + " of " + floor
            .getMaterial().getName().toLowerCase() + ".");
        }
        performer.getCommunicator().sendActionResult(false);
        return true;
      }
      Skill buildSkill = getBuildSkill(floor.getType(), floor.getMaterial(), performer);
      time = Actions.getSlowActionTime(performer, buildSkill, source, 0.0D);
      act.setTimeLeft(time);
      performer.getStatus().modifyStamina(-1000.0F);
      damageTool(performer, floor, source);
      
      Server.getInstance().broadCastAction(performer.getName() + " continues to build a " + floor.getName() + ".", performer, 5);
      
      performer.getCommunicator().sendNormalServerMessage("You continue to build a " + floor.getName() + ".");
      if (!insta) {
        performer.sendActionControl("Building a " + floor.getName(), true, time);
      }
    }
    else
    {
      time = act.getTimeLeft();
      if (act.currentSecond() % 5 == 0)
      {
        SoundPlayer.playSound(getBuildSound(floor), floor.getTileX(), floor.getTileY(), performer.isOnSurface(), 1.6F);
        performer.getStatus().modifyStamina(-1000.0F);
        damageTool(performer, floor, source);
      }
    }
    if ((counter * 10.0F > time) || (insta))
    {
      String message = buildRequiredMaterialString(floor, false);
      if ((!advanceNextState(performer, floor, act, false)) && (!insta))
      {
        if (floor.getType() == StructureConstants.FloorType.WIDE_STAIRCASE) {
          performer.getCommunicator().sendNormalServerMessage("You need " + message + " to build the " + floor
            .getName() + " with " + floor
            .getMaterial().getName().toLowerCase() + ".");
        } else {
          performer.getCommunicator().sendNormalServerMessage("You need " + message + " to build the " + floor
            .getName() + " of " + floor
            .getMaterial().getName().toLowerCase() + ".");
        }
        performer.getCommunicator().sendActionResult(false);
        return true;
      }
      double bonus = 0.0D;
      Skill toolSkill = getToolSkill(floor, performer, source);
      if (toolSkill != null)
      {
        toolSkill.skillCheck(10.0D, source, 0.0D, false, counter);
        bonus = toolSkill.getKnowledge(source, 0.0D) / 10.0D;
      }
      Skill buildSkill = getBuildSkill(floor.getType(), floor.getMaterial(), performer);
      
      double check = buildSkill.skillCheck(buildSkill.getRealKnowledge(), source, bonus, false, counter);
      
      floor.buildProgress(1);
      if ((WurmPermissions.mayUseGMWand(performer)) && 
        ((source.getTemplateId() == 315) || (source.getTemplateId() == 176)) && (
        (Servers.isThisATestServer()) || (performer.getPower() >= 4)))
      {
        if (!Servers.isThisATestServer()) {
          performer.sendToLoggers("Building floor with GM powers at [" + floor.getTile().getTileX() + "," + floor.getTile().getTileY() + "] at floor level " + floor
            .getFloorLevel());
        }
        floor.setFloorState(StructureConstants.FloorState.COMPLETED);
      }
      Server.getInstance().broadCastAction(performer
        .getName() + " attaches " + message + " to a " + floor.getName() + ".", performer, 5);
      performer.getCommunicator().sendNormalServerMessage("You attach " + message + " to a " + floor.getName() + ".");
      
      float oldql = floor.getQualityLevel();
      float qlevel = MethodsStructure.calculateNewQualityLevel(act.getPower(), buildSkill.getKnowledge(0.0D), oldql, 
        getTotalMaterials(floor));
      qlevel = Math.max(1.0F, qlevel);
      floor.setQualityLevel(qlevel);
      if (floor.getState() >= getFinishedState(floor))
      {
        if (insta) {
          floor.setQualityLevel(80.0F);
        }
        floor.setFloorState(StructureConstants.FloorState.COMPLETED);
        VolaTile floorTile = Zones.getOrCreateTile(floor.getTileX(), floor.getTileY(), floor.getLayer() >= 0);
        floorTile.updateFloor(floor);
        String floorName = Character.toUpperCase(floor.getName().charAt(0)) + floor.getName().substring(1);
        performer.getCommunicator().sendNormalServerMessage(floorName + " completed!");
      }
      try
      {
        floor.save();
      }
      catch (IOException e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
      if (floor.isFinished()) {
        performer.getCommunicator().sendRemoveFromCreationWindow(floor.getId());
      }
      performer.getCommunicator().sendActionResult(true);
      
      return true;
    }
    return false;
  }
  
  public static final float getRequiredBuildSkillForFloorLevel(int floorLevel, boolean roof)
  {
    int fLevel = roof ? floorLevel - 1 : floorLevel;
    if (fLevel <= 0) {
      return 5.0F;
    }
    switch (fLevel)
    {
    case 1: 
      return 21.0F;
    case 2: 
      return 30.0F;
    case 3: 
      return 39.0F;
    case 4: 
      return 47.0F;
    case 5: 
      return 55.0F;
    case 6: 
      return 63.0F;
    case 7: 
      return 70.0F;
    case 8: 
      return 77.0F;
    case 9: 
      return 83.0F;
    case 10: 
      return 88.0F;
    case 11: 
      return 92.0F;
    case 12: 
      return 95.0F;
    case 13: 
      return 97.0F;
    case 14: 
      return 98.0F;
    case 15: 
      return 99.0F;
    }
    return 200.0F;
  }
  
  public static final float getRequiredBuildSkillForFloorType(StructureConstants.FloorMaterial floorMaterial)
  {
    switch (floorMaterial)
    {
    case WOOD: 
    case STANDALONE: 
      return 5.0F;
    case THATCH: 
      return 21.0F;
    case STONE_BRICK: 
      return 21.0F;
    case SANDSTONE_SLAB: 
      return 21.0F;
    case STONE_SLAB: 
      return 21.0F;
    case CLAY_BRICK: 
      return 25.0F;
    case SLATE_SLAB: 
      return 30.0F;
    case MARBLE_SLAB: 
      return 40.0F;
    case METAL_IRON: 
    case METAL_COPPER: 
    case METAL_STEEL: 
    case METAL_SILVER: 
    case METAL_GOLD: 
      return 99.0F;
    }
    return 1.0F;
  }
  
  public static final Skill getBuildSkill(StructureConstants.FloorType floorType, StructureConstants.FloorMaterial floorMaterial, Creature performer)
  {
    int primSkillTemplate;
    int primSkillTemplate;
    if (floorType == StructureConstants.FloorType.ROOF) {
      primSkillTemplate = getSkillForRoof(floorMaterial);
    } else {
      primSkillTemplate = getSkillForFloor(floorMaterial);
    }
    Skill workSkill = null;
    try
    {
      workSkill = performer.getSkills().getSkill(primSkillTemplate);
    }
    catch (NoSuchSkillException nss)
    {
      workSkill = performer.getSkills().learn(primSkillTemplate, 1.0F);
    }
    return workSkill;
  }
  
  public static boolean hasValidTool(StructureConstants.FloorMaterial floorMaterial, Item source)
  {
    if ((source == null) || (floorMaterial == null)) {
      return false;
    }
    int tid = source.getTemplateId();
    
    boolean hasRightTool = false;
    switch (floorMaterial)
    {
    case METAL_STEEL: 
      hasRightTool = (tid == 62) || (tid == 63);
      break;
    case CLAY_BRICK: 
      hasRightTool = tid == 493;
      break;
    case SLATE_SLAB: 
      hasRightTool = tid == 493;
      break;
    case STONE_BRICK: 
      hasRightTool = tid == 493;
      break;
    case THATCH: 
      hasRightTool = (tid == 62) || (tid == 63);
      break;
    case WOOD: 
      hasRightTool = (tid == 62) || (tid == 63);
      break;
    case SANDSTONE_SLAB: 
      hasRightTool = tid == 493;
      break;
    case STONE_SLAB: 
      hasRightTool = tid == 493;
      break;
    case METAL_COPPER: 
      hasRightTool = (tid == 62) || (tid == 63);
      break;
    case METAL_IRON: 
      hasRightTool = (tid == 62) || (tid == 63);
      break;
    case MARBLE_SLAB: 
      hasRightTool = tid == 493;
      break;
    case METAL_GOLD: 
      hasRightTool = (tid == 62) || (tid == 63);
      break;
    case METAL_SILVER: 
      hasRightTool = (tid == 62) || (tid == 63);
      break;
    case STANDALONE: 
      hasRightTool = (tid == 62) || (tid == 63);
      break;
    default: 
      logger.log(Level.WARNING, "Enum value '" + floorMaterial.toString() + "' added to FloorMaterial but not to a switch statement in method FloorBehaviour.hasValidTool()");
    }
    if (tid == 315) {
      return true;
    }
    if (tid == 176) {
      return true;
    }
    return hasRightTool;
  }
  
  public static boolean actionDestroyFloor(Action act, Creature performer, Item source, Floor floor, short action, float counter)
  {
    if ((source.getTemplateId() == 824) || (source.getTemplateId() == 0))
    {
      performer.getCommunicator().sendNormalServerMessage("You will not do any damage to the floor with that.");
      return true;
    }
    if (floor.isIndestructible())
    {
      performer.getCommunicator().sendNormalServerMessage("That " + floor.getName() + " looks indestructable.");
      return true;
    }
    if (!Methods.isActionAllowed(performer, act.getNumber(), floor.getTileX(), floor.getTileY())) {
      return true;
    }
    if ((action == 524) && (MethodsHighways.onHighway(floor)))
    {
      performer.getCommunicator().sendNormalServerMessage("That floor is protected by the highway.");
      return true;
    }
    if (floor.getFloorState() == StructureConstants.FloorState.BUILDING)
    {
      if (((WurmPermissions.mayUseDeityWand(performer)) && (source.getTemplateId() == 176)) || (
        (WurmPermissions.mayUseGMWand(performer)) && (source.getTemplateId() == 315)))
      {
        floor.destroyOrRevertToPlan();
        performer.getCommunicator().sendNormalServerMessage("You remove a " + floor
          .getName() + " with your magic wand.");
        Server.getInstance().broadCastAction(performer
          .getName() + " effortlessly removes a " + floor.getName() + " with a magic wand.", performer, 3);
        
        return true;
      }
      return MethodsStructure.destroyFloor(action, performer, source, floor, counter);
    }
    if (floor.getFloorState() == StructureConstants.FloorState.COMPLETED)
    {
      if (((WurmPermissions.mayUseDeityWand(performer)) && (source.getTemplateId() == 176)) || (
        (WurmPermissions.mayUseGMWand(performer)) && (source.getTemplateId() == 315)))
      {
        floor.destroyOrRevertToPlan();
        
        performer.getCommunicator().sendNormalServerMessage("You remove a " + floor
          .getName() + " with your magic wand.");
        Server.getInstance().broadCastAction(performer
          .getName() + " effortlessly removes a " + floor.getName() + " with a magic wand.", performer, 3);
        
        return true;
      }
      return MethodsStructure.destroyFloor(action, performer, source, floor, counter);
    }
    if (floor.getFloorState() == StructureConstants.FloorState.PLANNING)
    {
      VolaTile vtile = Zones.getOrCreateTile(floor.getTileX(), floor.getTileY(), floor.getLayer() >= 0);
      Structure structure = vtile.getStructure();
      if (structure.wouldCreateFlyingStructureIfRemoved(floor))
      {
        performer.getCommunicator().sendNormalServerMessage("Removing that would cause a collapsing section.");
        return true;
      }
      floor.destroy();
      performer.getCommunicator().sendNormalServerMessage("You remove a plan for a new floor.");
      Server.getInstance().broadCastAction(performer.getName() + " removes a plan for a new floor.", performer, 3);
      return true;
    }
    return true;
  }
  
  public boolean action(Action act, Creature performer, boolean onSurface, Floor floor, int encodedTile, short action, float counter)
  {
    if ((action == 523) || (action == 522))
    {
      boolean done = false;
      if ((floor.getType() == StructureConstants.FloorType.OPENING) && ((floor.isFinished()) || (action == 523)))
      {
        if (floor.getFloorLevel() == performer.getFloorLevel())
        {
          if (action != 523) {
            return true;
          }
        }
        else
        {
          if (floor.getFloorLevel() != performer.getFloorLevel() + 1) {
            return true;
          }
          if (action != 522) {
            return true;
          }
        }
      }
      else
      {
        performer.getCommunicator().sendNormalServerMessage("Move a little bit closer to the ladder.", (byte)3);
        return true;
      }
      if (performer.getVehicle() != -10L)
      {
        performer.getCommunicator().sendNormalServerMessage("You can't climb right now.");
        return true;
      }
      if (performer.getFollowers().length > 0)
      {
        performer.getCommunicator().sendNormalServerMessage("You stop leading.", (byte)3);
        performer.stopLeading();
      }
      if (counter == 1.0F)
      {
        float qx = performer.getPosX() % 4.0F;
        float qy = performer.getPosY() % 4.0F;
        boolean getCloser = false;
        if ((performer.getTileX() != floor.getTileX()) || (performer.getTileY() != floor.getTileY()))
        {
          performer.getCommunicator().sendNormalServerMessage("You are too far away to climb that.", (byte)3);
          return true;
        }
        if (floor.getMaterial() == StructureConstants.FloorMaterial.STANDALONE) {
          switch (floor.getDir())
          {
          case 0: 
            getCloser = (qx < 1.0F) || (qx > 3.0F) || (qy < 3.0F);
            break;
          case 6: 
            getCloser = (qx < 1.0F) || (qy < 1.0F) || (qy > 3.0F);
            break;
          case 4: 
            getCloser = (qx < 1.0F) || (qx > 3.0F) || (qy > 1.0F);
            break;
          case 2: 
            getCloser = (qx > 1.0F) || (qy < 1.0F) || (qy > 3.0F);
            break;
          case 1: 
          case 3: 
          case 5: 
          default: 
            getCloser = true;
            break;
          }
        } else {
          getCloser = (qx < 1.0F) || (qx > 3.0F) || (qy < 1.0F) || (qy > 3.0F);
        }
        if (getCloser)
        {
          performer.getCommunicator().sendNormalServerMessage("Move a little bit closer to the ladder.", (byte)3);
          return true;
        }
        performer.sendActionControl("Climbing", true, 22);
        if (action == 523)
        {
          int groundoffset = 3;
          if (performer.getFloorLevel() - 1 == 0)
          {
            VolaTile t = performer.getCurrentTile();
            if (t.getFloors(-10, 10).length == 0) {
              groundoffset = 0;
            }
          }
          else
          {
            VolaTile t = performer.getCurrentTile();
            int tfloor = (performer.getFloorLevel() - 1) * 30;
            if (t.getFloors(tfloor - 10, tfloor + 10).length == 0)
            {
              performer.getCommunicator().sendNormalServerMessage("You can't climb down there.", (byte)3);
              return true;
            }
          }
          performer.getCommunicator().setGroundOffset(groundoffset + (performer.getFloorLevel() - 1) * 30, false);
        }
        else if (action == 522)
        {
          performer.getCommunicator().setGroundOffset((performer.getFloorLevel() + 1) * 30, false);
        }
      }
      else if (counter > 2.0F)
      {
        done = true;
      }
      return done;
    }
    if ((action == 177) || (action == 178))
    {
      if (!floor.isNotTurnable()) {
        return MethodsStructure.rotateFloor(performer, floor, counter, act);
      }
      performer.getCommunicator().sendNormalServerMessage("Looks like that floor is stuck in place.");
      return true;
    }
    if (action == 607)
    {
      if (!floor.isFinished()) {
        performer.getCommunicator().sendAddFloorRoofToCreationWindow(floor, -10L);
      }
      return true;
    }
    return super.action(act, performer, floor.getTileX(), floor.getTileY(), onSurface, 
      Zones.getTileIntForTile(floor.getTileX(), floor.getTileY(), 0), action, counter);
  }
  
  public boolean action(Action act, Creature performer, Item source, boolean onSurface, Floor floor, int encodedTile, short action, float counter)
  {
    if (action == 1) {
      return examine(performer, floor);
    }
    if (action == 607)
    {
      if (!floor.isFinished()) {
        performer.getCommunicator().sendAddFloorRoofToCreationWindow(floor, -10L);
      }
      return true;
    }
    if ((action == 523) || (action == 522) || (action == 177) || (action == 178)) {
      return action(act, performer, onSurface, floor, encodedTile, action, counter);
    }
    if (source == null) {
      return super.action(act, performer, floor.getTileX(), floor.getTileY(), onSurface, 
        Zones.getTileIntForTile(floor.getTileX(), floor.getTileY(), 0), action, counter);
    }
    if (action == 179)
    {
      if (((source.getTemplateId() == 176) || (source.getTemplateId() == 315)) && 
        (WurmPermissions.mayUseGMWand(performer))) {
        Methods.sendSummonQuestion(performer, source, floor.getTileX(), floor.getTileY(), floor.getStructureId());
      }
      return true;
    }
    if (action == 684)
    {
      if (((source.getTemplateId() == 315) || (source.getTemplateId() == 176)) && 
        (performer.getPower() >= 2)) {
        Methods.sendItemRestrictionManagement(performer, floor, floor.getId());
      } else {
        logger.log(Level.WARNING, performer.getName() + " hacking the protocol by trying to set the restrictions of " + floor + ", counter: " + counter + '!');
      }
      return true;
    }
    if (!isConstructionAction(action)) {
      return super.action(act, performer, source, floor.getTileX(), floor.getTileY(), onSurface, floor
        .getHeightOffset(), Zones.getTileIntForTile(floor.getTileX(), floor.getTileY(), 0), action, counter);
    }
    if ((action == 524) || (action == 525)) {
      return actionDestroyFloor(act, performer, source, floor, action, counter);
    }
    if (action == 193)
    {
      if (((!Servers.localServer.challengeServer) || (performer.getEnemyPresense() <= 0)) && 
        (!floor.isNoRepair())) {
        return MethodsStructure.repairFloor(performer, source, floor, counter, act);
      }
      return true;
    }
    if (action == 192)
    {
      if (!floor.isNoImprove()) {
        return MethodsStructure.improveFloor(performer, source, floor, counter, act);
      }
      return true;
    }
    if (action == 169)
    {
      if (floor.getFloorState() != StructureConstants.FloorState.BUILDING)
      {
        performer.getCommunicator().sendNormalServerMessage("The floor is in an invalid state to be continued.");
        
        performer.getCommunicator().sendActionResult(false);
        return true;
      }
      if (floorBuilding(act, performer, source, floor, action, counter))
      {
        performer.getCommunicator().sendAddFloorRoofToCreationWindow(floor, floor.getId());
        return true;
      }
      return false;
    }
    if (action == 508)
    {
      if (floor.isRoof())
      {
        performer.getCommunicator().sendNormalServerMessage("You can't plan above the " + floor.getName() + ".");
        return true;
      }
      return MethodsStructure.floorPlanAbove(performer, source, floor.getTileX(), floor.getTileY(), encodedTile, performer
        .getLayer(), counter, act, StructureConstants.FloorType.FLOOR);
    }
    if (action == 507) {
      return MethodsStructure.floorPlanRoof(performer, source, floor.getTileX(), floor.getTileY(), encodedTile, floor
        .getLayer(), counter, act);
    }
    if (action == 514) {
      return MethodsStructure.floorPlanAbove(performer, source, floor.getTileX(), floor.getTileY(), encodedTile, performer
        .getLayer(), counter, act, StructureConstants.FloorType.DOOR);
    }
    if (action == 515)
    {
      if (floor.isRoof())
      {
        performer.getCommunicator().sendNormalServerMessage("You can't plan above the " + floor.getName() + ".");
        return true;
      }
      return MethodsStructure.floorPlanAbove(performer, source, floor.getTileX(), floor.getTileY(), encodedTile, performer
        .getLayer(), counter, act, StructureConstants.FloorType.OPENING);
    }
    if ((action == 659) || (action == 704) || (action == 705) || (action == 706) || (action == 709) || (action == 710) || (action == 711) || (action == 712) || (action == 713) || (action == 714) || (action == 715))
    {
      if (floor.isRoof())
      {
        performer.getCommunicator().sendNormalServerMessage("You can't plan above the " + floor.getName() + ".");
        return true;
      }
      StructureConstants.FloorType ft;
      StructureConstants.FloorType ft;
      if (action == 704)
      {
        ft = StructureConstants.FloorType.WIDE_STAIRCASE;
      }
      else
      {
        StructureConstants.FloorType ft;
        if (action == 705)
        {
          ft = StructureConstants.FloorType.RIGHT_STAIRCASE;
        }
        else
        {
          StructureConstants.FloorType ft;
          if (action == 706)
          {
            ft = StructureConstants.FloorType.LEFT_STAIRCASE;
          }
          else
          {
            StructureConstants.FloorType ft;
            if (action == 709)
            {
              ft = StructureConstants.FloorType.CLOCKWISE_STAIRCASE;
            }
            else
            {
              StructureConstants.FloorType ft;
              if (action == 710)
              {
                ft = StructureConstants.FloorType.ANTICLOCKWISE_STAIRCASE;
              }
              else
              {
                StructureConstants.FloorType ft;
                if (action == 711)
                {
                  ft = StructureConstants.FloorType.CLOCKWISE_STAIRCASE_WITH;
                }
                else
                {
                  StructureConstants.FloorType ft;
                  if (action == 712)
                  {
                    ft = StructureConstants.FloorType.ANTICLOCKWISE_STAIRCASE_WITH;
                  }
                  else
                  {
                    StructureConstants.FloorType ft;
                    if (action == 713)
                    {
                      ft = StructureConstants.FloorType.WIDE_STAIRCASE_RIGHT;
                    }
                    else
                    {
                      StructureConstants.FloorType ft;
                      if (action == 714)
                      {
                        ft = StructureConstants.FloorType.WIDE_STAIRCASE_LEFT;
                      }
                      else
                      {
                        StructureConstants.FloorType ft;
                        if (action == 715) {
                          ft = StructureConstants.FloorType.WIDE_STAIRCASE_BOTH;
                        } else {
                          ft = StructureConstants.FloorType.STAIRCASE;
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
      return MethodsStructure.floorPlanAbove(performer, source, floor.getTileX(), floor.getTileY(), encodedTile, performer
        .getLayer(), counter, act, ft);
    }
    if (action - 20000 >= 0) {
      return buildAction(act, performer, source, floor, action, counter);
    }
    return true;
  }
  
  public static boolean isConstructionAction(short action)
  {
    if (action - 20000 >= 0) {
      return true;
    }
    switch (action)
    {
    case 169: 
    case 192: 
    case 193: 
    case 507: 
    case 508: 
    case 514: 
    case 515: 
    case 524: 
    case 525: 
    case 659: 
    case 704: 
    case 705: 
    case 706: 
    case 709: 
    case 710: 
    case 711: 
    case 712: 
    case 713: 
    case 714: 
    case 715: 
      return true;
    }
    return false;
  }
  
  private boolean examine(Creature performer, Floor floor)
  {
    String materials = "";
    if (floor.getFloorState() == StructureConstants.FloorState.BUILDING)
    {
      materials = buildRequiredMaterialString(floor, true);
      
      performer.getCommunicator().sendNormalServerMessage("You see a " + floor
        .getName() + " under construction. The " + floor.getName() + " requires " + materials + " to be finished.");
    }
    else
    {
      if (floor.getFloorState() == StructureConstants.FloorState.PLANNING)
      {
        performer.getCommunicator().sendNormalServerMessage("You see plans for a " + floor.getName() + ".");
        
        return true;
      }
      performer.getCommunicator().sendNormalServerMessage("It is a normal " + floor
        .getName() + " made of " + getMaterialDescription(floor).toLowerCase() + ".");
    }
    sendQlString(performer, floor);
    return true;
  }
  
  private static final String getMaterialDescription(Floor floor)
  {
    if (floor.getType() == StructureConstants.FloorType.ROOF)
    {
      switch (floor.getMaterial())
      {
      case CLAY_BRICK: 
        return "pottery shingles";
      case SLATE_SLAB: 
        return "slate shingles";
      case WOOD: 
        return "wood shingles";
      }
      return floor.getMaterial().getName().toLowerCase();
    }
    return floor.getMaterial().getName();
  }
  
  static final void sendQlString(Creature performer, Floor floor)
  {
    performer.getCommunicator().sendNormalServerMessage("QL = " + floor.getCurrentQL() + ", dam=" + floor.getDamage() + ".");
    if (performer.getPower() > 0) {
      performer.getCommunicator().sendNormalServerMessage("id: " + floor
        .getId() + " " + floor.getTileX() + "," + floor.getTileY() + " height: " + floor
        .getHeightOffset() + " " + floor
        .getMaterial().getName() + " " + floor.getType().getName() + " (" + floor
        .getFloorState().toString().toLowerCase() + ").");
    }
  }
  
  private static final String buildRequiredMaterialString(Floor floor, boolean detailed)
  {
    String description = new String();
    int numMats = 0;
    List<BuildMaterial> billOfMaterial = getRequiredMaterialsAtState(floor);
    
    int maxMats = 0;
    for (BuildMaterial mat : billOfMaterial) {
      if (mat.getNeededQuantity() > 0) {
        maxMats++;
      }
    }
    for (BuildMaterial mat : billOfMaterial) {
      if (mat.getNeededQuantity() > 0)
      {
        numMats++;
        ItemTemplate template = null;
        try
        {
          template = ItemTemplateFactory.getInstance().getTemplate(mat.getTemplateId());
        }
        catch (NoSuchTemplateException e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
        if (numMats > 1) {
          if (numMats < maxMats) {
            description = description + ", ";
          } else {
            description = description + " and ";
          }
        }
        if (template != null)
        {
          if (detailed) {
            description = description + mat.getNeededQuantity() + " ";
          }
          if (template.sizeString.length() > 0) {
            description = description + template.sizeString;
          }
          description = description + (mat.getNeededQuantity() > 1 ? template.getPlural() : template.getName());
        }
        if (description.length() == 0) {
          description = "unknown quantities of unknown materials";
        }
      }
    }
    if (description.length() == 0) {
      description = "no materials";
    }
    return description;
  }
  
  private static String getBuildSound(Floor floor)
  {
    String soundToPlay = "";
    switch (floor.getMaterial())
    {
    case CLAY_BRICK: 
    case SLATE_SLAB: 
    case STONE_BRICK: 
    case SANDSTONE_SLAB: 
    case STONE_SLAB: 
    case MARBLE_SLAB: 
      soundToPlay = "sound.work.masonry";
      break;
    case METAL_IRON: 
    case METAL_COPPER: 
    case METAL_STEEL: 
    case METAL_SILVER: 
    case METAL_GOLD: 
      soundToPlay = "sound.work.smithing.metal";
      break;
    case WOOD: 
    case THATCH: 
    default: 
      soundToPlay = Server.rand.nextInt(2) == 0 ? "sound.work.carpentry.mallet1" : "sound.work.carpentry.mallet2";
    }
    return soundToPlay;
  }
  
  private static void damageTool(Creature performer, Floor floor, Item source)
  {
    if (source.getTemplateId() == 63) {
      source.setDamage(source.getDamage() + 0.0015F * source.getDamageModifier());
    } else if (source.getTemplateId() == 62) {
      source.setDamage(source.getDamage() + 3.0E-4F * source.getDamageModifier());
    } else if (source.getTemplateId() == 493) {
      source.setDamage(source.getDamage() + 5.0E-4F * source.getDamageModifier());
    }
  }
  
  private static Skill getToolSkill(Floor floor, Creature performer, Item source)
  {
    Skill toolSkill = null;
    try
    {
      toolSkill = performer.getSkills().getSkill(source.getPrimarySkill());
    }
    catch (NoSuchSkillException nss)
    {
      try
      {
        toolSkill = performer.getSkills().learn(source.getPrimarySkill(), 1.0F);
      }
      catch (NoSuchSkillException localNoSuchSkillException1) {}
    }
    return toolSkill;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\behaviours\FloorBehaviour.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */