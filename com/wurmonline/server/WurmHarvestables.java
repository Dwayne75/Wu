package com.wurmonline.server;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
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
  private static final Harvestable[] harvestables = new Harvestable[21];
  public static long lastHarvestableCheck = 0L;
  public static final Random endRand = new Random();
  
  public static enum Harvestable
  {
    NONE(0, "none", 0, 0, (byte)-1, (byte)-1, -1, "", ""),  OLIVE(1, "olive", 7, 0, (byte)108, (byte)122, -1, "olive", "an olive"),  GRAPE(2, "grape", 8, 0, (byte)-111, (byte)-105, 920, "grape", "a grape"),  CHERRY(3, "cherry", 6, 0, (byte)109, (byte)123, -1, "cherry", "a cherry"),  APPLE(4, "apple", 8, 2, (byte)106, (byte)120, -1, "apple", "an apple"),  LEMON(5, "lemon", 8, 1, (byte)107, (byte)121, -1, "lemon", "a lemon"),  OLEANDER(6, "oleander", 3, 1, (byte)-109, (byte)-103, -1, "oleander leaf", "an oleander leaf"),  CAMELLIA(7, "camellia", 3, 3, (byte)-110, (byte)-104, -1, "camellia leaf", "a camellia leaf"),  LAVENDER(8, "lavender", 4, 1, (byte)-114, (byte)-108, -1, "lavender flower", "a lavender flower"),  MAPLE(9, "maple", 4, 3, (byte)105, (byte)119, -1, "maple sap", "some maple sap"),  ROSE(10, "rose", 4, 2, (byte)-113, (byte)-107, 1018, "rose flower", "a rose flower"),  CHESTNUT(11, "chestnut", 8, 3, (byte)110, (byte)124, -1, "chestnut", "a chestnut"),  WALNUT(12, "walnut", 9, 1, (byte)111, (byte)125, -1, "walnut", "a walnut"),  PINE(13, "pine", 0, 0, (byte)101, (byte)115, -1, "pinenut", "a pinenut"),  HAZEL(14, "hazel", 9, 2, (byte)-96, (byte)-95, -1, "hazelnut", "a hazelnut"),  HOPS(15, "hops", 7, 2, (byte)-1, (byte)-1, 1274, "hops", "some hops"),  OAK(16, "oak", 5, 1, (byte)102, (byte)116, -1, "acorn", "an acorn"),  ORANGE(17, "orange", 7, 3, (byte)-93, (byte)-92, -1, "orange", "an orange"),  RASPBERRY(18, "raspberry", 9, 0, (byte)-90, (byte)-89, -1, "raspberry", "a raspberry"),  BLUEBERRY(19, "blueberry", 7, 1, (byte)-87, (byte)-86, -1, "blueberry", "a blueberry"),  LINGONBERRY(20, "lingonberry", 9, 3, (byte)-84, (byte)-84, -1, "lingonberry", "a lingonberry");
    
    private final int harvestableId;
    private final String name;
    private final int month;
    private final int week;
    private final byte tileNormal;
    private final byte tileMycelium;
    private final int trellis;
    private final int reportDifficulty;
    private final String fruit;
    private final String fruitWithGenus;
    private long seasonStart = Long.MAX_VALUE;
    private long seasonEnd = Long.MAX_VALUE;
    private boolean isHarvestable = false;
    
    private Harvestable(int id, String name, int month, int week, byte tileNormal, byte tileMycelium, int trellis, String fruit, String fruitWithGenus)
    {
      this.harvestableId = id;
      this.name = name;
      this.month = month;
      this.week = week;
      this.tileNormal = tileNormal;
      this.tileMycelium = tileMycelium;
      this.trellis = trellis;
      this.fruit = fruit;
      this.fruitWithGenus = fruitWithGenus;
      if (tileNormal > 0)
      {
        Tiles.Tile tile = Tiles.getTile(tileNormal);
        if (tile != null) {
          this.reportDifficulty = tile.getWoodDificulity();
        } else {
          this.reportDifficulty = 2;
        }
      }
      else
      {
        this.reportDifficulty = 2;
      }
      if ((id < 0) || (id > 20)) {
        WurmHarvestables.logger.severe("Invalid Harvest Id " + id);
      } else {
        WurmHarvestables.harvestables[id] = this;
      }
    }
    
    public int getHarvestableId()
    {
      return this.harvestableId;
    }
    
    public String getName()
    {
      return this.name;
    }
    
    public String getHarvestableWithDates()
    {
      if (this.isHarvestable) {
        return this.name + ", season ends in " + WurmCalendar.getDaysFrom(getSeasonEnd());
      }
      return "";
    }
    
    public String getState()
    {
      if (isHarvestable()) {
        return "Harvestable";
      }
      if (isAlmostRipe()) {
        return "Almost Ripe";
      }
      return "";
    }
    
    public String getFruit()
    {
      return this.fruit;
    }
    
    public String getFruitWithGenus()
    {
      return this.fruitWithGenus;
    }
    
    public long getSeasonStart()
    {
      return this.seasonStart;
    }
    
    public long getSeasonEnd()
    {
      return this.seasonEnd;
    }
    
    public void setSeasonStart(long newGrowth, boolean updateHarvestables)
    {
      this.seasonStart = newGrowth;
      boolean harvestable = WurmCalendar.getCurrentTime() > this.seasonStart;
      if (harvestable != this.isHarvestable)
      {
        this.isHarvestable = harvestable;
        if (updateHarvestables) {
          updateHarvestables(harvestable);
        }
      }
      WurmHarvestables.endRand.setSeed(this.seasonStart);
      
      int diff = Math.min(7, this.reportDifficulty);
      int adjust = WurmHarvestables.endRand.nextInt((int)(diff * 86400L));
      this.seasonEnd = (getSeasonStart() + 2419200L - adjust);
    }
    
    public boolean isHarvestable()
    {
      return this.isHarvestable;
    }
    
    private void calcHarvestStart(int yearOffset)
    {
      long rDay = 259200L + Server.rand.nextInt(604800) + Server.rand.nextInt(604800) - Server.rand.nextInt(604800) - Server.rand.nextInt(604800);
      long startWeek = yearOffset * 29030400L + this.month * 2419200L + this.week * 604800L;
      
      setSeasonStart(startWeek + rDay, true);
      WurmHarvestables.dbUpdateHarvestEvent(this.harvestableId, this.seasonStart);
    }
    
    public long getDefaultSeasonStart()
    {
      int yearOffset = (int)(this.seasonStart / 29030400L);
      return yearOffset * 29030400L + this.month * 2419200L + this.week * 604800L;
    }
    
    public long getDefaultSeasonEnd()
    {
      return getDefaultSeasonStart() + 2419200L;
    }
    
    private boolean isHarvestOver()
    {
      if (this.harvestableId == 0) {
        return false;
      }
      if (WurmHarvestables.lastHarvestableCheck > getSeasonEnd())
      {
        calcHarvestStart(WurmCalendar.getYearOffset() + 1);
        
        updateHarvestables(false);
        return true;
      }
      return false;
    }
    
    private boolean hasSeasonStarted()
    {
      if (this.harvestableId == 0) {
        return false;
      }
      if ((!this.isHarvestable) && (WurmHarvestables.lastHarvestableCheck > this.seasonStart))
      {
        updateHarvestables(true);
        return true;
      }
      return false;
    }
    
    private void updateHarvestables(boolean harvestable)
    {
      this.isHarvestable = harvestable;
      if (this.tileNormal != -1) {
        WurmHarvestables.setHarvestable(this.tileNormal, this.tileMycelium, this.isHarvestable);
      }
      if (this.trellis > 0) {
        WurmHarvestables.setHarvestable(this.trellis, this.isHarvestable);
      }
    }
    
    public String getHarvestEvent()
    {
      return "start " + this.name + " season: " + (this.isHarvestable ? "(harvestable) " : "") + this.seasonStart + " - " + WurmCalendar.getTimeFor(this.seasonStart);
    }
    
    public boolean isAlmostRipe()
    {
      return (WurmCalendar.currentTime >= getSeasonStart() - 2419200L) && 
        (WurmCalendar.currentTime < getSeasonStart());
    }
    
    public int getReportDifficulty()
    {
      return this.reportDifficulty;
    }
    
    public boolean isSap()
    {
      return this.harvestableId == 9;
    }
    
    public boolean isFlower()
    {
      return (this.harvestableId == 8) || (this.harvestableId == 10);
    }
    
    public boolean isLeaf()
    {
      return (this.harvestableId == 7) || (this.harvestableId == 6);
    }
    
    public boolean isBerry()
    {
      return (this.harvestableId == 18) || (this.harvestableId == 19) || (this.harvestableId == 20);
    }
    
    public boolean isNut()
    {
      return (this.harvestableId == 11) || (this.harvestableId == 12) || (this.harvestableId == 13) || (this.harvestableId == 14);
    }
    
    public boolean isFruit()
    {
      return (this.harvestableId == 1) || (this.harvestableId == 2) || (this.harvestableId == 3) || (this.harvestableId == 4) || (this.harvestableId == 5) || (this.harvestableId == 17);
    }
    
    public boolean isHops()
    {
      return this.harvestableId == 15;
    }
    
    public boolean isAcorn()
    {
      return this.harvestableId == 16;
    }
  }
  
  @Nullable
  public static Harvestable getHarvestable(int id)
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
    Harvestable harvestable = getHarvestable(eventId);
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
        Harvestable harvestable = getHarvestable(lEventId);
        if (harvestable != null)
        {
          if (recalc) {
            harvestable.calcHarvestStart(WurmCalendar.getYearOffset());
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
    boolean forceEnumLoad = Harvestable.APPLE.isHarvestable();
    dbLoadHarvestStartTimes();
    for (Harvestable harvestable : Harvestable.values()) {
      if ((harvestable != Harvestable.NONE) && (harvestable.getSeasonStart() > WurmCalendar.currentTime + 29030400L + 2419200L)) {
        harvestable.calcHarvestStart(WurmCalendar.getYearOffset());
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
    buf.append("\n" + Harvestable.APPLE.getHarvestEvent());
    buf.append("\n" + Harvestable.BLUEBERRY.getHarvestEvent());
    buf.append("\n" + Harvestable.CAMELLIA.getHarvestEvent());
    buf.append("\n" + Harvestable.CHERRY.getHarvestEvent());
    buf.append("\n" + Harvestable.CHESTNUT.getHarvestEvent());
    buf.append("\n" + Harvestable.GRAPE.getHarvestEvent());
    buf.append("\n" + Harvestable.HAZEL.getHarvestEvent());
    buf.append("\n" + Harvestable.HOPS.getHarvestEvent());
    buf.append("\n" + Harvestable.LAVENDER.getHarvestEvent());
    buf.append("\n" + Harvestable.LEMON.getHarvestEvent());
    buf.append("\n" + Harvestable.LINGONBERRY.getHarvestEvent());
    buf.append("\n" + Harvestable.MAPLE.getHarvestEvent());
    buf.append("\n" + Harvestable.OAK.getHarvestEvent());
    buf.append("\n" + Harvestable.OLEANDER.getHarvestEvent());
    buf.append("\n" + Harvestable.OLIVE.getHarvestEvent());
    buf.append("\n" + Harvestable.ORANGE.getHarvestEvent());
    buf.append("\n" + Harvestable.PINE.getHarvestEvent());
    buf.append("\n" + Harvestable.RASPBERRY.getHarvestEvent());
    buf.append("\n" + Harvestable.ROSE.getHarvestEvent());
    buf.append("\n" + Harvestable.WALNUT.getHarvestEvent());
    logger.log(Level.INFO, buf.toString());
  }
  
  public static void checkHarvestables(long currentTime)
  {
    boolean haveDatesChanged = false;
    if (currentTime < lastHarvestableCheck + 3600L) {
      return;
    }
    lastHarvestableCheck = WurmCalendar.currentTime;
    for (Harvestable harvestable : harvestables)
    {
      haveDatesChanged = (haveDatesChanged) || (harvestable.isHarvestOver());
      haveDatesChanged = (haveDatesChanged) || (harvestable.hasSeasonStarted());
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
  
  public static Harvestable[] getHarvestables()
  {
    return harvestables;
  }
  
  public static int getHarvestableIdFromTile(byte tileType)
  {
    for (Harvestable harvestable : harvestables) {
      if ((harvestable.tileNormal == tileType) || (harvestable.tileMycelium == tileType)) {
        return harvestable.harvestableId;
      }
    }
    return -1;
  }
  
  public static int getHarvestableIdFromTrellis(int trellis)
  {
    for (Harvestable harvestable : harvestables) {
      if (harvestable.trellis == trellis) {
        return harvestable.harvestableId;
      }
    }
    return -1;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\WurmHarvestables.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */