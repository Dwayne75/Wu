package com.wurmonline.server.zones;

import com.wurmonline.mesh.Tiles.TileBorderDirection;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.structures.DbFence;
import com.wurmonline.server.structures.DbFenceGate;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.FenceGate;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.StructureConstantsEnum;
import com.wurmonline.shared.constants.StructureStateEnum;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

final class DbZone
  extends Zone
  implements CounterTypes
{
  private static final Logger logger = Logger.getLogger(DbZone.class.getName());
  private static final String GET_FENCES = "Select * from FENCES where ZONEID=?";
  private static final String DELETE_FENCES = "DELETE from FENCES where ZONEID=?";
  
  DbZone(int aStartX, int aEndX, int aStartY, int aEndY, boolean aIsOnSurface)
    throws IOException
  {
    super(aStartX, aEndX, aStartY, aEndY, aIsOnSurface);
    this.zoneWatchers = new HashSet();
    this.structures = new HashSet();
  }
  
  void load()
    throws IOException
  {}
  
  void loadFences()
    throws IOException
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("Select * from FENCES where ZONEID=?");
      ps.setInt(1, this.id);
      rs = ps.executeQuery();
      while (rs.next())
      {
        int fid = -10;
        try
        {
          fid = rs.getInt("ID");
          int tilex = rs.getInt("TILEX");
          int tiley = rs.getInt("TILEY");
          float currentQL = rs.getFloat("ORIGINALQL");
          float originalQL = rs.getFloat("CURRENTQL");
          long lastUsed = rs.getLong("LASTMAINTAINED");
          StructureConstantsEnum type = StructureConstantsEnum.getEnumByValue(rs.getShort("TYPE"));
          StructureStateEnum state = StructureStateEnum.getStateByValue(rs.getByte("STATE"));
          int color = rs.getInt("COLOR");
          int dir = rs.getByte("DIR");
          float damage = rs.getFloat("DAMAGE");
          int heightOffset = rs.getInt("HEIGHTOFFSET");
          int layer = rs.getInt("LAYER");
          int settings = rs.getInt("SETTINGS");
          Fence fence = new DbFence(fid, type, state, color, tilex, tiley, heightOffset, currentQL, originalQL, lastUsed, dir == 0 ? Tiles.TileBorderDirection.DIR_HORIZ : Tiles.TileBorderDirection.DIR_DOWN, this.id, this.isOnSurface, damage, layer, settings);
          if ((dir == 3) || (dir == 1))
          {
            try
            {
              fence.delete();
            }
            catch (Exception ex)
            {
              logger.log(Level.WARNING, "Failed to delete fence " + ex.getMessage(), ex);
            }
          }
          else
          {
            addFence(fence);
            if (fence.isDoor()) {
              if (fence.isFinished())
              {
                FenceGate gate = new DbFenceGate(fence);
                gate.addToTiles();
              }
            }
          }
        }
        catch (SQLException iox)
        {
          logger.log(Level.WARNING, "Failed to load fence with id " + fid, iox);
        }
      }
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to load fences for zone with id " + this.id, sqx);
      throw new IOException(sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  void save()
    throws IOException
  {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\zones\DbZone.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */