package com.wurmonline.server.structures;

import com.wurmonline.math.TilePos;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.tutorial.MissionTargets;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.BridgeConstants.BridgeMaterial;
import com.wurmonline.shared.constants.BridgeConstants.BridgeState;
import com.wurmonline.shared.constants.BridgeConstants.BridgeType;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbBridgePart
  extends BridgePart
{
  private static final String CREATEBRIDGEPART = "INSERT INTO BRIDGEPARTS(TYPE, LASTMAINTAINED , CURRENTQL, ORIGINALQL, DAMAGE, STRUCTURE, TILEX, TILEY, STATE, MATERIAL, HEIGHTOFFSET, DIR, SLOPE, STAGECOUNT, NORTHEXIT, EASTEXIT, SOUTHEXIT, WESTEXIT, LAYER) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
  private static final String UPDATEBRIDGEPART = "UPDATE BRIDGEPARTS SET TYPE=?,LASTMAINTAINED=?,CURRENTQL=?,ORIGINALQL=?,DAMAGE=?,STRUCTURE=?,STATE=?,MATERIAL=?,HEIGHTOFFSET=?,DIR=?,SLOPE=?,STAGECOUNT=?,NORTHEXIT=?,EASTEXIT=?,SOUTHEXIT=?,WESTEXIT=?,LAYER=? WHERE ID=?";
  private static final String GETBRIDGEPART = "SELECT * FROM BRIDGEPARTS WHERE ID=?";
  private static final String DELETEBRIDGEPART = "DELETE FROM BRIDGEPARTS WHERE ID=?";
  private static final String SETDAMAGE = "UPDATE BRIDGEPARTS SET DAMAGE=? WHERE ID=?";
  private static final String SETQUALITYLEVEL = "UPDATE BRIDGEPARTS SET CURRENTQL=? WHERE ID=?";
  private static final String SETSTATE = "UPDATE BRIDGEPARTS SET STATE=?,MATERIAL=? WHERE ID=?";
  private static final String SETLASTUSED = "UPDATE BRIDGEPARTS SET LASTMAINTAINED=? WHERE ID=?";
  private static final String SET_SETTINGS = "UPDATE BRIDGEPARTS SET SETTINGS=? WHERE ID=?";
  private static final String SETROADTYPE = "UPDATE BRIDGEPARTS SET ROADTYPE=? WHERE ID=?";
  private static final Logger logger = Logger.getLogger(DbWall.class.getName());
  
  public boolean isFence()
  {
    return false;
  }
  
  public boolean isWall()
  {
    return false;
  }
  
  public DbBridgePart(int id, BridgeConstants.BridgeType floorType, int tilex, int tiley, byte aDbState, int heightOffset, float currentQl, long structureId, BridgeConstants.BridgeMaterial floorMaterial, float origQL, float dam, int materialCount, long lastmaintained, byte dir, byte slope, int aNorthExit, int aEastExit, int aSouthExit, int aWestExit, byte roadType, int layer)
  {
    super(id, floorType, tilex, tiley, aDbState, heightOffset, currentQl, structureId, floorMaterial, origQL, dam, materialCount, lastmaintained, dir, slope, aNorthExit, aEastExit, aSouthExit, aWestExit, roadType, layer);
  }
  
  public DbBridgePart(BridgeConstants.BridgeType floorType, int tilex, int tiley, int heightOffset, float qualityLevel, long structure, BridgeConstants.BridgeMaterial material, byte dir, byte slope, int aNorthExit, int aEastExit, int aSouthExit, int aWestExit, byte roadType, int layer)
  {
    super(floorType, tilex, tiley, heightOffset, qualityLevel, structure, material, dir, slope, aNorthExit, aEastExit, aSouthExit, aWestExit, roadType, layer);
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
        ps = dbcon.prepareStatement("UPDATE BRIDGEPARTS SET STATE=?,MATERIAL=? WHERE ID=?");
        ps.setByte(1, this.dbState);
        ps.setByte(2, getMaterial().getCode());
        ps.setInt(3, getNumber());
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to set state to " + newState + " for bridge part with id " + getNumber(), sqx);
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
        ps = dbcon.prepareStatement("UPDATE BRIDGEPARTS SET TYPE=?,LASTMAINTAINED=?,CURRENTQL=?,ORIGINALQL=?,DAMAGE=?,STRUCTURE=?,STATE=?,MATERIAL=?,HEIGHTOFFSET=?,DIR=?,SLOPE=?,STAGECOUNT=?,NORTHEXIT=?,EASTEXIT=?,SOUTHEXIT=?,WESTEXIT=?,LAYER=? WHERE ID=?");
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
        ps.setByte(11, getSlope());
        ps.setInt(12, getMaterialCount());
        ps.setInt(13, getNorthExit());
        ps.setInt(14, getEastExit());
        ps.setInt(15, getSouthExit());
        ps.setInt(16, getWestExit());
        ps.setInt(17, getLayer());
        ps.setInt(18, getNumber());
        
        ps.executeUpdate();
      }
      else
      {
        ps = dbcon.prepareStatement("INSERT INTO BRIDGEPARTS(TYPE, LASTMAINTAINED , CURRENTQL, ORIGINALQL, DAMAGE, STRUCTURE, TILEX, TILEY, STATE, MATERIAL, HEIGHTOFFSET, DIR, SLOPE, STAGECOUNT, NORTHEXIT, EASTEXIT, SOUTHEXIT, WESTEXIT, LAYER) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 1);
        ps.setByte(1, getType().getCode());
        ps.setLong(2, getLastUsed());
        ps.setFloat(3, getCurrentQL());
        ps.setFloat(4, getOriginalQL());
        ps.setFloat(5, getDamage());
        ps.setLong(6, getStructureId());
        ps.setInt(7, getTileX());
        ps.setInt(8, getTileY());
        ps.setByte(9, getState());
        ps.setByte(10, getMaterial().getCode());
        ps.setInt(11, getHeightOffset());
        ps.setByte(12, getDir());
        ps.setByte(13, getSlope());
        ps.setInt(14, getMaterialCount());
        ps.setInt(15, getNorthExit());
        ps.setInt(16, getEastExit());
        ps.setInt(17, getSouthExit());
        ps.setInt(18, getWestExit());
        ps.setInt(19, getLayer());
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
      forcePlan = true;
      BridgeConstants.BridgeState oldBridgeState = getBridgePartState();
      setBridgePartState(BridgeConstants.BridgeState.PLANNED);
      setQualityLevel(1.0F);
      saveRoadType((byte)0);
      if (tile != null)
      {
        tile.updateBridgePart(this);
        if (oldBridgeState != BridgeConstants.BridgeState.PLANNED)
        {
          BridgeConstants.BridgeType bType = getType();
          switch (DbBridgePart.1.$SwitchMap$com$wurmonline$shared$constants$BridgeConstants$BridgeMaterial[getMaterial().ordinal()])
          {
          case 1: 
          case 2: 
          case 3: 
          case 4: 
          case 5: 
          case 6: 
          case 7: 
            if (bType.isSupportType())
            {
              damageAdjacent("abutment", 50);
            }
            else if (bType.isAbutment())
            {
              damageAdjacent("bracing", 25);
            }
            else if (bType.isBracing())
            {
              damageAdjacent("crown", 10);
              damageAdjacent("floating", 10);
            }
            break;
          case 8: 
            if (bType.isSupportType())
            {
              damageAdjacent("abutment", 50);
              damageAdjacent("crown", 25);
            }
            else if (bType.isAbutment())
            {
              damageAdjacent("crown", 10);
            }
            break;
          case 9: 
            if (bType.isAbutment()) {
              damageAdjacent("crown", 50);
            }
            break;
          }
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
          ps = dbcon.prepareStatement("UPDATE BRIDGEPARTS SET DAMAGE=? WHERE ID=?");
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
            getTile().updateBridgePartDamageState(this);
          }
        }
      }
      else
      {
        VolaTile t = getTile();
        if (t != null) {
          t.removeBridgePart(this);
        }
        delete();
      }
    }
    return this.damage >= 100.0F;
  }
  
  private void damageAdjacent(String typeName, int addDamage)
  {
    VolaTile vtNorth = Zones.getTileOrNull(getTileX(), getTileY() - 1, isOnSurface());
    if (vtNorth != null)
    {
      Structure structNorth = vtNorth.getStructure();
      if ((structNorth != null) && (structNorth.getWurmId() == getStructureId()))
      {
        BridgePart[] bps = vtNorth.getBridgeParts();
        if ((bps.length == 1) && (bps[0].getType().getName().equalsIgnoreCase(typeName))) {
          bps[0].setDamage(bps[0].getDamage() + addDamage);
        }
      }
    }
    VolaTile vtEast = Zones.getTileOrNull(getTileX() + 1, getTileY(), isOnSurface());
    if (vtEast != null)
    {
      Structure structEast = vtEast.getStructure();
      if ((structEast != null) && (structEast.getWurmId() == getStructureId()))
      {
        BridgePart[] bps = vtEast.getBridgeParts();
        if ((bps.length == 1) && (bps[0].getType().getName().equalsIgnoreCase(typeName))) {
          bps[0].setDamage(bps[0].getDamage() + addDamage);
        }
      }
    }
    VolaTile vtSouth = Zones.getTileOrNull(getTileX(), getTileY() + 1, isOnSurface());
    if (vtSouth != null)
    {
      Structure structSouth = vtSouth.getStructure();
      if ((structSouth != null) && (structSouth.getWurmId() == getStructureId()))
      {
        BridgePart[] bps = vtSouth.getBridgeParts();
        if ((bps.length == 1) && (bps[0].getType().getName().equalsIgnoreCase(typeName))) {
          bps[0].setDamage(bps[0].getDamage() + addDamage);
        }
      }
    }
    VolaTile vtWest = Zones.getTileOrNull(getTileX() - 1, getTileY(), isOnSurface());
    if (vtWest != null)
    {
      Structure structWest = vtWest.getStructure();
      if ((structWest != null) && (structWest.getWurmId() == getStructureId()))
      {
        BridgePart[] bps = vtWest.getBridgeParts();
        if ((bps.length == 1) && (bps[0].getType().getName().equalsIgnoreCase(typeName))) {
          bps[0].setDamage(bps[0].getDamage() + addDamage);
        }
      }
    }
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
        ps = dbcon.prepareStatement("UPDATE BRIDGEPARTS SET LASTMAINTAINED=? WHERE ID=?");
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
        ps = dbcon.prepareStatement("UPDATE BRIDGEPARTS SET CURRENTQL=? WHERE ID=?");
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
      ps = dbcon.prepareStatement("SELECT * FROM BRIDGEPARTS WHERE ID=?");
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
      ps = dbcon.prepareStatement("DELETE FROM BRIDGEPARTS WHERE ID=?");
      ps.setInt(1, getNumber());
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to delete bridge part with id " + getNumber(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public long getTempId()
  {
    return -10L;
  }
  
  public void savePermissions()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("UPDATE BRIDGEPARTS SET SETTINGS=? WHERE ID=?");
      ps.setLong(1, this.permissions.getPermissions());
      ps.setLong(2, getNumber());
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to save settings for bridge part with id " + getNumber(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public void saveRoadType(byte roadType)
  {
    if (this.roadType != roadType)
    {
      this.roadType = roadType;
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("UPDATE BRIDGEPARTS SET ROADTYPE=? WHERE ID=?");
        ps.setByte(1, this.roadType);
        ps.setLong(2, getNumber());
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to save roadtype for bridge part with id " + getNumber(), sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  public final boolean isOnSouthBorder(TilePos pos)
  {
    return false;
  }
  
  public final boolean isOnNorthBorder(TilePos pos)
  {
    return false;
  }
  
  public final boolean isOnWestBorder(TilePos pos)
  {
    return false;
  }
  
  public final boolean isOnEastBorder(TilePos pos)
  {
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\structures\DbBridgePart.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */