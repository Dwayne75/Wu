package com.wurmonline.server.structures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbFenceGate
  extends FenceGate
{
  private static final Logger logger = Logger.getLogger(DbFenceGate.class.getName());
  private static final String GET_GATE = "SELECT * FROM GATES WHERE ID=?";
  private static final String EXISTS_GATE = "SELECT 1 FROM GATES WHERE ID=?";
  private static final String CREATE_GATE = "INSERT INTO GATES (NAME,OPENTIME,CLOSETIME,LOCKID,VILLAGE,ID) VALUES(?,?,?,?,?,?)";
  private static final String UPDATE_GATE = "UPDATE GATES SET NAME=?,OPENTIME=?,CLOSETIME=?,LOCKID=?,VILLAGE=? WHERE ID=?";
  private static final String DELETE_GATE = "DELETE FROM GATES WHERE ID=?";
  private static final String SET_NAME = "UPDATE GATES SET NAME=? WHERE ID=?";
  private static final String SET_OPEN_TIME = "UPDATE GATES SET OPENTIME=? WHERE ID=?";
  private static final String SET_CLOSE_TIME = "UPDATE GATES SET CLOSETIME=? WHERE ID=?";
  private static final String SET_LOCKID = "UPDATE GATES SET LOCKID=? WHERE ID=?";
  private static final String SET_VILLAGEID = "UPDATE GATES SET VILLAGE=? WHERE ID=?";
  
  public DbFenceGate(Fence aFence)
  {
    super(aFence);
  }
  
  public void save()
    throws IOException
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      String string = "INSERT INTO GATES (NAME,OPENTIME,CLOSETIME,LOCKID,VILLAGE,ID) VALUES(?,?,?,?,?,?)";
      if (exists(dbcon)) {
        string = "UPDATE GATES SET NAME=?,OPENTIME=?,CLOSETIME=?,LOCKID=?,VILLAGE=? WHERE ID=?";
      }
      ps = dbcon.prepareStatement(string);
      ps.setString(1, getName());
      ps.setByte(2, (byte)getOpenTime());
      ps.setByte(3, (byte)getCloseTime());
      ps.setLong(4, this.lock);
      ps.setInt(5, getVillageId());
      ps.setLong(6, getFence().getId());
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to save gate with id " + getFence().getId(), ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private boolean exists(Connection dbcon)
    throws SQLException
  {
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      ps = dbcon.prepareStatement("SELECT 1 FROM GATES WHERE ID=?");
      ps.setFetchSize(1);
      ps.setLong(1, getFence().getId());
      rs = ps.executeQuery();
      return rs.next();
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
    }
  }
  
  void load()
    throws IOException
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM GATES WHERE ID=?");
      ps.setFetchSize(1);
      ps.setLong(1, getFence().getId());
      rs = ps.executeQuery();
      if (rs.next())
      {
        this.openTime = rs.getByte("OPENTIME");
        this.closeTime = rs.getByte("CLOSETIME");
        this.name = rs.getString("NAME");
        this.lock = rs.getLong("LOCKID");
        this.villageId = rs.getInt("VILLAGE");
      }
      else
      {
        save();
      }
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to load gate with id " + getFence().getId(), ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public void delete()
  {
    gates.remove(new Long(getFence().getId()));
    DoorSettings.remove(getFence().getId());
    PermissionsHistories.remove(getFence().getId());
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("DELETE FROM GATES WHERE ID=?");
      ps.setLong(1, getFence().getId());
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to delete fencegate with id " + getFence().getId(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
    if (this.lock != -10L)
    {
      Items.decay(this.lock, null);
      this.lock = -10L;
    }
  }
  
  public void setOpenTime(int time)
  {
    if (getOpenTime() != time)
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        this.openTime = time;
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("UPDATE GATES SET OPENTIME=? WHERE ID=?");
        ps.setByte(1, (byte)getOpenTime());
        ps.setLong(2, getFence().getId());
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to set opentime to " + getOpenTime() + " for fencegate with id " + 
          getFence().getId(), sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  public void setCloseTime(int time)
  {
    if (getCloseTime() != time)
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        this.closeTime = time;
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("UPDATE GATES SET CLOSETIME=? WHERE ID=?");
        ps.setByte(1, (byte)getCloseTime());
        ps.setLong(2, getFence().getId());
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to set closetime to " + getCloseTime() + " fencegate with id " + 
          getFence().getId(), sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  public void setName(String aName)
  {
    String newname = aName.substring(0, Math.min(39, aName.length()));
    if (!getName().equals(newname))
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        this.name = newname;
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("UPDATE GATES SET NAME=? WHERE ID=?");
        ps.setString(1, getName());
        ps.setLong(2, getFence().getId());
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to set name to " + 
          getName() + " for fencegate with id " + getFence().getId(), sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  public void setLock(long lockid)
  {
    if (this.lock != lockid)
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        this.lock = lockid;
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("UPDATE GATES SET LOCKID=? WHERE ID=?");
        ps.setLong(1, lockid);
        ps.setLong(2, getFence().getId());
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to set lock for fencegate with id " + getFence().getId(), sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  public void setVillageId(int newVillageId)
  {
    if (getVillageId() != newVillageId)
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        this.villageId = newVillageId;
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("UPDATE GATES SET VILLAGE=? WHERE ID=?");
        ps.setString(1, getName());
        ps.setLong(2, getVillageId());
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to set villageId to " + 
          getVillageId() + " for fencegate with id " + getFence().getId(), sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  public boolean isItem()
  {
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\structures\DbFenceGate.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */