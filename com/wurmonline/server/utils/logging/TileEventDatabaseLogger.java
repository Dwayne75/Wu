package com.wurmonline.server.utils.logging;

import com.wurmonline.server.Constants;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TileEventDatabaseLogger
  extends DatabaseLogger<TileEvent>
{
  private static final Logger logger = Logger.getLogger(TileEventDatabaseLogger.class.getName());
  private int numsBatched = 0;
  private static final int pruneInterval = 10000;
  
  public TileEventDatabaseLogger(String aLoggerDescription, int aMaxLoggablesToRemovePerCycle)
  {
    super(aLoggerDescription, TileEvent.class, aMaxLoggablesToRemovePerCycle);
    logger.info("Creating Tile Event logger, System useTileLog option: " + Constants.useTileEventLog);
  }
  
  public void addLoggableToBatch(PreparedStatement logsStatement, TileEvent object)
    throws SQLException
  {
    TileEvent tileEvent = object;
    logsStatement.setInt(1, tileEvent.getTileX());
    logsStatement.setInt(2, tileEvent.getTileY());
    logsStatement.setInt(3, tileEvent.getLayer());
    logsStatement.setLong(4, tileEvent.getPerformer());
    logsStatement.setInt(5, tileEvent.getAction());
    logsStatement.setLong(6, tileEvent.getDate());
    logsStatement.addBatch();
    this.numsBatched += 1;
    checkPruneLimit();
  }
  
  private void checkPruneLimit()
  {
    if (this.numsBatched > 10000)
    {
      logger.log(Level.INFO, "Pruning entries");
      TileEvent.pruneLogEntries();
      this.numsBatched = 0;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\utils\logging\TileEventDatabaseLogger.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */