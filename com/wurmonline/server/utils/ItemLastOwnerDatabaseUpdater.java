package com.wurmonline.server.utils;

import com.wurmonline.server.DbConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ItemLastOwnerDatabaseUpdater
  extends DatabaseUpdater<ItemLastOwnerDatabaseUpdatable>
{
  private static final Logger logger = Logger.getLogger(ItemOwnerDatabaseUpdater.class.getName());
  
  public ItemLastOwnerDatabaseUpdater(String aUpdaterDescription, int aMaxUpdatablesToRemovePerCycle)
  {
    super(aUpdaterDescription, ItemLastOwnerDatabaseUpdatable.class, aMaxUpdatablesToRemovePerCycle);
    logger.info("Creating Item Last Owner Updater.");
  }
  
  Connection getDatabaseConnection()
    throws SQLException
  {
    return DbConnector.getItemDbCon();
  }
  
  void addUpdatableToBatch(PreparedStatement updateStatement, ItemLastOwnerDatabaseUpdatable aDbUpdatable)
    throws SQLException
  {
    if (logger.isLoggable(Level.FINEST)) {
      logger.finest("Adding to batch: " + aDbUpdatable);
    }
    updateStatement.setLong(1, aDbUpdatable.getOwner());
    updateStatement.setLong(2, aDbUpdatable.getId());
    updateStatement.addBatch();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\utils\ItemLastOwnerDatabaseUpdater.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */