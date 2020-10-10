package com.wurmonline.server.villages;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public final class VillagePerimeter
{
  private static final Logger logger = Logger.getLogger(VillagePerimeter.class.getName());
  private final int villageId;
  private static final String INSERT_PERIMETERVALUES = "INSERT INTO VILLAGEPERIMETERS(SETTINGS,ID) VALUES (?,?)";
  private static final String UPDATE_PERIMETERVALUES = "UPDATE VILLAGEPERIMETERS SET SETTINGS=? WHERE ID=?";
  private static final String DELETE_PERIMETERVALUES = "DELETE FROM VILLAGEPERIMETERS WHERE ID=?";
  private static final String INSERT_PERIMETERFRIEND = "INSERT INTO PERIMETERFRIENDS(ID,NAME) VALUES (?,?)";
  private static final String DELETE_PERIMETERFRIEND = "DELETE FROM PERIMETERFRIENDS WHERE NAME=? AND ID=?";
  private static final String DELETE_PERIMETERFRIENDVILLAGE = "DELETE FROM PERIMETERFRIENDS WHERE ID=?";
  private final Set<String> perimeterFriends = new HashSet();
  private static final Map<Integer, VillagePerimeter> parmap = new HashMap();
  private long settings;
  private static final String[] emptyFriends = new String[0];
  
  VillagePerimeter(int aVillageId)
  {
    this.villageId = aVillageId;
  }
  
  static VillagePerimeter getPerimeter(int villageId)
  {
    return (VillagePerimeter)parmap.get(Integer.valueOf(villageId));
  }
  
  static void removePerimeter(int villageId)
  {
    parmap.remove(Integer.valueOf(villageId));
  }
  
  void create()
    throws IOException
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("INSERT INTO VILLAGEPERIMETERS(SETTINGS,ID) VALUES (?,?)");
      ps.setLong(1, this.settings);
      ps.setInt(2, this.villageId);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      throw new IOException(sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  void update()
    throws IOException
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("UPDATE VILLAGEPERIMETERS SET SETTINGS=? WHERE ID=?");
      ps.setLong(1, this.settings);
      ps.setInt(2, this.villageId);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      throw new IOException(sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  void delete()
    throws IOException
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("DELETE FROM VILLAGEPERIMETERS WHERE ID=?");
      ps.setInt(1, this.villageId);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      throw new IOException(sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
    deleteAllFriend();
  }
  
  void setAllowsFenceDestruction(boolean allow)
    throws IOException
  {
    boolean allowFenceDestruction = (this.settings & 1L) == 1L;
    if (allow != allowFenceDestruction)
    {
      if (allow) {
        this.settings += 1L;
      } else {
        this.settings -= 1L;
      }
      update();
    }
  }
  
  void setAllowsRoadDestruction(boolean allow)
    throws IOException
  {
    boolean allowRoadDestruction = (this.settings >> 1 & 1L) == 1L;
    if (allow != allowRoadDestruction)
    {
      if (allow) {
        this.settings += 2L;
      } else {
        this.settings -= 2L;
      }
      update();
    }
  }
  
  void setAllowsFenceBuilding(boolean allow)
    throws IOException
  {
    boolean allowFenceBuilding = (this.settings >> 2 & 1L) == 1L;
    if (allow != allowFenceBuilding)
    {
      if (allow) {
        this.settings += 4L;
      } else {
        this.settings -= 4L;
      }
      update();
    }
  }
  
  void setAllowsRoadBuilding(boolean allow)
    throws IOException
  {
    boolean allowRoadBuilding = (this.settings >> 3 & 1L) == 1L;
    if (allow != allowRoadBuilding)
    {
      if (allow) {
        this.settings += 8L;
      } else {
        this.settings -= 8L;
      }
      update();
    }
  }
  
  void setAllowsBuildings(boolean allow)
    throws IOException
  {
    boolean allowBuildings = (this.settings >> 4 & 1L) == 1L;
    if (allow != allowBuildings)
    {
      if (allow) {
        this.settings += 16L;
      } else {
        this.settings -= 16L;
      }
      update();
    }
  }
  
  void setAllowsPerimeterActionsForAllies(boolean allow)
    throws IOException
  {
    boolean allowPerimeterActionsForAllies = (this.settings >> 5 & 1L) == 1L;
    if (allow != allowPerimeterActionsForAllies)
    {
      if (allow) {
        this.settings += 32L;
      } else {
        this.settings -= 32L;
      }
      update();
    }
  }
  
  boolean allowsFenceDestruction()
  {
    return (this.settings & 1L) == 1L;
  }
  
  boolean allowsRoadDestruction()
  {
    return (this.settings >> 1 & 1L) == 1L;
  }
  
  boolean allowsFenceBuilding()
  {
    return (this.settings >> 2 & 1L) == 1L;
  }
  
  boolean allowsRoadBuilding()
  {
    return (this.settings >> 3 & 1L) == 1L;
  }
  
  boolean allowsBuildings()
  {
    return (this.settings >> 4 & 1L) == 1L;
  }
  
  boolean allowsPerimeterActionsForAllies()
  {
    return (this.settings >> 5 & 1L) == 1L;
  }
  
  void deleteAllFriend()
    throws IOException
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("DELETE FROM PERIMETERFRIENDS WHERE ID=?");
      ps.setInt(1, this.villageId);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      throw new IOException(sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  boolean addFriend(String friendName)
    throws IOException
  {
    if (!this.perimeterFriends.contains(friendName))
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("INSERT INTO PERIMETERFRIENDS(ID,NAME) VALUES (?,?)");
        ps.setInt(1, this.villageId);
        ps.setString(2, friendName);
        
        ps.executeUpdate();
        this.perimeterFriends.add(friendName);
        return true;
      }
      catch (SQLException sqx)
      {
        throw new IOException(sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
    return false;
  }
  
  boolean deleteFriend(String friendName)
    throws IOException
  {
    if (this.perimeterFriends.contains(friendName))
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("DELETE FROM PERIMETERFRIENDS WHERE NAME=? AND ID=?");
        ps.setString(1, friendName);
        ps.setInt(2, this.villageId);
        ps.executeUpdate();
        this.perimeterFriends.remove(friendName);
        return true;
      }
      catch (SQLException sqx)
      {
        throw new IOException(sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
    return false;
  }
  
  boolean isFriend(String name)
  {
    return this.perimeterFriends.contains(name);
  }
  
  public String[] getFriends()
  {
    if (this.perimeterFriends.isEmpty()) {
      return emptyFriends;
    }
    return (String[])this.perimeterFriends.toArray(new String[this.perimeterFriends.size()]);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\villages\VillagePerimeter.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */