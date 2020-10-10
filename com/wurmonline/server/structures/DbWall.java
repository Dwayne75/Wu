package com.wurmonline.server.structures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.MethodsStructure;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.tutorial.MissionTargets;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.shared.constants.StructureMaterialEnum;
import com.wurmonline.shared.constants.StructureStateEnum;
import com.wurmonline.shared.constants.StructureTypeEnum;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbWall
  extends Wall
{
  private static final Logger logger = Logger.getLogger(DbWall.class.getName());
  private static final String createWall = "insert into WALLS(TYPE, LASTMAINTAINED , CURRENTQL, ORIGINALQL,DAMAGE, STRUCTURE, STARTX, STARTY, ENDX, ENDY, OUTERWALL, TILEX, TILEY, STATE,MATERIAL,ISINDOOR, HEIGHTOFFSET, LAYER, WALLORIENTATION) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
  private static final String updateWall = "update WALLS set TYPE=?, LASTMAINTAINED =?, CURRENTQL=?, ORIGINALQL=?,DAMAGE=?, STRUCTURE=?, STATE=?,MATERIAL=?,ISINDOOR=?,HEIGHTOFFSET=?,LAYER=?,TILEX=?,TILEY=? where ID=?";
  private static final String getWall = "select * from WALLS where ID=?";
  private static final String deleteWall = "delete from WALLS where ID=?";
  private static final String setDamage = "update WALLS set DAMAGE=? where ID=?";
  private static final String setState = "update WALLS set STATE=?,MATERIAL=? where ID=?";
  private static final String setQL = "update WALLS set CURRENTQL=? where ID=?";
  private static final String setOrigQL = "update WALLS set ORIGINALQL=? where ID=?";
  private static final String setLastUsed = "update WALLS set LASTMAINTAINED=? where ID=?";
  private static final String setIsIndoor = "update WALLS set ISINDOOR=? where ID=?";
  private static final String setColor = "update WALLS set COLOR=? WHERE ID=?";
  private static final String setOrientation = "update WALLS set WALLORIENTATION=? WHERE ID=?";
  private static final String SET_SETTINGS = "UPDATE WALLS SET SETTINGS=? WHERE ID=?";
  
  public DbWall(StructureTypeEnum aType, int aTileX, int aTileY, int aStartX, int aStartY, int aEndX, int aEndY, float aQualityLevel, long aStructure, StructureMaterialEnum aMaterial, boolean aIsIndoor, int aHeightOffset, int aLayer)
  {
    super(aType, aTileX, aTileY, aStartX, aStartY, aEndX, aEndY, aQualityLevel, aStructure, aMaterial, aIsIndoor, aHeightOffset, aLayer);
  }
  
  DbWall(int aNumber, StructureTypeEnum aType, int aTileX, int aTileY, int aStartX, int aStartY, int aEndX, int aEndY, float aQualityLevel, float aOriginalQl, float aDamage, long aStructure, long aLastUsed, StructureStateEnum aState, int aColor, StructureMaterialEnum aMaterial, boolean aIsIndoor, int aHeightOffset, int aLayer, boolean wallOrientation, int aSettings)
  {
    super(aNumber, aType, aTileX, aTileY, aStartX, aStartY, aEndX, aEndY, aQualityLevel, aOriginalQl, aDamage, aStructure, aLastUsed, aState, aColor, aMaterial, aIsIndoor, aHeightOffset, aLayer, wallOrientation, aSettings);
  }
  
  public DbWall(int aNumber)
    throws IOException
  {
    super(aNumber, false);
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
        ps = dbcon.prepareStatement("update WALLS set TYPE=?, LASTMAINTAINED =?, CURRENTQL=?, ORIGINALQL=?,DAMAGE=?, STRUCTURE=?, STATE=?,MATERIAL=?,ISINDOOR=?,HEIGHTOFFSET=?,LAYER=?,TILEX=?,TILEY=? where ID=?");
        ps.setByte(1, this.type.value);
        ps.setLong(2, this.lastUsed);
        ps.setFloat(3, this.currentQL);
        ps.setFloat(4, this.originalQL);
        ps.setFloat(5, this.damage);
        ps.setLong(6, this.structureId);
        ps.setByte(7, this.state.state);
        ps.setByte(8, getMaterial().material);
        ps.setBoolean(9, isIndoor());
        ps.setInt(10, getHeight());
        ps.setInt(11, getLayer());
        ps.setInt(12, getTileX());
        ps.setInt(13, getTileY());
        ps.setInt(14, this.number);
        ps.executeUpdate();
      }
      else
      {
        ps = dbcon.prepareStatement("insert into WALLS(TYPE, LASTMAINTAINED , CURRENTQL, ORIGINALQL,DAMAGE, STRUCTURE, STARTX, STARTY, ENDX, ENDY, OUTERWALL, TILEX, TILEY, STATE,MATERIAL,ISINDOOR, HEIGHTOFFSET, LAYER, WALLORIENTATION) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 1);
        ps.setByte(1, this.type.value);
        ps.setLong(2, this.lastUsed);
        ps.setFloat(3, this.currentQL);
        ps.setFloat(4, this.originalQL);
        ps.setFloat(5, this.damage);
        ps.setLong(6, this.structureId);
        ps.setInt(7, getStartX());
        ps.setInt(8, getStartY());
        ps.setInt(9, getEndX());
        ps.setInt(10, getEndY());
        ps.setBoolean(11, false);
        ps.setInt(12, this.tilex);
        ps.setInt(13, this.tiley);
        ps.setByte(14, this.state.state);
        ps.setByte(15, getMaterial().material);
        ps.setBoolean(16, isIndoor());
        ps.setInt(17, getHeight());
        ps.setInt(18, getLayer());
        ps.setBoolean(19, false);
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
      ps = dbcon.prepareStatement("select * from WALLS where ID=?");
      ps.setInt(1, this.number);
      rs = ps.executeQuery();
      if (rs.next())
      {
        this.x1 = rs.getInt("STARTX");
        this.x2 = rs.getInt("ENDX");
        this.y1 = rs.getInt("STARTY");
        this.y2 = rs.getInt("ENDY");
        this.tilex = rs.getInt("TILEX");
        this.tiley = rs.getInt("TILEY");
        this.currentQL = rs.getFloat("ORIGINALQL");
        this.originalQL = rs.getFloat("CURRENTQL");
        this.lastUsed = rs.getLong("LASTMAINTAINED");
        this.structureId = rs.getLong("STRUCTURE");
        this.type = StructureTypeEnum.getTypeByINDEX(rs.getByte("TYPE"));
        this.state = StructureStateEnum.getStateByValue(rs.getByte("STATE"));
        this.damage = rs.getFloat("DAMAGE");
        setColor(rs.getInt("COLOR"));
        setIndoor(rs.getBoolean("ISINDOOR"));
        this.heightOffset = rs.getInt("HEIGHTOFFSET");
        this.wallOrientationFlag = rs.getBoolean("WALLORIENTATION");
      }
      else
      {
        logger.log(Level.WARNING, "Failed to find wall with number " + this.number);
      }
      DbUtilities.closeDatabaseObjects(ps, rs);
      if (this.state.state <= StructureStateEnum.UNINITIALIZED.state)
      {
        this.state = StructureStateEnum.FINISHED;
        save();
      }
      if (this.type.value == Byte.MAX_VALUE)
      {
        this.type = StructureTypeEnum.PLAN;
        save();
      }
      if (this.type == StructureTypeEnum.RUBBLE) {
        addRubble(this);
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
      ps = dbcon.prepareStatement("select * from WALLS where ID=?");
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
      ps = dbcon.prepareStatement("delete from WALLS where ID=?");
      ps.setInt(1, this.number);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to delete wall with id " + this.number, sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public boolean setDamage(float dam)
  {
    if (dam >= 100.0F)
    {
      if (Servers.localServer.testServer) {
        logger.fine("TEMPORARY LOGGING FOR BUG #1264 - Destroying wall with ID:" + getId() + " which was part of structure with id:" + getStructureId());
      }
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
      if ((MethodsStructure.isWallInsideStructure(this, isOnSurface())) && (!forcePlan))
      {
        destroy();
        
        return true;
      }
      if (Servers.localServer.isChallengeServer())
      {
        if ((isFinished()) && (getType() != StructureTypeEnum.RUBBLE))
        {
          setAsRubble();
          return true;
        }
        dam = 0.0F;
        setAsPlan();
        setQualityLevel(1.0F);
      }
      else
      {
        dam = 0.0F;
        setAsPlan();
        setQualityLevel(1.0F);
      }
    }
    if (this.damage != dam)
    {
      boolean updateState = false;
      if (((this.damage >= 60.0F) && (dam < 60.0F)) || ((this.damage < 60.0F) && (dam >= 60.0F))) {
        updateState = true;
      }
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        this.damage = dam;
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("update WALLS set DAMAGE=? where ID=?");
        ps.setFloat(1, this.damage);
        ps.setInt(2, this.number);
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to set damage to " + dam + " for wall with id " + this.number, sqx);
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
          getTile().updateWallDamageState(this);
        }
      }
    }
    return false;
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
        ps = dbcon.prepareStatement("update WALLS set CURRENTQL=? where ID=?");
        ps.setFloat(1, this.currentQL);
        ps.setInt(2, this.number);
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to set quality to " + ql + " for wall with id " + this.number, sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
    return false;
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
        ps = dbcon.prepareStatement("update WALLS set ORIGINALQL=? where ID=?");
        ps.setFloat(1, this.originalQL);
        ps.setInt(2, this.number);
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to set original quality to " + ql + " for wall with id " + this.number, sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  public void setIndoor(boolean indoor)
  {
    if (this.isIndoor != indoor)
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        this.isIndoor = indoor;
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("update WALLS set ISINDOOR=? where ID=?");
        ps.setBoolean(1, this.isIndoor);
        ps.setInt(2, this.number);
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to set indoor to " + indoor + " for wall with id " + this.number, sqx);
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
        ps = dbcon.prepareStatement("update WALLS set LASTMAINTAINED=? where ID=?");
        ps.setLong(1, last);
        ps.setInt(2, this.number);
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to set lastUsed to " + last + " for wall with id " + this.number, sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  public void setState(StructureStateEnum newState)
  {
    if (this.state == StructureStateEnum.FINISHED) {
      if (newState != StructureStateEnum.INITIALIZED) {
        return;
      }
    }
    if (newState.state >= getFinalState().state) {
      newState = StructureStateEnum.FINISHED;
    }
    if (this.state != newState)
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        this.state = newState;
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("update WALLS set STATE=?,MATERIAL=? where ID=?");
        ps.setByte(1, this.state.state);
        ps.setByte(2, getMaterial().material);
        ps.setInt(3, this.number);
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to set state to " + this.state + " for wall with id " + this.number, sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  public void setWallOrientation(boolean rotated)
  {
    if (this.wallOrientationFlag != rotated)
    {
      this.wallOrientationFlag = rotated;
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("update WALLS set WALLORIENTATION=? WHERE ID=?");
        ps.setBoolean(1, this.wallOrientationFlag);
        ps.setInt(2, this.number);
        ps.executeUpdate();
        try
        {
          Structure struct = Structures.getStructure(this.structureId);
          VolaTile tile = struct.getTileFor(this);
          if (tile != null) {
            tile.updateWall(this);
          }
        }
        catch (NoSuchStructureException nss)
        {
          logger.log(Level.WARNING, "wall at " + this.x1 + ", " + this.y1 + "-" + this.x2 + "," + this.y2 + ", StructureId: " + this.structureId + " - " + nss
            .getMessage(), nss);
        }
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to set wall orientation to " + this.wallOrientationFlag + " for wall with id " + this.number, sqx);
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
        ps = dbcon.prepareStatement("update WALLS set COLOR=? WHERE ID=?");
        ps.setInt(1, newcolor);
        ps.setInt(2, this.number);
        ps.executeUpdate();
        try
        {
          Structure struct = Structures.getStructure(this.structureId);
          tile = struct.getTileFor(this);
          if (tile != null) {
            tile.updateWall(this);
          }
        }
        catch (NoSuchStructureException nss)
        {
          logger.log(Level.WARNING, "wall at " + this.x1 + ", " + this.y1 + "-" + this.x2 + "," + this.y2 + ", StructureId: " + this.structureId + " - " + nss
          
            .getMessage(), nss);
        }
        return 1;
      }
      catch (SQLException sqx)
      {
        VolaTile tile;
        logger.log(Level.WARNING, "Failed to set color to " + getColor() + " for wall with id " + this.number, sqx);
        return 1;
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
      ps = dbcon.prepareStatement("UPDATE WALLS SET SETTINGS=? WHERE ID=?");
      ps.setLong(1, this.permissions.getPermissions());
      ps.setLong(2, this.number);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to save settings for wall with id " + this.number, sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\structures\DbWall.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */