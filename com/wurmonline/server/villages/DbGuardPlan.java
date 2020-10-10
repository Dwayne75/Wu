package com.wurmonline.server.villages;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.shared.constants.CounterTypes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbGuardPlan
  extends GuardPlan
  implements CounterTypes
{
  private static final Logger logger = Logger.getLogger(DbGuardPlan.class.getName());
  private static final String CREATE_GUARDPLAN = "INSERT INTO GUARDPLAN (VILLAGEID,  TYPE, LASTPAYED,MONEYLEFT, GUARDS) VALUES(?,?,?,?,?)";
  private static final String CHANGE_PLAN = "UPDATE GUARDPLAN SET LASTPAYED=?,TYPE=?,MONEYLEFT=?, GUARDS=? WHERE VILLAGEID=?";
  private static final String LOAD_PLAN = "SELECT * FROM GUARDPLAN WHERE VILLAGEID=?";
  private static final String DELETE_GUARDPLAN = "DELETE FROM GUARDPLAN WHERE VILLAGEID=?";
  private static final String ADD_RETURNEDGUARD = "INSERT INTO RETURNEDGUARDS (VILLAGEID, CREATUREID ) VALUES(?,?)";
  private static final String DELETE_RETURNEDGUARD = "DELETE FROM RETURNEDGUARDS WHERE CREATUREID=?";
  private static final String LOAD_RETURNEDGUARDS = "SELECT CREATUREID FROM RETURNEDGUARDS WHERE VILLAGEID=?";
  private static final String ADD_PAYMENT = "INSERT INTO GUARDPLANPAYMENTS (VILLAGEID, CREATUREID,MONEY,PAYED ) VALUES(?,?,?,?)";
  private static final String SET_LAST_DRAINED = "UPDATE GUARDPLAN SET LASTDRAINED=?, MONEYLEFT=? WHERE VILLAGEID=?";
  private static final String SET_DRAINMOD = "UPDATE GUARDPLAN SET DRAINMOD=? WHERE VILLAGEID=?";
  
  DbGuardPlan(int aType, int aVillageId)
  {
    super(aType, aVillageId);
  }
  
  DbGuardPlan(int aVillageId)
  {
    super(aVillageId);
  }
  
  void load()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM GUARDPLAN WHERE VILLAGEID=?");
      ps.setInt(1, this.villageId);
      rs = ps.executeQuery();
      boolean found = false;
      if (rs.next())
      {
        found = true;
        this.type = rs.getInt("TYPE");
        this.lastChangedPlan = rs.getLong("LASTPAYED");
        this.moneyLeft = rs.getLong("MONEYLEFT");
        this.lastDrained = rs.getLong("LASTDRAINED");
        this.drainModifier = rs.getFloat("DRAINMOD");
        this.hiredGuardNumber = rs.getInt("GUARDS");
        loadReturnedGuards();
      }
      if (!found)
      {
        this.type = 1;
        create();
      }
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  void create()
  {
    this.lastChangedPlan = 0L;
    this.moneyLeft = 0L;
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("INSERT INTO GUARDPLAN (VILLAGEID,  TYPE, LASTPAYED,MONEYLEFT, GUARDS) VALUES(?,?,?,?,?)");
      ps.setInt(1, this.villageId);
      ps.setInt(2, this.type);
      ps.setLong(3, this.lastChangedPlan);
      ps.setLong(4, this.moneyLeft);
      ps.setInt(5, this.hiredGuardNumber);
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
  
  public void updateGuardPlan(int aType, long aMoneyLeft, int newNumberOfHiredGuards)
  {
    this.type = aType;
    this.moneyLeft = aMoneyLeft;
    
    this.hiredGuardNumber = newNumberOfHiredGuards;
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("UPDATE GUARDPLAN SET LASTPAYED=?,TYPE=?,MONEYLEFT=?, GUARDS=? WHERE VILLAGEID=?");
      ps.setLong(1, this.lastChangedPlan);
      ps.setInt(2, aType);
      ps.setLong(3, aMoneyLeft);
      ps.setInt(4, this.hiredGuardNumber);
      ps.setInt(5, this.villageId);
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
  
  void drainGuardPlan(long aMoneyLeft)
  {
    this.moneyLeft = aMoneyLeft;
    this.lastDrained = System.currentTimeMillis();
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("UPDATE GUARDPLAN SET LASTDRAINED=?, MONEYLEFT=? WHERE VILLAGEID=?");
      ps.setLong(1, this.lastDrained);
      ps.setLong(2, aMoneyLeft);
      ps.setInt(3, this.villageId);
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
  
  void saveDrainMod()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("UPDATE GUARDPLAN SET DRAINMOD=? WHERE VILLAGEID=?");
      ps.setFloat(1, this.drainModifier);
      ps.setInt(2, this.villageId);
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
  
  void delete()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("DELETE FROM GUARDPLAN WHERE VILLAGEID=?");
      ps.setInt(1, this.villageId);
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
    loadReturnedGuards();
    deleteReturnedGuards();
  }
  
  void deleteReturnedGuards()
  {
    ListIterator<Creature> it;
    if (this.freeGuards.size() > 0) {
      for (it = this.freeGuards.listIterator(); it.hasNext();)
      {
        Creature guard = (Creature)it.next();
        removeReturnedGuard(guard.getWurmId());
        guard.destroy();
      }
    }
  }
  
  void addReturnedGuard(long guardId)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("INSERT INTO RETURNEDGUARDS (VILLAGEID, CREATUREID ) VALUES(?,?)");
      ps.setInt(1, this.villageId);
      ps.setLong(2, guardId);
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
  
  void removeReturnedGuard(long guardId)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("DELETE FROM RETURNEDGUARDS WHERE CREATUREID=?");
      ps.setLong(1, guardId);
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
  
  private void loadReturnedGuards()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("SELECT CREATUREID FROM RETURNEDGUARDS WHERE VILLAGEID=?");
      ps.setInt(1, this.villageId);
      rs = ps.executeQuery();
      while (rs.next())
      {
        long cid = rs.getLong("CREATUREID");
        try
        {
          Creature guard = Creatures.getInstance().getCreature(cid);
          this.freeGuards.add(guard);
        }
        catch (NoSuchCreatureException nsc)
        {
          logger.log(Level.WARNING, "Failed to retrieve creature " + cid, nsc);
        }
      }
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public void addPayment(String creatureName, long creatureId, long money)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("INSERT INTO GUARDPLANPAYMENTS (VILLAGEID, CREATUREID,MONEY,PAYED ) VALUES(?,?,?,?)");
      ps.setInt(1, this.villageId);
      ps.setLong(2, creatureId);
      ps.setLong(3, money);
      ps.setLong(4, WurmCalendar.currentTime);
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
    try
    {
      if (WurmId.getType(creatureId) == 0) {
        getVillage().addHistory(creatureName, "added " + 
          Economy.getEconomy().getChangeFor(money).getChangeString() + " to upkeep");
      }
    }
    catch (NoSuchVillageException nsv)
    {
      logger.log(Level.WARNING, creatureName + " tried to add " + money + " irons to nonexistant village with id " + this.villageId, nsv);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\villages\DbGuardPlan.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */