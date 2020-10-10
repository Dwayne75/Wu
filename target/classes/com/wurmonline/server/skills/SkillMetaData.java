package com.wurmonline.server.skills;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SkillMetaData
{
  private final long id;
  private final int number;
  private double knowledge;
  private double minvalue;
  private final long owner;
  private final long lastused;
  private static final String CREATE_SKILL = "INSERT INTO SKILLS( ID, OWNER,NUMBER,VALUE,MINVALUE,LASTUSED) VALUES(?,?,?,?,?,?)";
  private static final String QUERY_SKILL = "SELECT VALUE,MINVALUE FROM SKILLS WHERE OWNER=? AND NUMBER=?";
  private static final String DELETE_SKILL = "DELETE FROM SKILLS WHERE OWNER=? AND NUMBER=?";
  
  public SkillMetaData(long aId, long aOwner, int aNumber, double aKnowledge, double aMinvalue, long aLastused)
  {
    this.id = aId;
    this.owner = aOwner;
    this.number = aNumber;
    this.knowledge = aKnowledge;
    this.minvalue = aMinvalue;
    this.lastused = aLastused;
  }
  
  public final void setChallenge()
  {
    if ((this.number >= 100) && (this.knowledge < 21.0D))
    {
      this.knowledge = 21.0D;
      if (this.number == 100) {
        this.knowledge = 30.0D;
      }
      this.minvalue = this.knowledge;
    }
    if (this.number == 1023)
    {
      this.knowledge = 70.0D;
      this.minvalue = this.knowledge;
    }
  }
  
  public void save()
    throws IOException
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("INSERT INTO SKILLS( ID, OWNER,NUMBER,VALUE,MINVALUE,LASTUSED) VALUES(?,?,?,?,?,?)");
      ps.setLong(1, this.id);
      ps.setLong(2, this.owner);
      ps.setInt(3, this.number);
      ps.setDouble(4, this.knowledge);
      ps.setDouble(5, this.minvalue);
      ps.setLong(6, this.lastused);
      ps.executeUpdate();
    }
    catch (SQLException sqex)
    {
      throw new IOException(this.id + " " + sqex.getMessage(), sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public static void deleteSkill(long wurmId, int skillNumber)
    throws IOException
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("DELETE FROM SKILLS WHERE OWNER=? AND NUMBER=?");
      ps.setLong(1, wurmId);
      ps.setInt(2, skillNumber);
      ps.executeUpdate();
      
      DbUtilities.closeDatabaseObjects(ps, null);
    }
    catch (SQLException sqex)
    {
      throw new IOException(wurmId + " " + sqex.getMessage(), sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public static SkillMetaData copyToEpicSkill(long skillId, long wurmId, int skillNumber, double skillValue, double skillMinimum, long skillLastUsed)
    throws IOException
  {
    double lastVal = 0.0D;
    double lastMin = 0.0D;
    
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT VALUE,MINVALUE FROM SKILLS WHERE OWNER=? AND NUMBER=?");
      ps.setLong(1, wurmId);
      ps.setInt(2, skillNumber);
      rs = ps.executeQuery();
      if (rs.first())
      {
        lastVal = rs.getDouble("VALUE");
        lastMin = rs.getDouble("MINVALUE");
      }
    }
    catch (SQLException sqex)
    {
      throw new IOException(skillId + " " + sqex.getMessage(), sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    boolean useNewSkill = true;
    if ((lastVal < lastMin) || (lastVal > skillValue)) {
      useNewSkill = false;
    }
    return new SkillMetaData(skillId, wurmId, skillNumber, useNewSkill ? skillValue : lastVal, (skillMinimum > lastMin) && (useNewSkill) ? skillMinimum : lastMin, skillLastUsed);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\skills\SkillMetaData.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */