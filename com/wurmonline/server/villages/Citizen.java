package com.wurmonline.server.villages;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.shared.constants.CounterTypes;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Citizen
  implements MiscConstants, TimeConstants, Comparable<Citizen>, CounterTypes
{
  public final long wurmId;
  final String name;
  private static final Logger logger = Logger.getLogger(Citizen.class.getName());
  private static final String DELETE = "DELETE FROM CITIZENS WHERE WURMID=?";
  VillageRole role = null;
  long voteDate = -10L;
  long votedFor = -10L;
  
  Citizen(long aWurmId, String aName, VillageRole aRole, long aVotedate, long aVotedfor)
  {
    this.wurmId = aWurmId;
    this.name = aName;
    this.role = aRole;
    this.voteDate = aVotedate;
    this.votedFor = aVotedfor;
  }
  
  public final VillageRole getRole()
  {
    return this.role;
  }
  
  public final String getName()
  {
    return this.name;
  }
  
  public final long getId()
  {
    return this.wurmId;
  }
  
  public final boolean isPlayer()
  {
    return WurmId.getType(this.wurmId) == 0;
  }
  
  public final boolean hasVoted()
  {
    return System.currentTimeMillis() - this.voteDate < 604800000L;
  }
  
  public final long getVoteDate()
  {
    return this.voteDate;
  }
  
  public final long getVotedFor()
  {
    return this.votedFor;
  }
  
  public abstract void setRole(VillageRole paramVillageRole)
    throws IOException;
  
  abstract void setVoteDate(long paramLong)
    throws IOException;
  
  abstract void setVotedFor(long paramLong)
    throws IOException;
  
  static final void delete(long wid)
    throws IOException
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("DELETE FROM CITIZENS WHERE WURMID=?");
      ps.setLong(1, wid);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to delete citizen " + wid + ": " + sqx.getMessage(), sqx);
      throw new IOException(sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public int compareTo(Citizen aCitizen)
  {
    return getName().compareTo(aCitizen.getName());
  }
  
  abstract void create(Creature paramCreature, int paramInt)
    throws IOException;
  
  public String toString()
  {
    return "Citizen [wurmId=" + this.wurmId + ", name=" + this.name + ", role=" + this.role + "]";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\villages\Citizen.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */