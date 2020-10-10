package com.wurmonline.server.villages;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbGuard
  extends Guard
{
  private static final Logger logger = Logger.getLogger(DbGuard.class.getName());
  private static final String SET_EXPIREDATE = "UPDATE GUARDS SET EXPIREDATE=? WHERE WURMID=?";
  private static final String CREATE_GUARD = "INSERT INTO GUARDS (WURMID, VILLAGEID, EXPIREDATE ) VALUES (?,?,?)";
  static final String DELETE_GUARD = "DELETE FROM GUARDS WHERE WURMID=?";
  
  DbGuard(int aVillageId, Creature aCreature, long aExpireDate)
  {
    super(aVillageId, aCreature, aExpireDate);
  }
  
  void save()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("INSERT INTO GUARDS (WURMID, VILLAGEID, EXPIREDATE ) VALUES (?,?,?)");
      ps.setLong(1, this.creature.getWurmId());
      ps.setInt(2, this.villageId);
      ps.setLong(3, this.expireDate);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  void setExpireDate(long newDate)
  {
    if (this.expireDate != newDate)
    {
      this.expireDate = newDate;
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("UPDATE GUARDS SET EXPIREDATE=? WHERE WURMID=?");
        ps.setLong(1, this.expireDate);
        ps.setLong(2, this.creature.getWurmId());
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, sqx.getMessage(), sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  void delete()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("DELETE FROM GUARDS WHERE WURMID=?");
      ps.setLong(1, this.creature.getWurmId());
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\villages\DbGuard.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */