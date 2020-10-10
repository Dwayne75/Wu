package com.wurmonline.server.epic;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.mesh.TreeData.TreeType;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.Zones;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class EpicTargetItems
  implements MiscConstants
{
  private final long[] epicTargetItems = new long[18];
  private static final String LOAD_ALL_TARGET_ITEMS = "SELECT * FROM EPICTARGETITEMS WHERE KINGDOM=?";
  private static final String UPDATE_TARGET_ITEMS = "UPDATE EPICTARGETITEMS SET PILLARONE=?,PILLARTWO=?,PILLARTHREE=?,OBELISQUEONE=?,OBELISQUETWO=?,OBELISQUETHREE=?,PYLONONE=?,PYLONTWO=?,PYLONTHREE=?,TEMPLEONE=?,TEMPLETWO=?,TEMPLETHREE=?,SHRINEONE=?,SHRINETWO=?,SHRINETHREE=?,SPIRITGATEONE=?,SPIRITGATETWO=?,SPIRITGATETHREE=? WHERE KINGDOM=?";
  private static final String INSERT_TARGET_ITEMS = "INSERT INTO EPICTARGETITEMS (KINGDOM) VALUES(?)";
  private final byte kingdomId;
  static final int PILLAR_ONE = 0;
  static final int PILLAR_TWO = 1;
  static final int PILLAR_THREE = 2;
  static final int OBELISK_ONE = 3;
  static final int OBELISK_TWO = 4;
  static final int OBELISK_THREE = 5;
  static final int PYLON_ONE = 6;
  static final int PYLON_TWO = 7;
  static final int PYLON_THREE = 8;
  static final int TEMPLE_ONE = 9;
  static final int TEMPLE_TWO = 10;
  static final int TEMPLE_THREE = 11;
  static final int SHRINE_ONE = 12;
  static final int SHRINE_TWO = 13;
  static final int SHRINE_THREE = 14;
  static final int SPIRIT_GATE_ONE = 15;
  static final int SPIRIT_GATE_TWO = 16;
  static final int SPIRIT_GATE_THREE = 17;
  private static final Logger logger = Logger.getLogger(EpicTargetItems.class.getName());
  private static final Map<Byte, EpicTargetItems> KINGDOM_ITEMS = new ConcurrentHashMap();
  private static final ArrayList<Item> ritualTargetItems = new ArrayList();
  
  static
  {
    getEpicTargets((byte)4);
    getEpicTargets((byte)1);
    getEpicTargets((byte)2);
    getEpicTargets((byte)3);
    getEpicTargets((byte)0);
  }
  
  public EpicTargetItems(byte kingdomTemplateId)
  {
    this.kingdomId = kingdomTemplateId;
    loadAll();
    MissionHelper.loadAll();
  }
  
  public static void removeRitualTargetItem(Item ritualItem)
  {
    if (ritualTargetItems.contains(ritualItem)) {
      ritualTargetItems.remove(ritualItem);
    }
  }
  
  public static void addRitualTargetItem(Item ritualItem)
  {
    if (ritualItem == null) {
      return;
    }
    if (!ritualItem.isEpicTargetItem()) {
      return;
    }
    if (ritualItem.isUnfinished()) {
      return;
    }
    if (ritualTargetItems.contains(ritualItem)) {
      return;
    }
    ritualTargetItems.add(ritualItem);
  }
  
  public static Item getRandomRitualTarget()
  {
    if (ritualTargetItems.isEmpty()) {
      return null;
    }
    return (Item)ritualTargetItems.get(Server.rand.nextInt(ritualTargetItems.size()));
  }
  
  public static final EpicTargetItems getEpicTargets(byte kingdomTemplateId)
  {
    EpicTargetItems toReturn = (EpicTargetItems)KINGDOM_ITEMS.get(Byte.valueOf(kingdomTemplateId));
    if (toReturn == null)
    {
      toReturn = new EpicTargetItems(kingdomTemplateId);
      KINGDOM_ITEMS.put(Byte.valueOf(kingdomTemplateId), toReturn);
    }
    return toReturn;
  }
  
  public static final boolean isItemAlreadyEpic(Item itemChecked)
  {
    for (EpicTargetItems etis : KINGDOM_ITEMS.values()) {
      for (long item : etis.epicTargetItems) {
        if (item == itemChecked.getWurmId()) {
          return true;
        }
      }
    }
    return false;
  }
  
  public final byte getKingdomTemplateId()
  {
    return this.kingdomId;
  }
  
  public final void loadAll()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    
    boolean found = false;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM EPICTARGETITEMS WHERE KINGDOM=?");
      ps.setByte(1, this.kingdomId);
      rs = ps.executeQuery();
      while (rs.next())
      {
        rs.getByte(1);
        for (int x = 0; x <= 17; x++) {
          this.epicTargetItems[x] = rs.getLong(x + 2);
        }
        found = true;
      }
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to load epic target items.", sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    if (!found) {
      initialize();
    }
  }
  
  private final void initialize()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("INSERT INTO EPICTARGETITEMS (KINGDOM) VALUES(?)");
      ps.setByte(1, this.kingdomId);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to save epic target status for kingdom " + this.kingdomId, sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public final void testSetCounter(int toSet, long wid)
  {
    this.epicTargetItems[toSet] = wid;
    update();
  }
  
  public static final boolean isEpicItemWithMission(Item epicItem)
  {
    for (EpicMission m : )
    {
      boolean correctItem = false;
      switch (m.getMissionType())
      {
      case 101: 
        if ((epicItem.getTemplateId() == 717) || 
          (epicItem.getTemplateId() == 712)) {
          correctItem = true;
        }
        break;
      case 102: 
        if ((epicItem.getTemplateId() == 715) || 
          (epicItem.getTemplateId() == 714)) {
          correctItem = true;
        }
        break;
      case 103: 
        if ((epicItem.getTemplateId() == 713) || 
          (epicItem.getTemplateId() == 716)) {
          correctItem = true;
        }
        break;
      }
      if (correctItem)
      {
        int placementLocation = getTargetItemPlacement(m.getMissionId());
        int itemLocation = epicItem.getGlobalMapPlacement();
        if (itemLocation == placementLocation) {
          return true;
        }
        if ((placementLocation == 0) && (epicItem.isInTheNorth())) {
          return true;
        }
        if ((placementLocation == 2) && (epicItem.isInTheEast())) {
          return true;
        }
        if ((placementLocation == 4) && (epicItem.isInTheSouth())) {
          return true;
        }
        if ((placementLocation == 6) && (epicItem.isInTheWest())) {
          return true;
        }
      }
    }
    return false;
  }
  
  public final boolean addEpicItem(Item epicItem, Creature performer)
  {
    if (epicItem.isEpicTargetItem()) {
      if (mayBuildEpicItem(epicItem.getTemplateId(), epicItem.getTileX(), epicItem.getTileY(), epicItem.isOnSurface(), performer, performer
        .getKingdomTemplateId()))
      {
        if (epicItem.getGlobalMapPlacement() == getGlobalMapPlacementRequirement(epicItem.getTemplateId()))
        {
          logger.log(Level.INFO, performer.getName() + " Correct placement for " + epicItem);
          performer.sendToLoggers("Correct placement for " + epicItem, (byte)2);
          int toSet = getCurrentCounter(epicItem.getTemplateId());
          this.epicTargetItems[toSet] = epicItem.getWurmId();
          update();
          return true;
        }
        logger.log(Level.INFO, performer
          .getName() + " Not proper map placement " + epicItem.getGlobalMapPlacement() + " for " + epicItem
          .getName() + " here at " + epicItem
          .getTileX() + "," + epicItem
          .getTileY() + ": Required " + 
          getGlobalMapPlacementRequirement(epicItem.getTemplateId()));
        performer.sendToLoggers("Not proper map placement " + epicItem
          .getGlobalMapPlacement() + " for " + epicItem.getName() + " here at " + epicItem
          .getTileX() + "," + epicItem
          .getTileY() + ": Required " + 
          getGlobalMapPlacementRequirement(epicItem.getTemplateId()), (byte)2);
      }
      else
      {
        performer.sendToLoggers("May not build " + epicItem.getName() + " here at " + epicItem.getTileX() + "," + epicItem
          .getTileY(), (byte)2);
      }
    }
    return false;
  }
  
  public final int testGetCurrentCounter(int templateId)
  {
    return getCurrentCounter(templateId);
  }
  
  public final int getCurrentCounter(int itemTemplateId)
  {
    int toReturn = -1;
    switch (itemTemplateId)
    {
    case 717: 
      if (this.epicTargetItems[0] == 0L) {
        return 0;
      }
      if (this.epicTargetItems[1] == 0L) {
        return 1;
      }
      if (this.epicTargetItems[2] == 0L) {
        return 2;
      }
      break;
    case 714: 
      if (this.epicTargetItems[3] == 0L) {
        return 3;
      }
      if (this.epicTargetItems[4] == 0L) {
        return 4;
      }
      if (this.epicTargetItems[5] == 0L) {
        return 5;
      }
      break;
    case 713: 
      if (this.epicTargetItems[6] == 0L) {
        return 6;
      }
      if (this.epicTargetItems[7] == 0L) {
        return 7;
      }
      if (this.epicTargetItems[8] == 0L) {
        return 8;
      }
      break;
    case 712: 
      if (this.epicTargetItems[12] == 0L) {
        return 12;
      }
      if (this.epicTargetItems[13] == 0L) {
        return 13;
      }
      if (this.epicTargetItems[14] == 0L) {
        return 14;
      }
      break;
    case 715: 
      if (this.epicTargetItems[9] == 0L) {
        return 9;
      }
      if (this.epicTargetItems[10] == 0L) {
        return 10;
      }
      if (this.epicTargetItems[11] == 0L) {
        return 11;
      }
      break;
    case 716: 
      if (this.epicTargetItems[15] == 0L) {
        return 15;
      }
      if (this.epicTargetItems[16] == 0L) {
        return 16;
      }
      if (this.epicTargetItems[17] == 0L) {
        return 17;
      }
      break;
    default: 
      toReturn = -1;
    }
    return toReturn;
  }
  
  public static final String getSymbolNamePartString(Creature performer)
  {
    String toReturn = "Faith";
    int rand = Server.rand.nextInt(50);
    byte kingdomId = performer.getKingdomTemplateId();
    switch (rand)
    {
    case 0: 
      toReturn = "Secrets";
      break;
    case 1: 
      if (kingdomId == 3) {
        toReturn = "Libila";
      } else if (kingdomId == 2) {
        toReturn = "Magranon";
      } else if ((performer.getDeity() != null) && (performer.getDeity().number == 1)) {
        toReturn = "Fo";
      } else {
        toReturn = "Vynora";
      }
      break;
    case 2: 
      if (kingdomId == 3) {
        toReturn = "Hate";
      } else if (kingdomId == 2) {
        toReturn = "Fire";
      } else if ((performer.getDeity() != null) && (performer.getDeity().number == 1)) {
        toReturn = "Love";
      } else {
        toReturn = "Mysteries";
      }
      break;
    case 3: 
      if (kingdomId == 3) {
        toReturn = "Revenge";
      } else if (kingdomId == 2) {
        toReturn = "Power";
      } else if ((performer.getDeity() != null) && (performer.getDeity().number == 1)) {
        toReturn = "Compassion";
      } else {
        toReturn = "Wisdom";
      }
      break;
    case 4: 
      if (kingdomId == 3) {
        toReturn = "Death";
      } else if (kingdomId == 2) {
        toReturn = "Sand";
      } else if ((performer.getDeity() != null) && (performer.getDeity().number == 1)) {
        toReturn = "Tree";
      } else {
        toReturn = "Water";
      }
      break;
    case 5: 
      toReturn = "Spirit";
      break;
    case 6: 
      toReturn = "Soul";
      break;
    case 7: 
      toReturn = "Hope";
      break;
    case 8: 
      toReturn = "Despair";
      break;
    case 9: 
      toReturn = "Luck";
      break;
    case 10: 
      toReturn = "Heaven";
      break;
    case 11: 
      toReturn = "Valrei";
      break;
    case 12: 
      toReturn = "Strength";
      break;
    case 13: 
      toReturn = "Sleep";
      break;
    case 14: 
      toReturn = "Tongue";
      break;
    case 15: 
      toReturn = "Dreams";
      break;
    case 16: 
      toReturn = "Enlightened";
      break;
    case 17: 
      toReturn = "Fool";
      break;
    case 18: 
      toReturn = "Cat";
      break;
    case 19: 
      toReturn = "Troll";
      break;
    case 20: 
      toReturn = "Dragon";
      break;
    case 21: 
      toReturn = "Deep";
      break;
    case 22: 
      toReturn = "Square";
      break;
    case 23: 
      toReturn = "Song";
      break;
    case 24: 
      toReturn = "Jump";
      break;
    case 25: 
      toReturn = "High";
      break;
    case 26: 
      toReturn = "Low";
      break;
    case 27: 
      toReturn = "Inbetween";
      break;
    case 28: 
      toReturn = "One";
      break;
    case 29: 
      toReturn = "Many";
      break;
    case 30: 
      toReturn = "Sorrow";
      break;
    case 31: 
      toReturn = "Pain";
      break;
    case 32: 
      toReturn = "Oracle";
      break;
    case 33: 
      toReturn = "Slithering";
      break;
    case 34: 
      toReturn = "Roundabout";
      break;
    case 35: 
      toReturn = "Winter";
      break;
    case 36: 
      toReturn = "Summer";
      break;
    case 37: 
      toReturn = "Fallen";
      break;
    case 38: 
      toReturn = "Cherry";
      break;
    case 39: 
      toReturn = "Innocent";
      break;
    case 40: 
      toReturn = "Demon";
      break;
    case 41: 
      toReturn = "Left";
      break;
    case 42: 
      toReturn = "Shard";
      break;
    case 43: 
      toReturn = "Mantra";
      break;
    case 44: 
      toReturn = "Island";
      break;
    case 45: 
      toReturn = "Seafarer";
      break;
    case 46: 
      toReturn = "Ascendant";
      break;
    case 47: 
      toReturn = "Shame";
      break;
    case 48: 
      toReturn = "Running";
      break;
    case 49: 
      toReturn = "Lamentation";
      break;
    default: 
      toReturn = "Figure";
    }
    return toReturn;
  }
  
  public static final String getTypeNamePartString(int itemTemplateId)
  {
    String toReturn = "Focus";
    int rand = Server.rand.nextInt(10);
    toReturn = getTypeNamePartStringWithPart(itemTemplateId, rand);
    return toReturn;
  }
  
  static final String getTypeNamePartStringWithPart(int itemTemplateId, int partId)
  {
    String toReturn;
    String toReturn;
    String toReturn;
    String toReturn;
    String toReturn;
    String toReturn;
    String toReturn;
    switch (itemTemplateId)
    {
    case 717: 
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      switch (partId)
      {
      case 0: 
        toReturn = "Pillar";
        break;
      case 1: 
        toReturn = "Foundation";
        break;
      case 2: 
        toReturn = "Ram";
        break;
      case 3: 
        toReturn = "Symbol";
        break;
      case 4: 
        toReturn = "Tower";
        break;
      case 5: 
        toReturn = "Post";
        break;
      case 6: 
        toReturn = "Column";
        break;
      case 7: 
        toReturn = "Backbone";
        break;
      case 8: 
        toReturn = "Menhir";
        break;
      case 9: 
        toReturn = "Last Stand";
        break;
      default: 
        toReturn = "Pillar";
      }
      break;
    case 714: 
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      switch (partId)
      {
      case 0: 
        toReturn = "Needle";
        break;
      case 1: 
        toReturn = "Fist";
        break;
      case 2: 
        toReturn = "Obelisk";
        break;
      case 3: 
        toReturn = "Charge";
        break;
      case 4: 
        toReturn = "Mantra";
        break;
      case 5: 
        toReturn = "Testimonial";
        break;
      case 6: 
        toReturn = "Trophy";
        break;
      case 7: 
        toReturn = "Stand";
        break;
      case 8: 
        toReturn = "Spear";
        break;
      case 9: 
        toReturn = "Challenge";
        break;
      default: 
        toReturn = "Obelisk";
      }
      break;
    case 713: 
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      switch (partId)
      {
      case 0: 
        toReturn = "Memento";
        break;
      case 1: 
        toReturn = "Monument";
        break;
      case 2: 
        toReturn = "Path";
        break;
      case 3: 
        toReturn = "Way";
        break;
      case 4: 
        toReturn = "Door";
        break;
      case 5: 
        toReturn = "Victorial";
        break;
      case 6: 
        toReturn = "Shield";
        break;
      case 7: 
        toReturn = "Passage";
        break;
      case 8: 
        toReturn = "Rest";
        break;
      case 9: 
        toReturn = "Gate";
        break;
      default: 
        toReturn = "Pylon";
      }
      break;
    case 712: 
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      switch (partId)
      {
      case 0: 
        toReturn = "Shrine";
        break;
      case 1: 
        toReturn = "Barrow";
        break;
      case 2: 
        toReturn = "Vault";
        break;
      case 3: 
        toReturn = "Long Home";
        break;
      case 4: 
        toReturn = "Mausoleum";
        break;
      case 5: 
        toReturn = "Chamber";
        break;
      case 6: 
        toReturn = "Reliquary";
        break;
      case 7: 
        toReturn = "Remembrance";
        break;
      case 8: 
        toReturn = "Sacrarium";
        break;
      case 9: 
        toReturn = "Sanctum";
        break;
      default: 
        toReturn = "Shrine";
      }
      break;
    case 715: 
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      switch (partId)
      {
      case 0: 
        toReturn = "Church";
        break;
      case 1: 
        toReturn = "Temple";
        break;
      case 2: 
        toReturn = "Hand";
        break;
      case 3: 
        toReturn = "House";
        break;
      case 4: 
        toReturn = "Sanctuary";
        break;
      case 5: 
        toReturn = "Chapel";
        break;
      case 6: 
        toReturn = "Abode";
        break;
      case 7: 
        toReturn = "Walls";
        break;
      case 8: 
        toReturn = "Sign";
        break;
      case 9: 
        toReturn = "Fist";
        break;
      default: 
        toReturn = "Temple";
      }
      break;
    case 716: 
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      String toReturn;
      switch (partId)
      {
      case 0: 
        toReturn = "Pathway";
        break;
      case 1: 
        toReturn = "Mirror";
        break;
      case 2: 
        toReturn = "Mystery";
        break;
      case 3: 
        toReturn = "Gate";
        break;
      case 4: 
        toReturn = "Shimmer";
        break;
      case 5: 
        toReturn = "Route";
        break;
      case 6: 
        toReturn = "Run";
        break;
      case 7: 
        toReturn = "Trail";
        break;
      case 8: 
        toReturn = "Wake";
        break;
      case 9: 
        toReturn = "Secret";
        break;
      default: 
        toReturn = "Gate";
      }
      break;
    default: 
      toReturn = "Monument";
    }
    return toReturn;
  }
  
  public final String getInstructionString(int itemTemplateId)
  {
    return getInstructionStringForKingdom(itemTemplateId, this.kingdomId);
  }
  
  public static final String getInstructionStringForKingdom(int itemTemplateId, byte aKingdomId)
  {
    String toReturn;
    String toReturn;
    String toReturn;
    String toReturn;
    String toReturn;
    String toReturn;
    String toReturn;
    switch (itemTemplateId)
    {
    case 717: 
      toReturn = "This should be built in the darkness of a cave with sufficient ceiling height, not inside a settlement, and on a flat surface.";
      break;
    case 714: 
      toReturn = "This must be constructed on a 3x3 slabbed area, not inside a settlement, and on a flat surface.";
      break;
    case 713: 
      toReturn = "This must be constructed on a 7x7 slabbed area close to water, not inside a settlement, and on a flat surface.";
      
      break;
    case 712: 
      toReturn = "This must be constructed on a 5x5 slabbed area, not inside a settlement, and on a flat surface. A couple of fruit trees or bushes must be within 5 tiles.";
      
      break;
    case 715: 
      String toReturn;
      if (aKingdomId == 3) {
        toReturn = "This must be constructed on a 5x5 slabbed area, not inside a settlement, and on a flat surface. It must be within 5 tiles of marsh or mycelium.";
      } else {
        toReturn = "This must be constructed on a 5x5 slabbed area, not inside a settlement, and on a flat surface. It must be built higher up than 100 steps.";
      }
      break;
    case 716: 
      String toReturn;
      if (aKingdomId == 3)
      {
        String toReturn;
        if (Servers.localServer.PVPSERVER) {
          toReturn = "This must be constructed on a 5x5 slabbed area, not inside a settlement, and on a flat surface. It must be within 5 tiles of marsh as well as mycelium.";
        } else {
          toReturn = "This must be constructed on a 5x5 slabbed area, not inside a settlement, and on a flat surface. It must be within 5 tiles of marsh as well as moss.";
        }
      }
      else
      {
        toReturn = "This must be constructed on a 5x5 slabbed area, not inside a settlement, and on a flat surface. It must be built higher up than 100 steps.";
      }
      break;
    default: 
      toReturn = "It is not the right time to build this now.";
    }
    return toReturn;
  }
  
  public final String getGlobalMapPlacementRequirementString(int itemTemplateId)
  {
    int placement = getGlobalMapPlacementRequirement(itemTemplateId);
    String toReturn;
    String toReturn;
    String toReturn;
    String toReturn;
    String toReturn;
    switch (placement)
    {
    case 3: 
      toReturn = "This must be built in the south east.";
      break;
    case 5: 
      toReturn = "This must be built in the south west.";
      break;
    case 7: 
      toReturn = "This must be built in the north west.";
      break;
    case 1: 
      toReturn = "This must be built in the north east.";
      break;
    case 2: 
    case 4: 
    case 6: 
    default: 
      toReturn = "It is not the right time to build this now.";
    }
    return toReturn;
  }
  
  public final int getGlobalMapPlacementRequirement(int itemTemplateId)
  {
    int counter = getCurrentCounter(itemTemplateId);
    
    int toReturn = 0;
    if (counter <= -1) {
      return toReturn;
    }
    toReturn = getGlobalMapPlacementRequirementWithCounter(itemTemplateId, counter, this.kingdomId);
    return toReturn;
  }
  
  public static final String getTargetItemPlacementString(int placementLocation)
  {
    switch (placementLocation)
    {
    case 0: 
      return "This must be built in the north.";
    case 1: 
      return "This must be built in the northeast.";
    case 2: 
      return "This must be built in the east.";
    case 3: 
      return "This must be built in the southeast.";
    case 4: 
      return "This must be built in the south.";
    case 5: 
      return "This must be built in the southwest.";
    case 6: 
      return "This must be built in the west.";
    case 7: 
      return "This must be built in the northwest.";
    }
    return "It is not the right time to build this now.";
  }
  
  public static final int getTargetItemPlacement(int missionId)
  {
    Random r = new Random(missionId);
    return r.nextInt(8);
  }
  
  static final int getGlobalMapPlacementRequirementWithCounter(int aItemTemplateId, int aCounter, byte aKingdomId)
  {
    int toReturn = 0;
    switch (aItemTemplateId)
    {
    case 717: 
      switch (aKingdomId)
      {
      case 2: 
        switch (aCounter)
        {
        case 0: 
          toReturn = 5;
          break;
        case 1: 
          toReturn = 1;
          break;
        case 2: 
          toReturn = 3;
          break;
        default: 
          toReturn = 0;
        }
        break;
      case 3: 
        switch (aCounter)
        {
        case 0: 
          toReturn = 7;
          break;
        case 1: 
          toReturn = 1;
          break;
        case 2: 
          toReturn = 5;
          break;
        default: 
          toReturn = 0;
        }
        break;
      case 1: 
      case 4: 
        switch (aCounter)
        {
        case 0: 
          toReturn = 3;
          break;
        case 1: 
          toReturn = 7;
          break;
        case 2: 
          toReturn = 5;
          break;
        default: 
          toReturn = 0;
        }
        break;
      default: 
        toReturn = 0;
      }
      break;
    case 714: 
      switch (aKingdomId)
      {
      case 2: 
        switch (aCounter)
        {
        case 3: 
          toReturn = 7;
          break;
        case 4: 
          toReturn = 3;
          break;
        case 5: 
          toReturn = 1;
          break;
        default: 
          toReturn = 0;
        }
        break;
      case 3: 
        switch (aCounter)
        {
        case 3: 
          toReturn = 7;
          break;
        case 4: 
          toReturn = 3;
          break;
        case 5: 
          toReturn = 1;
          break;
        default: 
          toReturn = 0;
        }
        break;
      case 1: 
      case 4: 
        switch (aCounter)
        {
        case 3: 
          toReturn = 7;
          break;
        case 4: 
          toReturn = 3;
          break;
        case 5: 
          toReturn = 5;
          break;
        default: 
          toReturn = 0;
        }
        break;
      default: 
        toReturn = 0;
      }
      break;
    case 713: 
      switch (aKingdomId)
      {
      case 2: 
        switch (aCounter)
        {
        case 6: 
          toReturn = 3;
          break;
        case 7: 
          toReturn = 1;
          break;
        case 8: 
          toReturn = 5;
          break;
        default: 
          toReturn = 0;
        }
        break;
      case 3: 
        switch (aCounter)
        {
        case 6: 
          toReturn = 5;
          break;
        case 7: 
          toReturn = 7;
          break;
        case 8: 
          toReturn = 1;
          break;
        default: 
          toReturn = 0;
        }
        break;
      case 1: 
      case 4: 
        switch (aCounter)
        {
        case 6: 
          toReturn = 3;
          break;
        case 7: 
          toReturn = 7;
          break;
        case 8: 
          toReturn = 5;
          break;
        default: 
          toReturn = 0;
        }
        break;
      default: 
        toReturn = 0;
      }
      break;
    case 712: 
      switch (aKingdomId)
      {
      case 2: 
        switch (aCounter)
        {
        case 12: 
          toReturn = 1;
          break;
        case 13: 
          toReturn = 7;
          break;
        case 14: 
          toReturn = 3;
          break;
        default: 
          toReturn = 0;
        }
        break;
      case 3: 
        switch (aCounter)
        {
        case 12: 
          toReturn = 1;
          break;
        case 13: 
          toReturn = 3;
          break;
        case 14: 
          toReturn = 7;
          break;
        default: 
          toReturn = 0;
        }
        break;
      case 1: 
      case 4: 
        switch (aCounter)
        {
        case 12: 
          toReturn = 3;
          break;
        case 13: 
          toReturn = 7;
          break;
        case 14: 
          toReturn = 5;
          break;
        default: 
          toReturn = 0;
        }
        break;
      default: 
        toReturn = 0;
      }
      break;
    case 715: 
      switch (aKingdomId)
      {
      case 2: 
        switch (aCounter)
        {
        case 9: 
          toReturn = 3;
          break;
        case 10: 
          toReturn = 1;
          break;
        case 11: 
          toReturn = 5;
          break;
        default: 
          toReturn = 0;
        }
        break;
      case 3: 
        switch (aCounter)
        {
        case 9: 
          toReturn = 7;
          break;
        case 10: 
          toReturn = 5;
          break;
        case 11: 
          toReturn = 1;
          break;
        default: 
          toReturn = 0;
        }
        break;
      case 1: 
      case 4: 
        switch (aCounter)
        {
        case 9: 
          toReturn = 3;
          break;
        case 10: 
          toReturn = 7;
          break;
        case 11: 
          toReturn = 5;
          break;
        default: 
          toReturn = 0;
        }
        break;
      default: 
        toReturn = 0;
      }
      break;
    case 716: 
      switch (aKingdomId)
      {
      case 2: 
        switch (aCounter)
        {
        case 15: 
          toReturn = 7;
          break;
        case 16: 
          toReturn = 1;
          break;
        case 17: 
          toReturn = 3;
          break;
        default: 
          toReturn = 0;
        }
        break;
      case 3: 
        switch (aCounter)
        {
        case 15: 
          toReturn = 1;
          break;
        case 16: 
          toReturn = 7;
          break;
        case 17: 
          toReturn = 3;
          break;
        default: 
          toReturn = 0;
        }
        break;
      case 1: 
      case 4: 
        switch (aCounter)
        {
        case 15: 
          toReturn = 5;
          break;
        case 16: 
          toReturn = 3;
          break;
        case 17: 
          toReturn = 7;
          break;
        default: 
          toReturn = 0;
        }
        break;
      default: 
        toReturn = 0;
      }
      break;
    default: 
      toReturn = 0;
    }
    return toReturn;
  }
  
  public static final boolean mayBuildEpicItem(int itemTemplateId, int tilex, int tiley, boolean surfaced, Creature performer, byte kingdomTemplateId)
  {
    if (!Terraforming.isFlat(tilex, tiley, surfaced, 4))
    {
      performer.sendToLoggers("The tile is not flat", (byte)2);
      return false;
    }
    if (Villages.getVillage(tilex, tiley, surfaced) != null) {
      return false;
    }
    boolean toReturn = true;
    switch (itemTemplateId)
    {
    case 717: 
      toReturn = false;
      if (!surfaced)
      {
        toReturn = true;
        int cornerNorthW = Server.caveMesh.getTile(tilex, tiley);
        short ceilHeight = (short)(Tiles.decodeData(cornerNorthW) & 0xFF);
        if (ceilHeight < 50)
        {
          performer.sendToLoggers("The NW corner is too low " + ceilHeight, (byte)2);
          toReturn = false;
        }
        int cornerNorthE = Server.caveMesh.getTile(tilex + 1, tiley);
        short ceilHeightNE = (short)(Tiles.decodeData(Tiles.decodeTileData(cornerNorthE)) & 0xFF);
        if (ceilHeightNE < 50)
        {
          performer.sendToLoggers("The NE corner is too low " + ceilHeightNE, (byte)2);
          toReturn = false;
        }
        int cornerSE = Server.caveMesh.getTile(tilex + 1, tiley + 1);
        short ceilHeightSE = (short)(Tiles.decodeData(Tiles.decodeTileData(cornerSE)) & 0xFF);
        if (ceilHeightSE < 50)
        {
          performer.sendToLoggers("The SE corner is too low " + ceilHeightSE, (byte)2);
          toReturn = false;
        }
        int cornerSW = Server.caveMesh.getTile(tilex, tiley + 1);
        short ceilHeightSW = (short)(Tiles.decodeData(Tiles.decodeTileData(cornerSW)) & 0xFF);
        if (ceilHeightSW < 50)
        {
          performer.sendToLoggers("The SW corner is too low " + ceilHeightSW, (byte)2);
          toReturn = false;
        }
      }
      else
      {
        performer.sendToLoggers("The pillar is on the surface!", (byte)2);
      }
      break;
    case 714: 
      toReturn = true;
      for (int x = Zones.safeTileX(tilex - 1); x <= Zones.safeTileX(tilex + 1); x++) {
        for (int y = Zones.safeTileY(tiley - 1); y <= Zones.safeTileY(tiley + 1); y++)
        {
          if (!Terraforming.isFlat(x, y, true, 4)) {
            toReturn = false;
          }
          if (!Tiles.isRoadType(Tiles.decodeType(Server.surfaceMesh.getTile(x, y)))) {
            toReturn = false;
          }
        }
      }
      break;
    case 713: 
      toReturn = true;
      for (int x = Zones.safeTileX(tilex - 3); x <= Zones.safeTileX(tilex + 3); x++) {
        for (int y = Zones.safeTileY(tiley - 3); y <= Zones.safeTileY(tiley + 3); y++)
        {
          if (!Terraforming.isFlat(x, y, true, 4))
          {
            toReturn = false;
            break;
          }
          if (!Tiles.isRoadType(Tiles.decodeType(Server.surfaceMesh.getTile(x, y))))
          {
            toReturn = false;
            break;
          }
        }
      }
      if (toReturn == true)
      {
        toReturn = false;
        for (int x = Zones.safeTileX(tilex - 10); x <= Zones.safeTileX(tilex + 10); x += 5) {
          for (int y = Zones.safeTileY(tiley - 10); y <= Zones.safeTileY(tiley + 10); y += 5) {
            if (Tiles.decodeHeight(Server.surfaceMesh.getTile(x, y)) < 0)
            {
              toReturn = true;
              break;
            }
          }
        }
      }
      break;
    case 712: 
      for (int x = Zones.safeTileX(tilex - 2); x <= Zones.safeTileX(tilex + 2); x++) {
        for (int y = Zones.safeTileY(tiley - 2); y <= Zones.safeTileY(tiley + 2); y++)
        {
          if (!Terraforming.isFlat(x, y, true, 4))
          {
            toReturn = false;
            break;
          }
          if (!Tiles.isRoadType(Tiles.decodeType(Server.surfaceMesh.getTile(x, y))))
          {
            toReturn = false;
            break;
          }
        }
      }
      if (toReturn == true)
      {
        toReturn = false;
        int numSmalltrees = 0;
        for (int x = Zones.safeTileX(tilex - 5); x <= Zones.safeTileX(tilex + 5); x++) {
          for (int y = Zones.safeTileY(tiley - 5); y <= Zones.safeTileY(tiley + 5); y++)
          {
            int t = Server.surfaceMesh.getTile(x, y);
            Tiles.Tile theTile = Tiles.getTile(Tiles.decodeType(t));
            byte data = Tiles.decodeData(t);
            if (theTile.isNormalTree())
            {
              if (theTile.getTreeType(data).isFruitTree()) {
                numSmalltrees++;
              }
            }
            else if ((theTile.isMyceliumTree()) && (kingdomTemplateId == 3)) {
              if (theTile.getTreeType(data).isFruitTree()) {
                numSmalltrees++;
              }
            }
          }
        }
        if (numSmalltrees > 3) {
          toReturn = true;
        }
      }
      break;
    case 715: 
      for (int x = Zones.safeTileX(tilex - 2); x <= Zones.safeTileX(tilex + 2); x++) {
        for (int y = Zones.safeTileY(tiley - 2); y <= Zones.safeTileY(tiley + 2); y++)
        {
          if (!Terraforming.isFlat(x, y, true, 4))
          {
            toReturn = false;
            break;
          }
          if (!Tiles.isRoadType(Tiles.decodeType(Server.surfaceMesh.getTile(x, y))))
          {
            toReturn = false;
            break;
          }
        }
      }
      if (toReturn == true)
      {
        toReturn = false;
        if (kingdomTemplateId == 3)
        {
          for (int x = Zones.safeTileX(tilex - 5); x <= Zones.safeTileX(tilex + 5); x++) {
            for (int y = Zones.safeTileY(tiley - 5); y <= Zones.safeTileY(tiley + 5); y++)
            {
              int t = Server.surfaceMesh.getTile(x, y);
              byte type = Tiles.decodeType(t);
              if ((Tiles.decodeType(t) == Tiles.Tile.TILE_MARSH.id) || (type == Tiles.Tile.TILE_MYCELIUM.id) || 
                (Tiles.getTile(type).isMyceliumTree()))
              {
                toReturn = true;
                break;
              }
            }
          }
        }
        else
        {
          int t = Server.surfaceMesh.getTile(tilex, tiley);
          if (Tiles.decodeHeight(t) > 1000) {
            toReturn = true;
          }
        }
      }
      break;
    case 716: 
      for (int x = Zones.safeTileX(tilex - 2); x <= Zones.safeTileX(tilex + 2); x++) {
        for (int y = Zones.safeTileY(tiley - 2); y <= Zones.safeTileY(tiley + 2); y++)
        {
          if (!Terraforming.isFlat(x, y, true, 4))
          {
            toReturn = false;
            break;
          }
          if (!Tiles.isRoadType(Tiles.decodeType(Server.surfaceMesh.getTile(x, y))))
          {
            toReturn = false;
            break;
          }
        }
      }
      if (toReturn == true)
      {
        toReturn = false;
        if (kingdomTemplateId == 3)
        {
          boolean foundMycel = false;
          boolean foundMarsh = false;
          for (int x = Zones.safeTileX(tilex - 5); x <= Zones.safeTileX(tilex + 5); x++) {
            for (int y = Zones.safeTileY(tiley - 5); y <= Zones.safeTileY(tiley + 5); y++)
            {
              int t = Server.surfaceMesh.getTile(x, y);
              if (Servers.localServer.PVPSERVER)
              {
                byte type = Tiles.decodeType(t);
                if ((type == Tiles.Tile.TILE_MYCELIUM.id) || (Tiles.getTile(type).isMyceliumTree()))
                {
                  foundMycel = true;
                  continue;
                }
              }
              else if (Tiles.decodeType(t) == Tiles.Tile.TILE_MOSS.id)
              {
                foundMycel = true;
                continue;
              }
              if (Tiles.decodeType(t) == Tiles.Tile.TILE_MARSH.id) {
                foundMarsh = true;
              }
            }
          }
          if ((foundMycel) && (foundMarsh)) {
            toReturn = true;
          }
        }
        else
        {
          int t = Server.surfaceMesh.getTile(tilex, tiley);
          if (Tiles.decodeHeight(t) > 1000) {
            toReturn = true;
          }
        }
      }
      break;
    default: 
      toReturn = false;
    }
    return toReturn;
  }
  
  private final void update()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("UPDATE EPICTARGETITEMS SET PILLARONE=?,PILLARTWO=?,PILLARTHREE=?,OBELISQUEONE=?,OBELISQUETWO=?,OBELISQUETHREE=?,PYLONONE=?,PYLONTWO=?,PYLONTHREE=?,TEMPLEONE=?,TEMPLETWO=?,TEMPLETHREE=?,SHRINEONE=?,SHRINETWO=?,SHRINETHREE=?,SPIRITGATEONE=?,SPIRITGATETWO=?,SPIRITGATETHREE=? WHERE KINGDOM=?");
      for (int x = 0; x <= 17; x++) {
        ps.setLong(x + 1, this.epicTargetItems[x]);
      }
      ps.setByte(19, this.kingdomId);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to save epic target status for kingdom " + this.kingdomId, sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  final long getRandomTarget()
  {
    return getRandomTarget(0, 0, null);
  }
  
  private final long getRandomTarget(int attempts, int targetTemplate, @Nullable ArrayList<Long> itemList)
  {
    long itemFound = -1L;
    if (Servers.localServer.PVPSERVER)
    {
      int numsExisting = 0;
      for (int x = 0; x < 17; x++) {
        if (this.epicTargetItems[x] > 0L) {
          numsExisting++;
        }
      }
      if (numsExisting > 0) {
        for (int x = 0; x < 17; x++) {
          if (this.epicTargetItems[x] > 0L) {
            try
            {
              Item eti = Items.getItem(this.epicTargetItems[x]);
              Village v = Villages.getVillage(eti.getTilePos(), eti.isOnSurface());
              if (v == null)
              {
                if (itemFound == -1L) {
                  itemFound = this.epicTargetItems[x];
                } else if (Server.rand.nextInt(numsExisting) == 0) {
                  itemFound = this.epicTargetItems[x];
                }
              }
              else {
                logger.info("Disqualified Epic Mission Target item due to being in village " + v.getName() + ": Name: " + eti
                  .getName() + " | WurmID: " + eti.getWurmId() + " | TileX: " + eti.getTileX() + " | TileY: " + eti
                  .getTileY());
              }
            }
            catch (NoSuchItemException nsie)
            {
              logger.warning("Epic mission item could not be found when loaded, maybe it was wrongfully deleted? WurmID:" + this.epicTargetItems[x] + ". " + nsie);
            }
          }
        }
      }
    }
    else
    {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("Entering Freedom Version of Valrei Mission Target Structure selection.");
      }
      Connection dbcon = null;
      PreparedStatement ps1 = null;
      ResultSet rs = null;
      
      int structureType = Server.rand.nextInt(6);
      int templateId;
      int templateId;
      if (targetTemplate > 0)
      {
        templateId = targetTemplate;
      }
      else
      {
        int templateId;
        int templateId;
        int templateId;
        int templateId;
        int templateId;
        int templateId;
        switch (structureType)
        {
        case 0: 
          templateId = 717;
          break;
        case 1: 
          templateId = 714;
          break;
        case 2: 
          templateId = 713;
          break;
        case 3: 
          templateId = 715;
          break;
        case 4: 
          templateId = 712;
          break;
        case 5: 
          templateId = 716;
          break;
        default: 
          templateId = 713;
        }
      }
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("Selected template with id=" + templateId);
      }
      if (itemList == null)
      {
        itemList = new ArrayList();
        try
        {
          String dbQueryString = "SELECT WURMID FROM ITEMS WHERE TEMPLATEID=?";
          if (logger.isLoggable(Level.FINER)) {
            logger.finer("Query String [ SELECT WURMID FROM ITEMS WHERE TEMPLATEID=? ]");
          }
          dbcon = DbConnector.getItemDbCon();
          ps1 = dbcon.prepareStatement("SELECT WURMID FROM ITEMS WHERE TEMPLATEID=?");
          ps1.setInt(1, templateId);
          rs = ps1.executeQuery();
          while (rs.next())
          {
            long currentLong = rs.getLong("WURMID");
            if (currentLong > 0L) {
              itemList.add(Long.valueOf(currentLong));
            }
            if (logger.isLoggable(Level.FINEST)) {
              logger.finest(rs.toString());
            }
          }
        }
        catch (SQLException ex)
        {
          logger.log(Level.WARNING, "Failed to locate mission items with templateid=" + templateId, ex);
        }
        finally
        {
          DbUtilities.closeDatabaseObjects(ps1, null);
          DbConnector.returnConnection(dbcon);
        }
      }
      if (itemList.size() > 0)
      {
        int randomIndex = Server.rand.nextInt(itemList.size());
        if (itemList.get(randomIndex) != null)
        {
          long selectedTarget = ((Long)itemList.get(randomIndex)).longValue();
          try
          {
            Item eti = Items.getItem(selectedTarget);
            Village v = Villages.getVillage(eti.getTilePos(), eti.isOnSurface());
            if (v == null)
            {
              logger.info("Selected mission target with wurmid=" + selectedTarget);
              return selectedTarget;
            }
            logger.info("Disqualified Epic Mission Target item due to being in village " + v.getName() + ": Name: " + eti
              .getName() + " | WurmID: " + eti.getWurmId() + " | TileX: " + eti.getTileX() + " | TileY: " + eti
              .getTileY());
            
            int ATTEMPT_NUMBER_OF_TIMES = 25;
            if (attempts < 25)
            {
              logger.fine("Failing roll number " + attempts + "/" + 25 + " and trying again.");
              return getRandomTarget(attempts + 1, templateId, itemList);
            }
            logger.info("Failing roll of finding structure with templateID=" + templateId + " completely,  could not find any mission structure not in a village in " + 25 + " tries.");
            
            return -1L;
          }
          catch (NoSuchItemException localNoSuchItemException2) {}
        }
        logger.warning("WURMID was null for item with templateId=" + templateId);
        return -1L;
      }
      else
      {
        logger.info("Couldn't find any items with itemtemplate=" + templateId + " failing, the roll.");
        return -1L;
      }
    }
    return itemFound;
  }
  
  final int getNextBuildTarget(int difficulty)
  {
    difficulty = Math.min(5, difficulty);
    int start = difficulty * 3;
    int templateFound = -1;
    for (int x = start; x < 17; x++) {
      if (this.epicTargetItems[x] <= 0L)
      {
        templateFound = x;
        break;
      }
    }
    if (templateFound == -1) {
      for (int x = start; x > 0; x--) {
        if (this.epicTargetItems[x] <= 0L)
        {
          templateFound = x;
          break;
        }
      }
    }
    if (templateFound > -1)
    {
      if (templateFound < 3) {
        return 717;
      }
      if (templateFound < 6) {
        return 714;
      }
      if (templateFound < 9) {
        return 713;
      }
      if (templateFound < 12) {
        return 715;
      }
      if (templateFound < 15) {
        return 712;
      }
      return 716;
    }
    return -1;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\epic\EpicTargetItems.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */