package com.wurmonline.server.villages;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbAlliance
  extends Alliance
{
  private static final Logger logger = Logger.getLogger(DbAlliance.class.getName());
  private static final String createAlliance = "INSERT INTO ALLIANCES (VILLONE, VILLTWO) VALUES (?,?)";
  private static final String deleteAlliance = "DELETE FROM ALLIANCES WHERE VILLONE=? AND VILLTWO=?";
  
  DbAlliance(Village vone, Village vtwo)
  {
    super(vone, vtwo);
  }
  
  void save()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("INSERT INTO ALLIANCES (VILLONE, VILLTWO) VALUES (?,?)");
      ps.setInt(1, this.villone.getId());
      ps.setInt(2, this.villtwo.getId());
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to create alliance between " + this.villone.getName() + " and " + this.villtwo.getName(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  void delete()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("DELETE FROM ALLIANCES WHERE VILLONE=? AND VILLTWO=?");
      ps.setInt(1, this.villone.getId());
      ps.setInt(2, this.villtwo.getId());
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to delete alliance between " + this.villone.getName() + " and " + this.villtwo.getName(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\villages\DbAlliance.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */