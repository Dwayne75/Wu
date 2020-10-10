package com.wurmonline.server;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class WurmHarvestables
  implements TimeConstants, MiscConstants
{
  private static final Logger logger = Logger.getLogger(WurmHarvestables.class.getName());
  public static final int NONE_ID = 0;
  public static final int OLIVE_ID = 1;
  public static final int GRAPE_ID = 2;
  public static final int CHERRY_ID = 3;
  public static final int APPLE_ID = 4;
  public static final int LEMON_ID = 5;
  public static final int OLEANDER_ID = 6;
  public static final int CAMELLIA_ID = 7;
  public static final int LAVENDER_ID = 8;
  public static final int MAPLE_ID = 9;
  public static final int ROSE_ID = 10;
  public static final int CHESTNUT_ID = 11;
  public static final int WALNUT_ID = 12;
  public static final int PINE_ID = 13;
  public static final int HAZEL_ID = 14;
  public static final int HOPS_ID = 15;
  public static final int OAK_ID = 16;
  public static final int ORANGE_ID = 17;
  public static final int RASPBERRY_ID = 18;
  public static final int BLUEBERRY_ID = 19;
  public static final int LINGONBERRY_ID = 20;
  public static final int MAX_HARVEST_ID = 20;
  private static final String GET_CALENDAR_HARVEST_EVENTS = "SELECT * FROM CALENDAR WHERE type = 1";
  private static final String INSERT_CALENDAR_HARVEST_EVENT = "INSERT INTO CALENDAR (eventid, starttime, type) VALUES (?,?,1)";
  private static final String UPDATE_CALENDAR_HARVEST_EVENT = "UPDATE CALENDAR SET starttime = ? where eventid = ? and type = 1";
  private static final WurmHarvestables.Harvestable[] harvestables = new WurmHarvestables.Harvestable[21];
  public static long lastHarvestableCheck = 0L;
  public static final Random endRand = new Random();
  
  @Nullable
  public static WurmHarvestables.Harvestable getHarvestable(int id)
  {
    if (id == 0) {
      return null;
    }
    if ((id < 1) || (id > 20))
    {
      logger.severe("Invalid Harvest Id " + id);
      return null;
    }
    return harvestables[id];
  }
  
  public static int getMaxHarvestId()
  {
    return 20;
  }
  
  static long getLastHarvestableCheck()
  {
    return lastHarvestableCheck;
  }
  
  public static void setHarvestStart(int eventId, long newDate)
  {
    WurmHarvestables.Harvestable harvestable = getHarvestable(eventId);
    if (harvestable != null)
    {
      harvestable.setSeasonStart(newDate, true);
      dbUpdateHarvestEvent(eventId, newDate);
    }
  }
  
  private static void dbLoadHarvestStartTimes()
  {
    Connection dbcon = null;
    Statement stmt = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getItemDbCon();
      stmt = dbcon.createStatement();
      rs = stmt.executeQuery("SELECT * FROM CALENDAR WHERE type = 1");
      while (rs.next())
      {
        int lEventId = rs.getInt("eventid");
        
        long tStartTime = rs.getLong("starttime");
        
        boolean recalc = tStartTime > (WurmCalendar.getYearOffset() + 1) * 29030400L + (WurmCalendar.getStarfall() + 1) * 2419200L;
        
        long lStartTime = Math.max(WurmCalendar.getYearOffset() * 29030400L, tStartTime);
        if (logger.isLoggable(Level.FINEST)) {
          logger.finest("Loading harvest calendar event - Id: " + lEventId + ", start: " + lStartTime);
        }
        WurmHarvestables.Harvestable harvestable = getHarvestable(lEventId);
        if (harvestable != null)
        {
          if (recalc) {
            WurmHarvestables.Harvestable.access$500(harvestable, WurmCalendar.getYearOffset());
          } else {
            harvestable.setSeasonStart(lStartTime, false);
          }
        }
        else if (lEventId != 0) {
          logger.warning("Unknown harvest event in the Calendar: " + lEventId + ", start: " + lStartTime);
        }
      }
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to load harvest events from the calendar", ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(stmt, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private static void dbUpdateHarvestEvent(int aEventId, long aStartTime)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getItemDbCon();
      ps = dbcon.prepareStatement("UPDATE CALENDAR SET starttime = ? where eventid = ? and type = 1");
      ps.setLong(1, Math.max(0L, aStartTime));
      ps.setLong(2, aEventId);
      if (ps.executeUpdate() == 0)
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        ps = dbcon.prepareStatement("INSERT INTO CALENDAR (eventid, starttime, type) VALUES (?,?,1)");
        ps.setLong(1, aEventId);
        ps.setLong(2, aStartTime);
        ps.executeUpdate();
      }
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to update harvest event to calendar with event id " + aEventId + ", startTime: " + aStartTime, ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public static final void setStartTimes()
  {
    long start = System.nanoTime();
    boolean forceEnumLoad = WurmHarvestables.Harvestable.APPLE.isHarvestable();
    dbLoadHarvestStartTimes();
    for (WurmHarvestables.Harvestable harvestable : WurmHarvestables.Harvestable.values()) {
      if ((harvestable != WurmHarvestables.Harvestable.NONE) && (harvestable.getSeasonStart() > WurmCalendar.currentTime + 29030400L + 2419200L)) {
        WurmHarvestables.Harvestable.access$500(harvestable, WurmCalendar.getYearOffset());
      }
    }
    logGrowthStartDates();
    
    float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
    logger.log(Level.INFO, "Set harvest start dates. It took " + lElapsedTime + " millis.");
  }
  
  private static void logGrowthStartDates()
  {
    StringBuilder buf = new StringBuilder();
    buf.append("Current wurm time: ").append(WurmCalendar.currentTime).append(" - ").append(WurmCalendar.getTime());
    buf.append("\n" + WurmHarvestables.Harvestable.APPLE.getHarvestEvent());
    buf.append("\n" + WurmHarvestables.Harvestable.BLUEBERRY.getHarvestEvent());
    buf.append("\n" + WurmHarvestables.Harvestable.CAMELLIA.getHarvestEvent());
    buf.append("\n" + WurmHarvestables.Harvestable.CHERRY.getHarvestEvent());
    buf.append("\n" + WurmHarvestables.Harvestable.CHESTNUT.getHarvestEvent());
    buf.append("\n" + WurmHarvestables.Harvestable.GRAPE.getHarvestEvent());
    buf.append("\n" + WurmHarvestables.Harvestable.HAZEL.getHarvestEvent());
    buf.append("\n" + WurmHarvestables.Harvestable.HOPS.getHarvestEvent());
    buf.append("\n" + WurmHarvestables.Harvestable.LAVENDER.getHarvestEvent());
    buf.append("\n" + WurmHarvestables.Harvestable.LEMON.getHarvestEvent());
    buf.append("\n" + WurmHarvestables.Harvestable.LINGONBERRY.getHarvestEvent());
    buf.append("\n" + WurmHarvestables.Harvestable.MAPLE.getHarvestEvent());
    buf.append("\n" + WurmHarvestables.Harvestable.OAK.getHarvestEvent());
    buf.append("\n" + WurmHarvestables.Harvestable.OLEANDER.getHarvestEvent());
    buf.append("\n" + WurmHarvestables.Harvestable.OLIVE.getHarvestEvent());
    buf.append("\n" + WurmHarvestables.Harvestable.ORANGE.getHarvestEvent());
    buf.append("\n" + WurmHarvestables.Harvestable.PINE.getHarvestEvent());
    buf.append("\n" + WurmHarvestables.Harvestable.RASPBERRY.getHarvestEvent());
    buf.append("\n" + WurmHarvestables.Harvestable.ROSE.getHarvestEvent());
    buf.append("\n" + WurmHarvestables.Harvestable.WALNUT.getHarvestEvent());
    logger.log(Level.INFO, buf.toString());
  }
  
  public static void checkHarvestables(long currentTime)
  {
    boolean haveDatesChanged = false;
    if (currentTime < lastHarvestableCheck + 3600L) {
      return;
    }
    lastHarvestableCheck = WurmCalendar.currentTime;
    for (WurmHarvestables.Harvestable harvestable : harvestables)
    {
      haveDatesChanged = (haveDatesChanged) || (WurmHarvestables.Harvestable.access$600(harvestable));
      haveDatesChanged = (haveDatesChanged) || (WurmHarvestables.Harvestable.access$700(harvestable));
    }
    if (haveDatesChanged) {
      logGrowthStartDates();
    }
  }
  
  private static void setHarvestable(byte normalType, byte myceliumType, boolean harvestable)
  {
    int min = 1;
    int ms = Constants.meshSize;
    int max = (1 << ms) - 1;
    for (int x = 1; x < max; x++) {
      for (int y = 1; y < max; y++)
      {
        int encodedTile = Server.surfaceMesh.getTile(x, y);
        byte tileType = Tiles.decodeType(encodedTile);
        if ((tileType == normalType) || (tileType == myceliumType))
        {
          short newHeight = Tiles.decodeHeight(encodedTile);
          byte tileData = Tiles.decodeData(encodedTile);
          if (harvestable) {
            tileData = (byte)(tileData | 0x8);
          } else {
            tileData = (byte)(tileData & 0xF7);
          }
          Server.setSurfaceTile(x, y, newHeight, tileType, tileData);
          Players.getInstance().sendChangedTile(x, y, true, false);
        }
      }
    }
  }
  
  private static void setHarvestable(int itemType, boolean harvestable)
  {
    for (Item item : ) {
      if (item.getTemplateId() == itemType) {
        item.setHarvestable(harvestable);
      }
    }
  }
  
  public static WurmHarvestables.Harvestable[] getHarvestables()
  {
    return harvestables;
  }
  
  public static int getHarvestableIdFromTile(byte tileType)
  {
    for (WurmHarvestables.Harvestable harvestable : harvestables) {
      if ((WurmHarvestables.Harvestable.access$800(harvestable) == tileType) || (WurmHarvestables.Harvestable.access$900(harvestable) == tileType)) {
        return WurmHarvestables.Harvestable.access$1000(harvestable);
      }
    }
    return -1;
  }
  
  public static int getHarvestableIdFromTrellis(int trellis)
  {
    for (WurmHarvestables.Harvestable harvestable : harvestables) {
      if (WurmHarvestables.Harvestable.access$1100(harvestable) == trellis) {
        return WurmHarvestables.Harvestable.access$1000(harvestable);
      }
    }
    return -1;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\WurmHarvestables.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */