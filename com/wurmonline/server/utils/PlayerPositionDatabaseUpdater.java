package com.wurmonline.server.utils;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.players.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class PlayerPositionDatabaseUpdater
  extends DatabaseUpdater<PlayerPositionDbUpdatable>
{
  private static final Logger logger = Logger.getLogger(PlayerPositionDatabaseUpdater.class.getName());
  private final Map<Long, PlayerPositionDbUpdatable> updatesMap = new ConcurrentHashMap();
  
  public PlayerPositionDatabaseUpdater(String aUpdaterDescription, int aMaxUpdatablesToRemovePerCycle)
  {
    super(aUpdaterDescription, PlayerPositionDbUpdatable.class, aMaxUpdatablesToRemovePerCycle);
    logger.info("Creating Player Position Updater.");
  }
  
  Connection getDatabaseConnection()
    throws SQLException
  {
    return DbConnector.getPlayerDbCon();
  }
  
  public void addToQueue(PlayerPositionDbUpdatable updatable)
  {
    if (updatable != null)
    {
      PlayerPositionDbUpdatable waiting = (PlayerPositionDbUpdatable)this.updatesMap.get(Long.valueOf(updatable.getId()));
      if (waiting != null) {
        this.queue.remove(waiting);
      }
      this.updatesMap.put(Long.valueOf(updatable.getId()), updatable);
      this.queue.add(updatable);
    }
  }
  
  void addUpdatableToBatch(PreparedStatement updateStatement, PlayerPositionDbUpdatable aDbUpdatable)
    throws SQLException
  {
    this.updatesMap.remove(Long.valueOf(aDbUpdatable.getId()));
    updateStatement.setFloat(1, aDbUpdatable.getPositionX());
    updateStatement.setFloat(2, aDbUpdatable.getPositionY());
    updateStatement.setFloat(3, aDbUpdatable.getPositionZ());
    float rot = Player.normalizeAngle(aDbUpdatable.getRotation());
    updateStatement.setFloat(4, rot);
    updateStatement.setInt(5, aDbUpdatable.getZoneid());
    updateStatement.setInt(6, aDbUpdatable.getLayer());
    updateStatement.setLong(7, aDbUpdatable.getBridgeId());
    updateStatement.setLong(8, aDbUpdatable.getId());
    updateStatement.addBatch();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\utils\PlayerPositionDatabaseUpdater.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */