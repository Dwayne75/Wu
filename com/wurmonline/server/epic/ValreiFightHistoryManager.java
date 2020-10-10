package com.wurmonline.server.epic;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ValreiFightHistoryManager
{
  private static final Logger logger = Logger.getLogger(ValreiFightHistoryManager.class.getName());
  private static final String GET_MAX_FIGHTID = "SELECT MAX(FIGHTID) FROM ENTITYFIGHTS";
  private static final String LOAD_FIGHTS = "SELECT * FROM ENTITYFIGHTS";
  private static final String SAVE_FIGHT = "INSERT INTO ENTITYFIGHTS(FIGHTID,MAPHEXID,MAPHEXNAME,FIGHTTIME,FIGHTER1ID,FIGHTER1NAME,FIGHTER2ID,FIGHTER2NAME) VALUES (?,?,?,?,?,?,?,?)";
  private static ValreiFightHistoryManager instance;
  private static long fightIdCounter = 1L;
  private TreeMap<Long, ValreiFightHistory> allFights;
  
  static
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getDeityDbCon();
      ps = dbcon.prepareStatement("SELECT MAX(FIGHTID) FROM ENTITYFIGHTS");
      
      rs = ps.executeQuery();
      if (rs.next()) {
        fightIdCounter = rs.getLong("MAX(FIGHTID)");
      }
      rs.close();
      ps.close();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to load max fight id: " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public static long getNextFightId()
  {
    return ++fightIdCounter;
  }
  
  public static ValreiFightHistoryManager getInstance()
  {
    if (instance == null) {
      instance = new ValreiFightHistoryManager();
    }
    return instance;
  }
  
  private ValreiFightHistoryManager()
  {
    loadAllFights();
  }
  
  private void saveFight(long fightId, ValreiFightHistory newFight)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getDeityDbCon();
      ps = dbcon.prepareStatement("INSERT INTO ENTITYFIGHTS(FIGHTID,MAPHEXID,MAPHEXNAME,FIGHTTIME,FIGHTER1ID,FIGHTER1NAME,FIGHTER2ID,FIGHTER2NAME) VALUES (?,?,?,?,?,?,?,?)");
      
      ps.setLong(1, fightId);
      ps.setInt(2, newFight.getMapHexId());
      ps.setString(3, newFight.getMapHexName());
      ps.setLong(4, newFight.getFightTime());
      
      HashMap<Long, ValreiFightHistory.ValreiFighter> fighters = newFight.getFighters();
      int val;
      if (fighters.size() >= 2)
      {
        val = 5;
        for (ValreiFightHistory.ValreiFighter f : fighters.values())
        {
          ps.setLong(val++, f.getFighterId());
          ps.setString(val++, f.getName());
          if (val >= 8) {
            break;
          }
        }
      }
      else
      {
        ps.setLong(5, -1L);
        ps.setString(6, "Unknown");
        ps.setLong(7, -1L);
        ps.setString(8, "Unknown");
      }
      ps.executeUpdate();
      ps.close();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to save valrei fight: " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private void loadAllFights()
  {
    this.allFights = new TreeMap();
    
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getDeityDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM ENTITYFIGHTS");
      
      rs = ps.executeQuery();
      while (rs.next())
      {
        long fightId = rs.getLong("FIGHTID");
        int mapHexId = rs.getInt("MAPHEXID");
        String mapHexName = rs.getString("MAPHEXNAME");
        long fightTime = rs.getLong("FIGHTTIME");
        long fighter1 = rs.getLong("FIGHTER1ID");
        String fighter1Name = rs.getString("FIGHTER1NAME");
        long fighter2 = rs.getLong("FIGHTER2ID");
        String fighter2Name = rs.getString("FIGHTER2NAME");
        
        ValreiFightHistory oldFight = new ValreiFightHistory(fightId, mapHexId, mapHexName, fightTime);
        oldFight.addFighter(fighter1, fighter1Name);
        oldFight.addFighter(fighter2, fighter2Name);
        oldFight.loadActions();
        
        this.allFights.put(Long.valueOf(fightId), oldFight);
      }
      rs.close();
      ps.close();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to load all valrei fights: " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public int getNumberOfFights()
  {
    return this.allFights.size();
  }
  
  public void addFight(long fightId, ValreiFightHistory newFight)
  {
    addFight(fightId, newFight, true);
  }
  
  public void addFight(long fightId, ValreiFightHistory newFight, boolean save)
  {
    this.allFights.put(Long.valueOf(fightId), newFight);
    if (save) {
      saveFight(fightId, newFight);
    }
  }
  
  public ValreiFightHistory getFight(long fightId)
  {
    return (ValreiFightHistory)this.allFights.get(Long.valueOf(fightId));
  }
  
  public ArrayList<ValreiFightHistory> get10Fights(int listPage)
  {
    if (this.allFights.size() / 10 < listPage) {
      return null;
    }
    try
    {
      ArrayList<ValreiFightHistory> toReturn = new ArrayList();
      long finalKey = ((Long)this.allFights.lastKey()).longValue();
      for (int i = 0; i < listPage; i++) {
        for (int k = 0; k < 10; k++)
        {
          if (this.allFights.lowerKey(Long.valueOf(finalKey)) == null) {
            break;
          }
          finalKey = ((Long)this.allFights.lowerKey(Long.valueOf(finalKey))).longValue();
        }
      }
      for (int i = 0; i < 10; i++)
      {
        toReturn.add(this.allFights.get(Long.valueOf(finalKey)));
        if (this.allFights.lowerKey(Long.valueOf(finalKey)) == null) {
          break;
        }
        finalKey = ((Long)this.allFights.lowerKey(Long.valueOf(finalKey))).longValue();
      }
      return toReturn;
    }
    catch (NoSuchElementException ne)
    {
      logger.log(Level.WARNING, "Unable to load 10 fights for page " + listPage + ". No key exists in allFights map.");
    }
    return null;
  }
  
  public ArrayList<ValreiFightHistory> getAllFights()
  {
    return new ArrayList(this.allFights.values());
  }
  
  public ValreiFightHistory getLatestFight()
  {
    return (ValreiFightHistory)this.allFights.get(this.allFights.lastKey());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\epic\ValreiFightHistoryManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */