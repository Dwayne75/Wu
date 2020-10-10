package com.wurmonline.server.structures;

import com.wurmonline.mesh.Tiles.TileBorderDirection;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.tutorial.MissionTargets;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.StructureConstantsEnum;
import com.wurmonline.shared.constants.StructureStateEnum;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbFence
  extends Fence
{
  private static final Logger logger = Logger.getLogger(DbFence.class.getName());
  private static final String CREATE_FENCE = "insert into FENCES(TYPE,LASTMAINTAINED,CURRENTQL,ORIGINALQL,DAMAGE,TILEX,TILEY,DIR,ZONEID,STATE,HEIGHTOFFSET,LAYER) values(?,?,?,?,?,?,?,?,?,?,?,?)";
  private static final String UPDATE_FENCE = "update FENCES set TYPE=?, LASTMAINTAINED=?, CURRENTQL=?, ORIGINALQL=?,DAMAGE=?, STATE=? where ID=?";
  private static final String GET_FENCE = "select * from FENCES where ID=?";
  private static final String DELETE_FENCE = "delete from FENCES where ID=?";
  private static final String SET_ZONE_ID = "update FENCES set ZONEID=? where ID=?";
  private static final String SET_DAMAGE = "update FENCES set DAMAGE=? where ID=?";
  private static final String SET_QL = "update FENCES set CURRENTQL=? where ID=?";
  private static final String SET_ORIGINAL_QL = "update FENCES set ORIGINALQL=? where ID=?";
  private static final String SET_LAST_USED = "update FENCES set LASTMAINTAINED=? where ID=?";
  private static final String SET_COLOR = "update FENCES set COLOR=? WHERE ID=?";
  private static final String SET_SETTINGS = "UPDATE FENCES SET SETTINGS=? WHERE ID=?";
  
  public DbFence(StructureConstantsEnum aType, int aTileX, int aTileY, int aHeightOffset, float aQualityLevel, Tiles.TileBorderDirection aDir, int aZoneId, int aLayer)
  {
    super(aType, aTileX, aTileY, aHeightOffset, aQualityLevel, aDir, aZoneId, aLayer);
  }
  
  public DbFence(int aNumber, StructureConstantsEnum aType, StructureStateEnum aState, int aColor, int aTileX, int aTileY, int aHeightOffset, float aQualityLevel, float aOriginalQl, long aLastUsed, Tiles.TileBorderDirection aDir, int aZoneId, boolean aSurfaced, float aDamage, int aLayer, int aSettings)
  {
    super(aNumber, aType, aState, aColor, aTileX, aTileY, aHeightOffset, aQualityLevel, aOriginalQl, aLastUsed, aDir, aZoneId, aSurfaced, aDamage, aLayer, aSettings);
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
        ps = dbcon.prepareStatement("update FENCES set TYPE=?, LASTMAINTAINED=?, CURRENTQL=?, ORIGINALQL=?,DAMAGE=?, STATE=? where ID=?");
        ps.setShort(1, this.type.value);
        ps.setLong(2, this.lastUsed);
        ps.setFloat(3, this.currentQL);
        ps.setFloat(4, this.originalQL);
        ps.setFloat(5, this.damage);
        ps.setByte(6, this.state.state);
        ps.setInt(7, this.number);
        ps.executeUpdate();
      }
      else
      {
        ps = dbcon.prepareStatement("insert into FENCES(TYPE,LASTMAINTAINED,CURRENTQL,ORIGINALQL,DAMAGE,TILEX,TILEY,DIR,ZONEID,STATE,HEIGHTOFFSET,LAYER) values(?,?,?,?,?,?,?,?,?,?,?,?)", 1);
        ps.setShort(1, this.type.value);
        ps.setLong(2, this.lastUsed);
        ps.setFloat(3, this.currentQL);
        ps.setFloat(4, this.originalQL);
        ps.setFloat(5, 0.0F);
        ps.setInt(6, this.tilex);
        ps.setInt(7, this.tiley);
        ps.setByte(8, this.dir);
        ps.setInt(9, this.zoneId);
        ps.setByte(10, this.state.state);
        ps.setInt(11, this.heightOffset);
        ps.setInt(12, this.layer);
        ps.executeUpdate();
        rs = ps.getGeneratedKeys();
        if (rs.next()) {
          this.number = rs.getInt(1);
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
  
  void load()
    throws IOException
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("select * from FENCES where ID=?");
      ps.setInt(1, this.number);
      rs = ps.executeQuery();
      if (rs.next())
      {
        this.tilex = rs.getInt("TILEX");
        this.tiley = rs.getInt("TILEY");
        this.currentQL = rs.getFloat("ORIGINALQL");
        this.originalQL = rs.getFloat("CURRENTQL");
        this.lastUsed = rs.getLong("LASTMAINTAINED");
        this.type = StructureConstantsEnum.getEnumByValue(rs.getShort("TYPE"));
        this.state = StructureStateEnum.getStateByValue(rs.getByte("STATE"));
        this.zoneId = rs.getInt("ZONEID");
        this.dir = rs.getByte("DIR");
        this.damage = rs.getFloat("DAMAGE");
        setSettings(rs.getInt("SETTINGS"));
      }
      else
      {
        logger.log(Level.WARNING, "Failed to find fence with number " + this.number);
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
  
  private boolean exists(Connection dbcon)
    throws SQLException
  {
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      ps = dbcon.prepareStatement("select * from FENCES where ID=?");
      ps.setInt(1, this.number);
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
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("delete from FENCES where ID=?");
      ps.setInt(1, this.number);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to delete fence with id " + this.number, sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public void setZoneId(int zid)
  {
    if (this.zoneId != zid)
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        this.zoneId = zid;
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("update FENCES set ZONEID=? where ID=?");
        ps.setInt(1, this.zoneId);
        ps.setInt(2, this.number);
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to set zoneid to " + zid + " for fence with id " + this.number, sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  public boolean setDamage(float dam)
  {
    boolean destroyed = false;
    if (isIndestructible()) {
      return false;
    }
    if (dam >= 100.0F)
    {
      DoorSettings.remove(getId());
      PermissionsHistories.remove(getId());
      destroyed = true;
      if (supports())
      {
        boolean forcePlan = false;
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
          dam = 0.0F;
          setType(getFencePlanForType(getType()));
          setQualityLevel(1.0F);
          if (tile != null) {
            tile.updateFence(this);
          }
        }
      }
    }
    if (dam >= 100.0F)
    {
      destroy();
    }
    else if (this.damage != dam)
    {
      boolean updateState = false;
      if (((this.damage >= 60.0F) && (dam < 60.0F)) || ((this.damage < 60.0F) && (dam >= 60.0F))) {
        updateState = true;
      }
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        this.damage = Math.max(0.0F, dam);
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("update FENCES set DAMAGE=? where ID=?");
        ps.setFloat(1, this.damage);
        ps.setInt(2, this.number);
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to set damage to " + dam + " for fence with id " + this.number, sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
      if ((updateState) && (!isMagic())) {
        if (getTile() != null) {
          getTile().updateFenceState(this);
        }
      }
    }
    return destroyed;
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
        ps = dbcon.prepareStatement("update FENCES set CURRENTQL=? where ID=?");
        ps.setFloat(1, this.currentQL);
        ps.setInt(2, this.number);
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to set QL to " + ql + " for fence with id " + this.number, sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
    return ql >= 100.0F;
  }
  
  public void improveOrigQualityLevel(float ql)
  {
    if (ql > 100.0F) {
      ql = 100.0F;
    }
    if (this.originalQL != ql)
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        this.originalQL = ql;
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("update FENCES set ORIGINALQL=? where ID=?");
        ps.setFloat(1, this.originalQL);
        ps.setInt(2, this.number);
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to set original QL to " + ql + " for fence with id " + this.number, sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  public void setLastUsed(long last)
  {
    if (this.lastUsed != last)
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        this.lastUsed = last;
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("update FENCES set LASTMAINTAINED=? where ID=?");
        ps.setLong(1, last);
        ps.setInt(2, this.number);
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to set lastUsed to " + last + " for fence with id " + this.number, sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  boolean changeColor(int newcolor)
  {
    if (getColor() != newcolor)
    {
      this.color = newcolor;
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("update FENCES set COLOR=? WHERE ID=?");
        ps.setInt(1, newcolor);
        ps.setInt(2, this.number);
        ps.executeUpdate();
        VolaTile tile = Zones.getOrCreateTile(getTileX(), getTileY(), true);
        tile.updateFence(this);
        return true;
      }
      catch (SQLException sqx)
      {
        boolean bool;
        logger.log(Level.WARNING, "Failed to set color to " + getColor() + " for fence with id " + this.number, sqx);
        return true;
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
    return false;
  }
  
  public void savePermissions()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("UPDATE FENCES SET SETTINGS=? WHERE ID=?");
      ps.setLong(1, this.permissions.getPermissions());
      ps.setLong(2, getNumber());
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to save settings for fence id " + getNumber(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\structures\DbFence.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */