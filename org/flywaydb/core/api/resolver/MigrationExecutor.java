package org.flywaydb.core.api.resolver;

import java.sql.Connection;
import java.sql.SQLException;

public abstract interface MigrationExecutor
{
  public abstract void execute(Connection paramConnection)
    throws SQLException;
  
  public abstract boolean executeInTransaction();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\api\resolver\MigrationExecutor.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */