package com.wurmonline.server.players;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.statistics.ChallengePointEnum.ChallengePoint;
import com.wurmonline.server.statistics.ChallengeSummary;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerKills
{
  private final long wurmid;
  private static final Logger logger = Logger.getLogger(PlayerKills.class.getName());
  private static final String GET_KILLS = "SELECT * FROM KILLS WHERE WURMID=?";
  private final Map<Long, PlayerKill> kills = new HashMap();
  
  public PlayerKills(long _wurmId)
  {
    this.wurmid = _wurmId;
    load();
  }
  
  private void load()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM KILLS WHERE WURMID=?");
      ps.setLong(1, this.wurmid);
      rs = ps.executeQuery();
      while (rs.next())
      {
        Long vid = new Long(rs.getLong("VICTIM"));
        PlayerKill pk = (PlayerKill)this.kills.get(vid);
        if (pk != null) {
          pk.addKill(rs.getLong("KILLTIME"), rs.getString("VICTIMNAME"), true);
        } else {
          this.kills.put(vid, new PlayerKill(vid.longValue(), rs.getLong("KILLTIME"), rs.getString("VICTIMNAME"), 1));
        }
      }
    }
    catch (SQLException ex)
    {
      logger.log(Level.INFO, "Failed to load kills for " + this.wurmid, ex);
    }
    catch (Exception ex)
    {
      logger.log(Level.INFO, "Failed to load kills for " + this.wurmid, ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public long getLastKill(long victimId)
  {
    PlayerKill pk = (PlayerKill)this.kills.get(Long.valueOf(victimId));
    if (pk != null) {
      return pk.getLastKill();
    }
    return 0L;
  }
  
  public long getNumKills(long victimId)
  {
    PlayerKill pk = (PlayerKill)this.kills.get(Long.valueOf(victimId));
    if (pk != null) {
      return pk.getNumKills();
    }
    return 0L;
  }
  
  public void addKill(long victimId, String victimName)
  {
    Long vid = new Long(victimId);
    PlayerKill pk = (PlayerKill)this.kills.get(vid);
    if (pk != null)
    {
      pk.kill(this.wurmid, victimName);
    }
    else
    {
      pk = new PlayerKill(victimId, System.currentTimeMillis(), victimName, 0);
      if (Servers.localServer.isChallengeServer())
      {
        PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(this.wurmid);
        if (pinf != null)
        {
          Achievements ach = Achievements.getAchievementObject(this.wurmid);
          if ((ach != null) && (ach.getAchievement(369) != null))
          {
            ChallengeSummary.addToScore(pinf, ChallengePointEnum.ChallengePoint.PLAYERKILLS.getEnumtype(), 1.0F);
            ChallengeSummary.addToScore(pinf, ChallengePointEnum.ChallengePoint.OVERALL.getEnumtype(), 10.0F);
          }
        }
      }
      pk.kill(this.wurmid, victimName);
      this.kills.put(vid, pk);
    }
  }
  
  public boolean isOverKilling(long victimId)
  {
    Long vid = new Long(victimId);
    PlayerKill pk = (PlayerKill)this.kills.get(vid);
    if (pk != null) {
      return pk.isOverkilling();
    }
    return false;
  }
  
  public int getNumberOfKills()
  {
    return this.kills.size();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\players\PlayerKills.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */