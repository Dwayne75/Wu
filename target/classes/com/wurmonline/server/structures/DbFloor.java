package com.wurmonline.server.structures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.tutorial.MissionTargets;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.shared.constants.StructureConstants.FloorMaterial;
import com.wurmonline.shared.constants.StructureConstants.FloorState;
import com.wurmonline.shared.constants.StructureConstants.FloorType;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbFloor
  extends Floor
{
  private static final String CREATE_FLOOR = "INSERT INTO FLOORS(TYPE, LASTMAINTAINED , CURRENTQL, ORIGINALQL, DAMAGE, STRUCTURE, TILEX, TILEY, STATE,COLOR, MATERIAL,HEIGHTOFFSET,LAYER,DIR) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
  private static final String UPDATE_FLOOR = "UPDATE FLOORS SET TYPE=?, LASTMAINTAINED =?, CURRENTQL=?, ORIGINALQL=?,DAMAGE=?, STRUCTURE=?, STATE=?,MATERIAL=?,HEIGHTOFFSET=?,DIR=? WHERE ID=?";
  private static final String GET_FLOOR = "SELECT * FROM FLOORS WHERE ID=?";
  private static final String DELETE_FLOOR = "DELETE FROM FLOORS WHERE ID=?";
  private static final String SET_DAMAGE = "UPDATE FLOORS SET DAMAGE=? WHERE ID=?";
  private static final String SET_QUALITY_LEVEL = "UPDATE FLOORS SET CURRENTQL=? WHERE ID=?";
  private static final String SET_STATE = "UPDATE FLOORS SET STATE=?,MATERIAL=? WHERE ID=?";
  private static final String SET_LAST_USED = "UPDATE FLOORS SET LASTMAINTAINED=? WHERE ID=?";
  private static final String SET_SETTINGS = "UPDATE FLOORS SET SETTINGS=? WHERE ID=?";
  private static final Logger logger = Logger.getLogger(DbWall.class.getName());
  
  public boolean isFence()
  {
    return false;
  }
  
  public boolean isWall()
  {
    return false;
  }
  
  public DbFloor(int id, StructureConstants.FloorType floorType, int tilex, int tiley, byte aDbState, int heightOffset, float currentQl, long structureId, StructureConstants.FloorMaterial floorMaterial, int layer, float origQL, float aDamage, long lastmaintained, byte dir)
  {
    super(id, floorType, tilex, tiley, aDbState, heightOffset, currentQl, structureId, floorMaterial, layer, origQL, aDamage, lastmaintained, dir);
  }
  
  public DbFloor(StructureConstants.FloorType floorType, int tilex, int tiley, int heightOffset, float qualityLevel, long structure, StructureConstants.FloorMaterial material, int layer)
  {
    super(floorType, tilex, tiley, heightOffset, qualityLevel, structure, material, layer);
  }
  
  protected void setState(byte newState)
  {
    if (this.dbState != newState)
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        this.dbState = newState;
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("UPDATE FLOORS SET STATE=?,MATERIAL=? WHERE ID=?");
        ps.setByte(1, this.dbState);
        ps.setByte(2, getMaterial().getCode());
        ps.setInt(3, getNumber());
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to set state to " + newState + " for floor with id " + getNumber(), sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  public void save()
    throws IOException
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      if (exists(dbcon))
      {
        ps = dbcon.prepareStatement("UPDATE FLOORS SET TYPE=?, LASTMAINTAINED =?, CURRENTQL=?, ORIGINALQL=?,DAMAGE=?, STRUCTURE=?, STATE=?,MATERIAL=?,HEIGHTOFFSET=?,DIR=? WHERE ID=?");
        ps.setByte(1, getType().getCode());
        ps.setLong(2, getLastUsed());
        ps.setFloat(3, getCurrentQL());
        ps.setFloat(4, getOriginalQL());
        ps.setFloat(5, getDamage());
        ps.setLong(6, getStructureId());
        ps.setByte(7, getState());
        ps.setByte(8, getMaterial().getCode());
        ps.setInt(9, getHeightOffset());
        ps.setByte(10, getDir());
        ps.setInt(11, getNumber());
        
        ps.executeUpdate();
      }
      else
      {
        ps = dbcon.prepareStatement("INSERT INTO FLOORS(TYPE, LASTMAINTAINED , CURRENTQL, ORIGINALQL, DAMAGE, STRUCTURE, TILEX, TILEY, STATE,COLOR, MATERIAL,HEIGHTOFFSET,LAYER,DIR) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 1);
        ps.setByte(1, getType().getCode());
        ps.setLong(2, getLastUsed());
        ps.setFloat(3, getCurrentQL());
        ps.setFloat(4, getOriginalQL());
        ps.setFloat(5, getDamage());
        ps.setLong(6, getStructureId());
        ps.setInt(7, getTileX());
        ps.setInt(8, getTileY());
        ps.setByte(9, getState());
        ps.setInt(10, getColor());
        ps.setByte(11, getMaterial().getCode());
        ps.setInt(12, getHeightOffset());
        ps.setByte(13, getLayer());
        ps.setByte(14, getDir());
        ps.executeUpdate();
        rs = ps.getGeneratedKeys();
        if (rs.next()) {
          setNumber(rs.getInt(1));
        }
      }
    }
    catch (SQLException sqx)
    {
      throw new IOException(sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public boolean setDamage(float aDamage)
  {
    boolean forcePlan = false;
    if (isIndestructible()) {
      return false;
    }
    if (aDamage >= 100.0F)
    {
      VolaTile tile = getTile();
      if (tile != null)
      {
        Structure struct = tile.getStructure();
        if (struct != null) {
          if (struct.wouldCreateFlyingStructureIfRemoved(this)) {
            forcePlan = true;
          }
        }
      }
      if (forcePlan)
      {
        setFloorState(StructureConstants.FloorState.PLANNING);
        setQualityLevel(1.0F);
        if (tile != null) {
          tile.updateFloor(this);
        }
      }
    }
    if (this.damage != aDamage)
    {
      boolean updateState = false;
      if (((this.damage >= 60.0F) && (aDamage < 60.0F)) || ((this.damage < 60.0F) && (aDamage >= 60.0F))) {
        updateState = true;
      }
      this.damage = aDamage;
      if (forcePlan) {
        this.damage = 0.0F;
      }
      if (this.damage < 100.0F)
      {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try
        {
          dbcon = DbConnector.getZonesDbCon();
          ps = dbcon.prepareStatement("UPDATE FLOORS SET DAMAGE=? WHERE ID=?");
          ps.setFloat(1, getDamage());
          ps.setInt(2, getNumber());
          ps.executeUpdate();
        }
        catch (SQLException sqx)
        {
          logger.log(Level.WARNING, getName() + ", " + getNumber() + " " + sqx.getMessage(), sqx);
        }
        finally
        {
          DbUtilities.closeDatabaseObjects(ps, null);
          DbConnector.returnConnection(dbcon);
        }
        if (updateState)
        {
          VolaTile tile = getTile();
          if (tile != null) {
            getTile().updateFloorDamageState(this);
          }
        }
      }
      else
      {
        VolaTile t = getTile();
        if (t != null) {
          t.removeFloor(this);
        }
        delete();
      }
    }
    return this.damage >= 100.0F;
  }
  
  public void setLastUsed(long now)
  {
    if (this.lastUsed != now)
    {
      this.lastUsed = now;
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("UPDATE FLOORS SET LASTMAINTAINED=? WHERE ID=?");
        ps.setLong(1, this.lastUsed);
        ps.setInt(2, getNumber());
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, getName() + ", " + getNumber() + " " + sqx.getMessage(), sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  public boolean setQualityLevel(float ql)
  {
    if (ql > 100.0F) {
      ql = 100.0F;
    }
    if (this.currentQL != ql)
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        this.currentQL = ql;
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("UPDATE FLOORS SET CURRENTQL=? WHERE ID=?");
        ps.setFloat(1, this.currentQL);
        ps.setInt(2, getNumber());
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, getName() + ", " + getNumber() + " " + sqx.getMessage(), sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
    return ql >= 100.0F;
  }
  
  private boolean exists(Connection dbcon)
    throws SQLException
  {
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      ps = dbcon.prepareStatement("SELECT * FROM FLOORS WHERE ID=?");
      ps.setInt(1, getNumber());
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
    MissionTargets.destroyMissionTarget(getId(), true);
    MethodsHighways.removeNearbyMarkers(this);
    
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("DELETE FROM FLOORS WHERE ID=?");
      ps.setInt(1, getNumber());
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to delete floor with id " + getNumber(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public void savePermissions()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("UPDATE FLOORS SET SETTINGS=? WHERE ID=?");
      ps.setLong(1, this.permissions.getPermissions());
      ps.setLong(2, getNumber());
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to save settings for floor with id " + getNumber(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\structures\DbFloor.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */