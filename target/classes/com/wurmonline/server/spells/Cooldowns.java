package com.wurmonline.server.spells;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Cooldowns
  implements TimeConstants
{
  private static final Logger logger = Logger.getLogger(Cooldowns.class.getName());
  private static final String loadCooldowns = "SELECT * FROM COOLDOWNS";
  private static final String deleteCooldownsFor = "DELETE FROM COOLDOWNS WHERE OWNERID=?";
  private static final String createCooldown = "INSERT INTO COOLDOWNS (OWNERID,SPELLID,AVAILABLE) VALUES(?,?,?)";
  private static final String updateCooldown = "UPDATE COOLDOWNS SET AVAILABLE=? WHERE OWNERID=? AND SPELLID=?";
  public final Map<Integer, Long> cooldowns = new HashMap();
  private static final Map<Long, Cooldowns> allCooldowns = new HashMap();
  private final long ownerid;
  
  private Cooldowns(long _ownerid)
  {
    this.ownerid = _ownerid;
  }
  
  public static final Cooldowns getCooldownsFor(long creatureId, boolean create)
  {
    Cooldowns cd = (Cooldowns)allCooldowns.get(Long.valueOf(creatureId));
    if ((create) && (cd == null))
    {
      cd = new Cooldowns(creatureId);
      allCooldowns.put(Long.valueOf(creatureId), cd);
    }
    return cd;
  }
  
  public void addCooldown(int spellid, long availableAt, boolean loading)
  {
    boolean update = this.cooldowns.containsKey(Integer.valueOf(spellid));
    this.cooldowns.put(Integer.valueOf(spellid), Long.valueOf(availableAt));
    if (!loading) {
      if (System.currentTimeMillis() - availableAt > 600000L) {
        if (update) {
          updateToDisk(spellid, availableAt);
        } else {
          saveToDisk(spellid, availableAt);
        }
      }
    }
  }
  
  public long isAvaibleAt(int spellid)
  {
    Integer tocheck = Integer.valueOf(spellid);
    if (this.cooldowns.containsKey(tocheck)) {
      return ((Long)this.cooldowns.get(tocheck)).longValue();
    }
    return 0L;
  }
  
  private void saveToDisk(int spellid, long availableAt)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      
      ps = dbcon.prepareStatement("INSERT INTO COOLDOWNS (OWNERID,SPELLID,AVAILABLE) VALUES(?,?,?)");
      ps.setLong(1, this.ownerid);
      ps.setInt(2, spellid);
      ps.setLong(3, availableAt);
      ps.executeUpdate();
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, sqex.getMessage(), sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private void updateToDisk(int spellid, long availableAt)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      
      ps = dbcon.prepareStatement("UPDATE COOLDOWNS SET AVAILABLE=? WHERE OWNERID=? AND SPELLID=?");
      ps.setLong(1, availableAt);
      ps.setLong(2, this.ownerid);
      ps.setInt(3, spellid);
      ps.executeUpdate();
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, sqex.getMessage(), sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public static final void deleteCooldownsFor(long ownerId)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("DELETE FROM COOLDOWNS WHERE OWNERID=?");
      ps.setLong(1, ownerId);
      ps.executeUpdate();
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, sqex.getMessage(), sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
    allCooldowns.remove(Long.valueOf(ownerId));
  }
  
  public static final void loadAllCooldowns()
  {
    logger.log(Level.INFO, "Loading all cooldowns.");
    long start = System.nanoTime();
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM COOLDOWNS");
      rs = ps.executeQuery();
      while (rs.next())
      {
        long ownerId = rs.getLong("OWNERID");
        Cooldowns cd = getCooldownsFor(ownerId, false);
        if (cd == null) {
          cd = new Cooldowns(ownerId);
        }
        cd.addCooldown(rs.getInt("SPELLID"), rs.getLong("AVAILABLE"), true);
        allCooldowns.put(Long.valueOf(ownerId), cd);
      }
    }
    catch (SQLException sqx)
    {
      long end;
      logger.log(Level.WARNING, sqx.getMessage(), sqx);
    }
    finally
    {
      long end;
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
      
      long end = System.nanoTime();
      logger.info("Loaded cooldowns from database took " + (float)(end - start) / 1000000.0F + " ms");
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\Cooldowns.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */