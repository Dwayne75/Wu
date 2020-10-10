package com.wurmonline.server.items;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ItemRequirement
{
  private static final Logger logger = Logger.getLogger(ItemRequirement.class.getName());
  private static final String loadItemRequirements = "SELECT * FROM ITEMREQUIREMENTS";
  private static final String deleteItemRequirements = "DELETE FROM ITEMREQUIREMENTS WHERE WURMID=?";
  private static final String updateItemRequirements = "UPDATE ITEMREQUIREMENTS SET ITEMSDONE=? WHERE WURMID=? AND TEMPLATEID=?";
  private static final String createItemRequirements = "INSERT INTO ITEMREQUIREMENTS (ITEMSDONE, WURMID, TEMPLATEID) VALUES(?,?,?)";
  private final int templateId;
  private int numsDone;
  private static final Map<Long, Set<ItemRequirement>> requirements = new HashMap();
  private static boolean found = false;
  
  private ItemRequirement(int aItemTemplateId, int aNumbersDone)
  {
    this.templateId = aItemTemplateId;
    this.numsDone = aNumbersDone;
  }
  
  public static void loadAllItemRequirements()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getItemDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM ITEMREQUIREMENTS");
      rs = ps.executeQuery();
      while (rs.next()) {
        setRequirements(rs.getLong("WURMID"), rs.getInt("TEMPLATEID"), rs.getInt("ITEMSDONE"), false, false);
      }
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed loading item reqs " + ex.getMessage(), ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public static void setRequirements(long _wurmid, int _templateId, int _numsDone, boolean save, boolean create)
  {
    found = false;
    Set<ItemRequirement> doneset = (Set)requirements.get(Long.valueOf(_wurmid));
    if (doneset == null)
    {
      doneset = new HashSet();
      requirements.put(Long.valueOf(_wurmid), doneset);
    }
    for (ItemRequirement next : doneset) {
      if (next.templateId == _templateId)
      {
        next.numsDone = _numsDone;
        found = true;
      }
    }
    if (!found)
    {
      ItemRequirement newreq = new ItemRequirement(_templateId, _numsDone);
      doneset.add(newreq);
    }
    if (save) {
      updateDatabaseRequirements(_wurmid, _templateId, _numsDone, create);
    }
  }
  
  public final int getTemplateId()
  {
    return this.templateId;
  }
  
  public final int getNumsDone()
  {
    return this.numsDone;
  }
  
  public static void deleteRequirements(long _wurmid)
  {
    requirements.remove(Long.valueOf(_wurmid));
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getItemDbCon();
      ps = dbcon.prepareStatement("DELETE FROM ITEMREQUIREMENTS WHERE WURMID=?");
      ps.setLong(1, _wurmid);
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to delete reqs " + _wurmid, ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public static final Set<ItemRequirement> getRequirements(long wurmid)
  {
    return (Set)requirements.get(Long.valueOf(wurmid));
  }
  
  public static void updateDatabaseRequirements(long _wurmid, int _templateId, int numsDone, boolean create)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getItemDbCon();
      if ((numsDone == 1) || (create)) {
        ps = dbcon.prepareStatement("INSERT INTO ITEMREQUIREMENTS (ITEMSDONE, WURMID, TEMPLATEID) VALUES(?,?,?)");
      } else {
        ps = dbcon.prepareStatement("UPDATE ITEMREQUIREMENTS SET ITEMSDONE=? WHERE WURMID=? AND TEMPLATEID=?");
      }
      ps.setInt(1, numsDone);
      ps.setLong(2, _wurmid);
      ps.setInt(3, _templateId);
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to update reqs " + _wurmid + ",tid=" + _templateId + ", nums=" + numsDone, ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  static int getStateForRequirement(int _templateId, long _wurmId)
  {
    Set<ItemRequirement> doneSet = (Set)requirements.get(Long.valueOf(_wurmId));
    if (doneSet != null) {
      for (ItemRequirement next : doneSet) {
        if (next.templateId == _templateId) {
          return next.numsDone;
        }
      }
    }
    return 0;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\items\ItemRequirement.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */