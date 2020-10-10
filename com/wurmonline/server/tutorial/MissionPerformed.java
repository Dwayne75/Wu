package com.wurmonline.server.tutorial;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.shared.constants.CounterTypes;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MissionPerformed
  implements CounterTypes
{
  private static Logger logger = Logger.getLogger(MissionPerformed.class.getName());
  private static final Map<Long, MissionPerformer> missionsPerformers = new HashMap();
  private static final String LOADALLMISSIONSPERFORMER = "SELECT * FROM MISSIONSPERFORMED";
  private static final String ADDMISSIONSPERFORMED = "INSERT INTO MISSIONSPERFORMED (PERFORMER,MISSION,STATE,STARTTIME) VALUES(?,?,?,?)";
  private static final String DELETEALLMISSIONSPERFORMER = "DELETE FROM MISSIONSPERFORMED WHERE PERFORMER=?";
  private static final String UPDATESTATE = "UPDATE MISSIONSPERFORMED SET STATE=? WHERE MISSION=? AND PERFORMER=?";
  private static final String SETINACTIVATED = "UPDATE MISSIONSPERFORMED SET INACTIVE=? WHERE MISSION=? AND PERFORMER=?";
  private static final String RESTARTMISSION = "UPDATE MISSIONSPERFORMED SET STARTTIME=?,FINISHEDDATE=? WHERE MISSION=? AND PERFORMER=?";
  private static final String UPDATEFINISHEDDATE = "UPDATE MISSIONSPERFORMED SET FINISHEDDATE=?, ENDTIME=? WHERE MISSION=? AND PERFORMER=?";
  public static final float FINISHED = 100.0F;
  public static final float NOTSTARTED = 0.0F;
  public static final float STARTED = 1.0F;
  public static final float FAILED = -1.0F;
  public static final float SOME_COMPLETED = 33.0F;
  private final int mission;
  private float state = 0.0F;
  private long startTime = 0L;
  private long endTime = 0L;
  private String endDate = "";
  private boolean inactive = false;
  private final long wurmid;
  private final MissionPerformer performer;
  private static long tempMissionPerformedCounter = 0L;
  
  static
  {
    try
    {
      loadAllMissionsPerformed();
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, "Problems loading Missions Performed", ex);
    }
  }
  
  public MissionPerformed(int missionId, MissionPerformer perf)
  {
    this.mission = missionId;
    this.wurmid = generateWurmId(this.mission);
    this.performer = perf;
  }
  
  private static long generateWurmId(int mission)
  {
    tempMissionPerformedCounter += 1L;
    return BigInteger.valueOf(tempMissionPerformedCounter).shiftLeft(24).longValue() + (mission << 8) + 22L;
  }
  
  public static int decodeMissionId(long wurmId)
  {
    return (int)(wurmId >> 8 & 0xFFFFFFFFFFFFFFFF);
  }
  
  public long getWurmId()
  {
    return this.wurmid;
  }
  
  public int getMissionId()
  {
    return this.mission;
  }
  
  public Mission getMission()
  {
    return Missions.getMissionWithId(this.mission);
  }
  
  public float getState()
  {
    return this.state;
  }
  
  public boolean isInactivated()
  {
    return this.inactive;
  }
  
  public boolean isCompleted()
  {
    return this.state == 100.0F;
  }
  
  public boolean isFailed()
  {
    return this.state == -1.0F;
  }
  
  public boolean isStarted()
  {
    return this.state >= 1.0F;
  }
  
  public long getStartTimeMillis()
  {
    return this.startTime;
  }
  
  protected String getStartDate()
  {
    return DateFormat.getDateInstance(1).format(new Timestamp(this.startTime));
  }
  
  protected String getLastTimeToFinish(int maxSecondsToFinish)
  {
    return DateFormat.getDateInstance(1).format(new Timestamp(this.startTime + maxSecondsToFinish * 1000));
  }
  
  protected long getFinishTimeAsLong(int maxSecondsToFinish)
  {
    return this.startTime + maxSecondsToFinish * 1000;
  }
  
  protected long getStartTime()
  {
    return this.startTime;
  }
  
  String getEndDate()
  {
    return this.endDate;
  }
  
  long getEndTime()
  {
    return this.endTime;
  }
  
  public static MissionPerformer getMissionPerformer(long id)
  {
    return (MissionPerformer)missionsPerformers.get(Long.valueOf(id));
  }
  
  public static MissionPerformer[] getAllPerformers()
  {
    return (MissionPerformer[])missionsPerformers.values().toArray(new MissionPerformer[missionsPerformers.size()]);
  }
  
  public void setInactive(boolean inactivate)
  {
    if (this.inactive != inactivate)
    {
      this.inactive = inactivate;
      
      PreparedStatement ps = null;
      try
      {
        Connection dbcon = DbConnector.getPlayerDbCon();
        ps = dbcon.prepareStatement("UPDATE MISSIONSPERFORMED SET INACTIVE=? WHERE MISSION=? AND PERFORMER=?");
        ps.setBoolean(1, this.inactive);
        ps.setLong(2, this.performer.getWurmId());
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, sqx.getMessage(), sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
      }
      sendUpdate();
    }
  }
  
  private void sendUpdate()
  {
    if (this.performer != null) {
      this.performer.sendUpdatePerformer(this);
    }
  }
  
  public static void deleteMissionPerformer(long id)
  {
    missionsPerformers.remove(Long.valueOf(id));
    PreparedStatement ps = null;
    try
    {
      Connection dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("DELETE FROM MISSIONSPERFORMED WHERE PERFORMER=?");
      ps.setLong(1, id);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
    }
  }
  
  public boolean setState(float newState, long aPerformer)
  {
    if (this.state != newState)
    {
      this.state = newState;
      if (this.state > 100.0F) {
        this.state = 100.0F;
      }
      if (this.state < -1.0F) {
        this.state = -1.0F;
      }
      PreparedStatement ps = null;
      try
      {
        Connection dbcon = DbConnector.getPlayerDbCon();
        ps = dbcon.prepareStatement("UPDATE MISSIONSPERFORMED SET STATE=? WHERE MISSION=? AND PERFORMER=?");
        ps.setFloat(1, this.state);
        ps.setInt(2, this.mission);
        ps.setLong(3, aPerformer);
        
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, sqx.getMessage(), sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
      }
      if ((this.state >= 100.0F) || (this.state <= -1.0F)) {
        setFinishDate(DateFormat.getDateInstance(1).format(new Timestamp(System.currentTimeMillis())), aPerformer);
      }
      if (this.state == 1.0F) {
        restartMission(this.performer.getWurmId());
      }
      sendUpdate();
    }
    return (this.state >= 100.0F) || (this.state <= -1.0F);
  }
  
  private void setFinishDate(String date, long aPerformer)
  {
    this.endDate = date;
    
    PreparedStatement ps = null;
    try
    {
      Connection dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("UPDATE MISSIONSPERFORMED SET FINISHEDDATE=?, ENDTIME=? WHERE MISSION=? AND PERFORMER=?");
      ps.setString(1, date);
      this.endTime = System.currentTimeMillis();
      ps.setLong(2, this.endTime);
      
      ps.setInt(3, this.mission);
      ps.setLong(4, aPerformer);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
    }
  }
  
  public static MissionPerformer startNewMission(int mission, long performerId, float state)
  {
    MissionPerformer mp = (MissionPerformer)missionsPerformers.get(Long.valueOf(performerId));
    if (mp == null)
    {
      mp = new MissionPerformer(performerId);
      missionsPerformers.put(Long.valueOf(performerId), mp);
    }
    MissionPerformed mpf = new MissionPerformed(mission, mp);
    
    mpf.state = state;
    mpf.startTime = System.currentTimeMillis();
    mp.addMissionPerformed(mpf);
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("INSERT INTO MISSIONSPERFORMED (PERFORMER,MISSION,STATE,STARTTIME) VALUES(?,?,?,?)");
      ps.setLong(1, performerId);
      ps.setInt(2, mission);
      ps.setFloat(3, state);
      ps.setLong(4, mpf.startTime);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
    if (state == 100.0F) {
      mpf.setFinishDate(DateFormat.getDateInstance(1).format(new Timestamp(System.currentTimeMillis())), performerId);
    }
    mpf.sendUpdate();
    return mp;
  }
  
  private void restartMission(long aPerformer)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      this.startTime = System.currentTimeMillis();
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("UPDATE MISSIONSPERFORMED SET STARTTIME=?,FINISHEDDATE=? WHERE MISSION=? AND PERFORMER=?");
      ps.setLong(1, this.startTime);
      ps.setLong(2, this.endTime);
      
      ps.setInt(3, this.mission);
      ps.setLong(4, aPerformer);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private static void loadAllMissionsPerformed()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM MISSIONSPERFORMED");
      rs = ps.executeQuery();
      while (rs.next())
      {
        long performer = rs.getLong("PERFORMER");
        MissionPerformer mp = (MissionPerformer)missionsPerformers.get(Long.valueOf(performer));
        if (mp == null)
        {
          mp = new MissionPerformer(performer);
          missionsPerformers.put(Long.valueOf(performer), mp);
        }
        MissionPerformed mpf = new MissionPerformed(rs.getInt("MISSION"), mp);
        
        mpf.state = rs.getInt("STATE");
        mpf.startTime = rs.getLong("STARTTIME");
        mpf.endDate = rs.getString("FINISHEDDATE");
        mpf.endTime = rs.getLong("ENDTIME");
        mp.addMissionPerformed(mpf);
      }
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\tutorial\MissionPerformed.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */