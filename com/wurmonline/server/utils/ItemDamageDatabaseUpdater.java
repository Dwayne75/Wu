package com.wurmonline.server.utils;

import com.wurmonline.server.DbConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ItemDamageDatabaseUpdater
  extends DatabaseUpdater<ItemDamageDatabaseUpdatable>
{
  private static final Logger logger = Logger.getLogger(ItemDamageDatabaseUpdater.class.getName());
  
  public ItemDamageDatabaseUpdater(String aUpdaterDescription, int aMaxUpdatablesToRemovePerCycle)
  {
    super(aUpdaterDescription, ItemDamageDatabaseUpdatable.class, aMaxUpdatablesToRemovePerCycle);
    logger.info("Creating Item Damage Updater.");
  }
  
  Connection getDatabaseConnection()
    throws SQLException
  {
    return DbConnector.getItemDbCon();
  }
  
  void addUpdatableToBatch(PreparedStatement updateStatement, ItemDamageDatabaseUpdatable aDbUpdatable)
    throws SQLException
  {
    if (logger.isLoggable(Level.FINEST)) {
      logger.finest("Adding to batch: " + aDbUpdatable);
    }
    updateStatement.setFloat(1, aDbUpdatable.getDamage());
    updateStatement.setLong(2, aDbUpdatable.getLastMaintained());
    updateStatement.setLong(3, aDbUpdatable.getId());
    updateStatement.addBatch();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\utils\ItemDamageDatabaseUpdater.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */