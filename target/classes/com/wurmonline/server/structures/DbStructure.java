package com.wurmonline.server.structures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbStructure
  extends Structure
{
  private static final Logger logger = Logger.getLogger(DbStructure.class.getName());
  private static final String GET_STRUCTURE = "SELECT * FROM STRUCTURES WHERE WURMID=?";
  private static final String SAVE_STRUCTURE = "UPDATE STRUCTURES SET CENTERX=?,CENTERY=?,ROOF=?,SURFACED=?,NAME=?,FINISHED=?,WRITID=?,FINFINISHED=?,ALLOWSVILLAGERS=?,ALLOWSALLIES=?,ALLOWSKINGDOM=?,PLANNER=?,OWNERID=?,SETTINGS=?,VILLAGE=? WHERE WURMID=?";
  private static final String CREATE_STRUCTURE = "INSERT INTO STRUCTURES(WURMID, STRUCTURETYPE) VALUES(?,?)";
  private static final String DELETE_STRUCTURE = "DELETE FROM STRUCTURES WHERE WURMID=?";
  private static final String ADD_BUILDTILE = "INSERT INTO BUILDTILES(STRUCTUREID,TILEX,TILEY,LAYER) VALUES (?,?,?,?)";
  private static final String DELETE_BUILDTILE = "DELETE FROM BUILDTILES WHERE STRUCTUREID=? AND TILEX=? AND TILEY=? AND LAYER=?";
  private static final String DELETE_ALLBUILDTILES = "DELETE FROM BUILDTILES WHERE STRUCTUREID=?";
  private static final String LOAD_ALLBUILDTILES = "SELECT * FROM BUILDTILES";
  private static final String SET_FINISHED = "UPDATE STRUCTURES SET FINISHED=? WHERE WURMID=?";
  private static final String SET_FIN_FINISHED = "UPDATE STRUCTURES SET FINFINISHED=? WHERE WURMID=?";
  private static final String SET_WRITID = "UPDATE STRUCTURES SET WRITID=? WHERE WURMID=?";
  private static final String SET_OWNERID = "UPDATE STRUCTURES SET OWNERID=? WHERE WURMID=?";
  private static final String SET_SETTINGS = "UPDATE STRUCTURES SET SETTINGS=?,VILLAGE=? WHERE WURMID=?";
  private static final String SET_NAME = "UPDATE STRUCTURES SET NAME=? WHERE WURMID=?";
  
  DbStructure(byte theStructureType, String aName, long id, int x, int y, boolean isSurfaced)
  {
    super(theStructureType, aName, id, x, y, isSurfaced);
  }
  
  DbStructure(long id)
    throws IOException, NoSuchStructureException
  {
    super(id);
  }
  
  DbStructure(byte theStructureType, String aName, long aId, boolean aIsSurfaced, byte aRoof, boolean aFinished, boolean aFinFinished, long aWritId, String aPlanner, long aOwnerId, int aSettings, int aVillageId, boolean aAllowsVillagers, boolean aAllowsAllies, boolean aAllowKingdom)
  {
    super(theStructureType, aName, aId, aIsSurfaced, aRoof, aFinished, aFinFinished, aWritId, aPlanner, aOwnerId, aSettings, aVillageId, aAllowsVillagers, aAllowsAllies, aAllowKingdom);
  }
  
  void load()
    throws IOException, NoSuchStructureException
  {
    if (!isLoading())
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      try
      {
        setLoading(true);
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("SELECT * FROM STRUCTURES WHERE WURMID=?");
        ps.setLong(1, getWurmId());
        rs = ps.executeQuery();
        if (rs.next())
        {
          setStructureType(rs.getByte("STRUCTURETYPE"));
          setSurfaced(rs.getBoolean("SURFACED"));
          
          setRoof(rs.getByte("ROOF"));
          
          String lName = rs.getString("NAME");
          if (lName == null) {
            lName = "Unknown structure";
          }
          if (lName.length() >= 50) {
            lName = lName.substring(0, 49);
          }
          setName(lName, false);
          
          this.finished = rs.getBoolean("FINISHED");
          this.finalfinished = rs.getBoolean("FINFINISHED");
          this.allowsVillagers = rs.getBoolean("ALLOWSVILLAGERS");
          this.allowsAllies = rs.getBoolean("ALLOWSALLIES");
          this.allowsKingdom = rs.getBoolean("ALLOWSKINGDOM");
          setPlanner(rs.getString("PLANNER"));
          setOwnerId(rs.getLong("OWNERID"));
          setSettings(rs.getInt("SETTINGS"));
          this.villageId = rs.getInt("VILLAGE");
          if (isTypeHouse()) {
            try
            {
              setWritid(rs.getLong("WRITID"), false);
            }
            catch (SQLException nsi)
            {
              logger.log(Level.INFO, "No writ for house with id:" + getWurmId() + " creating new after loading.", nsi);
            }
          }
        }
        else
        {
          throw new NoSuchStructureException("No structure found with id " + getWurmId());
        }
      }
      catch (SQLException sqex)
      {
        throw new IOException(sqex);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  public void save()
    throws IOException
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      if (!exists(dbcon)) {
        create(dbcon);
      }
      ps = dbcon.prepareStatement("UPDATE STRUCTURES SET CENTERX=?,CENTERY=?,ROOF=?,SURFACED=?,NAME=?,FINISHED=?,WRITID=?,FINFINISHED=?,ALLOWSVILLAGERS=?,ALLOWSALLIES=?,ALLOWSKINGDOM=?,PLANNER=?,OWNERID=?,SETTINGS=?,VILLAGE=? WHERE WURMID=?");
      
      ps.setInt(1, getCenterX());
      ps.setInt(2, getCenterY());
      for (Iterator<VolaTile> it = this.structureTiles.iterator(); it.hasNext();)
      {
        VolaTile t = (VolaTile)it.next();
        Wall[] wallArr = t.getWalls();
        for (int x = 0; x < wallArr.length; x++) {
          try
          {
            wallArr[x].save();
          }
          catch (IOException iox)
          {
            logger.log(Level.WARNING, "Failed to save wall: " + wallArr[x]);
          }
        }
        Floor[] floorArr = t.getFloors();
        for (int x = 0; x < floorArr.length; x++) {
          try
          {
            floorArr[x].save();
          }
          catch (IOException iox)
          {
            logger.log(Level.WARNING, "Failed to save floor: " + floorArr[x]);
          }
        }
        BridgePart[] partsArr = t.getBridgeParts();
        for (int x = 0; x < partsArr.length; x++) {
          try
          {
            partsArr[x].save();
          }
          catch (IOException iox)
          {
            logger.log(Level.WARNING, "Failed to save bridge part: " + partsArr[x]);
          }
        }
      }
      ps.setByte(3, getRoof());
      ps.setBoolean(4, isSurfaced());
      ps.setString(5, getName());
      ps.setBoolean(6, isFinished());
      ps.setLong(7, getWritId());
      ps.setBoolean(8, isFinalFinished());
      ps.setBoolean(9, allowsCitizens());
      ps.setBoolean(10, allowsAllies());
      ps.setBoolean(11, allowsKingdom());
      ps.setString(12, getPlanner());
      ps.setLong(13, getOwnerId());
      ps.setInt(14, getSettings().getPermissions());
      ps.setInt(15, getVillageId());
      ps.setLong(16, getWurmId());
      ps.executeUpdate();
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Problem", sqex);
      throw new IOException(sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private void create(Connection dbcon)
    throws IOException
  {
    PreparedStatement ps = null;
    try
    {
      ps = dbcon.prepareStatement("INSERT INTO STRUCTURES(WURMID, STRUCTURETYPE) VALUES(?,?)");
      ps.setLong(1, getWurmId());
      ps.setByte(2, getStructureType());
      ps.executeUpdate();
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Problem", sqex);
      throw new IOException(sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
    }
  }
  
  private boolean exists(Connection dbcon)
    throws SQLException
  {
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      ps = dbcon.prepareStatement("SELECT * FROM STRUCTURES WHERE WURMID=?");
      ps.setLong(1, getWurmId());
      rs = ps.executeQuery();
      return rs.next();
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
    }
  }
  
  void delete()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("DELETE FROM STRUCTURES WHERE WURMID=?");
      ps.setLong(1, getWurmId());
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to delete structure with id=" + getWurmId(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
    StructureSettings.remove(getWurmId());
    PermissionsHistories.remove(getWurmId());
    Structures.removeStructure(getWurmId());
    deleteAllBuildTiles();
  }
  
  public void endLoading()
    throws IOException
  {
    if (!hasLoaded())
    {
      setHasLoaded(true);
      
      List<Wall> structureWalls = Wall.getWallsAsArrayListFor(getWurmId());
      if (loadStructureTiles(structureWalls)) {
        while (fillHoles()) {
          logger.log(Level.INFO, "Filling holes " + getWurmId());
        }
      }
      Set<Floor> floorset = Floor.getFloorsFor(getWurmId());
      Iterator<Floor> it;
      if (floorset != null) {
        for (it = floorset.iterator(); it.hasNext();) {
          try
          {
            Floor floor = (Floor)it.next();
            int tilex = floor.getTileX();
            int tiley = floor.getTileY();
            
            Zone zone = Zones.getZone(tilex, tiley, isSurfaced());
            VolaTile tile = zone.getOrCreateTile(tilex, tiley);
            if (this.structureTiles.contains(tile)) {
              tile.addFloor(floor);
            } else {
              logger.log(Level.FINE, "Floor #" + floor.getId() + " thinks it belongs to structure " + getWurmId() + " but structureTiles disagrees.");
            }
          }
          catch (NoSuchZoneException e)
          {
            logger.log(Level.WARNING, e.getMessage(), e);
          }
        }
      }
      Set<BridgePart> bridgePartsset = BridgePart.getBridgePartsFor(getWurmId());
      for (Iterator<BridgePart> it = bridgePartsset.iterator(); it.hasNext();) {
        try
        {
          BridgePart bridgePart = (BridgePart)it.next();
          int tilex = bridgePart.getTileX();
          int tiley = bridgePart.getTileY();
          
          Zone zone = Zones.getZone(tilex, tiley, isSurfaced());
          VolaTile tile = zone.getOrCreateTile(tilex, tiley);
          if (this.structureTiles.contains(tile)) {
            tile.addBridgePart(bridgePart);
          } else {
            logger.log(Level.FINE, "BridgePart #" + bridgePart.getId() + " thinks it belongs to structure " + getWurmId() + " but structureTiles disagrees.");
          }
        }
        catch (NoSuchZoneException e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
      Zone northW = null;
      Zone northE = null;
      Zone southW = null;
      Zone southE = null;
      try
      {
        northW = Zones.getZone(this.minX, this.minY, this.surfaced);
        northW.addStructure(this);
      }
      catch (NoSuchZoneException localNoSuchZoneException1) {}
      try
      {
        northE = Zones.getZone(this.maxX, this.minY, this.surfaced);
        if (northE != northW) {
          northE.addStructure(this);
        }
      }
      catch (NoSuchZoneException localNoSuchZoneException2) {}
      try
      {
        southE = Zones.getZone(this.maxX, this.maxY, this.surfaced);
        if ((southE != northE) && (southE != northW)) {
          southE.addStructure(this);
        }
      }
      catch (NoSuchZoneException localNoSuchZoneException3) {}
      try
      {
        southW = Zones.getZone(this.minX, this.maxY, this.surfaced);
        if ((southW != northE) && (southW != northW) && (southW != southE)) {
          southW.addStructure(this);
        }
      }
      catch (NoSuchZoneException localNoSuchZoneException4) {}
    }
  }
  
  public void setFinished(boolean finish)
  {
    if (isFinished() != finish)
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        this.finished = finish;
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("UPDATE STRUCTURES SET FINISHED=? WHERE WURMID=?");
        ps.setBoolean(1, isFinished());
        ps.setLong(2, getWurmId());
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to set finished to " + finish + " for structure " + getName() + " with id " + 
          getWurmId(), sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  public void setFinalFinished(boolean finfinish)
  {
    if (isFinalFinished() != finfinish)
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        this.finalfinished = finfinish;
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("UPDATE STRUCTURES SET FINFINISHED=? WHERE WURMID=?");
        ps.setBoolean(1, isFinalFinished());
        ps.setLong(2, getWurmId());
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to set finfinished to " + finfinish + " for structure " + getName() + " with id " + 
          getWurmId(), sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  public void saveWritId()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("UPDATE STRUCTURES SET WRITID=? WHERE WURMID=?");
      ps.setLong(1, this.writid);
      ps.setLong(2, getWurmId());
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to set writId to " + this.writid + " for structure " + getName() + " with id " + 
        getWurmId(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public void saveOwnerId()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("UPDATE STRUCTURES SET OWNERID=? WHERE WURMID=?");
      ps.setLong(1, this.ownerId);
      ps.setLong(2, getWurmId());
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to set ownerId to " + this.ownerId + " for structure " + getName() + " with id " + 
        getWurmId(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public void saveSettings()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("UPDATE STRUCTURES SET SETTINGS=?,VILLAGE=? WHERE WURMID=?");
      ps.setInt(1, getSettings().getPermissions());
      ps.setInt(2, getVillageId());
      ps.setLong(3, getWurmId());
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to set settings to " + getSettings().getPermissions() + " for structure " + getName() + " with id " + 
        getWurmId(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public void saveName()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("UPDATE STRUCTURES SET NAME=? WHERE WURMID=?");
      ps.setString(1, getName());
      ps.setLong(2, getWurmId());
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to set name to " + getName() + " for structure with id " + getWurmId(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public void setAllowVillagers(boolean allow)
  {
    if (allowsCitizens() != allow)
    {
      this.allowsVillagers = allow;
      if (allow) {
        addDefaultCitizenPermissions();
      } else {
        removeStructureGuest(-30L);
      }
    }
  }
  
  public void setAllowKingdom(boolean allow)
  {
    if (allowsKingdom() != allow)
    {
      this.allowsKingdom = allow;
      if (allow) {
        addDefaultKingdomPermissions();
      } else {
        removeStructureGuest(-40L);
      }
    }
  }
  
  public void setAllowAllies(boolean allow)
  {
    if (allowsAllies() != allow)
    {
      this.allowsAllies = allow;
      if (allow) {
        addDefaultAllyPermissions();
      } else {
        removeStructureGuest(-20L);
      }
    }
  }
  
  public void addNewGuest(long guestId, int aSettings)
  {
    StructureSettings.addPlayer(getWurmId(), guestId, aSettings);
  }
  
  public void removeStructureGuest(long guestId)
  {
    StructureSettings.removePlayer(getWurmId(), guestId);
  }
  
  public void removeBuildTile(int tilex, int tiley, int layer)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("DELETE FROM BUILDTILES WHERE STRUCTUREID=? AND TILEX=? AND TILEY=? AND LAYER=?");
      ps.setLong(1, getWurmId());
      ps.setInt(2, tilex);
      ps.setInt(3, tiley);
      ps.setInt(4, layer);
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to remove build tile for structure with id " + getWurmId(), ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public void addNewBuildTile(int tilex, int tiley, int layer)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("INSERT INTO BUILDTILES(STRUCTUREID,TILEX,TILEY,LAYER) VALUES (?,?,?,?)");
      
      ps.setLong(1, getWurmId());
      ps.setInt(2, tilex);
      ps.setInt(3, tiley);
      ps.setInt(4, layer);
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to add build tile for structure with id " + getWurmId(), ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public static final void loadBuildTiles()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM BUILDTILES");
      rs = ps.executeQuery();
      while (rs.next()) {
        try
        {
          Structure structure = Structures.getStructure(rs.getLong("STRUCTUREID"));
          structure.addBuildTile(new BuildTile(rs.getInt("TILEX"), rs.getInt("TILEY"), rs.getInt("LAYER")));
        }
        catch (NoSuchStructureException nss)
        {
          logger.log(Level.WARNING, nss.getMessage());
        }
      }
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to load all tiles for structures" + ex.getMessage(), ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public void deleteAllBuildTiles()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("DELETE FROM BUILDTILES WHERE STRUCTUREID=?");
      ps.setLong(1, getWurmId());
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to delete all build tiles for structure with id " + getWurmId(), ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public boolean isItem()
  {
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\structures\DbStructure.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */