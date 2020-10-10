package com.wurmonline.server.players;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class DbSearcher
{
  private static final String getPlayerName = "select * from PLAYERS where WURMID=?";
  private static final String getPlayerId = "select * from PLAYERS where NAME=?";
  
  public static String getNameForPlayer(long wurmId)
    throws IOException, NoSuchPlayerException
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("select * from PLAYERS where WURMID=?");
      ps.setLong(1, wurmId);
      rs = ps.executeQuery();
      if (rs.next())
      {
        String name = rs.getString("NAME");
        return name;
      }
      throw new NoSuchPlayerException("No player with id " + wurmId);
    }
    catch (SQLException sqx)
    {
      throw new IOException("Problem finding Player ID " + wurmId, sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public static long getWurmIdForPlayer(String name)
    throws IOException, NoSuchPlayerException
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("select * from PLAYERS where NAME=?");
      ps.setString(1, name);
      rs = ps.executeQuery();
      if (rs.next())
      {
        long id = rs.getLong("WURMID");
        return id;
      }
      throw new NoSuchPlayerException("No player with name " + name);
    }
    catch (SQLException sqx)
    {
      throw new IOException("Problem finding Player name " + name, sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\players\DbSearcher.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */