package com.wurmonline.server.utils;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.creatures.Creature;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreaturePositionDatabaseUpdater
  extends DatabaseUpdater<CreaturePositionDbUpdatable>
{
  private static final Logger logger = Logger.getLogger(CreaturePositionDatabaseUpdater.class.getName());
  private final Map<Long, CreaturePositionDbUpdatable> updatesMap = new ConcurrentHashMap();
  
  public CreaturePositionDatabaseUpdater(String aUpdaterDescription, int aMaxUpdatablesToRemovePerCycle)
  {
    super(aUpdaterDescription, CreaturePositionDbUpdatable.class, aMaxUpdatablesToRemovePerCycle);
    logger.info("Creating Creature Position Updater.");
  }
  
  public void addToQueue(CreaturePositionDbUpdatable updatable)
  {
    if (updatable != null)
    {
      if (logger.isLoggable(Level.FINEST)) {
        logger.finest("Adding to database " + updatable + " updatable queue: " + updatable);
      }
      CreaturePositionDbUpdatable waiting = (CreaturePositionDbUpdatable)this.updatesMap.get(Long.valueOf(updatable.getId()));
      if (waiting != null) {
        this.queue.remove(waiting);
      }
      this.updatesMap.put(Long.valueOf(updatable.getId()), updatable);
      this.queue.add(updatable);
    }
  }
  
  Connection getDatabaseConnection()
    throws SQLException
  {
    return DbConnector.getCreatureDbCon();
  }
  
  void addUpdatableToBatch(PreparedStatement updateStatement, CreaturePositionDbUpdatable aDbUpdatable)
    throws SQLException
  {
    this.updatesMap.remove(Long.valueOf(aDbUpdatable.getId()));
    updateStatement.setFloat(1, aDbUpdatable.getPositionX());
    updateStatement.setFloat(2, aDbUpdatable.getPositionY());
    updateStatement.setFloat(3, aDbUpdatable.getPositionZ());
    float rot = Creature.normalizeAngle(aDbUpdatable.getRotation());
    updateStatement.setFloat(4, rot);
    updateStatement.setInt(5, aDbUpdatable.getZoneid());
    updateStatement.setInt(6, aDbUpdatable.getLayer());
    updateStatement.setLong(7, aDbUpdatable.getBridgeId());
    updateStatement.setLong(8, aDbUpdatable.getId());
    updateStatement.addBatch();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\utils\CreaturePositionDatabaseUpdater.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */