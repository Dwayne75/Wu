package com.wurmonline.server.epic;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.questions.SimplePopup;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MissionHelper
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(MissionHelper.class.getName());
  private static final String LOAD_ALL_MISSION_HELPERS = "SELECT * FROM MISSIONHELPERS";
  private static final String INSERT_MISSION_HELPER = DbConnector.isUseSqlite() ? "INSERT OR IGNORE INTO MISSIONHELPERS (NUMS, MISSIONID, PLAYERID) VALUES(?,?,?)" : "INSERT IGNORE INTO MISSIONHELPERS (NUMS, MISSIONID, PLAYERID) VALUES(?,?,?)";
  private static final String MOVE_MISSION_HELPER = "UPDATE MISSIONHELPERS SET MISSIONID=? WHERE MISSIONID=?";
  private static final String DELETE_MISSION_HELPER = "DELETE FROM MISSIONHELPERS WHERE MISSIONID=?";
  private static final String UPDATE_MISSION_HELPER = "UPDATE MISSIONHELPERS SET NUMS=? WHERE MISSIONID=? AND PLAYERID=?";
  private static final Map<Long, MissionHelper> MISSION_HELPERS = new ConcurrentHashMap();
  private static boolean INITIALIZED = false;
  private final Map<Long, Integer> missionsHelped = new ConcurrentHashMap();
  private final long playerId;
  
  public MissionHelper(long playerid)
  {
    this.playerId = playerid;
    addHelper(this);
  }
  
  public final void increaseHelps(long missionId)
  {
    setHelps(missionId, getHelps(missionId) + 1);
  }
  
  public final void increaseHelps(long missionId, int nums)
  {
    setHelps(missionId, getHelps(missionId) + nums);
  }
  
  public static final void addHelper(MissionHelper helper)
  {
    MISSION_HELPERS.put(Long.valueOf(helper.getPlayerId()), helper);
  }
  
  private final void setHelpsAtLoad(long missionId, int nums)
  {
    this.missionsHelped.put(Long.valueOf(missionId), Integer.valueOf(nums));
  }
  
  public static final Map<Long, MissionHelper> getHelpers()
  {
    return MISSION_HELPERS;
  }
  
  public static final void loadAll()
  {
    if (!INITIALIZED)
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      try
      {
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("SELECT * FROM MISSIONHELPERS");
        rs = ps.executeQuery();
        while (rs.next())
        {
          long helperId = rs.getLong("PLAYERID");
          MissionHelper helper = (MissionHelper)MISSION_HELPERS.get(Long.valueOf(helperId));
          if (helper == null) {
            helper = new MissionHelper(helperId);
          }
          helper.setHelpsAtLoad(rs.getLong("MISSIONID"), rs.getInt("NUMS"));
        }
        INITIALIZED = true;
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to load epic item helpers.", sqx);
        INITIALIZED = false;
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  public static final void printHelpForMission(long missionId, String missionName, Creature performer)
  {
    float total = 0.0F;
    if (!INITIALIZED) {
      loadAll();
    }
    for (MissionHelper helper : MISSION_HELPERS.values()) {
      total += helper.getHelps(missionId);
    }
    if (total > 0.0F)
    {
      SimplePopup sp = new SimplePopup(performer, "Plaque on " + missionName, "These helped:", missionId, total);
      sp.sendQuestion();
    }
  }
  
  public static final void addKarmaForItem(long itemId)
  {
    for (MissionHelper helper : MISSION_HELPERS.values())
    {
      int i = helper.getHelps(itemId);
      if (i > 10) {
        try
        {
          Player p = Players.getInstance().getPlayer(helper.getPlayerId());
          p.modifyKarma(i / 10);
        }
        catch (NoSuchPlayerException nsp)
        {
          PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(helper.getPlayerId());
          pinf.setKarma(pinf.getKarma() + i / 10);
        }
      }
    }
  }
  
  public static final MissionHelper getOrCreateHelper(long playerId)
  {
    MissionHelper helper = (MissionHelper)MISSION_HELPERS.get(Long.valueOf(playerId));
    if (helper == null) {
      helper = new MissionHelper(playerId);
    }
    return helper;
  }
  
  public final long getPlayerId()
  {
    return this.playerId;
  }
  
  public final int getHelps(long missionId)
  {
    Integer nums = (Integer)this.missionsHelped.get(Long.valueOf(missionId));
    if (nums == null) {
      return 0;
    }
    return nums.intValue();
  }
  
  private final void moveLocalMissionId(long oldMissionId, long newMissionId)
  {
    int oldHelps = getHelps(oldMissionId);
    if (oldHelps > 0)
    {
      this.missionsHelped.remove(Long.valueOf(oldMissionId));
      this.missionsHelped.put(Long.valueOf(newMissionId), Integer.valueOf(oldHelps));
    }
  }
  
  private final void removeMissionId(long missionId)
  {
    this.missionsHelped.remove(Long.valueOf(missionId));
  }
  
  public static final void moveGlobalMissionId(long oldmissionId, long newMissionId)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("UPDATE MISSIONHELPERS SET MISSIONID=? WHERE MISSIONID=?");
      ps.setLong(1, newMissionId);
      ps.setLong(2, oldmissionId);
      ps.executeUpdate();
      for (MissionHelper h : MISSION_HELPERS.values()) {
        h.moveLocalMissionId(oldmissionId, newMissionId);
      }
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to move epic mission helps from mission " + oldmissionId + ", to" + newMissionId, sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public static final void deleteMissionId(long missionId)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("DELETE FROM MISSIONHELPERS WHERE MISSIONID=?");
      ps.setLong(1, missionId);
      ps.executeUpdate();
      for (MissionHelper h : MISSION_HELPERS.values()) {
        h.removeMissionId(missionId);
      }
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to delete epic mission helps for mission " + missionId, sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public final void setHelps(long missionId, int helps)
  {
    int oldHelps = getHelps(missionId);
    if (oldHelps != helps)
    {
      this.missionsHelped.put(Long.valueOf(missionId), Integer.valueOf(helps));
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        dbcon = DbConnector.getZonesDbCon();
        if (oldHelps == 0) {
          ps = dbcon.prepareStatement(INSERT_MISSION_HELPER);
        } else {
          ps = dbcon.prepareStatement("UPDATE MISSIONHELPERS SET NUMS=? WHERE MISSIONID=? AND PLAYERID=?");
        }
        ps.setInt(1, helps);
        ps.setLong(2, missionId);
        ps.setLong(3, this.playerId);
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to save epic item helps " + helps + " for mission " + missionId + ", pid=" + this.playerId, sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\epic\MissionHelper.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */