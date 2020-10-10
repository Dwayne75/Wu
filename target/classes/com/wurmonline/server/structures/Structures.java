package com.wurmonline.server.structures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.StructureConstants.FloorMaterial;
import com.wurmonline.shared.constants.StructureConstants.FloorState;
import com.wurmonline.shared.constants.StructureConstants.FloorType;
import com.wurmonline.shared.constants.StructureMaterialEnum;
import com.wurmonline.shared.constants.StructureStateEnum;
import com.wurmonline.shared.constants.StructureTypeEnum;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Structures
  implements MiscConstants, CounterTypes
{
  private static final String GET_STRUCTURES = "SELECT * FROM STRUCTURES";
  private static Map<Long, Structure> structures;
  private static Map<Long, Structure> bridges;
  private static final Structure[] emptyStructures = new Structure[0];
  private static final Logger logger = Logger.getLogger(Structures.class.getName());
  
  public static int getNumberOfStructures()
  {
    if (structures != null) {
      return structures.size();
    }
    return 0;
  }
  
  public static final Structure[] getAllStructures()
  {
    if (structures == null) {
      return emptyStructures;
    }
    return (Structure[])structures.values().toArray(new Structure[structures.size()]);
  }
  
  public static final Structure[] getManagedBuildingsFor(Player player, int villageId, boolean includeAll)
  {
    if (structures == null) {
      return emptyStructures;
    }
    Set<Structure> buildings = new HashSet();
    for (Structure structure : structures.values()) {
      if (structure.isTypeHouse())
      {
        if (structure.canManage(player)) {
          buildings.add(structure);
        }
        if (includeAll) {
          if (((villageId >= 0) && (structure.getVillageId() == villageId)) || 
            (structure.isActualOwner(player.getWurmId()))) {
            buildings.add(structure);
          }
        }
        if ((structure.getWritid() != -10L) && (structure.isActualOwner(player.getWurmId())))
        {
          Items.destroyItem(structure.getWritId());
          structure.setWritid(-10L, true);
        }
      }
    }
    return (Structure[])buildings.toArray(new Structure[buildings.size()]);
  }
  
  public static final Structure[] getOwnedBuildingFor(Player player)
  {
    if (structures == null) {
      return emptyStructures;
    }
    Set<Structure> buildings = new HashSet();
    for (Structure structure : structures.values()) {
      if (structure.isTypeHouse()) {
        if ((structure.isOwner(player)) || (structure.isActualOwner(player.getWurmId()))) {
          buildings.add(structure);
        }
      }
    }
    return (Structure[])buildings.toArray(new Structure[buildings.size()]);
  }
  
  public static final Structure getStructureOrNull(long id)
  {
    Structure structure = null;
    if (structures == null) {
      structures = new ConcurrentHashMap();
    } else {
      structure = (Structure)structures.get(new Long(id));
    }
    if (structure == null) {
      if (WurmId.getType(id) == 4) {
        try
        {
          structure = loadStructure(id);
          addStructure(structure);
        }
        catch (IOException localIOException) {}catch (NoSuchStructureException localNoSuchStructureException) {}
      }
    }
    return structure;
  }
  
  public static final Structure getStructure(long id)
    throws NoSuchStructureException
  {
    Structure structure = getStructureOrNull(id);
    if (structure == null) {
      throw new NoSuchStructureException("No such structure.");
    }
    return structure;
  }
  
  public static void addStructure(Structure structure)
  {
    if (structures == null) {
      structures = new ConcurrentHashMap();
    }
    structures.put(new Long(structure.getWurmId()), structure);
    if (structure.isTypeBridge()) {
      addBridge(structure);
    }
  }
  
  public static final void addBridge(Structure bridge)
  {
    if (bridges == null) {
      bridges = new ConcurrentHashMap();
    }
    bridges.put(new Long(bridge.getWurmId()), bridge);
  }
  
  public static void removeBridge(long id)
  {
    if (bridges != null) {
      bridges.remove(new Long(id));
    }
  }
  
  public static final Structure getBridge(long id)
  {
    Structure bridge = null;
    if (bridges != null) {
      bridge = (Structure)bridges.get(new Long(id));
    }
    return bridge;
  }
  
  public static void removeStructure(long id)
  {
    if (structures != null) {
      structures.remove(new Long(id));
    }
  }
  
  public static final Structure createStructure(byte theStructureType, String name, long id, int startx, int starty, boolean surfaced)
  {
    Structure toReturn = null;
    
    toReturn = new DbStructure(theStructureType, name, id, startx, starty, surfaced);
    addStructure(toReturn);
    
    return toReturn;
  }
  
  private static final Structure loadStructure(long id)
    throws IOException, NoSuchStructureException
  {
    Structure toReturn = null;
    
    toReturn = new DbStructure(id);
    addStructure(toReturn);
    
    return toReturn;
  }
  
  public static Structure getStructureForWrit(long writId)
    throws NoSuchStructureException
  {
    if (writId == -10L) {
      throw new NoSuchStructureException("No structure for writid " + writId);
    }
    for (Structure s : structures.values()) {
      if (s.getWritId() == writId) {
        return s;
      }
    }
    throw new NoSuchStructureException("No structure for writid " + writId);
  }
  
  public static void endLoadAll()
  {
    if (structures != null) {
      for (Structure struct : structures.values()) {
        try
        {
          struct.endLoading();
        }
        catch (IOException iox)
        {
          logger.log(Level.WARNING, iox.getMessage() + ": " + struct.getWurmId() + " writ " + struct.getWritid());
        }
      }
    }
  }
  
  public static void loadAllStructures()
    throws IOException
  {
    logger.info("Loading all Structures");
    long start = System.nanoTime();
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM STRUCTURES");
      rs = ps.executeQuery();
      while (rs.next())
      {
        long wurmid = rs.getLong("WURMID");
        byte structureType = rs.getByte("STRUCTURETYPE");
        boolean surfaced = rs.getBoolean("SURFACED");
        
        byte roof = rs.getByte("ROOF");
        
        String name = rs.getString("NAME");
        if (name == null) {
          name = "Unknown structure";
        }
        if (name.length() >= 50) {
          name = name.substring(0, 49);
        }
        boolean finished = rs.getBoolean("FINISHED");
        boolean finalfinished = rs.getBoolean("FINFINISHED");
        boolean allowsCitizens = rs.getBoolean("ALLOWSVILLAGERS");
        boolean allowsAllies = rs.getBoolean("ALLOWSALLIES");
        boolean allowsKingdom = rs.getBoolean("ALLOWSKINGDOM");
        String planner = rs.getString("PLANNER");
        long ownerId = rs.getLong("OWNERID");
        int settings = rs.getInt("SETTINGS");
        int villageId = rs.getInt("VILLAGE");
        long writid = -10L;
        try
        {
          writid = rs.getLong("WRITID");
        }
        catch (Exception nsi)
        {
          if (structureType == 0) {
            logger.log(Level.INFO, "No writ for house with id:" + wurmid + " creating new after loading.", nsi);
          }
        }
        addStructure(new DbStructure(structureType, name, wurmid, surfaced, roof, finished, finalfinished, writid, planner, ownerId, settings, villageId, allowsCitizens, allowsAllies, allowsKingdom));
      }
    }
    catch (SQLException sqex)
    {
      int numberOfStructures;
      throw new IOException(sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
      int numberOfStructures = structures != null ? structures.size() : 0;
      logger.log(Level.INFO, "Structures loaded. Number of structures=" + numberOfStructures + ". That took " + 
      
        (float)(System.nanoTime() - start) / 1000000.0F + " ms.");
    }
  }
  
  public static Structure getStructureForTile(int tilex, int tiley, boolean onSurface)
  {
    if (structures != null) {
      for (Structure s : structures.values()) {
        if ((s.isOnSurface() == onSurface) && (s.contains(tilex, tiley))) {
          return s;
        }
      }
    }
    return null;
  }
  
  public static Structure getBuildingForTile(int tilex, int tiley)
  {
    if (structures != null) {
      for (Structure s : structures.values()) {
        if (s.contains(tilex, tiley)) {
          return s;
        }
      }
    }
    return null;
  }
  
  public static final void createRandomStructure(Creature creator, int stx, int endtx, int sty, int endty, int centerx, int centery, byte material, String sname)
  {
    if ((creator.getCurrentTile() == null) || (creator.getCurrentTile().getStructure() == null)) {
      try
      {
        Structure struct = createStructure((byte)0, sname, WurmId.getNextPlanId(), centerx, centery, true);
        for (int currx = stx; currx <= endtx; currx++) {
          for (curry = sty; curry <= endty; curry++) {
            if ((currx != stx) || ((curry != sty) && (Server.rand.nextInt(3) < 2)))
            {
              vtile = Zones.getOrCreateTile(currx, curry, true);
              struct.addBuildTile(vtile, false);
              struct.clearAllWallsAndMakeWallsForStructureBorder(vtile);
            }
          }
        }
        float rot = Creature.normalizeAngle(creator.getStatus().getRotation());
        struct.makeFinal(creator, sname);
        int curry = struct.getStructureTiles();VolaTile vtile = curry.length;
        for (VolaTile localVolaTile1 = 0; localVolaTile1 < vtile; localVolaTile1++)
        {
          VolaTile bt = curry[localVolaTile1];
          
          StructureTypeEnum wtype = StructureTypeEnum.SOLID;
          if (Server.rand.nextInt(2) == 0) {
            wtype = StructureTypeEnum.WINDOW;
          }
          for (Wall plan : bt.getWalls())
          {
            if ((!plan.isHorizontal()) && (plan.getStartY() == creator.getTileY()) && 
              (rot <= 315.0F) && (rot >= 235.0F)) {
              wtype = StructureTypeEnum.DOOR;
            }
            if ((plan.isHorizontal()) && (plan.getStartX() == creator.getTileX()) && (
              ((rot >= 315.0F) && (rot <= 360.0F)) || ((rot >= 0.0F) && (rot <= 45.0F)))) {
              wtype = StructureTypeEnum.DOOR;
            }
            if ((plan.isHorizontal()) && (plan.getStartX() == creator.getTileX()) && 
              (rot >= 135.0F) && (rot <= 215.0F)) {
              wtype = StructureTypeEnum.DOOR;
            }
            if ((!plan.isHorizontal()) && (plan.getStartY() == creator.getTileY()) && 
              (rot <= 135.0F) && (rot >= 45.0F)) {
              wtype = StructureTypeEnum.DOOR;
            }
            if (material == 15) {
              plan.setMaterial(StructureMaterialEnum.STONE);
            } else {
              plan.setMaterial(StructureMaterialEnum.WOOD);
            }
            plan.setType(wtype);
            plan.setQualityLevel(80.0F);
            plan.setState(StructureStateEnum.FINISHED);
            bt.updateWall(plan);
            if (plan.isDoor())
            {
              Door door = new DbDoor(plan);
              door.setStructureId(struct.getWurmId());
              struct.addDoor(door);
              door.save();
              door.addToTiles();
            }
          }
        }
        struct.setFinished(true);
        struct.setFinalFinished(true);
        curry = struct.getStructureTiles();vtile = curry.length;
        for (VolaTile localVolaTile2 = 0; localVolaTile2 < vtile; localVolaTile2++)
        {
          VolaTile bt = curry[localVolaTile2];
          
          Floor floor = new DbFloor(StructureConstants.FloorType.FLOOR, bt.getTileX(), bt.getTileY(), 0, 80.0F, struct.getWurmId(), StructureConstants.FloorMaterial.WOOD, 0);
          
          floor.setFloorState(StructureConstants.FloorState.COMPLETED);
          bt.addFloor(floor);
          floor.save();
          Object roof = new DbFloor(StructureConstants.FloorType.ROOF, bt.getTileX(), bt.getTileY(), 30, 80.0F, struct.getWurmId(), StructureConstants.FloorMaterial.THATCH, 0);
          
          ((Floor)roof).setFloorState(StructureConstants.FloorState.COMPLETED);
          bt.addFloor((Floor)roof);
          ((Floor)roof).save();
        }
      }
      catch (Exception ex)
      {
        logger.log(Level.WARNING, "exception " + ex, ex);
        creator.getCommunicator().sendAlertServerMessage(ex.getMessage());
      }
    }
  }
  
  public static final void createSquareStructure(Creature creator, int stx, int endtx, int sty, int endty, int centerx, int centery, byte material, String sname)
  {
    if ((creator.getCurrentTile() == null) || (creator.getCurrentTile().getStructure() == null)) {
      try
      {
        Structure struct = createStructure((byte)0, sname, WurmId.getNextPlanId(), centerx, centery, true);
        for (int currx = stx; currx <= endtx; currx++) {
          for (int curry = sty; curry <= endty; curry++)
          {
            VolaTile vtile = Zones.getOrCreateTile(currx, curry, true);
            struct.addBuildTile(vtile, false);
            struct.clearAllWallsAndMakeWallsForStructureBorder(vtile);
          }
        }
        float rot = Creature.normalizeAngle(creator.getStatus().getRotation());
        struct.makeFinal(creator, sname);
        for (int currx = stx; currx <= endtx; currx++) {
          for (int curry = sty; curry <= endty; curry++)
          {
            VolaTile vtile = Zones.getOrCreateTile(currx, curry, true);
            StructureTypeEnum wtype = StructureTypeEnum.SOLID;
            if (Server.rand.nextInt(2) == 0) {
              wtype = StructureTypeEnum.WINDOW;
            }
            if (currx == stx) {
              for (Wall plan : vtile.getWalls()) {
                if ((!plan.isHorizontal()) && (plan.getStartX() == currx))
                {
                  if ((curry == creator.getTileY()) && 
                    (rot <= 315.0F) && (rot >= 235.0F)) {
                    wtype = StructureTypeEnum.DOOR;
                  }
                  if (material == 15) {
                    plan.setMaterial(StructureMaterialEnum.STONE);
                  } else {
                    plan.setMaterial(StructureMaterialEnum.WOOD);
                  }
                  plan.setType(wtype);
                  plan.setQualityLevel(80.0F);
                  plan.setState(StructureStateEnum.FINISHED);
                  vtile.updateWall(plan);
                  if (plan.isDoor())
                  {
                    Door door = new DbDoor(plan);
                    door.setStructureId(struct.getWurmId());
                    struct.addDoor(door);
                    door.save();
                    door.addToTiles();
                  }
                }
              }
            }
            if (curry == sty) {
              for (Wall plan : vtile.getWalls()) {
                if ((plan.isHorizontal()) && (plan.getStartY() == curry))
                {
                  if ((currx == creator.getTileX()) && (
                    ((rot >= 315.0F) && (rot <= 360.0F)) || ((rot >= 0.0F) && (rot <= 45.0F)))) {
                    wtype = StructureTypeEnum.DOOR;
                  }
                  if (material == 15) {
                    plan.setMaterial(StructureMaterialEnum.STONE);
                  } else {
                    plan.setMaterial(StructureMaterialEnum.WOOD);
                  }
                  plan.setType(wtype);
                  plan.setQualityLevel(80.0F);
                  plan.setState(StructureStateEnum.FINISHED);
                  vtile.updateWall(plan);
                  if (plan.isDoor())
                  {
                    Door door = new DbDoor(plan);
                    door.setStructureId(struct.getWurmId());
                    struct.addDoor(door);
                    door.save();
                    door.addToTiles();
                  }
                }
              }
            }
            if (curry == endty) {
              for (Wall plan : vtile.getWalls()) {
                if ((plan.isHorizontal()) && (plan.getStartY() == curry + 1))
                {
                  if ((currx == creator.getTileX()) && 
                    (rot >= 135.0F) && (rot <= 215.0F)) {
                    wtype = StructureTypeEnum.DOOR;
                  }
                  if (material == 15) {
                    plan.setMaterial(StructureMaterialEnum.STONE);
                  } else {
                    plan.setMaterial(StructureMaterialEnum.WOOD);
                  }
                  plan.setType(wtype);
                  plan.setQualityLevel(80.0F);
                  plan.setState(StructureStateEnum.FINISHED);
                  vtile.updateWall(plan);
                  if (plan.isDoor())
                  {
                    Door door = new DbDoor(plan);
                    door.setStructureId(struct.getWurmId());
                    struct.addDoor(door);
                    door.save();
                    door.addToTiles();
                  }
                }
              }
            }
            if (currx == endtx) {
              for (Wall plan : vtile.getWalls()) {
                if ((!plan.isHorizontal()) && (plan.getStartX() == currx + 1))
                {
                  if ((curry == creator.getTileY()) && 
                    (rot <= 135.0F) && (rot >= 45.0F)) {
                    wtype = StructureTypeEnum.DOOR;
                  }
                  if (material == 15) {
                    plan.setMaterial(StructureMaterialEnum.STONE);
                  } else {
                    plan.setMaterial(StructureMaterialEnum.WOOD);
                  }
                  plan.setType(wtype);
                  plan.setQualityLevel(80.0F);
                  plan.setState(StructureStateEnum.FINISHED);
                  vtile.updateWall(plan);
                  if (plan.isDoor())
                  {
                    Door door = new DbDoor(plan);
                    door.setStructureId(struct.getWurmId());
                    struct.addDoor(door);
                    door.save();
                    door.addToTiles();
                  }
                }
              }
            }
          }
        }
        struct.setFinished(true);
        struct.setFinalFinished(true);
        for (int currx = stx; currx <= endtx; currx++) {
          for (int curry = sty; curry <= endty; curry++)
          {
            VolaTile vtile = Zones.getOrCreateTile(currx, curry, true);
            Floor floor = new DbFloor(StructureConstants.FloorType.FLOOR, currx, curry, 0, 80.0F, struct.getWurmId(), StructureConstants.FloorMaterial.WOOD, 0);
            
            floor.setFloorState(StructureConstants.FloorState.COMPLETED);
            vtile.addFloor(floor);
            floor.save();
            Object roof = new DbFloor(StructureConstants.FloorType.ROOF, currx, curry, 30, 80.0F, struct.getWurmId(), StructureConstants.FloorMaterial.THATCH, 0);
            
            ((Floor)roof).setFloorState(StructureConstants.FloorState.COMPLETED);
            vtile.addFloor((Floor)roof);
            ((Floor)roof).save();
          }
        }
      }
      catch (Exception ex)
      {
        logger.log(Level.WARNING, "exception " + ex, ex);
        creator.getCommunicator().sendAlertServerMessage(ex.getMessage());
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\structures\Structures.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */