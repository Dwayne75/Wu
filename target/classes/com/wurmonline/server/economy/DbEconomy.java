package com.wurmonline.server.economy;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbEconomy
  extends Economy
{
  private static final Logger logger = Logger.getLogger(DbEconomy.class.getName());
  private static final String createEconomy = "insert into ECONOMY(ID, GOLDCOINS, SILVERCOINS, COPPERCOINS, IRONCOINS)values(?,?,?,?,?)";
  private static final String getEconomy = "SELECT * FROM ECONOMY WHERE ID=?";
  private static final String updateLastPolledTraders = "UPDATE ECONOMY SET LASTPOLLED=? WHERE ID=?";
  private static final String updateCreatedGold = "UPDATE ECONOMY SET GOLDCOINS=? WHERE ID=?";
  private static final String updateCreatedSilver = "UPDATE ECONOMY SET SILVERCOINS=? WHERE ID=?";
  private static final String updateCreatedCopper = "UPDATE ECONOMY SET COPPERCOINS=? WHERE ID=?";
  private static final String updateCreatedIron = "UPDATE ECONOMY SET IRONCOINS=? WHERE ID=?";
  private static final String logSoldItem = "INSERT INTO ITEMSSOLD (ITEMNAME,ITEMVALUE,TRADERNAME,PLAYERNAME, TEMPLATEID) VALUES(?,?,?,?,?)";
  private static final String getCoins = "SELECT * FROM COINS WHERE TEMPLATEID=? AND OWNERID=-10 AND PARENTID=-10 AND ZONEID=-10 AND BANKED=1 AND MAILED=0";
  private static final String getSupplyDemand = "SELECT * FROM SUPPLYDEMAND";
  private static final String getTraderMoney = "SELECT * FROM TRADER";
  private static final String createTransaction = "INSERT INTO TRANSACTS (ITEMID, OLDOWNERID,NEWOWNERID,REASON, VALUE) VALUES (?,?,?,?,?)";
  
  DbEconomy(int serverNumber)
    throws IOException
  {
    super(serverNumber);
  }
  
  void initialize()
    throws IOException
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getEconomyDbCon();
      if (exists(dbcon))
      {
        load();
      }
      else
      {
        ps = dbcon.prepareStatement("insert into ECONOMY(ID, GOLDCOINS, SILVERCOINS, COPPERCOINS, IRONCOINS)values(?,?,?,?,?)");
        ps.setInt(1, this.id);
        ps.setLong(2, goldCoins);
        ps.setLong(3, silverCoins);
        ps.setLong(4, copperCoins);
        ps.setLong(5, ironCoins);
        ps.executeUpdate();
      }
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
    loadSupplyDemand();
    loadShopMoney();
  }
  
  private void load()
    throws IOException
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getEconomyDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM ECONOMY WHERE ID=?");
      ps.setInt(1, this.id);
      rs = ps.executeQuery();
      if (rs.next())
      {
        goldCoins = rs.getLong("GOLDCOINS");
        silverCoins = rs.getLong("SILVERCOINS");
        copperCoins = rs.getLong("COPPERCOINS");
        ironCoins = rs.getLong("IRONCOINS");
        lastPolledTraders = rs.getLong("LASTPOLLED");
      }
      DbUtilities.closeDatabaseObjects(ps, rs);
      Change change = new Change(ironCoins + copperCoins * 100L + silverCoins * 10000L + goldCoins * 1000000L);
      if (lastPolledTraders <= 0L) {
        lastPolledTraders = System.currentTimeMillis() - 2419200000L;
      }
      updateCreatedIron(change.getIronCoins());
      logger.log(Level.INFO, "Iron=" + ironCoins);
      updateCreatedCopper(change.getCopperCoins());
      logger.log(Level.INFO, "Copper=" + copperCoins);
      updateCreatedSilver(change.getSilverCoins());
      logger.log(Level.INFO, "Silver=" + silverCoins);
      updateCreatedGold(change.getGoldCoins());
      logger.log(Level.INFO, "Gold=" + goldCoins);
      loadCoins(50);
      loadCoins(54);
      loadCoins(58);
      loadCoins(53);
      loadCoins(57);
      loadCoins(61);
      loadCoins(51);
      loadCoins(55);
      loadCoins(59);
      loadCoins(52);
      loadCoins(56);
      loadCoins(60);
    }
    catch (SQLException sqx)
    {
      throw new IOException(sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private boolean exists(Connection dbcon)
    throws SQLException
  {
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      ps = dbcon.prepareStatement("SELECT * FROM ECONOMY WHERE ID=?");
      ps.setInt(1, this.id);
      rs = ps.executeQuery();
      return rs.next();
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
    }
  }
  
  public void transaction(long itemId, long oldownerid, long newownerid, String newReason, long value)
  {
    if (DbConnector.isUseSqlite()) {
      return;
    }
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      String reason = newReason.substring(0, Math.min(19, newReason.length()));
      dbcon = DbConnector.getEconomyDbCon();
      ps = dbcon.prepareStatement("INSERT INTO TRANSACTS (ITEMID, OLDOWNERID,NEWOWNERID,REASON, VALUE) VALUES (?,?,?,?,?)");
      ps.setLong(1, itemId);
      ps.setLong(2, oldownerid);
      ps.setLong(3, newownerid);
      ps.setString(4, reason);
      ps.setLong(5, value);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to create transaction for itemId: " + itemId + " due to " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public void updateCreatedGold(long number)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      goldCoins = number;
      dbcon = DbConnector.getEconomyDbCon();
      ps = dbcon.prepareStatement("UPDATE ECONOMY SET GOLDCOINS=? WHERE ID=?");
      ps.setLong(1, goldCoins);
      ps.setInt(2, this.id);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to update num gold: " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public void updateLastPolled()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      lastPolledTraders = System.currentTimeMillis();
      dbcon = DbConnector.getEconomyDbCon();
      ps = dbcon.prepareStatement("UPDATE ECONOMY SET LASTPOLLED=? WHERE ID=?");
      ps.setLong(1, lastPolledTraders);
      ps.setInt(2, this.id);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to update last polled traders: " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public void updateCreatedSilver(long number)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      silverCoins = number;
      dbcon = DbConnector.getEconomyDbCon();
      ps = dbcon.prepareStatement("UPDATE ECONOMY SET SILVERCOINS=? WHERE ID=?");
      ps.setLong(1, silverCoins);
      ps.setInt(2, this.id);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to update num silver: " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public void updateCreatedCopper(long number)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      copperCoins = number;
      dbcon = DbConnector.getEconomyDbCon();
      ps = dbcon.prepareStatement("UPDATE ECONOMY SET COPPERCOINS=? WHERE ID=?");
      ps.setLong(1, copperCoins);
      ps.setInt(2, this.id);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to update num copper: " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public void updateCreatedIron(long number)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      ironCoins = number;
      dbcon = DbConnector.getEconomyDbCon();
      ps = dbcon.prepareStatement("UPDATE ECONOMY SET IRONCOINS=? WHERE ID=?");
      ps.setLong(1, ironCoins);
      ps.setInt(2, this.id);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to update num iron: " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private void loadCoins(int type)
  {
    List<Item> current = getListForCointype(type);
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getItemDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM COINS WHERE TEMPLATEID=? AND OWNERID=-10 AND PARENTID=-10 AND ZONEID=-10 AND BANKED=1 AND MAILED=0");
      ps.setInt(1, type);
      rs = ps.executeQuery();
      while (rs.next()) {
        try
        {
          Item toAdd = Items.getItem(rs.getLong("WURMID"));
          current.add(toAdd);
        }
        catch (NoSuchItemException nsi)
        {
          logger.log(Level.WARNING, "Failed to load coin: " + rs.getLong("WURMID"), nsi);
        }
      }
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to load coins: " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public Shop createShop(long wurmid)
  {
    return new DbShop(wurmid, Servers.localServer.getInitialTraderIrons());
  }
  
  public Shop createShop(long wurmid, long ownerid)
  {
    int coins = 0;
    if (ownerid == -10L) {
      coins = Servers.localServer.getInitialTraderIrons();
    }
    return new DbShop(wurmid, coins, ownerid);
  }
  
  SupplyDemand createSupplyDemand(int aId)
  {
    return new DbSupplyDemand(aId, 1000, 1000);
  }
  
  void loadSupplyDemand()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getEconomyDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM SUPPLYDEMAND");
      rs = ps.executeQuery();
      while (rs.next()) {
        new DbSupplyDemand(rs.getInt("ID"), rs.getInt("ITEMSBOUGHT"), rs.getInt("ITEMSSOLD"), rs.getLong("LASTPOLLED"));
      }
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to load supplyDemand: " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  void loadShopMoney()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getEconomyDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM TRADER");
      rs = ps.executeQuery();
      while (rs.next()) {
        new DbShop(rs.getLong("WURMID"), rs.getLong("MONEY"), rs.getLong("OWNER"), rs.getFloat("PRICEMODIFIER"), rs.getBoolean("FOLLOWGLOBALPRICE"), rs.getBoolean("USELOCALPRICE"), rs.getLong("LASTPOLLED"), rs.getFloat("TAX"), rs.getLong("SPENT"), rs.getLong("SPENTLIFE"), rs.getLong("EARNED"), rs.getLong("EARNEDLIFE"), rs.getLong("SPENTLASTMONTH"), rs.getLong("TAXPAID"), rs.getInt("NUMBEROFITEMS"), rs.getLong("WHENEMPTY"), true);
      }
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to load traderMoney: " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public void addItemSoldByTraders(String name, long money, String traderName, String playerName, int templateId)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getEconomyDbCon();
      ps = dbcon.prepareStatement("INSERT INTO ITEMSSOLD (ITEMNAME,ITEMVALUE,TRADERNAME,PLAYERNAME, TEMPLATEID) VALUES(?,?,?,?,?)");
      ps.setString(1, name.substring(0, Math.min(29, name.length())));
      ps.setLong(2, money);
      ps.setString(3, traderName.substring(0, Math.min(29, traderName.length())));
      ps.setString(4, playerName.substring(0, Math.min(29, playerName.length())));
      ps.setInt(5, templateId);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Failed to update num iron: " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\economy\DbEconomy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */