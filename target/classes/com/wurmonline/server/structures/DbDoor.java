package com.wurmonline.server.structures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbDoor
  extends Door
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(DbDoor.class.getName());
  private static final String GET_DOOR = "SELECT * FROM DOORS WHERE STRUCTURE=? AND INNERWALL=?";
  private static final String EXISTS_DOOR = "SELECT 1 FROM DOORS WHERE STRUCTURE=? AND INNERWALL=?";
  private static final String CREATE_DOOR = "INSERT INTO DOORS (LOCKID,NAME,SETTINGS,STRUCTURE,INNERWALL) VALUES(?,?,?,?,?)";
  private static final String UPDATE_DOOR = "UPDATE DOORS SET LOCKID=?,NAME=?,SETTINGS=? WHERE STRUCTURE=? AND INNERWALL=?";
  private static final String DELETE_DOOR = "DELETE FROM DOORS WHERE STRUCTURE=? AND INNERWALL=?";
  private static final String SET_NAME = "UPDATE DOORS SET NAME=? WHERE INNERWALL=?";
  
  public DbDoor(Wall aWall)
  {
    super(aWall);
  }
  
  public void save()
    throws IOException
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      String string = "INSERT INTO DOORS (LOCKID,NAME,SETTINGS,STRUCTURE,INNERWALL) VALUES(?,?,?,?,?)";
      if (exists(dbcon)) {
        string = "UPDATE DOORS SET LOCKID=?,NAME=?,SETTINGS=? WHERE STRUCTURE=? AND INNERWALL=?";
      }
      ps = dbcon.prepareStatement(string);
      ps.setLong(1, this.lock);
      ps.setString(2, this.name);
      ps.setInt(3, 0);
      ps.setLong(4, this.structure);
      long iid = this.wall.getId();
      ps.setLong(5, iid);
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to save door for structure with id " + this.structure, ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
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
      ps = dbcon.prepareStatement("SELECT * FROM DOORS WHERE STRUCTURE=? AND INNERWALL=?");
      ps.setLong(1, this.structure);
      ps.setLong(2, this.wall.getId());
      rs = ps.executeQuery();
      if (rs.next())
      {
        this.lock = rs.getLong("LOCKID");
        this.name = rs.getString("NAME");
      }
      else
      {
        save();
      }
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to load door for structure with id " + this.structure, ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
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
      ps = dbcon.prepareStatement("SELECT 1 FROM DOORS WHERE STRUCTURE=? AND INNERWALL=?");
      ps.setLong(1, this.structure);
      ps.setLong(2, this.wall.getId());
      rs = ps.executeQuery();
      return rs.next();
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
    }
  }
  
  public void delete()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("DELETE FROM DOORS WHERE STRUCTURE=? AND INNERWALL=?");
      ps.setLong(1, this.structure);
      ps.setLong(2, this.wall.getId());
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to delete wall for structure with id " + this.structure, sqx);
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, this.structure + ":" + ex.getMessage(), ex);
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
  
  public void setName(String aName)
  {
    String newname = aName.substring(0, Math.min(39, aName.length()));
    if (!getName().equals(newname))
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        setNewName(newname);
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("UPDATE DOORS SET NAME=? WHERE INNERWALL=?");
        ps.setString(1, getName());
        ps.setLong(2, this.wall.getId());
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to set name to " + 
          getName() + " for door with innerwall of " + this.wall.getId(), sqx);
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\structures\DbDoor.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */