package com.wurmonline.server.epic;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.shared.constants.ValreiConstants;
import com.wurmonline.shared.constants.ValreiConstants.ValreiFightAction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ValreiFightHistory
{
  private static final Logger logger = Logger.getLogger(ValreiFightHistoryManager.class.getName());
  private static final String LOAD_FIGHT_ACTIONS = "SELECT * FROM ENTITYFIGHTACTIONS WHERE FIGHTID=?";
  private static final String SAVE_FIGHT_ACTION = "INSERT INTO ENTITYFIGHTACTIONS(FIGHTID,FIGHTACTIONNUM,ACTIONID,ACTIONDATA) VALUES (?,?,?,?)";
  private final long fightId;
  private int mapHexId;
  private String mapHexName;
  private long fightTime;
  private HashMap<Long, ValreiFightHistory.ValreiFighter> fighters;
  private HashMap<Integer, ValreiConstants.ValreiFightAction> allActions;
  private int fightActionNum;
  private boolean fightCompleted;
  
  public ValreiFightHistory(int mapHexId, String mapHexName)
  {
    this.mapHexId = mapHexId;
    this.mapHexName = mapHexName;
    this.fightId = ValreiFightHistoryManager.getNextFightId();
    
    this.fighters = new HashMap();
    this.allActions = new HashMap();
    this.fightActionNum = 0;
    this.fightCompleted = false;
    this.fightTime = WurmCalendar.currentTime;
  }
  
  public ValreiFightHistory(long fightId, int mapHexId, String mapHexName, long fightTime)
  {
    this.fightId = fightId;
    this.mapHexId = mapHexId;
    this.mapHexName = mapHexName;
    this.fightTime = fightTime;
    
    this.fighters = new HashMap();
    this.allActions = new HashMap();
    this.fightCompleted = true;
  }
  
  public void saveActions()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getDeityDbCon();
      for (ValreiConstants.ValreiFightAction fa : this.allActions.values())
      {
        ps = dbcon.prepareStatement("INSERT INTO ENTITYFIGHTACTIONS(FIGHTID,FIGHTACTIONNUM,ACTIONID,ACTIONDATA) VALUES (?,?,?,?)");
        ps.setLong(1, this.fightId);
        ps.setInt(2, fa.getActionNum());
        ps.setShort(3, fa.getActionId());
        ps.setBytes(4, fa.getActionData());
        
        ps.executeUpdate();
        ps.close();
      }
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to save actions for this valrei fight: " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public void loadActions()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getDeityDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM ENTITYFIGHTACTIONS WHERE FIGHTID=?");
      ps.setLong(1, this.fightId);
      
      rs = ps.executeQuery();
      while (rs.next())
      {
        int actionNum = rs.getInt("FIGHTACTIONNUM");
        short action = rs.getShort("ACTIONID");
        byte[] actionData = rs.getBytes("ACTIONDATA");
        if (this.fightActionNum < actionNum) {
          this.fightActionNum = actionNum;
        }
        this.allActions.put(Integer.valueOf(actionNum), new ValreiConstants.ValreiFightAction(actionNum, action, actionData));
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
  
  public void addFighter(long fighterId, String fighterName)
  {
    if (this.fighters.get(Long.valueOf(fighterId)) == null)
    {
      this.fighters.put(Long.valueOf(fighterId), new ValreiFightHistory.ValreiFighter(this, fighterId, fighterName));
    }
    else
    {
      ValreiFightHistory.ValreiFighter f = (ValreiFightHistory.ValreiFighter)this.fighters.get(Long.valueOf(fighterId));
      f.setName(fighterName);
    }
  }
  
  public HashMap<Long, ValreiFightHistory.ValreiFighter> getFighters()
  {
    return this.fighters;
  }
  
  public void addAction(short actionType, byte[] actionData)
  {
    this.allActions.put(Integer.valueOf(this.fightActionNum), new ValreiConstants.ValreiFightAction(this.fightActionNum, actionType, actionData));
    this.fightActionNum += 1;
  }
  
  public ValreiConstants.ValreiFightAction getFightAction(int actionNum)
  {
    return (ValreiConstants.ValreiFightAction)this.allActions.get(Integer.valueOf(actionNum));
  }
  
  public String getPreviewString()
  {
    if (!this.fighters.isEmpty())
    {
      ArrayList<String> fighters = new ArrayList();
      for (ValreiFightHistory.ValreiFighter vf : this.fighters.values()) {
        fighters.add(ValreiFightHistory.ValreiFighter.access$000(vf));
      }
      return (String)fighters.get(0) + " vs " + (String)fighters.get(1) + " at " + getMapHexName() + " on " + WurmCalendar.getDateFor(this.fightTime);
    }
    return "Unknown fight on " + WurmCalendar.getDateFor(this.fightTime);
  }
  
  public long getFightWinner()
  {
    byte[] actionData = ((ValreiConstants.ValreiFightAction)this.allActions.get(Integer.valueOf(this.fightActionNum))).getActionData();
    
    return ValreiConstants.getEndFightWinner(actionData);
  }
  
  public long getFightId()
  {
    return this.fightId;
  }
  
  public int getMapHexId()
  {
    return this.mapHexId;
  }
  
  public String getMapHexName()
  {
    return this.mapHexName;
  }
  
  public boolean isFightCompleted()
  {
    return this.fightCompleted;
  }
  
  public void setFightCompleted(boolean isCompleted)
  {
    this.fightCompleted = isCompleted;
    this.fightActionNum -= 1;
  }
  
  public long getFightTime()
  {
    return this.fightTime;
  }
  
  public int getTotalActions()
  {
    return this.fightActionNum;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\epic\ValreiFightHistory.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */